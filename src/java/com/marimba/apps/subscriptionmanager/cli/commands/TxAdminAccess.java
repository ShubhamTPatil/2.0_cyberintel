// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.cli.commands;

import java.util.Hashtable;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLICommand;
import com.marimba.webapps.intf.SystemException;

public class TxAdminAccess extends SubscriptionCLICommand implements ISubscriptionConstants {

	public boolean run(Hashtable args) throws SystemException {
        boolean isFailed = false;
        isFailed = txadminaccess(args);
        return isFailed;
    }
	/* 
     * Obtains the username and password to use for the
    */
    private boolean txadminaccess(Hashtable args) throws SystemException {

    	checkPrimaryAdminRole();

        String username = ((String) args.get("txadminaccess:args0")).toLowerCase();
        String password = ((String) args.get("txadminaccess:args1")).toLowerCase();

        if ((username == null) || (password == null)) {
            return true;
        }
        subsMain.setAdminAccess(username, password);
        printMessage(resources.getString("cmdline.settxadminaccess"));
        logMsg(LOG_AUDIT_TX_ACCESS_CHANGED, LOG_AUDIT, null, CMD_SET_ADMIN_ACCESS);
        return false;
    }
}
