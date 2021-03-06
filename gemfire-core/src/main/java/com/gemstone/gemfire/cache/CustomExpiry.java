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

package com.gemstone.gemfire.cache;

/**
 * This is the contract that a <code>custom-expiry</code> element must honor.
 * It determines the expiration characteristics for a specific entry in a region.
 * <p>Note that if you wish to refer to an implementation of this interface in XML,
 * the implementation must also implement the Declarable interface.
 * 
 * @author jpenney
 *
 */
public interface CustomExpiry<K,V> extends CacheCallback {

  /**
   * Calculate the expiration for a given entry.
   * Returning null indicates that the
   * default for the region should be used.
   * <p>
   * The entry parameter should not be used after this method invocation completes.
   * @param entry the entry to calculate the expiration for
   * @return the expiration to be used, null if the region's defaults should be
   * used.
   */
  public ExpirationAttributes getExpiry(Region.Entry<K,V> entry);
}
