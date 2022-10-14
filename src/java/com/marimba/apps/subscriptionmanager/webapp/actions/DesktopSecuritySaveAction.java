// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscriptionmanager.webapp.forms.AbstractForm;
import com.marimba.apps.subscriptionmanager.webapp.forms.DesktopSecurityForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.intf.GUIException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.marimba.tools.config.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import java.util.Vector;
import java.io.File;

/**
 * DesktopSecuritySaveAction interprets the security form elements.
 *
 * @author Venkatesh Jeyaraman
 */
public final class DesktopSecuritySaveAction extends AbstractAction {

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws ServletException {
        if (DEBUG) {
            System.out.println("DesktopSecuritySaveAction called");
        }

        saveState(request, (DesktopSecurityForm) form);

        // and forward to the next action
        String forward = (String)((AbstractForm) form).getValue("forward");
        return (new ActionForward(forward, true));
    }

    /**
     * Save the changes in the form to the DistributionBean.
     */
    private void saveState(HttpServletRequest request, DesktopSecurityForm form) throws GUIException {
        // Security Option is the value of the radio box used to reset security setting values
        String securityOption = (String) form.getValue(DESKTOP_SECURITY_ENABLE);
        String priority = (String) form.getValue(DESKTOP_SECURITY_PRIORITY_VALUE);
        priority = priority.trim();

        String cmdPrompt = null;
        String fileSharing = null;

        String userReadFloppy = null;
        String userWriteFloppy = null;
        String userReadCD = null;
        String userWriteCD = null;
        String userReadWPD = null;
        String userWriteWPD = null;
        String machReadFloppy = null;
        String machWriteFloppy = null;
        String machReadCD = null;
        String machWriteCD = null;
        String machReadWPD = null;
        String machWriteWPD = null;

        String userInternet = null;
        String machInternet = null;

        String minPwdStrength = null;
        String maxPwdAge = null;
        String minPwdAge = null;
        String forceLogoutTime = null;
        String enforcePwdHistory = null;
        String accLockThreshold = null;
        String resetAccLockCounter = null;
        String accLockCounter = null;

        String userEnableScreen = null;
        String userSecureScreen = null;
        String userScreenTimout = null;
        String userForceSpecificScreen = null;

        String forceApply = null;
        String immediateUpdate = null;
        String profileSelected = null;

        String allowedSoftware = null;
        String blockedSoftware = null;

        Vector<String> securityProps = new Vector<String>();
        
        ServletContext context = request.getSession().getServletContext();
        init(request);

        if(NOTAVBLE.equals(priority) || "".equals(priority)) {
            priority = "99999";
        }
        
        if (DEBUG3) {
            System.out.println("Security Option = " + securityOption);
            System.out.println("priority_value = " + priority);
        }

        if ("true".equals(securityOption)) {

            profileSelected = (String) form.getValue(DESKTOP_SECURITY_TEMPLATE_NAME);
            if ("Select".equals(profileSelected)) {
                try {
                    ISubscription sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB_COPY);
                    profileSelected  = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_TEMPLATE_NAME);

                    allowedSoftware = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_SOFTWARES_ALLOWED);
                    blockedSoftware = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_SOFTWARES_BLOCKED);

                    cmdPrompt = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_CMD_PROMPT);
                    fileSharing = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_FILE_SHARING);

                    userReadFloppy = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_READ_FLOPPY);
                    userWriteFloppy = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_WRITE_FLOPPY);
                    userReadCD = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_READ_CDDVD);
                    userWriteCD = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_WRITE_CDDVD);
                    userReadWPD = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_READ_WPD);
                    userWriteWPD = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_WRITE_WPD);
                    machReadFloppy = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_READ_FLOPPY);
                    machWriteFloppy = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_WRITE_FLOPPY);
                    machReadCD = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_READ_CDDVD);
                    machWriteCD = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_WRITE_CDDVD);
                    machReadWPD = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_READ_WPD);
                    machWriteWPD = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_WRITE_WPD);

                    userInternet = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_INTERNET);
                    machInternet = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_INTERNET);

                    minPwdStrength = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MIN_PWD_STRENGTH);
                    maxPwdAge = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MAX_PWD_AGE);
                    minPwdAge = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MIN_PWD_AGE);
                    forceLogoutTime = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_FORCED_LOGOUT_TIME);
                    enforcePwdHistory = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_ENFORCE_PWD_HISTORY);
                    accLockThreshold = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_ACC_LOCK_THRESHOLD);
                    resetAccLockCounter = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_RESET_ACC_LOCK_COUNTER);
                    accLockCounter = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_ACC_LOCK_COUNTER);

                    userEnableScreen = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_ENABLE_SCREENSAVER);
                    userSecureScreen = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_SECURE_SCREENSAVER);
                    userScreenTimout = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_SCREENSAVER_TIMEOUT);
                    userForceSpecificScreen = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_FORCE_SCREENSAVER);

                    forceApply = "true".equals(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_FORCE_APPLY)) ? "on" : "false";
                    immediateUpdate = "true".equals(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_IMMEDIATE_UPDATE)) ? "on" : "false";

                } catch (SystemException e) {
                    throw new GUIException(e);
                }
            } else {
                ConfigProps profileProps = new ConfigProps(new File(main.getDataDirectory(), "desktop-security-settings.txt"));

                allowedSoftware = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_SOFTWARES_ALLOWED);
                blockedSoftware = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_SOFTWARES_BLOCKED);

                cmdPrompt = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_CMD_PROMPT);
                fileSharing = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_FILE_SHARING);

                userReadFloppy = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_USER_READ_FLOPPY);
                userWriteFloppy = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_USER_WRITE_FLOPPY);
                userReadCD = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_USER_READ_CDDVD);
                userWriteCD = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_USER_WRITE_CDDVD);
                userReadWPD = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_USER_READ_WPD);
                userWriteWPD = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_USER_WRITE_WPD);
                machReadFloppy = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_MACH_READ_FLOPPY);
                machWriteFloppy = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_MACH_WRITE_FLOPPY);
                machReadCD = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_MACH_READ_CDDVD);
                machWriteCD = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_MACH_WRITE_CDDVD);
                machReadWPD = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_MACH_READ_WPD);
                machWriteWPD = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_MACH_WRITE_WPD);

                userInternet = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_USER_INTERNET);
                machInternet = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_MACH_INTERNET);

                minPwdStrength = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_MIN_PWD_STRENGTH);
                maxPwdAge= profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_MAX_PWD_AGE);
                minPwdAge = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_MIN_PWD_AGE);
                forceLogoutTime = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_FORCED_LOGOUT_TIME);
                enforcePwdHistory = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_ENFORCE_PWD_HISTORY);
                accLockThreshold = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_ACC_LOCK_THRESHOLD);
                resetAccLockCounter = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_RESET_ACC_LOCK_COUNTER);
                accLockCounter = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_ACC_LOCK_COUNTER);

                userEnableScreen = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_USER_FORCE_SCREENSAVER);
                userSecureScreen = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_USER_SECURE_SCREENSAVER);
                userScreenTimout = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_USER_SCREENSAVER_TIMEOUT);
                userForceSpecificScreen = profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_USER_FORCE_SCREENSAVER);

                forceApply = "true".equals(profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_FORCE_APPLY)) ? "on" : "false";
                immediateUpdate = "true".equals(profileProps.getProperty(profileSelected + UCD_DOT + DESKTOP_SECURITY_IMMEDIATE_UPDATE)) ? "on" : "false";
            }
        } 

        try {
            ISubscription sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);
            DistributionBean distBean = getDistributionBean(request);

            if("exclude".equalsIgnoreCase(securityOption)) {
            	securityOption = null;
            	setValue(sub, securityProps, DESKTOP_SECURITY_EXCLUDE, "true", priority);
            } else if ("false".equalsIgnoreCase(securityOption)) {
            	securityOption = null;
            	setValue(sub, securityProps, DESKTOP_SECURITY_EXCLUDE, null, priority);
            } else if("true".equalsIgnoreCase(securityOption)) {
            	setValue(sub, securityProps, DESKTOP_SECURITY_EXCLUDE, null, priority);
            }

            setValue(sub, securityProps, DESKTOP_SECURITY_ENABLE, securityOption, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_TEMPLATE_NAME, profileSelected, priority);

            setValue(sub, securityProps, DESKTOP_SECURITY_CMD_PROMPT, cmdPrompt, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_FILE_SHARING, fileSharing, priority);

            setValue(sub, securityProps, DESKTOP_SECURITY_USER_READ_FLOPPY, userReadFloppy, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_USER_WRITE_FLOPPY, userWriteFloppy, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_USER_READ_CDDVD, userReadCD, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_USER_WRITE_CDDVD, userWriteCD, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_USER_READ_WPD, userReadWPD, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_USER_WRITE_WPD, userWriteWPD, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_MACH_READ_FLOPPY, machReadFloppy, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_MACH_WRITE_FLOPPY, machWriteFloppy, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_MACH_READ_CDDVD, machReadCD, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_MACH_WRITE_CDDVD, machWriteCD, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_MACH_READ_WPD, machReadWPD, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_MACH_WRITE_WPD, machWriteWPD, priority);

            setValue(sub, securityProps, DESKTOP_SECURITY_USER_INTERNET, userInternet, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_MACH_INTERNET, machInternet, priority);

            setValue(sub, securityProps, DESKTOP_SECURITY_MIN_PWD_STRENGTH, minPwdStrength, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_MAX_PWD_AGE, maxPwdAge, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_MIN_PWD_AGE, minPwdAge, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_FORCED_LOGOUT_TIME, forceLogoutTime, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_ENFORCE_PWD_HISTORY, enforcePwdHistory, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_ACC_LOCK_THRESHOLD, accLockThreshold, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_RESET_ACC_LOCK_COUNTER, resetAccLockCounter, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_ACC_LOCK_COUNTER, accLockCounter, priority);

            setValue(sub, securityProps, DESKTOP_SECURITY_USER_ENABLE_SCREENSAVER, userEnableScreen, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_USER_SECURE_SCREENSAVER, userSecureScreen, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_USER_SCREENSAVER_TIMEOUT, userScreenTimout, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_USER_FORCE_SCREENSAVER, userForceSpecificScreen, priority);

            setValue(sub, securityProps, DESKTOP_SECURITY_SOFTWARES_ALLOWED, allowedSoftware, priority);
            setValue(sub, securityProps, DESKTOP_SECURITY_SOFTWARES_BLOCKED, blockedSoftware, priority);

            if ("on".equals(forceApply)) {
                setValue(sub, securityProps, DESKTOP_SECURITY_FORCE_APPLY, "true", priority);
            } else {
                if (securityOption != null) {
                    setValue(sub, securityProps, DESKTOP_SECURITY_FORCE_APPLY, "false", priority);
                } else {
                    setValue(sub, securityProps, DESKTOP_SECURITY_FORCE_APPLY, null, priority);
                }
            }

            if ("on".equals(immediateUpdate)) {
                setValue(sub, securityProps, DESKTOP_SECURITY_IMMEDIATE_UPDATE, "true", priority);
            } else {
                if (securityOption != null) {
                    setValue(sub, securityProps, DESKTOP_SECURITY_IMMEDIATE_UPDATE, "false", priority);
                } else {
                    setValue(sub, securityProps, DESKTOP_SECURITY_IMMEDIATE_UPDATE, null, priority);
                }
            }

            distBean.setSecurityProps(securityProps);

       } catch (SystemException e) {
           throw new GUIException(e);
        }
    }

    private void setValue(ISubscription sub, Vector securityProps, String key, String value, String priority) throws SystemException {
        if (value != null) {
            if(priority != null && !("".equals(priority)))  {
                value = value + PROP_DELIM + Integer.parseInt(priority);
            }
        }
        sub.setProperty(PROP_SECURITY_KEYWORD, key, value);
        securityProps.add(PROP_SECURITY_KEYWORD + PROP_DELIM + key + "=" + value);
    }
}
