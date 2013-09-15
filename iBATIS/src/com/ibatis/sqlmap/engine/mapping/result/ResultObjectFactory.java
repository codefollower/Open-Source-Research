/*
 *  Copyright 2006 The Apache Software Foundation
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
package com.ibatis.sqlmap.engine.mapping.result;

/**
 * iBATIS uses an implementation of this interface to create
 * result objects after the execution of a statement.  To use, specify
 * your implementation class as the type for the "resultObjectFactory"
 * element in the SqlMapConfig.  Any implementation of this interface
 * must have a public no argument constructor.
 * 
 * Note that iBATIS makes use of this interface through the
 * ResultObjectFactoryUtil class.
 *   
 * @author Jeff Butler
 *
 */
public interface ResultObjectFactory {

    /**
     * Returns a new instance of the requested class.
     * iBATIS will call this method in these circumstances:
     * 
     * <ul>
     *   <li>When processing a result set - to create new instances of result objects</li>
     *   <li>When processing the output parameters of a stored procedure - to create
     *       instances of OUTPUT parameters
     *   </li>
     *   <li>When processing a nested select - to create instances of parameter
     *       objects on the nested select
     *   </li>
     *   <li>When processing result maps with nested result maps.  iBATIS will
     *     ask the factory to create an instance of the nested object.  If the nested
     *     object is some implementation of <code>java.util.Collection</code>
     *     then iBATIS will supply default implementations of the common interfaces
     *     if the factory chooses not to create the object.  If the
     *     embedded object is a <code>java.util.List</code> or 
     *     <code>java.util.Collection</code> the default behavior is to
     *     create an <code>java.util.ArrayList</code>.  If the embedded object is a
     *     <code>java.util.Set</code> the default behavior
     *     is to create a <code>java.util.HashSet</code>.</li>
     * </ul>
     *
     * If you return <code>null</code> from this method, iBATIS will attempt
     * to create in instance of the class with it's normal mechanism.  This means
     * that you can selectively choose which objects to create with this interface.
     * In the event that you choose not to create an object, iBATIS will translate some
     * common interfaces to their common implementations.  If the requested
     * class is List or Collection iBATIS will create an ArrayList.  If the requested
     * class is Set then iBATIS will create a HashSet.  But these rules only apply
     * if you choose not to create the object.  So you can use this factory to
     * supply custom implementations of those interfaces if you so desire.
     * 
     * @param statementId the ID of the statement that generated the call to this method
     * @param clazz the type of object to create
     * @return a new instance of the specified class.  The instance must
     *   be castable to the specified class.  If you return <code>null</code>,
     *   iBATIS will attempt to create the instance using it's normal 
     *   mechanism.
     * @throws InstantiationException if the instance cannot be created.  If you
     *   throw this Exception, iBATIS will throw a runtime exception in response and
     *   will end.
     * @throws IllegalAccessException if the constructor cannot be accessed.  If you
     *   throw this Exception, iBATIS will throw a runtime exception in response and
     *   will end.
     */
    Object createInstance(String statementId, Class clazz) throws InstantiationException, IllegalAccessException;

    /**
     * Called for each property configured in the SqlMapCong file.  All the properties
     * will be set before any call is made to createInstance
     * 
     * @param name
     * @param value
     */
    void setProperty(String name, String value);
}
