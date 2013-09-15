package com.ibatis.sqlmap.engine.builder.xml;

import com.ibatis.common.resources.*;
import com.ibatis.sqlmap.engine.config.*;

import javax.sql.DataSource;
import java.util.*;

public class XmlParserState {
    private static my.Debug DEBUG = new my.Debug(my.Debug.SqlMapConfigParser);//我加上的

    private SqlMapConfiguration config = new SqlMapConfiguration();

    private Properties globalProps = new Properties();
    private Properties txProps = new Properties();
    private Properties dsProps = new Properties();
    private Properties cacheProps = new Properties();
    private boolean useStatementNamespaces = false;
    private Map sqlIncludes = new HashMap();

    private ParameterMapConfig paramConfig;
    private ResultMapConfig resultConfig;
    private CacheModelConfig cacheConfig;

    private String namespace;
    private DataSource dataSource;

    public SqlMapConfiguration getConfig() {
        return config;
    }

    public void setGlobalProps(Properties props) {
        globalProps = props;
    }

    public Properties getGlobalProps() {
        return globalProps;
    }

    public Properties getTxProps() {
        return txProps;
    }

    public Properties getDsProps() {
        return dsProps;
    }

    public Properties getCacheProps() {
        return cacheProps;
    }

    public void setUseStatementNamespaces(boolean useStatementNamespaces) {
        this.useStatementNamespaces = useStatementNamespaces;
    }

    public boolean isUseStatementNamespaces() {
        return useStatementNamespaces;
    }

    public Map getSqlIncludes() {
        return sqlIncludes;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String applyNamespace(String id) {
        String newId = id;
        if (namespace != null && namespace.length() > 0 && id != null && id.indexOf('.') < 0) {
            newId = namespace + "." + id;
        }
        return newId;
    }

    public CacheModelConfig getCacheConfig() {
        return cacheConfig;
    }

    public void setCacheConfig(CacheModelConfig cacheConfig) {
        DEBUG.P("this.cacheConfig1 = " + this.cacheConfig);
        this.cacheConfig = cacheConfig;

        DEBUG.P("this.cacheConfig2 = " + this.cacheConfig);
    }

    public ParameterMapConfig getParamConfig() {
        return paramConfig;
    }

    public void setParamConfig(ParameterMapConfig paramConfig) {
        this.paramConfig = paramConfig;
    }

    public ResultMapConfig getResultConfig() {
        return resultConfig;
    }

    public void setResultConfig(ResultMapConfig resultConfig) {
        this.resultConfig = resultConfig;
    }

    public String getFirstToken(String s) {
        return new StringTokenizer(s, ", ", false).nextToken();
    }

    public String[] getAllButFirstToken(String s) {
        List strings = new ArrayList();
        StringTokenizer parser = new StringTokenizer(s, ", ", false);
        parser.nextToken();
        while (parser.hasMoreTokens()) {
            strings.add(parser.nextToken());
        }
        return (String[]) strings.toArray(new String[strings.size()]);
    }

    //处理<sqlMapConfig><properties>元素的属性值resource或url
    public void setGlobalProperties(String resource, String url) {
        DEBUG.P(this, "setGlobalProperties(2)");
        config.getErrorContext().setActivity("loading global properties");
        try {
            Properties props;
            if (resource != null) {
                config.getErrorContext().setResource(resource);
                props = Resources.getResourceAsProperties(resource);

                DEBUG.P("props1=" + props);
            } else if (url != null) {
                config.getErrorContext().setResource(url);
                props = Resources.getUrlAsProperties(url);

                DEBUG.P("props2=" + props);
            } else {
                throw new RuntimeException("The " + "properties" + " element requires either a resource or a url attribute.");
            }

            DEBUG.P("props3=" + props);

            // Merge properties with those passed in programmatically
            if (props != null) {
                props.putAll(globalProps);
                globalProps = props;
            }

            DEBUG.P("globalProps=" + globalProps);
        } catch (Exception e) {
            throw new RuntimeException("Error loading properties.  Cause: " + e, e);
        }

        DEBUG.P(0, this, "setGlobalProperties(2)");
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
