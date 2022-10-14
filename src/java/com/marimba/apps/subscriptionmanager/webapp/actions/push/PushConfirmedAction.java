// Copyright 1997-2004, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions.push;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;

import java.util.*;
import java.sql.Time;
import java.text.DateFormat;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.intf.SubKnownException;
import com.marimba.apps.subscription.common.intf.IMainDataSourceContext;
import com.marimba.apps.subscription.common.objects.Target;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.webapp.util.push.DMHelper;
import com.marimba.apps.subscriptionmanager.webapp.util.push.DMDeploymentView;
import com.marimba.apps.subscriptionmanager.webapp.util.*;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;

import com.marimba.webapps.intf.*;

import com.marimba.tools.ldap.LDAPConnection;

import com.marimba.tools.ldap.LDAPName;
import com.marimba.tools.util.Props;
import com.marimba.intf.msf.IUserPrincipal;


import com.marimba.intf.util.IConfig;
import com.marimba.apps.sdm.intf.simplified.*;

/**
 * This action is used from the push preview page when user confirms the push/update of selected packages from a single select mode.
 *
 * @author Anantha Kasetty
 * @version $Revision$, $Date$
 */
public final class PushConfirmedAction
    extends AbstractAction  {
    final boolean DEBUG = true;
    SubscriptionMain main;
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
     * @throws java.io.IOException REMIND
     * @throws javax.servlet.ServletException REMIND
     * @throws com.marimba.webapps.intf.GUIException REMIND
     */
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
        throws IOException,
            ServletException {
        if (!(form instanceof IMapProperty)) {
            InternalException ie = new InternalException(GUIUTILS_INTERNAL_WRONGARG, "PushConfirmedAction", form.toString());

            throw new GUIException(PUSH_FROMDETAILS_ERROR, ie);
        }
        init(request);
        IMapProperty formbean = (IMapProperty) form;
        HttpSession   session = request.getSession();

        List targets = (List) session.getAttribute(SESSION_TGS_TOPUSH);
        String all = request.getParameter("all");


        String[] endpoints = null;
        ServletContext   sc = servlet.getServletConfig()
                                     .getServletContext();
        main = TenantHelper.getTenantSubMain(sc, request);
        
        IConfig dmConfig = main.getServerConfig();
        String id = DMHelperUtils.targetToNameString(targets);
        Props pushConfig = main.getPushConfig();

        // Check to see if we have current active deployment running in the system


        try {

            endpoints = getEndPoints(session, request, targets);
            if (endpoints.length < 1)
                throw new SubKnownException(PUSH_FROMDETAILS_NOTARGETS);

        } catch(Exception e) {
            throw new GUIException(e);
        }


        try {
            DMHelper dm = DMHelper.getInstance(dmConfig, tenant.getName());

            // check to see if previous last deployment is still running

            String lastDeploymentID = getLastDeploymentID(pushConfig);
            if (lastDeploymentID != null && dm.isDeploymentRunning(lastDeploymentID))  {
                // we allow only one active deployment in this release 6.0.2.0
                return mapping.findForward("deploymentalreadyrunning");
            }  else {
                session.removeAttribute(PUSH_DEPLOYMENTBEAN);
            }
            String[] cmds = new String[2];
            if (all != null && all.equals("true")) {
                cmds[0] = getSubscriptionServiceCommand(null);
            } else {
                 cmds[0] = getSubscriptionServiceCommand(id);
            }
            cmds[1] = getPatchServiceCommand(request);
            // Create the deployment in the DM
            IDMDeployment deployment = dm.createDeployment(endpoints, cmds,
                                                           dmConfig.getProperty("dm.tunerlogin"),
                                                           dmConfig.getProperty("dm.tunerpassword"));
            IDMProperty deploymentProps = deployment.getConfig();

            // set the activelimit && quorum properties on the deploymentProps
            String activeLimit, quorum;
            activeLimit = dmConfig.getProperty("dm.activelimit");
            quorum = dmConfig.getProperty("dm.quorum");
            if (activeLimit != null && deploymentProps.setProperty("activeLimit", activeLimit) == false) {
                if (DEBUG)
                    System.out.println("Unable to set activeLimit on the DM");
            }
            if (quorum != null && deploymentProps.setProperty("quorum", quorum) == false) {
                if (DEBUG)
                    System.out.println("Unable to set quorum on the DM");
            }

            String deploymentID =  deployment.getID();

            // store the deployment in the session vars and also in the dm config props
            // as the last deployment issued from the CMS
            session.setAttribute(CURRENT_PUSH_DEPLOYMENT, deployment.getID());
            storePushProperties(pushConfig, targets, deploymentID);




            IDMDeploymentStatus status = deployment.start();
            DMDeploymentView deploymentview = new DMDeploymentView(deployment, main.getTenant());
            session.setAttribute(PUSH_DEPLOYMENTBEAN, deploymentview);


            // got to wait for 2 seconds as DM does not return a running DeploymentStatus
            // if we don't.  Wait is too short to use DelayedAction.  Could improve if we
            // have DM Notifications.
            synchronized (this) {
                try {
                    wait(2000);
                } catch(InterruptedException e) {
                }
            }
            DMHelperUtils.updateDeploymentStatus(deploymentview, main.getTenantName());
            GUIUtils.log(servlet, request, LOG_SUBSCRIPTION_PUSH_STARTED, deploymentID);



        } catch (DepMgrException dme) {
            dme.printStackTrace();
            return (mapping.findForward("failure"));
        } catch (CriticalException e) {
            e.printStackTrace();
            return (mapping.findForward("failure"));
        }
        return (mapping.findForward("success"));
    }

    private String getLastDeploymentID(Props pushConfig) {
        pushConfig.load();
        return pushConfig.getProperty(LAST_DEPLOYMENT_ID);
    }

    private void storePushProperties(Props pushConfig, List targets, String deploymentID) {

        pushConfig.load();

        String targetNameString = DMHelperUtils.targetToNameString(targets);
        String targetNameTypeKey = targetNameString + PUSH_TARGET_NAME_TYPE_SUFFIX;
        String targetNameTypeString = DMHelperUtils.targetToNameTypeString(targets);


        pushConfig.setProperty(LAST_DEPLOYMENT_ID, deploymentID);
        pushConfig.setProperty(LAST_DEPLOYMENT_NAMETYPE, targetNameTypeString);
        pushConfig.setProperty(LAST_DEPLOYMENT_DN, targetNameString);

        pushConfig.setProperty(targetNameString, deploymentID);
        pushConfig.setProperty(targetNameTypeKey, targetNameTypeString);

        pushConfig.save();
    }


    private String[] getEndPoints(HttpSession session, HttpServletRequest request, List targets)
            throws SubKnownException {
        // Get the targets  which are selected

        Vector vec = new Vector();
        String [] endpoints = null;
        GroupMembershipFromInventory groupMembershipFromInventory = new GroupMembershipFromInventory((IMainDataSourceContext)main, main.getTenant());

        for (int i = 0; i < targets.size(); i++) {
            Target tgt = (Target) targets.get(i);
            String id = tgt.getId();
            try {
                LDAPConnection conn = LDAPWebappUtils.getBrowseConn(request);
                LDAPName ldapName = conn.getParser();
                id = id.toLowerCase().trim();
                if (!"all".equals(id)) {
                    id = ldapName.getCanonicalName(id);
                }

                endpoints = groupMembershipFromInventory.getTargetList(id, (IUserPrincipal)request.getUserPrincipal());

                for (int index = 0; index < endpoints.length; index++) vec.addElement(endpoints[index]);


            } catch (Exception e) {
                throw new SubKnownException(PUSH_FROMDETAILS_NOTARGETS);
            }

        }

        String[] results = new String[vec.size()];

        return (String[]) vec.toArray(results);
    }



    /*
    * DM channel command is of the form
    * channel url|generic|success log id1,...success log idn|failure log id1,....failure log idn|min log id| max log id|wait for exit| command time out|=start
    * additional parameters can be specified at the end of the start arg
    */
    private String getSubscriptionServiceCommand(String id) {
        String publishurl = main.getConfig().getProperty("subscriptionmanager.push.subscriptionserviceurl");
        if (publishurl == null)  {
            publishurl = main.getConfig().getProperty("subscriptionmanager.publishurl");
        }
        String command = publishurl + "|" +
                        "generic|" +
                        "8598|" +
                        "8599|" +
                        SUBSCRIPTION_MIN + '|' +
                        SUBSCRIPTION_MAX + '|' +
                        "true|-1=start -updateNow";
        if (id != null) {
            command = command + " \"" + id + "\"";
        }
        return command;
    }
      /*String[] cmds = {"http://akasetty:5282/PatchService|generic|41101|41102|41000|41999|true|-1=start -preview debug"};
    }   */

    private String getPatchServiceCommand(HttpServletRequest request) throws CriticalException {
        String command = null;
        
      //if(GUIUtils.getMain(servlet).hasCapability(IAppConstants.CAPABILITIES_PATCH)) {
            try {
                String patchServiceUrl = main.getConfig().getProperty("subscriptionmanager.push.patchserviceurl");
                if (patchServiceUrl == null) {
                    patchServiceUrl= PatchUtils.getPatchServiceUrl(request, tenant);
                }
                if (patchServiceUrl == null) {
                    throw new CriticalException(IErrorConstants.PATCH_SERVICEURLNOTCONFIGURED);
                }
                command = patchServiceUrl + "|" +
                        "generic|" +
                        "41101|" +
                        "41102|" +
                        PATCH_MGR_MIN + '|' +
                        PATCH_MGR_MAX + '|' +
                        "true|-1=start -install";

            } catch (SystemException se) {
                GUIUtils.log(servlet, request, LOG_SUBSCRIPTION_PUSH_NOPATCHSERVICE);
                command = null;
            }
      //}

        return command;
    }



}