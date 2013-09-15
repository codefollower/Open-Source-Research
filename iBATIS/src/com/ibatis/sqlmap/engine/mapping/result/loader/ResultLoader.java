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
package com.ibatis.sqlmap.engine.mapping.result.loader;

import com.ibatis.sqlmap.engine.impl.SqlMapClientImpl;
import com.ibatis.sqlmap.engine.type.DomCollectionTypeMarker;

import java.sql.SQLException;
import java.util.*;
import java.lang.reflect.Array;

/**
 * Class to load results into objects
 */
public class ResultLoader {

    private ResultLoader() {
    }

    /**
     * Loads a result lazily
     * 
     * @param client
     *            - the client creating the object
     * @param statementName
     *            - the name of the statement to be used
     * @param parameterObject
     *            - the parameters for the statement
     * @param targetType
     *            - the target type of the result
     * @return the loaded result
     * @throws SQLException
     */
    public static Object loadResult(SqlMapClientImpl client, String statementName, Object parameterObject, Class targetType)
            throws SQLException {
        Object value = null;

        if (client.isLazyLoadingEnabled()) {
            if (client.isEnhancementEnabled()) {
                EnhancedLazyResultLoader lazy = new EnhancedLazyResultLoader(client, statementName, parameterObject, targetType);
                value = lazy.loadResult();
            } else {
                LazyResultLoader lazy = new LazyResultLoader(client, statementName, parameterObject, targetType);
                value = lazy.loadResult();
            }
        } else {
            value = getResult(client, statementName, parameterObject, targetType);
        }

        return value;
    }

    protected static Object getResult(SqlMapClientImpl client, String statementName, Object parameterObject, Class targetType)
            throws SQLException {
        Object value = null;
        if (DomCollectionTypeMarker.class.isAssignableFrom(targetType)) {
            value = client.queryForList(statementName, parameterObject);
        } else if (Set.class.isAssignableFrom(targetType)) {
            value = new HashSet(client.queryForList(statementName, parameterObject));
        } else if (Collection.class.isAssignableFrom(targetType)) {
            value = client.queryForList(statementName, parameterObject);
        } else if (targetType.isArray()) {
            List list = client.queryForList(statementName, parameterObject);
            value = listToArray(list, targetType.getComponentType());
        } else {
            value = client.queryForObject(statementName, parameterObject);
        }
        return value;
    }

    private static Object listToArray(List list, Class type) {
        Object array = java.lang.reflect.Array.newInstance(type, list.size());
        if (type.isPrimitive()) {
            Iterator iter = list.iterator();
            int index = 0;
            while (iter.hasNext()) {
                Array.set(array, index++, iter.next());
            }
        } else {
            array = list.toArray((Object[]) array);
        }
        return array;

    }

}
