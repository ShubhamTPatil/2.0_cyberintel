package com.marimba.apps.subscriptionmanager.webapp.actions.push;

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

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;


import javax.servlet.*;
import javax.servlet.http.*;



import com.marimba.webapps.intf.*;
import com.marimba.apps.sdm.intf.simplified.IDMDeployment;
import com.marimba.apps.sdm.intf.simplified.DepMgrException;
import com.marimba.apps.subscriptionmanager.webapp.util.push.DMDeploymentView;
import com.marimba.apps.subscriptionmanager.webapp.util.DMHelperUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;

/**
 * Retry the deployment whose deploymentbean should be present in the session
 *
 * @author Anantha Kasetty
 * @version $Revision$, $Date$
 */
public class PushRetryAction extends com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction {
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException, ServletException {

        HttpSession session = request.getSession();

        DMDeploymentView deploymentView = (DMDeploymentView) session.getAttribute("deploymentbean");
        IDMDeployment deployment = deploymentView.getDeployment();
        init(request);
        // throw an exception if deployment is
        try {
            deployment.retry();
            GUIUtils.log(servlet, request, LOG_SUBSCRIPTION_PUSH_RETRY, deploymentView.getDeploymentID());

            synchronized(this) {
                try {
                    // convert this to a Delayed Action to remove the ugly wait.
                    wait(3000);
                    DMHelperUtils.updateDeploymentStatus(deploymentView, tenant.getName());
                } catch(InterruptedException e) {

                }
            }
        } catch (DepMgrException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            throw new GUIException (PUSH_STATUS_DEPLOYMENTMANGER_ERROR, e);
        }
        return (mapping.findForward("success"));

    }
}

