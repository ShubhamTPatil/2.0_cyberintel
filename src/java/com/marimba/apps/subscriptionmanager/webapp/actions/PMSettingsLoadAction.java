// Copyright 1996-2015, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import static com.marimba.apps.subscriptionmanager.intf.IARTaskConstants.AR_TASK_ID_ENABLE;
import static com.marimba.apps.subscriptionmanager.intf.IARTaskConstants.AR_TASK_ID_ENABLED_CONFIG;

import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.webapp.forms.PMSettingsForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.intf.util.IConfig;
import com.marimba.intf.util.IProperty;
import com.marimba.tools.ldap.LDAPConstants;

import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.intf.IUser;

import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.SystemException;

import com.marimba.webapps.intf.GUIException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PMSettingsLoadAction extends AbstractAction {

    private IConfig config;
    private IProperty subConfig;

    /**
     * REMIND
     *
     * @param mapping REMIND
     * @param form REMIND
     * @param request REMIND
     * @param response REMIND
     *
     * @return REMIND
     *
     * @throws java.io.IOException REMIND
     * @throws javax.servlet.ServletException REMIND
     * @throws com.marimba.webapps.intf.GUIException REMIND
     */

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        PMSettingsForm myForm = (PMSettingsForm) form;
        try {
            init(request);
            this.config = main.getConfig();
            this.subConfig = main.getSubscriptionConfig();
            /*  int scrubberInt = ComplianceEngine.parseScrubberFlagFromString(config.getProperty(ComplianceConstants.sScrubberFlagString));
            if (scrubberInt == ComplianceConstants.sComplianceScrubberDoNothing) {
                myForm.setValue(PERFORMANCE_SCRUBBERON, null);
            } else {
                myForm.setValue(PERFORMANCE_SCRUBBERON, "true");
            }
            */
            myForm.setCloudEnabled(isCloudEnabled());

            myForm.setValue(PUSH_ENABLED_GUI, getPropValue(PUSH_ENABLED_CONFIG));
            myForm.setValue(AR_TASK_ID_ENABLE, getPropValue(AR_TASK_ID_ENABLED_CONFIG));
            myForm.setValue(COMPUTER_UNASSIGN_OTHER_GP_ENABLED, getPropValue(COMPUTER_UNASSIGN_OTHER_GP_ENABLED));
            myForm.setValue(ENABLE_WOW_FEATURE, isCloudEnabled() ? "false" : getPropValue(ENABLE_WOW_FEATURE));
            myForm.setValue(ENABLE_SITE_BASED_DEPLOYMENT, getSubConfigValue(LDAPConstants.CONFIG_SITEBASEDPOLICY_ENABLED));
            String masterTxUrl = config.getProperty(SITE_BASED_MASTER_TX_URL);
            myForm.setMasterTxURL(null == masterTxUrl ? "" : masterTxUrl);
            myForm.setValue(ENABLE_POLICY_START_LOCATION_ENABLED, getPropValue(ENABLE_POLICY_START_LOCATION_ENABLED));
            String startLocationPath = config.getProperty(ENABLE_POLICY_START_LOCATION_PATH);
            myForm.setStartLocationPath(null == startLocationPath ?  "" : startLocationPath);
            IUser user = GUIUtils.getUser(request);
            loadFeaturesProperty(myForm, user);
        } catch (Exception e) {
            throw new GUIException(e);
        }
        return (mapping.findForward("success"));
    }

    private String getPropValue(String propKey) {
        return "true".equals(config.getProperty(propKey)) ? "true" : null;
    }
    private String getSubConfigValue(String propKey) {
        try {
            if(null == subConfig) main.initLDAP();
        } catch(Exception ec) {

        }
        return "true".equals(subConfig.getProperty(propKey)) ? "true" : null;
    }
	private void loadFeaturesProperty(PMSettingsForm myForm, IUser user){
		try {
			ISubscription sub = ObjectManager.openSubForRead(TYPE_ALL,TYPE_ALL, user);

			String userControlledEnabled= sub.getProperty(PROP_TUNER_KEYWORD, ENABLE_USER_CONTROLLED_DEPLOYMENT);
			if(null == userControlledEnabled || "".equals(userControlledEnabled.trim())) {
				userControlledEnabled = "false";
			}
			myForm.setValue(ENABLE_USER_CONTROLLED_DEPLOYMENT, userControlledEnabled);
			
			String autoScanEnabled= sub.getProperty(PROP_TUNER_KEYWORD, ENABLE_AUTOSCAN_FEATURE);
			if(null == autoScanEnabled || "".equals(autoScanEnabled.trim())) {
				autoScanEnabled = "false";
			}
			myForm.setValue(ENABLE_AUTOSCAN_FEATURE, autoScanEnabled);

			String userCentricEnabled = sub.getProperty(PROP_TUNER_KEYWORD, ENABLE_USER_CENTRIC_DEPLOYMENT);
			if(null == userCentricEnabled || "".equals(userCentricEnabled.trim())) {
				userCentricEnabled = "false";
			}
			myForm.setValue(ENABLE_USER_CENTRIC_DEPLOYMENT, userCentricEnabled);

		} catch(Exception ex){
			ex.printStackTrace();
			myForm.setValue(ENABLE_USER_CONTROLLED_DEPLOYMENT, "false");
			myForm.setValue(ENABLE_AUTOSCAN_FEATURE, "false");
			myForm.setValue(ENABLE_USER_CENTRIC_DEPLOYMENT, "false");
		}
	}

}