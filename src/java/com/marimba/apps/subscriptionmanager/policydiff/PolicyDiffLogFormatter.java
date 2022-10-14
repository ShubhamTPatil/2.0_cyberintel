// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.policydiff;

import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;

import java.util.Map;

/**
 * PolicyDiffLogFormatter for populate diffing content which is used for logs
 *
 * @author Tamilselvan Teivasekamani
 * @version $Revision$,  $Date$
 */

public class PolicyDiffLogFormatter extends PolicyDiff {
	private SubscriptionMain main;
    private StringBuffer addedProps;
    private StringBuffer deletedProps;
    private StringBuffer modifiedProps;
    //    private StringBuffer blackoutProps;
    private StringBuffer addedChannels;
    private StringBuffer deletedChannels;

    public PolicyDiffLogFormatter(ISubscription oldsub, ISubscription newsub, SubscriptionMain main) {
        super(oldsub, newsub, main);
        this.main = main;
        init();
    }
    public PolicyDiffLogFormatter() {
        super();
        init();
    }

    private void init() {
        this.addedProps = new StringBuffer(100);
        this.deletedProps = new StringBuffer(100);
        this.modifiedProps = new StringBuffer(100);
//        this.blackoutProps = new StringBuffer(100);
        this.addedChannels = new StringBuffer(100);
        this.deletedChannels = new StringBuffer(100);
    }

    public void prepareDiff() {
        super.prepareDiff();
        for(String key : addedChannelsMap.keySet()) {
            addedChannels.append(key);
        }
        for(String key : deletedChannelsMap.keySet()) {
            deletedChannels.append(key);
        }
        for(String key : addedPropsMap.keySet()) {
            addedProps.append(key).append(" = ").append(addedPropsMap.get(key)).append(" ");
        }
        for(String key : deletedPropsMap.keySet()) {
            deletedProps.append(key).append(" = ").append(deletedPropsMap.get(key)).append(" ");
        }

        if (!modifiedPropsMap.isEmpty()) {
            Map<String, String> tmpMap;
            for(String key : modifiedPropsMap.keySet()) {
                tmpMap = modifiedPropsMap.get(key);
                modifiedProps.append(key).append(" from ").append(tmpMap.get("oldvalue")).append(" to ").append(tmpMap.get("newvalue"));
            }
        }
    }
    
    public String getAddedProps() {
        return addedProps.toString();
    }

    public String getDeletedProps() {
        return deletedProps.toString();
    }

    public String getModifiedProps() {
        return modifiedProps.toString();
    }

    public String getAddedChannels() {
        return addedChannels.toString();
    }

    public String getDeletedChannels() {
        return deletedChannels.toString();
    }

    protected  String writeChannelInfo(Channel channel) {
        StringBuffer channelinfo = new StringBuffer();

        channelinfo.append(" [URL : ").append(channel.getUrl());
        channelinfo.append(" Primary State : ").append(channel.getState());
        if(null != channel.getInitScheduleInfo()) {
            channelinfo.append(" Scheduled at : ").append(channel.getInitScheduleInfo());
        }
        if(null != channel.getSecState()) {
            channelinfo.append(" Secondary State : ").append(channel.getSecState());
        }
        if(null != channel.getSecScheduleString()) {
            channelinfo.append(" Scheduled at: ").append(channel.getSecScheduleString());
        }
        channelinfo.append(" Type : ").append(channel.getType());
        channelinfo.append(" Order : ").append(channel.getOrder());
        if(channel.isWowEnabled()) {
            channelinfo.append(" WOW Enabled for Urgent Package: ").append(channel.isWowEnabled());
        }
        if(channel.getWOWForInit()) {
            channelinfo.append(" WOW Enabled for Primary Schedule: ").append(channel.getWOWForInit());
        }
        if(channel.getWOWForSec()) {
            channelinfo.append(" WOW Enabled for Secondary Schedule: ").append(channel.getWOWForSec());
        }
        if(channel.getWOWForUpdate()) {
            channelinfo.append(" WOW Enabled for Update Schedule: ").append(channel.getWOWForUpdate());
        }
        if(channel.getWOWForRepair()) {
            channelinfo.append(" WOW Enabled for Repair Schedule: ").append(channel.getWOWForRepair());
        }
        channelinfo.append( "] " );

        return channelinfo.toString();
    }
}
