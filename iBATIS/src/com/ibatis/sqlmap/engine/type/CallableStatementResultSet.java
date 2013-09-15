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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

/**
 * A way to make a CallableStatement look like a ResultSet 
 */
public class CallableStatementResultSet implements ResultSet {
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateBlob(String columnLabel, Reader reader) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateBlob(int columnIndex, Reader reader) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateBlob(String str, InputStream is) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateBlob(int i, java.io.InputStream b) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateBinaryStream(java.lang.String a, java.io.InputStream b) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateAsciiStream(java.lang.String a, java.io.InputStream b) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateCharacterStream(int a, java.io.Reader b) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateBinaryStream(int a, java.io.InputStream b) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateAsciiStream(int a, java.io.InputStream b) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateNCharacterStream(java.lang.String a, java.io.Reader b) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateNCharacterStream(int a, java.io.Reader b) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateNClob(java.lang.String a, java.io.Reader b, long c) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateNClob(int a, java.io.Reader b, long c) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateClob(java.lang.String a, java.io.Reader b, long c) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateCharacterStream(java.lang.String a, java.io.Reader b) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateClob(int a, java.io.Reader b, long c) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateBlob(java.lang.String a, java.io.InputStream b, long c) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateBlob(int a, java.io.InputStream b, long c) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateCharacterStream(java.lang.String a, java.io.Reader b, long c) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateBinaryStream(java.lang.String a, java.io.InputStream b, long c) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateAsciiStream(java.lang.String a, java.io.InputStream b, long c) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateCharacterStream(int a, java.io.Reader b, long c) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateBinaryStream(int a, java.io.InputStream b, long c) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateAsciiStream(int a, java.io.InputStream b, long c) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateNCharacterStream(java.lang.String a, java.io.Reader b, long c) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateNCharacterStream(int a, java.io.Reader b, long c) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public java.io.Reader getNCharacterStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public RowId getRowId(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public RowId getRowId(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public int getHoldability() throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public boolean isClosed() throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public String getNString(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public String getNString(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public java.io.Reader getNCharacterStream(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateSQLXML(java.lang.String a, java.sql.SQLXML b) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateSQLXML(int a, java.sql.SQLXML b) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public NClob getNClob(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public NClob getNClob(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateNClob(java.lang.String a, java.sql.NClob b) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateNClob(int a, java.sql.NClob b) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateNString(java.lang.String a, java.lang.String b) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    public void updateNString(int a, java.lang.String b) throws SQLException {
        throw new UnsupportedOperationException("CallableStatementResultSet does not support this method.");
    }

    private CallableStatement cs;

    /**
     * Constructor to stretch a ResultSet interface over a CallableStatement
     *  
     * @param cs - the CallableStatement
     */
    public CallableStatementResultSet(CallableStatement cs) {
        this.cs = cs;
    }

    public boolean absolute(int row) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void afterLast() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void beforeFirst() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void cancelRowUpdates() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void clearWarnings() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void close() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void deleteRow() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public int findColumn(String columnName) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public boolean first() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public Array getArray(String colName) throws SQLException {
        return cs.getArray(colName);
    }

    public Array getArray(int i) throws SQLException {
        return cs.getArray(i);
    }

    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public InputStream getAsciiStream(String columnName) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return cs.getBigDecimal(columnIndex);
    }

    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        return cs.getBigDecimal(columnName);
    }

    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public InputStream getBinaryStream(String columnName) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public Blob getBlob(String colName) throws SQLException {
        return cs.getBlob(colName);
    }

    public Blob getBlob(int i) throws SQLException {
        return cs.getBlob(i);
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        return cs.getBoolean(columnIndex);
    }

    public boolean getBoolean(String columnName) throws SQLException {
        return cs.getBoolean(columnName);
    }

    public byte getByte(int columnIndex) throws SQLException {
        return cs.getByte(columnIndex);
    }

    public byte getByte(String columnName) throws SQLException {
        return cs.getByte(columnName);
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        return cs.getBytes(columnIndex);
    }

    public byte[] getBytes(String columnName) throws SQLException {
        return cs.getBytes(columnName);
    }

    public Reader getCharacterStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public Reader getCharacterStream(String columnName) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public Clob getClob(String colName) throws SQLException {
        return cs.getClob(colName);
    }

    public Clob getClob(int i) throws SQLException {
        return cs.getClob(i);
    }

    public int getConcurrency() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public String getCursorName() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public Date getDate(int columnIndex) throws SQLException {
        return cs.getDate(columnIndex);
    }

    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return cs.getDate(columnIndex, cal);
    }

    public Date getDate(String columnName) throws SQLException {
        return cs.getDate(columnName);
    }

    public Date getDate(String columnName, Calendar cal) throws SQLException {
        return cs.getDate(columnName, cal);
    }

    public double getDouble(int columnIndex) throws SQLException {
        return cs.getDouble(columnIndex);
    }

    public double getDouble(String columnName) throws SQLException {
        return cs.getDouble(columnName);
    }

    public int getFetchDirection() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public int getFetchSize() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public float getFloat(int columnIndex) throws SQLException {
        return cs.getFloat(columnIndex);
    }

    public float getFloat(String columnName) throws SQLException {
        return cs.getFloat(columnName);
    }

    public int getInt(int columnIndex) throws SQLException {
        return cs.getInt(columnIndex);
    }

    public int getInt(String columnName) throws SQLException {
        return cs.getInt(columnName);
    }

    public long getLong(int columnIndex) throws SQLException {
        return cs.getLong(columnIndex);
    }

    public long getLong(String columnName) throws SQLException {
        return cs.getLong(columnName);
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public Object getObject(String colName, Map map) throws SQLException {
        return cs.getObject(colName, map);
    }

    public Object getObject(int columnIndex) throws SQLException {
        return cs.getObject(columnIndex);
    }

    public Object getObject(String columnName) throws SQLException {
        return cs.getObject(columnName);
    }

    public Object getObject(int i, Map map) throws SQLException {
        return cs.getObject(i, map);
    }

    public Ref getRef(String colName) throws SQLException {
        return cs.getRef(colName);
    }

    public Ref getRef(int i) throws SQLException {
        return cs.getRef(i);
    }

    public int getRow() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public short getShort(int columnIndex) throws SQLException {
        return cs.getShort(columnIndex);
    }

    public short getShort(String columnName) throws SQLException {
        return cs.getShort(columnName);
    }

    public Statement getStatement() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public String getString(int columnIndex) throws SQLException {
        return cs.getString(columnIndex);
    }

    public String getString(String columnName) throws SQLException {
        return cs.getString(columnName);
    }

    public Time getTime(int columnIndex) throws SQLException {
        return cs.getTime(columnIndex);
    }

    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return cs.getTime(columnIndex, cal);
    }

    public Time getTime(String columnName) throws SQLException {
        return cs.getTime(columnName);
    }

    public Time getTime(String columnName, Calendar cal) throws SQLException {
        return cs.getTime(columnName, cal);
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return cs.getTimestamp(columnIndex);
    }

    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return cs.getTimestamp(columnIndex, cal);
    }

    public Timestamp getTimestamp(String columnName) throws SQLException {
        return cs.getTimestamp(columnName);
    }

    public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
        return cs.getTimestamp(columnName, cal);
    }

    public int getType() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public InputStream getUnicodeStream(String columnName) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public URL getURL(int columnIndex) throws SQLException {
        return cs.getURL(columnIndex);
    }

    public URL getURL(String columnName) throws SQLException {
        return cs.getURL(columnName);
    }

    public SQLWarning getWarnings() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void insertRow() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public boolean isAfterLast() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public boolean isBeforeFirst() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public boolean isFirst() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public boolean isLast() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public boolean last() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void moveToCurrentRow() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void moveToInsertRow() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public boolean next() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public boolean previous() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void refreshRow() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public boolean relative(int rows) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public boolean rowDeleted() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public boolean rowInserted() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public boolean rowUpdated() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void setFetchDirection(int direction) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void setFetchSize(int rows) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateArray(int columnIndex, Array x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateArray(String columnName, Array x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateBlob(String columnName, Blob x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateBoolean(String columnName, boolean x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateByte(int columnIndex, byte x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateByte(String columnName, byte x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateBytes(int columnIndex, byte x[]) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateBytes(String columnName, byte x[]) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateClob(int columnIndex, Clob x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateClob(String columnName, Clob x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateDate(int columnIndex, Date x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateDate(String columnName, Date x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateDouble(int columnIndex, double x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateDouble(String columnName, double x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateFloat(int columnIndex, float x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateFloat(String columnName, float x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateInt(int columnIndex, int x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateInt(String columnName, int x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateLong(int columnIndex, long x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateLong(String columnName, long x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateNull(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateNull(String columnName) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateObject(int columnIndex, Object x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateObject(String columnName, Object x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateObject(String columnName, Object x, int scale) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateRef(int columnIndex, Ref x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateRef(String columnName, Ref x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateRow() throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateShort(int columnIndex, short x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateShort(String columnName, short x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateString(int columnIndex, String x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateString(String columnName, String x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateTime(int columnIndex, Time x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateTime(String columnName, Time x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
        throw new UnsupportedOperationException("CallableStatement does not support this method.");
    }

    public boolean wasNull() throws SQLException {
        return cs.wasNull();
    }

}
