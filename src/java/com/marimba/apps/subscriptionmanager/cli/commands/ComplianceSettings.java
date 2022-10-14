// Copyright 1997-2015, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.cli.commands;
import java.io.*;
import java.util.*;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.intf.msf.acl.IAclMgr;
import com.marimba.intf.msf.*;
import com.marimba.tools.config.*;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLICommand;
import com.marimba.apps.subscriptionmanager.compliance.core.ComplianceMain;
import com.marimba.apps.subscriptionmanager.users.CLIUser;
import com.marimba.tools.gui.StringResources;
import com.marimba.webapps.intf.SystemException;
/**
 *  Command for Compliance option
 *
 * @author      Selvaraj Jegatheesan
 * @version 	$Revision$, $Date$
 */
public class ComplianceSettings extends SubscriptionCLICommand implements ISubscriptionConstants {
	// Cache information for list results (in seconds)
    public final static String CFG_CACHE_LIST_MAX = "cache.list.max";
    public final static String CFG_CACHE_LIST_EXT = "cache.list.ext";

    // Cache information for other results (in seconds)
    public final static String CFG_CACHE_OBJ_MAX = "cache.obj.max";
    public final static String CFG_CACHE_OBJ_EXT = "cache.obj.ext";

    // Maximum number of items to fetch (use RC when there is more)
    public final static String CFG_LIST_MAX = "list.max";

    // Default wait time for queries
    public final static String CFG_QUERY_WAIT = "query.wait";

    // Last check in limit (in hours)
    public final static String CFG_CHECKIN_LIMIT = "checkin.limit";

    // Enable/Disable the collection of compliance data
    public final static String COLLECT_INVENTORY_DATA = ISubscriptionConstants.COLLECT_INVENTORY_DATA;
	public void setSubscriptionMain(SubscriptionMain subsMain) {
		this.subsMain = subsMain;
	}

	public void setCLIUser(CLIUser cliUser) {
		this.cliUser = cliUser;
	}

	public void setResources(StringResources resources) {
		this.resources = resources;
	}

	public boolean run(Hashtable complianceProps) throws SystemException {
		boolean isFailed = false;
		checkPrimaryAdminRole(cliUser);
		isFailed = apply(complianceProps);
		return isFailed;
	}
	private boolean apply(Hashtable<String, String> complianceProps) throws SystemException {
		boolean isFailed = false;
		try {
			for(String hashKey : complianceProps.keySet()) {
				System.out.println("key :" + hashKey);
				System.out.println("Value : " + complianceProps.get(hashKey));
			}
			String complianceEnabled = complianceProps.get("complianceenabled:args0");
			if(null != complianceEnabled && ("true".equalsIgnoreCase(complianceEnabled) || "false".equalsIgnoreCase(complianceEnabled))) {
				complianceEnabled = complianceEnabled.toLowerCase();
				ConfigProps config = null;
				File rootDir = subsMain.getDataDirectory();
				System.out.println("Root dir :" + rootDir.getAbsolutePath());
			    if (rootDir != null && rootDir.isDirectory()) {
			        File configFile = new File(rootDir, "comp_config.txt");
				        if (!configFile.exists()) {
				        	System.out.println("File doesn't exists");
				            config = new ConfigProps(configFile);
		                    config.setProperty(CFG_LIST_MAX, (new Integer(500)).toString());
				            config.setProperty(CFG_CACHE_LIST_MAX, (new Integer(10 * 60)).toString());
		                    config.setProperty(CFG_CACHE_LIST_EXT, (new Integer(60)).toString());
				            config.setProperty(CFG_CACHE_OBJ_MAX, (new Integer(30 * 60)).toString());
				            config.setProperty(CFG_CACHE_OBJ_EXT, (new Integer(10 * 60)).toString());
				            config.setProperty(CFG_QUERY_WAIT, (new Integer(60 * 60)).toString());
				            config.setProperty(CFG_CHECKIN_LIMIT, (new Integer(-48)).toString());
		                    config.setProperty(COLLECT_INVENTORY_DATA, "disable");
				        } else {
				        	System.out.println("File exists");
				            config = new ConfigProps(configFile);
				        }
				        String access = "disable";
				        if("true".equalsIgnoreCase(complianceEnabled)) {
				        	access = "enable";
				        }
				        config.setProperty(COLLECT_INVENTORY_DATA, access);
				        if (!config.save()) {
				            throw new Exception("Failed to save compliance settings");
			            }
				        boolean policyStatus = savePolicy(complianceEnabled);
				        if(policyStatus) {
				        	isFailed = false;
				        	printMessage(resources.getString("cmdline.compliancesettingssuccess"));
				        } else {
				        	isFailed = true;
				        	printMessage(resources.getString("cmdline.savecompliancesettingsfailed"));
				        }
			    } else {
					isFailed = true;
					printMessage(resources.getString("cmdline.compliancesettingsfailed"));
			    }
				
			} else {
				isFailed = true;
				printMessage(resources.getString("cmdline.complainceinputinvalid"));
			}
		} catch(Exception ec) {
			if(DEBUG5) {
				ec.printStackTrace();
			}
			isFailed = true;
			printMessage(resources.getString("cmdline.compliancesettingsfailed"));
		}
		return isFailed;
	}
	private boolean savePolicy(String complianceEnabled) {
		boolean status = false;
		try {
			IAclMgr iAclMgr = subsMain.getTenant().getAclMgr();
	        ISubscription sub = ObjectManager.openSubForWrite( iAclMgr.getAllAllDn(), ISubscriptionConstants.TYPE_ALL, cliUser );
	        sub.setProperty( ISubscriptionConstants.PROP_TUNER_KEYWORD, COLLECT_INVENTORY_DATA, complianceEnabled );
	        sub.save();
	        status = true;
		} catch(Exception ec) {
			ec.printStackTrace();
		}
		return status;
	}
}
