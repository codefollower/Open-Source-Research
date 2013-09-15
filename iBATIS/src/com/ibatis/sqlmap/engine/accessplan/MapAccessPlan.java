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
package com.ibatis.sqlmap.engine.accessplan;

import java.util.Map;

/**
 * Access plan for working with Maps
 */
public class MapAccessPlan extends BaseAccessPlan {

    MapAccessPlan(Class clazz, String[] propertyNames) {
        super(clazz, propertyNames);
    }

    public void setProperties(Object object, Object[] values) {
        Map map = (Map) object;
        for (int i = 0; i < propertyNames.length; i++) {
            map.put(propertyNames[i], values[i]);
        }
    }

    public Object[] getProperties(Object object) {
        Object[] values = new Object[propertyNames.length];
        Map map = (Map) object;
        for (int i = 0; i < propertyNames.length; i++) {
            values[i] = map.get(propertyNames[i]);
        }
        return values;
    }

}
