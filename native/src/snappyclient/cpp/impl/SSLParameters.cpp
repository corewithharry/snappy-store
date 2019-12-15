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
    setSSLProperty(propertyName,propertyValue);
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
  }
//  thrift::SnappyException ex;
//
//  thrift::SnappyExceptionData snappyExData;
//  snappyExData.__set_sqlState(std::string("Unknown SSL property"));
//  snappyExData.__set_reason("Failed to connect");
//  ex.__set_exceptionData(snappyExData);
//  throw ex;
}
std::string SSLParameters::getSSLPropertyValue(std::string &propertyName) {
  std::string retStr="";
  auto itr = sslProperties.find(propertyName);
  if (itr != sslProperties.end()) {
    auto itr = sslPropValMap.find(propertyName);
    if (itr != sslPropValMap.end()) {
      return retStr = itr->second;
    }
  }
//  thrift::SnappyException ex;
//  thrift::SnappyExceptionData snappyExData;
//  snappyExData.__set_sqlState(std::string("Unknown SSL property"));
//  snappyExData.__set_reason("Failed to connect");
//  ex.__set_exceptionData(snappyExData);
//  throw ex;
  return retStr;
}

