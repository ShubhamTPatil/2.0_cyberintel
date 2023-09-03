// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.forms;

import java.util.Map;
import org.apache.struts.action.ActionForm;

/**
 * Used to handle run scan results.
 *
 * @author Inmkaklij
 * @version 1.1, 09/25/2001
 */
public class RunScanForm extends ActionForm {

	private String runScanJson;
	private String action = null;
	private String responseMsg;
	private String endDevicesArr;

	private Map<String,String> machineScanResultMap;

	private String scanRespStatusThreadRunning;

	public String getScanRespStatusThreadRunning() {
		return scanRespStatusThreadRunning;
	}

	public void setScanRespStatusThreadRunning(String scanRespStatusThreadRunning) {
		this.scanRespStatusThreadRunning = scanRespStatusThreadRunning;
	}

	public Map<String, String> getMachineScanResultMap() {
		return machineScanResultMap;
	}

	public void setMachineScanResultMap(
			Map<String, String> machineScanResultMap) {
		this.machineScanResultMap = machineScanResultMap;
	}

	public String getRunScanJson() {
		return runScanJson;
	}

	public void setRunScanJson(String runScanJson) {
		this.runScanJson = runScanJson;
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



}
