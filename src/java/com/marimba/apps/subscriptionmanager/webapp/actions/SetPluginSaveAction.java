// Copyright 1997-2009, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.securitymgr.utils.ConfigUtil;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.beans.ChangeRequestBean;
import com.marimba.apps.subscriptionmanager.webapp.PublishThread;
import com.marimba.apps.subscriptionmanager.webapp.forms.SetPluginForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.intf.msf.ITenant;
import com.marimba.intf.msf.IUserPrincipal;
import com.marimba.intf.db.IConnectionPool;
import com.marimba.intf.msf.IDatabaseMgr;
import com.marimba.intf.util.IConfig;
import com.marimba.tools.config.ConfigPrefix;
import com.marimba.tools.config.ConfigProps;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.tools.util.Password;
import com.marimba.tools.util.Props;
import com.marimba.webapps.intf.IMapProperty;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;


/**
 * This action will save the plugin properties to a config file in the data directory.  These properties are then used by the publish code to set the
 * properties in the plugin.
 *
 * @author Angela Saval
 * @version 1.27,02/07/2003
 */
public final class SetPluginSaveAction extends AbstractAction {
    String PASSWORD_STR = "*****";
    /**
     * REMIND
     *
     * @param mapping REMIND
     * @param form REMIND
     * @param request REMIND
     * @param response REMIND
     *
     * @return REMIND
     *
     * @throws IOException REMIND
     * @throws ServletException REMIND
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        init(request);
        Locale locale = request.getLocale();
    	ServletContext context = request.getSession().getServletContext();
        SubscriptionMain smmain = TenantHelper.getTenantSubMain(context, request);
        Props userPluginProps = new Props();
        IMapProperty formbean = (IMapProperty) form;
        SetPluginForm userForm = (SetPluginForm) form;
        String parameter = getPropertyValue(formbean,"publishurl");

        debug("execute(), SetPluginSaveAction: publishurl = " + parameter);

        userPluginProps.setProperty("subscriptionmanager.publishurl", (parameter != null) ? parameter : null);
        parameter = getPropertyValue(formbean, "ldaphost");
        userPluginProps.setProperty("subscriptionmanager.ldaphost", (parameter != null) ? parameter : null);
        parameter = getPropertyValue(formbean,"noautodiscover");
        userPluginProps.setProperty("subscriptionmanager.noautodiscover", (parameter != null) ? parameter : null);
        parameter = getPropertyValue(formbean, "basedn");
        userPluginProps.setProperty("subscriptionmanager.basedn", (parameter != null) ? parameter : null);
        parameter = getPropertyValue(formbean, "binddn");
        userPluginProps.setProperty("subscriptionmanager.binddn", (parameter != null) ? parameter : null);
        parameter = getPropertyValue(formbean, "bindpasswd");

        String changedPwd = getPropertyValue(formbean, "changedPassword");

        String pwdvalue = (parameter != null) ? parameter : null;

        if ("true".equals(changedPwd)) {
            String[] chpwd = new String[1];
            chpwd [0] = "false";
            formbean.setValue("changedPassword", chpwd);
        }

        userPluginProps.setProperty("subscriptionmanager.bindpasswd", pwdvalue);

        parameter = getPropertyValue(formbean, "poolsize");
        userPluginProps.setProperty("subscriptionmanager.poolsize", (parameter != null) ? parameter : null);

        parameter = getPropertyValue(formbean, "lastgoodhostexptime");
        userPluginProps.setProperty("subscriptionmanager.lastgoodhostexptime", (parameter != null) ? parameter : null);

        parameter = getPropertyValue(formbean, "pallowprov");
        userPluginProps.setProperty("subscriptionmanager.pallowprov", (parameter != null) ? parameter : "false");

        parameter = getPropertyValue(formbean, "usessl");
        userPluginProps.setProperty("subscriptionmanager.usessl", (parameter != null) ? parameter : "false");

        // Default Authentication type as Simple
        parameter = getPropertyValue(formbean, "authmethod");
        userPluginProps.setProperty("subscriptionmanager.authmethod", (parameter != null) ? parameter : LDAPConstants.AUTHMETHOD_SIMPLE);

        parameter = getPropertyValue(formbean, "pluginStatus");
        userPluginProps.setProperty("subscriptionmanager.pluginStatus",parameter );

        String dbType = getPropertyValue(formbean, "db.type");
        String hostname = getPropertyValue(formbean, "db.hostname");
        String port = getPropertyValue(formbean, "db.port");
        String dbName = getPropertyValue(formbean, "db.name");
        userPluginProps.setProperty("subscriptionmanager.db.hostname", hostname);
        userPluginProps.setProperty("subscriptionmanager.db.port", port);
        userPluginProps.setProperty("subscriptionmanager.db.name", dbName);
        boolean isNetserviceEnable = false;

        userPluginProps.setProperty("subscriptionmanager.db.type", dbType);

        String dbClass = ConfigUtil.getDRIVER_STRING(dbType);
        userPluginProps.setProperty("subscriptionmanager.db.class", dbClass);

        String dbUrl = ConfigUtil.getDRIVER_URL(dbType, hostname, port, dbName, false);
        userPluginProps.setProperty("subscriptionmanager.db.url", dbUrl);

        parameter = getPropertyValue(formbean, "db.username");
        userPluginProps.setProperty("subscriptionmanager.db.username",parameter );

        String dbPassword = getPropertyValue(formbean, "db.password");

        IConfig config = smmain.getConfig();
        if(null != dbPassword && !PASSWORD_STR.equals(dbPassword)) {
            dbPassword = Password.encode(dbPassword);
            userPluginProps.setProperty("subscriptionmanager.db.password",dbPassword);
        } else {
            String publishedDBPassword = smmain.getConfig().getProperty("subscriptionmanager.db.password");
            if (publishedDBPassword == null) {
                publishedDBPassword = getDBPassword(tenant);
            }
            userPluginProps.setProperty("subscriptionmanager.db.password", publishedDBPassword);
        }

        parameter = getPropertyValue(formbean, "db.thread.min");
        userPluginProps.setProperty("subscriptionmanager.db.thread.min", parameter);

        parameter = getPropertyValue(formbean, "db.thread.max");
        userPluginProps.setProperty("subscriptionmanager.db.thread.max", parameter);

        parameter = getPropertyValue(formbean, "repeaterInsert");
        userPluginProps.setProperty("subscriptionmanager.repeaterInsert", ("true".equals(parameter) ? "true" : "false"));

        parameter = getPropertyValue(formbean, "elasticurl");
        userPluginProps.setProperty("subscriptionmanager.elasticurl", parameter);

        parameter = getPropertyValue(formbean, "cveFiltersDir");
        userPluginProps.setProperty("subscriptionmanager.cveFiltersDir", parameter);

        //Obtain the user name and password used for publishing
        String publishUser = parameter = getPropertyValue(formbean, "publishurl.username");
        if(null != parameter) {
            userPluginProps.setProperty("subscriptionmanager.publishurl.username", parameter);
        } else {
            userPluginProps.setProperty("subscriptionmanager.publishurl.username",null);
        }

        String parameter2 = getPropertyValue(formbean, "publishurl.password");
        if(null != parameter2) {
        	parameter2 = Password.encode(parameter2);
            userPluginProps.setProperty("subscriptionmanager.publishurl.password",parameter2);
        } else {
            userPluginProps.setProperty("subscriptionmanager.publishurl.password",null);
        }
        String changedpublishPwd = getPropertyValue(formbean, "changedPublishPwd");

        String publishUserPWD = (parameter2 != null) ? parameter2 : null;
        String publishPassword = parameter2;
        if ("true".equals(changedpublishPwd)) {
            String[] chpwd = new String[1];
            chpwd [0] = "false";
            formbean.setValue("changedPublishPwd", chpwd);
        }

        //Obtain the user name and password used for subscribing
        parameter = getPropertyValue(formbean, "publishurl.subscribeuser");

        if(null != parameter) {
            userPluginProps.setProperty("subscriptionmanager.publishurl.subscribeuser", parameter);
        } else {
            userPluginProps.setProperty("subscriptionmanager.publishurl.subscribeuser",null);
        }

        parameter2 = getPropertyValue(formbean, "publishurl.subscribepassword");
        if(null != parameter2) {
            parameter2 = Password.encode(parameter2);
            userPluginProps.setProperty("subscriptionmanager.publishurl.subscribepassword",parameter2);
        } else {
            userPluginProps.setProperty("subscriptionmanager.publishurl.subscribepassword",null);
        }
        String changedSubscribePwd = getPropertyValue(formbean, "changedSubscribePwd");

        String subscribeUserPWD = (parameter2 != null) ? parameter2 : null;

        if ("true".equals(changedSubscribePwd)) {
            String[] chpwd = new String[1];
            chpwd [0] = "false";
            formbean.setValue("changedSubscribePwd", chpwd);
        }

        // security info channel
        parameter = getPropertyValue(formbean,"securityinfo.url");
        userPluginProps.setProperty("subscriptionmanager.securityinfo.url", (parameter != null) ? parameter : null);

        parameter = getPropertyValue(formbean,"securityinfo.subscribeuser");
        userPluginProps.setProperty("subscriptionmanager.securityinfo.subscribeuser", (parameter != null) ? parameter : null);

        String infoSubscribePassword = getPropertyValue(formbean, "securityinfo.subscribepassword");

        if(null != infoSubscribePassword && !PASSWORD_STR.equals(infoSubscribePassword)) {
        	infoSubscribePassword = Password.encode(infoSubscribePassword);
            userPluginProps.setProperty("subscriptionmanager.securityinfo.subscribepassword",infoSubscribePassword);
        } else {
            userPluginProps.setProperty("subscriptionmanager.securityinfo.subscribepassword", smmain.getConfig().getProperty("subscriptionmanager.securityinfo.subscribepassword"));
        }

        // custom scanner channel
        parameter = getPropertyValue(formbean,"customscanner.url");
        userPluginProps.setProperty("subscriptionmanager.customscanner.url", (parameter != null) ? parameter : null);

        parameter = getPropertyValue(formbean,"customscanner.subscribeuser");
        userPluginProps.setProperty("subscriptionmanager.customscanner.subscribeuser", (parameter != null) ? parameter : null);

        String scannerSubscribePassword = getPropertyValue(formbean, "customscanner.subscribepassword");

        if(null != scannerSubscribePassword && !PASSWORD_STR.equals(scannerSubscribePassword)) {
        	scannerSubscribePassword = Password.encode(scannerSubscribePassword);
            userPluginProps.setProperty("subscriptionmanager.customscanner.subscribepassword",scannerSubscribePassword);
        } else {
            userPluginProps.setProperty("subscriptionmanager.customscanner.subscribepassword", smmain.getConfig().getProperty("subscriptionmanager.customscanner.subscribepassword"));
        }

        //Set the current time that the plugin was stored
        userPluginProps.setProperty("subscriptionmanager.lastpublishedtime", Long.toString(System.currentTimeMillis()));

        //REMIND: this is done because for some reason logic:equal is not working
        //for the vendor attribute.  it does not handle String[] array. but for some
        //reason it works for the ldap.jsp in cms
        String paramstring = getPropertyValue(formbean, "vendor");
        userPluginProps.setProperty("subscriptionmanager.vendor", (paramstring != null) ? paramstring : LDAPConstants.VENDOR_NS);
        // Settings the Empirum properties to plugin

        try {
	        String noAutoDiscovery = getPropertyValue(formbean,"noautodiscover");
	        System.out.println("No auto discover : " + noAutoDiscovery);

	           if(null != noAutoDiscovery & noAutoDiscovery.equalsIgnoreCase("false")) {
	        	ITenant tenant = smmain.getTenant();
	        	IConfig tenantConfig = tenant.getConfig();
	        	System.out.println("AD dmaon :" + tenantConfig.getProperty(LDAPConstants.PROP_AD_DOMAIN));
	        	userPluginProps.setProperty("subscriptionmanager.admanagementdomain", tenantConfig.getProperty(LDAPConstants.PROP_AD_DOMAIN));
	        	userPluginProps.setProperty("subscriptionmanager.srvdnsserver", tenantConfig.getProperty(LDAPConstants.PROP_AD_DNSSERVER));
	        	userPluginProps.setProperty("subscriptionmanager.connectiontimeout", tenantConfig.getProperty(LDAPConstants.PROP_CONN_TIMEOUT));
	        	userPluginProps.setProperty("subscriptionmanager.querytimeout", tenantConfig.getProperty(LDAPConstants.PROP_QUERY_TIMEOUT));
	        	userPluginProps.setProperty("subscriptionmanager.preferreddcs", tenantConfig.getProperty(LDAPConstants.PROP_AD_PREFERREDDCS));
	        	userPluginProps.setProperty("subscriptionmanager.adsite", tenantConfig.getProperty(LDAPConstants.PROP_AD_SITE));
	        } else {
	        	userPluginProps.setProperty("subscriptionmanager.admanagementdomain", null);
	        	userPluginProps.setProperty("subscriptionmanager.srvdnsserver", null);
	        	userPluginProps.setProperty("subscriptionmanager.connectiontimeout", null);
	        	userPluginProps.setProperty("subscriptionmanager.querytimeout", null);
	        	userPluginProps.setProperty("subscriptionmanager.preferreddcs", null);
	        	userPluginProps.setProperty("subscriptionmanager.adsite", null);
	        }
        } catch(Exception ec) {

        }

        // REMIND instead of clearing out the session variable just use the entries directly all the steps above will not be  needed
        HttpSession session = request.getSession();
        session.removeAttribute("preview_values");

        String fwd = "success";
       // debug("execute(), tenant.isPolicyPluginEnabledForChangeRequest() - " + false);
        if (true) {
            //This session variable is set to indicate that this is the first time visiting the waitfor action.  This is so that
            //if the waitingforComplition.jsp is revisited after success, or failure, we will know it is due to the back button
            ConfigProps configProps = new ConfigProps(userPluginProps, null);
            session.setAttribute(SESSION_WAITFORMARKER, "true");
            PublishThread pb = new PublishThread(session, smmain, configProps, new String[]{publishUser}, new String[]{publishPassword});

            new Thread(pb).start();
        } else {
            session.setAttribute(SESSION_WAITFORMARKER, "true");
            Collection<ChangeRequestBean> changeRequestBeans = (Collection<ChangeRequestBean>) session.getAttribute("change_request_beans");

            //debug("execute(), changeRequestBeans - " + changeRequestBeans);
//            if (changeRequestBeans != null && changeRequestBeans.size() > 0) {
//                IChangeRequestManagement changeReqMgmt = tenant.getChangeRequestManagement();
//
//                if (changeReqMgmt != null) {
//                    String publishUrl = (String) formbean.getValue("publishurl");
//
//                    try {
//                        HashMap<String, String> requestDetails = new HashMap<String, String>();
//                        StringBuilder addedProps = new StringBuilder();
//                        StringBuilder modifiedProps = new StringBuilder();
//                        StringBuilder removedProps = new StringBuilder();
//                        int addedCount = 0, modifiedCount = 0, deletedCount = 0;
//
//                        handleDuplicateChangeRequests(publishUrl, request);
//                        int id = changeReqMgmt.createRequest(tenant.getName(), IChangeRequestManagement.POLICY_PLUGIN_CHANGE_REQUEST, publishUrl);
//                        debug("execute(), id - " + id);
//                        changeReqMgmt.updateTicketStatus(id, "Opened");
//                        boolean updateStatus = "Opened".equals(changeReqMgmt.getChangeRequestAsMapById(id).get("requestStatus"));
//                        debug("execute(), updateTicketStatus 'Opened' - " + updateStatus + " for marimbaID - " + id);
//
//                        Vector<HashMap<String, String>> changeRequests = changeReqMgmt.getChangeRequestsAsMap(tenant.getName(), IChangeRequestManagement.POLICY_PLUGIN_CHANGE_REQUEST, publishUrl);
//                        HashMap<String, String> changeRequest = changeRequests.get(changeRequests.size() - 1);
//                        String changeRequestStatus = changeRequest.get("requestStatus");
//                        if ((changeRequestStatus != null) && (changeRequestStatus.startsWith("Closed"))) {
//                            id = changeReqMgmt.createRequest(tenant.getName(), IChangeRequestManagement.POLICY_PLUGIN_CHANGE_REQUEST, publishUrl);
//                            debug("commit(), id - " + id);
//                            changeReqMgmt.updateTicketStatus(id, "Opened");
//                            updateStatus = "Opened".equals(changeReqMgmt.getChangeRequestAsMapById(id).get("requestStatus"));
//                            debug("commit(), updateTicketStatus 'Opened' - " + updateStatus + " for marimbaID - " + id);
//                        }
//
//                        for (ChangeRequestBean bean : changeRequestBeans) {
//                            if (IChangeRequestManagement.CHANGE_REQUEST_CREATE.equals(bean.getOperationType())) {
//                                changeReqMgmt.addNewProperty(id, bean.getKey(), bean.getNewValue());
//                                addedProps.append("Property").append(++addedCount).append(" [");
//                                addedProps.append(bean.getKey()).append("=").append(bean.getNewValue());
//                                addedProps.append(" ], ");
//                            } else if (IChangeRequestManagement.CHANGE_REQUEST_DELETE.equals(bean.getOperationType())) {
//                                changeReqMgmt.removeExistingProperty(id, bean.getKey());
//                                removedProps.append("Property").append(++deletedCount).append(" [");
//                                removedProps.append(bean.getKey()).append("=").append(bean.getNewValue());
//                                removedProps.append(" ], ");
//                            } else {
//                                changeReqMgmt.modifyExistingProperty(id, bean.getKey(), bean.getOldValue(), bean.getNewValue());
//                                modifiedProps.append("Property").append(++modifiedCount).append(" [");
//                                modifiedProps.append(bean.getKey()).append("=").append(bean.getNewValue()).append("(").append(bean.getOldValue()).append(")");
//                                modifiedProps.append(" ], ");
//                            }
//                        }
//
//                        requestDetails.put("subType", IChangeRequestManagement.POLICY_PLUGIN_CHANGE_REQUEST);
//                        requestDetails.put("target", publishUrl);
//                        requestDetails.put("tenant", tenant.getName());
//                        requestDetails.put("user", getUserName(request));
//                        requestDetails.put("marimbaId", "" + id);
//                        requestDetails.put("addedProp", addedProps.toString());
//                        requestDetails.put("deletedProp", removedProps.toString());
//                        requestDetails.put("modifiedProp", modifiedProps.toString());
//                        debug("execute(), requestDetails1 - " + requestDetails);
//                        for (Map.Entry<String, String> entry : requestDetails.entrySet()) {
//                            try {
//                                requestDetails.put(entry.getKey(), entry.getValue().replaceAll(" ", "<space>"));
//                            } catch (Throwable t) {
//                                //ignore...
//                            }
//                        }
//                        debug("execute(), requestDetails2 - " + requestDetails);
//
////                        IServiceAutomation serviceAutomation = tenantMgr.getServiceAutomation();
////                        String requestId = serviceAutomation.handleRequest(serviceAutomation.CMS_CHANGE_SUBMIT_REQUEST, requestDetails);
//                        String requestId = "";
//                        String[] serviceAutomationIds = requestId.split(";");
//                        changeReqMgmt.updateTicketId(id, serviceAutomationIds[0], serviceAutomationIds[1]);
//
//                        changeReqMgmt.updateTicketStatus(id, "Pending");
//                        updateStatus = "Pending".equals(changeReqMgmt.getChangeRequestAsMapById(id).get("requestStatus"));
//                        debug("execute(), updateTicketStatus 'Pending' - " + updateStatus + " for marimbaID - " + id);
//
//                        if (getTenantTunerConfig() != null) {
//                            debug("execute(), getTenantTunerConfig().getProperty(\"marimba.serviceautomation.cms.configchange.integration\") - " + getTenantTunerConfig().getProperty("marimba.serviceautomation.cms.configchange.integration"));
//                            if ("servicenow".equals(getTenantTunerConfig().getProperty("marimba.serviceautomation.cms.configchange.integration"))) {
//                                session.setAttribute(SESSION_CHANGE_REQUEST_MSG, getString(locale, "page.config.apply.success.servicenow", new String[]{serviceAutomationIds[1], publishUrl}));
//                            }
//                            if ("remedyforce".equals(getTenantTunerConfig().getProperty("marimba.serviceautomation.cms.configchange.integration"))) {
//                                session.setAttribute(SESSION_CHANGE_REQUEST_MSG, getString(locale, "page.config.apply.success.remedyforce", new String[]{serviceAutomationIds[1], publishUrl}));
//                            }
//                        }
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                        if (session != null) {
//                            session.setAttribute(SESSION_LONGTASK, "failure");
//                            session.setAttribute(SESSION_LONGTASKEXC, ex);
//                        }
//                    }
//                }
//            }
            if (session != null) {
                session.setAttribute(SESSION_LONGTASK, "success");
            }
        }

        GUIUtils.log(servlet, request, LOG_SET_PLUGIN, userPluginProps.getProperty("subscriptionmanager.publishurl"));

        smmain.logAuditInfo(LOG_AUDIT_SET_PLUGIN, LOG_AUDIT, "Policy Manager", userPluginProps.getProperty("subscriptionmanager.publishurl"), request, SAVE_PLUGIN_PROPS);

        String pluginStatus = getPropertyValue(formbean, "pluginStatus");
		System.out.println("Retrieved plugin status : " + pluginStatus);
		request.getSession().setAttribute("pluginStatus", pluginStatus);
        
        return mapping.findForward(fwd);
    }

    public String getPropertyValue(IMapProperty props,String property){
         if((props.getValue(property)) instanceof String[]){
             return ((String[]) props.getValue(property)) [0];
         }
         return (String)props.getValue(property);
     }

//    void handleDuplicateChangeRequests(String publishUrl, HttpServletRequest request) {
//        try {
//            if (tenant.isInventoryPluginEnabledForChangeRequest()) {
//                IChangeRequestManagement changeReqMgmt = tenant.getChangeRequestManagement();
//                if (changeReqMgmt != null) {
//                    Vector<HashMap<String, String>> changeRequests = changeReqMgmt.getChangeRequestsAsMap(tenant.getName(), IChangeRequestManagement.POLICY_PLUGIN_CHANGE_REQUEST, publishUrl);
//                    for (int i = 0; i < changeRequests.size(); i++) {
//                        HashMap<String, String> changeRequest = changeRequests.get(i);
//                        String changeRequestStatus = changeRequest.get("requestStatus");
//                        if ((changeRequestStatus != null) && (changeRequestStatus.startsWith("Closed"))) {
//                            //ignore...
//                        } else {
//                            String changeRequestIdStr = changeRequest.get("id");
//                            int changeRequestId = -1;
//                            try {
//                                changeRequestId = Integer.parseInt(changeRequestIdStr);
//                            } catch (Throwable t) {
//                                continue;
//                            }
//                            changeReqMgmt.updateTicketStatus(changeRequestId, "Closed Skipped");
//                            boolean updateStatus = "Closed Skipped".equals(changeReqMgmt.getChangeRequestAsMapById(changeRequestId).get("requestStatus"));
//                            debug("handleDuplicateChangeRequests(), updateTicketStatus 'Closed Skipped' - " + updateStatus + " for marimbaID - " + changeRequestId);
//
//                            HashMap<String, String> requestDetails = new HashMap<String, String>();
//                            requestDetails.put("sysId", changeRequest.get("primaryChangeRequestId"));
//                            requestDetails.put("number", changeRequest.get("secondaryChangeRequestId"));
//                            requestDetails.put("user", getUserName(request));
//                            requestDetails.put("comments", "Closed due to new request");
//                            requestDetails.put("approval", "rejected");
//                            requestDetails.put("state", "closed");
//                            debug("handleDuplicateChangeRequests(), requestDetails1 - " + requestDetails);
//                            for (Map.Entry<String, String> entry : requestDetails.entrySet()) {
//                                try {
//                                    requestDetails.put(entry.getKey(), entry.getValue().replaceAll(" ", "<space>"));
//                                } catch (Throwable t) {
//                                    //ignore...
//                                }
//                            }
//                            debug("handleDuplicateChangeRequests(), requestDetails2 - " + requestDetails);
//
////                            IServiceAutomation serviceAutomation = tenantMgr.getServiceAutomation();
////                            String requestId = serviceAutomation.handleRequest(serviceAutomation.CMS_CHANGE_UPDATE_REQUEST, requestDetails);
////                            debug("handleDuplicateChangeRequests(), ServiceAutomation Request Id - " + requestId);
//                        }
//                    }
//                }
//            }
//        } catch (Throwable t) {
//            if (DEBUG) {
//                t.printStackTrace();
//            }
//        }
//    }

    public String getUserName(HttpServletRequest request) {
        IUserPrincipal userPrincipal = (IUserPrincipal) request.getUserPrincipal();
        String userName = userPrincipal.getName();
        return userName;
    }

    public IConfig getTenantTunerConfig() {
//        if (null != tenant) {
//            return tenant.getTunerConfig();
//        }
        return null;
    }

    public void debug(String s) {
        if (DEBUG) {
            System.out.println("vDesk, SetPluginSaveAction.java => " + s);
        }
    }

    private IConfig getDBProps(IConfig config, String dbName) {
        int index = 0;
        IConfig cfg = new ConfigPrefix("dbmgr.", config);
        // Find all database-configs
        java.util.Hashtable dbs = new java.util.Hashtable();
        String[] pairs = cfg.getPropertyPairs();
        for (int i = 0; i < pairs.length; i += 2) {
            int dot = pairs[i].indexOf(".");
            if (dot > 0) {
                String prefix = pairs[i].substring(0, dot);
                try {
                    int x = Integer.parseInt(prefix);
                    if (x > index) {
                        index = x;
                    }
                    dbs.put(prefix, prefix);
                } catch (NumberFormatException nfe) {
                    // ignore
                }
            }
        }

        for (java.util.Enumeration e = dbs.elements(); e.hasMoreElements(); ) {
            String prefix = (String)e.nextElement();
            IConfig dbCfg = new ConfigPrefix(prefix + ".", cfg);
            String name = dbCfg.getProperty("name");
            if (name != null && name.equals(dbName)) {
                return dbCfg;
            }
        }
        return null;
    }

    private String getDBPassword(ITenant tenant) {
        String writePoolName = tenant.getDbMgr().getActive(IDatabaseMgr.DEFAULT_WRITE);
        IConnectionPool connectionPool = tenant.getDbMgr().getPool(writePoolName, null);
        IConfig msfConfig = tenant.getConfig();
        IConfig dbWriteProps = getDBProps(msfConfig, writePoolName);
        return dbWriteProps.getProperty("db.connection.pwd");
    }
}
