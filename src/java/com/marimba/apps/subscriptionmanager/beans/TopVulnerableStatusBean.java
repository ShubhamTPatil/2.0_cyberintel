// Copyright 2019-2022, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.beans;

import java.lang.*;


/**
 *  TopVulnerableStatusBean
 *  w.r.t show Top Vulnerable Info - Tabular Report
 *
 * @author Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 */


public class TopVulnerableStatusBean {
    String cveId;
    String severity;
    String affectedMachines;
    String patchId;
    String status;
    String riskScore;

    public String getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(String riskScore) {
        this.riskScore = riskScore;
    }

    public String getCveId() {
        return cveId;
    }

    public void setCveId(String cveId) {
        this.cveId = cveId;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getAffectedMachines() {
        return affectedMachines;
    }

    public void setAffectedMachines(String affectedMachines) {
        this.affectedMachines = affectedMachines;
    }

    public String getPatchId() {
        return patchId;
    }

    public void setPatchId(String patchId) {
        this.patchId = patchId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}


