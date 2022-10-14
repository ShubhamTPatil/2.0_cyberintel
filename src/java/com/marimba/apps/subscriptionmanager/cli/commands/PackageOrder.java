// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.cli.commands;

import java.util.*;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLIBase;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLICommand;
import com.marimba.apps.subscriptionmanager.cli.commands.intf.ISubscribe;
import com.marimba.apps.subscriptionmanager.users.CLIUser;
import com.marimba.tools.gui.StringResources;
import com.marimba.webapps.intf.SystemException;

/**
 * command for Rearranging install order of packages
 *
 * @author      Kumaravel Ayyakkannu
 * @version 	$Revision$, $Date$
 */

public class PackageOrder extends SubscriptionCLICommand implements ISubscriptionConstants  {
    private Hashtable validTypes;
    private SubscriptionCLIBase sb;

    public PackageOrder() {
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
    public boolean run(Hashtable args) throws SystemException {
	boolean isFailed = false;
	isFailed = changeorder(args);
	return isFailed;
    }
    /**
     * Changes the priority order of the channels in a policy and rearranges them.
     *
     * @param args arguments for modifying the channel's priority
     *
     * @return REMIND
     *
     * @throws SystemException REMIND
     */
    private boolean changeorder(Hashtable args) throws SystemException {
        int i = 0;
        String arg;
        String name = null;
        String type = null;

        // arg0 if -modify is not entered by the user
        String dnSwitchOrTargetName = ((String) args.get("changeorder:args" + i++)).toLowerCase();

        try {
            if ("-dn".equals(dnSwitchOrTargetName)) {
                // arg2 is name and arg3 is type if -modify is not given
                name = (String) args.get("changeorder:args" + i++);
                type = (String) args.get("changeorder:args" + i++);

                if ("-type".equals(type)) {
                    // arg4 is type if -modify is not given
                    type = (String) args.get("changeorder:args" + i++);

                    type = type.toLowerCase();
		    
                } else {
                    // Shifting the index backwards since type is not found
                    i--;
                    type = sb.resolveTargetType(name);
                }
            } else {
                // create the props file based on the name and type args
                // first validate the type
                name = dnSwitchOrTargetName.toLowerCase();
                type = (String) args.get("changeorder:args" + i++);

                type = type.toLowerCase();

                name = sb.resolveTargetDN(name, type);
            }

            // Create a new Subscription Object in the memory
            ISubscription sub = com.marimba.apps.subscriptionmanager.ObjectManager.openSubForWrite(name, type, cliUser);

            if (type == null) {
                type = sub.getTargetType();
            }

            List  modifyingChannels = new ArrayList();

            while ((arg = (String) args.get("changeorder:args" + i++)) != null) {
                if (arg.startsWith("http") || arg.startsWith("$")) {

                    // add channel objects from the command line args
                    int index = arg.indexOf("=");

                    if (index == -1) {
                        printMessage(resources.getString("cmdline.unknownarg") + " " + arg);

                        return true;
                    }

                    // Channel url
                    String url = arg.substring(0, index);
                    if ((url == null) || "".equals(url)) {
                        printMessage(resources.getString("cmdline.subscribeurlcantbenull"));

                        return true;
                    }
                    // mandatory order
                    String ord = arg.substring(index + 1);
                    int    order = ORDER;
                    try {
                        if( ord.length() > 0 ) {
                            order = Integer.parseInt(ord);

                            if ((order <= 0) || (order > ORDER)) {
                                printMessage(resources.getString("cmdline.subscribeinvorder") + ord);

                                return true;
                            }
                        }
                    } catch (NumberFormatException e) {
                        printMessage(resources.getString("cmdline.subscribeinvorder") + ord);

                        return true;
                    }
                    Channel channel =  sub.createDummyChannel(url);
                    channel.setOrder(order);
                    modifyingChannels.add(channel);
               } else {
                     printMessage(resources.getString("cmdline.unknownarg") + " " + arg);

                    return true;
               }
            }
            boolean channelsNotFound = false;
            Enumeration channelsEnum = sub.getChannels();
            List  channelsList = new ArrayList();

            for(int k =0; k < modifyingChannels.size(); k++) {
                Channel chn = (Channel) modifyingChannels.get(k);
                if(!sub.isChannelFound(chn.getUrl())) {
                    channelsNotFound = true;
                    break;
                }
            }
            if(channelsNotFound) {
                   printMessage(resources.getString("cmdline.changeorderchannelsnotpresent"));

                   return true;
            }
            while( channelsEnum.hasMoreElements() ) {
                Channel chn = (Channel) channelsEnum.nextElement();
                channelsList.add(chn);
            }
            InstallPriority ip = new InstallPriority();
            ip.setExistingChannels(channelsList);
            if(ip.initializeOrder()) {

                printMessage(resources.getString("cmdline.changeorder.rangeexceeded"));
                return true;
            }
            ip.setNewChannelList(modifyingChannels);
            List finalList = ip.getResultChannels();

            for ( int j=0; j < finalList.size(); j++ ) {
                String url = ((Channel)finalList.get(j)).getUrl();
                Channel chnl = sub.getChannel(url);
                chnl.setOrder(((Channel)finalList.get(j)).getOrder());
                sub.modifyChannel(url, chnl.getTitle(), chnl.getState(), chnl.getSecState(), chnl.getOrder(), chnl.isExemptFromBlackout());
            }
            // Save the Subscription into LDAP Server
            sub.save();
            printMessage(resources.getString("cmdline.changeordersaved") + " " + type + " " + name);
            logMsg(LOG_AUDIT_PRIORITY_CHANGE_SAVE_SUCCESS, LOG_AUDIT, " TargetType="+ type + " DN="+ name, CMD_PKG_INSTALL_ORDER);
            return false;

        } catch (SystemException se) {
            printMessage(resources.getString("cmdline.changeorderfailed") + " " + type + " " + name);
            logMsg(LOG_AUDIT_PRIORITY_CHANGE_SAVE_FAILED, LOG_AUDIT, " TargetType="+ type + " DN="+ name, CMD_PKG_INSTALL_ORDER);
            throw se;
        } finally {
	    if(sb.getOutputStr().length() > 0) {
	    	printMessage(sb.getOutputStr());
	    }
	}
    }

}
