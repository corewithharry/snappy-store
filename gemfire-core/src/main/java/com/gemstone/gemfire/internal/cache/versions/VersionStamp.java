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
package com.gemstone.gemfire.internal.cache.versions;

import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.distributed.internal.membership.InternalDistributedMember;
import com.gemstone.gemfire.internal.cache.LocalRegion;

/**
 * @author bruce
 *
 */
public interface VersionStamp<T extends VersionSource> extends VersionHolder<T> {

  
  
  /**
   * set the time stamp from the given clock value
   */
  void setVersionTimeStamp(long time);
  
//  /**
//   * @return the ID of the previous member that last changed the corresponding entry.
//   */
//  DistributedMember getPreviousMemberID();


  /**
   * Sets the version information with what is in the tag
   * 
   * @param tag the entryVersion to set
   */
  void setVersions(VersionTag<T> tag);

  /**
   * @param memberID the memberID to set
   */
  void setMemberID(VersionSource memberID);

//  /**
//   * @param previousMemberID the previousMemberID to set
//   */
//  void setPreviousMemberID(DistributedMember previousMemberID);

  /**
   * returns a VersionTag carrying this stamps information.  This is used
   * for transmission of the stamp in initial image transfer
   */
  VersionTag<T> asVersionTag();


  /**
   * Perform a versioning check with the incoming event.  Throws a
   * ConcurrentCacheModificationException if there is a problem.
   * @param event
   */
  public void processVersionTag(EntryEvent event);
  
  /**
   * Perform a versioning check with the given GII information.  Throws a
   * ConcurrentCacheModificationException if there is a problem.
   * @param r the region being modified
   * @param tag the version info for the modification
   * @param isTombstoneFromGII it's a tombstone
   * @param hasDelta it has delta
   * @param thisVM this cache's DM identifier
   * @param sender the identifier of the member providing the entry
   * @param checkConflicts true if conflict checks should be performed
   */
  public void processVersionTag(LocalRegion r, VersionTag<T> tag, boolean isTombstoneFromGII,
      boolean hasDelta,
      VersionSource thisVM, InternalDistributedMember sender, boolean checkConflicts);
  
  /**
   * return true if this stamp has valid entry/region version information, false if not
   */
  public boolean hasValidVersion();
}
