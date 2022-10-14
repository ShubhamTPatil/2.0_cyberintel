// Copyright 2018, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.intf;
import com.marimba.intf.msf.wakeonwan.IWoWConstants;
/**
 * Debug interface.  This interface centrally turn on or off debugging for the package.
 *
 * @author Theen-Theen Tan
 * @version 1.78, 01/06/2003
 */

public interface IWebAppConstants extends IAppConstants, IErrorConstants {

    public static final  String ACTION_CREATE = "create";
    public static final  String ACTION_EDIT = "edit";
    public static final  String ACTION_SAVE = "save";
    public static final  String ACTION_DELETE = "delete";
    public static final  String ACTION_ADD = "add";
    public static final  String ACTION_REMOVE = "remove";
    public static final  String ACTION_PREVIEW = "preview";
    public static final  String ACTION_REMOVE_SELECTED = "remove_selected";
    public static final  String ACTION_REMOVE_EXCLUDED = "remove_excluded";
    public static final  String ACTION_MOVE_TO_EXCLUDED = "move_to_excluded";
    public static final  String ACTION_MOVE_TO_SELECTED = "move_to_selected";
    public static final  String ACTION_OK = "ok";
    public static final  String ACTION_CANCEL = "cancel";
    public static final  String ACTION_MOVE_UP = "move_up";
    public static final  String ACTION_MOVE_DOWN = "move_down";
    public static final  String ACTION_MOVE_TO_TOP = "move_to_top";
    public static final  String ACTION_MOVE_TO_BOTTOM = "move_to_bottom";
    public static final  String ACTION_SET_STARTING_PRIORITY = "set_starting_priority";
    public static final  String ACTION_MODIFY_EXISTING_PRIORITY = "modify_exising_priority";
    public static final  String ACTION_REVERT_PRIORITY = "revert_exising_priority";
    public static final  int    CONNECT_SUCCESS = 1;
    public static final  int    FAILED_TO_CONNECT = 2;
    public static final  int    FAILED_PASSWORD = 3;
    public static final  int    FAILED_NO_SUBCONFIG = 4;
    public static final  int    FAILED_SEARCHFAILED_CONFIG = 5;
    public static final  int    FAILED_WRONGPORT_FORGC = 6;
    public static final  int    FAILED_NOPORT_FORGC = 7;
    public static final  int    FAILED_BASEDN  = 8;
    public static final  int    FAILED_UNKNOWN = 9;
    public static final  String TEST_SUCCESS = "Success";
    public static final  String TEST_FAILED = "Failed";
    public static final  String FAILURE_DETAIL = "failure_detail";
    public static final  String TEST_RESULT = "host_test_results";
    public static final  String SESSION_TARGETSELECT = "session_targetselect";
    public static final  String SESSION_MTARGETSELECT = "session_mtargetselect";
    public static final  String SESSION_LDAP = "session_ldap";
    public static final  String SESSION_PAGE = "session_page";
    public static final  String SESSION_RETURN_PAGE = "session_return_page";
    public static final  String SESSION_RETURN_PAGETYPE = "session_return_pagetype";
    public static final  String TARGET_VIEW = "target_view";
    public static final  String PKG_VIEW = "pkg_view";
    public static final  String SESSION_TLOGINBEAN = "session_tloginbean";
    public static final  String SESSION_EDITTRANSLOGIN = "session_edittranslogin";
    public static final  String SESSION_LONGTASK = "session_longtask";
    public static final  String SESSION_LONGTASKEXC = "session_longtaskexc";
    public static final  String SESSION_WAITFORMARKER = "session_waitForAction";
    public static final  String SESSION_CHANGE_REQUEST_MSG = "session_changeRequestMsg";
    public static final  String SUBSCRIPTION_PATH = "/spm";

    // Session var to hold selected packages
    public static final  String PAGE_PKGS = "page_pkgs";

    // Session var to hold deployed packages listing
    public static final  String PAGE_PKGS_DEP_RS = "page_pkgs_dep_rs";

    // Session var to hold deployed packages listing
    public static final  String PAGE_PKGS_DEP_SEARCH = "page_pkgs_dep_search";

    // Session var to hold selected targets
    public static final  String MAIN_PAGE_M_TARGETS = "main_page_m_targets";
    public static final  String MAIN_PAGE_TARGET = "main_page_target";
    public static final  String MAIN_PAGE_M_PKGS = "main_page_m_packages";
    public static final  String MAIN_PAGE_PACKAGE = "main_page_package";
    public static final  String PAGE_SCHEDBEAN = "page_schedulebean";
    // Session var to hold selected pending targets
    public static final  String PENDING_POLICY_SAMEUSER = "pending_policy_sameuser";
    public static final  String PENDING_POLICY_DIFFUSER = "pending_policy_differentuser";
    // Session var to hold deployed packages listing
    public static final  String PAGE_TCHPROPS_SUB = "page_tchprops_sub";
    public static final  String PAGE_TCHPROPS_SUB_COPY = "page_tchprops_sub_copy";
    public static final  String PAGE_SHOW_UCD_BUTTON = "page_show_ucd_button";
    public static final  String PAGE_SHOW_REGISTER_DEVICE = "page_show_reg_device";
    public static final String SELECTED_TARGET_ID = "selected_target_id";
    public static final String SELECTED_TARGET_NAME = "selected_target_name";
    public static final String SELECTED_TARGET_TYPE = "selected_target_type";
    // this holds a vector containing a string key=val representation of
    // all the properties in this subscription.
    public static final  String PAGE_TCHPROPS_TPROPSKEYS = "page_tchprops_tpropskeys";
    public static final  String PAGE_TCHPROPS_SPROPSKEYS = "page_tchprops_spropskeys";
    public static final  String PAGE_TCHPROPS_CHPROPSKEYS = "page_tchprops_chpropskeys";
    public static final  String PAGE_TCHPROPS_ALLCHSPROPSKEYS = "page_tchprops_allchspropskeys";
    public static final  String PAGE_TCHPROPS_CHANNELS = "page_tchprops_channels";

    // constant to indicate that the user is paging through the target result set in pkg view
    public static final  String TARGET_PAGING = "target_paging";

    //Constant used for the specifying the type of the tuner property
    public static final  String TCHPROPS_TUNERPROPTYPE = "tunerproptype";

    //Session variables for transmitter credentials
    public static final  String PAGE_TRANSLOGIN_SUB = "page_translogin_sub";
    public static final  String MARIMBA_KEYCHAIN = "marimba.keychain";

    // Session var to know whether in multiple select mode for targets or not
    public static final  String SESSION_MULTITGBOOL = "session_multitgbool";
    public static final  String ADD_REMOVE_SELECTED_PAGE_TARGETS = "add_remove_selected_page_targets";
    public static final  String ADD_REMOVE_EXCLUDED_PAGE_TARGETS = "add_remove_excluded_page_targets";

    // for the paging
    // Remind mlin: move this to user object
    public static final  int DEFAULT_COUNT_PER_PAGE = 50;
    public static final  int DEFAULT_GENPAGING_COUNT_PER_PAGE = 20;

    // session var for holding generic paging results to be displayed
    public static final  String SESSION_DISPLAY_RS = "display_rs";

    // generic paging bean for add/remove packages tx
    public static final  String TXLIST_BEAN = "txlist_bean";
    public static final  String DEPLIST_BEAN = "deplist_bean";
    public static final  String FORM_TXURL = "form_txurl";

    public static final  String TARGET_PKGS_BEAN = "target_pkgs_bean";

    // generic paging bean for package listing
    public static final  String PKGLIST_BEAN = "pkglist_bean";
    public static final  String TGS_FROMPKGSLIST_BEAN = "pkg_tglist_bean";
    public static final  String GEN_PAGE_BEAN = "gen_page_bean";

    // Session var to hold results to be displayed.  This is used with the LDAP paging.
    public static final  String PAGE_TARGETS_RS = "page_targets_rs";

    // Session var to hold temp results for generic paging
    public static final  String PAGE_GEN_RS = "page_gen_rs";

    // Session variables used by the target details area for multiple or single
    public static final  String SESSION_PKGS_FROMTGS_RS = "page_pkgs_fromtgs_rs";
    public static final  String SESSION_PKGS_TODELETE = "page_pkgs_todelete";
    public static final  String SESSION_TGS_TODELETE = "page_tgs_todelete";
    public static final  String SESSION_DISPLAY_DELETE_PROPS = "display_delete_props";
    public static final  String PAGE_PKGS_DELETEALL = "page_pkgs_delete_all";
    public static final  String SESSION_PKGS_FROMTGS_SELECTED = "page_pkgs_fromtgs_selected";
    public static final String SESSION_OSM_TEMPLATE_RESULT = "os_templateInfo";
    public static final  String SESSION_PKGS_FROMTGS_PREFIX = "pkgresult_";
    public static final  String SESSION_SHOWURL_SINGLE = "showurl_single";
    public static final  String SESSION_SHOWURL_MULTI = "showurl_multi";
    public static final  String SESSION_TGS_TOPUSH = "page_tgs_topush";
    // Variable that stores the TargetPackageCompliance map with keys.
    public static final  String SESSION_TGT_PKG_COMPMAP = "tgtpkgcmp_Map";

    //Session variables used by the package details area for multiple or single
    public static final  String SESSION_TGS_FROMPKGS_RS = "page_targets_frompkgs_rs";
    public static final  String SESSION_MULTIPKGBOOL = "session_multipkgbool";
    public static final  String SESSION_TGS_FROMPKGS_SELECTED = "page_targets_frompkgs_selected";

    // Session variable used for persistifying checked values across pages
    public static final  String SESSION_PERSIST_ALL_PREV = "session_page_all_prev";
    public static final  String SESSION_PERSIST_SELECTED = "session_page_selected";
    public static final  String SESSION_PERSIST_PREFIX = "session_page_prefix";
    public static final  String SESSION_PERSIST_BEANNAME = "session_page_beanname";
    public static final  String SESSION_PERSIST_RESETRESULTS = "session_page_resetresults";

    // Session variable that stores the SM's user object
    public static final  String SESSION_SMUSER = "session_smuser";
    public static final  String SESSION_TENANTNAME = "session_tenantname";

    // Session variable that stores the SM's user object
    public static final  String SESSION_PAGINGTYPE = "session_pagingtype";

    // Policy Compliance 6.5
    public static final String POLICIES_FORPKGNAME = "policies_forpkgname";
    public static final String PACKAGE_DETAILS_FORPKG = "package_details_forpkg";
    public static final String SESSION_PACKAGE_POLICIES = "session_details";

    public static final String COMPRPT_CACHERPTS = "cached_rpts";
    public static final String COMPRPT_QUEUERPTS = "queue_rpts";

    public static final String POLICIES_FORTGTNAME = "policies_fortgtname";
    public static final String POLICIES_DETAILS_FORTGT = "policy_details_fortgt";
    public static final String SESSION_POLICIES_DETAILS = "session_policy_details";
    public static final String SESSION_POLICY_LASTUPDATED = "session_policy_lastupdated";
    public static final String SESSION_COMP_LASTCALCULATED = "session_comp_lastcalc";
    public static final String SESSION_COMP_HASPOLICIES = "has_policies";
    public static final String POLICY_FROMPKG_BEAN = "pkg_policy_bean";
    public static final String COMP_SUMMARY_RESULT = "comp_sum_result";
    public static final String PWR_SUMMARY_RESULT = "power_sum_result";
    public static final String OSM_SUMMARY_RESULT = "osm_sum_result";

    // Session variable used for persistifying checked values across pages
    public static final  String SESSION_PC_TARGET = "session_pc_target";
    public static final  String SESSION_PC_COMP = "session_pc_comp";
    public static final  String SESSION_PC_COMPPKGS = "session_pc_comppkgs";
    public static final  String SESSION_PC_PAGEBEAN = "session_pc_pagebean";
    public static final  String SESSION_PC_COMPSTATUS = "session_pc_compstatus";
    public static final  String SESSION_PC_COMPSTATUS_FILTER = "session_pc_compstatus_filter";
    public static final  String SESSION_PC_TARGTPKGCOMP = "session_pc_target_pkg_comp";
    public static final  String PC_DETAILS_TYPE_OVERALL = "overall";
    public static final  String PC_DETAILS_TYPE_PKG = "pkg";
    public static final  String PC_STATUS_SUCCEEDED = "succeeded";
    public static final  String PC_STATUS_FAILED = "failed";
    public static final  String PC_STATUS_NOTCHECKEDIN = "notcheckedin";
    public static final  String PC_STATUS_PAGEBEAN = "compliance_status_pagebean";
    public static final  String PC_DETAILS_COMPLIANCETYPE = "complianceType";
    public static final  String PC_DETAILS_SHOWNUMBERS = "showNumber";
    public static final String PC_REFRESHACTION ="refreshaction";
    public static final String PC_CACHEACTION ="cacheaction";
    public static final  String REQUEST_STATUS = "status";
    public static final  String REQUEST_TYPE = "type";
    public static final  String REQUEST_IDX = "idx";
    public static final String REQUEST_PACKAGE = "pkgname";
    public static final String REQUEST_CALLER = "caller";
    public static final String REQUEST_KEY = "key";
    public static final String REQUEST_START = "start";
    public static final String REQUEST_RANGE = "range";
    public static final String SESSION_NOTCHECKEDIN = "notcheckedin";
    public static final String REQUEST_DOACTION = "doaction";
    public static final String REQUEST_ROWFILTER = "rowfilter";
    public static final String REQUEST_FILTERVALUE = "filtervalue";
    public static final String REQUEST_LIST = "list";
    public static final String REQUEST_RESULTS = "results";
    public static final String REQUEST_STATUSENDPOINT = "statusendpoint";

    public static final  String SESSION_TGTS_FROM_PKGS="session_tgts_frm_Pkgs";

    // Session valirable used in distribution assignment page
    public static final  String SESSION_DIST = "session_dist";
    public static final  String SESSION_DIST_PAGEPKGS_BEAN = "session_dist_pagepkgs_bean";
    public static final  String SESSION_DIST_PAGEPKGS_SELECTED = "session_dist_pagepkgs_selected";
    public static final  String SESSION_DIST_PAGEPKGS_PREFIX = "dist_pagepkgs_item_";
    public static final  String SESSION_DIST_MASSEDIT_PREFIX = "dist_mass_edit";
    public static final  String SESSION_DIST_PAGEPKGS_RS = "session_dist_pagepkgs_rs";
    public static final  String SESSION_DIST_PAGEPKGS_DISPLAYRS = "session_dist_pagepkgs_displayrs";
    public static final  String SESSION_DIST_PAGEPKGS_PREORDER = "session_preorder_state";
    public static final  String SESSION_SELECT_PACKAGES="session_dist_changed_pkgs";

    //session varaible for Edit Target List
    public static final String ADD_REMOVE_TARGET = "add_remove_target";
    public static final String ADD_REMOVE_PACKAGE = "add_remove_package";
    public static final String ADD_REMOVE_PATCH = "add_remove_patch";
    public static final String SESSION_ADD_PAGEPKGS_SELECTED = "session_add_pagepkgs_selected";
    public static final String SESSION_ADD_PAGEPKGS_PREFIX = "add_pagepkgs_item_";
    public static final String SESSION_ADD_PAGEPKGS_BEAN = "session_add_pagepkgs_bean";
    public static final String SESSION_ALL = "add_selected_all";

    public static final String SESSION_ADD_PAGEPATCH_SELECTED = "session_add_pagepatch_selected";
    public static final String SESSION_ADD_PAGEPATCH_PREFIX = "add_pagepatch_item_";
    public static final String SESSION_ADD_PAGEPATCH_BEAN = "session_add_pagepatch_bean";


    //Session varaible for copy operation
    public static final String COPY_RHS_LIST = "target_rhs_list";
    public static final String SESSION_COPY = "session_copy";

    // DistributionBean variable to store whether we are editing an assignment
    // or creating a new policy assignment
    public static final  String NEW = "type_new";
    public static final  String EDIT = "type_edit";

    // prefix to be used for checkboxes before targets on forms
    public static final  String TGRESULT_PREFIX = "tgresult_";

    // session var to hold bread crumb
    public static final  String PAGE_BREADCRUMB = "page_breadcrumb";

    //Constants used by tags
    //used by the ldap navigation tag
    public static final  String SEARCH = "search";

    //used by the target details tag for single and multiple mode (GetPkgsFromTargetsTag)
    public static final  String SORT_TITLE = "title";
    public static final  String SORT_STATE = "state";
    public static final  String AM = "AM";
    public static final  String PM = "PM";

    //used for Install Priority page
    public static final  String       PAGE_PRIORITY_CHANNELS = "priority_channels";
    public static final  String       PAGE_PRIORITY_SUBSCRIPTION = "priority_subscription";
    public static final   String HOME = "Home";
    public static final   String TYPE_HOME = "type_home";
    public static final   String INTRO_SHORT = "IntroShort";
    public static final   String INTRO_LONG = "IntroLong";
    public static final   String TGDETAILS_SINGLE = "tgDetailsSingle";
    public static final   String TGDETAILS_MULTI = "tgDetailsMulti";

    // these constants are used in LDAPRemeberAction- as indicators
    // for which ldap browsing action to use- LDAPBrowseOUAction or LDAPBrowseEPAction
    public static final   String ENTRYPOINT_PREFIX = "ep|";
    public static final   String OU_PREFIX = "ou|";
    public static final   String GROUP_PREFIX = "gr|";
    public static final   String PAGE_PREFIX = "pg|";

    // used to indicate an invalid collection in the GUI
    // for example an mrba collection object with more than one member
    // attribute, or a member attribute that is not a group of names
    public static final   String INVALID_COLLECTION = "invalid_coll";
    public static final   String INVALID_CLASS = "invalid_class";
    public static final   String INVALID_NONEXIST = "invalid_nonexist";
    public static final   String INVALID_LINK = "invalid_link";
    public static final   String MAINTAININITSCHED = "maintainInitSchedInc";
    public static final   String MAINTAINSECSCHED = "maintainSecSchedInc";
    public static final   String MAINTAINUPDATESCHED = "maintainUpdateSchedInc";
    public static final   String MAINTAINVERREPAIRSCHED = "maintainVerRepairSchedInc";
    public static final   String APP_PROC_MEMBER = "app_proc_member";
    public static final   String APP_PROC_TXMEMBER = "app_proc_txmember";
    public static final   String APP_PROC_SITE_MEMBER = "app_proc_site_member";
    public static final   String APP_PROC_MDM_DEVICEGROUP_MEMBER = "app_proc_mdm_devicegroup_member";
    public static final   String APP_PROC_MDM_DEVICE_MEMBER = "app_proc_mdm_device_member";

    // session attribute name for list of principals
    String PRINCIPAL_LIST = "principalList";
    // session attribute name
    String PRINCIPAL_VIEW = "principalAcl";
    String ACL_RHS_LIST = "aclRhsList";
    String ACL_LHS_ITEM = "aclLhsItem";

    String SESSION_PATCH_PATCHES = "session_patch_patches";
    String REQUEST_PATCH_GROUPURL =  "patchGroupUrl";
    

    // session attribute to store the push Deployment id for the session
    public static final   String PUSH_TARGET_NAME_TYPE_SUFFIX = ".nametype";
    public static final   String PUSH_DELIMITER = "|";
    public static final   String LAST_DEPLOYMENT_ID="lastdeployment";
    public static final   String LAST_DEPLOYMENT_DN = "lastdeployment.dn";
    public static final   String LAST_DEPLOYMENT_NAMETYPE = LAST_DEPLOYMENT_ID + PUSH_TARGET_NAME_TYPE_SUFFIX;
    public static final   String PUSH_DEPLOYMENT_FILE = "pushdeployments.txt";  // filename which contains the deployments to targets mapping
    public static final   String CURRENT_PUSH_DEPLOYMENT= "current_push_deployment"; // variable to contain the last DM deployment ID
    public static final   String CURRENT_PUSH_DN_CODE= "current_push_dn_code";  // variable to contain the last SPM group
    // in the last SPM target view.  It will be
    // hash code of all the dn's used to specify the target
    public static final String PUSH_DEPLOYMENTBEAN = "deploymentbean";
    public static final String PUSH_ALL_POLICIES   = "all_policies";
    public static final String PUSH_ENABLED_CONFIG   = "subscriptionmanager.push.enabled";
    public static final String PUSH_ENABLED_GUI  = "pushEnabled";
    public static final String COMPUTER_UNASSIGN_OTHER_GP_ENABLED = "subscriptionmanager.unassign.computer.enabled";
    public static final String OLD_ENABLE_WOW_FEATURE = "subscriptionmanager.wow.enable";
    public static final String ENABLE_WOW_FEATURE = IWoWConstants.ENABLE_WOW_FEATURE;
    public static final String ENABLE_MDM_FEATURE = "mdm.feature.enabled";
    public static final String SITE_BASED_MASTER_TX_URL = "subscriptionmanager.sitebaseddeployment.mastertxurl";
    public static final String ENABLE_SITE_BASED_DEPLOYMENT = "subscriptionmanager.sitebaseddeployment.enabled"; //marimba.subscription.sitebaseddeployment.enabled
    public static final String ENABLE_USER_CONTROLLED_DEPLOYMENT = "marimba.subscription.usercontrolled.enabled";
    public static final String ENABLE_AUTOSCAN_FEATURE = "marimba.subscription.autoscan.enabled";
    public static final String ENABLE_USER_CENTRIC_DEPLOYMENT = "marimba.subscription.ucd.enabled";
    public static final String ENABLE_POLICY_START_LOCATION_ENABLED = "subscriptionmanager.startlocation.enabled";
    public static final String ENABLE_POLICY_START_LOCATION_PATH = "subscriptionmanager.startlocation.path";

    String MAX_UCD_DEVICELEVEL = "subscriptionmanager.ucd.maxdevicelevel";
    String DEFAULT_MAX_DEVICELEVEL = "2";

    // E-mail settings
    String EMAIL_SETTINGS = "subscriptionmanager.email.enabled";
    String EMAIL_TO_ADDRESS = "subscriptionmanager.email.addresses";
    String PEER_APPROVAL_SETTINGS = "subscriptionmanager.peerapproval.enabled";
    String PEER_APPROVAL_TYPE = "subscriptionmanager.peerapproval.type";
    String PEER_APPROVAL_TO_ADDRESS = "subscriptionmanager.peerapproval.groups";
    String TUNER_PROP_POLICY_PEERAPPROVAL_SERVICEAUTOMATION_ENABLED = "marimba.serviceautomation.policy.peerapproval.enabled";
    String TUNER_PROP_POLICY_PEERAPPROVAL_SERVICEAUTOMATION_INTEGRATION = "marimba.serviceautomation.policy.peerapproval.integration";

    public static final   String PERFORMANCE_SCRUBBERON = "scrubberOn";
    public static final String QUERYMANAGER = "querymanager";
    public static final String IS_DATA_AVAILABLE = "dataAvailable";
    public static final String CACHELASTUPDATEDTIME = "cacheLastUpdatedTime";
    public static final String TARGETLASTSCANNEDTIME = "targetLastScannedTime";
    // Display date in the format  Wed, 4 Jul 2001 12:08:56
    public static final String DATE_FORMAT_PATTERN = "MM.dd.yyyy 'at' hh:mm:ss a zzz";

    public static final String SWAPFILE_DIRECTORY = "swapfiles";
    public static final String COMPLIANCE_SWAPFILE = "complianceswapfile";
    public static final String COMPLIANCE_SWAPFILE_EXT = ".dat";
    public static final String NO_ACL_PERMISSION = "noaclpermission";

    //Acl turn on/off identifiers
    String ACL_STATUS = "aclStatus";

    public static final String ATTR_LOGIN_RETURN_PAGE  =  "com.marimba.servlet.login.returnPage";
    public static final String ATTR_LOGIN_SAVED_PAGE   =  "com.marimba.subcsription.login.requestedpage";

    //Date format constants
    public static final String MRBA_TIMEFORMAT =  "hh:mma";
    String TUNER_PROP_MAR_SCH_FILTER = "marimba.schedule.filter";
    String TUNER_PROP_UPDATE_SCH = "update.schedule";
    String TUNER_PROP_REBOOT_SCH = "marimba.reboot.schedule.at";
    String TUNER_PROP_VALUE_NEVER = "never";

    //Not applicable
    String NOTAPP = "N/A";

    //Initalize Empirum Module Main
    public static final String EMPIRUM_APP_MAIN = "empirumAppMain";
    public static final String SESS_MACHINES_LIST = "machineslist";
    public static final String SESS_ORGINIAL_GROUP_INFO = "originalGroupInfo";
    public static final String SESS_DUPLICATE_GROUP_INFO = "duplicateGroupInfo";
    public static final String SESS_DIFF_GROUP_INFO = "diffGroupInfo";
    public static final String SESS_MACHINES_REPORT = "machinesListReport";
    public static final String SESS_MACHINE_EMPIRUM_DEPOT = "machineEmpirumDepot";
    public static final String SESS_MACHINE_BACKUP_STATUS = "machineBackupStatus";
    public static final String SESS_OS_PROVISIONING_ASSIGN_REPORT = "OSProvisioningAssignReport";

    // Multi-Tenancy constants
    public static final String TENANT_ATTRUBUTES = "tenantAttributes";
    public static final String DIRECTORY_TYPE = "directorytype";

    //
    String POWER_TEMPLATES = "power_templates";
    String DESKTOP_SECURITY_TEMPLATES = "desktop_security_templates";

    String MDM_TYPE_POLICY = "policy";
    String MDM_TYPE_DEVICE = "device";
    String MDM_TYPE_GROUP = "group";
    String MDM_TYPE_DEVICEGROUP = "devicegroup";
    String SESS_MDM_ADDED_DEVICE = "added_device";
    String SESS_MDM_REMOVED_DEVICE = "removed_device";
    String SESS_MDM_ADDED_POLICY = "added_policy";
    String SESS_MDM_REMOVED_POLICY = "removed_policy";
    String SESS_MDM_ASSINGEDPOLICY = "assigned_policy";
    String SESS_MDM_ASSINGEDDEVICE = "assigned_device";
    String SESS_MDM_ASSINGEDGROUP = "assigned_group";
    String SESS_MDM_AVAILABLEPOLICY = "available_policy";
    String SESS_MDM_AVAILABLEDEVICE = "available_device";
    String SESS_MDM_AVAILABLEGROUP = "available_group";
    String SESS_MDM_ORGIONAL_AVAILABLE_DEVICE = "orgional_device";
    String SESS_MDM_ORGIONAL_AVAILABLE_POLICY = "orgional_policy";
    String SESS_MDM_ORGIONAL_AVAILABLE_GROUP = "orgional_group";
    String SESS_MDM_ORGIONAL_ASS_DEVICE = "orgional_assinged _device";
    String SESS_MDM_ORGIONAL_ASS_POLICY = "orgional_assinged_policy";
    String MDM_ASSGINED_POLICY = "marimba.assigned.policy";
    String MDM_ASSGINED_GROUP = "marimba.assigned.group";
    String MDM_ASSGINED_DEVICE = "marimba.assigned.device";
    String MDM_SELECTED_TARGET_POLICY = "target_assigned_policy";

    String SECURITY_SCAN_TYPE_XCCDF  = "xccdf";
    String SECURITY_SCAN_TYPE_OVAL = "oval";

}
