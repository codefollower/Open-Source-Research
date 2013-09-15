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

import java.util.Map;
import java.util.StringTokenizer;

import com.ibatis.sqlmap.engine.mapping.result.ResultObjectFactoryUtil;

/**
 * StaticBeanProbe provides methods that allow simple, reflective access to
 * JavaBeans style properties.  Methods are provided for all simple types as
 * well as object types.
 * <p/>
 * Examples:
 * <p/>
 * StaticBeanProbe.setObject(object, propertyName, value);
 * <p/>
 * Object value = StaticBeanProbe.getObject(object, propertyName);
 */
public class ComplexBeanProbe extends BaseProbe {

    private static final Object[] NO_ARGUMENTS = new Object[0];

    protected ComplexBeanProbe() {
    }

    /**
     * Returns an array of the readable properties exposed by a bean
     *
     * @param object The bean
     * @return The properties
     */
    public String[] getReadablePropertyNames(Object object) {
        return ClassInfo.getInstance(object.getClass()).getReadablePropertyNames();
    }

    /**
     * Returns an array of the writeable properties exposed by a bean
     *
     * @param object The bean
     * @return The properties
     */
    public String[] getWriteablePropertyNames(Object object) {
        return ClassInfo.getInstance(object.getClass()).getWriteablePropertyNames();
    }

    /**
     * Returns the class that the setter expects to receive as a parameter when
     * setting a property value.
     *
     * @param object The bean to check
     * @param name   The name of the property
     * @return The type of the property
     */
    public Class getPropertyTypeForSetter(Object object, String name) {
        Class type = object.getClass();

        if (object instanceof Class) {
            type = getClassPropertyTypeForSetter((Class) object, name);
        } else if (object instanceof Map) {
            Map map = (Map) object;
            Object value = map.get(name);
            if (value == null) {
                type = Object.class;
            } else {
                type = value.getClass();
            }
        } else {
            if (name.indexOf('.') > -1) {
                StringTokenizer parser = new StringTokenizer(name, ".");
                while (parser.hasMoreTokens()) {
                    name = parser.nextToken();
                    type = ClassInfo.getInstance(type).getSetterType(name);
                }
            } else {
                type = ClassInfo.getInstance(type).getSetterType(name);
            }
        }

        return type;
    }

    /**
     * Returns the class that the getter will return when reading a property value.
     *
     * @param object The bean to check
     * @param name   The name of the property
     * @return The type of the property
     */
    public Class getPropertyTypeForGetter(Object object, String name) {
        Class type = object.getClass();

        if (object instanceof Class) {
            type = getClassPropertyTypeForGetter((Class) object, name);
        } else if (object instanceof Map) {
            Map map = (Map) object;
            Object value = map.get(name);
            if (value == null) {
                type = Object.class;
            } else {
                type = value.getClass();
            }
        } else {
            if (name.indexOf('.') > -1) {
                StringTokenizer parser = new StringTokenizer(name, ".");
                while (parser.hasMoreTokens()) {
                    name = parser.nextToken();
                    type = ClassInfo.getInstance(type).getGetterType(name);
                }
            } else {
                type = ClassInfo.getInstance(type).getGetterType(name);
            }
        }

        return type;
    }

    /**
     * Returns the class that the getter will return when reading a property value.
     *
     * @param type The class to check
     * @param name The name of the property
     * @return The type of the property
     */
    private Class getClassPropertyTypeForGetter(Class type, String name) {

        if (name.indexOf('.') > -1) {
            StringTokenizer parser = new StringTokenizer(name, ".");
            while (parser.hasMoreTokens()) {
                name = parser.nextToken();
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
                type = ClassInfo.getInstance(type).getSetterType(name);
            }
        } else {
            type = ClassInfo.getInstance(type).getSetterType(name);
        }

        return type;
    }

    /**
     * Gets an Object property from a bean
     *
     * @param object The bean
     * @param name   The property name
     * @return The property value (as an Object)
     */
    public Object getObject(Object object, String name) {
        if (name.indexOf('.') > -1) {
            StringTokenizer parser = new StringTokenizer(name, ".");
            Object value = object;
            while (parser.hasMoreTokens()) {
                value = getProperty(value, parser.nextToken());

                if (value == null) {
                    break;
                }

            }
            return value;
        } else {
            return getProperty(object, name);
        }
    }

    /**
     * Sets the value of a bean property to an Object
     *
     * @param object The bean to change
     * @param name   The name of the property to set
     * @param value  The new value to set
     */
    public void setObject(Object object, String name, Object value) {
        if (name.indexOf('.') > -1) {
            StringTokenizer parser = new StringTokenizer(name, ".");
            String property = parser.nextToken();
            Object child = object;
            while (parser.hasMoreTokens()) {
                Class type = getPropertyTypeForSetter(child, property);
                Object parent = child;
                child = getProperty(parent, property);
                if (child == null) {
                    if (value == null) {
                        return; // don't instantiate child path if value is null
                    } else {
                        try {
                            child = ResultObjectFactoryUtil.createObjectThroughFactory(type);
                            setObject(parent, property, child);
                        } catch (Exception e) {
                            throw new ProbeException("Cannot set value of property '" + name + "' because '" + property
                                    + "' is null and cannot be instantiated on instance of " + type.getName() + ". Cause:"
                                    + e.toString(), e);
                        }
                    }
                }
                property = parser.nextToken();
            }
            setProperty(child, property, value);
        } else {
            setProperty(object, name, value);
        }
    }

    /**
     * Checks to see if a bean has a writable property be a given name
     *
     * @param object       The bean to check
     * @param propertyName The property to check for
     * @return True if the property exists and is writable
     */
    public boolean hasWritableProperty(Object object, String propertyName) {
        boolean hasProperty = false;
        if (object instanceof Map) {
            hasProperty = true;//((Map) object).containsKey(propertyName);
        } else {
            if (propertyName.indexOf('.') > -1) {
                StringTokenizer parser = new StringTokenizer(propertyName, ".");
                Class type = object.getClass();
                while (parser.hasMoreTokens()) {
                    propertyName = parser.nextToken();
                    type = ClassInfo.getInstance(type).getGetterType(propertyName);
                    hasProperty = ClassInfo.getInstance(type).hasWritableProperty(propertyName);
                }
            } else {
                hasProperty = ClassInfo.getInstance(object.getClass()).hasWritableProperty(propertyName);
            }
        }
        return hasProperty;
    }

    /**
     * Checks to see if a bean has a readable property be a given name
     *
     * @param object       The bean to check
     * @param propertyName The property to check for
     * @return True if the property exists and is readable
     */
    public boolean hasReadableProperty(Object object, String propertyName) {
        boolean hasProperty = false;
        if (object instanceof Map) {
            hasProperty = true;//((Map) object).containsKey(propertyName);
        } else {
            if (propertyName.indexOf('.') > -1) {
                StringTokenizer parser = new StringTokenizer(propertyName, ".");
                Class type = object.getClass();
                while (parser.hasMoreTokens()) {
                    propertyName = parser.nextToken();
                    type = ClassInfo.getInstance(type).getGetterType(propertyName);
                    hasProperty = ClassInfo.getInstance(type).hasReadableProperty(propertyName);
                }
            } else {
                hasProperty = ClassInfo.getInstance(object.getClass()).hasReadableProperty(propertyName);
            }
        }
        return hasProperty;
    }

    protected Object getProperty(Object object, String name) {
        try {
            Object value = null;
            if (name.indexOf('[') > -1) {
                value = getIndexedProperty(object, name);
            } else {
                if (object instanceof Map) {
                    int index = name.indexOf('.');
                    if (index > -1) {
                        String mapId = name.substring(0, index);
                        value = getProperty(((Map) object).get(mapId), name.substring(index + 1));
                    } else {
                        value = ((Map) object).get(name);
                    }

                } else {
                    int index = name.indexOf('.');
                    if (index > -1) {
                        String newName = name.substring(0, index);
                        value = getProperty(getObject(object, newName), name.substring(index + 1));
                    } else {
                        ClassInfo classCache = ClassInfo.getInstance(object.getClass());
                        Invoker method = classCache.getGetInvoker(name);
                        if (method == null) {
                            throw new NoSuchMethodException("No GET method for property " + name + " on instance of "
                                    + object.getClass().getName());
                        }
                        try {
                            value = method.invoke(object, NO_ARGUMENTS);
                        } catch (Throwable t) {
                            throw ClassInfo.unwrapThrowable(t);
                        }
                    }

                }
            }
            return value;
        } catch (ProbeException e) {
            throw e;
        } catch (Throwable t) {
            if (object == null) {
                throw new ProbeException("Could not get property '" + name + "' from null reference.  Cause: " + t.toString(), t);
            } else {
                throw new ProbeException("Could not get property '" + name + "' from " + object.getClass().getName()
                        + ".  Cause: " + t.toString(), t);
            }
        }
    }

    protected void setProperty(Object object, String name, Object value) {
        ClassInfo classCache = ClassInfo.getInstance(object.getClass());
        try {
            if (name.indexOf('[') > -1) {
                setIndexedProperty(object, name, value);
            } else {
                if (object instanceof Map) {
                    ((Map) object).put(name, value);
                } else {
                    Invoker method = classCache.getSetInvoker(name);
                    if (method == null) {
                        throw new NoSuchMethodException("No SET method for property " + name + " on instance of "
                                + object.getClass().getName());
                    }
                    Object[] params = new Object[1];
                    params[0] = value;
                    try {
                        method.invoke(object, params);
                    } catch (Throwable t) {
                        throw ClassInfo.unwrapThrowable(t);
                    }
                }
            }
        } catch (ProbeException e) {
            throw e;
        } catch (Throwable t) {
            if (object == null) {
                throw new ProbeException("Could not set property '" + name + "' to value '" + value
                        + "' for null reference.  Cause: " + t.toString(), t);
            } else {
                throw new ProbeException("Could not set property '" + name + "' to value '" + value + "' for "
                        + object.getClass().getName() + ".  Cause: " + t.toString(), t);
            }
        }
    }

}
