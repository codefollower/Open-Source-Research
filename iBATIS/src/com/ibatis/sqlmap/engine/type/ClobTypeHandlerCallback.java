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

import com.ibatis.sqlmap.client.extensions.*;

import java.io.StringReader;
import java.sql.*;

public class ClobTypeHandlerCallback implements TypeHandlerCallback {

    public Object getResult(ResultGetter getter) throws SQLException {
        String value;
        Clob clob = getter.getClob();
        if (!getter.wasNull()) {
            int size = (int) clob.length();
            value = clob.getSubString(1, size);
        } else {
            value = null;
        }

        return value;
    }

    public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
        String s = (String) parameter;
        if (s != null) {
            StringReader reader = new StringReader(s);
            setter.setCharacterStream(reader, s.length());
        } else {
            setter.setNull(Types.CLOB);
        }
    }

    public Object valueOf(String s) {
        return s;
    }

}
