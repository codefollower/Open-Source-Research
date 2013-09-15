///*
// *  Copyright 2004 Clinton Begin
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// */
//package com.ibatis.common.jdbc;
//
//import com.ibatis.common.beans.ClassInfo;
//
//import com.ibatis.common.resources.Resources;
//import com.ibatis.common.logging.LogFactory;
//import com.ibatis.common.logging.Log;
//
//import javax.sql.DataSource;
//import java.io.PrintWriter;
//import java.lang.reflect.InvocationHandler;
//import java.lang.reflect.Method;
//import java.lang.reflect.Proxy;
//import java.sql.*;
//import java.util.*;
//
///**
// * This is a simple, synchronous, thread-safe database connection pool.
// * <p/>
// * REQUIRED PROPERTIES
// * -------------------
// * JDBC.Driver
// * JDBC.ConnectionURL
// * JDBC.Username
// * JDBC.Password
// * <p/>
// * Pool.MaximumActiveConnections
// * Pool.MaximumIdleConnections
// * Pool.MaximumCheckoutTime
// * Pool.TimeToWait
// * Pool.PingQuery
// * Pool.PingEnabled
// * Pool.PingConnectionsOlderThan
// * Pool.PingConnectionsNotUsedFor
// * Pool.QuietMode
// */
//public class SimpleDataSource222 implements DataSource {
//	
//	//我加上的，不然在jdk1.6中编译通不过
//	public boolean isWrapperFor(Class<?> iface) { return true; }
//	public <T> T unwrap(Class<T> iface) { return null; }
//
//
//  private static final Log log = LogFactory.getLog(SimpleDataSource.class);
//
//  // Required Properties
//  private static final String PROP_JDBC_DRIVER = "JDBC.Driver";
//  private static final String PROP_JDBC_URL = "JDBC.ConnectionURL";
//  private static final String PROP_JDBC_USERNAME = "JDBC.Username";
//  private static final String PROP_JDBC_PASSWORD = "JDBC.Password";
//  private static final String PROP_JDBC_DEFAULT_AUTOCOMMIT = "JDBC.DefaultAutoCommit";
//
//  // Optional Properties
//  private static final String PROP_POOL_MAX_ACTIVE_CONN = "Pool.MaximumActiveConnections";
//  private static final String PROP_POOL_MAX_IDLE_CONN = "Pool.MaximumIdleConnections";
//  private static final String PROP_POOL_MAX_CHECKOUT_TIME = "Pool.MaximumCheckoutTime";
//  private static final String PROP_POOL_TIME_TO_WAIT = "Pool.TimeToWait";
//  private static final String PROP_POOL_PING_QUERY = "Pool.PingQuery";
//  private static final String PROP_POOL_PING_CONN_OLDER_THAN = "Pool.PingConnectionsOlderThan";
//  private static final String PROP_POOL_PING_ENABLED = "Pool.PingEnabled";
//  private static final String PROP_POOL_PING_CONN_NOT_USED_FOR = "Pool.PingConnectionsNotUsedFor";
//  private int expectedConnectionTypeCode;
//  // Additional Driver Properties prefix
//  private static final String ADD_DRIVER_PROPS_PREFIX = "Driver.";
//  private static final int ADD_DRIVER_PROPS_PREFIX_LENGTH = ADD_DRIVER_PROPS_PREFIX.length();
//
//  // ----- BEGIN: FIELDS LOCKED BY POOL_LOCK -----
//  private final Object POOL_LOCK = new Object();
//  private List idleConnections = new ArrayList();
//  private List activeConnections = new ArrayList();
//  private long requestCount = 0;
//  private long accumulatedRequestTime = 0;
//  private long accumulatedCheckoutTime = 0;
//  private long claimedOverdueConnectionCount = 0;
//  private long accumulatedCheckoutTimeOfOverdueConnections = 0;
//  private long accumulatedWaitTime = 0;
//  private long hadToWaitCount = 0;
//  private long badConnectionCount = 0;
//  // ----- END: FIELDS LOCKED BY POOL_LOCK -----
//
//  // ----- BEGIN: PROPERTY FIELDS FOR CONFIGURATION -----
//  private String jdbcDriver;
//  private String jdbcUrl;
//  private String jdbcUsername;
//  private String jdbcPassword;
//  private boolean jdbcDefaultAutoCommit;
//  private Properties driverProps;
//  private boolean useDriverProps;
//
//  private int poolMaximumActiveConnections;
//  private int poolMaximumIdleConnections;
//  private int poolMaximumCheckoutTime;
//  private int poolTimeToWait;
//  private String poolPingQuery;
//  private boolean poolPingEnabled;
//  private int poolPingConnectionsOlderThan;
//  private int poolPingConnectionsNotUsedFor;
//  //----- END: PROPERTY FIELDS FOR CONFIGURATION -----
//
//  /**
//   * Constructor to allow passing in a map of properties for configuration
//   *
//   * @param props - the configuration parameters
//   */
//  public SimpleDataSource(Map props) {
//    initialize(props);
//  }
//
//  private void initialize(Map props) {
//    try {
//      String prop_pool_ping_query = null;
//    	
//      if (props == null) {
//        throw new RuntimeException("SimpleDataSource: The properties map passed to the initializer was null.");
//      }
//
//      if (!(props.containsKey(PROP_JDBC_DRIVER)
//          && props.containsKey(PROP_JDBC_URL)
//          && props.containsKey(PROP_JDBC_USERNAME)
//          && props.containsKey(PROP_JDBC_PASSWORD))) {
//        throw new RuntimeException("SimpleDataSource: Some properties were not set.");
//      } else {
//
//        jdbcDriver = (String) props.get(PROP_JDBC_DRIVER);
//        jdbcUrl = (String) props.get(PROP_JDBC_URL);
//        jdbcUsername = (String) props.get(PROP_JDBC_USERNAME);
//        jdbcPassword = (String) props.get(PROP_JDBC_PASSWORD);
//
//        poolMaximumActiveConnections =
//            props.containsKey(PROP_POOL_MAX_ACTIVE_CONN)
//            ? Integer.parseInt((String) props.get(PROP_POOL_MAX_ACTIVE_CONN))
//            : 10;
//
//        poolMaximumIdleConnections =
//            props.containsKey(PROP_POOL_MAX_IDLE_CONN)
//            ? Integer.parseInt((String) props.get(PROP_POOL_MAX_IDLE_CONN))
//            : 5;
//
//        poolMaximumCheckoutTime =
//            props.containsKey(PROP_POOL_MAX_CHECKOUT_TIME)
//            ? Integer.parseInt((String) props.get(PROP_POOL_MAX_CHECKOUT_TIME))
//            : 20000;
//
//        poolTimeToWait =
//            props.containsKey(PROP_POOL_TIME_TO_WAIT)
//            ? Integer.parseInt((String) props.get(PROP_POOL_TIME_TO_WAIT))
//            : 20000;
//
//        poolPingEnabled =
//            props.containsKey(PROP_POOL_PING_ENABLED)
//                && Boolean.valueOf((String) props.get(PROP_POOL_PING_ENABLED)).booleanValue();
//
//        prop_pool_ping_query = (String) props.get(PROP_POOL_PING_QUERY); 
//        poolPingQuery =
//            props.containsKey(PROP_POOL_PING_QUERY)
//            ? prop_pool_ping_query 
//            : "NO PING QUERY SET";
//
//        poolPingConnectionsOlderThan =
//            props.containsKey(PROP_POOL_PING_CONN_OLDER_THAN)
//            ? Integer.parseInt((String) props.get(PROP_POOL_PING_CONN_OLDER_THAN))
//            : 0;
//
//        poolPingConnectionsNotUsedFor =
//            props.containsKey(PROP_POOL_PING_CONN_NOT_USED_FOR)
//            ? Integer.parseInt((String) props.get(PROP_POOL_PING_CONN_NOT_USED_FOR))
//            : 0;
//
//        jdbcDefaultAutoCommit =
//            props.containsKey(PROP_JDBC_DEFAULT_AUTOCOMMIT)
//                && Boolean.valueOf((String) props.get(PROP_JDBC_DEFAULT_AUTOCOMMIT)).booleanValue();
//
//        useDriverProps = false;
//        Iterator propIter = props.keySet().iterator();
//        driverProps = new Properties();
//        driverProps.put("user", jdbcUsername);
//        driverProps.put("password", jdbcPassword);
//        while (propIter.hasNext()) {
//          String name = (String) propIter.next();
//          String value = (String) props.get(name);
//          if (name.startsWith(ADD_DRIVER_PROPS_PREFIX)) {
//            driverProps.put(name.substring(ADD_DRIVER_PROPS_PREFIX_LENGTH), value);
//            useDriverProps = true;
//          }
//        }
//
//        expectedConnectionTypeCode = assembleConnectionTypeCode(jdbcUrl, jdbcUsername, jdbcPassword);
//
//        Resources.instantiate(jdbcDriver);
//        
//        if ( poolPingEnabled && (!props.containsKey(PROP_POOL_PING_QUERY) ||
//        		prop_pool_ping_query.trim().length() == 0) ) {
//          throw new RuntimeException("SimpleDataSource: property '" + PROP_POOL_PING_ENABLED + "' is true, but property '" +
//                                           PROP_POOL_PING_QUERY + "' is not set correctly.");
//        }        
//      }
//
//    } catch (Exception e) {
//      log.error("SimpleDataSource: Error while loading properties. Cause: " + e.toString(), e);
//      throw new RuntimeException("SimpleDataSource: Error while loading properties. Cause: " + e, e);
//    }
//  }
//
//  private int assembleConnectionTypeCode(String url, String username, String password) {
//    return ("" + url + username + password).hashCode();
//  }
//
//  /**
//   * @see javax.sql.DataSource#getConnection()
//   */
//  public Connection getConnection() throws SQLException {
//    return popConnection(jdbcUsername, jdbcPassword).getProxyConnection();
//  }
//
//  /**
//   * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
//   */
//  public Connection getConnection(String username, String password) throws SQLException {
//    return popConnection(username, password).getProxyConnection();
//  }
//
//  /**
//   * @see javax.sql.DataSource#setLoginTimeout(int)
//   */
//  public void setLoginTimeout(int loginTimeout) throws SQLException {
//    DriverManager.setLoginTimeout(loginTimeout);
//  }
//
//  /**
//   * @see javax.sql.DataSource#getLoginTimeout()
//   */
//  public int getLoginTimeout() throws SQLException {
//    return DriverManager.getLoginTimeout();
//  }
//
//  /**
//   * @see javax.sql.DataSource#setLogWriter(java.io.PrintWriter)
//   */
//  public void setLogWriter(PrintWriter logWriter) throws SQLException {
//    DriverManager.setLogWriter(logWriter);
//  }
//
//  /**
//   * @see javax.sql.DataSource#getLogWriter()
//   */
//  public PrintWriter getLogWriter() throws SQLException {
//    return DriverManager.getLogWriter();
//  }
//
//  /**
//   * If a connection has not been used in this many milliseconds, ping the
//   * database to make sure the connection is still good.
//   *
//   * @return the number of milliseconds of inactivity that will trigger a ping
//   */
//  public int getPoolPingConnectionsNotUsedFor() {
//    return poolPingConnectionsNotUsedFor;
//  }
//
//  /**
//   * Getter for the name of the JDBC driver class used
//   * @return The name of the class
//   */
//  public String getJdbcDriver() {
//    return jdbcDriver;
//  }
//
//  /**
//   * Getter of the JDBC URL used
//   * @return The JDBC URL
//   */
//  public String getJdbcUrl() {
//    return jdbcUrl;
//  }
//
//  /**
//   * Getter for the JDBC user name used
//   * @return The user name
//   */
//  public String getJdbcUsername() {
//    return jdbcUsername;
//  }
//
//  /**
//   * Getter for the JDBC password used
//   * @return The password
//   */
//  public String getJdbcPassword() {
//    return jdbcPassword;
//  }
//
//  /**
//   * Getter for the maximum number of active connections
//   * @return The maximum number of active connections
//   */
//  public int getPoolMaximumActiveConnections() {
//    return poolMaximumActiveConnections;
//  }
//
//  /**
//   * Getter for the maximum number of idle connections
//   * @return The maximum number of idle connections
//   */
//  public int getPoolMaximumIdleConnections() {
//    return poolMaximumIdleConnections;
//  }
//
//  /**
//   * Getter for the maximum time a connection can be used before it *may* be
//   * given away again.
//   * @return The maximum time
//   */
//  public int getPoolMaximumCheckoutTime() {
//    return poolMaximumCheckoutTime;
//  }
//
//  /**
//   * Getter for the time to wait before retrying to get a connection
//   * @return The time to wait
//   */
//  public int getPoolTimeToWait() {
//    return poolTimeToWait;
//  }
//
//  /**
//   * Getter for the query to be used to check a connection
//   * @return The query
//   */
//  public String getPoolPingQuery() {
//    return poolPingQuery;
//  }
//
//  /**
//   * Getter to tell if we should use the ping query
//   * @return True if we need to check a connection before using it
//   */
//  public boolean isPoolPingEnabled() {
//    return poolPingEnabled;
//  }
//
//  /**
//   * Getter for the age of connections that should be pinged before using
//   * @return The age
//   */
//  public int getPoolPingConnectionsOlderThan() {
//    return poolPingConnectionsOlderThan;
//  }
//
//  private int getExpectedConnectionTypeCode() {
//    return expectedConnectionTypeCode;
//  }
//
//  /**
//   * Getter for the number of connection requests made
//   * @return The number of connection requests made
//   */
//  public long getRequestCount() {
//    synchronized (POOL_LOCK) {
//      return requestCount;
//    }
//  }
//
//  /**
//   * Getter for the average time required to get a connection to the database
//   * @return The average time
//   */
//  public long getAverageRequestTime() {
//    synchronized (POOL_LOCK) {
//      return requestCount == 0 ? 0 : accumulatedRequestTime / requestCount;
//    }
//  }
//
//  /**
//   * Getter for the average time spent waiting for connections that were in use
//   * @return The average time
//   */
//  public long getAverageWaitTime() {
//    synchronized (POOL_LOCK) {
//      return hadToWaitCount == 0 ? 0 : accumulatedWaitTime / hadToWaitCount;
//    }
//  }
//
//  /**
//   * Getter for the number of requests that had to wait for connections that were in use
//   * @return The number of requests that had to wait
//   */
//  public long getHadToWaitCount() {
//    synchronized (POOL_LOCK) {
//      return hadToWaitCount;
//    }
//  }
//
//  /**
//   * Getter for the number of invalid connections that were found in the pool
//   * @return The number of invalid connections
//   */
//  public long getBadConnectionCount() {
//    synchronized (POOL_LOCK) {
//      return badConnectionCount;
//    }
//  }
//
//  /**
//   * Getter for the number of connections that were claimed before they were returned
//   * @return The number of connections
//   */
//  public long getClaimedOverdueConnectionCount() {
//    synchronized (POOL_LOCK) {
//      return claimedOverdueConnectionCount;
//    }
//  }
//
//  /**
//   * Getter for the average age of overdue connections
//   * @return The average age
//   */
//  public long getAverageOverdueCheckoutTime() {
//    synchronized (POOL_LOCK) {
//      return claimedOverdueConnectionCount == 0 ? 0 : accumulatedCheckoutTimeOfOverdueConnections / claimedOverdueConnectionCount;
//    }
//  }
//
//
//  /**
//   * Getter for the average age of a connection checkout
//   * @return The average age
//   */
//  public long getAverageCheckoutTime() {
//    synchronized (POOL_LOCK) {
//      return requestCount == 0 ? 0 : accumulatedCheckoutTime / requestCount;
//    }
//  }
//
//  /**
//   * Returns the status of the connection pool
//   * @return The status
//   */
//  public String getStatus() {
//    StringBuffer buffer = new StringBuffer();
//
//    buffer.append("\n===============================================================");
//    buffer.append("\n jdbcDriver                     ").append(jdbcDriver);
//    buffer.append("\n jdbcUrl                        ").append(jdbcUrl);
//    buffer.append("\n jdbcUsername                   ").append(jdbcUsername);
//    buffer.append("\n jdbcPassword                   ").append((jdbcPassword == null ? "NULL" : "************"));
//    buffer.append("\n poolMaxActiveConnections       ").append(poolMaximumActiveConnections);
//    buffer.append("\n poolMaxIdleConnections         ").append(poolMaximumIdleConnections);
//    buffer.append("\n poolMaxCheckoutTime            " + poolMaximumCheckoutTime);
//    buffer.append("\n poolTimeToWait                 " + poolTimeToWait);
//    buffer.append("\n poolPingEnabled                " + poolPingEnabled);
//    buffer.append("\n poolPingQuery                  " + poolPingQuery);
//    buffer.append("\n poolPingConnectionsOlderThan   " + poolPingConnectionsOlderThan);
//    buffer.append("\n poolPingConnectionsNotUsedFor  " + poolPingConnectionsNotUsedFor);
//    buffer.append("\n --------------------------------------------------------------");
//    buffer.append("\n activeConnections              " + activeConnections.size());
//    buffer.append("\n idleConnections                " + idleConnections.size());
//    buffer.append("\n requestCount                   " + getRequestCount());
//    buffer.append("\n averageRequestTime             " + getAverageRequestTime());
//    buffer.append("\n averageCheckoutTime            " + getAverageCheckoutTime());
//    buffer.append("\n claimedOverdue                 " + getClaimedOverdueConnectionCount());
//    buffer.append("\n averageOverdueCheckoutTime     " + getAverageOverdueCheckoutTime());
//    buffer.append("\n hadToWait                      " + getHadToWaitCount());
//    buffer.append("\n averageWaitTime                " + getAverageWaitTime());
//    buffer.append("\n badConnectionCount             " + getBadConnectionCount());
//    buffer.append("\n===============================================================");
//    return buffer.toString();
//  }
//
//  /**
//   * Closes all of the connections in the pool
//   */
//  public void forceCloseAll() {
//    synchronized (POOL_LOCK) {
//      for (int i = activeConnections.size(); i > 0; i--) {
//        try {
//          SimplePooledConnection conn = (SimplePooledConnection) activeConnections.remove(i - 1);
//          conn.invalidate();
//
//          Connection realConn = conn.getRealConnection();
//          if (!realConn.getAutoCommit()) {
//            realConn.rollback();
//          }
//          realConn.close();
//        } catch (Exception e) {
//          // ignore
//        }
//      }
//      for (int i = idleConnections.size(); i > 0; i--) {
//        try {
//          SimplePooledConnection conn = (SimplePooledConnection) idleConnections.remove(i - 1);
//          conn.invalidate();
//
//          Connection realConn = conn.getRealConnection();
//          if (!realConn.getAutoCommit()) {
//            realConn.rollback();
//          }
//          realConn.close();
//        } catch (Exception e) {
//          // ignore
//        }
//      }
//    }
//    if (log.isDebugEnabled()) {
//      log.debug("SimpleDataSource forcefully closed/removed all connections.");
//    }
//  }
//
//  private void pushConnection(SimplePooledConnection conn)
//      throws SQLException {
//
//    synchronized (POOL_LOCK) {
//      activeConnections.remove(conn);
//      if (conn.isValid()) {
//        if (idleConnections.size() < poolMaximumIdleConnections && conn.getConnectionTypeCode() == getExpectedConnectionTypeCode()) {
//          accumulatedCheckoutTime += conn.getCheckoutTime();
//          if (!conn.getRealConnection().getAutoCommit()) {
//            conn.getRealConnection().rollback();
//          }
//          SimplePooledConnection newConn = new SimplePooledConnection(conn.getRealConnection(), this);
//          idleConnections.add(newConn);
//          newConn.setCreatedTimestamp(conn.getCreatedTimestamp());
//          newConn.setLastUsedTimestamp(conn.getLastUsedTimestamp());
//          conn.invalidate();
//          if (log.isDebugEnabled()) {
//            log.debug("Returned connection " + newConn.getRealHashCode() + " to pool.");
//          }
//          POOL_LOCK.notifyAll();
//        } else {
//          accumulatedCheckoutTime += conn.getCheckoutTime();
//          if (!conn.getRealConnection().getAutoCommit()) {
//            conn.getRealConnection().rollback();
//          }
//          conn.getRealConnection().close();
//          if (log.isDebugEnabled()) {
//            log.debug("Closed connection " + conn.getRealHashCode() + ".");
//          }
//          conn.invalidate();
//        }
//      } else {
//        if (log.isDebugEnabled()) {
//          log.debug("A bad connection (" + conn.getRealHashCode() + ") attempted to return to the pool, discarding connection.");
//        }
//        badConnectionCount++;
//      }
//    }
//  }
//
//  private SimplePooledConnection popConnection(String username, String password)
//      throws SQLException {
//    boolean countedWait = false;
//    SimplePooledConnection conn = null;
//    long t = System.currentTimeMillis();
//    int localBadConnectionCount = 0;
//
//    while (conn == null) {
//      synchronized (POOL_LOCK) {
//        if (idleConnections.size() > 0) {
//          // Pool has available connection
//          conn = (SimplePooledConnection) idleConnections.remove(0);
//          if (log.isDebugEnabled()) {
//            log.debug("Checked out connection " + conn.getRealHashCode() + " from pool.");
//          }
//        } else {
//          // Pool does not have available connection
//          if (activeConnections.size() < poolMaximumActiveConnections) {
//            // Can create new connection
//            if (useDriverProps) {
//              conn = new SimplePooledConnection(DriverManager.getConnection(jdbcUrl, driverProps), this);
//            } else {
//              conn = new SimplePooledConnection(DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword), this);
//            }
//            Connection realConn = conn.getRealConnection();
//            if (realConn.getAutoCommit() != jdbcDefaultAutoCommit) {
//              realConn.setAutoCommit(jdbcDefaultAutoCommit);
//            }
//            if (log.isDebugEnabled()) {
//              log.debug("Created connection " + conn.getRealHashCode() + ".");
//            }
//          } else {
//            // Cannot create new connection
//            SimplePooledConnection oldestActiveConnection = (SimplePooledConnection) activeConnections.get(0);
//            long longestCheckoutTime = oldestActiveConnection.getCheckoutTime();
//            if (longestCheckoutTime > poolMaximumCheckoutTime) {
//              // Can claim overdue connection
//              claimedOverdueConnectionCount++;
//              accumulatedCheckoutTimeOfOverdueConnections += longestCheckoutTime;
//              accumulatedCheckoutTime += longestCheckoutTime;
//              activeConnections.remove(oldestActiveConnection);
//              if (!oldestActiveConnection.getRealConnection().getAutoCommit()) {
//                oldestActiveConnection.getRealConnection().rollback();
//              }
//              conn = new SimplePooledConnection(oldestActiveConnection.getRealConnection(), this);
//              oldestActiveConnection.invalidate();
//              if (log.isDebugEnabled()) {
//                log.debug("Claimed overdue connection " + conn.getRealHashCode() + ".");
//              }
//            } else {
//              // Must wait
//              try {
//                if (!countedWait) {
//                  hadToWaitCount++;
//                  countedWait = true;
//                }
//                if (log.isDebugEnabled()) {
//                  log.debug("Waiting as long as " + poolTimeToWait + " milliseconds for connection.");
//                }
//                long wt = System.currentTimeMillis();
//                POOL_LOCK.wait(poolTimeToWait);
//                accumulatedWaitTime += System.currentTimeMillis() - wt;
//              } catch (InterruptedException e) {
//                break;
//              }
//            }
//          }
//        }
//        if (conn != null) {
//          if (conn.isValid()) {
//            if (!conn.getRealConnection().getAutoCommit()) {
//              conn.getRealConnection().rollback();
//            }
//            conn.setConnectionTypeCode(assembleConnectionTypeCode(jdbcUrl, username, password));
//            conn.setCheckoutTimestamp(System.currentTimeMillis());
//            conn.setLastUsedTimestamp(System.currentTimeMillis());
//            activeConnections.add(conn);
//            requestCount++;
//            accumulatedRequestTime += System.currentTimeMillis() - t;
//          } else {
//            if (log.isDebugEnabled()) {
//              log.debug("A bad connection (" + conn.getRealHashCode() + ") was returned from the pool, getting another connection.");
//            }
//            badConnectionCount++;
//            localBadConnectionCount++;
//            conn = null;
//            if (localBadConnectionCount > (poolMaximumIdleConnections + 3)) {
//              if (log.isDebugEnabled()) {
//                log.debug("SimpleDataSource: Could not get a good connection to the database.");
//              }
//              throw new SQLException("SimpleDataSource: Could not get a good connection to the database.");
//            }
//          }
//        }
//      }
//
//    }
//
//    if (conn == null) {
//      if (log.isDebugEnabled()) {
//        log.debug("SimpleDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
//      }
//      throw new SQLException("SimpleDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
//    }
//
//    return conn;
//  }
//
//  /**
//   * Method to check to see if a connection is still usable
//   *
//   * @param conn - the connection to check
//   * @return True if the connection is still usable
//   */
//  private boolean pingConnection(SimplePooledConnection conn) {
//    boolean result = true;
//
//    try {
//      result = !conn.getRealConnection().isClosed();
//    } catch (SQLException e) {
//      if (log.isDebugEnabled()) {
//        log.debug("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
//      }    	
//      result = false;
//    }
//
//    if (result) {
//      if (poolPingEnabled) {
//        if ((poolPingConnectionsOlderThan > 0 && conn.getAge() > poolPingConnectionsOlderThan)
//            || (poolPingConnectionsNotUsedFor > 0 && conn.getTimeElapsedSinceLastUse() > poolPingConnectionsNotUsedFor)) {
//
//          try {
//            if (log.isDebugEnabled()) {
//              log.debug("Testing connection " + conn.getRealHashCode() + " ...");
//            }
//            Connection realConn = conn.getRealConnection();
//            Statement statement = realConn.createStatement();
//            ResultSet rs = statement.executeQuery(poolPingQuery);
//            rs.close();
//            statement.close();
//            if (!realConn.getAutoCommit()) {
//              realConn.rollback();
//            }
//            result = true;
//            if (log.isDebugEnabled()) {
//              log.debug("Connection " + conn.getRealHashCode() + " is GOOD!");
//            }
//          } catch (Exception e) {
//            log.warn("Execution of ping query '" + poolPingQuery + "' failed: " + e.getMessage());          	
//            try {
//              conn.getRealConnection().close();
//            } catch (Exception e2) {
//              //ignore
//            }
//            result = false;
//            if (log.isDebugEnabled()) {
//              log.debug("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
//            }
//          }
//        }
//      }
//    }
//    return result;
//  }
//
//  /**
//   * Unwraps a pooled connection to get to the 'real' connection
//   *
//   * @param conn - the pooled connection to unwrap
//   * @return The 'real' connection
//   */
//  public static Connection unwrapConnection(Connection conn) {
//    if (conn instanceof SimplePooledConnection) {
//      return ((SimplePooledConnection) conn).getRealConnection();
//    } else {
//      return conn;
//    }
//  }
//
//  protected void finalize() throws Throwable {
//    forceCloseAll();
//  }
//
//  /**
//   * ---------------------------------------------------------------------------------------
//   * SimplePooledConnection
//   * ---------------------------------------------------------------------------------------
//   */
//  public static class SimplePooledConnection implements InvocationHandler {
//
//    private static final String CLOSE = "close";
//    private static final Class[] IFACES = new Class[]{Connection.class};
//
//    private int hashCode = 0;
//    private SimpleDataSource dataSource;
//    private Connection realConnection;
//    private Connection proxyConnection;
//    private long checkoutTimestamp;
//    private long createdTimestamp;
//    private long lastUsedTimestamp;
//    private int connectionTypeCode;
//    private boolean valid;
//
//    /**
//     * Constructor for SimplePooledConnection that uses the Connection and SimpleDataSource passed in
//     *
//     * @param connection - the connection that is to be presented as a pooled connection
//     * @param dataSource - the dataSource that the connection is from
//     */
//    public SimplePooledConnection(Connection connection, SimpleDataSource dataSource) {
//      this.hashCode = connection.hashCode();
//      this.realConnection = connection;
//      this.dataSource = dataSource;
//      this.createdTimestamp = System.currentTimeMillis();
//      this.lastUsedTimestamp = System.currentTimeMillis();
//      this.valid = true;
//
//      proxyConnection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), IFACES, this);
//    }
//
//    /**
//     * Invalidates the connection
//     */
//    public void invalidate() {
//      valid = false;
//    }
//
//    /**
//     * Method to see if the connection is usable
//     *
//     * @return True if the connection is usable
//     */
//    public boolean isValid() {
//      return valid && realConnection != null && dataSource.pingConnection(this);
//    }
//
//    /**
//     * Getter for the *real* connection that this wraps
//     * @return The connection
//     */
//    public Connection getRealConnection() {
//      return realConnection;
//    }
//
//    /**
//     * Getter for the proxy for the connection
//     * @return The proxy
//     */
//    public Connection getProxyConnection() {
//      return proxyConnection;
//    }
//
//    /**
//     * Gets the hashcode of the real connection (or 0 if it is null)
//     *
//     * @return The hashcode of the real connection (or 0 if it is null)
//     */
//    public int getRealHashCode() {
//      if (realConnection == null) {
//        return 0;
//      } else {
//        return realConnection.hashCode();
//      }
//    }
//
//    /**
//     * Getter for the connection type (based on url + user + password)
//     * @return The connection type
//     */
//    public int getConnectionTypeCode() {
//      return connectionTypeCode;
//    }
//
//    /**
//     * Setter for the connection type
//     * @param connectionTypeCode - the connection type
//     */
//    public void setConnectionTypeCode(int connectionTypeCode) {
//      this.connectionTypeCode = connectionTypeCode;
//    }
//
//    /**
//     * Getter for the time that the connection was created
//     * @return The creation timestamp
//     */
//    public long getCreatedTimestamp() {
//      return createdTimestamp;
//    }
//
//    /**
//     * Setter for the time that the connection was created
//     * @param createdTimestamp - the timestamp
//     */
//    public void setCreatedTimestamp(long createdTimestamp) {
//      this.createdTimestamp = createdTimestamp;
//    }
//
//    /**
//     * Getter for the time that the connection was last used
//     * @return - the timestamp
//     */
//    public long getLastUsedTimestamp() {
//      return lastUsedTimestamp;
//    }
//
//    /**
//     * Setter for the time that the connection was last used
//     * @param lastUsedTimestamp - the timestamp
//     */
//    public void setLastUsedTimestamp(long lastUsedTimestamp) {
//      this.lastUsedTimestamp = lastUsedTimestamp;
//    }
//
//    /**
//     * Getter for the time since this connection was last used
//     * @return - the time since the last use
//     */
//    public long getTimeElapsedSinceLastUse() {
//      return System.currentTimeMillis() - lastUsedTimestamp;
//    }
//
//    /**
//     * Getter for the age of the connection
//     * @return the age
//     */
//    public long getAge() {
//      return System.currentTimeMillis() - createdTimestamp;
//    }
//
//    /**
//     * Getter for the timestamp that this connection was checked out
//     * @return the timestamp
//     */
//    public long getCheckoutTimestamp() {
//      return checkoutTimestamp;
//    }
//
//    /**
//     * Setter for the timestamp that this connection was checked out
//     * @param timestamp the timestamp
//     */
//    public void setCheckoutTimestamp(long timestamp) {
//      this.checkoutTimestamp = timestamp;
//    }
//
//    /**
//     * Getter for the time that this connection has been checked out
//     * @return the time
//     */
//    public long getCheckoutTime() {
//      return System.currentTimeMillis() - checkoutTimestamp;
//    }
//
//    private Connection getValidConnection() {
//      if (!valid) {
//        throw new RuntimeException("Error accessing SimplePooledConnection. Connection is invalid.");
//      }
//      return realConnection;
//    }
//
//    public int hashCode() {
//      return hashCode;
//    }
//
//    /**
//     * Allows comparing this connection to another
//     *
//     * @param obj - the other connection to test for equality
//     * @see java.lang.Object#equals(java.lang.Object)
//     */
//    public boolean equals(Object obj) {
//      if (obj instanceof SimplePooledConnection) {
//        return realConnection.hashCode() == (((SimplePooledConnection) obj).realConnection.hashCode());
//      } else if (obj instanceof Connection) {
//        return hashCode == obj.hashCode();
//      } else {
//        return false;
//      }
//    }
//
//    // **********************************
//    // Implemented Connection Methods -- Now handled by proxy
//    // **********************************
//
//    /**
//     * Required for InvocationHandler implementation.
//     *
//     * @param proxy  - not used
//     * @param method - the method to be executed
//     * @param args   - the parameters to be passed to the method
//     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
//     */
//    public Object invoke(Object proxy, Method method, Object[] args)
//        throws Throwable {
//      String methodName = method.getName();
//      if (CLOSE.hashCode() == methodName.hashCode() && CLOSE.equals(methodName)) {
//        dataSource.pushConnection(this);
//        return null;
//      } else {
//        try {
//          return method.invoke(getValidConnection(), args);
//        } catch (Throwable t) {
//          throw ClassInfo.unwrapThrowable(t);
//        }
//      }
//    }
//
//    public Statement createStatement() throws SQLException {
//      return getValidConnection().createStatement();
//    }
//
//    public PreparedStatement prepareStatement(String sql) throws SQLException {
//      return getValidConnection().prepareStatement(sql);
//    }
//
//    public CallableStatement prepareCall(String sql) throws SQLException {
//      return getValidConnection().prepareCall(sql);
//    }
//
//    public String nativeSQL(String sql) throws SQLException {
//      return getValidConnection().nativeSQL(sql);
//    }
//
//    public void setAutoCommit(boolean autoCommit) throws SQLException {
//      getValidConnection().setAutoCommit(autoCommit);
//    }
//
//    public boolean getAutoCommit() throws SQLException {
//      return getValidConnection().getAutoCommit();
//    }
//
//    public void commit() throws SQLException {
//      getValidConnection().commit();
//    }
//
//    public void rollback() throws SQLException {
//      getValidConnection().rollback();
//    }
//
//    public void close() throws SQLException {
//      dataSource.pushConnection(this);
//    }
//
//    public boolean isClosed() throws SQLException {
//      return getValidConnection().isClosed();
//    }
//
//    public DatabaseMetaData getMetaData() throws SQLException {
//      return getValidConnection().getMetaData();
//    }
//
//    public void setReadOnly(boolean readOnly) throws SQLException {
//      getValidConnection().setReadOnly(readOnly);
//    }
//
//    public boolean isReadOnly() throws SQLException {
//      return getValidConnection().isReadOnly();
//    }
//
//    public void setCatalog(String catalog) throws SQLException {
//      getValidConnection().setCatalog(catalog);
//    }
//
//    public String getCatalog() throws SQLException {
//      return getValidConnection().getCatalog();
//    }
//
//    public void setTransactionIsolation(int level) throws SQLException {
//      getValidConnection().setTransactionIsolation(level);
//    }
//
//    public int getTransactionIsolation() throws SQLException {
//      return getValidConnection().getTransactionIsolation();
//    }
//
//    public SQLWarning getWarnings() throws SQLException {
//      return getValidConnection().getWarnings();
//    }
//
//    public void clearWarnings() throws SQLException {
//      getValidConnection().clearWarnings();
//    }
//
//    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
//      return getValidConnection().createStatement(resultSetType, resultSetConcurrency);
//    }
//
//    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
//      return getValidConnection().prepareCall(sql, resultSetType, resultSetConcurrency);
//    }
//
//    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
//      return getValidConnection().prepareCall(sql, resultSetType, resultSetConcurrency);
//    }
//
//    public Map getTypeMap() throws SQLException {
//      return getValidConnection().getTypeMap();
//    }
//
//    public void setTypeMap(Map map) throws SQLException {
//      getValidConnection().setTypeMap(map);
//    }
//
//    // **********************************
//    // JDK 1.4 JDBC 3.0 Methods below
//    // **********************************
//
//    public void setHoldability(int holdability) throws SQLException {
//      getValidConnection().setHoldability(holdability);
//    }
//
//    public int getHoldability() throws SQLException {
//      return getValidConnection().getHoldability();
//    }
//
//    public Savepoint setSavepoint() throws SQLException {
//      return getValidConnection().setSavepoint();
//    }
//
//    public Savepoint setSavepoint(String name) throws SQLException {
//      return getValidConnection().setSavepoint(name);
//    }
//
//    public void rollback(Savepoint savepoint) throws SQLException {
//      getValidConnection().rollback(savepoint);
//    }
//
//    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
//      getValidConnection().releaseSavepoint(savepoint);
//    }
//
//    public Statement createStatement(int resultSetType, int resultSetConcurrency,
//                                     int resultSetHoldability) throws SQLException {
//      return getValidConnection().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
//    }
//
//    public PreparedStatement prepareStatement(String sql, int resultSetType,
//                                              int resultSetConcurrency, int resultSetHoldability)
//        throws SQLException {
//      return getValidConnection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
//    }
//
//    public CallableStatement prepareCall(String sql, int resultSetType,
//                                         int resultSetConcurrency,
//                                         int resultSetHoldability) throws SQLException {
//      return getValidConnection().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
//    }
//
//    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
//        throws SQLException {
//      return getValidConnection().prepareStatement(sql, autoGeneratedKeys);
//    }
//
//    public PreparedStatement prepareStatement(String sql, int columnIndexes[])
//        throws SQLException {
//      return getValidConnection().prepareStatement(sql, columnIndexes);
//    }
//
//    public PreparedStatement prepareStatement(String sql, String columnNames[])
//        throws SQLException {
//      return getValidConnection().prepareStatement(sql, columnNames);
//    }
//
//
//  }
//}
