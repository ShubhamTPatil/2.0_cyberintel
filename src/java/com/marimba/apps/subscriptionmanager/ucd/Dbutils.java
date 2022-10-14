// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$
package com.marimba.apps.subscriptionmanager.ucd;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.intf.db.IConnectionPool;
import com.marimba.intf.db.IDatabaseClient;
import com.marimba.intf.db.IStatementPool;
import com.marimba.intf.msf.IDatabaseMgr;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * DB Connection and Get query result based on parameter
 *
 * @author	 Selvaraj Jegatheesan
 * @version  $Revision$, $Date$
 *
 */
public class Dbutils implements IAppConstants {
    private String activeDB;
    private IDatabaseMgr dbmgr;
    private IConnectionPool connPool;
    
    //private IStatementPool stmtPool;
    private Dbutils dbUtils = null;

    static final int MAX_IN_LIMIT = 100;
    static final int DB_TIME_OUT = 60 * 10000;

    /**
     * DatabaseUtils constructor
     * @param dbmgr
     * @param argActiveDB
     * @throws ProvisioningException
     */
    public Dbutils(IDatabaseMgr dbmgr, String argActiveDB) throws Exception {
        this.dbmgr = dbmgr;
        this.activeDB = argActiveDB;
        createConnection();
    }
    /**
     * Initialize DatabaseUtils
     * @param dbmgr
     * @param activeDB
     * @return
     * @throws ProvisioningException
     */
    public Dbutils init(IDatabaseMgr dbmgr, String activeDB) throws Exception {
        if (dbUtils != null && !isDBChanged(activeDB)) {
            return dbUtils;
        } else {
            dbUtils = new Dbutils(dbmgr, activeDB);
            return dbUtils;
        }
    }
    /**
     * To verify Database changed or not
     * @param newdbName
     * @return
     */
    private boolean isDBChanged(String newdbName) {
        if (newdbName != null && activeDB != null) {
            return (!newdbName.equals(activeDB));
        } else {
            return true;
        }
    }
    /**
     * Create a new connection from dbmgr connection pool
     * @throws ProvisioningException
     */
    public void createConnection() throws Exception {
        if (activeDB != null) {
            connPool = dbmgr.getPool(activeDB, new DatabaseClientImpl());
            if(null != connPool) {
                debug("DB Connection successful");
            } else {
                debug("Failed to create connection");
            }
        }
    }
    /**
     * Create a new Statement pool
     * @return
     * @throws Exception
     */
    public IStatementPool createStatementPool() throws Exception {
        IStatementPool stmtPool = connPool.getConnection(DB_TIME_OUT);
        if (null != stmtPool) {
            debug("Statement pool created");
        } else {
            debug("Failed to create statement pool");
        }
        return stmtPool;
    }
    /**
     * Create a new statement
     * @return
     * @throws Exception
     */
    private Statement createStatement(IStatementPool stmtPool) throws Exception {
        Statement stmt = null;
        try {
            if (null != stmtPool) {
                stmt = stmtPool.createStatement();
            }
        } catch (SQLException e) {
            debug("Failed to create Statement : "+ e.getMessage());
        }
        return stmt;
    }
    /**
     * To verify Database Connection
     * @return
     * @throws Exception
     */
    public boolean checkDBConn() throws Exception {
        Statement stmt = null;
        ResultSet rslt = null;
        boolean success = false;
        String db_check_query = null;
        IStatementPool stmtPool = null;
        try {
            db_check_query = "select * from schema_version";
            if(null != db_check_query) {
                stmtPool = createStatementPool();
                if(null != stmtPool) {
                    stmt = createStatement(stmtPool);
                    rslt = stmt.executeQuery(db_check_query);
                    success = true;
                }
            }
        } catch (SQLException e) {
            debug("Failed to check DB connection : " + e.getMessage());
            if(DEBUG5) e.printStackTrace();
        } finally {
            try {
                if (rslt != null) rslt.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                debug("Failed to close result set or statement :" + sqle.getMessage());
                if(DEBUG5) sqle.printStackTrace();
            }
            if(null != stmtPool) {
                debug("Release statement pool for db check query");
                connPool.releaseConnection(stmtPool);
            }
        }
        debug("Database connection successful ? " + success);
        return success;
    }
    /**
     * Get list of OS Name from inv.os and inv_machine table
     * @return
     * @throws Exception
     */
    public List<String> getOSNameList() throws Exception {
        ResultSet rslt = null;
        Statement stmt = null;
        String sqlQuery = null;
        List<String> osNameList = new ArrayList<String>();
        IStatementPool stmtPool = null;

        try {
            sqlQuery = "select distinct inv_os.product from inv_os";
            if(null != sqlQuery) {
                debug("sqlQuery is :"+ sqlQuery);
                stmtPool = createStatementPool();
                if(null != stmtPool) {
                    stmt = createStatement(stmtPool);
                    rslt = stmt.executeQuery(sqlQuery);
                    while (rslt.next()) {
                    	String osName = rslt.getString("product");
                    	if(null != osName && !"".equals(osName.trim())) {
                    		osNameList.add(osName.trim());
                    	}
                    	
                    }
                }
            } else {
                debug("sqlQuery is empty");
            }
        } catch (SQLException e) {
            debug("Failed to get OS Name list :" + e.getMessage());
            if(DEBUG5) e.printStackTrace();
        } finally {
            try {
                if (rslt != null) rslt.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                debug("Failed to close result set or statement :" + sqle.getMessage());
                if(DEBUG5) sqle.printStackTrace();
            }
            if(null != stmtPool) {
                connPool.releaseConnection(stmtPool);
            }
        }
        return osNameList;
    }

    /**
     * Get list of Processor Name from inv.processor and inv_machine table
     * @return
     * @throws Exception
     */
    public List<String> getProcessorNameList() throws Exception {
        ResultSet rslt = null;
        Statement stmt = null;
        String sqlQuery = null;
        List<String> processorNameList = new ArrayList<String>();
        IStatementPool stmtPool = null;

        try {
            sqlQuery = "select distinct inv_processor.model from inv_processor";
            if(null != sqlQuery) {
                debug("sqlQuery is :"+ sqlQuery);
                stmtPool = createStatementPool();
                if(null != stmtPool) {
                    stmt = createStatement(stmtPool);
                    rslt = stmt.executeQuery(sqlQuery);
                    while (rslt.next()) {
                    	String processorName = rslt.getString("model");
                    	if(null != processorName && !"".equals(processorName.trim())) {
                    		processorNameList.add(processorName.trim());
                    	}
                    }
                }
            } else {
                debug("sqlQuery is empty");
            }
        } catch (SQLException e) {
            debug("Failed to get Processor Name list :" + e.getMessage());
            if(DEBUG5) e.printStackTrace();
        } finally {
            try {
                if (rslt != null) rslt.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                debug("Failed to close result set or statement :" + sqle.getMessage());
                if(DEBUG5) sqle.printStackTrace();
            }
            if(null != stmtPool) {
                connPool.releaseConnection(stmtPool);
            }
        }
        return processorNameList;
    }

    /**
     * Get list of executable applications names from inv_application
     *
     * @return appsSet
     * @throws Exception
     */
    public Set<String> getExecutableFromInvApplication() throws Exception {
        ResultSet rslt = null;
        Statement stmt = null;
        Set<String> appsSet = new HashSet<String>();
        IStatementPool stmtPool = null;

        try {
            String sqlQuery = null;

            if (connPool.getDBProduct() == IConnectionPool.SQLSERVER) {
                sqlQuery = "select distinct filename from inv_application where lower(isnull(filename,'NULL')) like '%.exe%'";
            } else if (connPool.getDBProduct() == IConnectionPool.ORACLE) {
                sqlQuery = "select distinct filename from inv_application where lower(nvl(filename,'NULL')) like '%.exe%'";
            }

            debug("sqlQuery is :"+ sqlQuery);
            stmtPool = createStatementPool();

            if(null != stmtPool) {
                stmt = createStatement(stmtPool);
                rslt = stmt.executeQuery(sqlQuery);

                while (rslt.next()) {
                    String executableName = rslt.getString("filename");
                    if(null != executableName && !"".equals(executableName.trim())) {
                        appsSet.add(executableName.trim());
                    }
                }
            }
        } catch (SQLException e) {
            debug("Failed to get executable application name list :" + e.getMessage());
            if(DEBUG5) { e.printStackTrace(); }
        } finally {
            try {
                if (rslt != null) rslt.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                debug("Failed to close result set or statement :" + sqle.getMessage());
                if(DEBUG5) { sqle.printStackTrace(); }
            }

            if(null != stmtPool) {
                connPool.releaseConnection(stmtPool);
            }
        }

        return appsSet;
    }

    /**
     * Get list of executable application names from sw_exe_map
     *
     * @return appsSet
     * @throws Exception
     */
    public Set<String> getExectablesFromSoftwareTitle() throws Exception {
        ResultSet rslt = null;
        Statement stmt = null;
        Set<String> appsSet = new HashSet<String>();
        IStatementPool stmtPool = null;

        try {
            String sqlQuery = "select distinct exe from sw_exe_map order by exe";
            debug("sqlQuery is :"+ sqlQuery);
            stmtPool = createStatementPool();

            if(null != stmtPool) {
                stmt = createStatement(stmtPool);
                rslt = stmt.executeQuery(sqlQuery);

                while (rslt.next()) {
                    String executableName = rslt.getString("exe");
                    if(null != executableName && !"".equals(executableName.trim())) {
                        appsSet.add(executableName.trim());
                    }
                }
            }
        } catch (SQLException e) {
            debug("Failed to get Processor Name list :" + e.getMessage());
            if(DEBUG5) { e.printStackTrace(); }
        } finally {
            try {
                if (rslt != null) rslt.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                debug("Failed to close result set or statement :" + sqle.getMessage());
                if(DEBUG5) { sqle.printStackTrace(); }
            }
            if(null != stmtPool) {
                connPool.releaseConnection(stmtPool);
            }
        }

        return appsSet;
    }

    private void debug(String msg) {
        if(DEBUG5) System.out.println("UCD DatabaseUtils : " + msg);
    }

    class DatabaseClientImpl implements IDatabaseClient {
        public void initialize(DatabaseMetaData md) {
            return;
        }

        public void create(IStatementPool stPool){
            return;
        }

        public void notify(Object obj0, int ii, Object obj1){
            return;
        }
    }

}
