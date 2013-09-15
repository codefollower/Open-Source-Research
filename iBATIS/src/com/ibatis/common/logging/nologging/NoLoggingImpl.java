package com.ibatis.common.logging.nologging;

import com.ibatis.common.logging.Log;

public class NoLoggingImpl implements Log {

    public NoLoggingImpl(Class clazz) {
    }

    public boolean isDebugEnabled() {
        return false;
    }

    public void error(String s, Throwable e) {
    }

    public void error(String s) {
    }

    public void debug(String s) {
    }

    public void warn(String s) {
    }

}
