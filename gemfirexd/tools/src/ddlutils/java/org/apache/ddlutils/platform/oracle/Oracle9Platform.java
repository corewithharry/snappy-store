package org.apache.ddlutils.platform.oracle;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Changes for GemFireXD distributed data platform (some marked by "GemStone changes")
 *
 * Portions Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
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

import java.sql.Types;

/**
 * The platform for Oracle 9.
 *
 * @version $Revision: 231306 $
 */
public class Oracle9Platform extends Oracle8Platform
{
    /** Database name of this platform. */
    public static final String DATABASENAME = "Oracle9";

    /**
     * Creates a new platform instance.
     */
    public Oracle9Platform()
    {
        super();
// GemStone changes BEGIN
        getPlatformInfo().addNativeTypeMapping(Types.TIMESTAMP, "TIMESTAMP",
            Types.TIMESTAMP);

        setSqlBuilder(new Oracle9Builder(this));
        /* (original code)
        getPlatformInfo().addNativeTypeMapping(Types.TIMESTAMP, "TIMESTAMP");
        */
// GemStone changes END
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return DATABASENAME;
    }
}
