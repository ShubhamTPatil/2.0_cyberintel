// Copyright 1997-2009, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.objects.Subscription;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscriptionmanager.ObjectManager;
import static com.marimba.apps.subscriptionmanager.approval.IApprovalPolicyConstants.*;
import static com.marimba.apps.subscriptionmanager.intf.IPolicyDiffConstants.KEY_NEW_SUB;
import static com.marimba.apps.subscriptionmanager.intf.IPolicyDiffConstants.KEY_OLD_SUB;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiff;
import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiffLogFormatter;
import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiffMailFormatter;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;
import com.marimba.webapps.intf.CriticalException;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.KnownException;
import com.marimba.webapps.intf.SystemException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * This action is responsible for saving all the changes to the
 * the add targets to LDAP. It reads from the TargetChannelMap lsit and writes to LDAP.
 *
 * @author  Jayaprakash Paramasivam
 * @version 1.0, 05/09/2004
 */

public final class AddTargetSaveAction extends AbstractAction {

    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
        return new  AddTargetSaveTask(mapping, form, request, response);
    }

    protected class AddTargetSaveTask extends SubscriptionDelayedTask {
        AddTargetSaveTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
            super(mapping, form, request, response);
        }


        public void execute(){
            try{
                init(request);
                boolean hasKnownError = false;

                List patchTargets = (ArrayList) session.getAttribute(ADD_REMOVE_PATCH);
                List packageTargets = (ArrayList) session.getAttribute(ADD_REMOVE_PACKAGE);
                String channelSessionVar = MAIN_PAGE_PACKAGE;

                if(patchTargets == null) {
                    patchTargets = new ArrayList(DEF_COLL_SIZE);
                }
                if(packageTargets == null) {
                    packageTargets = new ArrayList(DEF_COLL_SIZE);
                }

                if (request.getSession().getAttribute(SESSION_MULTIPKGBOOL) != null) {
                    channelSessionVar = MAIN_PAGE_M_PKGS;
                }

                List channels = (ArrayList) request.getSession().getAttribute(channelSessionVar);
                if(channels == null) {
                    channels = new ArrayList(DEF_COLL_SIZE);
                }

                List packageChannels = new ArrayList(DEF_COLL_SIZE);
                List patchChannels = new ArrayList(DEF_COLL_SIZE);

                for (Object channel1 : channels) {
                    Channel channel = (Channel) channel1;
                    if (CONTENT_TYPE_PATCHGROUP.equals(channel.getType())) {
                        patchChannels.add(channel);
                    } else {
                        packageChannels.add(channel);
                    }
                }

                try {
                    //save the package targets
                    saveSubscription(request, packageTargets, packageChannels);
                    //save the patch targets
                    saveSubscription(request, patchTargets, patchChannels);
                } catch (Exception e) {
                    if (e instanceof KnownException) {
                        hasKnownError = true;
                    }
                    throw new GUIException(PKG_SAVE_ERROR, e);
                } finally {
                    if (!hasKnownError) {
                        clearSessionVars(request);
                    }
                }

                boolean isPeerPolicyEnabled = main.isPeerApprovalEnabled();
                String forwardStr = main.isPeerApprovalEnabled() ? "success_peer" : "success";
                forward = mapping.findForward(forwardStr);
            } catch (Exception ex) {
                if(ex instanceof GUIException) {
                    guiException = new GUIException(new CriticalException(((GUIException)ex).getMessageKey(),ex.toString()));
                } else if(ex instanceof SystemException) {
                    guiException = new GUIException(new CriticalException(((SystemException)ex).getKey(),ex.toString()));
                } else {
                    guiException = new GUIException(new CriticalException(ex.toString()));
                }
                forward = mapping.findForward("failure");
            }
        }
    }

    protected void checkAndSetChAttributes(Channel subch,
                                           TargetChannelMap tmap)
            throws SystemException {

        if (!INCONSISTENT.equals(tmap.getState())) {
            subch.setState(tmap.getState());
        }

        if (!INCONSISTENT.equals(tmap.getSecState())) {
            subch.setSecState(tmap.getSecState());
        }

        // Sets the schedule into the channel object if the schedules are not inconsistent
        if (!INCONSISTENT.equals(tmap.getInitSchedule())) {
            subch.setInitSchedule(tmap.getInitSchedule());
        }

        if (!INCONSISTENT.equals(tmap.getSecSchedule())) {
            subch.setSecSchedule(tmap.getSecSchedule());
        }

        if((!(subch.getState().equals(STATE_SUBSCRIBE_NOINSTALL) || subch.getState().equals(STATE_AVAILABLE))) && (INCONSISTENT.equals(subch.getState()))) {
            subch.setSecSchedule("");
        }

        if (subch.getSecState() == null) {
            subch.setSecSchedule("");
        }

        if (!INCONSISTENT.equals(tmap.getUpdateSchedule())) {
            subch.setUpdateSchedule(tmap.getUpdateSchedule());
        }

        if (!INCONSISTENT.equals(tmap.getVerRepairSchedule())) {
            subch.setVerRepairSchedule(tmap.getVerRepairSchedule());
        }

        if(tmap.getContentType() != null) {
            subch.setType(tmap.getContentType());
        }

        if("true".equals(tmap.getExemptFromBlackout())) {
            subch.setExemptFromBlackout(true);
        }

        if("true".equalsIgnoreCase(tmap.getWowEnabled())) {
            subch.setWowEnabled(true);
        }

        if("true".equalsIgnoreCase(tmap.getWowForInit())) {
            subch.setWOWForInit(true);
        }
        if("true".equalsIgnoreCase(tmap.getWowForSec())) {
            subch.setWOWForSec(true);
        }

        if("true".equalsIgnoreCase(tmap.getWowForUpdate())) {
            subch.setWOWForUpdate(true);
        }

        if("true".equalsIgnoreCase(tmap.getWowForRepair())) {
            subch.setWOWForRepair(true);
        }


    }

    /**
     * Subscription transactional save function ,if there is a problem with one of the
     * subscriptions for the target, save out the old ones for the previous.
     *
     * @param request request object to log the channel details
     * @param subscriptions vector containing subscription to save
     */
    protected void transactSave(HttpServletRequest request,
                                Map<String, Map<String, ISubscription>> subscriptions,
                                List tchmap)
            throws SystemException {
        int size = subscriptions.size();

        Channel curCh;
        String modifiedUserDN = null;
        IUser user = GUIUtils.getUser(request);
        List<PolicyDiff> policyDiffs = new ArrayList<PolicyDiff>();
        String isMultiTgt = (String)request.getSession().getAttribute(SESSION_MULTITGBOOL);

        boolean isPeerPolicyEnabled = main.isPeerApprovalEnabled();
        try {
            modifiedUserDN = main.resolveUserDN(user.getName());
        } catch (Exception e) {
            if (DEBUG) e.printStackTrace();
        }
        for (String targetId : subscriptions.keySet()) {
            Map<String, ISubscription> tmpMap = subscriptions.get(targetId);
            ISubscription sub = tmpMap.get(KEY_NEW_SUB);
            ISubscription oldSub = tmpMap.get(KEY_OLD_SUB);

            if (!isPeerPolicyEnabled) {
                sub.save();
            }
            // We iterate through all of the channels for each subscription because
            // in the case of inconsistent states, they may differ.
            // We do this AFTER it is saved, so that we can be sure there are no failures
            for (int j = 0; j < tchmap.size(); j++) {
                TargetChannelMap tempmap = (TargetChannelMap) tchmap.get(j);
                curCh = sub.getChannel(tempmap.getChannel().getUrl());

                if(curCh!=null )
                {
                    GUIUtils.log(servlet, request, LOG_SUBSCRIPTION_PKGMOD, curCh.getUrl() + "="+ j + curCh.getState()
                            + "," + curCh.getSecState(), sub.getTargetID(), "(" + curCh.getInitScheduleString()
                            + ")(" + curCh.getSecScheduleString() + ")(" + curCh.getUpdateScheduleString()
                            + ")(" + curCh.getVerRepairScheduleString() + ")(" + curCh.getPostponeScheduleString() + ")");
                }

            }
            if(main.isPeerApprovalEnabled()) {
                PolicyDiffLogFormatter logDiff = new PolicyDiffLogFormatter(oldSub, sub, main);
                logDiff.prepareDiff();

                PolicyDiff policyDiff = new PolicyDiff(oldSub, sub, main);
                boolean hasPolicyExists = ObjectManager.existsSubscription(oldSub.getTargetID(), oldSub.getTargetType(), user);
                if(hasPolicyExists) {
                    if (DEBUG) System.out.println("Old subscription is already exists");
                    policyDiff.setPolicyAction(MODIFY_OPERATION);
                } else {
                    policyDiff.setPolicyAction(ADD_OPERATION);
                }
                if(null != modifiedUserDN) {
                    policyDiff.setUser(modifiedUserDN);
                }
                // set default policy status as pending
                policyDiff.setPolicyStatus(POLICY_PENDING);
                if(policyDiff.hasDiff()) {
                    policyDiffs.add(policyDiff);
                }
            }
        }
        if(main.isPeerApprovalEnabled()) {
            if(policyDiffs.size() > 0) {
                main.storeApprovalPolicy2DB(policyDiffs);
                main.setPolicyChange(true);
            } else {
                main.setPolicyChange(false);
            }
        }
        if(main.isEmailFeatureEnabled() && policyDiffs.size() > 0) {
            PolicyDiffMailFormatter mailFormatter = new PolicyDiffMailFormatter(policyDiffs, resources);
            String userName = main.getDisplayName(modifiedUserDN);
            mailFormatter.setCreatedByDispName((null == userName || userName.trim().isEmpty()) ? user.getName() : userName);
            mailFormatter.setMultiMode("true".equalsIgnoreCase(isMultiTgt));
            mailFormatter.setPeerApprovalEnabled(main.isPeerApprovalEnabled());
            mailFormatter.prepare();
            main.sendMail(mailFormatter);
        }
    }

    private void saveSubscription(HttpServletRequest request, List targets, List Channels)
            throws SystemException {
        Map<String, Map<String, ISubscription>> subsMap = new LinkedHashMap<String, Map<String, ISubscription>>(10);

        // This sets aside the subscriptions that are
        // to be saved for each target.
        // We do not save them along the way because an
        // error could occur on one of them.
        // Create a subscription object if it doesn't exist
        // Loads the subscription if it already exists
        for (int i = 0; i < targets.size(); i++) {
            TargetChannelMap tcmap = (TargetChannelMap) targets.get(i);
            Target tgt =  tcmap.getTarget();

            if (DEBUG) {
                System.out.println("Setting channels for " + tgt);
            }
            ISubscription sub = ObjectManager.openSubForWrite(tgt.getId(), tgt.getType(), GUIUtils.getUser(request));
            Subscription oldSub = new Subscription((Subscription) sub);
            main.logAuditInfo(LOG_AUDIT_SUB_OBJ_CREATED, LOG_AUDIT, "vDesk", tgt.getID(), request, POLICY_CREATE);

            for(int j=0;j< Channels.size();j++) {
                Channel subch = (Channel) Channels.get(j);
                Channel tcMapChannel = new Channel(subch.getUrl(), subch.getTitle(),  subch.getState(), subch.getSecState(), subch.getOrder(), subch.getPolicyName(), subch.getPolicyType());;
                checkAndSetChAttributes(tcMapChannel, tcmap);
                if (DEBUG) {
                    System.out.println("Selected Channel " + subch.hashCode() + ": "
                            + subch.getUrl() + ", " + subch.getState()
                            + ", secState: " + subch.getSecState()
                            + ", initSched: " + subch.getInitSchedule()
                            + ", secSched: " + subch.getSecSchedule()
                            + ", verrepairSched: " + subch.getUpdateSchedule()
                            + ", updateSched: " + subch.getUpdateSchedule()
                            + ", order: " + subch.getOrder());
                }

                if (DEBUG) {
                    System.out.println("Subscription Channel " + tcMapChannel.hashCode() + ": "
                            + subch.getUrl() + ", " + tcMapChannel.getState()
                            + ", secState: " + tcMapChannel.getSecState()
                            + ", initSched: " + tcMapChannel.getInitSchedule()
                            + ", secSched: " + tcMapChannel.getSecSchedule()
                            + ", verrepairSched: " + tcMapChannel.getUpdateSchedule()
                            + ", updateSched: " + tcMapChannel.getUpdateSchedule()
                            + ", order: " + tcMapChannel.getOrder());
                }
                sub.setChannel(tcMapChannel);

                Map<String, ISubscription> subMap = new HashMap<String, ISubscription>(2);
                subMap.put(KEY_NEW_SUB, sub);
                subMap.put(KEY_OLD_SUB, oldSub);
                subsMap.put(tgt.getId(), subMap);
            }
        }

        // The subscription are saved outside of the loop because and error could occur during the save
        // of one of them and we do not want to save any of them if there is a problem.
        transactSave(request, subsMap, targets);
    }

    private void clearSessionVars(HttpServletRequest request) {
        request.getSession().removeAttribute(SESSION_ADD_PAGEPKGS_SELECTED);
        request.getSession().removeAttribute(SESSION_ADD_PAGEPATCH_SELECTED);
        request.getSession().removeAttribute(IWebAppConstants.SESSION_ADD_PAGEPKGS_BEAN);
        request.getSession().removeAttribute(IWebAppConstants.SESSION_ADD_PAGEPATCH_BEAN);
        request.getSession().removeAttribute(SESSION_PERSIST_RESETRESULTS);
        request.getSession().removeAttribute("add_common_target");
        request.getSession().removeAttribute("add_remove_package");
        request.getSession().removeAttribute("add_selected_list");
        request.getSession().removeAttribute("context");
        request.getSession().removeAttribute("add_remove_target");
        request.getSession().removeAttribute("add_remove_patch");
    }
}

