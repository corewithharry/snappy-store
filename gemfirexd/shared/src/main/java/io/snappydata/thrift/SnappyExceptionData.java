/**
 * Autogenerated by Thrift Compiler (0.9.3)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package io.snappydata.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
@Generated(value = "Autogenerated by Thrift Compiler (0.9.3)", date = "2018-12-10")
public class SnappyExceptionData implements org.apache.thrift.TBase<SnappyExceptionData, SnappyExceptionData._Fields>, java.io.Serializable, Cloneable, Comparable<SnappyExceptionData> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("SnappyExceptionData");

  private static final org.apache.thrift.protocol.TField REASON_FIELD_DESC = new org.apache.thrift.protocol.TField("reason", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField ERROR_CODE_FIELD_DESC = new org.apache.thrift.protocol.TField("errorCode", org.apache.thrift.protocol.TType.I32, (short)2);
  private static final org.apache.thrift.protocol.TField SQL_STATE_FIELD_DESC = new org.apache.thrift.protocol.TField("sqlState", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField UPDATE_COUNTS_FIELD_DESC = new org.apache.thrift.protocol.TField("updateCounts", org.apache.thrift.protocol.TType.LIST, (short)4);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new SnappyExceptionDataStandardSchemeFactory());
    schemes.put(TupleScheme.class, new SnappyExceptionDataTupleSchemeFactory());
  }

  public String reason; // required
  public int errorCode; // required
  public String sqlState; // optional
  public List<Integer> updateCounts; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    REASON((short)1, "reason"),
    ERROR_CODE((short)2, "errorCode"),
    SQL_STATE((short)3, "sqlState"),
    UPDATE_COUNTS((short)4, "updateCounts");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // REASON
          return REASON;
        case 2: // ERROR_CODE
          return ERROR_CODE;
        case 3: // SQL_STATE
          return SQL_STATE;
        case 4: // UPDATE_COUNTS
          return UPDATE_COUNTS;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __ERRORCODE_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  private static final _Fields optionals[] = {_Fields.SQL_STATE,_Fields.UPDATE_COUNTS};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.REASON, new org.apache.thrift.meta_data.FieldMetaData("reason", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.ERROR_CODE, new org.apache.thrift.meta_data.FieldMetaData("errorCode", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.SQL_STATE, new org.apache.thrift.meta_data.FieldMetaData("sqlState", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.UPDATE_COUNTS, new org.apache.thrift.meta_data.FieldMetaData("updateCounts", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(SnappyExceptionData.class, metaDataMap);
  }

  public SnappyExceptionData() {
  }

  public SnappyExceptionData(
    String reason,
    int errorCode)
  {
    this();
    this.reason = reason;
    this.errorCode = errorCode;
    setErrorCodeIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public SnappyExceptionData(SnappyExceptionData other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetReason()) {
      this.reason = other.reason;
    }
    this.errorCode = other.errorCode;
    if (other.isSetSqlState()) {
      this.sqlState = other.sqlState;
    }
    if (other.isSetUpdateCounts()) {
      List<Integer> __this__updateCounts = new ArrayList<Integer>(other.updateCounts);
      this.updateCounts = __this__updateCounts;
    }
  }

  public SnappyExceptionData deepCopy() {
    return new SnappyExceptionData(this);
  }

  @Override
  public void clear() {
    this.reason = null;
    setErrorCodeIsSet(false);
    this.errorCode = 0;
    this.sqlState = null;
    this.updateCounts = null;
  }

  public String getReason() {
    return this.reason;
  }

  public SnappyExceptionData setReason(String reason) {
    this.reason = reason;
    return this;
  }

  public void unsetReason() {
    this.reason = null;
  }

  /** Returns true if field reason is set (has been assigned a value) and false otherwise */
  public boolean isSetReason() {
    return this.reason != null;
  }

  public void setReasonIsSet(boolean value) {
    if (!value) {
      this.reason = null;
    }
  }

  public int getErrorCode() {
    return this.errorCode;
  }

  public SnappyExceptionData setErrorCode(int errorCode) {
    this.errorCode = errorCode;
    setErrorCodeIsSet(true);
    return this;
  }

  public void unsetErrorCode() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __ERRORCODE_ISSET_ID);
  }

  /** Returns true if field errorCode is set (has been assigned a value) and false otherwise */
  public boolean isSetErrorCode() {
    return EncodingUtils.testBit(__isset_bitfield, __ERRORCODE_ISSET_ID);
  }

  public void setErrorCodeIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __ERRORCODE_ISSET_ID, value);
  }

  public String getSqlState() {
    return this.sqlState;
  }

  public SnappyExceptionData setSqlState(String sqlState) {
    this.sqlState = sqlState;
    return this;
  }

  public void unsetSqlState() {
    this.sqlState = null;
  }

  /** Returns true if field sqlState is set (has been assigned a value) and false otherwise */
  public boolean isSetSqlState() {
    return this.sqlState != null;
  }

  public void setSqlStateIsSet(boolean value) {
    if (!value) {
      this.sqlState = null;
    }
  }

  public int getUpdateCountsSize() {
    return (this.updateCounts == null) ? 0 : this.updateCounts.size();
  }

  public java.util.Iterator<Integer> getUpdateCountsIterator() {
    return (this.updateCounts == null) ? null : this.updateCounts.iterator();
  }

  public void addToUpdateCounts(int elem) {
    if (this.updateCounts == null) {
      this.updateCounts = new ArrayList<Integer>();
    }
    this.updateCounts.add(elem);
  }

  public List<Integer> getUpdateCounts() {
    return this.updateCounts;
  }

  public SnappyExceptionData setUpdateCounts(List<Integer> updateCounts) {
    this.updateCounts = updateCounts;
    return this;
  }

  public void unsetUpdateCounts() {
    this.updateCounts = null;
  }

  /** Returns true if field updateCounts is set (has been assigned a value) and false otherwise */
  public boolean isSetUpdateCounts() {
    return this.updateCounts != null;
  }

  public void setUpdateCountsIsSet(boolean value) {
    if (!value) {
      this.updateCounts = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case REASON:
      if (value == null) {
        unsetReason();
      } else {
        setReason((String)value);
      }
      break;

    case ERROR_CODE:
      if (value == null) {
        unsetErrorCode();
      } else {
        setErrorCode((Integer)value);
      }
      break;

    case SQL_STATE:
      if (value == null) {
        unsetSqlState();
      } else {
        setSqlState((String)value);
      }
      break;

    case UPDATE_COUNTS:
      if (value == null) {
        unsetUpdateCounts();
      } else {
        setUpdateCounts((List<Integer>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case REASON:
      return getReason();

    case ERROR_CODE:
      return getErrorCode();

    case SQL_STATE:
      return getSqlState();

    case UPDATE_COUNTS:
      return getUpdateCounts();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case REASON:
      return isSetReason();
    case ERROR_CODE:
      return isSetErrorCode();
    case SQL_STATE:
      return isSetSqlState();
    case UPDATE_COUNTS:
      return isSetUpdateCounts();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof SnappyExceptionData)
      return this.equals((SnappyExceptionData)that);
    return false;
  }

  public boolean equals(SnappyExceptionData that) {
    if (that == null)
      return false;

    boolean this_present_reason = true && this.isSetReason();
    boolean that_present_reason = true && that.isSetReason();
    if (this_present_reason || that_present_reason) {
      if (!(this_present_reason && that_present_reason))
        return false;
      if (!this.reason.equals(that.reason))
        return false;
    }

    boolean this_present_errorCode = true;
    boolean that_present_errorCode = true;
    if (this_present_errorCode || that_present_errorCode) {
      if (!(this_present_errorCode && that_present_errorCode))
        return false;
      if (this.errorCode != that.errorCode)
        return false;
    }

    boolean this_present_sqlState = true && this.isSetSqlState();
    boolean that_present_sqlState = true && that.isSetSqlState();
    if (this_present_sqlState || that_present_sqlState) {
      if (!(this_present_sqlState && that_present_sqlState))
        return false;
      if (!this.sqlState.equals(that.sqlState))
        return false;
    }

    boolean this_present_updateCounts = true && this.isSetUpdateCounts();
    boolean that_present_updateCounts = true && that.isSetUpdateCounts();
    if (this_present_updateCounts || that_present_updateCounts) {
      if (!(this_present_updateCounts && that_present_updateCounts))
        return false;
      if (!this.updateCounts.equals(that.updateCounts))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_reason = true && (isSetReason());
    list.add(present_reason);
    if (present_reason)
      list.add(reason);

    boolean present_errorCode = true;
    list.add(present_errorCode);
    if (present_errorCode)
      list.add(errorCode);

    boolean present_sqlState = true && (isSetSqlState());
    list.add(present_sqlState);
    if (present_sqlState)
      list.add(sqlState);

    boolean present_updateCounts = true && (isSetUpdateCounts());
    list.add(present_updateCounts);
    if (present_updateCounts)
      list.add(updateCounts);

    return list.hashCode();
  }

  @Override
  public int compareTo(SnappyExceptionData other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetReason()).compareTo(other.isSetReason());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetReason()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.reason, other.reason);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetErrorCode()).compareTo(other.isSetErrorCode());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetErrorCode()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.errorCode, other.errorCode);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetSqlState()).compareTo(other.isSetSqlState());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSqlState()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sqlState, other.sqlState);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetUpdateCounts()).compareTo(other.isSetUpdateCounts());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetUpdateCounts()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.updateCounts, other.updateCounts);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("SnappyExceptionData(");
    boolean first = true;

    sb.append("reason:");
    if (this.reason == null) {
      sb.append("null");
    } else {
      sb.append(this.reason);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("errorCode:");
    sb.append(this.errorCode);
    first = false;
    if (isSetSqlState()) {
      if (!first) sb.append(", ");
      sb.append("sqlState:");
      if (this.sqlState == null) {
        sb.append("null");
      } else {
        sb.append(this.sqlState);
      }
      first = false;
    }
    if (isSetUpdateCounts()) {
      if (!first) sb.append(", ");
      sb.append("updateCounts:");
      if (this.updateCounts == null) {
        sb.append("null");
      } else {
        sb.append(this.updateCounts);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (reason == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'reason' was not present! Struct: " + toString());
    }
    // alas, we cannot check 'errorCode' because it's a primitive and you chose the non-beans generator.
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class SnappyExceptionDataStandardSchemeFactory implements SchemeFactory {
    public SnappyExceptionDataStandardScheme getScheme() {
      return new SnappyExceptionDataStandardScheme();
    }
  }

  private static class SnappyExceptionDataStandardScheme extends StandardScheme<SnappyExceptionData> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, SnappyExceptionData struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // REASON
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.reason = iprot.readString();
              struct.setReasonIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // ERROR_CODE
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.errorCode = iprot.readI32();
              struct.setErrorCodeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // SQL_STATE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.sqlState = iprot.readString();
              struct.setSqlStateIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // UPDATE_COUNTS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list112 = iprot.readListBegin();
                struct.updateCounts = new ArrayList<Integer>(_list112.size);
                int _elem113;
                for (int _i114 = 0; _i114 < _list112.size; ++_i114)
                {
                  _elem113 = iprot.readI32();
                  struct.updateCounts.add(_elem113);
                }
                iprot.readListEnd();
              }
              struct.setUpdateCountsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      if (!struct.isSetErrorCode()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'errorCode' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, SnappyExceptionData struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.reason != null) {
        oprot.writeFieldBegin(REASON_FIELD_DESC);
        oprot.writeString(struct.reason);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(ERROR_CODE_FIELD_DESC);
      oprot.writeI32(struct.errorCode);
      oprot.writeFieldEnd();
      if (struct.sqlState != null) {
        if (struct.isSetSqlState()) {
          oprot.writeFieldBegin(SQL_STATE_FIELD_DESC);
          oprot.writeString(struct.sqlState);
          oprot.writeFieldEnd();
        }
      }
      if (struct.updateCounts != null) {
        if (struct.isSetUpdateCounts()) {
          oprot.writeFieldBegin(UPDATE_COUNTS_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.I32, struct.updateCounts.size()));
            for (int _iter115 : struct.updateCounts)
            {
              oprot.writeI32(_iter115);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class SnappyExceptionDataTupleSchemeFactory implements SchemeFactory {
    public SnappyExceptionDataTupleScheme getScheme() {
      return new SnappyExceptionDataTupleScheme();
    }
  }

  private static class SnappyExceptionDataTupleScheme extends TupleScheme<SnappyExceptionData> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, SnappyExceptionData struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeString(struct.reason);
      oprot.writeI32(struct.errorCode);
      BitSet optionals = new BitSet();
      if (struct.isSetSqlState()) {
        optionals.set(0);
      }
      if (struct.isSetUpdateCounts()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSetSqlState()) {
        oprot.writeString(struct.sqlState);
      }
      if (struct.isSetUpdateCounts()) {
        {
          oprot.writeI32(struct.updateCounts.size());
          for (int _iter116 : struct.updateCounts)
          {
            oprot.writeI32(_iter116);
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, SnappyExceptionData struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.reason = iprot.readString();
      struct.setReasonIsSet(true);
      struct.errorCode = iprot.readI32();
      struct.setErrorCodeIsSet(true);
      BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        struct.sqlState = iprot.readString();
        struct.setSqlStateIsSet(true);
      }
      if (incoming.get(1)) {
        {
          org.apache.thrift.protocol.TList _list117 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.I32, iprot.readI32());
          struct.updateCounts = new ArrayList<Integer>(_list117.size);
          int _elem118;
          for (int _i119 = 0; _i119 < _list117.size; ++_i119)
          {
            _elem118 = iprot.readI32();
            struct.updateCounts.add(_elem118);
          }
        }
        struct.setUpdateCountsIsSet(true);
      }
    }
  }

}

