// Copyright 1997-2006, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.cli.commands;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.util.PluginUtils;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLICommand;
import com.marimba.apps.subscriptionmanager.cli.commands.intf.ISubscribe;
import com.marimba.apps.subscriptionmanager.users.CLIUser;
import com.marimba.intf.msf.acl.IAclConstants;
import com.marimba.webapps.intf.SystemException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;

import marimba.io.*;

/**
 *  Command for Copying LDAP Server files
 *
 * @author      Kumaravel Ayyakkannu
 * @version 	$Revision$, $Date$
 */

public class CopyLDAPServersFile  extends SubscriptionCLICommand implements ISubscribe,ISubscriptionConstants, IAclConstants  {

    private Hashtable validTypes;

    public CopyLDAPServersFile() {
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
    public boolean run(String file) throws SystemException, IOException {
	boolean isFailed = false;
	isFailed = copyLDAPServersFile(file);
	return isFailed;
    }
    /**
     * Copies specified file containing Transmitter to LDAP Server mapping. This file will be published to the Subscription Plugin during publish operation
     *
     * @param file File containing Transmitter to LDAP Server mapping
     *
     * @return true if command failed to execute succesfully false if command executes succesfully
     */
    private boolean copyLDAPServersFile(String file)
	        throws SystemException, IOException {

	    checkPrimaryAdminRole();

        FastInputStream is = null;

        // Open the user specified file
        try {
            is = new FastInputStream(new File(file));

            PluginUtils putils = new PluginUtils();
            putils.validateLDAPMappingFile(is);

            if (putils.errorCode != PluginUtils.VALID_FILE) {
                if (putils.errorCode == PluginUtils.INVALID_TX_NAME) {
                    printMessage(resources.getString("cmdline.ldapserversinvalidtxname") + " " + putils.errorLineNumber + " " + putils.errorLine);

                    return true;
                } else if (putils.errorCode == PluginUtils.INVALID_SERVER) {
                    printMessage(resources.getString("cmdline.ldapserversinvalidserver") + " " + putils.errorLineNumber + " " + putils.errorLine);

                    return true;
                } else if (putils.errorCode == PluginUtils.INVALID_USESSL) {
                    printMessage(resources.getString("cmdline.ldapserversinvalidusessl") + " " + putils.errorLineNumber + " " + putils.errorLine);

                    return true;
                } else if (putils.errorCode == PluginUtils.INVALID_AUTHMETH) {
                    printMessage(resources.getString("cmdline.ldapserversinvalidauthmethod") + " " + putils.errorLineNumber + " " + putils.errorLine);

                    return true;
                } else if (putils.errorCode == PluginUtils.INVALID_POOLSIZE) {
                    printMessage(resources.getString("cmdline.ldapserversinvalidpoolsize") + " " + putils.errorLineNumber + " " + putils.errorLine);

                    return true;
                } else if (putils.errorCode == PluginUtils.INVALID_KEY) {
                    printMessage(resources.getString("cmdline.ldapserversinvalidkey") + " " + putils.errorLineNumber + " " + putils.errorLine);

                    return true;
                } else if (putils.errorCode == PluginUtils.DUPLICATE_KEY) {
                    printMessage(resources.getString("cmdline.ldapserversduplicatekey") + " " + putils.errorLineNumber + " " + putils.errorLine);

                    return true;
                } else if (putils.errorCode == PluginUtils.MISSING_BASEDN) {
                    printMessage(resources.getString("cmdline.ldapserversmissingbasedn") + " " + putils.errorLine);

                    return true;
                } else if (putils.errorCode == PluginUtils.MISSING_BINDDN) {
                    printMessage(resources.getString("cmdline.ldapserversmissingbinddn") + " " + putils.errorLine);

                    return true;
                } else if (putils.errorCode == PluginUtils.MISSING_PASSWORD) {
                    printMessage(resources.getString("cmdline.ldapserversmissingpassword") + " " + putils.errorLine);
                    return true;
		        }
            }
        } catch (FileNotFoundException fne) {
            printMessage(resources.getString("cmdline.ldapserversfilenotfound") + " " + file);

            return true;
        } catch (IOException ioe) {
            printMessage(resources.getString("cmdline.ldapserverserrorread") + " " + file);

            return true;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe1) {
                printMessage(resources.getString("cmdline.ldapserverserrorread") + " " + file);

                return true;
            }
        }

        FastInputStream  is1 = null;
        FastOutputStream fos = null;

        try {
            is1 = new FastInputStream(new File(file));
        } catch (FileNotFoundException fne) {
            printMessage(resources.getString("cmdline.ldapserversfilenotfound") + " " + file);

            return true;
        } catch (IOException ioe) {
            printMessage(resources.getString("cmdline.ldapserverserrorread") + " " + file);

            return true;
        }

        if (is1 != null) {
            try {
            	
                File plugindir = new File(subsMain.getDataDirectory(), subsMain.generatePluginDirectoryName());

                if (!plugindir.exists()) {
                    plugindir.mkdir();
                }

                fos = new FastOutputStream(new File(plugindir, "ldapservers.txt"));

                try {
                   // Copy specified file into the data/persist/subconfig directory
                     //to publish to the Transmitter during -publish option
                    fos.sendStream(is1, -1);
                } finally {
                    if (fos != null) {
                        fos.close();
                        fos = null;
                    }

                    if (is1 != null) {
                        is1.close();
                        is1 = null;
                    }
                }

                printMessage(resources.getString("cmdline.importedldapservers") + " " + file);
            } catch (FileNotFoundException fne) {
                printMessage(resources.getString("cmdline.ldapserversfilenotfound") + " " + (subsMain.getDataDirectory()+ subsMain.generatePluginDirectoryName()));

                return true;
            } catch (IOException ioe) {
                printMessage(resources.getString("cmdline.ldapserverserrorwrite"));

                return true;
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                        fos = null;
                    }

                    if (is1 != null) {
                        is1.close();
                        is1 = null;
                    }
                } catch (IOException ioe) {
                    printMessage(resources.getString("cmdline.ldapserverserrorwrite"));

                    return true;
                }
            }
        }

        return false;
    }
}
