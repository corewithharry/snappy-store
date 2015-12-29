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
/*
 * IndexTest.java JUnit based test
 * 
 * Created on March 9, 2005, 3:30 PM
 */

package com.gemstone.gemfire.cache.query.internal;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.query.CacheUtils; // import
                                                    // com.gemstone.gemfire.cache.query.IndexType;
import com.gemstone.gemfire.cache.query.IndexType;
import com.gemstone.gemfire.cache.query.Query;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.data.Portfolio; // import
                                                        // com.gemstone.gemfire.cache.query.facets.lang.Employee;
import java.util.*;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * 
 * @author Asif
 */
public class QueryFromClauseCanonicalizationTest extends TestCase
{
  Region region = null;

  QueryService qs = null;

  static String queries[] = {
      "SELECT DISTINCT ID, value.secId FROM /pos, getPositions where status = 'active' and ID = 0",
      "SELECT DISTINCT ID, value.secId FROM /pos, positions where status = 'active' and ID = 0",
      "SELECT DISTINCT ID, value.secId FROM /pos, getPositions() where status = 'active' and ID = 0",
      "SELECT DISTINCT ID, p.value.secId FROM /pos, getPositions('true') p where status = 'active' and ID = 0",
      "SELECT DISTINCT * FROM /pos as a, a.collectionHolderMap['0'].arr as b where a.status = 'active' and a.ID = 0",
     /* "SELECT DISTINCT * FROM /pos as a, a.positions[a.collectionHolderMap['0'][1]] as b where a.status = 'active' and a.ID = 0",*/

  };

  public QueryFromClauseCanonicalizationTest(String testName) {
    super(testName);
  }

  public static Test suite()
  {
    TestSuite suite = new TestSuite(QueryFromClauseCanonicalizationTest.class);
    return suite;
  }

  protected void setUp() throws java.lang.Exception
  {
    CacheUtils.startCache();
    region = CacheUtils.createRegion("pos", Portfolio.class);
    region.put("0", new Portfolio(0));
    region.put("1", new Portfolio(1));
    region.put("2", new Portfolio(2));
    region.put("3", new Portfolio(3));

    qs = CacheUtils.getQueryService();
  }

  protected void tearDown() throws java.lang.Exception
  {
    CacheUtils.closeCache();
  }

  public void testCanonicalizedFromClause() throws Throwable
  {  

    boolean overallTestFailed = false;
    Query q = null;
    QueryObserverImpl observer = null;
    for (int j = 0; j < queries.length; j++) {

      try {
        q = qs.newQuery(queries[j]);
        observer = new QueryObserverImpl();
        QueryObserverHolder.setInstance(observer);
        q.execute();
      }
      catch (Exception e) {
        System.err
            .println("QueryFromClauseCanonicalizationTest::testCanonicalizedFromClause.Exception in running query number="
                + j + "  Exception=" + e);
        e.printStackTrace();
        overallTestFailed = true;
        continue;
      }

      switch (j) {
      case 0:
      case 1:
      case 2:
        if (observer.clauses.get(0).toString().equals("/pos")
            && observer.clauses.get(1).toString().equals("iter1.positions")) {
          assertTrue(true);
        }
        else {
          overallTestFailed = true;
          System.err
              .println("QueryFromClauseCanonicalizationTest::testCanonicalizedFromClause.Failure in query number="
                  + j);
        }
        break;
      case 3:
        if (observer.clauses.get(0).toString().equals("/pos")
            && observer.clauses.get(1).toString().equals(
                "iter1.getPositions('true')")) {
          assertTrue(true);
        }
        else {
          overallTestFailed = true;
          System.err
              .println("QueryFromClauseCanonicalizationTest::testCanonicalizedFromClause.Failure in query number="
                  + j);
        }
        break;
      case 5:
        if (observer.clauses.get(0).toString().equals("/pos")
            && observer.clauses.get(1).toString().equals(
                "iter1.positions[iter1.collectionHolderMap[][]]")) {
          assertTrue(true);
        }
        else {
          overallTestFailed = true;
          System.err
              .println("QueryFromClauseCanonicalizationTest::testCanonicalizedFromClause.Failure in query number="
                  + j);
        }
        break;
      case 4:
        if (observer.clauses.get(0).toString().equals("/pos")
            && observer.clauses.get(1).toString().equals(
                "iter1.collectionHolderMap['0'].arr")) {
          assertTrue(true);
        }
        else {
          overallTestFailed = true;
          System.err
              .println("QueryFromClauseCanonicalizationTest::testCanonicalizedFromClause.Failure in query number="
                  + j);
        }
        break;

      }

    }
    if (overallTestFailed)
      Assert.fail();

  }

  public void testCanonicalizationOfMethod() throws Exception
  {
    QCompiler compiler = new QCompiler(CacheUtils.getLogger()
        .convertToLogWriterI18n());
    List list = compiler.compileFromClause("/pos pf");
    ExecutionContext context = new ExecutionContext(new Object[]{"bindkey"}, CacheUtils.getCache());
    context.newScope(context.assosciateScopeID());

    Iterator iter = list.iterator();
    while (iter.hasNext()) {
      CompiledIteratorDef iterDef = (CompiledIteratorDef)iter.next();
      context.addDependencies(new CompiledID("dummy"), iterDef
          .computeDependencies(context));
      RuntimeIterator rIter = iterDef.getRuntimeIterator(context);
      context.bindIterator(rIter);
      context.addToIndependentRuntimeItrMap(iterDef);
    }
    CompiledPath cp = new CompiledPath(new CompiledID("pf"), "positions");
    CompiledLiteral cl = new CompiledLiteral("key1");
    List args = new ArrayList();
    args.add(cl);
    CompiledOperation cop = new CompiledOperation(cp, "get", args);
    StringBuffer sbuff = new StringBuffer();
    cop.generateCanonicalizedExpression(sbuff, context);
    assertEquals(sbuff.toString(),"iter1.positions.get('key1')");
    
//    cp = new CompiledPath(new CompiledID("pf"), "positions");
//    CompiledBindArgument cb = new CompiledBindArgument(1);
//    args = new ArrayList();
//    args.add(cb);
//    cop = new CompiledOperation(cp, "get", args);
////    context.setBindArguments(new Object[]{"bindkey"});
//    sbuff = new StringBuffer();
//    cop.generateCanonicalizedExpression(sbuff, context);
//    assertEquals(sbuff.toString(),"iter1.positions.get('bindkey')");
//    
    
//    cp = new CompiledPath(new CompiledID("pf"), "getPositions()");
//    cb = new CompiledBindArgument(1);
//    args = new ArrayList();
//    args.add(cb);
//    cop = new CompiledOperation(cp, "get", args);
//    sbuff = new StringBuffer();
//    cop.generateCanonicalizedExpression(sbuff, context);
//    assertEquals(sbuff.toString(),"iter1.positions().get('bindkey')");
//    
//    
//    cp = new CompiledPath(new CompiledID("pf"), "getPositions");
//    cb = new CompiledBindArgument(1);
//    args = new ArrayList();
//    args.add(cb);
//    cop = new CompiledOperation(cp, "get", args);
//    sbuff = new StringBuffer();
//    cop.generateCanonicalizedExpression(sbuff, context);
//    assertEquals(sbuff.toString(),"iter1.positions.get('bindkey')");
    
    cp = new CompiledPath(new CompiledID("pf"), "getPositions");
    CompiledPath cp1 = new CompiledPath(new CompiledID("pf"),"pkid");
    args = new ArrayList();
    args.add(cp1);
    cop = new CompiledOperation(cp, "get", args);
    sbuff = new StringBuffer();
    cop.generateCanonicalizedExpression(sbuff, context);
    assertEquals(sbuff.toString(),"iter1.positions.get(iter1.pkid)");
    
    
    cp = new CompiledPath(new CompiledID("pf"), "getPositions");
    cp1 = new CompiledPath(new CompiledID("pf"),"pkid");    
    CompiledIndexOperation ciop = new CompiledIndexOperation(cp,cp1);
    sbuff = new StringBuffer();
    ciop.generateCanonicalizedExpression(sbuff, context);
    assertEquals(sbuff.toString(),"iter1.positions[iter1.pkid]");
  } 
  
  
  class QueryObserverImpl extends QueryObserverAdapter
  {
    public List clauses = new ArrayList();

    public void beforeIterationEvaluation(CompiledValue executer,
        Object currentObject)
    {
      clauses.add(((RuntimeIterator)executer).getDefinition());

    }
  }
}