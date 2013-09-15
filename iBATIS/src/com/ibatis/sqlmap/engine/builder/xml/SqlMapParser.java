package com.ibatis.sqlmap.engine.builder.xml;

import com.ibatis.common.xml.*;
import com.ibatis.common.resources.*;
import com.ibatis.sqlmap.client.*;
import com.ibatis.sqlmap.engine.config.*;
import com.ibatis.sqlmap.engine.mapping.statement.*;
import com.ibatis.sqlmap.engine.cache.*;
import org.w3c.dom.Node;

import java.io.*;
import java.util.Properties;

//每一个<sqlMapConfig><sqlMap>元素对应一个com.ibatis.sqlmap.engine.builder.xml.SqlMapParser类实例
//这些实例都是用同一个XmlParserState实例，
//因为是用单线程解析配置文件的，所以XmlParserState实例的字段状态可能会在解析第二个 sqlMap文件时替换。
public class SqlMapParser {
    private static my.Debug DEBUG = new my.Debug(my.Debug.SqlMapConfigParser);//我加上的

    private final NodeletParser parser;
    private XmlParserState state;
    private SqlStatementParser statementParser;

    public SqlMapParser(XmlParserState state) {
        DEBUG.P(this, "SqlMapParser(1)");

        this.parser = new NodeletParser();
        this.state = state;

        parser.setValidation(true);
        parser.setEntityResolver(new SqlMapClasspathEntityResolver());

        statementParser = new SqlStatementParser(this.state);

        addSqlMapNodelets();
        addSqlNodelets();
        addTypeAliasNodelets();
        addCacheModelNodelets();
        addParameterMapNodelets();
        addResultMapNodelets();
        addStatementNodelets();

        DEBUG.P(0, this, "SqlMapParser(1)");

    }

    public void parse(Reader reader) throws NodeletException {
        parser.parse(reader);
    }

    public void parse(InputStream inputStream) throws NodeletException {
        parser.parse(inputStream);
    }

    private void addSqlMapNodelets() {
        parser.addNodelet("/sqlMap", new Nodelet() {
            public void process(Node node) throws Exception {
                DEBUG.P(this, "addSqlMapNodelets->process(1)");

                Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
                state.setNamespace(attributes.getProperty("namespace"));

                DEBUG.P("applyNamespace=" + state.applyNamespace("myid"));
                DEBUG.P("xmlns:fo=" + attributes.getProperty("xmlns:fo"));

                DEBUG.P(0, this, "addSqlMapNodelets->process(1)");
            }
        });
    }

    private void addSqlNodelets() {
        parser.addNodelet("/sqlMap/sql", new Nodelet() {
            public void process(Node node) throws Exception {
                DEBUG.P(this, "addSqlNodelets->process(1)");

                Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
                String id = attributes.getProperty("id");

                DEBUG.P("id=" + id);

                if (state.isUseStatementNamespaces()) {
                    id = state.applyNamespace(id);
                }
                if (state.getSqlIncludes().containsKey(id)) {
                    throw new SqlMapException("Duplicate <sql>-include '" + id + "' found.");
                } else {
                    state.getSqlIncludes().put(id, node);
                }

                DEBUG.P(0, this, "addSqlNodelets->process(1)");
            }
        });
    }

    private void addTypeAliasNodelets() {
        parser.addNodelet("/sqlMap/typeAlias", new Nodelet() {
            public void process(Node node) throws Exception {
                DEBUG.P(this, "addTypeAliasNodelets->process(1)");

                Properties prop = NodeletUtils.parseAttributes(node, state.getGlobalProps());
                String alias = prop.getProperty("alias");
                String type = prop.getProperty("type");

                DEBUG.P("alias=" + alias);
                DEBUG.P("type=" + type);

                state.getConfig().getTypeHandlerFactory().putTypeAlias(alias, type);

                DEBUG.P(0, this, "addTypeAliasNodelets->process(1)");
            }
        });
    }

    private void addCacheModelNodelets() {
        parser.addNodelet("/sqlMap/cacheModel", new Nodelet() {
            public void process(Node node) throws Exception {
                DEBUG.P(this, "addCacheModelNodelets->process(1)");
                Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
                String id = state.applyNamespace(attributes.getProperty("id"));
                String type = attributes.getProperty("type");
                String readOnlyAttr = attributes.getProperty("readOnly");
                Boolean readOnly = readOnlyAttr == null || readOnlyAttr.length() <= 0 ? null : new Boolean("true"
                        .equals(readOnlyAttr));
                String serializeAttr = attributes.getProperty("serialize");
                Boolean serialize = serializeAttr == null || serializeAttr.length() <= 0 ? null : new Boolean("true"
                        .equals(serializeAttr));

                DEBUG.P("id=" + id);
                DEBUG.P("type=" + type);
                DEBUG.P("readOnly=" + readOnly);
                DEBUG.P("serialize=" + serialize);

                type = state.getConfig().getTypeHandlerFactory().resolveAlias(type);

                DEBUG.P("type=" + type);

                Class clazz = Resources.classForName(type);
                if (readOnly == null) {
                    readOnly = Boolean.TRUE;
                }
                if (serialize == null) {
                    serialize = Boolean.FALSE;
                }
                CacheModelConfig cacheConfig = state.getConfig().newCacheModelConfig(id,
                        (CacheController) Resources.instantiate(clazz), readOnly.booleanValue(), serialize.booleanValue());

                //如果有多个<cacheModel>元素，就会产生多个CacheModelConfig，上一个就被垃圾收集掉了
                //会调用setCacheConfig多次
                state.setCacheConfig(cacheConfig);

                DEBUG.P(0, this, "addCacheModelNodelets->process(1)");
            }
        });
        parser.addNodelet("/sqlMap/cacheModel/end()", new Nodelet() {
            public void process(Node node) throws Exception {
                DEBUG.P(this, "addCacheModelNodelets:/sqlMap/cacheModel/end()->process(1)");

                DEBUG.P("state.getCacheProps()=" + state.getCacheProps());

                state.getCacheConfig().setControllerProperties(state.getCacheProps());

                DEBUG.P(0, this, "addCacheModelNodelets:/sqlMap/cacheModel/end()->process(1)");
            }
        });
        parser.addNodelet("/sqlMap/cacheModel/property", new Nodelet() {
            public void process(Node node) throws Exception {
                DEBUG.P(this, "addCacheModelNodelets:property->process(1)");

                state.getConfig().getErrorContext().setMoreInfo("Check the cache model properties.");
                Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
                String name = attributes.getProperty("name");
                String value = NodeletUtils.parsePropertyTokens(attributes.getProperty("value"), state.getGlobalProps());

                DEBUG.P("name=" + name);
                DEBUG.P("value=" + value);
                state.getCacheProps().setProperty(name, value);

                DEBUG.P(0, this, "addCacheModelNodelets:property->process(1)");
            }
        });
        parser.addNodelet("/sqlMap/cacheModel/flushOnExecute", new Nodelet() {
            public void process(Node node) throws Exception {
                DEBUG.P(this, "addCacheModelNodelets:flushOnExecute->process(1)");

                Properties childAttributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
                String statement = childAttributes.getProperty("statement");

                DEBUG.P("statement=" + statement);

                state.getCacheConfig().addFlushTriggerStatement(statement);

                DEBUG.P(0, this, "addCacheModelNodelets:flushOnExecute->process(1)");
            }
        });
        parser.addNodelet("/sqlMap/cacheModel/flushInterval", new Nodelet() {
            public void process(Node node) throws Exception {
                DEBUG.P(this, "addCacheModelNodelets:flushInterval->process(1)");

                Properties childAttributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
                try {
                    int milliseconds = childAttributes.getProperty("milliseconds") == null ? 0 : Integer.parseInt(childAttributes
                            .getProperty("milliseconds"));
                    int seconds = childAttributes.getProperty("seconds") == null ? 0 : Integer.parseInt(childAttributes
                            .getProperty("seconds"));
                    int minutes = childAttributes.getProperty("minutes") == null ? 0 : Integer.parseInt(childAttributes
                            .getProperty("minutes"));
                    int hours = childAttributes.getProperty("hours") == null ? 0 : Integer.parseInt(childAttributes
                            .getProperty("hours"));

                    DEBUG.P("milliseconds=" + milliseconds);
                    DEBUG.P("seconds=" + seconds);
                    DEBUG.P("minutes=" + minutes);
                    DEBUG.P("hours=" + hours);

                    state.getCacheConfig().setFlushInterval(hours, minutes, seconds, milliseconds);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Error building cache in '" + "resourceNAME"
                            + "'.  Flush interval milliseconds must be a valid long integer value.  Cause: " + e, e);
                }

                DEBUG.P(0, this, "addCacheModelNodelets:flushInterval->process(1)");
            }
        });
    }

    private void addParameterMapNodelets() {
        parser.addNodelet("/sqlMap/parameterMap/end()", new Nodelet() {
            public void process(Node node) throws Exception {
                state.getConfig().getErrorContext().setMoreInfo(null);
                state.getConfig().getErrorContext().setObjectId(null);
                state.setParamConfig(null);
            }
        });
        parser.addNodelet("/sqlMap/parameterMap", new Nodelet() {
            public void process(Node node) throws Exception {
                Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
                String id = state.applyNamespace(attributes.getProperty("id"));
                String parameterClassName = attributes.getProperty("class");
                parameterClassName = state.getConfig().getTypeHandlerFactory().resolveAlias(parameterClassName);
                try {
                    state.getConfig().getErrorContext().setMoreInfo("Check the parameter class.");
                    ParameterMapConfig paramConf = state.getConfig().newParameterMapConfig(id,
                            Resources.classForName(parameterClassName));
                    state.setParamConfig(paramConf);
                } catch (Exception e) {
                    throw new SqlMapException("Error configuring ParameterMap.  Could not set ParameterClass.  Cause: " + e, e);
                }
            }
        });
        parser.addNodelet("/sqlMap/parameterMap/parameter", new Nodelet() {
            public void process(Node node) throws Exception {
                Properties childAttributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
                String propertyName = childAttributes.getProperty("property");
                String jdbcType = childAttributes.getProperty("jdbcType");
                String type = childAttributes.getProperty("typeName");
                String javaType = childAttributes.getProperty("javaType");
                String resultMap = state.applyNamespace(childAttributes.getProperty("resultMap"));
                String nullValue = childAttributes.getProperty("nullValue");
                String mode = childAttributes.getProperty("mode");
                String callback = childAttributes.getProperty("typeHandler");
                String numericScaleProp = childAttributes.getProperty("numericScale");

                callback = state.getConfig().getTypeHandlerFactory().resolveAlias(callback);
                Object typeHandlerImpl = null;
                if (callback != null) {
                    typeHandlerImpl = Resources.instantiate(callback);
                }

                javaType = state.getConfig().getTypeHandlerFactory().resolveAlias(javaType);
                Class javaClass = null;
                try {
                    if (javaType != null && javaType.length() > 0) {
                        javaClass = Resources.classForName(javaType);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Error setting javaType on parameter mapping.  Cause: " + e);
                }

                Integer numericScale = null;
                if (numericScaleProp != null) {
                    numericScale = new Integer(numericScaleProp);
                }

                state.getParamConfig().addParameterMapping(propertyName, javaClass, jdbcType, nullValue, mode, type,
                        numericScale, typeHandlerImpl, resultMap);
            }
        });
    }

    private void addResultMapNodelets() {
        parser.addNodelet("/sqlMap/resultMap/end()", new Nodelet() {
            public void process(Node node) throws Exception {
                state.getConfig().getErrorContext().setMoreInfo(null);
                state.getConfig().getErrorContext().setObjectId(null);
            }
        });
        parser.addNodelet("/sqlMap/resultMap", new Nodelet() {
            public void process(Node node) throws Exception {
                Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
                String id = state.applyNamespace(attributes.getProperty("id"));
                String resultClassName = attributes.getProperty("class");
                String extended = state.applyNamespace(attributes.getProperty("extends"));
                String xmlName = attributes.getProperty("xmlName");
                String groupBy = attributes.getProperty("groupBy");

                resultClassName = state.getConfig().getTypeHandlerFactory().resolveAlias(resultClassName);
                Class resultClass;
                try {
                    state.getConfig().getErrorContext().setMoreInfo("Check the result class.");
                    resultClass = Resources.classForName(resultClassName);
                } catch (Exception e) {
                    throw new RuntimeException("Error configuring Result.  Could not set ResultClass.  Cause: " + e, e);
                }
                ResultMapConfig resultConf = state.getConfig().newResultMapConfig(id, resultClass, groupBy, extended, xmlName);
                state.setResultConfig(resultConf);
            }
        });
        parser.addNodelet("/sqlMap/resultMap/result", new Nodelet() {
            public void process(Node node) throws Exception {
                Properties childAttributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
                String propertyName = childAttributes.getProperty("property");
                String nullValue = childAttributes.getProperty("nullValue");
                String jdbcType = childAttributes.getProperty("jdbcType");
                String javaType = childAttributes.getProperty("javaType");
                String columnName = childAttributes.getProperty("column");
                String columnIndexProp = childAttributes.getProperty("columnIndex");
                String statementName = childAttributes.getProperty("select");
                String resultMapName = childAttributes.getProperty("resultMap");
                String callback = childAttributes.getProperty("typeHandler");
                String notNullColumn = childAttributes.getProperty("notNullColumn");

                state.getConfig().getErrorContext().setMoreInfo("Check the result mapping property type or name.");
                Class javaClass = null;
                try {
                    javaType = state.getConfig().getTypeHandlerFactory().resolveAlias(javaType);
                    if (javaType != null && javaType.length() > 0) {
                        javaClass = Resources.classForName(javaType);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Error setting java type on result discriminator mapping.  Cause: " + e);
                }

                state.getConfig()
                        .getErrorContext()
                        .setMoreInfo(
                                "Check the result mapping typeHandler attribute '" + callback
                                        + "' (must be a TypeHandler or TypeHandlerCallback implementation).");
                Object typeHandlerImpl = null;
                try {
                    if (callback != null && callback.length() > 0) {
                        callback = state.getConfig().getTypeHandlerFactory().resolveAlias(callback);
                        typeHandlerImpl = Resources.instantiate(callback);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error occurred during custom type handler configuration.  Cause: " + e, e);
                }

                Integer columnIndex = null;
                if (columnIndexProp != null) {
                    try {
                        columnIndex = new Integer(columnIndexProp);
                    } catch (Exception e) {
                        throw new RuntimeException("Error parsing column index.  Cause: " + e, e);
                    }
                }

                state.getResultConfig().addResultMapping(propertyName, columnName, columnIndex, javaClass, jdbcType, nullValue,
                        notNullColumn, statementName, resultMapName, typeHandlerImpl);
            }
        });

        parser.addNodelet("/sqlMap/resultMap/discriminator/subMap", new Nodelet() {
            public void process(Node node) throws Exception {
                Properties childAttributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
                String value = childAttributes.getProperty("value");
                String resultMap = childAttributes.getProperty("resultMap");
                resultMap = state.applyNamespace(resultMap);
                state.getResultConfig().addDiscriminatorSubMap(value, resultMap);
            }
        });

        parser.addNodelet("/sqlMap/resultMap/discriminator", new Nodelet() {
            public void process(Node node) throws Exception {
                Properties childAttributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
                String nullValue = childAttributes.getProperty("nullValue");
                String jdbcType = childAttributes.getProperty("jdbcType");
                String javaType = childAttributes.getProperty("javaType");
                String columnName = childAttributes.getProperty("column");
                String columnIndexProp = childAttributes.getProperty("columnIndex");
                String callback = childAttributes.getProperty("typeHandler");

                state.getConfig().getErrorContext().setMoreInfo("Check the disriminator type or name.");
                Class javaClass = null;
                try {
                    javaType = state.getConfig().getTypeHandlerFactory().resolveAlias(javaType);
                    if (javaType != null && javaType.length() > 0) {
                        javaClass = Resources.classForName(javaType);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Error setting java type on result discriminator mapping.  Cause: " + e);
                }

                state.getConfig()
                        .getErrorContext()
                        .setMoreInfo(
                                "Check the result mapping discriminator typeHandler attribute '" + callback
                                        + "' (must be a TypeHandlerCallback implementation).");
                Object typeHandlerImpl = null;
                try {
                    if (callback != null && callback.length() > 0) {
                        callback = state.getConfig().getTypeHandlerFactory().resolveAlias(callback);
                        typeHandlerImpl = Resources.instantiate(callback);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error occurred during custom type handler configuration.  Cause: " + e, e);
                }

                Integer columnIndex = null;
                if (columnIndexProp != null) {
                    try {
                        columnIndex = new Integer(columnIndexProp);
                    } catch (Exception e) {
                        throw new RuntimeException("Error parsing column index.  Cause: " + e, e);
                    }
                }

                state.getResultConfig()
                        .setDiscriminator(columnName, columnIndex, javaClass, jdbcType, nullValue, typeHandlerImpl);
            }
        });
    }

    protected void addStatementNodelets() {
        parser.addNodelet("/sqlMap/statement", new Nodelet() {
            public void process(Node node) throws Exception {
                statementParser.parseGeneralStatement(node, new MappedStatement());
            }
        });
        parser.addNodelet("/sqlMap/insert", new Nodelet() {
            public void process(Node node) throws Exception {
                statementParser.parseGeneralStatement(node, new InsertStatement());
            }
        });
        parser.addNodelet("/sqlMap/update", new Nodelet() {
            public void process(Node node) throws Exception {
                statementParser.parseGeneralStatement(node, new UpdateStatement());
            }
        });
        parser.addNodelet("/sqlMap/delete", new Nodelet() {
            public void process(Node node) throws Exception {
                statementParser.parseGeneralStatement(node, new DeleteStatement());
            }
        });
        parser.addNodelet("/sqlMap/select", new Nodelet() {
            public void process(Node node) throws Exception {
                statementParser.parseGeneralStatement(node, new SelectStatement());
            }
        });
        parser.addNodelet("/sqlMap/procedure", new Nodelet() {
            public void process(Node node) throws Exception {
                statementParser.parseGeneralStatement(node, new ProcedureStatement());
            }
        });
    }

}
