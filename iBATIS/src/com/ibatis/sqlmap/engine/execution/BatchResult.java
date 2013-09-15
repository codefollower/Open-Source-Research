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
package com.ibatis.sqlmap.engine.execution;

import java.io.Serializable;

/**
 * This class holds the statement and row information for every successful batch
 * executed by iBATIS
 * 
 * @author Jeff Butler
 * 
 */
public class BatchResult implements Serializable {
    private String sql;
    private String statementId;
    private int[] updateCounts;

    /**
    * 
    */
    public BatchResult(String statementId, String sql) {
        super();
        this.statementId = statementId;
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    public int[] getUpdateCounts() {
        return updateCounts;
    }

    public void setUpdateCounts(int[] updateCounts) {
        this.updateCounts = updateCounts;
    }

    public String getStatementId() {
        return statementId;
    }
}
