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

import java.util.ArrayList;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.util.ScheduleUtils;

import com.marimba.apps.subscriptionmanager.webapp.forms.DistAsgForm;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.KnownException;

/**
 * Handles the action level validations that is needed on the distribution bean when OK is clicked on the distribution assignment page.
 *
 * @author Theen-Theen Tan
 * @version 1.5, 09/11/2002
 */
public final class DistAsgPreviewAction
    extends AbstractAction {
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
     * @throws IOException REMIND
     * @throws ServletException REMIND
     * @throws GUIException REMIND
     */
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
        throws IOException, 
                   ServletException {
        if (DEBUG) {
            System.out.println("Distribution Asg Preview Action called");
        }

        try {
            DistributionBean distbean = getDistributionBean(request);
            ArrayList        targets = distbean.getTargets();

            // Do some verifications here
            if ((targets == null) || (targets.size() == 0)) {
                throw new KnownException(DIST_SAVE_NOTARGETS);
            }

            ArrayList channels = distbean.getChannels();

            if ((channels == null) || (channels.size() == 0)) {
                throw new KnownException(DIST_SAVE_NOCHANNELS);
            }

            /* //REMIND: this is problematic because there could be an overlap of time that should
             * not occur for secondary schedules.
             *
             */
            if (!INCONSISTENT.equals(distbean.getInitSchedule()) && !INCONSISTENT.equals(distbean.getSecSchedule())) {
                if (!ScheduleUtils.validateSchedules(distbean.getInitSchedule(), distbean.getSecSchedule())) {
                    throw new KnownException(SCHEDULE_ORDER);
                }
            }

            // If there is a channel in the assignment with secondary state
            // set, there must be a secondary schedule
            boolean noSecSchedule = (null == distbean.getSecSchedule());

            for (int j = 0; j < channels.size(); j++) {
                Channel selectedChn = (Channel) channels.get(j);

                if (noSecSchedule && (null != selectedChn.getSecState())) {
                    throw new KnownException(DIST_SAVE_NOSECSCHED);
                }
            }

            // If there is a secondary schedule, there should be some
            // channel assignments for secondary states
            boolean noStates = true;

            if (!noSecSchedule) {
                for (int j = 0; j < channels.size(); j++) {
                    Channel selectedChn = (Channel) channels.get(j);

                    if (null != selectedChn.getSecState()) {
                        noStates = false;

                        break;
                    }
                }

                if (noStates) {
                    throw new KnownException(DIST_SAVE_NOSECSTATES);
                }
            }
        } catch (Exception e) {
            throw new GUIException(PKG_SAVE_ERROR, e);
        }

        return (mapping.findForward("success"));
    }
}
