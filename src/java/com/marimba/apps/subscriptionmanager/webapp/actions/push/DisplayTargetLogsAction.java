package com.marimba.apps.subscriptionmanager.webapp.actions.push;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;

import com.marimba.webapps.intf.IMapProperty;
import com.marimba.apps.subscriptionmanager.webapp.util.push.DMDeploymentView;
import com.marimba.apps.subscriptionmanager.webapp.util.push.DMHelper;
import com.marimba.apps.subscriptionmanager.webapp.forms.PushUrlForm;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;

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
public class DisplayTargetLogsAction extends AbstractAction {


       public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
        throws IOException,
               ServletException {

           PushUrlForm formbean = (PushUrlForm) form;
           HttpSession   session = request.getSession();
           ArrayList targetLogs = null;

           // Get the current deployment bean from the session
           try {
               // remove old targetLogs session attribute
               session.removeAttribute("targetLogs");
               init(request);
               DMDeploymentView deploymentView = (DMDeploymentView) session.getAttribute("deploymentbean");
               targetLogs = DMHelper.getInstance(tenant.getName()).getTargetLogs(deploymentView.getDeployment(), formbean.getTargetUrl());
               session.setAttribute("targetLogs", targetLogs);
           } catch (Exception e) {
               e.printStackTrace();
           }


            return (mapping.findForward("success"));
    }
}
