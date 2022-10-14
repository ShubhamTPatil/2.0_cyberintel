// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.util;



import org.apache.commons.validator.*;


import java.util.*;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

import com.marimba.apps.subscriptionmanager.intf.*;
import com.marimba.apps.subscriptionmanager.webapp.forms.BlackoutForm;


import com.marimba.webapps.intf.*;
import com.marimba.webapps.tools.util.*;

/**
 * Validation for blackout schedule All form validation methods should be written such that null is a possible input.
 *
 * @author Michele Lin
 * @version 1.1, 01/31/2002
 */
public class BlackoutValidation
    implements IWebAppConstants,
                   IErrorConstants {
    final static boolean DEBUG = IAppConstants.DEBUG;
    final static int STYLE = DateFormat.SHORT;

    //    final static boolean DEBUG = false;

    /**
     * Validates the hour and minutes fields for the blackout schedule. namely, checking that each field is present (required), an integer, and its range.
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
    public static boolean validateBlackout(java.lang.Object                      bean,
                                           ValidatorAction                       va,
                                           Field                                 field,
                                           org.apache.struts.action.ActionErrors errors,
                                           javax.servlet.http.HttpServletRequest request,
                                           javax.servlet.ServletContext          application) {
        /** Validates only if blackout radio button not set to "clear blackout" */
        String  noBlackout = GUIUtils.getValueAsString((IMapProperty) bean, "noBlackout");
        boolean schedExists = "false".equals(noBlackout);

        if (DEBUG) {
            System.out.println("validateBlackout");
            System.out.println("schedExists= " + schedExists);
        }

        if (schedExists) {
            // strings for the error text
            boolean requiredCheck = ValidationUtil.validateRequiredNoAddError(bean, field);

            if (!requiredCheck) {
                //errors.add(field.getProperty(), new KnownActionError(VALIDATION_BLACKOUT_FIELD_REQUIRED, timeStr, fieldStr));

                return false;
            }
        }

        // user selected never and the hour and minutes do not apply
        return true;
    }

    /**
     * Validates the semantics for the blackout schedule,  namely, checking that the to time is after the from time.
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
    public static boolean validateBlackoutSeq(java.lang.Object                      bean,
                                              ValidatorAction                       va,
                                              Field                                 field,
                                              org.apache.struts.action.ActionErrors errors,
                                              javax.servlet.http.HttpServletRequest request,
                                              javax.servlet.ServletContext          application) {
        /**
         * Validates only if blackout radio button not set to "clear blackout"
         */
        if (DEBUG) {
            System.out.println("validateBlackoutSequence");
        }

        String  noBlackout = GUIUtils.getValueAsString((IMapProperty) bean, "noBlackout");
        boolean schedExists = "false".equals(noBlackout);
        int sMin = Integer.parseInt(field.getVarValue("min"));
        int sMax = Integer.parseInt(field.getVarValue("max"));

        if (DEBUG) {
            System.out.println("schedExists= " + schedExists);
        }

        if (schedExists) {
           // boolean sequenceCheck = validateSequence(bean, field);
            String fromTime = GUIUtils.getValueAsString((IMapProperty) bean, "fromTime");
            String toTime = GUIUtils.getValueAsString((IMapProperty) bean, "toTime");
            boolean validFrom  = validateTime(fromTime, request.getLocale());
            boolean validTo =  validateTime(toTime, request.getLocale());
            boolean validFromTo = validateBlackOut(bean, request.getLocale());

            String mon = GUIUtils.getValueAsString((IMapProperty) bean, "monday");
            String tue = GUIUtils.getValueAsString((IMapProperty) bean, "tuesday");
            String wed = GUIUtils.getValueAsString((IMapProperty) bean, "wednesday");
            String thu = GUIUtils.getValueAsString((IMapProperty) bean, "thursday");
            String fri = GUIUtils.getValueAsString((IMapProperty) bean, "friday");
            String sat = GUIUtils.getValueAsString((IMapProperty) bean, "saturday");
            String sun = GUIUtils.getValueAsString((IMapProperty) bean, "sunday");
            String priority = GUIUtils.getValueAsString((IMapProperty) bean, "blackoutPriority");
            if(!((NOTAPP.equals(priority)) || null == priority)) {
                try {
                    int blckPriority = Integer.parseInt(priority);
                    if(blckPriority == 0) {
                        errors.add("blackoutPriority", new KnownActionError(VALIDATION_BLACKOUT_PRIORITY_INVALID, priority ));
                        return false;
                    }
                    if(!((blckPriority >= sMin) && (blckPriority<= sMax))) {
                        errors.add("blackoutPriority", new KnownActionError(VALIDATION_BLACKOUT_PRIORITY_INVALID, priority ));
                        return false;
                    }
                } catch(NumberFormatException ne) {
                    errors.add("blackoutPriority", new KnownActionError(VALIDATION_BLACKOUT_PRIORITY_INVALID, priority ));
                    return false;
                }
            }



            boolean validateDays = validateWeekDays(mon,tue,wed,thu,fri,sat,sun);
            if (! validFrom) {
               errors.add("fromTime", new KnownActionError(VALIDATION_BLACKOUT_FIELD_INVALIDFORMAT, fromTime ));
            }

            if (!validTo) {
              errors.add("toTime", new KnownActionError(VALIDATION_BLACKOUT_FIELD_INVALIDFORMAT, toTime ));
            }
            if (!validFrom || !validTo) {
                return false;
            }
            if (!validFromTo) {
                errors.add(field.getProperty(), new KnownActionError(VALIDATION_BLACKOUT_FIELD_SEQUENCE));
                return false;
            }
             if (!validateDays) {
                errors.add("weekDays", new KnownActionError(VALIDATION_BLACKOUT_WEEKDAYS_REQUIRED));
                return false;
            }
        }

        // user selected never and the hour and minutes do not apply
        return true;

    }

    private static boolean validateSequence(java.lang.Object bean,
                                            Field            field) {
		int startHour;
		int startMin;
		int startampm;

		int endHour;
		int endMin;
		int endampm;

		boolean	areHoursEqual;
		boolean	areMinsEqual;
		boolean	areampmsEqual;
		boolean after;

        if (DEBUG) {
            System.out.println("in validateSequence sub-method");
        }

        Date startDate = ((BlackoutForm) bean).getPeriod(BlackoutForm.FROM);

		// To retrieve the starting hours, minutes and starting AM or PM for the blackout period
		startHour=(((BlackoutForm) bean).getPeriodCalendar(BlackoutForm.FROM)).get(Calendar.HOUR_OF_DAY);
		startMin=(((BlackoutForm) bean).getPeriodCalendar(BlackoutForm.FROM)).get(Calendar.MINUTE);
		startampm=(((BlackoutForm) bean).getPeriodCalendar(BlackoutForm.FROM)).get(Calendar.AM_PM);



        if (DEBUG) {
            System.out.println("startDate= " + startDate);
        }

        Date endDate = ((BlackoutForm) bean).getPeriod(BlackoutForm.TO);

		// To retrieve the ending hours, minutes and ending AM or PM for the blackout period
		endHour=(((BlackoutForm) bean).getPeriodCalendar(BlackoutForm.TO)).get(Calendar.HOUR_OF_DAY);
		endMin=(((BlackoutForm) bean).getPeriodCalendar(BlackoutForm.TO)).get(Calendar.MINUTE);
		endampm=(((BlackoutForm) bean).getPeriodCalendar(BlackoutForm.TO)).get(Calendar.AM_PM);


        if (DEBUG) {
            System.out.println("endDate= " + endDate);
        }

       // boolean after = startDate.after(endDate);

		//To set the block out period round the clock
	    areHoursEqual=(startHour==endHour)?true:false;
		areMinsEqual=(startMin==endMin)?true:false;
		areampmsEqual=(startampm==endampm)?true:false;

		//after gets the value "true"  when start period and end period are equal. Otherwise after gets the value "false"
		after=areHoursEqual && areMinsEqual && areampmsEqual;

        if (DEBUG) {
            System.out.println("after= " + after);
        }

        if (DEBUG) {
            System.out.println("startDate= " + startDate);
            System.out.println("endDate= " + endDate);
            System.out.println("after= " + after);
        }

        if (after) {
            return false;
        }

        return true;
    }

    private static boolean validateTime(String time, Locale locale) {
        if (time == null || "".equals(time)) {
            return false;
        }
        SimpleDateFormat sdf = (SimpleDateFormat)SimpleDateFormat.getTimeInstance(STYLE, locale);
        sdf.setLenient(false);
        try {
            Date d = sdf.parse(time);
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
            return false;
        }

        return true;
    }

    private static boolean validateBlackOut(java.lang.Object bean, Locale locale) {
        SimpleDateFormat sdf = (SimpleDateFormat)SimpleDateFormat.getTimeInstance(STYLE, locale);
        sdf.setLenient(false);
        String fromTime = GUIUtils.getValueAsString((IMapProperty)bean, "fromTime");
        String toTime =  GUIUtils.getValueAsString((IMapProperty)bean, "toTime");
        try {
            Date start = sdf.parse(fromTime);
            Date end =  sdf.parse(toTime);
            return end.after(start);
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
            return false;
        }
    }
    private static boolean validateWeekDays(String mon,String tue,String wed,String thu,String fri,String sat,String sun) {
        return (("true".equals(mon) || "true".equals(tue) || "true".equals(wed) || "true".equals(thu) ||
                "true".equals(fri) || "true".equals(sat) || "true".equals(sun)));
    }
}
