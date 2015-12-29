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
package templates.security;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.operations.OperationContext;
import com.gemstone.gemfire.cache.operations.OperationContext.OperationCode;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.security.AccessControl;
import com.gemstone.gemfire.security.NotAuthorizedException;

/**
 * A dummy implementation of the <code>AccessControl</code> interface that
 * allows authorization depending on the format of the <code>Principal</code>
 * string.
 * 
 * @author Sumedh Wale
 * @since 5.5
 */
public class DummyAuthorization implements AccessControl {

  private Set allowedOps;

  private DistributedMember remoteDistributedMember;

  private LogWriter logger;

  public static final OperationCode[] READER_OPS = { OperationCode.GET,
      OperationCode.QUERY, OperationCode.EXECUTE_CQ, OperationCode.CLOSE_CQ,
      OperationCode.STOP_CQ, OperationCode.REGISTER_INTEREST,
      OperationCode.UNREGISTER_INTEREST, OperationCode.KEY_SET,
      OperationCode.CONTAINS_KEY, OperationCode.EXECUTE_FUNCTION };

  public static final OperationCode[] WRITER_OPS = { OperationCode.PUT, OperationCode.PUTALL, 
      OperationCode.DESTROY, OperationCode.INVALIDATE, OperationCode.REGION_CLEAR };

  public DummyAuthorization() {
    this.allowedOps = new HashSet(20);
  }

  public static AccessControl create() {
    return new DummyAuthorization();
  }

  private void addReaderOps() {

    for (int index = 0; index < READER_OPS.length; index++) {
      this.allowedOps.add(READER_OPS[index]);
    }
  }

  private void addWriterOps() {

    for (int index = 0; index < WRITER_OPS.length; index++) {
      this.allowedOps.add(WRITER_OPS[index]);
    }
  }

  public void init(Principal principal, 
                   DistributedMember remoteMember,
                   Cache cache) throws NotAuthorizedException {

    if (principal != null) {
      String name = principal.getName().toLowerCase();
      if (name != null) {
        if (name.equals("root") || name.equals("admin")
            || name.equals("administrator")) {
          addReaderOps();
          addWriterOps();
          this.allowedOps.add(OperationCode.REGION_CREATE);
          this.allowedOps.add(OperationCode.REGION_DESTROY);
        }
        else if (name.startsWith("writer")) {
          addWriterOps();
        }
        else if (name.startsWith("reader")) {
          addReaderOps();
        }
      }
    }
    this.remoteDistributedMember = remoteMember;
    this.logger = cache.getSecurityLogger();
  }

  public boolean authorizeOperation(String regionName, OperationContext context) {

    OperationCode opCode = context.getOperationCode();
    this.logger.fine("Invoked authorize operation for [" + opCode
        + "] in region [" + regionName + "] for client: " + remoteDistributedMember);
    return this.allowedOps.contains(opCode);
  }

  public void close() {

    this.allowedOps.clear();
  }

}
