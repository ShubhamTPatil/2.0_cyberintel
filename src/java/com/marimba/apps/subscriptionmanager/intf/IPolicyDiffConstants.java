// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.intf;

/**
 * IPolicyDiffConstants, constants shared by policy diffing operation and approval operation
 *
 * @author Tamilselvan Teivasekamani
 * @version $Revision$,  $Date$
 */

public interface IPolicyDiffConstants {

    String STR_ON = " on ";
    String STR_NULL = "null";
    String STR_NONE = "None";
    String STR_SPACE = " ";
    String STR_UNKNOWN = "Unknown";

    String OPR_ADD = "add";
    String OPR_DELETE = "delete";
    String OPR_UPDATE = "update";
    
    String KEY_OLD_SUB = "oldsub";
    String KEY_NEW_SUB = "newsub";
    String KEY_OLD_VALUE = "oldvalue";
    String KEY_NEW_VALUE = "newvalue";

    String CH_TYPE = "Type";
    String CH_TITLE = "Title";
    String CH_ORDER = "Order";
    String ALL_END_POINTS = "All Endpoints";
    String CH_PRIMARY_STATE = "Primary state";
    String CH_SECONDARY_STATE = "Secondary state";
    String CH_UPDATE_SCHEDULE = "Update schedule";
    String CH_REPAIR_SCHEDULE = "Repair schedule";
    String CH_PRIMARY_SCHEDLUE = "Primary schedule";
    String CH_POSTPONE_SCHEDULE = "Postpone schedule";
    String CH_SECONDARY_SCHEDULE = "Secondary schedule";
    String CH_EXEMPT_BLACKOUT = "Enabled exempt from blackout";
    String CH_WOW_URGENT = "Enabled urgent WOW deployment";
    String CH_WOW_UPDATE = "Enabled WOW deployment for update schedule";
    String CH_WOW_REPAIR = "Enabled WOW deployment for repair schedule";
    String CH_WOW_PRIMARY = "Enabled WOW deployment for primary schedule";
    String CH_WOW_SECONDARY = "Enabled WOW deployment for secondary schedule";
    String KEY_CHANNEL_ATTRIBUTES = "channelAttr";

    // Using in policy approval
    String COMMA_SEPARATOR = ",";
    String PROPERTY_TYPE_TUNER = "tuner";
    String PROPERTY_TYPE_CHANNEL = "channel";
    String NO_STATE = "No state has been assigned";
    String NO_SCHEDULE = "No schedule has been assigned";
    String No_BLACKOUT_SCHEDULE_MSG = "None. Allow downloads anytime";
    String No_BLACKOUT_SCHEDULE = "No Blackout Schedule has been assigned";
    String DEFAULT_ACTIVE_PERIOD = "Activate next time policy service updates";
    

    // Mail related constants    
    // Date format - RFC 822 - refer http://docs.oracle.com/javase/tutorial/i18n/format/simpleDateFormat.html
    String MAIL_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z"; //"E MM/dd/yyyy h:m:ss a";
}
