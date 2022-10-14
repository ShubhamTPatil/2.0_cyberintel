// Copyright 2017-2020, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.LogConstants;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantAttributes;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.compliance.core.ComplianceMain;
import com.marimba.apps.subscriptionmanager.compliance.core.ComplianceServiceLoader;
import com.marimba.apps.subscriptionmanager.distribution.DistributionServiceLoader;
import com.marimba.apps.subscriptionmanager.intf.IARTaskConstants;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.users.UserManager;
import com.marimba.apps.subscriptionmanager.webapp.util.*;
import com.marimba.apps.subscriptionmanager.webapp.util.push.DMHelper;
import com.marimba.apps.subscriptionmanager.ws.WebServiceHelper;
import com.marimba.apps.subscriptionmanager.ws.subscribe.SubscribeService;
import com.marimba.intf.application.IApplicationContext;
import com.marimba.intf.castanet.IChannel;
import com.marimba.intf.castanet.IWorkspace;
import com.marimba.intf.logs.ILog;
import com.marimba.intf.msf.*;
import com.marimba.intf.msf.task.ITaskMgr;
import com.marimba.intf.msf.websvc.IWebSvcRegistry;
import com.marimba.intf.token.ITokenProvider;
import com.marimba.intf.util.*;
import com.marimba.tools.config.ConfigProps;
import com.marimba.tools.regex.Matcher;
import com.marimba.tools.token.PropsEncrypted;
import com.marimba.tools.util.Props;
import com.marimba.webapps.intf.CriticalException;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.tools.util.WebAppUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.PropertyConfigurator;
import org.apache.struts.Globals;
import org.apache.struts.util.MessageResources;
import org.apache.struts.util.MessageResourcesFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * Servlet that creates SubscriptionMain and place it in the session.   Any one time initialization for the SubscriptionManager should be done here.
 *
 * @author Theen-Theen Tan
 * @version 1.42, 04/23/2003
 */
public class SubscriptionInitServlet extends HttpServlet implements IObserver, IWebAppConstants,
        IErrorConstants, ISubscriptionConstants {

    File dir;
    ILog applog;
    IProducer app;
    IServer server;
    File configFile;
    ConfigProps props;
    IProducer producer;
    IDirectory features;
    IConfig tunerConfig;
    ServletConfig config;
    SubscriptionMain main;
    ServletContext context;
    IWebApplicationInfo info;
    IWebSvcRegistry registry;
    SubscribeService subsService;
    ITenantManager tenantMgr;
    private static Map<String, TenantAttributes> tenantsInfo;

    final static String TRUE = "true";
    final static String USER_DIR = "users";
    final static String EMERGENCY_USER = "admin";
    final static String CONFIG_FILE = "properties.txt";
    String UPGRADE_FILE = "upgrade_properties.txt";
    final static String COMPLIANCEMAIN ="compliancemain";

    private static boolean sIsDebugEnabled = DEBUG;
    private static PrintStream sDebugPrintStream = System.out;
    private static final String sLogPropertiesFilenameString = "/log4j.properties";

    private static final Logger sLogger = Logger.getLogger(SubscriptionInitServlet.class);

    static {
        Properties myLogProperties = new Properties();
        try {
            myLogProperties.load(SubscriptionInitServlet.class.getResourceAsStream(sLogPropertiesFilenameString));
        } catch (IOException e) {
            e.printStackTrace(sDebugPrintStream);
        }
        PropertyConfigurator.configure(myLogProperties);
    }

    static Matcher matcher;
    static {
        matcher = new Matcher();
        // We want to protect following passwords
        // subscriptionmanager.bindpasswd
        // cms.dirs.password
        matcher.add("re:.*passw.*", matcher);
        matcher.prepare();
    }

    protected static void debugEntered(String inMethodNameString) {
        if (sIsDebugEnabled) {
            sLogger.info(inMethodNameString + ": entered");
        }
    }

    protected static void debugLeaving(String inMethodNameString, Object inMessageObject) {
        if (sIsDebugEnabled) {
            sLogger.info(inMethodNameString + ": leaving: " + inMessageObject);
        }
    }

    protected static void debugLeaving(String inMethodNameString) {
        if (sIsDebugEnabled) {
            sLogger.info(inMethodNameString + ": leaving: <void>");
        }
    }

    protected static void debugMessage(String inMethodNameString, Object inMessageObject) {
        if (sIsDebugEnabled) {
            sLogger.info(inMethodNameString + ": " + inMessageObject);
        }
    }

    /**
     * REMIND
     *
     * @param config REMIND
     * @throws ServletException REMIND
     */
    public void init(ServletConfig config) throws ServletException {
        String dCurrentMethod = "init()";
        debugEntered(dCurrentMethod);

        this.config = config;
        context = config.getServletContext();

        app = (IProducer) context.getAttribute("com.marimba.servlet.context.producer");
        applog = (ILog) context.getAttribute("com.marimba.servlet.context.log");
        app.addObserver(this, 0, 0);

        dir = (File) context.getAttribute("com.marimba.servlet.context.data");
        features = (IDirectory) context.getAttribute("com.marimba.servlet.context.features");

        tenantMgr = (ITenantManager)features.getChild("tenantMgr");
        tenantMgr.addObserver(this, ITenantConstants.TENANT_EVENT_MIN, ITenantConstants.TENANT_EVENT_MAX);
        server = (IServer) features.getChild("server");

        registry = (IWebSvcRegistry) server.getManager("webSvcRegistry");
        info = ((IWebApplicationMgr) server.getWebApplicationMgr()).getApplication(SUBSCRIPTION_PATH);
        producer = (IProducer) context.getAttribute("com.marimba.servlet.context.producer");

        //initialize message resources
        initMessageResources();

        // load tenant specific config
        tenantsInfo = new Hashtable<String, TenantAttributes>();
        iterateTenantsConfig();

        this.tunerConfig = (IConfig) features.getChild("tunerConfig");
        this.tunerConfig.addObserver(this, 0, 0);
        loadSCAPContentDetails();
        loadCveDetailsFromDB();
    }

    private void loadSCAPContentDetails() {
        try {
            IChannel channel = info.getChannel();
            IApplicationContext iApplicationContext = (IApplicationContext) features.getChild("context");
            final SCAPUtils scapUtils = SCAPUtils.getSCAPUtils();
            scapUtils.setDebug(DEBUG);
            scapUtils.setManagerChannelURL(channel.getURL());
            scapUtils.init(context, iApplicationContext, main);
            new Thread() {public void run() {scapUtils.readStandardSCAPContents("scap");}}.start();
            new Thread() {public void run() {scapUtils.readStandardSCAPContents("usgcb");}}.start();
            new Thread() {public void run() {scapUtils.readStandardSCAPContents("custom");}}.start();
        } catch(Exception ed) {
            ed.printStackTrace();
        }
    }

    private void loadCveDetailsFromDB() {
        try {
            final SCAPUtils scapUtils = SCAPUtils.getSCAPUtils();
            for (String tenantName: tenantsInfo.keySet()) {
                if ((scapUtils.getOvalDefinitionDetails() == null) || (scapUtils.getOvalCVEDefinitionDetails() == null)) {
                    SubscriptionMain tenantMain = tenantsInfo.get(tenantName).getTenantMain();
                    scapUtils.loadDbData(tenantMain);
                }
            }
        } catch(Exception ed) {
            ed.printStackTrace();
        }
    }

    /**
     * Load tenant configuration
     */
    private void iterateTenantsConfig() {
        // collect all active tenant objects
        Collection<ITenant> tenantColl = tenantMgr.getTenants(SUBSCRIPTION_PATH);

        // load tenant specific config
        for(ITenant policyTenant : tenantColl) {
            System.out.println("loading details for " + policyTenant.getName());
            try {
                loadTenant(policyTenant);
            } catch(Exception ed) {
                ed.printStackTrace();
            } catch(Throwable e) {
                e.printStackTrace();
            }
        }
        context.setAttribute(IWebAppConstants.TENANT_ATTRUBUTES, tenantsInfo);
    }

    private void loadTenant(ITenant tenant) {
//    	this.tenant = tenantObj;
        // Add observer for all tenants
        tenant.addObserver(this, ITenantConstants.LDAP_EVENT_MIN, ITenantConstants.TENANT_EVENT_MAX);
        String dCurrentMethod = "loadTenant";

        dir = TenantHelper.getTenantDataFolder(dir, tenant.getName());

        debugMessage(dCurrentMethod, "dir for persist = " + dir.toString());

        if (!dir.isDirectory()) {
            debugMessage(dCurrentMethod, "creating the persist directory");
            dir.mkdirs();
        }

        initConfigFile(tenant);
        initMain(tenant);
    }

    public ITenantManager getTenantMgr() {
        return tenantMgr;
    }

    private void initConfigFile(ITenant tenant) {
        final String dCurrentMethod = "initConfigFile(String)";
        configFile = new File(dir, CONFIG_FILE);

        ITokenProvider tokenProvider = (ITokenProvider) features.getChild("tokenProvider");
        if(tokenProvider != null) {
            debugMessage(dCurrentMethod, "Running 7.0 tuner, hence using Token provider for the Config file");

            Props encryptionEnabledProps = new PropsEncrypted(tokenProvider, configFile, -1, -1, matcher);
            encryptionEnabledProps.load();
            props = new ConfigProps(encryptionEnabledProps, null);
        } else {
            debugMessage(dCurrentMethod, "Running pre 7.0 tuner, Config file will be processed normally");

            props = new ConfigProps(configFile);
        }

        if (!configFile.exists()) {
            // Need to copy properties from servlet-config
            debugMessage(dCurrentMethod, "Loading the servlet-config properties");

            for (Enumeration e = config.getInitParameterNames(); e.hasMoreElements();) {
                String key = (String) e.nextElement();
                String val = config.getInitParameter(key);
                props.setProperty(key, val);
            }
        }
        IConfig tenantConfig = tenant.getConfig();
        // initialize default properties
        // Upgrade case.
        // In 9.0.00 release, WOW feature enabled option moved to CMS
        // due to WOW related other settings are available in CMS
        if( null != props.getProperty(IWebAppConstants.OLD_ENABLE_WOW_FEATURE)) {
            tenantConfig.setProperty(IWebAppConstants.ENABLE_WOW_FEATURE, props.getProperty(IWebAppConstants.OLD_ENABLE_WOW_FEATURE));
            props.setProperty(IWebAppConstants.OLD_ENABLE_WOW_FEATURE, null);
        }

        boolean saved = props.save();
        debugMessage(dCurrentMethod, "props file saved successfully? saved=" + saved + "; configFile=" + configFile);
    }

    /**
     * Add the Scrubber and Cache compliance tasks to the TaskManager in CMS.
     * @param taskMgr handle to the TaskManager
     */
    private void createTasks(ITaskMgr taskMgr) {
        try{
            IProperty subconfig =  main.getSubscriptionConfig();
        } catch(Exception et) {
            sLogger.log(Priority.ERROR, et);
            et.printStackTrace(System.err);
        }
    }

    private static String getSubConfigProp(IProperty subconfig, String prop) {
        String value = subconfig.getProperty(prop);
        if (value == null){
            value = "";
        }
        return value;
    }

    /**
     * Handle notifications from CMS
     *
     * @param sender REMIND
     * @param msg    REMIND
     * @param args   REMIND
     */
    public void notify(Object sender, int msg, Object args) {
        final String dCurrentMethod = "notify(Object,int,Object)";
        debugEntered(dCurrentMethod);

        IWebAppUserEvent evt = null;

        debugMessage(dCurrentMethod, "msg=" + msg);
        debugMessage(dCurrentMethod, "args=" + args);

        ITenantManager tenantManager = null;
        ITenant tenant = null;
        String tenantName = null;

        switch (msg) {
            case WebAppConstants.WEBAPP_USER_LOGON:
                evt = (IWebAppUserEvent) args;
                IUserPrincipal userPrincipal = (IUserPrincipal) evt.getUser();

                try {
                    if(!EMERGENCY_USER.equalsIgnoreCase(evt.getUser().getName()) && null != userPrincipal.getTenantName()) {
                        tenantName = userPrincipal.getTenantName();
                        System.out.println("Logged Tenant Name :" + tenantName);
                        tenant = tenantMgr.getTenant(tenantName);
                    }

                    createUser(evt, tenant);

                    log(LogConstants.LOG_USER_LOGIN, userPrincipal.getName() + " (" + userPrincipal.getFullName() +")", LOG_AUDIT, userPrincipal.getName() + " (" + userPrincipal.getFullName() +")", null, POLICY_USER_LOGIN);
                } catch (Exception e) {
                    log(LogConstants.LOG_LOGIN_FAILED, userPrincipal.getName() + " (" + userPrincipal.getFullName() +")", LOG_CRITICAL, userPrincipal.getName() + " (" + userPrincipal.getFullName() +")", e);
                }

                break;

            case WebAppConstants.WEBAPP_USER_LOGOFF:
                evt = (IWebAppUserEvent) args;

                try {
                    HttpSession session = evt.getSession();
                    IUser sesuser = (IUser) session.getAttribute(SESSION_SMUSER);
                    UserManager.removeUser(sesuser);
                    //Unload service objects from the session.
                    ComplianceServiceLoader.unloadServiceObjects(session);
                    DistributionServiceLoader.unloadServiceObjects(session);
                    IUserPrincipal iUserPrincipal = ((IUserPrincipal) evt.getUser());
                    log(LogConstants.LOG_USER_LOGOUT, iUserPrincipal.getName() + " (" + iUserPrincipal.getFullName() +")", LOG_AUDIT, iUserPrincipal.getName() + " (" + iUserPrincipal.getFullName() +")", null, POLICY_USER_LOGOUT);
                } catch (Exception e) {
                    IUserPrincipal iUserPrincipal = ((IUserPrincipal) evt.getUser());
                    log(LogConstants.LOG_USER_LOGOUT_FAILED, iUserPrincipal.getName() + " (" + iUserPrincipal.getFullName() +")", LOG_CRITICAL, iUserPrincipal.getName() + " (" + iUserPrincipal.getFullName() +")", e);
                }

                break;

            case WebAppConstants.WEBAPP_AUTH_CHANGED:

                // Reconnect to LDAP with the new LDAP settings
                // This is assuming that all logged in user
                // will be forced to log out by CMS when CMS LDAP settings
                // changed.
                System.out.println("LDAP authentication changed");
//                String str = (String) args;
//                IUserPrincipal userPrincipal = (IUserPrincipal) evt.getUser();
//                if (!"local".equals(str)) {
//                	initMessageResources();
//                    initMain();
//                }
//
//                // Clear out the context variable that indicates that there was
//                // an initialization error.  This is so that other users
//                // will be able to pass through since the problem has been
//                // fixed.
//                context.removeAttribute(IWebAppsConstants.INITERROR_KEY);

                break;

            case IServer.CMS_SERVICE_REMOVED:
                String removedServiceName = (String) args;
//                if (PatchManagerHelper.PATCH_MANAGER_SERVICE_NAME.equals(removedServiceName)) {
//	                    if(DEBUG3) {
//	                    	System.out.println("SPM - notified Patch manager stopped");
//	                    }
//                    PatchManagerHelper.destroy();
//                }
                break;

            case IServer.CMS_SERVICE_ADDED:
                String addedServicenName = (String) args;
//                if (PatchManagerHelper.PATCH_MANAGER_SERVICE_NAME.equals(addedServicenName)) {
//                	if(null != tenantMgr) {
//	                	Collection<ITenant> tenantColl = tenantMgr.getTenants(SUBSCRIPTION_PATH);
//
//	                	// load patch manager config based on tenant
//	                    for(ITenant policyTenant : tenantColl) {
//	                    	PatchManagerHelper.initialize(policyTenant);
//	                    }
//                	}
//                }
                break;

            case IConfig.CONFIG_CHANGED:
                if (sender == tunerConfig) { //only for tuner config changes
                    String key = (String) args;
                    String val = tunerConfig.getProperty(key);
                    //System.out.println("IConfig.CONFIG_CHANGED, " + key + "=" + val);
                    IWorkspace workspace = (IWorkspace) main.getFeatures().getChild("workspace");
                    String securityInfoChnlUrl = main.getConfig().getProperty("subscriptionmanager.securityinfo.url");
                    IChannel channel = workspace.getChannel(securityInfoChnlUrl);
                    if (key.equalsIgnoreCase("marimba.securityinfo.sync.status." + channel.getURL())) {
                        if ("insync".equals(val)) {
                        	// After completion of content security synchronization from updates tab, vDesk channel should be restarted otherwise latest contents are not reflected
                            //System.out.println("vDef updated to latest, reloading contents from vDesk");
                            //loadSCAPContentDetails();
                        }
                    }
                }
                break;

            case ITenantConstants.TENANT_ADDED:
                tenantName = (String) args;
                // todo : if new tenant assign permission for policy manager then you take action based on selection
                System.out.println("Added new tenant :" + tenantName);
                tenantManager = (ITenantManager)  sender;
                tenant = tenantManager.getTenant(tenantName);
                loadTenant(tenant);
                context.setAttribute(IWebAppConstants.TENANT_ATTRUBUTES, tenantsInfo);
                break;

            case ITenantConstants.TENANT_REFRESHED:
                tenantName = (String) args;
                // todo : if new tenant assign permission for policy manager then you take action based on selection
                System.out.println("Added new tenant :" + tenantName);
                tenantManager = (ITenantManager)  sender;
                tenant = tenantManager.getTenant(tenantName);
                loadTenant(tenant);
                context.setAttribute(IWebAppConstants.TENANT_ATTRUBUTES, tenantsInfo);
                break;

            case ITenantConstants.TENANT_UPDATED:
                // on hold
                // this part covered LDAP_CONFIG_ADDED LDAP_CONFIG_UPDATED LDAP_CONFIG_REMOVED TENANT_APPS_CHANGED
                System.out.println("Existing tenant is updated");
                break;

            case ITenantConstants.TENANT_REMOVED:
                tenantName = (String) args;
                System.out.println("Existing tenant is removed :" + tenantName);
                File removeFile = (File) context.getAttribute("com.marimba.servlet.context.data");
                System.out.println("Data Directory :" + removeFile.getAbsolutePath());
                String removeFilePath = removeFile.getAbsolutePath() + File.separator + tenantName;
                System.out.println("Remove File Path :" + removeFilePath);
                boolean status = FileUtils.removeDirectory(new File(removeFilePath));
                System.out.println("Tenant removed : " + status);
                break;

            case ITenantConstants.TENANT_ACTIVATED:
                tenantName = (String) args;
                System.out.println("Tenant is activated : " + tenantName);
                tenantManager = (ITenantManager)  sender;
                tenant = tenantManager.getTenant(tenantName);
                loadTenant(tenant);
                context.setAttribute(IWebAppConstants.TENANT_ATTRUBUTES, tenantsInfo);
                break;

            case ITenantConstants.TENANT_DEACTIVATED:
                tenantName = (String) args;
                System.out.println("Tenant is deactivated : " + tenantName);
                TenantHelper.deActivateTenant(context, tenantName, false);
                break;

            case ITenantConstants.LDAP_CONFIG_ADDED:
                tenant = (ITenant) sender;
                System.out.println("New LDAP is added into tenant : " + tenant.getName());
                loadTenant(tenant);
                context.setAttribute(IWebAppConstants.TENANT_ATTRUBUTES, tenantsInfo);
                break;

            case ITenantConstants.LDAP_CONFIG_UPDATED:
                tenant = (ITenant) sender;
                System.out.println("Existing LDAP is modified into tenant : " + tenant.getName());
                loadTenant(tenant);
                context.setAttribute(IWebAppConstants.TENANT_ATTRUBUTES, tenantsInfo);
                break;

            case ITenantConstants.LDAP_CONFIG_REMOVED:
                tenant = (ITenant) sender;
                System.out.println("Existing LDAP is deleted from tenant : " + tenant.getName());
                loadTenant(tenant);
                context.setAttribute(IWebAppConstants.TENANT_ATTRUBUTES, tenantsInfo);
                break;

            case ITenantConstants.TENANT_APPS_CHANGED:
                tenantManager = (ITenantManager)  sender;
                tenant = (ITenant) args;
                System.out.println("Application Permission changed for tenant : " +  tenant.getName());
                loadTenant(tenant);
                context.setAttribute(IWebAppConstants.TENANT_ATTRUBUTES, tenantsInfo);
                break;
        }

        debugLeaving(dCurrentMethod);
    }

    private void createUser(IWebAppUserEvent evt, ITenant tenant) {
        IConfig serverConfig = server.getConfig(); // REMIND: ttt,dev,ah: Is this done for side-effect only? We don't use it below...
        if(null == tenant || null == tenant.getName()) {
            evt.getSession().setAttribute("emergencyUser", "true");
            WebAppUtils.saveInitException(evt.getSession(), new CriticalException(LDAP_CONNECT_EMERGENCYUSER));

            return;
        }
        if(null != tenant && !hasLDAPConfig(tenant)) {
            WebAppUtils.saveInitException(evt.getSession(), new CriticalException("error.ldap.connect.failed"));

            return;
        }
        // Check if there was SPM initialization exceptions stored in the context
        Exception ex = TenantHelper.getTenantInitError(context, tenant.getName());

        if (ex != null) {
            loadTenant(tenant);
            Exception refreshEx = TenantHelper.getTenantInitError(context, tenant.getName());
            if(null != refreshEx) {
                if (refreshEx instanceof CriticalException) {
                    WebAppUtils.saveInitException(evt.getSession(), (CriticalException) refreshEx);

                    return;
                } else if (refreshEx instanceof SystemException) {
                    SystemException syEx = (SystemException) refreshEx;
                    WebAppUtils.saveInitException(evt.getSession(), new CriticalException(syEx, syEx.getKey()));

                    return;
                }

                WebAppUtils.saveInitException(evt.getSession(), new CriticalException(ex, SYSTEM_INIT_FAIL));

                return;
            }
        }

        // Make sure LDAP Settings is set up on CMS
        if (!hasLDAPConfig(tenant)) {
            WebAppUtils.saveInitException(evt.getSession(), new CriticalException(LDAP_CONNECT_LDAPCONFIG_NOTFOUND));

            return;
        }

        // Make sure the logged on user is not the Emergency admin user
        // We just check against the hardcoded user name "admin"
        if (EMERGENCY_USER.equals(evt.getUser().getName())) {
            evt.getSession().setAttribute("emergencyUser", "true");
            WebAppUtils.saveInitException(evt.getSession(), new CriticalException(LDAP_CONNECT_EMERGENCYUSER));

            return;
        }

        IUser newuser = null;

        try {
            HttpSession session = evt.getSession();
            newuser = UserManager.createUser((IUserPrincipal) evt.getUser(), session, evt.getProperty("password"));
            session.setAttribute(SESSION_SMUSER, newuser);
            session.setAttribute(SESSION_TENANTNAME, tenant.getName());
            ComplianceServiceLoader.loadServiceObjects(session, tenant);
            DistributionServiceLoader.loadServiceObjects(session);

        } catch (CriticalException ce) {
            WebAppUtils.saveInitException(evt.getSession(), ce);
            evt.getSession()
                    .removeAttribute(SESSION_SMUSER);
        } catch (SystemException syEx) {
            WebAppUtils.saveInitException(evt.getSession(), new CriticalException(syEx, syEx.getKey()));
            evt.getSession()
                    .removeAttribute(SESSION_SMUSER);
        } catch (Exception se) {
            WebAppUtils.saveInitException(evt.getSession(), new CriticalException(se, SYSTEM_INIT_FAIL));
            evt.getSession()
                    .removeAttribute(SESSION_SMUSER);
        }
    }

    /**
     * Destroy everything here
     */
    public void destroy() {
        final String dCurrentMethod = "destroy()";
        debugEntered(dCurrentMethod);
        //Un-registering the service with the CMS Server.

        if (registry != null) {
            if(main != null) {
                WebServiceHelper wsHelper = new WebServiceHelper(main);
                wsHelper.delete(registry);
                debugMessage(dCurrentMethod, "Successfully Un-published Compliance Verification service to CMS Registry");

                if(subsService != null) {
                    subsService.unregister();
                    debugMessage(dCurrentMethod, "Successfully Un-published Subscribe Servicce from CMS Registry");
                }
            }
        } else {
            debugMessage(dCurrentMethod, "Failed to Un-publish the Compliance Verification service : Could not find CMS Webregistry");
        }

        closeTaskService();

        if (main != null) {
            main.destroy();
        }

        if (props != null) {
            props.close();
        }
        DMHelper.destroy();
        context.removeAttribute(IWebAppsConstants.APP_MAIN);
        context.removeAttribute(IWebAppsConstants.INITERROR_KEY);
        closeComplianceMain();
        closeApprovalStorageConnection();
        removeTenantAttributes();
        app.removeObserver(this);
        server.removeObserver(this);
        if (null != tunerConfig) tunerConfig.removeObserver(this);
        debugLeaving(dCurrentMethod);
        SCAPUtils.getSCAPUtils().destory();
    }
    private void closeTaskService() {
        try {
            Collection<ITenant> tenantColl = tenantMgr.getTenants(SUBSCRIPTION_PATH);

            // load tenant specific config
            for(ITenant policyTenant : tenantColl) {
                policyTenant.setManager(IARTaskConstants.VERIFY_TASK_SERVICE, null);
            }
        } catch(Exception ec) {

        }
    }
    private void closeApprovalStorageConnection() {
        try {
            Map<String, TenantAttributes> tenantsAttr = (Map<String, TenantAttributes>) context.getAttribute(IWebAppConstants.TENANT_ATTRUBUTES);
            if(null == tenantsAttr) return;
            String[] tenantNames = tenantMgr.getTenantNames();
            for(String tenantName : tenantNames) {
                TenantAttributes currentTenantAttr = tenantsAttr.get(tenantName);
                SubscriptionMain subMain = currentTenantAttr.getTenantMain();
                if(null != subMain) {
                    subMain.closeDBStorage();
                }
            }
        } catch(Exception ec) {
        }
    }
    private void closeComplianceMain() {
        try {
            Map<String, TenantAttributes> tenantsAttr = (Map<String, TenantAttributes>) context.getAttribute(IWebAppConstants.TENANT_ATTRUBUTES);
            if(null == tenantsAttr) return;
            String[] tenantNames = tenantMgr.getTenantNames();
            for(String tenantName : tenantNames) {
                TenantAttributes currentTenantAttr = tenantsAttr.get(tenantName);
                ComplianceMain compMain = currentTenantAttr.getCompMain();
                if(null != compMain) {
                    compMain.shutdown();
                }
            }
        } catch(Exception ec) {
        }
    }
    private void removeTenantAttributes() {
        if(null != context && null != context.getAttribute(IWebAppConstants.TENANT_ATTRUBUTES))
            context.removeAttribute(IWebAppConstants.TENANT_ATTRUBUTES);
    }

    /**
     * Returns true if CSF is set to use LDAP and the LDAP server configuration is complete.
     *
     * @return REMIND
     */
    private boolean hasLDAPConfig(ITenant tenant) {
        return (!"local".equals(tenant.getAccessControlMgr().getActive()));
    }

    private void log(int id,
                     String arg1,
                     int severity,
                     String user,
                     Throwable ex) {
        log(id, arg1, severity, user, ex, null);
    }
    private void log(int id,
                     String arg1,
                     int severity,
                     String user,
                     Throwable ex,
                     String target) {
        if (null != main) {

            logaudit(main.getAppLog(), main.getLogResources(), id, arg1, null, null, severity, user, ex, target);

        } else {
            if (null != ex) {
                ex.printStackTrace();
            }
        }
    }
    public void logaudit(ILog log, MessageResources logres, int logid, String arg1,
                         String arg2, String arg3, int severity, String user, Throwable exception, String target) {

        String stlogid = Integer.toString(logid);
        //Get the message from the application resources
        String msg = getMessage(logres,Locale.getDefault(), stlogid,arg1,arg2,arg3);
        main.log(logid, severity, "vDesk", user, msg, target);
    }
    public String getMessage(MessageResources msgres, Locale locale, String msgid,
                             String arg1, String arg2, String arg3) {
        Object[] args = new Object[3];
        args[0] = arg1;
        if (null != arg2) {
            args[1] = arg2;
        } else {
            args[1] = "";
        }
        if (null != arg3) {
            args[2] = arg3;
        }
        return msgres.getMessage(locale,msgid,args);
    }
    private void initMessageResources() {
        try {
            //Sometimes the application resources file not available in context.
            //When the file not available, reload from disk
            //Refer Defect : SW00430697 - Policy Manager throwing NPE during CMS restart
            MessageResources msgres = (MessageResources) context.getAttribute(Globals.MESSAGES_KEY);
            if(null == msgres) {
                MessageResourcesFactory factoryObject = MessageResourcesFactory.createFactory();
                msgres = factoryObject.createResources("/WEB-INF/classes/ApplicationResources");
                context.setAttribute(Globals.MESSAGES_KEY, msgres);
            }
        } catch(Exception ec) {
            debugMessage("initMessageResources", "Failed to initialize Message Resources");
            if(DEBUG5) {
                ec.printStackTrace();
            }
        }
    }

    private void loadDefaultPowerTemplates() {
        try {
            InputStream is = context.getResourceAsStream("/custom-power.properties");
            if (null != is) {
                Properties props = new Properties();
                props.load(is);
                context.setAttribute(POWER_TEMPLATES, props);
            }
        } catch (IOException e) {
            debugMessage("loadDefaultPowerTemplates", "Failed to initialize Power Templates");
            if(DEBUG5) {
                e.printStackTrace();
            }
        }
    }
    private void initMain(ITenant tenant) {
        String dCurrentMethod = "initMain()";
        debugEntered(dCurrentMethod);
        TenantAttributes tenantAttr = new TenantAttributes(tenant);

        try {
            main = new SubscriptionMain(context, props, features, dir, applog, server, tenant);
            tenantAttr.setTenantMain(main);

            // Policy Compliance
            ComplianceMain compMain = new ComplianceMain();
            compMain.setSubscriptionMain(main);
            compMain.init();
            // Servlet context should set after initialize complaince main because
            // when initialize context time, config manager to be initialized
            compMain.setServletContext(context);

            // adding compliance main into tenant attributes
            tenantAttr.setCompMain(compMain);

            main.initLDAP();
            main.loadSiteBasedInfo();

            // Exposing Policy Manger API
//            PolicyManagement policyAPI = new PolicyManagement();
//            ObjectManagerBridge objMgr = new ObjectManagerBridge();
//            objMgr.init(main);
//            policyAPI.setObjectManager(objMgr);
            //tenant.setManager(IPolicyManagement.SERVICE_NAME, policyAPI);
            tenant.setManager("smmain", main);

            // DM Integration
            //DMHelper.getInstance(tenant.getConfig(), tenant.getName());
            context.setAttribute(APP_PROC_TXMEMBER, new SiteListProcessor());
            context.setAttribute(APP_PROC_MEMBER, new MemberListProcessor());
            context.setAttribute(APP_PROC_TXMEMBER, new TxMemberListProcessor());

            debugMessage(dCurrentMethod, "Successfully initialized vDesk...");
        } catch (Exception se) {
            // There are no users logged on at this point
            // We save the error in the context, which is checked
            // when a user logs into the SPM.
            // This error is never cleared out unless SubscriptionManager is
            // restarted.
//            sLogger.log(Priority.WARN, "Subscription Manager failed to initialize! " + se.getMessage(),se);
            System.out.println("vDesk failed to initialize! " + se.getMessage());
            debugMessage(dCurrentMethod, "Failed to initialize vDesk...");
            se.printStackTrace();
            if (DEBUG2) {
                se.printStackTrace();
            }

            if (!hasLDAPConfig(tenant)) {
                tenantAttr.setInitError(new CriticalException(se, LDAP_CONNECT_LDAPCONFIG_NOTFOUND));
            } else {
                tenantAttr.setInitError(se);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }

        tenantsInfo.put(tenant.getName(), tenantAttr);
        UserManager.setTenantsAttributes(tenantsInfo);
        debugLeaving(dCurrentMethod);
    }
}
