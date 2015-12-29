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
package com.gemstone.gemfire.internal.cache.partitioned.fixed;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import com.gemstone.gemfire.cache.EntryOperation;
import com.gemstone.gemfire.cache.FixedPartitionResolver;

public class MyDate3 extends Date implements FixedPartitionResolver{

  public MyDate3(long time) {
    super(time);
  }

  public String getPartitionName(EntryOperation opDetails, Set targetPartitions) {
    Date date = (Date)opDetails.getCallbackArgument();
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    int month = cal.get(Calendar.MONTH);
    if (month == 0 || month == 1 || month == 2) {
      return "Q1";
    }
    else if (month == 3 || month == 4 || month == 5) {
      return "Q2";
    }
    else if (month == 6 || month == 7 || month == 8) {
      return "Q3";
    }
    else if (month == 9 || month == 10 || month == 11) {
      return "Q4";
    }
    else {
      return "Invalid Quarter";
    }
  }

  public String getName() {
    return "MyDate3";
  }

  public Serializable getRoutingObject(EntryOperation opDetails) {
    Date date = (Date)opDetails.getCallbackArgument();
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    int month = cal.get(Calendar.MONTH);
    return month;
  }

  public void close() {
    // TODO Auto-generated method stub
    
  }

}
