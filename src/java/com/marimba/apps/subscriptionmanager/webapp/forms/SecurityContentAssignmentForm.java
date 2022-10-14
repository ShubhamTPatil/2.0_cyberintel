// Copyright 1997-2017, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.forms;

import com.marimba.intf.util.IConfig;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class SecurityContentAssignmentForm extends AbstractForm {

    String action;

    final String FALSE = "false";
    String schemeDisplay = FALSE;
    String selectedWindowsProfiles = null;
    String selectedNonWindowsProfiles = null;
    String customizedProfiles = null;

    String priority;
    String remediateEnabled;
    Map<String, String> previewList = null;
    String stepId;

    public void initialize() {
        selectedWindowsProfiles = "";
        selectedNonWindowsProfiles = "";
        previewList = new HashMap<String, String>();
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }


    private String getValue(IConfig prop, String propName, String defValue) {
        String value = prop.getProperty(propName);
        return (value != null) ? value:defValue;
    }

    public String getschemeDisplay() {
        return schemeDisplay;
    }

    public void setschemeDisplay(String schemeDisplay) {
        this.schemeDisplay = schemeDisplay;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getRemediateEnabled() {
        return remediateEnabled;
    }

    public void setRemediateEnabled(String remediateEnabled) {
        this.remediateEnabled = remediateEnabled;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public Map<String, String> getPreviewList() {
        return previewList;
    }

    public void setPreviewList(Map<String, String> previewList) {
        this.previewList = previewList;
    }

    public String getSchemeDisplay() {
        return schemeDisplay;
    }

    public void setSchemeDisplay(String schemeDisplay) {
        this.schemeDisplay = schemeDisplay;
    }

    public String getSelectedWindowsProfiles() {
        return selectedWindowsProfiles;
    }

    public void setSelectedWindowsProfiles(String selectedWindowsProfiles) {
        this.selectedWindowsProfiles = selectedWindowsProfiles;
    }

    public String getSelectedNonWindowsProfiles() {
        return selectedNonWindowsProfiles;
    }

    public void setSelectedNonWindowsProfiles(String selectedNonWindowsProfiles) {
        this.selectedNonWindowsProfiles = selectedNonWindowsProfiles;
    }

    public String getCustomizedProfiles() {
        return customizedProfiles;
    }

    public void setCustomizedProfiles(String customizedProfiles) {
        this.customizedProfiles = customizedProfiles;
    }

    public void reset(ActionMapping mapping,
                      HttpServletRequest request) {
        selectedWindowsProfiles = "";
        selectedNonWindowsProfiles  = "";
        priority = "99999";
        remediateEnabled = "false";
        previewList = new HashMap<String, String>();
    }
}