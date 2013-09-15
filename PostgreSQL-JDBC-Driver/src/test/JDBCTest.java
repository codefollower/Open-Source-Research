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
            //initConnection("postgres");

            initConnection();

            stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY,
                    ResultSet.CLOSE_CURSORS_AT_COMMIT);

            //testStatementExecuteQuery();
            //testPreparedStatementExecuteQuery();
            //testStatementExecuteUpdate();
            //testStatementExecute();
            //testStatementExecuteBatch();
            //testPreparedStatementExecuteBatch();
            //testCallableStatementExecute();

            //testNotification();

            //testStatementSetFetchSize();

            //testPreparedStatement_getParameterMetaData();

            testTimestamp();

            //testPreparedStatement_setBigDecimal();
            //testPreparedStatement_setBinaryStream();

            //stmt.setMaxRows(3);

            //testConnection();
            //testMysqlIO();
            //testMysqlIO_getResultSet_usingCursor();
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
        conn.setAutoCommit(false);
        //stmt.setQueryTimeout(1);
        //stmt.setFetchSize(Integer.MIN_VALUE);
        stmt.setFetchSize(2);
        rs = stmt.executeQuery("select * from pet");

        while (rs.next()) {
            System.out.println("id=" + rs.getInt(1) + ", name=" + rs.getString(2) + ", age=" + rs.getInt(3));
        }
        conn.commit();

        conn.setAutoCommit(true);
    }

    static void testStatementSetFetchSize() throws SQLException {
        //stmt.setFetchSize(1);//在这里不起作用
        stmt.setMaxRows(1);
        rs = stmt.executeQuery("select * from pet");

        while (rs.next()) {
            System.out.println("id=" + rs.getInt(1) + ", name=" + rs.getString(2) + ", age=" + rs.getInt(3));
        }
    }

    static void testPreparedStatementExecuteQuery() throws SQLException {
        conn.setAutoCommit(false);
        ps = conn.prepareStatement("select * from pet where name=? and age=?");
        ps.setString(1, "name1");
        ps.setInt(2, 1);

        /*
        ps = conn.prepareStatement("select * from pet where name=? and age=?;select * from pet where name=? and age=?");
        ps.setString(1, "name1");
        ps.setInt(2, 1);

        ps.setString(3, "name1");
        ps.setInt(4, 1);
        */

        ps.setFetchSize(2);
        rs = ps.executeQuery();
        while (rs.next()) {
            System.out.println("id=" + rs.getInt(1) + ", name=" + rs.getString(2) + ", age=" + rs.getInt(3));
        }
        conn.commit();

        conn.setAutoCommit(true);
    }

    static void testPreparedStatement_getParameterMetaData() throws SQLException {
        ps = conn.prepareStatement("select * from pet where name=? and age=?");
        ps.getParameterMetaData();
    }

    static void testNotification() throws Exception {
        NotificationTest.main(new String[0]);
    }

    static void testStatementExecuteUpdate() throws SQLException {
        //stmt.setQueryTimeout(1); //没有实现这个方法

        /*
        stmt.setFetchSize(Integer.MIN_VALUE);
        //stmt.setFetchSize(2);
        rs = stmt.executeQuery("select * from pet");

        while(rs.next()) {
        	System.out.println("id="+rs.getInt(1)+", name="+rs.getString(2)+", age="+rs.getInt(3));
        }
        */

        //不支持列索引
        //stmt.executeUpdate("insert into pet(name,age) values('name1', 1000)", new int[] {1});
        //stmt.executeUpdate("insert into pet(name,age) values('name1', 1000)", new String[] {"id","name","age"});

        //stmt.executeUpdate("insert into pet(name,age) values('name1', 1000)");

        stmt.executeUpdate("insert into pet(name,age) values('name1', 1000)", Statement.RETURN_GENERATED_KEYS);

        conn.setCatalog("postgres");
        stmt.executeUpdate("delete from pet where age=1000");

        //stmt.executeUpdate("select * from pet");
    }

    static void testTimestamp() throws SQLException {
        long time1 = new java.util.Date().getTime();
        Timestamp ts1 = new Timestamp(time1);
        ps = conn.prepareStatement("insert into test(ts) values(?)");
        ps.setTimestamp(1, ts1);
        ps.executeUpdate();
        ps.close();

        ps = conn.prepareStatement("select ts from test", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = ps.executeQuery();
        rs.last();

        Timestamp ts2 = rs.getTimestamp(1);
        ;
        long time2 = ts2.getTime();

        System.out.println("time1=" + time1);
        System.out.println("time2=" + time2);
        System.out.println("time1==time2 =" + (time1 == time2));
        System.out.println("ts1=" + ts1);
        System.out.println("ts2=" + ts2);
        //stmt.executeUpdate("delete from test");
    }

    static void testStatementExecute() throws SQLException {
        stmt.setQueryTimeout(1);
        stmt.setFetchSize(Integer.MIN_VALUE);
        //stmt.setFetchSize(2);
        if (stmt.execute("select * from pet")) {
            rs = stmt.getResultSet();
            while (rs.next()) {
                System.out.println("id=" + rs.getInt(1) + ", name=" + rs.getString(2) + ", age=" + rs.getInt(3));
            }
        }

        stmt.execute("insert into pet(name,age) values('name1', 1000)", new int[] { 1 });

        System.out.println("getUpdateCount()=" + stmt.getUpdateCount());
        stmt.execute("delete from pet where age=1000");

        System.out.println("getUpdateCount()=" + stmt.getUpdateCount());
    }

    static void testStatementExecuteBatch() throws SQLException {
        conn.setAutoCommit(false);

        long t1 = System.currentTimeMillis();
        //stmt.setQueryTimeout(1);
        //stmt.setFetchSize(Integer.MIN_VALUE);

        stmt.addBatch("insert into pet(name,age) values('name1', 1000)");
        stmt.addBatch("insert into pet(name,age) values('name1', 1000)");
        stmt.addBatch("insert into pet(name,age) values('name1', 1000)");
        stmt.addBatch("insert into pet(name,age) values('name1', 1000)");

        //stmt.addBatch("select * from pet");//不能有select语句

        stmt.addBatch("delete from pet where age=1000");

        int[] updates = stmt.executeBatch();
        for (int updateCount : updates)
            System.out.println("updateCount=" + updateCount);
        conn.commit();

        conn.setAutoCommit(true);

        System.err.println("StatementExecuteBatch :" + (System.currentTimeMillis() - t1));

    }

    static void testPreparedStatementExecuteBatch() throws SQLException {
        conn.setAutoCommit(false);
        long t1 = System.currentTimeMillis();

        ps = conn.prepareStatement("insert into pet(name,age) values(?,?)");
        ps.setString(1, "name1");
        ps.setInt(2, 1000);
        ps.addBatch();
        ps.addBatch();
        ps.addBatch();
        ps.addBatch();
        //ps.addBatch("delete from pet where age=1000");

        int[] updates = ps.executeBatch();
        for (int updateCount : updates)
            System.out.println("updateCount=" + updateCount);

        System.out.println("updateCount=" + stmt.executeUpdate("delete from pet where age=1000"));
        conn.commit();

        conn.setAutoCommit(true);

        System.err.println("PreparedStatementExecuteBatch :" + (System.currentTimeMillis() - t1));
    }

    static void testCallableStatementExecute() throws SQLException {
        CallableStatement upperProc = conn.prepareCall("{ ? = call upper( ? ) }");
        upperProc.registerOutParameter(1, Types.VARCHAR);
        upperProc.setString(2, "lowercase to uppercase");
        upperProc.execute();
        String upperCased = upperProc.getString(1);
        upperProc.close();
        System.err.println("upperCased :" + upperCased);
    }

    static void testPreparedStatement_setBigDecimal() throws SQLException {
        ps = conn.prepareStatement("select * from pet where id=?");
        ps.setBigDecimal(1, new BigDecimal(1000));
        ps.setBigDecimal(1, new BigDecimal("1.23E3"));
        ps.executeQuery();
    }

    static void testPreparedStatement_setBinaryStream() throws SQLException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[] { (byte) 1, (byte) 2 });
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
    }

    static void initConnection() throws Exception {
        initConnection("test");
    }

    static void initConnection(String db) throws Exception {
        String driver = "org.postgresql.Driver";
        String url = "jdbc:postgresql://localhost/" + db;

        Properties props = new Properties();
        props.put("user", "postgres");
        props.put("password", "post");

        conn = ConnectionUtil.getConnection(driver, url, props);
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
        System.out.println("getResultSetType()=" + stmt.getResultSetType());

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
    }

    //先把useCursorFetch设为true
    static void testMysqlIO_getResultSet_usingCursor() throws SQLException {
        //ps = conn.prepareStatement("select * from pet");

        ps = conn.prepareStatement("select * from pet where age = 100");

        System.out.println("ps.getClass()=" + ps.getClass());
        ps.setFetchSize(3);
        rs = ps.executeQuery();
        while (rs.next()) {
            System.out.println(rs.getString(1));
        }
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

    static void testServerPreparedStatement() throws SQLException {
        //ps = conn.prepareStatement("insert into pet(age,name) values(?,?)");

        //要把asSql()的protected改成public才能测
        //System.out.println(((com.mysql.jdbc.PreparedStatement)ps).asSql());
        //System.out.println(((com.mysql.jdbc.PreparedStatement)ps).asSql(true));

        //ps.setInt(3, 10);
        //ps.setInt(3, 10);
        //ps.setString(2, "name20");
        //ps.addBatch();
        //ps.setInt(1, 20);
        //ps.setString(2, "name20");
        //ps.addBatch(); //一定要记得调用addBatch()，否则前面的一条记录不会插入(但是不会报错)

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
        while (rs.next()) {
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

        System.out.println("rsmd=" + rsmd);
    }

    static void testResultSetImpl() throws SQLException {
        stmt.executeUpdate("insert into pet(name,age) values('nameA', 30)");
        //stmt.executeUpdate("insert into pet(name,age) values('nameB', 30)");
        stmt.executeUpdate("update pet set age=60 where age=30");
        rs = stmt.executeQuery("select * from pet where age=60");
        //rs.getInt(1); //java.sql.SQLException: Before start of result set
        while (rs.next()) {
            System.out.println("id=" + rs.getInt(1) + ", name=" + rs.getString(2) + ", age=" + rs.getInt(3));
        }
        stmt.executeUpdate("delete from pet where age=60");
    }

    static void testBlobResultSetImpl() throws SQLException {
        ps = conn.prepareStatement("select User,x509_issuer,max_connections from user", ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_UPDATABLE);
        rs = ps.executeQuery();
        //rs.getInt(1); //java.sql.SQLException: Before start of result set
        while (rs.next()) {
            System.out
                    .println("User=" + rs.getString(1) + ", x509_issuer=" + rs.getBlob(2) + ", max_connections=" + rs.getInt(3));
        }
    }

    static void testStatementCancel() throws SQLException {
        //stmt.setMaxFieldSize(-1);
        //stmt.setMaxFieldSize(123456789);
        System.out.println("stmt=" + stmt.getClass().getName());
        stmt.executeQuery("select * from pet");
        stmt.cancel();
    }

    static void testStreamingResults() throws SQLException {
        stmt.setFetchSize(Integer.MIN_VALUE); //变成了StreamingResults
        stmt.execute("select * from pet");
    }
}