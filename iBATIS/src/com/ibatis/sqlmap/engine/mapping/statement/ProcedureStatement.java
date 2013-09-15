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
package com.ibatis.sqlmap.engine.mapping.statement;

import com.ibatis.sqlmap.engine.scope.StatementScope;

import java.sql.Connection;
import java.sql.SQLException;

public class ProcedureStatement extends MappedStatement {

    protected void postProcessParameterObject(StatementScope statementScope, Object parameterObject, Object[] parameters) {
        statementScope.getParameterMap().refreshParameterObjectValues(statementScope, parameterObject, parameters);
    }

    protected int sqlExecuteUpdate(StatementScope statementScope, Connection conn, String sqlString, Object[] parameters)
            throws SQLException {
        if (statementScope.getSession().isInBatch()) {
            getSqlExecutor().addBatch(statementScope, conn, sqlString, parameters);
            return 0;
        } else {
            return getSqlExecutor().executeUpdateProcedure(statementScope, conn, sqlString.trim(), parameters);
        }
    }

    protected void sqlExecuteQuery(StatementScope statementScope, Connection conn, String sqlString, Object[] parameters,
            int skipResults, int maxResults, RowHandlerCallback callback) throws SQLException {
        getSqlExecutor().executeQueryProcedure(statementScope, conn, sqlString.trim(), parameters, skipResults, maxResults,
                callback);
    }

    public StatementType getStatementType() {
        return StatementType.PROCEDURE;
    }
}
