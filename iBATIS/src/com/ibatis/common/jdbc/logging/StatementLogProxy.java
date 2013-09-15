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
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Statement proxy to add logging
 */
public class StatementLogProxy extends BaseLogProxy implements InvocationHandler {

    private static final Log log = LogFactory.getLog(Statement.class);

    private Statement statement;

    private StatementLogProxy(Statement stmt) {
        super();
        this.statement = stmt;
    }

    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        try {
            if (EXECUTE_METHODS.contains(method.getName())) {
                if (log.isDebugEnabled()) {
                    log.debug("{stmt-" + id + "} Statement: " + removeBreakingWhitespace((String) params[0]));
                }
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
     * Creates a logging version of a Statement
     * @param stmt - the statement
     * @return - the proxy
     */
    public static Statement newInstance(Statement stmt) {
        InvocationHandler handler = new StatementLogProxy(stmt);
        ClassLoader cl = Statement.class.getClassLoader();
        return (Statement) Proxy.newProxyInstance(cl, new Class[] { Statement.class }, handler);
    }

    /**
     * return the wrapped statement
     * 
     * @return the statement
     */
    public Statement getStatement() {
        return statement;
    }

}
