package com.mydomain;

import java.sql.*;
import com.ibatis.sqlmap.client.extensions.*;

public class YesNoBoolTypeHandlerCallback implements TypeHandlerCallback {

    private static final String YES = "Yes";
    private static final String NO = "No";

    public Object getResult(ResultGetter getter) throws SQLException {
        String s = getter.getString();
        if (YES.equalsIgnoreCase(s)) {
            return new Boolean(true);
        } else if (NO.equalsIgnoreCase(s)) {
            return new Boolean(false);
        } else {
            throw new SQLException("Unexpected value " + s + " found where " + YES + " or " + NO + " was expected.");
        }
    }

    public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
        boolean b = ((Boolean) parameter).booleanValue();
        if (b) {
            setter.setString(YES);
        } else {
            setter.setString(NO);
        }
    }

    public Object valueOf(String s) {
        if (YES.equalsIgnoreCase(s)) {
            return new Boolean(true);
        } else if (NO.equalsIgnoreCase(s)) {
            return new Boolean(false);
        } else {
            return s;
            //throw new SQLException ("Unexpected value " + s + " found where "+YES+" or "+NO+" was expected.");
        }
    }

}
