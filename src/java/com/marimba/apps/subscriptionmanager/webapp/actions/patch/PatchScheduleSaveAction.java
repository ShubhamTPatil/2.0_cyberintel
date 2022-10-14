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
import com.marimba.webapps.intf.KnownException;
import com.marimba.webapps.intf.SystemException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * PatchSaveAction extract the values from the Patch Assignment Form
 * and saves them in the DistributionBean object.
 *
 * This Action is called to commit the changes every time we move out
 * of any of the patch jsp pages
 *
 * @author Devendra Vamathevan
 *
 */

public final class PatchScheduleSaveAction extends AbstractAction {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        String pageName = request.getParameter("page");

        if (DEBUG) {
            System.out.println("PatchScheduleSaveAction called for page = " + pageName);
        }

        String patchServiceUrl;
        try {
        	init(request);
            patchServiceUrl = PatchUtils.getPatchServiceUrl(request, tenant);
            ScheduleEditForm patchScheduleForm = (ScheduleEditForm) form;
            String serviceSchedule = patchScheduleForm.getScheduleString();
            ISubscription sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);
            System.out.println("saving patch service utl :" + patchServiceUrl);
            if("true".equals(patchScheduleForm.getValue("SET_SCHEDULE"))) {
                String oldValue = PatchUtils.getPatchServiceProperty(sub, "subscription.update");
                // we have to remain with existing priority value
                String priority = getPriorityValue(oldValue);
                serviceSchedule = appendPriority(serviceSchedule, priority);
                PatchUtils.setPatchServiceProperty(sub, patchServiceUrl, "subscription.update", serviceSchedule);
            } else {
                PatchUtils.setPatchServiceProperty(sub, patchServiceUrl, "subscription.update", null);
            }
        } catch (KnownException ke){
            ke.printStackTrace();
        } catch (SystemException e) {
            throw new GUIException(e);
        }
        // and forward to the next action
        String forward = ((ScheduleEditForm) form).getForward();
        return (new ActionForward(forward, true));
    }
}