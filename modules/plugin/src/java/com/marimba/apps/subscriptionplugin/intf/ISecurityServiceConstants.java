// Copyright 2018, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionplugin.intf;

public interface ISecurityServiceConstants {

    static final int MAGIC                              = 0xDaceDeed;
    static final int VERSION                            = 1;

    int SECURITY_SERVICE_SEND_COMPLIANCE_DETAILS_REQUEST = 1;
    int SECURITY_SERVICE_SEND_COMPLIANCE_DETAILS_REPLY = 2;

    int UNKNOWN_REQUEST_TYPE = 70;

    // input for plugin requests
    String PLUGIN_REQUEST_URL                       = "plugin.request.url";
    String PLUGIN_REQUEST_AUTH                      = "plugin.request.auth";
    String PLUGIN_REQUEST_CODE                      = "plugin.request.code";
    String PLUGIN_REQUEST_COMPLIANCE_DETAILS_PATH   = "plugin.request.compliance.details.path";
    String PLUGIN_REQUEST_COMPLIANCE_DETAILS_SIZE   = "plugin.request.compliance.details.size";

    String PLUGIN_REQUEST_COMPLIANCE_DETAILS_LATEST            = "securitycompliance";
    String PLUGIN_REQUEST_COMPLIANCE_DETAILS_PREVIOUS          = "securitycompliance_prev";
    String PLUGIN_REQUEST_COMPLIANCE_DETAILS_DIFF              = "securitycompliance_diff";
    String PLUGIN_REQUEST_COMPLIANCE_DETAILS_CLIENT            = "securitycompliance_client_";
    String PLUGIN_REQUEST_COMPLIANCE_DETAILS_CLIENT_PREVIOUS   = "securitycompliance_client_prev";
    String PLUGIN_REQUEST_COMPLIANCE_DETAILS_CLIENT_DIFF       = "securitycompliance_client_diff";
    String PLUGIN_REQUEST_COMPLIANCE_DETAILS_CLIENT_QUEUE      = "queue_securitycompliance_client_";

    String MACHINE_NODE_PREFIX                          = "machine";
    String MACHINE_NAME                                 = "name";
    String MACHINE_SCANNER_COUNT                        = "scanners.count";
    String COMPLIANCE_DETAILS_NODE_PREFIX               = "machine.security_compliance";
    String COMPLIANCE_DETAILS_SCAN_TYPE                 = "scanType";
    String COMPLIANCE_DETAILS_CONTENT_ID                = "contentid";
    String COMPLIANCE_DETAILS_CONTENT_FILE_NAME         = "contentfilename";
    String COMPLIANCE_DETAILS_CONTENT_TITLE             = "contenttitle";
    String COMPLIANCE_DETAILS_CONTENT_TARGET_OS         = "contenttargetos";
    String COMPLIANCE_DETAILS_PROFILE_ID                = "profileid";
    String COMPLIANCE_DETAILS_PROFILE_TITLE             = "profiletitle";
    String COMPLIANCE_DETAILS_SCANNED_BY                = "scannedby";
    String COMPLIANCE_DETAILS_TARGET_NAME               = "targetname";
    String COMPLIANCE_DETAILS_OVERALL_COMPLIANCE        = "overallcompliance";
    String COMPLIANCE_DETAILS_INDIVIDUAL_COMPLIANCE     = "individualcompliance";
    String COMPLIANCE_DETAILS_START_TIME                = "starttime";
    String COMPLIANCE_DETAILS_FINISH_TIME               = "finishtime";
    String COMPLIANCE_DETAILS_LAST_POLICY_UPDATE_TIME   = "lastpolicyupdatetime";
    String COMPLIANCE_DETAILS_MACHINE_NAME              = "machinename";
    String COMPLIANCE_DETAILS_RULE_PREFIX               = "rule:";

    String COMPLIANCE_DETAILS_SCAN_TYPE_XCCDF           = "xccdf";
    String COMPLIANCE_DETAILS_SCAN_TYPE_OVAL            = "oval";

}

