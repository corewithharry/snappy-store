/*

   Derby - Class com.pivotal.gemfirexd.internal.impl.sql.execute.HashLeftOuterJoinResultSet

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

/*
 * Changes for GemFireXD distributed data platform (some marked by "GemStone changes")
 *
 * Portions Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
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

package com.pivotal.gemfirexd.internal.impl.sql.execute;

import com.pivotal.gemfirexd.internal.iapi.error.StandardException;
import com.pivotal.gemfirexd.internal.iapi.services.loader.GeneratedMethod;
import com.pivotal.gemfirexd.internal.iapi.sql.Activation;
import com.pivotal.gemfirexd.internal.iapi.sql.execute.ExecRow;
import com.pivotal.gemfirexd.internal.iapi.sql.execute.NoPutResultSet;


/**
 * Left outer join using hash join of 2 arbitrary result sets.
 * Simple subclass of nested loop left outer join, differentiated
 * to ease RunTimeStatistics output generation.
 */
class HashLeftOuterJoinResultSet extends NestedLoopLeftOuterJoinResultSet
{
    HashLeftOuterJoinResultSet(
						NoPutResultSet leftResultSet,
						int leftNumCols,
						NoPutResultSet rightResultSet,
						int rightNumCols,
						Activation activation,
						GeneratedMethod restriction,
						int resultSetNumber,
						GeneratedMethod emptyRowFun,
						boolean wasRightOuterJoin,
					    boolean oneRowRightSide,
					    boolean notExistsRightSide,
 					    double optimizerEstimatedRowCount,
						double optimizerEstimatedCost,
						String userSuppliedOptimizerOverrides, 
                                                // GemStone changes BEGIN
                                                int leftResultColumnNames, 
                                                int rightResultColumnNames
                                                // GemStone changes END
						)
    {
		super(leftResultSet, leftNumCols, rightResultSet, rightNumCols,
			  activation, restriction, resultSetNumber, 
			  emptyRowFun, wasRightOuterJoin,
			  oneRowRightSide, notExistsRightSide,
			  optimizerEstimatedRowCount, optimizerEstimatedCost, 
			  userSuppliedOptimizerOverrides,
                          // GemStone changes BEGIN
                          leftResultColumnNames, 
                          rightResultColumnNames
                          // GemStone changes END
			  );
    }
  // GemStone changes BEGIN
  // just for the stack to show up the join type appropriately.
    public ExecRow  getNextRowCore() throws StandardException
    {
      return super.getNextRowCore();
    }
    
    @Override
    public StringBuilder buildQueryPlan(StringBuilder builder,
        PlanUtils.Context context) {
      
      final boolean isSuccess = context.setNested();
      assert isSuccess : "must be success as this is the leaf as of now.";
      
      super.buildQueryPlan(builder, context);
      
      PlanUtils.xmlTermTag(builder, context, PlanUtils.OP_JOIN_HASH_LO);

      endBuildQueryPlan(builder, context);
      
      PlanUtils.xmlCloseTag(builder, context, this);        
      
      return builder;
    }
  // GemStone changes END
}
