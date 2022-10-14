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
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLIBase;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLICommand;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.webapp.intf.GUIConstants;
import com.marimba.apps.subscriptionmanager.users.CLIUser;
import com.marimba.webapps.intf.SystemException;
import com.marimba.tools.gui.StringResources;
import com.marimba.castanet.schedule.ScheduleInfo;
import com.marimba.castanet.schedule.Schedule;

/**
 *  Command for setting tuner properties.
 *
 * @author      Kumaravel Ayyakkannu
 * @version 	$Revision$, $Date$
 */

public class TunerProps extends SubscriptionCLICommand implements ISubscriptionConstants {

    private Hashtable validTypes;
    protected SubscriptionCLIBase  sb;

    public TunerProps() {
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
	isFailed = tuner(args);
	return isFailed;
    }
    /**
     * Set up tuner props based on cmdline args
     *
     * @param args multiple Farguments in key,type=value format
     *
     * @return REMIND
     *
     * @throws SystemException REMIND
     */
    public boolean tuner(Hashtable args)
        throws SystemException {
        int    i = 0;
        String arg;

        String name = null;
        String type = null;
        boolean status = false;
        boolean isModifyProperty = false;
        boolean removeProperty = false;

        //arg0
	    if("-modify".equals((String) args.get("tuner:args" + i++))) {
	        isModifyProperty = true;
	    } else {
	        // Shifting the index backwards since -modify is not found
	        i--;
	    }

        String arg1 = ((String) args.get("tuner:args" + i++)).toLowerCase();

        try {
            if ("-remove".equals(arg1)) {
                arg1 = ((String) args.get("tuner:args" + i++)).toLowerCase();
                removeProperty = true;
            }
            if ("-dn".equals(arg1)) {
                name = (String) args.get("tuner:args" + i++);
                type = (String) args.get("tuner:args" + i++);

                if ("-type".equals(type)) {
                    type = (String) args.get("tuner:args" + i++);

                    type = type.toLowerCase();

                } else {
                    i--;
                    type = sb.resolveTargetType(name);
                }
            } else {
                // create the props file based on the name and type args
                // first validate the type
                name = arg1.toLowerCase();
		        type = (String) args.get("tuner:args" + i++);

		        type = type.toLowerCase();
		        name = sb.resolveTargetDN(name, type);

                if (DEBUG) {
                    System.out.println("Commandline: the name of the dn passed in = " + name);
                }
            }

            ISubscription sub = com.marimba.apps.subscriptionmanager.ObjectManager.openSubForWrite(name, type, cliUser);

            if (type == null) {
                type = sub.getTargetType();
            }

            if (sub.exists()) {
                if(!isModifyProperty) {
                    // Clear all the properties for Subscription and
                    // all the channels before we set new properties
                    sub.clearProperties(PROP_SERVICE_KEYWORD);
                    sub.clearProperties(PROP_TUNER_KEYWORD);
                    sub.clearProperties(PROP_CHANNEL_KEYWORD);
                    sub.clearProperties(PROP_ALL_CHANNELS_KEYWORD);

                    Enumeration list = sub.getChannels();

                    while (list.hasMoreElements()) {
                        Channel chn = (Channel) list.nextElement();
                        chn.clearProperties();
                    }

                    list = sub.getDummyChannels();

                    while (list.hasMoreElements()) {
                        Channel chn = (Channel) list.nextElement();
                        sub.removeDummyChannel(chn.getUrl());
                    }
                }
            }
            if ( removeProperty ) {
                 while ((arg = (String) args.get("tuner:args" + i++)) != null) {
                    // create properties from the command line args
                    int index = arg.indexOf("=");

                    if (index <= 0) {
                        printMessage(resources.getString("cmdline.Invalidtunerprop") + " " + arg);
                    } else {

                    String key = arg.substring(0, index);
                    String value = arg.substring(index + 1);


                    int ind = key.indexOf(PROP_DELIM);

                    int  ind2 = key.toLowerCase().indexOf(TUNER_PROP_MAR_SCH_FILTER);

                    if(!(ind2 != -1 && value.equalsIgnoreCase(TUNER_PROP_VALUE_NEVER))) {
                        if (ind != -1) {
                            String proptype = key.substring(ind + 1);
                            key = key.substring(0, ind);
                            if (PROP_SERVICE_KEYWORD.equals(proptype) || PROP_CHANNEL_KEYWORD.equals(proptype) || PROP_ALL_CHANNELS_KEYWORD.equals(proptype)) {
                                sub.setProperty(proptype, key, null);
                                printMessage(resources.getString("cmdline.tunerpropdeleted") + " "  + key + ": " + value);
                            } else {
                                String  url = proptype;
                                Channel chn = sub.getChannel(url);

                                if (chn == null) {
                                    chn = sub.getDummyChannel(url);

                                    if (chn != null) {
                                        if (chn.getProperty(key) == null ) {
                                            printMessage(resources.getString("cmdline.tunerpropnotexists") + " "  + key + ": " + value);
                                        } else {
                                            chn.setProperty(key, null);
                                            printMessage(resources.getString("cmdline.tunerpropdeleted") + " "  + key + ": " + value);

                                            if (chn.getPropertyPairs() == null) {
                                                // remove the dummy channel if there is no longer
                                                // any properties associated with it
                                                sub.removeDummyChannel(chn.getUrl());
                                            }
                                        }

                                    } else {
                                        printMessage(resources.getString("cmdline.tunerpropnotexists") + " "  + key + ": " + value);
                                    }
                                    // do nothing, since channel is removed anyway.
                                } else {
                                        if (chn.getProperty(key) == null ) {
                                            printMessage(resources.getString("cmdline.tunerpropnotexists") + " "  + key + ": " + value);
                                        } else {
                                            chn.setProperty(key, null);
                                            printMessage(resources.getString("cmdline.tunerpropdeleted") + " "  + key + ": " + value);
                                        }
                                }
                            }
                        } else {
                            if (sub.getProperty(PROP_TUNER_KEYWORD, key ) == null ) {
                                printMessage(resources.getString("cmdline.tunerpropnotexists") + " "  + key + ": " + value);
                            } else {
                                sub.setProperty(PROP_TUNER_KEYWORD, key, null);
                                printMessage(resources.getString("cmdline.tunerpropdeleted") + " "  + key + ": " + value);
                            }
                        }
                    } else {
                        printMessage(resources.getString("cmdline.Invalidtunerprop") + " " + arg);
                    }
                 }
                 }
           } else {
                while ((arg = (String) args.get("tuner:args" + i++)) != null) {
                    // create properties from the command line args
                    int index = arg.indexOf("=");

                    if (index <= 0)  {
                        printMessage(resources.getString("cmdline.Invalidtunerprop") + " " + arg);
                    } else {

                    String key = arg.substring(0, index);
                    String value = arg.substring(index + 1);

                    printMessage(key + ": " + value);

                    int ind = key.indexOf(PROP_DELIM);

                    int  ind2 = key.toLowerCase().indexOf(TUNER_PROP_MAR_SCH_FILTER);

                    if(!(ind2 != -1 && value.equalsIgnoreCase(TUNER_PROP_VALUE_NEVER))) {
                    if (ind != -1) {
                        String proptype = key.substring(ind + 1);
                        key = key.substring(0, ind);

                        if (PROP_SERVICE_KEYWORD.equals(proptype) || PROP_CHANNEL_KEYWORD.equals(proptype) || PROP_ALL_CHANNELS_KEYWORD.equals(proptype)) {
                            // The property is either for a service, subscribers
                            // or all the channels in the tuner workspace
                            sub.setProperty(proptype, key, value);
                        } else {
                            // The property is set for individual channel by
                            // specifying the channel url
                            Channel chn = sub.getChannel(proptype);

                            if (chn == null) {
                                chn = sub.getDummyChannel(proptype);

                                if ( chn == null  ) {
                                    chn = sub.createDummyChannel(proptype);
                                }

                            }
                            chn.setProperty(key, value);
                        }
                    } else {
                        if(GUIConstants.REBOOT_SCHEDULE_AT.equalsIgnoreCase(key)) {
                            if ((value == null) || "".equals(value)) {
                                printMessage(resources.getString("cmdline.subscribeinvschedule") + " "+ value);
                            }
                            else {
                                ScheduleInfo schinfo = Schedule.getScheduleInfo(value);
                                // If the Schedule object gets set to NEVER that means
                                // the schedule string was invalid unless the scedule
                                // string itself is specified as never
                                if ((schinfo == null) || (!"never".equals(value) && (schinfo.getFlag(ScheduleInfo.CALENDAR_PERIOD)
                                                                                                 .intValue() == ScheduleInfo.NEVER))) {
                                    printMessage(resources.getString("cmdline.subscribeinvschedule") + " " + value);
                                }
                                else {
                                    sub.setProperty(PROP_TUNER_KEYWORD, key, value);
                                }
                            }
                        }
                        else {
                            sub.setProperty(PROP_TUNER_KEYWORD, key, value);
                        }
                    }
                            } else {
                                printMessage(resources.getString("cmdline.Invalidtunerprop") + " " + arg);
                            }
                }
                }
            }
            sub.save();
            status = true;
        } catch (SystemException se) {
            printMessage(resources.getString("cmdline.tunerpropfailed") + " " + type + " " + name);
            throw se;
        } finally {
	    if(sb.getOutputStr().length() > 0) {
	    	printMessage(sb.getOutputStr());
	    }
	}
        if (status){
            logMsg(LOG_AUDIT_TUNER_PROPERTY_CHANGE_SUCCESS, LOG_AUDIT, " TargetType="+ type + " DN="+ name, CMD_SET_TUNER_PROPS);
            printMessage(resources.getString("cmdline.tunerpropsaved") + " TargetType="+ type+ " DN="+ name);
       }  else {
            logMsg(LOG_AUDIT_TUNER_PROPERTY_CHANGE_FAILED, LOG_AUDIT, " TargetType="+ type + " DN="+ name, CMD_SET_TUNER_PROPS);
            printMessage(resources.getString("cmdline.tunerpropfailed") + " TargetType="+ type+ " DN="+ name);
       }

        return false;
    }
}
