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
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.text.DateFormat;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.castanet.schedule.Schedule;
import com.marimba.castanet.schedule.ScheduleInfo;

import com.marimba.apps.subscription.common.intf.SubInternalException;

import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.util.ScheduleUtils;

import com.marimba.apps.subscriptionmanager.webapp.forms.ScheduleEditForm;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.tools.util.ScheduleUtil;

/**
 * Handles set schedule on the distribution assignment page.
 *
 * @author Theen-Theen Tan
 * @version 1.5, 09/15/2002
 */
public final class DistAsgSchedEditAction
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
    DistributionBean distbean = getDistributionBean(request);
	ArrayList channels = distbean.getApplicationChannels();
    ScheduleEditForm schedForm = (ScheduleEditForm) form;
    schedForm.init(request.getLocale());

    String sch = null;
    String previousSch =null;

	String schedType = (String) request.getParameter("schedType");
	String channelNum = (String) request.getParameter("channel");


	if (DEBUG) {
	    System.out.println("Editing " + schedType + "schedule");
	}

        try {
	    if (schedType == null) {
		throw new SubInternalException(SCHED_INTERNAL_UNKNOWNTYPE, schedType);
	    }

	    if (channelNum == null) {
		// Editing common schedule
		if (PHASE_INIT.equals(schedType)) {
		    sch = distbean.getInitSchedule();
		    schedForm.setValue("ENABLE_WOW_ON_INIT", (distbean.getWowforInit() ? "on":"") );
		} else if (PHASE_SEC.equals(schedType)) {
		    sch = distbean.getSecSchedule();
		    schedForm.setValue("ENABLE_WOW_ON_SEC", (distbean.getWowforSec() ? "on":"") );
		} else if (PHASE_UPDATE.equals(schedType)) {
		    sch = distbean.getUpdateSchedule();
		    schedForm.setValue("ENABLE_WOW_ON_UPDATE", (distbean.getWowforUpdate() ? "on":"") );
		} else if (PHASE_VERREPAIR.equals(schedType)) {
		    sch = distbean.getVerRepairSchedule();
		    schedForm.setValue("ENABLE_WOW_ON_REPAIR", (distbean.getWowforRepair() ? "on":"") );
		} else if (PHASE_POSTPONE.equals(schedType)) {
            sch = distbean.getPostponeSchedule();
        } else {
		    throw new SubInternalException(SCHED_INTERNAL_UNKNOWNTYPE, schedType);
		}
		schedForm.setChannelId(-1);
	    } else {
		// Editing schedule for individual channel
		int channelId = 0;
		Channel channel = null;
		if (channelNum != null) {
		    channelId = Integer.parseInt(channelNum);
		    channel = (Channel) channels.get(channelId);
		}

		if (channel == null) {
		    throw new SubInternalException(SCHED_INTERNAL_UNKNOWNCHANNEL, channelNum);
		}
        schedForm.setValue("ENABLE_WOW_ON_INIT", (channel.getWOWForInit() ? "on":"") );
        schedForm.setValue("ENABLE_WOW_ON_SEC", (channel.getWOWForSec() ? "on":"") );
        schedForm.setValue("ENABLE_WOW_ON_UPDATE", (channel.getWOWForUpdate() ? "on":"") );
        schedForm.setValue("ENABLE_WOW_ON_REPAIR", (channel.getWOWForRepair() ? "on":"") );
		if (PHASE_INIT.equals(schedType)) {
		    sch = channel.getInitScheduleString();
                    previousSch = distbean.getInitSchedule(channel.getUrl());
		} else if (PHASE_SEC.equals(schedType)) {
		    sch = channel.getSecScheduleString();
                    previousSch = distbean.getSecondarySchedule(channel.getUrl());
		} else if (PHASE_UPDATE.equals(schedType)) {
		    sch = channel.getUpdateScheduleString();
                    previousSch = distbean.getUpdateSchedule(channel.getUrl());
		} else if (PHASE_VERREPAIR.equals(schedType)) {
		    sch = channel.getVerRepairScheduleString();
                    previousSch = distbean.getVerifyRepairSchedule(channel.getUrl());
		} else if (PHASE_POSTPONE.equals(schedType)) {
            sch = channel.getPostponeScheduleString();
            previousSch = distbean.getPostponeSchedule(channel.getUrl());
        } else {
		    throw new SubInternalException(SCHED_INTERNAL_UNKNOWNTYPE, schedType);
		}

		if (DEBUG) {
		    System.out.println("sch " + sch);
		}
		schedForm.setChannelId(channelId);
	    }

	    schedForm.setType(schedType);
            setScheduleString(sch, schedForm);

        if (channelNum != null) {
            int channelId = Integer.parseInt(channelNum);
            Channel channel = (Channel) channels.get(channelId);

           if((previousSch != null) && previousSch.length() > 0) {
                ScheduleInfo schedInfo = Schedule.getScheduleInfo(previousSch);
                if(ScheduleUtils.isScheduleStarted(schedInfo)) {
                    schedForm.setNewSched(true);
                } else {
                    schedForm.setNewSched(false);
                }
          } else {
                schedForm.setNewSched(false);
          }

        } else {
               if((sch != null) && (sch.length() > 0) ) {
                    ScheduleInfo schedInfo = Schedule.getScheduleInfo(sch);
                    if(ScheduleUtils.isScheduleStarted(schedInfo)) {
                        schedForm.setNewSched(true);
                    } else {
                        schedForm.setNewSched(false);
                    }
               } else {
                schedForm.setNewSched(false);
              }
          }
        } catch (Exception se) {
            throw new GUIException(se);
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
