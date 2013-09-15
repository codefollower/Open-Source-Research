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

import com.ibatis.sqlmap.client.event.RowHandler;
import com.ibatis.sqlmap.engine.scope.StatementScope;
import com.ibatis.sqlmap.engine.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

public class InsertStatement extends MappedStatement {

    private SelectKeyStatement selectKeyStatement;

    public StatementType getStatementType() {
        return StatementType.INSERT;
    }

    public Object executeQueryForObject(StatementScope statementScope, Transaction trans, Object parameterObject,
            Object resultObject) throws SQLException {
        throw new SQLException("Insert statements cannot be executed as a query.");
    }

    public List executeQueryForList(StatementScope statementScope, Transaction trans, Object parameterObject, int skipResults,
            int maxResults) throws SQLException {
        throw new SQLException("Insert statements cannot be executed as a query.");
    }

    public void executeQueryWithRowHandler(StatementScope statementScope, Transaction trans, Object parameterObject,
            RowHandler rowHandler) throws SQLException {
        throw new SQLException("Update statements cannot be executed as a query.");
    }

    public SelectKeyStatement getSelectKeyStatement() {
        return selectKeyStatement;
    }

    public void setSelectKeyStatement(SelectKeyStatement selectKeyStatement) {
        this.selectKeyStatement = selectKeyStatement;
    }
}
