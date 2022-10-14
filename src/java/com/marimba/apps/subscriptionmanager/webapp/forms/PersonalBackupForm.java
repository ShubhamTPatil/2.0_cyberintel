// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.forms;

import com.bmc.osm.object.BackupTemplates;
import com.marimba.webapps.intf.IScheduleConstants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * PersonalBackupForm
 *
 * @author	 Tamilselvan Teivasekamani
 * @version  $Revision$,  $Date$
 *
 */

public class PersonalBackupForm extends ScheduleEditForm {
    private boolean connected;

    public String priority;
    public String backupSetting;    
    public String selectedTemplate;
    public String actScheduleSetting;
    public String expScheduleSetting;
    public String backupScheduleSetting;

    public String activeSchedule;
    public String expireSchedule;

    private List<BackupTemplates> allPBtmplts;
    private Map<String, Object> props;

    private SimpleDateFormat mrbadf = new SimpleDateFormat("MM/dd/yyyy@hh:mma", java.util.Locale.US);
    private DateFormat systemFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT,
            DateFormat.SHORT, java.util.Locale.US);

    public PersonalBackupForm() {
        super();
        this.props = new HashMap<String, Object>();
        this.allPBtmplts = new ArrayList<BackupTemplates>();
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getBackupSetting() {
        return backupSetting;
    }

    public void setBackupSetting(String backupSetting) {
        this.backupSetting = backupSetting;
    }

    public String getBackupScheduleSetting() {
        return backupScheduleSetting;
    }

    public void setBackupScheduleSetting(String backupScheduleSetting) {
        this.backupScheduleSetting = backupScheduleSetting;
    }

    public String getActScheduleSetting() {
        return actScheduleSetting;
    }

    public void setActScheduleSetting(String actScheduleSetting) {
        this.actScheduleSetting = actScheduleSetting;
    }

    public String getActiveSchedule() {
        return activeSchedule;
    }

    public void setActiveSchedule(String activeSchedule) {
        this.activeSchedule = activeSchedule;
    }

    public String getExpScheduleSetting() {
        return expScheduleSetting;
    }

    public void setExpScheduleSetting(String expScheduleSetting) {
        this.expScheduleSetting = expScheduleSetting;
    }

    public String getExpireSchedule() {
        return expireSchedule;
    }

    public void setExpireSchedule(String expireSchedule) {
        this.expireSchedule = expireSchedule;
    }

    public String getSelectedTemplate() {
        return selectedTemplate;
    }

    public void setSelectedTemplate(String selectedTemplate) {
        this.selectedTemplate = selectedTemplate;
    }

    public List<BackupTemplates> getAllPBtmplts() {
        return allPBtmplts;
    }

    public void setAllPBtmplts(List<BackupTemplates> allPBtmplts) {
        this.allPBtmplts = allPBtmplts;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setValue(String property, Object value) {
        if (value instanceof String[]) {
            props.put(property, ((String[]) value) [0]);
        } else {
            if(null == props){
                props = new HashMap<String, Object>();
            }
            props.put(property, value);
        }
    }

    public Object getValue(String property) {
        return props.get(property);
    }

    private String getCurrentTime() {
        long extra_mins = 900000l; // set 15 mins ahead from the current server time
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis() + extra_mins);
            return systemFormat.format(cal.getTime());
        } catch (Exception e) {
            return "";
        }
    }

    private String getCurrentTimeMrbaFormat() {
        try {
            Calendar cal = Calendar.getInstance();
            return mrbadf.format(cal.getTime());
        } catch (Exception e) {
            return "";
        }
    }

    public String convertDateTimeSysToMrba(String dateTime) {
        try {
            dateTime = mrbadf.format(systemFormat.parse(dateTime));
        } catch(Exception e) {
            System.out.println("Exception while parsing date time from system format to marimba format");
            return getCurrentTimeMrbaFormat();
        }
        return dateTime;
    }

    public String convertDateTimeMrbaToSys (String dateTime) {
        try {
            dateTime = systemFormat.format(mrbadf.parse(dateTime));
        } catch(Exception e) {
            System.out.println("Exception while parsing date time from marimba format to system format, returning current time");
            return getCurrentTime();
        }
        return dateTime;
    }

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        int daysPropLength = IScheduleConstants.SCHEDULE_DAYS_PROPERTY_NAMES.length;
        for (int i = 0; i < daysPropLength; i++) {
            if (getString(IScheduleConstants.SCHEDULE_DAYS_PROPERTY_NAMES[i]) != null) {
                map.remove(IScheduleConstants.SCHEDULE_DAYS_PROPERTY_NAMES[i]);
            }
        }
        map.remove(IScheduleConstants.SCHEDULE_INTERVAL_DAY_OF_MONTH);
        map.remove(IScheduleConstants.SCHEDULE_INTERVAL_DAYS);
        map.remove(IScheduleConstants.SCHEDULE_INTERVAL_DAYS_TYPE);
        map.remove(IScheduleConstants.SCHEDULE_INTERVAL_WEEKS);
        map.remove(IScheduleConstants.SCHEDULE_INTERVAL_MINUTES);
        map.remove(IScheduleConstants.SCHEDULE_INTERVAL_MONTHS);
        map.remove(IScheduleConstants.SCHEDULE_TIME_INTERVAL_END_AM_PM);
        map.remove(IScheduleConstants.SCHEDULE_TIME_INTERVAL_END_HOUR);
        map.remove(IScheduleConstants.SCHEDULE_TIME_INTERVAL_END_MIN);
        map.remove(IScheduleConstants.SCHEDULE_TIME_INTERVAL_START_AM_PM);
        map.remove(IScheduleConstants.SCHEDULE_TIME_INTERVAL_START_HOUR);
        map.remove(IScheduleConstants.SCHEDULE_TIME_INTERVAL_START_MIN);
    }
}
