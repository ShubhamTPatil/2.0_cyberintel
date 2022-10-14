// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.forms;

import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

import com.marimba.intf.util.IConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * REMIND
 *
 * @author Venkatesh Jeyaraman
 * @version $File$
 */
public class DesktopSecurityForm extends AbstractForm {

    final String FALSE = "false";
    String schemeDisplay = FALSE;
    String selectedProfile = null;

    List<String> allowedSoftwareList = null;
    List<String> blockedSoftwareList = null;

    public void initialize(IConfig prop) {

        setValue(DESKTOP_SECURITY_ENABLE, getValue(prop, DESKTOP_SECURITY_ENABLE, FALSE));
        setValue(DESKTOP_SECURITY_FILE_SHARING, getValue(prop, DESKTOP_SECURITY_FILE_SHARING, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));
        setValue(DESKTOP_SECURITY_CMD_PROMPT, getValue(prop, DESKTOP_SECURITY_CMD_PROMPT, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));

        setValue(DESKTOP_SECURITY_USER_READ_FLOPPY, getValue(prop, DESKTOP_SECURITY_USER_READ_FLOPPY, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));
        setValue(DESKTOP_SECURITY_USER_WRITE_FLOPPY, getValue(prop, DESKTOP_SECURITY_USER_WRITE_FLOPPY, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));
        setValue(DESKTOP_SECURITY_USER_READ_CDDVD, getValue(prop, DESKTOP_SECURITY_USER_READ_CDDVD, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));
        setValue(DESKTOP_SECURITY_USER_WRITE_CDDVD, getValue(prop, DESKTOP_SECURITY_USER_WRITE_CDDVD, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));
        setValue(DESKTOP_SECURITY_USER_READ_WPD, getValue(prop, DESKTOP_SECURITY_USER_READ_WPD, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));
        setValue(DESKTOP_SECURITY_USER_WRITE_WPD, getValue(prop, DESKTOP_SECURITY_USER_WRITE_WPD, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));

        setValue(DESKTOP_SECURITY_MACH_READ_FLOPPY, getValue(prop, DESKTOP_SECURITY_MACH_READ_FLOPPY, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));
        setValue(DESKTOP_SECURITY_MACH_WRITE_FLOPPY, getValue(prop, DESKTOP_SECURITY_MACH_WRITE_FLOPPY, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));
        setValue(DESKTOP_SECURITY_MACH_READ_CDDVD, getValue(prop, DESKTOP_SECURITY_MACH_READ_CDDVD, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));
        setValue(DESKTOP_SECURITY_MACH_WRITE_CDDVD, getValue(prop, DESKTOP_SECURITY_MACH_WRITE_CDDVD, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));
        setValue(DESKTOP_SECURITY_MACH_READ_WPD, getValue(prop, DESKTOP_SECURITY_MACH_READ_WPD, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));
        setValue(DESKTOP_SECURITY_MACH_WRITE_WPD, getValue(prop, DESKTOP_SECURITY_MACH_WRITE_WPD, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));

        setValue(DESKTOP_SECURITY_USER_INTERNET, getValue(prop, DESKTOP_SECURITY_USER_INTERNET, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));
        setValue(DESKTOP_SECURITY_MACH_INTERNET, getValue(prop, DESKTOP_SECURITY_MACH_INTERNET, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));

//        setValue(DESKTOP_SECURITY_MAX_PWD_AGE, getValue(prop, DESKTOP_SECURITY_MAX_PWD_AGE, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));
//        setValue(DESKTOP_SECURITY_MIN_PWD_AGE, getValue(prop, DESKTOP_SECURITY_MIN_PWD_AGE, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));
//        setValue(DESKTOP_SECURITY_MIN_PWD_STRENGTH, getValue(prop, DESKTOP_SECURITY_MIN_PWD_STRENGTH, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));
//        setValue(DESKTOP_SECURITY_FORCED_LOGOUT_TIME, getValue(prop, DESKTOP_SECURITY_FORCED_LOGOUT_TIME, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));
//        setValue(DESKTOP_SECURITY_ENFORCE_PWD_HISTORY, getValue(prop, DESKTOP_SECURITY_ENFORCE_PWD_HISTORY, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));
//        setValue(DESKTOP_SECURITY_ACC_LOCK_THRESHOLD, getValue(prop, DESKTOP_SECURITY_ACC_LOCK_THRESHOLD, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));
//        setValue(DESKTOP_SECURITY_RESET_ACC_LOCK_COUNTER, getValue(prop, DESKTOP_SECURITY_RESET_ACC_LOCK_COUNTER, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));
//        setValue(DESKTOP_SECURITY_ACC_LOCK_COUNTER, getValue(prop, DESKTOP_SECURITY_ACC_LOCK_COUNTER, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));

        setValue(DESKTOP_SECURITY_MAX_PWD_AGE, getValue(prop, DESKTOP_SECURITY_MAX_PWD_AGE, ""));
        setValue(DESKTOP_SECURITY_MIN_PWD_AGE, getValue(prop, DESKTOP_SECURITY_MIN_PWD_AGE, ""));
        setValue(DESKTOP_SECURITY_MIN_PWD_STRENGTH, getValue(prop, DESKTOP_SECURITY_MIN_PWD_STRENGTH, ""));
        setValue(DESKTOP_SECURITY_FORCED_LOGOUT_TIME, getValue(prop, DESKTOP_SECURITY_FORCED_LOGOUT_TIME, ""));
        setValue(DESKTOP_SECURITY_ENFORCE_PWD_HISTORY, getValue(prop, DESKTOP_SECURITY_ENFORCE_PWD_HISTORY, ""));
        setValue(DESKTOP_SECURITY_ACC_LOCK_THRESHOLD, getValue(prop, DESKTOP_SECURITY_ACC_LOCK_THRESHOLD, ""));
        setValue(DESKTOP_SECURITY_RESET_ACC_LOCK_COUNTER, getValue(prop, DESKTOP_SECURITY_RESET_ACC_LOCK_COUNTER, ""));
        setValue(DESKTOP_SECURITY_ACC_LOCK_COUNTER, getValue(prop, DESKTOP_SECURITY_ACC_LOCK_COUNTER, ""));

        setValue(DESKTOP_SECURITY_USER_ENABLE_SCREENSAVER, getValue(prop, DESKTOP_SECURITY_USER_ENABLE_SCREENSAVER, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));
        setValue(DESKTOP_SECURITY_USER_SECURE_SCREENSAVER, getValue(prop, DESKTOP_SECURITY_USER_SECURE_SCREENSAVER, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));
        setValue(DESKTOP_SECURITY_USER_SCREENSAVER_TIMEOUT, getValue(prop, DESKTOP_SECURITY_USER_SCREENSAVER_TIMEOUT, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));
        setValue(DESKTOP_SECURITY_USER_FORCE_SCREENSAVER, getValue(prop, DESKTOP_SECURITY_USER_FORCE_SCREENSAVER, DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE));

        setValue(DESKTOP_SECURITY_USER_SCREENSAVER_TIMEOUT_VALUE, getValue(prop, DESKTOP_SECURITY_USER_SCREENSAVER_TIMEOUT_VALUE, ""));
        setValue(DESKTOP_SECURITY_USER_FORCE_SCREENSAVER_VALUE, getValue(prop, DESKTOP_SECURITY_USER_FORCE_SCREENSAVER_VALUE, ""));

        setValue(DESKTOP_SECURITY_FORCE_APPLY, getValue(prop, DESKTOP_SECURITY_FORCE_APPLY, FALSE));
        setValue(DESKTOP_SECURITY_IMMEDIATE_UPDATE, getValue(prop, DESKTOP_SECURITY_IMMEDIATE_UPDATE, FALSE));

        setValue(DESKTOP_SECURITY_PRIORITY_VALUE, getValue(prop, DESKTOP_SECURITY_PRIORITY_VALUE, ""));
        setValue(DESKTOP_SECURITY_TEMPLATE_NAME, getValue(prop, DESKTOP_SECURITY_TEMPLATE_NAME, ""));

        setValue(DESKTOP_SECURITY_SOFTWARES_ALLOWED, getValue(prop, DESKTOP_SECURITY_SOFTWARES_ALLOWED, ""));
        setValue(DESKTOP_SECURITY_SOFTWARES_BLOCKED, getValue(prop, DESKTOP_SECURITY_SOFTWARES_BLOCKED, ""));

        allowedSoftwareList = getList(getValue(prop, DESKTOP_SECURITY_SOFTWARES_ALLOWED, ""));
        blockedSoftwareList = getList(getValue(prop, DESKTOP_SECURITY_SOFTWARES_BLOCKED, ""));

    }

    private List<String> getList(String softwares) {
        List<String> softwareList = new ArrayList<String>();

        if (softwares != null && softwares.trim().length() != 0) {
            return Arrays.asList(softwares.split(";"));
        }

        return softwareList;
    }

    private String getValue(IConfig prop, String propName, String defValue) {
        String value = prop.getProperty(propName);
        return (value != null) ? value:defValue;
    }

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        setValue(DESKTOP_SECURITY_ENABLE, FALSE);
        setValue(DESKTOP_SECURITY_FILE_SHARING, "");
        setValue(DESKTOP_SECURITY_CMD_PROMPT, "");
        setValue(DESKTOP_SECURITY_USER_READ_FLOPPY, "");
        setValue(DESKTOP_SECURITY_USER_WRITE_FLOPPY, "");
        setValue(DESKTOP_SECURITY_USER_READ_CDDVD, "");
        setValue(DESKTOP_SECURITY_USER_WRITE_CDDVD, "");
        setValue(DESKTOP_SECURITY_USER_READ_WPD, "");
        setValue(DESKTOP_SECURITY_USER_WRITE_WPD, "");
        setValue(DESKTOP_SECURITY_USER_INTERNET, "");

        setValue(DESKTOP_SECURITY_MACH_READ_FLOPPY, "");
        setValue(DESKTOP_SECURITY_MACH_WRITE_FLOPPY, "");
        setValue(DESKTOP_SECURITY_MACH_READ_CDDVD, "");
        setValue(DESKTOP_SECURITY_MACH_WRITE_CDDVD, "");
        setValue(DESKTOP_SECURITY_MACH_READ_WPD, "");
        setValue(DESKTOP_SECURITY_MACH_WRITE_WPD, "");
        setValue(DESKTOP_SECURITY_MACH_INTERNET, "");

//        setValue(DESKTOP_SECURITY_MIN_PWD_STRENGTH, "");
//        setValue(DESKTOP_SECURITY_MAX_PWD_AGE, "");
//        setValue(DESKTOP_SECURITY_MIN_PWD_AGE, "");
//        setValue(DESKTOP_SECURITY_FORCED_LOGOUT_TIME, "");
//        setValue(DESKTOP_SECURITY_ENFORCE_PWD_HISTORY, "");
//        setValue(DESKTOP_SECURITY_ACC_LOCK_THRESHOLD, "");
//        setValue(DESKTOP_SECURITY_RESET_ACC_LOCK_COUNTER, "");
//        setValue(DESKTOP_SECURITY_ACC_LOCK_COUNTER, "");

        setValue(DESKTOP_SECURITY_USER_ENABLE_SCREENSAVER, "");
        setValue(DESKTOP_SECURITY_USER_SECURE_SCREENSAVER, "");
        setValue(DESKTOP_SECURITY_USER_SCREENSAVER_TIMEOUT, "");
        setValue(DESKTOP_SECURITY_USER_FORCE_SCREENSAVER, "");

        setValue(DESKTOP_SECURITY_TEMPLATE_NAME, "Select");

        setValue(DESKTOP_SECURITY_FORCE_APPLY, FALSE);
        setValue(DESKTOP_SECURITY_IMMEDIATE_UPDATE, FALSE);
        setValue(DESKTOP_SECURITY_PRIORITY_VALUE, "");

        setValue(DESKTOP_SECURITY_SOFTWARES_ALLOWED, "");
        setValue(DESKTOP_SECURITY_SOFTWARES_BLOCKED, "");
        selectedProfile = null;
    }

    public String getschemeDisplay() {
        return schemeDisplay;
    }

    public void setschemeDisplay(String schemeDisplay) {
        this.schemeDisplay = schemeDisplay;
    }

    public void setSelectedProfile(String selectedProfile) {
        this.selectedProfile = selectedProfile;
    }

    public String getSelectedProfile() {
        return selectedProfile;
    }


    public List<String> getAllowedSoftwareList() {
        return allowedSoftwareList;
    }

    public void setAllowedSoftwareList(List<String> allowedSoftwareList) {
        this.allowedSoftwareList = allowedSoftwareList;
    }

    public List<String> getBlockedSoftwareList() {
        return blockedSoftwareList;
    }

    public void setBlockedSoftwareList(List<String> blockedSoftwareList) {
        this.blockedSoftwareList = blockedSoftwareList;
    }
}
