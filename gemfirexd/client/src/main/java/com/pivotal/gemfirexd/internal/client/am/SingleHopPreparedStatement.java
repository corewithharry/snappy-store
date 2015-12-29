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
package com.pivotal.gemfirexd.internal.client.am;

import com.gemstone.gemfire.internal.shared.ClientSharedUtils;
import com.gemstone.gemfire.internal.shared.JdkHelper;
import com.pivotal.gemfirexd.internal.client.ClientPooledConnection;
import com.pivotal.gemfirexd.internal.client.am.Connection.FailoverStatus;
import com.pivotal.gemfirexd.internal.client.net.NetConnection;
import com.pivotal.gemfirexd.internal.client.net.NetConnection.DSConnectionInfo;
import com.pivotal.gemfirexd.internal.client.net.FinalizeSingleHopResultSet;
import com.pivotal.gemfirexd.internal.client.net.NetResultSet;
import com.pivotal.gemfirexd.internal.client.net.SingleHopResultSet;
import com.pivotal.gemfirexd.internal.shared.common.AbstractRoutingObjectInfo;
import com.pivotal.gemfirexd.internal.shared.common.CharColumnRoutingObjectInfo;
import com.pivotal.gemfirexd.internal.shared.common.ClientColumnResolver;
import com.pivotal.gemfirexd.internal.shared.common.ClientListResolver;
import com.pivotal.gemfirexd.internal.shared.common.ClientRangeResolver;
import com.pivotal.gemfirexd.internal.shared.common.ClientResolver;
import com.pivotal.gemfirexd.internal.shared.common.Converter;
import com.pivotal.gemfirexd.internal.shared.common.DecimalColumnRoutingObjectInfo;
import com.pivotal.gemfirexd.internal.shared.common.DoubleColumnRoutingObjectInfo;
import com.pivotal.gemfirexd.internal.shared.common.IntColumnRoutingObjectInfo;
import com.pivotal.gemfirexd.internal.shared.common.LongIntColumnRoutingObjectInfo;
import com.pivotal.gemfirexd.internal.shared.common.MultiColumnRoutingObjectInfo;
import com.pivotal.gemfirexd.internal.shared.common.RangeRoutingObjectInfo;
import com.pivotal.gemfirexd.internal.shared.common.RealColumnRoutingObjectInfo;
import com.pivotal.gemfirexd.internal.shared.common.ResolverUtils;
import com.pivotal.gemfirexd.internal.shared.common.SharedUtils;
import com.pivotal.gemfirexd.internal.shared.common.SingleHopInformation;
import com.pivotal.gemfirexd.internal.shared.common.SmallIntRoutingObjectInfo;
import com.pivotal.gemfirexd.internal.shared.common.SingleHopInformation.BucketAndNetServerInfo;
import com.pivotal.gemfirexd.internal.shared.common.VarCharColumnRoutingObjectInfo;
import com.pivotal.gemfirexd.internal.shared.common.reference.SQLState;
import com.pivotal.gemfirexd.internal.shared.common.sanity.SanityManager;
import com.pivotal.gemfirexd.jdbc.ClientDRDADriver;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * This prepared statement will be returned to the user in case single hop is
 * enabled on the connection and the query qualifies for the single hop.
 * 
 * @author kneeraj
 * 
 */
public abstract class SingleHopPreparedStatement extends PreparedStatement {

  static {
    CharColumnRoutingObjectInfo.dummy();
    DecimalColumnRoutingObjectInfo.dummy();
    DoubleColumnRoutingObjectInfo.dummy();
    IntColumnRoutingObjectInfo.dummy();
    LongIntColumnRoutingObjectInfo.dummy();
    MultiColumnRoutingObjectInfo.dummy();
    RangeRoutingObjectInfo.dummy();
    RealColumnRoutingObjectInfo.dummy();
    SmallIntRoutingObjectInfo.dummy();
    VarCharColumnRoutingObjectInfo.dummy();
  }
  
  private int noOfBuckets;

  private int redundancy;

  /* for a bucket id the server is at the index bucketid */
  private HostPort[] primaryServers;

  /* for a bucket id the server is at the index bucketid 
   * in individual server list*/
  private HostPort[][] secondaryServers;

  Map connToPrepStmntMap;

  ClientResolver resolver;

  private SingleHopInformation singleHopInformation;

  private Random randomServerSelector;

  // redundancy + 1
  private int numberOfPossibleServerArray;

  private HashSet alreadyConsideredList;

  private HashMap prepareAwareConnList;

  private DSConnectionInfo dsConnInfo;

  private PreparedStatement[] psArray;

  private java.sql.ResultSet shopResultSet;

  private Set routingObjectSet;

  private boolean useOnlyPrimary;

  private boolean refreshBucketDetails;

  private boolean hasAggregate;
  
  private int[] actualParamTypes;

  public final Converter crossConverter;
  
  private final ArrayList<NetConnection> poolConns = new ArrayList<NetConnection>();
  
  public SingleHopPreparedStatement(Agent agent, Connection connection,
      String sql, Section section, ClientPooledConnection cpc)
      throws SqlException {
    super(agent, connection, sql, section, cpc);
    this.crossConverter = new CrossConverters(agent);
  }

  public SingleHopPreparedStatement(Agent agent, Connection connection,
      String sql, int type, int concurrency, int holdability,
      int autoGeneratedKeys, String[] columnNames, int[] columnIndexes,
      ClientPooledConnection cpc) throws SqlException {
    super(agent, connection, sql, type, concurrency, holdability,
        autoGeneratedKeys, columnNames, columnIndexes, cpc);
    this.crossConverter = new CrossConverters(agent);
  }

  public void initialize(Section sec) throws SQLException {
    assert sec == this.section_;
    SingleHopInformation sinfo = this.section_.getSingleHopInformationObj();
    if (sinfo == null || !sinfo.isHoppable()) {
      if (SanityManager.TraceSingleHop) {
        SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
            "SingleHopPreparedStatement::initialize no single hop for: "
                + this.sql_ + " with prep stmnt: " + this);
      }

      if (sinfo != null
          && sinfo.getResolverByte() == SingleHopInformation.NO_SINGLE_HOP_AS_PRIMARY_NOT_DETERMINED) {
        // TODO: KN: set a warning here
        if (SanityManager.TraceSingleHop) {
          SanityManager
              .DEBUG_PRINT(
                  SanityManager.TRACE_SINGLE_HOP,
                  "SingleHopPreparedStatement::initialize no single hop for: "
                      + this.sql_
                      + " as some primary bucket locations don't have a network server ");
        }
      }
      return;
    }
    
    this.hasAggregate = sinfo.getHasAggregate();

    useOnlyPrimary = this.sqlMode_ == isUpdate__;
    if (!useOnlyPrimary) {
      if (!sinfo.getAllSecondaryServersDetermined()) {
        useOnlyPrimary = true;
        // TODO: KN: set a warning
        if (SanityManager.TraceSingleHop) {
          SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
              " use only primary true as secondaries not determined");
        }
      }
    }
    
    if (!useOnlyPrimary) {
      if (sinfo.getHdfsRegionCase()) {
        useOnlyPrimary = true;
        if (SanityManager.TraceSingleHop) {
          SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
              " use only primary true as Query Hdfs case");
        }
      }
    }

    if (sinfo.isHoppable()) {
      ArrayList bansiList = sinfo.getBucketAndNetServerInfoList();
      this.noOfBuckets = sinfo.getTotalNumberOfBuckets();
      assert this.noOfBuckets > 0;
      Iterator itr = bansiList.iterator();
      boolean arraySizesInitialized = false;
      while (itr.hasNext()) {
        BucketAndNetServerInfo bansi = (BucketAndNetServerInfo)itr.next();
        if (!arraySizesInitialized) {
          this.primaryServers = new HostPort[this.noOfBuckets];
          this.redundancy = (bansi.getSecondaryServerBucketStrings() == null) ? 0
              : bansi.getSecondaryServerBucketStrings().length;
          this.secondaryServers = new HostPort[this.redundancy][this.noOfBuckets];
          arraySizesInitialized = true;
        }
        int bucketId = bansi.getBucketId();
        String primaryServStr = bansi.getPrimaryBucketServerString();
        assert primaryServStr != null;
        this.primaryServers[bucketId] = returnHostPortFromServerString(primaryServStr);

        String[] secondaryServers = bansi.getSecondaryServerBucketStrings();
        for (int i = 0; i < this.redundancy; i++) {
          this.secondaryServers[i][bucketId] = returnHostPortFromServerString(secondaryServers[i]);
        }
      }

      initializeResolver(sinfo);
      this.alreadyConsideredList = new HashSet();
      this.numberOfPossibleServerArray = this.redundancy + 1;
      this.prepareAwareConnList = new HashMap();
      NetConnection netConn = ((NetConnection)this.connection_);
      this.dsConnInfo = netConn.getDSConnectionInfo();
      java.sql.CallableStatement getBktLocationProc =
        netConn.getBucketToServerDetails_;
      if (getBktLocationProc == null) {
        netConn.getBucketToServerDetails_ = netConn
            .prepareCall(NetConnection.BUCKET_AND_SERVER_PROC_QUERY);
      }
    }

    if (SanityManager.TraceSingleHop) {
      SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
          "SingleHopPreparedStatement::initialize single hop for: " + this.sql_
              + " with primary servers: " + this.primaryServers
              + ", secondary servers: " + this.secondaryServers
              + " and single hop information: " + sinfo);
    }
  }

  private HostPort returnHostPortFromServerString(String serverStr) {
    if (serverStr == null || serverStr.length() == 0) {
      return null;
    }
    java.util.regex.Pattern addrPattern = NetConnection.addrPat_;
    java.util.regex.Matcher matcher = addrPattern.matcher(serverStr);
    boolean match = matcher.find();
    if (!match) {
      SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
          "SingleHopPreparedStatement::returnHostPortFromServerString"
              + " match not found for serverserverStr: " + serverStr);
      return null;
    }
    String host = matcher.group(1);
    String portStr = matcher.group(3);
    return new HostPort(host, portStr);
  }

  private void initializeResolver(SingleHopInformation sinfo)
      throws SQLException {
    this.singleHopInformation = sinfo;
    int resolverType = sinfo.getResolverByte();
    int[] typeArray = sinfo.getTypeFormatIdArray();
    switch (resolverType) {
      case SingleHopInformation.COLUMN_RESOLVER_FLAG:
        this.resolver = new ClientColumnResolver(typeArray,
            sinfo.getRequiresSerializedHash());
        break;

      case SingleHopInformation.LIST_RESOLVER_FLAG:
        assert typeArray.length == 1;
        this.resolver = new ClientListResolver(typeArray[0],
            sinfo.getMapOfListValues());
        break;

      case SingleHopInformation.RANGE_RESOLVER_FLAG:
        assert typeArray.length == 1;
        this.resolver = new ClientRangeResolver(typeArray[0],
            sinfo.getRangeValueHolderList());
        break;

      default:
        throw new SQLException("cannot handle resolver type: " + resolverType);
    }
    this.resolver.setNumBuckets(this.noOfBuckets);
  }

  @Override
  public int executeUpdate() throws SQLException {
    try {
      super.executeUpdate();
      int cnt = 0;
      if (this.superFlowExecuteCalled_) {
        cnt = super.updateCount_;
      }
      else {
        for (int i = 0; i < this.psArray.length; i++) {
          cnt += this.psArray[i].updateCount_;
        }
      }
      return cnt;
    } finally {
      // return connections to the pool if any
      try {
        returnConnectionsToPool();
      } catch (SqlException e) {
        removeAllConnectionsUsedFromPoolNoThrow();
      }
    }
  }

  @Override
  protected java.sql.ResultSet singleHopPrepStmntExecuteQuery()
      throws SQLException {
    return this.getResultSet();
  }

  public static String returnParameters(Object[] parameters) {
    StringBuilder sb;
    if (parameters != null) {
      sb = new StringBuilder();
      sb.append(parameters).append('=');
      for(int i=0; i<parameters.length; i++) {
        sb.append(parameters[i]);
        if (i != (parameters.length - 1)) {
          sb.append(',');
        }
      }
      return sb.toString();
    }
    else {
      return null;
    }
  }

  public java.sql.ResultSet executeQuery() throws SQLException {
    try {
      return super.executeQuery();
    } finally {
      // return connections if any to the pool
      try {
        returnConnectionsToPool();
      } catch (SqlException e) {
        removeAllConnectionsUsedFromPoolNoThrow();
      }
    }
  }
  
  public boolean execute() throws SQLException {
    try {
      return super.execute();
    } finally {
      // return connections if any to the pool
      try {
        returnConnectionsToPool();
      } catch (SqlException e) {
        removeAllConnectionsUsedFromPoolNoThrow();
      }
    }
  }
  
  @Override
  protected void flowExecute(int executeType, final int currExecSeq) throws SqlException {
    if (SanityManager.TraceSingleHop) {
      SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
          " SHOP.flowexecute called sql is: " + this.sql_ + " parameters is: "
              + returnParameters(this.parameters_));
    }

    poolConns.clear();
    this.superFlowExecuteCalled_ = false;
    //doCleanupsOfPreviousExecute();
    this.shopResultSet = null;
    if (this.singleHopInformation == null
        || !this.singleHopInformation.isHoppable()) {
      if (SanityManager.TraceSingleHop) {
        SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
            " defaulting to no single hop mode as sinfo looks non hoppable: "
                + this.singleHopInformation);
      }
      super.flowExecute(executeType, currExecSeq);
      return;
    }

    if (this.clientParamTypeAtFirstExecute == null) {
      int[] clientParamtype = this.parameterMetaData_ != null ? this.parameterMetaData_.clientParamtertype_ : null;
      if (clientParamtype != null) {
        this.clientParamTypeAtFirstExecute = setAndStoreClientParamType(clientParamtype);
      }
    }
    
    if (SanityManager.TraceSingleHop) {
      SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
          " SingleHopPreparedStatement: " + this + " sql is: " + this.sql_
              + " probably hoppable");
    }

    assert !(this.sqlMode_ == isCall__);

    if (useOnlyPrimary
        && (this.primaryServers == null || this.primaryServers.length == 0)) {
      if (SanityManager.TraceSingleHop) {
        SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
            " defaulting to no single hop mode as use only primary is true "
                + " but primary server list: " + this.primaryServers
                + " is not proper");
      }
      super.flowExecute(executeType, currExecSeq);
      return;
    }

    Map serverAddressesToRoutingObject = getServerAddressToRoutingObjMap(executeType);

    if (SanityManager.TraceSingleHop) {
      SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
          "SingleHopPreparedStatement::flowExecute "
              + "server to robj mapping: " + serverAddressesToRoutingObject
              + ", section obj of this ps is: " + section_);
    }

    if (serverAddressesToRoutingObject == null
        || serverAddressesToRoutingObject.isEmpty()
        || (this.hasAggregate && serverAddressesToRoutingObject.size() > 1)) {
      if (SanityManager.TraceSingleHop) {
        SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
            "SingleHopPreparedStatement::flowExecute "
                + "serverAddressesToRoutingObject null or empty "
                + "or has aggregate and is going to more than 1 server"
                + " so defaulting to no single hop mode");
      }
      super.flowExecute(executeType, currExecSeq);
      return;
    }

    HashMap psUsed = null;
    boolean fromSuperFlowExecute = false;
    try {
      psUsed = getAllPrepStmntUsed(serverAddressesToRoutingObject, currExecSeq);

      if (psUsed == null || psUsed.isEmpty()) {
        if (SanityManager.TraceSingleHop) {
          SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
              "SingleHopPreparedStatement::flowExecute No internal "
                  + "prepared statement obtained "
                  + " so defaulting to no single hop mode");
        }
        fromSuperFlowExecute = true;
        super.flowExecute(executeType, currExecSeq);
        return;
      }

      final NetConnection thisConn = (NetConnection)this.connection_;
      if (thisConn.getBucketToServerDetails_ == null) {
        // rethrow any exceptions at this layer
        fromSuperFlowExecute = true;
        // reprepare and refetch bucket details
        reFetchBucketLocationInformation(true);
      }
      else if (this.refreshBucketDetails) {
        // rethrow any exceptions at this layer
        fromSuperFlowExecute = true;
        // refetch bucket details
        reFetchBucketLocationInformation(false);
      }
      this.refreshBucketDetails = false;
      fromSuperFlowExecute = false;
      callFlowExecuteReadWrite(psUsed, executeType, currExecSeq);
    } catch (SqlException sqle) {
      if (fromSuperFlowExecute) {
        throw sqle;
      }
      if (SanityManager.TraceSingleHop) {
        SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
            "SingleHopPreparedStatement::flowExecute got exception, "
                + "going to try for failover if required", sqle);
      }
      // check if we need to do failover
      if (!connection_.loadBalance_
          || connection_.doFailoverOnException(sqle.getSQLState(),
              sqle.getErrorCode(), sqle) == FailoverStatus.NONE) {
        throw sqle;
      }
      final Agent origAgent = connection_.agent_;
      // let super.flowExecute() handle all failover since it is possible
      // that target of parent connection is still alive to don't ignore
      // it by an explicit handleFailover here (#44862)
      final boolean origSingleHopEnabled = connection_.singleHopEnabled_;
      connection_.singleHopEnabled_ = false;
      this.singleHopInfoAlreadyFetched_ = true;
      try {
        if (SanityManager.TraceSingleHop) {
          SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
              "SingleHopPreparedStatement::flowExecute calling "
                  + "super.flowexecute for failover handling "
                  + "on the connection_: " + connection_);
        }
        super.flowExecute(executeType, currExecSeq);
      } finally {
        connection_.singleHopEnabled_ = origSingleHopEnabled;
      }
      // invalidate BucketToServerDetails so its refetched on next execute
      final Agent newAgent = connection_.agent_;
      if (origAgent != newAgent) {
        ((NetConnection)connection_).getBucketToServerDetails_ = null;
      }
      else {
        this.refreshBucketDetails = true;
      }
    }
  }

  private void reFetchBucketLocationInformation(boolean reprepare)
      throws SqlException {
    NetConnection netConn = (NetConnection)this.connection_;
    java.sql.CallableStatement getBktLocationProc =
      netConn.getBucketToServerDetails_;
    boolean origSingleHopEnabled = netConn.singleHopEnabled_;
    netConn.singleHopEnabled_ = false;
    try {
      if (reprepare) {
        if (SanityManager.TraceClientStatementHA) {
          SanityManager.DEBUG_PRINT(SanityManager.TRACE_CLIENT_HA,
              "preparing the fetch proc again");
        }
        if (getBktLocationProc != null) {
          SanityManager.THROWASSERT("expected the getBucketToServerDetails "
              + "proc as null");
        }
        getBktLocationProc = netConn
            .prepareCall(NetConnection.BUCKET_AND_SERVER_PROC_QUERY);
      }

      getBktLocationProc
          .setString(1, this.singleHopInformation.getRegionName());
      getBktLocationProc.registerOutParameter(2, java.sql.Types.LONGVARCHAR);
      getBktLocationProc.execute();
      String bucketToServerMappingStr = getBktLocationProc.getString(2);
      if (SanityManager.TraceSingleHop) {
        SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
            "SingleHopPreparedStatement::reFetchBucketLocationInformation"
                + "get bucket to server mapping returned follwing string:  "
                + bucketToServerMappingStr);
      }
      this.primaryServers = new HostPort[this.noOfBuckets];
      this.secondaryServers = new HostPort[this.redundancy][this.noOfBuckets];
      setBucketToServerMappingInfo(bucketToServerMappingStr);

      if (reprepare) {
        netConn.getBucketToServerDetails_ = getBktLocationProc;
      }
    } catch (SQLException sqle) {
      throw new SqlException(sqle);
    } finally {
      netConn.singleHopEnabled_ = origSingleHopEnabled;
    }
  }

  private void callFlowExecuteReadWrite(HashMap psUsed, int executeType, int currExecSeq)
      throws SqlException {
    this.connection_.singleHopAttempted++;
    int sz = psUsed.size();
    FlowExecuteContext[] ctxArr = new FlowExecuteContext[sz];
    HashMap exceptions = null;
    this.psArray = new PreparedStatement[sz];
    Iterator itr = psUsed.keySet().iterator();
    int i = 0;
    // do the write part of flowExecute for all prepared statements
    while (itr.hasNext()) {
      Connection conn = (Connection)itr.next();
      PreparedStatement ps = (PreparedStatement)psUsed.get(conn);
      try {
        ps.section_.setTxIdToBeSent(false);
        FlowExecuteContext ctx = ps.flowExecuteWrite(executeType, currExecSeq);
        ctxArr[i] = ctx;
        this.psArray[i] = ps;
        i++;
      } catch (SqlException sqle) {
        if (exceptions == null) {
          exceptions = new HashMap();
        }
        exceptions.put(conn, sqle);
      }
    }
    sz = i;
    // Now do the read part of flowExecute for all prepared statements
    for (i = 0; i < sz; i++) {
      try {
        this.psArray[i].flowExecuteRead(executeType, ctxArr[i]);
        // connection can now be returned to the pool
        returnConnection((NetConnection)this.psArray[i].connection_);
      } catch (SqlException sqle) {
        if (exceptions == null) {
          exceptions = new HashMap();
        }
        exceptions.put(this.psArray[i].connection_, sqle);
      }
    }
    if (exceptions != null) {
      SqlException throwEx = null;
      // got an exception
      Iterator exItr = exceptions.entrySet().iterator();
      while (exItr.hasNext()) {
        // check if failover type
        Map.Entry e = (Map.Entry)exItr.next();
        NetConnection conn = (NetConnection)e.getKey();
        SqlException sqle = (SqlException)e.getValue();
        if (conn.doFailoverOnException(sqle.getSQLState(), sqle.getErrorCode(),
            sqle) == FailoverStatus.NEW_SERVER) {
          // close the connection
          try {
            java.util.Set keySet = ((Connection)conn).openStatements_.keySet();
            for (java.util.Iterator it = keySet.iterator(); it.hasNext();) {
              Object st = it.next();
              if(!((Statement)st).isPreparedStatement_) {
                continue;
              }
              PreparedStatement internalPoolPrepstmt = (PreparedStatement)st;
              if (internalPoolPrepstmt != null && internalPoolPrepstmt.internalPreparedStatement) {
                setParametersToNullInTheInternalPS(internalPoolPrepstmt);
              }
            }
            conn.agent_.disableDisconnectEvent_ = false;
            conn.close();
          } catch (SQLException se) {
            // ignored
          }
          if (SanityManager.TraceSingleHop) {
            SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
                "SHPS callFlowExecuteReadWrite conn: " + conn
                    + " closed and is being removed from the pool");
          }
          removeConnection(conn);
          if (SanityManager.TraceSingleHop) {
            SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
                "SHPS callFlowExecuteReadWrite conn: " + conn
                    + " closed and is being removed from the pool");
          }
          this.prepareAwareConnList.remove(conn);
          throwEx = sqle;
        }
        else {
          if (SanityManager.TraceSingleHop) {
            SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
                "SingleHopPreparedStatement::callFlowExecuteReadWrite setting "
                    + "got exception to true on connection: " + conn);
          }
          conn.gotException_ = true;
          returnConnection(conn);
          if (throwEx == null) {
            throwEx = sqle;
          }
        }
      }
      if (throwEx != null) {
        if (SanityManager.TraceSingleHop) {
          SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
              "SHPS callFlowExecuteReadWrite throwing exception: " + throwEx
                  + " to be handled for retry");
        }
        throw throwEx;
      }
    }
    if (SanityManager.TraceSingleHop) {
      SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
          "SHPS callFlowExecuteReadWrite completed with ps size: "
              + this.psArray.length);
    }
  }

  private HashMap getAllPrepStmntUsed(Map serverAddressesToRoutingObject, final int currExecSeq)
      throws SqlException {
    Iterator serverMapItr = serverAddressesToRoutingObject.keySet().iterator();
    HashMap psUsed = null;
    boolean dBSyncFlagSet = false;
    while (serverMapItr.hasNext()) {
      HostPort server = (HostPort)serverMapItr.next();
      HashSet bucketIds = (HashSet)serverAddressesToRoutingObject
          .get(server);
      if (bucketIds == null) {
        if (SanityManager.TraceSingleHop) {
          SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
              "SingleHopPreparedStatement::getAllPrepStmntUsed bucketIDs or server null, bucketIds: "
                  + bucketIds + ", server: " + server);
        }
        return null;
      }

      if (server == null) {
        if (SanityManager.TraceSingleHop) {
          SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
              "SingleHopPreparedStatement::getAllPrepStmntUsed server null for bucketID: "
                  + bucketIds);
        }
      }
      Connection conn;
      try {
        conn = getTheConnectionForThisServer(server, false);
        if (SanityManager.TraceSingleHop) {
          SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
            "SingleHopPreparedStatement::getAllPrepStmntUsed Took from pool connection conn: "
                + conn);
        }
      } catch (SQLException e) {
        // clean the existing map
        if (psUsed != null) {
          Iterator iter = psUsed.keySet().iterator();
          while (iter.hasNext()) {
            returnConnection((NetConnection)iter.next());
          }
        }
        throw new SqlException(e);
      }

      if (conn == null) {
        if (SanityManager.TraceSingleHop) {
          SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
              "SingleHopPreparedStatement::getAllPrepStmntUsed no connection obtained"
                  + " for server: " + server
                  + " so defaulting to no single hop mode");
        }
        return null;
      }
      else {
        if (SanityManager.TraceSingleHop) {
          SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
              "SingleHopPreparedStatement::getAllPrepStmntUsed connection obtained is: "
                  + conn);
        }
        if (this.useOnlyPrimary && !dBSyncFlagSet) {
          // adding hard coded -1 as a hint to server that dbsync flag should be on.
          // Buckets themselves will never be -ve as we do Math.abs() on the routing
          // object calculated.
          bucketIds.add(ResolverUtils.TOKEN_FOR_DB_SYNC);
          dBSyncFlagSet = true;
          if (SanityManager.TraceSingleHop) {
            SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
                "SingleHopPreparedStatement::getAllPrepStmntUsed dbSync added in bucketId: "
                    + bucketIds);
          }
        }
        PreparedStatement ps = (PreparedStatement)this.prepareAwareConnList
            .get(conn);
        boolean removed = false;
        if (ps != null && ps.isClosedNoThrow()) {
          this.prepareAwareConnList.remove(conn);
          removed = true;
        }
        if (ps != null && !removed) {
          if (SanityManager.TraceSingleHop) {
            SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
                "SingleHopPreparedStatement::getAllPrepStmntUsed conn: " + conn
                    + " is prep statement aware so setting only bucket ids: "
                    + bucketIds + " in section obj: " + ps.section_);
          }

          ps.section_.setSqlStringToNull();
          ps.section_.setBucketIds(bucketIds);
          ps.section_.setCopiedSection(true, this);
          ps.section_.setExecutionSequence(currExecSeq);
        }
        else {
          if (SanityManager.TraceSingleHop) {
            SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
                "SingleHopPreparedStatement::getAllPrepStmntUsed conn: preparingX a new ps for: "
                    + this.sql_ + " on connection: " + conn);
          }
          try {
            synchronized (conn) {
              String baseStatementSchemaName = this.connection_.currentSchemaName_;
              String currConnSchemaName = conn.currentSchemaName_;
              if (baseStatementSchemaName != null
                  && !baseStatementSchemaName.equals(currConnSchemaName)) {
                conn.doSetSchema_ = baseStatementSchemaName;
              }
              else if (conn.doSetSchema_ != null) {
                conn.doSetSchema_ = null;
              }
              ps = conn.prepareStatementX(this.sql_, this.resultSetType_,
                  this.resultSetConcurrency_, this.resultSetHoldability_,
                  this.autoGeneratedKeys_, null, null, this.section_,
                  !this.hasAggregate ? this.singleHopInformation.getRegionName()
                      : "");
              ps.section_.setBucketIds(bucketIds);
              ps.section_.setCopiedSection(true, this);
              ps.section_.setExecutionSequence(currExecSeq);
              ps.section_.setTxIdToBeSent(true);
              setParametersAndOtherValuesEqualToThisPS(ps);
            }
          } catch (SqlException sqle) {
            // check for failover kind of exception
            if (conn.doFailoverOnException(sqle.getSQLState(),
                sqle.getErrorCode(), sqle) == FailoverStatus.NEW_SERVER) {
              // close the connection
              try {
                conn.close();
              } catch (SQLException se) {
                // ignored
              }
              if (SanityManager.TraceSingleHop) {
                SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
                    "SingleHopPreparedStatement::getAllPrepStmntUsed conn: " + conn
                        + " closed and is being removed from the pool");
              }
              removeConnection((NetConnection)conn);
              if (SanityManager.TraceSingleHop) {
                SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
                    "SingleHopPreparedStatement::getAllPrepStmntUsed conn: " + conn
                        + " closed and is being removed from the pool");
              }
            }
            else {
              NetConnection netConn = (NetConnection)conn;
              if (SanityManager.TraceSingleHop) {
                SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
                    "SingleHopPreparedStatement::getAllPrepStmntUsed setting "
                        + "got exception to true on connection: " + conn);
              }
              netConn.gotException_ = true;
              returnConnection((NetConnection)conn);
            }
            // clean the existing map
            if (psUsed != null) {
              Iterator iter = psUsed.keySet().iterator();
              while (iter.hasNext()) {
                returnConnection((NetConnection)iter.next());
              }
            }
            throw sqle;
          }
          this.prepareAwareConnList.put(conn, ps);
          if (SanityManager.TraceSingleHop) {
            SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
                "SingleHopPreparedStatement::getAllPrepStmntUsed conn: " + conn
                    + " is not prep statement aware so prepared a proxy ps: "
                    + ps + " with section copy: " + ps.section_
                    + " bucket ids: " + bucketIds);
          }
        }
        if (psUsed == null) {
          psUsed = new HashMap();
        }
        psUsed.put(conn, ps);
      }
    }
    return psUsed;
  }

  private Map getServerAddressToRoutingObjMap(int executeType)
      throws SqlException {
    Set routingObjectInfos = this.singleHopInformation
        .getRoutingObjectInfoSet();

    Iterator itr = routingObjectInfos.iterator();

    Map serverAddressesToRoutingObject = null;
    HashSet allRoutingObjects = null;
    
    if (this.routingObjectSet != null) {
      resolver.setTestSet(routingObjectSet);
    }

    while (itr.hasNext()) {
      AbstractRoutingObjectInfo rInfo = (AbstractRoutingObjectInfo)itr.next();
      if (this.actualParamTypes == null) {
        this.actualParamTypes = getActualParamTypes(this.routingObjectSet);
      }
      try {
        rInfo.setActualValue(this.parameters_, this.crossConverter);
      } catch (SQLException e) {
        Throwable sqle = e.getCause();
        throw(SqlException)sqle;
      }
      HashSet routingObjects = null;
      Integer robj = null;
      if (rInfo.isListRoutingObjectInfo()) {
        routingObjects = resolver.getListOfRoutingObjects(rInfo,
            this.singleHopInformation);
        if (routingObjects == null) {
          if (SanityManager.TraceSingleHop) {
            SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
                "SingleHopPreparedStatement::flowExecute no routingObject "
                    + "determined for list routing object info: " + rInfo
                    + " so returning null for default execution");
          }
          return null;
        }
        else {
          if (allRoutingObjects == null) {
            allRoutingObjects = new HashSet();
          }
          allRoutingObjects.addAll(routingObjects);
        }
      }
      else {
        robj = (Integer)resolver.getRoutingObject(rInfo,
            this.singleHopInformation,
            this.singleHopInformation.getRequiresSerializedHash());
        if (allRoutingObjects == null) {
          allRoutingObjects = new HashSet();
        }
        if (this.routingObjectSet != null) {
          this.routingObjectSet.add(robj);
        }
        int bid = robj.intValue() % this.noOfBuckets;
        int bucketId = Math.abs(bid);
        allRoutingObjects.add(ClientSharedUtils.getJdkHelper().newInteger(
            bucketId));
      }
    }

    Iterator robjItr = allRoutingObjects.iterator();
    while (robjItr.hasNext()) {
      if (serverAddressesToRoutingObject == null) {
        serverAddressesToRoutingObject = new HashMap();
      }
      boolean ret = processBucketId((Integer)robjItr.next(),
          useOnlyPrimary, serverAddressesToRoutingObject, executeType);
      if (!ret) {
        return null;
      }
    }
    
    return serverAddressesToRoutingObject;
  }

  private int[] getActualParamTypes(Set routingObjectSet2) {
    // TODO Auto-generated method stub
    return null;
  }

  public static void sleepForRetry(Connection connection_) throws SqlException {
    if (SanityManager.TraceSingleHop) {
      SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
          "SingleHopPreparedStatement::sleepForRetry sleeping for "
              + SharedUtils.HA_RETRY_SLEEP_MILLIS + " milliseconds");
    }
    try {
      Thread.sleep(SharedUtils.HA_RETRY_SLEEP_MILLIS);
    } catch (InterruptedException ie) {
      throw new SqlException(connection_.agent_.logWriter_,
          new ClientMessageId(SQLState.JAVA_EXCEPTION),
          ie.getClass().getName(), ie.getMessage(), ie);
    }
  }

  private void setBucketToServerMappingInfo(String bucketToServerMappingStr) {
    if (bucketToServerMappingStr != null) {
      String[] arr = bucketToServerMappingStr.split(":");
      this.noOfBuckets = Integer.parseInt(arr[0]);
      this.redundancy = Integer.parseInt(arr[1]);
      this.numberOfPossibleServerArray = this.redundancy + 1;
      this.primaryServers = new HostPort[this.noOfBuckets];
      this.secondaryServers = new HostPort[this.redundancy][this.noOfBuckets];
      String bucketsServers = arr[2];
      String[] newarr = bucketsServers.split("\\|");
      for (int i = 0; i < newarr.length; i++) {
        String[] aBucketInfo = newarr[i].split(";");
        int bid = Integer.parseInt(aBucketInfo[0]);
        if (!aBucketInfo[1].equals("null")) {
          this.primaryServers[bid] = returnHostPortFromServerString(aBucketInfo[1]);
        }
        String secondaryservers = ";";
        for (int j = 0; j < this.redundancy; j++) {
          String redndntBucketServer = aBucketInfo[2 + j];
          if (!redndntBucketServer.equals("null")) {
            this.secondaryServers[j][bid] = returnHostPortFromServerString(redndntBucketServer);
            if (SanityManager.TraceSingleHop) {
              secondaryservers += this.secondaryServers[j][bid] + " ; ";
            }
          }
        }
        if (SanityManager.TraceSingleHop) {
          SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
              "SingleHopPreparedStatement::setBucketToServerMappingInfo for bucket id: "
                  + bid + " primary server: " + this.primaryServers[bid]
                  + " secondary servers: " + secondaryservers);
        }
      }
      if (SanityManager.TraceSingleHop) {
        SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
            "SingleHopPreparedStatement::setBucketToServerMappingInfo2 num Buckets: "
                + this.noOfBuckets + ", redundancy: " + this.redundancy
                + ", number of possible arrays: "
                + this.numberOfPossibleServerArray + ", primary servers: "
                + this.primaryServers + "");
      }
    }
  }

  @Override
  protected void cleanSingleHopPrepStmnt() throws SqlException {
    doCleanupsOfPreviousExecute();
  }

  private void doCleanupsOfPreviousExecute() throws SqlException {
    if (this.psArray != null && this.psArray.length > 0) {
      for (int i = 0; i < this.psArray.length; i++) {
        this.psArray[i].readCloseResultSets(true);
        this.psArray[i].markResultSetsClosed(true);
      }
    }
    this.shopResultSet = null;
    if (this.singleHopInformation != null
        && this.singleHopInformation.isHoppable()) {
      readCloseResultSets(true);
      markResultSetsClosed(true);
    }
  }

  private void reset() throws SqlException {
    doCleanupsOfPreviousExecute();
    this.psArray = null;
    this.primaryServers = null;
    this.secondaryServers = null;
    this.numberOfPossibleServerArray = 0;
  }

  public Set getRoutingObjectSet() {
    return this.routingObjectSet;
  }

  public void createNewSetForTesting() {
    this.routingObjectSet = new HashSet();
  }

  public void setRoutingObjectSetToNull() {
    this.routingObjectSet = null;
  }

  private boolean processBucketId(Integer robj, boolean useOnlyPrimary,
      Map serverAddressesToRoutingObject, int executeType) throws SqlException {
    int bucketId = robj.intValue();
    HostPort server = null;
    //bucketId = Math.abs(bucketId % this.noOfBuckets);
    if (SanityManager.TraceSingleHop) {
      SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
          "SingleHopPreparedStatement::processBucketId called for bucketid "
              + robj);
    }

    final JdkHelper helper = ClientSharedUtils.getJdkHelper();
    if (useOnlyPrimary) {
      server = this.primaryServers[bucketId];
      HashSet routingObjSet = (HashSet)serverAddressesToRoutingObject
          .get(server);
      if (routingObjSet != null) {
        routingObjSet.add(helper.newInteger(bucketId));
      }
      else {
        HashSet newRoutingObjSet = new HashSet();
        newRoutingObjSet.add(helper.newInteger(bucketId));
        serverAddressesToRoutingObject.put(server, newRoutingObjSet);
      }
    }
    else {
      if (this.numberOfPossibleServerArray == 1) {
        server = this.primaryServers[bucketId];
        HashSet list = (HashSet)serverAddressesToRoutingObject.get(server);
        if (list == null) {
          list = new HashSet();
          serverAddressesToRoutingObject.put(server, list);
        }
        list.add(helper.newInteger(bucketId));
      }
      else {
        server = selectBestServer(serverAddressesToRoutingObject, bucketId);
      }
    }
    // TODO: KN need to identify whether null is returning because there is no
    // network server
    // or server strings were null
    if (server == null) {
      if (SanityManager.TraceSingleHop) {
        SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
            "SingleHopPreparedStatement::processBucketId no server "
                + "obtained for routing object: " + robj
                + " so defaulting to no single hop mode");
      }
      return false;
    }
    return true;
  }

  public boolean wasSingleHopCalledInExecution() {
    return !this.superFlowExecuteCalled_;
  }

  @Override
  public java.sql.ResultSet getResultSet() throws SQLException {
    if (this.superFlowExecuteCalled_) {
      this.shopResultSet = super.getResultSet();
    }

    //SanityManager.DEBUG_PRINT("KN: ", "getResultSet called with shopResultSet = " + this.shopResultSet + ", this.psarray=" + this.psArray, new Exception());
    // If psArray is null, this was a prepared UDI statement and only
    // contains the update count
    if ((this.shopResultSet == null) && (this.psArray != null)) {
      NetResultSet nrs0 = this.psArray[0].getSuperResultSet();
      this.shopResultSet = ClientDRDADriver.getFactory().getSingleHopResultSet(
          nrs0, this.psArray, this.dsConnInfo);
    }
    return this.shopResultSet;
  }

  @Override
  public NetResultSet getSuperResultSet() throws SQLException {
    return (NetResultSet)super.getResultSet();
  }

  private void setParametersAndOtherValuesEqualToThisPS(PreparedStatement ps) {
    ps.parameterRegistered_ = this.parameterRegistered_;
    ps.parameters_ = this.parameters_;
    ps.isCatalogQuery_ = this.isCatalogQuery_;
    ps.parameterSet_ = this.parameterSet_;
    ps.resultSetConcurrency_ = this.resultSetConcurrency_;
    ps.resultSetHoldability_ = this.resultSetHoldability_;
    ps.resultSetType_ = this.resultSetType_;
    // This copying is necessary because the parameter metadata
    // remains uninitialized of the internal prepared statements
    // specially the clientParamtertype_ which gets built up
    // as the user does setParameter methods on the base prepared
    // statement.
    if (this.parameterMetaData_ != null) {
      this.parameterMetaData_.copyTo(ps.parameterMetaData_);
      ps.parameterMetaData_.clientParamtertype_ = 
        makeNewClientParamTypeFrom(this.clientParamTypeAtFirstExecute);
    }
    else {
      ps.parameterMetaData_ = null;
    }
    ps.internalPreparedStatement = true;
  }

  private static void setParametersToNullInTheInternalPS(PreparedStatement ps) {
    ps.parameterRegistered_ = null;
    ps.parameters_ = null;
    ps.parameterSet_ = null;
    ps.parameterMetaData_ = null;
    // This will make sure that this ps is removed from conn aware list in shop
    ps.openOnClient_ = false;
  }

  // best in the sense that if possible if most of the buckets can be
  // catered by a single server then it will be good. (hopefully)
  private HostPort selectBestServer(Map serverAddressesToRoutingObject,
      int bucketId) {
    try {
      HostPort bestServer = null;
      HostPort[] serverArray = null;
      if (this.randomServerSelector == null) {
        this.randomServerSelector = new Random();
      }
      // select a random out of primary and secondaries
      // If the random server has already been selected for some other
      // bucket then prefer that and return it.
      // else select remaining ones one by one and see if already in the map.
      // If yes then return it else at the end randomly select one.

      final boolean serverMapEmpty = serverAddressesToRoutingObject.isEmpty();

      int numOfRemainingServerArray = this.numberOfPossibleServerArray;
      if (this.numberOfPossibleServerArray == 1) {
        return this.primaryServers[bucketId];
      }

      final JdkHelper helper = ClientSharedUtils.getJdkHelper();
      while (numOfRemainingServerArray > 0) {
        HostPort server = null;
        int randArrayNumber = this.randomServerSelector
            .nextInt(this.numberOfPossibleServerArray);
        if (randArrayNumber < this.redundancy) {
          serverArray = this.secondaryServers[randArrayNumber];
        }
        else {
          serverArray = this.primaryServers;
        }

        if (this.alreadyConsideredList.contains(serverArray)) {
          continue;
        }
        server = serverArray[bucketId];
        if (server == null) {
          numOfRemainingServerArray--;
          continue;
        }

        if (serverMapEmpty) {
          HashSet values = new HashSet();
          values.add(helper.newInteger(bucketId));
          bestServer = server;
          serverAddressesToRoutingObject.put(bestServer, values);
          break;
        }

        Object routingObjectsList = serverAddressesToRoutingObject.get(server);
        if (routingObjectsList != null) {
          HashSet listOfRoutingObjects = (HashSet)routingObjectsList;
          listOfRoutingObjects.add(helper.newInteger(bucketId));
          bestServer = server;
          break;
        }
        else {
          this.alreadyConsideredList.add(serverArray);
          numOfRemainingServerArray--;
        }
      }

      // bestServer is still null means that we were not able to find a
      // server which has already been considered for other routing objects
      // and in that case pick one randomly from already considered list
      if (bestServer == null) {
        bestServer = selectAServerFromAlreadyConsideredList(bucketId);
        if (bestServer == null) {
          return null;
        }
        HashSet values = new HashSet();
        values.add(helper.newInteger(bucketId));
        bestServer = serverArray[bucketId];
        serverAddressesToRoutingObject.put(bestServer, values);
      }
      return bestServer;
    } finally {
      this.alreadyConsideredList.clear();
    }
  }

  private HostPort selectAServerFromAlreadyConsideredList(int bucketId) {
    Iterator itr = this.alreadyConsideredList.iterator();
    HostPort server = null;
    while (itr.hasNext()) {
      HostPort[] serverArray = (HostPort[])itr.next();
      server = serverArray[bucketId];
      if (server != null) {
        break;
      }
    }
    return server;
  }

  private void returnConnection(NetConnection conn) {
    this.poolConns.add(conn);
  }

  private void returnConnectionsToPool() throws SqlException {
    for (NetConnection conn : poolConns) {
      if (!conn.isClosed()) {
        boolean gotException = conn.gotException_;
        if (gotException) {
          if (SanityManager.TraceSingleHop) {
            SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
                "SingleHopPreparedStatement::returnConnectionsToPool resetting connection: "
                    + conn);
          }
          conn.reset(conn.agent_.logWriter_);
          conn.gotException_ = false;
        }
        if (gotException || !(this.sqlMode_ == isQuery__)) {
          dsConnInfo.returnConnection(conn);
        }
        // In case of queries the connection will be returned to pool
        // once the result set is closed or the SingleHopResultSet is GC'ed
      }
      else {
        try {
          dsConnInfo.removeConnection(conn);
        } catch (InterruptedException ie) {
          throw new SqlException(connection_.agent_.logWriter_,
              new ClientMessageId(SQLState.JAVA_EXCEPTION), ie.getClass()
                  .getName(), ie.getMessage(), ie);
        }
      }
    }
  }
  
  private void removeAllConnectionsUsedFromPoolNoThrow() {
    for (NetConnection conn : poolConns) {
      try {
        if (conn.gotException_) {
          dsConnInfo.removeConnection(conn);
        }
      } catch (InterruptedException ie) {
        // ignore
      }
    }
  }
  
  private void removeConnection(NetConnection conn) throws SqlException {
    try {
      dsConnInfo.removeConnection(conn);
      if (SanityManager.TraceSingleHop) {
        SanityManager.DEBUG_PRINT(SanityManager.TRACE_SINGLE_HOP,
            "SingleHopPreparedStatement::removeConnection for connection: ");
      }
      if (this.sqlMode_ == isQuery__) {
        if (this.shopResultSet != null && this.shopResultSet instanceof SingleHopResultSet) {
          SingleHopResultSet rs = (SingleHopResultSet)this.shopResultSet;
          FinalizeSingleHopResultSet finalizer = rs.getFinalizer();
          if (finalizer != null) {
            finalizer.nullConnection(conn);
          }
        }
      }
    } catch (InterruptedException ie) {
      throw new SqlException(connection_.agent_.logWriter_,
          new ClientMessageId(SQLState.JAVA_EXCEPTION),
          ie.getClass().getName(), ie.getMessage(), ie);
    }
  }

  private Connection getTheConnectionForThisServer(HostPort server,
      boolean alwaysCreateNewConnection) throws SQLException, SqlException {
    if (server == null) {
      return null;
    }
    if (this.connection_.useBoundedQueuePool_) {
      Connection conn = getConnectionFromBoundedQueue(server);
      
      // Inherit autocommit and transaction isolation from top level connection
      // Set only if different.
      java.sql.Connection topLevelConnection = this.getConnection();
      boolean autocommit = topLevelConnection.getAutoCommit();
      int isolation = topLevelConnection.getTransactionIsolation();
      if (autocommit != conn.getAutoCommit()) {
        conn.setAutoCommit(autocommit);
      }
      if (isolation != conn.getTransactionIsolation()) {
        conn.setTransactionIsolation(isolation);
      }
      return conn;
    }
    return null;
  }

  private Connection getConnectionFromBoundedQueue(HostPort hp)
      throws SQLException, SqlException {
    String host = hp.host_;
    String portStr = hp.port_;
    return this.dsConnInfo.getConnectionForThisServerURLFromBQ(
        host, portStr, this.connection_);
  }

  private static class HostPort {
    final String host_;

    final String port_;

    HostPort(String host, String port) {
      this.host_ = host;
      this.port_ = port;
    }

    public String getHost() {
      return this.host_;
    }

    public String getPort() {
      return this.port_;
    }

    @Override
    public boolean equals(Object other) {
      if (other == null) {
        return false;
      }

      if (other instanceof HostPort) {
        if (this.host_.equals(((HostPort)other).host_)
            && this.port_.equals(((HostPort)other).port_)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public int hashCode() {
      if (this.host_ == null || this.port_ == null) {
        return 0;
      }
      return (this.host_.hashCode() ^ this.port_.hashCode());
    }

    @Override
    public String toString() {
      return this.host_ + ":" + this.port_;
    }
  }
}
