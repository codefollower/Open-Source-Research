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
package com.ibatis.common.jdbc.logging;

import com.ibatis.common.beans.ClassInfo;
import com.ibatis.common.logging.Log;
import com.ibatis.common.logging.LogFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * PreparedStatement proxy to add logging
 */
public class PreparedStatementLogProxy extends BaseLogProxy implements InvocationHandler {

    private static final Log log = LogFactory.getLog(PreparedStatement.class);

    private PreparedStatement statement;
    private String sql;

    private PreparedStatementLogProxy(PreparedStatement stmt, String sql) {
        this.statement = stmt;
        this.sql = sql;
    }

    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        try {
            if (EXECUTE_METHODS.contains(method.getName())) {
                if (log.isDebugEnabled()) {
                    log.debug("{pstm-" + id + "} Executing Statement: " + removeBreakingWhitespace(sql));
                    log.debug("{pstm-" + id + "} Parameters: " + getValueString());
                    log.debug("{pstm-" + id + "} Types: " + getTypeString());
                }
                clearColumnInfo();
                if ("executeQuery".equals(method.getName())) {
                    ResultSet rs = (ResultSet) method.invoke(statement, params);
                    if (rs != null) {
                        return ResultSetLogProxy.newInstance(rs);
                    } else {
                        return null;
                    }
                } else {
                    return method.invoke(statement, params);
                }
            } else if (SET_METHODS.contains(method.getName())) {
                if ("setNull".equals(method.getName())) {
                    setColumn(params[0], null);
                } else {
                    setColumn(params[0], params[1]);
                }
                return method.invoke(statement, params);
            } else if ("getResultSet".equals(method.getName())) {
                ResultSet rs = (ResultSet) method.invoke(statement, params);
                if (rs != null) {
                    return ResultSetLogProxy.newInstance(rs);
                } else {
                    return null;
                }
            } else if ("equals".equals(method.getName())) {
                Object ps = params[0];
                if (ps instanceof Proxy) {
                    return new Boolean(proxy == ps);
                }
                return new Boolean(false);
            } else if ("hashCode".equals(method.getName())) {
                return new Integer(proxy.hashCode());
            } else {
                return method.invoke(statement, params);
            }
        } catch (Throwable t) {
            throw ClassInfo.unwrapThrowable(t);
        }
    }

    /**
     * Creates a logging version of a PreparedStatement
     * @param stmt - the statement
     * @param sql - the sql statement
     * @return - the proxy
     */
    public static PreparedStatement newInstance(PreparedStatement stmt, String sql) {
        InvocationHandler handler = new PreparedStatementLogProxy(stmt, sql);
        ClassLoader cl = PreparedStatement.class.getClassLoader();
        return (PreparedStatement) Proxy.newProxyInstance(cl, new Class[] { PreparedStatement.class, CallableStatement.class },
                handler);
    }

    /**
     * Return the wrapped prepared statement
     * @return the PreparedStatement
     */
    public PreparedStatement getPreparedStatement() {
        return statement;
    }

}
