// Copyright 2017-2019, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionplugin;

import java.io.*;
import java.lang.ClassLoader;
import java.lang.Integer;
import java.lang.Throwable;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.StringResourcesHelper;
import com.marimba.apps.subscription.common.ISubscriptionStringResources;

import com.marimba.apps.subscriptionplugin.intf.request.ISecurityServiceRequest;
import com.marimba.apps.subscriptionplugin.intf.ISecurityServiceSettings;
import com.marimba.apps.subscriptionplugin.intf.ISecurityServiceConstants;
import com.marimba.apps.subscriptionplugin.intf.IDataStorage;
import com.marimba.apps.subscriptionplugin.impl.file.FileDataStorage;
import com.marimba.apps.subscriptionplugin.impl.db.DbDataStorage;
import com.marimba.apps.subscriptionplugin.impl.bigdata.BigDataStorage;
import com.marimba.apps.subscriptionplugin.request.SendComplianceDetailsRequest;

import com.marimba.apps.subscription.common.intf.IDebug;
import com.marimba.intf.plugin.IPlugin;
import com.marimba.intf.plugin.IPluginContext;
import com.marimba.intf.plugin.IRequest;
import com.marimba.intf.plugin.IHttpRequest;
import com.marimba.intf.util.IConfig;
import com.marimba.intf.util.IProperty;
import com.marimba.intf.transmitter.extension.ITExtension;
import com.marimba.intf.castanet.IIndex;
import com.marimba.intf.castanet.IWorkspace;
import com.marimba.intf.logs.ILog;
import com.marimba.intf.http.HTTPConstants;
import com.marimba.intf.ssl.ISSLProvider;
import com.marimba.intf.certificates.ICertProvider;

import com.marimba.io.*;
import com.marimba.tools.config.ConfigUtil;
import com.marimba.tools.config.ConfigPrefix;
import com.marimba.tools.config.ConfigDefaults;
import com.marimba.tools.config.ConfigProps;
import com.marimba.tools.util.*;
import com.marimba.tools.net.*;
import com.marimba.webapps.intf.SystemException;
import com.marimba.castanet.http.HTTPManager;
import com.marimba.castanet.http.JavaHTTPSManager;
import com.marimba.castanet.tools.PluginConnection;
import com.marimba.castanet.ssl.HTTPSManager;
import com.bmc.osm.soapclient.SoapClientMain;
import com.bmc.osm.soapclient.GetClientIDCall;
import com.bmc.osm.exception.OSMException;
import marimba.util.Timer;
import marimba.util.TimerClient;

/**
 * Subscription service .configurator segment
 */
public class SubscriptionPlugin
        implements IPlugin,
                   IPluginDebug,
                   ITExtension,
                   ISubscriptionConstants,
                   ISecurityServiceConstants,
                   ISecurityServiceSettings,
                   LogConstants,
                   Runnable,
                   TimerClient,
                   HTTPConstants {

    private LDAPDataSource dataSource;
    private ISubsPluginContext ctx;

    private int retryCount = 0;
    private int retryInterval;
    private Boolean rejectReq = Boolean.FALSE;
    private StartRetryTimeout startRetryT;
    private Timer timer = new Timer();
    private boolean timerStarted = false;
    boolean pluginStarted =  false;
    boolean isPluginStopping =  false;
	private int cacheRefreshInterval;

    private String HTTP_OP_ACTIVATE_PXE = "activate_pxe";
    private String DEPOT_LOCATION = "OS.INTERNAL_OS_MIGRATION_SETTINGS.LOCATION";
    private String ACCOUNT_USER = "OS.INTERNAL_OS_MIGRATION_SETTINGS.ACCOUNT_USER";
    private String ACCOUNT_PWD = "OS.INTERNAL_OS_MIGRATION_SETTINGS.ACCOUNT_PWD";
    private String ACCOUNT_DOMAIN = "OS.INTERNAL_OS_MIGRATION_SETTINGS.ACCOUNT_DOMAIN";
    private String PBU_FILE = "OS.INTERNAL_OS_MIGRATION_SETTINGS.PBU";

    ConfigUtil config;
    IProperty channelProps;
    IDataStorage storage;
    FileDataStorage fileStorage;
    DbDataStorage dbStorage;
    BigDataStorage bigDataStorage;
    boolean enabled;

    HTTPManager httpMgr;
    HTTPConfig httpConfig;

    private URL master;
    boolean repeaterInsert;
    private String securityInfoURL;
    private String securityInfoUserName;
    private String securityInfoPassword;
    private String customScannerURL;
    private String customScannerUserName;
    private String customScannerPassword;

    Timer scanReportQueueTimer = Timer.master;
    int scanReportQueueTimerInterval = 15; //in minutes
    SecurityScanReportQueue security_scan_report_queue;

    ThreadGroup threadgroup;		// thread group to run threads in
    int maxInsertingThreads;		// maximum number of threads to do inserts

    private static int DAY = 3600 * 24 * 1000;	// milliseconds per day

    private void start() {
        debug(INFO, "start(), Subscription plugin starting");

	    ConfigUtil tunerConfig = new ConfigUtil((IConfig) ctx.getPluginContext().getFeature("config"));
	    retryInterval = tunerConfig.getInteger("marimba.securityplugin.retryintervalsec", 30);

		cacheRefreshInterval = tunerConfig.getInteger("marimba.securityplugin.siterefreshinterval", 60);

        scanReportQueueTimerInterval = tunerConfig.getInteger("marimba.securityplugin.queueprocessinterval", 15);

        initializeStringResources();
        try {
            dataSource = new LDAPDataSource(ctx, cacheRefreshInterval);
        } catch (SystemException e) {
           //this exception in logge in the datasource class
            dataSource = null;
        }

        ILog log = (ILog)ctx.getPluginContext().getFeature("log");
        String rollPolicy = ctx.getSubsMrbaConfigMgr().
                getSubConfig().getProperty("marimba.subscriptionplugin.logs.roll.policy");
        String rollVersion = ctx.getSubsMrbaConfigMgr().
                getSubConfig().getProperty("marimba.subscriptionplugin.logs.roll.versions");

        if ((rollPolicy != null) && !("".equals(rollPolicy.trim()))) {
            log.setRollPolicy(rollPolicy);
        }
        if ((rollVersion != null) && !("".equals(rollVersion.trim()))) {
            log.setMaxRolledFiles(Integer.parseInt(rollVersion));
        }

        initScanReportQueue();

        ctx.log(LOG_START_PLUGIN, LOG_AUDIT);
    }

    private void initScanReportQueue() {
        // create thread pool for handling log report
        threadgroup = new ThreadGroup("vInspectorPlugin-QueueProcessor-ThreadGroup");
        threadgroup.setMaxPriority(Thread.NORM_PRIORITY);
        maxInsertingThreads = 30;
        try {
            maxInsertingThreads = Integer.parseInt(channelProps.getProperty("subscriptionplugin.db.thread.max"));
        } catch (Throwable t) {
            //ignore...
        }
        long maxfiles = config.getLong("security.pluginmaxfiles", 500000);
        long maxdiskSize = config.getLong("security.pluginmaxdisksize", 104857600);

        try {
            File queueFolder = new File(ctx.getPluginContext().getDataDirectory());
            security_scan_report_queue = new SecurityScanReportQueue(this, queueFolder, 5 * DAY, maxdiskSize, maxfiles, scanReportQueueTimerInterval);
        } catch (IOException e) {
            if (DETAILED_INFO) {
                e.printStackTrace();
            }
            ctx.log(LOG_ERROR_CREATING_QUEUE, LOG_CRITICAL, e);
        }
        debug(INFO, "initScanReportQueue(), security_scan_report_queue - " + security_scan_report_queue);

        if (security_scan_report_queue != null) {
            initThreads();
        }

        scanReportQueueTimer.add(this, System.currentTimeMillis() + (scanReportQueueTimerInterval * 60 * 1000), null);
        debug(INFO, "initScanReportQueue(), scanReportQueueTimer added...");
    }

    private void destroyScanReportQueue() {
        if (security_scan_report_queue != null) {
            security_scan_report_queue.stop();
            scanReportQueueTimer.remove(this, null);
            debug(INFO, "destroyScanReportQueue(), scanReportQueueTimer removed...");
            security_scan_report_queue = null;
            if (threadgroup != null) {
                threadgroup.destroy();
                threadgroup = null;
            }
        }
    }

    private void initializeStringResources() {
        try {
            PluginStringResources strResources = new PluginStringResources(ctx);
            StringResourcesHelper.setStringResources(strResources);
        } catch(Exception ex) {
            debug(WARNING, "initializeStringResources(), Unable to initialize string resources", ex);
        }
    }

    /**
     * Initialize the threads that will process the queue.  We allow up
     * to maxInsertingThreads threads to process the queue.  Threads that are
     * idle will exit, and then we need to keep track of how many are
     * running so we can create new threads if the load goes back up.
     */
    private void initThreads() {
        int threads = Math.min(getSecurityScanReportQueue().activeLength(), maxInsertingThreads);
        debug(INFO, "initThreads(), threads - " + threads);

        for (int i = 0; i < threads; i++) {
            fork();
        }
    }

    /**
     * Fork a new thread into the thread group.
     */
    private void fork() {
        getSecurityScanReportQueue();
        debug(INFO, "fork(), security_scan_report_queue - " + security_scan_report_queue);
        if (security_scan_report_queue != null) {
            debug(INFO, "fork(), security_scan_report_queue.length() - " + security_scan_report_queue.length());
            if (security_scan_report_queue.length() > 0) {
                debug(INFO, "fork(), start threads");
                new Thread(threadgroup, this, "vInspectorPlugin-QueueProcessor-Thread-" + System.currentTimeMillis()).start();
            }
        }
    }

    void checkFork() {
        synchronized (threadgroup) {
            debug(INFO, "checkFork(), threadgroup.activeCount() - " + threadgroup.activeCount() + ", maxInsertingThreads - " + maxInsertingThreads);
            initThreads();
        }
    }

    private void stop() {
        debug(INFO, "stop(), Subscription plugin stopped");

        isPluginStopping = true;

        if(bigDataStorage != null) {
        	bigDataStorage.stop();
        }

        if (dataSource != null) {
            dataSource.close();
        }

        if (dbStorage != null) {
            dbStorage.close();
            dbStorage = null;
        }
        storage = null;

        if (startRetryT != null) {
            timer.remove(startRetryT);
        }

        destroyScanReportQueue();

        ctx.log(LOG_STOP_PLUGIN, LOG_AUDIT);
    }

    private void setProfileAttributes(IRequest request) {
    	try {
            /*
            if(null != securityInfoURL) {
    			request.setProfileAttribute("securityinfo_url", securityInfoURL);
    		}
    	    if(null != securityInfoUserName) {
    	    	request.setProfileAttribute("securityinfo_username", securityInfoUserName);
    	    }
    	    if(null != securityInfoPassword) {
    	    	request.setProfileAttribute("securityinfo_password", securityInfoPassword);
    	    }
    	    if(null != customScannerURL) {
    	    	request.setProfileAttribute("customscanner_url", customScannerURL);
    	    }
    	    if(null != customScannerUserName) {
    	    	request.setProfileAttribute("customscanner_username", customScannerUserName);
    	    }
    	    if(null != customScannerPassword) {
    	    	request.setProfileAttribute("customscanner_password", customScannerPassword);
    	    }
    	    */
    	} catch(Exception ed) {
    		
    	}
    }
    private void handleRequest(IRequest request) {
        // create the subscription property files for the specific request
        if (dataSource != null) {
        	setProfileAttributes(request);
            IIndex reply = dataSource.getData(request);
            updateStatus(reply);
            request.setReplyIndex(reply);
        } else {
            // If we are trying to initialize, don't accept any request
            // Shouldn't synchronize on "this" as this feature will never work
            // because the "this" lock is held when we do initialize
            synchronized (rejectReq) {
                // synchronize because rejectReq is shared
                if (rejectReq.booleanValue()) {
                    debug(WARNING, "handleRequest(), Request rejected. reinitializing");
                    ctx.log(LOG_REJECTED_DATA_SOURCE, LOG_AUDIT, request.getRemoteAddress().getHostAddress(), request.getTunerID() + "/" + request.getUserID());
                    request.setReplyIndex(null);
                }
            }
            retryInitDataSource(request);
            if (dataSource == null) {
                debug(WARNING, "handleRequest(), No data source");
                ctx.log(LOG_NO_DATA_SOURCE, LOG_CRITICAL, request.getRemoteAddress().getHostAddress(), request.getTunerID() + "/" + request.getUserID());
            } else {
                debug(WARNING, "handleRequest(), Retry successful");
                ctx.log(LOG_START_PLUGIN, LOG_AUDIT);
            }
        }
    }

    /**
     * REMIND
     *
     * @param sender REMIND
     * @param msg REMIND
     * @param arg REMIND
     */
    public void notify(Object sender,
                       int    msg,
                       Object arg) {
        switch (msg) {
            case PLUGIN_INIT:

                ctx = new SubsPluginContext();
                ctx.registerPluginContext((IPluginContext)arg);
                init();
                debug(INFO, "notify(), Security plugin initialized");

                break;

            case PLUGIN_START:

                String subscriptionPluginStatus = ctx.getPluginContext().getChannelProperties().getProperty("subscriptionplugin.pluginStatus");

                if ("enable".equals(subscriptionPluginStatus)) {
                    pluginStarted = true;
                    isPluginStopping = false;
                    enabled = true;
                } else {
                    ctx.log(LOG_PLUGIN_DISABLED, LOG_INFO, "Security Plugin Disabled...");
                }

                start();
                debug(INFO, "notify(), Security plugin Started");
                break;

            case PLUGIN_UPDATE:

                if (pluginStarted) {
					String srcHost = ((IRequest) arg).getRemoteAddress().getHostAddress();
					if (srcHost == null) {
						srcHost = "unknown_host";
					}

					String endpointUniqueId = srcHost + "_" + System.currentTimeMillis ();
					Thread.currentThread ().setName (endpointUniqueId);
                    debug(INFO, "notify(), SubscriptionPlugin.java: endpointUniqueId : " + endpointUniqueId);

					handleRequest((IRequest) arg);
               }

                break;
            case PLUGIN_HTTP_REQUEST:
                debug(INFO, "notify(), PLUGIN_HTTP_REQUEST");
                IHttpRequest req = (IHttpRequest)arg;

                String typeStr = req.getHeader("request-type");
                debug(INFO, "notify(), PLUGIN_HTTP_REQUEST, typeStr - " + typeStr);

                handleRequest(req);
                break;

            case PLUGIN_STOP:
                debug(INFO, "notify(), PLUGIN_STOP");
                stop();

                break;
        }
    }

    /* Handle an incoming "client" request to the plugin */
    private void handleRequest(IHttpRequest request) {
        debug(INFO, "handleRequest(), Received a IHttpRequest");

        int reqCode = -1;

        try {
            FastInputStream in = new FastInputStream(request.getInputStream());

            // Check if the magic number and version matches the supported ones...
            int magic = in.readInt(); // read the magic number
            int version = in.readInt(); // read the version for the protocol

            // Make sure that MAGIC and VERSION are correct
            if (magic != MAGIC || version != VERSION) {
                debug(INFO, "handleRequest(), Bad protocol or version");
                request.reply(HTTP_BAD_REQUEST, "Bad protocol or version");
                request.getOutputStream(-1);
                return;
            }

            reqCode = in.readInt();
            debug(INFO, "handleRequest(), Received a request - " + reqCode);

            ISecurityServiceRequest securityServiceRequest = null;
            switch (reqCode) {
                case SECURITY_SERVICE_SEND_COMPLIANCE_DETAILS_REQUEST:
                    debug(INFO, "handleRequest(), Received a SECURITY_SERVICE_SEND_COMPLIANCE_DETAILS_REQUEST");
                    securityServiceRequest = new SendComplianceDetailsRequest();
                    break;
            }

            securityServiceRequest.handleRequest(in, request, this);

        } catch (Exception e) {
            if (DETAILED_INFO) {
                e.printStackTrace();
            }
        }
    }

    // TODO: Initialize the plugin.
    // NOTE: Over here, the data source has to be initialized. Now it is a "flat file" data source, when I
    // implement the DB handling logic, it will be loaded instead.
    private void init() {
        IConfig tunerConfig = (IConfig) ctx.getPluginContext().getFeature("config");
        config = new ConfigUtil(ctx.getPluginContext().getPluginConfiguration());
        config.setProperty("log.dir", ctx.getPluginContext().getDataDirectory() + File.separator);

        String[] configPropertyPairs = config.getPropertyPairs();
        for (int i=0; i < configPropertyPairs.length; i+=2) {
            debug(INFO, "init(), *** config: " + configPropertyPairs[i] + "=" + configPropertyPairs[i + 1]);
        }

        channelProps = ctx.getPluginContext().getChannelProperties();
        String[] channelPropertyPairs = channelProps.getPropertyPairs();
        for (int i=0; i < channelPropertyPairs.length; i+=2) {
            debug(INFO, "init(), *** channelProps: " + channelPropertyPairs[i] + "=" + channelPropertyPairs[i + 1]);
        }

        /* Check if running in master or repeater */
        master = ctx.getPluginContext().getMasterURL();
        debug(INFO, "init(), master - " + master);

        /**
         * Algorithm: -
         * [1] check if we have any master
         * 	[1 a] if we dont have have master, then
         * 		[1] check tuner property 'securityplugin.inserter.url
         * 			[1 a] if URL, then set master so that we do forward
         * 			[1 b] if no url, then check plugin property inserter.url
         * 				[1 b a] if inserter url is found, set master
         * 				[1 b b] else we are supposed to insert....
         *
         * 	[1 b] if we have master, that means we are repeater
         * 		[1 b a] check tuner property 'securityplugin.inserter.url
         * 			[1 b a a] if URL, then set master so that we do forward
         * 			[1 b a b] else check if plugin property repeater.insert is set
         * 				[1 b a b a] if yes, we do insert
         * 				[1 b a b b] else, we do forward
         */

        if ((master == null)) {
            ctx.log(LOG_PLUGIN_MASTER_MODE, LOG_AUDIT);
            String inserterUrl = tunerConfig.getProperty("securityplugin.inserter.url");
            if (inserterUrl == null || "".equals(inserterUrl.trim())) {
                inserterUrl = channelProps.getProperty("inserter.url");
            }

            if(inserterUrl != null && !"".equals(inserterUrl.trim())) {
                //we have got the inserter URL, we should try and forward the reports to inserter now
                try {
                    debug(INFO, "init(), inserterUrl1 - " + inserterUrl);
                    master = new URL(inserterUrl);
                } catch (MalformedURLException me) {
                    ctx.log(LOG_ERROR_DB_CONNECT, LOG_WARNING, "Failed to connect to inserter... the reports would be stored in the plugin queue.", null);
                }
            } else {
                //we are inserter and we just have to insert the data...
            }
        } else {
            boolean fallbackToRepeater = false;
            String inserterUrl = tunerConfig.getProperty("securityplugin.inserter.url");
            if(inserterUrl != null && !"".equals(inserterUrl.trim())) {
                //we have got the inserter URL, we should try and forward the reports to inserter now
                try {
                    debug(INFO, "init(), inserterUrl2 - " + inserterUrl);
                    master = new URL(inserterUrl);
                } catch (MalformedURLException me) {
                    ctx.log(LOG_ERROR_DB_CONNECT, LOG_WARNING, "Failed to connect to inserter... the reports would be stored in the plugin queue.", null);
                    fallbackToRepeater = true;
                }
            } else {
                fallbackToRepeater = true;
            }

            String repeaterToInsert = channelProps.getProperty("repeater.insert");
            debug(INFO, "init(), repeaterToInsert - " + repeaterToInsert);

            if(repeaterToInsert != null && "true".equalsIgnoreCase(repeaterToInsert) && fallbackToRepeater) {
                repeaterInsert = true;
            } else {
                repeaterInsert = false;
            }

            ctx.log(repeaterInsert ? LOG_PLUGIN_REPEATER_MODE_INSERTS : LOG_PLUGIN_SLAVE_MODE_NOINSERTS, LOG_AUDIT);
        }

        IConfig dbCfg = new ConfigProps(new Props(), null);
        dbCfg.setProperty("db.connection.class", channelProps.getProperty("subscriptionplugin.db.class"));
        dbCfg.setProperty("db.connection.url", channelProps.getProperty("subscriptionplugin.db.url"));
        dbCfg.setProperty("db.connection.user", channelProps.getProperty("subscriptionplugin.db.username"));
        dbCfg.setProperty("db.connection.pwd", Password.decode(channelProps.getProperty("subscriptionplugin.db.password")));
        dbCfg.setProperty("db.connection.type", channelProps.getProperty("subscriptionplugin.db.type"));
        dbCfg.setProperty("db.thread.max", channelProps.getProperty("subscriptionplugin.db.thread.max"));
        dbCfg.setProperty("db.thread.min", channelProps.getProperty("subscriptionplugin.db.thread.min"));

        String securityComplianceResultStorageType = channelProps.getProperty("securitycomplianceresult.storage.type");
        debug(INFO, "init(), securityComplianceResultStorageType - " + securityComplianceResultStorageType);

        // create the http manager for forwarding requests..
        httpConfig = new HTTPConfig(new ConfigUtil((IConfig) ctx.getPluginContext().getFeature("config")));
        try {
            ISSLProvider ssl = (ISSLProvider) ctx.getPluginContext().getFeature("ssl");
            ICertProvider cert = (ICertProvider) ctx.getPluginContext().getFeature("certificates");
            httpMgr = new HTTPSManager(httpConfig, cert, ssl);
        } catch (UnsatisfiedLinkError e) {
            httpMgr = new HTTPManager(httpConfig);
        }
        if ("file".equalsIgnoreCase(securityComplianceResultStorageType)) {
            String securityComplianceResultFileStorageDir = channelProps.getProperty("securitycomplianceresult.filestorage.dir");
            debug(INFO, "init(), securityComplianceResultFileStorageDir - " + securityComplianceResultFileStorageDir);

            fileStorage = new FileDataStorage(securityComplianceResultFileStorageDir);
            fileStorage.init();
            storage = fileStorage;
        } else if ("bigdata".equalsIgnoreCase(securityComplianceResultStorageType)) {
            bigDataStorage = new BigDataStorage(this, ctx.getPluginContext());
            bigDataStorage.init(httpMgr, httpConfig);
            storage = bigDataStorage;
        } else {
            if (master == null || repeaterInsert) {
                //db is default...
                debug(INFO, "init(), we are master/mirror or a repeater inserting into the database");
                String cl = dbCfg.getProperty("db.connection.class");
                boolean dbConnected = false, elasticConnected = false;
                storage = null;
                if (cl == null) {
                    debug(INFO, "init(), Database connection class not configured");
                    ctx.log(LOG_ERROR_DB_CONNECT, LOG_CRITICAL, "Failed to retrieve DB Connection Class... ", null);
                } else {
                    debug(INFO, "init(), About to initialize Database storage");
                    dbStorage = new DbDataStorage(this, new ConfigUtil(dbCfg));
                    debug(INFO, "init(), dbStorage - " + dbStorage);
                    dbConnected = dbStorage.init();
                    debug(INFO, "init(), dbConnected - " + dbConnected);
                    storage = dbStorage;
                    if (!dbConnected) {
                        ctx.log(LOG_ERROR_DB_CONNECT, LOG_CRITICAL, "Failed to connect to database... ", null);
                    }

                    //initializing Big Data as well for Database storage.
                    //this need to be removed later, when we migrate fully to Big Data...
                    bigDataStorage = new BigDataStorage(this, ctx.getPluginContext());
                    debug(INFO, "init(), bigDataStorage - " + bigDataStorage);
                    elasticConnected = bigDataStorage.init(httpMgr, httpConfig);
                    debug(INFO, "init(), elasticConnected - " + elasticConnected);
                    if (!elasticConnected) {
                        ctx.log(LOG_ERROR_BIGDATA_CONNECT, LOG_CRITICAL, "Failed to connect to elastic server... ", null);
                    }
                }
            } else {
                debug(INFO, "init(), Haven't been able to initialize database successfully.");
            }
        }

        // Get property values for custom scanner details as well security info details
        try {
        	securityInfoURL = channelProps.getProperty("subscriptionplugin.securityinfo.url");
        	securityInfoUserName = channelProps.getProperty("subscriptionplugin.securityinfo.subscribeuser");
        	securityInfoPassword = channelProps.getProperty("subscriptionplugin.securityinfo.subscribepassword");
        	customScannerURL = channelProps.getProperty("subscriptionplugin.customscanner.url");
        	customScannerUserName = channelProps.getProperty("subscriptionplugin.customscanner.subscribeuser");
        	customScannerPassword = channelProps.getProperty("subscriptionplugin.customscanner.subscribepassword");
        } catch(Exception ed) {
        	
        }
    }

    public ISubsPluginContext getPluginContext() {
        return ctx;
    }

    public PluginConnection getFreshPluginConnection() {
        try {
            return new PluginConnection(httpMgr, httpConfig, master);
        } catch(Throwable t) {
            debug(INFO, "getFreshPluginConnection(), Exception while trying to create plugin connection object to master plugin...");
            return null;
        }
    }

    public boolean needToForwardRequests() {
        if(master == null) {
            /* If this is a "master" plugin, immediately refuse to forward requests */
            debug(INFO, "needToForwardRequests(), This is a master plugin, so will NOT forward!");
            return false;
        } else {
            if(repeaterInsert) {
                debug(INFO, "needToForwardRequests(), repeater.insert is true, so will NOT forward!");
            } else {
                debug(INFO, "needToForwardRequests(), repeater.insert is false, so WILL forward requests!");
            }
            return !repeaterInsert; /* If this is not a master plugin, only forward requests if you are not allowed to insert directly */
        }
    }

    public IDataStorage getStorage() {
        return storage;
    }

    public IDataStorage getStorage(String storageType) {
        if ("file".equals(storageType)) return fileStorage;
        if ("bigdata".equals(storageType)) return bigDataStorage;
        if ("db".equals(storageType)) return dbStorage;
        return null;
    }

    public String getDataDir() {
        return ctx.getPluginContext().getDataDirectory();
    }

    public String getNewGUID() {
        com.marimba.tools.net.UUID uuid = new com.marimba.tools.net.UUID();
        return uuid.toHexStr().toString();
    }

    public String getPluginSubscribeCredentials() {
        String credentials = null;

        String userName = channelProps.getProperty("subscriptionplugin.subscribeuser");
        String password = channelProps.getProperty("subscriptionplugin.subscribepassword");

        if(userName != null && password != null) {
            try {
                credentials = toAuthString(userName, Password.decode(password));
                debug(INFO, "getPluginSubscribeCredentials(), getCredential will be returning: " + credentials);
            } catch(Throwable t) {
                if(ERROR) {
                    t.printStackTrace();
                }
            }
        }

        return credentials;
    }

    /**
     * Compute an authorization string using the basic authentication scheme.
     */
    public String toAuthString(String user, String password) {
        StringBuffer buf = new StringBuffer();
        buf.append("Basic ");
        buf.append(Password.encode(user + ":" + password));
        return buf.toString();
    }

    public void log(int id, int severity, String s) {
        ctx.getPluginContext().log(id, -1, severity, "plugin.module", System.getProperty("user.name"), s);
    }

    public void debug(boolean debugType, String msg) {
        if (debugType) {
            ctx.logToConsole("SubscriptionPlugin.java -- " + msg);
        }
    }

    public void debug(boolean debugType, String msg, Throwable cause) {
        if (debugType) {
            ctx.logToConsole("SubscriptionPlugin.java -- " + msg, cause);
        }
    }

    public synchronized void resetRetry() {
        retryCount = 0;
        timerStarted = false;
        debug(WARNING, "resetRetry(), Times up!  Reset retry count to 0" + new Date(System.currentTimeMillis()));
    }

    //
    //	We try for five requests to initialize the Plugin, then rest for retryInterval(seconds)
    //  before we start retrying again
    //
    private void retryInitDataSource(IRequest request) {
        synchronized (rejectReq) {
            rejectReq = Boolean.TRUE;
        }
        synchronized (this) {
            ctx.log(LOG_RETRY_DATA_SOURCE, LOG_AUDIT, request.getRemoteAddress().getHostAddress(), request.getTunerID() + "/" + request.getUserID(), new Integer(retryCount));
            try {
                // Always retry if we there's no retryInterval set.
                // Otherwise, retry only if we are in a retry bundle
                if (((retryInterval <= 0) || retryCount < 5) && dataSource == null) {
                    debug(WARNING, "retryInitDataSource(), retry init: " + retryCount);
                    dataSource = new LDAPDataSource(ctx, cacheRefreshInterval);
                }
            } catch (Throwable th) {
                return;
            } finally {
                if (retryCount < 5) {
                    retryCount++;
                }
                synchronized (rejectReq) {
                    rejectReq = Boolean.FALSE;
                }
                // If there's a retryInterval set, and we have finished the retry bundle,
                // create a timer client to reset the retryCount after the "rest period"
                // if the timer hasn't been started.
                if (retryInterval > 0 && retryCount > 4) {
                    if (!timerStarted) {
                        debug(WARNING, "retryInitDataSource(), startRetryT: " + new Date(System.currentTimeMillis()));
                        timer.remove(startRetryT);
                        startRetryT = new StartRetryTimeout();
                        timer.add(startRetryT, System.currentTimeMillis() + (retryInterval * 1000), this);
                        timerStarted = true;
                    }
                }
            }
        }
    }

    private void updateStatus(IIndex reply) {

        ConfigUtil pluginConfig = new ConfigUtil(ctx.getPluginContext().getPluginConfiguration());
        if(reply != null) {
            int scount = pluginConfig.getInteger("success_count", 0);
            ++scount;
            pluginConfig.setProperty("success_count", ""+scount);
        } else {
            int fcount = pluginConfig.getInteger("failure_count", 0);
            ++fcount;
            pluginConfig.setProperty("failure_count", ""+fcount);
        }

    }

    private void forwardActivationRequest(IHttpRequest iHttpReq) {

        //tuner identifier
        IWorkspace ws = (IWorkspace) ctx.getPluginContext().getFeature("workspace");

        long tunerID = ws.getIdentifier();
        DataInputStream in;

        try {
            in = new DataInputStream (iHttpReq.getInputStream());
            tunerID = in.readLong();
        } catch (IOException header) {
            debug(ERROR, "forwardActivationRequest(), [Master Forward] Reading tuner id failed", header);
        }

        String hostName = iHttpReq.getHeader("hostname");
        String hostIP = iHttpReq.getHeader("hostip");
        String hostDomain = iHttpReq.getHeader("hostdomain");
        String depotLocation = iHttpReq.getHeader("servername");
        String accountUserName = iHttpReq.getHeader("accountusername");
        String accountPwd = iHttpReq.getHeader("accountpwd");
        String accountDomain = iHttpReq.getHeader("accountdomain");
        String templateName = iHttpReq.getHeader("templatename");

        debug(INFO, "forwardActivationRequest(), [Master Forward] Forward Req :" + hostName + "." + hostDomain);
        HTTPConfig httpConfig = new HTTPConfig(new ConfigUtil((IConfig) ctx.getPluginContext().getFeature("config")));
        IConfig tunerConfig = (IConfig) ctx.getPluginContext().getFeature("config");

		// ToDo for selva: please retrieve the channel and tenantName from appropriate place and set it below
//		httpConfig.setChannel();
//		httpConfig.setTenantName();

		HTTPManager httpMgr;
        ISSLProvider ssl = null;
  		ICertProvider cert = null;
  		try {
  			ssl = (ISSLProvider) ctx.getPluginContext().getFeature("ssl");
  			cert = (ICertProvider) ctx.getPluginContext().getFeature("certificates");

  			if (tunerConfig != null && cert != null) {
  				httpMgr = new JavaHTTPSManager(httpConfig, tunerConfig, cert);
  			} else {
  				httpMgr = new HTTPManager(httpConfig);
  			}
  		} catch(Throwable e) {
  			if (DETAILED_INFO) {
				e.printStackTrace();
			}
  			try {
  				if(ssl != null && cert != null) {
  					httpMgr = new HTTPSManager(httpConfig, cert, ssl);
  				} else {
  					httpMgr = new HTTPManager(httpConfig);
  				}
  			} catch (UnsatisfiedLinkError e1) {
                debug(ERROR, "forwardActivationRequest(), Exception occurred while connecting with ssl details : " + e1.getMessage());
                httpMgr = new HTTPManager(httpConfig);
  			} catch (Exception ex) {
                debug(ERROR, "forwardActivationRequest(), Exception occurred while connecting with ssl details : " + ex.getMessage());
  	  			httpMgr = new HTTPManager(httpConfig);
  	  			if (ERROR) {
  	  				ex.printStackTrace();
  	  			}
  	  		}
  		} 

          // create the plugin connection and send the compressed data
        PluginConnection conn = null;
        try {
            conn = new PluginConnection(httpMgr, httpConfig, master);
        } catch (MalformedURLException e) {
        	debug(INFO, "forwardActivationRequest(), FATAL ERROR : Master Connection Failled with exception - " + e);
        }

        if(null != conn) {
			try {
				OutputStream out;
				int reply = -1;
				do {
					try {
						try {
							conn.open();
							conn.setCredential("Basic ");
							conn.setSegment(".configurator");
							conn.setTunerID(tunerID);
							conn.addField("request-type", HTTP_OP_ACTIVATE_PXE);
							conn.addField("Nuke-Connection", String.valueOf(true));

							conn.addField("hostname",hostName);
							conn.addField("hostip",hostIP);
							conn.addField("hostdomain",hostDomain);

							if(null != depotLocation) {
								conn.addField("servername",depotLocation);
							}
							if(null != accountUserName) {
								conn.addField("accountusername",accountUserName);
							}
							if(null != accountPwd) {
								conn.addField("accountpwd",accountPwd);
							}
							if(null != accountDomain) {
								conn.addField("accountdomain",accountDomain);
							}
							if(null != templateName) {
								conn.addField("templatename",templateName);
							}

							String msg = "Activate PXE\nForwarding request to master";
							out = conn.getOutputStream(msg.getBytes().length);
							out.write(msg.getBytes());
						} catch (IOException e) {
							debug(INFO, "forwardActivationRequest(), Writing to Plugin connection stream failed with exception.\n" + e.toString());
                            throw  e;
						}

						reply = conn.reply();
					} catch (IOException ie) {
						debug(INFO, "forwardActivationRequest(), Reading reply from plugin failed with exception :" + ie);
                        if (DETAILED_INFO) {
                            ie.printStackTrace();
                        }
						break;
					}
				} while (reply == HTTP_RETRY);

				switch (reply) {
					case HTTP_OK:
						debug(INFO, "forwardActivationRequest(), [Master Forward] Got reply from Master : HTTP_OK");
                        DataInputStream inReply = new DataInputStream (conn.getInputStream());
						StringBuffer buffer = new StringBuffer("");
						int c;
						try {
							while((c = inReply.read()) != -1) {
								buffer.append((char) c);
							}
							debug(INFO, "forwardActivationRequest(), [Master Forward] Reply from master after forward :" + buffer.toString());
                        } catch (IOException ioe) {
							debug(INFO, "forwardActivationRequest(), [Master Forward] Reading Input Stream error :" + ioe);
                        }
                        break;

					case HTTP_NOT_IMPLEMENTED:
						debug(INFO, "forwardActivationRequest(), [Master Forward] The requested function not implemented in plugin");
                        break;

					case HTTP_INTERNAL_SERVER_ERROR:
						debug(INFO, "forwardActivationRequest(), [Master Forward] Failed to connect or unable to send request");
                        break;

					default:
						debug(INFO, "forwardActivationRequest(), [Master Forward] Http Error while talking to master : " + reply);
                        break;
				}
			} finally {
				if (conn != null) {
					conn.close();
				}
			}
		} else {
			debug(INFO, "forwardActivationRequest(), FATAL : [Master Forward] Unable to create connection with Master");
        }
    }

    /**
     * This class implements the TimerClient.
     * It ticks we are suppose to start the series of retries again
     * after wating "retryInterval" minutes of time.
     */
     private static class StartRetryTimeout implements TimerClient {
        public long tick(long tm, Object arg) {
	        SubscriptionPlugin sp = (SubscriptionPlugin) arg;
	        sp.resetRetry();
	        return -1;
	    }
    }

    /**
     * This periodically ticks just to make sure we have
     * the right number of threads running, just in case
     * some die at a bad time.
     */
    public long tick(long tm, Object arg) {
        ctx.log(LOG_QUEUE_PROCESSING_START, LOG_AUDIT);
        getSecurityScanReportQueue();
        if (security_scan_report_queue != null) {
            debug(INFO, "tick(), security_scan_report_queue.length() - " + security_scan_report_queue.length());
            if (security_scan_report_queue.length() > 0) {
                debug(INFO, "invoke checkFork(), to start threads");
                checkFork();
            }
        }

        long nextTick = tm + (scanReportQueueTimerInterval * 60 * 1000);
        ctx.log(LOG_QUEUE_PROCESSING_END, LOG_AUDIT, nextTick);
        return nextTick;
    }

    /**
     * This is the main loop for the security plugin.  Once this gets a
     * task to run, it forks another thread to pick up the next task
     * after that.  This loop is either forwarding logs to a master, or
     * inserting them into the database.  Meanwhile, the transmitter may
     * notify() this class whenever a plugin connection is made for the
     * purpose of uploading reports.  Those reports are just copied into
     * the log queue as fast as possible, so that this method can do
     * with them what it will.
     */
    public void run() {
        try {
            runInsert();
        } catch (ThreadDeath d) {
            return;
        } catch (Throwable t) {
            if (DETAILED_INFO) {
                t.printStackTrace();
            }
            ctx.log(LOG_ERROR_PROCESSING_QUEUE, LOG_CRITICAL, t);
        } finally {
        }
    }

    private ClassLoader getContextClassLoader() {
        try {
            return Thread.currentThread().getContextClassLoader();
        } catch (Throwable t) {
            return null;
        }
    }

    private ClassLoader getSystemClassLoader() {
        try {
            return ClassLoader.getSystemClassLoader();
        } catch (Throwable t) {
            return null;
        }
    }

    private boolean setContextClassLoader(ClassLoader contextClassLoader) {
        try {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private void runInsert() {
        if (security_scan_report_queue == null) {
            debug(INFO, "runInsert(), Skip processing queue, its NULL");
            return;
        }

        debug(INFO, "runInsert(), queueLength - " + security_scan_report_queue.activeLength() + " :: " + security_scan_report_queue.length());

        try {
            SecurityScanReport lr = getSecurityScanReport();
            debug(INFO, "runInsert(), lr - " + lr);
            if (lr == null) {
                return;
            }

            ISecurityServiceRequest securityServiceRequest = new SendComplianceDetailsRequest();
            boolean result = securityServiceRequest.processQueueReport(lr.getFile(), this);
            debug(INFO, "runInsert(), lr.getFile().getName() - " + lr.getFile().getName() + " ,result - " + result);
            if (result) {
                security_scan_report_queue.nukeReport(lr);
            } else {
                try {
                    security_scan_report_queue.addReport(lr.getFile());
                } catch (Throwable t) {
                    if (DETAILED_INFO) {
                        t.printStackTrace();
                    }
                }
            }
        } catch (Throwable t) {
            if (DETAILED_INFO) {
                t.printStackTrace();
            }
        }
    }

    public void runInsert(File reportFile) {
        if (security_scan_report_queue == null) {
            debug(INFO, "runInsert(File), Skip processing queue, its NULL");
            return;
        }
        try {
            debug(INFO, "runInsert(File), Processing queued report - " + reportFile.getName());

            ISecurityServiceRequest securityServiceRequest = new SendComplianceDetailsRequest();
            boolean result = securityServiceRequest.processQueueReport(reportFile, this);
            debug(INFO, "runInsert(File), Processing queued report - " + reportFile.getName() + " ,result - " + result);
            if (result) {
                security_scan_report_queue.nukeReport(reportFile.getName());
            } else {
                try {
                    security_scan_report_queue.addReport(reportFile);
                } catch (Throwable t) {
                    if (DETAILED_INFO) {
                        t.printStackTrace();
                    }
                }
            }
        } catch (Throwable t) {
            if (DETAILED_INFO) {
                t.printStackTrace();
            }
        }
    }

    /**
     * Get a security scan report from the queue.  Returns null if it times out or
     * we're shutting down.  Retries if there are errors loading the report.
     */
    private SecurityScanReport getSecurityScanReport() {
        try {
            if(threadgroup == null) {
                return null;
            }
            SecurityScanReportQueue scanReportQueue = getSecurityScanReportQueue();
            if (scanReportQueue != null) {
                return scanReportQueue.getReport(10000);
            }
        } catch (InterruptedException e) {
            return null;
        } finally {
        }
        return null;
    }

    public SecurityScanReportQueue getSecurityScanReportQueue() {
        if (security_scan_report_queue == null) {
            destroyScanReportQueue();
            initScanReportQueue();
        }
        return security_scan_report_queue;
    }

}
