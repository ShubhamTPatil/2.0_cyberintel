// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;

import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

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
 * ServiceSchedSaveAction obtains the filled in schedule from ScheduleEditForm and saves it in the
 * DistributionBean object.
 *
 * @author Theen-Theen Tan
 * @author Sunil Ramakrishnan
 * @version
 */
public final class ServiceSchedSaveAction
    extends AbstractAction {
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
        throws IOException,
	       ServletException {
        if (DEBUG) {
            System.out.println("ServiceSchedSaveAction called");
        }

	// save the changes to the DistributionBean
	saveState(request, (ScheduleEditForm) form);

	// and forward to the next action
        String forward = ((ScheduleEditForm) form).getForward();
	return (new ActionForward(forward, true));
    }

    /**
     * Save the changes in the form to the DistributionBean.
     */
    private void saveState(HttpServletRequest request, ScheduleEditForm form) throws GUIException {
        try {
            ISubscription sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);
            DistributionBean distributionBean = getDistributionBean(request);
            if("true".equals(form.getValue("SET_SCHEDULE"))) {
                String serviceSchedule = form.getScheduleString();

                // If priority for this property is already defined, keep the priority and the value alone
                serviceSchedule = stripOutExistingPriority(sub.getProperty(PROP_SERVICE_KEYWORD, "update.schedule"), serviceSchedule);
                sub.setProperty(PROP_SERVICE_KEYWORD, "update.schedule", serviceSchedule);
                distributionBean.setServiceSchedule(serviceSchedule);
            } else {
                sub.setProperty(PROP_SERVICE_KEYWORD, "update.schedule", "null");
                distributionBean.setServiceSchedule(null);
            }
        } catch (SystemException e) {
            throw new GUIException(e);
        }
    }
}
