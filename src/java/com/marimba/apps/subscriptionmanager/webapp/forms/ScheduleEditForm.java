// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.forms;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.webapps.intf.IScheduleConstants;
import com.marimba.webapps.tools.form.ScheduleForm;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Schedule page form
 *
 * @author Theen-Theen Tan
 * @author Venu Gopala Rao Kotha
 * @version 1.15, 11/29/2005
 */

public class ScheduleEditForm extends ScheduleForm
        implements IAppConstants, IWebAppConstants, ISubscriptionConstants {

    public final boolean DEBUG = IAppConstants.DEBUG;
    public final boolean DEBUG2 = IAppConstants.DEBUG2;

    public static final String ACTIVE = "active";
    public static final String SET_SCHEDULE = "SET_SCHEDULE";
    public static final String ACTIVE_PERIOD = "ACTIVE_PERIOD";
    public static final String ENABLE_WOW_ON_SEC = "ENABLE_WOW_ON_SEC";
    public static final String ENABLE_WOW_ON_INIT = "ENABLE_WOW_ON_INIT";
    public static final String ENABLE_WOW_ON_UPDATE = "ENABLE_WOW_ON_UPDATE";
    public static final String ENABLE_WOW_ON_REPAIR = "ENABLE_WOW_ON_REPAIR";
    
    public static final String ACTIVE_PERIOD_END = ACTIVE_PERIOD + "_END";
    public static final String ACTIVE_PERIOD_START = ACTIVE_PERIOD + "_START";
    public static final String ACTIVE_PERIOD_SEMANTICS = ACTIVE_PERIOD + "_SEMANTICS";
    public static final String ACTIVE_PERIOD_START_EDIT = ACTIVE_PERIOD_START + "_EDIT";
    public static final String ACTIVE_PERIOD_END_DATETIME =  ACTIVE_PERIOD_END + "_DATETIME";
    public static final String ACTIVE_PERIOD_START_DATETIME =  ACTIVE_PERIOD_START + "_DATETIME";

    //private SimpleDateFormat mrbadf = new SimpleDateFormat("MM/dd/yyyy@hh:mma");
    private SimpleDateFormat mrbadf = new SimpleDateFormat("MM/dd/yyyy@hh:mma", java.util.Locale.US);
    
    private String type = PHASE_INIT;
    private int channelId;
    private int targetchmapId;
    private String forward;
    private String setSchedule;
    private Locale locale = Locale.getDefault();

    public void init(Locale locale) {
        map.clear();
        this.locale = locale;
        super.init();
    }

    public void init() {
        map.clear();
        super.init();
    }

    public String getType() {
        return type;
    }

    public String getSchedulePeriod() {
        StringBuffer buffer = new StringBuffer();
        try {
            SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
            if ("true".equals(getValue(ACTIVE_PERIOD_START))) {
                buffer.append(ACTIVE + " ");
                buffer.append(mrbadf.format(sdf.parse(getValue(ACTIVE_PERIOD_START_DATETIME).toString())));
                if ("true".equals(getValue(ACTIVE_PERIOD_END))) {
                    buffer.append(" - ");
                    buffer.append(mrbadf.format(sdf.parse(getValue(ACTIVE_PERIOD_END_DATETIME).toString())));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    public void setSchedulePeriod(String schedulePeriod) {
        SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE),0,0,0);
        if (schedulePeriod == null) {
            setValue(ACTIVE_PERIOD_START, "false");
            setValue(ACTIVE_PERIOD_END, "false");
            setValue(ACTIVE_PERIOD_START_DATETIME, sdf.format(calendar.getTime()));
            setValue(ACTIVE_PERIOD_END_DATETIME, sdf.format(calendar.getTime()));
        } else {
            schedulePeriod = schedulePeriod.substring(ACTIVE.length());
            int index =  schedulePeriod.indexOf("-");
            try {
                if (index == -1) {
                    Date date = mrbadf.parse(schedulePeriod);
                    setValue(ACTIVE_PERIOD_START, "true");
                    setValue(ACTIVE_PERIOD_START_DATETIME, sdf.format(date));
                    setValue(ACTIVE_PERIOD_END, "false");
                    setValue(ACTIVE_PERIOD_END_DATETIME, sdf.format(calendar.getTime()));
                } else {
                    setValue(ACTIVE_PERIOD_START, "true");
                    Date date = mrbadf.parse(schedulePeriod.substring(0, index));
                    setValue(ACTIVE_PERIOD_START_DATETIME, sdf.format(date));
                    setValue(ACTIVE_PERIOD_END, "true");
                    date =  mrbadf.parse(schedulePeriod.substring(index + 1));
                    setValue(ACTIVE_PERIOD_END_DATETIME, sdf.format(date));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * REMIND
     *
     * @param type REMIND
     */
    public void setType(String type) {
        this.type = type;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public int getTargetchmapId() {
        return targetchmapId;
    }

    public void setTargetchmapId(int targetchmapId) {
        this.targetchmapId = targetchmapId;
    }

    public void setValue(String property, Object value) {
        if (DEBUG2) {
            if (value != null) {
                System.out.println("ScheduleEditForm: Set " + property + " ," + value.toString());
            }
        }

        if (value instanceof String[]) {
            // Set being called from the GUI
            String[] valueArray = (String[]) value;

            if (valueArray.length > 0) {
                String valueStr = ((String[]) value)[0];
                if (DEBUG) System.out.println("ScheduleEditForm: Set " + property + " ," + valueStr);
            }
        } else {
            // Set being called from within the class
            map.put(property, value);
        }
    }

    /**
     * REMIND
     *
     * @param property REMIND
     *
     * @return REMIND
     */
    public Object getValue(String property) {
        Object value = null;
        value = map.get(property);
        if (DEBUG2) System.out.print("ScheduleEditForm: Get " + property + " ");

        if (value != null) {
            if (DEBUG2) System.out.println(value.toString());
            return value;
        } else {
            return "";
        }
    }

    public void setNewSched(boolean isEdit) {
        setValue(ACTIVE_PERIOD_START_EDIT, new Boolean(isEdit).toString());
    }

    public String getProperty(String key) {
        return (String) getValue(key);
    }

    public void setForward(String forward) {
        setValue("forward",forward);
        this.forward = forward;
    }

    public String getForward() {
        getValue("forward");
        return this.forward;
    }

    public void setScheduleString(String schStr) {
        if (schStr == null || "".equals(schStr)||"null".equals(schStr)) {
            schStr = null;
        }
        if (schStr == null) {
            setValue("SET_SCHEDULE", "false");
        } else {
            setValue("SET_SCHEDULE", "true");
        }
        super.setSchedule(updateSchduleString(schStr));
    }


    // Adding this method due to the differences in schedule.java class toString() method
    // and ScheduleUtils.SetDaySchedule and timeSchedule() methods.
    public String updateSchduleString(String schedule) {
        if (schedule == null || "".equals(schedule)) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        String token = null;
        String SPACE = " ";
        StringTokenizer tokens = new StringTokenizer(schedule);
        while (tokens.hasMoreTokens()) {
            token = tokens.nextToken();
            if ("EVERY".equalsIgnoreCase(token)) {
                buffer.append(token).append(SPACE);
                token = tokens.nextToken();
                if ("DAY".equalsIgnoreCase(token)) {
                    buffer.append("1 DAYS").append(SPACE);
                    continue;
                } else if ("WEEK".equalsIgnoreCase(token)) {
                    buffer.append("1 WEEKS").append(SPACE);
                    continue;
                } else if ("MINUTE".equalsIgnoreCase(token)) {
                    buffer.append("1 MINUTES").append(SPACE);
                    continue;
                } else if ("HOUR".equalsIgnoreCase(token)) {
                    buffer.append("1 HOURS").append(SPACE);
                    continue;
                }
            } else if ("DAY".equalsIgnoreCase(token)) {
                buffer.append(token).append(SPACE);
                buffer.append(tokens.nextToken()).append(SPACE);
                // skip "every"
                buffer.append(tokens.nextToken()).append(SPACE);
                token = tokens.nextToken();
                if ("MONTH".equalsIgnoreCase(token)) {
                    buffer.append("1 MONTHS").append(SPACE);
                    continue;
                }
            }
            buffer.append(token).append(SPACE);
        }

        if (DEBUG) {
            System.out.println("UPDATED SCHEDULE STRING:"+ buffer.toString());
        }

        return buffer.toString().trim();
    }

    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        MessageResources resources = (MessageResources) this.getServlet().getServletConfig().getServletContext().getAttribute(Globals.MESSAGES_KEY);
        ActionErrors errors = super.validate(mapping, request);
        if ("true".equals(getValue(SET_SCHEDULE)) && !"cancel".equals(request.getParameter("action"))) {
            super.validateSchedule(resources, errors, request.getLocale());
        }
        return errors;
    }

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        int daysPropLength = IScheduleConstants.SCHEDULE_DAYS_PROPERTY_NAMES.length;
        for (int i = 0; i < daysPropLength; i++) {
            if (getString(IScheduleConstants.SCHEDULE_DAYS_PROPERTY_NAMES[i]) != null) {
                map.remove(IScheduleConstants.SCHEDULE_DAYS_PROPERTY_NAMES[i]);
            }
        }
        map.remove(ENABLE_WOW_ON_INIT);
        map.remove(ENABLE_WOW_ON_SEC);
        map.remove(ENABLE_WOW_ON_UPDATE);
        map.remove(ENABLE_WOW_ON_REPAIR);
    }
}
