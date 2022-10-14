// Copyright 2009, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.forms;

import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscription.common.ISubscriptionConstants;

import com.marimba.webapps.intf.IMapProperty;
import com.marimba.webapps.intf.IScheduleConstants;
import com.marimba.webapps.tools.form.ScheduleForm;
import com.marimba.webapps.tools.util.KnownActionError;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionError;
import org.apache.struts.util.MessageResources;
import org.apache.struts.Globals;

import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;

/**
 * Action form for Reboot Settings page
 *
 * @author Marie Antoine (Tony)
 */

public class CommonRebootSettingsForm extends ScheduleForm
                                      implements  ISubscriptionConstants,
                                                  IAppConstants,
                                                  IWebAppConstants,
                                                  IMapProperty {
    
    public static final boolean DEBUG = IAppConstants.DEBUG;
    public static final boolean DEBUG2 = IAppConstants.DEBUG2;
    public static final String ACTIVE_PERIOD = "ACTIVE_PERIOD";
    public static final String ACTIVE_PERIOD_START = ACTIVE_PERIOD + "_START";
    public static final String ACTIVE_PERIOD_START_EDIT = ACTIVE_PERIOD_START + "_EDIT";
    public static final String ACTIVE_PERIOD_END = ACTIVE_PERIOD + "_END";
    public static final String ACTIVE_PERIOD_SEMANTICS = ACTIVE_PERIOD + "_SEMANTICS";
    public static final String SET_REBOOT_SCHEDULE = "SET_REBOOT_SCHEDULE";
    public static final String ACTIVE_PERIOD_START_DATETIME =  ACTIVE_PERIOD_START + "_DATETIME";
    public static final String ACTIVE_PERIOD_END_DATETIME =  ACTIVE_PERIOD_END + "_DATETIME";

    private String type = PHASE_INIT;
    private int channelId;
    private int targetchmapId;
    private String forward;
    private Locale locale = Locale.getDefault();
    public Map props;
    
    public CommonRebootSettingsForm() {
        super();
        props = new HashMap();
    }

    public void initialize() {
        props.clear();
        super.init();
    }

    public void setProps(Map map) {
        this.props = (HashMap) map;
    }

    public Map getProps() {
        return this.props;
    }
    
    public void init(Locale locale) {
        props.clear();
        this.locale = locale;
        super.init();
    }
      
    public String getType() {
        return type;
    }

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

    public void setValue(String property,
                        Object value) {
        if (value instanceof String[]) {
	            props.put(property, ((String[]) value) [0]);
	            } else {
	                    if(props==null){
                            props=new HashMap();
                        }
	                     props.put(property, value);
	                }
        if (DEBUG2) {
                if (value != null) {
                    System.out.println("ScheduleEditForm: Set " + property + " ," + value.toString());
                }
            }

        if (value instanceof String[]) {
            String[] valueArray = (String[]) value;

                if (valueArray.length > 0) {
                    String valueStr = ((String[]) value)[0];
                    if (DEBUG) {
                        System.out.println("ScheduleEditForm: Set " + property + " ," + valueStr);
                    }
                }
            } else {
                props.put(property, value);
                }
    }

    public Object getValue(String property) {
        Object value = null;
        value = props.get(property);
        if (DEBUG2) {
            System.out.print("ScheduleEditForm:  Get " + property + " ");
        }
        if (value != null) {
            if (DEBUG2) {
                System.out.println(value.toString());
            }
            return (value);
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
            setValue("SET_REBOOT_SCHEDULE", "false");
        } else {
            setValue("SET_REBOOT_SCHEDULE", "true");
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
                buffer.append(token + SPACE);
                token = tokens.nextToken();
                if ("DAY".equalsIgnoreCase(token)) {
                    buffer.append("1 DAYS" + SPACE);
                    continue;
                } else if ("WEEK".equalsIgnoreCase(token)) {
                    buffer.append("1 WEEKS" + SPACE);
                    continue;
                } else if ("MINUTE".equalsIgnoreCase(token)) {
                    buffer.append("1 MINUTES" + SPACE);
                    continue;
                } else if ("HOUR".equalsIgnoreCase(token)) {
                    buffer.append("1 HOURS" + SPACE);
                    continue;
                }
            } else if ("DAY".equalsIgnoreCase(token)) {
                buffer.append(token + SPACE);
                buffer.append(tokens.nextToken() + SPACE);
                // skip "every"
                buffer.append(tokens.nextToken() + SPACE);
            token = tokens.nextToken();
                if ("MONTH".equalsIgnoreCase(token)) {
                    buffer.append("1 MONTHS" + SPACE);
                    continue;
                }
            }
            buffer.append(token + SPACE);
        }

        if (DEBUG) {
            System.out.println("UPDATED SCHEDULE STRING:"+ buffer.toString());
        }

        return buffer.toString().trim();
    }

    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        MessageResources resources = (MessageResources) this.getServlet().getServletConfig().getServletContext().getAttribute(Globals.MESSAGES_KEY);
        ActionErrors errors = super.validate(mapping, request);
        if ("true".equals(getValue(SET_REBOOT_SCHEDULE)) && !"cancel".equals(request.getParameter("action"))) {
           super.validateSchedule(resources, errors, request.getLocale());
        }
        return errors;
    }

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        int daysPropLength = IScheduleConstants.SCHEDULE_DAYS_PROPERTY_NAMES.length;
        for (int i = 0; i < daysPropLength; i++) {
            if (getString(IScheduleConstants.SCHEDULE_DAYS_PROPERTY_NAMES[i]) != null) {
                props.remove(IScheduleConstants.SCHEDULE_DAYS_PROPERTY_NAMES[i]);
            }
        }
        props.remove("SET_REBOOT_SCHEDULE");
        props.remove(IScheduleConstants.SCHEDULE_INTERVAL_DAY_OF_MONTH);
        props.remove(IScheduleConstants.SCHEDULE_INTERVAL_DAYS);
        props.remove(IScheduleConstants.SCHEDULE_INTERVAL_DAYS_TYPE);
        props.remove(IScheduleConstants.SCHEDULE_INTERVAL_WEEKS);
        props.remove(IScheduleConstants.SCHEDULE_INTERVAL_MINUTES);
        props.remove(IScheduleConstants.SCHEDULE_INTERVAL_MONTHS);
        props.remove(IScheduleConstants.SCHEDULE_TIME_INTERVAL_END_AM_PM);
        props.remove(IScheduleConstants.SCHEDULE_TIME_INTERVAL_END_HOUR);
        props.remove(IScheduleConstants.SCHEDULE_TIME_INTERVAL_END_MIN);
        props.remove(IScheduleConstants.SCHEDULE_TIME_INTERVAL_START_AM_PM);
        props.remove(IScheduleConstants.SCHEDULE_TIME_INTERVAL_START_HOUR);
        props.remove(IScheduleConstants.SCHEDULE_TIME_INTERVAL_START_MIN);
    }
    public void dump() {
        Set set = props.keySet();
        Iterator it = set.iterator();
        String s;
        while (it.hasNext()) {
            s = (String) it.next();
            System.out.print(s);
            System.out.print("=");
            String[] o;
            try {
                o = (String[]) props.get(s);
                for (int i = 0; i < o.length; i++) {
                    System.out.println(o[i]);
                }
            } catch (Exception e) {
                // e.printStackTrace();
                 System.out.println(props.get(s));
            }

        }
    }
}

