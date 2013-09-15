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
package com.ibatis.sqlmap.engine.mapping.sql.dynamic;

import com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate;
import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMap;
import com.ibatis.sqlmap.engine.mapping.parameter.InlineParameterMapParser;
import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMapping;
import com.ibatis.sqlmap.engine.mapping.result.ResultMap;
import com.ibatis.sqlmap.engine.mapping.sql.Sql;
import com.ibatis.sqlmap.engine.mapping.sql.SqlChild;
import com.ibatis.sqlmap.engine.mapping.sql.SqlText;
import com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements.*;
import com.ibatis.sqlmap.engine.mapping.sql.simple.SimpleDynamicSql;
import com.ibatis.sqlmap.engine.mapping.statement.MappedStatement;
import com.ibatis.sqlmap.engine.scope.StatementScope;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DynamicSql implements Sql, DynamicParent {

    private static final InlineParameterMapParser PARAM_PARSER = new InlineParameterMapParser();

    private List children = new ArrayList();
    private SqlMapExecutorDelegate delegate;

    public DynamicSql(SqlMapExecutorDelegate delegate) {
        this.delegate = delegate;
    }

    public String getSql(StatementScope statementScope, Object parameterObject) {
        String sql = statementScope.getDynamicSql();
        if (sql == null) {
            process(statementScope, parameterObject);
            sql = statementScope.getDynamicSql();
        }
        return sql;
    }

    public ParameterMap getParameterMap(StatementScope statementScope, Object parameterObject) {
        ParameterMap map = statementScope.getDynamicParameterMap();
        if (map == null) {
            process(statementScope, parameterObject);
            map = statementScope.getDynamicParameterMap();
        }
        return map;
    }

    public ResultMap getResultMap(StatementScope statementScope, Object parameterObject) {
        return statementScope.getResultMap();
    }

    public void cleanup(StatementScope statementScope) {
        statementScope.setDynamicSql(null);
        statementScope.setDynamicParameterMap(null);
    }

    private void process(StatementScope statementScope, Object parameterObject) {
        SqlTagContext ctx = new SqlTagContext();
        List localChildren = children;
        processBodyChildren(statementScope, ctx, parameterObject, localChildren.iterator());

        ParameterMap map = new ParameterMap(delegate);
        map.setId(statementScope.getStatement().getId() + "-InlineParameterMap");
        map.setParameterClass(((MappedStatement) statementScope.getStatement()).getParameterClass());
        map.setParameterMappingList(ctx.getParameterMappings());

        String dynSql = ctx.getBodyText();

        // Processes $substitutions$ after DynamicSql
        if (SimpleDynamicSql.isSimpleDynamicSql(dynSql)) {
            dynSql = new SimpleDynamicSql(delegate, dynSql).getSql(statementScope, parameterObject);
        }

        statementScope.setDynamicSql(dynSql);
        statementScope.setDynamicParameterMap(map);
    }

    private void processBodyChildren(StatementScope statementScope, SqlTagContext ctx, Object parameterObject,
            Iterator localChildren) {
        PrintWriter out = ctx.getWriter();
        processBodyChildren(statementScope, ctx, parameterObject, localChildren, out);
    }

    private void processBodyChildren(StatementScope statementScope, SqlTagContext ctx, Object parameterObject,
            Iterator localChildren, PrintWriter out) {
        while (localChildren.hasNext()) {
            SqlChild child = (SqlChild) localChildren.next();
            if (child instanceof SqlText) {
                SqlText sqlText = (SqlText) child;
                String sqlStatement = sqlText.getText();
                if (sqlText.isWhiteSpace()) {
                    out.print(sqlStatement);
                } else if (!sqlText.isPostParseRequired()) {

                    // BODY OUT
                    out.print(sqlStatement);

                    ParameterMapping[] mappings = sqlText.getParameterMappings();
                    if (mappings != null) {
                        for (int i = 0, n = mappings.length; i < n; i++) {
                            ctx.addParameterMapping(mappings[i]);
                        }
                    }
                } else {

                    IterateContext itCtx = ctx.peekIterateContext();

                    if (null != itCtx && itCtx.isAllowNext()) {
                        itCtx.next();
                        itCtx.setAllowNext(false);
                        if (!itCtx.hasNext()) {
                            itCtx.setFinal(true);
                        }
                    }

                    if (itCtx != null) {
                        StringBuffer sqlStatementBuffer = new StringBuffer(sqlStatement);
                        iteratePropertyReplace(sqlStatementBuffer, itCtx);
                        sqlStatement = sqlStatementBuffer.toString();
                    }

                    sqlText = PARAM_PARSER.parseInlineParameterMap(delegate.getTypeHandlerFactory(), sqlStatement);

                    ParameterMapping[] mappings = sqlText.getParameterMappings();
                    out.print(sqlText.getText());
                    if (mappings != null) {
                        for (int i = 0, n = mappings.length; i < n; i++) {
                            ctx.addParameterMapping(mappings[i]);
                        }
                    }
                }
            } else if (child instanceof SqlTag) {
                SqlTag tag = (SqlTag) child;
                SqlTagHandler handler = tag.getHandler();
                int response = SqlTagHandler.INCLUDE_BODY;
                do {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);

                    response = handler.doStartFragment(ctx, tag, parameterObject);
                    if (response != SqlTagHandler.SKIP_BODY) {

                        processBodyChildren(statementScope, ctx, parameterObject, tag.getChildren(), pw);
                        pw.flush();
                        pw.close();
                        StringBuffer body = sw.getBuffer();
                        response = handler.doEndFragment(ctx, tag, parameterObject, body);
                        handler.doPrepend(ctx, tag, parameterObject, body);

                        if (response != SqlTagHandler.SKIP_BODY) {
                            if (body.length() > 0) {
                                out.print(body.toString());
                            }
                        }

                    }
                } while (response == SqlTagHandler.REPEAT_BODY);

                ctx.popRemoveFirstPrependMarker(tag);

                if (ctx.peekIterateContext() != null && ctx.peekIterateContext().getTag() == tag) {
                    ctx.setAttribute(ctx.peekIterateContext().getTag(), null);
                    ctx.popIterateContext();
                }

            }
        }
    }

    /**
     * 
     * @param bodyContent
     * @param iterate
     */
    protected void iteratePropertyReplace(StringBuffer bodyContent, IterateContext iterate) {
        if (iterate != null) {
            String[] mappings = new String[] { "#", "$" };
            for (int i = 0; i < mappings.length; i++) {
                int startIndex = 0;
                int endIndex = -1;
                while (startIndex > -1 && startIndex < bodyContent.length()) {
                    startIndex = bodyContent.indexOf(mappings[i], endIndex + 1);
                    endIndex = bodyContent.indexOf(mappings[i], startIndex + 1);
                    if (startIndex > -1 && endIndex > -1) {
                        bodyContent.replace(startIndex + 1, endIndex,
                                iterate.addIndexToTagProperty(bodyContent.substring(startIndex + 1, endIndex)));
                    }
                }
            }
        }
    }

    protected static void replace(StringBuffer buffer, String find, String replace) {
        int pos = buffer.toString().indexOf(find);
        int len = find.length();
        while (pos > -1) {
            buffer.replace(pos, pos + len, replace);
            pos = buffer.toString().indexOf(find);
        }
    }

    public void addChild(SqlChild child) {
        children.add(child);
    }

}
