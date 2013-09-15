package com.ibatis.sqlmap.engine.config;

import com.ibatis.sqlmap.client.extensions.*;
import com.ibatis.sqlmap.engine.impl.*;
import com.ibatis.sqlmap.engine.mapping.parameter.*;
import com.ibatis.sqlmap.engine.scope.*;
import com.ibatis.sqlmap.engine.type.*;

import java.util.*;

public class ParameterMapConfig {
    public static final String MODE_IN = "IN";
    public static final String MODE_OUT = "OUT";
    public static final String MODE_INOUT = "INUOT";

    private SqlMapConfiguration config;
    private ErrorContext errorContext;
    private SqlMapClientImpl client;
    private ParameterMap parameterMap;
    private List parameterMappingList;

    ParameterMapConfig(SqlMapConfiguration config, String id, Class parameterClass) {
        this.config = config;
        this.errorContext = config.getErrorContext();
        this.client = config.getClient();
        errorContext.setActivity("building a parameter map");
        parameterMap = new ParameterMap(client.getDelegate());
        parameterMap.setId(id);
        parameterMap.setResource(errorContext.getResource());
        errorContext.setObjectId(id + " parameter map");
        parameterMap.setParameterClass(parameterClass);
        errorContext.setMoreInfo("Check the parameter mappings.");
        this.parameterMappingList = new ArrayList();
        client.getDelegate().addParameterMap(parameterMap);
    }

    public void addParameterMapping(String propertyName, Class javaClass, String jdbcType, String nullValue, String mode,
            String outParamType, Integer numericScale, Object typeHandlerImpl, String resultMap) {
        errorContext.setObjectId(propertyName + " mapping of the " + parameterMap.getId() + " parameter map");
        TypeHandler handler;
        if (typeHandlerImpl != null) {
            errorContext.setMoreInfo("Check the parameter mapping typeHandler attribute '" + typeHandlerImpl
                    + "' (must be a TypeHandler or TypeHandlerCallback implementation).");
            if (typeHandlerImpl instanceof TypeHandlerCallback) {
                handler = new CustomTypeHandler((TypeHandlerCallback) typeHandlerImpl);
            } else if (typeHandlerImpl instanceof TypeHandler) {
                handler = (TypeHandler) typeHandlerImpl;
            } else {
                throw new RuntimeException("The class '" + typeHandlerImpl
                        + "' is not a valid implementation of TypeHandler or TypeHandlerCallback");
            }
        } else {
            errorContext.setMoreInfo("Check the parameter mapping property type or name.");
            handler = config.resolveTypeHandler(client.getDelegate().getTypeHandlerFactory(), parameterMap.getParameterClass(),
                    propertyName, javaClass, jdbcType);
        }
        ParameterMapping mapping = new ParameterMapping();
        mapping.setPropertyName(propertyName);
        mapping.setJdbcTypeName(jdbcType);
        mapping.setTypeName(outParamType);
        mapping.setResultMapName(resultMap);
        mapping.setNullValue(nullValue);
        if (mode != null && mode.length() > 0) {
            mapping.setMode(mode);
        }
        mapping.setTypeHandler(handler);
        mapping.setJavaType(javaClass);
        mapping.setNumericScale(numericScale);
        parameterMappingList.add(mapping);
        parameterMap.setParameterMappingList(parameterMappingList);
    }

}
