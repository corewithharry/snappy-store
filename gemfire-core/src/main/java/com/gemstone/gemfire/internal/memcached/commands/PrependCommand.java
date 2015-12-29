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
package com.gemstone.gemfire.internal.memcached.commands;

import java.nio.ByteBuffer;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.internal.memcached.Reply;
import com.gemstone.gemfire.internal.memcached.ResponseStatus;
import com.gemstone.gemfire.internal.memcached.ValueWrapper;

/**
 * "prepend" means "add this data to an existing key before existing data".
 * 
 * @author Swapnil Bawaskar
 *
 */
public class PrependCommand extends StorageCommand {

  @Override
  public ByteBuffer processStorageCommand(String key, byte[] value, int flags, Cache cache) {
    Region<Object, ValueWrapper> r = getMemcachedRegion(cache);
    ValueWrapper oldValWrapper = r.get(key);
    String retVal = Reply.NOT_FOUND.toString();
    if (oldValWrapper != null) {
      byte[] oldVal = oldValWrapper.getValue();
      byte[] prependVal = (byte[]) value;
      byte[] newVal = new byte[oldVal.length + prependVal.length];
      System.arraycopy(prependVal, 0, newVal, 0, prependVal.length);
      System.arraycopy(oldVal, 0, newVal, prependVal.length, oldVal.length);
      r.put(key, ValueWrapper.getWrappedValue(newVal, flags));
      retVal = Reply.STORED.toString();
    }
    return asciiCharset.encode(retVal);
  }

  @Override
  public ByteBuffer processBinaryStorageCommand(Object key, byte[] value, long cas,
      int flags, Cache cache, ByteBuffer response) {
    Region<Object, ValueWrapper> r = getMemcachedRegion(cache);
    ValueWrapper oldValWrapper = r.get(key);
    if (oldValWrapper != null) {
      byte[] oldVal = oldValWrapper.getValue();
      byte[] prependVal = (byte[]) value;
      byte[] newVal = new byte[oldVal.length + prependVal.length];
      System.arraycopy(prependVal, 0, newVal, 0, prependVal.length);
      System.arraycopy(oldVal, 0, newVal, prependVal.length, oldVal.length);
      ValueWrapper val = ValueWrapper.getWrappedValue(newVal, flags);
      r.put(key, val);
      if (isQuiet()) {
        return null;
      }
      response.putShort(POSITION_RESPONSE_STATUS, ResponseStatus.NO_ERROR.asShort());
      response.putLong(POSITION_CAS, val.getVersion());
    } else {
      response.putShort(POSITION_RESPONSE_STATUS, ResponseStatus.KEY_NOT_FOUND.asShort());
    }
    return response;
  }

  /**
   * Overridden by Q command
   */
  protected boolean isQuiet() {
    return false;
  }
}
