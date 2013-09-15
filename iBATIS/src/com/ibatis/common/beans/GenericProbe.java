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
package com.ibatis.common.beans;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * StaticBeanProbe provides methods that allow simple, reflective access to
 * JavaBeans style properties.  Methods are provided for all simple types as
 * well as object types.
 * <p/>
 * Examples:
 * <p/>
 * StaticBeanProbe.setObject(object, propertyName, value);
 * <P>
 * Object value = StaticBeanProbe.getObject(object, propertyName);
 */
public class GenericProbe extends BaseProbe {

    private static final BaseProbe BEAN_PROBE = new ComplexBeanProbe();
    private static final BaseProbe DOM_PROBE = new DomProbe();

    protected GenericProbe() {
    }

    /**
     * Gets an object from a Map or bean
     *
     * @param object - the object to probe
     * @param name   - the name of the property (or map entry)
     * @return The value of the property (or map entry)
     * @see com.ibatis.common.beans.BaseProbe#getObject(java.lang.Object, java.lang.String)
     */
    public Object getObject(Object object, String name) {

        if (object instanceof org.w3c.dom.Document) {
            return DOM_PROBE.getObject(object, name);
        } else if (object instanceof List) {
            return BEAN_PROBE.getIndexedProperty(object, name);
        } else if (object instanceof Object[]) {
            return BEAN_PROBE.getIndexedProperty(object, name);
        } else if (object instanceof char[]) {
            return BEAN_PROBE.getIndexedProperty(object, name);
        } else if (object instanceof boolean[]) {
            return BEAN_PROBE.getIndexedProperty(object, name);
        } else if (object instanceof byte[]) {
            return BEAN_PROBE.getIndexedProperty(object, name);
        } else if (object instanceof double[]) {
            return BEAN_PROBE.getIndexedProperty(object, name);
        } else if (object instanceof float[]) {
            return BEAN_PROBE.getIndexedProperty(object, name);
        } else if (object instanceof int[]) {
            return BEAN_PROBE.getIndexedProperty(object, name);
        } else if (object instanceof long[]) {
            return BEAN_PROBE.getIndexedProperty(object, name);
        } else if (object instanceof short[]) {
            return BEAN_PROBE.getIndexedProperty(object, name);
        } else {
            return BEAN_PROBE.getObject(object, name);
        }
    }

    /**
     * Sets an object in a Map or bean
     *
     * @param object - the object to probe
     * @param name   - the name of the property (or map entry)
     * @param value  - the new value of the property (or map entry)
     * @see com.ibatis.common.beans.BaseProbe#setObject(java.lang.Object, java.lang.String, java.lang.Object)
     */
    public void setObject(Object object, String name, Object value) {
        if (object instanceof org.w3c.dom.Document) {
            DOM_PROBE.setObject(object, name, value);
        } else {
            BEAN_PROBE.setObject(object, name, value);
        }
    }

    /**
     * Gets an array of the readable properties in a Map or JavaBean
     *
     * @param object - the object to get properties for
     * @return The array of properties (or map entries)
     * @see com.ibatis.common.beans.BaseProbe#getReadablePropertyNames(java.lang.Object)
     */
    public String[] getReadablePropertyNames(Object object) {
        if (object instanceof org.w3c.dom.Document) {
            return DOM_PROBE.getReadablePropertyNames(object);
        } else {
            return BEAN_PROBE.getReadablePropertyNames(object);
        }
    }

    /**
     * Gets an array of the writeable properties in a Map or JavaBean
     *
     * @param object - the object to get properties for
     * @return The array of properties (or map entries)
     * @see com.ibatis.common.beans.BaseProbe#getWriteablePropertyNames(java.lang.Object)
     */
    public String[] getWriteablePropertyNames(Object object) {
        if (object instanceof org.w3c.dom.Document) {
            return DOM_PROBE.getWriteablePropertyNames(object);
        } else {
            return BEAN_PROBE.getWriteablePropertyNames(object);
        }
    }

    /**
     * Returns the class that the setter expects to receive as a parameter when
     * setting a property value.
     *
     * @param object - The class to check
     * @param name   - the name of the property
     * @return The type of the property
     * @see com.ibatis.common.beans.Probe#getPropertyTypeForSetter(java.lang.Object, java.lang.String)
     */
    public Class getPropertyTypeForSetter(Object object, String name) {
        if (object instanceof Class) {
            return getClassPropertyTypeForSetter((Class) object, name);
        } else if (object instanceof org.w3c.dom.Document) {
            return DOM_PROBE.getPropertyTypeForSetter(object, name);
        } else {
            return BEAN_PROBE.getPropertyTypeForSetter(object, name);
        }
    }

    /**
     * Returns the class that the getter will return when reading a property value.
     *
     * @param object The bean to check
     * @param name   The name of the property
     * @return The type of the property
     * @see com.ibatis.common.beans.Probe#getPropertyTypeForGetter(java.lang.Object, java.lang.String)
     */
    public Class getPropertyTypeForGetter(Object object, String name) {
        if (object instanceof Class) {
            return getClassPropertyTypeForGetter((Class) object, name);
        } else if (object instanceof org.w3c.dom.Document) {
            return DOM_PROBE.getPropertyTypeForGetter(object, name);
        } else if (name.indexOf('[') > -1) {
            return BEAN_PROBE.getIndexedType(object, name);
        } else {
            return BEAN_PROBE.getPropertyTypeForGetter(object, name);
        }
    }

    /**
     * Checks to see if an object has a writable property by a given name
     *
     * @param object       The bean to check
     * @param propertyName The property to check for
     * @return True if the property exists and is writable
     * @see com.ibatis.common.beans.Probe#hasWritableProperty(java.lang.Object, java.lang.String)
     */
    public boolean hasWritableProperty(Object object, String propertyName) {
        if (object instanceof org.w3c.dom.Document) {
            return DOM_PROBE.hasWritableProperty(object, propertyName);
        } else {
            return BEAN_PROBE.hasWritableProperty(object, propertyName);
        }
    }

    /**
     * Checks to see if a bean has a readable property by a given name
     *
     * @param object       The bean to check
     * @param propertyName The property to check for
     * @return True if the property exists and is readable
     * @see com.ibatis.common.beans.Probe#hasReadableProperty(java.lang.Object, java.lang.String)
     */
    public boolean hasReadableProperty(Object object, String propertyName) {
        if (object instanceof org.w3c.dom.Document) {
            return DOM_PROBE.hasReadableProperty(object, propertyName);
        } else {
            return BEAN_PROBE.hasReadableProperty(object, propertyName);
        }
    }

    protected void setProperty(Object object, String property, Object value) {
        if (object instanceof org.w3c.dom.Document) {
            DOM_PROBE.setProperty(object, property, value);
        } else {
            BEAN_PROBE.setProperty(object, property, value);
        }
    }

    protected Object getProperty(Object object, String property) {
        if (object instanceof org.w3c.dom.Document) {
            return DOM_PROBE.getProperty(object, property);
        } else {
            return BEAN_PROBE.getProperty(object, property);
        }
    }

    private Class getClassPropertyTypeForGetter(Class type, String name) {

        if (name.indexOf('.') > -1) {
            StringTokenizer parser = new StringTokenizer(name, ".");
            while (parser.hasMoreTokens()) {
                name = parser.nextToken();
                if (Map.class.isAssignableFrom(type)) {
                    type = Object.class;
                    break;
                }
                type = ClassInfo.getInstance(type).getGetterType(name);
            }
        } else {
            type = ClassInfo.getInstance(type).getGetterType(name);
        }

        return type;
    }

    /**
     * Returns the class that the setter expects to receive as a parameter when
     * setting a property value.
     *
     * @param type The class to check
     * @param name The name of the property
     * @return The type of the property
     */
    private Class getClassPropertyTypeForSetter(Class type, String name) {

        if (name.indexOf('.') > -1) {
            StringTokenizer parser = new StringTokenizer(name, ".");
            while (parser.hasMoreTokens()) {
                name = parser.nextToken();
                if (Map.class.isAssignableFrom(type)) {
                    type = Object.class;
                    break;
                }
                type = ClassInfo.getInstance(type).getSetterType(name);
            }
        } else {
            type = ClassInfo.getInstance(type).getSetterType(name);
        }

        return type;
    }

}
