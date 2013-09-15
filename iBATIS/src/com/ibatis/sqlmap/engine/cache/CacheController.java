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
package com.ibatis.sqlmap.engine.cache;

import java.util.Properties;

/**
 * Cache controller (implementation) interface
 */
public interface CacheController {

    /**
     * Flush a cache model
     *
     * @param cacheModel - the model to flush
     */
    public void flush(CacheModel cacheModel);

    /**
     * Get an object from a cache model
     *
     * @param cacheModel - the model
     * @param key        - the key to the object
     * @return the object if in the cache, or null(?)
     */
    public Object getObject(CacheModel cacheModel, Object key);

    /**
     * Remove an object from a cache model
     *
     * @param cacheModel - the model to remove the object from
     * @param key        - the key to the object
     * @return the removed object(?)
     */
    public Object removeObject(CacheModel cacheModel, Object key);

    /**
     * Put an object into a cache model
     *
     * @param cacheModel - the model to add the object to
     * @param key        - the key to the object
     * @param object     - the object to add
     */
    public void putObject(CacheModel cacheModel, Object key, Object object);

    /**
     * Configure a cache controller
     *
     * @param props - the properties object continaing configuration information
     */
    public void setProperties(Properties props);

}