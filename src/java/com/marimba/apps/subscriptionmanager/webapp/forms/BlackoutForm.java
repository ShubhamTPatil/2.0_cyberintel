// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.forms;

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

/**
 * REMIND
 *
 * @author $Author$
 * @version $File$
 */
public class BlackoutForm
        extends AbstractForm
        implements IWebAppConstants {
    public final static String FROM = "from";
    public final static String TO = "to";
    public final static String FROM_TIME = "start time";
    public final static String TO_TIME = "end time";
    public final static String HOUR = "hour";
    public final static String MIN = "minutes";
    private Calendar calendar = new GregorianCalendar();
    private boolean serviceExemptFromBlackout = false;
    private boolean patchServiceExemptFromBlackout = false;

 /**
     * The initialize method is used to load the existing blackout schedule into the Edit Blackout form. The 'Blackout
     * Schedule' button that is currently located on the right-pane of the Target View page call the /blackoutLoad.do
     * action.  This action uses the target stored in the session, 'main_page_target'.  It queries LDAP for the
     * corresponding subscription policy, instantiates the blackoutForm, and calls this initialize method with the
     * subscription's blackout string.
     *
     * @param blackout REMIND
     *
     * @return REMIND
     */                                                                     

    public boolean initialize(String blackout, boolean serviceExempt, boolean patchExempt, Locale locale) {
        if (DEBUG) {
            System.out.println("in initialize, blackout= " + blackout);
        }
        String blckPriority = NOTAVBLE;

        setValue("serviceExemptFromBlackout", new Boolean(serviceExempt).toString());
        setValue("patchServiceExemptFromBlackout",  new Boolean(patchExempt).toString());

        SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getTimeInstance(DateFormat.SHORT, locale);
        SimpleDateFormat mrbadf = new SimpleDateFormat(IWebAppConstants.MRBA_TIMEFORMAT, java.util.Locale.US);

        // parse blackout string
        if (NOBLACKOUT.equals(blackout)) {
            setValue("noBlackout", "true");
            setValue("fromTime", sdf.toPattern());
            setValue("toTime", sdf.toPattern());
            setValue("blackoutPriority", blckPriority);
            return true;
        } else if ((blackout != null) && !"".equals(blackout)) {
            setValue("noBlackout", "false");

            int prtyIndex = blackout.lastIndexOf(PROP_DELIM);

            if(prtyIndex != -1) {
                blckPriority = blackout.substring(prtyIndex+1,blackout.length());
                blackout = blackout.substring(0,prtyIndex);
            }
            if(DEBUG) {
            	System.out.println("blckPriority : " + blckPriority);
            	System.out.println("blackout : " + blackout);
            }
            try {
            	//RemindMe: Code duplication need to be removed
	            if(blackout.indexOf("BLACKOUT") >=0) {
	            	//Old format policy blackout string
	            	String[] temp = blackout.split("anytime on | BLACKOUT ");
	                String schedule = "";
	                String weekDays = "";

	                if (!"".equals(temp[0])) {
	                    schedule = temp[0];
	                    weekDays = "";
	                    checkallDays("true");
	                } else if ("".equals(temp[0])){
	                    weekDays = temp[1];
	                    schedule = temp[2];
	                }
	                
	                if (DEBUG) {
	                    System.out.println("weekDays : " + weekDays);
	                    System.out.println("Blockout Period  : " + schedule);
	                }
	                StringTokenizer fromToTok = new StringTokenizer(schedule, "-");
	                String from = "";
	                String to = "";

	                while (fromToTok.hasMoreTokens() && (fromToTok.countTokens() == 2)) {
	                    from = fromToTok.nextToken();
	                    to = fromToTok.nextToken();
	                }

	                try {
	                    String fromTime = sdf.format(mrbadf.parse(from));
	                    String toTime = sdf.format(mrbadf.parse(to));
	                    setValue("fromTime", fromTime);
	                    setValue("toTime", toTime);
	                    setValue("blackoutPriority", blckPriority);

	                } catch (ParseException e) {
	                    if (DEBUG) {
	                        e.printStackTrace();
	                    }
	                }
	                
	                if (weekDays != "") {
	                    String [] day = weekDays.split("\\+");
	                    for (int i = 0 ; i < day.length ; i++) {                        
	                        if ("mon".equals(day[i])) {
	                            setValue("monday","true");
	                        }
	                         if ("tue".equals(day[i])) {
	                            setValue("tuesday","true");
	                        }
	                        if ("wed".equals(day[i])) {
	                            setValue("wednesday","true");
	                        }
	                         if ("thu".equals(day[i])) {
	                            setValue("thursday","true");
	                        }
	                         if ("fri".equals(day[i])) {
	                            setValue("friday","true");
	                        }
	                         if ("sat".equals(day[i])) {
	                            setValue("saturday","true");
	                        }
	                        if ("sun".equals(day[i])) {
	                            setValue("sunday","true");
	                        }

	                    }
	                }
	            }
	            else {
	            	if(DEBUG) {
	                	System.out.println("Blackout parsing in new format");
                	}
	            	String[] temp = blackout.split("between | on ");
	                String schedule = "";
	                String weekDays = "";
	                
	                if(temp != null) {
	                	if(DEBUG) {
		                	for(String tokens: temp){
		                		System.out.println("Tokens:"+ tokens);
		                	}
	                	}
		                
		                if (!"".equals(temp[0])) {
		                    schedule = temp[0];
		                    weekDays = "";
		                    checkallDays("true");
		                } else if ("".equals(temp[0])){
		                	schedule = temp[1];
		                	weekDays = temp[2];
		                }
	                }
	                else {
	                	//Do nothing
	                	if(DEBUG) {
	                		System.out.println("No value, new policy");
	                	}
	                }
	                
	                if (DEBUG) {
	                    System.out.println("weekDays : " + weekDays);
	                    System.out.println("Blockout Period  : " + schedule);
	                }
	                
	                StringTokenizer fromToTok = new StringTokenizer(schedule, "and");
	                String from = "";
	                String to = "";

	                while (fromToTok.hasMoreTokens() && (fromToTok.countTokens() == 2)) {
	                    from = fromToTok.nextToken();
	                    to = fromToTok.nextToken();
	                }
	                if(!"".equals(from) && !"".equals(to)) {
		                try {
		                    String fromTime = sdf.format(mrbadf.parse(from));
		                    String toTime = sdf.format(mrbadf.parse(to));
		                    setValue("fromTime", fromTime);
		                    setValue("toTime", toTime);
		                    setValue("blackoutPriority", blckPriority);
	
		                } catch (ParseException e) {
		                    if (DEBUG) {
		                        e.printStackTrace();
		                    }
		                }
	                }
	                
	                if (weekDays != "") {
	                    String [] day = weekDays.split("\\+");
	                    for (int i = 0 ; i < day.length ; i++) {                        
	                        if ("mon".equals(day[i])) {
	                            setValue("monday","true");
	                        }
	                         if ("tue".equals(day[i])) {
	                            setValue("tuesday","true");
	                        }
	                        if ("wed".equals(day[i])) {
	                            setValue("wednesday","true");
	                        }
	                         if ("thu".equals(day[i])) {
	                            setValue("thursday","true");
	                        }
	                         if ("fri".equals(day[i])) {
	                            setValue("friday","true");
	                        }
	                         if ("sat".equals(day[i])) {
	                            setValue("saturday","true");
	                        }
	                        if ("sun".equals(day[i])) {
	                            setValue("sunday","true");
	                        }

	                    }
	                }
	            }
              } catch (Exception e) {
                if (DEBUG) {
                    System.out.println("Error While Parsing");
                    e.printStackTrace();
                }
            }
            return true;
        } else {
            // if blackout string was null or empty string
            // set to defaults
            setValue("fromTime", sdf.toPattern());
            setValue("toTime", sdf.toPattern());
            checkallDays("false");
            setValue("blackoutPriority", blckPriority);
            return false;
        }

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
     * @param type REMIND
     *
     * @return REMIND
     */

    // this is added by kumaravel for get getting calendar object instead of the Date object to the calling function
    public Calendar getPeriodCalendar(String type) {
        setCalendar(type);

        return calendar;
    }

    /**
     * REMIND
     *
     * @param mapping REMIND
     * @param request REMIND
     */
    public void reset(ActionMapping mapping,
                      HttpServletRequest request) {
        setValue("noBlackout", "true");
        setValue("fromHour", "hh");
        setValue("fromMin", "mm");
        setValue("fromAMPM", AM);
        setValue("toHour", "hh");
        setValue("toMin", "mm");
        setValue("toAMPM", AM);
        setValue("blackoutPriority", NOTAVBLE);
        checkallDays("false");
    }
    public void checkallDays(String checkValue) {
        setValue("monday",checkValue);
        setValue("tuesday",checkValue);
        setValue("wednesday",checkValue);
        setValue("thursday",checkValue);
        setValue("friday",checkValue);
        setValue("saturday",checkValue);
        setValue("sunday",checkValue);
    }
    public boolean isServiceExemptFromBlackout() {
        return serviceExemptFromBlackout;
    }

    public void setServiceExemptFromBlackout(boolean serviceExemptFromBlackout) {
        this.serviceExemptFromBlackout = serviceExemptFromBlackout;
    }

    public boolean isPatchServiceExemptFromBlackout() {
        return patchServiceExemptFromBlackout;
    }

    public void setPatchServiceExemptFromBlackout(boolean patchServiceExemptFromBlackout) {
        this.patchServiceExemptFromBlackout = patchServiceExemptFromBlackout;
    }
}
