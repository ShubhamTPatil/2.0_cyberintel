// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionplugin;

import com.marimba.intf.logs.ILogConstants;

/**
 * Constants used security plugin logging
 *
 */
public interface LogConstants
    extends ILogConstants {

    // Audit messages
    int LOG_START_PLUGIN = SPM_MIN + 100; // LOG_AUDIT
    int LOG_STOP_PLUGIN = SPM_MIN + 101; // LOG_AUDIT
    int LOG_LDAP_CONNECTION = SPM_MIN + 102; // LOG_AUDIT, host string:baseDN
    int LOG_LDAP_CONFIG_READ = SPM_MIN + 103; // LOG_AUDIT, config info
    int LOG_PROCESSED_REQUEST = SPM_MIN + 104; // LOG_INFO, src = remote addrees of request, user = tunerid/user of request, Search timings
    int LOG_PROCESSING_REQUEST = SPM_MIN + 105; // LOG_AUDIT, src = remote addrees of request, user = tunerid/user of request, remote machine name
    int LOG_MAPPED_TX_NAME = SPM_MIN + 109; // LOG_AUDIT,Hostname/IP Address of the mapped Transmitter
    int LOG_PLUGIN_DISABLED = SPM_MIN + 110; // LOG_AUDIT
    int LOG_PLUGIN_NOREADACCESS = SPM_MIN + 111; // LOG_AUDIT
    int LOG_PROVISION_DN_LIST = SPM_MIN + 112;   // LOG_AUDIT

    int LOG_HM_UPDATESUCESS_CNT = SPM_MIN + 113; // LOG_INFO
    int LOG_HM_UPDATEFAILURE_CNT = SPM_MIN + 114;

    int LOG_PLUGIN_MASTER_MODE = SPM_MIN + 115; // LOG_INFO
    int LOG_PLUGIN_REPEATER_MODE_INSERTS = SPM_MIN + 116; // LOG_INFO
    int LOG_PLUGIN_SLAVE_MODE_NOINSERTS = SPM_MIN + 117; // LOG_INFO

    int LOG_QUEUE_PROCESSING_START = SPM_MIN + 118; // LOG_AUDIT
    int LOG_QUEUE_PROCESSING_END = SPM_MIN + 119; // LOG_AUDIT

    int LOG_PLUGIN_REPORT_PROCESSING = SPM_MIN + 200; // LOG_AUDIT
    int LOG_PLUGIN_REPORT_PROCESSED = SPM_MIN + 201; // LOG_AUDIT
    int LOG_PLUGIN_REPORT_FORWARDING = SPM_MIN + 202; // LOG_AUDIT
    int LOG_PLUGIN_REPORT_FORWARDED = SPM_MIN + 203; // LOG_AUDIT
    int LOG_PLUGIN_REPORT_QUEUED = SPM_MIN + 204; // LOG_AUDIT

    // Error messages
    int LOG_NO_DATA_SOURCE = SPM_MIN + 209; // LOG_CRITICAL, src = remote addrees of request, user = tunerid/user of request
    int LOG_LDAP_CONN_ERROR = SPM_MIN + 210; // LOG_MAJOR, LDAPException
    int LOG_ERROR_LDAP_QUERY = SPM_MIN + 211; // LOG_MAJOR, LDAPException, src = remote addrees, user = tunerid/user of request
    int LOG_ERROR_LDAP_NAMING = SPM_MIN + 212; // LOG_MAJOR, NamingException, src = remote addrees, user = tunerid/user of request
    int LOG_ERROR_LDAP_IOERROR = SPM_MIN + 213; // LOG_MAJOR, IOException str, src = remote addrees, user = tunerid/user of request
    int LOG_ERROR_LDAP_RUNTIME = SPM_MIN + 214; // LOG_MAJOR, RuntimeException str, src = remote addrees, user = tunerid/user of request
    int LOG_ERROR_INVALID_TX_NAME = SPM_MIN + 216; // LOG_MAJOR, Line number in the mapping file and the string on the line
    int LOG_ERROR_INVALID_SERVER = SPM_MIN + 217; // LOG_MAJOR, Error
    int LOG_ERROR_INVALID_USESSL = SPM_MIN + 218; // LOG_MAJOR, Error
    int LOG_ERROR_INVALID_KEY = SPM_MIN + 219; // LOG_MAJOR, Error Line number in the mapping file and the string on the line
    int LOG_ERROR_DUPLICATE_KEY = SPM_MIN + 220; // LOG_MAJOR, Error Line number in the mapping file and the string on the line
    int LOG_ERROR_READING_MAP_FILE = SPM_MIN + 221; // LOG_MAJOR, Error/Exception stack trace
    int LOG_ERROR_MISSING_BASEDN = SPM_MIN + 222; // LOG_MAJOR, Error
    int LOG_ERROR_MISSING_BINDDN = SPM_MIN + 223; // LOG_MAJOR, Error
    int LOG_ERROR_MISSING_PASSWORD = SPM_MIN + 224; // LOG_MAJOR, Error
    int LOG_ERROR_MISSING_SERVERANDDOMAIN = SPM_MIN + 225; // LOG_MAJOR, Error
    int LOG_ERROR_LDAP_ENV = SPM_MIN + 226; // LOG_MAJOR, Error
    int LOG_ERROR_CURRENTDOMAIN_NULL = SPM_MIN + 227; // LOG_MAJOR, Error
    int LOG_ERROR_LDAP_CONFIG_CONN = SPM_MIN + 228; // LOG_MAJOR, Error
    int LOG_ERROR_LDAP_SUBCONFIG_NULL = SPM_MIN + 229; // LOG_MAJOR, Error
    int LOG_ERROR_LDAP_SUBCONFIG_SE = SPM_MIN + 230; // LOG_MAJOR, Error
    int LOG_ERROR_LDAP_DOMAIN_EX = SPM_MIN + 235; // LOG_MAJOR, Error
    int LOG_ERROR_GETFORESTROOT_FAILED = SPM_MIN + 236; // LOG_MAJOR, Error
    int LOG_ERROR_GETSITE_FAILED = SPM_MIN + 237; // LOG_MAJOR, Error
    int LOG_ERROR_NO_DN_FROM_CLIENT = SPM_MIN + 238; // LOG_MAJOR, Error
    int LOG_ERROR_LDAP_GROUPMEMBERSHIP = SPM_MIN + 239; // LOG_MAJOR, Error
    int LOG_ERROR_LDAP_USERMACHINERES = SPM_MIN + 240; // LOG_MAJOR, Error
    int LOG_ERROR_LDAP_ENCLOSINGCONTAINERS = SPM_MIN + 241; // LOG_MAJOR, Error
    int LOG_ERROR_LDAP_INIT_FAILED = SPM_MIN + 242; // LOG_MAJOR, Error
    int LOG_ERROR_LDAP_NORESULTDNS = SPM_MIN + 243; // LOG_MAJOR, Error
    int LOG_ERROR_INVALIDCREDENTIALS = SPM_MIN + 244; // LOG_MAJOR, Error
    int LOG_ERROR_LDAP_CONN_NORECS = SPM_MIN + 245; // LOG_MAJOR, Error
    int LOG_ERROR_LDAP_NODOMAINDNS = SPM_MIN + 246; // LOG_MAJOR, Error
    int LOG_RETRY_DATA_SOURCE  = SPM_MIN + 247; // LOG_CRITICAL, Error
    int LOG_REJECTED_DATA_SOURCE  = SPM_MIN + 248; // LOG_CRITICAL, Error

    int LOG_ERROR_INVALID_DOMAIN = SPM_MIN + 249; // LOG_MAJOR, Error
    int LOG_ERROR_DUPLICATE_SERVERANDDOMAIN = SPM_MIN + 250; // LOG_MAJOR, Error
    int LOG_ERROR_USER_AND_MACHINE_NOT_FOUND_AD =  SPM_MIN + 251; // LOG_MAJOR, Error
    int LOG_ERROR_USER_AND_MACHINE_NOT_FOUND_GEN =  SPM_MIN + 252; // LOG_MAJOR, Error
    int LOG_ERROR_LDAP_DOWN = SPM_MIN + 253; // LOG_MAJOR, Error
    int LOG_ERROR_INVALID_PROVISION_DN = SPM_MIN + 254; // LOG_MAJOR, Error
    int LOG_ERROR_LDAP_MRBACONFIG_NULL = SPM_MIN + 255; // LOG_MAJOR, Error    
    int LOG_ERROR_INVALID_POOLSIZE = SPM_MIN + 256; // LOG_MAJOR, Error
    int LOG_ERROR_LDAP_MACHINE_GROUPMEMBERSHIP = SPM_MIN + 257; //LOG_MAJOR Machine not found in LDAP.
    int LOG_ERROR_LDAP_USER_GROUPMEMBERSHIP = SPM_MIN + 258; //LOG_MAJOR User not found in LDAP.
    int LOG_ERROR_LDAP_UNKNOWNHOST_USERDN_NOT_FOUND = SPM_MIN + 259; //LOG_MAJOR host name is not detected at the end point and user DN not resolved.
    int LOG_ERROR_LDAP_UNKNOWNHOST_USERDN_FOUND = SPM_MIN + 260; //LOG_MAJOR host name is not detected at the end point But user DN is resolved.
    int LOG_ERROR_DB_CONNECT = SPM_MIN + 261; // LOG_CRITICAL, SQLException
    int LOG_ERROR_PROCESSING = SPM_MIN + 262; // LOG_MAJOR, reason
    int LOG_ERROR_FORWARDING = SPM_MIN + 263; // LOG_MAJOR, reason
    int LOG_ERROR_QUEUING = SPM_MIN + 264; // LOG_MAJOR, reason
    int LOG_ERROR_CREATING_QUEUE = SPM_MIN + 265; // LOG_CRITICAL, reason
    int LOG_ERROR_PROCESSING_QUEUE = SPM_MIN + 266; // LOG_MAJOR, reason
    int LOG_ERROR_RETRY_SCHEDULED = SPM_MIN + 267; // LOG_MINOR
    int LOG_ERROR_GIVING_UP = SPM_MIN + 268; // LOG_MAJOR
    int LOG_ERROR_DISK_FULL = SPM_MIN + 269; // LOG_MAJOR
    int LOG_REPORT_EXISTS = SPM_MIN + 270; // LOG_INFO
    int LOG_ERROR_BIGDATA_CONNECT = SPM_MIN + 271; // LOG_CRITICAL, SQLException

    // Security Plugin related logs - Authentication type
    int LOG_ERROR_INVALID_AUTHTYPE = SPM_MIN + 274; // LOG_MAJOR, Error

    int LOG_PLUGIN_REPORT_INSERTED_DB = SPM_MIN + 280; // LOG_AUDIT
    int LOG_PLUGIN_REPORT_INSERTED_BIGDATA = SPM_MIN + 281; // LOG_AUDIT
    int LOG_PLUGIN_REPORT_INSERTED_IN = SPM_MIN + 282; // LOG_INFO
    int LOG_PLUGIN_REPORT_INSERTION_DISABLED = SPM_MIN + 283; // LOG_INFO
    int LOG_PLUGIN_REPORT_NOT_INSERTED_DB = SPM_MIN + 284; // LOG_MAJOR
    int LOG_PLUGIN_REPORT_NOT_INSERTED_BIGDATA = SPM_MIN + 285; // LOG_MAJOR
    int LOG_PLUGIN_REPORT_QUEUED_BIGDATA = SPM_MIN + 286; // LOG_AUDIT
    int LOG_PLUGIN_REPORT_NOT_QUEUED_BIGDATA = SPM_MIN + 287; // LOG_MAJOR
    int LOG_PLUGIN_DOWNREPORTS_START_BIGDATA = SPM_MIN + 288; // LOG_INFO
    int LOG_PLUGIN_DOWNREPORTS_STOP_BIGDATA = SPM_MIN + 289; // LOG_INFO
    int LOG_PLUGIN_DOWNREPORTS_SKIP_BIGDATA = SPM_MIN + 290; // LOG_MAJOR
}
