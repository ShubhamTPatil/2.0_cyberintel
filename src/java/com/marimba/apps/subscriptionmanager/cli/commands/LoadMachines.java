// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.cli.commands;

import java.io.File;
import java.util.*;

import javax.naming.NamingException;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscriptionmanager.MachinesLoader;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLICommand;
import com.marimba.apps.subscriptionmanager.cli.commands.intf.ISubscribe;
import com.marimba.apps.subscriptionmanager.users.CLIUser;
import com.marimba.webapps.intf.SystemException;

/**
 *  Command for Loading machines from a file to the LDAP
 *
 * @author      Kumaravel Ayyakkannu
 * @version 	$Revision$, $Date$
 */

public class LoadMachines  extends SubscriptionCLICommand implements ISubscribe,ISubscriptionConstants {
    private Hashtable validTypes;

    public LoadMachines() {
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
    }
    public void setCLIUser(CLIUser cliUser) {
    	super.setCLIUser(cliUser);
    }
    public boolean run(String file) throws SystemException {
    	boolean isFailed = false;
    	isFailed = loadMachines(file);
    	return isFailed;
    }
    /**
     * Loads machines and groups from a file into an iPlanet LDAP Server
     *
     * @param file File containing machine and machines group names
     *
     * @return true if command failed to execute succesfully false if command executes succesfully
     *
     * @throws SystemException REMIND
     */
    private boolean loadMachines(String file)
        throws SystemException {
    	
    	File impFile = new File(new File(file).getAbsolutePath());
        try {

            MachinesLoader mcloader = new MachinesLoader(subsMain, impFile, cliUser.getSubConn());
            mcloader.importFile();
            printMessage(resources.getString("cmdline.importedmachines") + " " + file);
            logMsg(LOG_AUDIT_LOAD_MACHINES_SUCCESS, LOG_AUDIT, file, CMD_LOAD_MACHINE_GROUP);
            return false;
        } catch (NamingException ne){
        	printMessage(resources.getString("cmdline.failedimportmachines") + " " + file);
            logMsg(LOG_AUDIT_LOAD_MACHINES_FAILED, LOG_AUDIT, file, CMD_LOAD_MACHINE_GROUP);
            throw new SystemException(ne,IMPORTMACHINES_INSERTMACHINE);
        } catch (SystemException se) {
        	printMessage(resources.getString("cmdline.failedimportmachines") + " " + file);
            logMsg(LOG_AUDIT_LOAD_MACHINES_FAILED, LOG_AUDIT, file, CMD_LOAD_MACHINE_GROUP);
            throw se;
        }
    }


}
