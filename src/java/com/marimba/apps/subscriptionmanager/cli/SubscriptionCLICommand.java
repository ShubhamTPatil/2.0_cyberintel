// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.cli;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.*;

import javax.servlet.ServletContext;

import com.marimba.apps.subscriptionmanager.users.CLIUser;
import com.marimba.apps.subscriptionmanager.util.Utils;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscription.common.intf.SubKnownException;
import com.marimba.apps.subscription.common.intf.LogConstants;
import com.marimba.intf.msf.ITenant;
import com.marimba.tools.gui.StringResources;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.webapps.intf.SystemException;


/**
 * Base class for the subscription CLI commands. Resources common to atleast 3 CLI
 * commands can be added here.
 *
 * @author	Narayanan A R
 * @version 	$Revision$, $Date$
 */
public abstract class SubscriptionCLICommand implements ISubscriptionCLICommand, IErrorConstants, LDAPConstants, IAppConstants, LogConstants {
    protected StringResources  resources;
    protected StringBuffer     commStr;
    protected SubscriptionMain subsMain;

    protected CLIUser cliUser;
    protected ITenant tenant;

    public void setSubscriptionMain(SubscriptionMain subsMain) {
        this.subsMain = subsMain;
    }

    public void setCLIUser(CLIUser cliUser) {
    	this.cliUser = cliUser;
        tenant = cliUser.getTenant();
    }

    public void setResources(StringResources  resources) {
    	this.resources = resources;
    }

    /**
     * Initialize anything specific to the command and delegate to
     * run method.
     *
     * @param args Arguments passed from the shell
     * @return True if a command fails.
     */
    public boolean execute(Hashtable args) throws SystemException {
        commStr = new StringBuffer(2056);
        return run(args);
    }

    public boolean execute(ServletContext context, Hashtable args) throws SystemException {
        commStr = new StringBuffer(2056);
        return run(context, args);
    }

    public boolean execute(String file) throws SystemException, IOException {
        commStr = new StringBuffer(2056);
        return run(file);
    }

    public boolean execute()throws SystemException {
    	return run();
    }

    public boolean run(ServletContext context, Hashtable args) throws SystemException {
    	return false;
    }

    public boolean run(String file) throws SystemException, IOException {
    	return false;
    }

    public boolean run() throws SystemException {
    	return false;
    }

    /**
     * This method would need to be overridden for providing specific functionality
     * for a given CLI command.
     *
     * @param cliUser User object
     * @param args Arguments passed from the shell
     * @return True if a command fails.
     */
    public boolean run(Hashtable args) throws SystemException {
    	return false;
    }

    public String getOutputStr() {
        if(commStr != null) {
            return commStr.toString();
        } else {
            return "";
        }
    }

    /**
     * Print the output string and append it for display later
     *
     * @param msg String to be displayed
     */
    public void printMessage(String msg) {
        commStr.append(msg);
        commStr.append("\n");
    }
    
    protected void checkPrimaryAdminRole() throws SystemException {
    	boolean isPrimary = Utils.isPrimaryAdmin(cliUser.getUser());
    	
		if  (!isPrimary) {
		    throw new SubKnownException(ACL_ROLE_NOTPRIMARY);
		}
    }
    
    protected void checkAdminRole()	throws SystemException {
		boolean isAdmin = Utils.isAdministrator(cliUser.getUser());

		if  (!isAdmin) {
		    throw new SubKnownException(ACL_ROLE_NOTADMIN);
		}
    }
    
    protected void checkPrimaryAdminRole(CLIUser cliUser) throws SystemException {

	boolean isPrimary = Utils.isPrimaryAdmin(cliUser.getUser());
        if  (!isPrimary) {
            throw new SubKnownException(ACL_ROLE_NOTPRIMARY);
        }
    }

    protected void checkAdminRole(CLIUser cliUser) throws SystemException {

	boolean isAdmin = Utils.isAdministrator(cliUser.getUser());
        if  (!isAdmin) {
            throw new SubKnownException(ACL_ROLE_NOTADMIN);
        }
    }

    /**
     * Method used to log audit messages to common audit log
     *
     * @param id  Message identifier or number
     * @param severity Message Severity
     * @param message Log message or Throwable to capture exceptions
     */
    protected void logMsg(int id, int severity, Object message ) {
        logMsg(id, severity, message, null );
    }
    /**
     * Method used to log audit messages to common audit log
     *
     * @param id  Message identifier or number
     * @param severity Message Severity
     * @param message Log message or Throwable to capture exceptions
     * @param target An additional argument for Audit Log Enhancement
     */
    protected void logMsg(int id, int severity, Object message, String target ) {
        subsMain.log(id, severity, "Policy Manager CLI", cliUser.getName() + " (" + cliUser.getFullName() +")", message, target);
    }
}
