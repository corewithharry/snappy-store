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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.gemstone.gemfire.cache.DiskAccessException;
import com.gemstone.gemfire.internal.Assert;
import com.gemstone.gemfire.internal.cache.DiskEntry.RecoveredEntry;
import com.gemstone.gemfire.internal.cache.persistence.DiskRegionView;

/**
 * An implementation if DiskRegion used for offline export. After each oplog
 * is recovered this disk region passes the contents recovered from the oplog
 * to an ExportWriter, which can export those entries.
 */
public class ExportDiskRegion extends ValidatingDiskRegion {

  private Map<Object, RecoveredEntry> currentOplogEntries = new HashMap();
  private ExportWriter writer;


  public ExportDiskRegion(DiskStoreImpl ds, DiskRegionView drv, ExportWriter writer) {
    super(ds, drv);
    this.writer = writer;
  }

  @Override
  public DiskEntry initializeRecoveredEntry(Object key, RecoveredEntry re) {
    if(re.getValue() == null) {
      Assert.fail("Value should not have been null for key " + key);
    }
    currentOplogEntries.put(key, re);
    return super.initializeRecoveredEntry(key, re);
  }

  @Override
  public DiskEntry updateRecoveredEntry(Object key, RegionEntry entry, DiskEntry.RecoveredEntry re) {
    currentOplogEntries.put(key, re);
    return super.updateRecoveredEntry(key, entry, re);
  }

  @Override
  public void destroyRecoveredEntry(Object key) {
    currentOplogEntries.remove(key);
    super.destroyRecoveredEntry(key);
  }
  @Override
  public void oplogRecovered(long oplogId) {
    try {
      writer.writeBatch(currentOplogEntries);
    } catch(IOException e) {
      throw new DiskAccessException("Error during export", e, this.getDiskStore());
    }
    currentOplogEntries.clear();
  }

  public static interface ExportWriter {
    /** Write a batch of entries to the export system. The value
     * in RecoveredEntry is will not be null, so there
     * is no need to fault in the value in the implementor
     * @throws IOException
     */
    public void writeBatch(Map<Object, RecoveredEntry> entries) throws IOException;
  }

}
