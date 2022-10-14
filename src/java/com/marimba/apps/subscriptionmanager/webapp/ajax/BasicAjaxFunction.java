package com.marimba.apps.subscriptionmanager.webapp.ajax;

// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

import com.bmc.web.ajax.AjaxCall;
import com.bmc.web.ajax.AjaxException;
import com.bmc.web.ajax.AjaxFunction;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.intf.msf.*;
import com.marimba.intf.msf.AppManagerConstants;
import com.marimba.intf.util.IConfig;
import com.marimba.tools.util.DebugFlag;
import com.marimba.webapps.intf.IWebAppsConstants;
import org.apache.struts.Globals;
import org.apache.struts.util.MessageResources;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.util.Locale;

/**
 * Base-class for AjaxFunctions
 *
 * @author	Selvaraj Jegatheesan
 * @author	Tamilselvan Teivasekamani
 *
 * @version $Revision$, $Date$
 */

public abstract class BasicAjaxFunction extends AjaxFunction implements AppManagerConstants, IWebAppConstants, IWebAppsConstants, ISubscriptionConstants {

    protected IConfig config;
    protected JSONObject jsonObj;
    protected HttpSession session;
    protected ServletContext ctx;
    protected SubscriptionMain main;
    protected ITenantManager tenantMgr;
    protected ITenant tenant;
    protected MessageResources resources;

    protected boolean DEBUG = DebugFlag.getDebug("SUB/AJAX") >= 1;
    protected boolean SUB_DEBUG = DebugFlag.getDebug("SUB") >= 1;

    public final void init(ServletContext context) {
        super.init(context);
        this.ctx = context;
        //this.main = (SubscriptionMain) context.getAttribute(IWebAppsConstants.APP_MAIN);
        this.resources = (MessageResources) context.getAttribute(Globals.MESSAGES_KEY);
        //this.config = main.getServerConfig();
        //this.features = (IDirectory)context.getAttribute(MGR_FEATURES);
        //this.features = (IDirectory)main.getFeatures();
        //this.server = (IServer)features.getChild(MGR_SERVER);
        //this.dbmgr = (IDatabaseMgr)server.getManager(MGR_DATABASE);
    }

    public JSONObject invoke(AjaxCall call) throws AjaxException, JSONException {
        try {
            // loading tenant Subscription Main
            IUserPrincipal user = (IUserPrincipal) call.getUserPrincipal();
            String tenantName = user.getTenantName();
            System.out.println("BasicAjaxFunction : Current tenant name " + tenantName);
            this.main = TenantHelper.getTenantSubMain(this.ctx, call.getSession(), tenantName);
            this.tenantMgr = main.getTenantManager();
            this.tenant = main.getTenant();
            this.config = main.getServerConfig();
            return doInvoke(call);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AjaxException(e.getMessage());
        }
    }

    protected abstract JSONObject doInvoke(AjaxCall call) throws AjaxException, JSONException;

    public IConfig getServerConfig() {
        return this.config;
    }

    protected String getMessage(Locale locale, String key) {
        String val = null;
        if (resources != null) {
            val = resources.getMessage(locale, key);
        }
        return val;
    }

    protected String getMessage(Locale locale, String key, Object[] args) {
        String val = null;
        if (resources != null) {
            val = resources.getMessage(locale, key, args);
        }
        return val;
    }

    public void log(int id, int severity, Object message, String target) {
        IUser user = (IUser) session.getAttribute(SESSION_SMUSER);
        String loggedInUser = user.getName();
        main.log(id, severity, "vDesk", loggedInUser, message, target);
    }

    protected void debug(String message) {
        if (DEBUG) System.out.println("SPM-AJAX : " + message);
    }
}