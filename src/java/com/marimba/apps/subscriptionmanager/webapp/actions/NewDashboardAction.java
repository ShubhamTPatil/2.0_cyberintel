// Copyright 2019-2022, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.webapp.util.defensight.AnomalyUtil;
import com.marimba.intf.msf.*;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscriptionmanager.util.Utils;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.intf.msf.ITenant;
import com.marimba.intf.util.IConfig;
import com.marimba.intf.util.IDirectory;
import com.marimba.webapps.intf.IWebAppsConstants;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Initializes new dashboard objects when user logged-in or relogin after time out.
   we execute this action when NewDashboardApp.do is called.
 *
 * @author Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 */
public class NewDashboardAction extends AbstractAction implements IWebAppConstants, IErrorConstants {

    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        return new InitTask(mapping, form, request, response);
    }

    protected class InitTask extends SubscriptionDelayedTask {

        InitTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
            super(mapping, form, request, response);
        }

        public void execute() {
            String reqTenantName;
            SubscriptionMain main = null;

            initApp(request);

            try {
                reqTenantName = TenantHelper.getTenantName(request);

                if(null != reqTenantName && "admin".equalsIgnoreCase(reqTenantName)) {
                    forward = mapping.findForward("initerror");
                    return;
                }
                ITenant reqTenant = tenantMgr.getTenant(reqTenantName);
                if(null != reqTenant && hasLocalDBConfig(reqTenant)) {
                    forward = mapping.findForward("initerror");
                    return;
                }
                main = TenantHelper.getTenantSubMain(context, request.getSession(), reqTenantName);

                //Starting the thread to make the connection with Cosmos DB for AnomalyReport
                IDirectory features = main.getFeatures();
                IConfig tunerConfig = (IConfig) features.getChild("tunerConfig");
                SubscriptionMain finalMain = main;
                Thread cosmosConnectionThread = new Thread(AnomalyUtil.getCosmosConectionRunnable(tunerConfig));
                cosmosConnectionThread.setName("cosmosConnectionThread");
                cosmosConnectionThread.start();
                //END

                String loggedInUserRole = ((IUserPrincipal)request.getUserPrincipal()).getUserRole();
               	System.out.println("Logged in user role :" + loggedInUserRole);
            } catch(Exception ex) {

            }

            String dCurrentMethod = "execute()";
            debugEntered(klass, dCurrentMethod);

            IUser user = initUser(session, main);
            session.removeAttribute(MAIN_PAGE_TARGET);
            Object bean = session.getAttribute(IWebAppsConstants.INITERROR_KEY);

            if ((bean != null) && bean instanceof Exception) {
                //remove initerror from the session because it has served its purpose
                if (DEBUG) {
                    System.out.println("InitAppAction: critical exception found");
                }
                forward = mapping.findForward("initerror");

                return;
            }
            if (session.getAttribute("context") == null || !((String) session.getAttribute("context")).equals("targetDetailsAddMulti")) {
                session.setAttribute("context", "targetDetailsAdd");
            }

            // Fowarding logic

            // if its a ccm page allow forwading to that page
            String fowardUrl = (String) session.getAttribute(ATTR_LOGIN_SAVED_PAGE);
            if ( fowardUrl == null) {
                fowardUrl = (String) session.getAttribute(ATTR_LOGIN_RETURN_PAGE);
            }

            Target target = new Target("All Endpoints", "all", "all");
    		session.setAttribute(SELECTED_TARGET_ID, "all");
			session.setAttribute(SELECTED_TARGET_NAME, "All Endpoints");
			session.setAttribute(SELECTED_TARGET_TYPE, "all");
			session.setAttribute( "target", target );

            if(null != main && null != main.getConfig() && Utils.isPrimaryAdmin(request)) {
                IConfig policyConfig = main.getConfig();
                if (null == policyConfig.getProperty("subscriptionmanager.publishurl")) {
                    forward = mapping.findForward("plugin");
                    return;
                }
                if(null != policyConfig.getProperty(ENABLE_POLICY_START_LOCATION_ENABLED)) {
                    boolean startLocationEnabled = "true".equals(policyConfig.getProperty(ENABLE_POLICY_START_LOCATION_ENABLED).toString());
                    if(startLocationEnabled) {
                        String loadingLocation = policyConfig.getProperty(ENABLE_POLICY_START_LOCATION_PATH);
                        System.out.println("Policy Manager start location :" + loadingLocation);
                        if(null != loadingLocation) {
                            setSessionVar("policystartlocation", loadingLocation, false);
                            setSessionVar("policyfirsttime", "true", false);
                        } else {
                            setSessionVar("policystartlocation", null, true);
                            setSessionVar("policyfirsttime", null, true);
                        }
                    } else {
                        setSessionVar("policystartlocation", null, true);
                        setSessionVar("policyfirsttime", null, true);
                    }
                }
            }

            String securityStartLocation = "all";
            setSessionVar("securitystartlocation", securityStartLocation, false);
            setSessionVar("securityfirsttime", "true", false);
            setSessionVar("allendpointdashboard", "true", false);
            if (fowardUrl != null) {
                if (fowardUrl.indexOf("ccm") > 0) {
                    int relative = fowardUrl.indexOf(SUBSCRIPTION_PATH);
                    if (relative > -1) {
                        fowardUrl = fowardUrl.substring(relative + 3);
                    }
                    forward = new ActionForward(fowardUrl, true);

                    return;
                }
            }

            // foward to namespace page if property is not set
            if (user != null) {
                if (!"true".equals(user.getProperty(IUser.PROP_NAMESPACE_SKIPSETTING))  && !Utils.isOperator(request)) {
                    session.setAttribute(IUser.PROP_NAMESPACE_SKIPSETTING, "false");
                    forward = mapping.findForward("namespaceaction");
                } else {
                    forward = mapping.findForward("newdashboard");
                }

                return;
            } else {
                forward = mapping.findForward("login");
            }

            debugLeaving(klass, dCurrentMethod);
        }

        public String getWaitMessage() {
            return getString("page.global.processing");
        }

        private void setSessionVar(String propName, String PropValue, boolean isDelete) {
            if(isDelete) {
                if(null != session.getAttribute(propName)) {
                    session.setAttribute(propName, null);
                }
            } else {
                session.setAttribute(propName, PropValue);
            }
        }
    }
}

