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

import com.ibatis.common.beans.ClassInfo;
import com.ibatis.common.beans.Invoker;

/**
 * Base implementation of the AccessPlan interface
 */
public abstract class BaseAccessPlan implements AccessPlan {

    protected Class clazz;
    protected String[] propertyNames;
    protected ClassInfo info;

    BaseAccessPlan(Class clazz, String[] propertyNames) {
        this.clazz = clazz;
        this.propertyNames = propertyNames;
        info = ClassInfo.getInstance(clazz);
    }

    protected Class[] getTypes(String[] propertyNames) {
        Class[] types = new Class[propertyNames.length];
        for (int i = 0; i < propertyNames.length; i++) {
            types[i] = info.getGetterType(propertyNames[i]);
        }
        return types;
    }

    protected Invoker[] getGetters(String[] propertyNames) {
        Invoker[] methods = new Invoker[propertyNames.length];
        for (int i = 0; i < propertyNames.length; i++) {
            methods[i] = info.getGetInvoker(propertyNames[i]);
        }
        return methods;
    }

    protected Invoker[] getSetters(String[] propertyNames) {
        Invoker[] methods = new Invoker[propertyNames.length];
        for (int i = 0; i < propertyNames.length; i++) {
            methods[i] = info.getSetInvoker(propertyNames[i]);
        }
        return methods;
    }

    protected String[] getGetterNames(String[] propertyNames) {
        String[] names = new String[propertyNames.length];
        for (int i = 0; i < propertyNames.length; i++) {
            names[i] = info.getGetter(propertyNames[i]).getName();
        }
        return names;
    }

    protected String[] getSetterNames(String[] propertyNames) {
        String[] names = new String[propertyNames.length];
        for (int i = 0; i < propertyNames.length; i++) {
            names[i] = info.getSetter(propertyNames[i]).getName();
        }
        return names;
    }

}
