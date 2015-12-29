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
package com.gemstone.gemfire.internal.cache.tier.sockets.command;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.i18n.LogWriterI18n;
import com.gemstone.gemfire.internal.cache.EntrySnapshot;
import com.gemstone.gemfire.internal.cache.LocalRegion;
import com.gemstone.gemfire.internal.cache.NonLocalRegionEntry;
import com.gemstone.gemfire.internal.cache.tier.Command;
import com.gemstone.gemfire.internal.cache.tier.sockets.ServerConnection;
import com.gemstone.gemfire.internal.cache.versions.VersionTag;

/**
 * getEntry(key) operation performed on server.
 * Extends Request, and overrides getValueAndIsObject() in Request
 * so as to not invoke loader.
 * @author sbawaska
 * @since 6.6
 */
public class GetEntry70 extends Get70 {

  private final static GetEntry70 singleton = new GetEntry70();

  public static Command getCommand() {
    return singleton;
  }

  protected GetEntry70() {
  }
  
  @Override
  public Get70.Entry getValueAndIsObject(Region region, Object key,
      Object callbackArg, LogWriterI18n log,
      ServerConnection servConn) {
    LocalRegion lregion = (LocalRegion)region;
    Object data = null;
    Region.Entry entry = region.getEntry(key);
    if (logger.fineEnabled()) {
      logger.fine("GetEntryCommand: for key:"+key+" returning entry:"+entry);
    }
    VersionTag tag = null;
    if (entry != null) {
      EntrySnapshot snap;
      if (entry instanceof EntrySnapshot) {
        snap = (EntrySnapshot)entry;
        tag = snap.getVersionTag();
      }
      else {
        snap = new EntrySnapshot();
      }
      NonLocalRegionEntry re = NonLocalRegionEntry.newEntry(key,
          entry.getValue(), lregion, tag);
      snap.setRegionEntry(re);
      snap.setRegion(lregion);
      data = snap;
    }
    Get70.Entry result = new Get70.Entry();
    result.value = data;
    result.isObject = true;
    result.keyNotPresent = false;
    result.versionTag = tag;
    return result;
  }
}
