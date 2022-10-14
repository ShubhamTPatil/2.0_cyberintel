// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.forms;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

import com.marimba.webapps.tools.util.KnownActionError;
import com.marimba.tools.config.*;
import com.marimba.apps.subscriptionmanager.webapp.system.SoftwareDetailsBean;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
/**
 * DesktopSecurityProfileForm
 *
 * @author Selvaraj Jegatheesan
 * @version $Revision$, $Date$
 */
public class DesktopSecurityProfileForm extends AbstractForm {

	private File theFile;
    private String action;
    private String create = "false";
    private String name;
    private String descr;

    private String selected = "false";
    private String showAppTab = "user";

    private Map<String, String> affectedTargets = new Hashtable<String, String>();
    private boolean targetsAffected = false;
    private String status = "";
    private String statusDesc = "";
    private FormFile file;

    private Set<String> appSet = null;

    private Map<String, SoftwareDetailsBean> managedApps = new HashMap<String, SoftwareDetailsBean>();

    private String blockedAsStr = "";
    private String allowedAsStr = "";

    private String forceApplyEnabled;
    private String immediateUpdateEnabled;
    private String selectedApp;

    private String cmdPrompt = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
    private String fileSharing = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;

    private String userReadFloppy = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
    private String userWriteFloppy = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
    private String userReadCDDVD = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
    private String userWriteCDDVD = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
    private String userReadWPD = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
    private String userWriteWPD = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
    private String machReadFloppy = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
    private String machWriteFloppy = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
    private String machReadCDDVD = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
    private String machWriteCDDVD = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
    private String machReadWPD = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
    private String machWriteWPD = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;

    private String userInternet = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
    private String machInternet = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;

    private String userEnableScreen = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
    private String userSecureScreen = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
    private String userScreenTimeout = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
    private String userForceSpecificScreen = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;

//    private String minPwdStrength = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
//    private String maxPwdAge = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
//    private String minPwdAge = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
//    private String forcedLogoutTime = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
//    private String enforcePwdHistory = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
//    private String accountLockoutThreshold = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
//    private String resetAccountLockoutCounter = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
//    private String accountLockoutCounter = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;

    private int userScreenTimeoutVal = 300;
    private String userForceSpecificScreenVal = "";

    private int minPwdStrengthVal = 8;
    private int maxPwdAgeVal = 90;
    private int minPwdAgeVal = 30;
    private int forcedLogoutTimeVal = 3600;
    private int enforcePwdHistoryVal = 5;
    private int accountLockoutThresholdVal = 3;
    private int resetAccountLockoutCounterVal = 60;
    private int accountLockoutCounterVal = 180;

    public void initialize() {
        super.initialize();
        setStatus("");
        setStatusDesc("");
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return this.action;
    }

	public String getBlockedAsStr() {
		return blockedAsStr;
	}

	public void setBlockedAsStr(String blockedAsStr) {
		this.blockedAsStr = blockedAsStr;
	}

	public String getAllowedAsStr() {
		return allowedAsStr;
	}

	public void setAllowedAsStr(String allowedAsStr) {
		this.allowedAsStr = allowedAsStr;
	}

    public void setAppSet(Set<String> appSet) {
        this.appSet = appSet;
    }

    public Set<String> getAppSet() {
        return appSet;
    }

    public String getCmdPrompt() {
        return cmdPrompt;
    }

    public void setCmdPrompt(String cmdPrompt) {
        this.cmdPrompt = cmdPrompt;
    }

    public String getFileSharing() {
        return fileSharing;
    }

    public void setFileSharing(String fileSharing) {
        this.fileSharing = fileSharing;
    }

    public String getUserReadFloppy() {
        return userReadFloppy;
    }

    public void setUserReadFloppy(String userReadFloppy) {
        this.userReadFloppy = userReadFloppy;
    }

    public String getUserWriteFloppy() {
        return userWriteFloppy;
    }

    public void setUserWriteFloppy(String userWriteFloppy) {
        this.userWriteFloppy = userWriteFloppy;
    }

    public String getUserReadCDDVD() {
        return userReadCDDVD;
    }

    public void setUserReadCDDVD(String userReadCDDVD) {
        this.userReadCDDVD = userReadCDDVD;
    }

    public String getUserWriteCDDVD() {
        return userWriteCDDVD;
    }

    public void setUserWriteCDDVD(String userWriteCDDVD) {
        this.userWriteCDDVD = userWriteCDDVD;
    }

    public String getUserReadWPD() {
        return userReadWPD;
    }

    public void setUserReadWPD(String userReadWPD) {
        this.userReadWPD = userReadWPD;
    }

    public String getUserWriteWPD() {
        return userWriteWPD;
    }

    public void setUserWriteWPD(String userWriteWPD) {
        this.userWriteWPD = userWriteWPD;
    }

    public String getUserInternet() {
        return userInternet;
    }

    public void setUserInternet(String userInternet) {
        this.userInternet = userInternet;
    }

    public String getUserSecureScreen() {
        return userSecureScreen;
    }

    public void setUserSecureScreen(String userSecureScreen) {
        this.userSecureScreen = userSecureScreen;
    }

    public String getUserEnableScreen() {
        return userEnableScreen;
    }

    public void setUserEnableScreen(String userEnableScreen) {
        this.userEnableScreen = userEnableScreen;
    }

    public String getUserScreenTimeout() {
        return userScreenTimeout;
    }

    public void setUserScreenTimeout(String userScreenTimeout) {
        this.userScreenTimeout = userScreenTimeout;
    }

    public String getUserForceSpecificScreen() {
        return userForceSpecificScreen;
    }

    public void setUserForceSpecificScreen(String userForceSpecificScreen) {
        this.userForceSpecificScreen = userForceSpecificScreen;
    }

//    public String getMinPwdStrength() {
//        return minPwdStrength;
//    }
//
//    public void setMinPwdStrength(String minPwdStrength) {
//        this.minPwdStrength = minPwdStrength;
//    }
//
//    public String getMaxPwdAge() {
//        return maxPwdAge;
//    }
//
//    public void setMaxPwdAge(String maxPwdAge) {
//        this.maxPwdAge = maxPwdAge;
//    }
//
//    public String getMinPwdAge() {
//        return minPwdAge;
//    }
//
//    public void setMinPwdAge(String minPwdAge) {
//        this.minPwdAge = minPwdAge;
//    }
//
//    public String getForcedLogoutTime() {
//        return forcedLogoutTime;
//    }
//
//    public void setForcedLogoutTime(String forcedLogoutTime) {
//        this.forcedLogoutTime = forcedLogoutTime;
//    }
//
//    public String getAccountLockoutThreshold() {
//        return accountLockoutThreshold;
//    }
//
//    public void setAccountLockoutThreshold(String accountLockoutThreshold) {
//        this.accountLockoutThreshold = accountLockoutThreshold;
//    }
//
//    public String getEnforcePwdHistory() {
//        return enforcePwdHistory;
//    }
//
//    public void setEnforcePwdHistory(String enforcePwdHistory) {
//        this.enforcePwdHistory = enforcePwdHistory;
//    }
//
//    public String getResetAccountLockoutCounter() {
//        return resetAccountLockoutCounter;
//    }
//
//    public void setResetAccountLockoutCounter(String resetAccountLockoutCounter) {
//        this.resetAccountLockoutCounter = resetAccountLockoutCounter;
//    }
//
//    public String getAccountLockoutCounter() {
//        return accountLockoutCounter;
//    }
//
//    public void setAccountLockoutCounter(String accountLockoutCounter) {
//        this.accountLockoutCounter = accountLockoutCounter;
//    }

	public void reset(ActionMapping mapping, HttpServletRequest request) {
        System.out.println("ActionDebug: Reset method called(mapping, request)");
        setStatus("");
        setStatusDesc("");
    }

    public void reset() {
        setName("");
        setDescription("");

        cmdPrompt = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
        fileSharing = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;

        userReadFloppy = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
        userWriteFloppy = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
        userReadCDDVD = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
        userWriteCDDVD = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
        userReadWPD = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
        userWriteWPD = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
        machReadFloppy = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
        machWriteFloppy = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
        machReadCDDVD = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
        machWriteCDDVD = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
        machReadWPD = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
        machWriteWPD = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;

        userInternet = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
        machInternet = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;

        userSecureScreen = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
        userEnableScreen = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
        userScreenTimeout = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
        userForceSpecificScreen = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;

//        minPwdStrength = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
//        maxPwdAge = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
//        minPwdAge = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
//        forcedLogoutTime = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
//        enforcePwdHistory = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
//        accountLockoutThreshold = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
//        resetAccountLockoutCounter = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;
//        accountLockoutCounter = DESKTOP_SECURITY_RESTRICTION_DEFAULT_VALUE;

        userScreenTimeoutVal = 300;
        userForceSpecificScreenVal = "";

        minPwdStrengthVal = 8;
        maxPwdAgeVal = 90;
        minPwdAgeVal = 30;
        forcedLogoutTimeVal = 3600;
        enforcePwdHistoryVal = 5;
        accountLockoutThresholdVal = 3;
        resetAccountLockoutCounterVal = 60;
        accountLockoutCounterVal = 180;

        blockedAsStr = "";
        allowedAsStr = "";

        managedApps.clear();

        targetsAffected = false;

        setValue(DESKTOP_SECURITY_FORCE_APPLY, "false");
        setValue(DESKTOP_SECURITY_IMMEDIATE_UPDATE, "false");
    }

    public FormFile getFile() {
        return file;
    }

    public void setFile(FormFile file) {
        this.file = file;
    }

    public void loadProfile(Hashtable props) {
        System.out.println("DesktopSecurityProfileForm:: Loading profile: " + props.get(DESKTOP_SECURITY_PROFILE_NAME));
        reset();
        System.out.println("DesktopSecurityProfileForm:: Loading profile: after reset() method...");

        setName((String) props.remove(DESKTOP_SECURITY_PROFILE_NAME));
        String desc = (String) props.remove(DEKTOP_SECURITY_PROFILE_DESC);
        if (desc != null) {
            setDescription(desc);
        }

        try {
            cmdPrompt = (String) props.get(DESKTOP_SECURITY_CMD_PROMPT);
            fileSharing = (String) props.get(DESKTOP_SECURITY_FILE_SHARING);

            userReadFloppy = (String) props.get(DESKTOP_SECURITY_USER_READ_FLOPPY);
            userWriteFloppy = (String) props.get(DESKTOP_SECURITY_USER_WRITE_FLOPPY);
            userReadCDDVD = (String) props.get(DESKTOP_SECURITY_USER_READ_CDDVD);
            userWriteCDDVD = (String) props.get(DESKTOP_SECURITY_USER_WRITE_CDDVD);
            userReadWPD = (String) props.get(DESKTOP_SECURITY_USER_READ_WPD);
            userWriteWPD = (String) props.get(DESKTOP_SECURITY_USER_WRITE_WPD);

            machReadFloppy = (String) props.get(DESKTOP_SECURITY_MACH_READ_FLOPPY);
            machWriteFloppy = (String) props.get(DESKTOP_SECURITY_MACH_WRITE_FLOPPY);
            machReadCDDVD = (String) props.get(DESKTOP_SECURITY_MACH_READ_CDDVD);
            machWriteCDDVD = (String) props.get(DESKTOP_SECURITY_MACH_WRITE_CDDVD);
            machReadWPD = (String) props.get(DESKTOP_SECURITY_MACH_READ_WPD);
            machWriteWPD = (String) props.get(DESKTOP_SECURITY_MACH_WRITE_WPD);

            userInternet = (String) props.get(DESKTOP_SECURITY_USER_INTERNET);
            machInternet = (String) props.get(DESKTOP_SECURITY_MACH_INTERNET);

            userEnableScreen = (String) props.get(DESKTOP_SECURITY_USER_ENABLE_SCREENSAVER);
            userSecureScreen = (String) props.get(DESKTOP_SECURITY_USER_SECURE_SCREENSAVER);
            userScreenTimeout = getOption((String) props.get(DESKTOP_SECURITY_USER_SCREENSAVER_TIMEOUT));
            userScreenTimeoutVal = Integer.parseInt(getInputValue((String) props.get(DESKTOP_SECURITY_USER_SCREENSAVER_TIMEOUT)));
            userForceSpecificScreen = getOption((String) props.get(DESKTOP_SECURITY_USER_FORCE_SCREENSAVER));
            userForceSpecificScreenVal = getInputValue((String) props.get(DESKTOP_SECURITY_USER_FORCE_SCREENSAVER));

//            minPwdStrength = getOption((String) props.get(DESKTOP_SECURITY_MIN_PWD_STRENGTH));
            minPwdStrengthVal = Integer.parseInt((String) props.get(DESKTOP_SECURITY_MIN_PWD_STRENGTH));
//            maxPwdAge = getOption((String) props.get(DESKTOP_SECURITY_MAX_PWD_AGE));
            maxPwdAgeVal = Integer.parseInt((String) props.get(DESKTOP_SECURITY_MAX_PWD_AGE));
//            minPwdAge = getOption((String) props.get(DESKTOP_SECURITY_MIN_PWD_AGE));
            minPwdAgeVal = Integer.parseInt((String) props.get(DESKTOP_SECURITY_MIN_PWD_AGE));
//            forcedLogoutTime = getOption((String) props.get(DESKTOP_SECURITY_FORCED_LOGOUT_TIME));
            forcedLogoutTimeVal = Integer.parseInt((String) props.get(DESKTOP_SECURITY_FORCED_LOGOUT_TIME));
//            enforcePwdHistory = getOption((String) props.get(DESKTOP_SECURITY_ENFORCE_PWD_HISTORY));
            enforcePwdHistoryVal = Integer.parseInt((String) props.get(DESKTOP_SECURITY_ENFORCE_PWD_HISTORY));
//            accountLockoutThreshold = getOption((String) props.get(DESKTOP_SECURITY_ACC_LOCK_THRESHOLD));
            accountLockoutThresholdVal = Integer.parseInt((String) props.get(DESKTOP_SECURITY_ACC_LOCK_THRESHOLD));
//            resetAccountLockoutCounter = getOption((String) props.get(DESKTOP_SECURITY_RESET_ACC_LOCK_COUNTER));
            resetAccountLockoutCounterVal = Integer.parseInt((String) props.get(DESKTOP_SECURITY_RESET_ACC_LOCK_COUNTER));
//            accountLockoutCounter = getOption((String) props.get(DESKTOP_SECURITY_ACC_LOCK_COUNTER));
            accountLockoutCounterVal = Integer.parseInt((String) props.get(DESKTOP_SECURITY_ACC_LOCK_COUNTER));

            setValue(DESKTOP_SECURITY_FORCE_APPLY, props.get(DESKTOP_SECURITY_FORCE_APPLY));
            setValue(DESKTOP_SECURITY_IMMEDIATE_UPDATE, props.get(DESKTOP_SECURITY_IMMEDIATE_UPDATE));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String value = (String) props.get(DESKTOP_SECURITY_SOFTWARES_BLOCKED);
        formSoftwareBean(value == null ? "" : value, "blocked");

        value = (String) props.get(DESKTOP_SECURITY_SOFTWARES_ALLOWED);
        formSoftwareBean(value == null ? "" : value, "allowed");

        System.out.println("DesktopSecurityProfileForm:: Loading profile: method leaving...");
    }

    private String getOption(String value) {
        int idx = value.indexOf(",");

        if (idx != -1) {
            return value.substring(0, idx);
        }

        return value;
    }

    private String getInputValue(String value) {
        int idx = value.indexOf(",");

        if (idx != -1) {
            return value.substring(idx+1);
        }

        return "";
    }

    public void formSoftwareBean(String value, String type) {
    	for(String anApp : value.split(";")) {
            if (anApp != null && anApp.trim().length() != 0) {
                SoftwareDetailsBean bean = new SoftwareDetailsBean();
                bean.setName(anApp);
                bean.setType(type);
                managedApps.put(anApp, bean);
            }
    	}
    }

    private String getValue(ConfigProps prop, String propName, String defValue) {
        String value = prop.getProperty(propName);
        return (value != null) ? value:defValue;
    }

    public File getTheFile() {
        return theFile;
    }

    public void setTheFile(File theFile) {
        this.theFile = theFile;
    }

    public String getCreate() {
	return create;
    }

    public void setCreate(String create) {
	this.create = create;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getDescription() {
	return descr;
    }

    public void setDescription(String description) {
	this.descr = description;
    }

    public String getSelected() {
	return selected;
    }

    public void setSelected(String selected) {
	this.selected = selected;
    }

	public String getForceApplyEnabled() {
		return forceApplyEnabled;
	}

	public void setForceApplyEnabled(String forceApplyEnabled) {
		this.forceApplyEnabled = forceApplyEnabled;
	}

	public String getImmediateUpdateEnabled() {
		return immediateUpdateEnabled;
	}

	public void setImmediateUpdateEnabled(String immediateUpdateEnabled) {
		this.immediateUpdateEnabled = immediateUpdateEnabled;
	}

    public String getSelectedApp() {
        return selectedApp;
    }

    public void setSelectedApp(String selectedApp) {
        this.selectedApp = selectedApp;
    }

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if (("save".equals(request.getParameter("action")))) {
            if (((name == null) || (name.trim().length() < 1))) {
                errors.add("Profile Name", new KnownActionError(VALIDATION_POWERPROFILE_EMPTY));
            } else {
                // Remove multiple space in between two words.
                name = name.replaceAll("\\s+", " ");
                
                if (!Pattern.matches("[\\w\\s+]+", name)) {
                    errors.add("Profile Name", new KnownActionError(VALIDATION_POWERPROFILE_SPLCHAR));
                }
            }
        }

        return errors;
    }

    public void setAffectedTargets(Map<String, String> targets) {
        affectedTargets = targets;

        if (affectedTargets != null && affectedTargets.size() > 0) {
            targetsAffected = true;
        } else {
            targetsAffected = false;
        }
    }

    public Set getAffectedTargetsSet() {
        return affectedTargets.keySet();
    }

    public Map<String, String> getAffectedTargetsHash() {
        return affectedTargets;
    }

    public boolean getTargetsAffected() {
        return targetsAffected;
    }

    public void setTargetsAffected(boolean targetsAffected) {
        this.targetsAffected = targetsAffected;
    }

    public String getUserForceSpecificScreenVal() {
        return userForceSpecificScreenVal;
    }

    public void setUserForceSpecificScreenVal(String userForceSpecificScreenVal) {
        this.userForceSpecificScreenVal = userForceSpecificScreenVal;
    }

    public int getMinPwdStrengthVal() {
        return minPwdStrengthVal;
    }

    public void setMinPwdStrengthVal(int minPwdStrengthVal) {
        this.minPwdStrengthVal = minPwdStrengthVal;
    }

    public int getMaxPwdAgeVal() {
        return maxPwdAgeVal;
    }

    public void setMaxPwdAgeVal(int maxPwdAgeVal) {
        this.maxPwdAgeVal = maxPwdAgeVal;
    }

    public int getMinPwdAgeVal() {
        return minPwdAgeVal;
    }

    public void setMinPwdAgeVal(int minPwdAgeVal) {
        this.minPwdAgeVal = minPwdAgeVal;
    }

    public int getForcedLogoutTimeVal() {
        return forcedLogoutTimeVal;
    }

    public void setForcedLogoutTimeVal(int forcedLogoutTimeVal) {
        this.forcedLogoutTimeVal = forcedLogoutTimeVal;
    }

    public int getEnforcePwdHistoryVal() {
        return enforcePwdHistoryVal;
    }

    public void setEnforcePwdHistoryVal(int enforcePwdHistoryVal) {
        this.enforcePwdHistoryVal = enforcePwdHistoryVal;
    }

    public int getAccountLockoutThresholdVal() {
        return accountLockoutThresholdVal;
    }

    public void setAccountLockoutThresholdVal(int accountLockoutThresholdVal) {
        this.accountLockoutThresholdVal = accountLockoutThresholdVal;
    }

    public int getResetAccountLockoutCounterVal() {
        return resetAccountLockoutCounterVal;
    }

    public void setResetAccountLockoutCounterVal(int resetAccountLockoutCounterVal) {
        this.resetAccountLockoutCounterVal = resetAccountLockoutCounterVal;
    }

    public int getAccountLockoutCounterVal() {
        return accountLockoutCounterVal;
    }

    public void setAccountLockoutCounterVal(int accountLockoutCounterVal) {
        this.accountLockoutCounterVal = accountLockoutCounterVal;
    }

    public int getUserScreenTimeoutVal() {
        return userScreenTimeoutVal;
    }

    public void setUserScreenTimeoutVal(int userScreenTimeoutVal) {
        this.userScreenTimeoutVal = userScreenTimeoutVal;
    }

    public String getMachReadFloppy() {
        return machReadFloppy;
    }

    public void setMachReadFloppy(String machReadFloppy) {
        this.machReadFloppy = machReadFloppy;
    }

    public String getMachWriteFloppy() {
        return machWriteFloppy;
    }

    public void setMachWriteFloppy(String machWriteFloppy) {
        this.machWriteFloppy = machWriteFloppy;
    }

    public String getMachReadCDDVD() {
        return machReadCDDVD;
    }

    public void setMachReadCDDVD(String machReadCDDVD) {
        this.machReadCDDVD = machReadCDDVD;
    }

    public String getMachWriteCDDVD() {
        return machWriteCDDVD;
    }

    public void setMachWriteCDDVD(String machWriteCDDVD) {
        this.machWriteCDDVD = machWriteCDDVD;
    }

    public String getMachReadWPD() {
        return machReadWPD;
    }

    public void setMachReadWPD(String machReadWPD) {
        this.machReadWPD = machReadWPD;
    }

    public String getMachWriteWPD() {
        return machWriteWPD;
    }

    public void setMachWriteWPD(String machWriteWPD) {
        this.machWriteWPD = machWriteWPD;
    }

    public String getMachInternet() {
        return machInternet;
    }

    public void setMachInternet(String machInternet) {
        this.machInternet = machInternet;
    }

    public Map<String, SoftwareDetailsBean> getManagedApps() {
        return managedApps;
    }

    public void setManagedApps(Map<String, SoftwareDetailsBean> managedApps) {
        this.managedApps = managedApps;
    }

    public void setShowAppTab(String showAppTab) {
        this.showAppTab = showAppTab;
    }

    public String getShowAppTab() {
        return showAppTab;
    }
}
