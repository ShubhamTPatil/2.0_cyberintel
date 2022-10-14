// Copyright 1997-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.approval;

import java.util.List;
import java.util.ArrayList;
/**
 * This class is used to store list of channels and its properties 
 * for peer approval storage
 *
 * @author Selvaraj Jegatheesan
 */
public class ApprovalChannelDTO {
	
	private int policyChannelId;
	private int m_channelID;
	private int m_changeID;
	private String m_channelURL;
	private String contentType;
	private String title;
	private int channelAction;
	private String m_pri_state_schedule;
	private String m_sec_state_schedule;
	private String m_update_schedule;
	private String m_repair_schedule;
	private int m_priority;
	private String m_wow_status;
	private String exemptBlackout;
	private List<ApprovalPropertyDTO> propertyList;
	private List<ApprovalPropertyDTO> addedPropertyList;
	private List<ApprovalPropertyDTO> modifiedPropertyList;
	private List<ApprovalPropertyDTO> deletedPropertyList;
	
	public ApprovalChannelDTO() {
		propertyList = new ArrayList<ApprovalPropertyDTO>();
		addedPropertyList = new ArrayList<ApprovalPropertyDTO>();
		modifiedPropertyList = new ArrayList<ApprovalPropertyDTO>();
		deletedPropertyList = new ArrayList<ApprovalPropertyDTO>();
	}

	public int getPolicyChannelId() {
		return policyChannelId;
	}

	public void setPolicyChannelId(int policyChannelId) {
		this.policyChannelId = policyChannelId;
	}

	/**
	 * @return the channleId from DB
	 */
	public int getChannelID() {
		return m_channelID;
	}

	
	public void setChannelID(int channelID) {
		this.m_channelID = channelID;
	}

	public int getM_changeID() {
		return m_changeID;
	}

	public void setM_changeID(int m_changeID) {
		this.m_changeID = m_changeID;
	}

	public String getM_channelURL() {
		return m_channelURL;
	}

	public void setM_channelURL(String m_channelURL) {
		this.m_channelURL = m_channelURL;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getChannelAction() {
		return channelAction;
	}

	public void setChannelAction(int channelAction) {
		this.channelAction = channelAction;
	}

	/**
	 * @return the channel primary state and schedule string as comma separated
	 */
	public String getPrimaryStateSchedule() {
		return m_pri_state_schedule;
	}
	
	public void setPrimaryStateSchedule(String pri_state_schedule) {
		this.m_pri_state_schedule = pri_state_schedule;
	}

	/**
	 * @return the the channel secondary state and schedule string as comma separated
	 */
	public String getSecondaryStateSchedule() {
		return m_sec_state_schedule;
	}

	public void setSecondaryStateSchedule(String sec_state_schedule) {
		this.m_sec_state_schedule = sec_state_schedule;
	}

	/**
	 * @return the updateSchedule string. Null when no update schedule is set
	 */
	public String getUpdateSchedule() {
		return m_update_schedule;
	}

	public void setUpdateSchedule(String update_schedule) {
		this.m_update_schedule = update_schedule;
	}

	/**
	 * @return the repair schedule string. Null when no update schedule is set
	 */
	public String getRepairSchedule() {
		return m_repair_schedule;
	}

	public void setRepairSchedule(String repair_schedule) {
		this.m_repair_schedule = repair_schedule;
	}

	/**
	 * @return the channel priority
	 */
	public int getPriority() {
		return m_priority;
	}

	public void setPriority(int priority) {
		this.m_priority = priority;
	}

	/**
	 * @return the WOW status for four schedules as comma separated value
	 */
	public String getWowStatus() {
		return m_wow_status;
	}

	public void setWowStatus(String wow_status) {
		this.m_wow_status = wow_status;
	}

	public String getExemptBlackout() {
		return exemptBlackout;
	}

	public void setExemptBlackout(String exemptBlackout) {
		this.exemptBlackout = exemptBlackout;
	}


	public List<ApprovalPropertyDTO> getPropertyList() {
		return propertyList;
	}

	public void setPropertyList(List<ApprovalPropertyDTO> propertyList) {
		this.propertyList = propertyList;
	}

    public List<ApprovalPropertyDTO> getAddedPropertyList() {
		return addedPropertyList;
	}

	public void setAddedPropertyList(List<ApprovalPropertyDTO> addedPropertyList) {
		this.addedPropertyList = addedPropertyList;
	}

	public List<ApprovalPropertyDTO> getModifiedPropertyList() {
		return modifiedPropertyList;
	}

	public void setModifiedPropertyList(
			List<ApprovalPropertyDTO> modifiedPropertyList) {
		this.modifiedPropertyList = modifiedPropertyList;
	}

	public List<ApprovalPropertyDTO> getDeletedPropertyList() {
		return deletedPropertyList;
	}

	public void setDeletedPropertyList(List<ApprovalPropertyDTO> deletedPropertyList) {
		this.deletedPropertyList = deletedPropertyList;
	}

	public void addPropertyList(ApprovalPropertyDTO chProps) {
        this.propertyList.add(chProps);
        System.out.println("chProps.getPropAction() :" + chProps.getPropAction());
        switch (chProps.getPropAction()) {
            case IApprovalPolicyConstants.ADD_OPERATION:
            	System.out.println("Channel property added to list");
                addedPropertyList.add(chProps);
                break;
            case IApprovalPolicyConstants.DELETE_OPERATION:
                deletedPropertyList.add(chProps);
                break;
            case IApprovalPolicyConstants.MODIFY_OPERATION:
            	modifiedPropertyList.add(chProps);;
            	break;
        }
    }

    //clear all variables before usage
	public void reset() {
		
	}
		
}
