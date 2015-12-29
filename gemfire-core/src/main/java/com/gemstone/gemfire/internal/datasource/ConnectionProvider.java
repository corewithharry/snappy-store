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
package com.gemstone.gemfire.internal.datasource;

/**
 * This interface outlines the behavior of a Connection provider.
 * 
 * @author tnegi
 */
public interface ConnectionProvider {

  /**
   * Provides a PooledConnection from the connection pool. Default user and
   * password are used.
   * 
   * @return a PooledConnection object to the user.
   */
  public Object borrowConnection() throws PoolException;

  /**
   * Returns a PooledConnection object to the pool.
   * 
   * @param connectionObject to be returned to the pool
   */
  public void returnConnection(Object connectionObject);

  /**
   * Closes a PooledConnection object .
   */
  public void returnAndExpireConnection(Object connectionObject);

  /**
   * Clean up the resources before restart of Cache
   */
  public void clearUp();
}
