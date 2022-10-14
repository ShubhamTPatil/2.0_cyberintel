// Copyright 1997-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.approval;
/**
 * This class is used to store all policy properties 
 * for peer approval policy
 *
 * @author Selvaraj Jegatheesan
 */
public class ApprovalPropertyDTO {
	private int policyChannelId;
	private int policyChangeId;
	private int propertyId;
	private String propKey;
	private String propValue;
	private String propType;
	private int propAction;
	
	public ApprovalPropertyDTO() {
		
	}

	public int getPolicyChannelId() {
		return policyChannelId;
	}

	public void setPolicyChannelId(int policyChannelId) {
		this.policyChannelId = policyChannelId;
	}

	public int getPolicyChangeId() {
		return policyChangeId;
	}

	public void setPolicyChangeId(int policyChangeId) {
		this.policyChangeId = policyChangeId;
	}

	public int getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(int propertyId) {
		this.propertyId = propertyId;
	}

	public String getPropKey() {
		return propKey;
	}

	public void setPropKey(String propKey) {
		this.propKey = propKey;
	}

	public String getPropValue() {
		return propValue;
	}

	public void setPropValue(String propValue) {
		this.propValue = propValue;
	}

	public String getPropType() {
		return propType;
	}

	public void setPropType(String propType) {
		this.propType = propType;
	}

	public int getPropAction() {
		return propAction;
	}

	public void setPropAction(int propAction) {
		this.propAction = propAction;
	}
	
	
}
