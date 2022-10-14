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
import com.marimba.apps.subscription.common.util.ScheduleUtils;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLICommand;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLIBase;
import com.marimba.apps.subscriptionmanager.cli.commands.utils.SubscribeArgParser;
import com.marimba.apps.subscriptionmanager.cli.commands.intf.ISubscribe;
import com.marimba.apps.subscriptionmanager.util.Utils;
import com.marimba.castanet.schedule.Schedule;
import com.marimba.castanet.schedule.ScheduleInfo;
import com.marimba.castanet.schedule.SchedulePeriod;
import com.marimba.tools.gui.StringResources;
import com.marimba.webapps.intf.SystemException;

/**
 * Subscribe packages with states and schedules to targets.
 *
 * @author      Kumaravel Ayyakkannu
 * @version 	$Revision$, $Date$
 */
public class Subscribe extends SubscriptionCLICommand implements ISubscribe,ISubscriptionConstants {

    private Hashtable subStateMapping;
    private Hashtable objectConversionMapping;
    private Hashtable validTypes;
    private Hashtable validContentType;
    private Hashtable validStates;
    private Hashtable validExemptFromBlackout;

    protected SubscriptionCLIBase sb;

    public Subscribe() {

	sb =  new SubscriptionCLIBase();
    objectConversionMapping = new Hashtable();
	objectConversionMapping.put("machines", "computers");
	objectConversionMapping.put("computers", "machines");
	objectConversionMapping.put("people", "users");
	objectConversionMapping.put("users", "people");

	// Valid value for Content Type
	validContentType = new Hashtable();
	validContentType.put(CONTENT_TYPE_APPLICATION, "type");
	validContentType.put(CONTENT_TYPE_PATCHGROUP, "type");
	validContentType.put(CONTENT_TYPE_ALL, "type");

	validTypes  = new Hashtable();
	validTypes.put("machine", "machine");
	validTypes.put("machinegroup", "machinegroup");
	validTypes.put("user", "user");
	validTypes.put("usergroup", "usergroup");
	validTypes.put("all", "all");
	validTypes.put("container", "container");
	validTypes.put("collection", "collection");
	validTypes.put("ldapqc", "ldapqc");

	// Display state to  Subscription state mapping
	subStateMapping = new Hashtable();
	subStateMapping.put("advertise", "available");
	subStateMapping.put("stage", "subscribe_noinstall");
	subStateMapping.put("install", "subscribe");
	subStateMapping.put("assign", "subscribe");
	subStateMapping.put("install-start", "subscribe_start");
	subStateMapping.put("install-start-persist", "start_persist");
	subStateMapping.put("install-persist", "subscribe_persist");
	subStateMapping.put("uninstall", "delete");
	subStateMapping.put("exclude", "exclude");
	subStateMapping.put("primary", "primary");


	// Valid subscription states when a single state is specified
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
	isFailed = subscribe(args);
	return isFailed;
    }
    public boolean subscribe(Hashtable args) throws SystemException {
	int i = 0;
	String arg;
	String name = null;
	String type = null;
    String url = null;
    boolean isModifyPolicy = false;
    boolean removeChannel = false;

    //arg0
	if("-modify".equals((String) args.get("subscribe:args" + i++))) {
	    isModifyPolicy = true;
	} else {
	    // Shifting the index backwards since -modify is not found
	    i--;
	}

	// arg0 if -modify is not entered by the user
	String dnSwitchOrTargetName = ((String) args.get("subscribe:args" + i++)).toLowerCase();

	try {
        if ("-remove".equals(dnSwitchOrTargetName)) {
            dnSwitchOrTargetName = ((String) args.get("subscribe:args" + i++)).toLowerCase();
            removeChannel = true;
        }
        if ("-dn".equals(dnSwitchOrTargetName)) {
		// arg2 is name and arg3 is type if -modify is not given
		name = (String) args.get("subscribe:args" + i++);
		type = (String) args.get("subscribe:args" + i++);

		if ("-type".equals(type)) {
		    // arg4 is type if -modify is not given
		    type = (String) args.get("subscribe:args" + i++);
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
		type = (String) args.get("subscribe:args" + i++);

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
                for (Enumeration enumChannels = sub.getChannels(); enumChannels.hasMoreElements();) {
                     Channel chn = (Channel) enumChannels.nextElement();
                     url = (String) chn.getUrl();
                     sub.removeChannel(url);
                 }
           }
	    }
        if ( removeChannel) {
            while ((url = (String) args.get("subscribe:args" + i++)) != null) {
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
                if(sub.isChannelFound(url)) {
		            sub.removeChannel(url);
                    printMessage(resources.getString("cmdline.removedchannel") + " " + url );
                } else {
                    printMessage(resources.getString("cmdline.channelnotexits") + " " + url );
                }

            }
        } else {
            while ((arg = (String) args.get("subscribe:args" + i++)) != null) {
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

                int    order = ORDER;
                boolean isExempt = false;

                SubscribeArgParser parser =new  SubscribeArgParser(stateOrder);
                if(parser.getArgCount() == 0 || parser.getArgCount() > 4 ) {
                    printMessage(resources.getString("cmdline.acl.missingargs") + " " + stateOrder);

                    return true;
                }
                HashMap map = parser.parseStatesAndOrder();
                String state1 = (String) map.get("state1");
                String state2 = (String) map.get("state2");
                String orderstr = (String) map.get("order");
                String exemptStr = (String) map.get("exempt");

                // Primary State is mandatory. So check for its presence.
                if(state1 == null || state1.length() == 0 ) {
                    return true;
                }
                state1 = getSubscriptionState(state1);
                if (!validate(state1, VALIDATESTATE)) {
                    return true;
                }
                // validates Secondary state
                if(state2 != null && state2.length() > 0 ) {
                    state2 = getSubscriptionState(state2);
                    if (!validate(state2, VALIDATESECSTATE)) {
                        return true;
                    }
                }
                // validates order
                if(orderstr != null && orderstr.length() > 0 ) {
                    if (!validate(orderstr, VALIDATEORDER)) {
                        return true;
                    }
                    order = Integer.parseInt(orderstr);
                }
                // validates exempt from blackout.
                if(exemptStr != null && exemptStr.length() > 0 ) {
                    if (!validate(exemptStr, VALIDATEEXPTBLACKOUT)) {
                        return true;
                    }
                    isExempt = Boolean.valueOf(exemptStr).booleanValue();
                }
                // If the channel is already found, modify the channel else add a new one to the
                // Subscription object.
                if(sub.isChannelFound(url)) {
                    sub.modifyChannel(url, null, state1, state2, order, isExempt);
                } else {
                    sub.addChannel(url, null, state1, state2, order, isExempt);
                }
            }

            // Type of the schedule such as primary , secondary etc.
            String proptype = null;
            Channel ch = sub.getChannel(url);


            // Read the schedule arguments
            while ((arg = (String) args.get("subscribe:args" + i++)) != null) {
                if (arg.startsWith("-sched")) {
                    // Read the blackout schedule
                    if (SCHEDBLACKOUT.equals(arg)) {
                        String blkshd = (String) args.get("subscribe:args" + i++);

                        if (blkshd == null) {
                            printMessage(resources.getString("cmdline.subscribeinvblackoutschedule"));

                            return true;
                        }
                        else {
                            String time, days = null ;
                            StringTokenizer st_time = new StringTokenizer(blkshd, "@");

                            if(st_time.countTokens() == 2) {
                                days = st_time.nextToken().trim();
                            }

                            time = st_time.nextToken().trim();

                            if (!Utils.validateBlackoutScheduleTime(time)) {
                                printMessage(resources.getString("cmdline.subscribeinvblackoutscheduletime") + time);
                                printMessage(resources.getString("cmdline.usage.subscrib1.opt2.desc"));

                                return true;
                            }
                            if (!Utils.validateBlackoutScheduleDays(days)) {
                                printMessage(resources.getString("cmdline.subscribeinvblackoutscheduleday") + days);
                                printMessage(resources.getString("cmdline.usage.subscrib1.opt2.desc"));

                                return true;
                            }
                            else {
                                blkshd = Utils.constructBlkoutString(days, time);
                            }
                            sub.setBlackOut(blkshd);
                        }

                        continue;
                    }

                    if (SCHEDPRIMARY.equals(arg) || SCHEDSECONDARY.equals(arg) ||
                            SCHEDUPDATE.equals(arg) || SCHEDVERREPAIR.equals(arg) || SCHEDPOSTPONE.equals(arg)) {
                        proptype = arg;

                        continue;
                        } else {
                            printMessage(resources.getString("cmdline.unknownarg") + " " + arg);

                            return true;
                        }
                        } else if (arg.startsWith("http") || arg.startsWith("$")) {

                            // The channel URL must start with http or if the
                            // transmitter name is specified as a macro then with a $
                            if (proptype == null) {
                            printMessage(resources.getString("cmdline.unknownarg") + " " + arg);

                            return true;
                        }

                        // All the Channel Schedule Args should be in form
                        // <channelurl>=<schedulestring>
                        int index = arg.indexOf("=");

                        if (index == -1) {
                            printMessage(resources.getString("cmdline.subscribeinvschedule") + arg);

                            return true;
                        }

                        // Channel URL
                        url = arg.substring(0, index);

                        if ((url == null) || "".equals(url)) {
                            printMessage(resources.getString("cmdline.subscribeurlcantbenull"));

                            return true;
                        }
                        if(url.startsWith("http")) {
                            if(!sb.isValidUrl(arg)) {
                                printMessage(resources.getString("cmdline.subscribeurlinvalid") + " : " + url);

                                return true;
                            }
                        }

                        ch = sub.getChannel(url);
                        if (ch == null) {
                            printMessage(resources.getString("cmdline.subscribechannelnotinpolicy") + url);
                            return true;
                        }


                        // Schedule String
                        String schstr = arg.substring(index + 1);

                        // Pass the schedule string to the Schedule Engine
                        ScheduleEngine se = new  ScheduleEngine(schstr);

                        if (SCHEDPRIMARY.equals(proptype)) {
                            if(!se.validatePrimarySchedule()) {
                                return true;
                            }
                            ch.setInitSchedule(se.getSchedule());
                            } else if (SCHEDSECONDARY.equals(proptype)) {
                                // If secondary schedule is specified then secondary
                                // state also must be specified
                                if (ch.getSecState() == null) {
                                    printMessage(resources.getString("cmdline.secondarystatenotfound") + " " + schstr );
                                    return true;
                                }
                                if(!se.validateSecondaySchedule()) {
                                    return true;
                                }
                                ch.setSecSchedule(se.getSchedule());
                                } else if (SCHEDUPDATE.equals(proptype)) {
                                    if(!se.validateUpdateSchedule()) {
                                        return true;
                                    }
                                    ch.setUpdateSchedule(se.getSchedule());
                                } else if (SCHEDVERREPAIR.equals(proptype)) {
                                    if(!se.validateVerifyRepairSchedule()) {
                                        return true;
                                    }
                                    ch.setVerRepairSchedule(se.getSchedule());
                               } else if (SCHEDPOSTPONE.equals(proptype)) {
                                   if (!se.validatePostponeSchedule()) {
                                       return true;
                                   }
                                   ch.setPostponeSchedule(se.getSchedule());
                               }
                        } else if ("-wowdep".equals(arg)) {
                            String wowDeployment = (String) args.get("subscribe:args" + i++);
                            ch.setWowEnabled(Boolean.valueOf(wowDeployment).booleanValue());

                        } else {
                            printMessage(resources.getString("cmdline.unknownarg") + " " + arg);

                            return true;
                        }
                    }

            // If there is either a primary or secondary schedule,
            // the start date for the secondary schedule must be before
            // the start date of the initial schedule.
            // If there is a secondary state specified, there must be a
            // secondary schedule associated.
            Enumeration list = sub.getChannels();

            while (list.hasMoreElements()) {
            Channel chn = (Channel) list.nextElement();

            if ((chn.getSecState() != null) && (chn.getSecSchedule() == null)) {
                printMessage(resources.getString("cmdline.subscribesecerror") + " " + chn.getUrl());

                return true;
            }

            Schedule initsched = chn.getInitSchedule();
            Schedule secsched = chn.getSecSchedule();

            if ((initsched != null) && (secsched != null)) {
                if (!ScheduleUtils.validateSchedules(initsched.toString(), secsched.toString())) {
                printMessage(resources.getString("cmdline.subscribeprimschedfirst") + " " + chn.getUrl());

                return true;
                }
            }
            }
        }

        if (sub.exists()) {
		    if(!isModifyPolicy) {
		        printMessage(resources.getString("cmdline.overwritesubscription") + " " + type + " " + name);
                logMsg(LOG_AUDIT_CHANNEL_SUBSCRIBE_POLICY_OVERWRITTING, LOG_AUDIT, type + " "+ name, CMD_SUBSCRIBE_PKG);
		    } else {
		        printMessage(resources.getString("cmdline.modifiessubscription") + " " + type + " " + name);
                logMsg(LOG_AUDIT_CHANNEL_SUBSCRIBE_POLICY_MODIFIED, LOG_AUDIT, type+ " "+ name, CMD_SUBSCRIBE_PKG);
		    }
	        } else {
		        printMessage(resources.getString("cmdline.createssubscription") + " " + type + " " + name);
                logMsg(LOG_AUDIT_CHANNEL_SUBSCRIBE_POLICY_CREATED, LOG_AUDIT, type + " "+ name, CMD_SUBSCRIBE_PKG);
	        }
	        // Save the Subscription into LDAP Server
	        sub.save();
            com.marimba.apps.subscriptionmanager.webapp.util.Utils.scheduleCMSTask(sub, subsMain.getWakeMgr(), subsMain.getTaskMgr(), subsMain.getTenant());
	        printMessage(resources.getString("cmdline.subscribesaved") + " " + type + " " + name);
            logMsg(LOG_AUDIT_CHANNEL_SUBSCRIBE_POLICY_SAVE_SUCCESS, LOG_AUDIT, type + " "+ name, CMD_SUBSCRIBE_PKG);

	        return false;
	    } catch (SystemException se) {
	        printMessage(resources.getString("cmdline.subscribesavefailed") + " " + type + " " + name);
            logMsg(LOG_AUDIT_CHANNEL_SUBSCRIBE_POLICY_SAVE_FAILED, LOG_AUDIT, type + " "+ name, CMD_SUBSCRIBE_PKG);
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
  private class ScheduleEngine {
    String schstr;
    Schedule sch;
    ScheduleInfo schinfo;

    ScheduleEngine() {
    }
    ScheduleEngine(String schstr) {
	    this.schstr = schstr;
	    prepareSched();
    }
    private void prepareSched() {
	    this.schstr = schstr.toLowerCase().trim();
	    this.sch = Schedule.readSchedule(schstr);
	    this.schinfo = Schedule.getScheduleInfo(schstr);
    }
    private boolean isSchedulePresent() {
	if ((this.schstr == null) || "".equals(this.schstr)) {
	    return false;
	}
    	return true;
    }
    private boolean isValid() {
	if ((schinfo == null) || (!"never".equals(schstr) && (schinfo.getFlag(ScheduleInfo.CALENDAR_PERIOD).intValue() == ScheduleInfo.NEVER))) {
	    return false;
	}
	return true;
    }
    private boolean isValidStartDate() {
	    return (!ScheduleUtils.isScheduleStarted(schinfo));
    }
    private boolean isValidEndDate() {
	    return (!ScheduleUtils.isScheduleExpired(schinfo));
    }
    private boolean isEndAfterStartDate() {
	SchedulePeriod schedulePeriod = ScheduleUtils.getActivePeriod(schinfo);
	if( schedulePeriod == null || schedulePeriod.getStartSchedulePeriod() == null) {
		return false;
	} else {
		return (schedulePeriod.getStartSchedulePeriod().getTime() < schedulePeriod.getEndSchedulePeriod().getTime());
	}
    }
    public boolean isActiveEndPresent() {
        SchedulePeriod schedulePeriod = ScheduleUtils.getActivePeriod(schinfo);
        if( schedulePeriod == null || schedulePeriod.getEndSchedulePeriod() == null) {
                return false;
        }
        return true;
    }
    public boolean isActiveStartPresent() {
        SchedulePeriod schedulePeriod = ScheduleUtils.getActivePeriod(schinfo);
        if( schedulePeriod == null || schedulePeriod.getStartSchedulePeriod() == null) {
              return false;
        }
        return true;
    }
    public boolean validateSchedule(String schstr) {
	this.schstr = schstr;
	this.sch = Schedule.readSchedule(schstr);
	this.schinfo = Schedule.getScheduleInfo(schstr);
	return validateActiveStartEndSchedules();
    }

    public boolean validateSchedule(String primarySchstr, String secondarySchstr) {
	    if(validateSchedule(primarySchstr) && validateSchedule(secondarySchstr) ) {
			    if(!ScheduleUtils.validateSchedules(primarySchstr, secondarySchstr)) {
				    printMessage(resources.getString("cmdline.subscribeprimschedfirst"));
				    return false;
			    }
			    return true;
	    }
	    return false;
    }
    private boolean validatePrimarySchedule() {
       return validateActiveStartEndSchedules();
    }
    private boolean validateSecondaySchedule() {
       return validateActiveStartEndSchedules();
    }
    private boolean validateUpdateSchedule() {
       return validateRecurrenceSchedules();
   }
   private boolean validateVerifyRepairSchedule() {
      return validateRecurrenceSchedules();
   }
   private boolean validatePostponeSchedule() {
      return validateActiveStartEndSchedules();
   }
    private boolean validateActiveStartEndSchedules() {
        if(!isSchedulePresent() || !isValid()) {
            printMessage(resources.getString("cmdline.subscribeinvschedule") + "  " + schstr);
            return false;
        }
        boolean startExists = isActiveStartPresent();
        boolean endExists = isActiveEndPresent();

        if(!startExists) {
            printMessage(resources.getString("cmdline.subscribeinvschedule") + "  " + schstr);
            return false;
        }
        if(!isValidStartDate()) {
            printMessage(resources.getString("cmdline.subscribestartbeforecurrent") + "  " + schstr);
            return false;
        }
        if(endExists && !isValidEndDate()) {
            printMessage(resources.getString("cmdline.subscribeendbeforecurrent") + "  " + schstr);
            return false;
        }
        if((startExists && endExists)  && (!isEndAfterStartDate())) {
            printMessage(resources.getString("cmdline.subscribeendafterstart") + "  " + schstr);
            return false;
        }
     return true;
    }
   private boolean validateRecurrenceSchedules() {
       if(!isSchedulePresent() || !isValid()) {
           printMessage(resources.getString("cmdline.subscribeinvschedule") + "  " + schstr);
           return false;
       }
       boolean startExists = isActiveStartPresent();
       boolean endExists = isActiveEndPresent();

       if(startExists && !isValidStartDate()) {
           printMessage(resources.getString("cmdline.subscribestartbeforecurrent") + "  " + schstr);
           return false;
       }
       if(endExists && !isValidEndDate()) {
           printMessage(resources.getString("cmdline.subscribeendbeforecurrent") + "  " + schstr);
           return false;
       }
       if((startExists && endExists)  && (!isEndAfterStartDate())) {
           printMessage(resources.getString("cmdline.subscribeendafterstart") + "  " + schstr);
           return false;
       }
    return true;
   }
    public Schedule getSchedule() {
	return this.sch;
    }
 }
}
