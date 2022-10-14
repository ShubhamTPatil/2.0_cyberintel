// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.cli.commands;

import java.util.*;

import javax.naming.NamingException;

import com.marimba.apps.subscriptionmanager.users.CLIUser;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.SubKnownException;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscriptionmanager.MergeAllSub;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLIBase;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLICommand;
import com.marimba.apps.subscriptionmanager.cli.commands.intf.ISubscribe;
import com.marimba.castanet.schedule.Schedule;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.tools.ldap.LDAPException;
import com.marimba.tools.ldap.LDAPSearchFilter;
import com.marimba.webapps.intf.SystemException;
import com.marimba.intf.util.IProperty;

/**
 * Deletes the policy.
 *
 * @author      Kumaravel Ayyakkannu
 * @version 	$Revision$, $Date$
 */

public class DeletePolicy extends SubscriptionCLICommand implements ISubscribe,ISubscriptionConstants {

    private Hashtable validTypes;
    private SubscriptionCLIBase sb;
	
    public DeletePolicy() {
	sb = new SubscriptionCLIBase();
	validTypes  = new Hashtable();
	validTypes.put("machine", "machine");
	validTypes.put("machinegroup", "machinegroup");
	validTypes.put("user", "user");
	validTypes.put("usergroup", "usergroup");
	validTypes.put("all", "all");
	validTypes.put("container", "container");
	validTypes.put("collection", "collection");
	validTypes.put("ldapqc", "ldapqc");
    }
    public void setSubscriptionMain(SubscriptionMain subsMain) {
	   super.setSubscriptionMain(subsMain);
	   sb.setSubscriptionMain(subsMain);
    }
    public void setCLIUser(CLIUser cliUser) {
    	super.setCLIUser(cliUser);
    	sb.setCLIUser(cliUser);
    }
    public boolean run(Hashtable args) throws SystemException {
	boolean isFailed = false;
	isFailed = delete(args);
	return isFailed;
    }
    /**
     * Delete the listed internal subscription files (or all if '' is specified
     *
     * @param cmds names of Subscription Policies to be deleted or all policies
     *
     * @return REMIND
     *
     * @throws SystemException REMIND
     */
    private boolean delete(Hashtable cmds)
        throws SystemException {
        boolean allflag = "-all".equals(cmds.get("delete:args0"));
        boolean cascadeflag = "-cascade".equals(cmds.get("delete:args0"));
        boolean dnflag = "-dn".equals(cmds.get("delete:args0"));
        Map<String, String> LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(subsMain.getDirType());
        try {
            if (dnflag) {
                String dn = (String) cmds.get("delete:args1");
                String type = (String) cmds.get("delete:args2");

                if ("-type".equals(type)) {
                    type = (String) cmds.get("delete:args3");
                    type = type.toLowerCase();
                } else {
                    type = sb.resolveTargetType(dn);
                }

                deleteSub(dn, type);
            } else if (allflag) {
                // delete all Subscription from current namespace
                String[] sublist = com.marimba.apps.subscriptionmanager.ObjectManager.listSubscriptionByName(subsMain, cliUser);

                if (sublist == null) {
                    return false;
                }

                for (int i = 0; i < sublist.length; i++) {
                    deleteSubByName(sublist [i]);
                }
            } else if (cascadeflag) {
                // delete all subscriptions in all namespaces
                // delete the default subscription location first
            	sb.setNameSpace(null);

                String[] sublist = com.marimba.apps.subscriptionmanager.ObjectManager.listSubscriptionByName(subsMain, cliUser);

                if (sublist != null) {
                    for (int i = 0; i < sublist.length; i++) {
                        deleteSubByName(sublist [i]);
                    }
                }

                // delete all subscriptions in all namespaces
                try {
                    ArrayList nsList = subsMain.listNameSpaces(cliUser.getSubConn());
                    String    namespace = null;

                    for (int i = 0; i < nsList.size(); i++) {
                        namespace = (String) nsList.get(i);
                        sb.setNameSpace(namespace);
                        printMessage(resources.getString("cmdline.delete.namespace") + " " + namespace);
                        sublist = com.marimba.apps.subscriptionmanager.ObjectManager.listSubscriptionByName(subsMain, cliUser);

                        if (sublist != null) {
                            for (int j = 0; j < sublist.length; j++) {
                                deleteSubByName(sublist [j]);
                            }
                        }
                    }

                    sb.setNameSpace(null);
                } catch (NamingException le) {
                    LDAPUtils.classifyLDAPException(le);
                }
            } else {
                // process list
                if (((String) cmds.get("delete:args0")).endsWith(SUBSCRIPTION_EXT)) {
                    // process list, deleting specified .sub entries
                    int    i = 0;
                    String arg1;

                    while ((arg1 = (String) cmds.get("delete:args" + i++)) != null) {
                        deleteSubByName(arg1);
                    }
                } else {
                    // delete subscription objects based on 'targetname' and 'targettype' syntax
                    String fname = (String) cmds.get("delete:args0");
                    String type = (String) cmds.get("delete:args1");

                    type = type.toLowerCase();
		    
                    fname = sb.resolveTargetDN(fname, type);
                    deleteSub(fname, type);
                }
            }
			 MergeAllSub.checkAccessToken( cliUser.getSubConn(), subsMain.getSubBase2(), LDAPVarsMap);
        } catch (SystemException se) {
            printMessage(resources.getString("cmdline.deletefailed"));
            logMsg(LOG_AUDIT_POLICY_DELETE_FAILED, LOG_AUDIT, se.toString(), CMD_POLICY_DEL);
            throw se;
        } finally {
	    if(sb.getOutputStr().length() > 0) {
	    	printMessage(sb.getOutputStr());
	    }
	}
        printMessage(resources.getString("cmdline.delete"));

        return false;
    }
    /**
     * Delete the specified subscription object
     *
     * @param dn Subscription Policy Target name to be deleted
     * @param type REMIND
     *
     * @throws SystemException REMIND
     */
    private void deleteSub(String dn,
                           String type)
        throws SystemException {
        printMessage(resources.getString("cmdline.subscribedelete") + " " + dn);
        com.marimba.apps.subscriptionmanager.ObjectManager.deleteSub(dn, type, cliUser);
        logMsg(LOG_AUDIT_POLICY_DELETED, LOG_AUDIT, " DN="+ dn, CMD_POLICY_DEL);
    }
    private void deleteSubByName(String subname)
                 throws SystemException {
	    if (com.marimba.apps.subscriptionmanager.ObjectManager.deleteSubNoError(subname, cliUser)) {
            logMsg(LOG_AUDIT_POLICY_DELETED, LOG_AUDIT, " Policy Name="+ subname, CMD_POLICY_DEL);
	        printMessage(resources.getString("cmdline.subscribedeletebyname") + " " + subname);
	    } else {
            logMsg(LOG_AUDIT_POLICY_DELETE_SKIPPED, LOG_INFO, " Policy Name="+ subname );
	        printMessage(resources.getString("cmdline.subscribedeletebynameskip") + " " + subname);
	    }
    }
}
