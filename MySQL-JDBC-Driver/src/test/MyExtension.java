package test;

import java.sql.*;
import java.util.Properties;
import com.mysql.jdbc.Connection;

public class MyExtension implements com.mysql.jdbc.Extension {

	public void init(Connection conn, Properties props) throws SQLException {
		System.err.println("MyExceptionInterceptor.init: props = "+props);
	}

	public void destroy() {}
}