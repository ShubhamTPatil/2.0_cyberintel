// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.ajax.ucd;

import static com.marimba.apps.subscription.common.ISubscriptionConstants.*;

import java.util.Map;

/**
 * UCDTemplateDetailsFormatter, Format UCD Template details used to display in the UI
 *
 * @author  Tamilselvan Teivasekamani
 * @version $Revision$, $Date$
 */

public class UCDTemplateDetailsFormatter {
    private StringBuilder ucdDetails;
    private Map<String, String> propsMap;
    private UCDTemplatesAction baseClass;
    private String PRE_KEY = "page.edit_ucd_template.";

    public UCDTemplateDetailsFormatter(UCDTemplatesAction baseClass, Map<String, String> propsMap) {
        this.propsMap = propsMap;
        this.baseClass = baseClass;
        this.ucdDetails = new StringBuilder();
        formatDetails();
    }

    public String getFormatedDetails() {
        return this.ucdDetails.toString();
    }

    private void formatDetails() {
        addAsMainItem("TemplateNameLabel", UCD_TEMPLATE_NAME);
        addAsMainItem("TemplateDescLabel", UCD_TEMPLATE_DESCRIPTION);
        addAsMainItem("label.deviceLevel", UCD_DEVICELEVEL);
        addAsMainItem("label.enableLoginHistroy", UCD_LOGIN_HISTORY);
        if (Boolean.parseBoolean(propsMap.get(UCD_LOGIN_HISTORY))) {
            addAsSubItem("label.workinghrs", UCD_WORKHRS);
            addAsSubItem("label.workdays", UCD_WORKDAYS);
            addAsSubItem("label.enableLockUnlock", UCD_LOCKUNLOCK);
            addAsSubItem("label.enableRemoteLoginMode", UCD_REMOTELOGINMODE);
        }
        addAsMainItem("label.enableDeviceConfig", UCD_DEVICECONFIG);
        if (Boolean.parseBoolean(propsMap.get(UCD_DEVICECONFIG))) {
            addAsSubItem("label.ramsize", UCD_RAMSIZE);
            addAsSubItem("label.diskspace", UCD_DISKSPACE);
            addAsSubItem("label.osname", UCD_OSNAME);
            addAsSubItem("label.processor", UCD_PROCESSOR);
        }
    }

    private void addAsMainItem(String msgKey, String value) {
        ucdDetails.append("<tr><td><b>").append(getMessage(msgKey)).append("</b></td><td>:&nbsp;").append(getPropertValue(value)).append("</td></tr>");
    }

    private void addAsSubItem(String msgKey, String value) {
        ucdDetails.append("<tr><td>&nbsp;</td><td>&nbsp;&nbsp;").append(getMessage(msgKey)).append("&nbsp;:&nbsp;").append(getPropertValue(value)).append("</td></tr>");
    }

    private String getPropertValue(String key) {
        return propsMap.get(key) != null ? propsMap.get(key) : getMessage0("page.target_details.NA");
    }
    
    private String getMessage(String key) {
        return getMessage0(PRE_KEY + key);
    }

    private String getMessage0(String key) {
        return this.baseClass.getMessage(key);
    }
}
