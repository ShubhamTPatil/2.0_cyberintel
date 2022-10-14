
package com.marimba.apps.subscriptionmanager.arsystem;

import com.marimba.intf.logs.ILogConstants;

/**
 * Log constants for AR Integration classes.
 *
 * @author   Jayaprakash Paramasivam
 * @version  $Revision$, $Date$
 *
 */

public interface ARTaskLogConstants extends ILogConstants {

    // Log Constants for Skyline Integration

    int LOG_AR_TASK_ELEMENT = SPM_MIN + 900;  //LOG_AUDIT Element
    int LOG_AR_RETURN_ELEMENT = SPM_MIN + 901; //LOG_AUDIT Return Element

    int LOG_AR_TASK_ID = SPM_MIN + 902; //LOG_AUDIT TaskID
    int LOG_AR_CHANGE_ID = SPM_MIN + 903; //LOG_AUDIT ChangeID

    int LOG_AR_TARGETS = SPM_MIN  + 904;  //LOG_AUDIT Task Relationship target
    int LOG_AR_CHANNELS = SPM_MIN + 905;  //LOG_AUDIT Task Relationship channel

    int LOG_AR_TARGETS_NOT_FOUND = SPM_MIN + 906; //LOG_MAJOR AR Targets not found
    int LOG_AR_CHANNELS_NOT_FOUND = SPM_MIN + 907; //LOG_MAJOR AR Channels not found

    int LOG_AR_TARGET_CHANNELS_NOT_FOUND = SPM_MIN + 908; //LOG_MAJOR Target/Channel Not found

    int LOG_AR_VERIFYTASK_CREATE_OK = SPM_MIN + 909; //LOG_AUDIT Verification Task creation  succeeds
    int LOG_AR_VERIFYTASK_CREATE_ERROR = SPM_MIN + 910; //LOG_MAJOR Verification Task creation failed

    int LOG_AR_VERIFYTASK_SUCCEEDED = SPM_MIN + 911; //LOG_AUDIT  Verification execution succeeds
    int LOG_AR_VERIFYTASK_FAILED = SPM_MIN + 912; //LOG_MAJOR Verification execution failed

    int LOG_AR_VERIFYTASK_RETURN_MSG = SPM_MIN + 913; //LOG_AUDIT Verification task return message

    int LOG_AR_COMPLIANCE_PERCENTAGE = SPM_MIN + 914; //LOG_AUDIT compliance percentage
    int LOG_AR_USER = SPM_MIN + 915;                  //LOG_AUDIT compliance user
    int LOG_AR_ENDTIME = SPM_MIN + 916;               //LOG_AUDIT compliance endtime
    int LOG_AR_TASK_SCHEDULE = SPM_MIN + 917;         //LOG_AUDIT compliance schedule
    int LOG_AR_NO_ENDTIME = SPM_MIN + 918;            //LOG_AUDIT compliance no endtime

    int LOG_AR_TASK_CREATION_SUCCESS = SPM_MIN + 919;  //LOG_AUDIT Task created to taskmanager
    int LOG_AR_TASK_REMOVAL_SUCCESS = SPM_MIN + 920;   //LOG_AUDIT Task removed from taskmanager

    int LOG_AR_SQL_EXCEPTION = SPM_MIN + 921; //LOG_MAJOR SQL Exception

    int LOG_AR_EXCEPTION = SPM_MIN + 922; //LOG_MAJOR Any General Exception

    int LOG_AR_POLICY_CREATION_SUCCESS = SPM_MIN + 923; //LOG_AUDIT Updated the policy sucessfully
    int LOG_AR_POLICY_CREATION_FAILED = SPM_MIN + 924; //LOG_MAJOR Exception While updating the policy

    int LOG_AR_NO_ACLWRITE_PERMISSION = SPM_MIN + 925; //LOG_MAJOR no acl write permission
}
