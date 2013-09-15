package test;

import java.sql.*;

import java.sql.Connection;
import java.util.Properties;

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

	public static Connection getConnection(String driver, String url, Properties info) {
		return getConnection(driver, url, info, true);
	}

	public static Connection getConnection(String driver, String url, Properties info, boolean autoCommit) {
		Connection conn = null;
		try {
			Class.forName(driver);
			conn = java.sql.DriverManager.getConnection(url, info);

			conn.setAutoCommit(autoCommit);
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