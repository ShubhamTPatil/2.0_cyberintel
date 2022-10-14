package com.marimba.apps.subscriptionmanager.intf;

/**
 * Constants for AR Integration classes.
 *
 * @author Jayaprakash Paramasivam
 * @version 1.0 12/28/2005
 */

public interface IARTaskConstants {

    // ArSystem session variables
    String AR_TARGET_REFRESH = "arTargetRefresh";
    String AR_TASK_ID = "taskid";
    String AR_CHANGE_ID = "changeid";
    String AR_COMPLIANCE_TARGET_DISPLAY = "targetCompDisplay";
    String AR_COMPLIANCE_CHANNEL_DISPLAY = "channelCompDisplay";
    String AR_VIEW_TYPE = "arViewType";
    String AR_USER ="arUser";
    String AR_COMP_PERCENTAGE = "arCompPercentage";
    String AR_EXPIRY_TIME = "arExpiryTime";
    String AR_SCHEDULE = "arSchedule";
    String AR_TARGETS_DISPLAY = "arVerifyTargets";
    String AR_CHANNELS_DISPLAY = "arVerifyChannels";
    String AR_TASK_ID_ENABLE = "taskEnabled";
    String AR_TASK_ID_ENABLED_CONFIG = "subscriptionmanager.ar.taskid.enabled";
    String SESSION_AR_TASKID_ENABLED = "taskIDEnabled";
    String AR_CHANNELS = "arChannels";
    String VERIFY_TASK_SERVICE = "verifytaskservice";
    String RETURN_CODE = "returnCode";

    // Verification Task Constants
    String VERIFY_TASK_TYPE = "PolicyCompliance/verifytask";
    String VERIFY_TASK_GROUP = "verifycompliance";
    String VERIFY_TASK_NAME = "ccmtask_";
    String TASK_SCHEDULE_NEVER = "never";
    String DEFAULT_PERCENTAGE   = "100";
    String DEFAULT_TIMEOUT      = "0";

    // Targets with no write permission
    String ACL_TARGETS = "targetsACLNoWrite";
}
