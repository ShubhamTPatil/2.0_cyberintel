// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionplugin;

import com.marimba.intf.util.IProperty;
import com.marimba.tools.util.Props;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.webapps.intf.SystemException;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscription.common.LDAPEnv;

/**
 * Abstracts Subscription & Marimba Config props loaded from the LDAP. This depends on
 * PluginLDAPEnvironment for connecting to the directory server.
 */
class SubsMrbaConfigManager implements LogConstants, IErrorConstants, IPluginDebug, LDAPConstants {
    /**
     * This is a local copy of the the subscription configuration stored
     * in ldap.  It contains the settings for making client queries.
     * However, the credentials are stored in ldapCfg in PluginLDAPEnvironment.
     */
    private Props subConfig;

    private Props mrbaConfig;

    private ISubsPluginContext ctx;

    private String subscriptionConfigVersion;

    private boolean usersInLdap;

    private boolean restrictAllAll;

    private boolean resolveUser;
    private boolean resolveMachine;

    private boolean isConfigUsable;

    /**
     * the default poll period to read the LDAP config to determine whether to
     * accept plugin requests
     */
    private static final int DEFAULT_POLL_PERIOD = 15 * 60 * 1000;

    private int pollPeriod = DEFAULT_POLL_PERIOD;

    /**
     * the last time (in ms) that the config was polled
     */
    private long lastPollTime;

    /**
     * indicates whether the plugin is disabled.  this is set from a config entry in Subscription Config
     */
    private boolean pluginDisabled;

    SubsMrbaConfigManager(ISubsPluginContext ctx) throws SystemException {
        // Default value whether to resolve user or machine
        resolveUser = true;
        resolveMachine = true;

        this.ctx = ctx;

        fetchSchemaVersion();
        isConfigUsable = loadSubsMrbaConfig();
        ctx.registerSubsMrbaConfigMgr(this);
    }

    private void fetchSchemaVersion() {
        String version = ctx.getPluginLDAPEnv().getProperty(LDAPConstants.CONFIG_SCHEMA_VERSION);
        subscriptionConfigVersion = (version != null) ? version : LDAPEnv.CURRENT_SUBSCRIPTION_CONFIG_VERSION;
        if(INFO){
            System.out.println("SubsMrbaConfigManager: subscriptionConfigVersion " + subscriptionConfigVersion+
                    " Schema version "+version);
        }
    }

    /**
     * Method used for initializing the the ldap and subscription configuration
     *
     * @throws com.marimba.webapps.intf.SystemException REMIND
     */
    private boolean loadSubsMrbaConfig() throws SystemException {

        // Get the subscription configuration object which is used for the search attribute
        // of the policies and user and machine resolution.
        try {

            subConfig = (Props) ctx.getPluginLDAPEnv().
                                    getLDAPEnv().
                                    getSubscriptionConfig(ctx.getPluginLDAPEnv().getDefaultConnection(),
                                                          true,
                                                          subscriptionConfigVersion);

            mrbaConfig = (Props) ctx.getPluginLDAPEnv().
                                     getLDAPEnv().
                                     getMarimbaConfig(ctx.getPluginLDAPEnv().getDefaultConnection(),
                                                      true,
                                                      subscriptionConfigVersion);

        } catch (SystemException se) {

            // stack trace to find the reason for failure
            se.printStackTrace();

            // just log the error for the first two exceptions
            // since retrying does not make any sense anyway
            if (LDAP_CONNECT_SUBCONFIG_NOTFOUND.equals(se.getKey())) {
                ctx.log(LOG_ERROR_LDAP_SUBCONFIG_NULL, LOG_MAJOR);
            } else if (LDAP_CONNECT_MRBACONFIG_NOTFOUND.equals(se.getKey())) {
                ctx.log(LOG_ERROR_LDAP_MRBACONFIG_NULL, LOG_MAJOR);
            } else if (LDAP_AUTHENTICATION_FAILED.equals(se.getKey())) {
                ctx.log(LOG_ERROR_INVALIDCREDENTIALS, LOG_MAJOR);
            } else {
                ctx.log(LOG_ERROR_LDAP_SUBCONFIG_SE, LOG_MAJOR, se);
                throw new SystemException(PLUGIN_INIT_LDAP_FAILED);
            }

        }

        if(subConfig == null || mrbaConfig == null) {
            return false;
        }

        if (DETAILED_INFO) {
            ctx.logToConsole("-- Plugin Subscription Configuration --");
            LDAPUtils.printLDAPConfig(subConfig);
        }

        //This utility will initialize the subscription configuration with defaults
        // if none were provided in the subscsription config stored in ldap.
        LDAPUtils.initSubsPluginConfig(subConfig, ctx.getPluginLDAPEnv().getProperty(PROP_VENDOR), null, null);
        LDAPUtils.initMrbaPluginConfig(mrbaConfig, ctx.getPluginLDAPEnv().getProperty(PROP_VENDOR), null, null);

        // We still support the ability to source users and user groups from the transmitter
        // for both AD and iPlanet.  That is what this value will be used for
        String value = subConfig.getProperty(LDAPConstants.CONFIG_USETRANSMITTERUSERS);
        usersInLdap = true;

        if ("true".equals(value)) {
            usersInLdap = false;
        } else if ("false".equals(value)) {
            usersInLdap = true;
        }

        // fetch value for subscriptionconfig property marimba.subscriptionplugin.resolvetype
        // valid values - user/machine/both. If the property is not set, the default is 'both'
        readResolveType();

        // read the gcfromsamedomain subsConfig property to decide whether to resolve groups
        // in GC from a different domain
        String gcFromSameDomain = subConfig.getProperty(LDAPConstants.CONFIG_GCFROMSAMEDOMAIN);
        if(INFO) {
            System.out.println(LDAPConstants.CONFIG_GCFROMSAMEDOMAIN + " value:" + gcFromSameDomain);
        }
        if(gcFromSameDomain != null && "true".equals(gcFromSameDomain)) {
            ctx.getPluginLDAPEnv().setGcFromSameDomain(true);
        }

        // set this property to true will result in ALL_ALL policies not
        // being sent down if both the machine and user are not present in
        // the directory server. default to true to retain backward compatibility
        String allall = subConfig.getProperty(LDAPConstants.CONFIG_RESTRICTALLALL);
        restrictAllAll = false;

        if ((allall != null) && ("true".equals(allall))) {
            restrictAllAll = true;
        }

        readConfig();

        ctx.log(LOG_LDAP_CONFIG_READ,
                LOG_AUDIT,
                LDAPUtils.printPluginConfig(subConfig, mrbaConfig) + " " + ctx.getPluginLDAPEnv().
                                                                               getLDAPConfigLogString());
        return true;
    }

    synchronized void readConfig() {

        long currTime = System.currentTimeMillis();

        if (lastPollTime == 0 || (currTime - lastPollTime) > pollPeriod) {

            lastPollTime = currTime;

            LDAPConnection ldapCon = ctx.getPluginLDAPEnv().getDefaultConnection();
            if(ldapCon != null) {
                // Get the subscription configuration object which is used for the search attribute
                // of the policies and user and machine resolution.
                try {

                    subConfig = (Props) ctx.getPluginLDAPEnv().
                                            getLDAPEnv().
                                            getSubscriptionConfig(ldapCon, true, subscriptionConfigVersion);

                    mrbaConfig = (Props) ctx.getPluginLDAPEnv().
                                             getLDAPEnv().
                                             getMarimbaConfig(ldapCon, true, subscriptionConfigVersion);

                } catch (SystemException se) {

                    // stack trace to find the reason for failure
                    se.printStackTrace();

                    if ( LDAP_CONNECT_SUBCONFIG_NOTFOUND.equals(se.getKey()) ) {
                        ctx.log(LOG_ERROR_LDAP_SUBCONFIG_NULL, LOG_MAJOR);
                        return;
                    } else if( LDAP_CONNECT_MRBACONFIG_NOTFOUND.equals(se.getKey()) ) {
                        ctx.log(LOG_ERROR_LDAP_MRBACONFIG_NULL, LOG_MAJOR);
                        return;
                    } else {
                        ctx.log(LOG_ERROR_LDAP_SUBCONFIG_SE, LOG_MAJOR, se);
                    }
                    return;
                }

                if (DETAILED_INFO) {
                    ctx.logToConsole("--subscription configuration--");
                    LDAPUtils.printLDAPConfig(subConfig);
                }

                // read whether the plugin is disabled
                String plugDisabled = subConfig.getProperty(LDAPConstants.CONFIG_DISABLEPLUGIN);
                if ((plugDisabled != null) && ("offline".equals(plugDisabled))) {
                    pluginDisabled = true;
                } else {
                    pluginDisabled = false;
                }
                if (DETAILED_INFO) {
                    ctx.logToConsole("pluginDisabled = " + pluginDisabled);
                }

                // read the poll period
                String plugPollPeriod = subConfig.getProperty(LDAPConstants.CONFIG_PLUGINCONFIGPOLLPERIOD);
                pollPeriod = DEFAULT_POLL_PERIOD;
                if (plugPollPeriod != null) {
                    try {
                        pollPeriod = Integer.valueOf(plugPollPeriod).intValue();
                    } catch (NumberFormatException nfe) {
                    }
                }
                if (DETAILED_INFO) {
                    ctx.logToConsole("pollPeriod = " + pollPeriod);
                }
            }
        }
    }

    private void readResolveType() {
        String resolveType = subConfig.getProperty(LDAPConstants.CONFIG_RESOLVETYPE);
        if("user".equalsIgnoreCase(resolveType)) {
            resolveUser = true;
            resolveMachine = false;
        } else if("machine".equalsIgnoreCase(resolveType)) {
            resolveUser = false;
            resolveMachine = true;
        } else {
            resolveUser = true;
            resolveMachine = true;
        }
    }

    public Props getSubConfig() {
        return subConfig;
    }

    public Props getMrbaConfig() {
        return mrbaConfig;
    }

    public boolean isUsersInLdap() {
        return usersInLdap;
    }

    public boolean isRestrictAllAll() {
        return restrictAllAll;
    }

    public boolean isResolveUser() {
        return resolveUser;
    }

    public boolean isResolveMachine() {
        return resolveMachine;
    }

    public boolean isConfigUsable() {
        return isConfigUsable;
    }

    public boolean isPluginDisabled() {
        return pluginDisabled;
    }
}
