// Copyright 1997-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.approval;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscriptionmanager.intf.IPolicyDiffConstants;

/**
 * This class used to generate policy change in UI format
 *
 * @author Selvaraj Jegatheesan
 */
public class PolicyChangeUIGenerator implements ISubscriptionConstants, IApprovalPolicyConstants,IAppConstants,
						IPolicyDiffConstants {
	ApprovalPolicyDTO policyChange;
	List<Hashtable<String, String>> addedChannel;
	List<Hashtable<String, String>> modifiedChannel;
	List<Hashtable<String, String>> deletedChannel;
	List<String> addedProp;
	List<String> modifiedProp;
	List<String> deletedProp;
	SubscriptionMain main;

	public PolicyChangeUIGenerator(ApprovalPolicyDTO policy, SubscriptionMain main) {
		this.main = main;
		this.policyChange = policy;
		addedChannel = new ArrayList<Hashtable<String, String>>();
		modifiedChannel = new ArrayList<Hashtable<String, String>>();
		deletedChannel = new ArrayList<Hashtable<String, String>>();
		addedProp = new ArrayList<String>();
		modifiedProp = new ArrayList<String>();
		deletedProp = new ArrayList<String>();
		preparePolicyChange();
	}
	
	public ApprovalPolicyDTO getPolicyChange() {
		return policyChange;
	}

	public void setPolicyChange(ApprovalPolicyDTO policyChange) {
		this.policyChange = policyChange;
	}

	public List<Hashtable<String, String>> getAddedChannel() {
		return addedChannel;
	}

	public void setAddedChannel(List<Hashtable<String, String>> addedChannel) {
		this.addedChannel = addedChannel;
	}

	public List<Hashtable<String, String>> getModifiedChannel() {
		return modifiedChannel;
	}

	public void setModifiedChannel(List<Hashtable<String, String>> modifiedChannel) {
		this.modifiedChannel = modifiedChannel;
	}

	public List<Hashtable<String, String>> getDeletedChannel() {
		return deletedChannel;
	}

	public void setDeletedChannel(List<Hashtable<String, String>> deletedChannel) {
		this.deletedChannel = deletedChannel;
	}

	public List<String> getAddedProp() {
		return addedProp;
	}

	public void setAddedProp(List<String> addedProp) {
		this.addedProp = addedProp;
	}

	public List<String> getModifiedProp() {
		return modifiedProp;
	}

	public void setModifiedProp(List<String> modifiedProp) {
		this.modifiedProp = modifiedProp;
	}

	public List<String> getDeletedProp() {
		return deletedProp;
	}

	public void setDeletedProp(List<String> deletedProp) {
		this.deletedProp = deletedProp;
	}

	public void preparePolicyChange() {
		genereateChannelInfo();
		generateTunerProplInfo();
	}
	private void setChannels(Hashtable<String, String> channelInfo, int channelAction) {
		if(ADD_OPERATION == channelAction) {
			addedChannel.add(channelInfo);
		} else if (MODIFY_OPERATION == channelAction) {
			modifiedChannel.add(channelInfo);
		} else if(DELETE_OPERATION == channelAction) {
			deletedChannel.add(channelInfo);
		}
	}
	private void setProps(String prop, int propAction) {
		if(ADD_OPERATION == propAction) {
			addedProp.add(prop);
		} else if (MODIFY_OPERATION == propAction) {
			modifiedProp.add(prop);
		} else if(DELETE_OPERATION == propAction) {
			deletedProp.add(prop);
		}
	}
	private void genereateChannelInfo() {
		if(null == policyChange) {
			System.out.println("PolicyChangeUIGenerator : Failed to generate Channel information due to Empty policy change");
			return;
		}
		for(ApprovalChannelDTO channelInfo : policyChange.getPolicyChannels()) {
			genereateChannelContent(channelInfo);
		}
	}
	private void genereateChannelContent(ApprovalChannelDTO  channel) {
		if(null != channel) {
			Hashtable<String, String> channelDetails = new Hashtable<String, String>();
			String channelInfo = null;
			boolean isModified = false;
			
			channelInfo = "<b>URL : </b>" + channel.getM_channelURL()+"<br>";
			channelInfo = channelInfo + "<b>Title : </b>" + channel.getTitle()+"<br>";
			channelInfo = channelInfo + "<b>Type : </b>" + channel.getContentType()+"<br>";
			if(null != channel.getPrimaryStateSchedule()) {
				isModified = true;
				String primaryStateSchedule = (channel.getPrimaryStateSchedule()).replaceAll(STR_NULL, NO_SCHEDULE); 
				channelInfo = channelInfo + "<b>Primary State and Schedule : </b>" + primaryStateSchedule+"<br>";
			}
			if(null != channel.getSecondaryStateSchedule() && !("".equalsIgnoreCase(channel.getSecondaryStateSchedule().trim()))) {
				isModified = true;
				channelInfo = channelInfo + "<b>Secondary State and Schedule : </b>" + channel.getSecondaryStateSchedule()+"<br>";
			}
			if(null != channel.getUpdateSchedule() && !("".equalsIgnoreCase(channel.getUpdateSchedule().trim()))) {
				isModified = true;
				channelInfo = channelInfo + "<b>Update Schedule : </b>" + channel.getUpdateSchedule()+"<br>";
			}
			if(null != channel.getRepairSchedule() && !("".equalsIgnoreCase(channel.getRepairSchedule().trim()))) {
				isModified = true;
				channelInfo = channelInfo + "<b>Repair Schedule : </b>" + channel.getRepairSchedule()+"<br>";
			}
			
			if(channel.getPropertyList().size() > 0) {
				for(ApprovalPropertyDTO props : channel.getPropertyList()) {
					if(CH_POSTPONESCHED_KEY.equalsIgnoreCase(props.getPropKey())) {
						isModified = true;
						channelInfo = channelInfo + "<b>Postpone Schedule : </b>" + props.getPropValue()+"<br>";
					}
				}
			}			
			if(0 != channel.getPriority()) {
				isModified = true;
				channelInfo = channelInfo + "<b>Priority : </b>" + channel.getPriority()+"<br>";
			}
			if(!main.isCloudModel()) {
				if(null != channel.getWowStatus() && !("".equalsIgnoreCase(channel.getWowStatus().trim()))) {
					isModified = true;
					channelInfo = channelInfo + "<b>WOW Deployment : </b>" + channel.getWowStatus()+"<br>";
				}
			}
			if(null != channel.getExemptBlackout()) {
				isModified = true;
				channelInfo = channelInfo + "<b>Exempt Blackout : </b>" + channel.getExemptBlackout()+"<br>";
			}
			channelDetails.put("url", channel.getM_channelURL());
			channelDetails.put("info", channelInfo);
			if(isModified) {
				setChannels(channelDetails, channel.getChannelAction());
			}
			if(channel.getPropertyList().size() > 0) {
				boolean isNotRequired = false; 
				for(ApprovalPropertyDTO chProps : channel.getPropertyList()) {
					// Use case : channel 1 has some properties and when user delete the channel alone 
					// then remaining properties consider as dummy channel but not required to show in View Details page
					if(ADD_OPERATION == channel.getChannelAction() && ADD_OPERATION == chProps.getPropAction() && !isNotRequired) {
						if(isDummyChannel(channel.getM_channelURL())) {
							isNotRequired = true;
						}
					}
					if(!isNotRequired) {
						// Postpone schedule information show with channel tool-tip not show in property  
						if(!CH_POSTPONESCHED_KEY.equalsIgnoreCase(chProps.getPropKey())) {
							String channelProp = chProps.getPropKey() +"," + channel.getM_channelURL() + "=" + chProps.getPropValue();
							setProps(channelProp, chProps.getPropAction());
						}
					}

				}
			}
		}
	}
	// This method used to verify given URL is dummy channel or not from Database 
	private boolean isDummyChannel(String channelURL) {
		if(null == policyChange) {
			return false;
		}
		for(ApprovalChannelDTO channelInfo : policyChange.getPolicyChannels()) {
			if((channelInfo.getM_channelURL().equalsIgnoreCase(channelURL) && DELETE_OPERATION == channelInfo.getChannelAction())) {
				return true;
			}
		}
		return false;
	}
	private void generateTunerProplInfo() {
		if(null == policyChange) {
			System.out.println("PolicyChangeUIGenerator : Failed to generate Tuner Property information due to Empty policy change");
			return;
		}
		for(ApprovalPropertyDTO tunerInfo : policyChange.getTunerProps()) {
			String propInfo = null;
			propInfo = tunerInfo.getPropKey();
			if(!PROPERTY_TYPE_TUNER.equalsIgnoreCase(tunerInfo.getPropType())) {
				propInfo = propInfo + "," + tunerInfo.getPropType();
			}
			propInfo = propInfo + "=" + tunerInfo.getPropValue();
			setProps(propInfo, tunerInfo.getPropAction());
		}	
	}
	
}
