// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.view;

import com.marimba.apps.subscriptionmanager.compliance.util.WebUtil;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
/**
 * Single machine&package compliance data
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */

public class MachinePssBean {

    String targetName;
    String complianceLevel;
    String policyName;
    String policyTargetType;
    Map pssMap = new TreeMap();

    public MachinePssBean() {
    }

   
    public String getComplianceLevel() {
	return complianceLevel;
    }

    public void setComplianceLevel(String complianceLevel) {
	this.complianceLevel = (complianceLevel == null) ? "" : complianceLevel;
    }

    public void setTargetName( String targetName ) {
	this.targetName = targetName;
    }

    public String getTargetName() {
	return targetName;
    }


    public void setPolicyName(String targetName) {
	this.targetName = (targetName == null) ? "" : targetName;

	int ind1 = this.targetName.indexOf(',');
	int ind2 = this.targetName.indexOf('=');
	if (ind1 != -1 && ind2 != -1) {
	    policyName = this.targetName.substring(ind2 + 1, ind1);
	} else {
	    policyName = this.targetName;
	}
    }

    public String getPolicyName() {
	return policyName;
    }

    public void setPssMap(Map pssMap) {
	this.pssMap = pssMap;
    }

    public Map getPssMap() {
	return pssMap;
    }

    public void setPolicyTargetType( String policyTargetType ){
        this.policyTargetType = policyTargetType;
    }

    public String getPolicyTargetType(){
        return policyTargetType;      
    }


}
