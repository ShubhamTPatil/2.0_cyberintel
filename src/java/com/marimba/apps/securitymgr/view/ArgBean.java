package com.marimba.apps.securitymgr.view;
// Copyright 1997-2010, Marimba Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// %Z%%M%, %I%, %G%

import com.marimba.apps.securitymgr.webapp.forms.VDeskReportForm;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.intf.dbtree.IDBTreeNode;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;



import java.util.Date;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.intf.msf.query.QueryConstants;



/**
 * A bean that saves information about an argument for a form query.
 *
 * @author    	Zheng Xia
 * @author	Pritpal Singh
 * @version   	1.9, 06/01/2003
 */
public class ArgBean implements QueryConstants, IAppConstants {

    // index of this arg bean in the array
    int index;
    ArgBean[] array;

    String id;
    String name;
    String type;
    boolean nullable = false;
    VDeskReportForm form;

    // integer type
    int argType;


    // values
    boolean isNull = false;
    String[] values;
    int intValue = -1;
    boolean boolValue = false;
    String strValue;
    java.sql.Date dateValue;
    java.sql.Timestamp timeValue;

    boolean valid = false;     // if it has valid value
    String message = "";
    Locale locale;
    int DEBUG = 0;//genUtil.getDebug("RC/BEAN");

    /**
     * Constructor.
     */
    public ArgBean(String id, Locale locale) {
        this.id = id;
        name = "";
        type = "";
        argType = UNKNOWN_QUERY;

        values = null;
        this.locale = locale;
    }

    public ArgBean() {
        id = "";
        name = "";
        type = "";
        argType = UNKNOWN_QUERY;

        values = null;
    }

    public ArgBean(String name, String type, boolean nullable) {
        this("", name, type, nullable);
    }

    public ArgBean(String id, String name, String type, boolean nullable) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.nullable = nullable;

        if (BOOL_TYPE.equals(type)) {
            argType = BOOLEAN_QUERY;
        } else if (STR_TYPE.equals(type)) {
            argType = STRING_QUERY;
        } else if (TIME_TYPE.equals(type)) {
            argType = TIME_QUERY;
        } else if (DATE_TYPE.equals(type)) {
            argType = DATE_QUERY;
        } else if (INT_TYPE.equals(type)) {
            argType = INT_QUERY;
        } else {
            argType = UNKNOWN_QUERY;
        }
    }


    private void getValue(String prefix, int length, HttpServletRequest request) throws IOException, ServletException {
        values = new String[length];
        for (int i = 0; i < length; i++) {
            if (DEBUG > 4) {
                System.out.println(prefix + i + "=" + request.getParameter(prefix + i));
            }
            values[i] = request.getParameter(prefix + i);
            values[i] = (values[i] == null) ? "" : values[i];
        }
    }

    private void getValue(String prefix, int length, IDBTreeNode node) {
        values = new String[length];
        for (int i = 0; i < length; i++) {
            if (DEBUG > 4) {
                System.out.println(prefix + i + "=" + node.getProperty(prefix + i));
            }
            values[i] = node.getProperty(prefix + i);
            values[i] = (values[i] == null) ? "" : values[i];
        }
    }

    public boolean getValue(int index, IDBTreeNode node, Locale locale, TimeZone timezone) {
        StringBuffer tmp = new StringBuffer(ARG_VALUE_PREFIX);
        tmp.append(index).append("_");

        String valuePrefix = tmp.toString();
        boolean res = true;

        isNull = nullable && "true".equals(node.getProperty(valuePrefix + "null"));

        switch (argType) {
            case INT_QUERY:
            case BOOLEAN_QUERY:
            case STRING_QUERY:
                getValue(valuePrefix, 1, node);
                break;
            case DATE_QUERY:
                getValue(valuePrefix, 3, node);
                break;
            case TIME_QUERY:
                getValue(valuePrefix, 7, node);
                break;
            default:
                values = new String[0];
                res = false;
        }

        if (res || !isNull) {
            res = processData(locale, timezone);
            if (!res && DEBUG > 4){
                System.out.println("ArgBean: processData returned null");
            }
        }

        valid = res;
        return res;
    }

    /**
     * Get value from request.
     */
    public boolean getValue(int index, HttpServletRequest request) throws IOException, ServletException {
        StringBuffer tmp = new StringBuffer(ARG_VALUE_PREFIX);
        tmp.append(index).append("_");

        String valuePrefix = tmp.toString();
        boolean res = true;

        String v = request.getParameter(valuePrefix + "null");
        isNull = nullable && ("on".equalsIgnoreCase(v) || "true".equalsIgnoreCase(v));

        switch (argType) {
            case INT_QUERY:
            case BOOLEAN_QUERY:
            case STRING_QUERY:
                getValue(valuePrefix, 1, request);
                break;
            case DATE_QUERY:
                getValue(valuePrefix, 1, request);
                break;
            case TIME_QUERY:
                getValue(valuePrefix, 1, request);
                break;
            default:
                if (DEBUG > 0){
                    System.out.println("Failed to get argument from HTTPRequest");
                    System.out.println("Argument type is:" + argType);
                }
                values = new String[0];
                res = false;
        }
        String ATTR_TIMEZONE	= "com.marimba.servlet.timeZone";
        if (res && !isNull) {
            res = processData(request.getLocale(), (TimeZone)request.getSession().getAttribute(ATTR_TIMEZONE));
        }

        valid = res;
        return res;
    }

    /**
     * Get the string representation of the value.
     * For date/datetime parameters, Locale-given format
     */
    public String getValue(Locale locale) {
        switch (argType) {
            case INT_QUERY:
                return String.valueOf(intValue);
            case BOOLEAN_QUERY:
                return String.valueOf(boolValue);
            case STRING_QUERY:
                return strValue;
            case DATE_QUERY:
                return dateValue == null ? "" : formatDate(dateValue, locale, false);
            case TIME_QUERY:
                return timeValue == null ? "" :  formatDate(timeValue, locale, true);
            default:
                return "";
        }
    }

    /**
     * Set a string value.
     */
    public boolean setValue(String value, Locale locale, TimeZone timezone, boolean isRequest) {
        Date d = null;
        boolean res = true;

        switch (argType) {
            case INT_QUERY:
                try {
                    intValue = Integer.parseInt(value);
                    values = new String[1];
                    values[0] = value;
                } catch (Exception e) {
                    //setMsg(ERROR_INVALID_ARG_INTEGER, locale);
                    res = false;
                }
                break;
            case BOOLEAN_QUERY:
                if ("true".equalsIgnoreCase(value)) {
                    boolValue = true;
                } else {
                    boolValue = false;
                }
                values = new String[1];
                values[0] = value;
                break;
            case STRING_QUERY:
                strValue = value;
                values = new String[1];
                values[0] = value;
                break;
            case DATE_QUERY:
                try {
                    values = new String[1];
                    values[0] = value;
                    d = parseDateTime(locale, timezone, false, false, true);
                    if(d == null) {
                        return false;
                    }
                    dateValue = new java.sql.Date(d.getTime());
                } catch(Throwable t) {
                    if(DEBUG > 1) {
                        t.printStackTrace();
                    }
                }


                break;
            case TIME_QUERY:
                values = new String[1];
                values[0] = regexp(value);
                if (isRequest) {
                    d = parseDateTime(Locale.US, timezone, true, true, true);
                } else {
                    d = parseDateTime(locale, timezone, true, true, true);
                }
                if(d == null) {
                    return false;
                }
                timeValue = new java.sql.Timestamp(d.getTime());

                break;
            default:
                values = new String[0];
                res = false;
        }
        valid = res;
        return res;
    }

    // check to see if asterisk is in the input param,
    public boolean isAsteriskNotInArg() {
        return (values[0]==null || (values[0].indexOf("*") == -1));
    }


    private Date parseDateTime(Locale locale, TimeZone tzUser, boolean time, boolean applyGmtConversion, boolean attemptOldStyle) {
        Date parsedDate = null;
        ArrayList result = null;

        try {
            parsedDate = parseDateCommon(values[0], time);
            //already in the common format, no need to transofrm
            return parsedDate;
        } catch (ParseException pe) {
            try {
                if(DEBUG > 0) {
                    System.out.println("Unable to parse the value " + values[0] + "in common format");
                }
                result = parseDateToUTC(values[0], locale, tzUser, time, applyGmtConversion);
                parsedDate = (Date)result.get(0);
                values[0] = (String)result.get(1);
                return parsedDate;
            } catch(ParseException pex) {
                try {
                    if(DEBUG > 0) {
                        System.out.println("Unable to parse the value " + values[0] + "to locale specific format, Trying to read old style time format data");
                    }
                    result = parseDateOldCommon(values, tzUser, time, applyGmtConversion);
                    parsedDate = (Date)result.get(0);
                    values[0] = (String)result.get(1);
                    return parsedDate;
                } catch(ParseException pexcp) {
                    if(!attemptOldStyle) {
                        if(DEBUG > 0) {
                            System.out.println("attemptOldStyle is " + attemptOldStyle + " skipping the old style parsing");
                        }
                        if(time) {
                            //setMsg(ERROR_INVALID_ARG_TIME, locale);
                        } else {
                            //setMsg(ERROR_INVALID_ARG_DATE, locale);
                        }
                        return null;
                    }
                    try {
                        // not able to tranform in both the formats
                        if(DEBUG > 0) {
                            System.out.println("Unable to parse the value " + values[0] + "to locale specific format, Trying to read old style time format data");
                        }
                        parsedDate = parseDateOld(values[0], tzUser, time);
                        values[0] = formatDateCommon(parsedDate, true, time);
                        if(time) {
                            //parsedDate = GeneralUtil.getCommontimeformat().parse(values[0]);
                        }
                        return parsedDate;
                    } catch(ParseException pexfinal) {
                        if(time) {
                            //setMsg(ERROR_INVALID_ARG_TIME, locale);
                        } else {
                            //setMsg(ERROR_INVALID_ARG_DATE, locale);
                        }
                        return null;
                    }
                }
            }


        }
    }


    /**
     * Set the value to be null.
     */
    public boolean setNull() {
        if (nullable) {
            isNull = true;
        }
        return nullable;
    }

    private boolean processData(Locale locale, TimeZone timezone) {
        boolean res = true;
        Date d = null;
        switch (argType) {
            case INT_QUERY:
                try {
                    intValue = Integer.parseInt(values[0]);
                } catch (Exception e) {
                    //setMsg(ERROR_INVALID_ARG_INTEGER, locale);
                    res = false;
                }
                break;
            case BOOLEAN_QUERY:
                if ("true".equalsIgnoreCase(values[0])) {
                    boolValue = true;
                } else {
                    boolValue = false;
                }
                break;
            case STRING_QUERY:
                strValue = values[0];
                break;
            case DATE_QUERY:
                values[0] = (values[0] == null || values[0].trim().length() == 0) ? "" : values[0];
                d = parseDateTime(locale, timezone, false, false, false);
                if(d == null) {
                    return false;
                }
                dateValue = new java.sql.Date(d.getTime());

                break;
            case TIME_QUERY:
                values[0] = (values[0] == null || values[0].trim().length() == 0) ? "" : values[0];
                d = parseDateTime(locale, timezone, true, true, false);
                if(d == null) {
                    return false;
                }
                timeValue = new java.sql.Timestamp(d.getTime());

                break;
            default:
                res = false;
        }
        return res;
    }

    private int prepareNull(int index, PreparedStatement stmt, int type) throws SQLException {
        if (nullable) {
            if (isNull) {
                stmt.setInt(index++, 1);
            } else {
                stmt.setInt(index++, 0);
            }
        }
        return index;
    }

    /**
     * To be compliant with query builder, we need to make wild card as
     * '*' and '\'', instead of '%' and '_'.
     */
    private String prepareString(String v) {
        if (v == null) {
            return "";
        }

        int length = v.length();
        StringBuffer sb = new StringBuffer(length);
        for (int i = 0; i < length; i++) {
            char ch = v.charAt(i);
            switch (ch) {
                case '\\' :
                    i++;
                    if (i < length) {
                        sb.append(v.charAt(i));
                    }
                    break;
                case '*':
                    sb.append('%');
                    break;
                // Defect SW00288997, in form queries parameter '.' is replaced as '_', which gives incorrect results.
                //   case '.':
                //sb.append('_');
                //break;
                default:
                    sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * Prepare the PreparedStatement.
     */
    public int prepare(int index, PreparedStatement stmt) throws SQLException {
        if (DEBUG > 4){
            System.out.println("ArgBean.prepare(), argType is:" + argType);
            System.out.println("ArgBean.prepare(), intValue is:" + intValue);
            System.out.println("ArgBean.prepare(), strValue is:" + strValue);
            System.out.println("ArgBean.prepare(), dateValue is:" + dateValue);
            System.out.println("ArgBean.prepare(), timeValue is:" + timeValue);
        }
        switch (argType) {
            case INT_QUERY:
                index = prepareNull(index, stmt, Types.INTEGER);
                stmt.setInt(index++, intValue);
                index = prepareNull(index, stmt, Types.INTEGER);
                break;
            case BOOLEAN_QUERY:
                index = prepareNull(index, stmt, Types.BIT);
                stmt.setBoolean(index++, boolValue);
                index = prepareNull(index, stmt, Types.BIT);
                break;
            case STRING_QUERY:
                index = prepareNull(index, stmt, Types.VARCHAR);
                stmt.setString(index++, prepareString(strValue));
                index = prepareNull(index, stmt, Types.VARCHAR);
                break;
            case DATE_QUERY:
                index = prepareNull(index, stmt, Types.DATE);
                //Calendar is not really used for Date, but to be consistent with
                //inventory we set it

                // Removing GMT Convertion to get the exact match with Database.
                //stmt.setDate(index++, dateValue, TimeUtil.getGMTCalendar());

                stmt.setDate(index++, dateValue);
                index = prepareNull(index, stmt, Types.DATE);
                break;
            case TIME_QUERY:
                index = prepareNull(index, stmt, Types.TIMESTAMP);
                // because of defect in mssql jdbc driver that ignores passed calendar
                //for searchinfg only, timeValue is local TZ of CMS - see line GeneralUtil
                // line 274 in parseDateToUTC() method; defect SW00242340
                stmt.setTimestamp(index++, timeValue);
                index = prepareNull(index, stmt, Types.TIMESTAMP);
                break;
            default:
                // REMIND: error message
                throw new SQLException("Invalid argument type!");
        }
        return index;
    }


    public String getSqlValue(String strValue, int dbType) {
        switch (argType) {
            case INT_QUERY:
                return String.valueOf(intValue);
            case BOOLEAN_QUERY:
                return String.valueOf(boolValue);
            case STRING_QUERY:
                return "'" + prepareString(strValue) + "'";
            case DATE_QUERY:
                return "{d '" + (dateValue == null?"":dateValue.toString()) + "'}";
            case TIME_QUERY:
                if (dbType != 1) {
                    String timeStr = null;
                    if(timeValue != null) {
                        timeStr = timeValue.toString();
                        timeStr = timeStr.substring(0, timeStr.indexOf('.'));
                    }
                    return "to_date('" + (timeStr == null?"":timeStr) + "', 'yyyy-mm-dd hh24:mi:ss')";
                }
                return "{ts '" + (timeValue == null?"":timeValue.toString()) + "'}";
            default:
                return "";
        }
    }

    /**
     * Prepare for displaying
     * array is in which this bean is
     * index is the index of this bean in the array
     */
    public void prepare(int index, ArgBean[] array, VDeskReportForm form) {
        this.index = index;
        this.array = array;
        this.form = form;
    }

    /**
     * Check if it is valid.
     */
    public boolean check() {
        boolean res = (name != null && !"".equals(name.trim()));
        return (res) ? argType != UNKNOWN_QUERY : false;
    }

    private void setMsg(String msg, Locale locale) {
        message = msg;
    }

    public void setMsgWithParams(String key, Object [] args, Locale locale) {
        message = key;
        valid = false;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean getValid() {
        return valid;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getNullable() {
        return String.valueOf(nullable);
    }

    public String getIsNull() {
        return String.valueOf(isNull);
    }

    public String[] getValues() {
        return values;
    }

    public int getArgType() {
        return argType;
    }

    public String getMsg() {
        return message;
    }

    /** --- Names --- **/

    public String getNameStr() {
        return ARG_NAME_PREFIX + index;
    }

    public String getTypeStr() {
        return ARG_TYPE_PREFIX + index;
    }

    public String getValueStr() {
        return ARG_VALUE_PREFIX + index;
    }

    /** --- Links --- **/

    public HashMap getParameters(HashMap map) {
        map.put(ARG_NAME_PREFIX + index, (name == null) ? "" : name);
        map.put(ARG_TYPE_PREFIX + index, (type == null) ? "" : type);

        int len = 0;
        switch (argType) {
            case INT_QUERY:
            case BOOLEAN_QUERY:
            case STRING_QUERY:
                len = 1;
                break;
            case DATE_QUERY:
                len = 3;
                break;
            case TIME_QUERY:
                len = 7;
                break;
            default:
        }

        if (values != null && values.length >= len) {
            for (int i = 0; i < len; i++) {
                StringBuffer sb = new StringBuffer(ARG_VALUE_PREFIX);
                sb.append(index);
                sb.append("_");
                sb.append(i);
                String v = (values[i] == null) ? "" : values[i];
                map.put(sb.toString(), v);
            }
        }

        return map;
    }

    public String regexp(String str) {
        if (str != null) {
            int len = str.length();
            StringBuffer out = new StringBuffer(len);
            for (int i = 0; i < len; i++) {
                int c = str.charAt(i);
                switch (c) {
                    case '\\':
                        int n = (i + 1 < len) ? (int)str.charAt(i + 1) : -1;
                        if (n == '.') {
                            out.append((char)n);
                            i++;
                        } else {
                            out.append((char)c);
                        }
                        break;
                    default:
                        out.append((char)c);
                        break;
                }
            }
            return out.toString();
        }
        return str;
    }

    /*
     * Parse the Date time  to a string format. The formatter used is locale specific(since 7.0).
     */
    public String formatDate(Date datetime, Locale locale, boolean time) {
        SimpleDateFormat formatter = null;
        if(time) {
            formatter = (SimpleDateFormat)SimpleDateFormat.getDateTimeInstance(INPUT_RC_DATESTYLE, INPUT_RC_TIMESTYLE, locale);
        } else {
            formatter = (SimpleDateFormat)SimpleDateFormat.getDateInstance(INPUT_RC_DATESTYLE, locale);
        }
        return formatter.format(datetime);
    }

    private Date parseDate(String datetimeString, SimpleDateFormat formatter) throws ParseException {
        try {
            return formatter.parse(datetimeString);
        } catch(ParseException pe) {
            if (DEBUG > 0){
                System.out.println("Failed to parse value" + datetimeString + " with formatter:" + formatter.toLocalizedPattern());
            }
            throw pe;
        }
    }
    /*
     * Parse the Date time string in common format to a date. The formatter used is common datetimeformat
     */
    public Date parseDateCommon(String datetimeString, boolean time) throws ParseException {
        SimpleDateFormat formatter = time ? commonTimeFormat : commonDateFormat;
        return parseDate(datetimeString, formatter);
    }

    /*
     * Parse the Date time string (as result of casting) in common db format to a date.
     The formatter used is common commonDBTimeFormat
     */
    public Date parseDateDB(String datetimeString, boolean time) throws ParseException {
        SimpleDateFormat formatter = time ? commonDBTimeFormat : commonDBDateFormat;
        return parseDate(datetimeString, formatter);
    }

    /*
     * Parse the Date time  to a string format. The formatter used is common datetimeformat
     */
    public String formatDateCommon(Date datetime, boolean gmtConversion, boolean time) {
        SimpleDateFormat formatter = null;
        if(time) {
            formatter = commonTimeFormat;
            if(gmtConversion) {
                formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            }
        } else {
            formatter = commonDateFormat;
        }
        return formatter.format(datetime);
    }

    public Date parseDateOld(String datetimeString, TimeZone tzUser, boolean time) throws ParseException {
        SimpleDateFormat formatter = time ? oldTimeFormat : oldDateFormat;
        if(time && tzUser != null) {
            formatter = (SimpleDateFormat)formatter.clone();
            formatter.setTimeZone(tzUser);
        }
        return parseDate(datetimeString, formatter);
    }

    public ArrayList parseDateOldCommon(String [] values, TimeZone tzUser, boolean time, boolean applyGmtConversion) throws ParseException {
        ArrayList result = new ArrayList();
        java.util.Date d = buildOldStyleDate(values,tzUser, time, applyGmtConversion);
        SimpleDateFormat formatter = time ? commonTimeFormat: commonDateFormat;
        if(applyGmtConversion && time) {
            // cloning since we dont want to modify static formatter
            formatter = (SimpleDateFormat) formatter.clone();
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        String formattedValueStr = formatter.format(d);
        result.add(d);
        result.add(formattedValueStr);
        return result;
    }

    /**
     * Parse the input datetime string with the given locale format to common format(commonTimePatter)
     * in gmt timezone. This format can be saved in database in locale independent form and when retrieving
     * it can be transformed to locale dependent form(timzeone and locale)
     *
     * @param datetimeString
     * @param locale
     * @param tzUser users timezone
     * @param time
     * @param applyGMTConversion specifies if gmt time conversion to be applied
     * @return ArrayList with two values - 1. java.util.Date - datetimeString parsed as util.Date
     *                                     2. String - string in utc format(commonTimePattern) and in gmt timzeone
     * @throws ParseException
     */
    public ArrayList parseDateToUTC(String datetimeString, Locale locale, TimeZone tzUser, boolean time, boolean applyGMTConversion) throws ParseException {
        SimpleDateFormat formatter = null;
        String commonPattern = null;
        ArrayList result = new ArrayList();
        //create formatter based on locale
        if(time) {
            formatter = (SimpleDateFormat)SimpleDateFormat.getDateTimeInstance(INPUT_RC_DATESTYLE, INPUT_RC_TIMESTYLE, locale);
            commonPattern = COMMONTIMEPATTERN;
        } else {
            formatter = (SimpleDateFormat)SimpleDateFormat.getDateInstance(INPUT_RC_DATESTYLE, locale);
            commonPattern = COMMONDATEPATTERN;
        }
        //set timezone if needed and get date
        java.util.Date d = null;
        if(applyGMTConversion && time) {
            formatter.setTimeZone(tzUser);
        }
        d = formatter.parse(datetimeString);
        //modify formatter to UTC standard and get string representation of date out of it
        if(applyGMTConversion && time) {
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        formatter.applyPattern(commonPattern);
        String commonFormatResult = formatter.format(d);
        //this code converts the date using local default TZ in commonTimeFormat on purpose
        //to work around the defect in MSSQL jdbc driver that ignores passed GMT calendar
        //ArgBean line # 524; defect SW00242340
        result.add(parseDate(commonFormatResult, commonTimeFormat));
        result.add(commonFormatResult);
        return result;
    }

    public java.util.Date buildOldStyleDate(String [] values, TimeZone tzUser, boolean time, boolean applyGmtConversion) throws ParseException {
        StringBuffer tmp = new StringBuffer();
        if(time) {
            if(values.length >= 6) {
                tmp = new StringBuffer((values[0] == null || values[0].trim().length() == 0) ? "--" : values[0]);
                tmp.append("/").append(values[1]);
                tmp.append("/").append(values[2]);
                tmp.append(" ").append(values[3]);
                tmp.append(":").append(values[4]);
                tmp.append(":").append(values[5]);
                tmp.append(" ").append(values[6]);
            }
        } else {
            if(values.length >= 2) {
                tmp = new StringBuffer(values[0]);
                tmp.append("/").append(values[1]);
                tmp.append("/").append(values[2]);
            }
        }
        return parseDateOld(tmp.toString(), tzUser, time);
    }
}

