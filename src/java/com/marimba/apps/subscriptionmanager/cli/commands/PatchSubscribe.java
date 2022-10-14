// Copyright 1997-2009, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.cli.commands;

import java.util.*;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscriptionmanager.users.CLIUser;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.objects.PatchGroupChannel;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLIBase;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLICommand;
import com.marimba.apps.subscriptionmanager.cli.commands.intf.ISubscribe;
import com.marimba.apps.subscriptionmanager.webapp.intf.GUIConstants;
import com.marimba.apps.subscriptionmanager.webapp.util.PatchManagerHelper;
import com.marimba.apps.subscriptionmanager.webapp.util.PatchUtils;
import com.marimba.castanet.schedule.Schedule;
import com.marimba.castanet.schedule.ScheduleInfo;
import com.marimba.tools.gui.StringResources;
import com.marimba.webapps.intf.SystemException;

/**
 * Command for Subscibing Patches
 *
 * @author      Kumaravel Ayyakkannu
 * @version 	$Revision$, $Date$
 */

public class PatchSubscribe extends SubscriptionCLICommand implements ISubscribe,ISubscriptionConstants {

    private SubscriptionCLIBase  sb;
    private Hashtable validTypes;
    private Hashtable validPatchState;
    private Hashtable subStateMapping;
    private Hashtable validExemptFromBlackout;

    public PatchSubscribe() {
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

	//Valid patch assignment types
	validPatchState = new Hashtable();
	validPatchState.put(PATCH_ASSIGN, "type");
	validPatchState.put(PATCH_EXCLUDE, "type");

	// Valid value for Exempt from blackout
	validExemptFromBlackout = new Hashtable();
	validExemptFromBlackout.put(EXEMPT_TRUE, "exempt-value");
	validExemptFromBlackout.put(EXEMPT_FALSE, "exempt-value");

	subStateMapping = new Hashtable();
	subStateMapping.put(ADVERTISE, AVAILABLE);
	subStateMapping.put(STAGE, SUBSCRIBE_NOINSTALL);
	subStateMapping.put(INSTALL, SUBSCRIBE);
	subStateMapping.put(ASSIGN, SUBSCRIBE);
	subStateMapping.put(INSTALL_START, SUBSCRIBE_START);
	subStateMapping.put(INSTALL_START_PERSIST, START_PERSIST);
	subStateMapping.put(INSTALL_PERSIST, SUBSCRIBE_PERSIST);
	subStateMapping.put(UNINSTALL, DELETE);
	subStateMapping.put(EXCLUDE, EXCLUDE);
	subStateMapping.put(PRIMARY, PRIMARY);
    }
    public boolean run(Hashtable args) throws SystemException {
	boolean isFailed = false;
	isFailed = patchsubscribe(args);
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
     * Set up patch subscription based on cmdline args
     *
     * @param args arguments for creating new Patch Subscription Policy
     *
     * @return REMIND
     *
     * @throws SystemException REMIND
     */
    private boolean patchsubscribe(Hashtable args)
        throws SystemException {
        int i = 0;
        String arg;
        String name = null;
        String type = null;
        boolean isModifyPolicy = false;

        //arg0
        if("-modify".equals((String) args.get("patchsubscribe:args" + i++))) {
            isModifyPolicy = true;
        } else {
            // Shifting the index backwards since -modify is not found
            i--;
        }

        // arg0 if -modify is not entered by the user
        String dnSwitchOrTargetName = ((String) args.get("patchsubscribe:args" + i++)).toLowerCase();

        try {
            if ("-dn".equals(dnSwitchOrTargetName)) {
                // arg2 is name and arg3 is type if -modify is not given
                name = (String) args.get("patchsubscribe:args" + i++);
                type = (String) args.get("patchsubscribe:args" + i++);

                if ("-type".equals(type)) {
                    // arg4 is type if -modify is not given
                    type = (String) args.get("patchsubscribe:args" + i++);

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
                type = (String) args.get("patchsubscribe:args" + i++);

              	type = type.toLowerCase();

                name = sb.resolveTargetDN(name, type);
            }

            // Create a new Subscription Object in the memory
            ISubscription sub = com.marimba.apps.subscriptionmanager.ObjectManager.openSubForWrite(name, type, cliUser);

            if (type == null) {
                type = sub.getTargetType();
            }

            if (sub.exists()) {
                if(!isModifyPolicy) {
                    printMessage(resources.getString("cmdline.overwritesubscription") + " " + type + " " + name);
                    sub.clearChannels();
                    sub.setBlackOut(null);
                } else {
                    printMessage(resources.getString("cmdline.modifiessubscription") + " " + type + " " + name);
                }
            } else {
                printMessage(resources.getString("cmdline.createssubscription") + " " + type + " " + name);
            }

            String url = null;
            while ((arg = (String) args.get("patchsubscribe:args" + i++)) != null) {
                // Read schedules in a different loop
                if (arg.startsWith("-sched") || "-wowdep".equals(arg)) {
                    i--;

                    break;
                }

                // add channel objects from the command line args
                int index = arg.indexOf("=");

                if (index == -1) {
                    printMessage(resources.getString("cmdline.subscribeinvstate") + arg);

                    return true;
                }

                // Channel url
                url = arg.substring(0, index);
                if ((url == null) || "".equals(url)) {
                    printMessage(resources.getString("cmdline.subscribeurlcantbenull"));

                    return true;
                }

				if(!(url.startsWith("http") || url.startsWith("$"))) {
					printMessage(resources.getString("cmdline.unknownarg") + " : " + url);

					return true;
				} else {
					if(url.startsWith("http")) {
						if(!sb.isValidUrl(url)) {
							printMessage(resources.getString("cmdline.subscribeurlinvalid") + " : " + url);

							return true;
						}
					}
				}
                // String containing primary , optional secondary
                // states and optional order
                String stateOrder = arg.substring(index + 1)
                                       .toLowerCase();
                index = stateOrder.indexOf(PROP_DELIM);

                String state1 = null;
                String exemptFromBlackout = null;
                boolean isExempt = false;

                if (index != -1) {
                    state1 = stateOrder.substring(0, index);
                    exemptFromBlackout = stateOrder.substring(index + 1);
                    if(exemptFromBlackout != null) {
                        if(!validExemptFromBlackout.containsKey(exemptFromBlackout)) {
                            printMessage(resources.getString("cmdline.subscribeinvexempt") + exemptFromBlackout);
                            return true;
                        }
                        isExempt = Boolean.valueOf(exemptFromBlackout).booleanValue();
                    }
                }else{
                    state1 = stateOrder;
                }

                if (!validPatchState.containsKey(state1)) {
                    printMessage(resources.getString("cmdline.subscribeinvstate") + " " + state1);
                    return true;
                }

                state1 = getSubscriptionState(state1);

                // If the channel is already found, modify the channel else add a new one to the
                // Subscription object.
                if(sub.isChannelFound(url)) {
                    sub.modifyChannel(url, null, state1, null, ORDER, isExempt);
                } else {
                    //Create a new channel for a patch group
                    //channel name is the title of the patch group
                    String patchtitle = "";
                    if(url != null) {
                        StringTokenizer st = new StringTokenizer(url,"/");
                        if(st != null) {
                            while(st.hasMoreTokens()) {
                                patchtitle = st.nextToken();
                            }
                        }
                    }
                    PatchGroupChannel pGroupCh = new PatchGroupChannel(url,patchtitle,state1,null,ORDER,null,null);
                    pGroupCh.setExemptFromBlackout(isExempt);
                    sub.addChannel(pGroupCh);
                }

                printMessage(url + ": " + state1);
            }

            // Read the schedule arguments
            while ((arg = (String) args.get("patchsubscribe:args" + i++)) != null) {
                if (arg.startsWith("-sched") && arg.equalsIgnoreCase("-schedpatch")) {
                    // Schedule String
                    String schstr = (String) args.get("patchsubscribe:args" + i++);

                    if ((schstr == null) || "".equals(schstr)) {
                        printMessage(resources.getString("cmdline.subscribeinvschedule"));
                        return true;
                    }

                    Schedule     sch = Schedule.readSchedule(schstr);
                    ScheduleInfo schinfo = Schedule.getScheduleInfo(schstr);

                    // If the Schedule object gets set to NEVER that means
                    // the schedule string was invalid unless the scedule
                    // string itself is specified as never
                    if ((schinfo == null) || (!"never".equals(schstr) && (schinfo.getFlag(ScheduleInfo.CALENDAR_PERIOD)
                                                                                     .intValue() == ScheduleInfo.NEVER))) {
                        printMessage(resources.getString("cmdline.subscribeinvschedule") + schstr);
                        return true;
                    }

                    //Validate the update schedule??
                    //Set the patch schedule globally
                    //todo : get current tenant from subscriptioncli command after supporting multi-tenancy in command line
                    String patchServiceUrl = PatchManagerHelper.getPatchServiceUrl(cliUser.getUser(), subsMain.getTenant());
                    if(patchServiceUrl != null) {
                        PatchUtils.setPatchServiceProperty(sub,patchServiceUrl, "subscription.update",schstr);
                    }else{
                        printMessage(resources.getString("cmdline.unknownarg") + " " + arg);
                        return true;
                    }
                } else if ("-wowdep".equals(arg)) {
                    String wowDeployment = (String) args.get("patchsubscribe:args" + i++);
                    Channel ch = sub.getChannel(url);
                    if (ch != null) {
                        ch.setWowEnabled(Boolean.valueOf(wowDeployment).booleanValue());
                    }
                } else {
                    printMessage(resources.getString("cmdline.unknownarg") + " " + arg);

                    return true;
                }
            }

            // Save the Subscription into LDAP Server
            sub.save();
            com.marimba.apps.subscriptionmanager.webapp.util.Utils.scheduleCMSTask(sub, subsMain.getWakeMgr(), subsMain.getTaskMgr(), subsMain.getTenant());
            printMessage(resources.getString("cmdline.subscribesaved") + " " + type + " " + name);
            logMsg(LOG_AUDIT_PATCH_SUBSCRIBE_SAVE_SUCCESS, LOG_AUDIT, type + " " + name, CMD_SUBSCRIBE_PATCH);
            return false;
        } catch (SystemException se) {
            printMessage(resources.getString("cmdline.subscribesavefailed") + " " + type + " " + name);
            logMsg(LOG_AUDIT_PATCH_SUBSCRIBE_SAVE_FAILED, LOG_AUDIT, type + " " + name, CMD_SUBSCRIBE_PATCH);
            throw se;
        } finally {
	    if(sb.getOutputStr().length() > 0) {
	    	printMessage(sb.getOutputStr());
	    }
	}
    }

    /**
     * Get Subscription state given the display state
     *
     * @param  displayState  display state
     * @return substate      subscription state
     */
    private String getSubscriptionState(String displayState) {
        String subState = (String) subStateMapping.get(displayState);

        // No mapping found , must be already a subscription state
        if (subState == null) {
            return displayState;
        }

        return subState;
    }

}
