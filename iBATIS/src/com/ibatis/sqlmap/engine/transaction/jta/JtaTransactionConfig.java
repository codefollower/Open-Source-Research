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

import com.ibatis.sqlmap.client.*;
import com.ibatis.sqlmap.engine.transaction.*;

import javax.naming.*;
import javax.transaction.UserTransaction;
import java.sql.SQLException;
import java.util.Properties;

public class JtaTransactionConfig extends BaseTransactionConfig {

    private UserTransaction userTransaction;

    public Transaction newTransaction(int transactionIsolation) throws SQLException, TransactionException {
        return new JtaTransaction(userTransaction, dataSource, transactionIsolation);
    }

    public UserTransaction getUserTransaction() {
        return userTransaction;
    }

    public void setUserTransaction(UserTransaction userTransaction) {
        this.userTransaction = userTransaction;
    }

    public void setProperties(Properties props) throws SQLException, TransactionException {
        String utxName = null;
        try {
            utxName = (String) props.get("UserTransaction");
            InitialContext initCtx = new InitialContext();
            userTransaction = (UserTransaction) initCtx.lookup(utxName);
        } catch (NamingException e) {
            throw new SqlMapException("Error initializing JtaTransactionConfig while looking up UserTransaction (" + utxName
                    + ").  Cause: " + e);
        }
    }

}
