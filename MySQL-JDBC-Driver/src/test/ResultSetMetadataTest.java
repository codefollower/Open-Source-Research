package test;

import java.sql.*;
import java.util.*;

public class ResultSetMetadataTest {

    public static void main(String[] args) throws Exception {
        try {
            String driver = "com.mysql.jdbc.Driver";
            String url = "jdbc:mysql://localhost/mysql";

            Properties info = new Properties();

            //info.put("user", "root");
            //info.put("password", "test");

            if ("true".equals(args[1])) {
                //info.put("useServerPrepStmts", "true");
                info.put("cacheResultSetMetadata", "true");
            }

            Class.forName(driver);

            Connection conn = DriverManager.getConnection(url, info);

            int loopCount = Integer.parseInt(args[0]);

            long t1 = System.currentTimeMillis();

            for (int i = 0; i < loopCount; i++) {

                //String sql = "select * from pet where age=? or name=?";
                String sql = "select * from user";
                PreparedStatement stmt = conn.prepareStatement(sql);
                //System.err.println("MySQL的实现类: " + stmt.getClass().getName());

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    rs.getString("User");
                    rs.getInt("max_connections");
                }

                //stmt.setInt(1, i);
                //stmt.setString(2, i + "");
                //stmt.executeQuery();
                rs.close();
                stmt.close();
            }

            System.err.println("查询: " + loopCount + " 次，用了 " + (System.currentTimeMillis() - t1) + " 毫秒");

            //stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}