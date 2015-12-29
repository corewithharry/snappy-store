/* GemFireXDDialect for Hibernate. 
 *
 * Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
/*
 * Derived from sources of Hibernate DerbyDialect with the license below.
 *
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>
 */
package com.pivotal.gemfirexd.hibernate.v3;

import java.sql.SQLException;

import org.hibernate.Hibernate;
import org.hibernate.JDBCException;
/*import org.hibernate.dialect.function.AnsiTrimFunction;
import org.hibernate.dialect.function.DerbyConcatFunction;*/
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.NvlFunction;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.exception.DataException;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.exception.SQLExceptionConverter;
import org.hibernate.exception.JDBCExceptionHelper;

/*
 * Hibernate V3.x
 */
public class GemFireXDDialect extends com.pivotal.gemfirexd.hibernate.GemFireXDDialectBase {
	public GemFireXDDialect() {
		super();
	        LOG.info("GemFireXDDialect for Hibernate 3 initialized.");
	              
/*		registerFunction("trim", new AnsiTrimFunction());
		registerFunction("concat", new DerbyConcatFunction());*/

                registerFunction("value", new StandardSQLFunction("coalesce"));
                registerFunction("nvl", new NvlFunction());
                registerFunction("groups", new StandardSQLFunction("GROUPS",
                    Hibernate.STRING));
                registerFunction("dsid", new StandardSQLFunction("DSID", Hibernate.STRING));
                registerFunction("groupsintersection", new StandardSQLFunction(
                    "GROUPSINTERSECTION", Hibernate.STRING));
                registerFunction("groupsintersect", new StandardSQLFunction(
                    "GROUPSINTERSECT", Hibernate.BOOLEAN));
                registerFunction("groupsunion", new StandardSQLFunction("GROUPSUNION",
                    Hibernate.STRING));
                registerFunction("longint", new StandardSQLFunction("bigint",
                    Hibernate.LONG));
                registerFunction("int", new StandardSQLFunction("integer",
                    Hibernate.INTEGER));
                registerFunction("pi", new StandardSQLFunction("pi", Hibernate.DOUBLE));
                registerFunction("random", new NoArgSQLFunction("random", Hibernate.DOUBLE));
                registerFunction("rand", new StandardSQLFunction("rand", Hibernate.DOUBLE));// override
                registerFunction("sinh", new StandardSQLFunction("sinh", Hibernate.DOUBLE));
                registerFunction("cosh", new StandardSQLFunction("cosh", Hibernate.DOUBLE));
                registerFunction("tanh", new StandardSQLFunction("tanh", Hibernate.DOUBLE));
                registerFunction("user", new NoArgSQLFunction("USER", Hibernate.STRING,
                    false));
                registerFunction("current_user", new NoArgSQLFunction("CURRENT_USER",
                    Hibernate.STRING, false));
                registerFunction("session_user", new NoArgSQLFunction("SESSION_USER",
                    Hibernate.STRING, false));
                registerFunction("current isolation", new NoArgSQLFunction(
                    "CURRENT ISOLATION", Hibernate.STRING, false));
                registerFunction("current_role", new NoArgSQLFunction("CURRENT_ROLE",
                    Hibernate.STRING, false));
                registerFunction("current schema", new NoArgSQLFunction("CURRENT SCHEMA",
                    Hibernate.STRING, false));
                registerFunction("current sqlid", new NoArgSQLFunction("CURRENT SQLID",
                    Hibernate.STRING, false));
                registerFunction("xmlexists", new StandardSQLFunction("XMLEXISTS",
                    Hibernate.BOOLEAN));
                registerFunction("xmlparse", new StandardSQLFunction("XMLPARSE",
                    Hibernate.TEXT));
                registerFunction("xmlquery", new StandardSQLFunction("XMLQUERY",
                    Hibernate.STRING));
                registerFunction("xmlserialize", new StandardSQLFunction("XMLSERIALIZE",
                    Hibernate.STRING));
                registerFunction("get_current_connection", new NoArgSQLFunction(
                    "GET_CURRENT_CONNECTION", Hibernate.BINARY, true));
                registerFunction("identity_val_local", new NoArgSQLFunction(
                    "IDENTITY_VAL_LOCAL", Hibernate.BINARY, true));
	}
	
	@Override
	public boolean supportsPooledSequences() {
		return false;
	}

	@Override
	public boolean supportsUnboundedLobLocatorMaterialization() {
		return false;
	}
	
	@Override
	public boolean doesReadCommittedCauseWritersToBlockReaders() {
		return false;
	}
	
	/*
	 *  Override
	 */
	public String getWriteLockString(int timeout) {
		return " for update";
	}

	/*
	 *  Override
	 */
	public String getReadLockString(int timeout) {
		return " for update";
	}
	
        @Override
        public SQLExceptionConverter buildSQLExceptionConverter() {
          return new SQLExceptionConverter() {
            @Override
            public JDBCException convert(SQLException sqlException, String message,
                String sql) {
              final String sqlState = JDBCExceptionHelper
                  .extractSqlState(sqlException);
              if (sqlState != null) {
                if (SQL_GRAMMAR_CATEGORIES.contains(sqlState)) {
                  return new SQLGrammarException(message, sqlException, sql);
                }
                else if (DATA_CATEGORIES.contains(sqlState)) {
                  return new DataException(message, sqlException, sql);
                }
                else if (LOCK_ACQUISITION_CATEGORIES.contains(sqlState)) {
                  return new LockAcquisitionException(message, sqlException, sql);
                }
              }
              return null;
            }
          };
        }
}
