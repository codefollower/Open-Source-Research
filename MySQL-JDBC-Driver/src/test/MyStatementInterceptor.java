package test;

import java.sql.SQLException;
import java.util.Properties;

import com.mysql.jdbc.Statement;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSetInternalMethods;

public class MyStatementInterceptor implements com.mysql.jdbc.StatementInterceptor {
	public SQLException interceptException(SQLException sqlEx, Connection conn) {
		System.err.println("MyExceptionInterceptor.interceptException:");
		ConnectionUtil.sqlException(sqlEx);
		return sqlEx;
	}

	public void init(Connection conn, Properties props) throws SQLException {
		System.err.println("MyStatementInterceptor.init");
	}

	public void destroy() {
		System.err.println("MyStatementInterceptor.destroy");
	}

	//注意是com.mysql.jdbc.Statement;
	public ResultSetInternalMethods preProcess(String sql,
			Statement interceptedStatement, Connection connection)
			throws SQLException {

		System.err.println("MyStatementInterceptor.preProcess");

		return null;
	}

	public ResultSetInternalMethods postProcess(String sql,
			Statement interceptedStatement,
			ResultSetInternalMethods originalResultSet,
			Connection connection) throws SQLException {

		System.err.println("MyStatementInterceptor.postProcess old");

		return null;
	}

	public ResultSetInternalMethods postProcess(String sql,
			Statement interceptedStatement,
			ResultSetInternalMethods originalResultSet,
			Connection connection, int warningCount, boolean noIndexUsed, boolean noGoodIndexUsed, 
			SQLException statementException) throws SQLException {
		System.err.println("MyStatementInterceptor.postProcess new");

		return null;
	}

	public boolean executeTopLevelOnly() {
		System.err.println("MyStatementInterceptor.executeTopLevelOnly");

		return true;
	}
}