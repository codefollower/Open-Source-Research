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

import net.sf.cglib.beans.BulkBean;

/**
 * Enhanced PropertyAccessPlan (for working with beans using CG Lib)
 */
public class EnhancedPropertyAccessPlan extends BaseAccessPlan {

    private BulkBean bulkBean;

    EnhancedPropertyAccessPlan(Class clazz, String[] propertyNames) {
        super(clazz, propertyNames);
        bulkBean = BulkBean.create(clazz, getGetterNames(propertyNames), getSetterNames(propertyNames), getTypes(propertyNames));
    }

    public void setProperties(Object object, Object[] values) {
        bulkBean.setPropertyValues(object, values);
    }

    public Object[] getProperties(Object object) {
        return bulkBean.getPropertyValues(object);
    }

}
