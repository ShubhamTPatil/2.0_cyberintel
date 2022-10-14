// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.cli.commands;

import java.sql.SQLException;
import java.text.DateFormat;
import java.util.*;

import javax.naming.*;
import javax.naming.directory.*;
import javax.servlet.ServletContext;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.LDAPEnv;
import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.SubKnownException;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLICommand;
import com.marimba.apps.subscriptionmanager.cli.commands.intf.ISubscribe;
import com.marimba.apps.subscription.common.LDAPQueryInfo;
import com.marimba.apps.subscriptionmanager.ldapquery.LDAPQueryCollnManager;
import com.marimba.apps.subscriptionmanager.users.CLIUser;
import com.marimba.castanet.schedule.Schedule;
import com.marimba.castanet.schedule.ScheduleInfo;
import com.marimba.tools.config.ConfigDefaults;
import com.marimba.tools.config.ConfigProps;
import com.marimba.tools.gui.StringResources;
import com.marimba.tools.ldap.LDAPConnUtils;
import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.tools.ldap.LDAPException;
import com.marimba.tools.ldap.LDAPLocalException;
import com.marimba.tools.ldap.LDAPSearchFilter;
import com.marimba.tools.util.Props;
import com.marimba.webapps.intf.SystemException;
import com.marimba.intf.util.IProperty;
import com.marimba.intf.util.IConfig;
import com.marimba.intf.msf.IUserPrincipal;

/**
 *  Command for LDAP Query collection.
 *
 * @author      Kumaravel Ayyakkannu
 * @author      Marie Antoine (Tony)
 * @version 	$Revision$, $Date$
 */

public class LdapQc extends SubscriptionCLICommand implements ISubscribe,ISubscriptionConstants  {

    private Hashtable validLdapqcSubArgs;
    private Hashtable validTypes;
    private Hashtable displayStateMapping;
    ServletContext context;

    public LdapQc() {

	validLdapqcSubArgs = new Hashtable();
	validLdapqcSubArgs.put("-schedule","-schedule");
	validLdapqcSubArgs.put("-searchBase","-searchBase");
	validLdapqcSubArgs.put("-filter","-filter");
	validLdapqcSubArgs.put("-query","-query");
	validLdapqcSubArgs.put("-modify","-modify");
	validLdapqcSubArgs.put("-preview","-preview");
	validLdapqcSubArgs.put("-refresh","-refresh");
	validLdapqcSubArgs.put("-config","-config");
	validLdapqcSubArgs.put("-removetask","-removetask");
	validLdapqcSubArgs.put("-listtask","-listtask");
	validLdapqcSubArgs.put("-list","-list");
	validLdapqcSubArgs.put("-delete","-delete");
	validLdapqcSubArgs.put("-cname","-cname");
    validLdapqcSubArgs.put("-addtask", "-addtask");

	validTypes  = new Hashtable();
	validTypes.put("machine", "machine");
	validTypes.put("machinegroup", "machinegroup");
	validTypes.put("user", "user");
	validTypes.put("usergroup", "usergroup");
	validTypes.put("all", "all");
	validTypes.put("container", "container");
	validTypes.put("collection", "collection");
	validTypes.put("ldapqc", "ldapqc");

	// Subscription state to  Display state mapping
	displayStateMapping = new Hashtable();
	displayStateMapping.put("available", "advertise");
	displayStateMapping.put("subscribe_noinstall", "stage");
	displayStateMapping.put("subscribe", "install");
	displayStateMapping.put("subscribe_start", "install-start");
	displayStateMapping.put("start_persist", "install-start-persist");
	displayStateMapping.put("subscribe_persist", "install-persist");
	displayStateMapping.put("delete", "uninstall");
	displayStateMapping.put("exclude", "exclude");
	displayStateMapping.put("primary", "primary");
    }
    public void setSubscriptionMain(SubscriptionMain subsMain) {
       this.subsMain = subsMain;
    }
    public void setCLIUser(CLIUser cliUser) {
    	this.cliUser = cliUser;
    }
    public void setResources(StringResources  resources) {
    	this.resources = resources;
    }
    public boolean run(ServletContext context, Hashtable args) throws SystemException {
	this.context = context;
	return run(args);
    }
    public boolean run(Hashtable args) throws SystemException {
        boolean isFailed = false;
        isFailed = ldapqc(args);
        return isFailed;
    }
	private boolean ldapqc(Hashtable cmds ) {

        boolean createFlag = "-create".equals(cmds.get("ldapqc:args0"));
        boolean modifyFlag = "-modify".equals(cmds.get("ldapqc:args0"));
        boolean deleteFlag = "-delete".equals(cmds.get("ldapqc:args0"));
        boolean previewFlag = "-preview".equals(cmds.get("ldapqc:args0"));
        boolean listFlag = "-list".equals(cmds.get("ldapqc:args0"));
        boolean refreshFlag = "-refresh".equals(cmds.get("ldapqc:args0"));
        boolean configFlag = "-config".equals(cmds.get("ldapqc:args0"));
        boolean removeTaskFlag = "-removetask".equals(cmds.get("ldapqc:args0"));
        boolean listTaskFlag = "-listtask".equals(cmds.get("ldapqc:args0"));
        boolean addTaskFlag = "-addtask".equals(cmds.get("ldapqc:args0"));

        IUserPrincipal user = cliUser.getUser();
        String createdBy= null;

        try {
             createdBy = subsMain.resolveUserDN(user.getName());
        } catch (SystemException se) {
            return true;
        }
        String nextrun = null;

        try {
                   if(configFlag) {
                   checkPrimaryAdminRole();
                    try {
                        IProperty oldConfig = subsMain.getSubscriptionConfig();
                        String    key = (String) cmds.get("ldapqc:args1");
                        String    val = (String) cmds.get("ldapqc:args2");
                        boolean   preview = "-preview".equals(cmds.get("ldapqc:args3"));

                        key = key.toLowerCase();

                        if (preview) {
                            printMessage(resources.getString("cmdline.config.preview") + key);
                            printMessage(resources.getString("cmdline.config.old") + oldConfig.getProperty(key));
                            printMessage(resources.getString("cmdline.config.new") + val);
                            printMessage("\n");
                        } else {
                            LDAPEnv        ldapenv = subsMain.getLDAPEnv();
                            String         configDN = ldapenv.getSubConfigDN();
                            LDAPConnection defConn = cliUser.getConnectionCache()
                                                            .getDefaultConnection();
                            String         domain = LDAPConnUtils.getDomainFromDN(defConn, LDAPConnUtils.getDomainDNFromDN(defConn, configDN));
                            LDAPConnection dConn = cliUser.getConnectionCache()
                                                          .getConnection(LDAPConstants.TYPE_DC, domain);
                            IConfig        newConfig = new ConfigDefaults(oldConfig, new ConfigProps(new Props(), null));
                            newConfig.setProperty(key, val);
                            ldapenv.saveSubscriptionConfig(newConfig, configDN, dConn,subsMain.getLDAPVarsMap());
                            printMessage(resources.getString("cmdline.config.old") + oldConfig.getProperty(key));
                            printMessage(resources.getString("cmdline.config.new") + val);
                            printMessage("\n");
                            printMessage(resources.getString("cmdline.config.success") + key + "=" + newConfig.getProperty(key));
                            logMsg(LOG_AUDIT_LDAPQC_CONFIG_CHANGED, LOG_AUDIT, key + " "+ val, CMD_LDAP_QC );
                        }
                    } catch (NamingException ne) {
                        LDAPUtils.classifyLDAPException(ne);
                    } catch (LDAPLocalException le) {
                        throw new SystemException(le, ACL_MGRERROR);
                    }
                 return false;
                } else if (createFlag) {

                        Map map = getArgs(cmds);
                        if( map != null ) {
                            // If any of the arguments is null or valid commands throw the usage.
                            String collnName = (String) map.get("-cname");
                            String ldapQuery = (String) map.get("-query");
                            String schedule = (String) map.get("-schedule");
                            String searchBase = (String) map.get("-searchBase");
                            String filter = (String) map.get("-filter");

                            if (collnName == null) {
                                    printMessage(resources.getString("cmdline.ldapqc.collnnamecannotbenull"));

                                    return true;
                            }
                            if (ldapQuery == null) {
                                    printMessage(resources.getString("cmdline.ldapqc.querymissing"));

                                    return true;
                            }
                            if(schedule != null) {
                                ScheduleInfo schInfo = Schedule.getScheduleInfo(schedule);
                                // If the Schedule object gets set to NEVER that means
                                // the schedule string was invalid unless the scedule
                                // string itself is specified as never
                                if ((schInfo == null) || (!"never".equalsIgnoreCase(schedule) && (schInfo.getFlag(ScheduleInfo.CALENDAR_PERIOD).intValue() == ScheduleInfo.NEVER))) {
                                    printMessage(resources.getString("cmdline.ldapqc.invalidschedule") + schedule);

                                    return true;
                                } else {
                                        if(!"never".equalsIgnoreCase(schedule)) {
                                            Schedule sch = Schedule.readSchedule(schedule);
                                            nextrun = String.valueOf((sch.nextTime(-1,new Date().getTime())));
                                        }
                                }
                            }
                            if ( filter !=  null) {
                                if(!("usersonly".equals(filter) || "machinesonly".equals(filter) || "both".equals(filter) )) {
                                    printMessage(resources.getString("cmdline.ldapqc.invalidfilter"));

                                    return true;
                                }
                            } else {
                                filter = "both";
                            }
                            LDAPQueryInfo ldapQueryInfo = new LDAPQueryInfo(collnName, ldapQuery, searchBase, schedule, createdBy, nextrun);
                            ldapQueryInfo.setFilter(filter);
                            boolean failed = !createldapqc(ldapQueryInfo);
                            if(!failed) {
                                logMsg(LOG_AUDIT_LDAPQC_CREATED, LOG_AUDIT, collnName + " "+ ldapQuery, CMD_LDAP_QC);
                            }
                            return failed;
                       }
                    return true;
                } else if(modifyFlag) {

                        Map map = getArgs(cmds);
                        if( map != null ) {
                            // If any of the arguments is null or valid commands throw the usage.
                            String collnName = (String) map.get("-cname");
                            String ldapQuery = (String) map.get("-query");
                            String schedule = (String) map.get("-schedule");
                            String searchBase = (String) map.get("-searchBase");
                            String filter = (String) map.get("-filter");


                            if (collnName == null) {
                                    printMessage(resources.getString("cmdline.ldapqc.collnnamecannotbenull"));

                                    return true;
                            }

                            if( ldapQuery == null &&  searchBase == null &&  schedule == null && filter == null ) {
                                  printMessage(resources.getString("cmdline.ldapqc.missingarguments"));

                                  return true;
                            }

                            if(schedule != null) {
                                ScheduleInfo schInfo = Schedule.getScheduleInfo(schedule);
                                // If the Schedule object gets set to NEVER that means
                                // the schedule string was invalid unless the scedule
                                // string itself is specified as never
                                if ((schInfo == null) || (!"never".equalsIgnoreCase(schedule) && (schInfo.getFlag(ScheduleInfo.CALENDAR_PERIOD).intValue() == ScheduleInfo.NEVER))) {
                                    printMessage(resources.getString("cmdline.ldapqc.invalidschedule") + schedule);

                                    return true;
                                } else {
                                        if(!"never".equalsIgnoreCase(schedule)) {
                                            Schedule sch = Schedule.readSchedule(schedule);
                                            nextrun = String.valueOf((sch.nextTime(-1,new Date().getTime())));
                                        }
                                }
                            }
                            if ( filter !=  null) {
                                if(!("usersonly".equals(filter) || "machinesonly".equals(filter) || "both".equals(filter) )) {
                                    printMessage(resources.getString("cmdline.ldapqc.invalidfilter"));

                                    return true;
                                }
                            }

                            LDAPQueryInfo ldapQueryInfo = new LDAPQueryInfo(collnName, ldapQuery, searchBase, schedule, createdBy, nextrun);
                            ldapQueryInfo.setFilter(filter);
                            boolean failed = !modifyldapqc(ldapQueryInfo);
                            if(!failed) {
                                logMsg(LOG_AUDIT_LDAPQC_MODIFIED, LOG_AUDIT, collnName + " " + ldapQuery, CMD_LDAP_QC);
                            }
                            return failed;
                       }
                    return true;
                } else if (deleteFlag) {
                    boolean allFlag = "-all".equals(cmds.get("ldapqc:args1"));
                    if( allFlag ) {
                        LDAPQueryInfo ldapQueryInfo = new LDAPQueryInfo(createdBy);
                        return (!deleteAllLdapqc(ldapQueryInfo));
                    } else {
                        Map map = getArgs(cmds);
                        String collnName = (String) map.get("-cname");
                        if( map != null ) {
                            LDAPQueryInfo ldapQueryInfo=new LDAPQueryInfo(collnName,createdBy);
                            boolean failed =!deleteldapqc(ldapQueryInfo);
                            if(!failed) {
                                logMsg(LOG_AUDIT_LDAPQC_DELETED, LOG_AUDIT, collnName, CMD_LDAP_QC);
                            }
                            return failed;
                        }
                        return true;
                    }
                } else if(previewFlag) {
                        Map map = getArgs(cmds);
                        if( map != null ) {
                            // If any of the arguments is null or valid commands throw the usage.
                            String collnName = (String) map.get("-cname");
                            String ldapQuery = (String) map.get("-query");
                            String schedule = (String) map.get("-schedule");
                            String searchBase = (String) map.get("-searchBase");
                            String filter = (String) map.get("-filter");

                            if ( filter !=  null) {
                                if(!("usersonly".equals(filter) || "machinesonly".equals(filter) || "both".equals(filter) )) {
                                    printMessage(resources.getString("cmdline.ldapqc.invalidfilter"));

                                    return true;
                                }
                            }

                            LDAPQueryInfo ldapQueryInfo = new LDAPQueryInfo(collnName, ldapQuery, searchBase, schedule, createdBy, nextrun);
                            ldapQueryInfo.setFilter(filter);
                            return (!previewldapqc(ldapQueryInfo));
                       }
                    return true;
                } else if(listFlag) {

                    boolean allFlag = "-all".equals(cmds.get("ldapqc:args1"));

                    if(allFlag) {
                            return (!listldapqcs());
                    } else {
                        Map  map = getArgs(cmds);
                        if( map != null ) {
                            String collnName = (String)map.get("-cname");
                            if (collnName == null) {
                                    printMessage(resources.getString("cmdline.ldapqc.collnnamecannotbenull"));

                                    return true;
                            }
                            LDAPQueryInfo ldapQueryInfo = new LDAPQueryInfo(collnName,createdBy);
                            return (!listldapqc(ldapQueryInfo));
                        }
                        return true;
                    }
                } else if (refreshFlag) {    // Refresh  command is exectued.
                        Map  map = getArgs(cmds);
                        if( map != null ) {
                            String collnName = (String) map.get("-cname");

                            if (collnName == null) {
                                    printMessage(resources.getString("cmdline.ldapqc.collnnamecannotbenull"));

                                    return true;
                            }

                            LDAPQueryInfo ldapQueryInfo = new LDAPQueryInfo(collnName,createdBy);
                            boolean failed = !refreshldapqc(ldapQueryInfo);
                            if(!failed) {
                                logMsg(LOG_AUDIT_LDAPQC_REFRESHED, LOG_AUDIT, collnName, CMD_LDAP_QC );
                            }
                            return failed;
                        }
                        return true;
                } else if(removeTaskFlag) {
                    boolean allFlag = "-all".equals(cmds.get("ldapqc:args1"));
                    if (allFlag) {
                        LDAPQueryInfo ldapQueryInfo = new LDAPQueryInfo(createdBy);
                        return (!removeAllTasksFromTaskMgr(ldapQueryInfo));
                    } else {
                        Map map = getArgs(cmds);
                        if (map != null) {
                            String collnName = (String) map.get("-cname");
                            if (collnName == null) {
                                printMessage(resources.getString("cmdline.ldapqc.collnnamecannotbenull"));
                                return true;
                            }
                            LDAPQueryInfo ldapQueryInfo = new LDAPQueryInfo(collnName,createdBy);
                            return (!removeTaskFromTaskMgr(ldapQueryInfo));
                        }
                         return true;
                    }
                } else if (addTaskFlag) {
                      boolean allFlag = "-all".equals(cmds.get("ldapqc:args1"));
                      if (allFlag) {
                          LDAPQueryInfo ldapQueryInfo = new LDAPQueryInfo(createdBy);
                          return (!addAllTasksToTaskMgr(ldapQueryInfo));
                      } else {
                           Map map = getArgs(cmds);
                            String collnName = (String) map.get("-cname");
                            if (collnName == null) {
                                printMessage(resources.getString("cmdline.ldapqc.collnnamecannotbenull"));
                                return true;
                            }
                            if (map != null) {
                                LDAPQueryInfo ldapQueryInfo = new LDAPQueryInfo(collnName, createdBy);
                                return (!addTaskToTaskMgr(ldapQueryInfo));
                            }
                        return true;
                       }
                  } else if(listTaskFlag) {
                        return (!listAllTasksFromTaskMgr());
                }
        } catch (SystemException se) {
            se.printStackTrace();
        }
      return true;
    }
    private Map getArgs(Hashtable cmds) {
        Map map = new HashMap();
        String value = null;
        boolean usage = false;

        for ( int i=0 ; i < cmds.size(); ) {


            if("-cname".equals(cmds.get("ldapqc:args"+ i))) {
                   value = (String)cmds.get("ldapqc:args"+ (++i));
                   if (!isValidArg(value, "collnname")) {
                        usage = true;
                        break;
                   } else {
                      map.put("-cname",value);
                   }
            } else if("-query".equals(cmds.get("ldapqc:args"+ i))) {
                   value = (String)cmds.get("ldapqc:args"+ (++i));
                   if (!isValidArg(value, "query")) {
                        usage = true;
                        break;
                   } else {
                      map.put("-query",value);
                   }
            } else if("-schedule".equals(cmds.get("ldapqc:args"+ i))) {
                    value = (String)cmds.get("ldapqc:args"+ (++i));
                    if (!isValidArg(value, "schedule")){
                        usage = true;
                        break;
                    } else {
                       map.put("-schedule",value);
                    }
            } else if("-searchBase".equals(cmds.get("ldapqc:args"+ i))) {
                    value = (String)cmds.get("ldapqc:args"+ (++i));
                    if (!isValidArg(value, "searchbase") ){
                        usage = true;
                        break;
                    } else {
                       map.put("-searchBase",value);
                    }
            } else if("-filter".equals(cmds.get("ldapqc:args"+ i))) {
                value = (String)cmds.get("ldapqc:args"+ (++i));
                if (!isValidArg(value, "filter")){
                        usage = true;
                        break;
                } else {
                   map.put("-filter",value);
                }
            } else {
                i++;
            }
        }
      return map;
    }
    private boolean createldapqc(LDAPQueryInfo ldapQueryInfo) {
        printMessage("\n");
        LDAPQueryCollnManager ldapMgr = new LDAPQueryCollnManager(context, subsMain);

        if ( !ldapMgr.isDataSourceExists()) {
            printMessage(resources.getString("cmdline.ldapqc.reportcenterNotConfigured") + "\n");
            return false;
        }

        try {
            ldapMgr.setQuery(ldapQueryInfo);
        } catch(NumberFormatException e) {
            printMessage(resources.getString("cmdline.ldapqc.missingpagesize") + "\n");
            return false;
        }
        try {
            if( ldapMgr.create() ) {
                return true;
            }
        } catch (NoPermissionException npe) {
            printMessage(resources.getString("cmdline.ldapqc.nowritepermission") +"\n");
        }
        catch (NamingException e) {
            printMessage(resources.getString("cmdline.ldapqc.collnnamealreadyexists") +"\n");
        }
        return false;
    }
    private boolean modifyldapqc(LDAPQueryInfo ldapQueryInfo) {
        printMessage("\n");
        LDAPQueryCollnManager ldapMgr =new LDAPQueryCollnManager(context,subsMain);

        if (!ldapMgr.isDataSourceExists()) {
            printMessage(resources.getString("cmdline.ldapqc.reportcenterNotConfigured") + "\n");
            return false;
        }

        try {
            ldapMgr.setQuery(ldapQueryInfo);
        } catch(NumberFormatException e){
            printMessage(resources.getString("cmdline.ldapqc.missingpagesize") + "\n");
            return false;
        }
        try {
            if(ldapMgr.modify()) {
                return true;
            }
        } catch(NameNotFoundException nnfe ) {
            printMessage(resources.getString("cmdline.ldapqc.queryobjectnotfound")+"\n");
        } catch (NoPermissionException npe) {
            printMessage(resources.getString("cmdline.ldapqc.nowritepermission") +"\n");
        }
        catch( NamingException ne ) {
            printMessage(resources.getString("cmdline.ldapqc.operationrestricted")+"\n");
        }
      return false;
    }
    private boolean listldapqc(LDAPQueryInfo ldapQueryInfo) {
     try {
        printMessage("\n");
        LDAPQueryCollnManager ldapMgr =new LDAPQueryCollnManager(context,subsMain);

         if ( !ldapMgr.isDataSourceExists()) {
            printMessage(resources.getString("cmdline.ldapqc.reportcenterNotConfigured") + "\n");
            return false;
        }

         try {
             ldapMgr.setQuery(ldapQueryInfo);
         } catch(NumberFormatException e){
             printMessage(resources.getString("cmdline.ldapqc.missingpagesize") + "\n");
             return false;
         }
        if( ldapMgr.list()) {
            Vector results = new Vector();
            results.addAll(ldapMgr.getResults());
            Object[] res = results.toArray();

            Enumeration enumeration =  results.elements();
            if(res.length >= 1) {
                LDAPQueryInfo  ldapinfo = null ;

                printMessage(resources.getString("cmdline.ldapqc.listresults"));
                printMessage("\n");
                 String[] headerKeys = {
                            "cmdline.ldapqc.querynamehdr","cmdline.ldapqc.querysyntaxhdr","cmdline.ldapqc.queryfilterhdr",
                            "cmdline.ldapqc.querysearchbasehdr","cmdline.ldapqc.queryschedulehdr","cmdline.ldapqc.createdbyhdr",
                            "cmdline.ldapqc.datetimelastrunhdr","cmdline.ldapqc.datetimenextrunhdr"};
                int maxHeaderSize = determineMaxHeaderSize(headerKeys);
                while(enumeration.hasMoreElements()) {
                    ldapinfo =(LDAPQueryInfo )enumeration.nextElement();
                    printMessage(printHeaderWithSpace("cmdline.ldapqc.querynamehdr", maxHeaderSize) + ldapinfo.getCollnName() );
                    printMessage(printHeaderWithSpace("cmdline.ldapqc.querysyntaxhdr", maxHeaderSize) + ldapinfo.getQuery() );
                    printMessage(printHeaderWithSpace("cmdline.ldapqc.queryfilterhdr", maxHeaderSize) + ldapinfo.getFilter());
                    if("null".equalsIgnoreCase(ldapinfo.getSearchBase()) || ldapinfo.getSearchBase().trim().length() == 0 ){
                        printMessage(printHeaderWithSpace("cmdline.ldapqc.querysearchbasehdr", maxHeaderSize)+ resources.getString("cmdline.ldapqc.querysearchbase"));
                      }
                    else {
                        printMessage(printHeaderWithSpace("cmdline.ldapqc.querysearchbasehdr", maxHeaderSize) + ldapinfo.getSearchBase());
                     }
                    if("null".equalsIgnoreCase(ldapinfo.getSchedule()) || ldapinfo.getSchedule().trim().length() == 0 ){
                        printMessage(printHeaderWithSpace("cmdline.ldapqc.queryschedulehdr", maxHeaderSize) + resources.getString("cmdline.ldapqc.queryschedule"));
                    }
                    else {
                        printMessage(printHeaderWithSpace("cmdline.ldapqc.queryschedulehdr", maxHeaderSize) + ldapinfo.getSchedule() );
                    }
                    if("null".equalsIgnoreCase(ldapinfo.getLastrun()) || ldapinfo.getLastrun().trim().length() == 0 ){
                        printMessage(printHeaderWithSpace("cmdline.ldapqc.datetimelastrunhdr", maxHeaderSize) + resources.getString("cmdline.ldapqc.datetimelastrun"));
                    }
                    else {
                        printMessage(printHeaderWithSpace("cmdline.ldapqc.datetimelastrunhdr", maxHeaderSize) + ldapinfo.getLastrun());
                    }
                    if("null".equalsIgnoreCase(ldapinfo.getNextrun()) || ldapinfo.getNextrun().trim().length() == 0 ){
                        printMessage(printHeaderWithSpace("cmdline.ldapqc.datetimenextrunhdr", maxHeaderSize) + resources.getString("cmdline.ldapqc.datetimenextrun"));
                    }
                    else{
                        printMessage(printHeaderWithSpace("cmdline.ldapqc.datetimenextrunhdr", maxHeaderSize) + ldapinfo.getNextrun());
                    }
                    printMessage(printHeaderWithSpace("cmdline.ldapqc.createdbyhdr", maxHeaderSize) + ldapinfo.getCreatedBy());
                    printMessage("\n");
                }
                return true;
            } else {
                printMessage(resources.getString("cmdline.ldapqc.givenobjectnotfound")+ "\n" );
                return true;
            }
        }
     } catch (NameNotFoundException nfe) {
         printMessage(resources.getString("cmdline.ldapqc.queryobjectnotfound") + "\n");
     } catch (NamingException e) {

     }
     return false;
   }
    private boolean listldapqcs() {

        printMessage("\n");
        LDAPQueryCollnManager ldapMgr =new LDAPQueryCollnManager(context,subsMain);

        if (!ldapMgr.isDataSourceExists()) {
            printMessage(resources.getString("cmdline.ldapqc.reportcenterNotConfigured") + "\n");
            return false;
        }

        try {
            ldapMgr.setQuery(new LDAPQueryInfo());
        } catch(NumberFormatException e){
            printMessage(resources.getString("cmdline.ldapqc.missingpagesize") + "\n");
            return false;
        }

      try {
        if( ldapMgr.listAll() ) {

            Vector results = new Vector();
            results.addAll(ldapMgr.getResults());

            Object[] res = results.toArray();
            if(res.length > 0) {
                Enumeration enumeration =  results.elements();
                LDAPQueryInfo  ldapinfo = null ;

                printMessage(resources.getString("cmdline.ldapqc.listallresults"));
                printMessage("\n");
                String[] headerKeys = { "cmdline.ldapqc.querynamehdr","cmdline.ldapqc.queryschedulehdr",
                                        "cmdline.ldapqc.createdbyhdr","cmdline.ldapqc.datetimelastrunhdr",
                                        "cmdline.ldapqc.datetimenextrunhdr"};
                int maxHeaderSize = determineMaxHeaderSize(headerKeys);
                while(enumeration.hasMoreElements()){
                    ldapinfo =(LDAPQueryInfo )enumeration.nextElement();
                    printMessage(printHeaderWithSpace("cmdline.ldapqc.querynamehdr", maxHeaderSize) + ldapinfo.getCollnName());
                    if("null".equalsIgnoreCase(ldapinfo.getSchedule()) || ldapinfo.getSchedule().trim().length() == 0 ){
                        printMessage(printHeaderWithSpace("cmdline.ldapqc.queryschedulehdr", maxHeaderSize) + resources.getString("cmdline.ldapqc.queryschedule"));
                    }
                    else{
                        printMessage(printHeaderWithSpace("cmdline.ldapqc.queryschedulehdr", maxHeaderSize)+ ldapinfo.getSchedule());
                    }
                    if("null".equalsIgnoreCase(ldapinfo.getLastrun()) || ldapinfo.getLastrun().trim().length() == 0 ){
                        printMessage(printHeaderWithSpace("cmdline.ldapqc.datetimelastrunhdr", maxHeaderSize) + resources.getString("cmdline.ldapqc.datetimelastrun"));
                    }
                    else{
                        printMessage(printHeaderWithSpace("cmdline.ldapqc.datetimelastrunhdr", maxHeaderSize) + ldapinfo.getLastrun());
                     }
                    if("null".equalsIgnoreCase(ldapinfo.getNextrun()) || ldapinfo.getNextrun().trim().length() == 0 ){
                        printMessage(printHeaderWithSpace("cmdline.ldapqc.datetimenextrunhdr", maxHeaderSize) + resources.getString("cmdline.ldapqc.datetimenextrun"));
                    }
                    else{
                        printMessage(printHeaderWithSpace("cmdline.ldapqc.datetimenextrunhdr", maxHeaderSize) + ldapinfo.getNextrun());
                    }
                    printMessage(printHeaderWithSpace("cmdline.ldapqc.createdbyhdr", maxHeaderSize) +  ldapinfo.getCreatedBy());
                    printMessage("\n");
                }
                printMessage(resources.getString("cmdline.ldapqc.noofmatchingobjects") +" = " + res.length +"\n");
                return true;
            } else {
                printMessage(resources.getString("cmdline.ldapqc.noobjectsfound") + "\n");
                return true;
            }
        } else {
            return false;
        }
       } catch(NamingException ee){
          return false;
       }
    }
    private boolean previewldapqc(LDAPQueryInfo ldapQueryInfo) {
        printMessage("\n");
        LDAPQueryCollnManager ldapMgr =new LDAPQueryCollnManager(context,subsMain);

        if (!ldapMgr.isDataSourceExists()) {
            printMessage(resources.getString("cmdline.ldapqc.reportcenterNotConfigured") + "\n");
            return false;
        }

        try {
            ldapMgr.setQuery(ldapQueryInfo);
        } catch(NumberFormatException e){
            printMessage(resources.getString("cmdline.ldapqc.missingpagesize") + "\n");
            return false;
        }
        try {
            if( ldapMgr.preview() ) {
                Vector results = new Vector();
                results.addAll(ldapMgr.getPreviewResults());
                Object[] res = results.toArray();

                if(res.length >= 1) {
                  Enumeration enumeration =  results.elements();
                    printMessage(resources.getString("cmdline.ldapqc.previewresults") + "\n");
                    while(enumeration.hasMoreElements()){
                        printMessage((String)enumeration.nextElement());
                    }
                    printMessage("\n"+ resources.getString("cmdline.ldapqc.noofmatchingobjects") + " = " +res.length);
                    return true;
                } else {
                     printMessage("\n"+ resources.getString("cmdline.ldapqc.nomachingobjects") +"\n");
                    return true;
                }
            }
        } catch( NameNotFoundException nf ){
              if( "Object does not exist".equals(nf.getMessage()) ) {
                    printMessage(resources.getString("cmdline.ldapqc.queryobjectnotfound") +"\n");
              } else {
                    printMessage(resources.getString("cmdline.ldapqc.searchbasenotfound") +"\n");
              }
        } catch( InvalidSearchFilterException isf ){

            printMessage(resources.getString("cmdline.ldapqc.invalidldapquery") +"\n");
        } catch (InvalidNameException ine) {

                printMessage(resources.getString("cmdline.ldapqc.invalidsearchbase") +"\n");
        } catch (NoPermissionException npe) {

            printMessage(resources.getString("cmdline.ldapqc.nowritepermission") +"\n");
        }catch (NamingException ne) {

            printMessage(resources.getString("cmdline.ldapqc.operationrestricted")+"\n");
        } catch (SQLException sql) {

            printMessage(resources.getString("cmdline.ldapqc.dbconnectionfailed")+"\n");
        }
      return false;
    }
    private boolean refreshldapqc(LDAPQueryInfo ldapQueryInfo) {
        printMessage("\n");
        LDAPQueryCollnManager ldapMgr =new LDAPQueryCollnManager(context,subsMain);

        if (!ldapMgr.isDataSourceExists()) {
            printMessage(resources.getString("cmdline.ldapqc.reportcenterNotConfigured") + "\n");
            return false;
        }

        try {
            ldapMgr.setQuery(ldapQueryInfo);
        } catch(NumberFormatException e){
            printMessage(resources.getString("cmdline.ldapqc.missingpagesize") + "\n");
            return false;
        }
        try {
            if( ldapMgr.refresh() ) {
                return true;
            }
        } catch( NameNotFoundException nf ){
            if("Object does not exist".equals(nf.getMessage())) {
                  printMessage(resources.getString("cmdline.ldapqc.queryobjectnotfound") +"\n");
            } else {
                  printMessage(resources.getString("cmdline.ldapqc.searchbasenotfound") +"\n");
            }
        } catch (InvalidSearchFilterException ise){

            printMessage(resources.getString("cmdline.ldapqc.invalidldapquery") +"\n");
        } catch (InvalidNameException ine) {

                printMessage(resources.getString("cmdline.ldapqc.invalidsearchbase") +"\n");
        } catch (NoPermissionException npe) {
            printMessage(resources.getString("cmdline.ldapqc.nowritepermission") +"\n");
        } catch(NamingException ne) {

            printMessage(resources.getString("cmdline.ldapqc.operationrestricted")+"\n");
        } catch (SQLException sql) {

             printMessage(resources.getString("cmdline.ldapqc.dbconnectionfailed")+"\n");
        }
      return false;
    }
    private boolean deleteldapqc(LDAPQueryInfo ldapQueryInfo) {
        printMessage("\n");
        LDAPQueryCollnManager ldapMgr =new LDAPQueryCollnManager(context,subsMain);

        if (!ldapMgr.isDataSourceExists()) {
            printMessage(resources.getString("cmdline.ldapqc.reportcenterNotConfigured") + "\n");
            return false;
        }

        try {
            ldapMgr.setQuery(ldapQueryInfo);
        } catch(NumberFormatException e){
            printMessage(resources.getString("cmdline.ldapqc.missingpagesize") + "\n");
            return false;
        }
        try {
            if(ldapMgr.delete()) {
                return true;
            }
        } catch (NameNotFoundException nfe) {
            printMessage(resources.getString("cmdline.ldapqc.queryobjectnotfound") +"\n");
        } catch (NoPermissionException npe) {
            printMessage(resources.getString("cmdline.ldapqc.nowritepermission") +"\n");
        } catch (NamingException ne ){
            printMessage(resources.getString("cmdline.ldapqc.operationrestricted")+"\n");
        }
       return false;
    }
    private boolean deleteAllLdapqc(LDAPQueryInfo ldapqc) {
        printMessage("\n");
        LDAPQueryCollnManager ldapMgr =new LDAPQueryCollnManager(context,subsMain);

        if (!ldapMgr.isDataSourceExists()) {
            printMessage(resources.getString("cmdline.ldapqc.reportcenterNotConfigured") + "\n");
            return false;
        }

        ldapMgr.setQuery(ldapqc);
        try {
            int delCount = ldapMgr.deleteAll();
            if(delCount > 0) {
                printMessage(resources.getString("cmdline.ldapqc.noofdeletedobjects") + " = " + delCount + "\n");
                return true;
            } else {
               printMessage(resources.getString("cmdline.ldapqc.noobjectsfound") + "\n");
               return true;
            }
        } catch (NoPermissionException npe) {
            printMessage(resources.getString("cmdline.ldapqc.nowritepermission") +"\n");
        } catch (NamingException ne ){
            printMessage(resources.getString("cmdline.ldapqc.operationrestricted")+"\n");
        }
       return false;
    }

    private boolean listAllTasksFromTaskMgr() {

        LDAPQueryCollnManager ldapMgr =new LDAPQueryCollnManager(context,subsMain);
        if( ldapMgr.listAllTasks() ) {
            Vector v= new Vector();
            v.addAll(ldapMgr.getResults());
            Object arr[] = v.toArray();
            for(int i = 0; i < arr.length; i++ ) {
                        printMessage(arr[i].toString());
            }
            printMessage(resources.getString("cmdline.ldapqc.noofmatchingobjects")+ " = " +arr.length +"\n");
            return true;
        }
        return false;
    }
    private boolean removeTaskFromTaskMgr(LDAPQueryInfo ldapQueryInfo){

        LDAPQueryCollnManager ldapMgr =new LDAPQueryCollnManager(context,subsMain);
        try {
            ldapMgr.setQuery(ldapQueryInfo);
        } catch(NumberFormatException e){
            printMessage(resources.getString("cmdline.ldapqc.missingpagesize") + "\n");
            return false;
        }

        try{
            if( ldapMgr.remove() ) {
                printMessage("Removed Succeessfully ");
                return true;
            } else {
               printMessage("Removing the task Failed");
               return false;
             }
        }  catch (NameNotFoundException nfe) {
            printMessage(resources.getString("cmdline.ldapqc.queryobjectnotfound") + "\n");
        } catch (NoPermissionException npe) {
            printMessage(resources.getString("cmdline.ldapqc.nowritepermission") + "\n");
        } catch (NamingException ne) {
            printMessage(resources.getString("cmdline.ldapqc.operationrestricted") + "\n");
        }
        return false;
    }
    private boolean removeAllTasksFromTaskMgr(LDAPQueryInfo ldapqc) {
        printMessage("\n");
        LDAPQueryCollnManager ldapMgr = new LDAPQueryCollnManager(context, subsMain);
        if (!ldapMgr.isDataSourceExists()) {
            printMessage(resources.getString("cmdline.ldapqc.reportcenterNotConfigured") + "\n");
            return false;
        }
        ldapMgr.setQuery(ldapqc);
        try {
            int delCount = ldapMgr.removeTaskAll();
            if (delCount > 0) {
                printMessage(resources.getString("cmdline.ldapqc.noofdeletedtasks") + " = " + delCount + "\n");
                return true;
            } else {
                printMessage(resources.getString("cmdline.ldapqc.notasksfound") + "\n");
                return true;
            }
        } catch (NoPermissionException npe) {
            printMessage(resources.getString("cmdline.ldapqc.nowritepermission") + "\n");
        } catch (NamingException ne) {
            printMessage(resources.getString("cmdline.ldapqc.operationrestricted") + "\n");
        }
        return false;
    }

    private boolean addTaskToTaskMgr(LDAPQueryInfo ldapQueryInfo) {

        LDAPQueryCollnManager ldapMgr = new LDAPQueryCollnManager(context, subsMain);
        if (!ldapMgr.isDataSourceExists()) {
            printMessage(resources.getString("cmdline.ldapqc.reportcenterNotConfigured") + "\n");
            return false;
        }
        try {
            ldapMgr.setQuery(ldapQueryInfo);
        } catch (NumberFormatException e) {
            printMessage(resources.getString("cmdline.ldapqc.missingpagesize") + "\n");
            return false;
        }
        try {

            if (ldapMgr.add()) {
                printMessage("Added Succeessfully ");
                return true;
            } else {
                printMessage("Adding the task Failed");
                return false;
            }
        } catch (NameNotFoundException nfe) {
            printMessage(resources.getString("cmdline.ldapqc.queryobjectnotfound") + "\n");
        } catch (NoPermissionException npe) {
            printMessage(resources.getString("cmdline.ldapqc.nowritepermission") + "\n");
        } catch (NamingException ne) {
            printMessage(resources.getString("cmdline.ldapqc.operationrestricted") + "\n");
        }
        return false;
   }

   private boolean addAllTasksToTaskMgr(LDAPQueryInfo ldapqc) {
        printMessage("\n");
        LDAPQueryCollnManager ldapMgr = new LDAPQueryCollnManager(context, subsMain);
        if (!ldapMgr.isDataSourceExists()) {
            printMessage(resources.getString("cmdline.ldapqc.reportcenterNotConfigured") + "\n");
            return false;
        }
        ldapMgr.setQuery(ldapqc);
        try {
            if(ldapMgr.addTaskAll(this)) {
                return true;
            } else {
                return false;
            }
        } catch (NoPermissionException npe) {
            printMessage(resources.getString("cmdline.ldapqc.nowritepermission") + "\n");
        } catch (NamingException ne) {
            printMessage(resources.getString("cmdline.ldapqc.operationrestricted") + "\n");
        }
        return false;
   }

    private boolean isValidArg( String arg, String teststr ) {

        if ( arg == null || (arg.trim()).length() == 0 ) {
            //printMessage(resources.getString("cmdline.ldapqc."+ teststr +"cannotbenull"));
            return false;
        }
        if( validLdapqcSubArgs.containsKey(arg) ) {
            return false;
        }
    return true;

    }
    void ldapqcArgs(Hashtable cmds, HashMap map) {
        String value = null;
        boolean usage = false ;


        for ( int i=2 ; i < cmds.size(); ) {

            String key  =(String) cmds.get("ldapqc:args"+ i);
            if(validLdapqcSubArgs.contains(key)) {
                value = (String)cmds.get("ldapqc:args"+ (++i));
                if("null".equals(value)|| validLdapqcSubArgs.contains(value)) {

                }
            }

            if("-query".equals(cmds.get("ldapqc:args"+ i))) {
                   value = (String)cmds.get("ldapqc:args"+ (++i));
                   if (!isValidArg(value, "query")) {
                        usage = true;
                        break;
                   } else {
                      map.put("-query",value);  // ldapQuery = value;
                   }
            } else if("-schedule".equals(cmds.get("ldapqc:args"+ i))) {
                    value = (String)cmds.get("ldapqc:args"+ (++i));
                    if (!isValidArg(value, "schedule")){
                        usage = true;
                        break;
                    } else {
                       map.put("-schedule",value);
                    }
            } else if("-searchBase".equals(cmds.get("ldapqc:args"+ i))) {
                    value = (String)cmds.get("ldapqc:args"+ (++i));
                    if (!isValidArg(value, "searchbase") ){
                        usage = true;
                        break;
                    } else {
                       map.put("-searchBase",value);
                    }
            } else if("-filter".equals(cmds.get("ldapqc:args"+ i))) {
                value = (String)cmds.get("ldapqc:args"+ (++i));
                if (!isValidArg(value, "filter")){
                        usage = true;
                        break;
                } else {
                   map.put("-filter",value);
                }
            } else {
                i++;
            }
        }
    }
    private String printHeaderWithSpace(String key, int maxHeaderSize) {
        StringBuffer msg = new StringBuffer(resources.getString(key));
        int currHeaderSize = msg.length();
        int spaceNeed =  maxHeaderSize - currHeaderSize;
        StringBuffer alignString = new StringBuffer();
        alignString.append(resources.getString(key));
        for ( int i = 0; i < spaceNeed; i++ ) {
             alignString.append(" ");
        }
        alignString.append("  :  ");
        return alignString.toString();
     }

     private int determineMaxHeaderSize(String[] keys) {
         StringBuffer msg = new StringBuffer(resources.getString(keys[0]));
         int maxHeaderSize = msg.length();
         for( int i = 1; i < keys.length; i++ ) {
             msg = new StringBuffer(resources.getString(keys[i]));
             int len = msg.length();
             if(maxHeaderSize < len ) {
                maxHeaderSize = len;
             }
         }
         return maxHeaderSize;
     }
/*	 *//**
	  * Remind: This method is redundant in CLI.java and here.
	  * @throws SystemException
	  *//*
    void checkPrimaryAdminRole()
 	throws SystemException {
    	boolean isPrimary = SubscriptionMain.isPrimaryAdmin(cliUser.getUser());
    	if  (!isPrimary) {
    		throw new SubKnownException(ACL_ROLE_NOTPRIMARY);
    	}
    }
*/
}
