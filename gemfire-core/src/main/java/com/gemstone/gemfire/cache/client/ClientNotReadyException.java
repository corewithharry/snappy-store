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

package com.gemstone.gemfire.cache.client;

import com.gemstone.gemfire.cache.OperationAbortedException;

/**
 * A <code>ClientNotReadyException</code> indicates a client attempted to invoke
 * the {@link com.gemstone.gemfire.cache.Cache#readyForEvents}
 * method, but failed.
 * <p>This exception was moved from the <code>util</code> package in 5.7.
 * 
 * @author darrel
 *
 * @since 5.7
 * @deprecated as of 6.5 this exception is no longer thrown by GemFire so any code that catches it should be removed.
 * 
 */
public class ClientNotReadyException extends OperationAbortedException {
private static final long serialVersionUID = -315765802919271588L;
  /**
   * Constructs an instance of <code>ClientNotReadyException</code> with the
   * specified detail message.
   * 
   * @param msg the detail message
   */
  public ClientNotReadyException(String msg) {
    super(msg);
  }
  
  /**
   * Constructs an instance of <code>ClientNotReadyException</code> with the
   * specified detail message and cause.
   * 
   * @param msg the detail message
   * @param cause the causal Throwable
   */
  public ClientNotReadyException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
