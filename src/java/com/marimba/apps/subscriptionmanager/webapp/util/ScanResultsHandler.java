// Copyright 2019-2022, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.webapp.forms.ScanResultsForm;

/**
 * ScanResultsHandler Class w.r.t fetch run scan results.
 *
 * @author insthayyur
 * @version: $Date$, $Revision$
 */

public class ScanResultsHandler {

	SubscriptionMain main;

	public ScanResultsHandler(SubscriptionMain main) {
		this.main = main;
	}

	public String getScanResultsData() {
		String ScanResultsJsonResponse = null;
		try {
			System.out.println("start - getScanResultsData() ");
			ScanResultsJsonResponse = new ScanResultsDetails.GetScanResultsDetails(main).getScanResultsData();
			System.out.println("END - getScanResultsData() final response ==> " + ScanResultsJsonResponse);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ScanResultsJsonResponse;
	}

	/**
	 * 
	 * @param formbean
	 */
	public String processScanResultsCLI(ScanResultsForm formbean) {

		String responseMsg = "ScanResults Handler method has been called ";

		System.out.println(" ScanResults Form bean for CLI is received :" + formbean);
		List<String> endDeviceList = Arrays.asList(formbean.getEndDevicesArr());
		if (endDeviceList.size() > 0) {
			//Logic for Export to excel functionality
			ScanResults4SelectedDevices(endDeviceList);
		} else {
			responseMsg = "No end devices are selected for ScanResults";
		}
		return responseMsg;
	}

	/**
	 * 
	 * @param ipPortList
	 */
	public void ScanResults4SelectedDevices(List<String> ipPortList) {

		System.out.println("Logic inside ScanResults4SelectedDevices()");
}
}