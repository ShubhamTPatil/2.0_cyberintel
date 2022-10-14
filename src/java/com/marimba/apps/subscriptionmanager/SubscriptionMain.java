// Copyright 2019, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager;

import com.bmc.web.ajax.AjaxRegistry;
import com.marimba.apps.securitymgr.db.QueryExecutor;
import com.marimba.apps.subscription.common.*;
import com.marimba.apps.subscription.common.intf.*;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscription.sbrp.SBRPSiteHttpConnector;
import com.marimba.apps.subscriptionmanager.approval.*;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiff;
import com.marimba.apps.subscriptionmanager.users.AdminUser;
import com.marimba.apps.subscriptionmanager.users.User;
import com.marimba.apps.subscriptionmanager.users.UserManager;
import com.marimba.apps.subscriptionmanager.util.Utils;
import com.marimba.apps.subscriptionmanager.webapp.PublishThread;
import com.marimba.apps.subscriptionmanager.webapp.ajax.ucd.UCDTemplatesAction;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.RPCHelper;
import com.marimba.apps.subscriptionmanager.webapp.util.push.DMHelper;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.SCAPUtils;
import com.marimba.castanet.publish.*;
import com.marimba.intf.admin.IDigestLogin;
import com.marimba.intf.admin.ILogin;
import com.marimba.intf.admin.IUserDirectory;
import com.marimba.intf.application.IApplicationContext;
import com.marimba.intf.castanet.IChannel;
import com.marimba.intf.castanet.IWorkspace;
import com.marimba.intf.certificates.ICertProvider;
import com.marimba.intf.db.IConnectionPool;
import com.marimba.intf.logs.ILog;
import com.marimba.intf.logs.ILogConstants;
import com.marimba.intf.msf.*;
import com.marimba.intf.msf.acl.IAclMgr;
import com.marimba.intf.msf.task.ITaskMgr;
import com.marimba.intf.msf.wakeonwan.IWakeManager;
import com.marimba.intf.ssl.ISSLProvider;
import com.marimba.intf.transmitter.IServerAdmin;
import com.marimba.intf.util.IConfig;
import com.marimba.intf.util.IDirectory;
import com.marimba.intf.util.IProperty;
import com.marimba.rpc.RPC;
import com.marimba.rpc.RPCSession;
import com.marimba.tools.config.ConfigDefaults;
import com.marimba.tools.config.ConfigProps;
import com.marimba.tools.config.ConfigUtil;
import com.marimba.tools.ldap.*;
import com.marimba.tools.ldap.util.LDAPExceptionUtils;
import com.marimba.tools.util.Password;
import com.marimba.tools.util.Props;
import com.marimba.webapps.intf.*;
import marimba.io.FastOutputStream;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.struts.Globals;
import org.apache.struts.util.MessageResources;

import javax.naming.CommunicationException;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Interacts with the Tuner to set/get global configurations such as Subscription Manager Property, LDAP connection, command line parsing etc. The subscription
 * main is used by actions and tags for accessing common features and variables that are shared for web application.
 *
 * @author Theen-Theen Tan
 * @author Angela Saval
 * @version 1.126, 04/22/2003
 */

public class SubscriptionMain implements ISubscriptionConstants, IMainDataSourceContext, IWebAppConstants, IErrorConstants, ILogConstants, LogConstants {

    ConfigProps config = null;
    IConfig serverConfig = null; /* This is the configuration settings for CMS that
    * are needed for obtaining information for the LDAP
    * connectivity
    */
    IDirectory features = null; /* These are the features that are accessible by a web
    * application.  They are needed for publishing
    * capabilities.
    */
    File dataDir = null; /* This is the persistent storage directory that the
    * web application has access to.  This is used for creating
    * a properties file for publishing and for storing
    * the namespaces for each user.
    */
    Props subConfig = null; /* This is the subscription configuration object that is
    * stored in LDAP.  It is loaded from ou=Castanet, <domain
    * suffix>.  If certain properties are not defined, then
    * default values are used.
    * @see com.marimba.apps.subscription.common.util.initPluginConfig
    */
    Props mrbaConfig = null;
    ObjectManager objectManager;
    boolean initialized = false;
    IConfig ldapCfg; /* This is the ldap configuration that is used for
    * creating new directory connections.
    */
    IServer server; /* This equivalent to CMS. This variable is used for obtaining
    * features from CMS.
    */
    ITenantManager tenantMgr;
    ITenant tenant;
    String tenantName;
    IAccessControlMgr acmgr; /* This is needed for LDAP authentication
    */
    ITaskMgr taskMgr; /* This is needed for LDAP authentication
    */
    IWakeManager wakeMgr; /* This is needed for LDAP authentication
    */
    ILog applog; /* This is the log file used for audit and error logs
    * within actions.
    */
    MessageResources logres; /* The resources that contain the messages for logging.
    * These logs are stored in the web application channel
    * history logs.
    * see products/rsrc/classesrsrc/ApplicationResources.properties
    */
    MessageResources msgres;
    boolean commandLine = false;
    boolean usersInLDAP; /* Indicates whether or not the users have been sourced from
    * the plugin transmitter.  It is true if the users are stored
    * in LDAP. FALSE if they are sourced from the transmitter.
    */
    String subBase; /* This is the search base used for searching for subscriptions
    * in ldap.  It is a static variable for convenient access
    * from actions and tags.
    */
    String collBase;
    String ldapCollBase;
    String host; /*This is the host which contains the users and user groups when
    *sourcing from the transmitter
    */
    com.marimba.tools.util.Props txAdminProps = null;
    LDAPConnection currentdc = null;
    User adminUser;
    LDAPEnv ldapEnv;
    String dirType;
    Map<String, String> LDAPVarsMap;
    int sRPCTimeout = 10 * 1000; // give ourselves 10 seconds before letting the RPC timeout kick in
    boolean aclsOn; // indicates whether ACLs are on or off

    private ApprovalPolicyStorage dbStorage = null;
    Props pushConfig = null; /* Config to store different parameter of push deployments    */

    //    static String ldapBasedn = null;
    private String subscriptionConfigVersion = LDAPEnv.CURRENT_SUBSCRIPTION_CONFIG_VERSION;

    IConnectionPool connectionPool = null;

    ServletContext context;
    IWorkspace workspace;
    IChannel channel;
    // IServiceAutomation serviceAutomation;
    String logbundlekey = Globals.MESSAGES_KEY;
    String messagekey = Globals.MESSAGES_KEY; /*  these are the keys to use for obtaining
    *  the resources in the session.  logbundlekey
    * and messagekey are currently equivalent because
    * the log messages are kept in the the same
    * file with the page text.
    * However, this may change in the future
    * @see products/rsrc/classesrsrc/ApplicationResources.properties
    *
    */
    AjaxRegistry ajaxRegistry;
    final static Class[] AJAX_FUNCTIONS = {
            UCDTemplatesAction.class,
    };

    private SBRPSiteHttpConnector sitesInfo;
    private boolean hasPolicyChange = false;
    IConfig tunerConfig;
    QueryExecutor queryExecutor;

    /**
     * DOCUMENT ME!
     *
     * @param context
     * @param config
     * @param features
     * @param dataDir
     * @param applog
     * @throws SystemException
     */
    public SubscriptionMain(ServletContext context, ConfigProps config,
                            IDirectory features, File dataDir, ILog applog, IServer cmsServer, ITenant tenantObj) throws SystemException {

        this.context = context;
        this.config = config;
        this.features = features;
        this.dataDir = dataDir;
        this.applog = applog;
        this.msgres = (MessageResources) context.getAttribute(messagekey);
        this.logres = (MessageResources) context.getAttribute(logbundlekey);
        server = cmsServer;
        tenantMgr = (ITenantManager)features.getChild("tenantMgr");
        this.tenant = tenantObj;
        this.tenantName = tenant.getName();
        this.serverConfig = tenant.getConfig();
        tunerConfig = (IConfig) features.getChild("tunerConfig");
        workspace = (IWorkspace) features.getChild("workspace");
        IWebApplicationInfo Info = ((IWebApplicationMgr) server.getWebApplicationMgr()).getApplication(SUBSCRIPTION_PATH);
        channel = Info.getChannel();
        // setting message resource for classes in subscription and its subpackages
        MessageResources sysErrorResources = (MessageResources)context.getAttribute(IWebAppsConstants.SYSTEMERRORS);
        StringResourcesHelper.setStringResources(new StringResourcesAdapter(sysErrorResources));

        // set the LDAPSSL provider.
        // we need to do this for every channel during initialization as
        // each channel(webapp) has its own class loader, which is different from CMS
        LDAPSSLSocketFactory.provider = tenantMgr.getSslProvider();
        taskMgr = tenant.getTaskMgr();
        wakeMgr = (IWakeManager) tenantMgr.getService("wow");

        acmgr = tenant.getAccessControlMgr();
        ldapCfg = tenant.getActiveLdapCfg();
        // Reading the LDAP settings from CMS
        ObjectManager objectManager =  new ObjectManager();
        objectManager.init(this, "com.marimba.apps.subscription.common.objects.dao.LDAPSubscription", tenant.getName());
        this.objectManager = objectManager;
        UserManager.init("com.marimba.apps.subscriptionmanager.users.FileUser", this);

        File txAdminAccessFile = new File(dataDir, TXADMIN_FILE);


        debug("dataDir = " + dataDir.toString());
        debug("adminaccessfile = " + txAdminAccessFile.toString());
        debug("Current time in millis" + System.currentTimeMillis());

        txAdminProps = new com.marimba.tools.util.Props(txAdminAccessFile);
        txAdminProps.load();
        LDAPConnUtils.getInstance(tenant.getConfig(), tenant.getName(), channel);

        try {
            this.queryExecutor = new QueryExecutor(this);
        } catch (Throwable t) {
            this.queryExecutor = null;
        }
    }

    public void setConnectionPool(IConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public IConnectionPool getConnectionPool() {
        return connectionPool;
    }
    public void loadSiteBasedInfo() {
        if (isSiteBasedPolicyEnabled()) initSiteInfo();
    }
    public QueryExecutor getQueryExecutor() {
        return this.queryExecutor;
    }
    public IChannel getChannel() {
        return channel;
    }

    public ITenantManager getTenantManager() {
        return tenantMgr;
    }

    public String getCMSMode() {
        return tenantMgr.getCMSMode();
    }

    public boolean isCloudModel() {
        return tenantMgr.isCloudModel();
    }

    public boolean isTenantModel() {
        return tenantMgr.isTenantModel();
    }

    public boolean isDefaultModel() {
        return tenantMgr.isDefaultModel();
    }

    public ITenant getTenant() {
        return this.tenant;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public ObjectManager getObjectManager() {
        return objectManager;
    }

    public void setObjectManager(ObjectManager objectManager) {
        this.objectManager = objectManager;
    }

    public Map<String, String> getLDAPVarsMap() {
        return LDAPVarsMap;
    }

    public void setLDAPVarsMap(Map<String, String> lDAPVarsMap) {
        LDAPVarsMap = lDAPVarsMap;
    }


    /**
     * DOCUMENT ME!
     *
     * @return IDirectory
     */
    public IDirectory getFeatures() {
        return features;
    }
    public IServer getServer() {
        return this.server;
    }

    public String getDirType() {
        return dirType;
    }
    public void setDirType(String dirType) {
        this.dirType = dirType;
    }
    /**
     * This returns the data directory where the web application can store any configuration data for itself.  This is used to store the publish directory so
     * that the plugin may be published
     *
     * @return File
     */
    public File getDataDirectory() {
        return dataDir;
    }

    public IWebApplicationInfo getWebApplicationInfo() {
        return ((IWebApplicationMgr) server.getManager("application-mgr")).getApplication("subscription", "Subscription");
    }

    /**
     * These are the settings made in the manager. For example, plugin settings and log roll policy settings.
     *
     * @return IProperty
     */
    public ConfigProps getConfig() {
        return config;
    }

//    public IServiceAutomation getServiceAutomation() {
//        return serviceAutomation;
//    }

    /**
     * Generate the directory for creating .configurator segment for publish
     *
     * @return Random directory name if not created already.
     */
    public String generatePluginDirectoryName() {
        File dataDirContents[] = getDataDirectory().listFiles();
        for (File dataDirContent : dataDirContents) {
            if (dataDirContent.isDirectory()) {
                File ldapserversFile = new File(dataDirContent, "ldapservers.txt");
                if (ldapserversFile.exists()) {
                    return dataDirContent.getName();
                }
            }
        }
        return RandomStringUtils.randomAlphabetic(8);
    }

    /**
     * returns the full dn of where our subscriptions are stored
     *
     * @return REMIND
     */
    public String getSubBase() {
        return subBase;
    }

    /**
     * returns the full dn of where our subscriptions are stored
     *
     * @return REMIND
     */
    public String getSubBase2() {
        return subBase;
    }

    /**
     * returns the full dn of where our collections are stored
     *
     * @return REMIND
     */
    public String getCollBase() {
        return collBase;
    }

    /**
     * returns the full dn of where our ldap collections are stored
     *
     * @return REMIND
     */
    public String getLDAPCollBase() {
        return ldapCollBase;
    }

    /**
     * returns the full filename to store the DM deployments
     * the file contains key value pairs where keys are hashcode of the target DN
     * and the value is the deployment id returned by the DM.  A given target combination can have
     * only one deployment ID.
     *
     * @return string
     */
    public String getPushDeploymentFileName() {
        return getDataDirectory() + File.separator + PUSH_DEPLOYMENT_FILE;
    }

    /**
     * returns IWakeManager object
     *
     * @return IWakeManager
     */
    public IWakeManager getWakeMgr() {
        return wakeMgr;
    }

    /**
     * returns ITaskMgr object
     *
     * @return ITaskMgr
     */
    public ITaskMgr getTaskMgr() {
        return taskMgr;
    }
    /**
     * Return the CMS config
     *
     * @return REMIND
     */
    public IConfig getServerConfig() {
        return serverConfig;
    }

    /**
     * DOCUMENT ME!
     *
     * @return IProperty the Subscription configuration settings stored in LDAP.
     */
    public Props getPushConfig() {
        return  pushConfig;
    }


    public String getLDAPProperty(String key) {
        if (ldapCfg != null) {
            return ldapCfg.getProperty(key);
        }
        return null;
    }

    public IProperty getLDAPConfig() {
        return ldapCfg;
    }

    public boolean hasLDAP() {
        return ldapCfg != null;
    }

    /**
     * The main should get a handle on the application log from the servlet cont into the initialization servlet.  The application log is a handle on the that
     * is to be used for loggin error and audit messages that occurs in the webapplicaiton
     *
     * @return ILog handle to the log file.
     */
    public ILog getAppLog() {
        return applog;
    }
    /**
     * The web application controller loads in the appropriate resource file tha as a parameter in the web.xml definition.  This method returns a handle t
     * resources that correspond that file.
     *
     * @return MessageResources handle the the message resources loaded in by th     web application controller.
     * @see com.marimba.webapps.tools.action.WebAppController
     */
    public MessageResources getLogResources() {
        return logres;
    }

    /**
     * The web application controller loads in the appropriate resource file tha as a parameter in the web.xml definition.  This method returns a handle t
     * resources that correspond that file.
     *
     * @return MessageResources handle the the message resources loaded in by th     web application controller.
     * @see com.marimba.webapps.tools.action.WebAppController
     */
    public MessageResources getAppResources() {
        return msgres;
    }

    public boolean isAclOn() {
        return aclsOn;
    }

    InputStream getResourceAsStream(String filename) throws IOException {
        return context.getResourceAsStream(filename);
    }

    //
    // Implements IMainDataSourceContext
    //

    /**
     * DOCUMENT ME!
     *
     * @return IProperty the Subscription configuration settings stored in LDAP.
     */
    public IProperty getSubscriptionConfig() {
        return (IProperty) subConfig;
    }

    public IProperty getMarimbaConfig() {
        return (IProperty) mrbaConfig;
    }

    /**
     * DOCUMENT ME!
     *
     * @param user REMIND
     * @return the Datasource context from the User
     */
    public ILDAPDataSourceContext getLDAPDataSourceContext(IUser user) {
        return (ILDAPDataSourceContext) user;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the LDAP Environment object that may have directory specific information cached.
     */
    public LDAPEnv getLDAPEnv() {
        return ldapEnv;
    }

    /**
     * DOCUMENT ME!
     *
     * @return false if we're sourcing users from transmitter
     */
    public boolean getUsersInLDAP() {
        return usersInLDAP;
    }

    /**
     * DOCUMENT ME!
     *
     * @return true if ACLS are on, false if not
     */
    public boolean isAclsOn() {
        return aclsOn;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the LDAP ACL mgr that enforces DN-based principals
     */
    public IAclMgr getAclMgr() {
        return tenant.getAclMgr();
    }

    public ILDAPDataSourceContext getAdminUser() {
        return adminUser;
    }

    // This is for testing purpose only
    public String getProperty(String key) {
        return config.getProperty(key);
    }

    /**
     * Since the LDAP settings are obtained through CSF, this method is needed as an access point for classes that needed csf configurations
     *
     * @param key REMIND
     * @return REMIND
     */
    public String getServerProperty(String key) {
        return serverConfig.getProperty(key);
    }

    public ApprovalPolicyStorage getDBStorage() {
        if(null == dbStorage) {
            debug("Data base storage re-initialized");
            dbStorage = new ApprovalPolicyStorage(this);
        }
        return this.dbStorage;
    }
    public void closeDBStorage() {
        if(null != dbStorage) {
            dbStorage.closeStorage();
        }
    }

    /**
     * Assumes initLDAP already called
     *
     * @param username REMIND
     * @return REMIND
     * @throws SystemException REMIND
     */
    public String resolveUserDN(String username) throws SystemException {
        if (!initialized) {
            initLDAP();
        }

        try {
            // If the user entered only the uid/saMAccountName
            // Try to resolve the user's DN using
            // (&(<uid>=<username>)(objectclass=<userClass>))) query
            if (!adminUser.getBrowseConn().getParser().isDN(username)) {
                String uid = ldapCfg.getProperty(LDAPConstants.PROP_USERATTR);
                String userClass = ldapCfg.getProperty(LDAPConstants.PROP_USERCLASS);

                if ((uid == null) || "".equals(uid.trim())) {
                    uid = LDAPVarsMap.get("USER_ID");
                }

                String query = null;
                if (userClass != null && userClass.contains(",")) {
                    StringTokenizer st = new StringTokenizer(userClass, ",");
                    StringBuffer buf = new StringBuffer(userClass.length() * 2);
                    int items = 0;

                    while (st.hasMoreTokens()) {
                        buf.append("(objectclass=").append(st.nextToken()).append(')');
                        items++;
                    }

                    if (items > 1) {
                        query = "(|" + buf + ")";
                    } else {
                        query = buf.toString();
                    }

                } else {
                    query = "(&(" + uid + "=" + username + ")(objectclass=" + userClass + "))";
                }

                query = "(&(" + uid + "=" + username + ")" + query + ")";
                String[] dns = searchAndReturnDNsAllTrees(query);

                if (dns != null) {
                    if (dns.length > 1) {
                        // User must specify a fullDN in that case
                        throw new CriticalException(LDAP_CONNECT_MULTIPLEUSERDN, username);
                    }
                } else {
                    // If this is Active Directory, user might be
                    // trying to log in using UPN format.
                    String vendor = ldapCfg.getProperty(LDAPConstants.PROP_VENDOR);
                    if (LDAPConstants.VENDOR_AD.equals(vendor) || LDAPConstants.VENDOR_ADAM.equals(vendor)) {
                        if (LDAPUserConnection.isUPN(username)) {
                            dns = searchAndReturnDNsAllTrees("(&(" + LDAPVarsMap.get("UPN") + "=" + username + ")(objectclass=" + userClass + "))");
                        }
                    }

                    if (dns == null) {
                        throw new CriticalException(LDAP_CONNECT_RESOLVEUSERDN, username);
                    }
                }

                return dns[0];
            }
        } catch (NamingException ne) {
            LDAPUtils.classifyLDAPException(ne, null, false);
        } catch (LDAPLocalException lle) {
            new CriticalException(lle.getRootException(), LDAP_CONNECT_RESOLVEUSERDN);
        } catch (LDAPException le) {
            LDAPUtils.classifyLDAPException(le.getRootException(), null, false);
        } catch(Exception ec) {
            ec.printStackTrace();
        }
        return username;
    }
    public String resolveGroupDN(String groupName) throws SystemException {

        String  base = adminUser.getProperty(LDAPConstants.PROP_DOMAINASDN);
        String[]  groupDN = null;
        groupName = LDAPSearchFilter.escapeComponentValue(groupName);

        if (!LDAPVars.ACTIVE_DIRECTORY.equals(LDAPVarsMap.get("DIRECTORY_TYPE"))) {
            base = adminUser.getProperty(LDAPConstants.PROP_BASEDN);
        }

        try {
            if (!adminUser.getBrowseConn().getParser().isDN(groupName)) {
                String groupClass = subConfig.getProperty(LDAPConstants.CONFIG_GROUPCLASS);
                // group class can be comma separated list
                StringTokenizer st = new StringTokenizer(groupClass, ",");
                String          searchStr = "";
                int             items = 0;
                String serachQuery = null;

                while (st.hasMoreTokens()) {
                    searchStr += ("(objectclass=" + st.nextToken() + ")");
                    items++;
                }

                if (items > 1) {
                    searchStr = "(|" + searchStr + ")";
                }

                serachQuery = "(&(cn=" + groupName + ")" + searchStr + ")";
                groupDN = adminUser.getBrowseConn().searchAndReturnDNs(serachQuery, base, false);
            }
        } catch (Exception le) {
            throw new SubKnownException("Failed to resolve Group DN", groupName);
        }
        return groupDN[0];
    }

    /**
     * REMIND
     *
     * @param str REMIND
     * @return REMIND
     */
    public boolean validateTarget(String str) {
        String notallowed;

        notallowed = NOTALLOWED_FILECHARS;

        int len = notallowed.length();

        for (int i = 0; i < len; i++) {
            char c = notallowed.charAt(i);

            if (str.indexOf(c) != -1) {
                return false;
            }
        }

        return true;
    }

    /* Write the username and password that is used for authenticating
     * against the transmitter when sourcing users from the tranmitter.
     */
    public void setAdminAccess(String username, String password) throws SystemException {
        try {
            txAdminProps.setProperty("txadmin", username);
            txAdminProps.setProperty("txpwd", Password.encode(password));

            debug("txadmin param2=" + username);
            debug("txadmin prop=" + txAdminProps.getProperty("txadmin"));

            boolean saved = txAdminProps.save();

            if (!saved) {
                debug("setAdminAccess() saved = " + saved);
            }
        } catch (Exception iexc) {
            KnownException ke = new KnownException(iexc, USETX_CANTSAVEPROPS);
            ke.setLogException(true);
            throw ke;
        }
    }

    /**
     * This internal method does the work of copying over the files needed for the plugin into the publish directory.
     *
     * @param userPluginProps This contains the plugin settings that were inputted from the commandline or from the GUI.  These have NOT been saved to the disk
     *                        yet.  THis is not done until the publish is successful.
     * @throws SystemException
     * @throws SubKnownException REMIND
     */
    private void preparePublish(IConfig userPluginProps, File pluginDirectory)
            throws SystemException {
        String configZipDir = "/pluginlib";
        String[] configZipFiles = {"/subconfig.zip",
                "/axis.jar","/axis-ant.jar","/axis-schema.jar","/saaj.jar","/xml-apis.jar",
                "/xercesImpl.jar","/wsdl4j.jar","/commons-logging.jar","/commons-discovery.jar",
                "/geronimo-jaxrpc.jar","/activation.jar","/commons-httpclient.jar","/commons-codec.jar","/ldapbp.jar","/oracle.jar","/sqljdbc.jar"};

        String mainClass = "com.marimba.apps.subscriptionplugin.SubscriptionPlugin";

        try {
            // copy the zip and JNDI Jar files to the publish directory
            for (String configZipFile : configZipFiles) {
                InputStream is = context.getResourceAsStream(configZipDir + configZipFile);
                FastOutputStream fos = new FastOutputStream(new File(pluginDirectory, configZipFile));
                fos.sendStream(is, -1);
                fos.close();
            }

            // generate properties.txt etc. for the plugin
            //String classPathLib = "subconfig.zip";
            String classPathLib = "subconfig.zip"+
                    ":activation.jar:axis.jar:axis-ant.jar:axis-schema.jar:saaj.jar:xml-apis.jar"+
                    ":xercesImpl.jar:wsdl4j.jar:commons-logging.jar:commons-discovery.jar"+
                    ":geronimo-jaxrpc.jar:commons-httpclient.jar:commons-codec.jar:ldapbp.jar:oracle.jar:sqljdbc.jar";

            Props pluginDirProps = new Props(new File(pluginDirectory, "properties.txt"));
            pluginDirProps.load();
            pluginDirProps.setProperty("capabilities", "all");
            pluginDirProps.setProperty("main", mainClass);
            pluginDirProps.setProperty("classpath", classPathLib);
            pluginDirProps.setProperty("type", "Extension");
            pluginDirProps.setProperty("autostart", "true");
            if (LDAPEnv.CURRENT_SUBSCRIPTION_CONFIG_VERSION.equals(subscriptionConfigVersion)) {                // wipe out the property if it is set
                pluginDirProps.setProperty("subscriptionplugin.configSchemaVersion",null);
            } else {
                // set the property so that the plugin run in compatibility mode
                pluginDirProps.setProperty("subscriptionplugin.configSchemaVersion",subscriptionConfigVersion);
            }
            IProperty ipr = (IProperty) context.getAttribute("com.marimba.servlet.context.properties");

            String channel_version = ipr.getProperty("channel.version");
            if(ipr.getProperty("channel.internalversion") != null) {
                channel_version = ipr.getProperty("channel.internalversion");
            }
            pluginDirProps.setProperty("channel.version", channel_version);
            pluginDirProps.setProperty("title", "vInspector " + channel_version);
            pluginDirProps.setProperty("category", "@category.internal@");

            pluginDirProps.setProperty("subscriptionplugin.mode", "directoryenabled");
            pluginDirProps.setProperty("subscriptionplugin.ldaphost", userPluginProps.getProperty("subscriptionmanager.ldaphost"));
            pluginDirProps.setProperty("subscriptionplugin.basedn", userPluginProps.getProperty("subscriptionmanager.basedn"));
            pluginDirProps.setProperty("subscriptionplugin.binddn", userPluginProps.getProperty("subscriptionmanager.binddn"));
            pluginDirProps.setProperty("subscriptionplugin.bindpasswd", userPluginProps.getProperty("subscriptionmanager.bindpasswd"));
            pluginDirProps.setProperty("subscriptionplugin.authmethod", userPluginProps.getProperty("subscriptionmanager.authmethod"));
            pluginDirProps.setProperty("subscriptionplugin.poolsize", userPluginProps.getProperty("subscriptionmanager.poolsize"));
            pluginDirProps.setProperty("subscriptionplugin.lastgoodhostexptime", userPluginProps.getProperty("subscriptionmanager.lastgoodhostexptime"));
            pluginDirProps.setProperty(PROVISION_ALLOW, userPluginProps.getProperty("subscriptionmanager.pallowprov"));
            pluginDirProps.setProperty("subscriptionplugin.usessl", userPluginProps.getProperty("subscriptionmanager.usessl"));
            pluginDirProps.setProperty("subscriptionplugin.vendor", userPluginProps.getProperty("subscriptionmanager.vendor"));
            pluginDirProps.setProperty("subscriptionplugin.noautodiscover", userPluginProps.getProperty("subscriptionmanager.noautodiscover"));
            pluginDirProps.setProperty("subscriptionplugin.pluginStatus", userPluginProps.getProperty("subscriptionmanager.pluginStatus"));
            pluginDirProps.setProperty("subscriptionplugin.admanagementdomain", userPluginProps.getProperty("subscriptionmanager.admanagementdomain"));
            pluginDirProps.setProperty("subscriptionplugin.srvdnsserver", userPluginProps.getProperty("subscriptionmanager.srvdnsserver"));
            pluginDirProps.setProperty("subscriptionplugin.connectiontimeout", userPluginProps.getProperty("subscriptionmanager.connectiontimeout"));
            pluginDirProps.setProperty("subscriptionplugin.querytimeout", userPluginProps.getProperty("subscriptionmanager.querytimeout"));
            pluginDirProps.setProperty("subscriptionplugin.preferreddcs", userPluginProps.getProperty("subscriptionmanager.preferreddcs"));
            pluginDirProps.setProperty("subscriptionplugin.adsite", userPluginProps.getProperty("subscriptionmanager.adsite"));
            pluginDirProps.setProperty("subscriptionplugin.db.type", userPluginProps.getProperty("subscriptionmanager.db.type"));
            pluginDirProps.setProperty("subscriptionplugin.db.class", userPluginProps.getProperty("subscriptionmanager.db.class"));
            pluginDirProps.setProperty("subscriptionplugin.db.url", userPluginProps.getProperty("subscriptionmanager.db.url"));
            pluginDirProps.setProperty("subscriptionplugin.securityinfo.url", userPluginProps.getProperty("subscriptionmanager.securityinfo.url"));
            pluginDirProps.setProperty("subscriptionplugin.db.username", userPluginProps.getProperty("subscriptionmanager.db.username"));
            pluginDirProps.setProperty("subscriptionplugin.db.password", userPluginProps.getProperty("subscriptionmanager.db.password"));
            pluginDirProps.setProperty("subscriptionplugin.db.thread.min", userPluginProps.getProperty("subscriptionmanager.db.thread.min"));
            pluginDirProps.setProperty("subscriptionplugin.db.thread.max", userPluginProps.getProperty("subscriptionmanager.db.thread.max"));
            pluginDirProps.setProperty("repeater.insert", userPluginProps.getProperty("subscriptionmanager.repeaterInsert"));
            pluginDirProps.setProperty("subscriptionplugin.publishuser", userPluginProps.getProperty("subscriptionmanager.publishurl.username"));
            pluginDirProps.setProperty("subscriptionplugin.publishpassword", userPluginProps.getProperty("subscriptionmanager.publishurl.password"));
            pluginDirProps.setProperty("subscriptionplugin.subscribeuser", userPluginProps.getProperty("subscriptionmanager.publishurl.subscribeuser"));
            pluginDirProps.setProperty("subscriptionplugin.subscribepassword", userPluginProps.getProperty("subscriptionmanager.publishurl.subscribepassword"));
            pluginDirProps.setProperty("subscriptionplugin.securityinfo.subscribeuser", userPluginProps.getProperty("subscriptionmanager.securityinfo.subscribeuser"));
            pluginDirProps.setProperty("subscriptionplugin.securityinfo.subscribepassword", userPluginProps.getProperty("subscriptionmanager.securityinfo.subscribepassword"));
            pluginDirProps.setProperty("subscriptionplugin.customscanner.url", userPluginProps.getProperty("subscriptionmanager.customscanner.url"));
            pluginDirProps.setProperty("subscriptionplugin.customscanner.subscribeuser", userPluginProps.getProperty("subscriptionmanager.customscanner.subscribeuser"));
            pluginDirProps.setProperty("subscriptionplugin.customscanner.subscribepassword", userPluginProps.getProperty("subscriptionmanager.customscanner.subscribepassword"));

            pluginDirProps.setProperty("subscriptionplugin.elastic.url", userPluginProps.getProperty("subscriptionmanager.elasticurl"));
            pluginDirProps.setProperty("subscriptionplugin.endpoint.cveFiltersDir", userPluginProps.getProperty("subscriptionmanager.cveFiltersDir"));


            //get cached value
            String val = userPluginProps.getProperty("subscriptionmanager.counter");

            if ((val == null) || "".equals(val)) {
                val = "0";
            }

            // add a cyclical counter so the plugin restarts everytime
            int counter = Integer.parseInt(val);
            String countStr = Integer.toString(++counter);
            userPluginProps.setProperty("subscriptionmanager.counter", countStr);
            pluginDirProps.setProperty("subscriptionplugin.counter", countStr);

            // Add subscriptionplugin log roll policy setting
            IProperty iprop = getSubscriptionConfig();
            subConfig = (Props) iprop;

            String policy = subConfig.getProperty("marimba.subscriptionplugin.logs.roll.policy");
            String size = subConfig.getProperty("marimba.subscriptionplugin.logs.roll.size");
            String versions = subConfig.getProperty("marimba.subscriptionplugin.logs.roll.versions");

            // If the values for policy and versions is not set
            // then use weekly and 8 as the default values
            if ((policy == null) || "".equals(policy.trim())) {
                policy = "weekly";
            }

            if ((versions == null) || "".equals(versions.trim())) {
                versions = "8";
            }

            pluginDirProps.setProperty("logs.roll.policy", policy);
            pluginDirProps.setProperty("logs.roll.versions", versions);
            if ( subscriptionConfigVersion.equals(LDAPEnv.CURRENT_SUBSCRIPTION_CONFIG_VERSION)) {
                pluginDirProps.setProperty("subscriptionplugin.configSchemaVersion",null);
            } else{
                pluginDirProps.setProperty("subscriptionplugin.configSchemaVersion",subscriptionConfigVersion);
            }


            if ((size != null) && !"".equals(size.trim())) {
                pluginDirProps.setProperty("logs.roll.size", size);
            }

            pluginDirProps.setProperty("segment", ".configurator");

            try {
                Props ucdConfigInTemp = new Props(new File(pluginDirectory, UCD_TEMPLATE_FILENAME));
                IConfig ucdConfig = new ConfigProps(new File(getDataDirectory(), UCD_TEMPLATE_FILENAME));
                String[] ucdConfigArr = ucdConfig.getPropertyPairs();

                if (ucdConfigArr != null) {
                    int i = 0;

                    while (i < ucdConfigArr.length) {
                        String key = ucdConfigArr[i++];
                        String value = ucdConfigArr[i++];

                        ucdConfigInTemp.setProperty (key, value);
                    }
                    ucdConfigInTemp.save();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // scap(non-windows) template
            try {
                String scapTemplatePath = getDataDirectory().getAbsolutePath() + File.separator + "scaptemplates";
                File scapTemplateDir = new File(scapTemplatePath);
                if(scapTemplateDir.exists()) {
                    File[] directoryListing = scapTemplateDir.listFiles();
                    if (directoryListing != null) {
                        for(File scapTemplateFile : directoryListing) {
                            debug("File Name :" + scapTemplateFile.getName());
                            debug("scap temaplte path :" + scapTemplatePath);
                            Props scapTemplateInTemp = new Props(new File(pluginDirectory, scapTemplateFile.getName()));
                            IConfig scapTemplate = new ConfigProps(new File(scapTemplatePath, scapTemplateFile.getName()));
                            if(null != scapTemplate) {
                                String[] scapProps = scapTemplate.getPropertyPairs();

                                if (scapProps != null) {
                                    int i = 0;

                                    while (i < scapProps.length) {
                                        String key = scapProps[i++];
                                        String value = scapProps[i++];
                                        debug("key :" + key + " value:" + value);
                                        scapTemplateInTemp.setProperty (key, value);
                                    }
                                    scapTemplateInTemp.save();
                                }
                            }
                        }
                    }
                }
            } catch(Exception ed) {
                ed.printStackTrace();
            }

            // scap(windows) template
            try {
                String scapTemplatePath = getDataDirectory().getAbsolutePath() + File.separator + "usgcbtemplates";
                File scapTemplateDir = new File(scapTemplatePath);
                if(scapTemplateDir.exists()) {
                    File[] directoryListing = scapTemplateDir.listFiles();
                    if (directoryListing != null) {
                        for(File scapTemplateFile : directoryListing) {
                            debug("File Name :" + scapTemplateFile.getName());
                            debug("scap temaplte path :" + scapTemplatePath);
                            Props scapTemplateInTemp = new Props(new File(pluginDirectory, scapTemplateFile.getName()));
                            IConfig scapTemplate = new ConfigProps(new File(scapTemplatePath, scapTemplateFile.getName()));
                            if(null != scapTemplate) {
                                String[] scapProps = scapTemplate.getPropertyPairs();

                                if (scapProps != null) {
                                    int i = 0;

                                    while (i < scapProps.length) {
                                        String key = scapProps[i++];
                                        String value = scapProps[i++];
                                        debug("key :" + key + " value:" + value);
                                        scapTemplateInTemp.setProperty (key, value);
                                    }
                                    scapTemplateInTemp.save();
                                }
                            }
                        }
                    }
                }
            } catch(Exception ed) {
                ed.printStackTrace();
            }

            // custom option
            try {
                String scapTemplatePath = getDataDirectory().getAbsolutePath() + File.separator + "customtemplates";
                File scapTemplateDir = new File(scapTemplatePath);
                if(scapTemplateDir.exists()) {
                    File[] directoryListing = scapTemplateDir.listFiles();
                    if (directoryListing != null) {
                        for(File scapTemplateFile : directoryListing) {
                            debug("File Name :" + scapTemplateFile.getName());
                            debug("scap temaplte path :" + scapTemplatePath);
                            Props scapTemplateInTemp = new Props(new File(pluginDirectory, scapTemplateFile.getName()));
                            IConfig scapTemplate = new ConfigProps(new File(scapTemplatePath, scapTemplateFile.getName()));
                            if(null != scapTemplate) {
                                String[] scapProps = scapTemplate.getPropertyPairs();

                                if (scapProps != null) {
                                    int i = 0;

                                    while (i < scapProps.length) {
                                        String key = scapProps[i++];
                                        String value = scapProps[i++];
                                        debug("key :" + key + " value:" + value);
                                        scapTemplateInTemp.setProperty (key, value);
                                    }
                                    scapTemplateInTemp.save();
                                }
                            }
                        }
                    }
                }
            } catch(Exception ed) {
                ed.printStackTrace();
            }

            // cve filters
            try {
                String cveFiltersPath = userPluginProps.getProperty("subscriptionmanager.cveFiltersDir");
                if (cveFiltersPath != null) {
                    File cveFiltersDir = new File(cveFiltersPath);
                    if (cveFiltersDir.exists()) {
                        File[] cveFilters = cveFiltersDir.listFiles(new FilenameFilter() {
                            public boolean accept(File dir, String name) {
                                return name.endsWith(".txt");
                            }
                        });
                        if (cveFilters != null) {
                            new File(pluginDirectory, "cve_filters").mkdirs();
                            Props cveFiltersProp = new Props(new File(pluginDirectory + File.separator + "cve_filters" + File.separator + "cve_filters.txt"));
                            for (File cveFilter : cveFilters) {
                                debug("CVE Filter, Src - " + cveFilter + ", Dst - " + new File(pluginDirectory + File.separator + "cve_filters" + File.separator + cveFilter.getName()).getAbsolutePath());
                                copyFile(cveFilter.getAbsolutePath(), new File(pluginDirectory + File.separator + "cve_filters" + File.separator + cveFilter.getName()).getAbsolutePath());
                                cveFiltersProp.setProperty (cveFilter.getName(), cveFilter.getName());
                            }
                            cveFiltersProp.save();
                        }
                    }
                }
            } catch(Exception ed) {
                ed.printStackTrace();
            }

            // cve info
            try {
                IChannel securityInfoChannel = SCAPUtils.getSCAPUtils().getSecurityInfoChannelCreated();
                debug("CVE Info, securityInfoChannel - " + securityInfoChannel);
                if (securityInfoChannel != null) {
                    String tunerWorkspace = tunerConfig.getProperty("runtime.workspace.dir");
                    String securityInfoDataDir = workspace.getChannelFolderName(securityInfoChannel.getURL()) + File.separator + "data";
                    String securityInfoChannelDataDir = tunerWorkspace + File.separator + securityInfoDataDir;
                    File cveInfoFile = new File(securityInfoChannelDataDir, "cve_info.properties");
                    debug("CVE Info, cveInfoFile - " + cveInfoFile);
                    if (cveInfoFile.exists()) {
                        debug("CVE Info, Src - " + cveInfoFile + ", Dst - " + new File(pluginDirectory + File.separator + cveInfoFile.getName()).getAbsolutePath());
                        copyFile(cveInfoFile.getAbsolutePath(), new File(pluginDirectory + File.separator + cveInfoFile.getName()).getAbsolutePath());
                    }
                }
            } catch(Exception ed) {
                ed.printStackTrace();
            }

            // REMIND t3
            // Copy any marimba.ldap.ad properties from the Tuner Config
            //
            // LDAPConnUtils.getInstance().copyNoAutoProps(new ConfigProps(pluginDirProps, null));
            pluginDirProps.save();
        } catch (IOException e) {
            throw new SubKnownException(e, PUBLISH_CANTSAVEPROPS);
        }
    }

    public static void copyFile(String sourceFilePath, String destinationFilePath) {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(sourceFilePath);
            os = new FileOutputStream(destinationFilePath);

            byte[] buffer = new byte[1024];
            int bytesRead;
            //read from is to buffer
            while((bytesRead = is.read(buffer)) !=-1){
                os.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch(Exception ed) {

        } finally {
            if(null != is) {
                try {
                    is.close();
                } catch(Exception e){}
            }
            if(null != os) {
                try {
                    os.flush();
                    os.close();
                } catch(Exception f){}
            }
        }
    }

    /**
     * This publishes the Subscription Plugin to the Subscription Service channel.  This method is used by the commandline and by the set plugin GUI.
     *
     * @param username        The username for publishing the channel
     * @param password        The password for publishing.  It is expected to be in plain text. It is encoded before sending.
     * @param userPluginProps These are the settings that were made in the set plugin page They have NOT been saved to disk yet.  That will occur when the
     *                        publishing is successful.  THe method calling publishChannel is responsble for that.
     * @throws SystemException
     * @throws KnownException       REMIND
     * @throws SubInternalException REMIND
     */
    public boolean publishChannel(String username, String password, IConfig userPluginProps) throws SystemException {
        boolean result = false;
        String url = userPluginProps.getProperty("subscriptionmanager.publishurl");

        // if the passed URL is null get the last URL used
        if (url == null) {
            result = false;
            // error, no valid url
            throw new KnownException(PUBLISH_INVALIDSUBSVCURL, url);
        }

        if (!url.endsWith("vInspector")) {
            url += "/vInspector";
        }
        debug("vInspector url = " + url);

        URL publishURL;

        try {
            publishURL = new URL(url);
        } catch (Exception e) {
            throw new KnownException(PUBLISH_INVALIDSUBSVCURL, url);
        }

        // If plugin directory name generated already while copying ldapservers.txt file
        // would reuse it
        File pluginDirectory = new File(getDataDirectory(), generatePluginDirectoryName());
        pluginDirectory.mkdir();

        try {
            // prepare the publish operation (create XML, properties.txt etc.)
            preparePublish(userPluginProps, pluginDirectory);

            IConfig tunerConfig = (IConfig) getFeatures().getChild("tunerConfig");
            PublishEnv pubEnv = new PublishEnv(new ConfigUtil(tunerConfig));
            CastanetPublish publisher;

            try {
                publisher = new CastanetPublish(pubEnv, null, username, pluginDirectory);
            } catch (Exception e) {
                throw new SubInternalException(e, PUBLISH_INTERNAL_CANTCREATEPUB);
            }

            ISSLProvider ssl = (ISSLProvider) getFeatures().getChild("ssl");
            ICertProvider cert = (ICertProvider) getFeatures().getChild("certificates");
            RPC rpc = (RPC) getFeatures().getChild("rpc");

            publisher.setSecurityProviders(cert, ssl);
            publisher.setRPC(rpc);
            IApplicationContext iApplicationContext = (IApplicationContext) getFeatures().getChild("context");
            publisher.setApplicationContext(iApplicationContext);

            if ("https".equals(publishURL.getProtocol())) {
                if (!publisher.setSecure()) {
                    throw new SubInternalException(PUBLISH_INTERNAL_CANTSETSECURE, publishURL.toString());
                }
            }

            // Find the port, if not present, default based on protocol
            int port = publishURL.getPort();

            if (port == -1) {
                if (publishURL.getProtocol().equals("http")) {
                    port = 80;
                } else if (publishURL.getProtocol().equals("https")) {
                    port = 443;
                }
            }

            try {
                result = publisher.publish(publishURL.getHost() + ":" + port, publishURL.getFile().substring(1), null, Password.encode(password));
            } catch (Exception e) {
                result = false;
                KnownException ke = new KnownException(e, PUBLISH_FAILEDPUBLISH);
                ke.setLogException(true);
                throw ke;
            }

            if (!result) {
                KnownException ie = new KnownException(PUBLISH_FAILEDPUBLISH_NORESULT);
                throw ie;
            }
        } catch(SystemException e) {
            result = false;
            throw e;
        } finally {
            boolean isDeleted = deleteDirectory(pluginDirectory);
            debug("Plugin segment directory delete result:" + isDeleted);
        }
        return result;
    }

    private boolean deleteDirectory(File node) {
        if(node.isFile()) {
            return node.delete();
        } else {
            File childs[] = node.listFiles();
            for(int i=0; i<childs.length; i++) {
                deleteDirectory(childs[i]);
            }
            return node.delete();
        }
    }

    /**
     * verify the LDAP connection to the comma separated hosts in the default config object
     *
     * @return String
     * @throws SystemException
     */
    public String verifyLDAPConnections() throws SystemException {
        Hashtable invalidHosts = verifyLDAPConnections(config, tenantName);
        String invHosts = "";
        int invHostsTotal = invalidHosts.size();
        int i = 0;

        if (invHostsTotal > 0) {
            for (Enumeration enumKeys = invalidHosts.keys(); enumKeys.hasMoreElements();) {
                invHosts += (String) enumKeys.nextElement();

                if (i != (invHostsTotal - 1)) {
                    invHosts += PROP_DELIM;
                }

                i++;
            }
        }

        return invHosts;
    }

    /**
     * Method to verify LDAP parameters entered on the plugin page. The verification is done from the SPM to the LDAP.  It is not an exact check frokm the
     * plugin to the LDAP. On iPlanet, we test if we can connect to the specified list of comma separated LDAP hosts. On AD, the test is from the SPM to the
     * GC in the current domain.
     *
     * @param config REMIND
     * @return Hashtable Key is host, and Value is the error code.     Vector is of size 0 if there's no error.
     * @throws SystemException REMIND
     */
    public Hashtable verifyLDAPConnections(IConfig config, String tenantName) throws SystemException {
        Hashtable invalidHosts = new Hashtable();
        // Set kerberos property
        IConfig localLdapCfg = new ConfigProps(new Props(), null);

        String host = config.getProperty("subscriptionmanager.ldaphost");

        String basedn = config.getProperty("subscriptionmanager.basedn");

        String binddn = config.getProperty("subscriptionmanager.binddn");

        if ((binddn == null) || "".equals(binddn)) {
            invalidHosts.put(host, new Integer(FAILED_PASSWORD));
        }

        String bindpasswd = config.getProperty("subscriptionmanager.bindpasswd");
        String authmethod = config.getProperty("subscriptionmanager.authmethod");
        String us = config.getProperty("subscriptionmanager.usessl");
        boolean usessl = false;
        if ("true".equals(us)) {
            usessl = true;
        }

        if ("".equals(authmethod)) {
            authmethod = null;
        }
        // To set Authentication method property for test connection from plugin configuration
        String authMethod = config.getProperty("subscriptionmanager.authmethod");
        localLdapCfg.setProperty(LDAPConstants.PROP_AUTHMETHOD, authMethod);
        localLdapCfg.setProperty(LDAPConstants.PROP_TENANT_NAME, tenantName);

        LDAPConnection ldap = null;
        int status;
        //REMIND this is ugly move to LDAPEnv
        if (LDAPConnUtils.getInstance(tenantName).isADWithAutoDiscovery(ldapCfg)) {
            try {
                ldap = LDAPConnection.createLDAPConnectionToGC(ldapCfg.getProperty(LDAPConstants.PROP_FORESTROOT),
                        ldapCfg.getProperty(LDAPConstants.PROP_SITE),
                        ldapCfg.getProperty(LDAPConstants.PROP_DOMAINPATH),
                        basedn, binddn, bindpasswd,
                        localLdapCfg, authmethod, usessl,
                        0, LDAPConnection.parseExpiryTimeProperty(ldapCfg.getProperty(LDAPConstants.PROP_LASTGOODHOST_EXPTIME)), channel);
                status = checkLDAPParams(ldap);
            } catch (LDAPLocalException le) {
                status = FAILED_UNKNOWN;

                Throwable e = le.getRootException();

                if (e != null) {
                    debug(e.toString() + ": " + e.getMessage());
                } else {
                    debug(le.toString());
                }
            } catch (NamingException ne) {
                if (ne instanceof javax.naming.AuthenticationException) {
                    // Note that we will get an Authentication exception when
                    // the bind DN is incorrect error code 32
                    // if the password is invalid we will get error code 49
                    // this could be the cause of some confusion because users
                    // see the same message - failed password
                    status = FAILED_PASSWORD;
                } else if (ne instanceof javax.naming.CommunicationException) {
                    status = FAILED_TO_CONNECT;
                } else {
                    if (DEBUG5) {
                        ne.printStackTrace();
                        debug("The explanation is " + ne.getExplanation());
                        debug("The Root cause is " + ne.getRootCause());
                        debug("The message is " + ne.getMessage());
                    }
                    status = FAILED_UNKNOWN;
                }
            }

            if (status != CONNECT_SUCCESS) {
                invalidHosts.put(host, new Integer(status));
            }

            return invalidHosts;
        }

        String[] hosts = null;

        // List of invalid hosts
        // When you have a list of comma separated LDAP hosts each host
        // is individually verified for its setting
        if (host.indexOf(',') > 0) {
            StringTokenizer tok = new StringTokenizer(host, ",");
            hosts = new String[tok.countTokens()];

            for (int i = 0; i < hosts.length; i++) {
                hosts[i] = tok.nextToken()
                        .trim();
            }
        } else {
            hosts = new String[]{host};
        }

        for (String host1 : hosts) {
            try {
                ldap = LDAPConnection.createLDAPConnection(host1, basedn, binddn,
                        bindpasswd, localLdapCfg, authmethod,
                        usessl, 0, LDAPConnection.parseExpiryTimeProperty(ldapCfg.getProperty(LDAPConstants.PROP_LASTGOODHOST_EXPTIME)), channel);
                status = checkLDAPParams(ldap);
                //Fix for the defect 28550 since basedn is not being validated
                // in checkLDAPParams() method adding an additional check here.
                //Checking the binddn is consistent with basedn
                if (status == CONNECT_SUCCESS) {
                    try {
                        String[] attrs = ldap.getAttrs(basedn, LDAPConstants.OBJECT_CLASS);
                        if (attrs == null && attrs.length == 0) {
                            status = FAILED_BASEDN;
                        } else {
                            status = CONNECT_SUCCESS;
                        }
                    } catch (LDAPException e) {
                        status = FAILED_BASEDN;
                        if (DEBUG5) {
                            e.printStackTrace();
                        }

                    }
                }

                if (status != CONNECT_SUCCESS) {
                    invalidHosts.put(host1, new Integer(status));
                }
            } catch (LDAPLocalException le) {
                le.printStackTrace();
                Throwable e = le.getRootException();

                if (e != null) {
                    if (DEBUG5) {
                        debug(e.toString() + ": " + e.getMessage());
                    }
                } else {
                    if (DEBUG5) {
                        debug(le.toString());
                    }
                }
            }
        }

        return invalidHosts;
    }

    public HashSet getGroupList(LDAPConnection theLDAPConnection, IProperty subconfig) {
        String value =subconfig.getProperty(ISubscriptionConstants.COMPLIANCE_GROUP2CACHE);
        ArrayList targetList = new ArrayList();
        if (ISubscriptionConstants.SPECIFIED_ONLY.equals(value)){
            ArrayList targets = LDAPUtils.getTargetList(subconfig);
            if(targets != null) {
                return new HashSet(targets);
            } else if (ISubscriptionConstants.ALL_TARGETS.equals(value)){
                targetList.add("all_all");
            } else if (ISubscriptionConstants.ONLY_ALL_TARGET.equals(value)){
                targetList.add("all");
            }
        }
        return new HashSet(targetList);
    }

    /**
     * Method to verify that all the parameters to connect to the LDAP are correct. Check if we can connect to the LDAP Server and if connected , check if we
     * can load the plugin configuration or not
     *
     * @param directory An LDAPConnection object created using a host entry from the comma separated list of hosts in the plugin LDAP connection parameters.
     * @return int    FAILED_TO_CONNECT, if the ping on the directory failed. FAILED_NO_SUBCONFIG, if there is no subscription object created. FAILED_UNKNOWN,
     *         if an exception is thrown. FAILED_PASSWORD, if the authentication information fails. SUCCESS, if the check succeeded.
     */
    int checkLDAPParams(LDAPConnection directory) {
        try {
            directory.poke();
        } catch (NamingException ex) {
            if(DEBUG5) {
                ex.printStackTrace();
            }
            if (ex instanceof javax.naming.AuthenticationException) {
                int ldapErrorCode = LDAPExceptionUtils.getLDAPException(ex.getMessage());
                debug("ldap error code - " + ldapErrorCode);
                int errorCode = Utils.getErrorCode(ldapErrorCode);

                // Note that we will get an Authentication exception when
                // the bind DN is incorrect error code 32
                // if the password is invalid we will get error code 49
                // this could be the cause of some confusion because users
                // see the same message - failed password

                if(errorCode == 0) {
                    errorCode = FAILED_PASSWORD;
                }
                return errorCode;
            } else if (ex instanceof javax.naming.CommunicationException || (null != ex.getMessage() && ex.getMessage().contains("CommunicationException"))) {
                debug("communicaiton exception");
                return FAILED_TO_CONNECT;
            } else {
                debug(" The explanation is " + ex.getExplanation());
                debug(" The Root cause is " + ex.getRootCause());
                debug(" The message is " + ex.getMessage());

                return FAILED_UNKNOWN;
            }
        } catch (Exception le) {
            if(DEBUG5) {
                String errorMsg = le.getMessage();
                if(null == errorMsg) {
                    debug("Failed to authenticate. Make sure the specified DN and password are correct.");
                } else {
                    le.printStackTrace();
                }

            }
            return FAILED_PASSWORD;
        }
        return CONNECT_SUCCESS;
    }
    /**
     * This is called when an application stops and should release any resources being used. (LDAP connections, RPC, etc.)
     */
    public void destroy() {
        debug("destroy()");

        if (adminUser != null) adminUser.destroy();

        if(LDAPConnection.performanceCollector != null) {
            Iterator it = LDAPConnection.performanceCollector.getPerformanceData();
            while (it.hasNext()) {
                debug((String)it.next());
            }
        }
        ObjectManager.destroy();
        UserManager.destroy();

        if (subConfig != null) subConfig.close();
        if (mrbaConfig != null) mrbaConfig.close();
        if (txAdminProps != null) txAdminProps.close();
        if (ajaxRegistry != null) ajaxRegistry.destroy();
    }

    /**
     * Obtains the host name that is used for listing the users and user groups
     *
     * @return REMIND
     * @throws SystemException REMIND
     * @throws KnownException  REMIND
     */
    public String getTxHostName() throws SystemException {
        /* The host to use can be derived from the publish plug in URL.
         * This is because the transmitter on which the plugin is published is the
         * transmitter used for obtaining the user and group information
         */
        String urlstr = config.getProperty("subscriptionmanager.publishurl");

        if ((urlstr == null) || "".equals(urlstr)) {
            /* In order for us to obtain the transmitter for sourcing, they
             * need to publish the plugin first.  This is because they should
             * not be able to create subscription until we are able to
             * connect to the transmitter that will be usedfor authentication
             */
            throw new KnownException(USETX_NO_PLUGIN_URL);
        }

        try {
            URL purl = new URL(urlstr);
            String hostname = purl.getHost();
            int port = purl.getPort();
            debug("hostname = " + hostname);
            debug("port = " + port);

            if (port == -1) {
                return purl.getProtocol() + "://" + hostname;
            } else {
                return purl.getProtocol() + "://" + hostname + ":" + Integer.toString(port);
            }
        } catch (Exception purlexc) {
            throw new KnownException(USETX_INVALID_PLUGIN_URL, urlstr);
        }
    }

    /**
     * This method is used to obtain the user directory for the transmitter.  This is used when the users and the user groups are sourced from the transmitter.
     *
     * @param request REMIND
     * @return IUserDirectory interface to the directory which contains the users and user groups to display in the navigation.
     * @throws SystemException   thrown when there are problems with connecting to the     plugin transmitter or establishing an RPC connection.
     * @throws InternalException REMIND
     * @throws KnownException    REMIND
     */
    public IUserDirectory getUserDirectoryTx(HttpServletRequest request) throws SystemException {
        RPC rpc;
        host = getTxHostName();

        RPCHelper rpch = new RPCHelper(features, host); /* Helper class that encompasses
        * the data for the rpc connection.
        */

        IDirectory root; /* This will ecome the root directory for the RPC connection that is made.
        * The root is used for obtaining the desired class on the endpoint.
        */

        String rpcHost = rpch.getHost();
        int rpcPort = rpch.getPort();
        boolean rpcSecure = rpch.isSecure();

        if (rpcPort == -1) {
            debug("Could not get rpc port");

            return null;
        }

        debug("SubscriptionMain:RPC host is " + rpcHost + ", port = " + rpcPort);
        try {
            /*
             * Obtains the RPC object that is used for connecting to a remote tuner.
             * A RPC object handles the connections made. This means dealing with secure
             * connections and proxies.
             *
             * @see com.marimba.rpc.RPC#doConnect
             */
            rpc = (RPC) features.getChild("rpc");

            /* An RPC session is kept with each user session.  This is so that
             * we can make sure that another user does not close another users rpc session.
             * http://chrysalis/defect/defect/summary.jhtml?id=29844
             */
            User user = (User) GUIUtils.getUser(request);
            RPCSession rpcSession = user.getRPCSession();

            if (rpcSession == null) {
                debug("connect to transmitter " + rpcHost + ":" + rpcPort + " " + rpcSecure + " " + sRPCTimeout);

                try {
                    //this method needs the tuner version to be 4621 or greater
                    rpcSession = rpc.connect(rpcHost, rpcPort, rpcSecure, sRPCTimeout);
                } catch (NoSuchMethodError e) {
                    debug("Hit NoSuchMethodError  - connectiong w/o timeout ");
                    rpcSession = rpc.connect(rpcHost, rpcPort, rpcSecure);
                }
                debug("connect suceeded");

                user.setRPCSession(rpcSession);
                debug("SubscriptionMain: rpcSession was null so creating a new one; rpcSession=" + rpcSession);

            }

            root = (IDirectory) rpcSession.getRoot(IDirectory.class);
        } catch (IOException iexc) {
            /* The io exception occurs if there is problems establishing the RPC connection
             * See com.marimba.rpc.RPC#doConnect.  The IOException message string contains
             * a somewhat informative error string. However, we will still log the exception
             */
            if (RPC_SSLCONNECTIONS_NOTAVAIL.equals(iexc.getMessage())) {
                /* No connection could be made because a valid SSL Context could not be
                 * create for the client (meaning the SPM tuner).  A context is necessary in
                 * order to establish any RPC SSL connection.  This would occur if the
                 * tuner does not have any valid SSL certificates for RPC
                 *
                 * @see com.marimba.rpc.RPC#doConnect ->
                 * @see com.marimba.castanet.ssl.SSLProvider#getClientContext ->
                 * @see com.marimba.castanet.ssl.SSLContext
                 */
                KnownException ke = new KnownException(iexc, USETX_CONNECT_RPC_SSL);
                ke.setLogException(true);
                throw ke;
            } else if ((iexc.getMessage() != null) && iexc.getMessage().startsWith(RPC_URL_FAILURE)) {
                /* If the host and port passed into the rpc.connect attempt is not valid
                 * then the error message will begin with URL_FAILURE ("URL failure").
                 * This is thrown from com.marimba.rpc.RPC#doConnect.
                 *
                 * @see com.marimba.rpc.RPC#doConnect ->
                 * @see com.marimba.castanet.ssl.SSLProvider#getClientContext ->
                 * @see com.marimba.castanet.ssl.SSLContext
                 */
                throw new InternalException(iexc, USETX_CONNECT_RPC_MALFORMEDURL, rpcHost + ":" + rpcPort);
            } else {
                /* This is to cover any other kind of IOException that may occur while attempting
                 * to make the RPC connection. We have already handled the cases we could for
                 * better classification.
                 */
                CriticalException ce = new CriticalException(iexc, USETX_CONNECT_RPC_IO, iexc.getMessage());
                throw ce;
            }
        }

        /* This is the path to the remote object we would like to obtain, IServerAdmin.
         * appropriately.  The path[] below ("transmiter/admin/admin") points to the IServerAdmin
         * However, before the IServerAdmin can be obtained, the local SPM tuner must authenticate
         *
         *
         * @see #open ->
         * @see com.marimba.rpc.RPC#safeNarrow
         */
        String[] path = {"transmitter", "admin", "admin"};

        /* This grabs a hold of the transmitter Administrator interface which gives access
         * to all portions of the transmitter.  If there is no transmitter in the workspace,
         * this admin will be NULL.
         *
         * @see com.marimba.intf.transmitter.IServerAdmin#getWorkspace ->
         * @see com.marimba.intf.transmitter.ITWorkspace#getDefaultTransmitter ->
         @ @see com.marimba.intf.transmitter.ITTransmitter
         */
        IServerAdmin admin = (IServerAdmin) RPC.narrow(open(root, path, host), IServerAdmin.class);

        /* Now we are ready to obtain the user directory.  This can be used for obtaining
         * the user and user group listing.
         */
        IUserDirectory directory = null;

        try {
            directory = (IUserDirectory) admin.getUserDirectory();
        } catch (NoSuchMethodError nsme) {
            //This is a transmitter that is 4.0 or before.  We do not support this.
            throw new KnownException(USETX_OLD_TRANSMITTER, host);
        }

        /* This user directory should never be null.  This is because
         * com.marimba.apps.transmitter.Server ensure that even if a user directory cannot
         * be configured for LDAP, then it will default to the local user database.
         * It is important to note that when com.marimba.apps.transmitter.Server instantiates
         * the user directory, it does NOT attempt a connection.  A connection is only made
         * when the directory is used for listing users or groups.
         *
         * @see com.marimba.apps.transmitter.Server#createUserDirectory
         * @see com.marimba.tools.ldap.LDAPUserConnection
         */
        return directory;
    }

    String buildDigestLoginResponse(String user, String pw, String nonce) {
        String response = Password.encode("MD5", user + ":" + pw);

        return Password.encode("MD5", response + ":" + nonce);
    }

    /**
     * This method does the authentication between the local tuner (the SPM tuner) and the remote tuner (the tuner on which the plugin transmitter runs) to
     * obtain the desired remote object. Specifically, it will use the path supplied to find the remote login service and then perform a "handshake" in order
     * to allow for an RPC connection to occur.  This operation must occur before any secured object can be accessed.
     *
     * @param root This is the root directory for the REMOTE tuner (the transmitter's tuner)
     * @param path This is the path that must be traversed to find the appropriate login service     for authenticating to the transmitter administration port
     *             (RPC).  This     is usually {"transmitter","admin","admin}
     * @param host This is the remote host name that is to be authenticated against.  This     would be the transmitter host name.
     * @return Object  This is the REMOTE object that is being requested at the specified path.
     * @throws SystemException   occurs if there are problems with authenticating or with RPC     narrowing the remote object.
     * @throws InternalException REMIND
     */
    Object open(Object root, String[] path, String host) throws SystemException {
        Object remoteObj = root;

        /*
         * This loop traverses to the specified directory path of the transmitter
         * workspace until the item specified
         * is NOT of type IDirectory.  For example, in the path specified of
         * {"transmitter","admin","admin"}, the first two items listed are of type IDirectory.
         * However, the list "admin" is the object of interest. It is the last item in the
         * list that we are trying to RPC narrow into a remote object we can access.
         *
         */
        for (int i = 0; i < path.length;) {
            IDirectory dir = (IDirectory) RPC.safeNarrow(remoteObj, IDirectory.class);

            if (dir != null) {
                remoteObj = dir.getChild(path[i]);
                ++i;

                continue;
            }

            /*
             * The "admin" is a restricted resource on the REMOTE tuner, therefore, before
             * the local tuner has successfully authenticated, an object of type IDigestLogin
             * (or ILogin.. see below) will be returned until successful authentication occurs.
             *
             * The LOCAL login service will authenticate against the remote service to
             * obtain the ACTUAL object desired (IServerAdmin)
             *
             * @see com.marimba.intf.tuner.IDigestLogin#login ->
             * @see com.marimba.tools.util.KeyChain#login (location where the attempt is made
             *        to obtain the desired object )
             *
             */
            IDigestLogin digest = (IDigestLogin) RPC.safeNarrow(remoteObj, IDigestLogin.class);

            /* Since a digest is returned, we must attempt authentication since it is a restricted
             * resource.  To facilitate this com.marimba.intf.tuner.IDigestLogin#login is used.
             * NOTE: since the SPM is to be on a HEADLESS tuner, IDigestLogin is used directly.
             * If the tuner were interactive, then a com.marimba.intf.tuner.ILoginService would
             * be used to do the multiple retries when a user entered username and password in a dialog.
             */
            String username = "admin";
            String password = "admin";

            if (txAdminProps != null) {
                username = txAdminProps.getProperty(TXADMIN_USER_KEY);
                password = txAdminProps.getProperty(TXADMIN_PWD_KEY);
            } else {
                throw new InternalException(USETX_TXADMINPROPS_NOINIT);
            }

            /*
             * The administrator may not have run -txadminacces from the commandline.
             * In which case, we will attempt an anonymous login using admin, admin.
             *
             */
            if (username == null) {
                username = "admin";
            }

            if (password == null) {
                password = Password.encode("admin");
            }

            debug("SubscriptionMain: txuser = " + username);

            if (digest != null) {
                String pw = Password.decode(password);

                String nonce = digest.getNonce();
                String response = buildDigestLoginResponse(username, pw, nonce);

                debug("SubscriptionMain: response = " + response);

                try {
                    remoteObj = digest.respond(username, response);
                } catch (Exception exc) {
                    KnownException ke = null;

                    if (exc instanceof CommunicationException) {
                        CommunicationException cexc = (CommunicationException) exc;
                        ke = new KnownException(cexc.getRootCause(), USETX_AUTH_TXADMIN_FAILED_DIGEST, host);
                        throw ke;
                    } else {
                        ke = new KnownException(exc, USETX_AUTH_TXADMIN_FAILED_DIGEST, host);
                    }

                    exc.printStackTrace();
                    ke.setLogException(true);
                    throw ke;
                }

                if (remoteObj == null) {
                    KnownException ke = new KnownException(USETX_AUTH_TRANSACCESS_FAILED_DIGEST, host);
                    ke.setLogException(true);
                    throw ke;
                }

                continue; //continues on in the path iteration
            }

            /* If the remote object is configured to do just a simple authentication check,
             * and not a digestMD5 authentication, this is the type ILogin is returned.
             *
             * @see com.marimba.intf.tuner.ILogin#login ->
             * @see com.marimba.tools.util.ChildLock#login (location where the attempt is made
             *        to obtain the desired object )
             */
            ILogin login = (ILogin) RPC.safeNarrow(remoteObj, ILogin.class);

            if (login != null) {
                /* We use the simple login.  The Ilogin implementation does not expect
                 * the password to be decoded.
                 */
                try {
                    remoteObj = login.login(username, password);
                } catch (Exception exc) {
                    KnownException ke = null;

                    if (exc instanceof CommunicationException) {
                        CommunicationException cexc = (CommunicationException) exc;
                        ke = new KnownException(cexc.getRootCause(), USETX_AUTH_TXADMIN_FAILED_SIMPLE, host);
                        throw ke;
                    } else {
                        ke = new KnownException(exc, USETX_AUTH_TXADMIN_FAILED_SIMPLE, host);
                    }

                    exc.printStackTrace();
                    ke.setLogException(true);
                    throw ke;
                }

                if (remoteObj == null) {
                    KnownException ke = new KnownException(USETX_AUTH_TRANSACCESS_FAILED_SIMPLE, host);
                    ke.setLogException(true);
                    throw ke;
                }

                continue; //continues on in the path iteration
            }

            /* It is expected that the path for obtaining the remote object is valid.  If it isn't, then
             * the method has been used incorrectly.  This would occur if a remote object in the pat
             * does match what is expected.
             */
            throw new InternalException(USETX_AUTH_TRANSACCESS_WRONGPATH_ADMIN, host, "transmitter,admin,admin");
        }

        return remoteObj;
    }

    /**
     * Can't use CMS's LDAPConnection because there will be a classcastexception.  Therefore, we create a connection using the CMS LDAP settings to be used for
     * resolving username.
     *
     * @throws SystemException if failed to connect to ldap
     */
    public void initLDAP() throws SystemException {
        if(null == ldapCfg) {
            // Reread the LDAP settings from CMS in the case the LDAP
            // configuration Changes
            if (tenant.getActiveLdapCfg() == null) {
                throw new CriticalException(LDAP_CONNECT_LDAPCONFIG_NOTFOUND);
            }
            ldapCfg = tenant.getActiveLdapCfg();
        }
        //get schema version from property file

        if (config.getProperty("subscriptionmanager.configSchemaVersion") != null) {
            subscriptionConfigVersion = config.getProperty("subscriptionmanager.configSchemaVersion");
        }
        initLDAP(ldapCfg);
    }

    private void initLDAP(IConfig cfg) throws SystemException {

        try {
            this.dirType = cfg.getProperty("vendor");
            //LDAPEnv.destroy();
            LDAPConnUtils.getInstance(tenant.getConfig(), tenant.getName(), channel);
            LDAPEnv env = new LDAPEnv();
            ldapEnv = env.getInstance(cfg);
            LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(dirType);

            if (DEBUG5) {
                debug("*** SubscriptionMain initLDAP : LDAP Environment Configuration");
                listPairs(cfg);
            }

            // Try to get the subscription config and marimba config object
            IProperty iprop = ldapEnv.getSubscriptionConfig(null,false,subscriptionConfigVersion);
            IProperty iprop_mrba = ldapEnv.getMarimbaConfig(null,false,subscriptionConfigVersion);

            if (iprop instanceof Props) {
                debug("Casting subConfig to Props object");
                subConfig = (Props) iprop;
            } else {
                debug("Setting subConfig to null");
                subConfig = null;
            }
            if (iprop_mrba instanceof Props) {
                debug("Casting mrbaConfig to Props object");
                mrbaConfig = (Props) iprop_mrba;
            } else {
                debug("Setting mrbaConfig to null");
                mrbaConfig = null;
            }

            if (subConfig == null) {
                // could not find subscription config
                throw new CriticalException(LDAP_CONNECT_SUBCONFIG_NOTFOUND);
            } else if (mrbaConfig ==null) {
                //could not find marimba config
                throw new CriticalException(LDAP_CONNECT_MRBACONFIG_NOTFOUND);
            } else {

                adminUser = new AdminUser(this);
                adminUser.initialize();

                // Try a connection to the server
                LDAPConnection ldap = adminUser.getBrowseConn();

                ldap.poke();

                // If the subscription config has been initialized properly,
                // use it to initialize the ldap entry points class that is
                // used by the GUI to display the top level entry points.
                LDAPUtils.initSubsPluginConfig(subConfig, cfg.getProperty(LDAPConstants.PROP_VENDOR), tenantName, channel);
                LDAPUtils.initMrbaPluginConfig(mrbaConfig, cfg.getProperty(LDAPConstants.PROP_VENDOR), tenantName, channel);

                String sourceTx = subConfig.getProperty(LDAPConstants.CONFIG_USETRANSMITTERUSERS);

                usersInLDAP = !"true".equals(sourceTx);
                try {
                    String subbase = LDAPUtils.getLDAPVarString("CONFIG_SUBSCRIPTIONBASE", cfg.getProperty(LDAPConstants.PROP_VENDOR));
                } catch(Exception ec) {
                    ec.printStackTrace();
                }
                String securityContainerDn = createSecurityObjects();
                if(null != securityContainerDn) {
                	modifySecurityAttributes(securityContainerDn);
                }
                
                subBase = subConfig.getProperty("marimba.subscriptionplugin.securitybase");
                collBase = mrbaConfig.getProperty(LDAPConstants.CONFIG_COLLECTIONBASE);
                ldapCollBase = subConfig.getProperty(LDAPConstants.CONFIG_LDAPCOLLECTIONBASE);
                setAccessPermission();

                try {
                    LDAPConnection subsConnection = getLDAPEnv().createSubConn(ldapCfg.getProperty("userrdn"), ldapCfg.getProperty("password"));
                    subsConnection.setCanRefreshHostList(false);
                    MergeAllSub.checkAccessToken(subsConnection, getSubBase(), getLDAPVarsMap());
                } catch (SystemException sysEx) {
                    debug("Either 'checkaccesstoken' is already created or problem on creating");
                }
                initialized = true;
            }


        } catch (NamingException ne) {
            initialized = false;
            LDAPUtils.classifyLDAPException(ne, null, false);
        }
    }
    private String createSecurityObjects() {
    	String securityContainerDN = null;
    	try {
            String subscriptionBase = subConfig.getProperty("marimba.subscriptionplugin.subscriptionbase");
            String configObjectsBase = LDAPWebappUtils.getRelativeDN(subscriptionBase);
            String secuirtyBaseDN = LDAPVarsMap.get("CONTAINER_PREFIX2") + "=Security," + configObjectsBase;

            System.out.println("security base dn :" + secuirtyBaseDN);
            
            LDAPConnection subsConnection = getLDAPEnv().createSubConn(ldapCfg.getProperty("userrdn"), ldapCfg.getProperty("password"));
            subsConnection.setCanRefreshHostList(false);
                
            securityContainerDN = LDAPWebappUtils.createContainerObject(subsConnection, secuirtyBaseDN, getLDAPVarsMap());
            System.out.println("Security Container :" + securityContainerDN);
    	} catch(Exception ed) {
    		ed.printStackTrace();
    	}
    	return securityContainerDN;
    }
    private void modifySecurityAttributes(String securityBaseDN) {
    	try {
	    	// add security base attribute
    		String securityBase = subConfig.getProperty("marimba.subscriptionplugin.securitybase");
    		if(null == securityBase) {
		    	HashMap securityBaseMap = new HashMap();
		    	securityBaseMap.put("marimba.subscriptionplugin.securitybase", securityBaseDN);
		    	setSubscriptionConfigProperties(securityBaseMap);
    		}
	    	// update ldap browse hidden attribute
	    	String hiddenValues = mrbaConfig.getProperty("marimba.ldap.browse.hideentries");
	    	if(null != hiddenValues) {
	    		int index = hiddenValues.indexOf(securityBaseDN);
	    		if(index == -1) {
		    		HashMap ldapHiddenMap = new HashMap();
		    		hiddenValues = hiddenValues + ",\"" +  securityBaseDN + "\"";
		    		ldapHiddenMap.put("marimba.ldap.browse.hideentries", hiddenValues);
		    		setMarimbaConfigProperties(ldapHiddenMap);
		    		refreshSubscriptionConfig(getAdminUser().getBrowseConn());
	    		}
	    	}
    	} catch(Exception ed) {
    		ed.printStackTrace();
    	}
    }
    private void listPairs(IConfig config) {
        String[] configPairs = config.getPropertyPairs();
        debug("LDAP Config Pairs are :");
        for (int i = 0; i < configPairs.length; i += 2) {
            if (configPairs[i].indexOf("pass") == -1) {
                debug(configPairs[i] + "=" + configPairs[i + 1]);
            }
        }
    }

    public void setAccessPermission() throws NamingException {
        // store whether the ACLs are on or off
        String aclState = subConfig.getProperty(LDAPConstants.CONFIG_ACLON);
        aclsOn = false;

        if (aclState == null) {
           // debug("No ACL property in Subscription Config for ACL, defaulting to ACLs OFF");
        }

        if ("true".equals(aclState)) {
            aclsOn = true;
        }
        //debug("ACLs are " + (aclsOn ? "" : "not ") + "enabled");
    }

    /**
     * For backward compatibility.  If the namespace does not specify CN or OU, we will append CN or OU to it depending on whether the Subscription base is CN
     * or OU. This method doesn't not actually change the user's namespace on disk.  The namespace will be saved when the user reselects namespaces from the
     * Namespace configuration page.  The namespaces used in SubscriptionManager is lower case.
     *
     * @param ns REMIND
     * @return REMIND
     */
    public String upgradeNamespace(String ns) {
        if (ns != null) {
            ns = ns.toLowerCase();

            if (!ns.startsWith(LDAPVarsMap.get("CONTAINER_PREFIX")) && !ns.startsWith(LDAPVarsMap.get("CONTAINER_PREFIX2"))) {
                if (getSubBase2()
                        .startsWith(LDAPVarsMap.get("CONTAINER_PREFIX"))) {
                    ns = LDAPVarsMap.get("CONTAINER_PREFIX") + '=' + ns;
                } else {
                    ns = LDAPVarsMap.get("CONTAINER_PREFIX2") + '=' + ns;
                }
            }
        }

        return ns;
    }

    /**
     * A query on LDAP to obtain the containers directory recursively under the Subscription OU.
     *
     * @param conn REMIND
     * @return the list of namespaces under the subscription base specified in marimba.subscriptionplugin.subscriptionbase.  The list returned does NOT include
     *         the top level Subscription Base.  The return list of namespaces are all in lowercase.
     * @throws NamingException REMIND
     */
    public ArrayList listNameSpaces(LDAPConnection conn) throws NamingException {

        String searchFilter = "(|(objectclass=" + LDAPVarsMap.get("CONTAINER_CLASS") + ")(objectclass=" + LDAPVarsMap.get("CONTAINER_CLASS2") + "))";

        String vendor = ldapCfg.getProperty(LDAPConstants.PROP_VENDOR);
        if (LDAPConstants.VENDOR_AD.equals(vendor) || LDAPConstants.VENDOR_ADAM.equals(vendor)) {
            String schemaNamingContext = LDAPConnUtils.getInstance(tenantName).getSchemaNamingContext(conn);

            searchFilter = "(|(objectcategory=" +  "CN=Organizational-Unit,"+ schemaNamingContext +
                    ")(objectcategory=" +  "CN=Container," + schemaNamingContext + "))";
        }

        // get the Subscription OU or CN so that we can eliminate it from
        // the result list


        ArrayList nsList = new ArrayList(DEF_COLL_SIZE);
        String namespace = null;
        NamingEnumeration results = conn.search(searchFilter, new String[]{LDAPVarsMap.get("CONTAINER_PREFIX"), LDAPVarsMap.get("CONTAINER_PREFIX2")}, subBase, LDAPConstants.LDAP_SCOPE_SUBTREE);

        while (results.hasMoreElements()) {
            SearchResult sr = (SearchResult) results.next();

            // We are storing cn=<Namespace> or ou=<Namespace>
            // If the Subscription base is an OU, the namespaces
            // can be CN or OUs.
            // If the Subscription base is an CN, the namespaces
            // can be only be CN.
            namespace = LDAPName.unescapeJNDISearchResultName(sr.getName());

            if ((namespace != null) && (namespace.trim()
                    .length() > 0)) {
                nsList.add(namespace.toLowerCase());
            }
        }

        return nsList;
    }

    /**
     * DOCUMENT ME!
     *
     * @param srcCtx REMIND
     * @return the Subscription base prepended with the child container obtained from the ILDAPDataSourceContext.
     */
    public String getSubBaseWithNamespace(ILDAPDataSourceContext srcCtx) {
        String namespace = srcCtx.getNameSpace();

        if (namespace != null) {
            namespace = upgradeNamespace(namespace);

            return namespace + "," + subBase;
        }

        return subBase;
    }

    String[] searchAndReturnDNsAllTrees(String filter) throws LDAPLocalException, LDAPException, NamingException {
        if (LDAPConnUtils.getInstance(tenantName).isADWithAutoDiscovery(ldapCfg)) {
            // If we are in the 5.1 release and we are in the Forest Environment,
            // we will search from all the tree roots.
            Vector roots = new Vector();
            Vector results = new Vector();

            LDAPConnection dcDir = LDAPConnUtils.getInstance(tenantName).createDCConn(ldapCfg.getProperty(LDAPConstants.PROP_DOMAINPATH), ldapCfg);
            debug("dcDir: " + dcDir);
            debug("adminUser.getBrowseConn(): " + adminUser.getBrowseConn());

            String configNamingContext = LDAPConnUtils.getInstance(tenantName).getConfigNamingContext(dcDir);
            roots = dcDir.searchAndReturnDomainTrees(configNamingContext, false);

            debug("DOMAINPATH: " + ldapCfg.getProperty(LDAPConstants.PROP_DOMAINPATH));
            debug("configNamingContext: " + configNamingContext);
            debug("roots: " + roots.size());

            for (Enumeration enumElements = roots.elements(); enumElements.hasMoreElements();) {
                Name searchbase = (Name) enumElements.nextElement();
                String[] res = adminUser.getBrowseConn().searchAndReturnDNs(filter, searchbase.toString(), false);

                debug("searchbase: " + searchbase);

                if (res != null) {
                    for (String re : res) {
                        results.addElement(re);
                    }
                }
            }
            String resArr[] = null;
            if (results.size() > 0) {
                resArr = new String[results.size()];
                results.copyInto(resArr);
            }
            return resArr;
        } else {
            return adminUser.getBrowseConn().searchAndReturnDNs(filter, null, false);
        }
    }



    /**
     * Indicates whether ACL mgr setup
     */
    public int getAclInitStatus() {
        IAclMgr aclMgr = getAclMgr();
        return aclMgr.getInitStatus();
    }

    /**
     * Checks if ACLs have been initialized, if not throws a KnownException
     *
     * @throws KnownException
     */
    public void checkAclsInitialized() throws KnownException {
        int status;
        if ((status = getAclInitStatus()) != IAclMgr.INIT_SUCCESS) {
            switch (status) {
                case IAclMgr.INIT_FAILED_LDAP_CONFIG_MISSING:
                    throw new SubKnownException(IErrorConstants.ACL_LDAP_CONFIG_MISSING);

                case IAclMgr.INIT_FAILED_LDAP_CONNECTION_FAILED:
                    throw new SubKnownException(IErrorConstants.ACL_LDAP_CONNECTION_FAILED);

                default:
                    throw new SubKnownException(IErrorConstants.ACL_INIT_UNKNOWN_ERROR);
            }
        }
    }

    /**
     * This one throws a CriticalException
     *
     * @throws CriticalException
     */
    public void checkAclsInitialized2() throws CriticalException {
        int status;
        if ((status = getAclInitStatus()) != IAclMgr.INIT_SUCCESS) {
            switch (status) {
                case IAclMgr.INIT_FAILED_LDAP_CONFIG_MISSING:
                    throw new CriticalException(IErrorConstants.ACL_LDAP_CONFIG_MISSING);

                case IAclMgr.INIT_FAILED_LDAP_CONNECTION_FAILED:
                    throw new CriticalException(IErrorConstants.ACL_LDAP_CONNECTION_FAILED);

                default:
                    throw new CriticalException(IErrorConstants.ACL_INIT_UNKNOWN_ERROR);
            }
        }

    }

    public boolean hasCapability(String capability) {
        return context.getAttribute(capability) != null;
    }

    public boolean isPushEnabled() {
        return (DMHelper.getInstance(getTenantName()) != null && isDMEnabled());
    }
    private boolean isDMEnabled() {
        if( null != getConfig()){
            return "true".equals(getConfig().getProperty(IWebAppConstants.PUSH_ENABLED_CONFIG));
        } else {
            return false;
        }
    }
    public synchronized void setSubscriptionConfigProperties(HashMap map) throws SystemException {
        if(map == null) {
            throw new SubInternalException(VALIDATION_INTERNAL_WRONGARG, "setSubscriptionConfigProperties", "map");
        }
        String configDN = null;
        try {
            IProperty oldConfig = getSubscriptionConfig();
            IConfig newConfig = new ConfigDefaults(oldConfig, new ConfigProps(new Props(), null));
            Set keys = map.keySet();
            Iterator mapIt = keys.iterator();

            while(mapIt.hasNext()) {
                String key = (String)mapIt.next();
                String value = (String)map.get(key);
                newConfig.setProperty(key, value);
            }
            configDN = ldapEnv.getSubConfigDN();
            LDAPConnection conn = adminUser.getSubConn();

            ldapEnv.saveSubscriptionConfig(newConfig, configDN, conn, LDAPVarsMap);
            reloadSubscriptionConfig();
        } catch (NamingException ne) {
            LDAPUtils.classifyLDAPException(ne);
        } catch (CriticalException le) {
            throw new CriticalException(LDAP_CONNECT_SUBCONFIG_NOTFOUND);
        }
    }
    public synchronized void setMarimbaConfigProperties(HashMap map) throws SystemException {
        if(map == null) {
            throw new SubInternalException(VALIDATION_INTERNAL_WRONGARG, "setMarimbaConfigProperties", "map");
        }
        String configDN = null;
        try {
            IProperty oldConfig = getMarimbaConfig();
            IConfig newConfig = new ConfigDefaults(oldConfig, new ConfigProps(new Props(), null));
            Set keys = map.keySet();
            Iterator mapIt = keys.iterator();

            while(mapIt.hasNext()) {
                String key = (String)mapIt.next();
                String value = (String)map.get(key);
                newConfig.setProperty(key, value);
            }
            configDN = ldapEnv.getMarimbaCurConfigDN();
            LDAPConnection conn = adminUser.getSubConn();

            ldapEnv.saveMarimbaConfig(newConfig, configDN, conn);
        } catch (NamingException ne) {
            LDAPUtils.classifyLDAPException(ne);
        } catch (CriticalException le) {
            throw new CriticalException(LDAP_CONNECT_SUBCONFIG_NOTFOUND);
        }
    }

    public void refreshSubscriptionConfig(LDAPConnection conn) throws CriticalException, SystemException {
        //Refresh the subscription config in memory
        IProperty iprop = ldapEnv.getSubscriptionConfig(conn,true,subscriptionConfigVersion);

        if ((iprop != null) && (iprop instanceof Props)) {
            subConfig = (Props) iprop;
        } else {
            throw new CriticalException(LDAP_CONNECT_SUBCONFIG_NOTFOUND);
        }
        IProperty iprop_mrba = ldapEnv.getMarimbaConfig(conn,true,subscriptionConfigVersion);

        if ((iprop_mrba != null) && (iprop_mrba instanceof Props)) {
            mrbaConfig = (Props) iprop_mrba;
        } else {
            throw new CriticalException(LDAP_CONNECT_MRBACONFIG_NOTFOUND);
        }

    }

    private void reloadSubscriptionConfig() throws CriticalException, SystemException {
        //Refresh the subscription config in memory
        String currentConfigDN = ldapEnv.getSubConfigDN();
        IProperty iprop = null;
        //First try to reload using the know configuration DN using the DC connection
        try {
            if(currentConfigDN != null && currentConfigDN.length() > 0) {
                iprop = ldapEnv.getSubsConfigFromCurrentConfigDN(adminUser.getSubConn(), subscriptionConfigVersion);
            }
        }catch(Exception e) {}

        //If the configuration is not found above, use the GC connection to find the configuration.
        if(iprop == null) {
            iprop = ldapEnv.getSubscriptionConfig(adminUser.getBrowseConn(),true,subscriptionConfigVersion);
        }

        if ((iprop != null) && (iprop instanceof Props)) {
            subConfig = (Props) iprop;
        } else {
            throw new CriticalException(LDAP_CONNECT_SUBCONFIG_NOTFOUND);
        }
    }
    public void removeSubscriptionConfigProperties(HashMap map) throws SystemException, NamingException {
        if(map == null || map.size() == 0) return;

        Set keys = map.keySet();
        Iterator it = keys.iterator();
        Vector modValues = new Vector();
        while(it.hasNext()) {
            String key = (String)it.next();
            String val = (String)map.get(key);
            modValues.add(key + "=" + val);
        }
        ModificationItems modItems = new ModificationItems();
        modItems.removeAttribute(LDAPVarsMap.get("PROPERTY"), modValues);
        LDAPConnection conn = adminUser.getSubConn();
        if(conn != null) {
            conn.modifyObject(ldapEnv.getSubConfigDN(),modItems.getArray());
        }
        reloadSubscriptionConfig();
    }

    public boolean isValidComplianceTarget(StringBuffer bufferedName) {

        if(bufferedName == null) return false;
        String name = bufferedName.toString();
        if(name == null || name.trim().length() ==0) return false;
        try {
            if(adminUser.getBrowseConn().getParser().isDN(name)) {
                //See if this DN actually exists by searching for it
                if(doesDNExist(name)) return true;
            }else{
                //Not a DN format - check if valid CN exists
                String[] dns = resolveTargetDN(name);
                if(dns != null && dns.length >0) {
                    bufferedName.delete(0,bufferedName.length());
                    bufferedName.append(dns[0]);
                    return true;
                }
            }
        }catch(Exception e){}
        return false;
    }

    private boolean doesDNExist(String target) {

        try {
            String query = "(" + LDAPVarsMap.get("MACHINE_NAME") + "=*)";
            NamingEnumeration ne = adminUser.getBrowseConn().search(query, null, target, SearchControls.OBJECT_SCOPE);
            if(ne != null) {
                if(ne.hasMore()) return true;
            }
            query = "(" + LDAPConstants.OBJECT_CLASS + "=*)";
            ne = adminUser.getBrowseConn().search(query, null, target, SearchControls.OBJECT_SCOPE);
            if(ne != null) {
                if(ne.hasMore()) return true;
            }

        } catch (Exception se) {}

        return false;

    }

    private String[] resolveTargetDN(String target) throws SubKnownException {
        IProperty subConfig = getSubscriptionConfig();
        IProperty mrbaConfig = getMarimbaConfig();
        // Shouldn't use DOMAINASDN
        String    base = adminUser.getProperty(LDAPConstants.PROP_DOMAINASDN);
        String[]  dns = null;
        target = LDAPSearchFilter.escapeComponentValue(target);

        if (!LDAPVars.ACTIVE_DIRECTORY.equals(LDAPVarsMap.get("DIRECTORY_TYPE"))) {
            base = adminUser.getProperty(LDAPConstants.PROP_BASEDN);
        }

        try {
            //check if this is a machine
            String attr = subConfig.getProperty(LDAPConstants.CONFIG_MACHINENAMEATTR);
            String machineClass = mrbaConfig.getProperty(LDAPConstants.CONFIG_MACHINECLASS);
            String query = "(&(" + attr + "=" + LDAPSearchFilter.escapeComponentValue(target) + ")(objectclass=" + machineClass + "))";
            dns = adminUser.getBrowseConn().searchAndReturnDNs(query, base, false);
            if(dns != null && dns.length > 0) return dns;

            attr = subConfig.getProperty(LDAPConstants.CONFIG_USERIDATTR);
            String userClass = subConfig.getProperty(LDAPConstants.CONFIG_USERCLASS);
            query = "(&(" + attr + "=" + target + ")(objectclass=" + userClass + "))";
            dns = adminUser.getBrowseConn().searchAndReturnDNs(query, base, false);
            if(dns != null && dns.length > 0) return dns;

            query = "(&(cn=" + target + ")(|(objectclass=organizationalunit)(objectclass=container)))";
            dns = adminUser.getBrowseConn().searchAndReturnDNs(query, base, false);
            if(dns == null) {
                query = "(&(ou=" + target + ")(|(objectclass=organizationalunit)(objectclass=container)))";
                dns = adminUser.getBrowseConn().searchAndReturnDNs(query, base, false);
            }
            if(dns != null && dns.length > 0) return dns;

            String groupClass = subConfig.getProperty(LDAPConstants.CONFIG_GROUPCLASS);
            // group class can be comma separated list
            StringTokenizer st = new StringTokenizer(groupClass, ",");
            String          searchStr = "";
            int             items = 0;

            while (st.hasMoreTokens()) {
                searchStr += ("(objectclass=" + st.nextToken() + ")");
                items++;
            }

            if (items > 1) {
                searchStr = "(|" + searchStr + ")";
            }

            query = "(&(cn=" + target + ")" + searchStr + ")";
            dns = adminUser.getBrowseConn().searchAndReturnDNs(query, base, false);

        } catch (LDAPException le) {
            throw new SubKnownException(SUB_LDAP_RESOLVETARGETDN, target);
        }

        return dns;
    }

    // Read a specific attribute(s) for a particular DN, eg: mail, displayName, etc.....
    // Refer http://www.kouti.com/tables/userattributes.htm
    public String readAttribute(String dn, String attrId) {
        String rtnValue = "";
        try {
            String[] attrValues = getAdminUser().getBrowseConn().getAttrs(dn, attrId);
            for (String attrValue: attrValues) {
                rtnValue = rtnValue + "," + attrValue;
            }
        } catch (LDAPException e) {
            e.printStackTrace();
        }
        rtnValue = rtnValue.trim();
        rtnValue = rtnValue.startsWith(",") ? rtnValue.substring(1, rtnValue.length()) : rtnValue;
        rtnValue = rtnValue.endsWith(",") ? rtnValue.substring(0, rtnValue.length() - 1) : rtnValue;
        return rtnValue;
    }

    public String getDisplayName(String reviewedUserDN) {
        String displayName = readAttribute(reviewedUserDN, LDAPConstants.DISPLAY_NAME);
        if (displayName.isEmpty()) {
            debug("Failed to read display name for the user [" + reviewedUserDN + "] from ldap");
        } else {
            debug("Display name of the user [" + reviewedUserDN + "] in ldap : " + displayName);
        }
        return displayName;
    }

    public String getMailId(String createdUserDN) {
        String mailId =  readAttribute(createdUserDN, "mail");
        if (mailId.isEmpty()) {
            debug("Failed to read mail-id for the user [" + createdUserDN + "] from ldap");
        } else {
            debug("Mail-id of the user [" + createdUserDN + "] in ldap : " + mailId);
        }
        return mailId;
    }

    class StringResourcesAdapter implements ISubscriptionStringResources {

        MessageResources messageResources;

        StringResourcesAdapter(MessageResources messageResources) {
            StringResourcesAdapter.this.messageResources = messageResources;
        }

        public String getMessage(String key) {
            return getMessage(key, null, null);
        }

        public String getMessage(String key, Object arg0) {
            return getMessage(key, arg0, null);
        }

        public String getMessage(String key, Object arg0, Object arg1) {
            String message = null;
            if(messageResources != null) {
                message = messageResources.getMessage(key, arg0, arg1);
            }
            return message;
        }
    }

    public void sendMail(MailFormatter mailFormatter) {
        sendMail(mailFormatter, null, false);
    }

    public void sendMail(MailFormatter mailFormatter, String senderAddress, boolean isDeviceRegister) {

        String smtpHost = getServerConfig().getProperty(PROP_SMTP_HOST); // Refer msf.txt for SMTP settings properties
        if (null == smtpHost || smtpHost.trim().isEmpty()) {
            debug("SMTP settings were not enabled, will not send policy update report mail");
            return;
        }

        String emailSettings = null;
        String toAddress = null;
        IConfig spmConfig = getConfig();

        if(isDeviceRegister) {
            toAddress = senderAddress;
            if (null == toAddress || toAddress.trim().isEmpty()) {
                debug("Could not find to addresses, it's required to send device registration notification mail");
                return;
            }
        } else {
            emailSettings = spmConfig.getProperty(EMAIL_SETTINGS);
            if (null == emailSettings || emailSettings.trim().isEmpty()) {
                debug("E-Mail settings were not enabled in Policy Manager configuration page");
                return;
            }
            toAddress = spmConfig.getProperty(EMAIL_TO_ADDRESS);
            if (null == toAddress || toAddress.trim().isEmpty()) {
                debug("Could not find to addresses, it's required to send policy update notification mail");
                return;
            }
        }

        IConfig cmsConfig = getServerConfig();
        Map<String, String> props = new HashMap<String, String>(10);

        props.put(PROP_SMTP_HOST, smtpHost);
        props.put(PROP_SMTP_PORT, cmsConfig.getProperty(PROP_SMTP_PORT));
        props.put(PROP_SMTP_USER, cmsConfig.getProperty(PROP_SMTP_USER));
        props.put(PROP_SMTP_PWD, cmsConfig.getProperty(PROP_SMTP_PWD));
        String useAuth = Boolean.valueOf(cmsConfig.getProperty(PROP_USE_AUTH)) ? "true" : "false";
        props.put(PROP_USE_AUTH, useAuth);
        if (null != mailFormatter.getCreatedByMailId() && !mailFormatter.getCreatedByMailId().trim().isEmpty()) {
            // Just swapping configured mail id's to CC address and created user mail id to TO address,
            // Since this is applicable only for approval mail.
            // For policy update mail mpde, we have send to all configured id's and there is no CC.
            String ccAddress = toAddress;
            props.put(PROP_MAIL_CC, ccAddress);
            toAddress = mailFormatter.getCreatedByMailId();
        }
        props.put(PROP_MAIL_TO, toAddress);

        new Thread(new MailSender(props, mailFormatter)).start();
    }

    public void storeApprovalPolicy2DB(List<PolicyDiff> policyDiffs) {
        try {
            if(!isPeerApprovalEnabled()) {
                debug("Policy approval settings were not enabled, will not store policy diff's in database for approval");
                return;
            }

            if(policyDiffs.isEmpty()) {
                debug("Unable to store policy changes in database for approval since there is no policy diff");
                return;
            }

            // Store Policy Change to Database
            PolicyApprovalHandler policyChange = new PolicyApprovalHandler();
            policyChange.setCreated_on();
            policyChange.setLdapSource(getSubBase());
            policyChange.policyDiff2DBFormat(policyDiffs);

            if(null == dbStorage) {
                dbStorage = new ApprovalPolicyStorage(this);
            }
            dbStorage.setApprovalPolicy(policyChange.getApprovalPolicy());
            dbStorage.storeApprovalPolicy();

        } catch(ApprovalPolicyException apex) {
            debug("Failed to store policy changes in database for approval : " + apex.getMessage());
            if(DEBUG5) apex.printStackTrace();
        }
    }
    public boolean hasPolicyChange() {
        return hasPolicyChange;
    }
    public void setPolicyChange(boolean change) {
        hasPolicyChange = change;
    }
    /**
     * Update Pending Policy detail store to session variable
     * @param request
     * @param target
     */
    public void updatePendingPolicySessionVar(HttpServletRequest request, Target target) {
        IUser user;

        HttpSession session = request.getSession();

        boolean excludeMode = false; // clear session value except copy operation or multimode
        LoadDBPolicy dbPolicy;
        String modifiedUserDN = null;

        boolean isApprovalPolicy = isPeerApprovalEnabled();

        if(!isApprovalPolicy) {
            // clear approval policy session var
            if(null != session.getAttribute(PENDING_POLICY_SAMEUSER)) {
                session.removeAttribute(PENDING_POLICY_SAMEUSER);
            }
            if(null != session.getAttribute(PENDING_POLICY_DIFFUSER)) {
                session.removeAttribute(PENDING_POLICY_DIFFUSER);
            }
            return;
        }
        debug("Entering into update pending policy to session variable");
        user = (IUser) session.getAttribute(SESSION_SMUSER);

        try {
            modifiedUserDN = resolveUserDN(user.getName());
        } catch (Exception e) {
            if (DEBUG5) e.printStackTrace();
        }

        // This list describes list of same user and different user has some pending policy for an approval
        List<Target> suserPendingList = (List<Target>) session.getAttribute(PENDING_POLICY_SAMEUSER);
        if(null == suserPendingList) {
            suserPendingList = Collections.synchronizedList(new ArrayList<Target>(DEF_COLL_SIZE));
        }

        List<Target> duserPendingList = (List<Target>) session.getAttribute(PENDING_POLICY_DIFFUSER);
        if(null == duserPendingList) {
            duserPendingList = Collections.synchronizedList(new ArrayList<Target>(DEF_COLL_SIZE));
        }
        if (null != session.getAttribute(SESSION_MULTITGBOOL) || null != session.getAttribute("target_rhs_list")) {
            excludeMode = true; // clear session value except copy operation or multimode
        }
        debug("excludeMode:"  + excludeMode);
        if (!excludeMode) {
            // clear session value except copy operation or multimode
            suserPendingList.clear();
            duserPendingList.clear();
        }

        try {
            dbPolicy = new LoadDBPolicy(getDBStorage());
            List<ApprovalPolicyDTO> pendingPolicyList = dbPolicy.getPendingPolicyByTarget(target.getName(),
                    target.getId(), target.getType());
            debug("Pending Target List size() - " + pendingPolicyList.size());
            if(pendingPolicyList.size() > 0) {
                for(ApprovalPolicyDTO policy : pendingPolicyList) {
                    // logged user is equal to created pending request user then add to same user
                    // list otherwise different user list
                    if (null != policy.getChangeOwner()
                            && null != modifiedUserDN) {
                        if (policy.getChangeOwner().equalsIgnoreCase(modifiedUserDN)) {
                            debug("Policy Change Owner - " + modifiedUserDN);
                            debug("Added Target - " + target.getId());
                            debug("Approval pending policy information update to same user session");
                            suserPendingList.add(target);
                        } else {
                            debug("Policy Change Owner - " + policy.getChangeOwner());
                            debug("Logged User - " + modifiedUserDN);
                            debug("Added Target - " + target.getId());
                            debug("Approval pending policy information update to different session");
                            duserPendingList.add(target);
                        }
                    }
                }
            }
            session.setAttribute(PENDING_POLICY_SAMEUSER, suserPendingList);
            session.setAttribute(PENDING_POLICY_DIFFUSER, duserPendingList);
        } catch(Exception e) {
            if(DEBUG5) e.printStackTrace();
            debug("Failed to update pending policy target to session variable : " + target.getId());
        }
    }

    private void initSiteInfo() {
        String masterTxURL = config.getProperty(SITE_BASED_MASTER_TX_URL);
        if (null == masterTxURL || masterTxURL.trim().isEmpty()) {
            debug("WARNING: Site based deployment is enabled, but master Tx url is not valid.");
            return;
        }
        this.sitesInfo = new SBRPSiteHttpConnector(tunerConfig, features, tenantName, channel);
        this.sitesInfo.sendRepInfoHTTPRequest(masterTxURL);
    }

    public SBRPSiteHttpConnector getSBRPSiteHttpConnector() {
        if (!isSiteBasedPolicyEnabled()) {
            this.sitesInfo = null;
            return null;
        }
        if (null == this.sitesInfo) initSiteInfo();

        return this.sitesInfo;
    }

    // Below methods used to check advanced settings option

    public boolean isPeerApprovalEnabled() {
        return Boolean.valueOf(getConfig().getProperty(PEER_APPROVAL_SETTINGS));
    }
    public boolean isPluginConfigured() {
    	boolean isPluginConfigured = false;
    	if(null != getConfig()) {
    		String pluginURl = getConfig().getProperty("subscriptionmanager.publishurl");
    		if(null != pluginURl) {
    			isPluginConfigured = true;
    		}
    	}
    	return isPluginConfigured;
    }
    public boolean isEmailFeatureEnabled() {
        return Boolean.valueOf(getConfig().getProperty(EMAIL_SETTINGS));
    }
    public String getPeerApprovalType() {
        if( null != getConfig()){
            return (getConfig().getProperty(PEER_APPROVAL_TYPE));
        } else {
            return "ldap";
        }
    }

    // This is only consumed by schedule_ontime.jsp page. also we have methods in DistAsgForm.java form for all other pages
    public boolean isWoWEnabled() {
        return Boolean.valueOf(tenant.getConfig().getProperty(ENABLE_WOW_FEATURE));
    }
    // wow is not applicable for CLOUD mode. so we will not show relevant settings in the UI
    public boolean isWoWApplicable() {
        return tenantMgr.isTenantModel() || tenantMgr.isDefaultModel();
    }

    public boolean isSiteBasedPolicyEnabled() {
        if(null == subConfig) {
            try {
                initLDAP();
            } catch(Exception ex) {

            }
        }

        if (subConfig != null) {
            return Boolean.valueOf(subConfig.getProperty(LDAPConstants.CONFIG_SITEBASEDPOLICY_ENABLED));
        }

        return false;
    }

    public void publishPlugin(HashMap<String, String> props, String userName, String password) {
        if (props != null && props.size() > 0) {
            for (String key : props.keySet()) {
                config.setProperty(key, props.get(key));
            }
        }

        PublishThread pb = new PublishThread(null, this, config, new String[]{userName}, new String[]{password});
        new Thread(pb).start();

        debug("Publishing security plugin is initiated.");
    }

    // Log audit messages to the tuner audit log file

    public void logAuditInfo(int id, int severity,  String source, String description, HttpServletRequest req) {
        logAuditInfo(id, severity,  source, description, req, null);
    }

    public void logAuditInfo(int id, int severity,  String source, String description, HttpServletRequest req, String target) {
        String user = null;
        try {
            IUser iUser = GUIUtils.getUser(req);
            user = iUser.getName() + " (" + iUser.getFullName() +")";
        } catch (SystemException se) {
            //we attempt to get the user, however if we cannot, we simply continue logging the action
        }
        channel.log(id, System.currentTimeMillis(), severity, source, user, description, target);
    }

    /**
     * Method used to log audit messages to common audit log
     *
     * @param id  Message identifier or number
     * @param severity Message Severity
     * @param source Module generating message
     * @param user User doing current action
     * @param message Log message or Throwable to capture exceptions
     */
    public void log(int id, int severity, String source, String user, Object message ) {
        log(id, severity, source, user, message, null);
    }

    public void log(int id, int severity, String source, String user, Object message, String target ) {
        channel.log(id, System.currentTimeMillis(), severity, source, user, message, target);
    }

    public void log(int id, String tenantName, int severity, String source, String user, Object message, String target ) {
        channel.tenantLog(id, tenantName, System.currentTimeMillis(), severity, source, user, message, target);
    }


    private void debug(String msg) {
        if(DEBUG5) System.out.println("Security Main: " + msg);
    }

    public Properties getCVEInfoProps() {
        Properties props = new Properties();
        try {
            IChannel securityInfoChannel = SCAPUtils.getSCAPUtils().getSecurityInfoChannelCreated();
            debug("CVE Info, securityInfoChannel - " + securityInfoChannel);
            if (securityInfoChannel != null) {
                String tunerWorkspace = tunerConfig.getProperty("runtime.workspace.dir");
                String securityInfoDataDir = workspace.getChannelFolderName(securityInfoChannel.getURL()) + File.separator + "data";
                String securityInfoChannelDataDir = tunerWorkspace + File.separator + securityInfoDataDir;
                File cveInfoFile = new File(securityInfoChannelDataDir, "cve_info.properties");
                debug("CVE Info, cveInfoFile - " + cveInfoFile);
                if (cveInfoFile.exists()) {
                    props.load(new FileInputStream(cveInfoFile));
                }
            }
        } catch(Exception ed) {
            ed.printStackTrace();
        }
        return props;
    }

    public int getExcelReportEntriesPerSheet() {
        int MAX_ENTRIES_PER_SHEET_DEFAULT = 50000;
        int MAX_ENTRIES_PER_SHEET = MAX_ENTRIES_PER_SHEET_DEFAULT;
        String vDeskExcelReportEntriesPerSheetStr = tunerConfig.getProperty("marimba.vdesk.report.excel.sheet.entries.max");
        debug("getExcelReportEntriesPerSheet(), vDeskExcelReportEntriesPerSheetStr: " + vDeskExcelReportEntriesPerSheetStr);
        if ((vDeskExcelReportEntriesPerSheetStr == null) || (vDeskExcelReportEntriesPerSheetStr.trim().length() < 1)) {
            // use default...
        } else {
            try {
                MAX_ENTRIES_PER_SHEET = Integer.parseInt(vDeskExcelReportEntriesPerSheetStr);
                if (MAX_ENTRIES_PER_SHEET > 65500) {
                    MAX_ENTRIES_PER_SHEET = MAX_ENTRIES_PER_SHEET_DEFAULT;
                }
            } catch (Throwable t) {
                MAX_ENTRIES_PER_SHEET = MAX_ENTRIES_PER_SHEET_DEFAULT;
            }
        }
        return MAX_ENTRIES_PER_SHEET;
    }
}
