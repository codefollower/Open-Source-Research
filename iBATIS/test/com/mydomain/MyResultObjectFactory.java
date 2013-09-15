package com.mydomain;

public class MyResultObjectFactory implements com.ibatis.sqlmap.engine.mapping.result.ResultObjectFactory {

    public Object createInstance(String statementId, @SuppressWarnings("rawtypes") Class clazz) throws InstantiationException,
            IllegalAccessException {
        return null;
    }

    public void setProperty(String name, String value) {
        System.out.println("MyResultObjectFactory name=" + name + ", value=" + value);
    }

}
