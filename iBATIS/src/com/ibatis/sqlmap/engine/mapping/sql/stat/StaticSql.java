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
package com.ibatis.sqlmap.engine.mapping.sql.stat;

import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMap;
import com.ibatis.sqlmap.engine.mapping.result.ResultMap;
import com.ibatis.sqlmap.engine.mapping.sql.Sql;
import com.ibatis.sqlmap.engine.scope.StatementScope;

public class StaticSql implements Sql {

    private String sqlStatement;

    public StaticSql(String sqlStatement) {
        this.sqlStatement = sqlStatement.replace('\r', ' ').replace('\n', ' ');
    }

    public String getSql(StatementScope statementScope, Object parameterObject) {
        return sqlStatement;
    }

    public ParameterMap getParameterMap(StatementScope statementScope, Object parameterObject) {
        return statementScope.getParameterMap();
    }

    public ResultMap getResultMap(StatementScope statementScope, Object parameterObject) {
        return statementScope.getResultMap();
    }

    public void cleanup(StatementScope statementScope) {
    }

}
