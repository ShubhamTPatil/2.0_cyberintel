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
import org.apache.struts.util.MessageResources;

import java.io.IOException;

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.ISubscriptionConstants;

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean;

import com.marimba.apps.subscriptionmanager.webapp.forms.DistAsgForm;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.KnownException;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.tools.action.DelayedAction;

/**
 * Saves the changes in the Distribution assignment form to the DistributionBean
 * The primary and secondary states select boxes are identified as
 * state#&lt;channel object hashcode> and secState#&lt;channel object hashcode>
 *
 * @author Theen-Theen Tan
 * @author Sunil Ramakrishnan
 */
public final class DistAsgSaveAction extends AbstractAction
        implements IWebAppConstants {

    protected DelayedAction.Task createTask( ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response ) {
        return new  DistAsgSaveAction.DistAsgSveTask( mapping, form, request, response );
    }
    protected class DistAsgSveTask
            extends SubscriptionDelayedTask  {
        DistAsgSveTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
            super(mapping, form, request, response);
        }
        public void execute()
        {
            try {
                if( ((DistAsgForm) form).getValue("hasLoaded") != null ) {
                    ((DistAsgForm) form).setValue("hasLoaded", null);
                    // save the changes to the DistributionBean
                    saveState(request, (DistAsgForm) form);
                }
            } catch(GUIException ge) {
                guiException = ge;
            }
            // and forward to the next action

            session.removeAttribute(TXLIST_CURRENT_USERNAME);
            session.removeAttribute(TXLIST_CURRENT_PASSWORD);
            session.removeAttribute(PAGE_PKGS_DEP_SEARCH);
            String fwd = ((DistAsgForm) form).getForward();
            session.removeAttribute(PAGE_PKGS_DEP_SEARCH);
            forward = getReturnPage(fwd, request);
        }
    }
    private ActionForward getReturnPage(String forward, HttpServletRequest request) {
        // strip out /sm if it exists
        String context = request.getContextPath();

        if (forward.startsWith(context)) {
            return new ActionForward(forward.substring(context.length()));
        }

        return new ActionForward(forward);
    }

    /**
     * REMIND
     *
     * @param form REMIND
     * @param distributionBean REMIND
     */
    public static void setSchedules(DistAsgForm      form,
                                    DistributionBean distributionBean) {
        String initSchedInc = (String) form.getValue("maintainInitSchedInc");
        String secSchedInc = (String) form.getValue("maintainSecSchedInc");
        String updateSchedInc = (String) form.getValue("maintainUpdateSchedInc");
        String verRepairSchedInc = (String) form.getValue("maintainVerRepairSchedInc");

        if (DEBUG) {
            System.out.println("DistAsgSetStatesAction: set Schedules called - initSchedInc = " + initSchedInc);
            System.out.println("DistAsgSetStatesAction: set Schedules called - secSchedInc = " + secSchedInc);
            System.out.println("DistAsgSetStatesAction: set Schedules called - updateSchedInc = " + updateSchedInc);
            System.out.println("DistAsgSetStatesAction: set Schedules called - updateSchedInc = " + verRepairSchedInc);
        }

        if ((initSchedInc != null) && "true".equals(initSchedInc)) {
            if (DEBUG) {
                System.out.println("DistAsgSetStatesAction: init is inconsistent");
            }
            distributionBean.setInitSchedule(INCONSISTENT);
        }

        if ((secSchedInc != null) && "true".equals(secSchedInc)) {
            distributionBean.setSecSchedule(INCONSISTENT);
        }

        if ((updateSchedInc != null) && "true".equals(updateSchedInc)) {
            distributionBean.setUpdateSchedule(INCONSISTENT);
        }

        if ((verRepairSchedInc != null) && "true".equals(verRepairSchedInc)) {
            distributionBean.setVerRepairSchedule(INCONSISTENT);
        }
    }

    /**
     * If this method is called through the distSetStates action as an intermediate step to the add targets,
     * add packages, edit schedule package, we also want to keep track of whether there are any secondary states set.
     * The session variable hasSecStates is used by schedule_active.jsp to decide whether to show the radio buttons
     * for activation sections.
     *
     * @param request REMIND
     * @param form REMIND
     *
     */
    public void saveState(HttpServletRequest request, DistAsgForm form) throws GUIException{

        DistributionBean distributionBean = getDistributionBean(request);
        String fwd = ((DistAsgForm) form).getForward();
        String action = fwd.substring( fwd.indexOf('=') + 1);
        if (distributionBean != null) {
            if (distributionBean.getChannels() != null) {
                Channel curapp;

                if (request != null) {
                    request.getSession().removeAttribute("hasSecStates");
                }

                GenericPagingBean pageBean = (GenericPagingBean) request.getSession().getAttribute((String) form.getValue(SESSION_PERSIST_BEANNAME));
                ArrayList         tglist = (ArrayList) pageBean.getResults();
                int    startIdx = pageBean.getStartIndex();
                int    endIdx = pageBean.getEndIndex();

                String tgtMulti = (String)request.getSession().getAttribute( SESSION_MULTITGBOOL);
                String pkgMulti = (String)request.getSession().getAttribute("multi_trgts_pkg");
                boolean isMultiTgt = "true".equals(tgtMulti) || "true".equals(pkgMulti);

                for (int i = startIdx; i < endIdx; i++) {

                    curapp = (Channel) tglist.get(i);

                    /* Since the channels can be left as inconsistent, we need the ability to
                     * set this value to the channels in the assignment.
                     */
                    if (CONTENT_TYPE_PATCHGROUP.equals(curapp.getType())){
                        //ignore patch groups
                        continue;
                    }
                    String stateInc = (String) form.getValue("stateInc#" + curapp.hashCode());
                    String secStateInc = (String) form.getValue("secStateInc#" + curapp.hashCode());
                    String changeOrderInc = (String) form.getValue("changeOrderInc#" + curapp.hashCode());

                    if (DEBUG) {
                        System.out.println("DisAsgSetStatesAction: stateInc# = " + stateInc);
                        System.out.println("DisAsgSetStatesAction: secStateInc# = " + secStateInc);
                        System.out.println("DisAsgSetStatesAction: changeOrderInc# = " + changeOrderInc);
                    }

                    if ("true".equals(stateInc)) {
                        curapp.setState(INCONSISTENT);
                    } else {
                        String state = (String) form.getValue("state#" + curapp.hashCode());
                        if (state != null) {
                            // If we haven't paged, the widgets will not be created
                            // therefore, the values for the states will be null.
                            // In that case, we use the default state (subscribe) that was set
                            // during the channel creation time in DistributionBean.setChannel
                            curapp.setState((String) form.getValue("state#" + curapp.hashCode()));
                        }
                    }

                    if ("true".equals(secStateInc)) {
                        curapp.setSecState(INCONSISTENT);
                    } else {
                        curapp.setSecState((String) form.getValue("secState#" + curapp.hashCode()));
                    }

                    if ((curapp.getSecState() != null) && (request != null)) {
                        request.getSession()
                                .setAttribute("hasSecStates", "true");
                    }
                    // Set blackout exemption
                    String paramstr = (String) form.getValue("exemptBo#" + curapp.hashCode());
                    if ((paramstr != null) && ("true".equals(paramstr))) {
                        curapp.setExemptFromBlackout(true);
                    } else {
                        curapp.setExemptFromBlackout(false);
                    }

                    // Set wow deployment
                    paramstr = (String) form.getValue("wowDep#" + curapp.hashCode());
                    if ((paramstr != null) && ("true".equals(paramstr))) {
                        curapp.setWowEnabled(true);
                    } else {
                        curapp.setWowEnabled(false);
                    }

                    if ("true".equals(changeOrderInc) || "true".equals(pkgMulti)){
                        curapp.setOrderState(INCONSISTENT);
                    } else {
                        curapp.setOrderState("");
                        String orderOriginalStr = (String)form.getValue("changeorderhdn#" + curapp.hashCode());
                        String changedOrderStr = (String)form.getValue("changeorder#" + curapp.hashCode());
                        int originalOrder = 0;
                        int changedOrder = 0;

                        originalOrder = Integer.parseInt(orderOriginalStr);

                        if(changedOrderStr == null || changedOrderStr.length() == 0 ) {
                            if( originalOrder != ISubscriptionConstants.MAX_INSTALL_PRIORITY ) {
                                distributionBean.setNewPriority(curapp.getUrl(), new Channel(curapp.getUrl(), curapp.getState(), ISubscriptionConstants.MAX_INSTALL_PRIORITY));
                            }
                        } else {
                            try {
                                changedOrder = Integer.parseInt(changedOrderStr);
                                if(changedOrder < 1 || changedOrder  >= ISubscriptionConstants.MAX_INSTALL_PRIORITY) {
                                    if(ACTION_MODIFY_EXISTING_PRIORITY.equals(action)) {
                                        throw new GUIException(new KnownException(IErrorConstants.ASSIGN_PRIORITY_EXCEED));
                                    }
                                }
                            } catch(NumberFormatException ne) {
                                changedOrder = ISubscriptionConstants.MAX_INSTALL_PRIORITY;
                            }
                            if(originalOrder != changedOrder) {
                                if(isMultiTgt){
                                    HashSet<Integer> usedPriorities=(HashSet<Integer>)request.getSession().getAttribute("usedPriorities");
                                    if(usedPriorities.contains(changedOrder)){
                                        throw new GUIException(new KnownException(IErrorConstants.PRIORITY_ALREADY_EXIST,changedOrderStr,usedPriorities.toString()));
                                    }
                                }
                                distributionBean.setNewPriority(curapp.getUrl(), new Channel(curapp.getUrl(), curapp.getState(), changedOrder));
                            }
                        }
                    }
                }
            }
            // set the starting priority
            distributionBean.setStartingPriority(form.getStartingPriority());
        }
    }
}
