package test;

import java.sql.*;
import java.util.Properties;
import com.mysql.jdbc.ReplicationDriver;

public class ReplicationTest {

	static Connection conn;
	static Statement stmt;
	static PreparedStatement ps;
	static ResultSet rs;

	public static void main(String[] args) throws Exception {
		try {
			initConnection();

			testMasterSlaves();

			//testMasterFailed();

		} catch (SQLException e) {
			ConnectionUtil.sqlException(e);
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (ps != null)
				ps.close();
			if (conn != null)
				conn.close();
		}
	}

	static void initConnection() throws Exception {
		String url = "jdbc:mysql://localhost,localhost:3306/test";
		ReplicationDriver driver = new ReplicationDriver();
		Properties props = new Properties();
		//props.put("propertiesTransform", "my.NotFoundConnectionPropertiesTransform");

		props.put("user", "root");
		props.put("password", "test");
		props.put("holdResultsOpenOverStatementClose", "true");
		//props.put("allowMultiQueries","true");
		props.put("useCursorFetch", "true");
		//props.put("traceProtocol","true");
		// We want this for failover on the slaves
		//props.put("autoReconnect", "true");
		// We want to load balance between the slaves
		props.put("roundRobinLoadBalance", "true");

		//会抛出NullPointerException，是个Bug???
		//props.put("retriesAllDown", "0");
		props.put("retriesAllDown", "3"); //retriesAllDown默认是120，所以主机不可用时重试次数

		props.put("loadBalanceBlacklistTimeout", "1");

		//loadBalanceStrategy默认是random，也可以自定义一个实现com.mysql.jdbc.BalanceStrategy接口的类
		props.put("loadBalanceStrategy", "bestResponseTime");

		//
		// Looks like a normal MySQL JDBC url, with a
		// comma-separated list of hosts, the first
		// being the 'master', the rest being any number
		// of slaves that the driver will load balance against
		//
		conn = driver.connect(url, props);
	}

	static void testMasterSlaves() throws Exception {
		//
		// Perform read/write work on the master
		// by setting the read-only flag to "false"
		//
		conn.setReadOnly(false);
		conn.setAutoCommit(false);
		conn.createStatement().executeUpdate("insert into pet(name) values('pet1')");
		conn.commit();
		//
		// Now, do a query from a slave, the driver automatically picks one
		// from the list
		//
		conn.setReadOnly(true);
		rs = conn.createStatement().executeQuery("SELECT id,name FROM pet");

		conn.commit();

		if (rs.next()) {
			System.out.println(rs.getInt(1));
			System.out.println(rs.getString(2));
		}
	}

	//先运行，然后断开网线后测试
	static void testMasterFailed() throws Exception {
		/*
		com.mysql.jdbc.CommunicationsException: Communications link failure

		The last packet successfully received from the server was 10,999 milliseconds ago.  The last packet sent successfully to the server was 8,999 milliseconds ago.
			at com.mysql.jdbc.SQLError.createCommunicationsException(SQLError.java:1118)
			at com.mysql.jdbc.MysqlIO.reuseAndReadPacket(MysqlIO.java:2883)
			at com.mysql.jdbc.MysqlIO.reuseAndReadPacket(MysqlIO.java:2778)
			at com.mysql.jdbc.MysqlIO.checkErrorPacket(MysqlIO.java:3280)
			at com.mysql.jdbc.MysqlIO.sendCommand(MysqlIO.java:1861)
			at com.mysql.jdbc.MysqlIO.sqlQueryDirect(MysqlIO.java:2007)
			at com.mysql.jdbc.ConnectionImpl.execSQL(ConnectionImpl.java:2522)
			at com.mysql.jdbc.ConnectionImpl.setCatalog(ConnectionImpl.java:4871)
			at com.mysql.jdbc.ReplicationConnection.swapConnections(ReplicationConnection.java:561)
			at com.mysql.jdbc.ReplicationConnection.switchToMasterConnection(ReplicationConnection.java:536)
			at com.mysql.jdbc.ReplicationConnection.setReadOnly(ReplicationConnection.java:491)
			at test.ReplicationTest.testMasterFailed(ReplicationTest.java:104)
			at test.ReplicationTest.main(ReplicationTest.java:20)
		Caused by: java.net.SocketException: Connection reset
			at java.net.SocketInputStream.read(SocketInputStream.java:168)
			at com.mysql.jdbc.util.ReadAheadInputStream.fill(ReadAheadInputStream.java:114)
			at com.mysql.jdbc.util.ReadAheadInputStream.readFromUnderlyingStreamIfNecessary(ReadAheadInputStream.java:161)
			at com.mysql.jdbc.util.ReadAheadInputStream.read(ReadAheadInputStream.java:189)
			at com.mysql.jdbc.MysqlIO.readFully(MysqlIO.java:2360)
			at com.mysql.jdbc.MysqlIO.reuseAndReadPacket(MysqlIO.java:2788)
			... 11 more

		SQLException:com.mysql.jdbc.CommunicationsException: Communications link failure

		The last packet successfully received from the server was 10,999 milliseconds ago.  The last packet sent successfully to the server was 8,999 milliseconds ago.
		-----------------------------------
		Message  : Communications link failure

		The last packet successfully received from the server was 10,999 milliseconds ago.  The last packet sent successfully to the server was 8,999 milliseconds ago.
		SQLState : 08S01
		ErrorCode: 0
		*/


		while (true) {
			//
			// Perform read/write work on the master
			// by setting the read-only flag to "false"
			//
			conn.setReadOnly(false);
			conn.setAutoCommit(false);
			conn.createStatement().executeUpdate("insert into pet(name) values('pet1')");
			conn.commit();
			//
			// Now, do a query from a slave, the driver automatically picks one
			// from the list
			//
			conn.setReadOnly(true);
			rs = conn.createStatement().executeQuery("SELECT id,name FROM pet");

			conn.commit();

			if (rs.next()) {
				System.out.println(rs.getInt(1));
				System.out.println(rs.getString(2));
			}
			rs.close();

			Thread.sleep(2000);
		}
	}
}