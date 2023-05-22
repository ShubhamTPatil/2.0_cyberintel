// Copyright 2020-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.webapp.forms.RunScanForm;
import com.marimba.webapps.common.Resources;

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

	public String getRunScanData() {
		String runScanJsonResponse = null;
		try {
			runScanJsonResponse = new RunScanDetails.GetRunScanDetails(main).getRunScanData();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return runScanJsonResponse;
	}


}
