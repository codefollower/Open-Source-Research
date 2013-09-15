package test;

import java.sql.*;
import java.util.*;

public class PreparedStatementTest {
    private static Connection getMySQLConnection() throws Exception {
        return getMySQLConnection(true);
    }

    private static Connection getMySQLConnection(boolean autoCommit) throws Exception {
        String driver = "org.postgresql.Driver";
        String url = "jdbc:postgresql://localhost/test";
        //String url = "jdbc:postgresql://[::1]:5432/test";

        Properties info = new Properties();
        info.put("user", "postgres");
        info.put("password", "post");

        //info.put("loglevel", "2");

        Class.forName(driver);

        Connection conn = DriverManager.getConnection(url, info);
        conn.setAutoCommit(autoCommit);
        return conn;
    }

    public static void sqlException(SQLException e) {
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

    static void print(int[] array) {
        if (array == null)
            System.err.println("array=null");

        System.err.println("array.length=" + array.length);
        for (int i : array) {
            System.err.println("i=" + i);
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            //getMySQLConnection();
            PreparedStatement ps = null;
            //System.out.println("(8.4.compareTo(8.0) >= 0)="+("8.4".compareTo("8.0") >= 0));
            Connection conn = getMySQLConnection();

            /*

            LargeObjectManager lobj = ((org.postgresql.PGConnection)conn).getLargeObjectAPI();

            //long oid = lobj.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);

            //conn.createClob();
            //conn.setClientInfo("myname","myvalue");
            //conn.createArrayOf("myname",null);
            //conn.setSavepoint();

            ps = conn.prepareStatement("insert into weather(city,temp_lo) values('aa',10)",
            	Statement.RETURN_GENERATED_KEYS);
            ps.close();

            ps = conn.prepareStatement("insert into weather(city,temp_lo) values('aa',10)",
            	new String[]{"city","temp_lo"});
            ps.close();

            //不支持
            //ps = conn.prepareStatement("insert into weather(city,temp_lo) values('aa',10)",
            //	new int[]{1,2});
            //ps.close();
            */
            conn.setAutoCommit(false);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("insert into weather(city,temp_lo) values('aa',10)");

            conn.commit();

            conn.setAutoCommit(true);
            stmt.close();

            ///*

            // conn.close();
            // String sql = null;
            // String sql =
            // "select * from pet where name = ?  ON DUPLICATE KEY UPDATE ";
            //String sql = "select * from weather where temp_lo = ? and temp_hi = ?";
            //String sql = "select * from weather where temp_lo = ?";

            String sql = "select * from weather where city = ? and temp_hi = ?";
            ps = conn.prepareStatement(sql);
            System.err.println(ps.getClass().getName());

            //stmt.setQueryTimeout(9);

            long t1 = System.currentTimeMillis();

            ps.setString(1, "aa");
            ps.setInt(2, 10);
            //ps.setInt(2, 200);
            ResultSet rs = ps.executeQuery();
            rs.next();
            rs.close();

            System.err.println(System.currentTimeMillis() - t1);

            ps.close();

            stmt = conn.createStatement();
            stmt.executeUpdate("insert into weather(city,temp_lo) values('aa',10);insert into weather(city,temp_lo) values('aa',10)");

            stmt.close();

            //*/
            conn.close();
        } catch (SQLException e) {
            sqlException(e);
            e.printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}