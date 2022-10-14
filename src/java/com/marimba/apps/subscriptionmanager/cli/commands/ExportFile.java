// Copyright 1997-2006, BMC Software. All Rights Reserved.
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
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLICommand;
import com.marimba.apps.subscriptionmanager.users.CLIUser;
import com.marimba.tools.gui.StringResources;
import com.marimba.webapps.intf.SystemException;

/**
 *  Command for Exporting policies to a file.
 *
 * @author      Kumaravel Ayyakkannu
 * @version 	$Revision$, $Date$
 */

public class ExportFile extends SubscriptionCLICommand implements ISubscriptionConstants {
	
    public void setSubscriptionMain(SubscriptionMain subsMain) {
	this.subsMain = subsMain;
    }
    public void setCLIUser(CLIUser cliUser) {
	this.cliUser = cliUser;
    }
    public void setResources(StringResources  resources) {
	this.resources = resources;
    }

    public boolean run(String dir) throws SystemException, IOException {
	boolean isFailed = false;
	checkPrimaryAdminRole(cliUser);
	isFailed = export(dir);
	return isFailed;
    }

    /**
     * Export the .sub property file in normalised form to
     * the specified directory.
     * Normailised form means without the '|group' key, and with all tuner
     * props from the associated .tpr file (if any).
     *
     * This method is not being used in 5.0. If it is ever being used
     * keep in mind to add the properties from dummy channels.
     * @param dir Directory used to stored the exported subscriptions
     */
    private boolean export(String dir) throws SystemException, IOException {
	if (dir.startsWith(".")) {
	    // get cwd
	    if (dir.length() == 1) {
		dir = new File("").getAbsolutePath();
	    } else {
	    dir = new File("").getAbsolutePath() + dir.substring(1);
	    }
	}
	// Check for a valid directory
	File  expDir = new File(dir);

	if(!expDir.isDirectory()) {
	    printMessage(resources.getString("cmdline.subscribeinvdir") + " " + dir);
	    return true;
	}
	    OutputStreamWriter expFileStream = null;

	    // get a list of all the Subscriptions in curent namespace
		//String[] sublist = ObjectManager.listSubscriptionByName(this, this);
		String[] sublist = com.marimba.apps.subscriptionmanager.ObjectManager.listSubscriptionByName(subsMain, cliUser, false);
	    if (sublist == null) {
		printMessage(resources.getString("cmdline.nothingtoexport"));
		return false;
	    }

	    for (int i = 0 ; i < sublist.length; i++) {
		ISubscription sub = com.marimba.apps.subscriptionmanager.ObjectManager.openSubForReadNoError(sublist[i], cliUser, false);
		File expFile;
		if (sub != null) {
		    try {
			if (sub.getTargetID() != null && null != sub.getTargetType() && !(TYPE_SITE.equalsIgnoreCase(sub.getTargetType()))) {
			    expFile = new File(expDir, sub.getTargetID() + "_" + sub.getTargetType() + SUBSCRIPTION_EXT);
			} else {
			    expFile = new File(expDir, sublist[i] + "_" + sub.getTargetType() + SUBSCRIPTION_EXT);
			}

			// Do not overwrite existing file
			if (expFile.exists()) {
			    printMessage(resources.getString("cmdline.cannotcreateexpfile") + sublist[i]);
			    continue;
			}
			expFileStream = new OutputStreamWriter(new FileOutputStream(expFile), "UTF-8");
		    } catch (FileNotFoundException fe) {
			printMessage(resources.getString("cmdline.cannotcreateexpfile") + sublist[i]);
			continue;
		    }

		    try {

			Enumeration chlist = sub.getChannels();
			// Write channel/state/order assignments and
			// individual channel properties

			while(chlist.hasMoreElements()) {
			    Channel chn = (Channel)chlist.nextElement();
			    // Format : url=primaryState,secondaryState,order,content.type,primarySchedule,secondarySchedule,updateSchedule,verifyRepair,exemptFromBlackout,channel properties
			    expFileStream.write(chn.getUrl() + "=" + chn.getState());

			    if (chn.getOrder() < ORDER ) {
				expFileStream.write("," + chn.getOrder()) ;
			    }
			    else {
				expFileStream.write(",null");
			    }

			    if(chn.getSecState() != null) {
				expFileStream.write("," + chn.getSecState());
			    }
			    else {
				expFileStream.write(",null");
			    }

			    if(chn.getType() != null) {
				expFileStream.write("," + chn.getType());
			    }
			    else {
				expFileStream.write(",null");
			    }

			    if(chn.getInitScheduleString() != null) {
				expFileStream.write("," + chn.getInitScheduleString());
			    }
			    else {
				expFileStream.write(",null") ;
			    }

			    if(chn.getSecScheduleString() != null) {
				expFileStream.write("," + chn.getSecScheduleString());
			    }
			    else {
				expFileStream.write(",null");
			    }

			    if(chn.getUpdateScheduleString() != null) {
				expFileStream.write("," + chn.getUpdateScheduleString());
			    }
			    else {
				expFileStream.write(",null");
			    }

			    if(chn.getVerRepairScheduleString() != null) {
				expFileStream.write("," + chn.getVerRepairScheduleString());
			    }
			    else {
				expFileStream.write(",null") ;
			    }

			    if(chn.isExemptFromBlackout() == true) {
				expFileStream.write("," + true);
			    }
			    else {
				expFileStream.write(",null") ;
			    }

			    expFileStream.write("\n");

			    //expFileStream.write(chstr);
			    String[] pairs = chn.getPropertyPairs();

			    for (int j = 0 ; j < pairs.length; j+= 2) {
				expFileStream.write(pairs[j] + "," + chn.getUrl() + "=" + pairs[j+1] + "\n");
			    }
			}

		     // Write tuner and channel properties

			String[] tpairs = sub.getPropertyPairs(PROP_TUNER_KEYWORD);
			String[] spairs = sub.getPropertyPairs(PROP_SERVICE_KEYWORD);
			String[] cpairs = sub.getPropertyPairs(PROP_CHANNEL_KEYWORD);
			String[] apairs = sub.getPropertyPairs(PROP_ALL_CHANNELS_KEYWORD);
			// Write tuner properties

			if (tpairs.length > 0) {
			saveProps(expFileStream, tpairs, PROP_TUNER_KEYWORD);
			}

			// Write subscription service properties

			if (spairs.length > 0) {
			saveProps(expFileStream, spairs, PROP_SERVICE_KEYWORD);
			}

			// Write properties for subscribed channels

			if (cpairs.length > 0) {
			saveProps(expFileStream, cpairs, PROP_CHANNEL_KEYWORD);
			}

			// Write properties for all the channels in the tuner workspace

			if (apairs.length > 0) {
			saveProps(expFileStream, apairs, PROP_ALL_CHANNELS_KEYWORD);
			}

			if(sub.getBlackOut() != null) {
			    String blackOutPeriod = "blackoutperiod=" + sub.getBlackOut();
			    expFileStream.write(blackOutPeriod);
			}
		} catch (IOException ioe) {
		    printMessage(resources.getString("cmdline.errorexportsub"));
		    throw ioe;
		} catch (SystemException se) {
		    printMessage(resources.getString("cmdline.errorexportsub"));
		    throw se;
		} finally {
		    if (expFileStream != null) {
		    try {
			expFileStream.close();
		    } catch (IOException ioe1) {
			    printMessage(resources.getString("cmdline.errorexportsub"));
			throw ioe1;
		    }
		}
	    }
	}
       }
    	printMessage(resources.getString("cmdline.subscribeexported"));
        return false;
    }
    /**
     * Save the properties strings
     *
     * @param expFileStream REMIND
     * @param pairs key value pairs of property strings
     * @param typeStr type of the property
     *
     * @throws IOException REMIND
     */
    private void saveProps(OutputStreamWriter expFileStream,
                           String[]         pairs,
                           String           typeStr)
        throws IOException {
        for (int i = 0; i < pairs.length; i += 2) {
            String str = null;

            if ("".equals(typeStr)) {
                expFileStream.write(pairs [i] + "=" + pairs [i + 1] + "\n");
            } else {
                expFileStream.write(pairs [i] + "," + typeStr + "=" + pairs [i + 1] + "\n");
            }

            if (str != null) {
                expFileStream.write(str);
            }
        }
    }

	
}
