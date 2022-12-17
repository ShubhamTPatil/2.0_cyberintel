// Copyright 2019-2022, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.util;



import com.marimba.apps.subscriptionmanager.SubscriptionMain;

/**
 * RunScanHandler Class w.r.t fetch run scan results.
 *
 * @author Inmkaklij
 * @version: $Date$, $Revision$
 */

public class RunScanHandler {

	SubscriptionMain main;

	public RunScanHandler(SubscriptionMain main) {
		this.main = main;
	}

	// Controller for total number of machine
	public String getRunScanData() {
		String runScanJsonResponse = null;
		try {
			runScanJsonResponse = new RunScanDetails.GetRunScanData(main).getRunScanData();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return runScanJsonResponse;
	}

}
