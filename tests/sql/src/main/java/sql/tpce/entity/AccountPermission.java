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
package sql.tpce.entity;

import java.io.Serializable;

/**
 * ACCOUNT_PERMISSION database table.
 * 
 */

public class AccountPermission implements Serializable {
	private static final long serialVersionUID = 1L;

	private AccountPermissionPK id;

	private String apAcl;

	private String apFName;

	private String apLName;

	private CustomerAccount customerAccount;

	public AccountPermission() {
	}

	public AccountPermissionPK getId() {
		return this.id;
	}

	public void setId(AccountPermissionPK id) {
		this.id = id;
	}

	public String getApAcl() {
		return this.apAcl;
	}

	public void setApAcl(String apAcl) {
		this.apAcl = apAcl;
	}

	public String getApFName() {
		return this.apFName;
	}

	public void setApFName(String apFName) {
		this.apFName = apFName;
	}

	public String getApLName() {
		return this.apLName;
	}

	public void setApLName(String apLName) {
		this.apLName = apLName;
	}

	public CustomerAccount getCustomerAccount() {
		return this.customerAccount;
	}

	public void setCustomerAccount(CustomerAccount customerAccount) {
		this.customerAccount = customerAccount;
	}

}