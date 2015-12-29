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
/**
 * 
 */
package com.gemstone.gemfire.internal.cache.execute;

import com.gemstone.gemfire.cache.EntryOperation;
import com.gemstone.gemfire.cache.PartitionResolver;
import com.gemstone.gemfire.internal.cache.execute.data.CustId;
import com.gemstone.gemfire.internal.cache.execute.data.OrderId;
import com.gemstone.gemfire.internal.cache.execute.data.ShipmentId;

import java.io.Serializable;

public class CustomerIDPartitionResolver implements PartitionResolver {

  private static CustomerIDPartitionResolver customerIDPartitionResolver = null;

  private String id;

  public CustomerIDPartitionResolver() {
  }

  private String resolverName;

  public CustomerIDPartitionResolver(String resolverID) {
    id = resolverID;
  }

  public String getName() {
    return this.resolverName;
  }

  public Serializable getRoutingObject(EntryOperation opDetails) {

    Serializable routingbject = null;

    if (opDetails.getKey() instanceof ShipmentId) {
      ShipmentId shipmentId = (ShipmentId)opDetails.getKey();
      routingbject = shipmentId.getOrderId().getCustId();
    }
    if (opDetails.getKey() instanceof OrderId) {
      OrderId orderId = (OrderId)opDetails.getKey();
      routingbject = orderId.getCustId();
    }
    else if (opDetails.getKey() instanceof CustId) {
      CustId custId = (CustId)opDetails.getKey();
      routingbject = custId.getCustId();
    }
    return routingbject;
  }

  public void close() {}

  public boolean equals(Object o) {
    if (this == o)
      return true;

    if (!(o instanceof CustomerIDPartitionResolver))
      return false;

    CustomerIDPartitionResolver otherCustomerIDPartitionResolver = (CustomerIDPartitionResolver)o;
    return otherCustomerIDPartitionResolver.id.equals(this.id);

  }

}
