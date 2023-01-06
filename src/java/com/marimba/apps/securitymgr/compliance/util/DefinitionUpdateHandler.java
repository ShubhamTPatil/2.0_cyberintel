package com.marimba.apps.securitymgr.compliance.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.webapp.util.DefinationUpdateDetails;
import com.marimba.apps.subscriptionmanager.webapp.util.RunScanDetails;
import com.marimba.webapps.common.Resources;

/**
 * DefinationUpdateHandler Class w.r.t fetch meta data from DB
 *
 * @author inmkaklij
 *
 */

public class DefinitionUpdateHandler {

	SubscriptionMain main;

	public DefinitionUpdateHandler(SubscriptionMain main) {
		this.main = main;
	}

	public String getMetaData() {
		String runScanJsonResponse = null;
		try {
			System.out.println("start - getMetaData() ");
			runScanJsonResponse = new DefinationUpdateDetails.getDefinationUpdateMetaData(main)
					.getDefinationUpdateMetaData();
			System.out.println("END - getMetaData() ");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return runScanJsonResponse;
	}

	/**
	 * This method is responsible to load definition update metadata.
	 */
	public String loadDefinationUpdateMetaData() {

		return "";
	}
}
