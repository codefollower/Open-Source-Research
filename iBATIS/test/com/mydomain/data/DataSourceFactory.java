package com.mydomain.data;

import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;

public class DataSourceFactory {
    public static DataSource getDataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.hsqldb.jdbcDriver");
        ds.setUsername("sa");
        ds.setPassword("sa");
        ds.setUrl("jdbc:hsqldb:.");
        ds.setInitialSize(2);
        ds.setMaxActive(5);
        return ds;
    }
}
