/*
 * SSLParameters.cpp
 *
 *  Created on: 13-Dec-2019
 *      Author: pbisen
 */

//#include "thrift/snappydata_struct_SnappyException.h"
#include "SSLParameters.h"
using namespace io::snappydata;
using namespace io::snappydata::client::impl;
using namespace io::snappydata::thrift;

void SSLParameters::operator()(const std::string& str) {
  size_t spos;
  if ((spos = str.find('=')) != std::string::npos) {
    std::string propertyValue = str.substr(spos + 1);
    std::string propertyName = str.substr(0, spos);
    setSSLProperty(propertyName, propertyValue);
    return;
  }
}

void SSLParameters::setSSLProperty(std::string &propertyName,
    std::string& value) {
  auto itr = sslProperties.find(propertyName);
  if (itr != sslProperties.end()) {
    auto mapItr = sslPropValMap.find(propertyName);
    if (mapItr == sslPropValMap.end()) {
      sslPropValMap.insert(
          std::pair<std::string, std::string>(propertyName, value));
    }
  }else{
    throw std::invalid_argument(":Unknown SSL Property:" + propertyName );
  }
}
std::string SSLParameters::getSSLPropertyValue(std::string &propertyName) {
  std::string val = "";
  auto itr = sslProperties.find(propertyName);
  if (itr != sslProperties.end()) {
    auto itr = sslPropValMap.find(propertyName);
    if (itr != sslPropValMap.end()) {
      val = itr->second;
    }
    return val;
  } else {
    throw std::invalid_argument(":Unknown SSL Property:" + propertyName);
  }

}
std::string SSLParameters::getSSLPropertyName(SSLProperty sslProperty) {
  std::string propertyName = "";
  switch (sslProperty) {
    case SSLProperty::CIPHERSUITES:
      propertyName = "cipher-suites";
      break;
    case SSLProperty::CLIENTAUTH:
      propertyName = "client-auth";
      break;
    case SSLProperty::ENABLEPROTOCOLS:
      propertyName = "enabled-protocols";
      break;
    case SSLProperty::KEYSTORE:
      propertyName = "keystore";
      break;
    case SSLProperty::KEYSTOREPASSWORD:
      propertyName = "keystore-password";
      break;
    case SSLProperty::PROTOCOL:
      propertyName = "protocol";
      break;
    case SSLProperty::TRUSTSTORE:
      propertyName = "truststore";
      break;
    case SSLProperty::TRUSTSTOREPASSWORD:
      propertyName = "truststore-password";
      break;
    default:
      throw std::invalid_argument(":Unknown SSL Property:" + propertyName);
      break;
  }
  return propertyName;
}
