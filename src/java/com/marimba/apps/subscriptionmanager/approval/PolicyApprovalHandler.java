// Copyright 1997-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.approval;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscriptionmanager.intf.IPolicyDiffConstants;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.policydiff.ChannelDiffer;
import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiff;
import com.marimba.apps.subscriptionmanager.util.Utils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is used to handle all policy in DB format 
 * for peer approval transaction
 *
 * @author Selvaraj Jegatheesan
 */

public class PolicyApprovalHandler implements IApprovalPolicyConstants,
		IAppConstants, ISubscriptionConstants, IPolicyDiffConstants {
	
	private List<PolicyDiff> policyDiffs;
	private String ldapSource;
	private String changeOwner = null;
	private Timestamp created_on;
	private int policyAction = ADD_OPERATION;
	private int policyStatus = POLICY_PENDING;
	private List<ApprovalPolicyDTO> approvalPolicy;
    	
	public PolicyApprovalHandler() {
		init();
	}
	
	public void init() {
		policyDiffs = new ArrayList<PolicyDiff>();
		approvalPolicy = new ArrayList<ApprovalPolicyDTO>();
	}

	public List<PolicyDiff> getPolicyDiffs() {
		return policyDiffs;
	}

	public void setPolicyDiffs(List<PolicyDiff> policyDiffs) {
		this.policyDiffs = policyDiffs;
	}


	public String getLdapSource() {
		return ldapSource;
	}

	public void setLdapSource(String ldapSource) {
		this.ldapSource = ldapSource;
	}

	public String getChangeOwner() {
		return changeOwner;
	}

	public void setChangeOwner(String changeOwner) {
		this.changeOwner = changeOwner;
	}

	public Timestamp getCreated_on() {
		return created_on;
	}

	public List<ApprovalPolicyDTO> getApprovalPolicy() {
		return approvalPolicy;
	}

	public void setApprovalPolicy(List<ApprovalPolicyDTO> approvalPolicy) {
		this.approvalPolicy = approvalPolicy;
	}

	public void setCreated_on() {
		Timestamp created_on = null;
		try {
			created_on = Utils.getCurrentTimeStamp();
		} catch(Exception e) {
			System.out.println("Failed to set current time stamp for approval policy");
			e.printStackTrace();
		}
		this.created_on = created_on;
	}
	
	public int getPolicyAction() {
		return policyAction;
	}

	public void setPolicyAction(int policyAction) {
		this.policyAction = policyAction;
	}

	public int getPolicyStatus() {
		return policyStatus;
	}

	public void setPolicyStatus(int policyStatus) {
		this.policyStatus = policyStatus;
	}

	/**
	 * Used to all policy changes to DB format
	 * @param policyDiffs
	 * @throws ApprovalPolicyException
	 */
	public void policyDiff2DBFormat(List<PolicyDiff> policyDiffs) throws ApprovalPolicyException {
		for(PolicyDiff policyChangeDiff : policyDiffs) {
			try {
				debug("Convert policy change to Database format : " + policyChangeDiff.getTargetId());
				ApprovalPolicyDTO policyChange = new ApprovalPolicyDTO();
				
				
				policyChange.setPolicyTargetName(policyChangeDiff.getTargetName());
				policyChange.setPolicyTargetType(policyChangeDiff.getTargetType());
				policyChange.setPolicyTargetId(policyChangeDiff.getTargetId());
				policyChange.setPolicyAction(policyChangeDiff.getPolicyAction());
				policyChange.setPolicyStatus(policyChangeDiff.getPolicyStatus());
				policyChange.setBlackoutChanged(policyChangeDiff.isBlackoutChanged());
				if(policyChangeDiff.isBlackoutChanged()) {
					policyChange.setBlackoutSchedule(policyChangeDiff.getBlackoutSchedule());
				}
				policyChange.setChangeOwner(policyChangeDiff.getUser());
				policyChange.setCreated_on(created_on);
				policyChange.setLdapSource(ldapSource);
				
				// List of channels assigned to target
				List<ApprovalChannelDTO> targetChannels = new ArrayList<ApprovalChannelDTO>();
				
				List<ApprovalChannelDTO> addedChannels = getChannelDBFormat(policyChangeDiff.getAddedChannelsMap(), 
						ADD_OPERATION);
				
				if(addedChannels.size() > 0) {
					debug("Added channel size is : " + addedChannels.size());
					targetChannels.addAll(addedChannels);
				}
				List<ApprovalChannelDTO> modifyChannels = getModifyChannelDBFormat(policyChangeDiff.getModifiedChannelPropsMap(), 
				 		MODIFY_OPERATION);
				
				if(modifyChannels.size() > 0) {
					debug("Modified channel size is : " + modifyChannels.size());
					targetChannels.addAll(modifyChannels);
				}
				
				List<ApprovalChannelDTO> deleteChannels = getChannelDBFormat(policyChangeDiff.getDeletedChannelsMap(), 
						DELETE_OPERATION);
				
				if(deleteChannels.size() > 0) {
					debug("Deleted channel size is : " + deleteChannels.size());
					targetChannels.addAll(deleteChannels);
				}
				// Store Dummy Channels
				List<ApprovalChannelDTO> addedDummyChannels = getDummyChannelDBFormat(policyChangeDiff.getAddedDummyChannelsMap(), 
						ADD_OPERATION);
				if(addedDummyChannels.size() > 0) {
					debug("Added Dummy channel size is : " + addedDummyChannels.size());
					targetChannels.addAll(addedDummyChannels);
				}
				List<ApprovalChannelDTO> modifiedDummyChannels = getModifyDummyChannelDBFormat(policyChangeDiff.getModifiedDummyChannelsMap(), 
						MODIFY_OPERATION);
				
				if(modifiedDummyChannels.size() > 0) {
					debug("Modified Dummy channel size is : " + modifiedDummyChannels.size());
					targetChannels.addAll(modifiedDummyChannels);
				}

				List<ApprovalChannelDTO> deletedDummyChannels = getDummyChannelDBFormat(policyChangeDiff.getDeletedDummyChannelsMap(), 
						DELETE_OPERATION);
				
				if(deletedDummyChannels.size() > 0) {
					debug("Deleted Dummy channel size is : " + deletedDummyChannels.size());
					targetChannels.addAll(deletedDummyChannels);
				}

				// Store all channels to target Channels
				if(targetChannels.size() > 0) {
					policyChange.setPolicyChannels(targetChannels);
					
				}
				
				List<ApprovalPropertyDTO> tunerPorpsList = new ArrayList<ApprovalPropertyDTO>();
				
				// List of properties assigned to target
				List<ApprovalPropertyDTO> addedTunerProps = getTunerPropsDBFormat(policyChangeDiff.getAddedPropsMap(), 
						ADD_OPERATION);
				if(addedTunerProps.size() > 0) {
					debug("Added Tuner Property size is : " + addedTunerProps.size());
					policyChange.setAddTunerProps(addedTunerProps);
					tunerPorpsList.addAll(addedTunerProps);
				}
				List<ApprovalPropertyDTO> modifyTunerProps = getModifyTunerPropsDBFormat(policyChangeDiff.getModifiedPropsMap(), 
						MODIFY_OPERATION);
				if(modifyTunerProps.size() > 0) {
					debug("Modified Tuner Property size is : " + modifyTunerProps.size());
					policyChange.setModifyTunerProps(modifyTunerProps);
					tunerPorpsList.addAll(modifyTunerProps);
				}
				List<ApprovalPropertyDTO> deleteTunerProps = getTunerPropsDBFormat(policyChangeDiff.getDeletedPropsMap(), 
						DELETE_OPERATION);
				if(deleteTunerProps.size() > 0) {
					debug("Deleted Tuner Property size is : " + deleteTunerProps.size());
					policyChange.setDeleteTunerProps(deleteTunerProps);
					tunerPorpsList.addAll(deleteTunerProps);
				}
				// Store all tuner props to Policy Change tuner props
				if(tunerPorpsList.size() > 0) {
					policyChange.setTunerProps(tunerPorpsList);
					
				}
				
				// All policy changes send to approval storage
				approvalPolicy.add(policyChange);
			} catch(ApprovalPolicyException e) {
				System.out.println("Failed to convert policy change to Database format :" + policyChangeDiff.getTargetId());
				e.printStackTrace();
			}
		}
		if(approvalPolicy.size() > 0) {
			setApprovalPolicy(approvalPolicy);
		}
	}
	/**
	 * Convert all Channels diff to DB format
	 * @param channelList
	 * @param policyChannelAction
	 * @return
	 * @throws ApprovalPolicyException
	 */
	public List<ApprovalChannelDTO> getChannelDBFormat(Map<String, ChannelDiffer> channelList, int policyChannelAction) throws ApprovalPolicyException {
		List<ApprovalChannelDTO> channels = new ArrayList<ApprovalChannelDTO>();
		
		Map<String, ChannelDiffer> ChannelMap = channelList;
		for (Map.Entry<String, ChannelDiffer> channelEntry : ChannelMap.entrySet()) {
		    ChannelDiffer chDiffer = channelEntry.getValue();
			ApprovalChannelDTO channel = prepareChannelDTO(chDiffer.getChannel(), policyChannelAction, chDiffer.isRemovedChAlone());
			channels.add(channel);
		}
		return channels;
	}
	public List<ApprovalChannelDTO> getModifyChannelDBFormat(Map<String, ChannelDiffer> modifyChList, int channelAction) throws ApprovalPolicyException {
		List<ApprovalChannelDTO> channels = new ArrayList<ApprovalChannelDTO>();
		
		Map<String, ChannelDiffer> ChannelMap = modifyChList;
		for (Map.Entry<String, ChannelDiffer> channelEntry : ChannelMap.entrySet()) {
		    System.out.println("Key = " + channelEntry.getKey() + ", Value = " + channelEntry.getValue());
		    String channelURL = channelEntry.getKey();
		    ChannelDiffer chDiffer = channelEntry.getValue();
			ApprovalChannelDTO channel = prepareModifyChannelDTO(channelURL, chDiffer);
			channels.add(channel);
		}
		return channels;
		
	}
	public List<ApprovalChannelDTO> getDummyChannelDBFormat(Map<String, Map<String, String>> channelList, int channelAction) throws ApprovalPolicyException {
		List<ApprovalChannelDTO> channels = new ArrayList<ApprovalChannelDTO>();
		
		for (Map.Entry<String, Map<String, String>> channelEntry : channelList.entrySet()) {
			ApprovalChannelDTO dummyCh = new ApprovalChannelDTO();
			
			List<ApprovalPropertyDTO> propsList = prepareChannelPropDTO(channelEntry.getValue(), channelAction);

			if(propsList.size() > 0) {
			    dummyCh.setM_channelURL(channelEntry.getKey());
			    dummyCh.setChannelAction(channelAction);
				dummyCh.setPropertyList(propsList);
				channels.add(dummyCh);
			}
		}
		return channels;
	}
	public List<ApprovalChannelDTO> getModifyDummyChannelDBFormat(Map<String, Map<String, Map<String, String>>> modifyChList, int channelAction) throws ApprovalPolicyException {
		List<ApprovalChannelDTO> channels = new ArrayList<ApprovalChannelDTO>();
		
		for (Map.Entry<String, Map<String, Map<String, String>>> channelEntry : modifyChList.entrySet()) {
		    System.out.println("Key = " + channelEntry.getKey() + ", Value = " + channelEntry.getValue());
		    ApprovalChannelDTO dummyCh = new ApprovalChannelDTO();
		    
		    String channelURL = channelEntry.getKey();
		    
		    List<ApprovalPropertyDTO> propsList = prepareModifyChPropDTO(channelEntry.getValue(), channelAction);

		    if(propsList.size() > 0) {
			    dummyCh.setM_channelURL(channelURL);
			    dummyCh.setChannelAction(channelAction);
				dummyCh.setPropertyList(propsList);
				channels.add(dummyCh);
			}
		}
		return channels;
	}
	/**
	 * Preapre all channels diff to DB format
	 * @param ch
	 * @param channelAction
	 * @return
	 * @throws ApprovalPolicyException
	 */
	private ApprovalChannelDTO prepareChannelDTO(Channel ch, int channelAction, boolean isRemovedChAlone) throws ApprovalPolicyException {
	    
	    String primaryState_Schedule = encodeStateScheduleString(ch.getState(), ch.getInitScheduleString(), "primary");
	    String secState_Schedule = encodeStateScheduleString(ch.getSecState(), ch.getSecScheduleString(), "secondary");
	    String wowStatus = encodeWOWString(ch.isWowEnabled(), ch.getWOWForInit(), ch.getWOWForSec(), 
	    		ch.getWOWForUpdate(),ch.getWOWForRepair());

		ApprovalChannelDTO channelDTO = new ApprovalChannelDTO();
		channelDTO.setPrimaryStateSchedule(primaryState_Schedule);
		channelDTO.setSecondaryStateSchedule(secState_Schedule);
		channelDTO.setUpdateSchedule(ch.getUpdateScheduleString());
		channelDTO.setRepairSchedule(ch.getVerRepairScheduleString());
		channelDTO.setPriority(ch.getOrder());
		channelDTO.setWowStatus(wowStatus);
		channelDTO.setExemptBlackout((ch.isExemptFromBlackout() ? "true" : "false"));
		channelDTO.setChannelAction(channelAction);
		channelDTO.setM_channelURL(ch.getUrl());
		channelDTO.setTitle(ch.getTitle());
		channelDTO.setContentType(ch.getType());
		
		if(DEBUG5) {
			System.out.println("Channel URL :" + ch.getUrl());
			System.out.println("Channel Action : " + channelAction);
			System.out.println("Channel Get Type :" + ch.getType());
			System.out.println("Channel primaryState_Schedule :" + primaryState_Schedule);
			System.out.println("Channel secState_Schedule :" + secState_Schedule);
			System.out.println("Channel update schedule:" + ch.getUpdateScheduleString());
			System.out.println("Channel repair schedule :" + ch.getVerRepairScheduleString());
			System.out.println("Channel Postpone schedule :" + ch.getPostponeScheduleString());
			
			String[] pairs = ch.getPropertyPairs();
			System.out.println("Channel Property Pairs Size : " + pairs.length);
			for (int i = 0; i < pairs.length; i += 2) {
	                System.out.println(pairs[i] + "," + ch.getUrl() + "=" + pairs[i + 1]);
	        }
		}
        // If channel alone remove from ldap then don't delete property 
        // because that property stored as Dummy channel automatically when removing channel
		if(!isRemovedChAlone) {
			List<ApprovalPropertyDTO> propsList = new ArrayList<ApprovalPropertyDTO>();
			String[] propertyPairs = ch.getPropertyPairs();
	
	        for (int count = 0; count < propertyPairs.length; count += 2) {
	        	ApprovalPropertyDTO chProp = new ApprovalPropertyDTO();
	        	chProp.setPropKey(propertyPairs[count]);
	        	chProp.setPropValue(encodePropsString(propertyPairs[count + 1]));
	        	chProp.setPropAction(channelAction);
	        	chProp.setPropType(PROPERTY_TYPE_CHANNEL);
	        	propsList.add(chProp);
	        }
	        if(propsList.size() > 0) {
	        	channelDTO.setPropertyList(propsList);
	        }
		}
		ApprovalPropertyDTO prop = getPostPoneScheduleProperty(ch.getPostponeScheduleString(), channelAction);
		if(null != prop) {
			channelDTO.addPropertyList(prop);
		}

		return channelDTO;
	}
	public ApprovalChannelDTO prepareModifyChannelDTO(String channelURL, ChannelDiffer modifyCh) throws ApprovalPolicyException {
		
		Map<String, Map<String, String>> modifyChannelAttrr = modifyCh.getChannelDiffInfo();
		ApprovalChannelDTO channelDTO = new ApprovalChannelDTO();
		String state = null;
		String secState = null;
		String primarySchedule = null;
		String secSchedule = null;
		String updateSchedule = null;
		String repairSchedule = null;
		String wowstatus = "";
		debug("Modified Channel attributes Value : " + channelURL);
		for( Map.Entry<String, Map<String, String>> chAttr : modifyChannelAttrr.entrySet()) {
		
			debug("Key : " + chAttr.getKey() + " Value :" + chAttr.getValue());
			
			String channelKey = chAttr.getKey();
			Map<String, String> chAttrValue = chAttr.getValue();
			
			if(CH_TYPE.equalsIgnoreCase(channelKey)) {
				channelDTO.setContentType(chAttrValue.get("newvalue"));
			} else if(CH_TITLE.equalsIgnoreCase(channelKey)) {
				channelDTO.setTitle(chAttrValue.get("newvalue"));
			} else if(CH_ORDER.equalsIgnoreCase(channelKey)) {
				try {
					channelDTO.setPriority(Integer.parseInt((chAttrValue.get("newvalue"))));
				} catch(Exception ex) {
					System.out.println("Failed to parse modify channel priority :" + channelURL);
				}
			} else if(CH_PRIMARY_STATE.equalsIgnoreCase(channelKey)) {
				state = chAttrValue.get("newvalue");
			} else if(CH_PRIMARY_SCHEDLUE.equalsIgnoreCase(channelKey)) {
				primarySchedule = chAttrValue.get("newvalue");
			} else if(CH_SECONDARY_STATE.equalsIgnoreCase(channelKey)) {
				secState = chAttrValue.get("newvalue");
			} else if(CH_SECONDARY_SCHEDULE.equalsIgnoreCase(channelKey)) {
				secSchedule = chAttrValue.get("newvalue");
			} else if(CH_UPDATE_SCHEDULE.equalsIgnoreCase(channelKey)) {
				channelDTO.setUpdateSchedule(chAttrValue.get("newvalue"));
			} else if(CH_REPAIR_SCHEDULE.equalsIgnoreCase(channelKey)) {
				channelDTO.setRepairSchedule(chAttrValue.get("newvalue"));
			} else if(CH_POSTPONE_SCHEDULE.equalsIgnoreCase(channelKey)) {
				ApprovalPropertyDTO prop = getPostPoneScheduleProperty(chAttrValue.get("newvalue"), MODIFY_OPERATION);
				if(null != prop) {
					channelDTO.addPropertyList(prop);
				}
			} else if(CH_EXEMPT_BLACKOUT.equalsIgnoreCase(channelKey)) {
				channelDTO.setExemptBlackout(chAttrValue.get("newvalue"));
			} else if(CH_WOW_URGENT.equalsIgnoreCase(channelKey)) {
				System.out.println("Channel Key : " + channelKey);
				System.out.println("Channel Key Value: " + chAttrValue.get("newvalue"));
				if("true".equalsIgnoreCase(chAttrValue.get("newvalue"))) {
					wowstatus = ("".equals(wowstatus)) ? "1-urgent" : wowstatus + ",1-urgent";
				} else if("false".equalsIgnoreCase(chAttrValue.get("newvalue"))) {
					wowstatus = ("".equals(wowstatus)) ? "0-urgent" : wowstatus + ",0-urgent";
				}
			} else if(CH_WOW_PRIMARY.equalsIgnoreCase(channelKey)) {
				if("true".equalsIgnoreCase(chAttrValue.get("newvalue"))) {
					wowstatus = ("".equals(wowstatus)) ? "1-init" : wowstatus + ",1-init";
				} else if("false".equalsIgnoreCase(chAttrValue.get("newvalue"))) {
					wowstatus = ("".equals(wowstatus)) ? "0-init" : wowstatus + ",0-init";
				}
			} else if(CH_WOW_SECONDARY.equalsIgnoreCase(channelKey)) {
				if("true".equalsIgnoreCase(chAttrValue.get("newvalue"))) {
					wowstatus = ("".equals(wowstatus)) ? "1-secondary" : wowstatus + ",1-secondary";
				} else if("false".equalsIgnoreCase(chAttrValue.get("newvalue"))) {
					wowstatus = ("".equals(wowstatus)) ? "0-secondary" : wowstatus + ",0-secondary";
				}
			} else if(CH_WOW_UPDATE.equalsIgnoreCase(channelKey)) {
				if("true".equalsIgnoreCase(chAttrValue.get("newvalue"))) {
					wowstatus = ("".equals(wowstatus)) ? "1-update" : wowstatus + ",1-update";
				} else if("false".equalsIgnoreCase(chAttrValue.get("newvalue"))) {
					wowstatus = ("".equals(wowstatus)) ? "0-update" : wowstatus + ",0-update";
				}

			} else if(CH_WOW_REPAIR.equalsIgnoreCase(channelKey)) {
				if("true".equalsIgnoreCase(chAttrValue.get("newvalue"))) {
					wowstatus = ("".equals(wowstatus)) ? "1-repair" : wowstatus + ",1-repair";
				} else if("false".equalsIgnoreCase(chAttrValue.get("newvalue"))) {
					wowstatus = ("".equals(wowstatus)) ? "0-repair" : wowstatus + ",0-repair";
				}
			} 
			channelDTO.setChannelAction(MODIFY_OPERATION);
		} 
		if(null != state || null !=  primarySchedule) {
			channelDTO.setPrimaryStateSchedule(state+","+primarySchedule);
		}
		if(null != secState || null != secSchedule) {
			channelDTO.setSecondaryStateSchedule(secState+","+secSchedule);
		}
		if(!"".equals(wowstatus)) {
			channelDTO.setWowStatus(wowstatus);
		}
		List<ApprovalPropertyDTO> propsList = new ArrayList<ApprovalPropertyDTO>();
		
		List<ApprovalPropertyDTO> addList = prepareChannelPropDTO(modifyCh.getAddedPropsMap(), ADD_OPERATION);
		if(addList.size() > 0) {
			propsList.addAll(addList);
		}
		List<ApprovalPropertyDTO> modifyList = prepareModifyChPropDTO(modifyCh.getModifiedPropsMap(), MODIFY_OPERATION);
		if(modifyList.size() > 0) {
			propsList.addAll(modifyList);
		}
		List<ApprovalPropertyDTO> deleteList = prepareChannelPropDTO(modifyCh.getDeletedPropsMap(), DELETE_OPERATION);
		if(deleteList.size() > 0) {
			propsList.addAll(deleteList);
		}
		if(propsList.size() > 0) {
			channelDTO.setChannelAction(MODIFY_OPERATION);
			channelDTO.setPropertyList(propsList);
		}
		Channel currentChannel = modifyCh.getChannel();
		
		channelDTO.setM_channelURL(currentChannel.getUrl());
		channelDTO.setTitle(currentChannel.getTitle());
		
		if(null == channelDTO.getContentType() || "".equals(channelDTO.getContentType())) {
			channelDTO.setContentType(currentChannel.getType());
		}
		return channelDTO;
	}
	private List<ApprovalPropertyDTO> prepareChannelPropDTO(Map<String, String> modifyChProp, int chPropAction) throws ApprovalPolicyException {
		List<ApprovalPropertyDTO> ChPropsList = new ArrayList<ApprovalPropertyDTO>();
		for(Map.Entry<String, String> propMap : modifyChProp.entrySet()) {
			ApprovalPropertyDTO propDTO = new ApprovalPropertyDTO();
			propDTO.setPropAction(chPropAction);
			propDTO.setPropKey(propMap.getKey());
			propDTO.setPropType(PROPERTY_TYPE_CHANNEL);
			propDTO.setPropValue(encodePropsString(propMap.getValue()));
			ChPropsList.add(propDTO);
		}
		return ChPropsList;
	}
	private List<ApprovalPropertyDTO> prepareModifyChPropDTO(Map<String, Map<String, String>> modifyChProp, int chPropAction) throws ApprovalPolicyException {

		List<ApprovalPropertyDTO> ChPropsList = new ArrayList<ApprovalPropertyDTO>();
		for(Map.Entry<String, Map<String, String>> propMap : modifyChProp.entrySet()) {
			ApprovalPropertyDTO propDTO = new ApprovalPropertyDTO();
			propDTO.setPropAction(chPropAction);
			propDTO.setPropKey(propMap.getKey());
			propDTO.setPropType(PROPERTY_TYPE_CHANNEL);
			Map<String, String> propValue = propMap.getValue();
			propDTO.setPropValue(encodePropsString(propValue.get("newvalue")));
			ChPropsList.add(propDTO);
		}
		return ChPropsList;
	}
	/**
	 * Convert all tuner props Diff to DB format
	 * @param tunerPropsList
	 * @param policyTunerPropsAction
	 * @return
	 * @throws ApprovalPolicyException
	 */
	public List<ApprovalPropertyDTO> getTunerPropsDBFormat(Map<String, String> tunerPropsList, int policyTunerPropsAction) throws ApprovalPolicyException {
		List<ApprovalPropertyDTO> propList = new ArrayList<ApprovalPropertyDTO>();
		
		Map<String, String> propsMap = tunerPropsList;
		for( Map.Entry<String, String> tunerProp : propsMap.entrySet()) {
			ApprovalPropertyDTO prop = new ApprovalPropertyDTO();
			// tuner prop key contains key,type
		    if(tunerProp.getKey().indexOf(",") > 0) {
				String[] keyPair = tunerProp.getKey().split(",");
				prop.setPropKey(keyPair[0]);
				prop.setPropType(keyPair[1]);
		    } else {
		    	prop.setPropKey(tunerProp.getKey());
		    	prop.setPropType(PROPERTY_TYPE_TUNER);
		    }
			prop.setPropValue(encodePropsString(tunerProp.getValue()));
			prop.setPropAction(policyTunerPropsAction);
		    propList.add(prop);
		}
		return propList;

	}
	private List<ApprovalPropertyDTO> getModifyTunerPropsDBFormat(Map<String, Map<String, String>> modifyChProp, int chPropAction) throws ApprovalPolicyException {
		List<ApprovalPropertyDTO> ChPropsList = new ArrayList<ApprovalPropertyDTO>();
		for(Map.Entry<String, Map<String, String>> tunerProp : modifyChProp.entrySet()) {
			ApprovalPropertyDTO propDTO = new ApprovalPropertyDTO();
			propDTO.setPropAction(chPropAction);
			
			if(tunerProp.getKey().indexOf(",") > 0) {
				String[] keyPair = tunerProp.getKey().split(",");
				propDTO.setPropKey(keyPair[0]);
				propDTO.setPropType(keyPair[1]);
		    } else {
		    	propDTO.setPropKey(tunerProp.getKey());
		    	propDTO.setPropType(PROPERTY_TYPE_TUNER);
		    }
			Map<String, String> propValue = tunerProp.getValue();
			propDTO.setPropValue(encodePropsString(propValue.get("newvalue")));
			ChPropsList.add(propDTO);
		}
		return ChPropsList;
	}

	// postpone schedule storing as channel property because no specific 
	// attribute for post pone schedule in ldap, same to follow when storing DB 
	private ApprovalPropertyDTO getPostPoneScheduleProperty(String postPoneSchedule, int propertyAction) {
		ApprovalPropertyDTO prop = null;
		if(null != postPoneSchedule) {
			prop = new ApprovalPropertyDTO();
			prop.setPropAction(propertyAction);
			prop.setPropKey(CH_POSTPONESCHED_KEY);
			prop.setPropType(PROPERTY_TYPE_CHANNEL);
			prop.setPropValue(postPoneSchedule);
		}
		return prop;

	}
	private static String encodeWOWString(boolean urgentSch, boolean initSch, boolean secSch, boolean updateSch, boolean repairSch) {
		String wowString = "";
		if (urgentSch)
			wowString = "urgent";
		if (initSch)
			wowString = "".equals(wowString) ? "init" : wowString + ",init";
		if (secSch)
			wowString = "".equals(wowString) ? "secondary" : wowString + ",secondary";
		if (updateSch)
			wowString = "".equals(wowString) ? "update" : wowString + ",update";
		if (repairSch)
			wowString = "".equals(wowString) ? "repair" : wowString + ",repair";
		return wowString;
    }
	private static String encodeStateScheduleString(String channelState, String channelSchedule, String stateType) {
		String stateScheduleStr = "";
	    String state = (null != channelState) ? channelState : "";
	    String schedule = (null != channelSchedule) ? channelSchedule : "";
	    
	    if("primary".equalsIgnoreCase(stateType)) {
		    if("".equals(schedule)) {
		    	stateScheduleStr = state;
		    } else {
		    	stateScheduleStr = state + COMMA_SEPARATOR + schedule;
		    }

	    } else if("secondary".equalsIgnoreCase(stateType)) {
		    if( (!("".equals(state))) && "".equals(schedule)) {
		    	stateScheduleStr = state;
		    } else if ("".equals(state)) {
		    	stateScheduleStr = "";
		    } else {
		    	stateScheduleStr = state + COMMA_SEPARATOR + schedule;
		    }
	    }
	    return stateScheduleStr;
	}
	// If admin wants to remove property from endpoint machine then property value should set as "null" 
	private static String encodePropsString(String propValue) {
		if(null != propValue && STR_NONE.equals(propValue)) {
			return STR_NULL;
		}
		return propValue;
	}
	private void debug(String msg) {
		if(DEBUG5) {
			System.out.println("PolicyApprovalHandler : " + msg);
		}
	}
}
