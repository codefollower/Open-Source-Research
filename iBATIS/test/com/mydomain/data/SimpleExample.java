package com.mydomain.data;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.List;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;
import com.ibatis.sqlmap.engine.transaction.TransactionConfig;
import com.ibatis.sqlmap.engine.transaction.TransactionManager;
import com.ibatis.sqlmap.engine.transaction.external.ExternalTransactionConfig;
import com.mydomain.domain.Account;

/**
 * This is not a best practices class.  It's just an example
 * to give you an idea of how iBATIS works.  For a more complete
 * example, see JPetStore 5.0 at http://www.ibatis.com.
 */
@SuppressWarnings("deprecation")
public class SimpleExample {

    /**
     * SqlMapClient instances are thread safe, so you only need one.
     * In this case, we'll use a static singleton.  So sue me.  ;-)
     */
    private static SqlMapClient sqlMapper;

    //private static SqlMapSession sqlMapper;

    private static SqlMapClient sqlMapper2;

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
            //sqlMapper2 = SqlMapClientBuilder.buildSqlMapClient(reader, p);
            System.out.println("sqlMapper=" + sqlMapper);
            reader.close();

            reader = Resources.getResourceAsReader("com/mydomain/data/SqlMapConfig.xml");
            sqlMapper2 = SqlMapClientBuilder.buildSqlMapClient(reader);
            //sqlMapper2 = SqlMapClientBuilder.buildSqlMapClient(reader, p);
            System.out.println("sqlMapper2=" + sqlMapper2);

            //sqlMapper = sqlMapper2.openSession(ConnectionUtil.getMySQLConnection());
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
        //sqlMapper.delete("deleteAccount", id);//�����Ǵ���ģ�û��deleteAccount
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

        Class<ExternalTransactionConfig> transactionConfigClass = ExternalTransactionConfig.class;

        TransactionConfig transactionConfig = (TransactionConfig) transactionConfigClass.newInstance();

        transactionConfig.setDataSource(DataSourceFactory.getDataSource());

        ((ExternalTransactionConfig) transactionConfig).setDefaultAutoCommit(true);

        ExtendedSqlMapClient extendedClient = (ExtendedSqlMapClient) sqlMapper;
        transactionConfig.setMaximumConcurrentTransactions(extendedClient.getDelegate().getMaxTransactions());
        extendedClient.getDelegate().setTxManager(new TransactionManager(transactionConfig));

        //sqlMapper = extendedClient;

        transactionConfig = (TransactionConfig) transactionConfigClass.newInstance();

        transactionConfig.setDataSource(DataSourceFactory.getDataSource());

        ((ExternalTransactionConfig) transactionConfig).setDefaultAutoCommit(true);

        extendedClient = (ExtendedSqlMapClient) sqlMapper2;
        transactionConfig.setMaximumConcurrentTransactions(extendedClient.getDelegate().getMaxTransactions());
        extendedClient.getDelegate().setTxManager(new TransactionManager(transactionConfig));

        //sqlMapper2 = extendedClient;

        //*/

        //List list = selectAllAccounts();
        // System.out.println(list.size());
        // System.out.println(list);

        //System.out.println("selectAccountById(10)="+selectAccountById(10));
        Account account = new Account();
        account.setId(2);
        account.setFirstName("a");
        account.setLastName("b");
        account.setEmailAddress("c");

        /*
        insertAccount(account);
        list = selectAllAccounts();
        System.out.println(list.size());
        System.out.println(list);

        account.setEmailAddress("c2");
        updateAccount(account);

        //deleteAccount(2);

        account = selectAccountById(2);
        System.out.println(account);
        */

        // insertAccount(sqlMapper, account);
        //  insertAccount(sqlMapper2, account);

        //account = selectAccountById(2);
        //System.out.println(account);

        insertAccount(sqlMapper, account);
        insertAccount(sqlMapper2, account);

        List<?> list = selectAllAccounts(sqlMapper);
        System.out.println(list.size());
        System.out.println(list);

        list = selectAllAccounts(sqlMapper2);
        System.out.println(list.size());
        System.out.println(list);

        System.out.println(selectAccountById(sqlMapper, 222416));
        System.out.println(selectAccountById(sqlMapper2, 222416));
    }

}
