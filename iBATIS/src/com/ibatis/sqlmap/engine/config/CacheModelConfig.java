package com.ibatis.sqlmap.engine.config;

import com.ibatis.sqlmap.engine.cache.*;
import com.ibatis.sqlmap.engine.impl.*;
import com.ibatis.sqlmap.engine.scope.*;

import java.util.Properties;

public class CacheModelConfig {
    private ErrorContext errorContext;
    private CacheModel cacheModel;

    //解析配置文件生成CacheModelConfig实例后，如果有多个<cacheModel>元素，
    //前面的CacheModelConfig实例是不保留的，
    //但是CacheModel实例会通过CacheModel.getId()放到SqlMapExecutorDelegate.cacheModels中
    CacheModelConfig(SqlMapConfiguration config, String id, CacheController controller, boolean readOnly, boolean serialize) {
        this.errorContext = config.getErrorContext();
        this.cacheModel = new CacheModel();
        SqlMapClientImpl client = config.getClient();
        errorContext.setActivity("building a cache model");
        cacheModel.setReadOnly(readOnly);
        cacheModel.setSerialize(serialize);
        errorContext.setObjectId(id + " cache model");
        errorContext.setMoreInfo("Check the cache model type.");
        cacheModel.setId(id);
        cacheModel.setResource(errorContext.getResource());
        try {
            cacheModel.setCacheController(controller);
        } catch (Exception e) {
            throw new RuntimeException("Error setting Cache Controller Class.  Cause: " + e, e);
        }
        errorContext.setMoreInfo("Check the cache model configuration.");
        if (client.getDelegate().isCacheModelsEnabled()) {
            client.getDelegate().addCacheModel(cacheModel);
        }
        errorContext.setMoreInfo(null);
        errorContext.setObjectId(null);
    }

    public void setFlushInterval(long hours, long minutes, long seconds, long milliseconds) {
        errorContext.setMoreInfo("Check the cache model flush interval.");
        long t = 0L;
        t += milliseconds;
        t += seconds * 1000L;
        t += minutes * 60L * 1000L;
        t += hours * 60L * 60L * 1000L;
        if (t < 1L)
            throw new RuntimeException("A flush interval must specify one or more of milliseconds, seconds, minutes or hours.");
        cacheModel.setFlushInterval(t);
    }

    public void addFlushTriggerStatement(String statement) {
        errorContext.setMoreInfo("Check the cache model flush on statement elements.");
        cacheModel.addFlushTriggerStatement(statement);
    }

    public CacheModel getCacheModel() {
        return cacheModel;
    }

    public void setControllerProperties(Properties cacheProps) {
        cacheModel.setControllerProperties(cacheProps);
    }
}
