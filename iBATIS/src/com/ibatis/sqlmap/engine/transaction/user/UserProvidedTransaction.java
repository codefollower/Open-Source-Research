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
package com.ibatis.sqlmap.engine.transaction.user;

import com.ibatis.common.jdbc.logging.ConnectionLogProxy;
import com.ibatis.common.logging.Log;
import com.ibatis.common.logging.LogFactory;
import com.ibatis.sqlmap.engine.transaction.Transaction;
import com.ibatis.sqlmap.engine.transaction.TransactionException;

import java.sql.Connection;
import java.sql.SQLException;

public class UserProvidedTransaction implements Transaction {

    private static final Log connectionLog = LogFactory.getLog(Connection.class);

    private Connection connection;

    public UserProvidedTransaction(Connection connection) {
        if (connectionLog.isDebugEnabled()) {
            this.connection = ConnectionLogProxy.newInstance(connection);
        } else {
            this.connection = connection;
        }
    }

    public void commit() throws SQLException, TransactionException {
        connection.commit();
    }

    public void rollback() throws SQLException, TransactionException {
        connection.rollback();
    }

    public void close() throws SQLException, TransactionException {
        connection.close();
    }

    public Connection getConnection() throws SQLException, TransactionException {
        return connection;
    }

}
