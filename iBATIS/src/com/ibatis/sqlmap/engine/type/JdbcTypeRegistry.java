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

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton
 */
public class JdbcTypeRegistry {

    /**
     * Value for the unknown type
     */
    public static final int UNKNOWN_TYPE = -99999999;

    private static final Map TYPE_MAP = new HashMap();

    /**
     * Value for a JDBC 3.o datalink type
     */
    public final static int JDBC_30_DATALINK = 70;

    /**
     * Value for a JDBC 3.o boolean type
     */
    public final static int JDBC_30_BOOLEAN = 16;

    static {
        initializeTypes();
    }

    private JdbcTypeRegistry() {
    }

    public static void setType(String name, int value) {
        TYPE_MAP.put(name, new Integer(value));
    }

    /**
     * Looks up a type by name, and returns it's int value (from java.sql.Types)
     * 
     * @param name - the type name
     * 
     * @return - the int value (from java.sql.Types)
     */
    public static int getType(String name) {
        if (name == null)
            return UNKNOWN_TYPE;
        Integer i = (Integer) TYPE_MAP.get(name);
        if (i != null) {
            return i.intValue();
        } else {
            return UNKNOWN_TYPE;
        }
    }

    private static void initializeTypes() {
        setType("ARRAY", Types.ARRAY);
        setType("BIGINT", Types.BIGINT);
        setType("BINARY", Types.BINARY);
        setType("BIT", Types.BIT);
        setType("BLOB", Types.BLOB);
        setType("BOOLEAN", JDBC_30_BOOLEAN);
        setType("CHAR", Types.CHAR);
        setType("CLOB", Types.CLOB);
        setType("DATALINK", JDBC_30_DATALINK);
        setType("DATE", Types.DATE);
        setType("DECIMAL", Types.DECIMAL);
        setType("DISTINCT", Types.DISTINCT);
        setType("DOUBLE", Types.DOUBLE);
        setType("FLOAT", Types.FLOAT);
        setType("INTEGER", Types.INTEGER);
        setType("JAVA_OBJECT", Types.JAVA_OBJECT);
        setType("LONGVARBINARY", Types.LONGVARBINARY);
        setType("LONGVARCHAR", Types.LONGVARCHAR);
        setType("NULL", Types.NULL);
        setType("NUMERIC", Types.NUMERIC);
        setType("OTHER", Types.OTHER);
        setType("REAL", Types.REAL);
        setType("REF", Types.REF);
        setType("SMALLINT", Types.SMALLINT);
        setType("STRUCT", Types.STRUCT);
        setType("TIME", Types.TIME);
        setType("TIMESTAMP", Types.TIMESTAMP);
        setType("TINYINT", Types.TINYINT);
        setType("VARBINARY", Types.VARBINARY);
        setType("VARCHAR", Types.VARCHAR);

        setType("CH", Types.CHAR);
        setType("VC", Types.VARCHAR);

        setType("DT", Types.DATE);
        setType("TM", Types.TIME);
        setType("TS", Types.TIMESTAMP);

        setType("NM", Types.NUMERIC);
        setType("II", Types.INTEGER);
        setType("BI", Types.BIGINT);
        setType("SI", Types.SMALLINT);
        setType("TI", Types.TINYINT);

        setType("DC", Types.DECIMAL);
        setType("DB", Types.DOUBLE);
        setType("FL", Types.FLOAT);

        setType("ORACLECURSOR", -10);

    }

}
