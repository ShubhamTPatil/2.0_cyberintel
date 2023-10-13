// Copyright 2020-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.util;

import java.io.*;
import java.net.*;
import java.util.*;

import com.marimba.rpc.*;

import com.marimba.intf.util.*;
import com.marimba.intf.tuner.*;
import com.marimba.intf.target.*;
import com.marimba.intf.logs.ILog;

import com.marimba.intf.remoteadmin.*;
import com.marimba.tools.remoteadmin.*;
import com.marimba.tools.remoteadmin.rpc.*;
import com.marimba.tools.remoteadmin.RemoteAdminMgr;
import com.marimba.tools.remoteadmin.rpc.RPCTunerSession;
import com.marimba.intf.castanet.IChannel;

import com.marimba.tools.net.*;
import com.marimba.tools.util.*;
import com.marimba.tools.logs.*;
import com.marimba.tools.config.*;
import com.marimba.tools.target.*;
import com.marimba.tools.gui.StringResources;

import com.marimba.tools.remoteadmin.rpc.*;
import com.marimba.tools.remoteadmin.test.*;
import com.marimba.tools.remoteadmin.util.*;
import com.sun.javafx.collections.MappingChange;

/**
 * RemoteAdmin Util w.r.t kick off security service scan in RunScan feature
 *
 * @author Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 */

public class RemoteAdminHandler  {
    RPC rpc;
    ILog log;
    ConfigUtil config;
    IConfig tunerConfig;
    Hashtable tuners;
    Hashtable sessions;
    Hashtable deployments;
    URL[] knownTuners;
    IServiceLocator locator;
    StringResources logRes;
    Credentials credentials;
    RemoteAdminMgr mgr;
    TunerSession session;
    String status;
    Hashtable <String, String> channelStatus = new Hashtable<String, String>();
    
    public void init(IConfig config, IConfig tunerConfig) {
        try {
            rpc = new RPC();
            config = new ConfigUtil(config);
            credentials = new Credentials();
            mgr = new RemoteAdminMgr(rpc, config, tunerConfig, null, null, null);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void triggerRunScan(String hostNameOrIP, String remoteMachineTunerAdminUserName, String  remoteMachineTunerAdminPassword) {
        String status = "Security Service not started";
        try {
            String id = createUUID();
            credentials.setCredentials("tuner", remoteMachineTunerAdminUserName, "plain:"+remoteMachineTunerAdminPassword);
            URL remoteMachineURL = new URL(hostNameOrIP);
            session  = new RPCTunerSession(id, mgr, remoteMachineURL, credentials);
            ChannelMgr cmgr = null;
            ITunerAdmin tunerAdmin = (ITunerAdmin)session.getAdmin("tuner");

            if (tunerAdmin != null) {
                cmgr = (ChannelMgr)tunerAdmin.getChannelMgr();
                ITunerChannel channel[] = cmgr.getAllChannels();
                ITunerChannel tunerChannel = null;
                String channelURL = "";
                for (int i = 0; i < channel.length; i++) {
                    channelURL = channel[i].getURL().toString();
                    if (channelURL.toString().endsWith("vInspector")) {
                        tunerChannel = channel[i];
                        tunerChannel.update();
                        status  = "Security Service Scan Started";
                        break;
                    } else {
                        status  = "Security Service not found";
                    }
                }

                if (channelURL != null && (!"".equals(channelURL.trim()))) {
                    int currentChStatus = tunerChannel.getStatus();
                    channelStatus.put(channelURL, String.valueOf(currentChStatus));
                }
            } else {
                status = "Unable to connect end-point tuner";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            status = ex.toString();

        } finally{
            if (session != null) {
                session.close();
            }
        }

        setStatus(status);
    }

    public String getChannelStatus(String url) {
        return channelStatus.get(url);    
    }

    public String getChannelStatusInfo(String url) {
        int status = Integer.parseInt(getChannelStatus(url));
        String infoMsg = "";
        switch(status) {
            case IChannel.CH_UNSUBSCRIBED:
                infoMsg = "unsubscribed";
                break;
            case IChannel.CH_RUNNING:
                infoMsg = "running";
                break;
            case IChannel.CH_UPDATING:
                infoMsg = "updating";
                break;
            case IChannel.CH_SUBSCRIBED:
                infoMsg = "subscribed";
                break;
            default:
                 if (status == IChannel.CH_SUBSCRIBED) {
                    infoMsg = "stopped";
                 }
        }
        return infoMsg;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus () {
        return status;
    }

    private String createUUID() {
        // Generate unique ID
        com.marimba.tools.net.UUID uuid = new com.marimba.tools.net.UUID();
        return uuid.toStr().toString().substring(9);
    }


}
