// Copyright 1997-2010, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.forms;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

/**
 * This form is called when visiting the Intel vPro PCAlarm Settings page. It is used for set and
 * get the Vpro PCAlarm Clock form elements
 *
 * @author Selvaraj Jegatheesan
 */

public class vProPCAlarmSettingsForm extends AbstractForm {
    private SimpleDateFormat mrbadf = new SimpleDateFormat("MM/dd/yyyy@hh:mma", java.util.Locale.US);
    private DateFormat systemFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT,
            DateFormat.SHORT, java.util.Locale.US);
    String DEFAULT_VALUE = null;
    String enablePCAlarm = null;
    String applyAlarm = null;
    String vpro_alarm_none = null;
    String setAlarmSetting = null;
    String vProPCAlarmClkPriority = null;
    String clrAlarmClock = null;
    String alarmStartTime = null;
    String wakeIntervalDays = null;
    String wakeIntervalhrmin = null;
    String forceApply = null;

    public void reset(ActionMapping mapping,
                      HttpServletRequest request) {
        setValue(ENABLE_AMT_ALARM,INVALID);
        setValue(AMT_ALARM_STARTTIME, getCurrentTime());
        setValue(AMT_ALARM_WAKEINTERVAL_DAYS, AMT_ALARM_DAYS_DEFAULT);
        setValue(AMT_ALARM_WAKEINTERVAL_HRMIN, AMT_ALARM_TIME_DEFAULT);
        setValue(AMT_ALARM_PRIORITY, NOTAVBLE);
        setValue(AMT_ALARM_FORCEAPPLY, INVALID);
    }

    private String getCurrentTime() {
        String cuuretnTime = null;
        try {
            Calendar cal = Calendar.getInstance();
            cuuretnTime = systemFormat.format(cal.getTime());
        } catch (Exception e) {
            return null;
        }
        return cuuretnTime;
    }

    private String getCurrentTimeMrbaFormat() {
        String cuuretnTime = null;
        try {
            Calendar cal = Calendar.getInstance();
            cuuretnTime = mrbadf.format(cal.getTime());
        } catch (Exception e) {
            return null;
        }
        return cuuretnTime;
    }

    public void setEnablePCAlarm(String enablePCAlarm) {
        this.enablePCAlarm = enablePCAlarm;
    }

    public String getEnablePCAlarm() {
        return enablePCAlarm;
    }

    public void setApplyAlarm(String applyAlarm) {
        this.applyAlarm = applyAlarm;
    }

    public String getApplyAlarm() {
        return applyAlarm;
    }

    public void setVpro_alarm_none(String vpro_alarm_none) {
        this.vpro_alarm_none = vpro_alarm_none;
    }

    public String getVpro_alarm_none() {
        return vpro_alarm_none;
    }

    public void setSetAlarmSetting(String setAlarmSetting) {
        this.setAlarmSetting = setAlarmSetting;
    }

    public String getSetAlarmSetting() {
        return setAlarmSetting;
    }
    public void setVProPCAlarmClkPriority(String vProPCAlarmClkPriority) {
        this.vProPCAlarmClkPriority = vProPCAlarmClkPriority;
    }

    public String getVProPCAlarmClkPriority() {
        return vProPCAlarmClkPriority;
    }
    public void setClrAlarmClock(String clrAlarmClock) {
        this.clrAlarmClock = clrAlarmClock;
    }

    public String getClrAlarmClock() {
        return clrAlarmClock;
    }
    public void setAlarmStartTime(String alarmStartTime) {
        this.alarmStartTime = alarmStartTime;
    }

    public String getAlarmStartTime() {
        return alarmStartTime;
    }
    public void setWakeIntervalDays(String wakeIntervalDays) {
        this.wakeIntervalDays = wakeIntervalDays;
    }

    public String getWakeIntervalDays() {
        return wakeIntervalDays;
    }
    public void setWakeIntervalhrmin(String wakeIntervalhrmin) {
        this.wakeIntervalhrmin = wakeIntervalhrmin;
    }

    public String getWakeIntervalhrmin() {
        return wakeIntervalhrmin;
    }
    public void setForceApply(String forceApply) {
        this.forceApply = forceApply;
    }

    public String getForceApply() {
        return forceApply;
    }

    public String convertDateTimeSysToMrba(String dateTime) {
        try {
            dateTime = mrbadf.format(systemFormat.parse(dateTime));
        } catch(Exception e) {
            return getCurrentTimeMrbaFormat();
        }
        return dateTime;
    }

    public String convertDateTimeMrbaToSys (String dateTime) {
        try {
            dateTime = systemFormat.format(mrbadf.parse(dateTime));
        } catch(Exception e) {
            return getCurrentTime();
        }
        return dateTime;
    }
}
