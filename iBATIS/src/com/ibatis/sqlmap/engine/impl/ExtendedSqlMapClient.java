package com.ibatis.sqlmap.engine.impl;

import com.ibatis.sqlmap.client.*;
import com.ibatis.sqlmap.client.event.RowHandler;
import com.ibatis.sqlmap.engine.execution.*;
import com.ibatis.sqlmap.engine.mapping.statement.MappedStatement;
import com.ibatis.sqlmap.engine.mapping.result.ResultObjectFactory;
import com.ibatis.common.util.PaginatedList;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * @deprecated - this class is uneccessary and should be removed as
 * soon as possible. Currently spring integration depends on it.
 */
public interface ExtendedSqlMapClient extends SqlMapClient {

    Object insert(String id, Object param) throws SQLException;

    Object insert(String id) throws SQLException;

    int update(String id, Object param) throws SQLException;

    int update(String id) throws SQLException;

    int delete(String id, Object param) throws SQLException;

    int delete(String id) throws SQLException;

    Object queryForObject(String id, Object paramObject) throws SQLException;

    Object queryForObject(String id) throws SQLException;

    Object queryForObject(String id, Object paramObject, Object resultObject) throws SQLException;

    List queryForList(String id, Object paramObject) throws SQLException;

    List queryForList(String id) throws SQLException;

    List queryForList(String id, Object paramObject, int skip, int max) throws SQLException;

    List queryForList(String id, int skip, int max) throws SQLException;

    PaginatedList queryForPaginatedList(String id, Object paramObject, int pageSize) throws SQLException;

    PaginatedList queryForPaginatedList(String id, int pageSize) throws SQLException;

    Map queryForMap(String id, Object paramObject, String keyProp) throws SQLException;

    Map queryForMap(String id, Object paramObject, String keyProp, String valueProp) throws SQLException;

    void queryWithRowHandler(String id, Object paramObject, RowHandler rowHandler) throws SQLException;

    void queryWithRowHandler(String id, RowHandler rowHandler) throws SQLException;

    void startTransaction() throws SQLException;

    void startTransaction(int transactionIsolation) throws SQLException;

    void commitTransaction() throws SQLException;

    void endTransaction() throws SQLException;

    void startBatch() throws SQLException;

    int executeBatch() throws SQLException;

    List executeBatchDetailed() throws SQLException, BatchException;

    void setUserConnection(Connection connection) throws SQLException;

    Connection getUserConnection() throws SQLException;

    Connection getCurrentConnection() throws SQLException;

    DataSource getDataSource();

    MappedStatement getMappedStatement(String id);

    boolean isLazyLoadingEnabled();

    boolean isEnhancementEnabled();

    SqlExecutor getSqlExecutor();

    SqlMapExecutorDelegate getDelegate();

    SqlMapSession openSession();

    SqlMapSession openSession(Connection conn);

    SqlMapSession getSession();

    void flushDataCache();

    void flushDataCache(String cacheId);

    ResultObjectFactory getResultObjectFactory();

}
