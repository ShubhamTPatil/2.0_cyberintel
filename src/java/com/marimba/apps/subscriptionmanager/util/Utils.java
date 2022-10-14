// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.util;

import java.sql.Timestamp;
import java.util.*;

import com.marimba.apps.subscription.common.*;                                                                                                                  
import javax.servlet.http.HttpServletRequest;
import com.marimba.intf.msf.*;
import com.marimba.intf.ldap.*;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.intf.util.IProperty;
import com.marimba.intf.msf.AuthenticationException;

/**
 * REMIND
 *
 * @author $Author$
 * @version $File$
 */
public class Utils
    implements ISubscriptionConstants,ILDAPConstants, ILDAPErrConstants {
	static boolean DEBUG = IAppConstants.DEBUG;
    /* User roles */
    final static String[] PRIMARY_ADMIN_ROLES = {"PrimaryAdmin", "CloudPrimaryAdmin", "TenantPrimaryAdmin"};
    final static String[] ADMIN_ROLES = {"Admin", "TenantAdmin", "CloudAdmin"};
    final static String[] OPERATOR_ROLES = {"Operator", "TenantOperator", "CloudOperator"};

    boolean validateTarget(String str) {
        String notallowed;
        String errormsg;

        notallowed = NOTALLOWED_FILECHARS;

        int len = notallowed.length();

        for (int i = 0; i < len; i++) {
            char c = notallowed.charAt(i);

            if (str.indexOf(c) != -1) {
                return false;
            }
        }

        return true;
    }

    /* Validate Blackout schedule
     * @param  blkshd        blackout schedule string valid syntax is
     *                       hh:mm[am/pm]-hh:mm[am/pm]
     * @return returns true if the ScheduleInfo is a valid Active Schedule
     */
    static public boolean validateBlackoutScheduleTime(String time) {
        if (time == null) {
            return false;
        }

        time = time.toLowerCase().trim();

        if (time.startsWith("-") || time.endsWith("-")) {
            return false;
        }

        StringTokenizer st = new StringTokenizer(time, "-");
        int             count = st.countTokens();

        if (count != 2) {
            return false;
        }

        int start = 0;
        int end = 0;
        count = 0;

        while (st.hasMoreTokens()) {
            count++;

            String str = st.nextToken();

            if (str != null) {
                str = str.trim();

                boolean isAM = true;

                if (str.endsWith("pm")) {
                    isAM = false;
                } else if (!str.endsWith("am")) {
                    return false;
                }

                int len = str.length();
                str = str.substring(0, len - 2);

                int ind = str.indexOf(":");

                if (ind == -1) {
                    return false;
                }

                String hourStr = str.substring(0, ind);
                String minStr = str.substring(ind + 1);

                try {
                    int hour = Integer.parseInt(hourStr);
                    int min = Integer.parseInt(minStr);

                    if ((min < 0) || (min > 59)) {
                        return false;
                    }

                    if ((hour < 0) || (hour > 12)) {
                        return false;
                    }

                    if (hour == 12) {
                        if (isAM) {
                            hour = 0;
                        }
                    } else {
                        if (!isAM) {
                            hour += 12;
                        }
                    }

                    if (count == 1) {
                        start = (hour * 60) + min;
                    } else {
                        end = (hour * 60) + min;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }

        // end should be greater than start as the blackout period
        // is not allowed to crosss day boundaries
        if (start >= end) {
            return false;
        }

        return true;
    }

    // Checking for Week days
    static public boolean validateBlackoutScheduleDays(String days) {
        if( null != days && days.length() > 0 ) {
            int hitcount = 0;

            String temp_days[] = days.split("[ + ]");
            String weekdays[] = {"sun","mon","tue","wed","thu","fri","sat"};

            for(int i =0 ; i < temp_days.length; i++) {

                for(int j = 0; j < weekdays.length; j++ ) {

                    if( (weekdays[j].equalsIgnoreCase( temp_days[i] )) ) {

                        hitcount = hitcount + 1;
                    }
                }
            }

            if( hitcount != temp_days.length) {
                return false;
            }
        }

        return true;
    }
    static public String constructBlkoutString(String daysValue, String timeValue) {        

        daysValue = daysValue.trim();
        timeValue = timeValue.trim();

        if( null == daysValue ) {
            daysValue = "sun+mon+tue+wed+thu+fri+sat";
        }
        
        return "anytime on " + daysValue.toLowerCase() + BLACKOUT + timeValue;
    }
    public static Timestamp getCurrentTimeStamp()
            throws Exception {
            Timestamp  timestamp = null;
            Date date = null;
            try {
            	Date curDateTime = new Date();
                timestamp = new Timestamp(curDateTime.getTime());
            } catch (Exception pe) {
                System.out.println("Failed to fet current time in TimeStamp type");
            }

            return timestamp;
    }
    public static boolean isUserInRole(IUserPrincipal user, String[] roleNames) {
    	if(null == user) return false;
    	
    	String userRole = user.getUserRole();
    	if(null == userRole) return false; 
    	for(String roleName : roleNames) {
    		if(userRole.equalsIgnoreCase(roleName)) {
    			return true;
    		}
    	}
    	return false;
    }

    public static boolean isPrimaryAdmin(IUserPrincipal user) {
        boolean isPrimaryAdmin = isUserInRole(user, PRIMARY_ADMIN_ROLES);
        if (DEBUG) System.out.println("Is PrimaryAdmin? : " + isPrimaryAdmin);
        return isPrimaryAdmin;
    }

    public static boolean isPrimaryAdmin(HttpServletRequest request) {
        IUserPrincipal user = (IUserPrincipal) request.getUserPrincipal();
        return isPrimaryAdmin(user);
    }

    public static boolean isAdministrator(IUserPrincipal user) {
        boolean op = isUserInRole(user, ADMIN_ROLES);
        if (DEBUG) System.out.println("Is Administrator? : " + op);
        return op;
    }

    public static boolean isAdministrator(HttpServletRequest request) {
        IUserPrincipal user = (IUserPrincipal) request.getUserPrincipal();
        return isAdministrator(user);
    }

    public static boolean isOperator(IUserPrincipal user) {
        boolean op = isUserInRole(user, OPERATOR_ROLES);
        if (DEBUG) System.out.println("Is Operator? : " + op);
        return op;
    }

    public static boolean isOperator(HttpServletRequest request) {
        IUserPrincipal user = (IUserPrincipal) request.getUserPrincipal();
        return isOperator(user);
    }
	public static int getErrorCode(int errorCode) {
		int errorCodeStr = 0;

		if (errorCode == WRONG_PASSWORD) {
			errorCodeStr = AuthenticationException.USER_WRONG_PASSWORD;
		} else if (errorCode == LOGON_FAILURE) {
			errorCodeStr = AuthenticationException.USER_LOGON_FAILURE;
		} else if (errorCode == NO_SUCH_USER) {
			errorCodeStr = AuthenticationException.USER_NO_SUCH_USER;
		} else if (errorCode == INVALID_LOGON_HOURS) {
			errorCodeStr = AuthenticationException.USER_INVALID_LOGON_HOURS;
		} else if (errorCode == INVALID_WORKSTATION) {
			errorCodeStr = AuthenticationException.USER_INVALID_WORKSTATION;
		} else if (errorCode == PASSWORD_EXPIRED) {
			errorCodeStr = AuthenticationException.USER_PASSWORD_EXPIRED;
		} else if (errorCode == ACCOUNT_DISABLED) {
			errorCodeStr = AuthenticationException.USER_ACCOUNT_DISABLED;
		} else if (errorCode == ACCOUNT_EXPIRED) {
			errorCodeStr = AuthenticationException.USER_ACCOUNT_EXPIRED;
		} else if (errorCode == PASSWORD_MUST_CHANGE) {
			errorCodeStr = AuthenticationException.USER_PASSWORD_MUST_CHANGE;
		} else if (errorCode == ACCOUNT_LOCKED_OUT) {
			errorCodeStr = AuthenticationException.USER_ACCOUNT_LOCKED_OUT;
        }
		return errorCodeStr;
	}
}
