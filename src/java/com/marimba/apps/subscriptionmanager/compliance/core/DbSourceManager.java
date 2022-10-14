// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.core;

/**
 * Database connection pool manager for compliance
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import java.sql.SQLException;
import java.sql.DatabaseMetaData;

import com.marimba.apps.subscriptionmanager.TenantAttributes;
import com.marimba.apps.subscriptionmanager.users.UserManager;
import com.marimba.intf.db.IConnectionPool;
import com.marimba.intf.db.IStatementPool;
import com.marimba.intf.db.IDatabaseClient;
import com.marimba.intf.db.IDatabaseManager;
import com.marimba.intf.msf.IServer;
import com.marimba.intf.msf.IDatabaseMgr;
import com.marimba.intf.msf.query.IQueryMgr;
import com.marimba.intf.msf.*;

public class DbSourceManager implements IDatabaseClient {

    // Some constants
    static final String QUERYMANAGER = "atlas.query.manager";
    
    // Time out for connection
    static int timeout = 6000;

    // CMS server handle
    IServer iserver;

    // Handle to Query Manager (RC)
    IQueryMgr queryMgr;
    ITenantManager tenantMgr;
    ITenant tenant;
    // Connection pool
    IConnectionPool cpool;

    // stop?
    boolean stop;

    public DbSourceManager(ComplianceMain main) {
		iserver = main.getCMSserver();
		tenantMgr = main.getTenantMgr();
		tenant = main.getTenant();
		queryMgr = (IQueryMgr)tenant.getManager(QUERYMANAGER);
		System.out.println("Query manager is create for tenant :" + tenant.getName());
		this.cpool = null;
		this.stop = false;
    }
    /**
     * Get a statement pool
     */
    public synchronized IStatementPool getPool() {
	if (!stop) {
	    if (cpool == null) {
		cpool = getConnectionPool(false);
	    }
	    return (cpool != null) ? cpool.getConnection(timeout) : null;
	} else {
	    return null;
	}
    }
    /**
     * Return the IStatement pool object to the ConnectionPool
     * @param pool
     */
    public synchronized void returnPool(IStatementPool pool){
        if(!stop){
            if(null != pool) {
                if(null != cpool)
                    cpool.releaseConnection(pool);
            }
        }
    }
    
    public synchronized IQueryMgr getQueryMgr() {
		return queryMgr;
	}

	public synchronized void setQueryMgr(IQueryMgr queryMgr) {
		this.queryMgr = queryMgr;
	}

	/**
     * Stop, release the pool
     */
    public synchronized void stop() {
	stop = true;

	if (cpool != null) {
	    tenant.getDbMgr().releasePool(cpool, this);
	}
    }
    
    /*
     * Get connection from CMS
     */
    IConnectionPool getConnectionPool(boolean read) {
        try {
            
            String dbName = "";
            IDatabaseMgr dbmgr = (IDatabaseMgr) tenant.getDbMgr();
            if(read){
                dbName = dbmgr.getActive(IDatabaseMgr.DEFAULT_READ);
            }
            else {
                dbName = dbmgr.getActive(IDatabaseMgr.DEFAULT_WRITE);
            }

            if(dbName != null) {
                return dbmgr.getPool(dbName,this);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // REMIND, log something
	}

	// Failed
	return null;
    }

    public synchronized static IQueryMgr getQueryManager(ITenant tenant) throws SQLException {
    	IQueryMgr queryMgr = null;
    	try {
	        queryMgr = (IQueryMgr)tenant.getManager(QUERYMANAGER);
		    if (queryMgr == null) {
		    	throw new SQLException("Can't find query manager service");
		    }
    	} catch(Exception ec) {
    		ec.printStackTrace();
    		throw new SQLException("Can't find query manager service");
    	}
		return queryMgr;
    }

    // IDatabaseClient interface

    public void initialize(DatabaseMetaData md) throws SQLException {
	// do nothing for now
    }

    public void create(IStatementPool spool) {
	// REMIND, need to add prepared statement here
    }

    public void notify(Object sender, int msg, Object arg) {
	switch (msg) {
	case DB_CONNECT_ERROR:
	    break;
	case DB_CONNECTED:
	    break;
	case DB_USING_DRIVER:
	    break;
	}
    }
}
