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
 * An abstract factory for getting Probe implementations.
 */
public class ProbeFactory {

    private static final Probe DOM = new DomProbe();
    private static final Probe BEAN = new ComplexBeanProbe();
    private static final Probe GENERIC = new GenericProbe();

    /**
     * Factory method for getting a Probe object
     *
     * @return An implementation of the Probe interface
     */
    public static Probe getProbe() {
        return GENERIC;
    }

    /**
     * Factory method for getting a Probe object that is
     * the best choice for the type of object supplied
     * by the object parameter.
     *
     * @param object The object to get a Probe for
     * @return An implementation of the Probe interface
     */
    public static Probe getProbe(Object object) {
        if (object instanceof org.w3c.dom.Document) {
            return DOM;
        } else {
            return BEAN;
        }
    }

}
