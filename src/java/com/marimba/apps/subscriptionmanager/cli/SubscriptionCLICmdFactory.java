// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.cli;

import com.marimba.tools.gui.StringResources;
import com.marimba.apps.subscriptionmanager.cli.commands.*;
import com.marimba.apps.subscriptionmanager.users.CLIUser;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.webapps.intf.SystemException;

import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * Factory class which hosts the CLI command instances. This class is not thread safe, since only one user can
 * use CLI commands at a time.
 *
 * @author	Narayanan A R
 * @version 	$Revision$, $Date$
 */
public class SubscriptionCLICmdFactory {

    // Constant definition for commands
    public static final int SET_PLUGIN_PARAM 		= 0;
    public static final int CREATE_SUBSCRIPTION 	= 1;
    public static final int LIST_SUBSCRIPTION 		= 2;
    public static final int DELETE_SUBSCRIPTION 	= 3;
    public static final int LDAPQC 			= 4;
    public static final int PRIORITY_ORDERING 		= 5;
    public static final int SET_TUNER_PROPS 		= 6;
    public static final int PATCH_SUBSCRIPTION 		= 7;
    public static final int IMPORT_FILE 		= 8;
    public static final int EXPORT_FILE 		= 9;
    public static final int LOAD_MACHINES		= 10;
    public static final int COPY_LDAPSERVERS_FILES 	= 11;
    public static final int PUBLISH 			= 12;
    public static final int CONFIG_SET 			= 13;
    public static final int TX_ADMIN_ACCESS		= 14;
    public static final int CREATE_MULTI_SUBSCRIPTION = 15;
    public static final int CREATE_MULTI_PROPS = 16;
    public static final int REMEDY_SUBSCRIPTION = 17;
    public static final int COPY_POLICY = 18;
    public static final int COMPLIANCE_SETTINGS = 19;

    private Map              cliObjects;
    private StringResources  resources;
    private SubscriptionMain subsMain;
    private CLIUser cliUser;

    public SubscriptionCLICmdFactory(StringResources resources, SubscriptionMain subsMain, CLIUser cliUser) {
        this.resources = resources;
        this.subsMain = subsMain;
        this.cliUser = cliUser;
        cliObjects = new HashMap();
    }

    public ISubscriptionCLICommand getCommand(int cmdIdentifier) {
    	
        Integer cmdIdentifierObj = new Integer(cmdIdentifier);

        ISubscriptionCLICommand command = (ISubscriptionCLICommand)cliObjects.get(cmdIdentifierObj);
        if(command == null) {
            command = prepareCommand(cmdIdentifier);
            cliObjects.put(cmdIdentifierObj, command);
        }
        return command;
    }

    private ISubscriptionCLICommand prepareCommand(int cmdIdentifier) {
        SubscriptionCLICommand command = null;

        switch(cmdIdentifier) {
            case SET_PLUGIN_PARAM:
                command = new SetPluginParam();
                break;

            case CREATE_SUBSCRIPTION:
                command = new Subscribe();
                break;

            case LIST_SUBSCRIPTION:
                command = new ListPolicy();
                break;

            case DELETE_SUBSCRIPTION:
                command = new DeletePolicy();
                break;

            case LDAPQC:
                command = new LdapQc();
                break;

            case PRIORITY_ORDERING:
                command = new PackageOrder();
                break;

            case SET_TUNER_PROPS:
                command = new TunerProps();
                break;

            case PATCH_SUBSCRIPTION:
                command = new PatchSubscribe();
                break;

            case IMPORT_FILE:
                command = new ImportFile();
                break;

            case EXPORT_FILE:
                command = new ExportFile();
                break;

            case LOAD_MACHINES:
                command = new LoadMachines();
                break;

            case COPY_LDAPSERVERS_FILES:
                command = new CopyLDAPServersFile();
                break;

            case PUBLISH:
                command = new Publish();
                break;

            case CONFIG_SET:
                command = new ConfigSet();
                break;

            case TX_ADMIN_ACCESS:
                command = new TxAdminAccess();
                break;

            case CREATE_MULTI_SUBSCRIPTION:
                command = new SubscribeMulti();
                break;

            case CREATE_MULTI_PROPS:
                command = new TunerPropsMulti();
                break;

           case REMEDY_SUBSCRIPTION:
                command = new RemediationSubscribe();
                break;
           case COPY_POLICY:
        	   	command = new CopyPolicy();
        	   	break;
        	   	
           case COMPLIANCE_SETTINGS:
       	   	command = new ComplianceSettings();
       	   	break;
       	   	
            default:
                command = new EmptyCommand();
        }

        // The resources required for the command can be set here.
        command.setResources(resources);
        command.setSubscriptionMain(subsMain);
        command.setCLIUser(cliUser);

        return command;
    }

    private static class EmptyCommand extends SubscriptionCLICommand {

        public boolean run(Hashtable args) throws SystemException {
            printMessage(EmptyCommand.super.resources.getString("cmdline.emptycommand.warning"));
            return false;
        }

    }
}
