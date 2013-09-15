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
import com.ibatis.sqlmap.engine.scope.StatementScope;

import java.util.Map;

/**
 * DataExchange implementation for primitive objects
 */
public class PrimitiveDataExchange extends BaseDataExchange implements DataExchange {

    protected PrimitiveDataExchange(DataExchangeFactory dataExchangeFactory) {
        super(dataExchangeFactory);
    }

    public void initialize(Map properties) {
    }

    public Object[] getData(StatementScope statementScope, ParameterMap parameterMap, Object parameterObject) {
        ParameterMapping[] mappings = parameterMap.getParameterMappings();
        Object[] data = new Object[mappings.length];
        for (int i = 0; i < mappings.length; i++) {
            data[i] = parameterObject;
        }
        return data;
    }

    public Object setData(StatementScope statementScope, ResultMap resultMap, Object resultObject, Object[] values) {
        return values[0];
    }

    public Object setData(StatementScope statementScope, ParameterMap parameterMap, Object parameterObject, Object[] values) {
        return values[0];
    }

}
