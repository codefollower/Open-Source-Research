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
package com.ibatis.sqlmap.engine.transaction.jta;

import com.ibatis.common.jdbc.logging.ConnectionLogProxy;
import com.ibatis.common.logging.Log;
import com.ibatis.common.logging.LogFactory;
import com.ibatis.sqlmap.engine.transaction.IsolationLevel;
import com.ibatis.sqlmap.engine.transaction.Transaction;
import com.ibatis.sqlmap.engine.transaction.TransactionException;

import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import java.sql.Connection;
import java.sql.SQLException;

public class JtaTransaction implements Transaction {

    private static final Log connectionLog = LogFactory.getLog(Connection.class);

    private UserTransaction userTransaction;
    private DataSource dataSource;
    private Connection connection;
    private IsolationLevel isolationLevel = new IsolationLevel();

    private boolean commmitted = false;
    private boolean newTransaction = false;

    public JtaTransaction(UserTransaction utx, DataSource ds, int isolationLevel) throws TransactionException {
        // Check parameters
        userTransaction = utx;
        dataSource = ds;
        if (userTransaction == null) {
            throw new TransactionException("JtaTransaction initialization failed.  UserTransaction was null.");
        }
        if (dataSource == null) {
            throw new TransactionException("JtaTransaction initialization failed.  DataSource was null.");
        }
        this.isolationLevel.setIsolationLevel(isolationLevel);
    }

    private void init() throws TransactionException, SQLException {
        // Start JTA Transaction
        try {
            newTransaction = userTransaction.getStatus() == Status.STATUS_NO_TRANSACTION;
            if (newTransaction) {
                userTransaction.begin();
            }
        } catch (Exception e) {
            throw new TransactionException("JtaTransaction could not start transaction.  Cause: ", e);
        }

        // Open JDBC Connection
        connection = dataSource.getConnection();
        if (connection == null) {
            throw new TransactionException(
                    "JtaTransaction could not start transaction.  Cause: The DataSource returned a null connection.");
        }
        // Isolation Level
        isolationLevel.applyIsolationLevel(connection);
        // AutoCommit
        if (connection.getAutoCommit()) {
            connection.setAutoCommit(false);
        }
        // Debug
        if (connectionLog.isDebugEnabled()) {
            connection = ConnectionLogProxy.newInstance(connection);
        }
    }

    public void commit() throws SQLException, TransactionException {
        if (connection != null) {
            if (commmitted) {
                throw new TransactionException(
                        "JtaTransaction could not commit because this transaction has already been committed.");
            }
            try {
                if (newTransaction) {
                    userTransaction.commit();
                }
            } catch (Exception e) {
                throw new TransactionException("JtaTransaction could not commit.  Cause: ", e);
            }
            commmitted = true;
        }
    }

    public void rollback() throws SQLException, TransactionException {
        if (connection != null) {
            if (!commmitted) {
                try {
                    if (userTransaction != null) {
                        if (newTransaction) {
                            userTransaction.rollback();
                        } else {
                            userTransaction.setRollbackOnly();
                        }
                    }
                } catch (Exception e) {
                    throw new TransactionException("JtaTransaction could not rollback.  Cause: ", e);
                }
            }
        }
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
