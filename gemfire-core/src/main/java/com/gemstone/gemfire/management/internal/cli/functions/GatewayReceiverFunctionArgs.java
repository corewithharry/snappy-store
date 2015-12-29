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
package com.gemstone.gemfire.management.internal.cli.functions;

import java.io.Serializable;

/**
 * This class stores the arguments provided in the create gateway-receiver command.
 */
public class GatewayReceiverFunctionArgs implements Serializable {
  private static final long serialVersionUID = -5158224572470173267L;

  private final Integer startPort;

  private final Integer endPort;

  private final String bindAddress;

  private final Integer socketBufferSize;

  private final Integer maximumTimeBetweenPings;

  private final String[] gatewayTransportFilters;

  public GatewayReceiverFunctionArgs(Integer startPort, Integer endPort,
      String bindAddress, Integer socketBufferSize,
      Integer maximumTimeBetweenPings, String[] gatewayTransportFilters) {
    this.startPort = startPort;
    this.endPort = endPort;
    this.bindAddress = bindAddress;
    this.socketBufferSize = socketBufferSize;
    this.maximumTimeBetweenPings = maximumTimeBetweenPings;
    this.gatewayTransportFilters = gatewayTransportFilters;
  }
  
  public Integer getStartPort() {
    return this.startPort;
  }
  
  public Integer getEndPort() {
    return this.endPort;
  }
  
  public String getBindAddress() {
    return this.bindAddress;
  }
  
  public Integer getSocketBufferSize() {
    return this.socketBufferSize;
  }
  
  public Integer getMaximumTimeBetweenPings() {
    return this.maximumTimeBetweenPings;
  }
  
  public String[] getGatewayTransportFilters() {
    return this.gatewayTransportFilters;
  }
}
