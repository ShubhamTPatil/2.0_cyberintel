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
import com.marimba.apps.subscription.common.intf.SubInternalException;

import com.marimba.apps.subscriptionmanager.webapp.forms.ScheduleEditForm;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;

import com.marimba.castanet.schedule.Schedule;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;

/**
 * Handles setting of schedule in the distribution assignment section page.
 *
 * @author Jayaprakash Paramasivam
 * @version 1.0, 05/09/2005
 */
public final class AddTargetSchedSaveAction
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
        HttpSession session = request.getSession();
        DistributionBean tempbean = (DistributionBean) session.getAttribute("add_common_target");
        ArrayList tchannelMap = (ArrayList) session.getAttribute(ADD_REMOVE_PACKAGE);
        int              tchmapId = schedForm.getTargetchmapId();

	if ("cancel".equals(request.getParameter("action"))) {
	    if (DEBUG) {
		System.out.println("AddtargetSchedSave Cancel");
	    }
	    if (-1 == schedForm.getTargetchmapId()) {
		return (mapping.findForward("common_success"));
	    }
	} else {
            String           schedPeriod = schedForm.getSchedulePeriod();
	    String           sched = schedForm.getScheduleString();
	    String           schedType = schedForm.getType();
	    Schedule         schedObj = null;
	    boolean wowOnUpdate = false;
	    boolean wowOnRepair = false;
	    boolean wowOnInit = false;
	    boolean wowOnSec = false;


	    if (DEBUG) {
		System.out.println("AddTargetSchedSaveAction Edit ");
	    }

	    if (DEBUG) {
		System.out.println(schedType + ": " + sched + ": "+tchmapId);
	    }

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
                        sched = sched + " "+ schedPeriod;
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

		if (sched.length() != 0 && sched.length() !=0) {
		    try {
			schedObj = Schedule.readSchedule(sched);
		    } catch (Exception e) {
			throw new SubInternalException(SCHED_INTERNAL_INVALIDFORMAT, sched);
		    }
		}

                if (tchmapId == -1) {
                    // Editing common schedule
                    if (PHASE_INIT.equals(schedType)) {
                        tempbean.setInitSchedule((schedObj == null)?null:schedObj.toString());
                        tempbean.setWowforInit(wowOnInit);                        
                    } else if (PHASE_SEC.equals(schedType)) {
                        tempbean.setSecSchedule((schedObj == null)?null:schedObj.toString());
                        tempbean.setWowforSec(wowOnSec);
                    } else if (PHASE_UPDATE.equals(schedType)) {
                        tempbean.setUpdateSchedule((schedObj == null)?null:schedObj.toString());
                        tempbean.setWowforUpdate(wowOnUpdate);
                    } else if (PHASE_VERREPAIR.equals(schedType)) {
                        tempbean.setVerRepairSchedule((schedObj == null)?null:schedObj.toString());
                        tempbean.setWowforRepair(wowOnRepair);
                    } else if (PHASE_POSTPONE.equals(schedType)) {
                        tempbean.setPostponeSchedule((schedObj == null)?null:schedObj.toString());
                    }
                    return (mapping.findForward("common_success"));
                } else {

		    TargetChannelMap tcmap= (TargetChannelMap) tchannelMap.get(tchmapId);
                    Channel ch = tcmap.getChannel();
                        if (PHASE_INIT.equals(schedType)) {
                            ch.setInitSchedule(schedObj);
                            ch.setWOWForInit(wowOnInit);
                        } else if (PHASE_SEC.equals(schedType)) {
                            ch.setSecSchedule(schedObj);
                            ch.setWOWForSec(wowOnSec);
                        } else if (PHASE_UPDATE.equals(schedType)) {
                            ch.setUpdateSchedule(schedObj);
                            ch.setWOWForUpdate(wowOnUpdate);
                        } else if (PHASE_VERREPAIR.equals(schedType)) {
                            ch.setVerRepairSchedule(schedObj);
                            ch.setWOWForRepair(wowOnRepair);
                        } else if (PHASE_POSTPONE.equals(schedType)) {
                            ch.setPostponeSchedule(schedObj);
                        }
                }
            } catch (SystemException se) {
                GUIException guie = new GUIException(se);
                throw guie;
	    } catch (Exception se) {
		throw new GUIException(SCHED_SAVE_ERROR, se);
	    }
	}
	return (mapping.findForward("success"));
    }
}
