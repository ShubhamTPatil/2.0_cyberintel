// Copyright 1997-2010, BMC, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.util;

import org.apache.commons.validator.*;

import com.marimba.apps.subscriptionmanager.intf.*;
import com.marimba.apps.subscriptionmanager.webapp.forms.vProPCAlarmSettingsForm;

import com.marimba.webapps.intf.*;
import com.marimba.webapps.tools.util.*;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Validation for Intel vPro PC Alarm Clock Settings All form validation methods should be
 * written such that null is a possible input.
 *
 * @author Selvaraj Jegatheesan
 * @version 1.0, 10/15/2010
 */
public class vProAlarmSettingValidation
        implements IWebAppConstants,
        IErrorConstants{
    final static int STYLE = DateFormat.SHORT;

    /**
     * Validates Start Date and time , wake interval time and priority for Intel vPro PC Alarm Clcok settings.
     * Checking that each field is present (required), an integer, and its range.
     *
     * @param bean Bean validation is being performed on.
     * @param va The current ValidatorAction being performed
     * @param field Field object being validated.
     * @param errors The errors objects to add an ActionError to if the validation fails
     * @param request Current request object.
     * @param application The application's ServletContext.
     *
     * @return boolean false if there is no error, true is there is.
     */
    public static boolean validateAlarmClk(java.lang.Object                      bean,
                                           ValidatorAction                       va,
                                           Field                                 field,
                                           org.apache.struts.action.ActionErrors errors,
                                           javax.servlet.http.HttpServletRequest request,
                                           javax.servlet.ServletContext          application) {
        int priorityMin = 1;
        int priorityMax = 99999;
        int wakupDaysMax = 65535;

        String enablePCAlarm = (String) ((vProPCAlarmSettingsForm) bean).getValue("enablePCAlarm");
        if("true".equals(enablePCAlarm)) {

            String priority = ((String) ((vProPCAlarmSettingsForm) bean).getValue("vProPCAlarmClkPriority")).trim();
            if(!((NOTAPP.equals(priority)) || null == priority || "".equals(priority))) {
                try {
                    int vProAlarmClkPriority = Integer.parseInt(priority);
                    if(vProAlarmClkPriority == 0) {
                        errors.add("vProPCAlarmClkPriority", new KnownActionError(VALIDATION_VPROALARM_PRIORITY_INVALID, priority ));
                        return false;
                    }
                    if(!((vProAlarmClkPriority >= priorityMin) && (vProAlarmClkPriority<= priorityMax))) {
                        errors.add("vProPCAlarmClkPriority", new KnownActionError(VALIDATION_VPROALARM_PRIORITY_INVALID, priority ));
                        return false;
                    }
                } catch(NumberFormatException ne) {
                    errors.add("vProPCAlarmClkPriority", new KnownActionError(VALIDATION_VPROALARM_PRIORITY_INVALID, priority ));
                    return false;
                }
            }

            String applyAlarmSettings = (String) ((vProPCAlarmSettingsForm) bean).getValue("applyAlarm");
            if("true".equals(applyAlarmSettings)) {
                String alarmStartTime = (String) ((vProPCAlarmSettingsForm) bean).getValue("alarmStartTime");
                if(null == alarmStartTime || "".equals(alarmStartTime)) {
                    errors.add("alarmStartTime", new KnownActionError(VALIDATION_VPROALARM_STARTTIME_INVALID_FORMAT));
                    return false;
                }
                SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, request.getLocale());
                dateFormat.setLenient(false);

                try {
                    Date startDate = dateFormat.parse(alarmStartTime);
                    Date systemDate = new Date();
                    boolean beforeGivenDate = startDate.before(systemDate);
                    if(beforeGivenDate) {
                        errors.add("alarmStartTime", new KnownActionError(VALIDATION_VPROALARM_STARTTIME_INVALID));
                        return false;
                    }
                } catch(Exception e) {
                    errors.add("alarmStartTime", new KnownActionError(VALIDATION_VPROALARM_STARTTIME_INVALID_FORMAT));
                    return false;
                }
                String wakeDays = (String) ((vProPCAlarmSettingsForm) bean).getValue("wakeIntervalDays");
                if(!"".equals(wakeDays)) {
                    try {
                        int wakeupDays = Integer.parseInt(wakeDays);
                        if(wakeupDays > wakupDaysMax) {
                            errors.add("wakeIntervalDays", new KnownActionError(VALIDATION_VPROALARM_WAKEUPDAYS_INVALID));
                            return false;
                        }
                    } catch(Exception e) {
                        errors.add("wakeIntervalDays", new KnownActionError(VALIDATION_VPROALARM_WAKEUPDAYS_INVALID));
                        return false;
                    }
                }
                String wakeTime = (String) ((vProPCAlarmSettingsForm) bean).getValue("wakeIntervalhrmin");
                if(!"".equals(wakeTime)) {
                    if(!checkTime(wakeTime)) {
                        errors.add("wakeIntervalhrmin", new KnownActionError(VALIDATION_VPROALARM_WAKEUPHRMIN_INVALID));
                        return false;
                    }
                }
            }
        }
        return true;
    }
    private static boolean checkTime(String wakeTime) {
        int hoursMax = 23;
        int minsMax = 59;

        if(wakeTime.indexOf(":") == -1) return false;
        String[] intervalTimes = wakeTime.split(":");
        try {
            int wakeHours = Integer.parseInt(intervalTimes[0]);
            int wakeMins = Integer.parseInt(intervalTimes[1]);
            if(wakeHours > hoursMax ||  wakeMins > minsMax) {
                return false;
            }
        } catch(Exception e){
            return false;
        }
        return true;
    }
}

