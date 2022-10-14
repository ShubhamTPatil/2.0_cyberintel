// Copyright 2011, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.view;

/**
 * Result object for OS Migration compliance Status
 *
 * @author  Selvaraj Jegatheesan
 * @version $Revision$, $Date$
 */

public class OSDeploymentStatusBean implements java.io.Serializable {
    // OS Migration Summary Result
    int notcheckedinCount;
    int compliantCount;
    int noncompliantCount;
    int totalCount;
    // OS Migration Reports
    String machineName = null;
    String complianceLevel = null;
    String updatedTime = null;
    String templateName = null;

    // Storing exception message to facilitate to display in GUI
    String exception = "";

    public void setException( String exception ){
        this.exception = exception;
    }

    public String getException(){
        return exception;
    }

    public void setNotcheckedinCount(int notcheckedinCount) {
        this.notcheckedinCount = notcheckedinCount;
    }

    public int getNotcheckedinCount() {
        return this.notcheckedinCount;
    }

    public void setCompliantCount(int compliantCount) {
        this.compliantCount = compliantCount;
    }

    public int getCompliantCount() {
        return this.compliantCount;
    }

    public void setNoncompliantConut(int noncompliantCount) {
        this.noncompliantCount = noncompliantCount;
    }

    public int getNoncompliantCount () {
        return this.noncompliantCount;
    }

    public void setTotalCount( int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalCount() {
        return this.totalCount;
    }

    public int getNotcheckedinPer() {
        if (totalCount > 0 && notcheckedinCount >= 0 && notcheckedinCount <= totalCount) {
            return Math.round( ( ( float )notcheckedinCount/totalCount )*100 );
        } else {
            return 0;
        }
    }

    public int getNoncompliantPer() {
        if (totalCount > 0 && noncompliantCount >= 0 && noncompliantCount <= totalCount) {
            return Math.round( ( ( float )noncompliantCount/totalCount )*100 );
        } else {
            return 0;
        }
    }

    public int getCompliantPer() {
        if (totalCount > 0 && compliantCount >= 0 && compliantCount <= totalCount) {
            return Math.round( ( ( float )compliantCount/totalCount )*100 );
        } else {
            return 0;
        }
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getMachineName() {
        return this.machineName;
    }

    public void setComplianceLevel(String complianceLevel) {
        this.complianceLevel = complianceLevel;
    }

    public String getComplianceLevel() {
        return this.complianceLevel;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getUpdatedTime() {
        return this.updatedTime;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return this.templateName;
    }

}
