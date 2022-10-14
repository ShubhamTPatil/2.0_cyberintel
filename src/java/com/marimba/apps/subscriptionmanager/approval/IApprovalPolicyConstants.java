// Copyright 1997-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.approval;

import com.marimba.apps.subscriptionmanager.intf.IPolicyDiffConstants;

/**
 * This class is used to store Approval Policy transactions constants 
 *
 * @author Selvaraj Jegatheesan
 */
public interface IApprovalPolicyConstants extends IPolicyDiffConstants {
	// Overall policy status
	int POLICY_PENDING = 500;
	int POLICY_APPROVED = 501;
	int POLICY_REJECTED = 502;
	int POLICY_CANCELLED = 503;
	int POLICY_PENDING_COPYOPERATION = 504; // special flag for copy operation. Because of when copying operation old policy removed and new policy created
	// Policy operation like add/modify/delete policy,channel,channel props and tuner props
	int ADD_OPERATION = 100;
	int MODIFY_OPERATION = 200;
	int DELETE_OPERATION = 300;

    String GET_POLICY_DETAILS = "policyDetails";
}
