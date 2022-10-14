// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.taglib;

import org.apache.struts.taglib.html.SelectTag;
import org.apache.struts.util.MessageResources;
import org.apache.struts.util.RequestUtils;
import org.apache.struts.util.ResponseUtils;

import java.io.IOException;

import java.lang.reflect.InvocationTargetException;

import java.text.*;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import com.marimba.apps.subscription.common.ISubscriptionConstants;

import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;

import com.marimba.castanet.schedule.*;

/**
 * This tag renders the entire or specified components of a Marimba channel of Tuner schedule string(see com.marimba.castanet.schedule.Schedule for valid
 * format) within a <font class="{activefont|inactivefont}">schedule</font> The input schedule string can be specified as the 'schedule' attribute, or
 * obtained as a property specified by from a bean specified 'name'. If schedule is specified, then name/property will be ignored. If property is not
 * specified, the value of the bean will be used.  If schedule string is invalid, an exception will be thrown. java.util.SimpleDateFormat is used to format
 * the dates and time for active period's start and end, and the blackout period components. type - {"init"|"sec"|"update"|"verrepair"}.  The input string
 * should begin with "init", "sec", "update", or "verrepair" to identify the type of schedule being displayed.  If this is an initial or update schedule, when
 * there is no schedule "Activate on next subscription update" displayed.  Otherwise, "N/A" is displayed. activefont - CSS font class for displaying the
 * schedule inactivefont - CSS font class for the N/A and inconsistent states schedule - the input schedule string. There is no output if the string passed in
 * is null or "". name - the name of a bean (from any scope) containing a property that stores the input schedule string property - only meaningful if name is
 * filled.   component - { recurring|activestart|activeend|blackoutstart|blackoutend }. If not specified, the entire schedule string is printed. pattern - a
 * valid pattern for SimpleDateFormat.  If not specified or specified pattern is invalid, the default pattern of SimpleDateFormat is used. format - {short|
 * long} use long or short message format when displaying the schedule.  Messages are looked up in "ApplicationResources.properties". ".short" should be
 * appended to a message key that has short and long versions.  By default, the long message is selected.
 *
 * @author Theen-Theen Tan
 * @version 1.12, 02/18/2003
 */
public class ScheduleDisplayTag
    extends TagSupport
    implements IErrorConstants,
                   ISubscriptionConstants {
    final static boolean DEBUG = com.marimba.apps.subscriptionmanager.intf.IAppConstants.DEBUG;
    private static String MSG_SHORT = "short";
    private Locale locale;

    /** The message resources for this package. */
    protected static MessageResources messages = MessageResources.getMessageResources("ApplicationResources");
    private DateFormat                df = null;
    private Calendar                  calendar = new GregorianCalendar();
    private ScheduleInfo              schedInfo = null;
    private String                    context = "";

    /** The name of the bean containing the schedule string.  Bean may exists in any scope */
    protected String name = null;

    /** The property from the bean specified by 'name' that stores the schedule object. Only applicable if 'name' is filled. */
    protected String property = null;

    /** The input schedule string */
    protected String schedule = null;

    /**
     * The component of the schedule to render { recurring|activestart|activeend|blackoutstart|blackoutend }. If not specified, the entire schedule string is
     * printed.
     */
    protected String component = null;

    /** {"short" | "long"}  use long or short message format when displaying the schedule. */
    protected String format = null;

    /** A valid pattern for SimpleDateFormat.  If not specified or specified pattern is invalid, the default pattern of SimpleDateFormat is used. */
    protected String pattern = null;

    /**
     * The input string should begin with "init", "sec", "update", or "verrepair" to identify the type of schedule being displayed.  If this is an initial or
     * update schedule, when there is no schedule "Activate on next subscription update" displayed.  Otherwise, "N/A" is displayed.
     */
    protected String type = null;

    /** The CSS font class for displaying the schedule */
    protected String activeFont = null;

    /** The CSS font class for displaying the schedule */
    protected String inactiveFont = null;

    /** True if the message should be double spaced (default) for example, on the Target Details and Package Details pages */
    protected String doubleSpace = "true";

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getName() {
        return name;
    }

    /**
     * REMIND
     *
     * @param name REMIND
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getProperty() {
        return property;
    }

    /**
     * REMIND
     *
     * @param property REMIND
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getSchedule() {
        return schedule;
    }

    /**
     * REMIND
     *
     * @param schedule REMIND
     */
    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getComponent() {
        return component;
    }

    /**
     * REMIND
     *
     * @param component REMIND
     */
    public void setComponent(String component) {
        this.component = component;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getFormat() {
        return format;
    }

    /**
     * REMIND
     *
     * @param format REMIND
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * REMIND
     *
     * @param pattern REMIND
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
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

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getActiveFont() {
        return activeFont;
    }

    /**
     * REMIND
     *
     * @param activeFont REMIND
     */
    public void setActiveFont(String activeFont) {
        this.activeFont = activeFont;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getInactiveFont() {
        return inactiveFont;
    }

    /**
     * REMIND
     *
     * @param inactiveFont REMIND
     */
    public void setInactiveFont(String inactiveFont) {
        this.inactiveFont = inactiveFont;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getDoubleSpace() {
        return this.doubleSpace;
    }

    /**
     * REMIND
     *
     * @param doubleSpace REMIND
     */
    public void setDoubleSpace(String doubleSpace) {
        this.doubleSpace = doubleSpace.trim()
                                      .toLowerCase();
    }

    /**
     * Apply the pattern to the date formatter if one is specified. Renders the schedule string or the specified component
     *
     * @return REMIND
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doStartTag()
        throws JspException {
        df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT,
					    ((HttpServletRequest) pageContext.getRequest()).getLocale());

         HttpServletRequest      request =((HttpServletRequest) pageContext.getRequest());
                 locale= request.getLocale();

		TimeZone timeZone = TimeZone.getDefault();

	    df.setTimeZone(timeZone);


        // initialize context
        this.context = ((HttpServletRequest) pageContext.getRequest()).getContextPath();

        Object value = null;

        // Schedule not specified.  Check for schedule input from the session
        if ((schedule == null) || (schedule.length() == 0)) {
            if ((property == null) || (property.length() == 0)) {
                value = RequestUtils.lookup(pageContext, name, null);
            } else {
                value = RequestUtils.lookup(pageContext, name, property, null);
            }
        }

        if (value instanceof String) {
            schedule = ((String) value).trim();
        } else if (value instanceof Schedule) {
            schedule = ((Schedule) value).toString()
                        .trim();
        }

        StringBuffer sb = new StringBuffer();

        if ((schedule == null) || (schedule.length() == 0)) {
            if ((type == null) || (type.length() == 0) || type.toLowerCase()
                                                                  .startsWith("secondary")) {
                printScheduleMsg(sb, getMessage(locale,"page.schedule.STARTNoSchedNA"), inactiveFont);
            } else if (type.toLowerCase()
                               .startsWith("init")) {
                printScheduleMsg(sb, getMessage(locale,"page.schedule.STARTNoSched"), activeFont);
            } else if (type.toLowerCase().equals("postpone")) {
                printScheduleMsg(sb, getMessage(locale,"page.schedule.NoPostponeSched"), activeFont);
            } else {
                // Update and Verify Repair
                printScheduleMsg(sb, getMessage(locale,"page.schedule.NotManaged"), activeFont);
            }

            ResponseUtils.write(pageContext, sb.toString());

            return SKIP_BODY;
        }


        if (INCONSISTENT.equals(schedule)) {
            printMsg(sb, getMessage(locale, "page.schedule.Different"), inactiveFont, "false");
            ResponseUtils.write(pageContext, sb.toString());
	    this.schedule = null;
            return SKIP_BODY;
        }

        try {
            schedInfo = Schedule.getScheduleInfo(schedule);
        } catch (Exception ex) {
            JspException e = new JspException(messages.getMessage(SCHED_INTERNAL_INVALIDFORMAT, schedule));
            RequestUtils.saveException(pageContext, e);
            throw e;
        }

        String str = null;

        if ((component == null) || (component.length() == 0)) {
            String activeStart = getStart(ScheduleInfo.ACTIVE_PERIODS)
                                     .trim();
            String activeEnd = getEnd(ScheduleInfo.ACTIVE_PERIODS)
                                   .trim();
            String recurring = getRecurring()
                                   .trim();

            if (recurring.length() > 0) {
                printScheduleComponent(sb, "page.schedule.Recurring", translate(recurring));
            }

            if ("postpone".equals(type)) {
                if (activeStart.length() > 0) {
                    printScheduleComponent(sb, "page.schedule.postponeSched", translate(activeStart));
                } else {
                    printHeader(sb, activeFont, this.doubleSpace);
                    sb.append(getMessage(locale, "page.schedule.NoPostponeSched"));
                    printFooter(sb, this.doubleSpace);
                }
            } else {
                if (activeStart.length() > 0) {
                    printScheduleComponent(sb, "page.schedule.Active", translate(activeStart));

                    if (activeEnd.length() > 0) {
                        printScheduleComponent(sb, "page.schedule.Expires", translate(activeEnd));
                    } else {
                        printScheduleComponent(sb, "page.schedule.Expires", messages.getMessage(locale,"page.schedule.NoExpire"));
                    }
                } else {
                    printHeader(sb, activeFont, this.doubleSpace);
                    sb.append(getMessage(locale, "page.schedule.STARTNoSched"));
                    printFooter(sb, this.doubleSpace);
                }
            }

            str = "";
        } else if ("recurring".equals(component)) {
            str = getRecurring();

            if (str != null) {
                printHeader(sb, activeFont, this.doubleSpace);
                sb.append(messages.getMessage("page.schedule.Recurring"));
                printFooter(sb, this.doubleSpace);
            }
        } else if ("activestart".equals(component)) {
            str = getStart(ScheduleInfo.ACTIVE_PERIODS);

            if (str != null) {
                printHeader(sb, activeFont, this.doubleSpace);
                sb.append(messages.getMessage("page.schedule.Active"));
                printFooter(sb, this.doubleSpace);
            }
        } else if ("activeend".equals(component)) {
            str = getEnd(ScheduleInfo.ACTIVE_PERIODS);

            if (str != null) {
                printHeader(sb, activeFont, this.doubleSpace);
                sb.append(messages.getMessage("page.schedule.Expires"));
                printFooter(sb, this.doubleSpace);
            }
        } else if ("blackoutstart".equals(component)) {
            str = getStart(ScheduleInfo.BLACKOUT_PERIODS);

            if (str != null) {
                printHeader(sb, activeFont, this.doubleSpace);
                sb.append(messages.getMessage("page.schedule.BlackoutStart"));
                printFooter(sb, this.doubleSpace);
            }
        } else if ("blackoutend".equals(component)) {
            str = getEnd(ScheduleInfo.BLACKOUT_PERIODS);

            if (str != null) {
                printHeader(sb, activeFont, this.doubleSpace);
                sb.append(messages.getMessage("page.schedule.BlackoutEnd"));
                printFooter(sb, this.doubleSpace);
            }
        } else {
            JspException e = new JspException(messages.getMessage(SCHED_INTERNAL_UNKNOWNCOMPONENT, component));
            RequestUtils.saveException(pageContext, e);
            throw e;
        }

	this.schedule = null;

        sb.append(str);

        // Render this element to our writer
        ResponseUtils.write(pageContext, sb.toString());

        // Evaluate the remainder of this page
        return SKIP_BODY;
    }

    /**
     * Process the end of this tag.  The default implementation does nothing.
     *
     * @return REMIND
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doEndTag()
        throws JspException {
        return (EVAL_PAGE);
    }

    /**
     * Release any acquired resources.
     */
    public void release() {
        super.release();
        schedule  = null;
        component = null;
        df        = null;
    }

    // ------------------------------------------------------ Protected Methods

    /**
     * Returns the first ActivePeriod specified in the scheduleInfo object defined in this class. (There may be more than one active periods associated with a
     * schedule)
     *
     * @param type REMIND
     *
     * @return the active period from the schedule
     */
    protected SchedulePeriod getPeriod(String type) {
        List           periods = schedInfo.getList(type);
        SchedulePeriod period = null;

        if ((periods != null) && (periods.size() > 0)) {
            period = (SchedulePeriod) periods.get(0);
        }

        return period;
    }

    /**
     * Returns the recurring portion of the schedule from the schedule string  defined in this class.
     *
     * @return the string of the recurring portion of the schedule
     */
    protected String getRecurring() {
        int idx = schedule.toLowerCase()
                          .indexOf("active");

        if (idx == -1) {
            return schedule;
        } else {
            return schedule.substring(0, idx);
        }
    }

    /**
     * Returns the start component of the active period from the ScheduleInfo defined in this class.
     *
     * @param type REMIND
     *
     * @return the string of the start component of the active period
     */
    protected String getStart(String type) {
        SchedulePeriod period = getPeriod(type);

        if(DEBUG) {
            System.out.println("GET START");
            System.out.println("Period: " + period);
            System.out.println("Schedule: " + schedule);
            System.out.println("TYpe: " + type);
        }

        if (period == null) {
            return "";
        } else {
            return df.format(new Date(period.getStartSchedulePeriod().getTime()));
        }
    }

    /**
     * Returns the end component of the active period from the ScheduleInfo defined in this class.
     *
     * @param type REMIND
     *
     * @return the string of the end component of the active period
     */
    protected String getEnd(String type) {
        SchedulePeriod period = getPeriod(type);

        if (period == null) {
            return "";
        } else {
            if (ScheduleInfo.ACTIVE_PERIODS.equals(type)) {
                ScheduleDateTime endPeriod = period.getEndSchedulePeriod();

                if (endPeriod == null) {
                    return "";
                }
            }

            return df.format(new Date(period.getEndSchedulePeriod().getTime()));
        }
    }

    private void printHeader(StringBuffer sb,
                             String       fonttype,
                             String       doubleSpace) {
        if ("true".equals(doubleSpace)) {
            sb.append("<p>");
        }

        sb.append("<font class=\"" + fonttype + "\">");
    }

    private void printFooter(StringBuffer sb,
                             String       doubleSpace) {
        sb.append("</font>");

        if ("true".equals(doubleSpace)) {
            sb.append("</p>");
        } else {
            sb.append("<br>");
        }
    }

    private void printMsg(StringBuffer sb,
                          String       msg,
                          String       fonttype,
                          String       doubleSpace) {
        printHeader(sb, fonttype, doubleSpace);
        sb.append(msg);
        printFooter(sb, doubleSpace);
    }

    private void printScheduleMsg(StringBuffer sb,
                                  String       msg,
                                  String       fonttype) {
        printHeader(sb, fonttype, this.doubleSpace);
        sb.append(msg);
        printFooter(sb, this.doubleSpace);
    }

    private void printScheduleComponent(StringBuffer sb,
                                        String       msgkey,
                                        String       value) {
        String msg = messages.getMessage(locale,msgkey);

        printHeader(sb, activeFont, this.doubleSpace);
        sb.append(messages.getMessage(locale,msgkey));
        sb.append("&nbsp;");
        sb.append(value);
        printFooter(sb, this.doubleSpace);
    }

     private String getMessage(Locale locale,String key) {
         String msg = null;
        if (MSG_SHORT.equals(format)) {
            msg = messages.getMessage(locale,key + ".short");
        }
         if(msg == null) {
            msg = messages.getMessage(locale,key);
        }
        return msg;
    }

    public String translate(String schedule)  {
        final String  TEXT_PAGE_PREFIX = "page.schedule";
        final char[] escapeTokens = {',', '.' , ' ' , '+'};
        final String DELIMITER = new String(escapeTokens);
        final String SPACE = " ";

        String key = null;

        if (schedule == null) {
            return schedule;
        }
        StringTokenizer tokens = new StringTokenizer(schedule, DELIMITER, true);
        StringBuffer buffer = new StringBuffer();
        while (tokens.hasMoreTokens()) {
           String token = tokens.nextToken().toLowerCase();
           key = TEXT_PAGE_PREFIX + "." +  token;
           String message = getMessage(locale, key);
           if (message == null) {
               if (isDelimiter(token)) {
                   buffer.append(token);
               } else {
                   buffer.append(translateSubTokens(token)  );
               }
           } else {
               buffer.append(message);
           }
        }

        return buffer.toString().trim();
    }

        public String translateSubTokens(String token) {
            final String[] subTokens = {"AM", "PM"};
            final String  TEXT_PAGE_PREFIX = "page.schedule";
            String key = null;
            if (token == null) {
                return token;
            }
            StringBuffer buffer = new StringBuffer(token);
        for (int i = 0; i < subTokens.length; i++ ) {
            int index = token.lastIndexOf(subTokens[i].toLowerCase());
            if (index != -1) {
               key = TEXT_PAGE_PREFIX + "." +  subTokens[i].toLowerCase();
               String message = getMessage(locale, key);
               if ( message != null ) {
                    buffer.replace(index ,index + subTokens[i].length(), message);
                }
            }

        }

        return buffer.toString();
        }

    public static boolean isDelimiter(String token) {
        final char[] escapeTokens = {',', '.' , ' ' , '+'};
        for (int i = 0; i < escapeTokens.length ; i++) {
             if (token.equals(String.valueOf(escapeTokens[i]))) {
                 return true;
             }
        }
        return false;
    }

}
