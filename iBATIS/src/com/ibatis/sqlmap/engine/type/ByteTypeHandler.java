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
 * Byte implementation of TypeHandler
 */
public class ByteTypeHandler extends BaseTypeHandler implements TypeHandler {

    public void setParameter(PreparedStatement ps, int i, Object parameter, String jdbcType) throws SQLException {
        ps.setByte(i, ((Byte) parameter).byteValue());
    }

    public Object getResult(ResultSet rs, String columnName) throws SQLException {
        byte b = rs.getByte(columnName);
        if (rs.wasNull()) {
            return null;
        } else {
            return new Byte(b);
        }
    }

    public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
        byte b = rs.getByte(columnIndex);
        if (rs.wasNull()) {
            return null;
        } else {
            return new Byte(b);
        }
    }

    public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
        byte b = cs.getByte(columnIndex);
        if (cs.wasNull()) {
            return null;
        } else {
            return new Byte(b);
        }
    }

    public Object valueOf(String s) {
        return Byte.valueOf(s);
    }

}
