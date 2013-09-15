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
package com.ibatis.sqlmap.engine.mapping.result;

import com.ibatis.common.beans.*;
import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate;
import com.ibatis.sqlmap.engine.scope.StatementScope;
import com.ibatis.sqlmap.engine.type.DomTypeMarker;

import java.sql.*;
import java.util.*;

/**
 * An automatic result map for simple stuff
 */
public class AutoResultMap extends ResultMap {

    /**
     * Constructor to pass in the SqlMapExecutorDelegate
     * 
     * @param delegate
     *            - the delegate
     */
    public AutoResultMap(SqlMapExecutorDelegate delegate, boolean allowRemapping) {
        super(delegate);
        this.allowRemapping = allowRemapping;
    }

    public synchronized Object[] getResults(StatementScope statementScope, ResultSet rs) throws SQLException {
        if (allowRemapping || getResultMappings() == null) {
            initialize(rs);
        }
        return super.getResults(statementScope, rs);
    }

    public Object setResultObjectValues(StatementScope statementScope, Object resultObject, Object[] values) {
        // synchronization is only needed when remapping is enabled
        if (allowRemapping) {
            synchronized (this) {
                return super.setResultObjectValues(statementScope, resultObject, values);
            }
        }
        return super.setResultObjectValues(statementScope, resultObject, values);
    }

    private void initialize(ResultSet rs) {
        if (getResultClass() == null) {
            throw new SqlMapException("The automatic ResultMap named " + this.getId() + " had a null result class (not allowed).");
        } else if (Map.class.isAssignableFrom(getResultClass())) {
            initializeMapResults(rs);
        } else if (getDelegate().getTypeHandlerFactory().getTypeHandler(getResultClass()) != null) {
            initializePrimitiveResults(rs);
        } else if (DomTypeMarker.class.isAssignableFrom(getResultClass())) {
            initializeXmlResults(rs);
        } else {
            initializeBeanResults(rs);
        }
    }

    private void initializeBeanResults(ResultSet rs) {
        try {
            ClassInfo classInfo = ClassInfo.getInstance(getResultClass());
            String[] propertyNames = classInfo.getWriteablePropertyNames();

            Map propertyMap = new HashMap();
            for (int i = 0; i < propertyNames.length; i++) {
                propertyMap.put(propertyNames[i].toUpperCase(java.util.Locale.ENGLISH), propertyNames[i]);
            }

            List resultMappingList = new ArrayList();
            ResultSetMetaData rsmd = rs.getMetaData();
            for (int i = 0, n = rsmd.getColumnCount(); i < n; i++) {
                String columnName = getColumnIdentifier(rsmd, i + 1);
                String upperColumnName = columnName.toUpperCase(java.util.Locale.ENGLISH);
                String matchedProp = (String) propertyMap.get(upperColumnName);
                Class type = null;
                if (matchedProp == null) {
                    Probe p = ProbeFactory.getProbe(this.getResultClass());
                    try {
                        type = p.getPropertyTypeForSetter(this.getResultClass(), columnName);
                    } catch (Exception e) {
                        // TODO - add logging to this class?
                    }
                } else {
                    type = classInfo.getSetterType(matchedProp);
                }
                if (type != null || matchedProp != null) {
                    ResultMapping resultMapping = new ResultMapping();
                    resultMapping.setPropertyName((matchedProp != null ? matchedProp : columnName));
                    resultMapping.setColumnName(columnName);
                    resultMapping.setColumnIndex(i + 1);
                    resultMapping.setTypeHandler(getDelegate().getTypeHandlerFactory().getTypeHandler(type)); // map
                                                                                                              // SQL
                                                                                                              // to
                                                                                                              // JDBC
                                                                                                              // type
                    resultMappingList.add(resultMapping);
                }
            }
            setResultMappingList(resultMappingList);

        } catch (SQLException e) {
            throw new RuntimeException("Error automapping columns. Cause: " + e);
        }

    }

    private void initializeXmlResults(ResultSet rs) {
        try {
            List resultMappingList = new ArrayList();
            ResultSetMetaData rsmd = rs.getMetaData();
            for (int i = 0, n = rsmd.getColumnCount(); i < n; i++) {
                String columnName = getColumnIdentifier(rsmd, i + 1);
                ResultMapping resultMapping = new ResultMapping();
                resultMapping.setPropertyName(columnName);
                resultMapping.setColumnName(columnName);
                resultMapping.setColumnIndex(i + 1);
                resultMapping.setTypeHandler(getDelegate().getTypeHandlerFactory().getTypeHandler(String.class));
                resultMappingList.add(resultMapping);
            }
            setResultMappingList(resultMappingList);
        } catch (SQLException e) {
            throw new RuntimeException("Error automapping columns. Cause: " + e);
        }
    }

    private void initializeMapResults(ResultSet rs) {
        try {
            List resultMappingList = new ArrayList();
            ResultSetMetaData rsmd = rs.getMetaData();
            for (int i = 0, n = rsmd.getColumnCount(); i < n; i++) {
                String columnName = getColumnIdentifier(rsmd, i + 1);
                ResultMapping resultMapping = new ResultMapping();
                resultMapping.setPropertyName(columnName);
                resultMapping.setColumnName(columnName);
                resultMapping.setColumnIndex(i + 1);
                resultMapping.setTypeHandler(getDelegate().getTypeHandlerFactory().getTypeHandler(Object.class));
                resultMappingList.add(resultMapping);
            }

            setResultMappingList(resultMappingList);

        } catch (SQLException e) {
            throw new RuntimeException("Error automapping columns. Cause: " + e);
        }
    }

    private void initializePrimitiveResults(ResultSet rs) {
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            String columnName = getColumnIdentifier(rsmd, 1);
            ResultMapping resultMapping = new ResultMapping();
            resultMapping.setPropertyName(columnName);
            resultMapping.setColumnName(columnName);
            resultMapping.setColumnIndex(1);
            resultMapping.setTypeHandler(getDelegate().getTypeHandlerFactory().getTypeHandler(getResultClass()));

            List resultMappingList = new ArrayList();
            resultMappingList.add(resultMapping);

            setResultMappingList(resultMappingList);

        } catch (SQLException e) {
            throw new RuntimeException("Error automapping columns. Cause: " + e);
        }
    }

    private String getColumnIdentifier(ResultSetMetaData rsmd, int i) throws SQLException {
        if (delegate.isUseColumnLabel()) {
            return rsmd.getColumnLabel(i);
        } else {
            return rsmd.getColumnName(i);
        }
    }

}
