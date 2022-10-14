// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.intf.SubInternalException;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscriptionmanager.webapp.forms.ScheduleEditForm;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Handles set schedule on the add target details page.
 *
 * @author Jayaprakash Paramasivam
 * @version 1.0, 05/09/2005
 */

public final class AddTargetSchedEditAction extends AbstractAction {
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

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession();
        ArrayList tchannelmap = (ArrayList) session.getAttribute(ADD_REMOVE_PACKAGE);
        ArrayList selectedList = new ArrayList(DEF_COLL_SIZE);
        DistributionBean tempbean = (DistributionBean) session.getAttribute("add_common_target");
        ScheduleEditForm schedForm = (ScheduleEditForm) form;
        String sch = null;
        String schedType = (String) request.getParameter("schedType");
        String targetchNum = (String) request.getParameter("target");
        int tchmapId = 0;

        if (DEBUG) {
            System.out.println("Editing " + schedType + "schedule");
        }

        try {
            if (schedType == null) {
                throw new SubInternalException(SCHED_INTERNAL_UNKNOWNTYPE, schedType);
            }

            schedForm.init(request.getLocale());
            TargetChannelMap targetchmap = null;
            Channel ch=null;
            if (targetchNum == null) {
                // Editing common schedule
                if (PHASE_INIT.equals(schedType)) {
                    sch = tempbean.getInitSchedule();
                    schedForm.setValue("ENABLE_WOW_ON_INIT", (tempbean.getWowforInit() ? "on":"") );
                } else if (PHASE_SEC.equals(schedType)) {
                    sch = tempbean.getSecSchedule();
                    schedForm.setValue("ENABLE_WOW_ON_SEC", (tempbean.getWowforSec() ? "on":"") );
                } else if (PHASE_UPDATE.equals(schedType)) {
                    sch = tempbean.getUpdateSchedule();
                    schedForm.setValue("ENABLE_WOW_ON_UPDATE", (tempbean.getWowforUpdate() ? "on":"") );
                } else if (PHASE_VERREPAIR.equals(schedType)) {
                    sch = tempbean.getVerRepairSchedule();
                    schedForm.setValue("ENABLE_WOW_ON_REPAIR", (tempbean.getWowforRepair() ? "on":"") );
                } else if (PHASE_POSTPONE.equals(schedType)) {
                    sch = tempbean.getPostponeSchedule();
                } else {
                    throw new SubInternalException(SCHED_INTERNAL_UNKNOWNTYPE, schedType);
                }
                schedForm.setTargetchmapId(-1);
            } else {

                if (targetchNum != null) {
                    tchmapId = Integer.parseInt(targetchNum);
                    targetchmap = (TargetChannelMap) tchannelmap.get(tchmapId);
                    ch = targetchmap.getChannel();
                }
                if (ch == null) {
                    throw new SubInternalException(ADDTARGET_SCHED_INTERNAL_UNKNOWNCHANNEL, targetchNum);
                }
                schedForm.setValue("ENABLE_WOW_ON_INIT", (ch.getWOWForInit() ? "on":"") );
                schedForm.setValue("ENABLE_WOW_ON_SEC", (ch.getWOWForSec() ? "on":"") );
                schedForm.setValue("ENABLE_WOW_ON_UPDATE", (ch.getWOWForUpdate() ? "on":"") );
                schedForm.setValue("ENABLE_WOW_ON_REPAIR", (ch.getWOWForRepair() ? "on":"") );
                if (PHASE_INIT.equals(schedType)) {
                    sch = ch.getInitScheduleString();
                } else if (PHASE_SEC.equals(schedType)) {
                    sch = ch.getSecScheduleString();
                } else if (PHASE_UPDATE.equals(schedType)) {
                    sch = ch.getUpdateScheduleString();
                } else if (PHASE_VERREPAIR.equals(schedType)) {
                    sch = ch.getVerRepairScheduleString();
                } else if (PHASE_POSTPONE.equals(schedType)) {
                    sch = ch.getPostponeScheduleString();
                } else {
                    throw new SubInternalException(SCHED_INTERNAL_UNKNOWNTYPE, schedType);
                }

                if (DEBUG) {
                    System.out.println("sch " + sch);
                }
                schedForm.setTargetchmapId(tchmapId);
                selectedList.add(targetchmap);
                GUIUtils.setToSession(request, "add_selected_list", (Object) selectedList);
            }
            schedForm.setType(schedType);
            setScheduleString(sch, schedForm);
        } catch (SystemException se) {
            GUIException guie = new GUIException(se);
            throw guie;
        } catch (Exception se) {
            throw new GUIException(se);
        } finally {
            session.removeAttribute("session_dist");
        }

        return (mapping.findForward("success"));
    }

    public void setScheduleString(String schStr, ScheduleEditForm form) {
        String ACTIVE ="active";
        if ( schStr == null) {
            form.setSchedulePeriod(null);
            form.setValue("SET_SCHEDULE", "false");
        } else {
            int index = schStr.toLowerCase().indexOf(ACTIVE);
            form.setValue("SET_SCHEDULE", "true");
            if (index == -1) {
                form.setScheduleString(schStr);
                form.setSchedulePeriod(null);
            } else if (index ==0) {
                form.setSchedulePeriod(schStr);
            } else {
                form.setScheduleString(schStr.substring(0, index).trim());
                form.setSchedulePeriod(schStr.substring(index).trim());
            }
        }
    }
}
