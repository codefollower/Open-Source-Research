package com.mydomain.data;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.ibatis.common.resources.Resources;
import com.mydomain.domain.Account;

import java.io.Reader;
import java.io.IOException;
import java.util.List;
import java.sql.SQLException;

/**
 * This is not a best practices class. It's just an example to give you an idea
 * of how iBATIS works. For a more complete example, see JPetStore 5.0 at
 * http://www.ibatis.com.
 */
public class IbatisTest {

    /**
     * SqlMapClient instances are thread safe, so you only need one. In this
     * case, we'll use a static singleton. So sue me. ;-)
     */
    private static SqlMapClient sqlMapper;

    // private static SqlMapSession sqlMapper;
    /**
     * It's not a good idea to put code that can fail in a class initializer,
     * but for sake of argument, here's how you configure an SQL Map.
     */
    static {
        try {
            java.util.Properties p = new java.util.Properties();
            p.put("myvar1", "myvalue1");
            p.put("myvar2", "myvalue2");

            Reader reader = Resources.getResourceAsReader("com/mydomain/data/SqlMapConfig.xml");
            sqlMapper = SqlMapClientBuilder.buildSqlMapClient(reader);
            // sqlMapper2 = SqlMapClientBuilder.buildSqlMapClient(reader, p);
            System.out.println("sqlMapper=" + sqlMapper);
            reader.close();

        } catch (IOException e) {
            // Fail fast.
            throw new RuntimeException("Something bad happened while building the SqlMapClient instance." + e, e);
        }
    }

    public static List<?> selectAllAccounts() throws SQLException {
        return sqlMapper.queryForList("selectAllAccounts");
    }

    public static Account selectAccountById(int id) throws SQLException {
        return (Account) sqlMapper.queryForObject("selectAccountById", id);
    }

    public static void insertAccount(Account account) throws SQLException {
        sqlMapper.insert("insertAccount", account);
    }

    public static void updateAccount(Account account) throws SQLException {
        sqlMapper.update("updateAccount", account);
    }

    public static void deleteAccount(int id) throws SQLException {
        sqlMapper.delete("deleteAccountById", id);
    }

    public static void insertAccount(SqlMapClient sqlMapper, Account account) throws SQLException {
        sqlMapper.insert("insertAccount", account);
    }

    public static List<?> selectAllAccounts(SqlMapClient sqlMapper) throws SQLException {
        return sqlMapper.queryForList("selectAllAccounts");
    }

    public static Account selectAccountById(SqlMapClient sqlMapper, int id) throws SQLException {
        return (Account) sqlMapper.queryForObject("selectAccountById", id);
    }

    public static void main(String[] args) throws Throwable {

        // System.out.println("selectAccountById(10)="+selectAccountById(10));
        Account account = new Account();
        account.setId(2);
        account.setFirstName("a");
        account.setLastName("b");
        account.setEmailAddress("c");

        insertAccount(sqlMapper, account);

        //
        //
        // List list = selectAllAccounts(sqlMapper);
        // System.out.println(list.size());
        // System.out.println(list);
        //
        // list = selectAllAccounts(sqlMapper2);
        // System.out.println(list.size());
        // System.out.println(list);
        //
        //
        // System.out.println(selectAccountById(sqlMapper,222416));
        // System.out.println(selectAccountById(sqlMapper2,222416));
    }

}
