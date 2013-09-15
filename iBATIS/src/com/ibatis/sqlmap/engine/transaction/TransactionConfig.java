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
import java.sql.SQLException;
import java.util.Properties;

public interface TransactionConfig {

    Transaction newTransaction(int transactionIsolation) throws SQLException, TransactionException;

    DataSource getDataSource();

    void setDataSource(DataSource ds);

    /**
     * This should not be used and is here purely to avoid spring integration from breaking
     * @deprecated
     * @return -1
     */
    int getMaximumConcurrentTransactions();

    /**
     * This should not be used. It does nothing and is here purely to prevent Spring integration from breaking
     * @deprecated
     * @param maximumConcurrentTransactions
     */
    void setMaximumConcurrentTransactions(int maximumConcurrentTransactions);

    boolean isForceCommit();

    void setForceCommit(boolean forceCommit);

    /**
     * This method should call setProperties. It is here simply to ease transition
     *
     * @deprecated
     * @param props - Properties
     */
    void initialize(Properties props) throws SQLException, TransactionException;

    void setProperties(Properties props) throws SQLException, TransactionException;

}
