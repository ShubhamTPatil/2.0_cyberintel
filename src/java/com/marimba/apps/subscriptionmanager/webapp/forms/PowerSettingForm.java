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

/**
 * REMIND
 *
 * @author Venkatesh Jeyaraman
 * @version $File$
 */
public class PowerSettingForm
        extends AbstractForm {

    final String FALSE = "false";
    String schemeDisplay = FALSE;
    String selectedProfile = null;

    public void initialize(IConfig prop) {

        setValue(POWER_OPTION_PROP, getValue(prop, POWER_OPTION_PROP, FALSE));
        setValue(POWER_PROFILE_SELECTED_PROP, getValue(prop, POWER_PROFILE_SELECTED_PROP, ""));
        setValue(HIBERNATE_PROP, getValue(prop, HIBERNATE_PROP, FALSE));
        setValue(HIBER_IDLETIME_PROP, getValue(prop, HIBER_IDLETIME_PROP, POWER_IDLE_DEFAULT_VALUE));
        setValue(MONITOR_IDLETIME_PROP, getValue(prop, MONITOR_IDLETIME_PROP, POWER_IDLE_DEFAULT_VALUE));
        setValue(DISK_IDLETIME_PROP, getValue(prop, DISK_IDLETIME_PROP, POWER_IDLE_DEFAULT_VALUE));
        setValue(STANDBY_IDLETIME_PROP, getValue(prop, STANDBY_IDLETIME_PROP, POWER_IDLE_DEFAULT_VALUE));
        setValue(HIBER_IDLETIME_DC_PROP, getValue(prop, HIBER_IDLETIME_DC_PROP, POWER_IDLE_DEFAULT_VALUE));
        setValue(MONITOR_IDLETIME_DC_PROP, getValue(prop, MONITOR_IDLETIME_DC_PROP, POWER_IDLE_DEFAULT_VALUE));
        setValue(DISK_IDLETIME_DC_PROP, getValue(prop, DISK_IDLETIME_DC_PROP, POWER_IDLE_DEFAULT_VALUE));
        setValue(STANDBY_IDLETIME_DC_PROP, getValue(prop, STANDBY_IDLETIME_DC_PROP, POWER_IDLE_DEFAULT_VALUE));
        setValue(PROMPT_PASSWORD_PROP, getValue(prop, PROMPT_PASSWORD_PROP, FALSE));
        setValue(FORCE_APPLY_PROP, getValue(prop, FORCE_APPLY_PROP, FALSE));
        setValue(POWER_PRIORITY_VALUE, getValue(prop, POWER_PRIORITY_VALUE, ""));
    }

    private String getValue(IConfig prop, String propName, String defValue) {
        String value = prop.getProperty(propName);
        return (value != null) ? value:defValue;
    }

    public void reset(ActionMapping mapping,
                      HttpServletRequest request) {
        setValue(POWER_OPTION_PROP, FALSE);
        setValue(HIBERNATE_PROP, FALSE);
        setValue(HIBER_IDLETIME_PROP, POWER_IDLE_DEFAULT_VALUE);
        setValue(MONITOR_IDLETIME_PROP, POWER_IDLE_DEFAULT_VALUE);
        setValue(DISK_IDLETIME_PROP, POWER_IDLE_DEFAULT_VALUE);
        setValue(STANDBY_IDLETIME_PROP, POWER_IDLE_DEFAULT_VALUE);
        setValue(HIBER_IDLETIME_DC_PROP, POWER_IDLE_DEFAULT_VALUE);
        setValue(MONITOR_IDLETIME_DC_PROP, POWER_IDLE_DEFAULT_VALUE);
        setValue(DISK_IDLETIME_DC_PROP, POWER_IDLE_DEFAULT_VALUE);
        setValue(STANDBY_IDLETIME_DC_PROP, POWER_IDLE_DEFAULT_VALUE);
        setValue(PROMPT_PASSWORD_PROP, FALSE);
        setValue(FORCE_APPLY_PROP, FALSE);
        setValue(POWER_PRIORITY_VALUE, "");
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
}
