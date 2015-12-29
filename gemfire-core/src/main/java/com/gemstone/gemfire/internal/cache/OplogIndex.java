/*
 * Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */

package com.gemstone.gemfire.internal.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import com.gemstone.gemfire.CancelCriterion;
import com.gemstone.gemfire.InternalGemFireError;
import com.gemstone.gemfire.cache.DiskAccessException;
import com.gemstone.gemfire.i18n.LogWriterI18n;
import com.gemstone.gemfire.internal.Assert;
import com.gemstone.gemfire.internal.InternalDataSerializer;
import com.gemstone.gemfire.internal.cache.Oplog.DiskRegionInfo;
import com.gemstone.gemfire.internal.cache.Oplog.KRFEntry;
import com.gemstone.gemfire.internal.cache.Oplog.OplogEntryIdMap;
import com.gemstone.gemfire.internal.cache.control.MemoryThresholdListener;
import com.gemstone.gemfire.internal.cache.persistence.DiskRegionView;
import com.gemstone.gemfire.internal.i18n.LocalizedStrings;
import com.gemstone.gemfire.internal.offheap.SimpleMemoryAllocatorImpl.Chunk;
import com.gemstone.gemfire.internal.offheap.annotations.Released;
import com.gemstone.gemfire.internal.shared.unsafe.ChannelBufferUnsafeDataInputStream;
import com.gemstone.gemfire.internal.shared.unsafe.ChannelBufferUnsafeDataOutputStream;
import com.gemstone.gnu.trove.THashMap;
import com.gemstone.gnu.trove.THashSet;
import com.gemstone.gnu.trove.TLongArrayList;
import com.gemstone.gnu.trove.TObjectObjectProcedure;

/**
 * Encapsulates methods to read and write from index files in an Oplog.
 * 
 * @author kneeraj, swale
 * @since gfxd 1.0
 */
public final class OplogIndex {

  static final String IDX_FILE_EXT = ".idxkrf";

  // for index file records
  public static final byte INDEXID_RECORD     = 0x01;
  public static final byte INDEX_RECORD       = 0x02;
  public static final byte INDEX_END_OF_FILE  = 0x03;

  // randomly generated bytes to mark the valid end of an index file.
  public static final byte[] INDEX_END_OF_FILE_MAGIC = new byte[] {
      INDEX_END_OF_FILE, -0x37, -0x11, -0x26, -0x46, 0x25, 0x71, 0x3b, 0x1f,
      0x4b, -0x77, 0x2b, -0x6f, -0x1f, 0x6b, -0x02 };

  /**
   * The max entries batched in memory before flusing to index file. Batching is
   * primarily done to determine any multiple entries against the same index key
   * to optimize its serialization by avoiding writing the same index key
   * multiple times.
   */
  public static final int BATCH_FLUSH_SIZE_AT_ROLLOVER = 20000;

  private final Oplog oplog;

  private final DiskStoreImpl dsi;

  private File irf;

  private FileOutputStream fos;
  private ChannelBufferUnsafeDataOutputStream dos;

  OplogIndex(Oplog oplog) {
    this.oplog = oplog;
    this.dsi = oplog.getParent();
  }

  public void addRecoveredFile(String fname) {
    if (this.irf == null) {
      this.irf = new File(oplog.getDirectoryHolder().getDir(), fname);
    } else {
      // If, for some strange reason, we end up with two idx files
      // pick the one with the higher version.
      long currentVersion = getIndexFileVersion(this.irf.getName());
      long incomingVersion = getIndexFileVersion(fname);

      if (incomingVersion > currentVersion) {
        this.irf = new File(oplog.getDirectoryHolder().getDir(), fname);
      }
    }
  }

  /**
   * Extract the version of the current index file from the name.
   */
  private long getIndexFileVersion(String indxFile) {
    Matcher matcher = Oplog.IDX_PATTERN.matcher(indxFile);
    if (!matcher.matches()) {
      throw new InternalGemFireError(
          "Could not match index file pattern against " + indxFile);
    }
    String versionStr = matcher.group(1);
    return Long.parseLong(versionStr);
  }

  private File getFileName(long version) {
    return new File(oplog.getDirectoryHolder().getDir(),
        oplog.getDiskFileName() + "." + version + IDX_FILE_EXT);
  }

  public synchronized File getIndexFile() {
    return this.irf;
  }

  public synchronized File getIndexFileIfValid() {
    File f = getIndexFile();
    return checkValidIndexFile(f) ? f : null;
  }

  synchronized void initializeForWriting(boolean truncate)
      throws IOException {
    // this method is only used by offline compaction. validating will not
    // create krf
    assert !this.dsi.isValidating();

    // Create the idx file with a version of 1, or roll the version by
    // renaming a new file. This allows the incremental backup to detect
    // that the file contents have changed.
    if (this.irf == null) {
      this.irf = getFileName(1);
    } else {
      long version = getIndexFileVersion(this.irf.getName());
      version++;
      File newFile = getFileName(version);
      if (!this.irf.renameTo(newFile)) {
        throw new DiskAccessException("Failed to rename index file " +
            this.irf + " to " + newFile, this.dsi);
      }
      this.irf = newFile;
    }

    final boolean append = checkValidIndexFile(this.irf);
    FileOutputStream fos = new FileOutputStream(this.irf, append);
    // position before EOF indicator if append is true
    if (truncate) {
      FileChannel channel = fos.getChannel();
      channel.truncate(0);
      channel.position(0);
    } else if (append) {
      FileChannel channel = fos.getChannel();
      channel.truncate(channel.size() - INDEX_END_OF_FILE_MAGIC.length);
      channel.position(channel.size());
    }
    ChannelBufferUnsafeDataOutputStream dos =
        new ChannelBufferUnsafeDataOutputStream(
            fos.getChannel(), Oplog.DEFAULT_BUFFER_SIZE);

    this.fos = fos;
    this.dos = dos;
  }

  boolean checkValidIndexFile(File f) {
    if (f != null && f.exists()) {
      boolean hasIrf = this.dsi.getDiskInitFile()
          .hasIrf(this.oplog.getOplogId());
      if (hasIrf) {
        try {
          // check if the file is closed properly
          try (FileInputStream fis = new FileInputStream(f)) {
            FileChannel channel = fis.getChannel();
            long size = channel.size();
            // The file should end with the end of file magic.
            if (size >= INDEX_END_OF_FILE_MAGIC.length) {
              channel.position(size - INDEX_END_OF_FILE_MAGIC.length);
              byte[] data = new byte[INDEX_END_OF_FILE_MAGIC.length];
              if (fis.read(data) == INDEX_END_OF_FILE_MAGIC.length &&
                  Arrays.equals(data, INDEX_END_OF_FILE_MAGIC)) {
                return true;
              }
            }
          }
        } catch (IOException ioe) {
          // ignore and continue to deleting the file
        }
      }
      // delete the existing, unreadable file
      deleteIRF(hasIrf ? "unreadable file" : "metadata missing");
    }
    this.oplog.indexesWritten.clear();
    return false;
  }

  final synchronized void deleteIRF(String reason) {
    DiskInitFile initFile = this.dsi.getDiskInitFile();
    if (initFile.hasIrf(this.oplog.getOplogId())) {
      // add deleted IRF record
      initFile.irfDelete(this.oplog.getOplogId());
    }
    if (this.irf != null && this.irf.exists()) {
      if (reason != null) {
        final LogWriterI18n logger = this.oplog.logger;
        if (logger.infoEnabled()) {
          logger.info(LocalizedStrings.Oplog_DELETE_0_1_2,
              new Object[] { this.oplog.toString(),
                  this.irf.getAbsolutePath() + '(' + reason + ')',
                  this.dsi.getName() });
        }
      }
      if (!this.irf.delete()) {
        final LogWriterI18n logger = this.oplog.logger;
        logger.warning(LocalizedStrings.Oplog_DELETE_FAIL_0_1_2,
            new Object[] { this.oplog.toString(), this.irf.getAbsolutePath(),
                this.dsi.getName() });
      }
    }
  }

  public void close() {
    boolean allClosed = false;
    try {
      if (this.dos == null) {
        return;
      }

      this.dos.write(INDEX_END_OF_FILE_MAGIC);
      this.dos.flush();
      this.dos.close();
      this.dos = null;
      this.fos.close();
      this.fos = null;

      allClosed = true;
    } catch (IOException e) {
      throw new DiskAccessException("Failed to close index file " + this.irf,
          e, this.dsi);
    } finally {
      if (!allClosed) {
        // IOException happened during close, delete this idxkrf
        deleteIRF("failed to close");
      }
    }
  }

  public static final class IndexData {
    public final SortedIndexContainer index;
    public final SortedIndexRecoveryJob indexJob;
    public final THashMapWithCreate indexEntryMap;

    public final int action;

    // flags for "action" to indicate whether an index has to be only dumped to
    // file, or only loaded or both dumped to file as well as loaded
    public static final int ONLY_DUMP = 0x1;

    public static final int ONLY_LOAD = 0x2;

    public static final int BOTH_DUMP_AND_LOAD = 0x3;

    IndexData(final SortedIndexContainer index,
        final SortedIndexRecoveryJob indexJob, int action,
        int entryCacheSize) {
      this.index = index;
      this.indexJob = indexJob;
      this.indexEntryMap = (action != ONLY_LOAD
          ? new THashMapWithCreate(entryCacheSize) : null);
      this.action = action;
    }

    @Override
    public int hashCode() {
      return this.index.hashCode();
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof IndexData &&
          this.index.equals(((IndexData)other).index);
    }
  }

  public void writeIndexRecords(List<KRFEntry> entries,
      Set<KRFEntry> notWrittenKRFs, Set<SortedIndexContainer> dumpIndexes,
      Map<SortedIndexContainer, SortedIndexRecoveryJob> loadIndexes) {
    if ((dumpIndexes == null || dumpIndexes.isEmpty())
        && loadIndexes == null) {
      return;
    }
    final LogWriterI18n logger = this.oplog.logger;
    if (DiskStoreImpl.INDEX_LOAD_DEBUG) {
      logger.info(LocalizedStrings.DEBUG, "OplogIndex#writeIndexRecords: "
          + "local indexes to be dumped are: " + dumpIndexes
          + ", to be loaded are: " + loadIndexes);
    }

    if (logger.infoEnabled()) {
      logger.info(LocalizedStrings.Oplog_CREATE_0_1_2, new Object[] {
          this.oplog.toString(), this.irf.getAbsolutePath(),
          this.dsi.getName() });
    }

    final MemoryThresholdListener thresholdListener = GemFireCacheImpl
        .getInternalProductCallbacks().getMemoryThresholdListener();
    final int entryCacheSize = Math.min(BATCH_FLUSH_SIZE_AT_ROLLOVER,
        entries.size());
    final HashMap<Long, IndexData[]> drvIdToIndexes = new HashMap<>();

    if (notWrittenKRFs != null && notWrittenKRFs.isEmpty()) {
      notWrittenKRFs = null;
    }

    // Populate the drvIdToIndexes from oplog map first.
    // We ensure that the map referred to in indexToIndexData for each index
    // is also the one which is in drdIdToIndexes so insert can happen into
    // drdIdToIndexes while flushing can be done from the global
    // indexToIndexData map.
    final boolean hasOffHeap = getDiskIdToIndexDataMap(dumpIndexes,
        loadIndexes, entryCacheSize, drvIdToIndexes, null);

    final THashMapWithCreate.ValueCreator entryListCreator =
        new THashMapWithCreate.ValueCreator() {
      @Override
      public Object create(Object key, Object params) {
        if (hasOffHeap) {
          // Snapshot the key bytes, as the offheap value bytes used as index
          // key would be
          // released , before the data is dumped in the irf.
          // Since a newTLongArrayList is created, implying this index key will
          // be used in the dumping code
          // Check if snap shot is needed in case of only load
          ((SortedIndexKey)key).snapshotKeyFromValue();         
        }
        return new TLongArrayList(2);
      }
    };
    if (DiskStoreImpl.INDEX_LOAD_DEBUG) {
      logger.info(LocalizedStrings.DEBUG, "OplogIndex#writeIndexRecords: "
          + "affected indexes to be dumped are: " + dumpIndexes);
      logger.info(LocalizedStrings.DEBUG, "OplogIndex#writeIndexRecords: "
          + "affected indexes to be loaded are: " + loadIndexes);
    }
    int processedCnt = 0;
    for (KRFEntry krf : entries) {
      if (notWrittenKRFs != null && notWrittenKRFs.contains(krf)) {
        continue;
      }
      final DiskRegionView drv = krf.getDiskRegionView();
      final IndexData[] indexes = drvIdToIndexes.get(drv.getId());
      if (indexes == null) {
        continue;
      }
      final LocalRegion baseRegion = indexes[0].index.getBaseRegion();
      final DiskEntry entry = krf.getDiskEntry();
      @Released final Object val = DiskEntry.Helper
          .getValueOffHeapOrDiskWithoutFaultIn(entry, drv, baseRegion);
      if (val == null || Token.isInvalidOrRemoved(val)) {
        if (DiskStoreImpl.INDEX_LOAD_DEBUG) {
          logger.info(LocalizedStrings.DEBUG, "OplogIndex#writeIndexRecords: "
              + "row null for entry: " + entry + "; continuing to next.");
        }
        continue;
      }
      if (!hasOffHeap || !(val instanceof Chunk)) {
        for (IndexData indexData : indexes) {
          dumpOrLoadIndex(indexData, val, entry, entryListCreator);
        }
      } else {
        try {
          for (IndexData indexData : indexes) {
            dumpOrLoadIndex(indexData, val, entry, entryListCreator);
          }
        } finally {
          ((Chunk)val).release();
        }
      }
      processedCnt += indexes.length;
      if (processedCnt >= BATCH_FLUSH_SIZE_AT_ROLLOVER
          || thresholdListener.isEviction()) {
        flushEntries(drvIdToIndexes.values());
        processedCnt = 0;
      }
    }
    if (processedCnt > 0) {
      flushEntries(drvIdToIndexes.values());
    }
  }

  private void dumpOrLoadIndex(final IndexData indexData, final Object val,
      DiskEntry entry, final THashMapWithCreate.ValueCreator entryListCreator) {
    final SortedIndexContainer index = indexData.index;
    SortedIndexKey ikey = index.getIndexKey(val, entry);
    switch (indexData.action) {
      case IndexData.ONLY_LOAD:
        // submit insert into the index immediately
        indexData.indexJob.addJob(ikey, entry);
        break;
      case IndexData.BOTH_DUMP_AND_LOAD:
        // submit insert into the index immediately
        indexData.indexJob.addJob(ikey, entry);
        // fall-through deliberate
      case IndexData.ONLY_DUMP:
        THashMapWithCreate entryIdsPerIndexKey = indexData.indexEntryMap;
        TLongArrayList entryList = (TLongArrayList)entryIdsPerIndexKey
            .create(ikey, entryListCreator, null);
        entryList.add(Math.abs(entry.getDiskId().getKeyId()));
        break;
      default:
        Assert.fail("OplogIndex#writeIndexRecords: unexpected action="
            + indexData.action);
    }
  }

  private void flushEntries(Collection<IndexData[]> allIndexes) {
    for (IndexData[] indexes : allIndexes) {
      for (IndexData indexData : indexes) {
        SortedIndexContainer index = indexData.index;
        THashMapWithCreate entryIdsPerIndexKey = indexData.indexEntryMap;
        if (entryIdsPerIndexKey != null && entryIdsPerIndexKey.size() > 0) {
          writeIRFRecords(index, entryIdsPerIndexKey, dos);
          entryIdsPerIndexKey.clear();
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<String, THashSet> getRegionIdToIndexes(
      Set<SortedIndexContainer> dumpIndexes,
      Map<SortedIndexContainer, SortedIndexRecoveryJob> loadIndexes,
      int entryCacheSize) {
    final Map<String, THashSet> regionIdToIndexData = new THashMap();
    if (dumpIndexes != null) {
      for (SortedIndexContainer index : dumpIndexes) {
        String regionId = index.getBaseRegion().getRegionID();
        THashSet indexData = regionIdToIndexData.get(regionId);
        if (indexData == null) {
          indexData = new THashSet(4);
          regionIdToIndexData.put(regionId, indexData);
        }
        IndexData data;
        SortedIndexRecoveryJob indexJob = loadIndexes != null
            ? loadIndexes.get(index) : null;
        if (indexJob != null) {
          data = new IndexData(index, indexJob, IndexData.BOTH_DUMP_AND_LOAD,
              entryCacheSize);
        } else {
          data = new IndexData(index, null, IndexData.ONLY_DUMP,
              entryCacheSize);
        }
        indexData.add(data);
      }
    }
    if (loadIndexes != null) {
      for (Map.Entry<SortedIndexContainer, SortedIndexRecoveryJob> entry :
          loadIndexes.entrySet()) {
        SortedIndexContainer index = entry.getKey();
        String regionId = index.getBaseRegion().getRegionID();
        THashSet indexData = regionIdToIndexData.get(regionId);
        if (indexData == null) {
          indexData = new THashSet(4);
          regionIdToIndexData.put(regionId, indexData);
        }
        // we can safely set ONLY_LOAD here since if there is already an entry
        // then it will already have BOTH_DUMP_AND_LOAD as per the loop above
        IndexData data = new IndexData(index, entry.getValue(),
            IndexData.ONLY_LOAD, entryCacheSize);
        indexData.add(data);
      }
    }
    return regionIdToIndexData;
  }

  public boolean getDiskIdToIndexDataMap(Set<SortedIndexContainer> dumpIndexes,
      Map<SortedIndexContainer, SortedIndexRecoveryJob> loadIndexes,
      int entryCacheSize, final Map<Long, IndexData[]> drvIdToIndexes,
      final List<DiskRegionInfo> targetRegions) {
    boolean hasOffHeap = false;
    Collection<DiskRegionInfo> recoveredRegions = this.oplog
        .getRegionRecoveryMap();
    Map<String, THashSet> regionIdToIndexes = getRegionIdToIndexes(
        dumpIndexes, loadIndexes, entryCacheSize);
    for (DiskRegionInfo regionInfo : recoveredRegions) {
      DiskRegionView drv = regionInfo.getDiskRegion();
      Long drvId = drv.getId();
      String baseRegionID = Oplog.getParentRegionID(drv);
      THashSet indexSet = regionIdToIndexes.get(baseRegionID);
      if (indexSet == null) {
        continue;
      }
      if (drvIdToIndexes != null) {
        IndexData[] indexes = new IndexData[indexSet.size()];
        indexSet.toArray(indexes);
        drvIdToIndexes.put(drvId, indexes);
      }
      if (targetRegions != null) {
        targetRegions.add(regionInfo);
      }
      IndexData firstKey;
      if (!hasOffHeap && (firstKey = (IndexData)indexSet.firstKey()) != null
          && firstKey.index.getBaseRegion().getEnableOffHeapMemory()) {
        hasOffHeap = true;
      }
    }
    return hasOffHeap;
  }

  public void writeIRFRecords(final SortedIndexContainer indexContainer,
      THashMap entryIdsPerIndexKey,
      final ChannelBufferUnsafeDataOutputStream dos) {
    try {
      final LogWriterI18n logger = this.oplog.logger;
      if (DiskStoreImpl.INDEX_LOAD_DEBUG) {
        logger.info(LocalizedStrings.DEBUG, "OplogIndex#writeIRFRecords: "
            + "write called for " + indexContainer);
      }
      String indexId = indexContainer.getUUID();
      dos.writeByte(INDEXID_RECORD);
      InternalDataSerializer.writeString(indexId, dos);
      if (DiskStoreImpl.INDEX_LOAD_DEBUG) {
        logger.info(LocalizedStrings.DEBUG, "OplogIndex#writeIRFRecords: "
            + "written indexId record for index: " + indexId);
      }
      entryIdsPerIndexKey.forEachEntry(new TObjectObjectProcedure() {
        @Override
        public boolean execute(Object key, Object value) {
          try {
            dos.writeByte(INDEX_RECORD);
            SortedIndexKey ikey = (SortedIndexKey)key;
            ikey.writeKeyBytes(dos);
            TLongArrayList entryKeyIds = (TLongArrayList)value;
            int numKeyIds = entryKeyIds.size();
            assert numKeyIds > 0;
            InternalDataSerializer.writeUnsignedVL(numKeyIds, dos);
            if (DiskStoreImpl.INDEX_LOAD_DEBUG_FINER) {
              logger.info(LocalizedStrings.DEBUG, "OplogIndex#writeIRFRecords: "
                  + "writing actual index record with index key: " + key
                  + " list of oplogEntryIds: " + entryKeyIds.toString());
            }
            if (numKeyIds == 1) {
              InternalDataSerializer.writeUnsignedVL(entryKeyIds.getQuick(0),
                  dos);
            } else {
              // sort the key ids to keep the deltas small and thus minimize the
              // size of unsigned long that will be written to disk
              entryKeyIds.sort();
              long previousValue = 0;
              for (int index = 0; index < numKeyIds; index++) {
                long currValue = entryKeyIds.getQuick(index);
                if (previousValue == 0) {
                  previousValue = currValue;
                  InternalDataSerializer.writeUnsignedVL(previousValue, dos);
                } else {
                  long delta = currValue - previousValue;
                  InternalDataSerializer.writeUnsignedVL(delta, dos);
                  previousValue = currValue;
                }
              }
            }
          } catch (IOException ioe) {
            throw new DiskAccessException(ioe);
          }
          return true;
        }
      });
    } catch (IOException ioe) {
      throw new DiskAccessException(ioe);
    }
  }

  public void recoverIndexes(
      Map<SortedIndexContainer, SortedIndexRecoveryJob> indexes) {
    try {
      final LogWriterI18n logger = this.oplog.logger;
      final boolean logEnabled = DiskStoreImpl.INDEX_LOAD_DEBUG
          || logger.fineEnabled();
      final boolean logFinerEnabled = DiskStoreImpl.INDEX_LOAD_DEBUG_FINER
          || logger.finerEnabled();
      final CancelCriterion cc = this.dsi.getCancelCriterion();

      // check early for stop
      cc.checkCancelInProgress(null);

      if (logEnabled || DiskStoreImpl.INDEX_LOAD_PERF_DEBUG) {
        logger.info(LocalizedStrings.DEBUG, "OplogIndex#recoverIndexes: for "
            + this.oplog + " processing file: " + this.irf + " of size: "
            + this.irf.length());
      }
      if (logger.infoEnabled()) {
        logger.info(LocalizedStrings.DiskRegion_RECOVERING_OPLOG_0_1_2,
            new Object[] { this.oplog.toString(), this.irf.getAbsolutePath(),
                dsi.getName() });
      }

      final RandomAccessFile raf = new RandomAccessFile(this.irf, "r");
      final FileChannel channel = raf.getChannel();
      final ChannelBufferUnsafeDataInputStream in =
          new ChannelBufferUnsafeDataInputStream(channel,
              Oplog.LARGE_BUFFER_SIZE);
      final OplogEntryIdMap recoveryMap = this.oplog.getInitRecoveryMap();
      final HashMap<String, IndexData> indexMap = new HashMap<>();

      boolean endOfFile = false;

      String currentIndexID;
      SortedIndexContainer currentIndex = null;
      SortedIndexRecoveryJob currentIndexJob = null;

      for (Map.Entry<SortedIndexContainer, SortedIndexRecoveryJob> entry :
          indexes.entrySet()) {
        SortedIndexContainer index = entry.getKey();
        indexMap.put(index.getUUID(), new IndexData(index, entry.getValue(),
            IndexData.ONLY_LOAD, 0));
      }

      while (!endOfFile) {
        final int opCode = in.read();
        switch (opCode) {
          case INDEX_END_OF_FILE:
            if (logEnabled) {
              logger.info(LocalizedStrings.DEBUG, "OplogIndex#recoverIndexes: "
                  + "read end 0xf file record for " + this.irf);
            }
            byte[] data = new byte[INDEX_END_OF_FILE_MAGIC.length];
            data[0] = INDEX_END_OF_FILE;
            in.readFully(data, 1, INDEX_END_OF_FILE_MAGIC.length - 1);
            if (!Arrays.equals(data, INDEX_END_OF_FILE_MAGIC)) {
              throw new DiskAccessException(
                  "Did not find end of file magic at the end of index "
                      + this.irf, oplog.getParent());
            }
            break;

          case INDEXID_RECORD:
            currentIndexID = InternalDataSerializer.readString(in);
            IndexData currentIndexData = indexMap.get(currentIndexID);
            if (currentIndexData != null) {
              currentIndex = currentIndexData.index;
              currentIndexJob = currentIndexData.indexJob;
            } else {
              currentIndex = null;
              currentIndexJob = null;
            }
            if (logFinerEnabled) {
              if (currentIndex != null) {
                logger.info(LocalizedStrings.DEBUG, String.format("OplogIndex#"
                    + "recoverIndexes: indexContainer=%s, indexUUID=%s",
                    currentIndex, currentIndexID));
              } else {
                logger.info(LocalizedStrings.DEBUG, "OplogIndex#"
                    + "recoverIndexes: index is null for indexUUID="
                    + currentIndexID);
              }
            }
            break;

          case INDEX_RECORD:
            byte[] indexKeyBytes = InternalDataSerializer.readByteArray(in);
            int numRegionKeys = (int)InternalDataSerializer
                .readUnsignedVL(in);
            long regionEntryKeyId = 0;
            for (int i = 0; i < numRegionKeys; i++) {
              if (i == 0) {
                regionEntryKeyId = InternalDataSerializer.readUnsignedVL(in);
              } else {
                regionEntryKeyId = regionEntryKeyId
                    + InternalDataSerializer.readUnsignedVL(in);
              }
              if (currentIndex == null) {
                continue;
              }

              // check if this region key is in current live list
              Object entry = recoveryMap.get(regionEntryKeyId);
              if (entry == null) {
                // This is possible if it has been deleted. So just
                // continue.
                if (logEnabled) {
                  logger.info(LocalizedStrings.DEBUG, String.format(
                      "OplogIndex#recoverIndexes: ignoring oplogentryid=%s "
                          + "as not found in kvMap for index=%s ",
                          regionEntryKeyId, currentIndex));
                }
                continue;
              }
              final DiskEntry diskEntry = (DiskEntry)entry;
              final DiskId diskId = diskEntry.getDiskId();
              if (diskId != null
                  && diskId.getOplogId() == this.oplog.oplogId) {
                if (logEnabled) {
                  logger.info(LocalizedStrings.DEBUG, String.format(
                      "OplogIndex#recoverIndexes: adding index diskEntry=%s "
                          + "indexContainer=%s indexKeyBytes=%s",
                          diskEntry, currentIndex,
                          Arrays.toString(indexKeyBytes)));
                }

                currentIndexJob.addJob(currentIndex.getIndexKey(indexKeyBytes,
                    diskEntry), diskEntry);
              } else {
                if (logEnabled) {
                  logger.info(LocalizedStrings.DEBUG, String.format(
                      "OplogIndex#recoverIndexes: skipping adding index for "
                          + "diskEntry=%s in indexContainer=%s as oplogid=%s "
                          + "doesn't match entry's oplogid=%s",
                          diskEntry, currentIndex, this.oplog.oplogId,
                          diskId != null ? diskId.getOplogId() : -1L));
                }
              }
            }
            break;

          default:
            if (opCode < 0) {
              endOfFile = true;
              break;
            } else {
              throw new IOException("unexpected opCode=" + opCode
                  + " encountered while reading file: " + this.irf);
            }
        }
      }
      in.close();
      raf.close();
      // check for stop
      cc.checkCancelInProgress(null);
      if (logEnabled || DiskStoreImpl.INDEX_LOAD_PERF_DEBUG) {
        logger.info(LocalizedStrings.DEBUG, "OplogIndex#recoverIndexes: "
            + "Processed file: " + this.irf);
      }
    } catch (IOException ioe) {
      throw new DiskAccessException(ioe);
    }
  }
}
