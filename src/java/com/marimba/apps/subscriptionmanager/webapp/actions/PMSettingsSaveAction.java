// Copyright 1996-2015, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.compliance.ComplianceConstants;
import static com.marimba.apps.subscriptionmanager.intf.IARTaskConstants.AR_TASK_ID_ENABLE;
import static com.marimba.apps.subscriptionmanager.intf.IARTaskConstants.AR_TASK_ID_ENABLED_CONFIG;
import com.marimba.apps.subscriptionmanager.webapp.forms.PMSettingsForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.tools.config.ConfigProps;

import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.intf.IUser;

import com.marimba.webapps.intf.GUIException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PMSettingsSaveAction extends AbstractAction {
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
                                 HttpServletRequest  request, HttpServletResponse response)
            throws IOException, ServletException {

        String masterTxUrl = null;
        String startLocationPath = null;
        PMSettingsForm myForm = (PMSettingsForm) form;

        try {
            init(request);
            ConfigProps config = main.getConfig();

            boolean scrubberOn = "true".equals(myForm.getValue(PERFORMANCE_SCRUBBERON));

            config.setProperty(ComplianceConstants.sScrubberFlagString, scrubberOn ? Integer.toString(ComplianceConstants.sComplianceScrubberDoEverything) : Integer.toString(ComplianceConstants.sComplianceScrubberDoNothing));
            config.setProperty(PUSH_ENABLED_CONFIG, myForm.getValue(PUSH_ENABLED_GUI).toString());
            config.setProperty(AR_TASK_ID_ENABLED_CONFIG, myForm.getValue(AR_TASK_ID_ENABLE).toString());
            config.setProperty(COMPUTER_UNASSIGN_OTHER_GP_ENABLED, myForm.getValue(COMPUTER_UNASSIGN_OTHER_GP_ENABLED).toString());
            config.setProperty(ENABLE_WOW_FEATURE, myForm.getValue(ENABLE_WOW_FEATURE).toString());
            HashMap subConfigMap = new HashMap();
            String isSitePolicyEnabled = myForm.getValue(ENABLE_SITE_BASED_DEPLOYMENT).toString();
            if(null == isSitePolicyEnabled || isSitePolicyEnabled.trim().length() == 0) {
            	isSitePolicyEnabled = "false";
            }
            subConfigMap.put(LDAPConstants.CONFIG_SITEBASEDPOLICY_ENABLED, isSitePolicyEnabled);
            main.setSubscriptionConfigProperties(subConfigMap);
            
            if ("true".equals(myForm.getValue(ENABLE_SITE_BASED_DEPLOYMENT).toString())) {
                masterTxUrl = myForm.getMasterTxURL();
            }
            config.setProperty(SITE_BASED_MASTER_TX_URL, (null == masterTxUrl) ? null : masterTxUrl.trim());
            String isStartLocationEnabled = myForm.getValue(ENABLE_POLICY_START_LOCATION_ENABLED).toString();
            if(null == isStartLocationEnabled || isStartLocationEnabled.trim().length() == 0) {
            	isStartLocationEnabled = "false";
            }
            config.setProperty(ENABLE_POLICY_START_LOCATION_ENABLED, isStartLocationEnabled);
            if("true".equals(myForm.getValue(ENABLE_POLICY_START_LOCATION_ENABLED).toString())) {
            	startLocationPath = myForm.getStartLocationPath();
            }
            config.setProperty(ENABLE_POLICY_START_LOCATION_PATH, startLocationPath);
            String dmSettings = "true".equals(myForm.getValue(PUSH_ENABLED_GUI).toString()) ? "\"Enabled\"" : "\"Disabled\"";
            String arSettings = "true".equals(myForm.getValue(AR_TASK_ID_ENABLE).toString()) ? "\"Enabled\"" : "\"Disabled\"";
            String unassignSettings = "true".equals(myForm.getValue(COMPUTER_UNASSIGN_OTHER_GP_ENABLED).toString()) ? "\"Enabled\"" : "\"Disabled\"";
            String enableWoW = "true".equals(myForm.getValue(ENABLE_WOW_FEATURE).toString()) ? "\"Enabled\"" : "\"Disabled\"";
            String siteBasedPolicy = "true".equals(myForm.getValue(ENABLE_SITE_BASED_DEPLOYMENT).toString()) ? "\"Enabled\"" : "\"Disabled\"";

            StringBuilder toPrint = new StringBuilder(500);
            toPrint.append("Policy update feature is ").append(dmSettings).append(", ");
            toPrint.append("Editable Task ID and Change ID is ").append(arSettings).append(", ");
            toPrint.append("Computer unassignment in Empirum is ").append(unassignSettings).append(", ");
            toPrint.append("WoW deployment is ").append(enableWoW).append(", ");
            toPrint.append("Site based policy is ").append(siteBasedPolicy);

            main.logAuditInfo(LOG_AUDIT_PMSETTINGS, LOG_AUDIT, "Policy Manager", toPrint.toString(), request, PM_SETTINGS);

            config.save();
            IUser user = GUIUtils.getUser(request);
            saveFeaturesProperty(myForm, user);
        } catch (Exception e) {
            throw new GUIException(e);
        }
        return (mapping.findForward("success"));
    }
	private void saveFeaturesProperty(PMSettingsForm myForm, IUser user){
		try {
			ISubscription sub = ObjectManager.openSubForWrite(TYPE_ALL,TYPE_ALL, user);
			sub.setProperty(PROP_TUNER_KEYWORD, ENABLE_USER_CONTROLLED_DEPLOYMENT, myForm.getValue(ENABLE_USER_CONTROLLED_DEPLOYMENT).toString());
			sub.setProperty(PROP_TUNER_KEYWORD, ENABLE_AUTOSCAN_FEATURE, myForm.getValue(ENABLE_AUTOSCAN_FEATURE).toString());
			sub.setProperty(PROP_TUNER_KEYWORD, ENABLE_USER_CENTRIC_DEPLOYMENT, myForm.getValue(ENABLE_USER_CENTRIC_DEPLOYMENT).toString());
			sub.save();

		} catch(Exception ex){
		}
	}
}
