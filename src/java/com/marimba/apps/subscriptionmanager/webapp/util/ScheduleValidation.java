// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.util;

import org.apache.commons.validator.*;

import org.apache.struts.action.*;
import org.apache.struts.util.MessageResources;
import org.apache.struts.Globals;

import java.io.*;

import java.net.*;

import java.util.*;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.ParseException;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.ScheduleEditForm;

import com.marimba.castanet.schedule.ScheduleInfo;

import com.marimba.tools.ldap.*;

import com.marimba.tools.util.*;

import com.marimba.webapps.intf.*;

import com.marimba.webapps.tools.util.*;

/**
 * This is the location for method used for schedule input validation
 *
 * @author Theen-Theen Tan
 * @author Venu Gopala Rao Kotha
 * @version 1.6, 30/11/2005
 */
public class ScheduleValidation implements IWebAppConstants, IErrorConstants {

    public static boolean validateActivationStart(java.lang.Object                      bean,
                                                  ValidatorAction                       va,
                                                  Field                                 field,
                                                  org.apache.struts.action.ActionErrors errors,
                                                  javax.servlet.http.HttpServletRequest request,
                                                  javax.servlet.ServletContext          application) {
        if ("false".equals(GUIUtils.getValueAsString((IMapProperty) bean, ScheduleEditForm.SET_SCHEDULE))) {
            return true;
        }

        /** Validate only if there is a  activation time must be greater than the current time. */
        boolean startSchedExists = "true".equals(GUIUtils.getValueAsString((IMapProperty) bean, ScheduleEditForm.ACTIVE_PERIOD_START));
        SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, request.getLocale());

        if (startSchedExists ) {
            String date =  GUIUtils.getValueAsString((IMapProperty) bean, "ACTIVE_PERIOD_START_DATETIME");
            try {
                Date startDate = sdf.parse(date);
                Date systemDate = new Date();

                boolean isActiveSchedEdit = "true".equals(GUIUtils.getValueAsString((IMapProperty) bean, ScheduleEditForm.ACTIVE_PERIOD_START_EDIT));

                if (isActiveSchedEdit) {
                    return true;
                }
                boolean before = startDate.before(systemDate);
                if (before && !"cancel".equals(request.getParameter("action"))) {
                    errors.add(field.getProperty(), new KnownActionError(VALIDATION_SCHEDULE_STARTBEFOREACTIVATE));
                    return false;
                }
            } catch (ParseException e) {
                // think already finished
            }
        }

        return true;
    }



    public static boolean validateActivationDates(java.lang.Object                      bean,
                                                  ValidatorAction                       va,
                                                  Field                                 field,
                                                  org.apache.struts.action.ActionErrors errors,
                                                  javax.servlet.http.HttpServletRequest request,
                                                  javax.servlet.ServletContext          application) {

        if ("false".equals(GUIUtils.getValueAsString((IMapProperty) bean, ScheduleEditForm.SET_SCHEDULE))) {
            return true;
        }
        
        if ("cancel".equals(request.getParameter("action"))) {
            return true;
        }
        /** Validate only if there is a schedule for the expiration time */
        Date startDate = null;
        Date endDate = null;

        boolean startSchedExists = "true".equals(GUIUtils.getValueAsString((IMapProperty) bean, ScheduleEditForm.ACTIVE_PERIOD_START));
        boolean endSchedExists = "true".equals(GUIUtils.getValueAsString((IMapProperty) bean, ScheduleEditForm.ACTIVE_PERIOD_END));
        SimpleDateFormat sdf = (SimpleDateFormat)SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, request.getLocale());
        sdf.setLenient(false);
        if (startSchedExists) {
            String date = ((ScheduleEditForm)bean).getValue("ACTIVE_PERIOD_START_DATETIME").toString();
            try {
                startDate = sdf.parse(date);
            } catch (ParseException e) {
                //e.printStackTrace();
                errors.add(ScheduleEditForm.ACTIVE_PERIOD_START_DATETIME, new KnownActionError(INVALID_ACTIVATION_DATE_FORMAT, date));
                return false;
            } catch (IllegalArgumentException e) {
                errors.add(ScheduleEditForm.ACTIVE_PERIOD_START_DATETIME, new KnownActionError(INVALID_ACTIVATION_DATE_FORMAT, date));
                return false;
            }
        }

        if (endSchedExists) {
            String date = ((ScheduleEditForm)bean).getValue("ACTIVE_PERIOD_END_DATETIME").toString();
            try {
                endDate = sdf.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
                errors.add(ScheduleEditForm.ACTIVE_PERIOD_END_DATETIME, new KnownActionError(INVALID_EXPIRATION_DATE_FORMAT, date));
                return false;
            } catch (IllegalArgumentException e) {
                errors.add(ScheduleEditForm.ACTIVE_PERIOD_START_DATETIME, new KnownActionError(INVALID_ACTIVATION_DATE_FORMAT, date));
                return false;
            }
        }

        if (startSchedExists && endSchedExists) {
            int ret = startDate.compareTo(endDate);
            if (ret != -1) {
                errors.add(field.getProperty(), new KnownActionError(VALIDATION_SCHEDULE_STARTAFTEREXPIRE));
                return false;
            }            
        }

        return true;
    }

    public static boolean validateFields(java.lang.Object                      bean,
                                         ValidatorAction                       va,
                                         Field                                 field,
                                         org.apache.struts.action.ActionErrors errors,
                                         javax.servlet.http.HttpServletRequest request,
                                         javax.servlet.ServletContext          application) {

        if ("false".equals(GUIUtils.getValueAsString((IMapProperty) bean, ScheduleEditForm.SET_SCHEDULE))) {
            return true;
        }
        ScheduleEditForm form = (ScheduleEditForm)bean;
        MessageResources resources = (MessageResources) application.getAttribute(Globals.MESSAGES_KEY);
        return form.validateSchedule(resources, errors);
    }

}
