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
package hydra;


/**
 * A class used to store keys for gateway sender configuration
 * settings.  The settings are used to create instances of {@link
 * GatewaySenderDescription}.
 * <p>
 * The number of description instances is gated by {@link #names}.  For other
 * parameters, if fewer values than names are given, the remaining instances
 * will use the last value in the list.  See $JTESTS/hydra/hydra.txt for more
 * details.
 * <p>
 * Unused parameters default to null, except where noted.  This uses the
 * product default, except where noted.
 * <p>
 * Values of a parameter can be set to {@link #DEFAULT}, except where noted.
 * This uses the product default, except where noted.
 * <p>
 * Values and fields can be set to {@link #NONE} where noted, with the
 * documented effect.
 * <p>
 * Values of a parameter can use oneof, range, or robing except where noted, but
 * each description created will use a fixed value chosen at test configuration
 * time.  Use as a task attribute is illegal.
 */
public class GatewaySenderPrms extends BasePrms {

  /**
   * (String(s))
   * Logical names of the gateway sender descriptions.  Each name must be
   * unique.  Defaults to null.  Not for use with oneof, range, or robing.
   * <p>
   * The names are used as prefixes for gateway sender ids generated by
   * {@link GatewaySenderHelper#createGatewaySender(String)}.
   */
  public static Long names;

  /**
   * (int(s))
   * Alert threshold for each gateway sender, in milliseconds.
   */
  public static Long alertThreshold;

  /**
   * (boolean(s))
   * Batch conflation enabled for each gateway sender.
   */
  public static Long batchConflationEnabled;

  /**
   * (int(s))
   * Batch size for each gateway sender.
   */
  public static Long batchSize;

  /**
   * (int(s))
   * Batch time interval for each gateway sender.
   */
  public static Long batchTimeInterval;

  /**
   * (String(s))
   * Name of logical disk store configuration (and actual disk store name)
   * for each gateway sender, as found in {@link DiskStorePrms#names}.
   * This is a required parameter.
   */
  public static Long diskStoreName;

  /**
   * (boolean(s))
   * Disk synchronous for each gateway sender.
   */
  public static Long diskSynchronous;

  /**
   * (int(s))
   * Dispatcher threads for each gateway sender.
   */
  public static Long dispatcherThreads;

  /**
   * (Comma-separated String(s))
   * Class names of gateway event filters for each gateway sender.  Can be
   * specified as {@link #NONE} (default).
   * <p>
   * Example: To use ClassA and ClassB for the first gateway sender, none for
   * the second gateway sender, and ClassC for the third gateway sender,
   * specify:
   *     <code>ClassA ClassB, none, ClassC</code>
   */
  public static Long gatewayEventFilters;

  /**
   * (Comma-separated String(s))
   * Class names of gateway transport filters for each gateway sender.  Can be
   * specified as {@link #NONE} (default).
   * <p>
   * Example: To use ClassA and ClassB for the first gateway sender, none for
   * the second gateway sender, and ClassC for the third gateway sender,
   * specify:
   *     <code>ClassA ClassB, none, ClassC</code>
   */
  public static Long gatewayTransportFilters;

  /**
   * (boolean(s))
   * Manual start for each gateway sender.
   */
  public static Long manualStart;

  /**
   * (int(s))
   * Maximum queue memory for each gateway sender.
   */
  public static Long maximumQueueMemory;

  /**
   * (String(s))
   * Order policy for each gateway sender.
   */
  public static Long orderPolicy;

  /**
   * (boolean(s))
   * Whether to send in parallel for each gateway sender.
   */
  public static Long parallel;

  /**
   * (boolean(s))
   * Persistence enabled for each gateway sender.
   */
  public static Long persistenceEnabled;

  /**
   * (Comma-separated String pair(s))
   * Algorithm used to generate the list of remote distributed systems for
   * each logical gateway sender description.
   * <p>
   * An algorithm consists of a classname followed by a method name.  It must
   * be of type <code>public static Set<String></code> and take no arguments.
   * The return value must be a list of distributed system names as specified
   * in {@link GemFirePrms#distributedSystem}that are remote to the caller.
   * <p>
   * The algorithm is used by {@link GatewaySenderHelper#createGatewaySender
   * (String)} to create a gateway sender for each remote distributed system
   * in the list.
   * <p>
   * Suitable test configuration functions to use with the WAN topology
   * include files in $JTESTS/hydraconfig are:
   * <pre>
   * <code>
   *   // for a ring-connected topology
   *   hydra.GatewaySenderHelper getRingDistributedSystems
   *
   *   // for a hub-and-spoke-connected topology
   *   hydra.GatewaySenderHelper getHubAndSpokeDistributedSystems
   *
   *   // for a fully-connected topology
   *   hydra.GatewaySenderHelper getRemoteDistributedSystems (default)
   * </code>
   */
  public static Long remoteDistributedSystemsAlgorithm;

  /**
   * (int(s))
   * Socket buffer size for each gateway sender.
   */
  public static Long socketBufferSize;

  /**
   * (int(s))
   * Socket read timeout for each gateway sender.
   */
  public static Long socketReadTimeout;

//------------------------------------------------------------------------------
// parameter setup
//------------------------------------------------------------------------------

  static {
    setValues(GatewaySenderPrms.class);
  }
}
