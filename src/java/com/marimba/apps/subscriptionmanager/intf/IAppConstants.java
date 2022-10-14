// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.intf;

import com.marimba.intf.logs.ILogConstants;
import com.marimba.tools.util.DebugFlag;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Constants that can be used from any part of Subscription Manager. This includes the commandline and the webapplication.
 *
 * @author Angela Saval
 * @version 1.10, 03/26/2002
 */

public interface IAppConstants extends ILogConstants {

    String CONFIG_PREFIX_SUBSCRPTMGR = "subscriptionmanager";
    String CONFIG_PREFIX_SUBSCRPTPLUGIN = "subscriptionplugin";
    String USER_DIR = "users";
    String TXADMIN_FILE = "txadminaccess";
    String TXADMIN_USER_KEY = "txadmin";
    String TXADMIN_PWD_KEY = "txpwd";
    /**
     * LDAP Query collection Paging size
     */
    int DEFAULT_PAGE_SIZE = 100;

   boolean DEBUG = DebugFlag.getDebug("SECURITY/MGR") >= 1;
    boolean DEBUG2 = DebugFlag.getDebug("SECURITY/MGR") >= 2;
    boolean DEBUG3 = DebugFlag.getDebug("SECURITY/MGR") >= 3;
    boolean DEBUG4 = DebugFlag.getDebug("SECURITY/MGR") >= 4;
    boolean DEBUG5 = DebugFlag.getDebug("SECURITY/MGR") >= 5;

    // Mail constants

    String PROP_SMTP_USER = "smtp.user";
    String PROP_SMTP_HOST = "smtp.host";
    String PROP_SMTP_PORT = "smtp.port";
    String PROP_SMTP_PWD = "smtp.password";
    String PROP_USE_AUTH = "smtp.useauth";
    String PROP_SENDER_MAILID = "smtp.sender.email";
    String PROP_SENDER_NAME = "smtp.sender.name";
    String PROP_BCC_MAILID = "smtp.bcc.email";
    String PROP_RECEIVER_MAILIDS = "smtp.receiver.emails";

    String PROP_MAIL_TO = "mail.to";
    String PROP_MAIL_CC = "mail.cc";
    String PROP_MAX_ATTACHMENT_SIZE = "smtp.maxAttachmentSize";
    String PROP_MAX_BODY_SIZE = "smtp.maxBodySize";
    String PROP_ENCRYPTION = "smtp.encryption";
    int EMAIL_DEFAULT_MAX_ATTACHMENT_SIZE = 5;
    int EMAIL_DEFAULT_MAX_BODY_SIZE = 1;
    int EMAIL_DEFAULT_EMAIL_PORT = 25;
    int EMAIL_PORT_MAX_RANGE = 65535;


    String MAIL_SMTP_HOST = "mail.smtp.host";
    String MAIL_SMTP_PORT = "mail.smtp.port";
    String MAIL_SMTP_AUTH = "mail.smtp.auth";
    String MAIL_DEBUG = "mail.debug";
    String TXLIST_CURRENT_USERNAME = "txlist_username";
    String TXLIST_CURRENT_PASSWORD = "txlist_password";

    final static String BOOL_TYPE = "boolean";
    final static String INT_TYPE  = "integer";
    final static String DATE_TYPE = "date";
    final static String TIME_TYPE = "time";
    final static String STR_TYPE  = "string";

    int INPUT_RC_DATESTYLE = java.text.DateFormat.SHORT;
    int INPUT_RC_TIMESTYLE = java.text.DateFormat.SHORT;
    int OUTPUT_RC_DATESTYLE = java.text.DateFormat.MEDIUM;
    int OUTPUT_RC_TIMESTYLE = java.text.DateFormat.FULL;
    int OUTPUT_RC_FTM_TIMESTYLE = java.text.DateFormat.MEDIUM;

    String ARG_NAME_PREFIX  = "an_";
    String ARG_TYPE_PREFIX  = "at_";
    String ARG_NULLABLE_PREFIX = "ax_";
    String ARG_VALUE_PREFIX = "av_";
    String ARG_ID_PREFIX = "aid_";
    static String OLD_TIME_PATTERN= "MM/dd/yyyy hh:mm:ss aa";
    static String OLD_DATE_PATTERN= "MM/dd/yyyy";

    //common datetime patterns (for storing parameter's values)
    static String COMMONDATEPATTERN = "yyyyMMddHHmmss'Z'";
    static String COMMONTIMEPATTERN = "yyyyMMddHHmmss'Z'";
    //if sql query converts date/time to varchar it should use this pattern during conversion
    // and column name should start with IAppConstants.GMTTIME_COL, if it expects RC to
    // format string value based on user's Locale; in MSSQL, style ID = 20 for CONVERT function,
    // on Oracle, format to to_nchar() is 'YYYY-MM-DD HH24:MI:SS'
    static String DBCONVERT_TIMEPATTERN = "yyyy-MM-dd hh:mm:ss";
    static String DBCONVERT_DATEPATTERN = "yyyy-MM-dd";

    final static SimpleDateFormat oldDateFormat = new SimpleDateFormat(OLD_DATE_PATTERN, Locale.US);
    final static SimpleDateFormat oldTimeFormat = new SimpleDateFormat(OLD_TIME_PATTERN, Locale.US);

    //formatter for param. date/time values => 7.0
    final static SimpleDateFormat commonDateFormat = new SimpleDateFormat(COMMONDATEPATTERN);
    final static SimpleDateFormat commonTimeFormat = new SimpleDateFormat(COMMONTIMEPATTERN);

    //formatter for db string values that need to be treated as date/time
    final static SimpleDateFormat commonDBDateFormat = new SimpleDateFormat(DBCONVERT_DATEPATTERN);
    final static SimpleDateFormat commonDBTimeFormat = new SimpleDateFormat(DBCONVERT_TIMEPATTERN);


    public final static int SAVE_OPER = 0; // ArgBean is constructed to save to database
    public final static int READ_OPER  = 1; // ArgBean is constructed to read from database

    // for view
    String KEY_TYPE      = "type";
    String KEY_PAGE_TYPE = "pageType";
    String KEY_NAME      = "name";
    String KEY_FOLDER    = "folder";
    String KEY_SQL       = "sql";
    String KEY_DESC      = "desc";
    String KEY_HIDE      = "hide";
    String KEY_PAGE      = "page";
    String KEY_PAGESIZE  = "pageSize";
    String KEY_ACTION    = "action";
    String KEY_NARROWNUM = "numNarrow";
    String KEY_SORT      = "sort";
    String KEY_SORT_TYPE = "sortType";
    String KEY_SELECTS   = "selects";
    String KEY_RESULTSET = "set";
    String KEY_ARGNUM    = "numArg";

    String TYPE_FORM_QUERY    = "f";
}
