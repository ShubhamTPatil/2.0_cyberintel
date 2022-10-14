// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.forms;

import org.apache.struts.action.ActionMapping;

import java.text.*;

import java.util.*;

import javax.servlet.http.HttpServletRequest;


import com.marimba.apps.subscription.common.util.ScheduleUtils;

import com.marimba.apps.subscriptionmanager.intf.IAppConstants;

import com.marimba.castanet.schedule.*;
import com.marimba.webapps.intf.IMapProperty;


/**
 * DOCUMENT ME!
 *
 * @author Theen-Theen Tan
 * @version 1.14, 02/18/2003
 */
public class PatchScheduleForm
        extends AbstractForm
        implements IAppConstants,IMapProperty {
    public static final boolean DEBUG = IAppConstants.DEBUG;
    public static final boolean DEBUG2 = IAppConstants.DEBUG2;
    public static final String ACTIVE_PERIOD = "ACTIVE_PERIOD";
    public static final String ACTIVE_PERIOD_START = ACTIVE_PERIOD + "_START";
    public static final String ACTIVE_PERIOD_START_DATE = ACTIVE_PERIOD_START + "_DATE";
    public static final String ACTIVE_PERIOD_START_HOUR = ACTIVE_PERIOD_START + "_HOUR";
    public static final String ACTIVE_PERIOD_START_AMPM = ACTIVE_PERIOD_START + "_AMPM";
    public static final String ACTIVE_PERIOD_START_MIN = ACTIVE_PERIOD_START + "_MIN";
    public static final String ACTIVE_PERIOD_START_MONTH = ACTIVE_PERIOD_START + "_MONTH";
    public static final String ACTIVE_PERIOD_START_YEAR = ACTIVE_PERIOD_START + "_YEAR";
    public static final String ACTIVE_PERIOD_START_DAY = ACTIVE_PERIOD_START + "_DAY";
    public static final String ACTIVE_PERIOD_END = ACTIVE_PERIOD + "_END";
    public static final String ACTIVE_PERIOD_END_DATE = ACTIVE_PERIOD_END + "_DATE";
    public static final String ACTIVE_PERIOD_END_HOUR = ACTIVE_PERIOD_END + "_HOUR";
    public static final String ACTIVE_PERIOD_END_AMPM = ACTIVE_PERIOD_END + "_AMPM";
    public static final String ACTIVE_PERIOD_END_MIN = ACTIVE_PERIOD_END + "_MIN";
    public static final String ACTIVE_PERIOD_END_MONTH = ACTIVE_PERIOD_END + "_MONTH";
    public static final String ACTIVE_PERIOD_END_YEAR = ACTIVE_PERIOD_END + "_YEAR";
    public static final String ACTIVE_PERIOD_END_DAY = ACTIVE_PERIOD_END + "_DAY";
    public static final String ACTIVE_PERIOD_SEMANTICS = ACTIVE_PERIOD + "_SEMANTICS";
    public static final String AT_TIME_HOUR = ScheduleInfo.AT_TIME + "_HOUR";
    public static final String AT_TIME_MIN = ScheduleInfo.AT_TIME + "_MIN";
    public static final String AT_TIME_AMPM = ScheduleInfo.AT_TIME + "_AMPM";
    public static final String BETWEEN_TIME = "BETWEEN_TIME";
    public static final String BETWEEN_TIME_HOUR_START = BETWEEN_TIME + "_HOUR_START";
    public static final String BETWEEN_TIME_HOUR_END = BETWEEN_TIME + "_HOUR_END";
    public static final String BETWEEN_TIME_MIN_START = BETWEEN_TIME + "_MIN_START";
    public static final String BETWEEN_TIME_MIN_END = BETWEEN_TIME + "_MIN_END";
    public static final String BETWEEN_TIME_AMPM_START = BETWEEN_TIME + "_AMPM_START";
    public static final String BETWEEN_TIME_AMPM_END = BETWEEN_TIME + "_AMPM_END";
    public static final String DAILY_WEEKDAYS = "DAILY_WEEKDAYS";
    public static final String EVERY_TIME_INTERVAL = "EVERY_TIME_INTERVAL";
    public static final String EVERY_TIME_INTERVAL_UNIT = EVERY_TIME_INTERVAL + "_UNIT";
    public static final String SET_SCHEDULE = "SET_SCHEDULE";
    public static final String CALTYPE_NEVER = "NEVER";
    public static final String CALTYPE_ONCE = "ONCE";
    public static final String CALTYPE_DAILY = "DAILY";
    public static final String CALTYPE_WEEKLY = "WEEKLY";
    public static final String CALTYPE_MONTHLY = "MONTHLY";
    public static final String TIMETYPE_AT = "AT";
    public static final String TIMETYPE_EVERY = "EVERY";
    static String[] calType = new String[5];
    static String[] daysOfWeekLbl = new String[7];
    static String DAYS_OF_WEEK_PFX = "daysOfWeek_";
    static NumberFormat nf = NumberFormat.getInstance();

    static {
        nf.setMinimumIntegerDigits(2);
        calType[0] = CALTYPE_NEVER;
        calType[1] = CALTYPE_ONCE;
        calType[2] = CALTYPE_DAILY;
        calType[3] = CALTYPE_WEEKLY;
        calType[4] = CALTYPE_MONTHLY;
        daysOfWeekLbl[1] = "Mon";
        daysOfWeekLbl[2] = "Tue";
        daysOfWeekLbl[3] = "Wed";
        daysOfWeekLbl[4] = "Thu";
        daysOfWeekLbl[5] = "Fri";
        daysOfWeekLbl[6] = "Sat";
        daysOfWeekLbl[0] = "Sun";
    }

    private Calendar calendar = new GregorianCalendar();
    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    private SimpleDateFormat mrbadf = new SimpleDateFormat("MM/dd/yyyy@hh:mma");
    private SchedulePeriod period = null;
    private String[] daysOfWeek = new String[7];
    private ScheduleInfo schedInfo;
    private String type = PHASE_INIT;
    private int channelId;

    /**
     * REMIND
     */
    public void initialize() {
        setValue(BETWEEN_TIME, "false");
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getType() {
        return type;
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

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String[] getDaysOfWeekLbl() {
        return daysOfWeekLbl;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String[] getDaysOfWeek() {
        return daysOfWeek;
    }

    /**
     * REMIND
     *
     * @param property REMIND
     *
     * @return REMIND
     */
    public String getDaysOfWeek(String property) {
        int index = 0;

        try {
            index = Integer.parseInt(property.substring(DAYS_OF_WEEK_PFX.length()));

            return daysOfWeek[index];
        } catch (NumberFormatException nfe) {
            return "";
        }
    }

    /**
     * REMIND
     *
     * @param index REMIND
     *
     * @return REMIND
     */
    public String getDaysOfWeek(int index) {
        return daysOfWeek[index];
    }

    /**
     * REMIND
     *
     * @param vals REMIND
     */
    public void setDaysOfWeek(String[] vals) {
        for (int i = 0; i < vals.length; i++) {
            daysOfWeek[i] = vals[i];
        }
    }

    /**
     * REMIND
     *
     * @param vals REMIND
     */
    public void setDaysOfWeek(boolean[] vals) {
        for (int i = 0; i < vals.length; i++) {
            daysOfWeek[i] = new Boolean(vals[i]).toString();
            setValue("daysOfWeek_"+i,new Boolean(vals[i]).toString());
        }
    }

    /**
     * REMIND
     *
     * @param property REMIND
     * @param val REMIND
     */
    public void setDaysOfWeek(String property,
                              String val) {
        setValue(property,val);
        try {
            int index = Integer.parseInt(property.substring(DAYS_OF_WEEK_PFX.length()));
            daysOfWeek[index] = val;
        } catch (NumberFormatException nfe) {
            ;
        }
    }

    /**
     * Used by the between time for the update every option in the scheduling.   This is needed so that a comparison can be made between the start and end
     * date.
     *
     * @return REMIND
     */
    public Date getBetweenStartDate() {
        return getBetweenDate(BETWEEN_TIME_HOUR_START, BETWEEN_TIME_MIN_START, BETWEEN_TIME_AMPM_START);
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public Date getBetweenEndDate() {
        return getBetweenDate(BETWEEN_TIME_HOUR_END, BETWEEN_TIME_MIN_END, BETWEEN_TIME_AMPM_END);
    }

    private Date getBetweenDate(String strHour,
                                String strMin,
                                String strAmpm) {
        int hr = new Integer(getProperty(strHour)).intValue();
        int min = new Integer(getProperty(strMin)).intValue();
        int ampm = AM.equals(getProperty(strAmpm)) ? Calendar.AM
                : Calendar.PM;

        // set the day to be today... then overwrite the other fields
        calendar.setTime(new Date());
        calendar.set(Calendar.AM_PM, ampm);
        calendar.set(Calendar.HOUR, hr);
        calendar.set(Calendar.MINUTE, min);

        return calendar.getTime();
    }

    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping,
                      HttpServletRequest request) {
        if (DEBUG) {
            System.out.println("ScheduleEditForm : reset method called");
        }

        for (int i = 0; i < daysOfWeek.length; i++) {
            daysOfWeek[i] = "";
        }

        props.put(BETWEEN_TIME, "false");

        for (int i = 0; i < daysOfWeek.length; i++) {
          props.put("daysOfWeek_"+i,"false");
        }

    }

    /**
     * REMIND
     *
     * @param property REMIND
     * @param value REMIND
     */
    public void setValue(String property,
                    Object value) {
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

                if (DEBUG) {
                    System.out.println("ScheduleEditForm: Set " + property + " ," + valueStr);
                }

                if (property.startsWith(DAYS_OF_WEEK_PFX)) {
                    props.put(property, valueStr);
                    setDaysOfWeek(property, valueStr);
                } else {
                    props.put(property, valueStr);
                }
            }
        } else {
            // Set being called from within the class
            props.put(property, value);
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

        if (property.indexOf("_DATE") != -1) {
            setDateFromTokens();
        }

     /*   if (property.indexOf(DAYS_OF_WEEK_PFX) != -1) {
            return getDaysOfWeek(property);
        } */

        value = props.get(property);

        if (DEBUG2) {
            System.out.print("ScheduleEditForm:  Get " + property + " ");
        }

        if (value != null) {
            if (DEBUG2) {
                System.out.println(value.toString());
            }

            return value;
        } else {
            if (DEBUG2) {
                System.out.println("");
            }

            return "";
        }
    }

    private void infoToProps() {
        props.clear();

        setValue(ScheduleInfo.TIME_PERIOD, TIMETYPE_AT);
        setValue(DAILY_WEEKDAYS, "false");

        // This is only an initialization.  The above two attributes will be
        // overwritten below
        period = ScheduleUtils.getActivePeriod(schedInfo);

        if (period == null) {
            setValue(ACTIVE_PERIOD_START, new Boolean(false).toString());
            setValue(ACTIVE_PERIOD_END, new Boolean(false).toString());
            calendar.setTime(new Date());
            setValue(ACTIVE_PERIOD_START_DATE, sdf.format(calendar.getTime()));
            setValue(ACTIVE_PERIOD_END_DATE, sdf.format(calendar.getTime()));
        } else {
            setValue(ACTIVE_PERIOD_START, new Boolean(period.getStartSchedulePeriod() != null).toString());

            int hr = period.getStartSchedulePeriod()
                    .getScheduleTime()
                    .getHours();
            setValue(ACTIVE_PERIOD_START_HOUR, String.valueOf(getAMPMHour(hr)));
            setValue(ACTIVE_PERIOD_START_AMPM, (((hr - 12) > 0) ? String.valueOf(Calendar.PM)
                    : String.valueOf(Calendar.AM)));
            setValue(ACTIVE_PERIOD_START_MIN, nf.format(new Integer(period.getStartSchedulePeriod().getScheduleTime().getMinutes()).doubleValue()));
            calendar.setTime(period.getStartSchedulePeriod().getScheduleDate().getDate());
            setValue(ACTIVE_PERIOD_START_DATE, sdf.format(calendar.getTime()));
            calendar.setTime(period.getStartSchedulePeriod().getScheduleDate().getDate());
            setValue(ACTIVE_PERIOD_START_YEAR, String.valueOf(calendar.get(Calendar.YEAR)));
            setValue(ACTIVE_PERIOD_START_MONTH, String.valueOf(calendar.get(Calendar.MONTH)));
            setValue(ACTIVE_PERIOD_START_DAY, String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));

            setValue(ACTIVE_PERIOD_END, new Boolean(period.getEndSchedulePeriod() != null).toString());

            if ("true".equals(getProperty(ACTIVE_PERIOD_END))) {
                hr = period.getEndSchedulePeriod()
                        .getScheduleTime()
                        .getHours();
                setValue(ACTIVE_PERIOD_END_AMPM, (((hr - 12) > 0) ? String.valueOf(Calendar.PM)
                        : String.valueOf(Calendar.AM)));
                setValue(ACTIVE_PERIOD_END_HOUR, String.valueOf(getAMPMHour(hr)));
                setValue(ACTIVE_PERIOD_END_MIN, nf.format(new Integer(period.getEndSchedulePeriod().getScheduleTime().getMinutes()).doubleValue()));
                calendar.setTime(period.getEndSchedulePeriod().getScheduleDate().getDate());
                setValue(ACTIVE_PERIOD_END_DATE, sdf.format(calendar.getTime()));
                calendar.setTime(period.getEndSchedulePeriod().getScheduleDate().getDate());
                setValue(ACTIVE_PERIOD_END_YEAR, String.valueOf(calendar.get(Calendar.YEAR)));
                setValue(ACTIVE_PERIOD_END_MONTH, String.valueOf(calendar.get(Calendar.MONTH)));
                setValue(ACTIVE_PERIOD_END_DAY, String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
                setDateFromTokens();
            }

            if (DEBUG2) {
                System.out.println("start after setDateFromTokens()" + getProperty(ACTIVE_PERIOD_START_DATE));
                System.out.println("end after setDateFromTokens()" + getProperty(ACTIVE_PERIOD_END_DATE));
            }
        }

        // We have initialized the radio buttons that needs values
        // up till this point.  Now we go through and set up the calendar period
        // related values
        // If CALENDAR_PERIOD is null, it means we start with an empty schedule
        setValue(SET_SCHEDULE, "true");

        if (schedInfo.getFlag(ScheduleInfo.CALENDAR_PERIOD) == null) {
            if (PHASE_INIT.equals(type) || PHASE_SEC.equals(type)) {
                setValue(ScheduleInfo.CALENDAR_PERIOD, CALTYPE_ONCE);
            } else {
                setValue(SET_SCHEDULE, "false");

                if ("service".equals(type)) {
                    setValue(ScheduleInfo.CALENDAR_PERIOD, CALTYPE_DAILY);
                } else {
                    setValue(ScheduleInfo.CALENDAR_PERIOD, CALTYPE_NEVER);
                }
            }
        } else {
            setValue(ScheduleInfo.CALENDAR_PERIOD, calTypeToStr(schedInfo.getFlag(ScheduleInfo.CALENDAR_PERIOD).intValue()));

            if (DEBUG) {
                System.out.println("CALENDAR_PERIOD is " + getProperty(ScheduleInfo.CALENDAR_PERIOD));
            }

            if ((ScheduleInfo.ONCE != schedInfo.getFlag(ScheduleInfo.CALENDAR_PERIOD)
                    .intValue()) && (ScheduleInfo.NEVER != schedInfo.getFlag(ScheduleInfo.CALENDAR_PERIOD)
                    .intValue())) {
                if (ScheduleInfo.DAILY == schedInfo.getFlag(ScheduleInfo.CALENDAR_PERIOD)
                        .intValue()) {
                    if (DEBUG) {
                        System.out.println("DAY_INTERVAL" + schedInfo.getLong(ScheduleInfo.DAY_INTERVAL));
                    }

                    if (schedInfo.getLong(ScheduleInfo.DAY_INTERVAL)
                            .longValue() < 0) {
                        setValue(DAILY_WEEKDAYS, "true");
                    } else {
                        setValue(DAILY_WEEKDAYS, "false");
                        setValue(ScheduleInfo.DAY_INTERVAL, schedInfo.getLong(ScheduleInfo.DAY_INTERVAL).toString());
                    }
                } else if (ScheduleInfo.WEEKLY == schedInfo.getFlag(ScheduleInfo.CALENDAR_PERIOD)
                        .intValue()) {
                    if (DEBUG) {
                        System.out.println("WEEK_INTERVAL" + schedInfo.getLong(ScheduleInfo.WEEK_INTERVAL));
                    }

                    setValue(ScheduleInfo.WEEK_INTERVAL, schedInfo.getLong(ScheduleInfo.WEEK_INTERVAL).toString());
                    setDaysOfWeek(schedInfo.getBooleanArray(ScheduleInfo.DAYS_OF_WEEK));
                } else if (ScheduleInfo.MONTHLY == schedInfo.getFlag(ScheduleInfo.CALENDAR_PERIOD)
                        .intValue()) {
                    setValue(ScheduleInfo.MONTH_INTERVAL, schedInfo.getLong(ScheduleInfo.MONTH_INTERVAL).toString());
                    setValue(ScheduleInfo.DAY_OF_MONTH, schedInfo.getLong(ScheduleInfo.DAY_OF_MONTH).toString());
                } else {
                    //default or sched==null
                    //REMIND tcube error
                }

                //process Time Filter
                setValue(ScheduleInfo.TIME_PERIOD, timeTypeToStr(schedInfo.getFlag(ScheduleInfo.TIME_PERIOD).intValue()));

                if (ScheduleInfo.AT == schedInfo.getFlag(ScheduleInfo.TIME_PERIOD)
                        .intValue()) {
                    setValue(ScheduleInfo.AT_TIME, schedInfo.getLong(ScheduleInfo.AT_TIME));

                    int time = schedInfo.getLong(ScheduleInfo.AT_TIME)
                            .intValue();
                    setValue(ScheduleInfo.AT_TIME, String.valueOf(time));

                    String timeStr = intToTime(time);
                    int cidx = timeStr.indexOf(':');
                    int midx = timeStr.indexOf('M');
                    setValue(AT_TIME_HOUR, timeStr.substring(0, cidx));

                    if (midx != -1) {
                        setValue(AT_TIME_MIN, timeStr.substring(cidx + 1, midx - 1));
                        setValue(AT_TIME_AMPM, timeStr.substring(midx - 1));
                    } else {
                        setValue(AT_TIME_MIN, timeStr.substring(cidx + 1));
                    }

                    if (DEBUG) {
                        System.out.println("AT_TIME_HOUR" + getProperty(AT_TIME_HOUR));
                        System.out.println("AT_TIME_MIN" + getProperty(AT_TIME_MIN));
                        System.out.println("AT_TIME_AMPM" + getProperty(AT_TIME_AMPM));
                    }

                    // REMIND military time
                } else {
                    if (schedInfo.getLong(ScheduleInfo.HOUR_INTERVAL) != null) {
                        setValue(EVERY_TIME_INTERVAL, schedInfo.getLong(ScheduleInfo.HOUR_INTERVAL).toString());
                        setValue(EVERY_TIME_INTERVAL_UNIT, "hours");
                    } else {
                        setValue(EVERY_TIME_INTERVAL, schedInfo.getLong(ScheduleInfo.MINUTE_INTERVAL).toString());
                        setValue(EVERY_TIME_INTERVAL_UNIT, "minutes");
                    }

                    if ((schedInfo.getBoolean(ScheduleInfo.BETWEEN) != null) && schedInfo.getBoolean(ScheduleInfo.BETWEEN)
                            .booleanValue()) {
                        setValue(BETWEEN_TIME, "true");

                        int time = schedInfo.getLong(ScheduleInfo.START_TIME)
                                .intValue();
                        setHourAndMin(time, BETWEEN_TIME_HOUR_START, BETWEEN_TIME_MIN_START, BETWEEN_TIME_AMPM_START);
                        time = schedInfo.getLong(ScheduleInfo.END_TIME)
                                .intValue();
                        setHourAndMin(time, BETWEEN_TIME_HOUR_END, BETWEEN_TIME_MIN_END, BETWEEN_TIME_AMPM_END);
                    } else {
                        setValue(BETWEEN_TIME, "false");
                    }
                }
            }
        }

        // Finally we set 1 as the default text box values
        setDefault(EVERY_TIME_INTERVAL, "90");
        setDefault(ScheduleInfo.DAY_INTERVAL, "1");
        setDefault(ScheduleInfo.WEEK_INTERVAL, "1");
        setDefault(ScheduleInfo.MONTH_INTERVAL, "1");
        setDefault(ACTIVE_PERIOD_START_HOUR, "12");
        setDefault(ACTIVE_PERIOD_START_MIN, "00");
        setDefault(ACTIVE_PERIOD_END_HOUR, "12");
        setDefault(ACTIVE_PERIOD_END_MIN, "00");
        setDefault(AT_TIME_HOUR, "12");
        setDefault(AT_TIME_MIN, "00");
        setDefault(BETWEEN_TIME_HOUR_START, "9");
        setDefault(BETWEEN_TIME_MIN_START, "00");
        setDefault(BETWEEN_TIME_AMPM_START, "AM");

        setDefault(BETWEEN_TIME_HOUR_END, "5");
        setDefault(BETWEEN_TIME_MIN_END, "00");
        setDefault(BETWEEN_TIME_AMPM_END, "PM");
    }

    /**
     * REMIND
     *
     * @param sch REMIND
     */
    public void setSchedule(String sch) {
        if (DEBUG) {
            System.out.println("ScheduleEditForm:  setSchedule " + sch);
        }

        if ((sch == null) || (sch.length() == 0) || sch.equals("inconsistent")) {
            schedInfo = new ScheduleInfo();
        } else {
            schedInfo = Schedule.getScheduleInfo(sch);
        }

        infoToProps();
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getSchedule() {
        if (DEBUG) {
            System.out.println("ScheduleEditForm:  getSchedule ");
        }

        if ((schedInfo == null) || "false".equals(getProperty(SET_SCHEDULE))) {
            return "";
        }

        StringBuffer buf = new StringBuffer();

        if (CALTYPE_NEVER.equals(getProperty(ScheduleInfo.CALENDAR_PERIOD))) {
            buf.append("NEVER");

            return buf.toString();
        }

        // Daily, Weekly or Monthly periods
        if (CALTYPE_DAILY.equals(getProperty(ScheduleInfo.CALENDAR_PERIOD))) {
            if ("true".equals(getProperty(DAILY_WEEKDAYS))) {
                buf.append("weekdays");
            } else {
                buf.append("every ");
                buf.append(getProperty(ScheduleInfo.DAY_INTERVAL));
                buf.append(" days");
            }
        } else if (CALTYPE_WEEKLY.equals(getProperty(ScheduleInfo.CALENDAR_PERIOD))) {
            StringBuffer dbuf = null;

            //	    boolean all = true;
            //	    boolean weekdays = true;
            String[] days = getDaysOfWeek();

            for (int i = 0; i < 7; ++i) {
            //    if ((new Boolean(days[i])).booleanValue()) {
                 if (getValue("daysOfWeek_"+i).equals("true")) {
                    if (dbuf == null) {
                        dbuf = new StringBuffer();
                    } else {
                        dbuf.append("+");
                    }

                    switch (i) {
                        case 0:
                            dbuf.append("sun");

                            break;

                        case 1:
                            dbuf.append("mon");

                            break;

                        case 2:
                            dbuf.append("tue");

                            break;

                        case 3:
                            dbuf.append("wed");

                            break;

                        case 4:
                            dbuf.append("thu");

                            break;

                        case 5:
                            dbuf.append("fri");

                            break;

                        case 6:
                            dbuf.append("sat");

                            break;
                    }
                }
            }

            int interval = 1;

            try {
                interval = Integer.parseInt(getProperty(ScheduleInfo.WEEK_INTERVAL));
            } catch (NumberFormatException nex) {
                ;
            }

            if (interval != 1) {
                buf.append("every ");
                buf.append(interval);
                buf.append(" weeks ");
            } else {
                buf.append("every week ");
            }

            buf.append("on ");
            buf.append(dbuf);
        } else if (CALTYPE_MONTHLY.equals(getProperty(ScheduleInfo.CALENDAR_PERIOD))) {
            buf.append("day ");
            buf.append(getProperty(ScheduleInfo.DAY_OF_MONTH));
            buf.append(" every ");
            buf.append(getProperty(ScheduleInfo.MONTH_INTERVAL));
            buf.append(" months");
        }

        if (DEBUG) {
            System.out.println("setSchedule.TIME_PERIOD" + getProperty(ScheduleInfo.TIME_PERIOD));
        }

        if (!CALTYPE_ONCE.equals(getProperty(ScheduleInfo.CALENDAR_PERIOD))) {
            // Create time filter
            String token = "update";

            if (PHASE_VERREPAIR.equals(type)) {
                token = "start";
            }

            if (ScheduleInfo.AT == strToTimeType(getProperty(ScheduleInfo.TIME_PERIOD))) {
                buf.append(" " + token + " at ");
                buf.append(getProperty(AT_TIME_HOUR));
                buf.append(":");
                buf.append(getProperty(AT_TIME_MIN));

                if (getProperty(AT_TIME_MIN)
                        .length() > 0) {
                    buf.append(getProperty(AT_TIME_AMPM));
                }
            } else {
                buf.append(" " + token + " every ");
                buf.append(getProperty(EVERY_TIME_INTERVAL));

                if ("hours".equals(getProperty(EVERY_TIME_INTERVAL_UNIT))) {
                    buf.append(" hours");
                } else {
                    buf.append(" minutes");
                }

                String between = getProperty(BETWEEN_TIME);

                if (DEBUG) {
                    System.out.println("ScheduleEditform: between = " + between);
                }

                // Even though between is not input from this interface any more
                // we are writing back the original
                if ("true".equals(between)) {
                    buf.append(" between ");
                    buf.append(getProperty(BETWEEN_TIME_HOUR_START));
                    buf.append(":");
                    buf.append(getProperty(BETWEEN_TIME_MIN_START));

                    if (getProperty(BETWEEN_TIME_MIN_START)
                            .length() > 0) {
                        buf.append(getProperty(BETWEEN_TIME_AMPM_START));
                    }

                    buf.append(" and ");
                    buf.append(getProperty(BETWEEN_TIME_HOUR_END));
                    buf.append(":");
                    buf.append(getProperty(BETWEEN_TIME_MIN_END));

                    if (getProperty(BETWEEN_TIME_MIN_END)
                            .length() > 0) {
                        buf.append(getProperty(BETWEEN_TIME_AMPM_END));
                    }
                }
            }
        }

        String periodStr = formatPeriod();

        if (DEBUG2) {
            System.out.println("getSchedule: periodStr " + periodStr);
        }

        if (periodStr != null) {
            buf.append(" active ");
            buf.append(periodStr);
        }

        if (DEBUG2) {
            System.out.println("Schedule String formed " + buf.toString());
        }

        return buf.toString();
    }


    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getServiceScheduleString() {
        if ((schedInfo == null) || "false".equals(getProperty(SET_SCHEDULE))) {
            return "";
        }

        StringBuffer buf = new StringBuffer();

        if (CALTYPE_NEVER.equals(getProperty(ScheduleInfo.CALENDAR_PERIOD))) {
            buf.append("Never");
            return buf.toString();
        }

        // Daily, Weekly or Monthly periods
        if (CALTYPE_DAILY.equals(getProperty(ScheduleInfo.CALENDAR_PERIOD))) {
            if ("true".equals(getProperty(DAILY_WEEKDAYS))) {
                buf.append("Weekdays");
            } else {
                buf.append("Every ");
                buf.append(getProperty(ScheduleInfo.DAY_INTERVAL));
                buf.append(" day(s)");
            }
        } else if (CALTYPE_WEEKLY.equals(getProperty(ScheduleInfo.CALENDAR_PERIOD))) {
            StringBuffer dbuf = null;
            int lastComma = -1;

            //	    boolean all = true;
            //	    boolean weekdays = true;
            String[] days = getDaysOfWeek();
            if (DEBUG) {
                System.out.println("------->Days: " + days.length);
            }
            for (int i = 0; i < 7; ++i) {
                if ((new Boolean(days[i])).booleanValue()) {
                    if (dbuf == null) {
                        dbuf = new StringBuffer();
                    } else {
                        dbuf.append(", ");
                        lastComma = dbuf.length() - 2;
                    }

                    switch (i) {
                        case 0:
                            dbuf.append("Sun");
                            break;

                        case 1:
                            dbuf.append("Mon");
                            break;

                        case 2:
                            dbuf.append("Tue");
                            break;

                        case 3:
                            dbuf.append("Wed");
                            break;

                        case 4:
                            dbuf.append("Thu");
                            break;

                        case 5:
                            dbuf.append("Fri");
                            break;

                        case 6:
                            dbuf.append("Sat");
                            break;
                    }
                }
            }
            if (DEBUG) {
                System.out.println("-----> Last comma: " + lastComma);
            }
            if (lastComma > -1) {
                dbuf.replace(lastComma, lastComma + 1, " and");
            }

            int interval = 1;

            try {
                interval = Integer.parseInt(getProperty(ScheduleInfo.WEEK_INTERVAL));
            } catch (NumberFormatException nex) {
                ;
            }

            if (interval != 1) {
                buf.append("Every ");
                buf.append(interval);
                buf.append(" week(s) ");
            } else {
                buf.append("Every week ");
            }

            buf.append("on ");
            buf.append(dbuf);
        } else if (CALTYPE_MONTHLY.equals(getProperty(ScheduleInfo.CALENDAR_PERIOD))) {
            buf.append("On day ");
            buf.append(getProperty(ScheduleInfo.DAY_OF_MONTH));
            buf.append(", every ");
            buf.append(getProperty(ScheduleInfo.MONTH_INTERVAL));
            buf.append(" month(s)");
        }

        if (DEBUG) {
            System.out.println("setSchedule.TIME_PERIOD" + getProperty(ScheduleInfo.TIME_PERIOD));
        }

        if (!CALTYPE_ONCE.equals(getProperty(ScheduleInfo.CALENDAR_PERIOD))) {
            // Create time filter
            String token = "Update";

            if (PHASE_VERREPAIR.equals(type)) {
                token = "Start";
            }

            if (ScheduleInfo.AT == strToTimeType(getProperty(ScheduleInfo.TIME_PERIOD))) {
                buf.append(". " + token + " at ");
                buf.append(getProperty(AT_TIME_HOUR));
                buf.append(":");
                buf.append(getProperty(AT_TIME_MIN));

                if (getProperty(AT_TIME_MIN)
                        .length() > 0) {
                    buf.append(getProperty(AT_TIME_AMPM));
                }
            } else {
                buf.append(". " + token + " every ");
                buf.append(getProperty(EVERY_TIME_INTERVAL));

                if ("hours".equals(getProperty(EVERY_TIME_INTERVAL_UNIT))) {
                    buf.append(" hour(s)");
                } else {
                    buf.append(" minute(s)");
                }

                String between = getProperty(BETWEEN_TIME);

                if (DEBUG) {
                    System.out.println("ScheduleEditform: between = " + between);
                }

                // Even though between is not input from this interface any more
                // we are writing back the original
                if ("true".equals(between)) {
                    buf.append(" between ");
                    buf.append(getProperty(BETWEEN_TIME_HOUR_START));
                    buf.append(":");
                    buf.append(getProperty(BETWEEN_TIME_MIN_START));

                    if (getProperty(BETWEEN_TIME_MIN_START)
                            .length() > 0) {
                        buf.append(getProperty(BETWEEN_TIME_AMPM_START));
                    }

                    buf.append(" and ");
                    buf.append(getProperty(BETWEEN_TIME_HOUR_END));
                    buf.append(":");
                    buf.append(getProperty(BETWEEN_TIME_MIN_END));

                    if (getProperty(BETWEEN_TIME_MIN_END)
                            .length() > 0) {
                        buf.append(getProperty(BETWEEN_TIME_AMPM_END));
                    }
                }
            }
        }

        String periodStr = formatPeriod();

        if (DEBUG2) {
            System.out.println("getSchedule: periodStr " + periodStr);
        }

        if (periodStr != null) {
            buf.append(" active ");
            buf.append(periodStr);
        }

        buf.append(".");
        if (DEBUG2) {
            System.out.println("Schedule String formed " + buf.toString());
        }

        return buf.toString();
    }

    private int getAMPMHour(int hr) {
        if (hr == 0) {
            return 12;
        }

        if (hr > 12) {
            return hr - 12;
        }

        return hr;
    }

    private String formatPeriod() {
        if ("true".equals(getProperty(ACTIVE_PERIOD_START))) {
            if ("false".equals(getProperty(ACTIVE_PERIOD_END))) {
                setCalendar(ACTIVE_PERIOD_START);

                return formatPeriod(ACTIVE_PERIOD_START);
            } else {
                return formatPeriod(ACTIVE_PERIOD_START) + " - " + formatPeriod(ACTIVE_PERIOD_END);
            }
        }

        return null;
    }

    private String formatPeriod(String type) {
        setCalendar(type);

        return mrbadf.format(calendar.getTime());
    }

    /**
     * REMIND
     *
     * @param type REMIND
     *
     * @return REMIND
     */
    public Date getPeriod(String type) {
        setCalendar(type);

        return calendar.getTime();
    }

    /**
     * Set the DATE property from the values of the DAY/MONTH/YEAR Widget
     *
     * @param type REMIND
     */
    private void setSimpleDate(String type) {
        setDateCalendar(type);
        setValue(type + "_DATE", sdf.format(calendar.getTime()));
    }

    // Called to set the date for when one of the day/month/year option
    // widget changes
    private void setDateCalendar(String type) {
        if ((getProperty(type + "_DAY")
                .length() == 0) || (getProperty(type + "_MONTH")
                .length() == 0) || (getProperty(type + "_YEAR")
                .length() == 0)) {
            calendar.setTime(new Date());

            return;
        }

        calendar.set(Calendar.DAY_OF_MONTH, (new Integer(getProperty(type + "_DAY")).intValue()));
        calendar.set(Calendar.MONTH, (new Integer(getProperty(type + "_MONTH")).intValue()));
        calendar.set(Calendar.YEAR, (new Integer(getProperty(type + "_YEAR")).intValue()));
    }

    // Assumes that all fields, including, time has been validated
    private void setCalendar(String type) {
        setDateCalendar(type);

        int hr = new Integer(getProperty(type + "_HOUR")).intValue();
        int ampm = new Integer(getProperty(type + "_AMPM")).intValue();

        if (Calendar.PM == ampm) {
            if (hr != 12) {
                hr = hr + 12;
            }
        } else {
            if (hr == 12) {
                hr = 0;
            }
        }

        calendar.set(Calendar.AM_PM, ampm);
        calendar.set(Calendar.HOUR_OF_DAY, hr);
        calendar.set(Calendar.MINUTE, (new Integer(getProperty(type + "_MIN")).intValue()));
    }

    // Set the date from ACTIVE_PERIOD_x_HOUR and ACTIVE_PERIOD_x_MIN
    void setDateFromTokens() {
        if ("true".equals(getProperty(ACTIVE_PERIOD_START))) {
            setSimpleDate(ACTIVE_PERIOD_START);

            if ("true".equals(getProperty(ACTIVE_PERIOD_END))) {
                setSimpleDate(ACTIVE_PERIOD_END);
            }
        }
    }

    private String calTypeToStr(int type) {
        if ((type < 0) || (type > 4)) {
            return "NEVER";
        }

        return calType[type];
    }



    private String timeTypeToStr(int type) {
        if (type == ScheduleInfo.EVERY) {
            return TIMETYPE_EVERY;
        }

        return TIMETYPE_AT;
    }

    private int strToTimeType(String str) {
        if (TIMETYPE_EVERY.equals(str)) {
            return ScheduleInfo.EVERY;
        }

        return ScheduleInfo.AT;
    }

    private void setHourAndMin(int time,
                               String constHour,
                               String constMin,
                               String constAMPM) {
        String timeStr = intToTime(time);
        int cidx = timeStr.indexOf(':');
        int midx = timeStr.indexOf('M');
        setValue(constHour, timeStr.substring(0, cidx));

        if (midx != -1) {
            setValue(constMin, timeStr.substring(cidx + 1, midx - 1));
            setValue(constAMPM, timeStr.substring(midx - 1));
        } else {
            setValue(constMin, timeStr.substring(cidx + 1));
        }
    }

    /**
     * REMIND
     *
     * @param key REMIND
     *
     * @return REMIND
     */
    public String getProperty(String key) {
        return (String) getValue(key);
    }

    /**
     * Sets the given default value to the property if there is no value for the property.
     *
     * @param key REMIND
     * @param val REMIND
     */
    private void setDefault(String key,
                            String val) {
        if (getProperty(key)
                .length() == 0) {
            setValue(key, val);
        }
    }

    /**
     * REMIND
     *
     * @param key REMIND
     *
     * @return REMIND
     */
    public boolean getDisable(String key) {
        if (DEBUG) {
            System.out.println("getDisable " + key);
        }

        if ("true".equals(getProperty(SET_SCHEDULE))) {
            if (key.indexOf(ACTIVE_PERIOD_START) != -1) {
                if (key.equals(ACTIVE_PERIOD_START)) {
                    if (!CALTYPE_NEVER.equals(getProperty(ScheduleInfo.CALENDAR_PERIOD))) {
                        return false;
                    }
                } else {
                    if ("true".equals(getProperty(ACTIVE_PERIOD_START))) {
                        return false;
                    }
                }
            } else if (key.indexOf(ACTIVE_PERIOD_END) != -1) {
                if (key.equals(ACTIVE_PERIOD_END)) {
                    if (!CALTYPE_NEVER.equals(getProperty(ScheduleInfo.CALENDAR_PERIOD)) && "true".equals(getProperty(ACTIVE_PERIOD_START))) {
                        return false;
                    }
                } else {
                    if ("true".equals(getProperty(ACTIVE_PERIOD_START))) {
                        return false;
                    }
                }
            } else if (key.indexOf(ScheduleInfo.AT_TIME) != -1) {
                if (!CALTYPE_NEVER.equals(getProperty(ScheduleInfo.CALENDAR_PERIOD)) && TIMETYPE_AT.equals(getProperty(ScheduleInfo.TIME_PERIOD))) {
                    return false;
                }
            } else if (key.indexOf("EVERY_TIME") != -1) {
                if (!CALTYPE_NEVER.equals(getProperty(ScheduleInfo.CALENDAR_PERIOD)) && TIMETYPE_EVERY.equals(getProperty(ScheduleInfo.TIME_PERIOD))) {
                    return false;
                }
            } else if (key.equals(ScheduleInfo.TIME_PERIOD)) {
                if (!CALTYPE_NEVER.equals(getProperty(ScheduleInfo.CALENDAR_PERIOD))) {
                    return false;
                }
            } else if (key.equals(ScheduleInfo.DAY_INTERVAL)) {
                if ("false".equals(getProperty(DAILY_WEEKDAYS))) {
                    return false;
                }
            } else {
                return false;
            }
        }

        return true;
    }

    /**
     * Convert a minute to a time string. 0 = 12:00 AM, 1439 = 11:59 PM.  This is based on tool.util.TimeUtil. The difference is that it does not take the
     * tuner's locale into consideration because RC GUI doesn't support 24-hour time format.
     *
     * @param i REMIND
     *
     * @return REMIND
     */
    private String intToTime(int i) {
    boolean am = true;
        int hour = (i / 60);

        if (hour > 11) {
            am = false;
            hour -= 12;
        }

        if (hour == 0) {
            hour = 12;
        }

        int min = (i % 60);

        // to string
        StringBuffer buf = new StringBuffer();

        if (hour < 10) {
            buf.append('0');
        }

        buf.append(hour);
        buf.append(':');

        if (min < 10) {
            buf.append('0');
        }

        buf.append(min);
        buf.append(am ? "AM"
                : "PM");

        return buf.toString();
    }
}
