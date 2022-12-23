// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.forms;

import org.apache.struts.action.ActionForm;

/**
 * Used to handle run scan results.
 *
 * @author insthayyur
 * @version 1.1, 09/25/2001
 */
public class ScanResultsForm extends ActionForm {

	private String scanResultsJson;
	private String action = null;
	private String responseMsg;
	private String endDevicesArr;
	private String actionId;

	public String getScanResultsJson() {
		return scanResultsJson;
	}

	public void setScanResultsJson(String scanResultsJson) {
		this.scanResultsJson = scanResultsJson;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getResponseMsg() {
		return responseMsg;
	}

	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	public String getEndDevicesArr() {
		return endDevicesArr;
	}

	public void setEndDevicesArr(String endDevicesArr) {
		this.endDevicesArr = endDevicesArr;
	}
	public String getactionId() {
		return actionId;
	}

	public void setactionId(String actionId) {
		this.actionId = actionId;
	}
}
