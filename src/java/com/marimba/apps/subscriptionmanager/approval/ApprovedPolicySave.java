// Copyright 1997-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.approval;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.objects.PatchGroupChannel;
import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.util.Utils;
import com.marimba.intf.msf.ITenant;
import com.marimba.intf.msf.task.ITaskMgr;
import com.marimba.intf.msf.wakeonwan.IWakeManager;

/**
 * This class used to store approved policy from temporary Database storage to LDAP
 *
 * @author Selvaraj Jegatheesan
 */
public class ApprovedPolicySave implements ISubscriptionConstants, IApprovalPolicyConstants,IAppConstants {

    private ITaskMgr taskMgr;
    private IUser reviewedUser;
    private IWakeManager wakeMgr;
    private ITenant approvalTenant;
    private ApprovalPolicyDTO approvedPolicy;
    private ISubscription approvedSubscription;

    public ApprovedPolicySave(ApprovalPolicyDTO approvedPolicy, IUser user, ITenant tenant) {
        this.approvedSubscription = null;
        this.approvedPolicy = approvedPolicy;
        this.reviewedUser = user;
        this.approvalTenant = tenant;
        this.wakeMgr = approvalTenant.getWakeManager();
        this.taskMgr = approvalTenant.getTaskMgr();
    }

    public ISubscription getApprovedSubscription() {
        return approvedSubscription;
    }

    public void setApprovedSubscription(ISubscription approvedSubscription) {
        this.approvedSubscription = approvedSubscription;
    }
    /**
     * Save a new approved policy from DB to LDAP
     */
    public void addNewPolicy() {

        try {
            System.out.println("Creating new approved policy for the target: " + approvedPolicy.getPolicyTargetId());
            approvedSubscription = ObjectManager.createSubscription(approvedPolicy.getPolicyTargetId(), approvedPolicy.getPolicyTargetType(), reviewedUser);
            if(null != approvedSubscription) {
                loadSubscription();
                approvedSubscription.save();
                Utils.scheduleCMSTask(approvedSubscription, wakeMgr, taskMgr, approvalTenant);
                System.out.println("Successfully saved approved new policy into LDAP");
            } else {
                System.out.println("Failed to save approved new policy into LDAP for the target: " + approvedPolicy.getPolicyTargetId());
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            System.out.println("ApprovedPolicySave : Exception occurred while saving approved new policy for the target: " + approvedPolicy.getPolicyTargetId());
        }

    }
    /**
     * Update Existing policy from DB to LDAP
     */
    public void updateExistingPolicy() {

        try {
            System.out.println("Modifying existing policy for the target : " + approvedPolicy.getPolicyTargetId());
            approvedSubscription = ObjectManager.openSubForWrite(approvedPolicy.getPolicyTargetId(), approvedPolicy.getPolicyTargetType(), reviewedUser);
            if(null != approvedSubscription) {
                loadSubscription();
                approvedSubscription.save();
                Utils.scheduleCMSTask(approvedSubscription, wakeMgr, taskMgr, approvalTenant);
                System.out.println("Successfully saved a approved existing policy into LDAP");
            } else {
                System.out.println("Failed to modify existing policy for the target : " + approvedPolicy.getPolicyTargetId());
            }

        } catch(Exception ex) {
            ex.printStackTrace();
            System.out.println("ApprovedPolicySave : Failed to modify existing policy for the target : " + approvedPolicy.getPolicyTargetId());
        }

    }
    /**
     * Delete existing policy from LDAP
     */
    public void DeletePolicy() {

        try {
            System.out.println("Remove existing policy for the target : " + approvedPolicy.getPolicyTargetId());
            approvedSubscription = ObjectManager.openSubForWrite(approvedPolicy.getPolicyTargetId(), approvedPolicy.getPolicyTargetType(), reviewedUser);
            approvedSubscription.delete();
            System.out.println("Approved policy was removed successfully from LDAP for the target: " + approvedPolicy.getPolicyTargetId());
        } catch(Exception ex) {
            ex.printStackTrace();
            System.out.println("ApprovedPolicySave : Failed to delete apporved policy from LDAP for the target: " + approvedPolicy.getPolicyTargetId());
        }
    }
    private void loadSubscription() {
        try {
            debug("Load subscription for the target: " + approvedPolicy.getPolicyTargetId());
            approvedSubscription.setTargetName(approvedPolicy.getPolicyTargetName());
            String blackoutStr = approvedPolicy.getBlackoutSchedule();
            if(null != blackoutStr) {
                debug("Modified Blackout Schedule for the target : " + blackoutStr);
                if(No_BLACKOUT_SCHEDULE.equalsIgnoreCase(blackoutStr)) {
                    approvedSubscription.setBlackOut(null);
                } else {
                    approvedSubscription.setBlackOut(blackoutStr);
                }
            }
            loadChannel();
            loadTunerProperty();
        } catch(Exception ex) {
            ex.printStackTrace();
            System.out.println("ApprovedPolicySave : Failed to load subscription for the target");
        }
    }
    private void loadChannel() {
        try {
            debug("Loading channels for the target: " + approvedPolicy.getPolicyTargetId());
            debug("Added Channels Size :" + approvedPolicy.getAddChannels().size());
            for(ApprovalChannelDTO channelInfo : approvedPolicy.getAddChannels()) {
                debug("Channel URL:" + channelInfo.getM_channelURL());
                boolean hasExists = false;
                Channel channel = null;
                // if added channel has no primary state that channel consider as Dummy channel
                if(null != channelInfo.getPrimaryStateSchedule()) {
                    channel = createChannelContent(channelInfo);
                    if(null != channel) {
                        approvedSubscription.addChannel(channel);
                    }
                } else {
                    // before creating dummy channel need to check that channel appear in removed channel list
                    // if it is appeared then no need to create dummy channel because of
                    // when removing the channel internally create dummy channel
                    for(ApprovalChannelDTO removeChannelInfo : approvedPolicy.getDeleteChannels()) {
                        if(removeChannelInfo.getM_channelURL().equalsIgnoreCase(channelInfo.getM_channelURL())) {
                            hasExists = true;
                        }
                    }
                    if(!hasExists) {
                        channel = approvedSubscription.createDummyChannel(channelInfo.getM_channelURL());
                        if(null != channel) {
                            for(ApprovalPropertyDTO chProps : channelInfo.getAddedPropertyList()) {
                                channel.setProperty(chProps.getPropKey(), chProps.getPropValue());
                            }
                        }
                    }
                }
            }

            debug("Modified Channels Size :" + approvedPolicy.getModifyChannels().size());
            for(ApprovalChannelDTO channelInfo : approvedPolicy.getModifyChannels()) {
                modifyChannel(channelInfo);
            }

            debug("Removed Channels Size :" + approvedPolicy.getDeleteChannels().size());
            for(ApprovalChannelDTO channelInfo : approvedPolicy.getDeleteChannels()) {
                removeChannel(channelInfo);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            System.out.println("ApprovedPolicySave : Failed to load channels for the target");
        }
    }
    /**
     * Create a new Channel Content
     * @param channelDTO
     * @return
     */
    private Channel createChannelContent(ApprovalChannelDTO channelDTO) {

        String primaryState = "";
        String primarySchedule = "";
        String secState = "";
        String secSchedule = "";
        Channel channel = null;

        try {
            int stateIndex = channelDTO.getPrimaryStateSchedule().indexOf(',');
            if (stateIndex > 0) {
                primaryState = channelDTO.getPrimaryStateSchedule().substring(0, stateIndex);
                primarySchedule = channelDTO.getPrimaryStateSchedule().substring(stateIndex + 1).trim();
            } else {
                primaryState = channelDTO.getPrimaryStateSchedule().trim();
            }

            if(null != channelDTO.getSecondaryStateSchedule()) {
                int stateSecIndex = channelDTO.getSecondaryStateSchedule().indexOf(',');
                if (stateSecIndex > 0) {
                    secState = channelDTO.getSecondaryStateSchedule().substring(0, stateSecIndex);
                    secSchedule = channelDTO.getSecondaryStateSchedule().substring(stateSecIndex + 1).trim();
                } else {
                    secState = channelDTO.getSecondaryStateSchedule().trim();
                }
            }
            if (CONTENT_TYPE_PATCHGROUP.equals(channelDTO.getContentType())) {
                channel = new PatchGroupChannel(channelDTO.getM_channelURL(), channelDTO.getTitle(), primaryState, null, channelDTO.getPriority());
            } else if(CONTENT_TYPE_CONFIGPKG.equals(channelDTO.getContentType())) {
                channel = new com.marimba.apps.subscription.common.objects.ConfigPakgChannel(channelDTO.getM_channelURL(), channelDTO.getTitle(), primaryState, secState, channelDTO.getPriority());
                channel.setInitSchedule(primarySchedule);
                channel.setSecSchedule(secSchedule);
                channel.setVerRepairSchedule(channelDTO.getRepairSchedule());
                channel.setUpdateSchedule(channelDTO.getUpdateSchedule());
            } else {
                channel = new Channel(channelDTO.getM_channelURL(), channelDTO.getTitle(), primaryState, secState, channelDTO.getPriority());
                channel.setInitSchedule(primarySchedule);
                channel.setSecSchedule(secSchedule);
                channel.setVerRepairSchedule(channelDTO.getRepairSchedule());
                channel.setUpdateSchedule(channelDTO.getUpdateSchedule());
            }
            channel.setExemptFromBlackout("true".equalsIgnoreCase(channelDTO.getExemptBlackout()) ? true : false);
            String wowAllStatus = channelDTO.getWowStatus();
            channel.setWowEnabled(getWoWStatus("urgent", wowAllStatus));
            channel.setWOWForInit(getWoWStatus("init", wowAllStatus));
            channel.setWOWForSec(getWoWStatus("secondary", wowAllStatus));
            channel.setWOWForUpdate(getWoWStatus("update", wowAllStatus));
            channel.setWOWForRepair(getWoWStatus("repair", wowAllStatus));
            System.out.println("ApprovedPolicySave : Property Size : "+ channelDTO.getAddedPropertyList().size());
            for(ApprovalPropertyDTO chProps : channelDTO.getAddedPropertyList()) {
                String Key = chProps.getPropKey();
                String Value = chProps.getPropValue();
                // if post pone schedule value is no schedule has been assigned then remove schedule from LDAP
                if(Key.equalsIgnoreCase(CH_POSTPONESCHED_KEY)) {
                    channel.setPostponeSchedule(Value);
                } else {
                    channel.setProperty(Key, Value);
                }

            }
        } catch(Exception ec) {
            ec.printStackTrace();
            System.out.println("ApprovedPolicySave : Failed to create a new channel");
        }

        return channel;
    }
    /**
     * Get WOW Status
     * @param type
     * @param Value
     * @return
     */
    private boolean getWoWStatus(String type, String Value) {
        if(null == Value || "".equals(Value.trim())) {
            return false;
        }
        return Value.contains(type);
    }

    private void modifyChannel(ApprovalChannelDTO channeldbInfo) {
        try {
            Channel subChannel = null;
            subChannel = approvedSubscription.getChannel(channeldbInfo.getM_channelURL());
            if(null != subChannel) {
                debug("Modify channel : " + channeldbInfo.getM_channelURL());
                modifyChannelContent(subChannel, channeldbInfo);
                modifyChannelProperty(subChannel, channeldbInfo);
            } else {
                subChannel = approvedSubscription.getDummyChannel(channeldbInfo.getM_channelURL());
                if(null != subChannel) {
                    debug("Modify Dummy channel : " + channeldbInfo.getM_channelURL());
                    modifyChannelProperty(subChannel, channeldbInfo);
                } else {
                    debug("Channel is not available for modified operation " + channeldbInfo.getM_channelURL());
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            System.out.println("ApprovedPolicySave : Failed to modify the channel : " + channeldbInfo.getM_channelURL());
        }
    }
    private void modifyChannelContent(Channel subChannel, ApprovalChannelDTO channeldbInfo) {
        try {
            if(null != channeldbInfo.getPrimaryStateSchedule()) {
                String primaryState = null;
                String primarySchedule = null;
                int stateIndex = channeldbInfo.getPrimaryStateSchedule().indexOf(',');
                if (stateIndex > 0) {
                    primaryState = channeldbInfo.getPrimaryStateSchedule().substring(0, stateIndex);
                    primarySchedule = channeldbInfo.getPrimaryStateSchedule().substring(stateIndex + 1).trim();
                } else {
                    primaryState = channeldbInfo.getPrimaryStateSchedule().trim();
                }
                debug("Primary State :" + primaryState);
                debug("Primary Schedule :" + primarySchedule);

                if(null != primaryState && !"null".equalsIgnoreCase(primaryState) ) {
                    debug("Primary state has been changed");
                    subChannel.setState(primaryState);
                }
                if(null != primarySchedule && !"null".equalsIgnoreCase(primarySchedule)) {
                    if(DEFAULT_ACTIVE_PERIOD.equalsIgnoreCase(primarySchedule)) {
                        // Default value set Activate next time policy service updates
                        debug("Primary Schedule empty value set to channel");
                        subChannel.setInitSchedule("");
                    } else {
                        debug("Primary Schedule set to channel");
                        subChannel.setInitSchedule(primarySchedule);
                    }
                }
            }
            if(null != channeldbInfo.getSecondaryStateSchedule()) {
                String secState = null;
                String secSchedule = null;
                int stateSecIndex = channeldbInfo.getSecondaryStateSchedule().indexOf(',');
                if (stateSecIndex > 0) {
                    secState = channeldbInfo.getSecondaryStateSchedule().substring(0, stateSecIndex);
                    secSchedule = channeldbInfo.getSecondaryStateSchedule().substring(stateSecIndex + 1).trim();
                } else {
                    secState = channeldbInfo.getSecondaryStateSchedule().trim();
                }
                debug("Secondary State :" + secState);
                debug("Secondary Schedule :" + secSchedule);

                if(null != secState && !"null".equalsIgnoreCase(secState)) {
                    if(NO_STATE.equalsIgnoreCase(secState)) {
                        // No secondary state has been assigned
                        debug("Secondary State empty value set to channel");
                        subChannel.setSecState("");
                    } else {
                        debug("Secondary State set to channel");
                        subChannel.setSecState(secState);
                    }

                }
                if(null != secSchedule && !"null".equalsIgnoreCase(secSchedule)) {
                    if(NO_SCHEDULE.equalsIgnoreCase(secSchedule)) {
                        // No secondary Schedule has been assigned
                        debug("Secondary Schedule empty value set to channel");
                        subChannel.setSecSchedule("");
                    } else {
                        debug("Secondary Schedule set to channel");
                        subChannel.setSecSchedule(secSchedule);
                    }
                }
            }
            if(null != channeldbInfo.getUpdateSchedule()) {
                if(NO_SCHEDULE.equalsIgnoreCase(channeldbInfo.getUpdateSchedule())) {
                    // No update Schedule has been assigned
                    debug("Update Schedule empty value set to channel");
                    subChannel.setUpdateSchedule("");
                } else {
                    debug("Update Schedule set to channel");
                    subChannel.setUpdateSchedule(channeldbInfo.getUpdateSchedule());
                }
            }
            if(null != channeldbInfo.getRepairSchedule()) {
                if(NO_SCHEDULE.equalsIgnoreCase(channeldbInfo.getRepairSchedule())) {
                    // No Repair Schedule has been assigned
                    debug("Repair Schedule empty value set to channel");
                    subChannel.setVerRepairSchedule("");
                } else {
                    debug("Repair Schedule set to channel");
                    subChannel.setVerRepairSchedule(channeldbInfo.getRepairSchedule());
                }
            }
            if(0 != channeldbInfo.getPriority()) {
                subChannel.setOrder(channeldbInfo.getPriority());
            }
            if(null != channeldbInfo.getWowStatus()) {
                String wowAllStatus = channeldbInfo.getWowStatus();
                System.out.println("WOW Status :" + wowAllStatus);
                if(getWoWStatus("1-urgent", wowAllStatus)) {
                    subChannel.setWowEnabled(true);
                }
                if(getWoWStatus("0-urgent", wowAllStatus)) {
                    subChannel.setWowEnabled(false);
                }
                if(getWoWStatus("1-init", wowAllStatus)) {
                    subChannel.setWOWForInit(true);
                }
                if(getWoWStatus("0-init", wowAllStatus)) {
                    subChannel.setWOWForInit(false);
                }
                if(getWoWStatus("1-secondary", wowAllStatus)) {
                    subChannel.setWOWForSec(true);
                }
                if(getWoWStatus("0-secondary", wowAllStatus)) {
                    subChannel.setWOWForSec(false);
                }
                if(getWoWStatus("1-update", wowAllStatus)) {
                    subChannel.setWOWForUpdate(true);
                }
                if(getWoWStatus("0-update", wowAllStatus)) {
                    subChannel.setWOWForUpdate(false);
                }
                if(getWoWStatus("1-repair", wowAllStatus)) {
                    subChannel.setWOWForRepair(true);
                }
                if(getWoWStatus("0-repair", wowAllStatus)) {
                    subChannel.setWOWForRepair(false);
                }
            }
            debug("channeldbInfo.getExemptBlackout() :  " + channeldbInfo.getExemptBlackout());
            if(null != channeldbInfo.getExemptBlackout()) {
                debug("Channel Exempt Blackout Changed");
                subChannel.setExemptFromBlackout("true".equalsIgnoreCase(channeldbInfo.getExemptBlackout()) ? true : false);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            System.out.println("ApprovedPolicySave : Failed to modify channel content in LDAP from DB :" + channeldbInfo.getM_channelURL());
        }

    }
    private void modifyChannelProperty(Channel subChannel, ApprovalChannelDTO channeldbInfo) {
        try {
            for(ApprovalPropertyDTO chPorps : channeldbInfo.getPropertyList()) {
                String Key = chPorps.getPropKey();
                String Value = (chPorps.getPropAction() == DELETE_OPERATION) ? null : chPorps.getPropValue();
                // if post pone schedule value is no schedule has been assigned then remove schedule from LDAP
                if(Key.equalsIgnoreCase(CH_POSTPONESCHED_KEY)) {
                    if(NO_SCHEDULE.equalsIgnoreCase(Value)) {
                        subChannel.setPostponeSchedule("");
                    } else {
                        subChannel.setPostponeSchedule(Value);
                    }

                } else {
                    debug("Modified Channel property : Key = " + Key + " ,Value = " + Value);
                    subChannel.setProperty(Key, Value);
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            System.out.println("ApprovedPolicySave : Failed to modify channel proerty :" + channeldbInfo.getM_channelURL());
        }
    }
    private void removeChannel(ApprovalChannelDTO channelInfo) {
        try {
            Channel deleteChannel,newChannel,newDummyChannel = null;
            // if removed channel has no primary state that channel consider as Dummy channel
            if(null != channelInfo.getPrimaryStateSchedule()) {
                deleteChannel = approvedSubscription.getChannel(channelInfo.getM_channelURL());
                if(null != deleteChannel) {
                    approvedSubscription.removeChannel(deleteChannel.getUrl());
                    if(deleteChannel.getPropertyPairs().length > 0) {
                        newDummyChannel = approvedSubscription.getDummyChannel(deleteChannel.getUrl());
                        if(null != newDummyChannel) {
                            modifyChannelProperty(newDummyChannel, channelInfo);
                        }
                    }
                } else {
                    debug("Channel is not avialable from LDAP : " + channelInfo.getM_channelURL());
                }
            } else {
                deleteChannel = approvedSubscription.getDummyChannel(channelInfo.getM_channelURL());
                if(null != deleteChannel) {
                    approvedSubscription.removeDummyChannel(deleteChannel.getUrl());
                } else {
                    debug("Dummy Channel is not avialable from LDAP : " + channelInfo.getM_channelURL());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("ApprovedPolicySave : Failed to remove channel from LDAP : " + channelInfo.getM_channelURL());
        }

    }
    /**
     * Load Tuner Property
     */
    private void loadTunerProperty() {
        try {
            for(ApprovalPropertyDTO propertyDTO : approvedPolicy.getTunerProps()) {
                debug("Property: [" + propertyDTO.getPropType() + ", " + propertyDTO.getPropKey() + " = " + propertyDTO.getPropValue() + "] (Action = " + propertyDTO.getPropAction() + ")");

                String propType = propertyDTO.getPropType();
                String propValue = (propertyDTO.getPropAction() == DELETE_OPERATION) ? null : propertyDTO.getPropValue();
                if("tuner".equals(propType)) {
                    propType = "";
                }
                approvedSubscription.setProperty(propType, propertyDTO.getPropKey(), propValue);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            System.out.println("ApprovedPolicySave : Failed to load tuner properties for the target");
        }
    }
    private void debug(String msg) {
        if(DEBUG5) {
            System.out.println("ApprovedPolicySave : " + msg);
        }
    }
}
