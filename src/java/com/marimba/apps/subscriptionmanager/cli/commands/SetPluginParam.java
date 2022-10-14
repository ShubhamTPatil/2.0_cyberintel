// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.cli.commands;

import java.util.*;

import com.marimba.apps.subscriptionmanager.PluginPropsProcessor;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLICommand;
import com.marimba.apps.subscriptionmanager.users.CLIUser;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.tools.config.ConfigProps;
import com.marimba.tools.ldap.LDAPConnUtils;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.tools.util.Password;
import com.marimba.webapps.intf.SystemException;

/**
 * Set the plugin parameters for publishing the plugin.
 *
 * @author	Narayanan A R
 * @version 	$Revision$, $Date$
 */
public class SetPluginParam extends SubscriptionCLICommand implements ISubscriptionConstants {

    //These are the properties used for saving to the ldapconfig
    private static final String configprops[] = {
                                                 "ldaphost", "basedn", "binddn",
                                                 "bindpasswd", "poolsize", "usessl", "authmethod",
                                                 "lastpublishedtime", "publishcounter",
                                                 "vendor", "lastgoodhostexptime", "pallowprov"
                                                 };

    private static final Map validArgs = new HashMap(7);
    static {
        validArgs.put("pbinddn", "binddn");
        validArgs.put("bindpasswd", "bindpasswd");
        validArgs.put("pbasedn", "basedn");
        validArgs.put("poolsize", "poolsize");
        validArgs.put("usessl", "usessl");
        validArgs.put("authmethod", "authmethod");
        validArgs.put("phost", "ldaphost");
        validArgs.put("expirytime", "lastgoodhostexptime");
    }

    private static final Map validArgsRevMap = new HashMap(7);

    static {
        validArgsRevMap.put("binddn", "pbinddn");
        validArgsRevMap.put("bindpasswd", "bindpasswd");
        validArgsRevMap.put("basedn", "pbasedn");
        validArgsRevMap.put("poolsize", "poolsize");
        validArgsRevMap.put("usessl", "usessl");
        validArgsRevMap.put("authmethod", "authmethod");
        validArgsRevMap.put("ldaphost", "phost");
        validArgsRevMap.put("lastgoodhostexptime", "expirytime");
    }

    private ConfigProps config;
    
    private Map publishProps;

    public SetPluginParam() {
        publishProps = new HashMap();
    }

    public void setSubscriptionMain(SubscriptionMain subsMain) {
 	   super.setSubscriptionMain(subsMain);
     }

    public void setCLIUser(CLIUser cliUser) {
     	super.setCLIUser(cliUser);
    }

    public boolean run(Hashtable args) throws SystemException {
        boolean isFailed = false;

        checkPrimaryAdminRole(cliUser);

        config = subsMain.getConfig();
        publishProps.clear();

        if(isPluginPublished()) {
            initWithSavedConfig();
        } else {
            initWithCMSConfig();
        }

        isFailed = overrideProps(args);
        if(!isFailed) {
            copyToConfig();
            logMsg(LOG_AUDIT_PLUGIN_CONFIG_CHANGED, LOG_AUDIT, null, CMD_SET_PLUGIN_PARAMS);
        }

        return isFailed;
    }

    private void copyToConfig() {
        Iterator publishPropsItr = publishProps.keySet().iterator();
        String key;
        while(publishPropsItr.hasNext()) {
            key = (String)publishPropsItr.next();
            config.setProperty(CONFIG_PREFIX_SUBSCRPTMGR + "." + key, (String)publishProps.get(key));
        }
    }

    private boolean overrideProps(Hashtable args) {
        boolean isFailed = false;

        // Command line usage:
        // -setpluginparam
        //   -binddn <bind_dn> - Optional
        //   -password <password> - Optional, if given encode and store
        //   -basedn <base_dn>  - Optional. If AD and Auto, should not be given.
        //   -poolsize <pool_size> - Optional and integer if given
        //   -usessl true/false  - Optional  and true/false if given
        //   -authmethod simple or kerberos(GSSAPI)- Optional.This argument is applicable only for Active Directory
        //   -host {<host>:<port>} - Optional.  If AD and Auto, should not be given.
        //   -expirytime <time>  - Optional and integer if given
        Map setParamArgs = readArgs(args);
        if(setParamArgs.size() > 0) {
            if(validateArgs(setParamArgs)) {

                // Encode password. May need to refactor this part out, if more such operations are required.
                Object passwordObj = setParamArgs.get("bindpasswd");
                if(passwordObj != null) {
                    setParamArgs.put("bindpasswd", Password.encode((String)passwordObj));
                }

                publishProps.putAll(setParamArgs);
            } else {
                isFailed = true;
            }
        }
        return isFailed;
    }

    private boolean validateArgs(Map setParamArgs) {
        boolean isValid = true;

        isValid = validateKeys(setParamArgs);

        if(isValid) {
            isValid = validateValues(setParamArgs);
        }

        if(isValid) {
            isValid = validateADAutoParams(setParamArgs);
        }

        if(isValid) {
            isValid = validatePoolSize(setParamArgs);
        }

        if(isValid) {
            isValid = validateExpiryTime(setParamArgs);
        }

        if(isValid) {
            isValid = validateUseSSL(setParamArgs);
        }

        if(isValid) {
            isValid = validateAuthMethod(setParamArgs);
        }

        if(isValid) {
            isValid = validateHosts(setParamArgs);
        }

        return isValid;
    }

    private boolean validateValues(Map setParamArgs) {
        // Validate argument value. If a param is specified, the value cannot be empty
        Iterator keyItr = setParamArgs.keySet().iterator();
        String key;
        while(keyItr.hasNext()) {
            key = (String)keyItr.next();
            if("".equals(setParamArgs.get(key))) {
                printMessage(resources.getString("cmdline.plugin.emptyvalue") + validArgsRevMap.get(key));
                return false;
            }
        }
        return true;
    }

    private boolean validateHosts(Map setParamArgs) {
        // Validate host and port, if not AD with auto discovery.
        Object hostlist = setParamArgs.get("ldaphost");
        if(hostlist != null &&
           !LDAPConnUtils.getInstance(tenant.getName()).isADWithAutoDiscovery(subsMain.getLDAPConfig())) {
            StringTokenizer st = new StringTokenizer((String)hostlist, ",");

            while (st.hasMoreTokens()) {
                String hostport = st.nextToken();

                if (hostport != null) {
                    int ind = hostport.lastIndexOf(":");

                    if (ind == -1) {
                        printMessage(resources.getString("cmdline.plugin.invhostport") + hostport);
                        return false;
                    }

                    String hoststr = hostport.substring(0, ind);

                    if ((hoststr == null) || (hoststr.length() == 0)) {
                        printMessage(resources.getString("cmdline.plugin.invhostport") + hostport);
                        return false;
                    }

                    String portstr = hostport.substring(ind + 1);

                    try {
                        Integer.parseInt(portstr);
                    } catch (NumberFormatException e) {
                        printMessage(resources.getString("cmdline.plugin.invhostport") + hostport);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean validateUseSSL(Map setParamArgs) {
        // Validate usessl
        Object usessl = setParamArgs.get("usessl");
        if(usessl != null) {
            if(!("true".equals(usessl) || "false".equals(usessl))) {
                printMessage(resources.getString("cmdline.plugin.invalidsslflag") + usessl);
                return false;
            }
        }
        return true;
    }
    
    private boolean validateAuthMethod(Map setParamArgs) {
        // Validate Authentication Type
        Object authmethod = setParamArgs.get("authmethod");
        if(authmethod != null) {
            if(!("simple".equals(authmethod) || "kerberos".equals(authmethod))) {
                printMessage(resources.getString("cmdline.plugin.invalidauthmethod") + authmethod);
                return false;
            }
        }
        return true;
    }


    private boolean validateExpiryTime(Map setParamArgs) {
        // Validate expirytime
        Object expirytimeObj = setParamArgs.get("lastgoodhostexptime");
        if(expirytimeObj != null) {
            try {
                Integer.parseInt((String)expirytimeObj);
            } catch (NumberFormatException ex) {
                printMessage(resources.getString("cmdline.plugin.lastgoodhostexptimemins") + expirytimeObj);
                return false;
            }
        }
        return true;
    }

    private boolean validatePoolSize(Map setParamArgs) {
        // Validate poolsize
        Object poolsizeObj = setParamArgs.get("poolsize");
        if(poolsizeObj != null) {
            try {
                int poolSize = Integer.parseInt((String)poolsizeObj);
                if(!(poolSize > 0 && poolSize <=100)) {
                    printMessage(resources.getString("cmdline.plugin.invpoolsizerange") + poolsizeObj);
                    return false;
                }
            } catch (NumberFormatException ex) {
                printMessage(resources.getString("cmdline.plugin.invpoolsize") + poolsizeObj);
                return false;
            }
        }
        return true;
    }

    private boolean validateADAutoParams(Map setParamArgs) {
        // If AD and Auto Discovery is used -basedn and -host should not be specified
        if(LDAPConnUtils.getInstance(tenant.getName()).isADWithAutoDiscovery(subsMain.getLDAPConfig())) {
            if(setParamArgs.containsKey("basedn")) {
                printMessage(resources.getString("cmdline.plugin.cannotsetbasedn"));
                return false;
            }

            if(setParamArgs.containsKey("ldaphost")) {
                printMessage(resources.getString("cmdline.plugin.cannotsethost"));
                return false;
            }
        }
        return true;
    }

    private boolean validateKeys(Map setParamArgs) {
        // Validate argument key
        Iterator keyItr = setParamArgs.keySet().iterator();
        Object arg;
        while(keyItr.hasNext()) {
            if(!validArgs.containsValue((arg=keyItr.next()))) {
                printMessage(resources.getString("cmdline.plugin.invalidarg") + validArgsRevMap.get(arg));
                return false;
            }
        }
        return true;
    }

    private Map readArgs(Hashtable args) {
        Map setParamArgs = new HashMap();
        Enumeration keys = args.keys();
        String propKey;
        String propValue;
        int argIdx;
        while(keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            if(key.startsWith("setpluginparam:args")) {
                propKey = (String)args.get(key);
                if(propKey.startsWith("-")) {
                    argIdx = Integer.parseInt(key.substring("setpluginparam:args".length()).trim());
                    propValue = (String)args.get("setpluginparam:args" + ++argIdx);
                    if(propValue != null) {
                        // If no value is given for a given command, replace with space
                        propValue = (validArgs.containsKey(propValue.substring(1))) ? "" : propValue;
                    } else {
                        propValue = "";
                    }
                    // Skip the first character "-"
                    setParamArgs.put(validArgs.get(propKey.substring(1)), propValue);
                }
            }
        }
        return setParamArgs;
    }

    private void initWithCMSConfig() {

        if (DEBUG) {
            System.out.println("SetPluginParam: initializing with CMS values");
        }

        publishProps.put(PROP_VENDOR, subsMain.getLDAPProperty(LDAPConstants.PROP_VENDOR));

        if (LDAPConnUtils.getInstance(tenant.getName()).isADWithAutoDiscovery(subsMain.getLDAPConfig())) {
            publishProps.put("ldaphost", subsMain.getLDAPProperty("domainpath"));
            publishProps.put("basedn", subsMain.getLDAPProperty("domainAsDN"));
            publishProps.put("noautodiscover", "false");
        } else {
            publishProps.put("ldaphost", subsMain.getLDAPProperty(LDAPConstants.PROP_HOST));
            publishProps.put("basedn", subsMain.getLDAPProperty(LDAPConstants.PROP_BASEDN));
            if(isAD()) {
                publishProps.put("noautodiscover", "true");
            } else {
                publishProps.put("noautodiscover", null);
            }
        }

        publishProps.put("binddn", subsMain.getLDAPProperty(LDAPConstants.PROP_USERRDN));
        publishProps.put("usessl", subsMain.getLDAPProperty(LDAPConstants.PROP_USESSL));
        publishProps.put("authmethod", subsMain.getLDAPProperty(LDAPConstants.PROP_AUTHMETHOD));
        publishProps.put("bindpasswd", subsMain.getLDAPProperty(LDAPConstants.PROP_PASSWORD));

        if (DEBUG) {
            System.out.println("SetPluginParam: initWithCMSConfig - binddn = " + publishProps.get("binddn"));
            System.out.println("SetPluginParam: initWithCMSConfig - usessl = " + publishProps.get("usessl"));
            System.out.println("SetPluginParam: initWithCMSConfig - authmethod = " + publishProps.get("authmethod"));
        }

        publishProps.put("poolsize", subsMain.getLDAPProperty(LDAPConstants.PROP_POOLSIZE));
        publishProps.put("lastgoodhostexptime", subsMain.getLDAPProperty(LDAPConstants.PROP_LASTGOODHOST_EXPTIME));
    }

    private void initWithSavedConfig() {

        if (DEBUG) {
            System.out.println("SetPluginParam: initializing with saved values");
        }

        loadPublishedProps();

        PluginPropsProcessor pluginPropsProc = new PluginPropsProcessor(subsMain.getLDAPConfig(), subsMain.getConfig());
        boolean isLDAPConfigModified = pluginPropsProc.isLDAPConfigModified();

		if (DEBUG) {
		    System.out.println("Is LDAP config modified in CMS: " + isLDAPConfigModified);
		}

	// This occurs when CMS vendor is changed, so we reload the CMS properties
        if (isLDAPConfigModified) {
        	initWithCMSConfig();
        }
    }

    public void loadPublishedProps() {
        // This is used to temporarily store the property values returned from the config.
        String propval = null;

        for (int i = 0; i <configprops.length; i++) {
            propval = config.getProperty(CONFIG_PREFIX_SUBSCRPTMGR + "." + configprops[i]);

            if (DEBUG) {
            	if(configprops[i].indexOf("pass") == -1) {
            		System.out.println("SetPluginParam.loadPublishedProps: prop = " + configprops [i] + ", propval = " + propval);
            	}
            }

            publishProps.put(configprops[i], propval);
        }
    }

    private boolean isAD() {
        return VENDOR_AD.equals(subsMain.getLDAPProperty(PROP_VENDOR));
    }

    private boolean isPluginPublished() {
        //Check to see if the plugin has ever been published before.
        String pluginurl = config.getProperty("subscriptionmanager.publishurl");

        if (DEBUG) {
            System.out.println("SetPluginParam: plugin url = " + pluginurl);
        }

        if((pluginurl == null) || "".equals(pluginurl)) {
            return false;
        } else {
            return true;
        }
    }
}
