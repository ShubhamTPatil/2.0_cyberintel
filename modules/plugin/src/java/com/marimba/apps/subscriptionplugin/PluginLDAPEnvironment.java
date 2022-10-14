// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionplugin;

import com.marimba.intf.util.IConfig;
import com.marimba.intf.util.IObserver;
import com.marimba.intf.util.IProperty;
import com.marimba.intf.ssl.ISSLProvider;
import com.marimba.tools.ldap.*;
import com.marimba.tools.util.Props;
import com.marimba.tools.config.ConfigProps;
import com.marimba.webapps.intf.SystemException;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscription.common.LDAPEnv;
import com.marimba.apps.subscription.common.LDAPVars;

import javax.naming.NamingException;

/**
 * References of LDAPConnectionCache, LDAPConnUtils, LDAPConnection & the LDAP
 * configuration are managed here.
 */
class PluginLDAPEnvironment
        implements IConfig,
                   LDAPConstants,
                   LogConstants,
                   IErrorConstants,
                   IPluginDebug {

    /**
     * Plugin context.
     */
    private ISubsPluginContext ctx;

    /**
     * Cache of the ldap connections used during the getData process. This contains a cache
     * of connection to th DC and GC of domains that th clients may need.
     */
    private LDAPConnectionCache connCache;

    /**
     * Default connection.
     */
    private LDAPConnection defaultConnection;
    protected LDAPConnUtils ldapConnUtils;

    /**
     * This is the props object used to store the credentlas of the ldap
     * connection configuration.  See com.marimba.tools.ldap.LDAPConstants
     */
    private Props ldapCfg;

    private LDAPEnv ldapEnv;

    /**
     * Initialize the LDAP environment.
     */
    PluginLDAPEnvironment(ISubsPluginContext ctx, Props ldapCfg) throws SystemException {
        this.ctx = ctx;
        this.ldapCfg = ldapCfg;

        // Initialize the socket factor that is used for connections
        LDAPSSLSocketFactory.provider = (ISSLProvider) ctx.getPluginContext().getFeature("ssl");

        //Each time a new connection is needed, it is obtained from this cache so that repeat
        //LDAPConnections are not made to the various domains in the system.
        connCache = new LDAPConnectionCache();

        this.ldapConnUtils = LDAPConnUtils.getInstance(mergeADInfo());

        //We set the ldap configuration for the cache. Only one can exist for the cache
        //since the cache only tracks the various connections to domains for one user
        connCache.setLDAPConfig(ldapCfg);
        init0();
        createDefaultConnection();

        ctx.registerPluginLDAPEnv(this);
    }

    void setGcFromSameDomain(boolean isGcFromSameDomain) {
    	ldapConnUtils.setGcFromSameDomain(isGcFromSameDomain); 
    }

    private void init0() throws SystemException {
        //Try to obtain the forest root and site if the vendor is AD. This is needed to make
        //any connection to the global catalog.  If we can't get the forest root,
        //no connections can be handled.
        if (LDAPConstants.VENDOR_AD.equals(getProperty(PROP_VENDOR))) {
	    initializeForest();
        }

        if (DETAILED_INFO) {
            ctx.logToConsole("-- Plugin LDAP Configuration --");
            LDAPUtils.printLDAPConfig(ldapCfg);
        }

        // Initialize the LDAP env in the manner that Subscription Main does.
        // This initialization requires no ldap connection.
        try {
        	LDAPEnv env = new LDAPEnv();
            ldapEnv = env.getInstance(ldapCfg);
        } catch (SystemException se) {
            ctx.log(LOG_ERROR_LDAP_ENV, LOG_MAJOR, null, null, se);
            throw new SystemException(PLUGIN_INIT_LDAP_FAILED);
        }
    }

    public LDAPEnv getLDAPEnv() {
        return ldapEnv;
    }

    public LDAPConnUtils getLdapConnUtils() {
		return ldapConnUtils;
	}

	/**
     * Combine the marimba.ldap.ad.* properties published from the SPM, and these
     * two properties from the Tuner's prefs.txt
     * <p/>
     * marimba.ldap.admanagementdomain=qa.m6.subs.dev
     * marimba.ldap.srvdnsserver=192.168.15.175
     * marimba.ldap.preferreddcs=inch1mhc.chn.bbca.com,inch2mhc.chn.bbca.com
     * 
     * @return AD properties merged with Plugin properties
     */
    private IConfig mergeADInfo() {
        IConfig tunerCfg = (IConfig) ctx.getPluginContext().getFeature("config");

        IConfig merged = new ConfigProps(new Props(), null);
        merged.setProperty(PROP_AD_DOMAIN, ldapCfg.getProperty(PROP_AD_DOMAIN));
        merged.setProperty(PROP_AD_DNSSERVER, ldapCfg.getProperty(PROP_AD_DNSSERVER));
        merged.setProperty(PROP_CONN_TIMEOUT, ldapCfg.getProperty(PROP_CONN_TIMEOUT));
        merged.setProperty(PROP_QUERY_TIMEOUT, ldapCfg.getProperty(PROP_QUERY_TIMEOUT));
        merged.setProperty(PROP_AD_PREFERREDDCS, ldapCfg.getProperty(PROP_AD_PREFERREDDCS));
        
        boolean noAutoDiscover = "true".equals(ldapCfg.getProperty(LDAPConstants.PROP_NOAUTODISCOVER));

        if (noAutoDiscover) {
            String[] pairs = ldapCfg.getPropertyPairs();
            for (int i = 0; i < pairs.length; i += 2) {
                merged.setProperty(pairs[i], pairs[i + 1]);
            }
        }

        return merged;
    }

    private void createDefaultConnection() throws SystemException {
        // Create a connection to obtain the subscription configuration.  For AD, this
        // results in a connection to the GC.  For iPlanet, this will result in a connection
        // to the first available host.
        try {
            defaultConnection = connCache.getDefaultConnection();
        } catch (NamingException e) {
            ctx.log(LOG_LDAP_CONN_ERROR, LOG_MAJOR, e);
            throw new SystemException(PLUGIN_INIT_LDAP_FAILED);
        }
    }

    LDAPConnection getDefaultConnection() {
        return defaultConnection;
    }

    LDAPConnection getConnection(String serverType, String domain) throws NamingException {
        try {
            return connCache.getConnection(serverType, domain);
        } catch (NamingException e) {
            ctx.log(LOG_LDAP_CONN_ERROR, LOG_MAJOR, e);
            throw e;
        }
    }

    void cleanup() {
        defaultConnection.unbind();
        connCache.cleanup();
    }

    /**
     * REMIND: Narayanan A R
     * This method needs to be removed. LDAPEnv, LDAPConnUtils need to be refactored to directly
     * access the default connection.
     * @return
     */
    LDAPConnectionCache getLDAPConnectionCache() {
        return connCache;
    }

    /**
     * Calling intialize forest will fill the ldapCfg with the current domain, current site, and forest root.
     * <p/>
     * Usages of Current Domain (may be null)
     * - For the plugin, the current domain is needed for establishing the connection for obtaining the
     * site and forestroot.	(which is done from within the LDAPConnUtils.initializeForest method).
     * - The current domain is also used to resolve user DN if user and machine dn cannot be determined.
     * Usage of Site (may be null)
     * - Site information is used to get the closest connection to the GC and the DC so that we do not
     * unnecessarily go to a distant location.  If null, plugin performance may be slowed down
     * Usage of Forest Root (can not be null)
     * - Requests cannot be handled without a connection to the forest root.  This is because
     * it is used for GC connections.
     */
    private void initializeForest() throws SystemException {
        try {
        	ldapConnUtils.initializeForest(new ConfigProps(ldapCfg, null));
        } catch (LDAPLocalException le) {
            int errorCode = le.getErrorCode();
            if (ERROR) {
                ctx.logToConsole("Unable to initialize forest", le);
            }
            if (le.getRootException() instanceof javax.naming.AuthenticationException) {
                //  If the credentials that have been published are incorrect, this
                //  is where the exception would occur when the forest root is first
                //  accessed.  Therefore, we add a more informative error message
                ctx.log(LOG_ERROR_INVALIDCREDENTIALS, LOG_MAJOR);
            } else {
                if (errorCode == LDAPErrConstants.UTILS_AD_CURRENTDOMAIN) {
                    ctx.log(LOG_ERROR_LDAP_DOMAIN_EX, LOG_MAJOR, le.getRootException());
                    // No current domain is only acceptable if the resolution of machine and
                    // users that do not provide a DN is turned off and if unix machines are not to be supported
                    if (!"true".equals(ldapCfg.getProperty(LDAPConstants.CONFIG_USEDNFROMCLIENTONLY))) {
                        ctx.log(LOG_ERROR_CURRENTDOMAIN_NULL, LOG_MAJOR);
                    } else {
                        return;
                    }
                } else if (errorCode == LDAPErrConstants.UTILS_AD_CURRENTSITE) {
                    // It is alright that the site is not discovered, however it will slow down plugin
                    // performance, therefore we log this, instead of throwing an exception
                    ctx.log(LOG_ERROR_GETSITE_FAILED, LOG_MAJOR, le.getRootException());
                    return;
                } else if (errorCode == LDAPErrConstants.UTILS_AD_FORESTROOT) {
                    ctx.log(LOG_ERROR_GETFORESTROOT_FAILED, LOG_MAJOR, le.getRootException());
                }
            }
            throw new SystemException(PLUGIN_INIT_LDAP_FAILED);
        }

    }


    String getLDAPConfigLogString() {
      StringBuffer sb = new StringBuffer();
        sb.append('[');
        sb.append(ldapCfg.getProperty(PROP_VENDOR));
        sb.append("|");
        if (VENDOR_AD.equals(ldapCfg.getProperty(PROP_VENDOR)) && !"true".equals(ldapCfg.getProperty(PROP_NOAUTODISCOVER))) {
                sb.append(ldapCfg.getProperty(PROP_DOMAINPATH));
                sb.append("|");
                sb.append(ldapCfg.getProperty(PROP_SITE));
                sb.append("|");
                sb.append(ldapCfg.getProperty(PROP_FORESTROOT));
        } else {
            sb.append(ldapCfg.getProperty(PROP_HOST));
            sb.append("|");
            sb.append(ldapCfg.getProperty(PROP_BASEDN));
        }
        sb.append("|");
        sb.append(ldapCfg.getProperty(PROP_USERRDN));
        sb.append(']');
        return sb.toString();
    }

    public String getProperty(String key) {
        return ldapCfg.getProperty(key);
    }

    public String[] getPropertyPairs() {
        return ldapCfg.getPropertyPairs();
    }

    public void setProperty(String key, String value) {
        ldapCfg.setProperty(key, value);
    }

    public void addObserver(IObserver object, int min, int max) {
        throw new IllegalStateException("Unsupported operation - addObserver");
    }

    public void removeObserver(IObserver iObserver) {
        throw new IllegalStateException("Unsupported iperation - removeObserver");
    }
}
