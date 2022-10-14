//Copyright 1996-2014, BMC Software Inc. All Rights Reserved.
//Confidential and Proprietary Information of BMC Software Inc.
//Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
//6,381,631, and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager;

import com.marimba.apps.subscriptionmanager.compliance.core.ComplianceMain;
import com.marimba.intf.msf.ITenant;

/**
 * Tenant Attributes
 *
 * @author	Selvaraj Jegatheesan
 * @version 	$Revision$, $Date$
 */
public class TenantAttributes {
	ITenant tenant;
	SubscriptionMain tenantMain;
	String tenantName;
	ComplianceMain compMain;
	Exception initError;
	
	public TenantAttributes(ITenant curTenant) {
		this.tenant = curTenant;
	}

	public SubscriptionMain getTenantMain() {
		return tenantMain;
	}
	public void setTenantMain(SubscriptionMain tenantMain) {
		this.tenantMain = tenantMain;
	}

	public ComplianceMain getCompMain() {
		return compMain;
	}

	public void setCompMain(ComplianceMain compMain) {
		this.compMain = compMain;
	}

	public void setInitError(Exception ec) {
		this.initError = ec;
	}
	public Exception getInitError() {
		return initError;
	}
}
