// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionplugin;

import com.marimba.intf.plugin.IPluginContext;

/**
 * Interface for providing access to the plugin context information to rest of the classes.
 */
public interface ISubsPluginContext {

    // Methods to write debug messages to console
    void logToConsole(String message);
    void logToConsole(String message, Throwable cause);

    // Various method signatures for writing the message to plugin audit logs
    void log(int msgID, int severity);
    void log(int msgID, int severity, Object arg);
    void log(int msgID, int severity, String src, String user);
    void log(int msgID, int severity, String src, String user, Object arg);

    void registerPluginContext(IPluginContext pluginContext);
    IPluginContext getPluginContext();

    void registerPluginLDAPEnv(PluginLDAPEnvironment pluginLDAPEnv);
    PluginLDAPEnvironment getPluginLDAPEnv();

    void registerSubsMrbaConfigMgr(SubsMrbaConfigManager subsMrbaConfigMgr);
    SubsMrbaConfigManager getSubsMrbaConfigMgr();

    void registerPublishedPluginProps(PublishedProperties pubPluginprops);
    PublishedProperties getPublishedPluginProps();

    void registerPluginLDAPUtils(PluginLDAPUtils pluginLDAPUtils);
    PluginLDAPUtils getPluginLDAPUtils();
}
