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


package com.gemstone.gemfire.internal.cache.tier.sockets;

import com.gemstone.gemfire.cache.InterestResultPolicy;
import com.gemstone.gemfire.internal.DataSerializableFixedID;
import com.gemstone.gemfire.internal.shared.Version;

import java.io.*;

/**
 * Used to make InterestResultPolicy implement DataSerializableFixedID
 *
 * @author Darrel Schneider
 *
 * @since 5.7 
 */
public final class InterestResultPolicyImpl extends InterestResultPolicy
  implements DataSerializableFixedID {
  private static final long serialVersionUID = -7456596794818237831L;
  /** Should only be called by static field initialization in InterestResultPolicy */
  public InterestResultPolicyImpl(String name) {
    super(name);
  }

  public int getDSFID() {
    return INTEREST_RESULT_POLICY;
  }

  public void toData(DataOutput out) throws IOException {
    out.writeByte(getOrdinal());
  }

  public void fromData(DataInput in) throws IOException, ClassNotFoundException {
    // should never be called since DSFIDFactory.readInterestResultPolicy is used
    throw new UnsupportedOperationException();
  }

  @Override
  public Version[] getSerializationVersions() {
     return null;
  }
}
