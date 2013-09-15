/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibatis.sqlmap.engine.transaction;

import javax.sql.DataSource;
import java.util.Properties;
import java.sql.SQLException;

public abstract class BaseTransactionConfig implements TransactionConfig {

    protected DataSource dataSource;
    protected boolean forceCommit;

    public boolean isForceCommit() {
        return forceCommit;
    }

    public void setForceCommit(boolean forceCommit) {
        this.forceCommit = forceCommit;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource ds) {
        this.dataSource = ds;
    }

    /**
     * @deprecated
     * @return -1
     */
    public int getMaximumConcurrentTransactions() {
        return -1;
    }

    /**
     * @deprecated
     * @param maximumConcurrentTransactions - do not use here for Spring integration
     */
    public void setMaximumConcurrentTransactions(int maximumConcurrentTransactions) {
    }

    /**
     * @deprecated
     * @param props - propertes
     */
    public void initialize(Properties props) throws SQLException, TransactionException {
        setProperties(props);
    }
}
