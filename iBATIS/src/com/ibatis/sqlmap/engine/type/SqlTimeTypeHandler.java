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
 * SQL time implementation of TypeHandler
 */
public class SqlTimeTypeHandler extends BaseTypeHandler implements TypeHandler {

    private static final String DATE_FORMAT = "hh:mm:ss";

    public void setParameter(PreparedStatement ps, int i, Object parameter, String jdbcType) throws SQLException {
        ps.setTime(i, (java.sql.Time) parameter);
    }

    public Object getResult(ResultSet rs, String columnName) throws SQLException {
        Object sqlTime = rs.getTime(columnName);
        if (rs.wasNull()) {
            return null;
        } else {
            return sqlTime;
        }
    }

    public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
        Object sqlTime = rs.getTime(columnIndex);
        if (rs.wasNull()) {
            return null;
        } else {
            return sqlTime;
        }
    }

    public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object sqlTime = cs.getTime(columnIndex);
        if (cs.wasNull()) {
            return null;
        } else {
            return sqlTime;
        }
    }

    public Object valueOf(String s) {
        return SimpleDateFormatter.format(DATE_FORMAT, s);
    }

}
