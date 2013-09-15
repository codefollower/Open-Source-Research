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

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.engine.type.JdbcTypeRegistry;
import com.ibatis.sqlmap.engine.type.TypeHandler;

public class ParameterMapping {

    private static final String MODE_INOUT = "INOUT";
    private static final String MODE_OUT = "OUT";
    private static final String MODE_IN = "IN";

    private String propertyName;
    private TypeHandler typeHandler;
    private String typeName; // this is used for REF types or user-defined types
    private int jdbcType;
    private String jdbcTypeName;
    private String nullValue;
    private String mode;
    private boolean inputAllowed;
    private boolean outputAllowed;
    private Class javaType;
    private String resultMapName;
    private Integer numericScale;

    private String errorString;

    public ParameterMapping() {
        mode = "IN";
        inputAllowed = true;
        outputAllowed = false;
    }

    public String getNullValue() {
        return nullValue;
    }

    public void setNullValue(String nullValue) {
        this.nullValue = nullValue;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.errorString = "Check the parameter mapping for the '" + propertyName + "' property.";
        this.propertyName = propertyName;
    }

    public String getErrorString() {
        return errorString;
    }

    public TypeHandler getTypeHandler() {
        return typeHandler;
    }

    public void setTypeHandler(TypeHandler typeHandler) {
        this.typeHandler = typeHandler;
    }

    public Class getJavaType() {
        return javaType;
    }

    public void setJavaType(Class javaType) {
        this.javaType = javaType;
    }

    public String getJavaTypeName() {
        if (javaType == null) {
            return null;
        } else {
            return javaType.getName();
        }
    }

    public void setJavaTypeName(String javaTypeName) {
        try {
            if (javaTypeName == null) {
                this.javaType = null;
            } else {
                this.javaType = Resources.classForName(javaTypeName);
            }
        } catch (ClassNotFoundException e) {
            throw new SqlMapException("Error setting javaType property of ParameterMap.  Cause: " + e, e);
        }
    }

    public int getJdbcType() {
        return jdbcType;
    }

    public String getJdbcTypeName() {
        return jdbcTypeName;
    }

    public void setJdbcTypeName(String jdbcTypeName) {
        this.jdbcTypeName = jdbcTypeName;
        this.jdbcType = JdbcTypeRegistry.getType(jdbcTypeName);
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
        inputAllowed = MODE_IN.equals(mode) || MODE_INOUT.equals(mode);
        outputAllowed = MODE_OUT.equals(mode) || MODE_INOUT.equals(mode);
    }

    public boolean isInputAllowed() {
        return inputAllowed;
    }

    public boolean isOutputAllowed() {
        return outputAllowed;
    }

    /**
     * user-defined or REF types
     *
     * @return typeName
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * for user-defined or REF types
     * @param typeName
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getResultMapName() {
        return resultMapName;
    }

    public void setResultMapName(String resultMapName) {
        this.resultMapName = resultMapName;
    }

    public Integer getNumericScale() {
        return numericScale;
    }

    public void setNumericScale(Integer numericScale) {
        if (numericScale != null && numericScale.intValue() < 0) {
            throw new RuntimeException(
                    "Error setting numericScale on parameter mapping.  Cause: scale must be greater than or equal to zero");
        }
        this.numericScale = numericScale;
    }

}
