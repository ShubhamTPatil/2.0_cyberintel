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

import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * This tag renders the &lt;option&gt; elements depending upon the given date, and type.  This tags is only valid if nested within a
 * org.apache.struts.taglib.html.SelectTag  date - a date string that can be parsed by java.text.SimpleDateFormat object type - { months|days|years }
 *
 * @author Theen-Theen Tan
 * @version 1.2, 12/14/2001
 */
public class DateOptionsTag
    extends TagSupport {
    static SimpleDateFormat  sdf = new SimpleDateFormat();
    static DateFormatSymbols symbols = sdf.getDateFormatSymbols();
    static TreeMap           months = new TreeMap();
    static TreeMap[]         days = new TreeMap[4];
    static int[]             monthToDays = { 0, 3, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0 };

    // Maps option values to option labels
    static {
        String[] monArray = symbols.getMonths();

        for (int i = 0; i < 12; i++) {
            months.put(new Integer(i + 1), monArray [i]);
        }

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(2);
        days [0] = new TreeMap();

        for (int i = 1; i <= 31; i++) {
            days [0].put(new Integer(i), nf.format(new Integer(i).doubleValue()));
        }

        days [1] = new TreeMap();

        for (int i = 1; i <= 30; i++) {
            days [1].put(new Integer(i), nf.format(new Integer(i).doubleValue()));
        }

        days [2] = new TreeMap();

        for (int i = 1; i <= 29; i++) {
            days [2].put(new Integer(i), nf.format(new Integer(i).doubleValue()));
        }

        days [3] = new TreeMap();

        for (int i = 1; i <= 28; i++) {
            days [3].put(new Integer(i), nf.format(new Integer(i).doubleValue()));
        }
    }

    /** The message resources for this package. */
    protected static MessageResources messages = MessageResources.getMessageResources("ApplicationResources");

    /** The name of the bean containing the Date object */
    protected String date = null;

    /** The name of the bean containing the Date object { days, months, years} */
    protected String type = null;

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getDate() {
        return date;
    }

    /**
     * REMIND
     *
     * @param date REMIND
     */
    public void setDate(String date) {
        this.date = date;
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
     * Process the start of this tag.
     *
     * @return REMIND
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doStartTag()
        throws JspException {
        // Acquire the select tag we are associated with
        SelectTag selectTag = (SelectTag) pageContext.getAttribute(org.apache.struts.taglib.html.Constants.SELECT_KEY);

        if (selectTag == null) {
            JspException e = new JspException(messages.getMessage("optionTag.select"));
            RequestUtils.saveException(pageContext, e);
            throw e;
        }

        StringBuffer sb = new StringBuffer();
        Date         dateObj = null;

        if ((date == null) || (date.length() == 0)) {
            dateObj = new Date();
        } else {
            dateObj = new Date(date);
        }

        Map map = null;

        if ("days".equals(type)) {
            // Get the days in the month associated with the given date
            if ((dateObj.getMonth() == 1) && ((dateObj.getYear() % 4) == 0)) {
                map = days [2];
            } else {
                map = days [monthToDays [dateObj.getMonth()]];
            }
        } else if ("months".equals(type)) {
            map = months;
        } else if ("years".equals(type)) {
            int yr = 1900 + new Date().getYear();
            map = new TreeMap();

            for (int i = yr; i < (yr + 10); i++) {
                map.put(new Integer(i), String.valueOf(i));
            }
        }

        Iterator ite = map.entrySet()
                          .iterator();

        while (ite.hasNext()) {
            Map.Entry entry = (Map.Entry) ite.next();
            addOption(sb, String.valueOf(entry.getKey()), (String) entry.getValue(), selectTag.isMatched(String.valueOf(entry.getKey())));
        }

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
        date = null;
        type = null;
    }

    // ------------------------------------------------------ Protected Methods

    /**
     * Add an option element to the specified StringBuffer based on the specified parameters.
     *
     * @param sb StringBuffer accumulating our results
     * @param value Value to be returned to the server for this option
     * @param label Value to be shown to the user for this option
     * @param matched Should this value be marked as selected?
     */
    protected void addOption(StringBuffer sb,
                             String       value,
                             String       label,
                             boolean      matched) {
        sb.append("<option value=\"");
        sb.append(value);
        sb.append("\"");

        if (matched) {
            sb.append(" selected=\"selected\"");
        }

        sb.append(">");
        sb.append(ResponseUtils.filter(label));
        sb.append("</option>\r\n");
    }
}
