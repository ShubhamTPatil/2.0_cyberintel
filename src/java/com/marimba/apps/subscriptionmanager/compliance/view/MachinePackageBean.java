// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.view;

import com.marimba.apps.subscriptionmanager.compliance.util.WebUtil;

import java.util.Date;
import java.text.SimpleDateFormat;
/**
 * Single machine&package compliance data
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */

public class MachinePackageBean {
    
    String url;
    String title;
    String policy;
    String policyName;
    String policyTargetType;
    String packageState;
    String endPointState;
    String complianceLevel;
    Date pkgPublishedTime;
    String content_type;

    public MachinePackageBean() {
	url = "";
	policy = "";
	policyName = "";
	packageState = "";
	endPointState = "";
	complianceLevel = "";
    content_type = "";
    }

    public void setPkgPublishedTime( java.sql.Date pkgPublishedTime ){
        this.pkgPublishedTime = pkgPublishedTime;
    }

    public String getPkgPublishedTime(){
        if( pkgPublishedTime != null ){
            return WebUtil.getComplianceDateFormat().format( pkgPublishedTime );
        }
        return null;
    }

    public String getTitle() {
	    return title;
    }

    public void setTitle( String title ) {
	this.title = title;
    }

    public String getUrl() {
	return url;
    }

    public void setUrl(String url) {
	this.url = (url == null) ? "" : url;
    }

    public String getContent_type() {
        return content_type;
    }

    public void setContent_type( String content_type ) {
        this.content_type = content_type;
    }

    public String getPolicy() {
	return policy;
    }
    
    public String getPolicyName() {
	return policyName;
    }

    public void setPolicy(String policy) {
	this.policy = (policy == null) ? "" : policy;
	
	int ind1 = this.policy.indexOf(',');
	int ind2 = this.policy.indexOf('=');
	if (ind1 != -1 && ind2 != -1) {
	    policyName = this.policy.substring(ind2 + 1, ind1);
	} else {
	    policyName = this.policy;
	}
    }

    public void setPolicyTargetType( String policyTargetType ){
        this.policyTargetType = policyTargetType;
    }

    public String getPolicyTargetType(){
        return this.policyTargetType;
    }

    public String getPackageState() {
	return packageState;
    }

    public void setPackageState(String packageState) {
	this.packageState = (packageState == null) ? "" : packageState;
    }    
    
    public String getEndPointState() {
	return endPointState;
    }

    public void setEndPointState(String endPointState) {
	this.endPointState = (endPointState == null) ? "" : endPointState;
    }    
    
    public String getComplianceLevel() {
	return complianceLevel;
    }

    public void setComplianceLevel(String complianceLevel) {
	this.complianceLevel = (complianceLevel == null) ? "" : complianceLevel;
    }    
    
}
