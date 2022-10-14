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

public class CustomSecurityForm extends AbstractForm {

    final String FALSE = "false";
    String schemeDisplay = FALSE;
    String selectedProfile = null;
    String selectedStandardProfile = null;
    String selectedStandardSubOption;
    String selectedSCAPOption = null;
    String selectedSCAPProfile = null;
    String customizePriorityValue = null;
    String standardPriorityValue = null;
    String remediate;
    String selectedScapAndProfilesCustom = null;
    Map<String, String> previewList = null;

    public void initialize() {
        selectedProfile = "Select";
        selectedStandardProfile = "";
        previewList = new HashMap<String, String>();
    }

    private String getValue(IConfig prop, String propName, String defValue) {
        String value = prop.getProperty(propName);
        return (value != null) ? value:defValue;
    }

    public void reset(ActionMapping mapping,
                      HttpServletRequest request) {
        selectedProfile = "Select";
        selectedStandardProfile = "";
        selectedStandardSubOption = "";
        customizePriorityValue = "N/A";
        standardPriorityValue = "N/A";
        remediate = "false";
        selectedScapAndProfilesCustom = "";
        previewList = new HashMap<String, String>();
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

    public String getSelectedStandardProfile() {
        return selectedStandardProfile;
    }

    public void setSelectedStandardProfile(String selectedStandardProfile) {
        this.selectedStandardProfile = selectedStandardProfile;
    }

    public String getSelectedSCAPOption() {
        return selectedSCAPOption;
    }

    public void setSelectedSCAPOption(String selectedSCAPOption) {
        this.selectedSCAPOption = selectedSCAPOption;
    }

    public String getSelectedSCAPProfile() {
        return selectedSCAPProfile;
    }

    public void setSelectedSCAPProfile(String selectedSCAPProfile) {
        this.selectedSCAPProfile = selectedSCAPProfile;
    }

    public String getCustomizePriorityValue() {
		return customizePriorityValue;
	}

	public void setCustomizePriorityValue(String customizePriorityValue) {
		this.customizePriorityValue = customizePriorityValue;
	}

	public String getStandardPriorityValue() {
		return standardPriorityValue;
	}

	public void setStandardPriorityValue(String standardPriorityValue) {
		this.standardPriorityValue = standardPriorityValue;
	}

	public String getSelectedStandardSubOption() {
        return selectedStandardSubOption;
    }

    public void setSelectedStandardSubOption(String selectedStandardSubOption) {
        this.selectedStandardSubOption = selectedStandardSubOption;
    }

    public String getRemediate() {
        return remediate;
    }

    public void setRemediate(String remediate) {
        this.remediate = remediate;
    }

    public String getSelectedScapAndProfilesCustom() {
        return selectedScapAndProfilesCustom;
    }

    public void setSelectedScapAndProfilesCustom(String selectedScapAndProfilesCustom) {
        this.selectedScapAndProfilesCustom = selectedScapAndProfilesCustom;
    }

    public Map<String, String> getPreviewList() {
        return previewList;
    }

    public void setPreviewList(Map<String, String> previewList) {
        this.previewList = previewList;
    }
}
