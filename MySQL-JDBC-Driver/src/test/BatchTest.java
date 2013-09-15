package test;

import java.sql.*;
import java.util.*;

//javac test\BatchTest.java
//java -cp test;lib\mysql-connector-java-5.1.6.jar;E:\Douyu\lib\oracle9i.jar BatchTest
public class BatchTest 
{
	private static Connection getMySQLConnection(boolean autoCommit) {
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://localhost/test";
		String userName = "root";
		String password = "test";

		Properties info =  new Properties();
		//info.put("propertiesTransform", "my.NotFoundConnectionPropertiesTransform");
		info.put("user","root");
		info.put("password","test");
		info.put("holdResultsOpenOverStatementClose","true");
		info.put("allowMultiQueries","true");

		Connection conn = null;
		try {
			Class.forName(driver);
			//conn=DriverManager.getConnection(url,userName,password);

			conn=DriverManager.getConnection(url,info);

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
	/*
	private static Connection getOracleConnection(boolean autoCommit) {
		String driver = "oracle.jdbc.OracleDriver";
		String url = "jdbc:oracle:thin:@127.0.0.1:1521:MyOracle";
		//String url="jdbc:oracle:thin:@127.0.0.1:1521:DOUYU";
		String userName = "test";
		String password = "test";

		Connection conn = null;
		try {
			Class.forName(driver);
			conn=DriverManager.getConnection(url,userName,password);

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
	*/
	public static void sqlException(SQLException e) {
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
	public static void main(String[] args) throws SQLException
	{
		Connection conn = getMySQLConnection(true);
		Statement stmt = conn.createStatement();
		stmt.addBatch("insert into pet(name) values('abc')");
		stmt.addBatch("insert into pet(name) values('abc2')");

		//stmt.addBatch("insert into pet(name,age) values('aaaa',11111111111111111111111111111)");
		
		stmt.addBatch("delete from pet where name = 'abc'");
		stmt.addBatch("insert into pet(name) values('abc2')");
		stmt.addBatch("insert into pet(name) values('abc2')");

		stmt.addBatch("select * from pet");
		print(stmt.executeBatch());

		stmt.close();
		conn.close();

		/*
		conn = getOracleConnection(true);
		stmt = conn.createStatement();

		stmt = conn.createStatement();
		stmt.addBatch("insert into tablea(a1) values(10)");
		stmt.addBatch("insert into tablea(a2) values('aaaaaaaaaaaaaaaaaaaaaaaaa')");
		stmt.addBatch("insert into tablea(a1) values(30)");
		//stmt.addBatch("select * from tablea");
		stmt.addBatch("delete from tablea where a1 = 10");
		print(stmt.executeBatch());

		stmt.close();
		conn.close();
		*/
	}

	static void print(int[] array) {
		if(array == null) System.err.println("array=null");

		System.err.println("array.length="+array.length);
		for(int i: array) {
			System.err.println("i="+i);
		}
	}


}