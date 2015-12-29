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
package sql.generic.dmlstatements;

import java.util.List;
import java.util.Map;

/**
 * SelectOperation
 * 
 * @author Namrata Thanvi
 */

public class SelectOperation extends AbstractDMLOperation implements
		DMLOperation {
	public SelectOperation(DMLExecutor executor, String stmt)  {
		super(executor, stmt,Operation.SELECT);
	}

        public SelectOperation(DMLExecutor executor, String statement, DBRow preparedColumnMap , Map<String, DBRow> dataPopulatedForTheTable) {
          super(executor, statement,preparedColumnMap,dataPopulatedForTheTable,Operation.SELECT);
        }
	@Override
	public void execute()  {	  
		executor.query(this);

	}

	@Override
	public void generateData(List<String> columnList) {
		getColumnValuesFromDataBase(columnList);

	}
	
	void setTableNameFromStatement()
        {
          //could have multiple tables. Need to handle it later 
        }

}
