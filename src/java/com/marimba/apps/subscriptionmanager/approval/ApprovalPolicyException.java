// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$
package com.marimba.apps.subscriptionmanager.approval;
/**
 * This class is used to handle Exception for Peer Approval Policy 
 *
 * @author Selvaraj Jegatheesan
 */

public class ApprovalPolicyException extends Exception {
	/**
	 * Default Serial Version UID for ApprovalPolicyEception serialize class 
	 */
	private static final long serialVersionUID = 1L;
	private Throwable t;
	public ApprovalPolicyException() {
		super();
	}
	
	public ApprovalPolicyException(String msg) {
		super(msg);
	}
	public ApprovalPolicyException(Exception ex) {
		super();
		this.t = ex;
	}
	
	public ApprovalPolicyException(Exception ex, String msg) {
		super(msg);
		this.t = ex;
	}
	
	public Throwable getRootCause() {
		return t;
	}
	
	public void printStackTrace() {
		if(t != null) {
			System.err.println("Outer-level Exception");
			super.printStackTrace();
			System.err.println("Wrapped Exception");
			t.printStackTrace();
		} else{
			super.printStackTrace();
		}
	}
}
