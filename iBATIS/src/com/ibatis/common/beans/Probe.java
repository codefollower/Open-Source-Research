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

/**
 * A Probe is an object that is used to work with beans, DOM objects, or other 
 * objects.
 */
public interface Probe {

    /**
     * Gets an Object property from another object
     *
     * @param object - the object
     * @param name   - the property name
     * @return The property value (as an Object)
     */
    public Object getObject(Object object, String name);

    /**
     * Sets the value of a property on an object
     *
     * @param object - the object to change
     * @param name   - the name of the property to set
     * @param value  - the new value to set
     */
    public void setObject(Object object, String name, Object value);

    /**
     * Returns the class that the setter expects when setting a property
     *
     * @param object - the object to check
     * @param name   - the name of the property
     * @return The type of the property
     */
    public Class getPropertyTypeForSetter(Object object, String name);

    /**
     * Returns the class that the getter will return when reading a property
     *
     * @param object - the object to check
     * @param name   - the name of the property
     * @return The type of the property
     */
    public Class getPropertyTypeForGetter(Object object, String name);

    /**
     * Checks to see if an object has a writable property by a given name
     *
     * @param object       - the object to check
     * @param propertyName - the property to check for
     * @return True if the property exists and is writable
     */
    public boolean hasWritableProperty(Object object, String propertyName);

    /**
     * Checks to see if an object has a readable property by a given name
     *
     * @param object       - the object to check
     * @param propertyName - the property to check for
     * @return True if the property exists and is readable
     */
    public boolean hasReadableProperty(Object object, String propertyName);

}
