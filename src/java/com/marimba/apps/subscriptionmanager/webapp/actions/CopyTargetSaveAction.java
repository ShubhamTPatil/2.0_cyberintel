// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.SubKnownException;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.intf.objects.dao.ISubDataSource;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.objects.Subscription;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.util.ScheduleUtils;
import com.marimba.apps.subscriptionmanager.ObjectManager;
import static com.marimba.apps.subscriptionmanager.approval.IApprovalPolicyConstants.ADD_OPERATION;
import static com.marimba.apps.subscriptionmanager.approval.IApprovalPolicyConstants.POLICY_PENDING_COPYOPERATION;
import static com.marimba.apps.subscriptionmanager.intf.IPolicyDiffConstants.KEY_NEW_SUB;
import static com.marimba.apps.subscriptionmanager.intf.IPolicyDiffConstants.KEY_OLD_SUB;
import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiff;
import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiffMailFormatter;
import com.marimba.apps.subscriptionmanager.webapp.forms.CopyEditForm;
import com.marimba.apps.subscriptionmanager.webapp.intf.GUIConstants;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.Utils;
import com.marimba.apps.subscriptionmanager.wow.WoWSchedule;
import com.marimba.castanet.schedule.Schedule;
import com.marimba.castanet.schedule.ScheduleInfo;
import com.marimba.intf.msf.wakeonwan.IWakeManager;
import com.marimba.intf.util.IConfig;
import com.marimba.tools.config.ConfigProps;
import com.marimba.webapps.intf.CriticalException;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.KnownException;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.tools.util.PropsBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.*;

/**
 * This action is responsible for saving all the changes to the
 * the copy assignment to LDAP. It reads from the
 * DistributionBean and writes to LDAP. It overwrites the policies for the targets.
 *
 * @author  Jayaprakash Paramasivam
 * @version  $Revision$,  $Date$
 *
 */

public final class CopyTargetSaveAction extends AbstractAction {

    private IUser user;

    private Map<String, Map<String, ISubscription>> subsMap;

    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
        return new CopyTargetSaveTask(mapping, form, request, response);
    }

    protected class CopyTargetSaveTask extends SubscriptionDelayedTask {
        CopyTargetSaveTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
            super(mapping, form, request, response);
        }

        public void execute(){
            try {
                init(request);
                subsMap = new LinkedHashMap<String, Map<String, ISubscription>>(20);
                user = (IUser) request.getSession().getAttribute(SESSION_SMUSER);
                boolean hasKnownError = false;
                boolean isPeerPolicy = main.isPeerApprovalEnabled();

                DistributionBean distributionBean = getDistributionBean(request);
                ArrayList channels = distributionBean.getChannels();
                DistributionBean copyBean = getDistributionBeanCopy(request);
                ArrayList to_targets = (ArrayList)getRhsItems(request, COPY_RHS_LIST);
                System.out.println("Copy bean targets  size : " + to_targets.size());
                // check if subscription object exists in ldap, if exists delete channels
                //removeChannels(request,to_targets);
                try {
                    // This sets aside the subscriptions that are
                    // to be saved for each target.
                    // We do not save them along the way because an
                    // error could occur on one of them.
                    List<ISubscription> subscriptions = new Vector<ISubscription>(5);
                    // Create a subscription object if it doesn't exist
                    // Loads the subscription if it already exists
                    for (Object to_target : to_targets) {
                    	PropsBean propsBean = (PropsBean) to_target;
                        Target tgt = propsBeanToTarget(propsBean);

                        //Target tgt = (Target) to_target;
                        //debug("Setting channels for " + tgt);
                        // In copy operation, old policy should be remove old policy if exists and new policy copy after approval happen
                        // In this case, every copy operation should be new policy and set special flag as pending_copyoperation
                        ISubscription sub;
                        if(isPeerPolicy) {
                        	ISubDataSource src = ObjectManager.getSubDataSource(user);
                            sub = new Subscription(tgt.getId(), tgt.getType(), src);
                        } else {
                        	sub = ObjectManager.openSubForWrite(tgt.getId(), tgt.getType(), user);
                        	main.logAuditInfo(LOG_AUDIT_SUB_OBJ_CREATED, LOG_AUDIT, "vDesk", tgt.getID() + " to copy policies", request, COPY_ASSIGNMENT);
                        }
                        subscriptions.add(sub);

//                        for (Object channel : channels) {
//                            Channel selectedChn = (Channel) channel;
//                            debug("Selected Channel " + selectedChn.hashCode() + ": "
//                                    + selectedChn.getUrl() + ", " + selectedChn.getState()
//                                    + ", secState: " + selectedChn.getSecState()
//                                    + ", initSched: " + selectedChn.getInitSchedule()
//                                    + ", secSched: " + selectedChn.getSecSchedule()
//                                    + ", verrepairSched: " + selectedChn.getUpdateSchedule()
//                                    + ", updateSched: " + selectedChn.getUpdateSchedule()
//                                    + ", order: " + selectedChn.getOrder());
//
//                            sub.setChannel(selectedChn);
//                        }
                        //saving the tuner properties of the target
                        setTunerChProps(sub, request);
                    }

                    // The subscription are saved outside of the loop because and error could occur during the save
                    // of one of them and we do not want to save any of them if there is a problem.
                    transactSave(request, subscriptions, channels);
//                    triggerMail();
//                    storeCopyPolicy2DB();
//                    if(!isPeerPolicy) {
//                    	scheduleCMSTask(subscriptions);
//                    }

                } catch (SubKnownException ske) {
                    guiException = new GUIException(new CriticalException(ske.getKey(), ske.getArg0(), ske.getArg1(), ske.getArg2()));
                } catch (Exception e) {
                    if (e instanceof KnownException) {
                        hasKnownError = true;
                    }
                    throw new GUIException(COPY_POLICY_SAVE_ERROR, e);
                } finally {
                    if (!hasKnownError) {
                        //removeMiscChannels(distributionBean, channels);
                        //removeMiscChannels(copyBean,channels);
                        // Clear the check boxes and paging objects
                        CopyEditForm formbean = (CopyEditForm) form;
                        ArrayList to_targets1 = new ArrayList();
                        formbean.clearPagingVars(request,to_targets1);
                    }
                }

                forward = mapping.findForward("success");
            } catch (Exception ex) {
                if (ex instanceof GUIException) {
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
    /**
     * Remove the channels for the targets, if the policy is already exists. It helps the save function
     * to create a new policy.
     *
     * @param request request object to check the subscription object exists
     * @param targets list of targets to check whether the subscription object exists
     */
    private void removeChannels(HttpServletRequest request, ArrayList targets) {

        try{
            for (Object target : targets) {
            	PropsBean propsBean = (PropsBean) target;
                Target tgt = propsBeanToTarget(propsBean);
                ISubscription sub = ObjectManager.openSubForWrite(tgt.getId(), tgt.getType(), GUIUtils.getUser(request));
                if (sub.exists()) {
                    if(!main.isPeerApprovalEnabled()) {
                    	addToSubsMap(KEY_OLD_SUB, tgt.getId(), new Subscription((Subscription) sub));
                    	ObjectManager.deleteSubscription(tgt.getId(), tgt.getType(), GUIUtils.getUser(request));
                    	GUIUtils.log(servlet, request, LOG_DELETE_SUBSCRIPTION, tgt.getId());
                    }
                }
            }
        } catch(Exception e){
            //no need to catch
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

    protected void scheduleCMSTask(List<ISubscription> subscriptions){

        IConfig cfg;
        String targetdn;
        String targetName;
        List<WoWSchedule> wowSchedule = new ArrayList<WoWSchedule>();
        IWakeManager wakeMgr = (IWakeManager) server.getService("wow");
        for (ISubscription sub : subscriptions) {
            targetdn = sub.getTargetID();
            targetName = sub.getTargetName();
            cfg = new ConfigProps(null);
            Enumeration enm = sub.getChannels();

            for (; enm.hasMoreElements(); ) {
                Channel ch = (Channel) enm.nextElement();
                if (ch.isWowEnabled()) {
                    debug("channel name : " + ch.getUrl());
                    if (CONTENT_TYPE_PATCHGROUP.equalsIgnoreCase(ch.getType())){
                        // Schedule WoW task as the patch group has to be applied now
                        wowSchedule.add(new WoWSchedule(ScheduleUtils.activeScheduleNow(DELAY)));
                        continue;
                    }
                    if (ch.getInitSchedule() == null) {
                        wowSchedule.add(new WoWSchedule(ScheduleUtils.activeScheduleNow(DELAY)));
                    } else {
                        if (!ScheduleUtils.isScheduleStarted(ch.getInitScheduleInfo())) {
                            wowSchedule.add(new WoWSchedule(ch.getInitSchedule()));
                        }
                    }
                    if (ch.getSecSchedule() != null) {
                        if (!ScheduleUtils.isScheduleStarted(ch.getSecScheduleInfo())) {
                            wowSchedule.add(new WoWSchedule(ch.getSecSchedule()));
                        }
                    }
                    if(ch.getUpdateSchedule() != null && ch.getWOWForUpdate() &&
                            ch.getUpdateScheduleInfo().getFlag(ScheduleInfo.CALENDAR_PERIOD) != ScheduleInfo.NEVER ) {
                        Schedule updateSch = ch.getUpdateSchedule();
                        long next = updateSch.nextTime(-1, System.currentTimeMillis());
                        if(next != -1) {
                            wowSchedule.add(new WoWSchedule(ch.getUpdateSchedule()));
                        }
                    }
                }
            }

            Collections.sort(wowSchedule);
            if (!wowSchedule.isEmpty()) {
                Schedule wakeSchedule = (wowSchedule.get(0)).geWowSchedule();

                cfg.setProperty("task.schedule", wakeSchedule.toString());
                cfg.setProperty("targetdn", targetdn);
                cfg.setProperty("task.timeout", "-1");
                cfg.setProperty("task.oneTime", "true");
                cfg.setProperty("task.lastInvokeTime", Long.toString(System.currentTimeMillis()));
                targetName = URLEncoder.encode(targetName.replace('.','_'));
                cfg.setProperty("targetName",targetName);
                cfg.setProperty("target.ScheduleList", Utils.convertSchedules (wowSchedule));
                //            if(IAppConstants.DEBUG) {
                System.out.println("[Debug] CopyTargetSaveAction - target.ScheduleList -"+
                        cfg.getProperty("target.ScheduleList"));
                //            }
                if (taskMgr.addTask("PolicyCompliance/wow", "policy", targetName, cfg, true)) {
                    System.out.println("CopyTargetSaveAction: Task is successfully added for schedule "+ wakeSchedule.toString());
                } else {
                    System.out.println("CopyTargetSaveAction: Failed to add WOW task for target - "+ targetName );
                }
            } else {
                removeScheduledTask(targetName);
                // Tamil: commenting this. I don't see any usage of this schedules in WakeManager.java class. same used in Utils.java at 450
//                wakeMgr.removeSchedules(targetdn);
                if (DEBUG) {
                    System.out.println("CopyTargetSaveAction: There was no package schedule or valid schedule defined "+
                            "for any of the wow package. Wakeup task will not be scheduled.");
                }
            }
        }
    }

    //Remove schedule task from  cms
    private void removeScheduledTask(String targetName) {
        String encodedTargetName = URLEncoder.encode(targetName.replace('.','_'));
        if (taskMgr.getTask("policy", encodedTargetName) != null) {
            if (taskMgr.removeTask("policy", encodedTargetName)) {
                System.out.println("CopyTargetSaveAction: Task already scheduled is got removed now, since there is no wow package or no package schedule defined for wow packages.");
            } else {
                System.out.println("CopyTargetSaveAction: There was a problem while removing a scheduled Task for target " + targetName);
            }
        } else {
            System.out.println("CopyTargetSaveAction: There was no wake-up task scheduled in CMS earlier for target " + targetName);
        }
    }
    /**
     * Subscription transactional save function ,if there is a problem with one of the
     * subscriptions for the target, save out the old ones for the previous.
     *
     * @param request request object to log the channel details
     * @param subscriptions vector containing subscription to save
     * @param channels Channels list which would be set back to the session
     * @throws com.marimba.webapps.intf.SystemException exception
     */
    protected void transactSave(HttpServletRequest request, List<ISubscription> subscriptions, ArrayList channels)
            throws SystemException {
        Channel curCh;
        ISubscription newsub;

        for (Object subscription : subscriptions) {
            newsub = (ISubscription) subscription;
            boolean isEnabledPeerPolicy = main.isPeerApprovalEnabled();
            if(!isEnabledPeerPolicy) {
                newsub.save();
                main.logAuditInfo(LOG_AUDIT_SUBSCRIPTION_PKGCOPY, LOG_AUDIT, "vDesk", newsub.getTargetID(), request, COPY_ASSIGNMENT);
            }
            addToSubsMap(KEY_NEW_SUB, newsub.getTargetID(), newsub);
        }
    }

    /**
     * Subscription setTunerChProps function, it copies the tuner properties from
     * the target to assigned targets.
     *
     * @param request request object to log the channel details
     * @param newsub Subscription object for copying tuner properties
     */
    protected void setTunerChProps(ISubscription newsub,HttpServletRequest request) throws SystemException{

        ISubscription oldsub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);
//        copyTunerProperties(oldsub, newsub, PROP_TUNER_KEYWORD);
//        copyTunerProperties(oldsub, newsub, PROP_SERVICE_KEYWORD);
//        copyTunerProperties(oldsub, newsub, PROP_CHANNEL_KEYWORD);
//        copyTunerProperties(oldsub, newsub, PROP_ALL_CHANNELS_KEYWORD);
//        copyTunerProperties(oldsub, newsub, PROP_DEVICES_KEYWORD);
//        copyTunerProperties(oldsub, newsub, PROP_POWER_KEYWORD);
//        copyTunerProperties(oldsub, newsub, PROP_SECURITY_KEYWORD);
        copyTunerProperties(oldsub, newsub, PROP_SCAP_SECURITY_KEYWORD);
        copyTunerProperties(oldsub, newsub, PROP_USGCB_SECURITY_KEYWORD);
        copyTunerProperties(oldsub, newsub, PROP_CUSTOM_SECURITY_KEYWORD);
//        copyTunerProperties(oldsub, newsub, PROP_AMT_KEYWORD);
//        copyTunerProperties(oldsub, newsub, PRO_AMT_ALARMCLK_KEYWORD);
//        copyTunerProperties(oldsub, newsub, PROP_OSM_KEYWORD);
//        copyTunerProperties(oldsub, newsub, PROP_PBACKUP_KEYWORD);
    }

    /**
     * Subscription setTunerChProps function, it copies the policy properties from
     * the from targets to seleted targets.
     *
     * @param oldsub Subscription object to copy tuner properties
     * @param newsub Subscription object to copy from target
     * @param type string type for the tuner property.
     */
    protected void copyTunerProperties(ISubscription oldsub, ISubscription newsub, String type)
            throws SystemException {
        String[] pairs = oldsub.getPropertyPairs(type);

        for (int i = 0; i < pairs.length; i += 2) {
            newsub.setBlackOut(oldsub.getBlackOut());
            newsub.setProperty(type, pairs [i], pairs [i + 1]);
        }
    }

    private void addToSubsMap(String subType, String targetId, ISubscription sub) {

        if (null != subsMap.get(targetId)) {
            subsMap.get(targetId).put(subType, sub);
        } else {
            Map<String, ISubscription> newTmpMap = new HashMap<String, ISubscription>(2);
            newTmpMap.put(subType, sub);
            subsMap.put(targetId, newTmpMap);
        }
    }

    private void triggerMail() {
        List<PolicyDiff> policyDiffs = new ArrayList<PolicyDiff>();
        for (String tgt : subsMap.keySet()) {
            Map<String, ISubscription> tmpMap = subsMap.get(tgt);
            // This is special case : when you doing copy operation, old policy should be remove if already exists and copy new policy
            // This case policy diff should not consider about old sub.That is the reason passing null value for old sub
            PolicyDiff policyDiff = new PolicyDiff(null, tmpMap.get(KEY_NEW_SUB), main);
            policyDiffs.add(policyDiff);
        }

        PolicyDiffMailFormatter mailFormatter = new PolicyDiffMailFormatter(policyDiffs, resources);

        String userName = null;
        try {
            userName = main.getDisplayName(main.resolveUserDN(user.getName()));
        } catch (SystemException e) {
            if (DEBUG) e.printStackTrace();
        }
        mailFormatter.setCreatedByDispName((null == userName || userName.trim().isEmpty()) ? user.getName() : userName);
        mailFormatter.setPeerApprovalEnabled(main.isPeerApprovalEnabled());
        mailFormatter.prepare();
        main.sendMail(mailFormatter);
    }

    private void storeCopyPolicy2DB() {
        if (!subsMap.isEmpty()) {
            List<PolicyDiff> policyDiffs = getPolicyDiff();
            debug("Policy Diff size is  : " + policyDiffs.size());
            if(policyDiffs.size() > 0) {
                main.storeApprovalPolicy2DB(policyDiffs);
            } else {
                System.out.println("CopyTargetSaveAction : Failed to store policy diff in database for approval");
            }
        }
    }

    private List<PolicyDiff> getPolicyDiff() {
        List<PolicyDiff> policyDiffs = new ArrayList<PolicyDiff>(subsMap.size());
        String modifiedUserDN = null;
        try {
            modifiedUserDN = main.resolveUserDN(user.getName());
        } catch (Exception e) {
            if (DEBUG) e.printStackTrace();
        }
        if(null == modifiedUserDN) {
            return policyDiffs;
        }
        debug("Subscription object size : " + subsMap.size());
        for (String key : subsMap.keySet()) {
            Map<String, ISubscription> subMap = subsMap.get(key);
            if (null != subMap && !subMap.isEmpty()) {
                ISubscription oldSub = subMap.get(KEY_OLD_SUB);
                ISubscription newSub = subMap.get(KEY_NEW_SUB);
                // This is special case : when you doing copy operation, old policy should be remove if already exists and copy new policy
                // This case policy diff should not consider about old sub.That is the reason passing null value for old sub
                PolicyDiff subDiff = new PolicyDiff(null, newSub, main);
                /*boolean hasPolicyExists = false;
                try {
                    hasPolicyExists = ObjectManager.existsSubscription(oldSub.getTargetID(), oldSub.getTargetType(), user);
                } catch(Exception ex) {
                    //
                }*/
                // here no required to check policy exists because of copy operation consider as add new policy and remove existing policy in approval process
                subDiff.setPolicyAction(ADD_OPERATION);
                // special policy status flag for copy operation. Because of when copying operation
                // old policy removed and new policy created warning msg show in approval page
                subDiff.setPolicyStatus(POLICY_PENDING_COPYOPERATION);
                subDiff.setUser(modifiedUserDN);
                policyDiffs.add(subDiff);
            }
        }
        return policyDiffs;
    }
    private List getRhsItems(HttpServletRequest request, String targetName) throws GUIException {
        try {
            //get stored values
           if(GUIUtils.getFromSession(request, targetName) == null) {
               return null;
           }
            return (List) GUIUtils.getFromSession(request, targetName);
        } catch (SystemException se) {
            se.printStackTrace();

            GUIException guie = new GUIException(se);
            throw guie;
        }
    }
    private Target propsBeanToTarget(PropsBean bean) {
        String allEndpoints = resources.getMessage(Locale.getDefault(), "page.global.All");
        String type = (String) bean.getValue(GUIConstants.TYPE);
        String displayName = TYPE_ALL.equals(type)?allEndpoints:(String) bean.getValue(GUIConstants.DISPLAYNAME);
        String dn = (String) bean.getValue("dn");

        return new Target(displayName, type, dn);
    }
    private void debug(String msg) {
        if (DEBUG) System.out.println("CopyTargetSaveAction: " + msg);
    }
}