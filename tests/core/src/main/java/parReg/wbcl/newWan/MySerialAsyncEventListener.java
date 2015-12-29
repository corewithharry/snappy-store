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
package parReg.wbcl.newWan; 

import hydra.CacheHelper;
import hydra.Log;
import hydra.MasterController;
import hydra.ProcessMgr;
import hydra.RegionHelper;
import hydra.RemoteTestModule;
import hydra.TestConfig;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;

import parReg.wbcl.WBCLTestBB;
import util.TestException;
import util.TestHelper;
import util.TxHelper;

import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.EntryExistsException;
import com.gemstone.gemfire.cache.EntryNotFoundException;
import com.gemstone.gemfire.cache.Operation;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.asyncqueue.AsyncEvent;
import com.gemstone.gemfire.cache.asyncqueue.AsyncEventListener;
import com.gemstone.gemfire.cache.wan.EventSequenceID;

/** MySerialAsyncEventListener (AsyncEventListener)
 * 
 * @author Rahul Diyewar
 * @since 7.0
 */
public class MySerialAsyncEventListener implements
    AsyncEventListener<Object, Object>, Declarable {

// updated with current time as each event processed by the WBCLEventListener
public static int lastEventTime;    

/** The process ID of the VM that created this listener */
public int whereIWasRegistered;
//protected Executor serialExecutor;

/** noArg constructor 
 */
public MySerialAsyncEventListener() {
   whereIWasRegistered = ProcessMgr.getProcessId();
//   serialExecutor = Executors.newSingleThreadExecutor();
}

//----------------------------------------------------------------------------
// GatewayEventListener API
//----------------------------------------------------------------------------

/**
 * process events
 */
public boolean processEvents(List<AsyncEvent<Object, Object>> events) {
  boolean status = false;

  Log.getLogWriter().info("processEvents received List with " + events.size() + " GatewayEvents");
  // Fail 10% of the time ... ensure that we replay these events
    if (TestConfig.tab().getRandGen().nextInt(1, 100) < 99) {
      status = true;
      for (Iterator i = events.iterator(); i.hasNext();) {
        AsyncEvent event = (AsyncEvent)i.next();  
        logCall("processEvents", event);
        WBCLTestBB.getBB().getSharedCounters().setIfLarger(WBCLTestBB.lastEventTime, System.currentTimeMillis());
        
            try {
              // use the event to update the local wbcl region
              final Region wbclRegion = CacheHelper.getCache().getRegion("wbclRegion");
              final Object key = event.getKey();
              final Object value = event.getDeserializedValue();
              final Operation op = event.getOperation();

              // serialExecutor.execute(new Runnable() {
              // public void run() {
              if (op.isCreate()) {
                try {
                  Log.getLogWriter().info("Creating key/value pair (" + key + ", " + value + ") in region named " + wbclRegion.getName());
                  wbclRegion.create(key, value);
                  Log.getLogWriter().info("Done creating key/value pair (" + key + ", " + value + ") in region named " + wbclRegion.getName());
                }
                catch (EntryExistsException e) {
                  Log.getLogWriter().info("Caught " + e + ", expected with concurrent operations; continuing with test");
                }
              }
              else if (op.isUpdate()) {
                Log.getLogWriter().info("Putting key/value pair (" + key + ", " + value + ") in region named " + wbclRegion.getName());
                wbclRegion.put(key, value);
                Log.getLogWriter().info("Done Putting key/value pair (" + key + ", " + value + ") in region named " + wbclRegion.getName());
              }
              else if (op.isInvalidate()) {
                throwException("Unexpected INVALIDATE encounted in WBCLEventListener " + op.toString() + ", " + TestHelper.getStackTrace());
              }
              else if (op.isDestroy()) {
                Log.getLogWriter().info("Destroying key/value pair (" + key + ", " + value + ") in region named " + wbclRegion.getName());
                try {
                  wbclRegion.destroy(key);
                }
                catch (EntryNotFoundException e) {
                  Log.getLogWriter().info("Caught " + e + ", expected with concurrent operations; continuing with test");
                }
                Log.getLogWriter().info("Done destroying key/value pair (" + key + ", " + value + ") in region named " + wbclRegion.getName());
              }
              // }
              // });
            }
            catch (Exception e) {
              status = false;
              throwException("WBCL Listener caught unexpected Exception " + e + ", " + TestHelper.getStackTrace(e));
            }            
      }
    }
  if (status) {
    Log.getLogWriter().info("WBCLEventListener processed batch of " + events.size() + " events, returning " + status);
  } else {
    Log.getLogWriter().info("WBCLEventListener DID NOT process batch of " + events.size() + " events, returning " + status);
  }
  return status;
}

public void init(java.util.Properties prop) {
   logCall("init(Properties)", null);
}

public void close() {
   logCall("close", null);
}

/** 
 * Utility method to write an Exception string to the Event Blackboard and 
 * to also throw an exception containing the same string.
 *
 * @param errStr String to log, post to EventBB and throw
 * @throws TestException containing the passed in String
 *
 * @see util.TestHelper.checkForEventError
 */
protected void throwException(String errStr) {
      hydra.blackboard.SharedMap aMap = event.EventBB.getBB().getSharedMap();
      aMap.put(TestHelper.EVENT_ERROR_KEY, errStr + " " + TestHelper.getStackTrace());
      Log.getLogWriter().info(errStr);
      throw new TestException(errStr);
}

/** Log that a gateway event occurred.
 *
 *  @param event The event object that was passed to the event.
 */
public String logCall(String methodName, AsyncEvent event) {
   String aStr = toString(methodName, event);
   Log.getLogWriter().info(aStr);
   return aStr;
}


/** Return a string description of the GatewayEvent.
 *
 *  @param event The AsyncEvent object that was passed to the CqListener
 *
 *  @return A String description of the invoked GatewayEvent
 */
public String toString(String methodName, AsyncEvent event) {
   StringBuffer aStr = new StringBuffer();

   aStr.append("Invoked " + this.getClass().getName() + ": " + methodName + " in " + RemoteTestModule.getMyClientName());
   aStr.append(", whereIWasRegistered: " + whereIWasRegistered);

   if (event == null) {
     return aStr.toString();
   }
   aStr.append(", Event:" + event);
   return aStr.toString();
}

  /** Inner class for serializing (ordering) application of updates 
   *  based on gateway events.
   */
  class SerialExecutor implements Executor {
     final Queue<Runnable> tasks = new ArrayDeque<Runnable>();
     final Executor executor;
     Runnable active;

     SerialExecutor(Executor executor) {
         this.executor = executor;
     }

     public synchronized void execute(final Runnable r) {
         tasks.offer(new Runnable() {
             public void run() {
                 try {
                     r.run();
                 } finally {
                     scheduleNext();
                 }
             }
         });
         if (active == null) {
             scheduleNext();
         }
     }

     protected synchronized void scheduleNext() {
         if ((active = tasks.poll()) != null) {
             executor.execute(active);
         }
     }
 }

/** Return when no events have been invoked for the given number of seconds.
 *
 *  @param sleepMS The number of milliseonds to sleep between checks for
 *         silence.
 */
public static void waitForSilence(long desiredSilenceSec, long sleepMS) {
   Log.getLogWriter().info("Waiting for a period of silence for " + desiredSilenceSec + " seconds...");
   long desiredSilenceMS = desiredSilenceSec * 1000;

   long silenceStartTime = System.currentTimeMillis();
   long currentTime = System.currentTimeMillis();
   long lastEventTime = WBCLTestBB.getBB().getSharedCounters().read(WBCLTestBB.lastEventTime);

   while (currentTime - silenceStartTime < desiredSilenceMS) {
      try {
         Thread.sleep(sleepMS);
      } catch (InterruptedException e) {
         throw new TestException(TestHelper.getStackTrace(e));
      }
      lastEventTime = WBCLTestBB.getBB().getSharedCounters().read(WBCLTestBB.lastEventTime);
      if (lastEventTime > silenceStartTime) {
         // restart the wait
         silenceStartTime = lastEventTime;
      }
      currentTime = System.currentTimeMillis();
   }
   long duration = currentTime - silenceStartTime;
   Log.getLogWriter().info("Done waiting, clients have been silent for " + duration + " ms");
}

}
