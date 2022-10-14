package com.marimba.apps.subscriptionmanager.webapp.actions.push;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.intf.CriticalException;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscriptionmanager.webapp.util.push.DMDeploymentView;
import com.marimba.apps.subscriptionmanager.webapp.util.push.DMHelper;
import com.marimba.apps.subscriptionmanager.webapp.util.DMHelperUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.forms.PushUrlForm;
import com.marimba.apps.sdm.intf.simplified.IDMDeployment;
import com.marimba.apps.sdm.intf.simplified.IDMDeploymentStatus;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.intf.SubKnownException;



// Copyright 1997-2004, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

/**
 * Created by IntelliJ IDEA.
 * @author  Anantha Kasetty
 * @version $Revision$, $Date$
 * $File$
 *
 */
public class PushStatusAction extends AbstractAction {
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
        throws IOException,
            ServletException {


        PushUrlForm statusForm = (PushUrlForm)form;
        HttpSession   session = request.getSession();
        init(request);
        ServletContext   sc = servlet.getServletConfig()
                .getServletContext();
        SubscriptionMain main = TenantHelper.getTenantSubMain(sc, request);

        String deploymentID = null;
        DMDeploymentView deploymentView = (DMDeploymentView) session.getAttribute(PUSH_DEPLOYMENTBEAN);

        DMHelper dm = null;
        IDMDeployment deployment = null;
        ActionForward forward = mapping.findForward("running");
        String dn = null;

        // this action can be called without an form, if called with an form, get the dn value from the form
        try {
            if (statusForm != null) {
                dn = statusForm.getDn();

                if (dn != null) {
                    dn = dn.toLowerCase().trim();
                    dn = DMHelperUtils.getActualDN(main.getPushConfig(), dn);
                    deploymentID = DMHelperUtils.getDeploymentIDFromDN(main.getPushConfig(), dn);
                }

            }
                // if no value is passed on the form then use the session vars
            if (deploymentID != null) {
                String[] targetNameType = DMHelperUtils.getTargetTypeNameFromDN(main.getPushConfig(), dn);

                // convert the TargetName type into a list of Targets and set it in the current session
                List targets = getTargetList(targetNameType, dn);
                if (targets != null && targets.size() > 0) {
                    /*GUIUtils.setToSession(request, MAIN_PAGE_TARGET, targets); */
                    GUIUtils.setToSession(request, SESSION_TGS_TOPUSH, targets);
                }
            } else {
                if (deploymentView != null) {
                    deploymentID = deploymentView.getDeploymentID();
                } else {
                    throw new SubKnownException(PUSH_STATUS_NODEPLOYMENT);
                }
            }



            dm = DMHelper.getInstance(tenant.getName());
            deployment = dm.getDeploymentFromDeploymentID(deploymentID);


            if (deploymentView != null && deploymentID.equals(deploymentView.getDeploymentID())) {
                DMHelperUtils.updateDeploymentStatus(deploymentView, tenant.getName());
            } else {
                deploymentView = new DMDeploymentView(deployment, tenant);
                DMHelperUtils.updateDeploymentStatus(deploymentView, tenant.getName());
                session.setAttribute(PUSH_DEPLOYMENTBEAN, deploymentView);
            }

            if (deployment != null) {
                if (deployment.getStatus().getStatus().equals(IDMDeploymentStatus.RUNNING)) {
                    forward = mapping.findForward("running");
                } else {
                    GUIUtils.log(servlet, request, LOG_SUBSCRIPTION_PUSH_COMPLETED, deploymentView.getDeploymentID());
                    forward = mapping.findForward("success");
                }
            }

        } catch (SystemException se) {
            throw new GUIException(se);

        } catch (Exception e) {
            e.printStackTrace();
            throw new GUIException (new CriticalException(e, PUSH_STATUS_DEPLOYMENTMANGER_ERROR));
        }

        return forward;

    }

    private ArrayList getTargetList(String[] targetNameType, String dn) {
        ArrayList targets = new ArrayList();
        String targetName;
        String targetType;
        try {
            for(int index = 0; index < targetNameType.length; index = index+2) {
                targetName = targetNameType[index];
                targetType = targetNameType[index+1];

                if (targetName != null && targetType != null) {
                    targets.add(new Target(targetName, targetType, dn));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return targets;
    }

}


