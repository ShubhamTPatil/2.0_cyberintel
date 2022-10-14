// Copyright 1997-2011, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.intf;

import com.marimba.apps.subscription.common.intf.ICommonErrorConstants;

/**
 * Interface which should contain the error constants used by the Subscription Manager
 *
 * @author Angela Saval
 * @author Theen-Theen Tan
 * @version 1.57, 03/20/2003
 */
public interface IErrorConstants
        extends ICommonErrorConstants {
    /* Error constants used for when unable to access the transmitter for sourcing
     * users from transmitter.
     */
    public static final String  USETX_CANNOT_OBTAIN_RPC = "error.usetx.cannot.obtain.rpc";
    public static final String  USETX_NO_PLUGIN_URL = "error.usetx.no.plugin.url";
    public static final String  USETX_INVALID_PLUGIN_URL = "error.usetx.invalid.plugin.url";
    public static final String  USETX_UNKNOWN_HOST_RPC_PORT = "error.usetx.unknownhost.rpc.port";
    public static final String  USETX_LOST_CONNECTION_RPC_TX = "error.lost.connection.rpc.tx";
    public static final String  USETX_INVALID_RPC_PORT_INT = "error.usetx.invalid.rpc.port.int";
    public static final String  USETX_CONNECT_RPC_SSL = "error.usetx.connect.rpc.ssl";
    public static final String  USETX_NO_TRANSMITTER_INWORKSPACE = "error.usetx.no.transmitter.inworkspace";
    public static final String  USETX_OLD_TRANSMITTER = "error.usetx.old.transmitter";
    public static final String  USETX_AUTH_TRANSACCESS_FAILED_DIGEST = "error.usetx.auth.transaccess.failed.digest";
    public static final String  USETX_AUTH_TXADMIN_FAILED_DIGEST = "error.usetx.auth.txadmin.failed.digest";
    public static final String  USETX_AUTH_TRANSACCESS_FAILED_SIMPLE = "error.usetx.auth.transaccess.failed.simple";
    public static final String  USETX_AUTH_TXADMIN_FAILED_SIMPLE = "error.usetx.auth.txadmin.failed.simple";
    public static final String  USETX_CANTSAVEPROPS = "error.usetx.cantsaveprops";

    /*
     * These are the error message strings thrown from the RPC when a connection
     * cannot be made.
     * @see com.marimba.rpc.RPC#doConnect
     */
    public static final String  RPC_SSLCONNECTIONS_NOTAVAIL = "SSL connections are not available";
    public static final String  RPC_URL_FAILURE = "URL failure:";
    public static final String  USETX_CONNECT_RPC_MALFORMEDURL = "internalerror.usetx.connect.rpc.malformedurl";
    public static final String  USETX_INVALID_RPC_URL_REQUEST = "internalerror.usetx.invalid.rpc.url.request";
    public static final String  USETX_CONNECT_RPC_IO = "internalerror.connect.rpc.io";
    public static final String  USETX_AUTH_TRANSACCESS_WRONGPATH_ADMIN = "internalerror.auth.transaccess.wrongpath.admin";
    public static final String  USETX_TXADMINPROPS_NOINIT = "internalerror.usetx.txadminprops.noinit";
    public static final String  USETX_NOUSERNAME_CONNECT = "internalerror.usetx.nousername.connect";
    public static final String  USETX_NOPASSWORD_CONNECT = "internalerror.usetx.nopassword.connect";

    /* Error constants for operations used by the actions in the GUI and commandline
     */
    public static final String  PUBLISH_INVALIDSUBSVCURL = "error.comp.publish.InvalidSubSvcURL";
    public static final String  PUBLISH_FAILEDPUBLISH = "error.comp.publish.FailedPublish";
    public static final String  PUBLISH_FAILEDPUBLISH_NORESULT = "error.comp.publish.FailedPublishNoResult";
    public static final String  PUBLISH_NULLLDAPHOST = "error.comp.publish.NullLDAPHost";
    public static final String  PUBLISH_CANTSAVEPROPS = "error.comp.publish.CantSaveProps";
    public static final String  PUBLISH_INTERNAL_CANTCREATEPUB = "internalerror.comp.publish.CantCreatePub";
    public static final String  PUBLISH_INTERNAL_CANTSETSECURE = "internalerror.comp.publish.CantSetSecure";

    /* Error constants used for input validation.  These can be used from the
     * commandline or GUI.  Please see
     * com.marimba.apps.subscriptionmanager.webapp.util.ValidationUtil for use
     * of these constants
     */
    public static final String  VALIDATION_INVALIDCHARINHOST = "error.input.invalidCharInHost";
    public static final String  VALIDATION_INVALIDURL = "error.input.invalidURL";
    public static final String  VALIDATION_INVALIDPORT = "error.input.invalidPort";
    public static final String  VALIDATION_POOLSIZENONINT = "error.input.poolSizeNonInt";
    public static final String  VALIDATION_FIELD_REQUIRED = "error.input.required";
    public static final String  VALIDATION_FIELD_INTEGER = "error.input.integer";
    public static final String  VALIDATION_FIELD_INTEGER_RANGE = "error.input.integer.range";
    public static final String  VALIDATION_INTERNAL_WRONGARG = "internalerror.input.wrongArguments";
    public static final String  VALIDATION_SCHEDULE_STARTBEFOREACTIVATE= "error.input.schedule.startbeforeactivate";
    public static final String  VALIDATION_SCHEDULE_REBOOT= "error.input.schedule.reboot";
    public static final String  VALIDATION_SCHEDULE_STARTAFTEREXPIRE = "error.input.schedule.startafterexpire";
    public static final String  VALIDATION_SCHEDULE_DAYSOFWEEK = "error.input.schedule.daysofweek";
    public static final String  VALIDATION_SCHEDULE_BETWEEN_INTERVAL = "error.input.schedule.betweeninterval";
    public static final String  SYSTEM_INTERNAL_EXCEPTION = "internalerror.system.exception";
    public static final String  GUIUTILS_INTERNAL_WRONGARG = "internalerror.guiutils.wrongArguments";
    public static final String  GUIUTILS_INTERNAL_SMUSERNOTFOUND = "internalerror.guiutils.usernotfound";
    public static final String  VALIDATION_INVALIDBINDDN = "error.input.invalidBindDn";
    public static final String  VALIDATION_INVALIDBASEDN = "error.input.invalidBaseDn";
    public static final String  VALIDATION_INVALIDSCHED = "error.input.schedule.string";
    public static final String VALIDATION_INVALID_TARGETDN = "error.input.invalidtargetdn";

    // Add/Remove Package pages
    public static final String  PKG_INTERNAL_ADD_PATHNOTFOUND = "internalerror.package.add.pathnotfound";
    public static final String  PKG_INTERNAL_ADD_TXNOTFOUND = "internalerror.package.add.txnotfound";
    public static final String  PKG_INTERNAL_ADDDEP_RSNOTFOUND = "internalerror.package.adddep.resultnotfound";
    public static final String  PKG_INTERNAL_REMOVE_NOTFOUND = "internalerror.package.remove.notfound";
    public static final String  PKG_INTERNAL_PKGSNOTFOUND = "internalerror.package.pkgsnotfound";
    public static final String  PKG_ADD_PKGEXIST = "error.package.add.pkgexist";
    public static final String  PKG_ADD_TARGET_SIMILAR_PATH_ERROR = "error.package.add.pkg.target.patherror";
    public static final String  PKG_ADD_TARGET_SPLCHAR = "error.package.add.splchar";
    public static final String  PKG_ADD_GROUP_SIMILAR_PATH_ERROR = "error.package.add.pkg.group.patherror";
    public static final String  PKG_ADD_ERROR = "error.package.add.exception";
    public static final String  PKG_EDIT_ERROR = "error.package.edit.exception";
    public static final String  PKG_REMOVE_ERROR = "error.package.remove.exception";
    public static final String  PKG_SAVE_ERROR = "error.package.save.exception";
    public static final String  DIST_SAVE_ERROR = "error.dist.save.exception";
    public static final String  DIST_EDIT_ERROR = "error.dist.edit.exception";
    public static final String  DIST_DELETE_ERROR = "error.dist.delete.exception";
    public static final String  DIST_SAVE_NOTARGETS = "error.dist.save.notargets";
    public static final String DIST_SAVE_TARGET_NOTFOUND = "error.sub.ldap.resolvetargetdn";
    public static final String  DIST_SAVE_NOCHANNELS = "error.dist.save.nochannels";
    public static final String  DIST_SAVE_NOSECSCHED = "error.dist.save.nosecsched";
    public static final String  DIST_SAVE_NOSECSTATES = "error.dist.save.nosecstates";
    public static final String  DIST_SAVE_NEWTARGETANDINCSTATES = "error.dist.save.newtargetandincstates";
    public static final String  DIST_SAVE_NEWTARGETANDINCSCHEDS = "error.dist.save.newtargetandincscheds";
    public static final String  DIST_SAVE_INVALIDSECSTATES = "error.dist.save.invalidsecstates";
    public static final String  DIST_DELETE_NOSUB = "error.dist.delete.nosub";
    public static final String  DIST_DELETE_NOPKG = "error.dist.delete.nopkg";
    public static final String  TUNERCHPROPS_SAVE_ERROR = "internalerror.tunerchprops.save.exception";
    public static final String  TUNERCHPROPS_CANCEL_ERROR = "internalerror.tunerchprops.cancel.exception";
    public static final String  TUNERCHPROPS_NOCHANNEL = "error.tunerchprops.nochannel";

    /** Error constants when there are problems with the transmitter logon page */
    public static final String  TRANSLOGIN_PWDCONFIRM_FAILED = "error.translogin.pwdconfirm.failed";
    public static final String  SERVICESCHED_SAVE_ERROR = "error.servicesched.save.exception";
    public static final String  SERVICESCHED_LOAD_ERROR = "error.servicesched.load.exception";
    public static final String  SERVICESCHED_TARGETDETAILS_NOTARGETFOUND_ERROR = "error.servicesched.targetdetails.notargetfound";

    // Error constants for adding and removing targets from the assignment page
    public static final String  ADDREMOVETG_ALREADYADDED = "error.target.add.alreadyAdded";
    public static final String  ADDREMOVETG_ACLDENIED = "error.target.add.aclDeny";
    public static final String  ADDREMOVETG_ACLSTORAGE_ERROR = "error.target.add.aclError";
    public static final String ADDREMOVETG_ACL_ERROR = "error.target.add.aclGeneralError";


    // Error constants when validating content in assignment page
    public static final String  ASSIGN_PRIORITY_EXCEED = "error.assign.priorityRangeExceeed";
    public static final String  EDIT_PRIORITY_EXCEED = "error.assign.unknownPriorityValue";
    public static final String  PROPERTY_PRIORITY_EXCEED = "error.assign.propsPriorityRangeExceeed";
    public static final String  PRIORITY_ALREADY_EXIST = "error.assign.propsPriorityExist";

    // LDAP Install script generations related errors
    public static final String  LDAPSCRIPT_CANTSAVEUPDATESCRIPT = "error.comp.ldapscript.CantSaveUpdateScript";
    public static final String  LDAPSCRIPT_CANTSAVEIPLANETSCRIPT = "error.comp.ldapscript.CantSaveIPlanetScript";
    public static final String  LDAPSCRIPT_CANTSAVEADSCRIPT = "error.comp.ldapscript.CantSaveADScript";
    public static final String  LDAPSCRIPT_CANTSAVECOLLSCRIPT = "error.comp.ldapscript.CantSaveCollectionScript";

    // Import Machines related errors
    public static final String  IMPORTMACHINES_INSERTGROUP = "error.comp.importmachines.insertgroup";
    public static final String  IMPORTMACHINES_INSERTMACHINE = "error.comp.importmachines.insertmachine";
    public static final String  IMPORTMACHINES_INPUTFILE = "error.comp.importmachines.inputfile";

    // General LDAP errors
    public static final String  LDAP_CONNECT_LDAPCONFIG_NOTFOUND = LDAP_CONNECT_PFX + "ldapconfig.notfound";
    public static final String  LDAP_CONNECT_EMERGENCYUSER = LDAP_CONNECT_PFX + "emergencyuser";
    public static final String  LDAP_CONNECT_RESOLVEUSERDN = LDAP_CONNECT_PFX + "resolveuserdn";
    public static final String  LDAP_CONNECT_MULTIPLEUSERDN = LDAP_CONNECT_PFX + "multipleuserdn";
    public static final String  LDAP_CONNECT_GCSITEORROOT = LDAP_CONNECT_PFX + "gcsiteorroot";
    public static final String  LDAP_CONNECT_UPDATECONTAINER = LDAP_CONNECT_PFX + "updatecontainer";
    public static final String  SCHED_SAVE_ERROR = "error.sched.save.exception";
    public static final String  SCHED_INTERNAL_INVALIDFORMAT = "internalerror.sched.invalidformat";
    public static final String  SCHED_INTERNAL_UNKNOWNTYPE = "internalerror.sched.unknowntype";
    public static final String  SCHED_INTERNAL_UNKNOWNCOMPONENT = "internalerror.sched.unknowncomponent";
    public static final String  SCHED_INTERNAL_UNKNOWNCHANNEL = "internalerror.sched.unknownchannel";

    //Edit Target List Errors

    public static final String  ADDTARGET_SCHED_INTERNAL_UNKNOWNCHANNEL = "internalerror.addtargetsched.unknownchannel";
    public static final String  OSM_TAB_ACCESS_RESTRICTED = "error.osm.accessrestricted";
    public static final String  MDM_TAB_ACCESS_TYPE_RESTRICTED = "error.mdm.typeaccessrestricted";
    public static final String  MDM_TAB_ACCESS_NOTENABLED = "error.mdm.notenabled";

    // Blackout errors
    public static final String  VALIDATION_BLACKOUT_FIELD_FROM_REQUIRED = "error.input.blackout.from.required";
    public static final String  VALIDATION_BLACKOUT_FIELD_TO_REQUIRED = "error.input.blackout.to.required";
    public static final String  VALIDATION_BLACKOUT_FIELD_INTEGER = "error.input.blackout.integer";
    public static final String  VALIDATION_BLACKOUT_FIELD_INTEGER_RANGE = "error.input.blackout.integer.range";
    public static final String  VALIDATION_BLACKOUT_FIELD_SEQUENCE = "error.input.blackout.sequence";
    public static final String  VALIDATION_BLACKOUT_FIELD_INVALIDFORMAT = "error.input.blackout.invalidformat";
    public static final String  VALIDATION_BLACKOUT_WEEKDAYS_REQUIRED = "error.input.blackout.daysrequired";
    public static final String  VALIDATION_BLACKOUT_PRIORITY_INVALID = "error.input.blackout.invalidpriority";


    //Vpro Provisioning Erros
    public static final String  VALIDATION_VPRO_KVM_SESSION_PWD_REQUIRED = "error.input.vpro.kvmsessionpwdrequired";
    public static final String  VALIDATION_VPRO_KVM_SESSION_PWD_INVALID = "error.input.vpro.kvmsessionpwdinvalid";
    public static final String  VALIDATION_VPRO_KVM_SESSION_CFRMPWD_INVALID = "error.input.vpro.kvmsessioncfrmpwdinvalid";
    public static final String  VALIDATION_VPRO_TIMEOUT_USERCONSENT_REQUIRED = "error.input.vpro.timeoutusrcstrequired";
    public static final String  VALIDATION_VPRO_TIMEOUT_USERCONSENT_INTEGER_REQUIRED = "error.input.vpro.timeoutusrcstintrequired";
    public static final String  VALIDATION_VPRO_TIMEOUT_USERCONSENT_RANGE = "error.input.vpro.timeoutusrcstrange";
    public static final String  VALIDATION_VPRO_TIMEOUT_IDLE_INTEGER_REQUIRED = "error.input.vpro.timeoutidleintrequired";
    public static final String  VALIDATION_VPRO_TIMEOUT_IDLE_RANGE = "error.input.vpro.timeoutidlerange";
    public static final String  VALIDATION_VPRO_TIMEOUT_IDLE_REQUIRED = "error.input.vpro.timeoutidlerequired";
    public static final String  VALIDATION_VPRO_ADMINUSER_PWD_REQUIRED = "error.input.vpro.adminuserpwdrequired";
    public static final String  VALIDATION_VPRO_ADMINUSER_PWD_INVALID = "error.input.vpro.adminuserpwdinvalid";
    public static final String  VALIDATION_VPRO_ADMINUSER_CFRMPWD_INVALID = "error.input.vpro.adminusercfrmpwdinvalid";
    public static final String  VALIDATION_VPRO_CERTFILE_REQUIRED = "error.input.vpro.crtfilerequired";
    public static final String  VALIDATION_VPRO_PRVTFILE_REQUIRED = "error.input.vpro.prvtfilerequired";
    public static final String  VALIDATION_VPRO_PRIORITY_INVALID = "error.input.vpro.invalidpriority";

    // Intel vPro Alarm Clock Settings Error Constants
    public static final String  VALIDATION_VPROALARM_PRIORITY_INVALID = "error.input.vproAlarm.invalidpriority";
    public static final String  VALIDATION_VPROALARM_STARTTIME_INVALID_FORMAT = "error.input.vproAlarm.invalidstarttimeformat";
    public static final String  VALIDATION_VPROALARM_STARTTIME_INVALID = "error.input.vproAlarm.invalidstarttime";
    public static final String  VALIDATION_VPROALARM_WAKEUPDAYS_INVALID = "error.input.vproAlarm.invalidwakeupdays";
    public static final String  VALIDATION_VPROALARM_WAKEUPHRMIN_INVALID = "error.input.vproAlarm.invalidwakeuphrmin";

    //Error constants used during ldap navigation
    public static final String  LDAP_BROWSE_LDAPLOCAL = "error.ldap.browse.ldaplocal";
    public static final String  LDAP_SEARCH_EMPTYCONTAINER = "error.ldap.search.emptycontainer";
    public static final String  LDAP_SEARCH_EMPTYSTRING = "error.ldap.search.emptystring";
    public static final String  LDAP_SEARCH_INVALIDDATE = "error.ldap.search.invaliddate";
    public static final String  SYSTEM_INTERNAL_STATE_NULL = "internalerror.system.state.null";
    public static final String  SYSTEM_INEP_FAIL = "error.system.checkInEp.failed";
    public static final String  SYSTEM_INIT_FAIL = "error.system.init.fail";
    public static final String  SYSTEM_INTERNAL_UIDNOTFOUND = "internalerror.system.uidnotfound";
    public static final String  CONTAINER_NOTAD = "error.container.notad";
    public static final String  CONTAINER_CREATEFAILED = "error.container.createfailed";
    public static final String  UPGRADE_CONFIG_INVALIDMODE = "error.upgrade.config.invalidmode";
    public static final String  UPGRADE_CONFIG_LDAPLOCAL = "internalerror.upgrade.config.ldaplocal";
    public static final String  GUI_INTERNAL_WRONGARG = "internalerror.gui.wrongArguments";
    public static final String  ACL_NOTSETUP = "error.acl.notsetup";
    public static final String  ACL_NOSUBPERM = "error.acl.nosubperm";
    public static final String  ACL_MGRERROR = "error.acl.mgrerror";
    public static final String  CLI_INVALIDARGS = "error.cli.invalidargs";
    public static final String  MALFORMED_PATTERN = "error.search.malformedpattern";

    // Error constants for ACLs
    public static final String  ACL_NO_ITEM_SELECTED = "error.acl.noitemsel";
    public static final String  ACL_MORETHANONE_ITEM_SELECTED = "error.acl.morethanoneitemsel";
    public static final String  ACL_ITEM_ALREADY_EXISTS = "error.acl.itemalreadyexists";
    public static final String  ACL_NO_PERMISSION_FOR_ACTION = "error.acl.noperforaction";
    public static final String  ACL_NO_ACTIONS_SPECIFIED = "error.acl.noactionspecified";
    public static final String  ACL_LDAP_CONFIG_MISSING = "error.acl.ldapconfigmissing";
    public static final String  ACL_LDAP_CONNECTION_FAILED = "error.acl.ldapconnfailed";
    public static final String  ACL_INIT_UNKNOWN_ERROR = "error.acl.initunknownerror";
    public static final String  ACL_ROLE_NOTPRIMARY = "error.acl.role.notprimary";
    public static final String  ACL_ROLE_NOTADMIN = "error.acl.role.notadmin";
    public static final String  ACL_INHERITED_NOTDELETED = "error.acl.inheritance.notdeleted";
    public static final String  ACL_SOME_TARGETS_DELETED = "error.acl.sometargetsdeleted";

    public static final String  PC_PRODUCTNOTINSTALLED = "error.compliance.productnotinstalled";
    public static final String  PC_PRODUCTNOTRUNNING = "error.compliance.productnotrunning";
    public static final String  PC_PRODUCTNOTCONFIGURED = "error.compliance.productnotconfigured";

    public static final String  PC_ACLDENYREAD = "error.compliance.aclDenyRead";

    public static final String  PC_TUNERADMINFAILED = "error.compliance.tuneradminfailed";
    public static final String  PC_NOQUERYCONTEXT = "error.compliance.noquerycontext";
    public static final String  PC_NOQUERYCONTEXTWRITE = "error.compliance.noquerycontextwrite";
    public static final String  PC_NOQUERYCONTEXTREAD = "error.compliance.noquerycontextread";
    public static final String  PC_QUERYSAVEFAILED = "error.compliance.querysavefailed";
    public static final String  PC_QUERYINVALIDNAME = "error.compliance.queryinvalidname";
    public static final String  PC_GENERAL = "error.compliance.general";
    public static final String  PC_HOST_NOTSET = "error.compliance.hostnotset";
    public static final String  PC_TG_RESOLUTION_FAILED = "error.compliance.tgresolutionfailed";
    public static final String  PC_REFRESH_FAILED = "error.compliance.refreshfailed";
    public static final String  PC_PURGE_CACHE_FAILED = "error.compliance.purgefailed";
    public static final String  PC_UNKNOWN_HOST = "error.compliance.unknownhost";
    public static final String  PC_GETGROUPS_FAILED = "error.compliance.getgroupfailed";
    public static final String  PC_GET_TGTPKGCOMP_ERROR = "error.compliance.gettargetpackagecompliancefailed";

    public static final String  PATCH_MANAGER_NOT_CONFIGURED = "error.patch.managernotconfigured";
    public static final String  PATCH_SIMULATE = "error.patch.simulate";
    public static final String  PATCH_GETPATCHES = "error.patch.getpatches";
    public static final String  PATCH_CREATECONTEXT = "error.patch.createcontext";
    public static final String  PATCH_SERVICEURLNOTCONFIGURED = "error.patch.serviceurlnotconfigured";

    // Security Policy Manager
    public static final String  SECURITY_POLICY_CREATECONTEXT = "error.security.policy.createcontext";
    public static final String  SECURITY_POLICY_MANAGER_NOT_CONFIGURED = "error.security.policy.managernotconfigured";
    
    // push errors
    public static final String  PUSH_DISABLED = "error.push.disabled";
    public static final String  PUSH_PREVIEW_NOSUB = "error.push.preview.nosub";
    public static final String  PUSH_FROMDETAILS_ERROR = "error.push.fromdetails.exception";
    public static final String  PUSH_FROMDETAILS_NOTARGETS = "error.push.fromdetails.notargets";
    public static final String  PUSH_STATUS_NODEPLOYMENT = "error.push.status.nodeployment";
    public static final String  PUSH_STATUS_DEPLOYMENTMANGER_ERROR = "error.push.status.deploymentmanager";

    public static final String DBCONNECT_FAILED = "error.dbconnect.failed";
    public static final String NO_CACHED_DATAFOUND = "error.nocached.datafound";

    //Error constants for copy operation
    public static final String  COPY_ITEM_ALREADY_EXISTS = "error.copy.itemalreadyexists";
    public static final String  COPY_FROM_ITEM_ALREADY_EXISTS = "error.copy.fromitemalreadyexists";
    public static final String  COPY_NO_ITEM_EXISTS = "error.copy.noitemexists";
    public static final String  COPY_NO_POLICY_EXISTS = "error.copy.nopolicyexists";
    public static final String  COPY_POLICY_SAVE_ERROR = "error.copy.package.save.exception";
    public static final String  CANT_REMOVE_NULLVALUE_FROM_SESSION = "CantRemoveNullValueFromSession";
    public static final String  REPORT_CENTER = "ReportCenter";
    public static final String  APPLY_TPROPS_ACTION = "ApplyTPropsAction_";

    //Error constants for Edit target List
    public static final String ADD_TARGET_ITEM_ALREADY_EXISTS = "error.addtarget.itemalreadyexists";
    public static final String ADD_TARGET_SELECT_ONE_TARGET = "error.addtarget.selectone";
    public static final String ADD_TARGET_SAVE_NOSECSCHED = "error.addtarget.save.nosecsched";
    public static final String ADD_TARGET_SAVE_NOSECSTATES = "error.addtarget.save.nosecstates";
    public static final String ADD_TARGET_NO_WRITE_PERMISSION = "error.addtarget.nowritepermission";
    public static final String ADD_TARGET_ACLSTORAGE_ERROR = "error.addtarget.aclError";
    public static final String ADD_TARGET_ACL_ERROR = "error.addtarget.aclGeneralError";

    //AR Error Constants
    String AR_NO_TASK_ID= "error.ar.notaskid";
    String AR_TARGETCHANNEL_NOTPRESENT = "error.ar.targetchannel.notpresent";
    String AR_TARGETCHANNEL_POLICY = "error.ar.targetchannel.notarget";
    String AR_NO_ACL_PERMISSION = "error.ar.noaclpermission";
    String AR_INVALID_PATCH_STATE = "error.ar.invalidpatchstate";
    String AR_INVALID_CHANNEL_STATE = "error.ar.invalidchannelstate";
    String AR_CONNECTION_ERROR = "error.ar.connectionerror";
    String AR_INVALID_TIMEOUT = "error.ar.invalidtimeout";
    String AR_INVALID_PERCENTAGE = "error.ar.invalidpercentage";


    public static final String INVALID_ACTIVATION_DATE_FORMAT = "error.input.schedule.activationrequired";
    public static final String INVALID_EXPIRATION_DATE_FORMAT = "error.input.schedule.expirationrequired";
    public static final String INVALID_REBOOT_DATE_FORMAT = "error.input.schedule.rebootrequired";

    // Error Constants for power option properties
    public static final String VALIDATION_POWERPROFILE_EMPTY   = "error.input.powerprofile.namerequired";
    public static final String VALIDATION_POWERPROFILE_SPLCHAR = "error.input.powerprofile.splchar";
    public static final String VALIDATION_POWERPROFILE_DEFAULT_NAME = "error.input.powerprofile.defaultprofilename";
    public static final String VALIDATION_PROFILE_DESCRIPTION = "error.input.profile.desrequired";

    // Error constants for OS Migration settings
    public static final String OSM_NO_TMPLT_SELECTED = "error.osm.ostmplt.required";

    // Error constants for Personal Backup settings

    public static final String PB_NO_TMPLT_SELECTED = "error.pb.pbtmplt.required";

    // Error Constants for Peer Approval settings

    String EMAIL_ID_INVALID = "error.email.invalid";
    String EMAIL_ID_REQUIRED = "error.email.required";
    String LDAP_GROUP_INVALID = "error.ldap.group.invalid";
    String LDAP_GROUP_REQUIRED = "error.ldap.group.required";
    String LDAP_OPERATOR_GROUP = "error.ldap.operatorgroup";

    // Error Constants for User Centric Deployment
    String USER_MAPPING_FILE_REQUIRED = "error.user.centric.mappingfile.required";
    String USER_MAPPING_FILE_INVALID = "error.user.centric.mappingfile.invalid";
    String VALIDATION_UCDTEMPLATENAME_EMPTY = "error.ucd.input.templatename.required";
    String VALIDATION_UCDTEMPLATENAME_SPLCHAR = "error.ucd.input.templatename.invalid";
    String VALIDATION_UCDTEMPLATENAME_UNIQUE = "error.ucd.input.templatename.unique";
    String VALIDATION_UCDDEVICELEVEL_EMPTY = "error.ucd.input.devicelevel.required";
    String VALIDATION_UCDDEVICELEVEL_INVALID = "error.ucd.input.devicelevel.invalid";
    String VALIDATION_UCDTEMPLATE_INVALID = "error.ucd.input.template.invalid";
}
