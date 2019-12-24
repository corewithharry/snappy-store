/*
 * SSLParameters.h
 *
 *  Created on: 13-Dec-2019
 *      Author: pbisen
 */

#ifndef SRC_SNAPPYCLIENT_HEADERS_SSLPARAMETERS_H_
#define SRC_SNAPPYCLIENT_HEADERS_SSLPARAMETERS_H_

#include "ClientBase.h"

namespace io {
  namespace snappydata {
    namespace client {
      namespace impl {

        enum class SSLProperty
          : int {
            CIPHERSUITES = 0,
          CLIENTAUTH = 1,
          ENABLEPROTOCOLS = 2,
          KEYSTORE = 3,
          KEYSTOREPASSWORD = 4,
          PROTOCOL = 5,
          TRUSTSTORE = 6,
          TRUSTSTOREPASSWORD = 7
        };

        class SSLParameters {
        private:
//          SSLParameters();
//          ~SSLParameters();
//        SSLParameters(const SSLParameters&) = delete; // no instances
//        SSLParameters operator=(const SSLParameters&) = delete; // no instances
          std::map<std::string, std::string> sslPropValMap;
          std::set<std::string> sslProperties = { "cipher-suites",
                                                  "client-auth",
                                                  "enabled-protocols",
                                                  "keystore",
                                                  "keystore-password",
                                                  "protocol", "truststore",
                                                  "truststore-password" };
        public:

          void setSSLProperty(std::string &propertyName, std::string& value);
          std::string getSSLPropertyValue(std::string &propertyName);
          std::string getSSLPropertyName(SSLProperty sslProperty);
          void operator()(const std::string& str);
        };
      }
    }
  }
}
#endif /* SRC_SNAPPYCLIENT_HEADERS_SSLPARAMETERS_H_ */
