// Copyright 1997-2006, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.cli.commands;

import java.text.DateFormat;
import java.util.*;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.SubKnownException;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLIBase;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLICommand;
import com.marimba.apps.subscriptionmanager.cli.commands.intf.ISubscribe;
import com.marimba.apps.subscriptionmanager.users.CLIUser;
import com.marimba.apps.subscriptionmanager.util.Utils;
import com.marimba.castanet.schedule.Schedule;
import com.marimba.tools.gui.StringResources;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.tools.ldap.LDAPException;
import com.marimba.tools.ldap.LDAPSearchFilter;
import com.marimba.tools.ldap.LDAPPagedSearch;
import com.marimba.webapps.intf.SystemException;
import com.marimba.intf.util.IProperty;

 /**
 * Set the plugin parameters for publishing the plugin.
 *
 * @author	 Kumaravel Ayyakkannu
 * @version 	$Revision$, $Date$
 */

public class ListPolicy extends SubscriptionCLICommand implements ISubscribe,ISubscriptionConstants {

    Hashtable validTypes;
    Hashtable displayStateMapping;
    SubscriptionCLIBase  sb;

    public ListPolicy() {
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

	// Subscription state to  Display state mapping
	displayStateMapping = new Hashtable();
	displayStateMapping.put("available", "advertise");
	displayStateMapping.put("subscribe_noinstall", "stage");
	displayStateMapping.put("subscribe", "install");
	displayStateMapping.put("subscribe_start", "install-start");
	displayStateMapping.put("start_persist", "install-start-persist");
	displayStateMapping.put("subscribe_persist", "install-persist");
	displayStateMapping.put("delete", "uninstall");
	displayStateMapping.put("exclude", "exclude");
	displayStateMapping.put("primary", "primary");
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
	isFailed = list(args);
	return isFailed;
    }
	  /**
     * List the listed internal subscription files (or all if noarg)
     *
     * @param cmds Names of the Subscription Policies or -all or -cascade
     *
     * @return REMIND
     *
     * @throws SystemException REMIND
     */
    private boolean list(Hashtable cmds)
        throws SystemException {
        printMessage("");
        printMessage(resources.getString("cmdline.listingsubscriptions"));
        printMessage("");

        String arg = (String) cmds.get("list:args");

        try {
            if ("list:noarg".equals(arg)) {
                // list all .sub  files in the current namespace
                String[] sublist = com.marimba.apps.subscriptionmanager.ObjectManager.listSubscriptionByName(subsMain, cliUser);

                if (sublist != null) {
                    for (int i = 0; i < sublist.length; i++) {
                        if (listSubByName(sublist [i])) {
                            return true;
                        }
                    }
                }
            } else if ("-dn".equals(cmds.get("list:args0"))) {
                String dn = (String) cmds.get("list:args1");
                String type = (String) cmds.get("list:args2");

                if ("-type".equals(type)) {
                    type = (String) cmds.get("list:args3");
                    type = type.toLowerCase();
                } else {
                    type = sb.resolveTargetType(dn);
                }
                if (listSub(dn, type)) {
                    return true;
                }
            } else if("-channel".equals(cmds.get("list:args0"))) {
                String channelURL = (String) cmds.get("list:args1");

                if (listTargets(channelURL)) {
                    return true;
                }
            } else if ("-cascade".equals(cmds.get("list:args0"))) {
                // list all skubscriptions in the LDAP server
                // list the default subscription location first
            	sb.setNameSpace(null);

                String[] sublist = com.marimba.apps.subscriptionmanager.ObjectManager.listSubscriptionByName(subsMain, cliUser);

                if (sublist != null) {
                    for (int i = 0; i < sublist.length; i++) {
                        if (listSubByName(sublist [i])) {
                            return true;
                        }
                    }
                }

                // list all subscriptions in all namespaces
                try {
                    ArrayList nsList = subsMain.listNameSpaces(cliUser.getSubConn());
                    String    namespace = null;

                    for (int i = 0; i < nsList.size(); i++) {
                        namespace = (String) nsList.get(i);
                        sb.setNameSpace(namespace);
                        printMessage(resources.getString("cmdline.list.namespace") + " " + namespace);

                        String[] sublistn = com.marimba.apps.subscriptionmanager.ObjectManager.listSubscriptionByName(subsMain, cliUser);

                        if (sublistn != null) {
                            for (int j = 0; j < sublistn.length; j++) {
                                if (listSubByName(sublistn [j])) {
                                    return true;
                                }
                            }
                        }
                    }

                    sb.setNameSpace(null);
                } catch (NamingException le) {
                    LDAPUtils.classifyLDAPException(le);
                }
            } else {
                if (((String) cmds.get("list:args0")).endsWith(SUBSCRIPTION_EXT)) {
                    // process list
                    int    i = 0;
                    String subname;

                    while ((subname = (String) cmds.get("list:args" + i++)) != null) {
                        int ind = subname.lastIndexOf(".");

                        if (ind != -1) {
                            subname = subname.substring(0, ind);
                        }

                        if (listSubByName(subname)) {
                            return true;
                        }
                    }
                } else {
                    String fname = (String) cmds.get("list:args0");
                    String type = (String) cmds.get("list:args1");
                    type = type.toLowerCase();
		    
                    fname = sb.resolveTargetDN(fname, type);
                    listSub(fname, type);
                }
            }
        } catch (SystemException se) {
        	printMessage(sb.getMessage());
            printMessage(resources.getString("cmdline.listfailed"));
            throw se;
        } finally {
	    if(sb.getOutputStr().length() > 0) {
	    	printMessage(sb.getOutputStr());
	    }
	}

        return false;
    }
    /* List specified subscription policy
    *
    * Used when target DN and type resolution should be avoided in the loading of
    * the object.  For example, to delete or list a subscription when the target
    * has been removed from the storage.
    *
    * @param subname  Subscription Policy name to be listed
    */
   private boolean listSubByName(String subname)
       throws SystemException {
       ISubscription sub = com.marimba.apps.subscriptionmanager.ObjectManager.openSubForReadNoError(subname, cliUser);

       if (sub != null) {
           return listSub(sub);
       } else {
           printMessage(resources.getString("cmdline.subscribelistbynameskip") + " " + subname);
       }

       return false;
   }

   /* List specified subscription policy
    *
    * @param targetID  Target ID
    * @param type      Subscription Policy name to be listed
    */
   private boolean listSub(String targetID,
                           String type)
       throws SystemException {
       return listSub(com.marimba.apps.subscriptionmanager.ObjectManager.openSubForRead(targetID, type, cliUser));
   }
   private boolean listSub(ISubscription sub)
       throws SystemException {
       String str = resources.getString("cmdline.targetdn") + " " + sub.getTargetID();
       printMessage(str);

       String lastModified = resources.getString("cmdline.lastmodified") + " " + filterValues(getLastModified(sub));
       printMessage(lastModified);

       str = resources.getString("cmdline.type") + " " + filterValues(sub.getTargetType());
       printMessage(str);

       // Print blackout schedule
       printMessage(resources.getString("cmdline.list.blackoutsched") + filterValues(sub.getBlackOut()));

       // Print Service Exempt from blackout value
       printMessage(resources.getString("cmdline.list.serviceexemptblackout") + sub.isServiceExemptFromBlackout());

       Enumeration list = sub.getChannels();
       boolean     printstr = true;
       Vector      chprops = new Vector();

       while (list.hasMoreElements()) {
           if (printstr) {
               printMessage(resources.getString("cmdline.list.channels"));
               printstr = false;
           }

           Channel chn = (Channel) list.nextElement();

           // Print Channel Title
           printMessage(resources.getString("cmdline.list.channeltitle") + filterValues(chn.getTitle()));

           // Print the URL
           printMessage(resources.getString("cmdline.list.channelurl") + chn.getUrl());

           // Print the Channel State
           String  state1 = getDisplayState(chn.getState());
           printMessage(resources.getString("cmdline.list.channelstate") + state1);

           // Print the Channel Secondary state
           String  state2 = chn.getSecState();
           if ((state2 != null) && !"".equals(state2)) {
               state2 = getDisplayState(state2);
           }else {
               state2 = NOTAVBLE;
           }
           printMessage(resources.getString("cmdline.list.channelsecstate") + state2);

           Schedule sch = null;

           // Print primary schedule
           String primarySched = ((sch = chn.getInitSchedule()) == null) ? NOTAVBLE : sch.toString();
           printMessage(resources.getString("cmdline.list.primarysched") + primarySched);

           // Print secondary schedule
           String secSched = ((sch = chn.getSecSchedule()) == null) ? NOTAVBLE : sch.toString();
           printMessage(resources.getString("cmdline.list.secondarysched") + secSched);

           // Print update schedule
           String updateSched = ((sch = chn.getUpdateSchedule()) == null ? NOTAVBLE : sch.toString());
           printMessage(resources.getString("cmdline.list.updatesched") + updateSched);

           // Print verify/repair schedule
           String verifyRepSched = ((sch = chn.getVerRepairSchedule()) == null ? NOTAVBLE : sch.toString());
           printMessage(resources.getString("cmdline.list.verrepairsched") + verifyRepSched);

           // Print Channel priority
           int order = chn.getOrder();
           String channelPriority;
           if (order < ORDER) {
               channelPriority = String.valueOf(order);
           } else {
               channelPriority = NOTAVBLE;
           }
           printMessage(resources.getString("cmdline.list.priority") + channelPriority);

           // Print Exempt from blackout value
           printMessage(resources.getString("cmdline.list.exemptFromBlackout") + chn.isExemptFromBlackout());

           // Store Channel properties in a Vector
           printMessage(" ");

           String[] pairs = chn.getPropertyPairs();

           for (int i = 0; i < pairs.length; i += 2) {
               chprops.addElement("    " + pairs [i] + "," + chn.getUrl() + "=" + pairs [i + 1]);
           }
       }

       // Add any properties for channels that doesn't exist
       list = sub.getDummyChannels();

       while (list.hasMoreElements()) {
           Channel  chn = (Channel) list.nextElement();
           String[] pairs = chn.getPropertyPairs();

           for (int i = 0; i < pairs.length; i += 2) {
               chprops.addElement("    " + pairs [i] + "," + chn.getUrl() + "=" + pairs [i + 1]);
           }
       }

       // Print Properties
       String[] tpairs = sub.getPropertyPairs(PROP_TUNER_KEYWORD);
       String[] spairs = sub.getPropertyPairs(PROP_SERVICE_KEYWORD);
       String[] cpairs = sub.getPropertyPairs(PROP_CHANNEL_KEYWORD);
       String[] apairs = sub.getPropertyPairs(PROP_ALL_CHANNELS_KEYWORD);

       if ((tpairs.length > 0) || (spairs.length > 0) || (cpairs.length > 0) || (apairs.length > 0) || (chprops.size() > 0)) {
           printMessage(resources.getString("cmdline.list.properties"));
       }

       // Print individual channel properties
       for (int i = 0; i < chprops.size(); i++) {
           printMessage((String) chprops.elementAt(i));
       }

       // Print tuner properties
       if (tpairs.length > 0) {
           writeProps(tpairs, PROP_TUNER_KEYWORD);
       }

       // Print subscription service properties
       if (spairs.length > 0) {
           writeProps(spairs, PROP_SERVICE_KEYWORD);
       }

       // Print properties for subscribed channels
       if (cpairs.length > 0) {
           writeProps(cpairs, PROP_CHANNEL_KEYWORD);
       }

       // Print properties for all the channels in the tuner workspace
       if (apairs.length > 0) {
           writeProps(apairs, PROP_ALL_CHANNELS_KEYWORD);
       }

       printMessage("");

       return false;
   }

   /* This method will print the list of assigned targets for input package URL in the command line.
    *
    * @param channelURL - package URL       
    */
     private boolean listTargets(String channelURL) {
         Channel ch = new Channel(channelURL, "");
         ArrayList channellist = new ArrayList();
         channellist.add(ch);

         String pagingType = null;
         Vector results = null;
         try {
             pagingType = LDAPPagedSearch.getType(cliUser.getBrowseConn());
             results = LDAPWebappUtils.searchTargetaForPkg(channellist, subsMain.getSubBase(),
                     subsMain.getSubBaseWithNamespace(cliUser), resources.getString("cmdline.all_endpoints"),
                     pagingType, cliUser, Utils.isPrimaryAdmin(cliUser.getUser()), subsMain);
         } catch (Exception e) {
             e.printStackTrace();
         }

         if(results.size() == 0) {
             printMessage(resources.getString("cmdline.list.nopolicy") + " " + channelURL);
         } else {
             printMessage(resources.getString("cmdline.list.policies") + " " + channelURL);
             for(int i=0;i<results.size();i++){
                 String targetId = ((TargetChannelMap)results.get(i)).getId();
                 if("all".equals(targetId)) {
                     targetId = resources.getString("cmdline.all_endpoints");
                 }
                 printMessage(targetId);
             }
         }
         return true;
     }
   /**
    * If the value is null or the length is 0
    * then N/A is returned.
    *
    * @param value Value to be filtered
    * @return N/A if the value is null or the length is 0
    */
   private String filterValues(String value) {
       String filteredValue = value;
       if(value == null || value.trim().length() == 0) {
           filteredValue = NOTAVBLE;
       }
       return filteredValue;
   }
   /**
    * Display the properties strings
    *
    * @param pairs key value pairs of property strings
    * @param typeStr type of the property
    */
   private void writeProps(String[] pairs,
                           String   typeStr) {
       for (int i = 0; i < pairs.length; i += 2) {
           if ("".equals(typeStr)) {
               printMessage("    " + pairs [i] + "=" + pairs [i + 1]);
           } else {
               printMessage("    " + pairs [i] + "," + typeStr + "=" + pairs [i + 1]);
           }
       }
   }
   /**
    * Get the last modified value and format for displaying in the console
    *
    * @param sub Subscription object instance
    * @return Formatted last modified value
    */
   private String getLastModified(ISubscription sub) {
       DateFormat formatter = DateFormat.getDateTimeInstance();
       Date lastModified = new Date(sub.getLastModifiedTime());
       return formatter.format(lastModified);
   }
   /* Get Display state from the Subscription state
    * @param  substate      subscription state
    * @return displaystate  display state
    */
   private String getDisplayState(String subState) {
       String displayState = (String) displayStateMapping.get(subState);

       // No mapping found , must be an invalid state
       if (displayState == null) {
           return null;
       }

       return displayState;
   }
}
