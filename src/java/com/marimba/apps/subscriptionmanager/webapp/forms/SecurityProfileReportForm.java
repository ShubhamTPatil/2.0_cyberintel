// Copyright 2018, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.forms;

import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

import com.marimba.apps.subscriptionmanager.beans.SecurityProfileDetailsBean;
import com.marimba.apps.subscriptionmanager.beans.SecurityOvalGeneralDetailsBean;

/**
 * This form is merely used for storing the state of the page.
 *
 * @author Elaiyaraja Thangavel
 *
 */
public class SecurityProfileReportForm extends AbstractForm {
    String action;
    String profileId;
    String contentId;
    String xmlType;
    SecurityProfileDetailsBean securityProfileDetailsBean;
    SecurityOvalGeneralDetailsBean securityOvalGeneralDetailsBean;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getXmlType() {
        return xmlType;
    }

    public void setXmlType(String xmlType) {
        this.xmlType = xmlType;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public SecurityProfileDetailsBean getSecurityProfileDetailsBean() {
        return securityProfileDetailsBean;
    }

    public void setSecurityProfileDetailsBean(SecurityProfileDetailsBean securityProfileDetailsBean) {
        this.securityProfileDetailsBean = securityProfileDetailsBean;
    }

    public SecurityOvalGeneralDetailsBean getSecurityOvalGeneralDetailsBean() {
        return securityOvalGeneralDetailsBean;
    }

    public void setSecurityOvalGeneralDetailsBean(SecurityOvalGeneralDetailsBean securityOvalGeneralDetailsBean) {
        this.securityOvalGeneralDetailsBean = securityOvalGeneralDetailsBean;
    }

    public void reset(ActionMapping mapping, HttpServletRequest request) {
    }
}