package test;

import java.sql.*;
import java.util.*;

//java -cp E:\MySQL\lib\mysql-connector-java-5.1.13\mysql-connector-java-5.1.13\mysql-connector-java-5.1.13-bin.jar;classes test.PreparedStatementTest
public class PreparedStatementTest {
	private static Connection getMySQLConnection() throws Exception {
		return getMySQLConnection(true);
	}
	private static Connection getMySQLConnection(boolean autoCommit) throws Exception {
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://localhost/test";

		Properties info =  new Properties();
		info.put("user","root");
		info.put("password","test");
		//info.put("holdResultsOpenOverStatementClose","true");
		//info.put("allowMultiQueries","true");

		info.put("useServerPrepStmts", "true");

		Class.forName(driver);

		Connection conn = DriverManager.getConnection(url,info);
		conn.setAutoCommit(autoCommit);
		return conn;
	}

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

	static void print(int[] array) {
		if(array == null) System.err.println("array=null");

		System.err.println("array.length="+array.length);
		for(int i: array) {
			System.err.println("i="+i);
		}
	}



	public static void main(String[] args) throws Exception {
		try {
			Connection conn = getMySQLConnection();
			//conn.close();
			//String sql = null;
			//String sql = "select * from pet where name = ?  ON DUPLICATE KEY UPDATE ";
			String sql = "select * from pet where age=? or name=?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			System.err.println(stmt.getClass().getName());

			long t1 = System.currentTimeMillis();
			for(int i=0;i<20000;i++) {
				stmt.setInt(1, i);
				stmt.setString(2, i+"");
				stmt.executeQuery();
			}

			System.err.println(System.currentTimeMillis() - t1);

			stmt.close();
			conn.close();
		} catch (SQLException e) {
			sqlException(e);
			e.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}