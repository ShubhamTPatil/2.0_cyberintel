// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.core;

/**
 * Main object for compliance
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */

import com.marimba.apps.subscriptionmanager.compliance.cache.*;
import com.marimba.apps.subscriptionmanager.intf.*;
import com.marimba.apps.subscriptionmanager.*;
import com.marimba.intf.msf.*;
import org.apache.log4j.*;

import javax.servlet.*;
import java.io.*;

public final class ComplianceMain {

    IServer cmsServer;
    DbSourceManager dbMgr;
    QueueManager queueMgr;
    ComplianceCacheMgr cache;
    ConfigManager cfgMgr;
    SubscriptionMain subMain;
    ITenantManager tenantMgr;
    ITenant tenant;
    ServletContext context;
    File rootDir;

    Logger sLogger = Logger.getLogger(ComplianceMain.class);
    private static boolean isDebug = IAppConstants.DEBUG;
    private ComplianceMain cm = null;

    public ComplianceMain() {
    }
    public void init() {
    	cmsServer = subMain.getServer();
    	rootDir = subMain.getDataDirectory();
    	tenantMgr = subMain.getTenantManager();
    	tenant = subMain.getTenant();
		dbMgr = new DbSourceManager(this);
		cache = new ComplianceCacheMgr();
		queueMgr = new QueueManager(this);
		queueMgr.initQueues();
		cfgMgr = new ConfigManager(this);
    }
    public void setCMSHandle(IServer _server){
        cmsServer = _server;
    }
    public void setSubscriptionMain(SubscriptionMain sm){
        subMain = sm;
    }

    public SubscriptionMain getSubscriptionMain(){
        return subMain;
    }
    public IServer getCMSserver() {
        return cmsServer;
    }

    public DbSourceManager getDbManager() {
        return dbMgr;
    }

    public QueueManager getQueueManager() {
        return queueMgr;
    }

    public void setServletContext(ServletContext ctx) {
        context = ctx;
        // Now, init the config manager
        cfgMgr.init();
    }
    public ITenantManager getTenantMgr() {
		return tenantMgr;
	}

	public void setTenantMgr(ITenantManager tenantManager) {
		tenantMgr = tenantManager;
	}

	public ITenant getTenant() {
		return tenant;
	}

	public void setTenant(ITenant tenantObj) {
		tenant = tenantObj;
	}
    public ComplianceCacheMgr getCache(){
        return cache;
    }

    public ConfigManager getConfig() {
        return cfgMgr;
    }

    public ServletContext getServletContext(){
        return context;
    }

    public File getRootDir() {
        return rootDir;
    }
    public void setRootDir(File rootDir) {
    	this.rootDir = rootDir;
    }
    public void error(String s,Throwable t){
        if(isDebug)
            sLogger.error(s,t);
    }
    public void shutdown(){
        dbMgr.stop();
        cache.shutDownCache();

    }

}
