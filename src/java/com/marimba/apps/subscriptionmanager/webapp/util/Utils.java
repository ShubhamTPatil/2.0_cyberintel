// Copyright 2004-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$
// $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.util;

import static com.marimba.apps.subscription.common.ISubscriptionConstants.*;
import com.marimba.apps.subscription.common.LDAPEnv;
import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscription.common.TargetResHelper;
import com.marimba.apps.subscription.common.intf.ILDAPDataSourceContext;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.util.ScheduleUtils;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import static com.marimba.apps.subscriptionmanager.intf.IAppConstants.*;
import com.marimba.apps.subscriptionmanager.webapp.system.LDAPBean;
import com.marimba.apps.subscriptionmanager.wow.WoWSchedule;
import com.marimba.castanet.schedule.Schedule;
import com.marimba.castanet.schedule.ScheduleInfo;
import com.marimba.intf.admin.IUserDirectory;
import com.marimba.intf.msf.acl.AclException;
import com.marimba.intf.msf.acl.AclStorageException;
import com.marimba.intf.msf.wakeonwan.IDBQueryExecutor;
import com.marimba.intf.msf.wakeonwan.IWakeManager;
import com.marimba.intf.msf.task.ITaskConfig;
import com.marimba.intf.msf.task.ITaskHandle;
import com.marimba.intf.msf.task.ITaskMgr;
import com.marimba.intf.msf.task.ITaskConstants;
import static com.marimba.intf.msf.task.ITaskConstants.PROP_TASK_SCHEDULE_LIST;
import static com.marimba.intf.msf.task.ITaskConstants.*;
import com.marimba.intf.msf.ITenant;
import com.marimba.intf.util.IConfig;
import com.marimba.intf.util.IProperty;
import com.marimba.tools.config.ConfigProps;
import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPException;
import com.marimba.tools.ldap.LDAPLocalException;
import com.marimba.tools.ldap.LDAPSearchFilter;
import com.marimba.webapps.intf.SystemException;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import com.marimba.tools.i18n.util.*;
import org.json.*;
/**
 * helper methods to convert between data types
 *
 * @author Rahul Ravulur
 * @version 1.2, 10/07/2002
 */
public class Utils {
    static final String DEFAULT_DATE_TIME_FORMAT = "MM/dd/yyyy HH:mm:ss";
    
    private static boolean isUrgentPackage = false;
    /**
     * Convert an array of strings to a 'delim' separated list.
     *
     * @param a the array to convert
     * @param delim the character to use to separate the elements
     *
     * @return REMIND
     */
    public static String arrayToString(String[] a,
                                       char     delim) {
        StringBuffer sb = new StringBuffer();
        int          len = a.length;

        if (len > 0) {
            sb.append(a [0]);

            for (int i = 1; i < len; sb.append(delim).append(a [i++])) {
                ;
            }
        }

        return sb.toString();
    }

    /**
     * Convert a comma separated list to an array, optionally sorting it.
     *
     * @param s the string to convert
     * @param delims the characters to be used as delimiters
     * @param sort sort
     *
     * @return REMIND
     */
    public static String[] stringToArray(String  s,
                                         String  delims,
                                         boolean sort) {
        String[] result = null;

        if ((s != null) && (s.length() != 0)) {
            StringTokenizer st = new StringTokenizer(s, delims);
            result = new String[st.countTokens()];

            for (int i = 0; i < result.length; result [i++] = st.nextToken()) {
                ;
            }

            if (sort) {
                marimba.util.QuickSort.sort(result, 0, result.length, false);
            }
        } else {
            result = new String[0];
        }

        return result;
    }

    // utility method to compare the value of two strings
    // returns true in the case both are null too.
    public static boolean channelAttrCompare(String attr1,
                                             String attr2) {
        if (attr1 == null) {
            if (attr2 != null) {
                return false;
            }
        } else if (!attr1.equals(attr2)) {
            return false;
        }

        return true;
    }

    public static String nomalize(String str) {
        int origLength = str.length();
        StringBuffer buffer = new StringBuffer(origLength);
        int i = 0;
        while (i < origLength) {
            char ch = str.charAt(i);
            //lowercase locale should not be an issue since we are only cocerned about the
            //LDAP values
            if (ch >= 'A' && ch <= 'Z') {
                ch = (char) ((ch - 'A') + 'a');
            }
            if (ch != ' ') {
                buffer.append(ch);
            }
            i++;
        }
        return buffer.toString();
    }

    public static boolean checkReadPermission(SubscriptionMain main, HttpServletRequest request, Target singleTarget) throws SystemException,
            LDAPException, NamingException, AclStorageException, AclException, LDAPLocalException {
        HttpSession session = request.getSession();

        LDAPBean ldapBean = (LDAPBean) session.getAttribute(IWebAppConstants.SESSION_LDAP);
        boolean browsingLDAP = main.getUsersInLDAP() || "ldap".equals(ldapBean.getEntryPoint());

        ILDAPDataSourceContext ldapCtx = GUIUtils.getUser(request);
        IUserDirectory userDirectory = null;

        LDAPConnection subConn = ldapCtx.getSubConn();
        String childContainer = main.getSubBaseWithNamespace(ldapCtx);
        IProperty subConfig = main.getSubscriptionConfig();

        if (!browsingLDAP && (TYPE_USER.equals(singleTarget.getType()) || TYPE_USERGROUP.equals(singleTarget.getType()))) {
            userDirectory = main.getUserDirectoryTx(request);
        }

        TargetResHelper targetDNs = null;
        // do group and resolution if its mot a domain and its not target type ALL
        if (! TYPE_ALL.equals(singleTarget.getType()) && !TYPE_DOMAIN.equals(singleTarget.getType())) {
            targetDNs = getGroupsAndContainers(singleTarget.getId(), singleTarget.getType(), subConfig,
                    userDirectory, ldapCtx, childContainer, browsingLDAP, subConn, main.getLDAPEnv());
        }

        boolean primaryAdmin = com.marimba.apps.subscriptionmanager.util.Utils.isPrimaryAdmin(request);
        if (!primaryAdmin && main.isAclsOn()) {
            IUser user = (IUser) session.getAttribute(IWebAppConstants.SESSION_SMUSER);
            if (main.getAclMgr().subReadPermissionExists(user, singleTarget, targetDNs == null ? null : targetDNs.getTargetsSet(), false,browsingLDAP)) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }


    public static TargetResHelper getGroupsAndContainers(String targetID,
                                                         String targetType,
                                                         IProperty subConfig,
                                                         IUserDirectory userDirectory,
                                                         ILDAPDataSourceContext ldapCtx,
                                                         String childContainer,
                                                         boolean usersInLdap, LDAPConnection subConn,
                                                         LDAPEnv ldapEnv)
            throws LDAPException,
            LDAPLocalException,
            NamingException {

        TargetResHelper targetDNs = new TargetResHelper(usersInLdap, ldapEnv.getVariables());

        // get all DNs for the user and machineName
        String[] memberOfattr = new String[1];
        memberOfattr[0] = "memberOf";

        String[] resultDNs = null;

        if (usersInLdap || (!usersInLdap && (TYPE_MACHINEGROUP.equals(targetType) || TYPE_COLLECTION.equals(targetType)))) {
            // A search for the group ID should only be done in LDAP if
            // users are not sourced from the transmitter
            resultDNs = new String[1];
            resultDNs[0] = targetID;
        }


        Set groupDNs = ldapEnv.getGroupMembership(resultDNs, ldapCtx, ldapCtx.getConnectionCache());
        // We default to USERGROUP for all groups from LDAP
        for (Iterator ite = groupDNs.iterator(); ite.hasNext();) {
            String dn = (String) ite.next();
            targetDNs.add(dn, TYPE_USERGROUP);
        }

        //DEBUGTIME(System.currentTimeMillis() - starttime, "Finish resolving group membership");

        if (usersInLdap || (!usersInLdap && (TYPE_MACHINEGROUP.equals(targetType)
                || TYPE_COLLECTION.equals(targetType)))) {
            Map containersMap;
            HashSet iSet = new HashSet(targetDNs.getTargetsSet());
            iSet.add(targetID);
            containersMap = ldapEnv.getEnclosingContainers(subConn, iSet);
            targetDNs.addAll(containersMap);
        }


        // add all user/group targets that point to user/groups
        // as seen from the Transmitter, i.e. via IUserDirectory
        if ((!usersInLdap) && (userDirectory != null)) {
            if (TYPE_USER.equals(targetType)) {
                //DEBUGTIME(starttime, "Start search for user from Tx " + targetID);
                targetDNs.add(targetID, TYPE_USER);

                String[] groups = userDirectory.listGroups(LDAPSearchFilter.escapeComponentValue(targetID));

                if (groups != null) {
                    if (groups.length > 0) {
                        for (int i = 0; i != groups.length; i++) {
                            targetDNs.add(groups[i], TYPE_USERGROUP);
                        }
                    }
                }
            } else if (TYPE_USERGROUP.equals(targetType)) {
                targetDNs.add(targetID, TYPE_USERGROUP);
            }
        }
        return targetDNs;
    }

    public static List sortSchedule(ISubscription sub) {
        isUrgentPackage = false;
        List<WoWSchedule> wowSchedule = new ArrayList<WoWSchedule>();
        Enumeration enm = sub.getChannels();

        for (; enm.hasMoreElements(); ) {
            Channel ch = (Channel) enm.nextElement();

            // check WOW task for urgent package
            if(ch.isWowEnabled()) {
                /*
				 * When WOW check box enabled on packages page, treat it as urgent task.
				 * For this case, a task without time zone is created and executed after
				 * 4 minutes delay from current time. So this urgent schedule will not be added
				 * with normal WOW package schedule list
				 */
                isUrgentPackage = true;
            }
            // check WOW task for primary schedule
            if(ch.getWOWForInit()){
                if (ch.getInitSchedule() != null && !ScheduleUtils.isScheduleStarted(ch.getInitScheduleInfo())) {
                    wowSchedule.add(new WoWSchedule(ch.getInitSchedule()));
                }
            }
            // check WOW task for secondary schedule
            if(ch.getWOWForSec()) {
                if (ch.getSecSchedule() != null && !ScheduleUtils.isScheduleStarted(ch.getSecScheduleInfo())) {
                    wowSchedule.add(new WoWSchedule(ch.getSecSchedule()));
                }
            }
            // check WOW task for update schedule
            if(ch.getUpdateSchedule() != null && ch.getWOWForUpdate() &&
                    ch.getUpdateScheduleInfo().getFlag(ScheduleInfo.CALENDAR_PERIOD) != ScheduleInfo.NEVER ) {
                Schedule updateSch = ch.getUpdateSchedule();
                long next = updateSch.nextTime(-1, System.currentTimeMillis());
                if(next != -1) {
                    wowSchedule.add(new WoWSchedule(ch.getUpdateSchedule()));
                }
            }
            // check WOW task for repair schedule
            if(ch.getVerRepairSchedule() != null && ch.getWOWForRepair() &&
                    ch.getVerRepairScheduleInfo().getFlag(ScheduleInfo.CALENDAR_PERIOD) != ScheduleInfo.NEVER ) {
                Schedule repairSch = ch.getVerRepairSchedule();
                long next = repairSch.nextTime(-1, System.currentTimeMillis());
                if(next != -1) {
                    wowSchedule.add(new WoWSchedule(ch.getVerRepairSchedule()));
                }
            }
        }
        Collections.sort(wowSchedule);
        return wowSchedule;
    }

    public static List collectUpdateSchedule(ISubscription sub) {

        List wowUpdateSchedules = new ArrayList();

        Enumeration enm = sub.getChannels();

        for (; enm.hasMoreElements(); ) {
            Channel ch = (Channel) enm.nextElement();
            if (ch.isWowEnabled()) {
                if(ch.getUpdateSchedule() != null && ch.getWOWForUpdate()) {
                    Schedule updateSch = ch.getUpdateSchedule();
                    long next = updateSch.nextTime(-1, System.currentTimeMillis());
                    if(next != -1) {
                        wowUpdateSchedules.add(ch.getUpdateSchedule());
                    }
                }
            }
        }
        return wowUpdateSchedules;
    }

    public static void scheduleCMSTask(ISubscription sub, IWakeManager wakeMgr, ITaskMgr taskmgr, ITenant tenant) {
        String targetdn  = sub.getTargetID();
        String targetType = sub.getTargetType();
        String targetName = sub.getTargetName();

        IConfig cfg = new ConfigProps(null);
        Schedule finalSchedule = null;
        boolean isTZAvailable = false;

        List wowSchedules = sortSchedule(sub);

        if (!wowSchedules.isEmpty()) {
            //Find list of machines and their time-zones to process
            IDBQueryExecutor queryExecutor = wakeMgr.getDBQueryExecutor();
            if (null == queryExecutor) {
                System.out.println("WOWTask: Failed to schedule task for \"" + targetdn + "\" since failed to get database connection");
                return;
            }
            List<String> tzListFrmDB = queryExecutor.getTimeZones(targetdn, targetType);

            finalSchedule = ((WoWSchedule) wowSchedules.get(0)).geWowSchedule();
            if (!tzListFrmDB.isEmpty()) {
                for (Object tzObj:tzListFrmDB) {
                    String timeZone = tzObj.toString().trim();
                    String nextSchedule = finalSchedule.toString();

                    ScheduleInfo info = Schedule.getScheduleInfo(finalSchedule.toString());
                    if (info.getFlag(ScheduleInfo.CALENDAR_PERIOD) == ScheduleInfo.DAILY ||
                            info.getFlag(ScheduleInfo.CALENDAR_PERIOD) == ScheduleInfo.WEEKLY ||
                            info.getFlag(ScheduleInfo.CALENDAR_PERIOD) == ScheduleInfo.MONTHLY ) {

                        long currTime = System.currentTimeMillis();
                        Schedule schedule = Schedule.readSchedule(finalSchedule.toString());
                        long nextTime = schedule.nextTime(-1, currTime + (61 * 1000));
                        long tzNextTime = taskmgr.getTZSchedule(timeZone, currTime);
                        if (tzNextTime < currTime) {
                            //The first occurrence is less than current time
                            //Hence create one-time time task with expired time
                            //This task will execute immediately

                            DateFormat mbraFormat = new SimpleDateFormat("MM/dd/yyyy@hh:mma");
                            //Assign the new one-time schedule for immediate execution
                            nextSchedule = "active " + mbraFormat.format(tzNextTime);
                            System.out.println("INFO: The first time execution for the recurrent schedule is expired - Schedule :"+ nextSchedule + " Timezone :"+ timeZone);
                        } else {
                            if(DEBUG) {
                                System.out.println("INFO: The first time execution for the schedule has NOT expired :"+ nextSchedule + " Timezone :"+ timeZone);
                            }
                        }
                    }

                    String target = targetName+"_"+timeZone;
                    target = target.replace("/", "_");
                    cfg.setProperty(PROP_TASK_SCHEDULE, nextSchedule);
                    cfg.setProperty(PROP_TARGET_DN, targetdn);
                    cfg.setProperty(PROP_TARGET_TYPE, targetType);
                    cfg.setProperty(PROP_TASK_TIMEOUT, "-1");
                    cfg.setProperty(PROP_TASK_ONE_TIME, "true");
                    cfg.setProperty(PROP_TASK_TIMEZONE, timeZone);
                    targetName = URLEncoder.encode(targetName.replace('.','_'));
                    cfg.setProperty(PROP_TARGET_NAME,targetName);
                    cfg.setProperty(PROP_TASK_SCHEDULE_LIST, convertSchedules (wowSchedules));
                    if (DEBUG) {
                        System.out.println("DEBUG: Utils.scheduleCMSTask - target.ScheduleList - "+ cfg.getProperty(PROP_TASK_SCHEDULE_LIST));
                    }
                    if (taskmgr.addTask(TASK_GROUP_NAME_POLICY, TASK_NAME_POLICY, target, cfg, true)) {
                        isTZAvailable = true;
                        System.out.println("INFO: Task is successfully scheduled for target "+ target +" with schedule "+ nextSchedule + " Timezone :" + timeZone);
                    } else {
                        System.out.println("WARNING: Failed to add WOW task for target - "+ target + " with schedule " + nextSchedule + " Timezone :" + timeZone);
                    }
                }
                if (isTZAvailable) {
                    ITaskHandle task = taskmgr.getTask(TASK_NAME_POLICY, targetName);
                    if (null != task) {
                        System.out.println("INFO: Removing task for target - "+ targetName);
                        taskmgr.removeTask(TASK_NAME_POLICY, targetName);
                    }
                }
            } else {
                if(isUrgentPackage) {
                    isUrgentPackage = false;
                    wowSchedules.add(new WoWSchedule(ScheduleUtils.activeScheduleNow(DELAY)));
                    Collections.sort(wowSchedules);
                    finalSchedule = ((WoWSchedule) wowSchedules.get(0)).geWowSchedule();
                }
                cfg.setProperty(PROP_TASK_TIMEOUT, "-1");
                cfg.setProperty(PROP_TARGET_DN, targetdn);
                cfg.setProperty(PROP_TASK_ONE_TIME, "true");
                cfg.setProperty(PROP_TARGET_NAME,targetName);
                cfg.setProperty(PROP_TARGET_TYPE, targetType);
                targetName = URLEncoder.encode(targetName.replace('.','_'));
                cfg.setProperty(PROP_TASK_SCHEDULE, finalSchedule.toString());
                cfg.setProperty(PROP_TASK_SCHEDULE_LIST, convertSchedules (wowSchedules));

                if (DEBUG) {
                    System.out.println("DEBUG: Utils.scheduleCMSTask() target.ScheduleList - "+ cfg.getProperty(PROP_TASK_SCHEDULE_LIST));
                }
                if (taskmgr.addTask(TASK_GROUP_NAME_POLICY, TASK_NAME_POLICY, targetName, cfg, true)) {
                    System.out.println("Task is successfully added for schedule "+ finalSchedule.toString());
                } else {
                    System.out.println("WARNING: Failed to add WOW task for target - "+ targetName );
                }
            }
        } else {
            Set<ITaskConfig> listTasks = taskmgr.getTasks(TASK_NAME_POLICY, PROP_TARGET_DN, targetdn);
            for (ITaskConfig config : listTasks) {
                String taskName = config.getProperty(PROP_TARGET_NAME);
                String timeZone = config.getProperty(PROP_TASK_TIMEZONE);
                config.setProperty(PROP_TASK_SCHEDULES_REMOVED, "true"); // Property will be listened by Policy Manager in WoWTask.java
                if (null != timeZone) {
                    taskName = (taskName + "_" + timeZone).replace("/", "_");
                }
                removeScheduledTask(taskmgr, taskName);
            }
            // Tamil: commenting this. I don't see any usage of this schedules in WakeManager.java class
            // Same used in CopyTargetAction.java at 294
//            wakeMgr.removeSchedules(targetdn);
            if (DEBUG && !isUrgentPackage) {
                System.out.println("INFO: There was no WOW package schedule enabled for the target -"+ targetdn);
            }
        }
        if (isUrgentPackage) {
            createUrgentTask(sub, taskmgr);
        }
    }

    /**
     * @param value  value
     * @return value for the specified key without appending the priority assigned
     * Ex : If marimba.reboot.never property is assigned a value true with priority '2',i.e., marimba.reboot.never=true,2
     * This is the check to filter Priority value, which may appended with schedule value
     */

    public static String getPropValue(String value) {
        if (value != null) {
            String[] tmp_arr = value.split(",");
            value = (tmp_arr.length == 2) ? tmp_arr[0] : value;
        }
        return value;
    }

    public static void removeScheduledTask(ITaskMgr taskmgr, String targetName) {
        String encodedTargetName = URLEncoder.encode(targetName.replace('.','_'));
        if (taskmgr.getTask("policy", encodedTargetName) != null) {
            if (taskmgr.removeTask("policy", encodedTargetName)) {
                System.out.println("INFO: Sucessfully removed WOW Task as no package has WOW schedule enabled for target -"+ targetName);
            } else {
                System.out.println("INFO: Failed to remove WOW Task from Taskmgr for the target -" + targetName);
            }
        } else {
            System.out.println("INFO: There was no WOW task scheduled for this target -" + targetName);
        }
    }

    public static String convertSchedules(List schedules) {
        StringBuffer buff = new StringBuffer();
        if(schedules != null && !schedules.isEmpty()) {
            for(int i = 0; i < schedules.size() ; i++) {
                Object sch = schedules.get(i);
                Schedule sched = ((WoWSchedule)sch).geWowSchedule();
                buff.append(sched.toString());
                if(i != (schedules.size() - 1)) {
                    buff.append("#");
                }
            }
            return buff.toString();
        }
        else {
            return "";
        }
    }

    /**
     * Create a urgent WOW task with 4 minutes delay from now.
     *
     * @param sub
     * @param taskmgr
     */
    private static void createUrgentTask(ISubscription sub, ITaskMgr taskmgr) {
        String targetdn  = sub.getTargetID();
        String targetType = sub.getTargetType();
        String targetName = sub.getTargetName();

        IConfig cfg = new ConfigProps(null);

        WoWSchedule defaultScheudle = new WoWSchedule(ScheduleUtils.activeScheduleNow(DELAY));
        Schedule urgentSchedule = defaultScheudle.geWowSchedule();

        System.out.println("INFO: Urgent Schedule created for target :" + targetdn + " with schedule -" + urgentSchedule.toString());
        cfg.setProperty("task.schedule", urgentSchedule.toString());
        cfg.setProperty("targetdn", targetdn);
        cfg.setProperty("targettype", targetType);
        cfg.setProperty("task.timeout", "-1");
        cfg.setProperty("task.oneTime", "true");
        targetName = URLEncoder.encode(targetName.replace('.','_'));
        cfg.setProperty("targetName", targetName);
        cfg.setProperty("target.ScheduleList", urgentSchedule.toString());
        cfg.setProperty("target.urgentTask", "true");
        if(DEBUG) {
            System.out.println("DEBUG: Utils.createUrgentTask - target.ScheduleList - "+ cfg.getProperty("target.ScheduleList"));
        }
        if (taskmgr.addTask("PolicyCompliance/wow", "policy", targetName, cfg, true)) {
            System.out.println("INFO: Urgent WOW task is successfully added for target : " + targetdn + " at "+ urgentSchedule.toString());
        } else {
            System.out.println("WARNING: Failed to add urgent WOW task for target : " + targetdn);
        }
    }

    public static String formatDate(long milliseconds, Locale locale) {
        return formatDate(milliseconds, DEFAULT_DATE_TIME_FORMAT, locale);
    }

    public static String formatDate(long milliseconds, String format, Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        DateFormat df;
        if ( !"en".equals(locale.getLanguage())) {

            // the i18nformat class will return the appropriate locale
            // there is only one format (which is full) so the options
            // specified below are ignored.
            df = I18nDateFormat.getLocaleDateTimeInstance(DateFormat.FULL, DateFormat.FULL, locale);
        }else {

            // this constructs the locale specifically for English
            df = new SimpleDateFormat(format, locale);
        }
        System.out.println("Date" + new Date(milliseconds));
        return df.format(new Date(milliseconds));
    }

    public static Map<String, Object> jsonObjectToMap(JSONObject object) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = jsonArrayToList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = jsonObjectToMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> jsonArrayToList(JSONArray array) throws Exception {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = jsonArrayToList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = jsonObjectToMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    public static LinkedHashMap<String, String> sortByMapValue(Map<String, String> unsortMap) {
        // 1. Convert Map to List of Map
        List<Map.Entry<String, String>> list =
                new LinkedList<Map.Entry<String, String>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
            public int compare(Map.Entry<String, String> o1,
                               Map.Entry<String, String> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        LinkedHashMap<String, String> sortedMap = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
}
