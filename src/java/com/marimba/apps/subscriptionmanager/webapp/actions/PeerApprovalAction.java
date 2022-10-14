// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscriptionmanager.webapp.forms.PeerApprovalForm;
import com.marimba.intf.util.IConfig;
import com.marimba.tools.config.ConfigProps;
import com.marimba.tools.util.QuotedTokenizer;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * PeerApprovalAction saving Email option related settings to "properties.txt" 
 *
 * @author Tamilselvan Teivasekamani
 * @version $Revision$,  $Date$
 */

public class PeerApprovalAction extends AbstractAction {

    private ConfigProps config;
    private IConfig tunerConfig;

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        PeerApprovalForm approvalForm = (PeerApprovalForm) form;

        super.init(request);
        this.config = main.getConfig();
        this.tunerConfig = (IConfig)main.getFeatures().getChild("tunerConfig");

        String action = request.getParameter("action");
        if ("save".equals(action)) {
            if ("true".equals(approvalForm.getMailSettings())) {
                config.setProperty(EMAIL_SETTINGS, approvalForm.getMailSettings());
                String ids = approvalForm.getMailToAddress();
                config.setProperty(EMAIL_TO_ADDRESS, filterDuplicate(ids));
            } else {
                config.setProperty(EMAIL_SETTINGS, null);
                config.setProperty(EMAIL_TO_ADDRESS, null);
                config.setProperty(PEER_APPROVAL_SETTINGS, null);
                config.setProperty(PEER_APPROVAL_TO_ADDRESS, null);
            }

            if ("true".equals(approvalForm.getPeersSettings())) {
                config.setProperty(PEER_APPROVAL_SETTINGS, approvalForm.getPeersSettings());
                String groups = approvalForm.getPeersToAddress();
                config.setProperty(PEER_APPROVAL_TO_ADDRESS, filterDuplicate(groups));
                if ("servicenow".equalsIgnoreCase(approvalForm.getPeersType())) {
                    config.setProperty(PEER_APPROVAL_TYPE, "servicenow");
                    tunerConfig.setProperty(TUNER_PROP_POLICY_PEERAPPROVAL_SERVICEAUTOMATION_INTEGRATION, "servicenow");
                    tunerConfig.setProperty(TUNER_PROP_POLICY_PEERAPPROVAL_SERVICEAUTOMATION_ENABLED, "true");
                } else if ("remedyforce".equalsIgnoreCase(approvalForm.getPeersType())) {
                    config.setProperty(PEER_APPROVAL_TYPE, "remedyforce");
                    tunerConfig.setProperty(TUNER_PROP_POLICY_PEERAPPROVAL_SERVICEAUTOMATION_INTEGRATION, "remedyforce");
                    tunerConfig.setProperty(TUNER_PROP_POLICY_PEERAPPROVAL_SERVICEAUTOMATION_ENABLED, "true");
                } else {
                    config.setProperty(PEER_APPROVAL_TYPE, "ldap");
                    tunerConfig.setProperty(TUNER_PROP_POLICY_PEERAPPROVAL_SERVICEAUTOMATION_INTEGRATION, null);
                    tunerConfig.setProperty(TUNER_PROP_POLICY_PEERAPPROVAL_SERVICEAUTOMATION_ENABLED, "false");
                }
            } else {
                config.setProperty(PEER_APPROVAL_SETTINGS, null);
                config.setProperty(PEER_APPROVAL_TYPE, null);
                config.setProperty(PEER_APPROVAL_TO_ADDRESS, null);
                tunerConfig.setProperty(TUNER_PROP_POLICY_PEERAPPROVAL_SERVICEAUTOMATION_INTEGRATION, null);
                tunerConfig.setProperty(TUNER_PROP_POLICY_PEERAPPROVAL_SERVICEAUTOMATION_ENABLED, "false");
            }
            config.save();
            return mapping.findForward("done");
        } else {
            String smtpHost = main.getServerConfig().getProperty("smtp.host"); // Refer msf.txt for SMTP settings properties
            String isEnabled = (null == smtpHost || smtpHost.trim().isEmpty()) ? "disabled" : "enabled";

            approvalForm.setValue("cms.smtp.settings", isEnabled);

            approvalForm.setMailSettings((getConfigValue(EMAIL_SETTINGS, "false")));
            approvalForm.setMailToAddress(getConfigValue(EMAIL_TO_ADDRESS, ""));
            approvalForm.setPeersSettings(getConfigValue(PEER_APPROVAL_SETTINGS, "false"));
            approvalForm.setPeersType(getConfigValue(PEER_APPROVAL_TYPE, "ldap"));
            approvalForm.setPeersToAddress(getConfigValue(PEER_APPROVAL_TO_ADDRESS, ""));

            if (false) {
                approvalForm.setIsServiceAutomationIntegrationLicenseAvailable("true");
            } else {
                approvalForm.setIsServiceAutomationIntegrationLicenseAvailable("false");
                tunerConfig.setProperty(TUNER_PROP_POLICY_PEERAPPROVAL_SERVICEAUTOMATION_INTEGRATION, null);
                tunerConfig.setProperty(TUNER_PROP_POLICY_PEERAPPROVAL_SERVICEAUTOMATION_ENABLED, "false");
            }
            return mapping.findForward("load");
        }
    }

    private String getConfigValue(String prop, String defaultVal) {
        return (null == config.getProperty(prop)) ? defaultVal : config.getProperty(prop);
    }

    // Just to avoid same group names at multiple times
    private String filterDuplicate(String org) {
        if (null == org) {
            return "";
        }
        Set<String> tmpSet = new HashSet<String>(5);
        QuotedTokenizer tok = new QuotedTokenizer(org, ",", '\\', '\"', true, false);
        while (tok.hasMoreTokens()) {
            tmpSet.add(tok.nextToken().trim());
        }
        org = "";
        for (String str : tmpSet) {
            org = org + "," + str;
        }
        if (org.startsWith(",")) {
            org = org.substring(1, org.length());
        }
        if (org.endsWith(",")) {
            org = org.substring(0, org.length() - 1);
        }
        return org;
    }
}