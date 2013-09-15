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

/**
 * Abstract class used to help development of Probe implementations
 */
public abstract class BaseProbe implements Probe {

    protected abstract void setProperty(Object object, String property, Object value);

    protected abstract Object getProperty(Object object, String property);

    /**
     * Returns an array of the readable properties exposed by an object
     *
     * @param object - the object
     * @return The array of property names
     */
    public abstract String[] getReadablePropertyNames(Object object);

    /**
     * Returns an array of the writeable properties exposed by an object
     *
     * @param object - the object
     * @return The array of property names
     */
    public abstract String[] getWriteablePropertyNames(Object object);

    protected Object getIndexedProperty(Object object, String indexedName) {

        Object value = null;

        try {
            String name = indexedName.substring(0, indexedName.indexOf('['));
            int i = Integer.parseInt(indexedName.substring(indexedName.indexOf('[') + 1, indexedName.indexOf(']')));
            Object list = null;
            if ("".equals(name)) {
                list = object;
            } else {
                list = getProperty(object, name);
            }

            if (list instanceof List) {
                value = ((List) list).get(i);
            } else if (list instanceof Object[]) {
                value = ((Object[]) list)[i];
            } else if (list instanceof char[]) {
                value = new Character(((char[]) list)[i]);
            } else if (list instanceof boolean[]) {
                value = new Boolean(((boolean[]) list)[i]);
            } else if (list instanceof byte[]) {
                value = new Byte(((byte[]) list)[i]);
            } else if (list instanceof double[]) {
                value = new Double(((double[]) list)[i]);
            } else if (list instanceof float[]) {
                value = new Float(((float[]) list)[i]);
            } else if (list instanceof int[]) {
                value = new Integer(((int[]) list)[i]);
            } else if (list instanceof long[]) {
                value = new Long(((long[]) list)[i]);
            } else if (list instanceof short[]) {
                value = new Short(((short[]) list)[i]);
            } else {
                throw new ProbeException("The '" + name + "' property of the " + object.getClass().getName()
                        + " class is not a List or Array.");
            }

        } catch (ProbeException e) {
            throw e;
        } catch (Exception e) {
            throw new ProbeException("Error getting ordinal list from JavaBean. Cause " + e, e);
        }

        return value;
    }

    protected Class getIndexedType(Object object, String indexedName) {

        Class value = null;

        try {
            String name = indexedName.substring(0, indexedName.indexOf('['));
            int i = Integer.parseInt(indexedName.substring(indexedName.indexOf('[') + 1, indexedName.indexOf(']')));
            Object list = null;
            if (!"".equals(name)) {
                list = getProperty(object, name);
            } else {
                list = object;
            }

            if (list instanceof List) {
                value = ((List) list).get(i).getClass();
            } else if (list instanceof Object[]) {
                value = ((Object[]) list)[i].getClass();
            } else if (list instanceof char[]) {
                value = Character.class;
            } else if (list instanceof boolean[]) {
                value = Boolean.class;
            } else if (list instanceof byte[]) {
                value = Byte.class;
            } else if (list instanceof double[]) {
                value = Double.class;
            } else if (list instanceof float[]) {
                value = Float.class;
            } else if (list instanceof int[]) {
                value = Integer.class;
            } else if (list instanceof long[]) {
                value = Long.class;
            } else if (list instanceof short[]) {
                value = Short.class;
            } else {
                throw new ProbeException("The '" + name + "' property of the " + object.getClass().getName()
                        + " class is not a List or Array.");
            }

        } catch (ProbeException e) {
            throw e;
        } catch (Exception e) {
            throw new ProbeException("Error getting ordinal list from JavaBean. Cause " + e, e);
        }

        return value;
    }

    protected void setIndexedProperty(Object object, String indexedName, Object value) {

        try {
            String name = indexedName.substring(0, indexedName.indexOf('['));
            int i = Integer.parseInt(indexedName.substring(indexedName.indexOf('[') + 1, indexedName.indexOf(']')));
            Object list = getProperty(object, name);
            if (list instanceof List) {
                ((List) list).set(i, value);
            } else if (list instanceof Object[]) {
                ((Object[]) list)[i] = value;
            } else if (list instanceof char[]) {
                ((char[]) list)[i] = ((Character) value).charValue();
            } else if (list instanceof boolean[]) {
                ((boolean[]) list)[i] = ((Boolean) value).booleanValue();
            } else if (list instanceof byte[]) {
                ((byte[]) list)[i] = ((Byte) value).byteValue();
            } else if (list instanceof double[]) {
                ((double[]) list)[i] = ((Double) value).doubleValue();
            } else if (list instanceof float[]) {
                ((float[]) list)[i] = ((Float) value).floatValue();
            } else if (list instanceof int[]) {
                ((int[]) list)[i] = ((Integer) value).intValue();
            } else if (list instanceof long[]) {
                ((long[]) list)[i] = ((Long) value).longValue();
            } else if (list instanceof short[]) {
                ((short[]) list)[i] = ((Short) value).shortValue();
            } else {
                throw new ProbeException("The '" + name + "' property of the " + object.getClass().getName()
                        + " class is not a List or Array.");
            }
        } catch (ProbeException e) {
            throw e;
        } catch (Exception e) {
            throw new ProbeException("Error getting ordinal value from JavaBean. Cause " + e, e);
        }
    }
}
