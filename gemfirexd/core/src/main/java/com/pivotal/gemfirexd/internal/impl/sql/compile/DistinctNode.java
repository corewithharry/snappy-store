/*

   Derby - Class com.pivotal.gemfirexd.internal.impl.sql.compile.DistinctNode

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

package	com.pivotal.gemfirexd.internal.impl.sql.compile;









import com.pivotal.gemfirexd.internal.engine.distributed.metadata.DistinctQueryInfo;
import com.pivotal.gemfirexd.internal.engine.distributed.metadata.OrderByQueryInfo;
import com.pivotal.gemfirexd.internal.engine.distributed.metadata.QueryInfo;
import com.pivotal.gemfirexd.internal.engine.distributed.metadata.QueryInfoContext;
import com.pivotal.gemfirexd.internal.iapi.error.StandardException;
import com.pivotal.gemfirexd.internal.iapi.reference.ClassName;
import com.pivotal.gemfirexd.internal.iapi.services.classfile.VMOpcode;
import com.pivotal.gemfirexd.internal.iapi.services.compiler.MethodBuilder;
import com.pivotal.gemfirexd.internal.iapi.services.sanity.SanityManager;
import com.pivotal.gemfirexd.internal.iapi.sql.Activation;
import com.pivotal.gemfirexd.internal.iapi.sql.ResultSet;
import com.pivotal.gemfirexd.internal.iapi.sql.compile.C_NodeTypes;
import com.pivotal.gemfirexd.internal.iapi.sql.compile.CostEstimate;
import com.pivotal.gemfirexd.internal.iapi.sql.compile.Optimizable;
import com.pivotal.gemfirexd.internal.iapi.sql.compile.OptimizableList;
import com.pivotal.gemfirexd.internal.iapi.sql.compile.OptimizablePredicate;
import com.pivotal.gemfirexd.internal.iapi.sql.compile.OptimizablePredicateList;
import com.pivotal.gemfirexd.internal.iapi.sql.compile.Optimizer;
import com.pivotal.gemfirexd.internal.iapi.sql.compile.RequiredRowOrdering;
import com.pivotal.gemfirexd.internal.iapi.sql.compile.RowOrdering;
import com.pivotal.gemfirexd.internal.iapi.sql.compile.Visitable;
import com.pivotal.gemfirexd.internal.iapi.sql.compile.Visitor;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.ConglomerateDescriptor;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.DataDictionary;
import com.pivotal.gemfirexd.internal.iapi.util.JBitSet;
import com.pivotal.gemfirexd.internal.impl.sql.compile.ActivationClassBuilder;


import java.util.Properties;
import java.util.Vector;

/**
 * A DistinctNode represents a result set for a disinct operation
 * on a select.  It has the same description as its input result set.
 *
 * For the most part, it simply delegates operations to its childResultSet,
 * which is currently expected to be a ProjectRestrictResultSet generated
 * for a SelectNode.
 *
 * NOTE: A DistinctNode extends FromTable since it can exist in a FromList.
 *
 */
public class DistinctNode extends SingleChildResultSetNode
{
	boolean inSortedOrder;

	/**
	 * Initializer for a DistinctNode.
	 *
	 * @param childResult	The child ResultSetNode
	 * @param inSortedOrder	Whether or not the child ResultSetNode returns its
	 *						output in sorted order.
	 * @param tableProperties	Properties list associated with the table
	 *
	 * @exception StandardException		Thrown on error
	 */
	public void init(
						Object childResult,
						Object inSortedOrder,
						Object tableProperties) throws StandardException
	{
		super.init(childResult, tableProperties);

		if (SanityManager.DEBUG)
		{
			if (!(childResult instanceof Optimizable))
			{
				SanityManager.THROWASSERT("childResult, " + childResult.getClass().getName() +
					", expected to be instanceof Optimizable");
			}
			if (!(childResult instanceof FromTable))
			{
				SanityManager.THROWASSERT("childResult, " + childResult.getClass().getName() +
					", expected to be instanceof FromTable");
			}
		}

		ResultColumnList prRCList;

		/*
			We want our own resultColumns, which are virtual columns
			pointing to the child result's columns.

			We have to have the original object in the distinct node,
			and give the underlying project the copy.
		 */

		/* We get a shallow copy of the ResultColumnList and its 
		 * ResultColumns.  (Copy maintains ResultColumn.expression for now.)
		 */
		prRCList = this.childResult.getResultColumns().copyListAndObjects();
		resultColumns = this.childResult.getResultColumns();
		this.childResult.setResultColumns(prRCList);

		/* Replace ResultColumn.expression with new VirtualColumnNodes
		 * in the DistinctNode's RCL.  (VirtualColumnNodes include
		 * pointers to source ResultSetNode, this, and source ResultColumn.)
		 */
		resultColumns.genVirtualColumnNodes(this, prRCList);

		/* Verify that we can perform a DISTINCT on the
		 * underlying tree.
		 */
		resultColumns.verifyAllOrderable();

		this.inSortedOrder = ((Boolean) inSortedOrder).booleanValue();
	}

	/*
	 *  Optimizable interface
	 */

	/**
	 * @see Optimizable#optimizeIt
	 *
	 * @exception StandardException		Thrown on error
	 */
	public CostEstimate optimizeIt(Optimizer optimizer,
									OptimizablePredicateList predList,
									CostEstimate outerCost,
									RowOrdering rowOrdering)
			throws StandardException
	{
		CostEstimate childCost =
			((Optimizable) childResult).optimizeIt(optimizer,
									predList,
									outerCost,
									rowOrdering);

		return super.optimizeIt(optimizer, predList, outerCost, rowOrdering);
	}

	/**
	 * @see Optimizable#estimateCost
	 *
	 * @exception StandardException		Thrown on error
	 */
	public CostEstimate estimateCost(OptimizablePredicateList predList,
									ConglomerateDescriptor cd,
									CostEstimate outerCost,
									Optimizer optimizer,
									RowOrdering rowOrdering)
			throws StandardException
	{
		// RESOLVE: WE NEED TO ADD IN THE COST OF SORTING HERE, AND FIGURE
		// OUT HOW MANY ROWS WILL BE ELIMINATED.
		CostEstimate childCost =
			((Optimizable) childResult).estimateCost(predList,
									cd,
									outerCost,
									optimizer,
									rowOrdering);

		costEstimate = getCostEstimate(optimizer);
		costEstimate.setCost(childCost.getEstimatedCost(),
							 childCost.rowCount(),
							 childCost.singleScanRowCount());


		/*
		** No need to use estimateCost on join strategy - that has already
		** been done on the child.
		*/
		return costEstimate;
	}

	/**
	 * @see com.pivotal.gemfirexd.internal.iapi.sql.compile.Optimizable#pushOptPredicate
	 *
	 * @exception StandardException		Thrown on error
	 */

	public boolean pushOptPredicate(OptimizablePredicate optimizablePredicate)
			throws StandardException
	{
		return false;
		// return ((Optimizable) childResult).pushOptPredicate(optimizablePredicate);
	}

	/**
	 * Convert this object to a String.  See comments in QueryTreeNode.java
	 * for how this should be done for tree printing.
	 *
	 * @return	This object as a String
	 */

	public String toString()
	{
		if (SanityManager.DEBUG)
		{
			return childResult.toString() + "\n" + super.toString();
		}
		else
		{
			return "";
		}
	}

	/**
	 * Optimize this DistinctNode.  
	 *
	 * @param dataDictionary	The DataDictionary to use for optimization
	 * @param predicates		The PredicateList to optimize.  This should
	 *							be a join predicate.
	 * @param outerRows			The number of outer joining rows
	 *
	 * @return	ResultSetNode	The top of the optimized subtree
	 *
	 * @exception StandardException		Thrown on error
	 */

	public ResultSetNode optimize(DataDictionary dataDictionary,
								  PredicateList predicates,
								  double outerRows) 
					throws StandardException
	{
		/* We need to implement this method since a PRN can appear above a
		 * SelectNode in a query tree.
		 */
		childResult = (ProjectRestrictNode) childResult.optimize(
															dataDictionary,
															predicates,
															outerRows);
		Optimizer optimizer = getOptimizer(
						(FromList) getNodeFactory().getNode(
							C_NodeTypes.FROM_LIST,
							getNodeFactory().doJoinOrderOptimization(),
							this,
							getContextManager()),
						predicates,
						dataDictionary,
						(RequiredRowOrdering) null);

		// RESOLVE: NEED TO FACTOR IN COST OF SORTING AND FIGURE OUT HOW
		// MANY ROWS HAVE BEEN ELIMINATED.
		costEstimate = optimizer.newCostEstimate();

		costEstimate.setCost(childResult.getCostEstimate().getEstimatedCost(),
							 childResult.getCostEstimate().rowCount(),
							 childResult.getCostEstimate().singleScanRowCount());

		return this;
	}

	/**
	 * Return whether or not the underlying ResultSet tree
	 * is ordered on the specified columns.
	 * RESOLVE - This method currently only considers the outermost table 
	 * of the query block.
	 *
	 * @param	crs					The specified ColumnReference[]
	 * @param	permuteOrdering		Whether or not the order of the CRs in the array can be permuted
	 * @param	fbtVector			Vector that is to be filled with the FromBaseTable	
	 *
	 * @return	Whether the underlying ResultSet tree
	 * is ordered on the specified column.
	 */
	boolean isOrderedOn(ColumnReference[] crs, boolean permuteOrdering, Vector fbtVector)
	{
		/* RESOLVE - DistinctNodes are ordered on their RCLs.
		 * Walk RCL to see if cr is 1st non-constant column in the
		 * ordered result.
		 */
		return false;
	}

    /**
     * generate the distinct result set operating over the source
	 * resultset.
     *
	 * @exception StandardException		Thrown on error
     */
	public void generate(ActivationClassBuilder acb,
								MethodBuilder mb)
							throws StandardException
	{
		/* Get the next ResultSet#, so we can number this ResultSetNode, its
		 * ResultColumnList and ResultSet.
		 */
		assignResultSetNumber();

		// Get the final cost estimate based on the child's cost.
		costEstimate = childResult.getFinalCostEstimate();

		/*
			create the orderItem and stuff it in.
		 */
		int orderItem = acb.addItem(acb.getColumnOrdering(resultColumns));

		/* Generate the SortResultSet:
		 *	arg1: childExpress - Expression for childResultSet
		 *  arg2: distinct - true, of course
		 *  arg3: isInSortedOrder - is the source result set in sorted order
		 *  arg4: orderItem - entry in saved objects for the ordering
		 *  arg5: rowAllocator - method to construct rows for fetching
		 *			from the sort
		 *  arg6: row size
		 *  arg7: resultSetNumber
		 */

		acb.pushGetResultSetFactoryExpression(mb);

		childResult.generate(acb, mb);
		mb.push(true);
		mb.push(inSortedOrder);
		mb.push(orderItem);
		resultColumns.generateHolder(acb, mb);
		mb.push(resultColumns.getTotalColumnSize());
		mb.push(resultSetNumber);
		mb.push(costEstimate.rowCount());
		mb.push(costEstimate.getEstimatedCost());

		mb.callMethod(VMOpcode.INVOKEINTERFACE, (String) null, "getSortResultSet",
                ClassName.NoPutResultSet, 9);
	}
	
        //GemStone changes BEGIN
        /**
         * Returns the QueryInfo object reprsenting the queries order by
         * list.
         */
        @Override
        public QueryInfo computeQueryInfo(QueryInfoContext qic) throws StandardException {
          return new DistinctQueryInfo(qic, resultColumns, 
                                       (qic.getParentPRN() != null? 
                                           qic.getParentPRN().getResultColumns()
                                            : null),
                                       true);
        }
        
        /**
         * This method will replace the current PRNs with childResult until 
         * current PRN's childResult is non-PRN.  
         */
        @Override
        public ResultSetNode getInnerMostPRN() {
          return this;
        }
        
        @Override
        public QueryInfo computeSubSelectQueryInfo(QueryInfoContext qic) 
            throws StandardException 
        {
              return ProjectRestrictNode.getSelectQueryInfo(qic, this); 
        }
        
        //TODO :Asif : Check if this can be optimized for offheap
        @Override
        protected void optimizeForOffHeap( boolean shouldOptimize) {
          this.childResult.optimizeForOffHeap(false);
        }
        //GemStone changes END
}
