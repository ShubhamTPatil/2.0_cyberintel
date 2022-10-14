// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.forms;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import java.io.*;

import java.net.*;

import java.text.NumberFormat;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.marimba.apps.subscription.common.intf.SubInternalException;

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.*;

import com.marimba.webapps.intf.GUIException;

/**
 * REMIND
 *
 * @author $Author$
 * @version $File$
 */
public class AdvancePkgForm
    extends AbstractForm
    implements IWebAppConstants {
    public final static String FROM = "from";
    public final static String TO = "to";
    public final static String FROM_TIME = "start time";
    public final static String TO_TIME = "end time";
    public final static String HOUR = "hour";
    public final static String MIN = "minutes";
    private Calendar           calendar = new GregorianCalendar();

    /**
     * The initialize method is used to load the existing blackout schedule into the Edit Blackout form.                                 The 'Blackout
     * Schedule' button that is currently located on the right-pane of the Target View page call the /blackoutLoad.do action.  This action uses the target
     * stored in the session, 'main_page_target'.  It queries LDAP for the corresponding subscription policy, instantiates the blackoutForm, and calls this
     * initialize method with the subscription's blackout string.
     *
     * @param blackout REMIND
     *
     * @return REMIND
     */
    public boolean initialize(String blackout) {
      /*  if (getServlet() == null) {
            return false;
        }

        if (DEBUG) {
            System.out.println("in initialize, blackout= " + blackout);
        }

        // parse blackout string

            set("textarea", "hi joe");
            set("fromHour", "hh");
            set("fromMin", "mm");
            set("fromAMPM", AM);
            set("toHour", "hh");
            set("toMin", "mm");
            set("toAMPM", AM);
                         (*/
            return true;

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

    // Assumes that all fields, including, time has been validated
    private void setCalendar(String type) {
        int hr = new Integer(getProperty(type + "Hour")).intValue();
        int min = new Integer(getProperty(type + "Min")).intValue();
        int ampm = AM.equals(getProperty(type + "AMPM")) ? Calendar.AM
                   : Calendar.PM;

        // set the day to be today... then overwrite the other fields
        calendar.setTime(new Date());
        calendar.set(Calendar.AM_PM, ampm);
        calendar.set(Calendar.HOUR, hr);
        calendar.set(Calendar.MINUTE, min);
    }

    /**
     * REMIND
     *
     * @param mapping REMIND
     * @param request REMIND
     */
    public void reset(ActionMapping      mapping,
                      HttpServletRequest request) {

    }

    /**
     * REMIND
     *
     * @param property REMIND
     *
     * @return REMIND
     */
    public String getProperty(String property) {
        Object value = getValue(property);

        if (value instanceof String[]) {
            String[] valueArray = (String[]) value;

            if (valueArray.length > 0) {
                return ((String[]) value) [0];
            }
        } else {
            return (String) value;
        }

        return "";
    }
}
