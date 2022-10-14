// Copyright 1997-2005, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$


package com.marimba.apps.subscriptionmanager.compliance.view;

import com.marimba.apps.subscriptionmanager.compliance.util.WebUtil;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Bean that holds machine information.
 *
 * @author  Manoj Kumar
 * @version $Revision$, $Date$
 */

public class MachineBean implements java.io.Serializable{
    String machineName;
    String machineID;
    String endpointState;
    String complianceLevel;
    String policyState;
    String url;
    String failureCause;
    String packageTitle;
    String policyName;
    String policyId;
    String osType;
    String osProduct;
    long scanTimeInMillis;

    Date lastCheckIn = null; // last checkin time of machine
    Date lastScanTime = null; // last scan time of machine
    java.util.Date policyLastUpdated;

    public String getFailureCause() {
        if( failureCause == null ){
            if( url != null ){
                int lastSlash = url.lastIndexOf( '/' );
                if( lastSlash != -1 ){
                    failureCause = url.substring( lastSlash+1, url.length() );
                }
            } else {
                failureCause = "";
            }
        }
        return failureCause;
    }

    public void setFailureCause(String info) {
        this.failureCause = info;
    }

    public String getLastCheckIn() {
        if( lastCheckIn != null ){
            return WebUtil.getComplianceDateFormat().format( lastCheckIn );
        }
        return "";
    }

    public void setLastCheckIn( Date lastCheckIn ) {
        this.lastCheckIn = lastCheckIn;
    }

    public String getPolicyState() {
        return policyState;
    }

    public void setPolicyState(String policyState) {
        this.policyState = policyState;
    }

    public String getPolicyName() {
	    return policyName;
    }

    public String getOsType() {
        if (osType == null && osProduct != null) {
            if (osProduct.toLowerCase().indexOf("windows") != -1) osType = "windows";
            else if (osProduct.toLowerCase().indexOf("linux") != -1) osType = "linux";
            else if (osProduct.toLowerCase().indexOf("macos") != -1) osType = "mac";
        }
        return osType;
    }

    public String getOsProduct() {
        return osProduct;
    }

    public void setOsProduct(String osProduct) {
        this.osProduct = osProduct;
    }

    public void setPolicyId(String policy) {
	this.policyId = policy;
	if (policyId != null) {
	    int index = policyId.indexOf(",");
	    if (index == -1) {
		policyName = policyId;
	    } else {
		policyName = policyId.substring(0, index);
	    }
	}
    }

    public String getPackageTitle() {
	return packageTitle;
    }

    public void setPackageTitle(String title) {
	this.packageTitle = title;
    }

    public String getUrl() {
	return url;
    }

    public void setUrl(String url) {
	this.url = url;
    }

    public String getMachineID() {
        return machineID;
    }

    public void setMachineID(String machineID) {
        this.machineID = machineID;
    }

    public String getEndpointState() {
        return endpointState;
    }

    public void setEndpointState(String endpointState) {
        this.endpointState = endpointState;
    }

    public String getPolicyLastUpdated() {
        if( policyLastUpdated != null ){
            return WebUtil.getComplianceDateFormat().format( policyLastUpdated );
        }
        return "";
    }

    public void setPolicyLastUpdated(java.util.Date policyLastUpdated ) {
        this.policyLastUpdated = policyLastUpdated;
    }

    public String getLastScanTime() {
        if( lastScanTime != null ){
            return WebUtil.getComplianceDateFormat().format( lastScanTime );
        }
        return "";
    }

    public void setLastScanTime( Date lastScanTime ) {
        this.lastScanTime = lastScanTime;
    }

    public String getComplianceLevel() {
        return complianceLevel;
    }

    public void setComplianceLevel(String complianceLevel) {
        this.complianceLevel = complianceLevel;
    }

    public MachineBean() {
        this("");
    }

    public MachineBean(String machineName) {
        this.machineName = machineName;
    }

    public long getScanTimeInMillis() {
        return scanTimeInMillis;
    }

    public void setScanTimeInMillis(long scanTimeInMillis) {
        this.scanTimeInMillis = scanTimeInMillis;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MachineBean)) return false;

        final MachineBean machineBean = (MachineBean) o;

        if (complianceLevel != null ? !complianceLevel.equals(machineBean.complianceLevel) : machineBean.complianceLevel != null) return false;
        if (endpointState != null ? !endpointState.equals(machineBean.endpointState) : machineBean.endpointState != null) return false;
        if (machineID != null ? !machineID.equals(machineBean.machineID) : machineBean.machineID != null) return false;
        if (machineName != null ? !machineName.equals(machineBean.machineName) : machineBean.machineName != null) return false;
        if (policyLastUpdated != null ? !policyLastUpdated.equals(machineBean.policyLastUpdated) : machineBean.policyLastUpdated != null) return false;
	if (url != null ? url.equals(machineBean.url) : machineBean.url != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (machineName != null ? machineName.hashCode() : 0);
        result = 29 * result + (machineID != null ? machineID.hashCode() : 0);
        result = 29 * result + (endpointState != null ? endpointState.hashCode() : 0);
        result = 29 * result + (policyLastUpdated != null ? policyLastUpdated.hashCode() : 0);
        result = 29 * result + (complianceLevel != null ? complianceLevel.hashCode() : 0);
	result = 29 * result + (url != null ? url.hashCode() : 0);
        return result;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }
}
