package test;

import java.math.*;
import java.util.*;
import java.sql.*;
import java.io.*;

public class JDBCTest {
	static Connection conn;
	static Statement stmt;
	static PreparedStatement ps;
	static ResultSet rs;

	public static void main(String[] args) throws Exception {
		try {
			//initConnection("mysql");

			initConnection();

			stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT);

			//testStatementExecuteQuery();
			//testStatementExecuteUpdate();
			//testStatementExecute();
			//testStatementExecuteBatch();


			//testPreparedStatement_setBigDecimal();
			//testPreparedStatement_setBinaryStream();

			//stmt.setMaxRows(3);

			//testConnection();
			//testMysqlIO();
			testMysqlIO_getResultSet_usingCursor();
			//testPreparedStatement();
			//testServerPreparedStatement();

			//initGetPropertyInfo();

			//testDatabaseMetaData();

			//testResultSetMetaData();

			//testResultSetImpl();

			//testBlobResultSetImpl();

			//testMiddleTransaction();

			//testUserPassword();

			//testStatementCancel();

			//testStreamingResults();
			//testResultSetMetaData();

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

	static void testStatementExecuteQuery() throws SQLException {
		stmt.setQueryTimeout(1);
		stmt.setFetchSize(Integer.MIN_VALUE);
		//stmt.setFetchSize(2);
		rs = stmt.executeQuery("/* ping */select * from pet");

		while(rs.next()) {
			System.out.println("rs1="+rs.getString(1));
		}

		rs = stmt.executeQuery("select * from pet");

		while(rs.next()) {
			System.out.println("id="+rs.getInt(1)+", name="+rs.getString(2)+", age="+rs.getInt(3));
		}
	}

	static void testStatementExecuteUpdate() throws SQLException {
		stmt.setQueryTimeout(1);

		/*
		stmt.setFetchSize(Integer.MIN_VALUE);
		//stmt.setFetchSize(2);
		rs = stmt.executeQuery("select * from pet");

		while(rs.next()) {
			System.out.println("id="+rs.getInt(1)+", name="+rs.getString(2)+", age="+rs.getInt(3));
		}
		*/

		
		stmt.executeUpdate("insert into pet(name,age) values('name1', 1000)", new int[] {1});
		
		conn.setCatalog("mysql");
		stmt.executeUpdate("delete from pet where age=1000");

		//stmt.executeUpdate("select * from pet");
	}

	static void testStatementExecute() throws SQLException {
		stmt.setQueryTimeout(1);
		stmt.setFetchSize(Integer.MIN_VALUE);
		//stmt.setFetchSize(2);
		if(stmt.execute("select * from pet")) {
			rs = stmt.getResultSet();
			while(rs.next()) {
				System.out.println("id="+rs.getInt(1)+", name="+rs.getString(2)+", age="+rs.getInt(3));
			}
		}

		
		stmt.execute("insert into pet(name,age) values('name1', 1000)", new int[] {1});
		
		System.out.println("getUpdateCount()="+stmt.getUpdateCount());
		stmt.execute("delete from pet where age=1000");

		System.out.println("getUpdateCount()="+stmt.getUpdateCount());
	}

	static void testStatementExecuteBatch() throws SQLException {
		stmt.setQueryTimeout(1);
		stmt.setFetchSize(Integer.MIN_VALUE);


		stmt.addBatch("insert into pet(name,age) values('name1', 1000)");
		stmt.addBatch("insert into pet(name,age) values('name1', 1000)");
		stmt.addBatch("insert into pet(name,age) values('name1', 1000)");
		stmt.addBatch("insert into pet(name,age) values('name1', 1000)");
		stmt.addBatch("delete from pet where age=1000");

		int[] updates = stmt.executeBatch();
		for(int updateCount : updates)
			System.out.println("updateCount="+updateCount);
	}

	static void testPreparedStatement_setBigDecimal() throws SQLException {
		ps = conn.prepareStatement("select * from pet where id=?");
		ps.setBigDecimal(1, new BigDecimal(1000));
		ps.setBigDecimal(1, new BigDecimal("1.23E3"));
		ps.executeQuery();
	}

	static void testPreparedStatement_setBinaryStream() throws SQLException {
		ByteArrayInputStream bais = new ByteArrayInputStream(new byte[] {(byte)1,(byte)2});
		ps = conn.prepareStatement("select * from pet where id=?");
		ps.setBinaryStream(1, bais);
		ps.executeQuery();
	}

	static void testMiddleTransaction() throws SQLException {
		conn.setAutoCommit(false);
		stmt.executeUpdate("insert into pet(name,age) values('nameA', 30)");
		stmt.executeUpdate("insert into pet(name,age) values('nameB', 40)");
		stmt.executeUpdate("update pet set age=60 where age=30");
		conn.setAutoCommit(true);

		/*
		rs = stmt.executeQuery("select * from pet where age=60");
		//rs.getInt(1); //java.sql.SQLException: Before start of result set
		while(rs.next()) {
			System.out.println("id="+rs.getInt(1)+", name="+rs.getString(2)+", age="+rs.getInt(3));
		}
		stmt.executeUpdate("delete from pet where age=60");
		*/
	}

	static void initGetPropertyInfo() throws Exception {
		String url ="jdbc:mysql://localhost:3306/test";
		for(DriverPropertyInfo dpi : new com.mysql.jdbc.Driver().getPropertyInfo(url, null))
			System.out.println("name = "+dpi.name+ ", value="+dpi.value+", required="+dpi.required);
	}

	static void initConnection() throws Exception {
		initConnection("test");
	}

	static void initConnection(String db) throws Exception {
		String url = "jdbc:mysql://localhost:3306/"+db+"?key1=value1&key2=value2";
		//String url = null;
		//String url = "jdbc:mysql://localhost:3307,localhost:3308/test?key1=value1&key2=value2";
		//String url = "jdbc:mysql://localhost,127.0.0.1/test?key1=value1&key2=value2";
		//String url = "jdbc:mysql://localhost:3306/test?key1=value1&key2=value2";
		//String url = "jdbc:mysql:mxj://localhost:3306/test?key1=value1&key2=value2";
		//String url = "jdbc:mysql:invalidprefix://localhost:3306/test?key1=value1&key2=value2";
		//String url = "jdbc:mysql://localhost:3307/test?key1=value1&key2=value2";

		//String url = "jdbc:mysql:loadbalance://localhost:3306,127.0.0.1/test?key1=value1&key2=value2";

		//String url ="jdbc:mysql:replication://localhost:3306,127.0.0.1/test";

		Properties props = new Properties();
		//props.put("propertiesTransform", "my.NotFoundConnectionPropertiesTransform");

		props.put("user", "root");
		props.put("password", "test");

		//props.put("useCursorFetch", "true");
		//props.put("maxRows", "2");

		//props.put("exceptionInterceptors", "test.MyExceptionInterceptor2");
		//props.put("exceptionInterceptors", "test.MyExceptionInterceptor");
		///*
		//props.put("password", "wrong password");
		//props.put("holdResultsOpenOverStatementClose", "true");
		//props.put("allowMultiQueries","true");

		//鎵归噺sql鐨勬潯鏁板ぇ浜�涓攔ewriteBatchedStatements涓簍rue鏃跺彲浠ユ祴璇昬xecuteBatchUsingMultiQueries
		//杩欑瓑浠蜂簬鎶奱llowMultiQueries璁句负true
		//props.put("rewriteBatchedStatements","true");

		//鍦–onnectionPropertiesImpl涓娇鐢ㄨ繖涓弬鏁版潵鎺㈡祴鏈嶅姟鍣ㄧ鐨刾repared statement
		//props.put("useCursorFetch", "true");
		//props.put("useServerPrepStmts", "true");
		//props.put("cachePrepStmts", "true");

		

		//props.put("logger", "com.mysql.jdbc.log.Log4JLogger");
		
		//props.put("logger", "com.mysql.jdbc.log.NullLogger");
		//props.put("exceptionInterceptors", "test.MyExceptionInterceptor");

		//props.put("cachePrepStmts", "true");

		//props.put("sessionVariables", "wait_timeout=10,warning_count=100");
		//props.put("sessionVariables", "@myvar='myvalue',wait_timeout=10,warning_count=100");
		//props.put("sessionVariables", "@myvar='myvalue',wait_timeout=10");

		//props.put("maxRows", "0");

		//props.put("allowMultiQueries", "true");
		//props.put("cacheResultSetMetadata", "true");

		//props.put("noDatetimeStringSync", "true");
		//props.put("useTimezone", "true");

		
		//props.put("resultSetScannerRegex", "\\.");
		//props.put("statementInterceptors", "com.mysql.jdbc.interceptors.ResultSetScannerInterceptor,com.mysql.jdbc.interceptors.ServerStatusDiffInterceptor,com.mysql.jdbc.interceptors.SessionAssociationInterceptor, java.lang.Object,NotStatementInterceptor");
		//props.put("statementInterceptors", "com.mysql.jdbc.interceptors.ResultSetScannerInterceptor,com.mysql.jdbc.interceptors.ServerStatusDiffInterceptor,com.mysql.jdbc.interceptors.SessionAssociationInterceptor, test.MyExtension");

		//props.put("statementInterceptors", "com.mysql.jdbc.interceptors.ResultSetScannerInterceptor,com.mysql.jdbc.interceptors.ServerStatusDiffInterceptor,com.mysql.jdbc.interceptors.SessionAssociationInterceptor,test.MyStatementInterceptor");

		//props.put("statementInterceptors", "test.MyStatementInterceptor");

		//鍙兘鏄�random", "bestResponseTime"
		//鍚﹀垯鍦–onnectionPropertiesImpl鐨剉alidateStringValues涓�涓嶈繃楠岃瘉
		//props.put("loadBalanceStrategy", "myloadBalanceStrategy");

		
		//鍙互鍚敤鏃ュ織
		//props.put("useUsageAdvisor", "true");

		//props.put("roundRobinLoadBalance", "true");

		//props.put("cacheServerConfiguration", "true");
		//props.put("useTimezone", "true");
		//props.put("characterEncoding", "GBK");

		//props.put("maxAllowedPacket", "20");

		//props.put("traceProtocol", "true");

		//props.put("enablePacketDebug", "true");

		props.put("autoGenerateTestcaseScript", "true");
		//props.put("profileSQL", "true");
		//props.put("logSlowQueries", "true");

		//props.put("useSSL", "true");
		//props.put("requireSSL", "true");

		//props.put("useCompression", "true");

		//props.put("createDatabaseIfNotExist", "true");

		//props.put("largeRowSizeThreshold", "10");
		//props.put("maxRows", "3");
		//*/

		//props.put("gatherPerfMetrics", "true");

		//props.put("useNanosForElapsedTime", "true");

		//props.put("maxAllowedPacket", "4194304"); //4*1024*1024 //鏈�ぇ鍏佽鐨勫寘涓�M
		//props.put("maxAllowedPacket", "20");

		//props.put("interactiveClient", "true");
		//props.put("useOldAliasMetadataBehavior", "true");

		conn = ConnectionUtil.getMySQLConnection(url, props);
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
		//MySQL鏈�ぇ鍙兘鍙�鍗冧竾鏉¤褰�
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
		rs = stmt.executeQuery("select * from pet");
		while(rs.next()) {
			System.out.println(rs.getString(1));
		}

		//StringBuffer s = new StringBuffer();
		//com.mysql.jdbc.MysqlDefs.appendJdbcTypeMappingQuery(s,"colName");
		//System.out.println(s);
	}

	//鍏堟妸useCursorFetch璁句负true
	static void testMysqlIO_getResultSet_usingCursor() throws SQLException {
		//ps = conn.prepareStatement("select * from pet");

		ps = conn.prepareStatement("select * from pet where age = 100");

		System.out.println("ps.getClass()="+ps.getClass());
		ps.setFetchSize(3);
		rs = ps.executeQuery();
		while(rs.next()) {
			System.out.println(rs.getString(1));
		}
	}

	static void testPreparedStatement() throws SQLException {
		ps = conn.prepareStatement("select * from pet /* a middle comment */ where id=? and name=? or age=10");

		//瑕佹妸asSql()鐨刾rotected鏀规垚public鎵嶈兘娴�
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

	static void testServerPreparedStatement() throws SQLException {
		//ps = conn.prepareStatement("insert into pet(age,name) values(?,?)");

		//瑕佹妸asSql()鐨刾rotected鏀规垚public鎵嶈兘娴�
		//System.out.println(((com.mysql.jdbc.PreparedStatement)ps).asSql());
		//System.out.println(((com.mysql.jdbc.PreparedStatement)ps).asSql(true));

		//ps.setInt(3, 10);
		//ps.setInt(3, 10);
		//ps.setString(2, "name20");
		//ps.addBatch();
		//ps.setInt(1, 20);
		//ps.setString(2, "name20");
		//ps.addBatch(); //涓�畾瑕佽寰楄皟鐢╝ddBatch()锛屽惁鍒欏墠闈㈢殑涓�潯璁板綍涓嶄細鎻掑叆(浣嗘槸涓嶄細鎶ラ敊)

		//System.out.println(((com.mysql.jdbc.PreparedStatement)ps).asSql());
		//System.out.println(((com.mysql.jdbc.PreparedStatement)ps).asSql(true));
		//ps.executeBatch();

		//StringBuffer s = new StringBuffer();
		//com.mysql.jdbc.MysqlDefs.appendJdbcTypeMappingQuery(s,"colName");
		//System.out.println(s);


		ps = conn.prepareStatement("select age,name from pet where age=? and name=?");
		ps.setInt(1, 20);
		ps.setString(2, "name20");
		//ps.addBatch();
		ps.executeQuery();
	}

	static void testDatabaseMetaData() throws SQLException {
		DatabaseMetaData dbmd = conn.getMetaData();
		rs = dbmd.getTableTypes();
		while(rs.next()) {
			System.out.println(rs.getString(1));
		}
	}

	static void testResultSetMetaData() throws SQLException {
		rs = stmt.executeQuery("select name, age as a from pet as b");
		ResultSetMetaData rsmd = rs.getMetaData();
		System.out.println(rsmd.getColumnCount());

		//System.out.println(rsmd.getCatalogName(3));
		//System.out.println(rsmd.getColumnName(3));
		//System.out.println(rsmd.getColumnName(0));
		System.out.println(rsmd.getColumnName(1));
		System.out.println(rsmd.getColumnName(2));
		System.out.println(rsmd.getTableName(1));

		System.out.println("rsmd="+rsmd);
	}

	static void testResultSetImpl() throws SQLException {
		stmt.executeUpdate("insert into pet(name,age) values('nameA', 30)");
		//stmt.executeUpdate("insert into pet(name,age) values('nameB', 30)");
		stmt.executeUpdate("update pet set age=60 where age=30");
		rs = stmt.executeQuery("select * from pet where age=60");
		//rs.getInt(1); //java.sql.SQLException: Before start of result set
		while(rs.next()) {
			System.out.println("id="+rs.getInt(1)+", name="+rs.getString(2)+", age="+rs.getInt(3));
		}
		stmt.executeUpdate("delete from pet where age=60");
	}

	static void testBlobResultSetImpl() throws SQLException {
		ps = conn.prepareStatement("select User,x509_issuer,max_connections from user", ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
		rs = ps.executeQuery();
		//rs.getInt(1); //java.sql.SQLException: Before start of result set
		while(rs.next()) {
			System.out.println("User="+rs.getString(1)+", x509_issuer="+rs.getBlob(2)+", max_connections="+rs.getInt(3));
		}
	}

	static String seed = "01234567890123456789";

	static void testUserPassword() throws Exception {
		Map<String, byte[]> user2password = new HashMap<String, byte[]>();
		rs = stmt.executeQuery("select user,password from mysql.user");
		
		byte[] password2Sha1= Security.sha1Sha1("test".getBytes("ASCII"));
		while (rs.next()) {
			String user = rs.getString(1);
			user2password.put(user, rs.getBytes(2));
			
			if(user.equalsIgnoreCase("root")) {
				byte[] reply = Security.scramble411("test",seed);
				//System.out.println("check411=" + check411(reply, user2password.get(user), seed.getBytes("ASCII")));
				System.out.println("check411=" + Security.check411(reply, password2Sha1, seed.getBytes("ASCII")));
			}
		}
		System.out.println("user2password=" + user2password);
	}


	static void testStatementCancel() throws SQLException {
		//stmt.setMaxFieldSize(-1);
		//stmt.setMaxFieldSize(123456789);
		System.out.println("stmt=" + stmt.getClass().getName());
		stmt.executeQuery("select * from pet");
		stmt.cancel();
	}

	static void testStreamingResults() throws SQLException {
		stmt.setFetchSize(Integer.MIN_VALUE); //鍙樻垚浜哠treamingResults
		stmt.execute("select * from pet");
	}
}