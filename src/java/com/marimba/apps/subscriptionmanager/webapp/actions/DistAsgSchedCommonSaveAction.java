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
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.castanet.schedule.Schedule;

import com.marimba.apps.subscription.common.intf.SubInternalException;

import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.ISubscriptionConstants;

import com.marimba.apps.subscriptionmanager.webapp.forms.ScheduleEditForm;
import com.marimba.apps.subscriptionmanager.webapp.forms.DistAsgForm;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IMapProperty;

/**
 * Handles saving schedules for a set of selected packages in the
 * distribution assignment section
 *
 * @author Theen-Theen Tan
 * @version 
 */
public final class DistAsgSchedCommonSaveAction
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
	IMapProperty formbean = (IMapProperty) form;


      String action=null;
      boolean consistentFlag=false;
      boolean consistentFlagSec=false;
      boolean consistentFlagUpd=false;
      boolean consistentFlagVer=false;
      boolean consistentFlagPostpone = false;
      boolean consistentFlagWOWForUpdate = false;
      boolean consistentFlagWOWForRepair = false;
      boolean consistentFlagWOWForInit = false;
      boolean consistentFlagWOWForSec = false;

      String scheduleString=null;
      String scheduleStringSec=null;
      String scheduleStringUpd=null;
      String scheduleStringVer=null;
      String scheduleStringPostpone = null;
      boolean wowForUpdate = false;
      boolean wowForRepair = false;
      boolean wowForInit = false;
      boolean wowForSec = false;

	try {
	     action= request.getParameter("action");
	    if ("cancel".equals(action)) {
            ((DistAsgForm) form).clearPagingVars(request);
        } else {
            if ("edit".equals(action)) {
                // REMIND t3, we should deal with showing that selected packages ahve inconsistent
                // schedules in the common schedule page (defect 38052)


                PersistifyChecksAction.SelectedRecords pkgSel =
                        (PersistifyChecksAction.SelectedRecords)request.getSession().getAttribute(
                                (String)formbean.getValue(SESSION_PERSIST_SELECTED));

                if (pkgSel!=null) {

                    ArrayList targetMapList = new ArrayList(pkgSel.getTargetChannelMaps());
                    Channel chn = null;
                    for (int i=0; i < targetMapList.size(); i++) {
                        chn = (Channel) targetMapList.get(i);

                         if((!chn.getInconsistentStates()) && (!chn.getInconsistentSched()))
                        {
                        distbean.setInitSchedule(chn.getInitScheduleString());
                        distbean.setInitScheduleInitValue(chn.getInitScheduleString());
                        distbean.setSecScheduleInitValue(chn.getSecScheduleString());
                        distbean.setSecSchedule(chn.getSecScheduleString());
                        distbean.setUpdateScheduleInitValue(chn.getUpdateScheduleString());
                        distbean.setUpdateSchedule(chn.getUpdateScheduleString());
                        distbean.setVerRepairSchedule(chn.getVerRepairScheduleString());
                        distbean.setVerRepairScheduleInitValue(chn.getVerRepairScheduleString());
                        distbean.setPostponeSchedule(chn.getPostponeScheduleString());
                        distbean.setPostponeScheduleInitValue(chn.getPostponeScheduleString());
                        distbean.setWowforUpdate(chn.getWOWForUpdate());
                        distbean.setWowforRepair(chn.getWOWForRepair());
                        distbean.setWowforInit(chn.getWOWForInit());
                        distbean.setWowforSec(chn.getWOWForSec());
                        }


                        if(chn.getInconsistentStates() || chn.getInconsistentSched())
                        consistentFlag=true;

                        if(i==0)
                        {
                        scheduleString=chn.getInitScheduleString();
                        scheduleStringSec= chn.getSecScheduleString();
                        scheduleStringUpd=chn.getUpdateScheduleString();
                        scheduleStringVer= chn.getVerRepairScheduleString();
                        scheduleStringPostpone = chn.getPostponeScheduleString();
                        wowForUpdate = chn.getWOWForUpdate();
                        wowForRepair = chn.getWOWForRepair();
                        wowForInit = chn.getWOWForInit();
                        wowForSec = chn.getWOWForSec();
                        }

                        if(i>0)
                        {
                            if(scheduleString==null && chn.getInitScheduleString()!=null)
                            consistentFlag=true;
                            else if(scheduleString==null && chn.getInitScheduleString()==null)
                            {
                                //Do nothing
                            }
                            else if(!scheduleString.equals(chn.getInitScheduleString()))
                            consistentFlag=true;

                            if(scheduleStringSec==null && chn.getSecScheduleString()!=null)
                            consistentFlagSec=true;
                            else if(scheduleStringSec==null && chn.getSecScheduleString()==null)
                            {
                                //Do nothing
                            }
                            else if(!scheduleStringSec.equals(chn.getSecScheduleString()))
                            consistentFlagSec=true;

                            if(scheduleStringUpd==null && chn.getUpdateScheduleString()!=null)
                            consistentFlagUpd=true;
                            else if(scheduleStringUpd==null && chn.getUpdateScheduleString()==null)
                            {
                                //Do nothing
                            }
                            else if(!scheduleStringUpd.equals(chn.getUpdateScheduleString()))
                            consistentFlagUpd=true;

                            if(scheduleStringVer==null && chn.getVerRepairScheduleString()!=null)
                            consistentFlagVer=true;
                            else if(scheduleStringVer==null && chn.getVerRepairScheduleString()==null)
                            {
                                //Do nothing
                            }
                            else if(!scheduleStringVer.equals(chn.getVerRepairScheduleString()))
                            consistentFlagVer=true;

                            if(scheduleStringPostpone == null && chn.getPostponeScheduleString() != null) {
                                consistentFlagPostpone = true;
                            } else if(scheduleStringPostpone == null && chn.getPostponeScheduleString() == null) {
                                //Do nothing
                            }
                            else if(!scheduleStringPostpone.equals(chn.getPostponeScheduleString())) {
                                consistentFlagPostpone = true;
                            }
                            // Check WOW Deployment Enabled option is inconsistent or not in Common Schedule page for update schedule
                            if((wowForUpdate && !chn.getWOWForUpdate()) || (!wowForUpdate && chn.getWOWForUpdate())) {
                            	consistentFlagWOWForUpdate = true;
                            }
                            // Check WOW Deployment Enabled option is inconsistent or not in Common Schedule page for Repair Schedule 
                            if((wowForRepair && !chn.getWOWForRepair()) || (!wowForRepair && chn.getWOWForRepair())) {
                            	consistentFlagWOWForRepair = true;
                            }
                            // Check WOW Deployment Enabled option is inconsistent or not in Common Schedule page for primary schedule
                            if((wowForInit && !chn.getWOWForInit()) || (!wowForInit && chn.getWOWForInit())) {
                            	consistentFlagWOWForInit = true;
                            }
                            // Check WOW Deployment Enabled option is inconsistent or not in Common Schedule page for secondary Schedule 
                            if((wowForSec && !chn.getWOWForSec()) || (!wowForSec && chn.getWOWForSec())) {
                            	consistentFlagWOWForSec = true;
                            }

                            scheduleString=chn.getInitScheduleString();
                            scheduleStringSec=chn.getSecScheduleString();
                            scheduleStringUpd=chn.getUpdateScheduleString();
                            scheduleStringVer= chn.getVerRepairScheduleString();
                            scheduleStringPostpone = chn.getPostponeScheduleString();
                            wowForUpdate = chn.getWOWForUpdate();
                            wowForRepair = chn.getWOWForRepair();
                            wowForInit = chn.getWOWForInit();
                            wowForSec = chn.getWOWForSec();

                            if(consistentFlag)
                            {
                            distbean.setInitSchedule(INCONSISTENT);
                            distbean.setInitScheduleInitValue(INCONSISTENT);
                            }

                            if(consistentFlagSec)
                            {
                            distbean.setSecSchedule(INCONSISTENT);
                            distbean.setSecScheduleInitValue(INCONSISTENT);
                            }

                            if(consistentFlagUpd)
                            {
                            distbean.setUpdateSchedule(INCONSISTENT);
                            distbean.setUpdateScheduleInitValue(INCONSISTENT);
                            }

                            if(consistentFlagVer)
                            {
                            distbean.setVerRepairSchedule(INCONSISTENT);
                            distbean.setVerRepairScheduleInitValue(INCONSISTENT);
                            }

                            if (consistentFlagPostpone) {
                                distbean.setPostponeSchedule(INCONSISTENT);
                                distbean.setPostponeScheduleInitValue(INCONSISTENT);
                            }
							if (consistentFlagWOWForUpdate) {
								distbean.setWowforUpdate(false);
							}
							if (consistentFlagWOWForRepair) {
								distbean.setWowforRepair(false);
							}
							if (consistentFlagWOWForInit) {
								distbean.setWowforInit(false);
							}
							if (consistentFlagWOWForSec) {
								distbean.setWowforSec(false);
							}
                        }
                    }
                }
            } else {
                PersistifyChecksAction.SelectedRecords pkgSel =
                        (PersistifyChecksAction.SelectedRecords)request.getSession().getAttribute(
                                (String)formbean.getValue(SESSION_PERSIST_SELECTED));


                if (pkgSel!=null) {
                    ArrayList targetMapList = new ArrayList(pkgSel.getTargetChannelMaps());
                    Channel chn = null;
                    for (int i=0; i < targetMapList.size(); i++) {
                        chn = (Channel) targetMapList.get(i);

                        if(distbean.getInitSchedule()!=null && (!distbean.getInitSchedule().equals(INCONSISTENT)))
                        chn.setInitSchedule(distbean.getInitSchedule());
                        else if(distbean.getInitSchedule()==null)
                        chn.setInitSchedule(distbean.getInitSchedule());


                        if(distbean.getSecSchedule()!=null && (!distbean.getSecSchedule().equals(INCONSISTENT)))
                        chn.setSecSchedule(distbean.getSecSchedule());
                        else if(distbean.getSecSchedule()==null)
                        chn.setSecSchedule(distbean.getSecSchedule());

                       if(distbean.getUpdateSchedule()!=null && (!distbean.getUpdateSchedule().equals(INCONSISTENT)))
                        chn.setUpdateSchedule(distbean.getUpdateSchedule());
                        else if(distbean.getUpdateSchedule()==null)
                        chn.setUpdateSchedule(distbean.getUpdateSchedule());


                        if(distbean.getVerRepairSchedule()!=null && (!distbean.getVerRepairSchedule().equals(INCONSISTENT)))
                        chn.setVerRepairSchedule(distbean.getVerRepairSchedule());
                        else if(distbean.getVerRepairSchedule()==null)
                        chn.setVerRepairSchedule(distbean.getVerRepairSchedule());

                        if(distbean.getPostponeSchedule()!=null && (!distbean.getPostponeSchedule().equals(INCONSISTENT))) {
                            chn.setPostponeSchedule(distbean.getPostponeSchedule());
                        } else if(distbean.getPostponeSchedule()==null) {
                            chn.setPostponeSchedule(distbean.getPostponeSchedule());
                        }
                        // Set for WOW Deployment in Common Schedule page for primary schedule
                        chn.setWOWForInit(distbean.getWowforInit());
                        // Set for WOW Deployment in Common Schedule page for secondary Schedule
                        chn.setWOWForSec(distbean.getWowforSec());

                        // Set for WOW Deployment in Common Schedule page for update schedule
                        chn.setWOWForUpdate(distbean.getWowforUpdate());
                        // Set for WOW Deployment in Common Schedule page for Repair Schedule
                        chn.setWOWForRepair(distbean.getWowforRepair());

                    }
                }                
           ((DistAsgForm) form).clearPagingVars(request);
                }
	    }
	} catch (Exception se) {
	    throw new GUIException(se);
	} finally {
              }
        return (mapping.findForward("success"));
    }
}
