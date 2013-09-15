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
package com.ibatis.sqlmap.engine.transaction.external;

import com.ibatis.common.jdbc.logging.ConnectionLogProxy;
import com.ibatis.common.logging.Log;
import com.ibatis.common.logging.LogFactory;
import com.ibatis.sqlmap.engine.transaction.IsolationLevel;
import com.ibatis.sqlmap.engine.transaction.Transaction;
import com.ibatis.sqlmap.engine.transaction.TransactionException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ExternalTransaction implements Transaction {

    private static final Log connectionLog = LogFactory.getLog(Connection.class);

    private DataSource dataSource;
    private boolean defaultAutoCommit;
    private boolean setAutoCommitAllowed;
    private Connection connection;
    private IsolationLevel isolationLevel = new IsolationLevel();

    public ExternalTransaction(DataSource ds, boolean defaultAutoCommit, boolean setAutoCommitAllowed, int isolationLevel)
            throws TransactionException {
        // Check Parameters
        dataSource = ds;
        if (dataSource == null) {
            throw new TransactionException("ExternalTransaction initialization failed.  DataSource was null.");
        }

        this.defaultAutoCommit = defaultAutoCommit;
        this.setAutoCommitAllowed = setAutoCommitAllowed;
        this.isolationLevel.setIsolationLevel(isolationLevel);
    }

    private void init() throws SQLException, TransactionException {
        // Open JDBC Transaction
        connection = dataSource.getConnection();
        if (connection == null) {
            throw new TransactionException(
                    "ExternalTransaction could not start transaction.  Cause: The DataSource returned a null connection.");
        }
        // Isolation Level
        isolationLevel.applyIsolationLevel(connection);
        // AutoCommit
        if (setAutoCommitAllowed) {
            if (connection.getAutoCommit() != defaultAutoCommit) {
                connection.setAutoCommit(defaultAutoCommit);
            }
        }
        // Debug
        if (connectionLog.isDebugEnabled()) {
            connection = ConnectionLogProxy.newInstance(connection);
        }
    }

    public void commit() throws SQLException, TransactionException {
    }

    public void rollback() throws SQLException, TransactionException {
    }

    public void close() throws SQLException, TransactionException {
        if (connection != null) {
            try {
                isolationLevel.restoreIsolationLevel(connection);
            } finally {
                connection.close();
                connection = null;
            }
        }
    }

    public Connection getConnection() throws SQLException, TransactionException {
        if (connection == null) {
            init();
        }
        return connection;
    }

}
