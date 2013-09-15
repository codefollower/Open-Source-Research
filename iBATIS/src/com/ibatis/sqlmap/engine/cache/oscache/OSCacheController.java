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
package com.ibatis.sqlmap.engine.cache.oscache;

import com.ibatis.sqlmap.engine.cache.CacheController;
import com.ibatis.sqlmap.engine.cache.CacheModel;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

import java.util.Properties;

/**
 * Cache implementation for using OSCache with iBATIS
 */
public class OSCacheController implements CacheController {

    private static final GeneralCacheAdministrator CACHE = new GeneralCacheAdministrator();

    public void flush(CacheModel cacheModel) {
        CACHE.flushGroup(cacheModel.getId());
    }

    public Object getObject(CacheModel cacheModel, Object key) {
        String keyString = key.toString();
        try {
            int refreshPeriod = (int) (cacheModel.getFlushIntervalSeconds());
            return CACHE.getFromCache(keyString, refreshPeriod);
        } catch (NeedsRefreshException e) {
            CACHE.cancelUpdate(keyString);
            return null;
        }
    }

    public Object removeObject(CacheModel cacheModel, Object key) {
        Object result;
        String keyString = key.toString();
        try {
            int refreshPeriod = (int) (cacheModel.getFlushIntervalSeconds());
            Object value = CACHE.getFromCache(keyString, refreshPeriod);
            if (value != null) {
                CACHE.flushEntry(keyString);
            }
            result = value;
        } catch (NeedsRefreshException e) {
            try {
                CACHE.flushEntry(keyString);
            } finally {
                CACHE.cancelUpdate(keyString);
                result = null;
            }
        }
        return result;
    }

    public void putObject(CacheModel cacheModel, Object key, Object object) {
        String keyString = key.toString();
        CACHE.putInCache(keyString, object, new String[] { cacheModel.getId() });
    }

    public void setProperties(Properties props) {
    }

}
