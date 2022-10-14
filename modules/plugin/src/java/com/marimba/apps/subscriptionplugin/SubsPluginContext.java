// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionplugin;

import com.marimba.intf.plugin.IPluginContext;
import com.marimba.intf.logs.ILogConstants;

import java.util.Date;
import java.net.URL;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Context information of the Subscription plugin.
 */
public class SubsPluginContext implements ISubsPluginContext, IPluginDebug {

    private IPluginContext pluginContext;
    private PluginLDAPEnvironment pluginLDAPEnv;
    private SubsMrbaConfigManager subsMrbaConfigMgr;
    private PublishedProperties pubPluginProps;
    private PluginLDAPUtils pluginLDAPUtils;
    private String hostName;

    public void logToConsole(String message) {
        StringBuffer messageBuf = new StringBuffer();
        messageBuf.append("Security Plugin => ");
        messageBuf.append(new Date());
        messageBuf.append(" - ");
        messageBuf.append(message);
        System.out.println(messageBuf);
    }

    public void logToConsole(String message, Throwable cause) {
        logToConsole(message);
        if(cause != null) {
            cause.printStackTrace();
        }
    }

    public void log(int msgID, int severity) {
        log(msgID, severity, null, null);
    }

    public void log(int msgID, int severity, Object arg) {
        log(msgID, severity, null, null, arg);
    }

    public void log(int msgID, int severity, String src, String user) {
        log(msgID, severity, src, user, null);
    }

    public void log(int msgID, int severity, String src, String user, Object arg) {
        // substitute transmitter host name if the source is null
        if (src == null) {
            src = hostName;
        }

        try {
            pluginContext.log(msgID, -1, severity, src, user, arg);
        } catch (NoSuchMethodError ne) {
            if(ERROR) {
                logToConsole("Unable to write to the plugin log", ne);
                logToConsole("Log Message:ID" + msgID + ",Severity" + severity + ",Src:" + src + ",User:" + user);
            }
        }
    }

    public void registerPluginContext(IPluginContext pluginContext) {
        this.pluginContext = pluginContext;
        registerTxHostName();
    }

    public IPluginContext getPluginContext() {
        return pluginContext;
    }

    public void registerPluginLDAPEnv(PluginLDAPEnvironment pluginLDAPEnv) {
        this.pluginLDAPEnv = pluginLDAPEnv;
    }

    public PluginLDAPEnvironment getPluginLDAPEnv() {
        return pluginLDAPEnv;
    }

    public void registerSubsMrbaConfigMgr(SubsMrbaConfigManager subsMrbaConfigMgr) {
        this.subsMrbaConfigMgr = subsMrbaConfigMgr;
    }

    public SubsMrbaConfigManager getSubsMrbaConfigMgr() {
        return subsMrbaConfigMgr;
    }

    public void registerPublishedPluginProps(PublishedProperties pubPluginProps) {
        this.pubPluginProps = pubPluginProps;
    }

    public PublishedProperties getPublishedPluginProps() {
        return pubPluginProps;
    }

    public void registerPluginLDAPUtils(PluginLDAPUtils pluginLDAPUtils) {
        this.pluginLDAPUtils = pluginLDAPUtils;
    }

    public PluginLDAPUtils getPluginLDAPUtils() {
        return pluginLDAPUtils;
    }

    private void registerTxHostName() {
        String host = "";
        URL masterURL = pluginContext.getMasterURL();

        if(masterURL != null) {
            host = masterURL.getHost();
        } else {
            /* Fall back to InetAddress if masterURL returned as null
             * This will happen when the host= "localhost" Ref: Segment.java */
            try {
                host = InetAddress.getLocalHost().getHostName();
                int i = host.indexOf('.');
                host = (i == -1) ? host : host.substring(0, i);
            } catch (UnknownHostException exp) {
                host = "unknown_host";
                // On error situation let the hostName = "";
            }
        }
        this.hostName = host;
    }
}
