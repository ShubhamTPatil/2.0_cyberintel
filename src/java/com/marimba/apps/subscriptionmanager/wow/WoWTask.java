// Copyright 2004-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.wow;


import com.marimba.apps.subscription.common.intf.LogConstants;
import com.marimba.apps.subscription.common.util.ScheduleUtils;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantAttributes;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import static com.marimba.apps.subscriptionmanager.intf.IWebAppConstants.DEBUG;
import static com.marimba.apps.subscriptionmanager.intf.IWebAppConstants.ENABLE_WOW_FEATURE;
import com.marimba.castanet.schedule.Schedule;
import com.marimba.castanet.schedule.ScheduleInfo;
import static com.marimba.castanet.schedule.ScheduleInfo.*;
import com.marimba.intf.msf.ITenant;
import com.marimba.intf.msf.task.*;
import com.marimba.intf.msf.wakeonwan.IDBQueryExecutor;
import com.marimba.intf.msf.wakeonwan.IWakeManager;
import com.marimba.intf.msf.wakeonwan.IWakeUpJob;
import com.marimba.intf.util.IConfig;
import com.marimba.intf.util.IObserver;
import com.marimba.tools.config.ConfigProps;
import static com.marimba.webapps.intf.IWebAppsConstants.APP_MAIN;

import javax.servlet.ServletContext;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Scheduled task called on Wake schedule from CMS
 * @author Tamilselvan Teivasekamani
 *
 */

public class WoWTask implements IErrorConstants, LogConstants, ITask, IObserver {

    private ITenant tenant;
    private ITaskMgr taskMgr;
    private IConfig taskConfig;
    private IWakeUpJob wakeJob;
    private IWakeManager wakeMgr;
    private ITaskContext taskCtxt;
    private SubscriptionMain smmain;
    private boolean isSchedulesRemoved = false;
    private String MODULE_NAME = "Policy WoW Task";

    static Calendar cal;
    static long tzOffset;
    static int SECOND = 1000;

    static {
        cal = new GregorianCalendar(TimeZone.getDefault());
        offset(System.currentTimeMillis());
    }

    public void init(ITaskContext itaskCtxt) throws TaskException {
        this.taskCtxt = itaskCtxt;
        this.tenant = itaskCtxt.getTenant();
        this.wakeMgr = tenant.getWakeManager();
        this.taskMgr = itaskCtxt.getTaskMgr();
        ServletContext servletctx = (ServletContext) itaskCtxt.getFeature("servletContext");
        TenantAttributes attribute = (TenantAttributes) ((Map) servletctx.getAttribute("tenantAttributes")).get(tenant.getName());
        this.smmain = attribute.getTenantMain();
    }

    public void execute(ITaskRuntime taskRuntime) throws TaskException {

        ITaskResult taskResult = taskRuntime.getResult();

        String targetdn = taskCtxt.getProperty(PROP_TARGET_DN);
        debug("Execute method called for the target : " + targetdn);

        if (null == targetdn || targetdn.isEmpty()) {
            System.out.println("WARNING: WOWTask - Targetdn is null or empty, WoW task will not be executed");
            return;
        }

        String targetName = taskCtxt.getProperty(PROP_TARGET_NAME);
        String targetType = taskCtxt.getProperty(PROP_TARGET_TYPE);
        String timeZone = taskCtxt.getProperty(PROP_TASK_TIMEZONE);
        String currentSch = taskCtxt.getProperty(PROP_TASK_SCHEDULE);
        String urgentTask = taskCtxt.getProperty(PROP_TARGET_URGENTTASK);
        String taskId = getTaskId(targetName, timeZone);

        try {
            // Fix : SW00427139 - WOW task fails as recurring scheduled becomes "UNKNOWN" after upgrading CMS from 8.1.01.008 and 8.2.00.003
            // The tasks created by old than Policy Manager 8.2.01 does not have this property.
            // In this case looking for the LDAP for it's type
            debug("***************** WoW task started for target: " + taskId);
            if (null == targetType || targetType.isEmpty()) {
                debug("Target type is null or unknown, so looking for it's type from LDAP to create WOW task");
                targetType = wakeMgr.getTargetsObjectClass(targetdn);
                targetType = (null == targetType || targetType.trim().isEmpty() ? "unknown" : targetType);
                debug("Target type after LDAP look up : " + targetType);
            }

            String target = getTaskId(targetName, timeZone);

            if (isWoWEnabled()) {
                ITaskHandle task = taskMgr.getTask(TASK_NAME_POLICY, getTaskId(targetName, timeZone));
                this.taskConfig = task.getConfig();
                this.taskConfig.addObserver(this, 0, 0);
                this.wakeJob = wakeMgr.getNewJob(target);
                taskConfig.setProperty("task.id", wakeJob.getJobID());
                this.wakeJob.setTargetGroup(targetdn.toLowerCase(), targetType, timeZone);
                this.wakeJob.execute();
                log(LOG_AUDIT_WOW_START, LOG_INFO, MODULE_NAME, null, this.wakeJob.getJobID());

                while (wakeJob.isRunning()) {
                    try {
                        Thread.sleep(15 * 1000);
                    } catch (InterruptedException ine) {
                        ine.printStackTrace();
                    }
                    log(LOG_AUDIT_WOW_WAIT, LOG_INFO, MODULE_NAME, null, this.wakeJob.getJobID());
                }
                this.taskConfig.removeObserver(this);
            } else {
                log(LOG_AUDIT_WOW_DISABLED, LOG_INFO, MODULE_NAME, null, "Wake On Wan settings currently disabled, use WOW settings link under Features Management tab in System settings page to enable.");
            }
            this.wakeJob = null;
            long lastInvokeTime = System.currentTimeMillis();
            if (taskCtxt.getProperty(PROP_TASK_LAST_INVOKE_TIME) != null) {
                lastInvokeTime = Long.parseLong(taskCtxt.getProperty(PROP_TASK_LAST_INVOKE_TIME));
            }

            String schList = taskCtxt.getProperty(PROP_TASK_SCHEDULE_LIST);

            List scheduleList = new ArrayList(0);
            if (schList != null && schList.length() > 0) {
                convertScheduleString(schList, scheduleList);
            }
            boolean isDeleted = "true".equals(taskConfig.getProperty(PROP_TASK_DELETED));
            if (!isDeleted && !isSchedulesRemoved && scheduleList.size() > 0) {
                boolean isAnyTZTaskDone = false;
                if (null == urgentTask) {
                    isAnyTZTaskDone = createNewTzCMSTask(targetdn, targetName, targetType, scheduleList, currentSch);
                }
                //Check the schedule has next invocation time, otherwise remove from list
                long now = System.currentTimeMillis();
                Iterator iterSch = scheduleList.iterator();
                while (iterSch.hasNext()) {
                    Schedule sched = (Schedule) iterSch.next();
                    debug("Validate schedule : " + sched.toString());
                    ScheduleInfo info = Schedule.getScheduleInfo(sched.toString());
                    if (info.getFlag(CALENDAR_PERIOD) == DAILY ||
                            info.getFlag(CALENDAR_PERIOD) == WEEKLY ||
                            info.getFlag(CALENDAR_PERIOD) == MONTHLY ) {
                        long schNext = sched.nextTime(lastInvokeTime, now);
                        if (-1 == schNext) {
                            debug("Schedule has no nextTime() and is -1");
                            iterSch.remove();
                            continue;
                        }
                    } else {
                        if (null != timeZone) {
                            long curTime = convertScheduleLongValue(sched.toString());
                            long tzTime = taskMgr.getTZSchedule(timeZone, curTime);
                            if (tzTime < System.currentTimeMillis()) {
                                debug("Onetime Schedule is already Expired, so removing it.");
                                iterSch.remove();
                                continue;
                            }
                        }
                        if (ScheduleUtils.isScheduleStarted(Schedule.getScheduleInfo(sched.toString()))) {
                            debug("Onetime Schedule is already started, so removing it.");
                            iterSch.remove();
                            continue;
                        }
                    }
                }
                isDeleted = "true".equals(taskConfig.getProperty(PROP_TASK_DELETED));
                if (!isDeleted && !isSchedulesRemoved && scheduleList.size() > 0) {
                    //Sort for latest next
                    Collections.sort(scheduleList, new ScheduleComparator(lastInvokeTime, timeZone));
                    debug("After schedule sort - " + convertSchedules(scheduleList));
                    IConfig cfg = new ConfigProps(null);
                    Schedule nextWakeSchedule = (Schedule) scheduleList.get(0);

                    cfg.setProperty(PROP_TASK_SCHEDULE, nextWakeSchedule.toString());
                    cfg.setProperty(PROP_TARGET_DN, targetdn);
                    cfg.setProperty(PROP_TARGET_TYPE, targetType);
                    cfg.setProperty(PROP_TASK_TIMEOUT, "-1");
                    cfg.setProperty(PROP_TASK_ONE_TIME, "true");
                    cfg.setProperty(PROP_TARGET_NAME, targetName);
                    cfg.setProperty(PROP_TASK_TENANT_NAME, tenant.getName());
                    if (null != timeZone) {
                        cfg.setProperty(PROP_TASK_TIMEZONE, timeZone);
                    }
                    cfg.setProperty(PROP_TASK_LAST_INVOKE_TIME, Long.toString(System.currentTimeMillis()));
                    cfg.setProperty(PROP_TASK_SCHEDULE_LIST, convertSchedules(scheduleList));

                    debug("Next schedule list for target \"" + taskId + "\" is: " + cfg.getProperty("target.ScheduleList"));
                    isDeleted = "true".equals(taskConfig.getProperty(PROP_TASK_DELETED));
                    if (null != cfg.getProperty(PROP_TASK_TIMEZONE) || !isAnyTZTaskDone) {
                        if (!isDeleted &&  !isSchedulesRemoved && taskMgr.addTask(TASK_GROUP_NAME_POLICY, TASK_NAME_POLICY, target, cfg, true))
                            log(LOG_AUDIT_WOW_SCHEDULE_SUCCESS, LOG_INFO, MODULE_NAME, null, target + " at \"" + nextWakeSchedule.toString() + "\"");
                        else
                            log(LOG_AUDIT_WOW_SCHEDULE_FAIL, LOG_WARNING, MODULE_NAME, null, target);
                    }
                } else {
                    taskMgr.removeTask(TASK_NAME_POLICY, target);
                    System.out.println("WOWTask: No pending schedule found for target -"+ target);
                }
            } else {
                taskMgr.removeTask(TASK_NAME_POLICY, target);
                debug("No further schedules found for the target: " + taskId + " and removed the task from task manager");
            }
        } catch (Exception exp) {
            if(DEBUG) exp.printStackTrace();
            System.out.println("WARNING: WOWTask failed for the target: " + targetdn + " Exception Msg: " + exp.toString());
            taskResult.setStatus(STATUS_FAILURE, "Failed wake-up task");
        }
        debug("***************** WoW task done for target: " + taskId);
    }

    public void destroy() {}

    /**
     * task.interrupted:
     *   This key will be update by CMS while we stop the running task and same will be listened by Policy Manager.
     *   If the key is set to "true" then current running task will be cancelled and will be scheduled to next occurrence if it’s configured so.
     *
     * task.deleted
     *   This key will be update by CMS while we delete the running task and same will be listened by Policy Manager.
     *   If the key is set to "true" then current running task will be cancelled and also will be removed from the task manager.
     *   It will not be rescheduled to re-occurrence even though it’s configured so.
     *
     * task.schedules.removed
     *   This key will be update by Policy Manager while we editing policies.
     *   If we removed a WoW schedule for a target which is already configured and running then it will be removed from task manager
     *   and will not be scheduled further even though once the task is completed.
     *
     * */

    public void notify(Object sender, int msg, Object arg) {
        String key = (String) arg;
        switch (msg) {
            case IConfig.CONFIG_CHANGED :
                if (PROP_TASK_SCHEDULES_REMOVED.equalsIgnoreCase(key)) {
                    this.isSchedulesRemoved = true;
                    System.out.println("WARNING: WoW Task schedules are removed for the target: " + taskCtxt.getProperty("targetdn"));
                }
                break;
            default :
                break;
        }
    }

    private boolean cancel(String taskId) {
        log(LOG_AUDIT_WOW_CANCEL, LOG_MAJOR, MODULE_NAME, null, taskId);
        if (null != this.wakeJob) this.wakeJob.cancelTask();
        return true;
    }

    protected String getTaskId(String targetName, String timeZone) {
        if (null == timeZone) {
            return targetName;
        }
        String taskId = targetName + "_" + timeZone;
        taskId = taskId.replace("/", "_");
        return taskId;
    }

    /**
     * Recalculate the timezone offset for the specified date in milliseconds
     * UTC.
     *
     * @param tm DOCUMENT ME
     */
    protected static void offset(long tm) {
        synchronized (cal) {
            cal.setTime(new Date(tm));
            tzOffset = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);
        }
    }
    /**
     * Convert from UTC milliseconds to local milliseconds. e.g. 16:00 GMT +
     * (-8) = 8:00 PST
     *
     * @param tm DOCUMENT ME
     *
     * @return DOCUMENT ME
     */
    long inMS(long tm) {
        debug("tzOffset = " + tzOffset);
        if (tm <= 0) {
            return tm;
        }

        return tm + tzOffset;
    }
    /**
     * Convert from local milliseconds to UTC milliseconds. e.g. 8:00 PST -
     * (-8) = 16:00 GMT
     *
     * @param tm DOCUMENT ME
     *
     * @return DOCUMENT ME
     */
    static long outMS(long tm) {
        if (tm <= 0) {
            return tm;
        }

        return tm - tzOffset;
    }

    private static long convertScheduleLongValue(String schedule) {
        int index = schedule.indexOf(" ");
        schedule = schedule.substring(index+1);
        DateFormat mbraFormat = new SimpleDateFormat("MM/dd/yyyy@hh:mma");
        Date date = null;
        long time = -1;
        try {
            date = (Date)mbraFormat.parse(schedule);
            time = date.getTime();
        } catch (ParseException e) {
            return -1;
        }
        return time;
    }
    /**
     * Convert schedule string into list of schedule object with delimiter #
     *
     * @param schedules schedule list string
     * @param scheduleList List where schedules to be added
     */
    private void convertScheduleString(String schedules, List scheduleList) {
        String scheduleStr[] = schedules.split("#");
        if (scheduleStr == null) {
            return;
        }
        for (String aScheduleStr : scheduleStr) {
            if (scheduleStr.length > 0) {
                try {
                    scheduleList.add(Schedule.readSchedule(aScheduleStr));
                } catch (Exception ex) {
                    debug("Schedule conversion error : " + ex.toString());
                }
            }
        }
    }

    /**
     * Converts schedules into strings with separator #. It only adds schedule has next invoke time
     *
     * @param schedules
     * @return
     */
    public String convertSchedules(List schedules) {
        if (schedules.isEmpty()) {
            return "";
        }
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < schedules.size() ; i++) {
            Schedule sched = (Schedule) schedules.get(i);
            buff.append(sched.toString());
            if(i != (schedules.size() - 1)) {
                buff.append("#");
            }
        }
        return buff.toString();
    }

    /**
     * Compares to schedules
     */
    class ScheduleComparator implements Comparator {
        private long lastInvokeTime = -1;
        private String timeZone = null;

        public ScheduleComparator(long lastInvokeTime, String timeZone) {
            this.lastInvokeTime = lastInvokeTime;
            this.timeZone = timeZone;
        }
        public int compare(Object first, Object next) {

            Schedule sched1 = (Schedule) first;
            Schedule sched2 = (Schedule) next;

            long now = System.currentTimeMillis();
            long sch1Next = -1;
            long sch2Next = -1;

            if (null != timeZone) {
                sch1Next = offsetDecode(timeZone, sched1.nextTime(offsetEncode(timeZone, lastInvokeTime), offsetEncode(timeZone, now + (61 * SECOND))));
                sch2Next = offsetDecode(timeZone, sched2.nextTime(offsetEncode(timeZone, lastInvokeTime), offsetEncode(timeZone, now + (61 * SECOND))));
            } else {
                sch1Next = outMS(sched1.nextTime(inMS(lastInvokeTime), inMS(now + (61 * SECOND))));
                sch2Next = outMS(sched2.nextTime(inMS(lastInvokeTime), inMS(now + (61 * SECOND))));
            }

            if (sch2Next < sch1Next) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    long offsetDecode(String timezone, long tm) {
        if (tm <= 0) {
            return tm;
        }
        if (null == timezone) {
            return inMS(tm);
        }
        long offset = getTimezoneOffset (timezone, tm);
        if (DEBUG) {
            System.out.println("Method offsetDecode() :Timezone Offset for "+ timezone+ " = " + offset);
            System.out.println("offsetDecode() : "+ (tm - offset));
        }
        return tm - offset;
    }

    long offsetEncode(String timezone, long tm) {
        if (tm <= 0) {
            return tm;
        }
        if (null == timezone) {
            return inMS(tm);
        }
        long offset = getTimezoneOffset (timezone, tm);
        if (DEBUG) {
            System.out.println("Method offsetEncode() : Timezone Offset for "+ timezone+ " = " + offset);
            System.out.println("offsetEncode() : "+ (tm + offset));
        }
        return tm + offset;
    }

    protected long getTimezoneOffset(String timezone, long tm) {
        if (null != timezone) {
            Calendar tzCal = new GregorianCalendar(TimeZone.getTimeZone(timezone));
            TimeZone tz = TimeZone.getTimeZone(timezone);
            if (tz != null) {
                debug("Raw offset of PST: " + tz.getRawOffset());
            }
            tzCal.setTime(new Date(tm));
            return tzCal.get(Calendar.ZONE_OFFSET) + tzCal.get(Calendar.DST_OFFSET);
        }
        return tzOffset;
    }

    /**
     * Create WOW task for newly added time zone machines of currently executing schedule
     *
     * Note: Make sure the new timezone is ahead of current timezone and the schedule is recursive
     *
     * @param targetdn TargetDN for which the WOW task applicable
     * @param targetName Target Name to be used for task creation
     * @param targetType Target type ex. collection, group, machine or user
     * @param scheduleList The schedule list assigned from Policy Manager
     * @param currSchedule The current task instance schedule string
     * @return list tz list
     */
    private boolean createNewTzCMSTask(String targetdn, String targetName, String targetType, List scheduleList, String currSchedule) {

        boolean isTZAvailable = false;
        IDBQueryExecutor queryExecutor = wakeMgr.getDBQueryExecutor();
        List<String> listTZ = queryExecutor.getTimeZones(targetdn, targetType);

        if (listTZ.isEmpty()) {
            return isTZAvailable;
        }
        for (String tzObj : listTZ) {
            String timeZone = tzObj.trim();
            boolean createTask = false;
            ScheduleInfo info = Schedule.getScheduleInfo(currSchedule);
            if( info.getFlag(CALENDAR_PERIOD) == DAILY ||
                    info.getFlag(CALENDAR_PERIOD) == WEEKLY ||
                    info.getFlag(CALENDAR_PERIOD) == MONTHLY ) {
                //Recursive schedule, hence create task
                createTask = true;
            } else {
                //Analyse one-time schedules
                long tzTime = taskMgr.getTZSchedule(timeZone, System.currentTimeMillis());
                if (tzTime < System.currentTimeMillis()) {
                    //Skip task create as the selected time zone time is less than current time
                    if (DEBUG) {
                        DateFormat mbraFormat = new SimpleDateFormat("MM/dd/yyyy@hh:mma");
                        System.out.println("WOWTask: One time task, schedule passed CMS time zone : " + mbraFormat.format(tzTime) + " Timezone :"+ timeZone);
                    }
                } else {
                    //The time is yet to occur
                    createTask = true;
                }
            }

            if (createTask) {
                String target = (targetName + "_" + timeZone).replace("/", "_");
                ITaskHandle task = taskMgr.getTask(TASK_NAME_POLICY, target);
                if (null == task) {
                    IConfig cfg = new ConfigProps(null);

                    cfg.setProperty(PROP_TASK_TIMEOUT, "-1");
                    cfg.setProperty(PROP_TARGET_DN, targetdn);
                    cfg.setProperty(PROP_TASK_ONE_TIME, "true");
                    cfg.setProperty(PROP_TARGET_NAME, targetName);
                    cfg.setProperty(PROP_TARGET_TYPE, targetType);
                    cfg.setProperty(PROP_TASK_TIMEZONE, timeZone);
                    cfg.setProperty(PROP_TASK_SCHEDULE, currSchedule);
                    cfg.setProperty(PROP_TASK_TENANT_NAME, tenant.getName());
                    cfg.setProperty(PROP_TASK_SCHEDULE_LIST, convertSchedules(scheduleList));

                    debug("Next schedule list: " + cfg.getProperty(PROP_TASK_SCHEDULE_LIST));

                    boolean isDeleted = "true".equals(taskConfig.getProperty(PROP_TASK_DELETED));

                    if (!isDeleted && taskMgr.addTask(TASK_GROUP_NAME_POLICY, TASK_NAME_POLICY, target, cfg, true)) {
                        isTZAvailable = true;
                        log(LOG_AUDIT_WOW_SCHEDULE_SUCCESS, LOG_INFO, MODULE_NAME, null, target + " at \"" + currSchedule + "\"");
                    } else {
                        log(LOG_AUDIT_WOW_SCHEDULE_FAIL, LOG_WARNING, MODULE_NAME, null, target + " at \"" + currSchedule + "\"");
                    }
                }
            }
            if (isTZAvailable) {
                ITaskHandle task = taskMgr.getTask(TASK_NAME_POLICY, targetName);
                if (null != task) {
                    System.out.println("WOWTask: Remove the task for target - " + targetName);
                    taskMgr.removeTask(TASK_NAME_POLICY, targetName);
                }
            }
        }
        return isTZAvailable;
    }

    private boolean isWoWEnabled() {
        return Boolean.valueOf(tenant.getConfig().getProperty(ENABLE_WOW_FEATURE));
    }

    private void debug(String msg) {
        if (DEBUG) System.out.println("WOWTask: " + msg);
    }

    /**
     * Method used to log audit messages to common audit log
     *
     * @param id  Message identifier or number
     * @param severity Message Severity
     * @param source Module generating message
     * @param user User doing current action
     * @param message Log message or Throwable to capture exceptions
     */
    private void log(int id, int severity, String source, String user, String message ) {
        if (null != smmain) {
            smmain.log(id, tenant.getName(), severity, source, user, message, null);
        }
    }
}