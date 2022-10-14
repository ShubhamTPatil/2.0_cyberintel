// Copyright 2009, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.cli.commands;

import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.naming.*;
import javax.naming.directory.*;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.SubKnownException;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLIBase;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLICommand;
import com.marimba.apps.subscriptionmanager.users.CLIUser;
import com.marimba.tools.gui.StringResources;
import com.marimba.tools.util.Props;
import com.marimba.tools.util.URLUTF8Decoder;
import com.marimba.webapps.intf.SystemException;

/**
 *  Command for importing policies
 *
 * @author      Kumaravel Ayyakkannu
 * @version 	$Revision$, $Date$
 */


public class ImportFile extends SubscriptionCLICommand implements ISubscriptionConstants {
	
    final static int VALIDATESTATE          = 0;
    final static int VALIDATESECSTATE       = 1;
    final static int VALIDATEORDER          = 2;
    final static int VALIDATECONTENTTYPE    = 3;
    final static int VALIDATEEXPTBLACKOUT   = 4;

    private SubscriptionCLIBase sb;

    private Hashtable validExemptFromBlackout;
    private Hashtable validStates;
    private Hashtable validContentType;
    private Hashtable validTypes;

    public ImportFile() {

	sb = new SubscriptionCLIBase();
	validTypes      = new Hashtable();
	validTypes.put("machine", "machine");
	validTypes.put("machinegroup", "machinegroup");
	validTypes.put("user", "user");
	validTypes.put("usergroup", "usergroup");
	validTypes.put("all", "all");
	validTypes.put("container", "container");
	validTypes.put("collection", "collection");
	validTypes.put("ldapqc", "ldapqc");
    validTypes.put("domain", "domain");
    validTypes.put("site", "site");

    validStates = new Hashtable();
	validStates.put(STATE_NONE, "state");
	validStates.put(STATE_SUBSCRIBE, "state");
	validStates.put(STATE_SUBSCRIBE_START, "state");
	validStates.put(STATE_PRIMARY, "state");
	validStates.put(STATE_DELETE, "state");
	validStates.put(STATE_AVAILABLE, "state");
	validStates.put(STATE_SUBSCRIBE_NOINSTALL, "state");
	validStates.put(STATE_SUBSCRIBE_PERSIST, "state");
	validStates.put(STATE_START_PERSIST, "state");
	validStates.put(STATE_EXCLUDE, "state");

	// Valid value for Exempt from blackout
	validExemptFromBlackout = new Hashtable();
	validExemptFromBlackout.put(EXEMPT_TRUE, "exempt-value");
	validExemptFromBlackout.put(EXEMPT_FALSE, "exempt-value");

	// Valid value for Content Type
	validContentType = new Hashtable();
	validContentType.put(CONTENT_TYPE_APPLICATION, "type");
	validContentType.put(CONTENT_TYPE_PATCHGROUP, "type");
	validContentType.put(CONTENT_TYPE_ALL, "type");
    }
    public boolean run(String file) throws SystemException, IOException {
	boolean isFailed = false;
	isFailed = importFile(file);
	return isFailed;
    }
    public boolean run(Hashtable args) throws SystemException {
	boolean isFailed = false;
	isFailed = importFiles(args);
	return isFailed;
    }
    public void setSubscriptionMain(SubscriptionMain subsMain) {
	this.subsMain = subsMain;
	sb.setSubscriptionMain(subsMain);
    }
    public void setCLIUser(CLIUser cliUser) {
	this.cliUser = cliUser;
	sb.setCLIUser(cliUser);
    }
    public void setResources(StringResources  resources) {
	this.resources = resources;
	sb.setResources(resources);
    }
    /**
     * Import a .sub file.
     *
     * @param file Subscription Policy file to be imported
     *
     * @return REMIND
     *
     * @throws SystemException REMIND
     */
    private boolean importFile(String file)
        throws SystemException, IOException {
    	
        // strip out the filename without the path and extension
        File impFile = null;
        
    	impFile = new File(file);

        if (!impFile.exists()) {
            printMessage(resources.getString("cmdline.subscribeimportfnf") + " " + file);

            return true;
        }

        printMessage(resources.getString("cmdline.subscribeimportfiles") + " " + file);

        String subname = impFile.getName();
        int    index = subname.lastIndexOf(".");

        if (index != -1) {
            subname = subname.substring(0, index)
                             .toLowerCase();
        }

        // validate the subname, since it will define a group type
        index = subname.lastIndexOf("_");

        if (index == -1) {
            printMessage(resources.getString("cmdline.invtargettype") + " " + subname);

            return true;
        }

        String name = subname.substring(0, index);

        String type = subname.substring(index + 1);

        if (!validTypes.containsKey(type)) {
            printMessage(resources.getString("cmdline.invtargettype") + " " + type);

            return true;
        }

        // load the imported props
        Props impProps = new Props(impFile);


        impProps.load();

        try {
            String targetDN = null;
            if(cliUser.getBrowseConn().getParser().isDN(name)) {
                type = sb.resolveTargetType(name);
                targetDN = name;
            } else {
                targetDN = sb.resolveTargetDN(name, type);
            }

            if (!subsMain.getUsersInLDAP()) {
                // In 4.7 Standalone,  there's no concept of machine groups.
                // We will import all groups subscription as user groups.
                // Even though they are actually machine groups in LDAP.
            }

            if (com.marimba.apps.subscriptionmanager.ObjectManager.existsSubscription(targetDN, type, cliUser)) {
                printMessage(resources.getString("cmdline.overwritesubscription") + subname);
                com.marimba.apps.subscriptionmanager.ObjectManager.deleteSub(targetDN, type, cliUser);
            }

            ISubscription sub = com.marimba.apps.subscriptionmanager.ObjectManager.openSubForWrite(targetDN, type, cliUser);

            Enumeration   enumueration = impProps.keys();

            while (enumueration.hasMoreElements()) {
                String key = (String) enumueration.nextElement();
                // Props object doenot support utf8 encoding
                // So read value and decode to UTF
                String value = URLUTF8Decoder.decodeUTF(impProps.getProperty(key));
                key = URLUTF8Decoder.decodeUTF(key);

                if (key.startsWith("http") || key.startsWith("$")) {

                    String temp = "";
                    String state = null;
                    String secState = null;
                    int stateOrder = ORDER;
                    String contentType = null;
                    String initSche = null;
                    String secSche = null;
                    String updateSche = null;
                    String verifyRepairSche = null;
                    boolean exemptBlackOut = false;

                    StringTokenizer st = new StringTokenizer(value,",");

                    if(st.hasMoreElements()) {
                        temp = st.nextToken();
                        if(!temp.equals("null")) {
                            if (!validate(temp, VALIDATESTATE)) {
                                return true;
                            }
                            state = temp;
                        }
                    }

                    if(st.hasMoreElements()) {
                        temp = st.nextToken();
                        if(!temp.equals("null")) {
                            if (!validate(temp, VALIDATEORDER)) {
                                return true;
                            }
                            stateOrder = Integer.parseInt(temp);
                        }
                    }

                    if(st.hasMoreElements()) {
                        temp = st.nextToken();
                        if(!temp.equals("null")) {
                            if (!validate(temp, VALIDATESECSTATE)) {
                                return true;
                            }
                            secState = temp;
                        }
                    }

                    if(st.hasMoreElements()) {
                        temp = st.nextToken();
                        if(!temp.equals("null")) {
                            if (!validate(temp, VALIDATECONTENTTYPE)) {
                                return true;
                            }
                            contentType = temp;
                        }
                    }

                    if(st.hasMoreElements()) {
                        temp = st.nextToken();
                        if(!temp.equals("null")) {
                            initSche = temp;
                        }
                    }

                    if(st.hasMoreElements()) {
                        temp = st.nextToken();
                        if(!temp.equals("null")) {
                            secSche = temp;
                        }
                    }

                    if(st.hasMoreElements()) {
                        temp = st.nextToken();
                        if(!temp.equals("null")) {
                            updateSche = temp;
                        }
                    }

                    if(st.hasMoreElements()) {
                        temp = st.nextToken();
                        if(!temp.equals("null")) {
                            verifyRepairSche = temp;
                        }
                    }

                    if(st.hasMoreElements()) {
                        temp = st.nextToken();
                        if(!temp.equals("null")) {
                            if (!validate(temp, VALIDATEEXPTBLACKOUT)) {
                                return true;
                            }
                            exemptBlackOut = true;
                        }
                    }

                    Channel ch = new Channel(key, null, state, secState, stateOrder);
                    if(contentType != null) {
                        ch.setType(contentType);
                    }

                    if(initSche != null) {
                        ch.setInitSchedule(initSche);
                    }

                    if(secSche != null) {
                        ch.setSecSchedule(secSche);
                    }

                    if(updateSche != null) {
                        ch.setUpdateSchedule(updateSche);
                    }

                    if(verifyRepairSche != null) {
                        ch.setVerRepairSchedule(verifyRepairSche);
                    }

                    if(exemptBlackOut == true) {
                        ch.setExemptFromBlackout(exemptBlackOut);
                    }

                    sub.addChannel(ch);
                } else {
                    if ("".equals(value)) {
                        printMessage(resources.getString("cmdline.subscribeemptyprop") + " " + key);
                    }

                    // A new property/value assignment
                    int ind = key.indexOf(PROP_DELIM);

                    if (ind != -1) {
                        String proptype = key.substring(ind + 1);
                        key = key.substring(0, ind);

                        if (PROP_SERVICE_KEYWORD.equals(proptype) || PROP_CHANNEL_KEYWORD.equals(proptype) || PROP_ALL_CHANNELS_KEYWORD.equals(proptype)) {
                            //The property is either for a service, subscribers
                            //or all the channels in the tuner workspace
                            sub.setProperty(proptype, key, value);
                        }
                    } else if("blackoutperiod".equals(key)) {
                        sub.setBlackOut(value);
                    } else {
                        sub.setProperty(PROP_TUNER_KEYWORD, key, value);
                    }
                }
            }

            // set Property described using <key>,<channel url>=<value> format
            enumueration = impProps.keys();

            while (enumueration.hasMoreElements()) {
                String key = (String) enumueration.nextElement();
                String value = URLUTF8Decoder.decodeUTF(impProps.getProperty(key));
                key = URLUTF8Decoder.decodeUTF(key);

                if (!key.startsWith("http") && !key.startsWith("$")) {
                    if ("".equals(value)) {
                        printMessage(resources.getString("cmdline.subscribeemptyprop") + " " + key);
                    }

                    int ind = key.indexOf(PROP_DELIM);

                    if (ind != -1) {
                        String proptype = key.substring(ind + 1);
                        key = key.substring(0, ind);

                        if (!PROP_SERVICE_KEYWORD.equals(proptype) && !PROP_CHANNEL_KEYWORD.equals(proptype) && !PROP_ALL_CHANNELS_KEYWORD.equals(proptype)) {
                            // The property is set for individual channel by
                            // specifying the channel url
                            Channel chn = sub.getChannel(proptype);

                            if (chn == null) {
                                if(sub.getDummyChannel(proptype) == null) {
                                    chn = sub.createDummyChannel(proptype);
                                }
                                else {
                                    chn =  sub.getDummyChannel(proptype);
                                }
                            }

                            chn.setProperty(key, value);

                        }
                    }
                }
            }

            sub.save();
            logMsg(LOG_AUDIT_POLICY_IMPORT_SUCCESS, LOG_AUDIT, sub.getName(), CMD_IMPORT_POLICY_FILE);
        } catch (SubKnownException se) {
            printMessage(resources.getString("cmdline.failedimportsub") + " " + subname);
            printMessage(resources.getString("cmdline.noimportforcontainers"));
            logMsg(LOG_AUDIT_POLICY_IMPORT_FAILED, LOG_AUDIT, subname, CMD_IMPORT_POLICY_FILE);
            throw se;
        } catch (SystemException se) {
            printMessage(resources.getString("cmdline.failedimportsub") + " " + subname);
            logMsg(LOG_AUDIT_POLICY_IMPORT_FAILED, LOG_AUDIT, subname, CMD_IMPORT_POLICY_FILE);
            throw se;
        } catch(Exception e) {
            e.printStackTrace();
            logMsg(LOG_AUDIT_POLICY_IMPORT_FAILED, LOG_AUDIT, subname, CMD_IMPORT_POLICY_FILE);
        } finally {
	    if(sb.getOutputStr().length() > 0) {
	    	printMessage(sb.getOutputStr());
	    }
	}

        return false;
    }
    
    /**
     * Import .sub properties files. Import files are normalised: i.e. they do not have the '|group' suffix on the keys. The .sub files also contain tuner
     * properties which are identified by not starting with 'http'. The imported files are processed by splitting then up and producing a .sub and a .trp file
     * internally.
     *
     * @param cmds Arguments specifying the .sub files or a directory
     *
     * @return REMIND
     *
     * @throws SystemException REMIND
     */
    private boolean importFiles(Hashtable cmds)
        throws SystemException {
        // determine if the args contain a list of files or a directory
        String arg0 = (String) cmds.get("import:args0");

        if (arg0.startsWith(".")) {
            // get cwd
            if (arg0.length() == 1) {
                arg0 = new File("").getAbsolutePath();
            } else {
                arg0 = new File("").getAbsolutePath() + arg0.substring(1);
            }
        }
        
        try {

        File impDir = new File(arg0);

        if (impDir.isDirectory()) {
            // import entire directory
            String[] files = impDir.list();

            if (files != null) {
                for (int i = 0; i != files.length; i++) {
                    String filename = files [i];
                    try {
                        if (importFile(impDir.getPath() + File.separator + filename)) {
                            return true;
                        }
                    } catch(Exception ex) {
                        printMessage(resources.getString("cmdline.failedimportsub") + " " + files [i] + ". " + resources.getString("cmdline.failedimportreason"));
                    }
                }
            }
        } else {
            // import files
            int    i = 0;
            String arg;

            while ((arg = (String) cmds.get("import:args" + i++)) != null) {
                // work around to open files in the current working directory
                arg = new File(arg).getAbsolutePath();

                if (importFile(arg)) {
                    return true;
                }
            }
        }
        } catch(IOException ioe) {
        	//REMIND: A.K to handle this.
        }

        return false;
    }
    /**
     * Validates the state, Secondary State, Order & Type against respective Hashtable.
     *
     * @param value a string that has to be evaluated.
     * @param msg type of validation that has to be done on value.
     *
     * @return true if valid, else false.
     */
    private boolean validate(String value, int msg) {

        switch (msg) {
            case VALIDATESTATE:
                // validate states with validStates entries
                if (!validStates.containsKey(value)) {
                    printMessage(resources.getString("cmdline.subscribeinvstate") + " " + value);
                    return false;
                }
                break;
            case VALIDATESECSTATE:
                // validate secondary states with validSecondaryStates entries
                if (!validStates.containsKey(value)) {
                    printMessage(resources.getString("cmdline.subscribeinvsecstate") + " " + value);
                    return false;
                }
                break;
            case VALIDATEORDER:
                try {
                    int ord = Integer.parseInt(value);

                    if ((ord <= 0) || (ord > ORDER)) {
                        printMessage(resources.getString("cmdline.subscribeinvorder"));
                        return false;
                    }
                    return true;
                } catch (NumberFormatException npe) {
                    printMessage(resources.getString("cmdline.subscribeinvorder"));
                    return false;
                }
            case VALIDATECONTENTTYPE:
                // validate content type with validContentType entries
                if (!validContentType.containsKey(value)) {
                    printMessage(resources.getString("cmdline.subscribeinvcontenttype") + " " + value);
                    return false;
                }
                break;
            case VALIDATEEXPTBLACKOUT:
                // validate exempt from blackout with validExemptFromBlackout entries
                if (!validExemptFromBlackout.containsKey(value)) {
                    printMessage(resources.getString("cmdline.subscribeinvexempt"));
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }
}
