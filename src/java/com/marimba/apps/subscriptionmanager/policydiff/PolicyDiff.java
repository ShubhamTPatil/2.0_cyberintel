// Copyright 2009, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.policydiff;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Channel;
import static com.marimba.apps.subscriptionmanager.approval.IApprovalPolicyConstants.ADD_OPERATION;
import static com.marimba.apps.subscriptionmanager.approval.IApprovalPolicyConstants.POLICY_PENDING;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IPolicyDiffConstants;
import com.marimba.tools.util.DebugFlag;
import com.marimba.webapps.intf.SystemException;

import java.util.*;

/**
 *  This class used to log the Subscription object changes.
 *  The channel and tuner properties added, removed, changed information is logged in audit log *
 *
 * @author Tamilselvan Teivasekamani
 * @version $Revision$,  $Date$
 *
 */

public class PolicyDiff implements ISubscriptionConstants, IAppConstants, IPolicyDiffConstants {
    public int DEBUG = DebugFlag.getDebug("SECURITY/MGR");

    private SubscriptionMain main;
    private String user;
    private String targetId;
    private String targetName;
    private String targetType;
    private int policyAction = ADD_OPERATION;
    private int policyStatus = POLICY_PENDING;

    private ISubscription oldsub, newsub;
    protected String blackoutSchedule = "";
    protected StringBuffer blackoutProps;
    protected boolean isBlackoutChanged = false;
    protected Map<String, String> addedPropsMap;
    protected Map<String, String> deletedPropsMap;
    protected Map<String, ChannelDiffer> addedChannelsMap;
    protected Map<String, ChannelDiffer> deletedChannelsMap;
    protected Map<String, ChannelDiffer> modifiedChannelPropsMap;

    protected Map<String, Map<String, String>> modifiedPropsMap;
    protected Map<String, Map<String, String>> addedDummyChannelsMap;
    protected Map<String, Map<String, String>> deletedDummyChannelsMap;
    protected Map<String, Map<String, Map<String, String>>> modifiedDummyChannelsMap;

    private String[] propTypes = { PROP_TUNER_KEYWORD, PROP_SERVICE_KEYWORD, PROP_CHANNEL_KEYWORD,
            PROP_ALL_CHANNELS_KEYWORD, PROP_DEVICES_KEYWORD, PROP_POWER_KEYWORD, PROP_SECURITY_KEYWORD, PROP_AMT_KEYWORD, PRO_AMT_ALARMCLK_KEYWORD,
            PROP_OSM_KEYWORD, PROP_PBACKUP_KEYWORD, PROP_SCAP_SECURITY_KEYWORD, PROP_USGCB_SECURITY_KEYWORD, PROP_CUSTOM_SECURITY_KEYWORD };

    public PolicyDiff() {
        init();
    }

    public PolicyDiff(ISubscription oldsub, ISubscription newsub, SubscriptionMain subMain) {
        this.oldsub = oldsub;
        this.newsub = newsub;
        this.main = subMain;
        if (null != newsub) { // Always try to get it from recent SUB object
            this.targetId = newsub.getTargetID();
            this.targetName = newsub.getTargetName();
            this.targetType = newsub.getTargetType();
        } else if (null != oldsub) {
            this.targetId = oldsub.getTargetID();
            this.targetName = oldsub.getTargetName();
            this.targetType = oldsub.getTargetType();
        }
        init();
        calculateFromSubObject();
    }

    private void init() {
        // For channels
        this.addedChannelsMap = new LinkedHashMap<String, ChannelDiffer>(10);
        this.deletedChannelsMap = new LinkedHashMap<String, ChannelDiffer>(10);
        this.modifiedChannelPropsMap = new LinkedHashMap<String, ChannelDiffer>(10);
        // For properties
        this.blackoutProps = new StringBuffer(25);
        this.addedPropsMap = new LinkedHashMap<String, String>(10);
        this.deletedPropsMap = new LinkedHashMap<String, String>(10);
        this.modifiedPropsMap = new LinkedHashMap<String, Map<String,String>>(10);
        this.addedDummyChannelsMap = new LinkedHashMap<String, Map<String, String>>(10);
        this.deletedDummyChannelsMap = new LinkedHashMap<String, Map<String, String>>(10);
        this.modifiedDummyChannelsMap = new LinkedHashMap<String, Map<String, Map<String, String>>>(10);
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTargetId() {
        return (targetId.equalsIgnoreCase("all")) ? ALL_END_POINTS : targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetName() {
        return (targetName.equalsIgnoreCase("all")) ? ALL_END_POINTS : targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getTargetType() {
        return targetType;
    }

    public Map<String, ChannelDiffer> getAddedChannelsMap() {
        return addedChannelsMap;
    }

    public Map<String, ChannelDiffer> getDeletedChannelsMap() {
        return deletedChannelsMap;
    }

    public Map<String, String> getAddedPropsMap() {
        return addedPropsMap;
    }

    public Map<String, String> getDeletedPropsMap() {
        return deletedPropsMap;
    }

    public Map<String, Map<String, String>> getModifiedPropsMap() {
        return modifiedPropsMap;
    }

    public StringBuffer getBlackoutProps() {
        return blackoutProps;
    }

    public String getBlackoutSchedule() {
        return blackoutSchedule;
    }

    public void setBlackoutSchedule(String blackoutSchedule) {
        this.blackoutSchedule = blackoutSchedule;
    }

    public boolean isBlackoutChanged() {
        return isBlackoutChanged;
    }

    public void setBlackoutChanged(boolean isBlackoutChanged) {
        this.isBlackoutChanged = isBlackoutChanged;
    }

    public Map<String, ChannelDiffer> getModifiedChannelPropsMap() {
        return modifiedChannelPropsMap;
    }

    public void setModifiedChannelPropsMap(
            Map<String, ChannelDiffer> modifiedChannelPropsMap) {
        this.modifiedChannelPropsMap = modifiedChannelPropsMap;
    }

    public int getPolicyAction() {
        return policyAction;
    }

    public void setPolicyAction(int policyAction) {
        this.policyAction = policyAction;
    }

    public int getPolicyStatus() {
        return policyStatus;
    }

    public void setPolicyStatus(int policyStatus) {
        this.policyStatus = policyStatus;
    }

    private void calculateFromSubObject() {
        calcAddedChannels();
        calcModifiedChannels();
        // don't change this order addedDummyChannels and deletedChannels 
        // because of some condition added based on both function order
        calcAddedDummyChannels();
        calcDeletedChannels();
        calcModifiedDummyChannels();
        calcDeletedDummyChannels();
        calcAddedProbs();
        calcDeletedProbs();
        calcModifiedProps();
        calcBlackoutProbs();
    }
    public boolean hasDiff() {
    	if(addedChannelsMap.size() > 0 || deletedChannelsMap.size() > 0 || modifiedChannelPropsMap.size() > 0
    			|| blackoutProps.length() > 0 || addedPropsMap.size() > 0
    			|| deletedPropsMap.size() > 0
    			|| modifiedPropsMap.size() > 0
    			|| addedDummyChannelsMap.size() > 0
    			|| deletedDummyChannelsMap.size() > 0
    			|| modifiedDummyChannelsMap.size() > 0) {
    		return true;
    	}
    	System.out.println("There is no policy diff found");
    	return false;
    }

    public Map<String, Map<String, String>> getAddedDummyChannelsMap() {
        return addedDummyChannelsMap;
    }

    public void setAddedDummyChannelsMap(
            Map<String, Map<String, String>> addedDummyChannelsMap) {
        this.addedDummyChannelsMap = addedDummyChannelsMap;
    }

    public Map<String, Map<String, String>> getDeletedDummyChannelsMap() {
        return deletedDummyChannelsMap;
    }

    public void setDeletedDummyChannelsMap(
            Map<String, Map<String, String>> deletedDummyChannelsMap) {
        this.deletedDummyChannelsMap = deletedDummyChannelsMap;
    }

    public Map<String, Map<String, Map<String, String>>> getModifiedDummyChannelsMap() {
        return modifiedDummyChannelsMap;
    }

    public void setModifiedDummyChannelsMap(
            Map<String, Map<String, Map<String, String>>> modifiedDummyChannelsMap) {
        this.modifiedDummyChannelsMap = modifiedDummyChannelsMap;
    }

    public void prepareDiff() {

    }

    private void calcAddedChannels() {
        if (null == newsub) {
            return;
        }
        Channel oldchn, newchn;
        ChannelDiffer chnlDiffer;
        Enumeration newChannels = newsub.getChannels();
        while(newChannels.hasMoreElements()) {
            newchn = (Channel) newChannels.nextElement();
            try {
                if (null != oldsub) {
                    oldchn = oldsub.getChannel(newchn.getUrl());
                    if (null != oldchn) {
                        continue;
                    }
                    chnlDiffer = new ChannelDiffer(oldchn, newchn, OPR_ADD, main);
                    if (chnlDiffer.hasChannelInfoUpdated()) {
                        addedChannelsMap.put(newchn.getUrl(), chnlDiffer);
                    }
                } else {
                    chnlDiffer = new ChannelDiffer(null, newchn, OPR_ADD, main);
                    if (chnlDiffer.hasChannelInfoUpdated()) {
                        addedChannelsMap.put(newchn.getUrl(), chnlDiffer);
                    }
                }
            } catch (SystemException sysEx) {
                // Just skip it now
            }
        }
    }

    private void calcAddedDummyChannels() {
        if (null == newsub) {
            return;
        }
        Channel oldchn, newchn;
        DummyChannelDiffer chnlDiffer;

        Enumeration newDummyChannels = newsub.getDummyChannels();
        while(newDummyChannels.hasMoreElements()) {
            newchn = (Channel) newDummyChannels.nextElement();
            try {
                if (null != oldsub) {
                    if(null != oldsub.getDummyChannel(newchn.getUrl())) {
                        oldchn = oldsub.getDummyChannel(newchn.getUrl());
                    } else {
                        oldchn = null;
                    }
                    chnlDiffer = new DummyChannelDiffer(oldchn, newchn, OPR_ADD);
                    if(chnlDiffer.hasDummyChannelChanged()) {
                        addedDummyChannelsMap.put(newchn.getUrl(), chnlDiffer.getAddedPropsMap());
                    }
                } else {
                    chnlDiffer = new DummyChannelDiffer(null, newchn, OPR_ADD);
                    if(chnlDiffer.hasDummyChannelChanged()) {
                        addedDummyChannelsMap.put(newchn.getUrl(), chnlDiffer.getAddedPropsMap());
                    }
                }
            } catch (SystemException sysEx) {
                // Just skip it now
            }
        }
    }

    private void calcDeletedChannels() {
        if (null == oldsub) {
            return;
        }
        Channel oldchn;
        ChannelDiffer chnlDiffer;
        Enumeration oldChannels = oldsub.getChannels();
        while(oldChannels.hasMoreElements()) {
            oldchn = (Channel) oldChannels.nextElement();
            try {
                if (null != newsub && null != newsub.getChannel(oldchn.getUrl())) {
                    continue;
                }

                chnlDiffer = new ChannelDiffer(oldchn, null, OPR_DELETE, main);
                if (chnlDiffer.hasChannelInfoUpdated()) {
                    if(null != addedDummyChannelsMap.get(oldchn.getUrl())) {
                        chnlDiffer.setRemovedChAlone(true);
                    }
                    deletedChannelsMap.put(oldchn.getUrl(), chnlDiffer);
                }
            } catch (SystemException sysEx) {
                // Skipt it now
            }
        }

    }

    private void calcDeletedDummyChannels() {
        if (null == oldsub) {
            return;
        }
        Channel oldchn,newchn;
        DummyChannelDiffer chnlDiffer;
        Enumeration oldChannels = oldsub.getDummyChannels();
        while(oldChannels.hasMoreElements()) {
            oldchn = (Channel) oldChannels.nextElement();
            try {
                if(null != newsub && null != newsub.getDummyChannel(oldchn.getUrl())) {
                    newchn = newsub.getDummyChannel(oldchn.getUrl());
                } else {
                    newchn = null;
                }

                chnlDiffer = new DummyChannelDiffer(oldchn, newchn, OPR_DELETE);
                if(chnlDiffer.hasDummyChannelChanged()) {
                    deletedDummyChannelsMap.put(oldchn.getUrl(), chnlDiffer.getDeletedPropsMap());
                }
            } catch (SystemException sysEx) {
                // Skipt it now
            }
        }

    }
    private void calcModifiedChannels() {
        if (null == oldsub || null == newsub) {
            return;
        }
        Enumeration oldChannels = oldsub.getChannels();
        if (!oldChannels.hasMoreElements()) {
            return;
        }
        String chnlUrl;
        ChannelDiffer chnlDiffer;
        Channel oldChannel, newChannel;
        Enumeration newChannels = newsub.getChannels();

        while (newChannels.hasMoreElements()) {
            newChannel = (Channel) newChannels.nextElement();
            chnlUrl = newChannel.getUrl();
            try {
                oldChannel = oldsub.getChannel(chnlUrl);
                if (null == oldChannel) {
                    continue;
                }
                chnlDiffer = new ChannelDiffer(oldChannel, newChannel, OPR_UPDATE, main);
                if (chnlDiffer.hasChannelInfoUpdated() || chnlDiffer.hasChannelPropInfoUpdated()) {
                    modifiedChannelPropsMap.put(chnlUrl, chnlDiffer);
                }
            } catch(SystemException sysEx) {
                //Skip it, continue with next package
            }
        }
    }

    private void calcModifiedDummyChannels() {
        if (null == oldsub || null == newsub) {
            return;
        }
        Enumeration oldChannels = oldsub.getDummyChannels();
        if (!oldChannels.hasMoreElements()) {
            return;
        }
        String chnlUrl;
        DummyChannelDiffer chnlDiffer;
        Channel oldChannel, newChannel;
        Enumeration newChannels = newsub.getDummyChannels();

        while (newChannels.hasMoreElements()) {
            newChannel = (Channel) newChannels.nextElement();
            chnlUrl = newChannel.getUrl();
            try {
                oldChannel = oldsub.getDummyChannel(chnlUrl);
                if (null == oldChannel) {
                    continue;
                }
                chnlDiffer = new DummyChannelDiffer(oldChannel, newChannel, OPR_UPDATE);
                if(chnlDiffer.hasDummyChannelChanged()) {
                    modifiedDummyChannelsMap.put(chnlUrl, chnlDiffer.getModifiedPropsMap());
                }
            } catch(Exception ex) {
                System.out.println("Failed to calculate Modified dummy channels");
            }
        }
    }

    private void calcAddedProbs() {
        if (null == newsub) {
            return;
        }
        Enumeration oldProperties, newProperties;
        for (String propertyType : propTypes) {
            try {
                List<String> oldkeys = new ArrayList<String>(10);
                List<String> newkeys = new ArrayList<String>(10);

                if (null != oldsub) { // chance for null
                    oldProperties = oldsub.getPropertyKeys(propertyType);
                    if (null != oldProperties) {
                        while(oldProperties.hasMoreElements()) {
                            oldkeys.add((String) oldProperties.nextElement());
                        }
                    }
                }

                newProperties = newsub.getPropertyKeys(propertyType);
                while(newProperties.hasMoreElements()) {
                    newkeys.add((String) newProperties.nextElement());
                }
                if (newkeys.containsAll(oldkeys)) {
                    newkeys.removeAll(oldkeys);
                    for (String keyval : newkeys) {
                        if (null != newsub.getProperty(propertyType, keyval)) {
                            String suffix = "";
                            if(!(PROP_TUNER_KEYWORD.equals(propertyType))) {
                                suffix = "," + propertyType;
                            }
                            addedPropsMap.put(keyval+suffix, newsub.getProperty(propertyType, keyval));
                        }
                    }
                } else {
                	// if Admin choose exclude option from existing power options then 
                	// power options goes to deleted category and exclude option should be comes to added category 
                	if(PROP_POWER_KEYWORD.equalsIgnoreCase(propertyType)) {
	                	newkeys.removeAll(oldkeys);
	                    for (String keyval : newkeys) {
	                        if (null != newsub.getProperty(propertyType, keyval)) {
	                            String suffix = "";
	                            if(!(PROP_TUNER_KEYWORD.equals(propertyType))) {
	                                suffix = "," + propertyType;
	                            }
	                            addedPropsMap.put(keyval+suffix, newsub.getProperty(propertyType, keyval));
	                        }
	                    }
                	}
                }

            }  catch (SystemException sysEx) {
                // Skip it now
            }
        }
    }

    private void calcModifiedProps() {
        if (null == oldsub || null == newsub) {
            return;
        }

        String oldvalue;
        String tmpstr;

        Map<String, String> tmpMap;
        for (String propertyType : propTypes) {
            try {
                String[] oldSubpairs = oldsub.getPropertyPairs(propertyType);
                for (int i = 0, length = oldSubpairs.length; i < length; i += 2) {
                    oldvalue = newsub.getProperty(propertyType, oldSubpairs [i]);
                    if (hasChanged(oldSubpairs [i], propertyType)) {
                        if (null != oldvalue && !oldSubpairs [i + 1].equals(oldvalue)) {
                            tmpMap = new HashMap<String, String>(2);
                            tmpstr = oldsub.getProperty(propertyType,oldSubpairs[i]);
                            tmpMap.put(KEY_OLD_VALUE, tmpstr.equals(STR_NULL) ? STR_NONE : tmpstr );
                            tmpstr = newsub.getProperty(propertyType, oldSubpairs[i]);
                            tmpMap.put(KEY_NEW_VALUE, tmpstr.equals(STR_NULL) ? STR_NONE : tmpstr );
                            String suffix = "";
                            if(!(PROP_TUNER_KEYWORD.equals(propertyType))) {
                                suffix = "," + propertyType;
                            }
                            modifiedPropsMap.put(oldSubpairs[i]+suffix, tmpMap);
                        }
                    }
                }

            } catch (SystemException sysEx) {
                // Skip it now
            }
        }
    }

    private void calcDeletedProbs() {
        if (null == oldsub) {
            return;
        }
        String key;
        for (String propertyType : propTypes) {
            try {
                Enumeration oldproperties = oldsub.getPropertyKeys(propertyType);
                while(oldproperties.hasMoreElements()) {
                    key = (String) oldproperties.nextElement();
                    if (null == newsub || null == newsub.getProperty(propertyType, key)) {
                        String suffix = "";
                        if(!(PROP_TUNER_KEYWORD.equals(propertyType))) {
                            suffix = "," + propertyType;
                        }
                        deletedPropsMap.put(key+suffix, oldsub.getProperty(propertyType, key));
                    }
                }

            }  catch (SystemException sysEx) {
                // Skip it now
            }
        }
    }

    private void calcBlackoutProbs() {
        if (null == oldsub || null == newsub) {
            return;
        }

        String oldBlk = oldsub.getBlackOut();
        String newBlk = newsub.getBlackOut();
        boolean changed = false;
//        if((null != oldBlk && null != newBlk) && !oldBlk.equalsIgnoreCase(newBlk)) {
//            changed = true;
//        }
        if(null == oldBlk  && null == newBlk) {
            blackoutProps.append("");
        } else if(null == oldBlk && newBlk.length() > 0) {
            blackoutProps.append("New Blackout : ").append(newBlk);
            changed = true;
        } else if(null != oldBlk && (null == newBlk || "".equals(newBlk.trim()))) {
            newBlk = No_BLACKOUT_SCHEDULE;
            blackoutProps.append("Blackout changed from ")
                    .append("<s>")
                    .append(oldBlk)
                    .append("</s>")
                    .append("&nbsp;<b>To</b>&nbsp;")
                    .append(No_BLACKOUT_SCHEDULE_MSG);
            changed = true;
        } else if(null != oldBlk && !"".equals(oldBlk) && !"".equals(newBlk) ) {

            String[] tempold = oldBlk.split("anytime on | BLACKOUT ");
            String[] tempnew = newBlk.split("anytime on | BLACKOUT ");
            String scheduleold = "", weekDaysold = "", schedulenew = "" , weekDaysnew = "";

            if (!"".equals(tempold[0])) {
                scheduleold = tempold[0];
                weekDaysold = "";
            } else if ("".equals(tempold[0])){
                weekDaysold = tempold[1];
                scheduleold = tempold[2];
            }
            if (!"".equals(tempnew[0])) {
                schedulenew = tempnew[0];
                weekDaysnew = "";
            } else if ("".equals(tempnew[0])){
                weekDaysnew = tempnew[1];
                schedulenew = tempnew[2];
            }
            if(!weekDaysold.equals(weekDaysnew)) {
                if (blackoutProps.length() > 0) {
                    blackoutProps.append("<br>");
                }
                blackoutProps.append("Days changed from ")
                        .append("<s>")
                        .append(weekDaysold)
                        .append("</s>")
                        .append("&nbsp;<b>To</b>&nbsp;")
                        .append(weekDaysnew);
                changed = true;
            }
            if(!scheduleold.equals(schedulenew)) {
                if (blackoutProps.length() > 0) {
                    blackoutProps.append("<br>");
                }
                blackoutProps.append("Time changed from ")
                        .append("<s>")
                        .append(scheduleold)
                        .append("</s>")
                        .append("&nbsp;<b>To</b>&nbsp;")
                        .append(schedulenew);
                changed = true;
            }
        }
        if(changed) {
            setBlackoutSchedule(newBlk);
            setBlackoutChanged(true);
        }
    }

    private boolean hasChanged(String key, String type)  throws SystemException {

        String val = oldsub.getProperty(type, key);

        if (null == val) {
            return true;
        }
        // it hasn't changed if and only val equals the value in the newsub.
        return !val.equals(newsub.getProperty(type, key));
    }

    protected  String writeChannelInfo(Channel channel) {
        return "";
    }
}