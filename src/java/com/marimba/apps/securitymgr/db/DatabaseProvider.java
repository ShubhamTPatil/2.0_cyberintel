// Copyright 2019, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$

package com.marimba.apps.securitymgr.db;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import static com.marimba.apps.subscriptionmanager.intf.IAppConstants.DEBUG;
import static com.marimba.apps.subscriptionmanager.intf.IAppConstants.DEBUG5;
import com.marimba.intf.db.IConnectionPool;
import com.marimba.intf.db.IDatabaseClient;
import com.marimba.intf.db.IStatementPool;
import com.marimba.intf.msf.IDatabaseMgr;
import static com.marimba.intf.msf.IDatabaseMgr.DEFAULT_WRITE;
import com.marimba.intf.msf.ITenant;
import com.marimba.tools.util.DebugFlag;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: intteiva
 * Date: Mar 22, 2017
 * Time: 3:09:02 PM
 * To change this template use File | Settings | File Templates.
 */

public class DatabaseProvider implements IDatabaseClient {

    private int DEBUG = DebugFlag.getDebug("SECURITY/MGR");
    private boolean stop;
    private ITenant tenant;
    private int dbtype;
    private IStatementPool stmtPool;
    private IConnectionPool cpool;
    protected QueryProvider queryProvider;
    SubscriptionMain main;
    final static int SECOND = 1000;
    final static int MINUTE = 60 * SECOND;
    final static int  DEFAULT_CONNECT_TIMEOUT = 25 * SECOND; // Have it as per CMS default value

    public DatabaseProvider(SubscriptionMain main) {
        this.main = main;
        this.tenant = main.getTenant();
        this.cpool = null;
        this.stop = false;
        //dbtype = cpool.getDBType();
        //queryProvider = new QueryProvider(dbtype);
    }

    private void createConnection() {
        this.stmtPool = getStmtPool();
        if (null == stmtPool) debug("Failed to create database connection..");
        else debug("Database connection is successful");
    }

    protected synchronized IConnectionPool getConnectionPool() {
        IConnectionPool connectionPool = main.getConnectionPool();
        if (connectionPool != null) return connectionPool;
        try {
            IDatabaseMgr dbmgr = tenant.getDbMgr();
//            String dbName = dbmgr.getActive(read ? DEFAULT_READ : DEFAULT_WRITE);
            String dbName = dbmgr.getActive(DEFAULT_WRITE); // By default it should be inventory (write) connection

            if (dbName != null) {
                connectionPool = dbmgr.getPool(dbName, this);
                main.setConnectionPool(connectionPool);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connectionPool;
    }

    protected synchronized IStatementPool getStmtPool() {
        if (stop) return null;
        if (null == cpool) this.cpool = getConnectionPool();

        return (null != cpool) ? cpool.getConnection(DEFAULT_CONNECT_TIMEOUT) : null;
    }

    protected synchronized Statement getStatement(IStatementPool stmtPool) throws SQLException {
        Statement stmt = null;
        try {
            if (null != stmtPool) {
                stmt = stmtPool.createStatement();
            }
        } catch (SQLException e) {
            debug("Failed to create Statement - "+ e.getMessage());
        }
        return stmt;
    }

    protected synchronized void releasePool(){
        if (stop && null == stmtPool) return;
        if (null != cpool) cpool.releaseConnection(stmtPool);
    }

    protected synchronized void releasePool(IStatementPool stmtPool){
        if (stop && null == stmtPool) return;
        if (null != cpool) cpool.releaseConnection(stmtPool);
    }

    private synchronized void stop() {
        this.stop = true;
        if (cpool != null) {
            tenant.getDbMgr().releasePool(cpool, this);
        }
    }

    void cleanUp() {
        stop();
    }

    public void initialize(DatabaseMetaData md) throws SQLException {

    }

    public void create(IStatementPool pool) {

    }

    public void notify(Object sender, int msg, Object arg) {
        switch (msg) {
            case DB_CONNECT_ERROR:
                break;
            case DB_CONNECTED:
                break;
            case DB_USING_DRIVER:
                break;
            case DB_CONNECTION_DROPPED:
                break;
            case DB_DISABLED:
                break;
            case DB_ENABLED:
                break;
        }
    }

    // Utility methods

    protected boolean checkDBConn() {
        Statement stmt = null;
        ResultSet rslt = null;
        boolean success = false;
        IStatementPool stmtPool = null;
        try {
            String db_check_query = "select version from schema_version where type='inventory'";
            stmtPool = getStmtPool();
            if (null != stmtPool) {
                stmt = getStatement(stmtPool);
                rslt = stmt.executeQuery(db_check_query);
                success = true;
            }
        } catch (Exception e) {
            if(DEBUG5) e.printStackTrace();
            debug("Failed to check DB connection - " + e.getMessage());
        } finally {
            try {
                if (rslt != null) rslt.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                if(DEBUG5) sqle.printStackTrace();
            }
            if (null != stmtPool) {
                releasePool(stmtPool);
            }
        }
        debug("Database connection successful ? " + success);
        return success;
    }

    protected void debug(String msg) {
        if (DEBUG >= 1) {
            System.out.println("DatabaseProvider.java :: [" + new Date().toString() + "] ==> " + msg);
        }
    }

    protected void debug(int level, String msg) {
        if (DEBUG >= level) {
            System.out.println("DatabaseProvider.java :: [" + new Date().toString() + "] ==> " + msg);
        }
    }
}
