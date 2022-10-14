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

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.ISubscriptionConstants;

import com.marimba.apps.subscriptionmanager.webapp.forms.AddTargetEditForm;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelComparator;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IMapProperty;

/**
 * Handles saving schedules for a set of selected packages in the
 * add target details section in the Package View Page
 *
 * @author Jayaprakash Paramasivam
 * @version 1.0, 05/09/2005
 */
public final class AddTargetSchedCommonSaveAction
    extends AbstractAction {
    static TargetChannelComparator comp = new TargetChannelComparator(ISubscriptionConstants.TARGET_DIRECTLYASSIGNED_KEY);
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
	
        HttpSession session = request.getSession();
	IMapProperty formbean = (IMapProperty) form;
        DistributionBean tempbean = new DistributionBean();


      String action=null;
      boolean consistentFlag=false;
      boolean consistentFlagSec=false;
      boolean consistentFlagUpd=false;
      boolean consistentFlagVer=false;
      boolean consistentFlagPost = false;
      boolean consistentFlagWOWForInit = false;
      boolean consistentFlagWOWForSec = false;
      boolean consistentFlagWOWForUpdate = false;
      boolean consistentFlagWOWForRepair = false;

      String scheduleString=null;
      String scheduleStringSec=null;
      String scheduleStringUpd=null;
      String scheduleStringVer=null;
      String scheduleStringPost = null;
      boolean wowForUpdate = false;
      boolean wowForRepair = false;
      boolean wowForInit = false;
      boolean wowForSec = false;

	try {
	     action= request.getParameter("action");
	    if ("cancel".equals(action)) {
            ((AddTargetEditForm) form).clearPagingVars(request);
             request.getSession().removeAttribute("add_common_target");
             request.getSession().removeAttribute("add_selected_list");
        } else {
            if ("edit".equals(action)) {
                PersistifyChecksAction.SelectedRecords tgSel =
                        (PersistifyChecksAction.SelectedRecords)session.getAttribute(
                                (String)formbean.getValue(SESSION_PERSIST_SELECTED));


                if (tgSel!=null) {

                    ArrayList targetMapList = new ArrayList(tgSel.getTargetChannelMaps());
                    Collections.sort(targetMapList,comp);
                    Channel chn = null;
                    TargetChannelMap tchmap = null;
                    for (int i=0; i < targetMapList.size(); i++) {
                        tchmap = (TargetChannelMap) targetMapList.get(i);
                        //to set common schedule
                        chn = tchmap.getChannel();

                        if((!chn.getInconsistentStates()) && (!chn.getInconsistentSched()))
                        {
                        tempbean.setInitSchedule(chn.getInitScheduleString());
                        tempbean.setSecSchedule(chn.getSecScheduleString());
                        tempbean.setUpdateSchedule(chn.getUpdateScheduleString());
                        tempbean.setVerRepairSchedule(chn.getVerRepairScheduleString());
                        tempbean.setPostponeSchedule(chn.getPostponeScheduleString());
                        tempbean.setWowforInit(chn.getWOWForInit());
                        tempbean.setWowforSec(chn.getWOWForSec());
                        tempbean.setWowforUpdate(chn.getWOWForUpdate());
                        tempbean.setWowforRepair(chn.getWOWForRepair());
                        }


                        if(chn.getInconsistentStates() || chn.getInconsistentSched())
                        consistentFlag=true;

                        if(i==0)
                        {
                        scheduleString=chn.getInitScheduleString();
                        scheduleStringSec= chn.getSecScheduleString();
                        scheduleStringUpd=chn.getUpdateScheduleString();
                        scheduleStringVer= chn.getVerRepairScheduleString();
                        scheduleStringPost= chn.getPostponeScheduleString();
                        wowForInit = chn.getWOWForInit();
                        wowForSec = chn.getWOWForSec();
                        wowForUpdate = chn.getWOWForUpdate();
                        wowForRepair = chn.getWOWForRepair();
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

                            if(scheduleStringPost == null && chn.getPostponeScheduleString()!=null) {
                                consistentFlagPost=true;
                            } else if(scheduleStringPost==null && chn.getPostponeScheduleString()==null) {
                                //Do nothing
                            }
                            else if(!scheduleStringPost.equals(chn.getPostponeScheduleString())) {
                                consistentFlagPost = true;
                            }
                            // Check WOW Deployment Enabled option is inconsistent or not in Common Schedule page for init schedule
                            if((wowForInit && !chn.getWOWForInit()) || (!wowForInit && chn.getWOWForInit())) {
                            	consistentFlagWOWForInit = true;
                            }

                            // Check WOW Deployment Enabled option is inconsistent or not in Common Schedule page for secondary schedule
                            if((wowForSec && !chn.getWOWForSec()) || (!wowForSec && chn.getWOWForSec())) {
                            	consistentFlagWOWForSec = true;
                            }

                            // Check WOW Deployment Enabled option is inconsistent or not in Common Schedule page for update schedule
                            if((wowForUpdate && !chn.getWOWForUpdate()) || (!wowForUpdate && chn.getWOWForUpdate())) {
                            	consistentFlagWOWForUpdate = true;
                            }
                            
                            // Check WOW Deployment Enabled option is inconsistent or not in Common Schedule page for Repair Schedule
                            if((wowForRepair && !chn.getWOWForRepair()) || (!wowForRepair && chn.getWOWForRepair())) {
                            	consistentFlagWOWForRepair = true;
                            }

                            scheduleString=chn.getInitScheduleString();
                            scheduleStringSec=chn.getSecScheduleString();
                            scheduleStringUpd=chn.getUpdateScheduleString();
                            scheduleStringVer= chn.getVerRepairScheduleString();
                            scheduleStringPost = chn.getPostponeScheduleString();
                            wowForInit = chn.getWOWForInit();
                            wowForSec = chn.getWOWForSec();
                            wowForUpdate = chn.getWOWForUpdate();
                            wowForRepair = chn.getWOWForRepair();

                            if(consistentFlag)
                            {
                            tempbean.setInitSchedule(INCONSISTENT);
                            }

                            if(consistentFlagSec)
                            {
                            tempbean.setSecSchedule(INCONSISTENT);
                            }

                            if(consistentFlagUpd)
                            {
                            tempbean.setUpdateSchedule(INCONSISTENT);
                            }

                            if(consistentFlagVer)
                            {
                            tempbean.setVerRepairSchedule(INCONSISTENT);
                            }

                            if(consistentFlagPost)
                            {
                            tempbean.setPostponeSchedule(INCONSISTENT);
                            }
							if (consistentFlagWOWForUpdate) {
								tempbean.setWowforUpdate(false);
							}
							if (consistentFlagWOWForRepair) {
								tempbean.setWowforRepair(false);
							}
							if (consistentFlagWOWForInit) {
								tempbean.setWowforInit(false);
							}
							if (consistentFlagWOWForSec) {
								tempbean.setWowforSec(false);
							}

                        }
                    }
                    GUIUtils.setToSession(request, "add_selected_list", targetMapList);
                    GUIUtils.setToSession(request, "add_common_target", (Object) tempbean);
                }
            } else {
                PersistifyChecksAction.SelectedRecords tgSel =
                        (PersistifyChecksAction.SelectedRecords)session.getAttribute(
                                (String)formbean.getValue(SESSION_PERSIST_SELECTED));

                DistributionBean schBean = (DistributionBean) request.getSession().getAttribute((String)"add_common_target");

                if (tgSel!=null) {
                    ArrayList tchmapList = new ArrayList(tgSel.getTargetChannelMaps());
                    TargetChannelMap tmap = null;
                    Channel ch = null;
                    for (int i=0; i < tchmapList.size(); i++) {
                        tmap = (TargetChannelMap) tchmapList.get(i);
                        ch = tmap.getChannel();

                        if(schBean.getInitSchedule()!=null && (!schBean.getInitSchedule().equals(INCONSISTENT))) {
                            ch.setInitSchedule(schBean.getInitSchedule());
                        }
                        if(schBean.getSecSchedule() !=null && (!schBean.getSecSchedule().equals(INCONSISTENT))) {
                            ch.setSecSchedule(schBean.getSecSchedule());
                        }
                        if(schBean.getUpdateSchedule() !=null && (!schBean.getUpdateSchedule().equals(INCONSISTENT))) {
                            ch.setUpdateSchedule(schBean.getUpdateSchedule());
                        }
                        if(schBean.getVerRepairSchedule() !=null && (!schBean.getVerRepairSchedule().equals(INCONSISTENT))) {
                            ch.setVerRepairSchedule(schBean.getVerRepairSchedule());
                        }

                        if(schBean.getPostponeSchedule() !=null && (!schBean.getPostponeSchedule().equals(INCONSISTENT))) {
                            ch.setPostponeSchedule(schBean.getPostponeSchedule());
                        }
                        // Set for WOW Deployment in Common Schedule page
                        ch.setWOWForInit(schBean.getWowforInit());
                        ch.setWOWForSec(schBean.getWowforSec());
                        ch.setWOWForUpdate(schBean.getWowforUpdate());
                        ch.setWOWForRepair(schBean.getWowforRepair());
                        if(DEBUG) {
                            System.out.println("Channel Selected Name : "+ ch.getTitle());
                            System.out.println("Target Selected Name : "+ tmap.getTarget().getId());
                            System.out.println("Channel Primary Schedule : "+ ch.getInitScheduleString());
                            System.out.println("Channel Secondary Schedule : "+ ch.getSecScheduleString());
                            System.out.println("Channel Update Schedule : "+ ch.getUpdateScheduleString());
                            System.out.println("Channel Verify/Repair Schedule : "+ ch.getVerRepairScheduleString());
                            System.out.println("Channel Postpone Schedule : "+ ch.getPostponeScheduleString());
                            System.out.println("Channel WOW Deployment for primary Schedule : "+ ch.getWOWForInit());
                            System.out.println("Channel WOW Deployment for secondary Schedule : "+ ch.getWOWForSec());
                            System.out.println("Channel WOW Deployment for Update Schedule : "+ ch.getWOWForUpdate());
                            System.out.println("Channel WOW Deployment for Repair Schedule : "+ ch.getWOWForRepair());
                        }
                    }
                }
            ((AddTargetEditForm)form).clearPagingVars(request);
            }
	    }
	} catch (Exception se) {
	    throw new GUIException(se);
	} finally {
              }
        return (mapping.findForward("success"));
    }
}
