// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;

import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.intf.SubInternalException;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Target;

import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.webapp.forms.ScheduleEditForm;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;

/**
 * This action is called when visiting the service update schedule page.  It gets the target from the session variable, queries LDAP to obtain the subscription
 * for the target, and loads the schedule information to be displayed into the ScheduleEditForm and forwards to /distribution/schedule.jsp page.
 *
 * @author Theen-Theen Tan
 * @author Sunil Ramakrishnan
 * @version
 */
public final class ServiceSchedLoadAction
        extends AbstractAction {
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest req,
                                 HttpServletResponse response)
            throws IOException,
            ServletException {
        if (DEBUG) {
            System.out.println("ServiceSchedEditAction called");
        }

        // read the service schedule string from SESSION_DIST Distribution bean.
        try {
            ISubscription sub = null;

            sub = (ISubscription) GUIUtils.getFromSession(req, PAGE_TCHPROPS_SUB);
            String serviceSchedule = getTChPropValue(sub.getProperty(PROP_SERVICE_KEYWORD, "update.schedule"));

            if(DEBUG5) {
                System.out.println("****** "+ serviceSchedule );
            }

            // initialize the form
            ScheduleEditForm schedForm = (ScheduleEditForm) form;
            schedForm.init();
            schedForm.setType("service");
            schedForm.setScheduleString(serviceSchedule);
        } catch (SystemException e) {
        	if(DEBUG) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        	}
        }

        // check if user clicked preview
        String action = req.getParameter("action");
        if ("preview".equals(action)) {
            return mapping.findForward("preview");
        }

        return (mapping.findForward("success"));
    }
}
