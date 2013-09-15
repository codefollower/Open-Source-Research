package com.ibatis.sqlmap.engine.builder.xml;

import com.ibatis.common.resources.*;
import com.ibatis.common.xml.*;
import com.ibatis.sqlmap.client.*;
import com.ibatis.sqlmap.engine.config.*;
import com.ibatis.sqlmap.engine.transaction.*;
import com.ibatis.sqlmap.engine.datasource.*;
import com.ibatis.sqlmap.engine.mapping.result.*;
import org.w3c.dom.Node;

import java.io.*;
import java.util.Properties;

public class SqlMapConfigParser {
    private static my.Debug DEBUG = new my.Debug(my.Debug.SqlMapConfigParser);//我加上的

    //下面两个类都没有默认构造函数
    protected final NodeletParser parser = new NodeletParser();
    private XmlParserState state = new XmlParserState();

    private boolean usingStreams = false;

    public SqlMapConfigParser() {
        DEBUG.P(this, "SqlMapConfigParser()");

        parser.setValidation(true);
        parser.setEntityResolver(new SqlMapClasspathEntityResolver());

        addSqlMapConfigNodelets();
        addGlobalPropNodelets();
        addSettingsNodelets();
        addTypeAliasNodelets();
        addTypeHandlerNodelets();
        addTransactionManagerNodelets();
        addSqlMapNodelets();
        addResultObjectFactoryNodelets();

        DEBUG.P(0, this, "SqlMapConfigParser()");

    }

    public SqlMapClient parse(Reader reader, Properties props) {
        if (props != null)
            state.setGlobalProps(props);
        return parse(reader);
    }

    public SqlMapClient parse(Reader reader) {
        try {//我加上的
            DEBUG.P(this, "parse(1)");

            try {
                usingStreams = false;

                parser.parse(reader);

                //DEBUG.P("state.getConfig().getTypeHandlerFactory().getTypeAliasesMap()="+state.getConfig().getTypeHandlerFactory().getTypeAliasesMap());

                return state.getConfig().getClient();
            } catch (Exception e) {
                throw new RuntimeException("Error occurred.  Cause: " + e, e);
            }

        } finally {//我加上的
            DEBUG.P(0, this, "parse(1)");
        }
    }

    public SqlMapClient parse(InputStream inputStream, Properties props) {
        if (props != null)
            state.setGlobalProps(props);
        return parse(inputStream);
    }

    public SqlMapClient parse(InputStream inputStream) {
        try {
            usingStreams = true;

            parser.parse(inputStream);
            return state.getConfig().getClient();
        } catch (Exception e) {
            throw new RuntimeException("Error occurred.  Cause: " + e, e);
        }
    }

    //对应com\ibatis\sqlmap\engine\builder\xml\sql-map-config-2.dtd文件
    //每个元素都对应一个方法
    private void addSqlMapConfigNodelets() {
        parser.addNodelet("/sqlMapConfig/end()", new Nodelet() {
            public void process(Node node) throws Exception {
                DEBUG.P(this, "addSqlMapConfigNodelets->process(1)");
                DEBUG.P("node=" + node);

                state.getConfig().finalizeSqlMapConfig();

                DEBUG.P(0, this, "addSqlMapConfigNodelets->process(1)");
            }
        });
    }

    //最多只能定义一个properties子元素
    //properties元素有两个属性:resource与url，
    //只要设置其中之一就可以了，如果两者都设置了，优先用resource属性的值
    private void addGlobalPropNodelets() {
        parser.addNodelet("/sqlMapConfig/properties", new Nodelet() {
            public void process(Node node) throws Exception {
                DEBUG.P(this, "addGlobalPropNodelets->process(1)");
                DEBUG.P("node=" + node);

                Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
                String resource = attributes.getProperty("resource");
                String url = attributes.getProperty("url");

                DEBUG.P("resource=" + resource);
                DEBUG.P("url=" + url);

                state.setGlobalProperties(resource, url);

                DEBUG.P(0, this, "addGlobalPropNodelets->process(1)");
            }
        });
    }

    private void addSettingsNodelets() {
        //少了4个属性: errorTracingEnabled maxSessions maxTransactions maxRequests
        parser.addNodelet("/sqlMapConfig/settings", new Nodelet() {
            public void process(Node node) throws Exception {
                DEBUG.P(this, "addSettingsNodelets->process(1)");
                DEBUG.P("node=" + node);

                Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
                SqlMapConfiguration config = state.getConfig();

                String classInfoCacheEnabledAttr = attributes.getProperty("classInfoCacheEnabled");
                boolean classInfoCacheEnabled = (classInfoCacheEnabledAttr == null || "true".equals(classInfoCacheEnabledAttr));
                config.setClassInfoCacheEnabled(classInfoCacheEnabled);

                String lazyLoadingEnabledAttr = attributes.getProperty("lazyLoadingEnabled");
                boolean lazyLoadingEnabled = (lazyLoadingEnabledAttr == null || "true".equals(lazyLoadingEnabledAttr));
                config.setLazyLoadingEnabled(lazyLoadingEnabled);

                String statementCachingEnabledAttr = attributes.getProperty("statementCachingEnabled");
                boolean statementCachingEnabled = (statementCachingEnabledAttr == null || "true"
                        .equals(statementCachingEnabledAttr));
                config.setStatementCachingEnabled(statementCachingEnabled);

                String cacheModelsEnabledAttr = attributes.getProperty("cacheModelsEnabled");
                boolean cacheModelsEnabled = (cacheModelsEnabledAttr == null || "true".equals(cacheModelsEnabledAttr));
                config.setCacheModelsEnabled(cacheModelsEnabled);

                String enhancementEnabledAttr = attributes.getProperty("enhancementEnabled");
                boolean enhancementEnabled = (enhancementEnabledAttr == null || "true".equals(enhancementEnabledAttr));
                config.setEnhancementEnabled(enhancementEnabled);

                String useColumnLabelAttr = attributes.getProperty("useColumnLabel");
                boolean useColumnLabel = (useColumnLabelAttr == null || "true".equals(useColumnLabelAttr));
                config.setUseColumnLabel(useColumnLabel);

                String forceMultipleResultSetSupportAttr = attributes.getProperty("forceMultipleResultSetSupport");
                boolean forceMultipleResultSetSupport = "true".equals(forceMultipleResultSetSupportAttr);
                config.setForceMultipleResultSetSupport(forceMultipleResultSetSupport);

                String defaultTimeoutAttr = attributes.getProperty("defaultStatementTimeout");
                Integer defaultTimeout = defaultTimeoutAttr == null ? null : Integer.valueOf(defaultTimeoutAttr);
                config.setDefaultStatementTimeout(defaultTimeout);

                String useStatementNamespacesAttr = attributes.getProperty("useStatementNamespaces");
                boolean useStatementNamespaces = "true".equals(useStatementNamespacesAttr);
                state.setUseStatementNamespaces(useStatementNamespaces);

                DEBUG.P("classInfoCacheEnabled=" + classInfoCacheEnabled);
                DEBUG.P("lazyLoadingEnabled=" + lazyLoadingEnabled);
                DEBUG.P("statementCachingEnabled=" + statementCachingEnabled);
                DEBUG.P("cacheModelsEnabled=" + cacheModelsEnabled);
                DEBUG.P("enhancementEnabled=" + enhancementEnabled);
                DEBUG.P("useColumnLabel=" + useColumnLabel);
                DEBUG.P("forceMultipleResultSetSupport=" + forceMultipleResultSetSupport);
                DEBUG.P("defaultTimeout=" + defaultTimeout);
                DEBUG.P("useStatementNamespaces=" + useStatementNamespaces);

                DEBUG.P(0, this, "addSettingsNodelets->process(1)");
            }
        });
    }

    private void addTypeAliasNodelets() {
        parser.addNodelet("/sqlMapConfig/typeAlias", new Nodelet() {
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

    private void addTypeHandlerNodelets() {
        parser.addNodelet("/sqlMapConfig/typeHandler", new Nodelet() {
            public void process(Node node) throws Exception {
                DEBUG.P(this, "addTypeHandlerNodelets->process(1)");

                Properties prop = NodeletUtils.parseAttributes(node, state.getGlobalProps());
                String jdbcType = prop.getProperty("jdbcType");
                String javaType = prop.getProperty("javaType");
                String callback = prop.getProperty("callback");

                DEBUG.P("jdbcType=" + jdbcType);
                DEBUG.P("javaType=" + javaType);
                DEBUG.P("callback=" + callback);

                javaType = state.getConfig().getTypeHandlerFactory().resolveAlias(javaType);
                callback = state.getConfig().getTypeHandlerFactory().resolveAlias(callback);

                DEBUG.P("javaType=" + javaType);
                DEBUG.P("callback=" + callback);

                state.getConfig().newTypeHandler(Resources.classForName(javaType), jdbcType, Resources.instantiate(callback));

                DEBUG.P(0, this, "addTypeHandlerNodelets->process(1)");
            }
        });
    }

    private void addTransactionManagerNodelets() {
        parser.addNodelet("/sqlMapConfig/transactionManager/property", new Nodelet() {
            public void process(Node node) throws Exception {
                Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
                String name = attributes.getProperty("name");
                String value = NodeletUtils.parsePropertyTokens(attributes.getProperty("value"), state.getGlobalProps());
                state.getTxProps().setProperty(name, value);
            }
        });
        parser.addNodelet("/sqlMapConfig/transactionManager/end()", new Nodelet() {
            public void process(Node node) throws Exception {
                DEBUG.P(this, "addTransactionManagerNodelets:transactionManager/end()->process(1)");

                Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
                String type = attributes.getProperty("type");
                boolean commitRequired = "true".equals(attributes.getProperty("commitRequired"));

                DEBUG.P("type=" + type);
                DEBUG.P("commitRequired=" + commitRequired);

                state.getConfig().getErrorContext().setActivity("configuring the transaction manager");
                type = state.getConfig().getTypeHandlerFactory().resolveAlias(type);

                DEBUG.P("type=" + type);

                TransactionManager txManager;
                try {
                    state.getConfig().getErrorContext().setMoreInfo("Check the transaction manager type or class.");
                    TransactionConfig config = (TransactionConfig) Resources.instantiate(type);
                    config.setDataSource(state.getDataSource());
                    state.getConfig().getErrorContext().setMoreInfo("Check the transactio nmanager properties or configuration.");
                    config.setProperties(state.getTxProps());
                    config.setForceCommit(commitRequired);
                    config.setDataSource(state.getDataSource());
                    state.getConfig().getErrorContext().setMoreInfo(null);
                    txManager = new TransactionManager(config);
                } catch (Exception e) {
                    if (e instanceof SqlMapException) {
                        throw (SqlMapException) e;
                    } else {
                        throw new SqlMapException(
                                "Error initializing TransactionManager.  Could not instantiate TransactionConfig.  Cause: " + e,
                                e);
                    }
                }
                state.getConfig().setTransactionManager(txManager);

                DEBUG.P(0, this, "addTransactionManagerNodelets:transactionManager/end()->process(1)");
            }
        });
        parser.addNodelet("/sqlMapConfig/transactionManager/dataSource/property", new Nodelet() {
            public void process(Node node) throws Exception {
                Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
                String name = attributes.getProperty("name");
                String value = NodeletUtils.parsePropertyTokens(attributes.getProperty("value"), state.getGlobalProps());
                state.getDsProps().setProperty(name, value);
            }
        });
        parser.addNodelet("/sqlMapConfig/transactionManager/dataSource/end()", new Nodelet() {
            public void process(Node node) throws Exception {
                DEBUG.P(this, "addTransactionManagerNodelets:dataSource->process(1)");

                state.getConfig().getErrorContext().setActivity("configuring the data source");

                Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());

                String type = attributes.getProperty("type");
                Properties props = state.getDsProps();

                DEBUG.P("type=" + type);
                DEBUG.P("props=" + props);

                type = state.getConfig().getTypeHandlerFactory().resolveAlias(type);

                DEBUG.P("type=" + type);
                try {
                    state.getConfig().getErrorContext().setMoreInfo("Check the data source type or class.");
                    DataSourceFactory dsFactory = (DataSourceFactory) Resources.instantiate(type);
                    state.getConfig().getErrorContext().setMoreInfo("Check the data source properties or configuration.");

                    //会在initialize中检查是否有些属性没有设置
                    //比如type=SIMPLE时，JDBC.Driver ConnectionURL Username Password都是必须的
                    dsFactory.initialize(props); //Properties实现了java.util.Map
                    state.setDataSource(dsFactory.getDataSource());
                    state.getConfig().getErrorContext().setMoreInfo(null);
                } catch (Exception e) {
                    if (e instanceof SqlMapException) {
                        throw (SqlMapException) e;
                    } else {
                        throw new SqlMapException(
                                "Error initializing DataSource.  Could not instantiate DataSourceFactory.  Cause: " + e, e);
                    }
                }

                DEBUG.P(0, this, "addTransactionManagerNodelets:dataSource->process(1)");
            }
        });
    }

    protected void addSqlMapNodelets() {
        parser.addNodelet("/sqlMapConfig/sqlMap", new Nodelet() {
            public void process(Node node) throws Exception {
                DEBUG.P(this, "addSqlMapNodelets->process(1)");

                state.getConfig().getErrorContext().setActivity("loading the SQL Map resource");

                Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());

                String resource = attributes.getProperty("resource");
                String url = attributes.getProperty("url");

                DEBUG.P("resource=" + resource);
                DEBUG.P("url=" + url);
                DEBUG.P("usingStreams=" + usingStreams);

                if (usingStreams) {
                    InputStream inputStream = null;
                    if (resource != null) {
                        state.getConfig().getErrorContext().setResource(resource);
                        inputStream = Resources.getResourceAsStream(resource);
                    } else if (url != null) {
                        state.getConfig().getErrorContext().setResource(url);
                        inputStream = Resources.getUrlAsStream(url);
                    } else {
                        throw new SqlMapException("The <sqlMap> element requires either a resource or a url attribute.");
                    }

                    new SqlMapParser(state).parse(inputStream);
                } else {
                    Reader reader = null;
                    if (resource != null) {
                        state.getConfig().getErrorContext().setResource(resource);
                        reader = Resources.getResourceAsReader(resource);
                    } else if (url != null) {
                        state.getConfig().getErrorContext().setResource(url);
                        reader = Resources.getUrlAsReader(url);
                    } else {
                        throw new SqlMapException("The <sqlMap> element requires either a resource or a url attribute.");
                    }

                    new SqlMapParser(state).parse(reader);
                }

                DEBUG.P(0, this, "addSqlMapNodelets->process(1)");
            }
        });
    }

    private void addResultObjectFactoryNodelets() {
        parser.addNodelet("/sqlMapConfig/resultObjectFactory", new Nodelet() {
            public void process(Node node) throws Exception {
                DEBUG.P(this, "addResultObjectFactoryNodelets->process(1)");

                Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
                String type = attributes.getProperty("type");

                DEBUG.P("type=" + type);

                state.getConfig().getErrorContext().setActivity("configuring the Result Object Factory");
                ResultObjectFactory rof;
                try {
                    rof = (ResultObjectFactory) Resources.instantiate(type);
                    state.getConfig().setResultObjectFactory(rof);
                } catch (Exception e) {
                    throw new SqlMapException("Error instantiating resultObjectFactory: " + type, e);
                }

                DEBUG.P(0, this, "addResultObjectFactoryNodelets->process(1)");

            }
        });
        parser.addNodelet("/sqlMapConfig/resultObjectFactory/property", new Nodelet() {
            public void process(Node node) throws Exception {
                Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
                String name = attributes.getProperty("name");
                String value = NodeletUtils.parsePropertyTokens(attributes.getProperty("value"), state.getGlobalProps());
                state.getConfig().getDelegate().getResultObjectFactory().setProperty(name, value);
            }
        });
    }

}
