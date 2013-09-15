package com.ibatis.sqlmap.engine.builder.xml;

import com.ibatis.common.xml.*;
import com.ibatis.common.resources.*;
import com.ibatis.sqlmap.engine.config.*;
import com.ibatis.sqlmap.engine.mapping.statement.*;
import com.ibatis.sqlmap.client.*;
import org.w3c.dom.CharacterData;
import org.w3c.dom.*;

import java.util.Properties;

public class SqlStatementParser {
    private static my.Debug DEBUG = new my.Debug(my.Debug.SqlMapConfigParser);//我加上的

    private XmlParserState state;

    public SqlStatementParser(XmlParserState config) {
        this.state = config;
    }

    public void parseGeneralStatement(Node node, MappedStatement statement) {

        DEBUG.P(this, "parseGeneralStatement(2)");
        DEBUG.P("node=" + node);
        DEBUG.P("statement=" + statement);
        DEBUG.P(1);

        // get attributes
        Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());

        //这些属性是statement* | insert* | update* | delete* | select* | procedure*元素的
        //属性的并集，并不是每个元素都有这么多属性
        String id = attributes.getProperty("id");
        String parameterMapName = state.applyNamespace(attributes.getProperty("parameterMap"));
        String parameterClassName = attributes.getProperty("parameterClass");
        String resultMapName = attributes.getProperty("resultMap");
        String resultClassName = attributes.getProperty("resultClass");
        String cacheModelName = state.applyNamespace(attributes.getProperty("cacheModel"));
        String xmlResultName = attributes.getProperty("xmlResultName");
        String resultSetType = attributes.getProperty("resultSetType");
        String fetchSize = attributes.getProperty("fetchSize");
        String allowRemapping = attributes.getProperty("remapResults");
        String timeout = attributes.getProperty("timeout");

        DEBUG.P("id=" + id);
        DEBUG.P("parameterMapName=" + parameterMapName);
        DEBUG.P("parameterClassName=" + parameterClassName);
        DEBUG.P("resultMapName=" + resultMapName);
        DEBUG.P("resultClassName=" + resultClassName);
        DEBUG.P("cacheModelName=" + cacheModelName);
        DEBUG.P("xmlResultName=" + xmlResultName);
        DEBUG.P("resultSetType=" + resultSetType);
        DEBUG.P("fetchSize=" + fetchSize);
        DEBUG.P("allowRemapping=" + allowRemapping);
        DEBUG.P("timeout=" + timeout);
        DEBUG.P(1);

        if (state.isUseStatementNamespaces()) {
            id = state.applyNamespace(id);
        }

        DEBUG.P("id=" + id);

        //多个用逗号分隔的resultMap,如resultMap="AccountResult,AccountResult2"
        String[] additionalResultMapNames = null;
        if (resultMapName != null) {
            additionalResultMapNames = state.getAllButFirstToken(resultMapName);
            resultMapName = state.getFirstToken(resultMapName);
            resultMapName = state.applyNamespace(resultMapName);
            for (int i = 0; i < additionalResultMapNames.length; i++) {
                additionalResultMapNames[i] = state.applyNamespace(additionalResultMapNames[i]);
            }
        }

        DEBUG.P("resultMapName=" + resultMapName);
        DEBUG.PA("additionalResultMapNames", additionalResultMapNames);

        String[] additionalResultClassNames = null;
        if (resultClassName != null) {
            additionalResultClassNames = state.getAllButFirstToken(resultClassName);
            resultClassName = state.getFirstToken(resultClassName);
        }

        DEBUG.P("resultClassName=" + resultClassName);
        DEBUG.PA("additionalResultClassNames", additionalResultClassNames);

        Class[] additionalResultClasses = null;
        if (additionalResultClassNames != null) {
            additionalResultClasses = new Class[additionalResultClassNames.length];
            for (int i = 0; i < additionalResultClassNames.length; i++) {
                additionalResultClasses[i] = resolveClass(additionalResultClassNames[i]);
            }
        }

        DEBUG.PA("additionalResultClasses", additionalResultClasses);

        DEBUG.P(1);

        state.getConfig().getErrorContext().setMoreInfo("Check the parameter class.");
        Class parameterClass = resolveClass(parameterClassName);

        DEBUG.P("parameterClass=" + parameterClass);

        state.getConfig().getErrorContext().setMoreInfo("Check the result class.");
        Class resultClass = resolveClass(resultClassName);

        DEBUG.P("resultClass=" + resultClass);

        Integer timeoutInt = timeout == null ? null : new Integer(timeout);
        Integer fetchSizeInt = fetchSize == null ? null : new Integer(fetchSize);
        boolean allowRemappingBool = "true".equals(allowRemapping);

        DEBUG.P("timeoutInt=" + timeoutInt);
        DEBUG.P("fetchSizeInt=" + fetchSizeInt);
        DEBUG.P("allowRemappingBool=" + allowRemappingBool);

        MappedStatementConfig statementConf = state.getConfig().newMappedStatementConfig(id, statement,
                new XMLSqlSource(state, node), parameterMapName, parameterClass, resultMapName, additionalResultMapNames,
                resultClass, additionalResultClasses, resultSetType, fetchSizeInt, allowRemappingBool, timeoutInt,
                cacheModelName, xmlResultName);

        findAndParseSelectKey(node, statementConf);

        DEBUG.P(0, this, "parseGeneralStatement(2)");
    }

    private Class resolveClass(String resultClassName) {
        try {
            if (resultClassName != null) {
                return Resources.classForName(state.getConfig().getTypeHandlerFactory().resolveAlias(resultClassName));
            } else {
                return null;
            }
        } catch (ClassNotFoundException e) {
            throw new SqlMapException("Error.  Could not initialize class.  Cause: " + e, e);
        }
    }

    //就是插入一条记录后，取出自动生成的ID值
    //对应<insert><selectKey>元素，并且使用AutoResultMap
    private void findAndParseSelectKey(Node node, MappedStatementConfig config) {
        try {//我加上的
            DEBUG.P(this, "findAndParseSelectKey(...)");

            state.getConfig().getErrorContext().setActivity("parsing select key tags");
            boolean foundSQLFirst = false;
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);

                DEBUG.P("child.getNodeName()=" + child.getNodeName());

                if (child.getNodeType() == Node.CDATA_SECTION_NODE || child.getNodeType() == Node.TEXT_NODE) {
                    String data = ((CharacterData) child).getData();
                    if (data.trim().length() > 0) {
                        foundSQLFirst = true;
                    }
                } else if (child.getNodeType() == Node.ELEMENT_NODE && "selectKey".equals(child.getNodeName())) {
                    Properties attributes = NodeletUtils.parseAttributes(child, state.getGlobalProps());
                    String keyPropName = attributes.getProperty("keyProperty");
                    String resultClassName = attributes.getProperty("resultClass");
                    String type = attributes.getProperty("type");

                    DEBUG.P("keyPropName=" + keyPropName);
                    DEBUG.P("resultClassName=" + resultClassName);
                    DEBUG.P("type=" + type);

                    config.setSelectKeyStatement(new XMLSqlSource(state, child), resultClassName, keyPropName, foundSQLFirst,
                            type);
                    break;
                }
            }
            state.getConfig().getErrorContext().setMoreInfo(null);

        } finally {//我加上的
            DEBUG.P(0, this, "findAndParseSelectKey(...)");
        }

    }

}
