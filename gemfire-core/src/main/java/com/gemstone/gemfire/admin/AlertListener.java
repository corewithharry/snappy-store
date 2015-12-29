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
package com.gemstone.gemfire.admin;

/**
 * A listener whose callback methods are invoked when an {@link Alert}
 * is received.
 * @deprecated as of 7.0 use the {@link com.gemstone.gemfire.management} package instead
 */
public interface AlertListener extends java.util.EventListener {

  /**
   * Invoked when an <code>Alert</code> is received.
   */
  public void alert(Alert alert);
}
