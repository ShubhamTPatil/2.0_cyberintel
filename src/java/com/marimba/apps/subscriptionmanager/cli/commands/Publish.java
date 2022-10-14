// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.cli.commands;

import java.util.Hashtable;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscriptionmanager.PluginPropsProcessor;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLICommand;
import com.marimba.apps.subscriptionmanager.users.CLIUser;
import com.marimba.tools.config.ConfigProps;
import com.marimba.tools.gui.StringResources;
import com.marimba.webapps.intf.SystemException;

/**
 *  Command for publishing the plugin
 *
 * @author      Kumaravel Ayyakkannu
 * @version 	$Revision$, $Date$
 */

public class Publish extends SubscriptionCLICommand implements ISubscriptionConstants  {
    public Publish() {

    }
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
	String pubUrl = (String)args.get("publishUrl");
	String username = (String)args.get("username");
	String password = (String)args.get("password");
	isFailed = publish(pubUrl, username, password);
	return isFailed;
    }
    /**
     * Publishes the Subscription Plugin configurator segment to Transmitter
     *
     * @param pubUrl URL of the Subscription Service channel used for publish
     * @param username username used to connect to the transmitter for publish
     * @param password password for the user . Password is not encoded.
     *
     * @return true if command failed to execute succesfully false if command executes succesfully
     *
     * @throws SystemException REMIND
     */
    private boolean publish(String pubUrl,
                            String username,
                            String password)
        throws SystemException {

    	checkPrimaryAdminRole();
        // publish the subscription to the
        // specified configurator segment
        String pstr = resources.getString("cmdline.publishurl") + pubUrl;

        if (!pstr.endsWith("SubscriptionService")) {
            pstr += "SubscriptionService";
        }
        printMessage(pstr);

        ConfigProps config = subsMain.getConfig();
        config.setProperty("subscriptionmanager.publishurl", pubUrl);
        config.setProperty("subscriptionmanager.lastpublishedtime", Long.toString(System.currentTimeMillis()));
        config.setProperty("subscriptionmanager.pallowprov", "false");
        config.setProperty("subscriptionmanager.pluginStatus", "enable");

        try {
            subsMain.publishChannel(username, password, config);
            persistProps();
            printMessage(resources.getString("cmdline.publishsucceeded"));
            logMsg(LOG_AUDIT_PUBLISH_CONFIG_SUCCESS, LOG_AUDIT, null, CMD_PLUGIN_PUBLISH);
        } catch (SystemException se) {
            printMessage(resources.getString("cmdline.publishfailed"));
            logMsg(LOG_AUDIT_PUBLISH_CONFIG_FAILED, LOG_AUDIT, se.toString(), CMD_PLUGIN_PUBLISH);
            throw se;
        }

        return false;
    }
    private void persistProps() {
        // Cache CMS config to Subscription properties.txt. This is used to
        // determine whether CMS LDAP properties were modified.
        // Along with CMS props, ** published plugin props would also be persisted **
        PluginPropsProcessor pluginPropsProc = new PluginPropsProcessor(subsMain.getLDAPConfig(),
                                                                        subsMain.getConfig());
        pluginPropsProc.setServerConfig(subsMain.getServerConfig());
        pluginPropsProc.copyCMSProps();
        pluginPropsProc.copyCMSLDAPProps();
        pluginPropsProc.persistSubsProps();
    }
}
