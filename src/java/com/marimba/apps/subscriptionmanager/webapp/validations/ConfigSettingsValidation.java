// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.validations;

import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.PMSettingsForm;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;
import com.marimba.webapps.struts.validation.ValidationUtil;
import com.marimba.webapps.tools.util.KnownActionError;
import org.apache.struts.action.ActionErrors;
import javax.servlet.http.HttpServletRequest;

/**
 * Class to validate configuration settings page
 *
 * @author   Tamilselvan Teivasekamani
 * @version  $Revision$,  $Date$
 *
 */

public class ConfigSettingsValidation implements IErrorConstants, IWebAppConstants {

    public static boolean validateAllSettings(java.lang.Object bean, ActionErrors errors, HttpServletRequest request) {
        boolean isError = false;
        PMSettingsForm pmSettingsForm = (PMSettingsForm) bean;
        boolean siteBasedPolicy = "true".equals(pmSettingsForm.getValue(ENABLE_SITE_BASED_DEPLOYMENT).toString());
        if (siteBasedPolicy) {
            if (!ValidationUtil.required(pmSettingsForm.getMasterTxURL())) {
                isError = true;
                errors.add("masterTxURL", new KnownActionError(VALIDATION_FIELD_REQUIRED, pmSettingsForm.getMasterTxURL()));
            }
            else if (!ValidationUtil.validateURL(pmSettingsForm.getMasterTxURL())) {
                isError = true;
                errors.add("masterTxURL", new KnownActionError(VALIDATION_INVALIDURL, pmSettingsForm.getMasterTxURL()));
            }
        }
        boolean startLocation = "true".equals(pmSettingsForm.getValue(ENABLE_POLICY_START_LOCATION_ENABLED).toString());
        if(startLocation) {
        	if (!ValidationUtil.required(pmSettingsForm.getStartLocationPath())) {
                isError = true;
                errors.add("startLocationPath", new KnownActionError(VALIDATION_FIELD_REQUIRED, pmSettingsForm.getStartLocationPath()));
            }
        	else if (!LDAPWebappUtils.validateTargetDN(pmSettingsForm.getStartLocationPath(), request)) {
                isError = true;
                errors.add("startLocationPath", new KnownActionError(VALIDATION_INVALID_TARGETDN, pmSettingsForm.getStartLocationPath()));
            }
        }
        return !isError;
    }
}
