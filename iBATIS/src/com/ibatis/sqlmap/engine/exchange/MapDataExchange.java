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
package com.ibatis.sqlmap.engine.exchange;

import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMap;
import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMapping;
import com.ibatis.sqlmap.engine.mapping.result.ResultMap;
import com.ibatis.sqlmap.engine.mapping.result.ResultMapping;
import com.ibatis.sqlmap.engine.scope.StatementScope;

import java.util.HashMap;
import java.util.Map;

/**
 * DataExchange implementation for Map objects
 */
public class MapDataExchange extends BaseDataExchange implements DataExchange {

    protected MapDataExchange(DataExchangeFactory dataExchangeFactory) {
        super(dataExchangeFactory);
    }

    public void initialize(Map properties) {
    }

    public Object[] getData(StatementScope statementScope, ParameterMap parameterMap, Object parameterObject) {
        if (!(parameterObject instanceof Map)) {
            throw new RuntimeException("Error.  Object passed into MapDataExchange was not an instance of Map.");
        }

        Object[] data = new Object[parameterMap.getParameterMappings().length];
        Map map = (Map) parameterObject;
        ParameterMapping[] mappings = parameterMap.getParameterMappings();
        for (int i = 0; i < mappings.length; i++) {
            data[i] = map.get(mappings[i].getPropertyName());
        }
        return data;
    }

    public Object setData(StatementScope statementScope, ResultMap resultMap, Object resultObject, Object[] values) {
        if (!(resultObject == null || resultObject instanceof Map)) {
            throw new RuntimeException("Error.  Object passed into MapDataExchange was not an instance of Map.");
        }

        Map map = (Map) resultObject;
        if (map == null) {
            map = new HashMap();
        }

        ResultMapping[] mappings = resultMap.getResultMappings();
        for (int i = 0; i < mappings.length; i++) {
            map.put(mappings[i].getPropertyName(), values[i]);
        }

        return map;
    }

    public Object setData(StatementScope statementScope, ParameterMap parameterMap, Object parameterObject, Object[] values) {
        if (!(parameterObject == null || parameterObject instanceof Map)) {
            throw new RuntimeException("Error.  Object passed into MapDataExchange was not an instance of Map.");
        }

        Map map = (Map) parameterObject;
        if (map == null) {
            map = new HashMap();
        }

        ParameterMapping[] mappings = parameterMap.getParameterMappings();
        for (int i = 0; i < mappings.length; i++) {
            if (mappings[i].isOutputAllowed()) {
                map.put(mappings[i].getPropertyName(), values[i]);
            }
        }

        return map;
    }

}
