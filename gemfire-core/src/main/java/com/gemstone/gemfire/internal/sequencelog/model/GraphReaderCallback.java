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
package com.gemstone.gemfire.internal.sequencelog.model;

import java.util.regex.Pattern;

import com.gemstone.gemfire.internal.sequencelog.GraphType;

/**
 * @author dsmith
 *
 */
public interface GraphReaderCallback {

  public abstract void addEdge(long timestamp, GraphType graphType,
      String graphName, String edgeName, String state, String source,
      String dest);
  
  public abstract void addEdgePattern(long timestamp, GraphType graphType,
      Pattern graphNamePattern, String edgeName, String state, String source,
      String dest);

}