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

import com.ibatis.sqlmap.engine.cache.CacheKey;
import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMap;
import com.ibatis.sqlmap.engine.mapping.result.ResultMap;
import com.ibatis.sqlmap.engine.scope.StatementScope;

import java.util.Map;

/**
 * Interface for exchanging data between a parameter map/result map and the related objects
 */
public interface DataExchange {

    /**
     * Initializes the data exchange instance.
     *
     * @param properties
     */
    public void initialize(Map properties);

    /**
     * Gets a data array from a parameter object.
     * 
     * @param statementScope - the scope of the request
     * @param parameterMap - the parameter map
     * @param parameterObject - the parameter object
     * 
     * @return - the objects
     */
    public Object[] getData(StatementScope statementScope, ParameterMap parameterMap, Object parameterObject);

    /**
     * Sets values from a data array into a result object.
     * 
     * @param statementScope - the request scope
     * @param resultMap - the result map
     * @param resultObject - the result object
     * @param values - the values to be mapped
     * 
     * @return the resultObject
     */
    public Object setData(StatementScope statementScope, ResultMap resultMap, Object resultObject, Object[] values);

    /**
     * Sets values from a data array into a parameter object
     * 
     * @param statementScope - the request scope
     * @param parameterMap - the parameter map
     * @param parameterObject - the parameter object
     * @param values - the values to set
     * 
     * @return parameterObject
     */
    public Object setData(StatementScope statementScope, ParameterMap parameterMap, Object parameterObject, Object[] values);

    /**
     * Returns an object capable of being a unique cache key for a parameter object.
     * 
     * @param statementScope - the request scope
     * @param parameterMap - the parameter map
     * @param parameterObject - the parameter object
     * 
     * @return - a cache key
     */
    public CacheKey getCacheKey(StatementScope statementScope, ParameterMap parameterMap, Object parameterObject);

}
