package com.ibatis.sqlmap.engine.config;

import com.ibatis.common.beans.*;
import com.ibatis.common.resources.*;
import com.ibatis.sqlmap.client.*;
import com.ibatis.sqlmap.engine.cache.*;
import com.ibatis.sqlmap.engine.impl.*;
import com.ibatis.sqlmap.engine.mapping.parameter.*;
import com.ibatis.sqlmap.engine.mapping.result.*;
import com.ibatis.sqlmap.engine.mapping.sql.*;
import com.ibatis.sqlmap.engine.mapping.sql.dynamic.*;
import com.ibatis.sqlmap.engine.mapping.sql.simple.*;
import com.ibatis.sqlmap.engine.mapping.sql.stat.*;
import com.ibatis.sqlmap.engine.mapping.statement.*;
import com.ibatis.sqlmap.engine.scope.*;
import com.ibatis.sqlmap.engine.type.*;

import java.sql.ResultSet;
import java.util.*;

public class MappedStatementConfig {
    private static my.Debug DEBUG = new my.Debug(my.Debug.SqlMapConfigParser);//我加上的
    private static final Probe PROBE = ProbeFactory.getProbe();
    private static final InlineParameterMapParser PARAM_PARSER = new InlineParameterMapParser();
    private ErrorContext errorContext;
    private SqlMapClientImpl client;
    private TypeHandlerFactory typeHandlerFactory;
    private MappedStatement mappedStatement;
    private MappedStatement rootStatement;

    //defaultStatementTimeout是指<settings>元素的defaultStatementTimeout属性值
    MappedStatementConfig(SqlMapConfiguration config, String id, MappedStatement statement, SqlSource processor,
            String parameterMapName, Class parameterClass, String resultMapName, String[] additionalResultMapNames,
            Class resultClass, Class[] additionalResultClasses, String cacheModelName, String resultSetType, Integer fetchSize,
            boolean allowRemapping, Integer timeout, Integer defaultStatementTimeout, String xmlResultName) {
        try {//我加上的
            DEBUG.P(this, "MappedStatementConfig(...)");
            DEBUG.P("id=" + id);

            this.errorContext = config.getErrorContext();
            this.client = config.getClient();
            SqlMapExecutorDelegate delegate = client.getDelegate();
            this.typeHandlerFactory = config.getTypeHandlerFactory();
            errorContext.setActivity("parsing a mapped statement");
            errorContext.setObjectId(id + " statement");
            errorContext.setMoreInfo("Check the result map name.");
            if (resultMapName != null) {
                statement.setResultMap(client.getDelegate().getResultMap(resultMapName));
                if (additionalResultMapNames != null) {
                    for (int i = 0; i < additionalResultMapNames.length; i++) {
                        statement.addResultMap(client.getDelegate().getResultMap(additionalResultMapNames[i]));
                    }
                }
            }
            errorContext.setMoreInfo("Check the parameter map name.");
            if (parameterMapName != null) {
                statement.setParameterMap(client.getDelegate().getParameterMap(parameterMapName));
            }
            statement.setId(id);
            statement.setResource(errorContext.getResource());
            if (resultSetType != null) {
                if ("FORWARD_ONLY".equals(resultSetType)) {
                    statement.setResultSetType(new Integer(ResultSet.TYPE_FORWARD_ONLY));
                } else if ("SCROLL_INSENSITIVE".equals(resultSetType)) {
                    statement.setResultSetType(new Integer(ResultSet.TYPE_SCROLL_INSENSITIVE));
                } else if ("SCROLL_SENSITIVE".equals(resultSetType)) {
                    statement.setResultSetType(new Integer(ResultSet.TYPE_SCROLL_SENSITIVE));
                }
            }
            if (fetchSize != null) {
                statement.setFetchSize(fetchSize);
            }

            // set parameter class either from attribute or from map (make sure to match)
            ParameterMap parameterMap = statement.getParameterMap();
            if (parameterMap == null) {
                statement.setParameterClass(parameterClass);
            } else {
                statement.setParameterClass(parameterMap.getParameterClass());
            }

            // process SQL statement, including inline parameter maps
            errorContext.setMoreInfo("Check the SQL statement.");
            Sql sql = processor.getSql();
            setSqlForStatement(statement, sql);

            // set up either null result map or automatic result mapping
            ResultMap resultMap = (ResultMap) statement.getResultMap();
            if (resultMap == null && resultClass == null) {
                statement.setResultMap(null);
            } else if (resultMap == null) {
                resultMap = buildAutoResultMap(allowRemapping, statement, resultClass, xmlResultName);
                statement.setResultMap(resultMap);
                if (additionalResultClasses != null) {
                    for (int i = 0; i < additionalResultClasses.length; i++) {
                        statement.addResultMap(buildAutoResultMap(allowRemapping, statement, additionalResultClasses[i],
                                xmlResultName));
                    }
                }

            }
            statement.setTimeout(defaultStatementTimeout);
            if (timeout != null) {
                try {
                    statement.setTimeout(timeout);
                } catch (NumberFormatException e) {
                    throw new SqlMapException("Specified timeout value for statement " + statement.getId()
                            + " is not a valid integer");
                }
            }
            errorContext.setMoreInfo(null);
            errorContext.setObjectId(null);
            statement.setSqlMapClient(client);
            if (cacheModelName != null && cacheModelName.length() > 0 && client.getDelegate().isCacheModelsEnabled()) {
                CacheModel cacheModel = client.getDelegate().getCacheModel(cacheModelName);
                mappedStatement = new CachingStatement(statement, cacheModel);
            } else {
                mappedStatement = statement;
            }
            rootStatement = statement;
            delegate.addMappedStatement(mappedStatement);

        } finally {//我加上的
            DEBUG.P(0, this, "MappedStatementConfig(...)");
        }
    }

    public void setSelectKeyStatement(SqlSource processor, String resultClassName, String keyPropName, boolean runAfterSQL,
            String type) {
        try {//我加上的
            DEBUG.P(this, "setSelectKeyStatement(...)");
            DEBUG.P("resultClassName=" + resultClassName);
            DEBUG.P("keyPropName=" + keyPropName);
            DEBUG.P("runAfterSQL=" + runAfterSQL);
            DEBUG.P("type=" + type);

            if (rootStatement instanceof InsertStatement) {
                InsertStatement insertStatement = ((InsertStatement) rootStatement);
                Class parameterClass = insertStatement.getParameterClass();
                errorContext.setActivity("parsing a select key");
                SelectKeyStatement selectKeyStatement = new SelectKeyStatement();
                resultClassName = typeHandlerFactory.resolveAlias(resultClassName);
                Class resultClass = null;

                DEBUG.P("parameterClass=" + parameterClass);
                DEBUG.P("resultClassName=" + resultClassName);

                // get parameter and result maps
                selectKeyStatement.setSqlMapClient(client);
                selectKeyStatement.setId(insertStatement.getId() + "-SelectKey");
                selectKeyStatement.setResource(errorContext.getResource());
                selectKeyStatement.setKeyProperty(keyPropName);
                selectKeyStatement.setRunAfterSQL(runAfterSQL);
                // process the type (pre or post) attribute
                if (type != null) {
                    selectKeyStatement.setRunAfterSQL("post".equals(type));
                }
                try {
                    if (resultClassName != null) {
                        errorContext.setMoreInfo("Check the select key result class.");
                        resultClass = Resources.classForName(resultClassName);
                    } else {
                        if (keyPropName != null && parameterClass != null) {
                            resultClass = PROBE.getPropertyTypeForSetter(parameterClass, selectKeyStatement.getKeyProperty());
                        }
                    }
                } catch (ClassNotFoundException e) {
                    throw new SqlMapException("Error.  Could not set result class.  Cause: " + e, e);
                }
                if (resultClass == null) {
                    resultClass = Object.class;
                }

                // process SQL statement, including inline parameter maps
                errorContext.setMoreInfo("Check the select key SQL statement.");
                Sql sql = processor.getSql();
                setSqlForStatement(selectKeyStatement, sql);
                ResultMap resultMap;
                resultMap = new AutoResultMap(client.getDelegate(), false);
                resultMap.setId(selectKeyStatement.getId() + "-AutoResultMap");
                resultMap.setResultClass(resultClass);
                resultMap.setResource(selectKeyStatement.getResource());
                selectKeyStatement.setResultMap(resultMap);
                errorContext.setMoreInfo(null);
                insertStatement.setSelectKeyStatement(selectKeyStatement);
            } else {
                throw new SqlMapException("You cant set a select key statement on statement named " + rootStatement.getId()
                        + " because it is not an InsertStatement.");
            }

        } finally {//我加上的
            DEBUG.P(0, this, "setSelectKeyStatement(...)");
        }
    }

    private void setSqlForStatement(MappedStatement statement, Sql sql) {
        try {//我加上的
            DEBUG.P(this, "setSqlForStatement(...)");
            DEBUG.P("(sql instanceof DynamicSql)=" + (sql instanceof DynamicSql));

            if (sql instanceof DynamicSql) {
                statement.setSql(sql);
            } else {
                applyInlineParameterMap(statement, sql.getSql(null, null));
            }

        } finally {//我加上的
            DEBUG.P(0, this, "setSqlForStatement(...)");
        }
    }

    private void applyInlineParameterMap(MappedStatement statement, String sqlStatement) {
        try {//我加上的
            DEBUG.P(this, "applyInlineParameterMap(...)");
            DEBUG.P("sqlStatement=" + sqlStatement);

            String newSql = sqlStatement;
            errorContext.setActivity("building an inline parameter map");
            ParameterMap parameterMap = statement.getParameterMap();
            errorContext.setMoreInfo("Check the inline parameters.");

            DEBUG.P("parameterMap=" + parameterMap);
            if (parameterMap == null) {
                ParameterMap map;
                map = new ParameterMap(client.getDelegate());
                map.setId(statement.getId() + "-InlineParameterMap");
                map.setParameterClass(statement.getParameterClass());
                map.setResource(statement.getResource());
                statement.setParameterMap(map);
                SqlText sqlText = PARAM_PARSER.parseInlineParameterMap(client.getDelegate().getTypeHandlerFactory(), newSql,
                        statement.getParameterClass());
                newSql = sqlText.getText();
                List mappingList = Arrays.asList(sqlText.getParameterMappings());
                map.setParameterMappingList(mappingList);

                DEBUG.P("newSql=" + newSql);
                DEBUG.P("mappingList=" + mappingList);
            }
            Sql sql;
            if (SimpleDynamicSql.isSimpleDynamicSql(newSql)) {
                //sql串中有"$"
                sql = new SimpleDynamicSql(client.getDelegate(), newSql);
            } else {
                sql = new StaticSql(newSql);
            }

            DEBUG.P("sql=" + sql);
            statement.setSql(sql);

        } finally {//我加上的
            DEBUG.P(0, this, "applyInlineParameterMap(...)");
        }

    }

    private ResultMap buildAutoResultMap(boolean allowRemapping, MappedStatement statement, Class firstResultClass,
            String xmlResultName) {
        try {//我加上的
            DEBUG.P(this, "buildAutoResultMap(...)");
            DEBUG.P("allowRemapping=" + allowRemapping);
            DEBUG.P("firstResultClass=" + firstResultClass);
            DEBUG.P("xmlResultName=" + xmlResultName);

            ResultMap resultMap;
            resultMap = new AutoResultMap(client.getDelegate(), allowRemapping);
            resultMap.setId(statement.getId() + "-AutoResultMap");
            resultMap.setResultClass(firstResultClass);
            resultMap.setXmlName(xmlResultName);
            resultMap.setResource(statement.getResource());
            return resultMap;

        } finally {//我加上的
            DEBUG.P(0, this, "buildAutoResultMap(...)");
        }
    }

    public MappedStatement getMappedStatement() {
        return mappedStatement;
    }
}
