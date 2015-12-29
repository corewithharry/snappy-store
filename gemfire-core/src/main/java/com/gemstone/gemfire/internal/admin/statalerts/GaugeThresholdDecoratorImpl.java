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
package com.gemstone.gemfire.internal.admin.statalerts;

import java.io.DataOutput;
import java.io.DataInput;
import java.io.IOException;

import com.gemstone.gemfire.DataSerializer;
import com.gemstone.gemfire.StatisticsFactory;
import com.gemstone.gemfire.internal.DataSerializableFixedID;
import com.gemstone.gemfire.internal.admin.StatAlert;
import com.gemstone.gemfire.internal.admin.StatAlertDefinition;
import com.gemstone.gemfire.internal.shared.Version;

/**
 * Implementation of {@link StatAlertDefinition}, represents threshold as data
 * range
 * 
 * @author hgadre
 */
public final class GaugeThresholdDecoratorImpl extends BaseDecoratorImpl 
  implements DataSerializableFixedID {

  private static final long serialVersionUID = -8555077820685711783L;

  protected Number lowerLimit;

  protected Number upperLimit;

  public GaugeThresholdDecoratorImpl() {
  }

  /**
   * @param definition
   */
  public GaugeThresholdDecoratorImpl(StatAlertDefinition definition,
      Number lowerLimit, Number upperLimit) {
    super(definition);
    this.lowerLimit = lowerLimit;
    this.upperLimit = upperLimit;
  }

  public int getDSFID() {
    return DataSerializableFixedID.STAT_ALERT_DEFN_GAUGE_THRESHOLD;
  }

  public Number getThresholdLowerLimit() {
    return lowerLimit;
  }

  public Number getThresholdUpperLimit() {
    return upperLimit;
  }

  public boolean isGauge() {
    return true;
  }

  @Override
  public boolean verify(StatisticsFactory factory) {
    return (super.verify(factory) && (lowerLimit != null) && (upperLimit != null));
  }

  @Override
  public String toString() {

    StringBuffer buffer = new StringBuffer(super.toString());
    buffer
        .append("Threshold Limit: " + lowerLimit + " to " + upperLimit + "\n");

    return buffer.toString();
  }

  /**
   * This eval just applies to a single value or the 1st value in params
   */
  @Override
  public boolean evaluate(Number[] params) {
    return super.evaluate(params)
        && (isGreaterThan(params[0], upperLimit) || isLessThan(params[0],
            lowerLimit));
  }

  @Override
  public boolean evaluate() {
    return evaluate(getValue());
  }

  @Override
  public StatAlert evaluateAndAlert(Number[] params) {
    return evaluate(params) ? super.evaluateAndAlert(params) : null;
  }

  @Override
  public StatAlert evaluateAndAlert() {
    return evaluate() ? super.evaluateAndAlert() : null;
  }

  @Override
  public Number[] getValue() {
    return super.getValue();
  }

  @Override
  public Number[] getValue(Number[] vals) {
    return super.getValue(vals);
  }

  @Override
  public boolean hasDecorator(String decoratorID) {
    return ID.equalsIgnoreCase(decoratorID) || super.hasDecorator(decoratorID);
  }

  @Override
  public StatAlertDefinition getDecorator(String decoratorID) {
    return ID.equalsIgnoreCase(decoratorID) ? this : super
        .getDecorator(decoratorID);
  }

  @Override
  public void toData(DataOutput out) throws IOException {
    super.toData(out);
    DataSerializer.writeObject(this.lowerLimit, out);
    DataSerializer.writeObject(this.upperLimit, out);
  }

  @Override
  public void fromData(DataInput in)
    throws IOException, ClassNotFoundException {
    super.fromData(in);
    this.lowerLimit = (Number) DataSerializer.readObject(in);
    this.upperLimit = (Number) DataSerializer.readObject(in);
  }

  public static final String ID = "GaugeThreshold";

  @Override
  public Version[] getSerializationVersions() {
     return null;
  }
}
