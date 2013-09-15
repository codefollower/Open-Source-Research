package test;

import java.util.*;
import java.sql.*;

import com.mysql.jdbc.Driver;

public class DriverTest {
	public static void sqlException(SQLException e) {
		e.printStackTrace();
		System.err.println();

		while(e!=null) {
			System.err.println("SQLException:"+e);
			System.err.println("-----------------------------------");
			System.err.println("Message  : "+e.getMessage());
			System.err.println("SQLState : "+e.getSQLState());
			System.err.println("ErrorCode: "+e.getErrorCode());
			System.err.println();
			System.err.println();
			e=e.getNextException();
		}
	}

	private static Connection getMySQLConnection() {
		return getMySQLConnection(true);
	}
	private static Connection getMySQLConnection(boolean autoCommit) {
		//return getMySQLConnection1(autoCommit);

		return getMySQLConnection2(autoCommit);
	}

	private static Connection getMySQLConnection1(boolean autoCommit) {
		String driver = "com.mysql.jdbc.Driver";

		//String url = "jdbc:mysql://localhost/test?key1=value1&key2=value2&pedantic=true";
		
		//Illegal connection port value '990j'
		//String url = "jdbc:mysql://localhost:990j,127.0.0.1/test?key1=value1&key2=value2";
		//String url = "jdbc:mysql:replication://localhost:3306,localhost,localhost/test?key1=value1&key2=value2";
		String url = "jdbc:mysql://localhost,127.0.0.1/test?key1=value1&key2=value2";
		
		String userName = "root";
		String password = "test";

		//Properties info =  new Properties();
		//info.put("propertiesTransform", "my.NotFoundConnectionPropertiesTransform");

		Properties info =  new Properties();
		//info.put("propertiesTransform", "my.NotFoundConnectionPropertiesTransform");
		info.put("user","root");
		info.put("password","test");
		info.put("holdResultsOpenOverStatementClose","true");
		//info.put("allowMultiQueries","true");
		info.put("useCursorFetch","true");
		info.put("traceProtocol","true");

		info.put("cachePrepStmts","true");

		//info.put("autoReconnect","true");
		info.put("roundRobinLoadBalance","true");
		info.put("autoGenerateTestcaseScript","true");

		

		
		
		Connection conn = null;
		try {
			Class.forName(driver); //jdk1.6不需要这一行了
			//conn=DriverManager.getConnection(url,userName,password);

			conn=DriverManager.getConnection(url,info);

			//conn=DriverManager.getConnection(url,info);

			conn.setAutoCommit(autoCommit);//禁用自动提交模式
			//conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			//conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			
		} catch (SQLException e) {
			sqlException(e);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return conn;
	}

	private static Connection getMySQLConnection2(boolean autoCommit) {
		String driver = "com.mysql.jdbc.Driver";

		//String url = "jdbc:mysql://localhost/test?key1=value1&key2=value2&pedantic=true";
		
		//Illegal connection port value '990j'
		//String url = "jdbc:mysql://localhost:990j,127.0.0.1/test?key1=value1&key2=value2";
		//String url = "jdbc:mysql:replication://localhost:3306,localhost,localhost/test?key1=value1&key2=value2";
		String url = "jdbc:mysql://localhost/test?key1=value1&key2=value2";
		
		String userName = "root";
		String password = "test";

		//Properties info =  new Properties();
		//info.put("propertiesTransform", "my.NotFoundConnectionPropertiesTransform");

		Properties info =  new Properties();
		//info.put("propertiesTransform", "my.NotFoundConnectionPropertiesTransform");
		info.put("user","root");
		info.put("password","test");
		info.put("holdResultsOpenOverStatementClose","true");
		//info.put("allowMultiQueries","true");
		//info.put("useCursorFetch","true");
		//info.put("traceProtocol","true");

		info.put("cachePrepStmts","true");

		//info.put("autoReconnect","true");
		info.put("roundRobinLoadBalance","true");
		info.put("autoGenerateTestcaseScript","true");

		info.put("statementInterceptors","com.mysql.jdbc.interceptors.ServerStatusDiffInterceptor,com.mysql.jdbc.interceptors.SessionAssociationInterceptor");

		

		
		
		Connection conn = null;
		try {
			Class.forName(driver); //jdk1.6不需要这一行了
			//conn=DriverManager.getConnection(url,userName,password);

			conn=DriverManager.getConnection(url,info);

			//conn=DriverManager.getConnection(url,info);

			conn.setAutoCommit(autoCommit);//禁用自动提交模式
			//conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			//conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			
		} catch (SQLException e) {
			sqlException(e);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return conn;
	}

	static Connection conn;
	static Statement stmt;
	static PreparedStatement ps;

	public static void main(String[] args) throws Exception {
		try {
			conn = getMySQLConnection();
			stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT);

			//testConnection();
			//testMysqlIO();
			testPreparedStatement();

		} catch (SQLException e) {
			sqlException(e);
		} finally {
			if(stmt!=null) stmt.close();
			if(ps!=null) ps.close();
			if(conn!=null) conn.close();
		}

	}

	static void testConnection() throws SQLException {

		/*
		Driver driver = new Driver();
		System.out.println(driver.getMajorVersion());
		System.out.println(driver.getMinorVersion());
		System.out.println(driver.jdbcCompliant());

		System.out.println("jdbc.drivers="+System.getProperty("jdbc.drivers"));
		*/
		//Statement stmt = conn.createStatement();
		//MySQL最大只能取5千万条记录
		//stmt.setMaxRows(50000000+1);
		System.out.println("getResultSetType()="+stmt.getResultSetType());

		//stmt.setFetchSize(Integer.MIN_VALUE);

		stmt.setFetchSize(100);
		

		stmt.execute("/* ping */ select * from pet");
		//ResultSet rs = stmt.executeQuery("/* a comment */ select * from pet");
		//ResultSet rs2 = stmt.executeQuery("/* a comment */ select * from pet");
		//rs.next();

		//stmt.executeUpdate("delete from pet where name = 'abc'");

		//Connection is read-only. Queries leading to data modification are not allowed.
		//conn.setReadOnly(true);
		//stmt.executeUpdate("delete from pet where name = 'abc'");

		//stmt.executeUpdate("/* a comment */select * from pet");
		//stmt.executeUpdate("select * from pet");


		conn.setReadOnly(true);
		//stmt.execute("/* a comment */delete from pet where name = 'abc'");
		stmt.execute("/* a comment */ select * from pet");

	}

	static void testMysqlIO() throws SQLException {
		com.mysql.jdbc.interceptors.SessionAssociationInterceptor.setSessionKey("testSessionAssociationInterceptor");

		((com.mysql.jdbc.Connection)conn).setStatementComment("My Comment");
		stmt.executeQuery("select * from pet");

		//StringBuffer s = new StringBuffer();
		//com.mysql.jdbc.MysqlDefs.appendJdbcTypeMappingQuery(s,"colName");
		//System.out.println(s);
	}

	static void testPreparedStatement() throws SQLException {
		ps = conn.prepareStatement("select * from pet /* a middle comment */ where id=? and name=? or age=10");

		//要把asSql()的protected改成public才能测
		//System.out.println(((com.mysql.jdbc.PreparedStatement)ps).asSql());
		//System.out.println(((com.mysql.jdbc.PreparedStatement)ps).asSql(true));

		//ps.setInt(3, 10);
		ps.setInt(1, 10);
		ps.setString(2, "aaa");

		//System.out.println(((com.mysql.jdbc.PreparedStatement)ps).asSql());
		//System.out.println(((com.mysql.jdbc.PreparedStatement)ps).asSql(true));
		ps.executeQuery();

		//StringBuffer s = new StringBuffer();
		//com.mysql.jdbc.MysqlDefs.appendJdbcTypeMappingQuery(s,"colName");
		//System.out.println(s);
	}
}