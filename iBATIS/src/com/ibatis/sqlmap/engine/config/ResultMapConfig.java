package com.ibatis.sqlmap.engine.config;

import com.ibatis.sqlmap.client.extensions.*;
import com.ibatis.sqlmap.engine.impl.*;
import com.ibatis.sqlmap.engine.mapping.result.*;
import com.ibatis.sqlmap.engine.scope.*;
import com.ibatis.sqlmap.engine.type.*;

import java.util.*;

//代表一整个<resultMap>的配置
//具体信息在用ResultMap resultMap存放
public class ResultMapConfig {
    private static my.Debug DEBUG = new my.Debug(my.Debug.SqlMapConfigParser);//我加上的

    private SqlMapConfiguration config;
    private ErrorContext errorContext;
    private SqlMapClientImpl client;
    private SqlMapExecutorDelegate delegate;
    private TypeHandlerFactory typeHandlerFactory;
    private ResultMap resultMap;
    private List resultMappingList;
    private int resultMappingIndex;
    private Discriminator discriminator;

    ResultMapConfig(SqlMapConfiguration config, String id, Class resultClass, String groupBy, String extendsResultMap,
            String xmlName) {
        DEBUG.P(this, "ResultMapConfig(...)");
        DEBUG.P("id=" + id);
        DEBUG.P("resultClass=" + resultClass);
        DEBUG.P("groupBy=" + groupBy);
        DEBUG.P("extendsResultMap=" + extendsResultMap);
        DEBUG.P("xmlName=" + xmlName);

        this.config = config;
        this.errorContext = config.getErrorContext();
        this.client = config.getClient();
        this.delegate = config.getDelegate();
        this.typeHandlerFactory = config.getTypeHandlerFactory();
        this.resultMap = new ResultMap(client.getDelegate());
        this.resultMappingList = new ArrayList();
        errorContext.setActivity("building a result map");
        errorContext.setObjectId(id + " result map");
        resultMap.setId(id);
        resultMap.setXmlName(xmlName);

        DEBUG.P("errorContext.getResource()=" + errorContext.getResource());

        resultMap.setResource(errorContext.getResource());
        if (groupBy != null && groupBy.length() > 0) {
            StringTokenizer parser = new StringTokenizer(groupBy, ", ", false);
            while (parser.hasMoreTokens()) {
                resultMap.addGroupByProperty(parser.nextToken());
            }
        }
        resultMap.setResultClass(resultClass);
        errorContext.setMoreInfo("Check the extended result map.");
        if (extendsResultMap != null) {
            ResultMap extendedResultMap = (ResultMap) client.getDelegate().getResultMap(extendsResultMap);
            ResultMapping[] resultMappings = extendedResultMap.getResultMappings();
            for (int i = 0; i < resultMappings.length; i++) {
                resultMappingList.add(resultMappings[i]);
            }
            List nestedResultMappings = extendedResultMap.getNestedResultMappings();
            if (nestedResultMappings != null) {
                Iterator iter = nestedResultMappings.iterator();
                while (iter.hasNext()) {
                    resultMap.addNestedResultMappings((ResultMapping) iter.next());
                }
            }
            if (groupBy == null || groupBy.length() == 0) {
                if (extendedResultMap.hasGroupBy()) {
                    Iterator i = extendedResultMap.groupByProps();
                    while (i.hasNext()) {
                        resultMap.addGroupByProperty((String) i.next());
                    }
                }
            }
        }
        errorContext.setMoreInfo("Check the result mappings.");
        resultMappingIndex = resultMappingList.size();

        DEBUG.P("resultMappingIndex=" + resultMappingIndex);
        resultMap.setResultMappingList(resultMappingList);
        client.getDelegate().addResultMap(resultMap);

        DEBUG.P(0, this, "ResultMapConfig(...)");
    }

    public void setDiscriminator(String columnName, Integer columnIndex, Class javaClass, String jdbcType, String nullValue,
            Object typeHandlerImpl) {
        TypeHandler handler;
        if (typeHandlerImpl != null) {
            if (typeHandlerImpl instanceof TypeHandlerCallback) {
                handler = new CustomTypeHandler((TypeHandlerCallback) typeHandlerImpl);
            } else if (typeHandlerImpl instanceof TypeHandler) {
                handler = (TypeHandler) typeHandlerImpl;
            } else {
                throw new RuntimeException("The class '' is not a valid implementation of TypeHandler or TypeHandlerCallback");
            }
        } else {
            handler = config.resolveTypeHandler(client.getDelegate().getTypeHandlerFactory(), resultMap.getResultClass(), "",
                    javaClass, jdbcType, true);
        }
        ResultMapping mapping = new ResultMapping();
        mapping.setColumnName(columnName);
        mapping.setJdbcTypeName(jdbcType);
        mapping.setTypeHandler(handler);
        mapping.setNullValue(nullValue);
        mapping.setJavaType(javaClass);
        if (columnIndex != null) {
            mapping.setColumnIndex(columnIndex.intValue());
        }
        discriminator = new Discriminator(delegate, mapping);
        resultMap.setDiscriminator(discriminator);
    }

    public void addDiscriminatorSubMap(Object value, String resultMap) {
        if (discriminator == null) {
            throw new RuntimeException("The discriminator is null, but somehow a subMap was reached.  This is a bug.");
        }
        discriminator.addSubMap(value.toString(), resultMap);
    }

    public void addResultMapping(String propertyName, String columnName, Integer columnIndex, Class javaClass, String jdbcType,
            String nullValue, String notNullColumn, String statementName, String resultMapName, Object impl) {
        errorContext.setObjectId(propertyName + " mapping of the " + resultMap.getId() + " result map");
        TypeHandler handler;
        if (impl != null) {
            if (impl instanceof TypeHandlerCallback) {
                handler = new CustomTypeHandler((TypeHandlerCallback) impl);
            } else if (impl instanceof TypeHandler) {
                handler = (TypeHandler) impl;
            } else {
                throw new RuntimeException("The class '" + impl
                        + "' is not a valid implementation of TypeHandler or TypeHandlerCallback");
            }
        } else {
            handler = config.resolveTypeHandler(client.getDelegate().getTypeHandlerFactory(), resultMap.getResultClass(),
                    propertyName, javaClass, jdbcType, true);
        }
        ResultMapping mapping = new ResultMapping();
        mapping.setPropertyName(propertyName);
        mapping.setColumnName(columnName);
        mapping.setJdbcTypeName(jdbcType);
        mapping.setTypeHandler(handler);
        mapping.setNullValue(nullValue);
        mapping.setNotNullColumn(notNullColumn);
        mapping.setStatementName(statementName);
        mapping.setNestedResultMapName(resultMapName);
        if (resultMapName != null && resultMapName.length() > 0) {
            resultMap.addNestedResultMappings(mapping);
        }
        mapping.setJavaType(javaClass);
        if (columnIndex != null) {
            mapping.setColumnIndex(columnIndex.intValue());
        } else {
            resultMappingIndex++;
            mapping.setColumnIndex(resultMappingIndex);
        }
        resultMappingList.add(mapping);
        resultMap.setResultMappingList(resultMappingList);
    }

}
