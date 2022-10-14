// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.policydiff;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IPolicyDiffConstants;

import java.util.*;

/**
 * Class ChannelDiffer used for calculate channel level difference for each channels 
 *
 * @author Tamilselvan Teivasekamani
 * @author Selvaraj Jegatheesan
 * @version $Revision$,  $Date$
 *
 */

public class ChannelDiffer implements ISubscriptionConstants, IAppConstants, IPolicyDiffConstants {
	private SubscriptionMain main;
    private String mode; // Mode refers "add", "delete" and "update"
    private boolean isRemovedChAlone = false;
    private Map<String, String> addedPropsMap;
    private Map<String, String> deletedPropsMap;
    private Map<String, Map<String, String>> channelDiffInfo;
    private Map<String, Map<String, String>> modifiedPropsMap;
    private Channel oldChannel, newChannel, currentChannel;

    public ChannelDiffer(Channel oldChannel, Channel newChannel, String mode, SubscriptionMain subMain) {
    	this.main = subMain;
        this.mode = mode;
        this.oldChannel = oldChannel;
        this.newChannel = newChannel;
        this.channelDiffInfo = new LinkedHashMap<String, Map<String,String>>();
        this.addedPropsMap = new LinkedHashMap<String, String>();
        this.modifiedPropsMap = new LinkedHashMap<String, Map<String, String>>();
        this.deletedPropsMap = new LinkedHashMap<String, String>();
        prepareDiff();
        if(mode.equalsIgnoreCase(OPR_UPDATE)) {
            // Properties diff calculate modified channels only because add or delete channel has properties
            preparePropsDif();
        }
    }

    private void prepareDiff() { // Just maintain the order from more important to less since this is stored in LinkedHashMap 
        isOrderChanged();
        isPrimStateChanged();
        isPrimaryScheduleChanged();
        isSecStateChanged();
        isSecondaryScheduleChanged();
        isUpdateScheduleChanged();
        isRepairScheduleChanged();
        isPostPoneScheduleChanged();
        isExemptBlackoutChanged();
        if(!main.isCloudModel()) {
	        isWOWForUrgentChanged();
	        isWOWForPrimarySchChanged();
	        isWOWForSecondarySchChanged();
	        isWOWForUpdateSchChanged();
	        isWOWForRepairSchChanged();
        }
        isTypeChanged();
        isTitleChanged();
        setCurrentChannel();
    }
    
    private void setCurrentChannel() {
        if (mode.equals(OPR_ADD) || mode.equals(OPR_UPDATE)) {
            setChannel(newChannel);
        } else if (mode.equals(OPR_DELETE)) {
            setChannel(oldChannel);
        }
    }

    private void preparePropsDif() {
        calcAddedProbs();
        calcModifiedProps();
        calcDeletedProbs();
    }

    public boolean hasChannelInfoUpdated() {
        return !channelDiffInfo.isEmpty();
    }

    public boolean hasChannelPropInfoUpdated() {
        return !(addedPropsMap.isEmpty() && modifiedPropsMap.isEmpty() && deletedPropsMap.isEmpty());
    }

    public Map<String, Map<String, String>> getChannelDiffInfo() {
        return this.channelDiffInfo;
    }

    public Map<String, String> getAddedPropsMap() {
        return addedPropsMap;
    }

    public Map<String, Map<String, String>> getModifiedPropsMap() {
        return modifiedPropsMap;
    }   

    public Map<String, String> getDeletedPropsMap() {
        return deletedPropsMap;
    }


    public boolean isRemovedChAlone() {
        return isRemovedChAlone;
    }

    public void setRemovedChAlone(boolean isRemovedChAlone) {
        this.isRemovedChAlone = isRemovedChAlone;
    }

    public void isTypeChanged() {
        String oldvalue = (null != oldChannel && null != oldChannel.getType()) ? oldChannel.getType().trim() : "";
        String newvalue = (null != newChannel && null != newChannel.getType()) ? newChannel.getType().trim() : "";

        if (oldvalue.isEmpty() && newvalue.isEmpty()) {
            return;
        }
        Map<String, String> map = updateDiff(oldvalue, newvalue);
        if (!map.isEmpty()) channelDiffInfo.put(CH_TYPE, map);
    }

    public void isTitleChanged() {
        String oldvalue = (null != oldChannel && null != oldChannel.getTitle()) ? oldChannel.getTitle().trim() : "";
        String newvalue = (null != newChannel && null != newChannel.getTitle()) ? newChannel.getTitle().trim() : "";

        if (oldvalue.isEmpty() && newvalue.isEmpty()) {
            return;
        }
        Map<String, String> map = updateDiff(oldvalue, newvalue);
        if (!map.isEmpty()) channelDiffInfo.put(CH_TITLE, map);
    }

    public void isOrderChanged() {
        String oldvalue = (null != oldChannel) ? Integer.toString(oldChannel.getOrder()) : "";
        String newvalue = (null != newChannel) ? Integer.toString(newChannel.getOrder()) : "";

        if (oldvalue.isEmpty() && newvalue.isEmpty()) {
            return;
        }
        Map<String, String> map = updateDiff(oldvalue, newvalue);
        if (!map.isEmpty()) channelDiffInfo.put(CH_ORDER, map);
    }

    public void isPrimStateChanged() {
        String oldvalue = (null != oldChannel && null != oldChannel.getState()) ? oldChannel.getState().trim() : "";
        String newvalue = (null != newChannel && null != newChannel.getState()) ? newChannel.getState().trim() : "";

        if (oldvalue.isEmpty() && newvalue.isEmpty()) {
            return;
        }
        Map<String, String> map = updateDiff(oldvalue, newvalue);
        if (!map.isEmpty()) channelDiffInfo.put(CH_PRIMARY_STATE, map);
    }

    public void isSecStateChanged() {
        String oldvalue = (null != oldChannel && null != oldChannel.getSecState()) ?
                oldChannel.getSecState().trim() : NO_STATE;
        String newvalue = (null != newChannel && null != newChannel.getSecState()) ?
                newChannel.getSecState().trim() : NO_STATE;

        Map<String, String> map = updateDiff(oldvalue, newvalue);
        if (!map.isEmpty()) channelDiffInfo.put(CH_SECONDARY_STATE, map);
    }

    public void isPrimaryScheduleChanged() {
        String oldvalue = (null != oldChannel && null != oldChannel.getInitScheduleString()) ?
                oldChannel.getInitScheduleString().trim() : DEFAULT_ACTIVE_PERIOD;
        String newvalue = (null != newChannel && null != newChannel.getInitScheduleString()) ?
                newChannel.getInitScheduleString().trim() : DEFAULT_ACTIVE_PERIOD;

        oldvalue = oldvalue.equalsIgnoreCase(STR_NULL) ? "" : oldvalue;
        newvalue = newvalue.equalsIgnoreCase(STR_NULL) ? "" : newvalue;

        Map<String, String> map = updateDiff(oldvalue, newvalue);
        if (!map.isEmpty()) channelDiffInfo.put(CH_PRIMARY_SCHEDLUE, map);
    }

    public void isSecondaryScheduleChanged() {
        String oldvalue = (null != oldChannel && null != oldChannel.getSecScheduleString()) ?
                oldChannel.getSecScheduleString().trim() : NO_SCHEDULE;
        String newvalue = (null != newChannel && null != newChannel.getSecScheduleString()) ?
                newChannel.getSecScheduleString().trim() : NO_SCHEDULE;

        oldvalue = oldvalue.equalsIgnoreCase(STR_NULL) ? "" : oldvalue;
        newvalue = newvalue.equalsIgnoreCase(STR_NULL) ? "" : newvalue;

        Map<String, String> map = updateDiff(oldvalue, newvalue);
        if (!map.isEmpty()) channelDiffInfo.put(CH_SECONDARY_SCHEDULE, map);
    }

    public void isUpdateScheduleChanged() {
        String oldvalue = (null != oldChannel && null != oldChannel.getUpdateScheduleString()) ?
                oldChannel.getUpdateScheduleString().trim() : NO_SCHEDULE;
        String newvalue = (null != newChannel && null != newChannel.getUpdateScheduleString()) ?
                newChannel.getUpdateScheduleString().trim() : NO_SCHEDULE;

        oldvalue = oldvalue.equalsIgnoreCase(STR_NULL) ? "" : oldvalue;
        newvalue = newvalue.equalsIgnoreCase(STR_NULL) ? "" : newvalue;

        Map<String, String> map = updateDiff(oldvalue, newvalue);
        if (!map.isEmpty()) channelDiffInfo.put(CH_UPDATE_SCHEDULE, map);
    }

    public void isRepairScheduleChanged() {
        String oldvalue = (null != oldChannel && null != oldChannel.getVerRepairScheduleString()) ?
                oldChannel.getVerRepairScheduleString().trim() : NO_SCHEDULE;
        String newvalue = (null != newChannel && null != newChannel.getVerRepairScheduleString()) ?
                newChannel.getVerRepairScheduleString().trim() : NO_SCHEDULE;

        oldvalue = oldvalue.equalsIgnoreCase(STR_NULL) ? "" : oldvalue;
        newvalue = newvalue.equalsIgnoreCase(STR_NULL) ? "" : newvalue;

        Map<String, String> map = updateDiff(oldvalue, newvalue);
        if (!map.isEmpty()) channelDiffInfo.put(CH_REPAIR_SCHEDULE, map);
    }

    public void isPostPoneScheduleChanged() {
        String oldvalue = (null != oldChannel && null != oldChannel.getPostponeScheduleString()) ?
                oldChannel.getPostponeScheduleString().trim() : NO_SCHEDULE;
        String newvalue = (null != newChannel && null != newChannel.getPostponeScheduleString()) ?
                newChannel.getPostponeScheduleString().trim() : NO_SCHEDULE;

        oldvalue = oldvalue.equalsIgnoreCase(STR_NULL) ? "" : oldvalue;
        newvalue = newvalue.equalsIgnoreCase(STR_NULL) ? "" : newvalue;

        Map<String, String> map = updateDiff(oldvalue, newvalue);
        if (!map.isEmpty()) channelDiffInfo.put(CH_POSTPONE_SCHEDULE, map);
    }

    public void isExemptBlackoutChanged() {
        String oldvalue = (null != oldChannel && oldChannel.isExemptFromBlackout()) ? VALID : INVALID;
        String newvalue = (null != newChannel && newChannel.isExemptFromBlackout()) ? VALID : INVALID;

        Map<String, String> map = updateDiff(oldvalue, newvalue);
        if (!map.isEmpty()) channelDiffInfo.put(CH_EXEMPT_BLACKOUT, map);
    }

    public void isWOWForUrgentChanged() {
        String oldvalue = (null != oldChannel && oldChannel.isWowEnabled()) ? VALID : INVALID;
        String newvalue = (null != newChannel && newChannel.isWowEnabled()) ? VALID : INVALID;

        Map<String, String> map = updateDiff(oldvalue, newvalue);
        if (!map.isEmpty()) channelDiffInfo.put(CH_WOW_URGENT, map);
    }

    public void isWOWForPrimarySchChanged() {
        String oldvalue = (null != oldChannel && oldChannel.getWOWForInit()) ? VALID : INVALID;
        String newvalue = (null != newChannel && newChannel.getWOWForInit()) ? VALID : INVALID;

        Map<String, String> map = updateDiff(oldvalue, newvalue);
        if (!map.isEmpty()) channelDiffInfo.put(CH_WOW_PRIMARY, map);
    }

    public void isWOWForSecondarySchChanged() {
        String oldvalue = (null != oldChannel && oldChannel.getWOWForSec()) ? VALID : INVALID;
        String newvalue = (null != newChannel && newChannel.getWOWForSec()) ? VALID : INVALID;

        Map<String, String> map = updateDiff(oldvalue, newvalue);
        if (!map.isEmpty()) channelDiffInfo.put(CH_WOW_SECONDARY, map);
    }

    public void isWOWForUpdateSchChanged() {
        String oldvalue = (null != oldChannel && oldChannel.getWOWForUpdate()) ? VALID : INVALID;
        String newvalue = (null != newChannel && newChannel.getWOWForUpdate()) ? VALID : INVALID;

        Map<String, String> map = updateDiff(oldvalue, newvalue);
        if (!map.isEmpty()) channelDiffInfo.put(CH_WOW_UPDATE, map);
    }

    public void isWOWForRepairSchChanged() {
        String oldvalue = (null != oldChannel && oldChannel.getWOWForRepair()) ? VALID : INVALID;
        String newvalue = (null != newChannel && newChannel.getWOWForRepair()) ? VALID : INVALID;

        Map<String, String> map = updateDiff(oldvalue, newvalue);
        if (!map.isEmpty()) channelDiffInfo.put(CH_WOW_REPAIR, map);
    }

    private void setChannel(Channel ch) {
        this.currentChannel = ch;
    }
    public Channel getChannel() {
        return currentChannel;
    }

    private Map<String, String> updateDiff(String oldvalue, String newvalue) {
        Map<String, String> map = new HashMap<String, String>();
        if (mode.equals(OPR_ADD)) {
            map.put(KEY_NEW_VALUE, newvalue);
        } else if (mode.equals(OPR_DELETE)) {
            map.put(KEY_OLD_VALUE, oldvalue);
        } else if (mode.equalsIgnoreCase(OPR_UPDATE)) {
            if (!oldvalue.equals(newvalue)) {
                map.put(KEY_OLD_VALUE, oldvalue);
                map.put(KEY_NEW_VALUE, newvalue);
            }
        }
        return map;
    }

    private void calcAddedProbs() {
        if (null == oldChannel || null == newChannel) {
            return;
        }
        Enumeration oldProperties, newProperties;
        try {
            oldProperties = oldChannel.getPropertyKeys();
            newProperties = newChannel.getPropertyKeys();

            List<String> oldkeys = new ArrayList<String>();
            List<String> newkeys = new ArrayList<String>();

            while(oldProperties.hasMoreElements()) {
                oldkeys.add((String) oldProperties.nextElement());
            }

            while(newProperties.hasMoreElements()) {
                newkeys.add((String) newProperties.nextElement());
            }
            if (newkeys.containsAll(oldkeys)) {
                newkeys.removeAll(oldkeys);
            }
            for (String keyval : newkeys) {
                if (null != newChannel.getProperty(keyval)) {
                    addedPropsMap.put(keyval, newChannel.getProperty(keyval));
                }
            }
        }  catch (Exception ex) {
            debug("Failed to calculate added channel props");
            if (DEBUG) ex.printStackTrace();
        }
        debug("AddedPropsMap.size(): " + addedPropsMap.size());
    }

    private void calcModifiedProps() {
        if (null == oldChannel || null == newChannel) {
            return;
        }

        String oldvalue, tmpstr;
        Map<String, String> tmpMap;

        try {
            String[] oldSubpairs = oldChannel.getPropertyPairs();
            for (int i = 0, length = oldSubpairs.length; i < length; i += 2) {
                oldvalue = newChannel.getProperty(oldSubpairs [i]);
                if (hasChanged(oldSubpairs [i])) {
                    if (null != oldvalue && !oldSubpairs [i + 1].equals(oldvalue)) {
                        tmpMap = new HashMap<String, String>(2);
                        tmpstr = oldChannel.getProperty(oldSubpairs[i]);
                        tmpMap.put(KEY_OLD_VALUE, tmpstr.equals(STR_NULL) ? STR_NONE : tmpstr );
                        tmpstr = newChannel.getProperty(oldSubpairs[i]);
                        tmpMap.put(KEY_NEW_VALUE, tmpstr.equals(STR_NULL) ? STR_NONE : tmpstr );
                        modifiedPropsMap.put(oldSubpairs[i], tmpMap);
                    }
                }
            }

        } catch (Exception ex) {
            // Skip it now
            debug("Failed to calculate modified channel props");
            if (DEBUG) ex.printStackTrace();

        }
        debug("ModifiedPropsMap.size(): " + modifiedPropsMap.size());
    }

    private void calcDeletedProbs() {
        if (null == oldChannel) {
            return;
        }
        String key;
        try {
            Enumeration oldproperties = oldChannel.getPropertyKeys();
            while(oldproperties.hasMoreElements()) {
                key = (String) oldproperties.nextElement();
                if (null == newChannel || null == newChannel.getProperty(key)) {
                    deletedPropsMap.put(key, oldChannel.getProperty(key));
                }
            }

        }  catch (Exception ex) {
            // Skip it now
            debug("Failed to calculate deleted channel props");
            if (DEBUG) ex.printStackTrace();
        }
        debug("DeletedPropsMap.size(): " + deletedPropsMap.size());
    }
    
    private boolean hasChanged(String key)  throws Exception {

        String val = oldChannel.getProperty(key);

        if (null == val) {
            return true;
        }
        // it hasn't changed if and only val equals the value in the newchannel.
        return !val.equals(newChannel.getProperty(key));
    }

    private void debug(String msg) {
        if (DEBUG) System.out.println("ChannelDiffer: " + msg);
    }
}
