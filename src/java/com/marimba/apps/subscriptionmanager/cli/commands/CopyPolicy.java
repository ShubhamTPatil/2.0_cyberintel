// Copyright 1997-2013, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.cli.commands;

import java.util.Enumeration;
import java.util.Hashtable;

import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLIBase;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLICommand;
import com.marimba.apps.subscriptionmanager.cli.commands.intf.ISubscribe;
import com.marimba.apps.subscriptionmanager.users.CLIUser;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.intf.objects.dao.ISubDataSource;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.intf.msf.acl.IAclConstants;
import com.marimba.webapps.intf.SystemException;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Subscription;

/**
 *  Command for Copying source target policy to destination target
 *
 * @author      Selvaraj Jegatheesan
 * @version 	$Revision$, $Date$
 */

public class CopyPolicy extends SubscriptionCLICommand implements ISubscribe, ISubscriptionConstants {
    private Hashtable validTypes;
    private SubscriptionCLIBase sb;
    public CopyPolicy() {
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
 	   super.setSubscriptionMain(subsMain);
 	   sb.setSubscriptionMain(subsMain);
     }
     public void setCLIUser(CLIUser cliUser) {
     	super.setCLIUser(cliUser);
     	sb.setCLIUser(cliUser);
     }
     public boolean run(Hashtable args) throws SystemException {
    	 boolean isFailed = false;
    	 isFailed = copyPolicy(args);
    	 return isFailed;
     }
     /**
      * Copy policy from source target to destination target
      *
      * @param cmds 'DN' of Source target and 'DN' of destination target
      *
      * @return boolean success or not
      *
      * @throws SystemException REMIND
      */
     private boolean copyPolicy(Hashtable cmds)
    	        throws SystemException {
		String sourceTargetDN = null;
		String sourcrTargetType = null;
		String destTargetDN = null;
		String destTargetType = null;
		ISubscription sourceSub, destSub = null;
		boolean isFailed = false;

		try {
			sourceTargetDN = (String) cmds.get("copypolicy:args1");
			destTargetDN = (String) cmds.get("copypolicy:args3");
			if (null != sourceTargetDN) {
				sourcrTargetType = sb.resolveTargetType(sourceTargetDN);
				if(null != sourcrTargetType) {
					sourceSub = getPolicy(sourceTargetDN, sourcrTargetType);
					if (null != sourceSub && null != destTargetDN.trim()) {
						destTargetType = sb.resolveTargetType(destTargetDN);
						if(null != destTargetType) {

							// delete existing policy for the destination policy if already exists 
							deleteExistingPolicy(destTargetDN, destTargetType);
							
							ISubDataSource src = ObjectManager.getSubDataSource(cliUser);
							destSub = new Subscription(destTargetDN, destTargetType, src);
							
							// Copy Channels
                            for (Enumeration channelEnum = (Enumeration) sourceSub.getChannels(); channelEnum.hasMoreElements();) {
                                Channel selectedChn = (Channel) channelEnum.nextElement();
                                destSub.setChannel(selectedChn);
                            }
                            
                            // Copy Dummy Channels
                            for (Enumeration dummyChannelEnum = (Enumeration) sourceSub.getDummyChannels(); dummyChannelEnum.hasMoreElements();) {
                                Channel selectedChn = (Channel) dummyChannelEnum.nextElement();
                                destSub.setDummyChannel(selectedChn);
                            }
                            
                            // Copy tuner properties
                            setTunerProperties(sourceSub, destSub);
                            
                            // finally save the subscription
                            destSub.save();
						} else {
							isFailed = true;
						}
					} else {
						isFailed = true;
					}
				} else {
					isFailed = true;
				}
			}
			
		} catch (Exception se) {
			printMessage(resources.getString("cmdline.copypolicyfailed"));
			if(DEBUG5) {
				se.printStackTrace();
			}
			isFailed = true;
			return isFailed;
		} finally {
			if (sb.getOutputStr().length() > 0) {
				printMessage(sb.getOutputStr());
			}
		}
		if(isFailed) {
			printMessage(resources.getString("cmdline.copypolicyfailed"));
		} else {
			printMessage(resources.getString("cmdline.copypolicy"));
		}

		return isFailed;
	}
    
     /**
      *  Get policy for the given targetID and target type
      * @param targetID
      * @param targetType
      * @return
      * @throws SystemException
      */
    private ISubscription getPolicy(String targetID, String targetType) throws SystemException {
    	ISubscription sub = null;
    	try {
    		boolean isPolicyExists = com.marimba.apps.subscriptionmanager.ObjectManager
					.existsSubscription(targetID, targetType, cliUser);
			if (isPolicyExists) {
				sub = com.marimba.apps.subscriptionmanager.ObjectManager.getSubscription(targetID, targetType, cliUser);
			} 

    	 } catch(SystemException se) {
    		 throw se;
    	 }
    	 return sub;
     }
     /**
      * Delete existing policy for copy operation if already exists 
      * @param targetID
      * @param targetType
      * @throws SystemException
      */
     private void deleteExistingPolicy(String targetID, String targetType) throws SystemException {
    	 try {
    		 boolean isPolicyExists = com.marimba.apps.subscriptionmanager.ObjectManager
 					.existsSubscription(targetID, targetType, cliUser);
 			if (isPolicyExists) {
 				com.marimba.apps.subscriptionmanager.ObjectManager.deleteSubscription(targetID, targetType, cliUser);
 			}  
    	 } catch(SystemException se) {
 				throw se;
 			}
     }
     /**
      * Set tuner properties for destination target 
      * @param oldsub
      * @param newsub
      */
     private void setTunerProperties(ISubscription oldsub, ISubscription newsub) {
    	 try {
	    	 copyTunerProperties(oldsub, newsub, PROP_TUNER_KEYWORD);
	         copyTunerProperties(oldsub, newsub, PROP_SERVICE_KEYWORD);
	         copyTunerProperties(oldsub, newsub, PROP_CHANNEL_KEYWORD);
	         copyTunerProperties(oldsub, newsub, PROP_ALL_CHANNELS_KEYWORD);
	         copyTunerProperties(oldsub, newsub, PROP_DEVICES_KEYWORD);
	         copyTunerProperties(oldsub, newsub, PROP_POWER_KEYWORD);
             copyTunerProperties(oldsub, newsub, PROP_SECURITY_KEYWORD);
             copyTunerProperties(oldsub, newsub, PROP_SCAP_SECURITY_KEYWORD);
             copyTunerProperties(oldsub, newsub, PROP_USGCB_SECURITY_KEYWORD);
             copyTunerProperties(oldsub, newsub, PROP_CUSTOM_SECURITY_KEYWORD);
	         copyTunerProperties(oldsub, newsub, PROP_AMT_KEYWORD);
	         copyTunerProperties(oldsub, newsub, PRO_AMT_ALARMCLK_KEYWORD);
	         copyTunerProperties(oldsub, newsub, PROP_OSM_KEYWORD);
	         copyTunerProperties(oldsub, newsub, PROP_PBACKUP_KEYWORD);
    	 } catch(Exception ec) {
    		 if(DEBUG5) {
    			 ec.printStackTrace();
    		 }
    	 }
     }
     /**
      * Copy tuner properties from source to target
      * @param oldsub
      * @param newsub
      * @param type
      * @throws SystemException
      */
     private void copyTunerProperties(ISubscription oldsub, ISubscription newsub, String type)
             throws SystemException {
         String[] pairs = oldsub.getPropertyPairs(type);

         for (int i = 0; i < pairs.length; i += 2) {
             newsub.setBlackOut(oldsub.getBlackOut());
             newsub.setProperty(type, pairs [i], pairs [i + 1]);
         }
     }
}
