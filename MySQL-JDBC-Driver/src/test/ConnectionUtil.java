package test;

import java.util.*;
import java.sql.*;

import com.mysql.jdbc.Driver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Properties;
import com.mysql.jdbc.ReplicationDriver;

public class ConnectionUtil {
	public static void sqlException(SQLException e) {
		e.printStackTrace();
		System.err.println();

		while (e != null) {
			System.err.println("SQLException:" + e);
			System.err.println("-----------------------------------");
			System.err.println("Message  : " + e.getMessage());
			System.err.println("SQLState : " + e.getSQLState());
			System.err.println("ErrorCode: " + e.getErrorCode());
			System.err.println();
			System.err.println();
			e = e.getNextException();
		}
	}

	public static Connection getMySQLConnection(String url, Properties info) {
		return getMySQLConnection(url, info, true);
	}

	public static Connection getMySQLConnection(String url, Properties info, boolean autoCommit) {
		String driver = "com.mysql.jdbc.Driver";

		Connection conn = null;
		try {
			Class.forName(driver); //jdk1.6不需要这一行了
			conn = DriverManager.getConnection(url, info);

			//conn = new com.mysql.jdbc.Driver().connect(url, info);

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
}