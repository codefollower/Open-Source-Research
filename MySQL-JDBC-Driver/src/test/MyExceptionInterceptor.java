package test;

import java.sql.*;
import java.util.Properties;
import com.mysql.jdbc.Connection;

public class MyExceptionInterceptor implements com.mysql.jdbc.ExceptionInterceptor {
	public SQLException interceptException(SQLException sqlEx, Connection conn) {
		System.err.println("MyExceptionInterceptor.interceptException:");
		ConnectionUtil.sqlException(sqlEx);
		return sqlEx;
	}

	public void init(Connection conn, Properties props) throws SQLException {
		System.err.println("MyExceptionInterceptor.init: props = "+props);
	}

	public void destroy() {}
}