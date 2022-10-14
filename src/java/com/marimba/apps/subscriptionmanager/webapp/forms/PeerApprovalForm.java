// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.forms;

/**
 * PeerApprovalForm
 *
 * @author Tamilselvan Teivasekamani
 * @version $Revision$,  $Date$
 */

public class PeerApprovalForm extends AbstractForm {

    private String mailSettings;
    private String peersSettings;
    private String mailToAddress;
    private String peersToAddress;
    private String peersType;
    private String isServiceAutomationIntegrationLicenseAvailable;

    public String getMailSettings() {
        return mailSettings;
    }

    public void setMailSettings(String mailSettings) {
        this.mailSettings = mailSettings;
    }

    public String getPeersSettings() {
        return peersSettings;
    }

    public void setPeersSettings(String peersSettings) {
        this.peersSettings = peersSettings;
    }

    public String getMailToAddress() {
        return mailToAddress;
    }

    public void setMailToAddress(String mailToAddress) {
        this.mailToAddress = mailToAddress;
    }

    public String getPeersToAddress() {
        return peersToAddress;
    }

    public void setPeersToAddress(String peersToAddress) {
        this.peersToAddress = peersToAddress;
    }

    public String getPeersType() {
        return peersType;
    }

    public void setPeersType(String peersType) {
        this.peersType = peersType;
    }

    public String getIsServiceAutomationIntegrationLicenseAvailable() {
        return isServiceAutomationIntegrationLicenseAvailable;
    }

    public void setIsServiceAutomationIntegrationLicenseAvailable(String isServiceAutomationIntegrationLicenseAvailable) {
        this.isServiceAutomationIntegrationLicenseAvailable = isServiceAutomationIntegrationLicenseAvailable;
    }
}
