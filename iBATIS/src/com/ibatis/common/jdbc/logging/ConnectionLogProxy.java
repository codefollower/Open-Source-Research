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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Connection proxy to add logging
 */
public class ConnectionLogProxy extends BaseLogProxy implements InvocationHandler {

    private static final Log log = LogFactory.getLog(Connection.class);

    private Connection connection;

    private ConnectionLogProxy(Connection conn) {
        super();
        this.connection = conn;
        if (log.isDebugEnabled()) {
            log.debug("{conn-" + id + "} Connection");
        }
    }

    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        try {
            if ("prepareStatement".equals(method.getName())) {
                if (log.isDebugEnabled()) {
                    log.debug("{conn-" + id + "} Preparing Statement: " + removeBreakingWhitespace((String) params[0]));
                }
                PreparedStatement stmt = (PreparedStatement) method.invoke(connection, params);
                stmt = PreparedStatementLogProxy.newInstance(stmt, (String) params[0]);
                return stmt;
            } else if ("prepareCall".equals(method.getName())) {
                if (log.isDebugEnabled()) {
                    log.debug("{conn-" + id + "} Preparing Call: " + removeBreakingWhitespace((String) params[0]));
                }
                PreparedStatement stmt = (PreparedStatement) method.invoke(connection, params);
                stmt = PreparedStatementLogProxy.newInstance(stmt, (String) params[0]);
                return stmt;
            } else if ("createStatement".equals(method.getName())) {
                Statement stmt = (Statement) method.invoke(connection, params);
                stmt = StatementLogProxy.newInstance(stmt);
                return stmt;
            } else {
                return method.invoke(connection, params);
            }
        } catch (Throwable t) {
            Throwable t1 = ClassInfo.unwrapThrowable(t);
            log.error("Error calling Connection." + method.getName() + ':', t1);
            throw t1;
        }

    }

    /**
     * Creates a logging version of a connection
     * @param conn - the original connection
     * @return - the connection with logging
     */
    public static Connection newInstance(Connection conn) {
        InvocationHandler handler = new ConnectionLogProxy(conn);
        ClassLoader cl = Connection.class.getClassLoader();
        return (Connection) Proxy.newProxyInstance(cl, new Class[] { Connection.class }, handler);
    }

    /**
     * return the wrapped connection
     * 
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }

}
