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
package com.ibatis.common.jdbc.exception;

import java.sql.SQLException;

/**
 * Unchecked exception to allow passing an Exception with the original SQLException 
 */
public class RuntimeSQLException extends RuntimeException {

    /**
     * Default constructor
     */
    public RuntimeSQLException() {
    }

    /**
     * Constructor to pass along a message
     * @param msg - the message
     */
    public RuntimeSQLException(String msg) {
        super(msg);
    }

    /**
     * Constructor to pass along another exception
     * @param sqlException - the exception
     */
    public RuntimeSQLException(SQLException sqlException) {
        super(sqlException);
    }

    /**
     * Constructor to pass along a message and an exception
     * @param msg - the message
     * @param sqlException - the exception
     */
    public RuntimeSQLException(String msg, SQLException sqlException) {
        super(msg, sqlException);
    }

    /**
     * Getter for the SQL State
     * @return - the state
     */
    public String getSQLState() {
        Throwable cause = getCause();
        if (cause instanceof SQLException) {
            return ((SQLException) cause).getSQLState();
        } else {
            return null;
        }

    }

    /**
     * Getter for the error code
     * @return - the error code
     */
    public int getErrorCode() {
        Throwable cause = getCause();
        if (cause instanceof SQLException) {
            return ((SQLException) cause).getErrorCode();
        } else {
            return -1;
        }
    }

    /**
     * Get the next exception in the chain
     * @return - the next exception
     */
    public SQLException getNextException() {
        Throwable cause = getCause();
        if (cause instanceof SQLException) {
            return ((SQLException) cause).getNextException();
        } else {
            return null;
        }
    }

    /**
     * Set the next exception in the chain
     * @param ex - the next exception
     */
    public synchronized void setNextException(SQLException ex) {
        Throwable cause = getCause();
        if (cause instanceof SQLException) {
            ((SQLException) cause).setNextException(ex);
        }
    }

}
