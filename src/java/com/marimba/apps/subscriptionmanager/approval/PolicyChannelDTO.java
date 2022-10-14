package com.marimba.apps.subscriptionmanager.approval;

import java.util.Map;

public class PolicyChannelDTO {
	
	private int m_channelID;
	private String m_pri_state_schedule;
	private String m_sec_state_schedule;
	private String m_update_schedule;
	private String m_repair_schedule;
	private int m_priority;
	private String m_wow_status;
	private String m_blockout;
	
	private Map<String, String> m_properties;
	
	public PolicyChannelDTO() {
		//No implementation, getter and setter methods will be used
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

	public String getBlockoutSchedule() {
		return m_blockout;
	}

	public void setBlockoutSchedule(String blockout) {
		this.m_blockout = blockout;
	}
	
	public Map<String, String> getProperties() {
		return this.m_properties;
	}
	
	public void setProperties(Map<String, String> properties) {
		this.m_properties = properties;
	}
	
	//clear all variables before usage
	public void reset() {
		
	}
		
}
