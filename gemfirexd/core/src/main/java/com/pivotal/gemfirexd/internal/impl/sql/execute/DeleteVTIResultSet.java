/*

   Derby - Class com.pivotal.gemfirexd.internal.impl.sql.execute.DeleteVTIResultSet

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

package com.pivotal.gemfirexd.internal.impl.sql.execute;



import com.pivotal.gemfirexd.internal.iapi.error.StandardException;
import com.pivotal.gemfirexd.internal.iapi.services.sanity.SanityManager;
import com.pivotal.gemfirexd.internal.iapi.sql.Activation;
import com.pivotal.gemfirexd.internal.iapi.sql.ResultDescription;
import com.pivotal.gemfirexd.internal.iapi.sql.conn.LanguageConnectionContext;
import com.pivotal.gemfirexd.internal.iapi.sql.execute.CursorResultSet;
import com.pivotal.gemfirexd.internal.iapi.sql.execute.ExecRow;
import com.pivotal.gemfirexd.internal.iapi.sql.execute.NoPutResultSet;
import com.pivotal.gemfirexd.internal.iapi.store.access.TransactionController;
import com.pivotal.gemfirexd.internal.iapi.types.SQLInteger;

import java.util.Properties;

/**
 * Delete the rows from the specified
 * base table. This will cause constraints to be checked
 * and triggers to be executed based on the c's and t's
 * compiled into the insert plan.
 */
class DeleteVTIResultSet extends DMLVTIResultSet
{

	private java.sql.ResultSet		rs;
    private TemporaryRowHolderImpl rowHolder;
    /* If the delete is deferred use a row holder to keep the list of IDs of the rows to be deleted.
     * A RowHolder is used instead of a simple list because a RowHolder will spill to disk when it becomes
     * too large. The row will consist of just one column -- an integer.
     */

    /*
     * class interface
     *
     */
    /**
     *
	 * @exception StandardException		Thrown on error
     */
    public DeleteVTIResultSet
	(
		NoPutResultSet		source,
		Activation			activation
	)
		throws StandardException
    {
		super(source, activation);
	}

	/**
		@exception StandardException Standard Derby error policy
	*/
	protected void openCore() throws StandardException
	{
		lcc.getStatementContext().setTopResultSet(this, subqueryTrackingArray);

		ExecRow row = getNextRowCore(sourceResultSet);

		if (row != null)
		{
			rs = activation.getTargetVTI();

			if (SanityManager.DEBUG)
			{
				SanityManager.ASSERT(rs != null,
					"rs expected to be non-null");
			}
		}


		/* The source does not know whether or not we are doing a
		 * deferred mode delete.  If we are, then we must clear the
		 * index scan info from the activation so that the row changer
		 * does not re-use that information (which won't be valid for
		 * a deferred mode delete).
		 */
		if (constants.deferred)
		{
			activation.clearIndexScanInfo();
            if( null == rowHolder)
                rowHolder =
                    new TemporaryRowHolderImpl(activation, new Properties());
		}

        try
        {
            while ( row != null )
            {
                if( !constants.deferred)
                    rs.deleteRow();
                else
                {
                    ExecRow rowId = new ValueRow(1);
                    rowId.setColumn( 1, new SQLInteger( rs.getRow()));
                    rowHolder.insert( rowId);
                }

                rowCount++;

                // No need to do a next on a single row source
                if (constants.singleRowSource)
                {
                    row = null;
                }
                else
                {
                    row = getNextRowCore(sourceResultSet);
                }
			}
		}
        catch (StandardException se)
        {
            throw se;
        }
        catch (Throwable t)
        {
            throw StandardException.unexpectedUserException(t);
        }

		if (constants.deferred)
		{
			CursorResultSet tempRS = rowHolder.getResultSet();
			try
			{
                ExecRow	deferredRowBuffer = null;

				tempRS.open();
				while ((deferredRowBuffer = tempRS.getNextRow()) != null)
				{
                    int rowNumber = deferredRowBuffer.getColumn( 1).getInt();
                    rs.absolute( rowNumber);
					rs.deleteRow();
				}
			}
            catch (Throwable t)
            {
                throw StandardException.unexpectedUserException(t);
            }
            finally
			{
				sourceResultSet.clearCurrentRow();
				tempRS.close(false);
			}
		}

		if (rowHolder != null)
		{
			rowHolder.close();
			// rowHolder kept across opens
		}
    } // end of openCore

  @Override
  public void accept(ResultSetStatisticsVisitor visitor) {
    visitor.visit(this);
  }
}
