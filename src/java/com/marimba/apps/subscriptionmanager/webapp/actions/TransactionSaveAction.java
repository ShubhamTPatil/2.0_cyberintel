// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.objects.Subscription;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscriptionmanager.ObjectManager;
import static com.marimba.apps.subscriptionmanager.approval.IApprovalPolicyConstants.*;
import static com.marimba.apps.subscriptionmanager.arsystem.ARTaskLogConstants.LOG_AR_POLICY_CREATION_FAILED;
import static com.marimba.apps.subscriptionmanager.arsystem.ARTaskLogConstants.LOG_AR_POLICY_CREATION_SUCCESS;
import com.marimba.apps.subscriptionmanager.arsystem.ARWorklog;
import static com.marimba.apps.subscriptionmanager.intf.IARTaskConstants.*;
import static com.marimba.apps.subscriptionmanager.intf.IPolicyDiffConstants.KEY_NEW_SUB;
import static com.marimba.apps.subscriptionmanager.intf.IPolicyDiffConstants.KEY_OLD_SUB;
import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiff;
import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiffLogFormatter;
import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiffMailFormatter;
import com.marimba.apps.subscriptionmanager.webapp.forms.DistAsgForm;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.SaveTunerChPropsHelper;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;
import com.marimba.intf.msf.arsys.IARConstants;
import com.marimba.intf.util.IConfig;
import com.marimba.webapps.intf.CriticalException;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.KnownException;
import com.marimba.webapps.intf.SystemException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * This action is responsible for saving all the changes to the
 * the distribution assignment to LDAP. It reads from the
 * DistributionBean and writes to LDAP.
 *
 *
 * @author Theen-Theen Tan
 * @author Angela Saval
 * @author Sunil Ramakrishnan
 */

public final class TransactionSaveAction extends AbstractAction {
    IUser user;
    String modifiedUserDN = null;
    IConfig subConfig;
    String isMultiPkg = "false";
    String isMultiTgt = "false";

    ArrayList<StringBuffer> oldPolicy;

    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
        return new  TransactSaveTask(mapping, form, request, response);
    }

    protected class TransactSaveTask extends SubscriptionDelayedTask {
        TransactSaveTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
            super(mapping, form, request, response);
        }

        public void execute(){
            try {
                if (DEBUG) {
                    System.out.println("TransactionSaveAction: execute called");
                }
                init(request);
                boolean hasKnownError = false;
                user = (IUser) session.getAttribute(SESSION_SMUSER);
                isMultiPkg = (String)session.getAttribute(SESSION_MULTIPKGBOOL);
                isMultiTgt = (String)session.getAttribute(SESSION_MULTITGBOOL);
                DistributionBean distributionBean = getDistributionBean(request);
                ArrayList targets = distributionBean.getTargets();
                ArrayList channels = distributionBean.getChannels();
                subConfig = main.getConfig();

                DistAsgForm formbean = (DistAsgForm) form;
                String arString = null;
                String taskId = formbean.getTaskid();
                String changeId = formbean.getChangeid();
                if(taskId != null && taskId.length() > 0 ) {
                    arString = taskId + ((changeId != null) && (changeId.length() > 0) ? '|' + changeId : "");
                }
                oldPolicy = new ArrayList<StringBuffer>(DEF_COLL_SIZE);
                try {

                    // This sets aside the subscriptions that are
                    // to be saved for each target.
                    // We do not save them along the way because an
                    // error could occur on one of them.
                    Vector<ISubscription> subscriptions = new Vector<ISubscription>(5);
                    Map<String, Map<String, ISubscription>> subsMap = new LinkedHashMap<String, Map<String, ISubscription>>(10);
                    try {
                        modifiedUserDN = main.resolveUserDN(user.getName());
                    } catch (Exception e) {
                        if (DEBUG) e.printStackTrace();
                    }
                    // Create a subscription object if it doesn't exist
                    // Loads the subscription if it already exists
                    for (int i = 0; i < targets.size(); i++) {
                        Target tgt = (Target) targets.get(i);
                        Map<String, ISubscription> subMap = new HashMap<String, ISubscription>(2);
                        if (DEBUG) {
                            System.out.println("Setting channels for target: " + tgt.getId());
                        }

                        ISubscription sub = ObjectManager.openSubForWrite(tgt.getId(), tgt.getType(), GUIUtils.getUser(request));
                        ISubscription oldSub = new Subscription((Subscription) sub);
                        main.logAuditInfo(LOG_AUDIT_SUB_OBJ_CREATED, LOG_AUDIT, "vDesk", tgt.getID(), request, POLICY_CREATE);

                        if(session.getAttribute(AR_TASK_ID) != null) {
                            if(oldPolicy != null && oldPolicy.size() > 0) {
                                ArrayList<StringBuffer> list = getOldPolicy(sub, request);
                                oldPolicy.addAll(list);
                            } else {
                                oldPolicy = new ArrayList<StringBuffer>(DEF_COLL_SIZE);
                                oldPolicy = getOldPolicy(sub, request);
                            }
                        }

                        subscriptions.addElement(sub);

                        // now remove all the deleted channels from the Subscription object
                        HttpSession session = request.getSession();
                        boolean isFromPkgView = session.getAttribute(IS_FROM_PKG_VIEW) != null;
                        if (!isFromPkgView) {
                            //removeDeletedChannels(session, distributionBean, sub);
                        }
                        // We support saving of blackout, policy schedule, tuner props/Tx login only
                        // if one target is selected.
                        if (targets.size() == 1 && session.getAttribute(SESSION_MULTITGBOOL) == null) {
                            // save the tx and tuner/channel props
                            SaveTunerChPropsHelper tPropHelper = new SaveTunerChPropsHelper();
                            tPropHelper.setTunerChProps(sub, request);

                        }
                        subMap.put(KEY_NEW_SUB, sub);
                        subMap.put(KEY_OLD_SUB, oldSub);
                        subsMap.put(tgt.getId(), subMap);
                    }

                    // The subscription are saved outside of the loop because and error could occur during the save
                    // of one of them and we do not want to save any of them if there is a problem.
                    transactSave(request, channels, subsMap);
                } catch (Exception e) {
                    if (DEBUG) {
                        e.printStackTrace();
                    }
                    ARWorklog workLog = null;
                    if(session.getAttribute(AR_TASK_ID) != null) {
                        HashMap hm = new HashMap();
                        hm.put(AR_TASK_ID, new String[] {(String)session.getAttribute(AR_TASK_ID)});
                        hm.put(AR_USER, getString(locale, "ar.worklog.modifieduser")+user.getName());
                        session.removeAttribute(AR_CHANGE_ID);
                        workLog = new ARWorklog(hm, tenant);
                        workLog.write(getString(locale, "ar.worklog.saveexception"));
                        workLog.close(hm, IARConstants.TMS_RETURN_CODE_ERROR, getString(request.getLocale(), "ar.worklog.policyfailed"));
                        GUIUtils.log(servlet, request, LOG_AR_POLICY_CREATION_FAILED, e.getMessage());
                    }

                    if (e instanceof KnownException) {
                        hasKnownError = true;
                        throw new GUIException(((KnownException)e).getKey(), ((KnownException)e).getRootCause());
                    } else {
                        throw new GUIException(PKG_SAVE_ERROR, e);
                    }
                } finally {

                    if (!hasKnownError) {
                        removeMiscChannels(distributionBean, channels);
                        removeDistributionBean(request);
                        //
                        request.getSession().removeAttribute(PAGE_TCHPROPS_CHANNELS);
                        request.getSession().removeAttribute(PAGE_TCHPROPS_SUB);
                        request.getSession().removeAttribute(PAGE_TCHPROPS_SUB_COPY);
                        //
                        updateSessionVar(request, channels, targets);
                        // Clear the check boxes and paging objects
                        formbean.clearPagingVars(request);
                        session.removeAttribute(SESSION_AR_TASKID_ENABLED);
                    }
                }


                forward= mapping.findForward( ( (session.getAttribute(AR_TASK_ID) == null) )? "confirmsave":"confirmarsave");
            } catch (Exception ex) {
                if(ex instanceof GUIException) {
                    guiException = new GUIException(new CriticalException(ex, ((GUIException)ex).getMessageKey()));
                } else if(ex instanceof SystemException) {
                    guiException = new GUIException(new CriticalException(ex, ((SystemException)ex).getKey()));
                } else {
                    guiException = new GUIException(new CriticalException(ex.toString()));
                }
                forward = mapping.findForward("failure");
            }
        }
    }
    private void removeDeletedChannels(HttpSession session, DistributionBean distributionBean, ISubscription sub) {
        //ISubscription sub = ...;
        Hashtable channelsTable = distributionBean.getChannelsTable();
        ArrayList targets = distributionBean.getTargets();
        // create a hashtable of the channels that form the intersection,
        // this is later used to delete the channels
        Hashtable<String, Channel> origChannels = new Hashtable<String, Channel>();
        ArrayList targetMapList = (ArrayList) session.getAttribute(SESSION_PKGS_FROMTGS_RS);
        if (targetMapList != null) {
            Channel tempch;
            for (Object aTargetMapList : targetMapList) {
                TargetChannelMap tempmap = (TargetChannelMap) aTargetMapList;
                tempch = tempmap.getChannel();
                origChannels.put(tempch.getUrl(), tempch);
            }
        }
        for (Enumeration e = sub.getChannels(); e.hasMoreElements();) {
            Channel subscriptionChannel = (Channel) e.nextElement();
            if (channelsTable.get(subscriptionChannel.getUrl()) == null) {
                if (targets.size() == 1 && session.getAttribute(SESSION_MULTITGBOOL) == null) {
                    sub.removeChannel(subscriptionChannel.getUrl());
                } else {
                    // in multi mode, only delete channles that were part of the intersection
                    if (origChannels.get(subscriptionChannel.getUrl()) != null) {
                        sub.removeChannel(subscriptionChannel.getUrl());
                    }

                }
            }
        }
    }

    /**
     * Remove the miscellaneous channels from the channels array list. Miscellaneous channels are the entries
     * added programmatically when the user navigates from Pkg view to Tg view. This has to be removed since, the
     * user hasn't selected it.
     *
     * @param distributionBean Distribution bean to get the miscellaneous channel urls
     * @param channels Channels list which would be set back to the session
     */
    private void removeMiscChannels(DistributionBean distributionBean, ArrayList channels) {
        List miscChnUrls = distributionBean.getMiscChannelUrls();
        if(miscChnUrls != null) {
            Iterator chUrlIterator = miscChnUrls.iterator();
            int i;
            int size;
            boolean isFound = false;
            while(chUrlIterator.hasNext()) {
                String miscChUrl = (String)chUrlIterator.next();
                size = channels.size();
                for(i=0; i<size; i++) {
                    Channel chn = (Channel) channels.get(i);
                    if(chn.getUrl().equals(miscChUrl)) {
                        isFound = true;
                        break;
                    }
                }

                if(isFound) {
                    isFound = false;
                    channels.remove(i);
                }
            }
        }
    }

    protected void checkAndSetChAttributes(Channel subch,
                                           Channel selectedChn,
                                           DistributionBean distributionBean)  {
        if (!INCONSISTENT.equals(selectedChn.getState())) {
            subch.setState(selectedChn.getState());
        }

        if (!INCONSISTENT.equals(selectedChn.getSecState())) {
            subch.setSecState(selectedChn.getSecState());
        }

        // Sets the schedule into the channel object if the schedules are not inconsistent
        if (!INCONSISTENT.equals(selectedChn.getInitScheduleString())) {
            subch.setInitSchedule(selectedChn.getInitSchedule());
        }

        if (!INCONSISTENT.equals(selectedChn.getSecScheduleString())) {
            subch.setSecSchedule(selectedChn.getSecSchedule());
        }

        if (!INCONSISTENT.equals(selectedChn.getUpdateScheduleString())) {
            subch.setUpdateSchedule(selectedChn.getUpdateSchedule());
        }

        if (!INCONSISTENT.equals(selectedChn.getVerRepairScheduleString())) {
            subch.setVerRepairSchedule(selectedChn.getVerRepairSchedule());
        }

        if (!INCONSISTENT.equals(selectedChn.getPostponeScheduleString())) {
            subch.setPostponeSchedule(selectedChn.getPostponeSchedule());
        }
        // sets the install priority if the channel orders are consistent
        // restricting modification of install priority in multi package mode
        if (!INCONSISTENT.equals(selectedChn.getOrderState())) {
            subch.setOrder(selectedChn.getOrder());
        }

        //set the exemptFromBlackout flag
        subch.setExemptFromBlackout(selectedChn.isExemptFromBlackout());
        subch.setWowEnabled(selectedChn.isWowEnabled());
        subch.setWOWForInit(selectedChn.getWOWForInit());
        subch.setWOWForSec(selectedChn.getWOWForSec());
        subch.setWOWForUpdate(selectedChn.getWOWForUpdate());
        subch.setWOWForRepair(selectedChn.getWOWForRepair());
        subch.setUcdTemplates(selectedChn.getUcdTemplates());
        subch.setARTaskValue(distributionBean.getARTaskValue(selectedChn.getUrl()));
    }

    // REMIND: this should do a transactional save.  Meaning, if there is a problem
    // with one of the subscriptions for the target, save out the old ones for the previous
    protected void transactSave(HttpServletRequest request, ArrayList channels,
                                Map<String, Map<String, ISubscription>> subsMap) throws SystemException {

        Channel curCh;
        ISubscription sub, oldSub;
        HttpSession session = request.getSession();
        init(request);
        boolean isPeerPolicyEnabled = main.isPeerApprovalEnabled();
        // Clearing OS variables, if it's used for OSMigration settings
        if(null != session.getAttribute("osvariables")) {
            session.removeAttribute("osvariables");
        }
        //before saving the policy, log the old policy for ARSystem
        ARWorklog workLog = null;
        if(!isPeerPolicyEnabled) {
            if(session.getAttribute(AR_TASK_ID) != null) {
                HashMap hm = new HashMap();
                hm.put(AR_TASK_ID, new String[] {(String)session.getAttribute(AR_TASK_ID)});
                hm.put(AR_USER, getString(request.getLocale(), "ar.worklog.modifieduser")+user.getName());
                session.removeAttribute(AR_CHANGE_ID);
                workLog = new ARWorklog(hm, tenant);
                workLog.write(getString(request.getLocale(), "ar.worklog.originalpolicy"));
                workLog.write("\n");
                workLog.save(oldPolicy);
                GUIUtils.log(servlet, request, LOG_AR_POLICY_CREATION_SUCCESS, oldPolicy.toString());
            }
        }
        List<PolicyDiff> policyDiffs = new ArrayList<PolicyDiff>();

        for (String targetId : subsMap.keySet()) {
            Map<String, ISubscription> tmpMap = subsMap.get(targetId);
            sub = tmpMap.get(KEY_NEW_SUB);
            oldSub = tmpMap.get(KEY_OLD_SUB);
            if (null != sub) {
                if(!isPeerPolicyEnabled) {
                    sub.save();
                }
            } else {
                System.out.println("TransactionSaveAction.transactSave : Failed to save subscription object to LDAP");
            }

            oldPolicy = getOldPolicy(sub, request);
            if(!isPeerPolicyEnabled) {
                if (workLog != null){
                    workLog.write(getString(request.getLocale(), "ar.worklog.updatedpolicy"));
                    workLog.write("\n");
                    workLog.save(oldPolicy);
                }
                // We iterate through all of the channels for each subscription because
                // in the case of inconsistent states, they may differ.
                // We do this AFTER it is saved, so that we can be sure there are no failures
                for (Object channel : channels) {
                    curCh = sub.getChannel(((Channel) channel).getUrl());
                    if (curCh != null) {
                        GUIUtils.log(servlet, request, LOG_SUBSCRIPTION_PKGMOD, curCh.getUrl() + "=" + curCh.getState()
                                + "," + curCh.getSecState(), sub.getTargetID(), "(" + curCh.getInitScheduleString()
                                + ")(" + curCh.getSecScheduleString() + ")(" + curCh.getUpdateScheduleString()
                                + ")(" + curCh.getVerRepairScheduleString() + ")" + ")(" + curCh.getPostponeScheduleString() + ")");
                    }

                }
                main.logAuditInfo(LOG_AUDIT_SUBSCRIPTION_PKGMOD, LOG_AUDIT, "vDesk", sub.getTargetID(), request, POLICY_CREATE);
            }
            ISubscription oldsub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB_COPY);

            // Calculate policy diff and used for both audit log and mail the diff report
            PolicyDiffLogFormatter logDiff = new PolicyDiffLogFormatter(oldsub, sub, main);
            logDiff.prepareDiff();

            String addedChannels = logDiff.getAddedChannels();
            String deletedChannels = logDiff.getDeletedChannels();

            if(null != oldsub && null == session.getAttribute(SESSION_MULTITGBOOL)) {

                String addedProbs = logDiff.getAddedProps();
                String deletedProbs = logDiff.getDeletedProps();
                String changedProbs = logDiff.getModifiedProps();
                String blackoutProbs = logDiff.getBlackoutSchedule();

                if (!addedProbs.trim().isEmpty()) {
                    main.logAuditInfo(LOG_AUDIT_TUNER_PROB_SUMMARY, LOG_AUDIT, "vDesk", "Added Properties " + addedProbs, request, "Target [" + sub.getTargetID() + "]" );
                }
                if (!changedProbs.trim().isEmpty()) {
                    main.logAuditInfo(LOG_AUDIT_TUNER_PROB_SUMMARY, LOG_AUDIT, "vDesk", "Changed Properties " + changedProbs, request, "Target [" + sub.getTargetID() + "]" );
                }
                if (!deletedProbs.trim().isEmpty()) {
                    main.logAuditInfo(LOG_AUDIT_TUNER_PROB_SUMMARY, LOG_AUDIT, "vDesk", "Deleted Properties " + deletedProbs, request, "Target [" + sub.getTargetID() + "]" );
                }
            }
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
        if (workLog != null){
            workLog.close(getString(request.getLocale(), "ar.worklog.policysuccess"));
        }
    }
    /**
     * SESSION_RETURN_PAGETYPE - target_view or pkg_view.
     * These variables should have been stored when the Edit/New Assignment action was called.
     * It can either be TARGET_VIEW or PACKAGE_VIEW. If not set, we default by returning to the Target View Page.
     * Always sends user back to the details page. I,e. if the page that initiated the Edit/New Assingment
     * is target view or target detail, return to the target details page. Likewise for package. - Sets the
     * session variables that will be used by the details pages
     * 1. the variable that holds targets or packages
     * 2. which single/multi select mode depending on the number of targets or packages
     * that is being saved by the assignment.
     *
     * @param req REMIND
     * @param channels REMIND
     * @param targets REMIND
     */
    private void updateSessionVar(HttpServletRequest req,
                                  ArrayList channels,
                                  ArrayList targets) {
        String returnPageType = null;

        try {
            returnPageType = (String) GUIUtils.getFromSession(req, SESSION_RETURN_PAGETYPE);
        } catch (Exception ex) {
            // Do nothing
        }

        if (null == returnPageType) {
            returnPageType = TARGET_VIEW;
        }

        // We are hardcoding the returnPage here and not using the
        // one from the session set to get around this problem :
        //
        // If "new assignment" has multiple targets and
        // it is the very first action done called before
        // the TargetDetailsMultiForm
        // (which is normally targetDetailsAdd or targetDetailsMulti
        // N the form),
        // the return page Target Details page will not display properly
        // because only TargetDetailsForm(single target)
        // is the default.
        HttpSession session = req.getSession();

        if (TARGET_VIEW.equals(returnPageType)) {
            if (targets.size() > 1) {
                session.setAttribute(MAIN_PAGE_M_TARGETS, targets);
                session.setAttribute(SESSION_MULTITGBOOL, "true");
            } else {
                session.setAttribute(MAIN_PAGE_TARGET, targets);
                session.removeAttribute(SESSION_MULTITGBOOL);
                session.setAttribute("context", "targetDetailsAdd");
            }
            for(Object pendingTarget : targets) {
                main.updatePendingPolicySessionVar(req, (Target)pendingTarget);
            }
        } else {
            // Only the channels selected by the user in Pkg View will be available in channels list.
            // Misc channels are removed after saving the policy to LDAP.
            if (channels.size() > 1) {
                session.setAttribute(MAIN_PAGE_M_PKGS, channels);
                session.setAttribute(SESSION_MULTIPKGBOOL, "true");
            } else {
                session.setAttribute(MAIN_PAGE_PACKAGE, channels);
                session.removeAttribute(SESSION_MULTIPKGBOOL);
            }
        }
    }

    private ArrayList<StringBuffer> getOldPolicy(ISubscription sub, HttpServletRequest request) throws SystemException{
        Enumeration enumChannels = sub.getChannels();
        Locale locale = request.getLocale();
        ArrayList<StringBuffer> list = new ArrayList<StringBuffer>(DEF_COLL_SIZE);
        while(enumChannels.hasMoreElements()) {
            StringBuffer sb = new StringBuffer();
            Channel ch = (Channel)enumChannels.nextElement();
            sb.append(getString(locale, "ar.worklog.targetName")).append(sub.getTargetName());
            sb.append("\n");
            sb.append(getString(locale, "ar.worklog.chTitle")).append(ch.getTitle());
            sb.append("\n");
            sb.append(getString(locale, "ar.worklog.priState")).append(ch.getState());
            sb.append("\n");
            sb.append(getString(locale, "ar.worklog.secState")).append(ch.getSecState());
            sb.append("\n");
            sb.append(getString(locale, "ar.worklog.channelurl")).append(ch.getUrl());
            sb.append('\n');
            list.add(sb);
        }
        return list;
    }
}