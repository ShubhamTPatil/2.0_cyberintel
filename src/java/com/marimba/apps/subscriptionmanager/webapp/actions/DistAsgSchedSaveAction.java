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

import java.util.ArrayList;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.objects.Channel;

import com.marimba.apps.subscription.common.intf.SubInternalException;

import com.marimba.apps.subscriptionmanager.webapp.forms.ScheduleEditForm;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;

import com.marimba.castanet.schedule.Schedule;

import com.marimba.webapps.intf.GUIException;

/**
 * Handles setting of schedule in the distribution assignment section page.
 *
 * @author Theen-Theen Tan
 * @version 1.2, 12/17/2001
 */
public final class DistAsgSchedSaveAction
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
        ScheduleEditForm schedForm = (ScheduleEditForm) form;

	if ("cancel".equals(request.getParameter("action"))) {
	    if (DEBUG) {
		System.out.println("DistAsgSchedSave Cancel");
	    }
	    if (-1 == schedForm.getChannelId()) {
		return (mapping.findForward("common_success"));
	    }
	} else {

	    DistributionBean distbean = getDistributionBean(request);
	    ArrayList channels = distbean.getApplicationChannels();
            String           schedPeriod = schedForm.getSchedulePeriod();
	    String           sched = schedForm.getScheduleString();
	    String           schedType = schedForm.getType();
	    Schedule         schedObj = null;
        boolean          wowOnUpdate = false;
        boolean          wowOnRepair = false;
        boolean          wowOnInit = false;
        boolean          wowOnSec = false;

	    int              channelId = schedForm.getChannelId();

	    if (DEBUG) {
		System.out.println("DistAsgSchedSaveAction Edit ");
	    }

	    if (DEBUG) {
		System.out.println(schedType + ": " + sched);
	    }

	    try {
		    try {
                        if ("true".equals(((ScheduleEditForm)form).getValue(ScheduleEditForm.SET_SCHEDULE))) {
                            if (PHASE_INIT.equals(schedType) || PHASE_SEC.equals(schedType) || PHASE_POSTPONE.equals(schedType)) {
                                sched = schedPeriod;
                                String wow_init = (String) ((ScheduleEditForm)form).getValue(ScheduleEditForm.ENABLE_WOW_ON_INIT);
                                if(wow_init != null && wow_init.length() > 0) {
                                    wowOnInit = true;
                                }
                                String wow_sec = (String) ((ScheduleEditForm)form).getValue(ScheduleEditForm.ENABLE_WOW_ON_SEC);
                                if(wow_sec != null && wow_sec.length() > 0) {
                                    wowOnSec = true;
                                }
                            } else if (PHASE_VERREPAIR.equals(schedType) || PHASE_UPDATE.equals(schedType) ) {
                                sched = sched + " " + schedPeriod;
                                String wow_update = (String) ((ScheduleEditForm)form).getValue(ScheduleEditForm.ENABLE_WOW_ON_UPDATE);
                                if(wow_update != null && wow_update.length() > 0) {
                                    wowOnUpdate = true;
                                }
                                String wow_repair = (String) ((ScheduleEditForm)form).getValue(ScheduleEditForm.ENABLE_WOW_ON_REPAIR);
                                if(wow_repair != null && wow_repair.length() > 0) {
                                    wowOnRepair = true;
                                }
                            }
                        } else {
                            sched = "";
                        }
                        if (sched != null && sched.length() !=0) {
                            schedObj = Schedule.readSchedule(sched.trim());
                        }
		    } catch (Exception e) {
			throw new SubInternalException(SCHED_INTERNAL_INVALIDFORMAT, sched);
		    }


		if (channelId == -1) {
		    // Editing common schedule
		    if (PHASE_INIT.equals(schedType)) {
			distbean.setInitSchedule((schedObj == null)?null:schedObj.toString());
			distbean.setWowforInit(wowOnInit);
		    } else if (PHASE_SEC.equals(schedType)) {
			distbean.setSecSchedule((schedObj == null)?null:schedObj.toString());
			distbean.setWowforSec(wowOnSec);
		    } else if (PHASE_UPDATE.equals(schedType)) {
			distbean.setUpdateSchedule((schedObj == null)?null:schedObj.toString());
			distbean.setWowforUpdate(wowOnUpdate);
		    } else if (PHASE_VERREPAIR.equals(schedType)) {
			distbean.setVerRepairSchedule((schedObj == null)?null:schedObj.toString());
			distbean.setWowforRepair(wowOnRepair);
		    } else if (PHASE_POSTPONE.equals(schedType)) {
                distbean.setPostponeSchedule((schedObj == null)?null:schedObj.toString());
            }
		    return (mapping.findForward("common_success"));
		} else {
		    Channel channel = (Channel) channels.get(channelId);
		    if (PHASE_INIT.equals(schedType)) {
			channel.setInitSchedule(schedObj);
			channel.setWOWForInit(wowOnInit);
		    } else if (PHASE_SEC.equals(schedType)) {
			channel.setSecSchedule(schedObj);
			channel.setWOWForSec(wowOnSec);
		    } else if (PHASE_UPDATE.equals(schedType)) {
			channel.setUpdateSchedule(schedObj);
                channel.setWOWForUpdate(wowOnUpdate);
		    } else if (PHASE_VERREPAIR.equals(schedType)) {
			channel.setVerRepairSchedule(schedObj);
			channel.setWOWForRepair(wowOnRepair);
		    } else if (PHASE_POSTPONE.equals(schedType)) {
                channel.setPostponeSchedule(schedObj);
            }
		}
	    } catch (Exception se) {
		throw new GUIException(SCHED_SAVE_ERROR, se);
	    }
	}
	return (mapping.findForward("success"));
    }
}
