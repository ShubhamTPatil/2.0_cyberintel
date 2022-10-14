// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions.patch;

import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscriptionmanager.webapp.forms.ScheduleEditForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.PatchUtils;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This action is called when visiting the service update schedule page.  It gets the target from the session variable, queries LDAP to obtain the subscription
 * for the target, and loads the schedule information to be displayed into the ScheduleEditForm and forwards to /distribution/schedule.jsp page.
 *

 * @author Devendra Vamathevan
 * @version  $Revision$,  $Date$
 *
 */

public final class PatchScheduleAction extends AbstractAction {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest req, HttpServletResponse response) throws IOException, ServletException {

        if (DEBUG) {
            System.out.println("PatchScheduleAction called");
        }
        String serviceSchedule = null;
        ScheduleEditForm schedForm = null;
        // read the service schedule string from SESSION_DIST Distribution bean.
        String action = req.getParameter("action");
        try {
            // get the "patchmanager-config" from CMS. This is so we can find out the patch service we need to store the schedule for

            ISubscription sub = (ISubscription) GUIUtils.getFromSession(req, PAGE_TCHPROPS_SUB);
            serviceSchedule = PatchUtils.getPatchServiceProperty(sub, "subscription.update");
            if (DEBUG) {
                System.out.println("Patch service schedule : " + serviceSchedule);
            }            

            // initialize the form
            schedForm = (ScheduleEditForm) form;
            schedForm.init();
            schedForm.setType("service");

            if (serviceSchedule != null) {
                String[] tmp_arr = serviceSchedule.split(",");
                serviceSchedule = tmp_arr[0]; 
                int pos = serviceSchedule.indexOf('=');
                if (pos > 0) {
                    serviceSchedule = serviceSchedule.substring(pos + 1);
                }
            }
        } catch (SystemException e) {
            if (!"preview".equals(action)) {
                throw new GUIException(e);
            }
        } finally{
            schedForm.setScheduleString(serviceSchedule);
        }

        // check if user clicked preview

        if ("preview".equals(action)) {
            return mapping.findForward("preview");
        }
        return (mapping.findForward("success"));
    }
}