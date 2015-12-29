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

package com.gemstone.gemfire.cache.operations;


/**
 * Encapsulates a region-level operation in both the pre-operation and
 * post-operation cases. The operations this class encapsulates are
 * {@link com.gemstone.gemfire.cache.operations.OperationContext.OperationCode#REGION_CLEAR} 
 * and {@link com.gemstone.gemfire.cache.operations.OperationContext.OperationCode#REGION_DESTROY}.
 * 
 * @author Sumedh Wale
 * @since 5.5
 */
public abstract class RegionOperationContext extends OperationContext {

  /** Callback object for the operation (if any) */
  private Object callbackArg;

  /** True if this is a post-operation context */
  private boolean postOperation;

  /**
   * Constructor for a region operation.
   * 
   * @param postOperation
   *                true to set the post-operation flag
   */
  public RegionOperationContext(boolean postOperation) {
    this.callbackArg = null;
    this.postOperation = postOperation;
  }

  /**
   * Return the operation associated with the <code>OperationContext</code>
   * object.
   * 
   * @return The <code>OperationCode</code> of this operation. This is one of
   *         {@link com.gemstone.gemfire.cache.operations.OperationContext.OperationCode#REGION_CLEAR} or
   *         {@link com.gemstone.gemfire.cache.operations.OperationContext.OperationCode#REGION_DESTROY}.
   */
  @Override
  public abstract OperationCode getOperationCode();

  /**
   * True if the context is for post-operation.
   */
  @Override
  public boolean isPostOperation() {
    return this.postOperation;
  }

  /**
   * Get the callback argument object for this operation.
   * 
   * @return the callback argument object for this operation.
   */
  public Object getCallbackArg() {
    return this.callbackArg;
  }

  /**
   * Set the callback argument object for this operation.
   * 
   * @param callbackArg
   *                the callback argument object for this operation.
   */
  public void setCallbackArg(Object callbackArg) {
    this.callbackArg = callbackArg;
  }

}
