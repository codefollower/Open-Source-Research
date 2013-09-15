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
package com.ibatis.sqlmap.engine.mapping.parameter;

import com.ibatis.sqlmap.engine.cache.CacheKey;
import com.ibatis.sqlmap.engine.exchange.DataExchange;
import com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate;
import com.ibatis.sqlmap.engine.scope.ErrorContext;
import com.ibatis.sqlmap.engine.scope.StatementScope;
import com.ibatis.sqlmap.engine.type.CustomTypeHandler;
import com.ibatis.sqlmap.engine.type.JdbcTypeRegistry;
import com.ibatis.sqlmap.engine.type.TypeHandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParameterMap {

    private String id;
    private Class parameterClass;

    private ParameterMapping[] parameterMappings;
    private DataExchange dataExchange;

    private String resource;

    private Map parameterMappingIndex = new HashMap();

    private SqlMapExecutorDelegate delegate;

    public ParameterMap(SqlMapExecutorDelegate delegate) {
        this.delegate = delegate;
    }

    public SqlMapExecutorDelegate getDelegate() {
        return delegate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Class getParameterClass() {
        return parameterClass;
    }

    public void setParameterClass(Class parameterClass) {
        this.parameterClass = parameterClass;
    }

    public DataExchange getDataExchange() {
        return dataExchange;
    }

    public void setDataExchange(DataExchange dataExchange) {
        this.dataExchange = dataExchange;
    }

    public ParameterMapping[] getParameterMappings() {
        return parameterMappings;
    }

    public void setParameterMappingList(List parameterMappingList) {
        this.parameterMappings = (ParameterMapping[]) parameterMappingList.toArray(new ParameterMapping[parameterMappingList
                .size()]);
        parameterMappingIndex.clear();
        for (int i = 0; i < parameterMappings.length; i++) {
            parameterMappingIndex.put(parameterMappings[i].getPropertyName(), new Integer(i));
        }
        Map props = new HashMap();
        props.put("map", this);

        dataExchange = delegate.getDataExchangeFactory().getDataExchangeForClass(parameterClass);
        dataExchange.initialize(props);
    }

    public int getParameterIndex(String propertyName) {
        Integer idx = null;
        idx = (Integer) parameterMappingIndex.get(propertyName);
        return idx == null ? -1 : idx.intValue();
    }

    public int getParameterCount() {
        return this.parameterMappings.length;
    }

    /**
     * @param ps
     * @param parameters
     * @throws java.sql.SQLException
     */
    public void setParameters(StatementScope statementScope, PreparedStatement ps, Object[] parameters) throws SQLException {

        ErrorContext errorContext = statementScope.getErrorContext();
        errorContext.setActivity("applying a parameter map");
        errorContext.setObjectId(this.getId());
        errorContext.setResource(this.getResource());
        errorContext.setMoreInfo("Check the parameter map.");

        if (parameterMappings != null) {
            for (int i = 0; i < parameterMappings.length; i++) {
                ParameterMapping mapping = parameterMappings[i];
                errorContext.setMoreInfo(mapping.getErrorString());
                if (mapping.isInputAllowed()) {
                    setParameter(ps, mapping, parameters, i);
                }
            }
        }
    }

    public Object[] getParameterObjectValues(StatementScope statementScope, Object parameterObject) {
        return dataExchange.getData(statementScope, this, parameterObject);
    }

    public CacheKey getCacheKey(StatementScope statementScope, Object parameterObject) {
        return dataExchange.getCacheKey(statementScope, this, parameterObject);
    }

    public void refreshParameterObjectValues(StatementScope statementScope, Object parameterObject, Object[] values) {
        dataExchange.setData(statementScope, this, parameterObject, values);
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    protected void setParameter(PreparedStatement ps, ParameterMapping mapping, Object[] parameters, int i) throws SQLException {
        Object value = parameters[i];
        // Apply Null Value
        String nullValueString = mapping.getNullValue();
        if (nullValueString != null) {
            TypeHandler handler = mapping.getTypeHandler();
            if (handler.equals(value, nullValueString)) {
                value = null;
            }
        }

        // Set Parameter
        TypeHandler typeHandler = mapping.getTypeHandler();
        if (value != null) {
            typeHandler.setParameter(ps, i + 1, value, mapping.getJdbcTypeName());
        } else if (typeHandler instanceof CustomTypeHandler) {
            // 和上面的if一样
            typeHandler.setParameter(ps, i + 1, value, mapping.getJdbcTypeName());
        } else {
            int jdbcType = mapping.getJdbcType();
            if (jdbcType != JdbcTypeRegistry.UNKNOWN_TYPE) {
                ps.setNull(i + 1, jdbcType);
            } else {
                ps.setNull(i + 1, Types.OTHER);
            }
        }
    }

}
