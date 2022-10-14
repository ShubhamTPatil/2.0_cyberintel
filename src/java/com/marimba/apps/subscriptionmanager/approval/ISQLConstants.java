package com.marimba.apps.subscriptionmanager.approval;

public interface ISQLConstants {
	public static String INSERT_POLICY_CHANGE_REQUEST_SQL = "insert into policy_change_request(policy_name, policy_target, policy_target_type, ldap_source,action, status,ar_reference_tag,software_path,tx_group,tx_user,all_target,blackout_schedule,change_owner,reviewed_by,remarks,reviewed_on,created_on) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	public static String INSERT_POLICY_CHANGE_REQUEST_ORACLE = "insert into policy_change_request(change_id,policy_name, policy_target, policy_target_type, ldap_source,action, status,ar_reference_tag,software_path,tx_group,tx_user,all_target,blackout_schedule,change_owner,reviewed_by,remarks,reviewed_on,created_on) values(pcr_seq.NEXTVAL,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	public static String INSERT_POLICY_CHANNELS_SQL = "insert into policy_channels(channel_id,change_id,channel_action,primary_state_schedule,secondary_state_schedule, update_schedule,repair_schedule,priority,wow_status,exempt_blackout) values(?,?,?,?,?,?,?,?,?,?)";
	public static String INSERT_POLICY_CHANNELS_ORACLE = "insert into policy_channels(policy_channel_id,channel_id,change_id,channel_action,primary_state_schedule,secondary_state_schedule, update_schedule,repair_schedule,priority,wow_status,exempt_blackout) values(pc_seq.NEXTVAL,?,?,?,?,?,?,?,?,?,?)";

}
