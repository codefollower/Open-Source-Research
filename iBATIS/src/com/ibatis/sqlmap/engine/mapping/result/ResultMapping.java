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

import com.ibatis.sqlmap.engine.type.JdbcTypeRegistry;
import com.ibatis.sqlmap.engine.type.TypeHandler;

/**
 * Basic implementation of ResultMapping
 */

// 对应<resultMap><result>元素
public class ResultMapping {

    private String propertyName;
    private String columnName;
    private int columnIndex;
    private TypeHandler typeHandler;
    private int jdbcType;
    private String jdbcTypeName;
    private String nullValue;
    private String notNullColumn;
    private String statementName;
    private Class javaType;

    private String nestedResultMapName;

    private String errorString;

    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Setter for the object property name (used by the automap, and the
     * builder)
     * 
     * @param propertyName
     *            - the property name
     */
    public void setPropertyName(String propertyName) {
        this.errorString = "Check the result mapping for the '" + propertyName + "' property.";
        this.propertyName = propertyName;
    }

    /**
     * Getter for the error message when something goes wrong mapping this
     * property
     * 
     * @return - the error message
     */
    public String getErrorString() {
        return errorString;
    }

    /**
     * Getter for the column name that we are mapping
     * 
     * @return - the column name
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * Setter for the column name we are mapping (used by the automap or
     * builder)
     * 
     * @param columnName
     *            - the column name
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * Getter for the column index that we are mapping
     * 
     * @return - the column index
     */
    public int getColumnIndex() {
        return columnIndex;
    }

    /**
     * Setter for the column index we are mapping (used by the automap or
     * builder)
     * 
     * @param columnIndex
     *            - the column index
     */
    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    /**
     * Getter for the type handler for the column
     * 
     * @return - the type handler
     */
    public TypeHandler getTypeHandler() {
        return typeHandler;
    }

    /**
     * Setter for the type handler for the column
     * 
     * @param typeHandler
     *            - the type handler
     */
    public void setTypeHandler(TypeHandler typeHandler) {
        this.typeHandler = typeHandler;
    }

    /**
     * Setter for the Java type of the column
     * 
     * @return - the Java type
     */
    public Class getJavaType() {
        return javaType;
    }

    /**
     * Setter for the Java type of the column
     * 
     * @param javaType
     *            - the Java type
     */
    public void setJavaType(Class javaType) {
        this.javaType = javaType;
    }

    /**
     * Getter for the JDBC type of the column
     * 
     * @return - the JDBC type
     */
    public int getJdbcType() {
        return jdbcType;
    }

    /**
     * Getter for the JDBC type name of the column
     * 
     * @return - the JDBC type name
     */
    public String getJdbcTypeName() {
        return jdbcTypeName;
    }

    /**
     * Setter for the JDBC type name of the column
     * 
     * @param jdbcTypeName
     *            - the JDBC type name
     */
    public void setJdbcTypeName(String jdbcTypeName) {
        this.jdbcTypeName = jdbcTypeName;
        this.jdbcType = JdbcTypeRegistry.getType(jdbcTypeName);
    }

    /**
     * Getter for what to return if the column is null
     * 
     * @return - the null substitution
     */
    public String getNullValue() {
        return nullValue;
    }

    /**
     * Setter for what to return if the column is null
     * 
     * @param nullValue
     *            - the null substitution
     */
    public void setNullValue(String nullValue) {
        this.nullValue = nullValue;
    }

    /**
     * Getter for the name of the column to check for null before instantiating
     * a nested resultMapping value
     * 
     * @return - the null substitution
     */
    public String getNotNullColumn() {
        return notNullColumn;
    }

    /**
     * Setter for the name of the column to check for null before instantiating
     * a nested resultMapping value
     * 
     * @param notNullColumn
     *            - the column name
     */
    public void setNotNullColumn(String notNullColumn) {
        this.notNullColumn = notNullColumn;
    }

    /**
     * Getter for the name of the statement
     * 
     * @return - the name
     */
    public String getStatementName() {
        return statementName;
    }

    /**
     * Setter for the name of the statement
     * 
     * @param statementName
     *            - the name
     */
    public void setStatementName(String statementName) {
        this.statementName = statementName;
    }

    public String getNestedResultMapName() {
        return nestedResultMapName;
    }

    public void setNestedResultMapName(String nestedResultMapName) {
        this.nestedResultMapName = nestedResultMapName;
    }

}
