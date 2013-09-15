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
package com.ibatis.sqlmap.engine.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interface for getting data into, and out of a mapped statement
 */
public interface TypeHandler {

    /**
     * Sets a parameter on a prepared statement
     * 
     * @param ps - the prepared statement
     * @param i - the parameter index
     * @param parameter - the parameter value
     * @param jdbcType - the JDBC type of the parameter
     * 
     * @throws SQLException if setting the parameter fails
     */
    public void setParameter(PreparedStatement ps, int i, Object parameter, String jdbcType) throws SQLException;

    /**
     * Gets a column from a result set
     * 
     * @param rs - the result set
     * @param columnName - the column name to get
     * 
     * @return - the column value
     * 
     * @throws SQLException if getting the value fails
     */
    public Object getResult(ResultSet rs, String columnName) throws SQLException;

    /**
     * Gets a column from a result set
     * 
     * @param rs - the result set
     * @param columnIndex - the column to get (by index)
     * 
     * @return - the column value
     * 
     * @throws SQLException if getting the value fails
     */
    public Object getResult(ResultSet rs, int columnIndex) throws SQLException;

    /**
     * Gets a column from a callable statement
     * 
     * @param cs - the statement
     * @param columnIndex - the column to get (by index)
     * 
     * @return - the column value
     * 
     * @throws SQLException if getting the value fails
     */
    public Object getResult(CallableStatement cs, int columnIndex) throws SQLException;

    /**
     * Converts the String to the type that this handler deals with
     * 
     * @param s - the String value
     * 
     * @return - the converted value
     */
    public Object valueOf(String s);

    /**
     * Compares two values (that this handler deals with) for equality
     * 
     * @param object - one of the objects
     * @param string - the other object as a String
     * 
     * @return - true if they are equal
     */
    public boolean equals(Object object, String string);

}
