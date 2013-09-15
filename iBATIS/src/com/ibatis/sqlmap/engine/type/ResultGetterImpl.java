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

import com.ibatis.sqlmap.client.extensions.ResultGetter;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

/**
 * A ResultGetter implementation
 */
public class ResultGetterImpl implements ResultGetter {

    private ResultSet rs;
    private String name;
    private int index;

    /**
     * Creates an instance for a PreparedStatement and column index
     * 
     * @param resultSet - the result set 
     * @param columnIndex - the column index
     */
    public ResultGetterImpl(ResultSet resultSet, int columnIndex) {
        this.rs = resultSet;
        this.index = columnIndex;
    }

    /**
     * Creates an instance for a PreparedStatement and column name
     * 
     * @param resultSet - the result set
     * @param columnName - the column index
     */
    public ResultGetterImpl(ResultSet resultSet, String columnName) {
        this.rs = resultSet;
        this.name = columnName;
    }

    public Array getArray() throws SQLException {
        if (name != null) {
            return rs.getArray(name);
        } else {
            return rs.getArray(index);
        }
    }

    public BigDecimal getBigDecimal() throws SQLException {
        if (name != null) {
            return rs.getBigDecimal(name);
        } else {
            return rs.getBigDecimal(index);
        }
    }

    public Blob getBlob() throws SQLException {
        if (name != null) {
            return rs.getBlob(name);
        } else {
            return rs.getBlob(index);
        }
    }

    public boolean getBoolean() throws SQLException {
        if (name != null) {
            return rs.getBoolean(name);
        } else {
            return rs.getBoolean(index);
        }
    }

    public byte getByte() throws SQLException {
        if (name != null) {
            return rs.getByte(name);
        } else {
            return rs.getByte(index);
        }
    }

    public byte[] getBytes() throws SQLException {
        if (name != null) {
            return rs.getBytes(name);
        } else {
            return rs.getBytes(index);
        }
    }

    public Clob getClob() throws SQLException {
        if (name != null) {
            return rs.getClob(name);
        } else {
            return rs.getClob(index);
        }
    }

    public Date getDate() throws SQLException {
        if (name != null) {
            return rs.getDate(name);
        } else {
            return rs.getDate(index);
        }
    }

    public Date getDate(Calendar cal) throws SQLException {
        if (name != null) {
            return rs.getDate(name, cal);
        } else {
            return rs.getDate(index, cal);
        }
    }

    public double getDouble() throws SQLException {
        if (name != null) {
            return rs.getDouble(name);
        } else {
            return rs.getDouble(index);
        }
    }

    public float getFloat() throws SQLException {
        if (name != null) {
            return rs.getFloat(name);
        } else {
            return rs.getFloat(index);
        }
    }

    public int getInt() throws SQLException {
        if (name != null) {
            return rs.getInt(name);
        } else {
            return rs.getInt(index);
        }
    }

    public long getLong() throws SQLException {
        if (name != null) {
            return rs.getLong(name);
        } else {
            return rs.getLong(index);
        }
    }

    public Object getObject() throws SQLException {
        if (name != null) {
            return rs.getObject(name);
        } else {
            return rs.getObject(index);
        }
    }

    public Object getObject(Map map) throws SQLException {
        if (name != null) {
            return rs.getObject(name, map);
        } else {
            return rs.getObject(index, map);
        }
    }

    public Ref getRef() throws SQLException {
        if (name != null) {
            return rs.getRef(name);
        } else {
            return rs.getRef(index);
        }
    }

    public short getShort() throws SQLException {
        if (name != null) {
            return rs.getShort(name);
        } else {
            return rs.getShort(index);
        }
    }

    public String getString() throws SQLException {
        if (name != null) {
            return rs.getString(name);
        } else {
            return rs.getString(index);
        }
    }

    public Time getTime() throws SQLException {
        if (name != null) {
            return rs.getTime(name);
        } else {
            return rs.getTime(index);
        }
    }

    public Time getTime(Calendar cal) throws SQLException {
        if (name != null) {
            return rs.getTime(name);
        } else {
            return rs.getTime(index);
        }
    }

    public Timestamp getTimestamp() throws SQLException {
        if (name != null) {
            return rs.getTimestamp(name);
        } else {
            return rs.getTimestamp(index);
        }
    }

    public Timestamp getTimestamp(Calendar cal) throws SQLException {
        if (name != null) {
            return rs.getTimestamp(name, cal);
        } else {
            return rs.getTimestamp(index, cal);
        }
    }

    public URL getURL() throws SQLException {
        if (name != null) {
            return rs.getURL(name);
        } else {
            return rs.getURL(index);
        }
    }

    public boolean wasNull() throws SQLException {
        return rs.wasNull();
    }

    public ResultSet getResultSet() {
        return rs;
    }

    public int getColumnIndex() {
        return index;
    }

    public String getColumnName() {
        return name;
    }
}
