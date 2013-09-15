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
 * BeansException for use for by BeanProbe and StaticBeanProbe.
 */
public class ProbeException extends RuntimeException {

    /**
     * Default constructor
     */
    public ProbeException() {
    }

    /**
     * Constructor to set the message for the exception
     *
     * @param msg - the message for the exception
     */
    public ProbeException(String msg) {
        super(msg);
    }

    /**
     * Constructor to create a nested exception
     *
     * @param cause - the reason the exception is being thrown
     */
    public ProbeException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor to create a nested exception with a message
     *
     * @param msg   - the message for the exception
     * @param cause - the reason the exception is being thrown
     */
    public ProbeException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
