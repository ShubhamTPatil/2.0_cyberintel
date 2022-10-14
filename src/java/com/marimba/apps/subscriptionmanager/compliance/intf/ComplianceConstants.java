// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.intf;

/**
 * Compliance related constants
 *
 * @author  Zheng Xia
 * @author  Manoj Kumar
 * @version $Revision$, $Date$
 */

public interface ComplianceConstants {

    // Compliance query state
    public static final int STATE_IN_QUEUE = 0;
    public static final int STATE_IN_QUERY = 1;
    public static final int STATE_DONE = 2;
    public static final int STATE_ERROR = 3;
    public static final int STATE_NOT_CALCULATE = 4;
    public static final int HOUR=60 * 60 * 1000; //milliseconds

    // Compliance level
    public static final int LEVEL_COMPLIANT = 0;
    public static final int LEVEL_NON_COMPLIANT = 1;
    public static final int LEVEL_NOT_CHECK_IN = 2;

    // Compliance level string
    public static final String STR_LEVEL_COMPLIANT = "COMPLIANT";
    public static final String STR_LEVEL_NON_COMPLIANT = "NON-COMPLIANT";
    public static final String STR_LEVEL_NOT_CHECK_IN = "NOT-CHECKED-IN";
    public static final String STR_LEVEL_NOT_APPLICABLE = "NOT APPLICABLE";
    public static final String STR_TOTAL_MACHINES =  "TOTAL";
    public static final String PKG_SUCCESS = "succeed";
    public static final String PKG_FAILED = "failed";
    public static final String PKG_NOTCHECKIN = "notcheckedin";
    public static final String PKG_NOT_APPLICABLE = "notapplicable";


    // power Compliance level string
    public static final String POWER_STR_LEVEL_COMPLIANT = "POWER_COMPLIANT";
    public static final String POWER_STR_LEVEL_NON_COMPLIANT = "POWER_NON-COMPLIANT";
    public static final String POWER_STR_LEVEL_NOT_CHECK_IN = "POWER_NOT-CHECKED-IN";
    public static final String POWER_STR_TOTAL_MACHINES =  "POWER_TOTAL";
    public static final String POWER_PROP_SUCCESS = "powerSucceed";
    public static final String POWER_PROP_FAILED = "powerFailed";
    public static final String POWER_PROP_NOTCHECKIN = "powerNotcheckedin";

    //OS Migration Compliance level string
    public static final String OSM_PROP_SUCCESS = "osmSucceed";
    public static final String OSM_PROP_FAILED = "osmFailed";
    public static final String OSM_PROP_PENDING ="osmPending";


    // group compliance result type
    public static final String GRP_COMP_TARGET_RSLT_TYPE = "target";
    public static final String GRP_COMP_OVERALL_RSLT_TYPE = "overall";
    public static final String GRP_COMP_OVERVIEW_RSLT_TYPE = "overview";
    public static final String GRP_COMP_PWR_PROP_RSLT_TYPE = "powerprops";
    public static final String MAC_COMP_PWR_PROP_RSLT_TYPE = "macPowerProps";
    public static final String GRP_COMP_OS_DEPLOYMENT = "osdeployment";
    public static final String MAC_COMP_USGCB = "USGCB";



    // Constants for session variables
    static final String JSON_RPC_BRIDGE = "JSONRPCBridge";
    static final String PACKAGE_VIEW_SERVICE = "packageviewservice";
    static final String TARGET_VIEW_SERVICE = "targetviewservice";
    static final String COMPLIANCE_SUMMARY_SERVICE="compliancesummaryservice";
    static final String COMPLIANCEMAIN = "compliancemain";
    static final String MACHINE_LIST_SERVICE = "machinelistservice";
    static final String COMPLIANCE_REPORT_SERVICE = "compliancerptservice";
    static final String POWER_SUMMARY_SERVICE = "powersummaryservice";

    // Name of the compliance query file.
    static final String SQL_QUERY_FILE = "/compliancesql.properties";
    public static final String QUERY_FOLDER_PATH = "/library/Policy Compliance/";
    public static final String USER_PRINCIPAL = "session_smuser";

    // compliance date and time format style
    public static final int INPUT_COMPLIANCE_DATESTYLE = java.text.DateFormat.SHORT;
    public static final int INPUT_COMPLIANCE_TIMESTYLE = java.text.DateFormat.SHORT;

    public static String[] powerOptions = {"MonitorIdle", "HDIdle", "StandByIdle", "HibernateIdle",
                                           "PwdPromt", "MonitorIdle_DC", "HDIdle_DC", "StandByIdle_DC", "HibernateIdle_DC",
                                           "EnableHibernate", "SchemeName",};
    public static String[] policyPowerOption = {"policy_monitor_idletime", "policy_disk_idletime", "policy_standby_idletime",
                       "policy_hibernate_idletime", "policy_password_prompt_enable", "policy_monitor_idletime_dc", "policy_disk_idletime_dc", "policy_standby_idletime_dc",
                       "policy_hibernate_idletime_dc", "policy_hibernate_enable", "policy_scheme_name"};
    public static String[] endpointPowerOption = {"endpoint_monitor_idletime", "endpoint_disk_idletime", "endpoint_standby_idletime",
                         "endpoint_hibernate_idletime", "endpoint_password_prompt_enabl", "endpoint_monitor_idletime_dc", "endpoint_disk_idletime_dc", "endpoint_standby_idletime_dc",
                         "endpoint_hibernate_idletime_dc", "endpoint_hibernate_enable", "endpoint_scheme_name"};
    public static String[] powercompliant = {"monitor_idletime", "disk_idletime", "standby_idletime",
                         "hibernate_idletime", "password_prompt_enable", "monitor_idletime_dc", "disk_idletime_dc", "standby_idletime_dc",
                         "hibernate_idletime_dc", "hibernate_enable", "scheme_name"};
    public static String LDAPSYNC_POLICY_UPDATE_TIME = "ldapsync_policy_update_time";
    public static String  ENDPOINT_POLICY_UPDATE_TIME = "endpoint_policy_update_time";
    public static final String PWR_UNSET_PROP_VALUE = "-1";
    public static final String PWD_PROMPT_ENABLE = "policy_password_prompt_enable";
    public static final String HIBERNATE_ENABLE = "policy_hibernate_enable";
    public static final String ENABLE = "Enable";
    public static final String DISABLE = "Disable";    
    public static final String POWER_KEYWORD = "POWER";

    public static String  COMPLAINT = "COMPLIANT";
    public static String  NON_COMPLAINT = "NON-COMPLIANT";
    public static String  NOT_CHECKEDIN = "NOT-CHECKED-IN";
    public static String  NOT_APPLICABLE = "NOT APPLICABLE";
    public static String  COMPLAINT_PERCENTEAGE = "compliant_percentage";
    public static String  NON_COMPLAINT_PERCENTEAGE = "noncompliant_percentage";
    public static String  NOT_CHECKEDIN_PERCENTEAGE = "notcheckedin_percentage";
    public static String  COUNT = "count";

}

