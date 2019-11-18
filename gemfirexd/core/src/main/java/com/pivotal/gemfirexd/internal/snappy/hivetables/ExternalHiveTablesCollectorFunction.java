package com.pivotal.gemfirexd.internal.snappy.hivetables;

import java.util.Collection;

import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.internal.cache.ExternalTableMetaData;
import com.pivotal.gemfirexd.internal.snappy.CallbackFactoryProvider;
import com.pivotal.gemfirexd.internal.snappy.hivetables.dto.ExternalHiveTablesCollectorResult;

/**
 * Server node(s) use this function to retrieve metadata of hive tables stored
 * in external hive metastore (if configured) which is available on lead node.
 * Returns {@link ExternalHiveTablesCollectorResult}.
 */
public class ExternalHiveTablesCollectorFunction implements Function {

  public static final String ID = "ExternalHiveTablesCollectorFunction";

  @Override
  public boolean hasResult() {
    return true;
  }

  @Override
  public void execute(FunctionContext context) {
    //todo[vatsal] : pass the correct connection id from server
    Collection<ExternalTableMetaData> hiveTablesMetadata =
        CallbackFactoryProvider.getClusterCallbacks().getHiveTablesMetadata(1);
    context.getResultSender().lastResult(new ExternalHiveTablesCollectorResult(hiveTablesMetadata));
  }

  @Override
  public String getId() {
    return ID;
  }

  //todo[vatsal] : verify this return value
  @Override
  public boolean optimizeForWrite() {
    return false;
  }

  //todo[vatsal] : verify this return value
  @Override
  public boolean isHA() {
    return false;
  }
}
