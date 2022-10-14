// Copyright 1997-2004, Marimba Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager;

import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.tools.config.ConfigProps;
import com.marimba.intf.util.IProperty;
import com.marimba.intf.util.IConfig;
import java.util.ArrayList;
import java.util.List;

/**
 * Processor class for manipulating the cms and plugin ldap proeprties while
 * publishing the plugin.
 *
 * @author	Narayanan A R
 * @version 	$Revision$, $Date$
 */
public class PluginPropsProcessor {

    private static final String CMS_LDAP_PROPS_PREFIX = "cms.dirs.";
    private static final String cmsPropsToCache[] = {
                                                     "poolname",
                                                     LDAPConstants.PROP_VENDOR,
                                                     LDAPConstants.PROP_NOAUTODISCOVER,
                                                     LDAPConstants.PROP_HOST,
                                                     LDAPConstants.PROP_USERRDN,
                                                     LDAPConstants.PROP_PASSWORD,
                                                     LDAPConstants.PROP_BASEDN,
                                                     LDAPConstants.PROP_DOMAINASDN,
                                                     LDAPConstants.PROP_DOMAINPATH,
                                                     LDAPConstants.PROP_AUTHMETHOD,
                                                     LDAPConstants.PROP_USESSL
                                                    };
    private static final String cmsLDAPPropsToCache[] = {
    												"admanagementdomain",
    												"srvdnsserver",
    												"connectiontimeout",
    												"querytimeout",
    												"preferreddcs",
    												"adsite"
    												};
    private static final List caseSensitiveProps = new ArrayList();
    static {
        caseSensitiveProps.add(cmsPropsToCache[0]); //poolname
    }

    private ConfigProps subsPersistProps;
    private IProperty ldapConfigProps;
    private IConfig serverConfig;

    public PluginPropsProcessor(IProperty ldapConfigProps, ConfigProps subsPersistProps) {
        this.ldapConfigProps = ldapConfigProps;
        this.subsPersistProps = subsPersistProps;
    }

    public IConfig getServerConfig() {
		return serverConfig;
	}

	public void setServerConfig(IConfig serverConfig) {
		this.serverConfig = serverConfig;
	}

	/**
     * We would need to cache the following CMS props to determine whether to preload with CMS props.
     * dir-name, vendor, noautodiscover, host, userrdn, basedn, domainAsDN & domainpath.
     */
    public void copyCMSProps() {
        String value;
        for(int i=0; i<cmsPropsToCache.length; i++) {
            value = ldapConfigProps.getProperty(cmsPropsToCache[i]);
            value = (value == null) ? "" : value;
            subsPersistProps.setProperty(CMS_LDAP_PROPS_PREFIX + cmsPropsToCache[i], value);
        }
    }
    public void copyCMSLDAPProps() {
    	String value;
        for(int i=0; i<cmsLDAPPropsToCache.length; i++) {
            value = serverConfig.getProperty(cmsLDAPPropsToCache[i]);
            value = (value == null) ? "" : value;
            subsPersistProps.setProperty(CMS_LDAP_PROPS_PREFIX + cmsLDAPPropsToCache[i], value);
        }
    }
    public void persistSubsProps() {
        subsPersistProps.save();
    }

    /**
     * This method is used in order to determine whether to read props from CMS or cached value in properties.txt.
     * @return
     */
    public boolean isLDAPConfigModified() {
        boolean isLDAPConfigModified = false;
        String cmsProp;
        String cachedCMSProp;
        for(int i=0; i<cmsPropsToCache.length; i++) {
            cachedCMSProp = subsPersistProps.getProperty(CMS_LDAP_PROPS_PREFIX + cmsPropsToCache[i]);
            cachedCMSProp = (cachedCMSProp == null) ? "" : cachedCMSProp;

            cmsProp = ldapConfigProps.getProperty(cmsPropsToCache[i]);
            cmsProp = (cmsProp == null) ? "" : cmsProp;

            if(caseSensitiveProps.contains(cmsPropsToCache[i])) {
                if(!cachedCMSProp.equals(cmsProp)) {
                    isLDAPConfigModified = true;
                    break;
                }
            } else {
                if(!cachedCMSProp.equalsIgnoreCase(cmsProp)) {
                    isLDAPConfigModified = true;
                    break;
                }
            }
        }
        for(int j=0; j<cmsLDAPPropsToCache.length; j++) {
            cachedCMSProp = subsPersistProps.getProperty(CMS_LDAP_PROPS_PREFIX + cmsLDAPPropsToCache[j]);
            cachedCMSProp = (cachedCMSProp == null) ? "" : cachedCMSProp;

            cmsProp = ldapConfigProps.getProperty(cmsLDAPPropsToCache[j]);
            cmsProp = (cmsProp == null) ? "" : cmsProp;

                if(!cachedCMSProp.equalsIgnoreCase(cmsProp)) {
                    isLDAPConfigModified = true;
                    break;
                }
        }
        return isLDAPConfigModified;
    }
}
