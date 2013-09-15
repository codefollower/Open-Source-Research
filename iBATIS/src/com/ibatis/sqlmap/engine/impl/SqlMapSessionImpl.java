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
package com.ibatis.sqlmap.engine.impl;

import com.ibatis.common.jdbc.exception.NestedSQLException;
import com.ibatis.common.util.PaginatedList;
import com.ibatis.sqlmap.client.SqlMapSession;
import com.ibatis.sqlmap.client.event.RowHandler;
import com.ibatis.sqlmap.engine.execution.BatchException;
import com.ibatis.sqlmap.engine.execution.SqlExecutor;
import com.ibatis.sqlmap.engine.mapping.statement.MappedStatement;
import com.ibatis.sqlmap.engine.scope.SessionScope;
import com.ibatis.sqlmap.engine.transaction.Transaction;
import com.ibatis.sqlmap.engine.transaction.TransactionException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Implementation of SqlMapSession
 */
public class SqlMapSessionImpl implements SqlMapSession {
    private static my.Debug DEBUG = new my.Debug(my.Debug.SqlMapExecutorDelegate);//我加上的

    protected SqlMapExecutorDelegate delegate;
    protected SessionScope sessionScope;
    protected boolean closed;

    /**
     * Constructor
     *
     * @param client - the client that will use the session
     */
    public SqlMapSessionImpl(SqlMapClientImpl client) {
        try {//我加上的
            DEBUG.P(this, "SqlMapSessionImpl(1)");

            this.delegate = client.getDelegate();

            //new一个SessionScope
            this.sessionScope = this.delegate.beginSessionScope();

            DEBUG.P("this.sessionScope=" + this.sessionScope);

            this.sessionScope.setSqlMapClient(client);
            this.sessionScope.setSqlMapExecutor(client);
            this.sessionScope.setSqlMapTxMgr(client);
            this.closed = false;

        } finally {//我加上的
            DEBUG.P(0, this, "SqlMapSessionImpl(1)");
        }
    }

    /**
     * Start the session
     */
    public void open() {
        //原先是SqlMapClientImpl client
        //SqlMapSessionImpl与SqlMapClientImpl都实现了com.ibatis.sqlmap.client.SqlMapTransactionManager
        sessionScope.setSqlMapTxMgr(this);
    }

    /**
     * Getter to tell if the session is still open
     *
     * @return - the status of the session
     */
    public boolean isClosed() {
        return closed;
    }

    public void close() {
        try {//我加上的
            DEBUG.P(this, "close()");
            DEBUG.P("delegate=" + delegate);

            if (delegate != null && sessionScope != null)
                delegate.endSessionScope(sessionScope);
            if (sessionScope != null)
                sessionScope = null;
            if (delegate != null)
                delegate = null;

            DEBUG.P("closed=" + closed);

            if (!closed)
                closed = true;

        } finally {//我加上的
            DEBUG.P(0, this, "close()");
        }
    }

    public Object insert(String id, Object param) throws SQLException {
        return delegate.insert(sessionScope, id, param);
    }

    public Object insert(String id) throws SQLException {
        return insert(id, null);
    }

    public int update(String id, Object param) throws SQLException {
        return delegate.update(sessionScope, id, param);
    }

    public int update(String id) throws SQLException {
        return update(id, null);
    }

    public int delete(String id, Object param) throws SQLException {
        return delegate.delete(sessionScope, id, param);
    }

    public int delete(String id) throws SQLException {
        return delete(id, null);
    }

    public Object queryForObject(String id, Object paramObject) throws SQLException {
        return delegate.queryForObject(sessionScope, id, paramObject);
    }

    public Object queryForObject(String id) throws SQLException {
        return queryForObject(id, null);
    }

    public Object queryForObject(String id, Object paramObject, Object resultObject) throws SQLException {
        return delegate.queryForObject(sessionScope, id, paramObject, resultObject);
    }

    public List queryForList(String id, Object paramObject) throws SQLException {
        try {//我加上的
            DEBUG.P(this, "queryForList(2)");

            return delegate.queryForList(sessionScope, id, paramObject);

        } finally {//我加上的
            DEBUG.P(0, this, "queryForList(2)");
        }
    }

    public List queryForList(String id) throws SQLException {
        try {//我加上的
            DEBUG.P(this, "queryForList(1)");

            return queryForList(id, null);

        } finally {//我加上的
            DEBUG.P(0, this, "queryForList(1)");
        }
    }

    public List queryForList(String id, Object paramObject, int skip, int max) throws SQLException {
        return delegate.queryForList(sessionScope, id, paramObject, skip, max);
    }

    public List queryForList(String id, int skip, int max) throws SQLException {
        return queryForList(id, null, skip, max);
    }

    /**
     * @deprecated All paginated list features have been deprecated
     */
    public PaginatedList queryForPaginatedList(String id, Object paramObject, int pageSize) throws SQLException {
        return delegate.queryForPaginatedList(sessionScope, id, paramObject, pageSize);
    }

    /**
     * @deprecated All paginated list features have been deprecated
     */
    public PaginatedList queryForPaginatedList(String id, int pageSize) throws SQLException {
        return queryForPaginatedList(id, null, pageSize);
    }

    public Map queryForMap(String id, Object paramObject, String keyProp) throws SQLException {
        return delegate.queryForMap(sessionScope, id, paramObject, keyProp);
    }

    public Map queryForMap(String id, Object paramObject, String keyProp, String valueProp) throws SQLException {
        return delegate.queryForMap(sessionScope, id, paramObject, keyProp, valueProp);
    }

    public void queryWithRowHandler(String id, Object paramObject, RowHandler rowHandler) throws SQLException {
        delegate.queryWithRowHandler(sessionScope, id, paramObject, rowHandler);
    }

    public void queryWithRowHandler(String id, RowHandler rowHandler) throws SQLException {
        queryWithRowHandler(id, null, rowHandler);
    }

    public void startTransaction() throws SQLException {
        DEBUG.P(this, "startTransaction()");
        delegate.startTransaction(sessionScope);
        DEBUG.P(0, this, "startTransaction()");
    }

    public void startTransaction(int transactionIsolation) throws SQLException {
        delegate.startTransaction(sessionScope, transactionIsolation);
    }

    public void commitTransaction() throws SQLException {
        delegate.commitTransaction(sessionScope);
    }

    public void endTransaction() throws SQLException {
        delegate.endTransaction(sessionScope);
    }

    public void startBatch() throws SQLException {
        delegate.startBatch(sessionScope);
    }

    public int executeBatch() throws SQLException {
        return delegate.executeBatch(sessionScope);
    }

    public List executeBatchDetailed() throws SQLException, BatchException {
        return delegate.executeBatchDetailed(sessionScope);
    }

    public void setUserConnection(Connection connection) throws SQLException {
        delegate.setUserProvidedTransaction(sessionScope, connection);
    }

    /**
     * TODO Deprecated
     *
     * @return Current connection
     * @throws SQLException
     * @deprecated
     */
    public Connection getUserConnection() throws SQLException {
        return getCurrentConnection();
    }

    //Connection从Transaction中获取
    public Connection getCurrentConnection() throws SQLException {
        try {
            Connection conn = null;
            Transaction trans = delegate.getTransaction(sessionScope);
            if (trans != null) {
                conn = trans.getConnection();
            }
            return conn;
        } catch (TransactionException e) {
            throw new NestedSQLException("Error getting Connection from Transaction.  Cause: " + e, e);
        }
    }

    public DataSource getDataSource() {
        return delegate.getDataSource();
    }

    /**
     * Gets a mapped statement by ID
     *
     * @param id - the ID
     * @return - the mapped statement
     */
    public MappedStatement getMappedStatement(String id) {
        return delegate.getMappedStatement(id);
    }

    /**
     * Get the status of lazy loading
     *
     * @return - the status
     */
    public boolean isLazyLoadingEnabled() {
        return delegate.isLazyLoadingEnabled();
    }

    /**
     * Get the status of CGLib enhancements
     *
     * @return - the status
     */
    public boolean isEnhancementEnabled() {
        return delegate.isEnhancementEnabled();
    }

    /**
     * Get the SQL executor
     *
     * @return -  the executor
     */
    public SqlExecutor getSqlExecutor() {
        return delegate.getSqlExecutor();
    }

    /**
     * Get the delegate
     *
     * @return - the delegate
     */
    public SqlMapExecutorDelegate getDelegate() {
        return delegate;
    }

}
