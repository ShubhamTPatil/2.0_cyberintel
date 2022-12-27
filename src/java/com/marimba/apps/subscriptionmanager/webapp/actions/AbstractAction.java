// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.LogConstants;
import com.marimba.apps.subscription.common.intf.SubInternalException;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscriptionmanager.MergeAllSub;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.system.LDAPBean;
import com.marimba.apps.subscriptionmanager.webapp.system.PagingBean;
import com.marimba.intf.castanet.IChannel;
import static com.marimba.intf.msf.AppManagerConstants.MGR_FEATURES;
import static com.marimba.intf.msf.AppManagerConstants.MGR_SERVER;
import com.marimba.intf.msf.IAccessControlMgr;
import com.marimba.intf.msf.IServer;
import com.marimba.intf.msf.ITenant;
import com.marimba.intf.msf.ITenantManager;
import com.marimba.intf.msf.task.ITaskMgr;
import com.marimba.intf.msf.wakeonwan.IWakeManager;
import com.marimba.intf.util.IDirectory;
import com.marimba.tools.ldap.LDAPPagedSearch;
import com.marimba.tools.util.URLUTF8Encoder;
import com.marimba.webapps.intf.CriticalException;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.tools.action.DelayedAction;
import com.marimba.webapps.tools.util.PropsBean;
import com.marimba.webapps.tools.util.WebAppUtils;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * A base Action class that contains several commonly used functions.
 *
 * @author Theen-Theen Tan
 * @version  $Revision$,  $Date$
 */

public abstract class AbstractAction extends DelayedAction implements IWebAppConstants, ISubscriptionConstants, LogConstants, IErrorConstants, IWebAppsConstants {

    protected IServer server;
    protected ITenant tenant;
    protected ITaskMgr taskMgr;
    protected IDirectory features;
    protected IWakeManager wakeMgr;
    protected SubscriptionMain main;
    protected ServletContext context;
    protected IAccessControlMgr acmgr;
    protected ITenantManager tenantMgr;
    protected MessageResources resources;
    protected Class klass = this.getClass();
    protected ArrayList conflicts = new ArrayList(10);
    private static PrintStream sDebugPrintStream = System.out;
    private static boolean sIsDebugEnabled = com.marimba.apps.subscriptionmanager.intf.IAppConstants.DEBUG;
    protected Map<String, String> LDAPVarsMap;
    protected String tenantName;
    protected IChannel channel;

    protected void init(HttpServletRequest request) {
        try {
            this.context = getServlet().getServletConfig().getServletContext();
            this.features = (IDirectory) context.getAttribute(MGR_FEATURES);
            this.server = (IServer) features.getChild(MGR_SERVER);
            this.tenantMgr = (ITenantManager) features.getChild("tenantMgr");
            String tenantName = TenantHelper.getTenantName(request);
            if(null != tenantName && !"admin".equalsIgnoreCase(tenantName)) {
                this.tenant = TenantHelper.getTenantObject(tenantMgr, tenantName);
                this.taskMgr = tenant.getTaskMgr();
                this.wakeMgr = tenant.getWakeManager();
                this.acmgr = tenant.getAccessControlMgr();
                this.resources = (MessageResources) context.getAttribute(Globals.MESSAGES_KEY);
                if(null != tenant && hasLocalDBConfig()) {
                    throw new Exception("Local user can't access vDesk");
                }
                this.main = TenantHelper.getTenantSubMain(context, request);
                this.LDAPVarsMap = main.getLDAPVarsMap();
                this.tenantName = main.getTenantName();
                this.channel = main.getChannel();
            }
        } catch(Exception ec) {
            if (DEBUG2) {
                ec.printStackTrace();
            }
            System.out.println("Exception getClass() name : "+ ec.getClass().getName());
//        	WebAppUtils.saveInitException(request.getSession(), new CriticalException("error.ldap.connect.failed"));
        }
    }

    public boolean isCloudEnabled() {
        return tenantMgr.isCloudModel();
    }

    protected void initApp(HttpServletRequest request) {
        this.context = getServlet().getServletConfig().getServletContext();
        this.resources = (MessageResources) context.getAttribute(Globals.MESSAGES_KEY);
        this.features = (IDirectory) context.getAttribute(MGR_FEATURES);
        this.server = (IServer) features.getChild(MGR_SERVER);
        this.tenantMgr = (ITenantManager) features.getChild("tenantMgr");

    }
    protected boolean hasLocalDBConfig() {
        try {
            return ("local".equals(this.tenant.getAccessControlMgr().getActive()));
        } catch(Exception ec) {

        }
        return false;
    }
    protected boolean hasLocalDBConfig(ITenant tenant) {
        try {
            return ("local".equals(tenant.getAccessControlMgr().getActive()));
        } catch(Exception ec) {

        }
        return false;
    }
    /**
     * This provides a utility for clearing out the session variables that  were used before committing the data to storage.  These session variables are known
     * as a the "transaction session variable".   Additionally, it is important to note that there is
     *
     * @param req     REMIND
     * @param pagekey REMIND
     */
    public void clearTransactSessions(HttpServletRequest req, String pagekey) {
    }

    /**
     * REMIND
     *
     * @param req     REMIND
     * @param pagekey REMIND
     */
    public void resetTransactSession(HttpServletRequest req, String pagekey) {
    }

    /**
     * REMIND
     *
     * @param req REMIND
     * @return REMIND
     */
    public static DistributionBean getDistributionBean(HttpServletRequest req) {
        HttpSession session = req.getSession();
        DistributionBean distbean = (DistributionBean) session.getAttribute(SESSION_DIST);

        if (null == distbean) {
            distbean = new DistributionBean();
        }

        return distbean;
    }

    /**
     * REMIND
     *
     * @param req REMIND
     * @return REMIND
     */
    public static DistributionBean getDistributionBeanCopy(HttpServletRequest req) {
        HttpSession session = req.getSession();
        DistributionBean distbean = (DistributionBean) session.getAttribute(SESSION_COPY);

        if (null == distbean) {
            distbean = new DistributionBean();
        }

        return distbean;
    }

    /**
     * REMIND
     *
     * @param distbean REMIND
     * @param req      REMIND
     * @throws SubInternalException REMIND
     */
    public void setDistributionBean(DistributionBean distbean,
                                    HttpServletRequest req)
            throws SubInternalException {
        if (null == distbean) {
            throw new SubInternalException(CANT_SET_NULL_SYSSTATE_DIST_BEAN);
        }

        HttpSession session = req.getSession();
        session.setAttribute(SESSION_DIST, distbean);
    }

    public void setDistributionBeanCopy(DistributionBean distbean,
                                        HttpServletRequest req)
            throws SubInternalException {
        if (null == distbean) {
            throw new SubInternalException("Can't set null system state: Distribution Bean");
        }

        HttpSession session = req.getSession();
        session.setAttribute(SESSION_COPY, distbean);
    }

    /**
     * REMIND
     *
     * @param req REMIND
     */
    public void removeDistributionBean(HttpServletRequest req) {
        HttpSession session = req.getSession();
        session.removeAttribute(SESSION_DIST);
    }

    public void removeDistributionBeanCopy(HttpServletRequest req) {
        HttpSession session = req.getSession();
        session.removeAttribute(SESSION_COPY);
    }

    /**
     * REMIND
     *
     * @param req REMIND
     * @return REMIND
     */
    public static LDAPBean getLDAPBean(HttpServletRequest req) {
        HttpSession session = req.getSession();
        LDAPBean ldapBean = (LDAPBean) session.getAttribute(SESSION_LDAP);

        if (null == ldapBean) {
            ldapBean = new LDAPBean();
        }

        return ldapBean;
    }

    /**
     * REMIND
     *
     * @param ldapBean REMIND
     * @param req      REMIND
     * @throws SubInternalException REMIND
     */
    public void setLDAPBean(LDAPBean ldapBean,
                            HttpServletRequest req)
            throws SubInternalException {
        if (null == ldapBean) {
            throw new SubInternalException(SYSTEM_INTERNAL_STATE_NULL, "LDAPBean");
        }

        HttpSession session = req.getSession();
        session.setAttribute(SESSION_LDAP, ldapBean);
    }

    /**
     * REMIND
     *
     * @param req REMIND
     * @return REMIND
     */
    public PagingBean getPagingBean(HttpServletRequest req) {
        HttpSession session = req.getSession();
        PagingBean pagingBean = (PagingBean) session.getAttribute(SESSION_PAGE);

        if (null == pagingBean) {
            pagingBean = new PagingBean();
        }

        return pagingBean;
    }

    /**
     * REMIND
     *
     * @param pagingBean REMIND
     * @param req        REMIND
     * @throws SubInternalException REMIND
     */
    public void setPagingBean(PagingBean pagingBean,
                              HttpServletRequest req)
            throws SubInternalException {
        if (null == pagingBean) {
            throw new SubInternalException(SYSTEM_INTERNAL_STATE_NULL, "pagingBean");
        }

        HttpSession session = req.getSession();
        session.setAttribute(SESSION_PAGE, pagingBean);
    }

    /**
     * REMIND
     *
     * @param req REMIND
     */
    public void removeLDAPBean(HttpServletRequest req) {
        HttpSession session = req.getSession();
        session.removeAttribute(SESSION_LDAP);
    }

    /**
     * REMIND
     *
     * @param e REMIND
     * @return REMIND
     */
    public static String getErrorString(Exception e) {
        if (e.getMessage() == null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            return sw.toString();
        } else {
            return e.getMessage();
        }
    }

    /**
     * REMIND
     *
     * @param list REMIND
     * @return REMIND
     */
    public static String listToString(ArrayList list) {
        if (list == null) {
            return null;
        }

        StringBuffer buf = new StringBuffer(1024);

        for (Iterator ite = list.iterator(); ite.hasNext();) {
            buf.append(ite.next() + "\n");
        }

        return buf.toString();
    }

    // This method takes a path and creates a path to redirect to
    public String createPath(HttpServletRequest request,
                             String subPath) {
        String path = (request.getRequestURL().toString());

        try {
            path = path.substring(0, path.lastIndexOf("/spm/"));

            return (path + subPath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return path;
    }

    protected String getSelected(HttpServletRequest request,
                                 Object parameter) {
        if (parameter instanceof String) {
            // This item was checked when the user was visiting this page
            // previously.  On this visit, the user has cleared
            // it out.  Therefore parameter holds the old value.
            // We want to clear the value.
            return null;
        } else {
            if (parameter == null) {
                return null;
            } else {
                return ((String[]) parameter)[0];
            }
        }
    }

    protected String getString(String key) {
        String val = null;
        if (resources != null) {
            val = resources.getMessage(key);
        }
        return val;
    }

    protected String getString(Locale locale, String key) {
        String val = null;
        if (resources != null) {
            val = resources.getMessage(locale, key);

        }
        return val;
    }

    protected String getString(Locale locale, String msgKey, String arg) {
        String val = null;
        if (resources != null) {
            val = resources.getMessage(locale, msgKey, arg);
        }
        return val;
    }

    protected String getString(Locale locale, String msgKey, Object args[]) {
        String val = null;
        if (resources != null) {
            val = resources.getMessage(locale, msgKey, args);
        }
        return val;
    }


    //
    // Begin set up for using DelayedAction
    //

    public Task create(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        return createTask(mapping, form, request, response);
    }

    /**
     * REMIND
     *
     * @param task     REMIND
     * @param mapping  REMIND
     * @param form     REMIND
     * @param request  REMIND
     * @param response REMIND
     * @return REMIND
     * @throws IOException      REMIND
     * @throws ServletException REMIND
     */
    public ActionForward done(Task task, ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        SubscriptionDelayedTask sTask = (SubscriptionDelayedTask) task;
        if (sTask.guiException != null) {
            throw sTask.guiException;
        }
        return sTask.forward;
    }

    /**
     * Factory method for creating the right task.
     */
    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {

        return new SubscriptionDelayedTask(mapping, form, request, response);
    }

    protected class SubscriptionDelayedTask extends Task {

        protected ActionForm form;
        protected ActionMapping mapping;
        protected HttpServletRequest request;
        protected HttpServletResponse response;

        protected Locale locale;
        protected HttpSession session;
        protected ActionForward forward;
        protected GUIException guiException;

        protected SubscriptionDelayedTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
            this.form = form;
            this.mapping = mapping;
            this.request = request;
            this.response = response;
            this.session = request.getSession();
            this.locale = this.request.getLocale();
        }

        /**
         * REMIND
         *
         * @return REMIND
         */
        public String getWaitMessage() {
            return getString(locale, "page.ldap_browse_wait.Title");
        }

        public void execute() {
            forward = new ActionForward(mapping.getPath());
        }
    }

    //
    // End set up for using DelayedAction
    //



    /**
     * Creates a Target object from request 'name', 'type' or 'targetType', 'id'
     * parameters.  All three must be present.
     * <p/>
     * 'type' is LDAP object class.  If this is given, the objectclass will be
     * converted to Subscription target type.
     * 'targetType' is Subscription target type which will be used to create the
     * Target object directly.
     * <p/>
     * If the request parameters are not given, we look into the session variable
     * given in sessionvar for the Target object.
     */
    public static Target getTarget(HttpServletRequest request, SubscriptionMain main)
            throws SystemException {

        String targetName = request.getParameter("name");
        String targetType = request.getParameter("type");
        String targetID = request.getParameter("id");
        LDAPBean ldapBean = getLDAPBean(request);

        Target target;
        if (targetName == null && targetType == null && targetID == null) {
            return null;
        } else {
            if (targetType == null) {
                targetType = request.getParameter("targetType");
            } else if(targetType.equals(IWebAppConstants.INVALID_NONEXIST)){
                return new Target(targetName, targetType, targetID);
            }else{
                targetType = LDAPUtils.objClassToTargetType(targetType, main.getLDAPVarsMap());
            }
            // If sourcing users from Transmitter, and we are not browsing user
            // group from the Transmitter, the default Target type will be
            // machinegroup
            if (!main.getUsersInLDAP() && TYPE_USERGROUP.equals(targetType) && "ldap".equals(ldapBean.getEntryPoint())) {
                targetType = TYPE_MACHINEGROUP;
            }
            target = new Target(targetName, targetType, targetID);
        }
        return target;
    }

    protected static ActionForward getForward(HttpServletRequest request) {
        String forward = request.getParameter("forward");
        String src = request.getParameter("src");
        return getForward(forward, src);
    }

    protected static ActionForward getForward(String forward, String src) {
        int idx = forward.indexOf('?');
        if (idx == -1) {
            return new ActionForward(forward + ((src == null) ? "" : "?src=" + src));
        } else {
            return new ActionForward(forward.substring(0, idx + 1) + ((src == null) ? "" : "src=" + src) + "&" + encodeQueryStr(forward.substring(idx + 1)));
        }
    }

    protected static String encodeQueryStr(String queryStr) {
        StringBuffer sb = new StringBuffer(1024);
        StringTokenizer st = new StringTokenizer(queryStr, "&");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            int idx = token.indexOf('=');
            sb.append(token.substring(0, idx + 1));
            //sb.append(URLEncoder.encode(token.substring(idx + 1)));
            sb.append(URLUTF8Encoder.encode(token.substring(idx + 1))); // Symbio modified 05/19/2005
            sb.append('&');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

//    IWebApplicationInfo getWebApplication(String webAppGroup, String webAppName) {
//        if (!initvars) {
//            init();
//        }
//        return ((IWebApplicationMgr) server.getManager("application-mgr")).getApplication(webAppGroup, webAppName);
//    }

    protected static void debugEntered(Class klass, String inMethodNameString) {
        if (sIsDebugEnabled) {
            sDebugPrintStream.println(klass.getName() + ": " + inMethodNameString + ": entered");
        }
    }

    protected static void debugLeaving(Class klass, String inMethodNameString,
                                       Object inMessageObject) {
        if (sIsDebugEnabled) {
            sDebugPrintStream.println(klass.getName() + ": " + inMethodNameString + ": leaving: " + inMessageObject);
        }
    }

    protected static void debugLeaving(Class klass, String inMethodNameString) {
        if (sIsDebugEnabled) {
            sDebugPrintStream.println(klass.getName() + ": " + inMethodNameString + ": leaving: <void>");
        }
    }

    protected void debugMessage(Class klass, String inMethodNameString, Object inMessageObject) {
        if (sIsDebugEnabled) {
            sDebugPrintStream.println(klass.getName() + ": " + inMethodNameString + ": " + inMessageObject);
        }
    }

    protected boolean isNull(String s) {
        return s == null || s.trim().length() == 0;
    }

    protected String getAction(HttpServletRequest req) {
        return req.getParameter("action");
    }

    /**
     * Gets the targets which is selected by the users on the TargetView page
     *
     * @param session REMIND
     * @return REMIND
     */
    public static List getSelectedTargets(HttpSession session) {
        if (session.getAttribute(SESSION_MULTITGBOOL) == null) {
            return (List) session.getAttribute(MAIN_PAGE_TARGET);
        } else {
            return (List) session.getAttribute(MAIN_PAGE_M_TARGETS);
        }
    }

    /**
     * Sets the appropriate license capabilities into the Application context
     * so that GUI and CLI interface can decide whether to hide/display
     * certian components.
     * Theoretically, this code should be in SubscriptionInitServlet, but as of
     * 6.0.1.  The license isn't checked by CMS until the Servlet is initialized
     * so as a workaround we are executing this method once per user login.
     */
    /*protected void setCapabilities() {
         boolean hasPatchCapability;

         IProperty license = (IProperty) context.getAttribute("com.marimba.servlet.context.license");
         if (license != null) {
             String capabilityString = license.getProperty("capabilities");
             if (capabilityString != null) {
                 hasPatchCapability = capabilityString.indexOf("patch") != -1;
                 if (hasPatchCapability) {
                     context.setAttribute(IAppConstants.CAPABILITIES_PATCH, "true");
                 }
                 if (capabilityString.indexOf("osprovisioning") != -1) {
                     context.setAttribute(IAppConstants.CAPABILITIES_OSPROVISIONING, "true");
                 }

             }
         }
     }*/


    // deprecated method
//    protected IUser initUser(HttpSession session) {
//        String dCurrentMethod = "initializeUser";
//        IUser user = (IUser) session.getAttribute(SESSION_SMUSER);
//        if ((user != null) && !user.isInitialized()) {
//            try {
//
//                debugMessage(klass, dCurrentMethod, "InitAppAction : finish initializing LDAP connections " + new Date(System.currentTimeMillis()));
//
//                user.initialize();
//                MergeAllSub.check(user.getSubConn(), GUIUtils.getMain(servlet).getSubBase());
//                MergeAllSub.checkAccessToken(user.getSubConn(), GUIUtils.getMain(servlet).getSubBase());
//                session.setAttribute(SESSION_PAGINGTYPE, LDAPPagedSearch.getType(user.getBrowseConn()));
//
//                debugMessage(klass, dCurrentMethod, "InitAppAction : finish initializing LDAP connections " + new Date(System.currentTimeMillis()));
//
//            } catch (CriticalException ce) {
//                WebAppUtils.saveInitException(session, ce);
//            } catch (SystemException syEx) {
//                WebAppUtils.saveInitException(session, new CriticalException(syEx, syEx.getKey()));
//            } catch (Exception se) {
//                WebAppUtils.saveInitException(session, new CriticalException(se, SYSTEM_INIT_FAIL));
//            }
//        }
//        return user;
    //    }
    protected IUser initUser(HttpSession session, SubscriptionMain main) {
        String dCurrentMethod = "initializeUser";
        IUser user = (IUser) session.getAttribute(SESSION_SMUSER);
        if ((user != null) && !user.isInitialized()) {
            try {

                debugMessage(klass, dCurrentMethod, "InitAppAction : finish initializing LDAP connections " + new Date(System.currentTimeMillis()));
                main.initLDAP();
                user.initialize();
                Map<String, String> LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(main.getDirType());
                MergeAllSub.check(user.getSubConn(), main.getSubBase(), main.getLDAPVarsMap());
                MergeAllSub.checkAccessToken(user.getSubConn(), main.getSubBase(), LDAPVarsMap);
                session.setAttribute(SESSION_PAGINGTYPE, LDAPPagedSearch.getType(user.getBrowseConn()));

                debugMessage(klass, dCurrentMethod, "InitAppAction : finish initializing LDAP connections " + new Date(System.currentTimeMillis()));

            } catch (CriticalException ce) {
                WebAppUtils.saveInitException(session, ce);
            } catch (SystemException syEx) {
                WebAppUtils.saveInitException(session, new CriticalException(syEx, syEx.getKey()));
            } catch (Exception se) {
                WebAppUtils.saveInitException(session, new CriticalException(se, SYSTEM_INIT_FAIL));
            }
        }
        return user;
    }
    protected IUser initUser(IUser user) throws SystemException {
        String dCurrentMethod = "initializeUser";
        if ((user != null) && !user.isInitialized()) {
            debugMessage(klass, dCurrentMethod, "InitAppAction : finish initializing LDAP connections " + new Date(System.currentTimeMillis()));
            user.initialize();
            //session.setAttribute(SESSION_PAGINGTYPE, LDAPPagedSearch.getType(user.getBrowseConn()));
            debugMessage(klass, dCurrentMethod, "InitAppAction : finish initializing LDAP connections " + new Date(System.currentTimeMillis()));
        }
        return user;
    }

    /**
     * @return value for the specified key without appending the priority assigned
     * Ex : If marimba.reboot.never property is assigned a value true with priority '2',i.e., marimba.reboot.never=true,2
     * then this method will return true
     */
    public String getTChPropValue(String value)
            throws SystemException {

        int index;

        if (value != null) {
            index = value.indexOf(PROP_DELIM);
            if (index == -1) {
                return value;
            } else {
                return value.substring(0, value.lastIndexOf(PROP_DELIM));
            }
        } else {
            return value;
        }
    }
    /**
     * @return priority value for the specified key
     * Ex : If marimba.reboot.never property is assigned a value true with priority '2',i.e., marimba.reboot.never=true,2
     * it will return the priority value 2, This is for appending the priority text field.
     */
    public String getPriorityValue( String probandvalue ){

        int indexVal;

        if( null != probandvalue ) {
            indexVal = probandvalue.lastIndexOf( PROP_DELIM );
            if( indexVal != -1 ) {
                probandvalue = probandvalue.substring( indexVal + 1, probandvalue.length() );
            }
            else {
                probandvalue = "";
            }
        }
        else {
            probandvalue = NOTAVBLE ;
        }
        return probandvalue;
    }

    /**
     * Method used to append priority value with property value
     * @param propValue value of a property
     * @param priority priority value
     * @return priority appeded with property value
     *
     */

    public String appendPriority(String propValue, String priority) {
        if (priority.isEmpty() || priority.equals(NOTAVBLE)) {
            return propValue;
        }

        String[] tmp_arr = propValue.split(",");
        if (tmp_arr.length == 1) {
            return propValue.trim() + "," + priority;
        }
        propValue = propValue.substring(0, propValue.lastIndexOf(","));
        try {
            Integer.parseInt(priority);
            propValue = propValue.trim() + "," + priority;
        } catch (NumberFormatException nfe) {
            // Nothing needs to do here
        }
        return propValue;
    }

    public String getPropertyValue( String probandval ){

        int indexVal;

        if( null != probandval ) {
            indexVal = probandval.lastIndexOf( PROP_DELIM );
            if( indexVal != -1) {
                probandval = probandval.substring( 0, indexVal );
            }
        }
        else {
            probandval = "";
        }
        return probandval;
    }

    public String getPropertyValueExcludingPriority( String probandval ){

        int indexVal;

        if( null != probandval ) {
            indexVal = probandval.lastIndexOf( PROP_DELIM );
            if( indexVal != -1) {
                probandval = probandval.substring( 0, indexVal );
            }
        } else {
            probandval = "";
        }

        return probandval;
    }

    /** @param oldValue oldvalue
     *  @param newValue newvalue
     *
     * If oldValue is null return the newValue, if not null then check for the priority.
     * If there is any priority then append it with the newvalue
     *
     *  @return newValue with priority
     */

    public String stripOutExistingPriority(String oldValue, String newValue) {
        int index = -1;
        if (oldValue == null) {
            return newValue;
        }
        index = oldValue.lastIndexOf(PROP_DELIM);
        return (index != -1) ? newValue + oldValue.substring(index) : newValue;
    }

    public String unescapeGroupName(String groupName) {
        if (groupName == null) {
            return null;
        }

        int i = 0;
        StringBuffer buf = new StringBuffer();

        while (i < groupName.length()) {
            char c = groupName.charAt(i++);

            if (c != '\\') {
                buf.append(c);
            } else {
                if (i < groupName.length()) {
                    buf.append(groupName.charAt(i++));
                }
            }
        }

        return buf.toString();
    }

    protected boolean isChannel(Vector<PropsBean> channelsList, String channelUrl) {
        for (PropsBean prop : channelsList) {
            if (channelUrl.equals(prop.getProperty("url")) && "channel".equals(prop.getProperty("type"))) {
                return true;
            }
        }

        return false;
    }

    protected boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public void sendJSONResponse(HttpServletResponse response, JSONObject jsonObject)throws Exception {
        PrintWriter out = response.getWriter();
        out.println(jsonObject.toString());
        out.flush();
    }

}