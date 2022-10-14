// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.util;

import java.util.*;

import com.marimba.apps.subscription.common.*;
import com.marimba.apps.subscription.common.intf.IPersistifyCheck;
import com.marimba.apps.subscription.common.objects.*;

import com.marimba.apps.subscriptionmanager.intf.*;

import com.marimba.castanet.schedule.Schedule;
import com.marimba.castanet.schedule.ScheduleInfo;

import com.marimba.webapps.intf.*;

/**
 * This class maps a target with a channel. It encapsulates the properties of the channel as well as the target. Useful for sorting and display purposes 1.14,
 * 12/15/2002 Rahul Ravulur
 */
public class TargetChannelMap
    implements ISubscriptionConstants,
                   IMapProperty,
                   IAppConstants,
                   IPersistifyCheck {
    private String    name; // the name of the target that is being mapped
    private String    type; // the type of the target
    private String    id; // the id of the target.
    private String    isSelectedTarget = "false";
    private String    isChecked = "false"; // indicates whether the element was checked.
    private Hashtable channels = new Hashtable(3); // list of channels mapped to this target

    // List of channels mapped to this target which are not checked for consistency. The channels to this
    // Hashtable can be added using method addMiscChannels. This is used for adding channels which are
    // not selected in the package view page by the user.
    private Hashtable miscChannels = new Hashtable(3);

    // the target this object is mapping
    private Target target;

    // the Subscription DN for the subscription assigned to this target
    // A target may be assigned different DN.
    private String subContainer;

    // the channel properties that are a intersection of all the channels this target is mapped to.
    private Channel fakeChannel;

    /**
     * Creates a new TargetChannelMap object.
     *
     * @param target REMIND
     * @param sub REMIND
     */
    public TargetChannelMap(Target target,
                            String sub) {
        this.target       = target;
        this.subContainer = sub;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getName() {
        return target.getName();
    }

    /**
     * return display friendly string
     *
     * @return REMIND
     */
    public String getType() {
        return target.getType();
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getIsSelectedTarget() {
        return isSelectedTarget;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getSubContainer() {
        return subContainer;
    }

    /**
     * REMIND
     *
     * @param starget REMIND
     */
    public void setIsSelectedTarget(String starget) {
        isSelectedTarget = starget;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getId() {
        return target.getID();
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public int getOrder() {
        return fakeChannel.getOrder();
    }

    /**
     * REMIND
     *
     * @param name REMIND
     */
    public void setName(String name) {
        target.setName(name);
    }

    /**
     * REMIND
     *
     * @param type REMIND
     *
     * @throws SystemException REMIND
     */
    public void setType(String type)
        throws SystemException {
        if (Target.validTargetType(type)) {
            target.setType(type);
        }
    }

    /**
     * REMIND
     *
     * @param id REMIND
     */
    public void setId(String id) {
        target.setID(id);
    }

    /**
     * REMIND
     *
     * @param target REMIND
     */
    public void setTarget(Target target) {
        this.target = target;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public Target getTarget() {
        return this.target;
    }

    /**
     * REMIND
     *
     * @param url REMIND
     *
     * @return REMIND
     */
    public Channel getChannel(String url) {
        return (Channel) channels.get(url);
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public Hashtable getChannels() {
        return this.channels;
    }

    /**
     * REMIND
     *
     * @param ch REMIND
     *
     * @throws SystemException REMIND
     */
    public void addChannel(Channel ch)
        throws SystemException {
        if (fakeChannel != null) {
            updateConsistency(ch);
        } else {

            try {
                fakeChannel = (Channel) ch.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            }
        }

        channels.put(ch.getUrl(), ch);
    }

    /**
     * REMIND
     *
     * @param ch REMIND
     *
     * @throws SystemException REMIND
     */
    public void addChannelEdit(Channel ch)
        throws SystemException {

        if (fakeChannel != null) {
            updateConsistency(ch);
            fakeChannel.setType(ch.getType());
        } else {
            fakeChannel = new Channel(ch.getUrl(),ch.getTitle(),ch.getState(),ch.getSecState());
            fakeChannel.setType(ch.getType());
        }

        channels.put(ch.getUrl(), ch);
    }

    /**
     * Returns the channels stored which are not part of consistency check.
     *
     * @return Hashtable containing Channels, which are not checked for the consistency
     */
    public Hashtable getMiscChannels() {
        return miscChannels;
    }

    /**
     * Add channel without altering fakeChannel contents.
     *
     * @param ch Channal reference to add
     */
    public void addMiscChannels(Channel ch) {
        miscChannels.put(ch.getUrl(), ch);
    }

    /**
     * Remove the channel from the miscellaneous channels.
     *
     * @param chUrl Channel reference to remove
     */
    public void removeMiscChannel(String chUrl) {
        miscChannels.remove(chUrl);
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public Channel getChannel() {
        return fakeChannel;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getInitSchedule() {
        return fakeChannel.getInitScheduleString();
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getSecSchedule() {
        return fakeChannel.getSecScheduleString();
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getVerRepairSchedule() {
        return fakeChannel.getVerRepairScheduleString();
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getPostponeSchedule() {
        return fakeChannel.getPostponeScheduleString();
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getUpdateSchedule() {
        return fakeChannel.getUpdateScheduleString();
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getState() {
        return fakeChannel.getState();
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getSecState() {
        return fakeChannel.getSecState();
    }

    /**
     * REMIND
     *
     * @param state REMIND
     */
    public void setState(String state) {
        fakeChannel.setState(state);
    }

    /**
     * REMIND
     *
     * @param state REMIND
     */
    public void setSecState(String state) {
        fakeChannel.setSecState(state);
    }

    /**
     * REMIND
     *
     * @param sched REMIND
     *
     * @throws SystemException REMIND
     */
    public void setInitSchedule(String sched)
        throws SystemException {
        fakeChannel.setInitSchedule(sched);
    }

    /**
     * REMIND
     *
     * @param secSched REMIND
     *
     * @throws SystemException REMIND
     */
    public void setSecSchedule(String secSched)
        throws SystemException {
        fakeChannel.setSecSchedule(secSched);
    }

    /**
     * REMIND
     *
     * @param updSched REMIND
     *
     * @throws SystemException REMIND
     */
    public void setUpdateSchedule(String updSched)
        throws SystemException {
        fakeChannel.setUpdateSchedule(updSched);
    }

    /**
     * REMIND
     *
     * @param verSched REMIND
     *
     * @throws SystemException REMIND
     */
    public void setVerRepairSchedule(String verSched)
        throws SystemException {
        fakeChannel.setVerRepairSchedule(verSched);
    }

    /**
     * REMIND
     *
     * @param postpone REMIND
     *
     * @throws SystemException REMIND
     */
    public void setPostponeSchedule(String postpone)
        throws SystemException {
        fakeChannel.setPostponeSchedule(postpone);
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public ScheduleInfo getInitScheduleInfo() {
        return fakeChannel.getInitScheduleInfo();
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public ScheduleInfo getSecScheduleInfo() {
        return fakeChannel.getSecScheduleInfo();
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public ScheduleInfo getUpdateScheduleInfo() {
        return fakeChannel.getUpdateScheduleInfo();
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public ScheduleInfo getVerRepairScheduleInfo() {
        return fakeChannel.getVerRepairScheduleInfo();
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public ScheduleInfo getPostponeScheduleInfo() {
        return fakeChannel.getPostponeScheduleInfo();
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getTitle() {
        return fakeChannel.getTitle();
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getUrl() {
        return fakeChannel.getUrl();
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getContentType() {
        return fakeChannel.getType();
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getExemptFromBlackout() {
        return String.valueOf(fakeChannel.isExemptFromBlackout());
    }

    /**
     * REMIND
     *
     */
    public void setExemptFromBlackout(boolean exemptBlackout ) {
        fakeChannel.setExemptFromBlackout(exemptBlackout);
    }
    /**
     * REMIND
     *
     * @return REMIND
     */

    public String getWowEnabled() {
        return String.valueOf(fakeChannel.isWowEnabled());
    }

    /**
     * REMIND
     *
     */
    public void setWowEnabled(boolean wowEnabled ) {
        fakeChannel.setWowEnabled(wowEnabled);
    }
    /**
     * REMIND
     *
     * @return REMIND
     */

    public String getWowForInit() {
        return String.valueOf(fakeChannel.getWOWForInit());
    }

    /**
     * REMIND
     *
     */
    public void setWowForInit(boolean wowEnabled ) {
        fakeChannel.setWOWForInit(wowEnabled);
    }
    /**
     * REMIND
     *
     * @return REMIND
     */

    public String getWowForSec() {
        return String.valueOf(fakeChannel.getWOWForSec());
    }

    /**
     * REMIND
     *
     */
    public void setWowForSec(boolean wowEnabled ) {
        fakeChannel.setWOWForSec(wowEnabled);
    }
    /**
     * REMIND
     *
     * @return REMIND
     */

    public String getWowForUpdate() {
        return String.valueOf(fakeChannel.getWOWForUpdate());
    }

    /**
     * REMIND
     *
     */
    public void setWowForUpdate(boolean wowEnabled ) {
        fakeChannel.setWOWForUpdate(wowEnabled);
    }
    /**
     * REMIND
     *
     * @return REMIND
     */

    public String getWowForRepair() {
        return String.valueOf(fakeChannel.getWOWForRepair());
    }

    /**
     * REMIND
     *
     */
    public void setWowForRepair(boolean wowEnabled ) {
        fakeChannel.setWOWForRepair(wowEnabled);
    }

    /**
     * REMIND
     *
     * @param property REMIND
     *
     * @return REMIND
     */
    public Object getValue(String property) {
        if (CH_STATE_KEY.equals(property)) {
            return getState();
        } else if (CH_TITLE_KEY.equals(property)) {
            return getTitle();
        } else if (CH_URL_KEY.equals(property)) {
            return getUrl();
        } else if (TARGETCHANNELMAP_CONTENTTYPE.equals(property)) {
             return getContentType();
        } else if (CH_INITSCHED_KEY.equals(property)) {
            return getInitSchedule();
        } else if (CH_SECSTATE_KEY.equals(property)) {
            return getSecState();
        } else if (CH_SECSCHED_KEY.equals(property)) {
            return getSecSchedule();
        } else if (CH_UPDATESCHED_KEY.equals(property)) {
            return getUpdateSchedule();
        } else if (CH_VERREPAIRSCHED_KEY.equals(property)) {
            return getVerRepairSchedule();
        } else if (CH_POSTPONESCHED_KEY.equals(property)) {
            return getPostponeSchedule();
        } else if (CH_INITSCHED_INFO_KEY.equals(property)) {
            return getInitScheduleInfo();
        } else if (CH_SECSCHED_INFO_KEY.equals(property)) {
            return getSecScheduleInfo();
        } else if (CH_UPDATESCHED_INFO_KEY.equals(property)) {
            return getUpdateScheduleInfo();
        } else if (CH_VERREPAIRSCHED_INFO_KEY.equals(property)) {
            return getVerRepairScheduleInfo();
        } else if (CH_POSTPONESCHED_INFO_KEY.equals(property)) {
            return getPostponeSchedule();
        } else if (TARGETCHANNELMAP_CHECKED.equals(property)) {
            return isChecked;
        } else if (TARGETCHANNELMAP_SUBCONTAINER.equals(property)) {
            return subContainer;
        }

        return null;
    }

    /**
     * REMIND
     *
     * @param property REMIND
     * @param value REMIND
     */
    public void setValue(String property,
                    Object value) {
        String valStr = "";

        try {
            valStr = (String) value;
        } catch (ClassCastException ex) {
            ex.printStackTrace();

            return;
        }

        try {
            if (CH_STATE_KEY.equals(property)) {
                setState(valStr);
            } else if (CH_INITSCHED_KEY.equals(property)) {
                setInitSchedule(valStr);
            } else if (CH_SECSTATE_KEY.equals(property)) {
                setSecState(valStr);
            } else if (CH_SECSCHED_KEY.equals(property)) {
                setSecSchedule(valStr);
            } else if (CH_UPDATESCHED_KEY.equals(property)) {
                setUpdateSchedule(valStr);
            } else if (CH_VERREPAIRSCHED_KEY.equals(property)) {
                setVerRepairSchedule(valStr);
            } else if (CH_POSTPONESCHED_KEY.equals(property)) {
                setPostponeSchedule(valStr);
            } else if (TARGETCHANNELMAP_CHECKED.equals(property)) {
                isChecked = valStr;
            } else if (TARGETCHANNELMAP_SUBCONTAINER.equals(property)) {
                subContainer = valStr;
            }
        } catch (SystemException exc) {
            // REMIND::RCR handle this exception correctly
            exc.printStackTrace();
        }
    }

    /**
     * This method updates the state and schedule properties of this object when a channel is added to it. If the channels contained in this class have the
     * same value for a property, it is deemed to be consistent. Even, if one channel differs, the property is inconsistent This method is used to check when
     * a new channel is added to the set. It will update the properties of this class, using the following logic. Pick the first channel in the set. If the
     * property was inconsistent across the channels to begin with, it stays inconsistent(since channels cannot be removed from this set, only added) If the
     * property was consistent, compare it with the new channel for equality. If equal, it stays that way, if not set it to inconsistent.
     *
     * @param newChannel REMIND
     *
     * @throws SystemException REMIND
     */
    private void updateConsistency(Channel newChannel)
        throws SystemException {
        if (!ISubscriptionConstants.INCONSISTENT.equals(fakeChannel.getState())) {
            if (!Utils.channelAttrCompare(newChannel.getState(), fakeChannel.getState())) {
                fakeChannel.setState(ISubscriptionConstants.INCONSISTENT);
            }
        }

        if (!ISubscriptionConstants.INCONSISTENT.equals(fakeChannel.getSecState())) {
            if (!Utils.channelAttrCompare(newChannel.getSecState(), fakeChannel.getSecState())) {
                fakeChannel.setSecState(ISubscriptionConstants.INCONSISTENT);
            }
        }

        if (!ISubscriptionConstants.INCONSISTENT.equals(fakeChannel.getInitScheduleString())) {
            if (!Utils.channelAttrCompare(newChannel.getInitScheduleString(), fakeChannel.getInitScheduleString())) {
                if (DEBUG) {
                    System.out.println("TargetChannelMap: setting the init schedule to inconsistent for chn= " + fakeChannel.getUrl());
                }

                fakeChannel.setInitSchedule(ISubscriptionConstants.INCONSISTENT);
            }
        }

        if (!ISubscriptionConstants.INCONSISTENT.equals(fakeChannel.getSecScheduleString())) {
            if (!Utils.channelAttrCompare(newChannel.getSecScheduleString(), fakeChannel.getSecScheduleString())) {
                fakeChannel.setSecSchedule(ISubscriptionConstants.INCONSISTENT);
            }
        }

        if (!ISubscriptionConstants.INCONSISTENT.equals(fakeChannel.getUpdateScheduleString())) {
            if (!Utils.channelAttrCompare(newChannel.getUpdateScheduleString(), fakeChannel.getUpdateScheduleString())) {

                fakeChannel.setUpdateSchedule(ISubscriptionConstants.INCONSISTENT);
            }
        }

        if (!ISubscriptionConstants.INCONSISTENT.equals(fakeChannel.getVerRepairScheduleString())) {
            if (!Utils.channelAttrCompare(newChannel.getVerRepairScheduleString(), fakeChannel.getVerRepairScheduleString())) {
                fakeChannel.setVerRepairSchedule(ISubscriptionConstants.INCONSISTENT);
            }
        }

        if (!ISubscriptionConstants.INCONSISTENT.equals(fakeChannel.getPostponeScheduleString())) {
            if (!Utils.channelAttrCompare(newChannel.getPostponeScheduleString(), fakeChannel.getPostponeScheduleString())) {
                fakeChannel.setPostponeSchedule(ISubscriptionConstants.INCONSISTENT);
            }
        }

        if (!ISubscriptionConstants.INCONSISTENT.equals(fakeChannel.getOrderState())) {
            if ( newChannel.getOrder() != fakeChannel.getOrder() ) {
                fakeChannel.setOrderState(ISubscriptionConstants.INCONSISTENT);
            }
        }

        if (!ISubscriptionConstants.INCONSISTENT.equals(fakeChannel.getBlackOutState())) {
            if ( newChannel.isExemptFromBlackout() != fakeChannel.isExemptFromBlackout() ) {
                fakeChannel.setBlackOutState(ISubscriptionConstants.INCONSISTENT);
            }
        }
    }

    /**
     * Used to determine if the schedules are inconsistent.
     */
    public static boolean equalScheduleStrings(String sch1,
                                               String sch2) {
        if ((sch1 == null) && (sch2 != null)) {
            return false;
        }

        if ((sch1 != null) && (sch2 == null)) {
            return false;
        }

        if ((sch1 == null) && (sch2 == null)) {
            return true;
        }

        //They are both not null after this point.  compare the strings
        if (sch1.equals(sch2)) {
            /*Check to see if one of the values is inconsistent already.  This
             *can happen if sch1 or sch2 is inconsistent when selected. In which case, false
             *should be returned
             */
            if (ISubscriptionConstants.INCONSISTENT.equals(sch1) || ISubscriptionConstants.INCONSISTENT.equals(sch2)) {
                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[Name:").append(name);
        buffer.append(";Type:").append(type);
        buffer.append(";Id:").append(id);
        buffer.append(";IsSelectedTarget:").append(isSelectedTarget);
        buffer.append(";IsChecked:").append(isChecked);
        buffer.append(";Channels:").append(channels);
        buffer.append(";Target:").append(target);
        buffer.append(";SubContainer:").append(subContainer);
        buffer.append(";FakeChannel:").append(fakeChannel);
        buffer.append(";MiscChannels:").append(miscChannels);
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * Implement the IPersistifyCheck.getPersistID method to get the unique values of this object.
     * This method would be helpful for persisting the check boxes values across the pages.
     * @return
     */
    public String getPersistID() {
        return target.getID();
    }
}
