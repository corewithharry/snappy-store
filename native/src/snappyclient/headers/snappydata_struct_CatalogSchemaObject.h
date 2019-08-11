/**
 * Autogenerated by Thrift Compiler (0.10.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */

#ifndef SNAPPYDATA_STRUCT_CATALOGSCHEMAOBJECT_H
#define SNAPPYDATA_STRUCT_CATALOGSCHEMAOBJECT_H


#include "snappydata_struct_Decimal.h"
#include "snappydata_struct_BlobChunk.h"
#include "snappydata_struct_ClobChunk.h"
#include "snappydata_struct_TransactionXid.h"
#include "snappydata_struct_ServiceMetaData.h"
#include "snappydata_struct_ServiceMetaDataArgs.h"
#include "snappydata_struct_OpenConnectionArgs.h"
#include "snappydata_struct_ConnectionProperties.h"
#include "snappydata_struct_HostAddress.h"
#include "snappydata_struct_SnappyExceptionData.h"
#include "snappydata_struct_StatementAttrs.h"
#include "snappydata_struct_ColumnValue.h"
#include "snappydata_struct_ColumnDescriptor.h"
#include "snappydata_struct_Row.h"
#include "snappydata_struct_OutputParameter.h"
#include "snappydata_struct_RowSet.h"
#include "snappydata_struct_PrepareResult.h"
#include "snappydata_struct_UpdateResult.h"
#include "snappydata_struct_StatementResult.h"
#include "snappydata_struct_BucketOwners.h"
#include "snappydata_struct_CatalogStorage.h"

#include "snappydata_types.h"

namespace io { namespace snappydata { namespace thrift {


class CatalogSchemaObject {
 public:

  CatalogSchemaObject(const CatalogSchemaObject&);
  CatalogSchemaObject(CatalogSchemaObject&&) noexcept;
  CatalogSchemaObject& operator=(const CatalogSchemaObject&);
  CatalogSchemaObject& operator=(CatalogSchemaObject&&) noexcept;
  CatalogSchemaObject() : name(), description(), locationUri() {
  }

  virtual ~CatalogSchemaObject() noexcept;
  std::string name;
  std::string description;
  std::string locationUri;
  std::map<std::string, std::string>  properties;

  void __set_name(const std::string& val);

  void __set_description(const std::string& val);

  void __set_locationUri(const std::string& val);

  void __set_properties(const std::map<std::string, std::string> & val);

  bool operator == (const CatalogSchemaObject & rhs) const
  {
    if (!(name == rhs.name))
      return false;
    if (!(description == rhs.description))
      return false;
    if (!(locationUri == rhs.locationUri))
      return false;
    if (!(properties == rhs.properties))
      return false;
    return true;
  }
  bool operator != (const CatalogSchemaObject &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const CatalogSchemaObject & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

  virtual void printTo(std::ostream& out) const;
};

void swap(CatalogSchemaObject &a, CatalogSchemaObject &b);

inline std::ostream& operator<<(std::ostream& out, const CatalogSchemaObject& obj)
{
  obj.printTo(out);
  return out;
}

}}} // namespace

#endif