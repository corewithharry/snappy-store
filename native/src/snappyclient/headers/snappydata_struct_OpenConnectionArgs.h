/**
 * Autogenerated by Thrift Compiler (0.10.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */

#ifndef SNAPPYDATA_STRUCT_OPENCONNECTIONARGS_H
#define SNAPPYDATA_STRUCT_OPENCONNECTIONARGS_H


#include "snappydata_struct_Decimal.h"
#include "snappydata_struct_BlobChunk.h"
#include "snappydata_struct_ClobChunk.h"
#include "snappydata_struct_TransactionXid.h"
#include "snappydata_struct_ServiceMetaData.h"
#include "snappydata_struct_ServiceMetaDataArgs.h"

#include "snappydata_types.h"

namespace io { namespace snappydata { namespace thrift {

typedef struct _OpenConnectionArgs__isset {
  _OpenConnectionArgs__isset() : userName(false), password(false), forXA(false), tokenSize(false), useStringForDecimal(false), properties(false) {}
  bool userName :1;
  bool password :1;
  bool forXA :1;
  bool tokenSize :1;
  bool useStringForDecimal :1;
  bool properties :1;
} _OpenConnectionArgs__isset;

class OpenConnectionArgs {
 public:

  OpenConnectionArgs(const OpenConnectionArgs&);
  OpenConnectionArgs(OpenConnectionArgs&&) noexcept;
  OpenConnectionArgs& operator=(const OpenConnectionArgs&);
  OpenConnectionArgs& operator=(OpenConnectionArgs&&) noexcept;
  OpenConnectionArgs() : clientHostName(), clientID(), security((SecurityMechanism::type)0), userName(), password(), forXA(0), tokenSize(0), useStringForDecimal(0) {
  }

  virtual ~OpenConnectionArgs() noexcept;
  std::string clientHostName;
  std::string clientID;
  SecurityMechanism::type security;
  std::string userName;
  std::string password;
  bool forXA;
  int32_t tokenSize;
  bool useStringForDecimal;
  std::map<std::string, std::string>  properties;

  _OpenConnectionArgs__isset __isset;

  void __set_clientHostName(const std::string& val);

  void __set_clientID(const std::string& val);

  void __set_security(const SecurityMechanism::type val);

  void __set_userName(const std::string& val);

  void __set_password(const std::string& val);

  void __set_forXA(const bool val);

  void __set_tokenSize(const int32_t val);

  void __set_useStringForDecimal(const bool val);

  void __set_properties(const std::map<std::string, std::string> & val);

  bool operator == (const OpenConnectionArgs & rhs) const
  {
    if (!(clientHostName == rhs.clientHostName))
      return false;
    if (!(clientID == rhs.clientID))
      return false;
    if (!(security == rhs.security))
      return false;
    if (__isset.userName != rhs.__isset.userName)
      return false;
    else if (__isset.userName && !(userName == rhs.userName))
      return false;
    if (__isset.password != rhs.__isset.password)
      return false;
    else if (__isset.password && !(password == rhs.password))
      return false;
    if (__isset.forXA != rhs.__isset.forXA)
      return false;
    else if (__isset.forXA && !(forXA == rhs.forXA))
      return false;
    if (__isset.tokenSize != rhs.__isset.tokenSize)
      return false;
    else if (__isset.tokenSize && !(tokenSize == rhs.tokenSize))
      return false;
    if (__isset.useStringForDecimal != rhs.__isset.useStringForDecimal)
      return false;
    else if (__isset.useStringForDecimal && !(useStringForDecimal == rhs.useStringForDecimal))
      return false;
    if (__isset.properties != rhs.__isset.properties)
      return false;
    else if (__isset.properties && !(properties == rhs.properties))
      return false;
    return true;
  }
  bool operator != (const OpenConnectionArgs &rhs) const {
    return !(*this == rhs);
  }

  bool operator < (const OpenConnectionArgs & ) const;

  uint32_t read(::apache::thrift::protocol::TProtocol* iprot);
  uint32_t write(::apache::thrift::protocol::TProtocol* oprot) const;

  virtual void printTo(std::ostream& out) const;
};

void swap(OpenConnectionArgs &a, OpenConnectionArgs &b);

inline std::ostream& operator<<(std::ostream& out, const OpenConnectionArgs& obj)
{
  obj.printTo(out);
  return out;
}

}}} // namespace

#endif
