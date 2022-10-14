// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.cli.commands;

import java.util.*;

import javax.naming.NamingException;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.LDAPEnv;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLICommand;
import com.marimba.apps.subscriptionmanager.users.CLIUser;
import com.marimba.tools.config.ConfigDefaults;
import com.marimba.tools.config.ConfigProps;
import com.marimba.tools.gui.StringResources;
import com.marimba.tools.ldap.LDAPConnUtils;
import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.tools.ldap.LDAPLocalException;
import com.marimba.tools.util.Props;
import com.marimba.webapps.intf.SystemException;
import com.marimba.intf.util.IProperty;
import com.marimba.intf.util.IConfig;

public class ConfigSet extends SubscriptionCLICommand implements ISubscriptionConstants  {


    public void setSubscriptionMain(SubscriptionMain subsMain) {
        this.subsMain = subsMain;
    }
    public void setCLIUser(CLIUser cliUser) {
     	this.cliUser = cliUser;
    }
    public void setResources(StringResources  resources) {
     	this.resources = resources;
    }	
    public boolean run(Hashtable args) throws SystemException {
        boolean isFailed = false;
        checkPrimaryAdminRole(cliUser);
        isFailed = configSet(args);
        return isFailed;
    }
    private boolean configSet(Hashtable cmds) throws SystemException {
    	
    	//checkPrimaryAdminRole();
        try {
            IProperty subConfig = subsMain.getSubscriptionConfig();
            IProperty mrbaConfig = subsMain.getMarimbaConfig();
            String    key = (String) cmds.get("configSet:args0");
            String    val = (String) cmds.get("configSet:args1");
            boolean   preview = "-preview".equals(cmds.get("configSet:args2"));

            // REMIND t3 we should change this implementation so users do not
            // need to restart
            key = key.toLowerCase();

            if (preview) {
                printMessage(resources.getString("cmdline.config.preview") + key);
                if(subConfig.getProperty(key) != null) {
                    printMessage(resources.getString("cmdline.config.old") + subConfig.getProperty(key));
                } else {
                    printMessage(resources.getString("cmdline.config.old") + mrbaConfig.getProperty(key));
                }
                printMessage(resources.getString("cmdline.config.new") + val);
                printMessage("\n");
            } else {
                LDAPEnv        ldapenv = subsMain.getLDAPEnv();
                String         subConfigDN = ldapenv.getSubConfigDN();
                String         mrbaConfigDN = ldapenv.getMarimbaCurConfigDN();
                LDAPConnection defConn = cliUser.getConnectionCache()
                                                .getDefaultConnection();

                String         domain = LDAPConnUtils.getDomainFromDN(defConn, LDAPConnUtils.getDomainDNFromDN(defConn, subConfigDN));
                LDAPConnection dConn = cliUser.getConnectionCache()
                                              .getConnection(LDAPConstants.TYPE_DC, domain);
                IConfig        newConfig = null;
                //Adding the marimbaconfig properties in the set.
                Set mrbaConfigSet = new HashSet();
                mrbaConfigSet.add("marimba.ldap.browse.collectionbase");
                mrbaConfigSet.add("marimba.ldap.browse.collectionmachinebase");
                mrbaConfigSet.add("marimba.ldap.browse.collectionmode");
                mrbaConfigSet.add("marimba.ldap.browse.machineclass");
                mrbaConfigSet.add("marimba.ldap.browse.hideentries");
                mrbaConfigSet.add("marimba.schemaversion");
                mrbaConfigSet.add("marimba.schemapatchversion");
                mrbaConfigSet.add("marimba.previousschemaversion");
                mrbaConfigSet.add("marimba.aclbase");

                if(mrbaConfigSet.contains((String)key)){
                    newConfig = new ConfigDefaults(mrbaConfig, new ConfigProps(new Props(), null));
                    newConfig.setProperty(key, val);
                    ldapenv.saveMarimbaConfig(newConfig, mrbaConfigDN, dConn);
                    logMsg(LOG_AUDIT_SUB_CONFIG_CHANGED, LOG_AUDIT, key +"="+ val, CMD_SET_CONF );
                } else {
                    newConfig = new ConfigDefaults(subConfig, new ConfigProps(new Props(), null));
                    newConfig.setProperty(key, val);
                    ldapenv.saveSubscriptionConfig(newConfig, subConfigDN, dConn, subsMain.getLDAPVarsMap());
                    logMsg(LOG_AUDIT_SUB_CONFIG_CHANGED, LOG_AUDIT, key +"="+ val, CMD_SET_CONF);
                }

                if(subConfig.getProperty(key) != null) {
                    printMessage(resources.getString("cmdline.config.old") + subConfig.getProperty(key));
                } else {
                    printMessage(resources.getString("cmdline.config.old") + mrbaConfig.getProperty(key));
                }
                printMessage(resources.getString("cmdline.config.new") + val);
                printMessage("\n");
                printMessage(resources.getString("cmdline.config.success") + key + "=" + newConfig.getProperty(key));
            }
        } catch (NamingException ne) {
            LDAPUtils.classifyLDAPException(ne);
        } catch (LDAPLocalException le) {
            throw new SystemException(le, ACL_MGRERROR);
        }

        return false;
    }
}
