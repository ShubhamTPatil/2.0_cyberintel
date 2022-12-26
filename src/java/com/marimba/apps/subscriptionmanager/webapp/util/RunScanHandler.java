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
import com.marimba.apps.subscriptionmanager.webapp.forms.RunScanForm;

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
			System.out.println("start - getRunScanData() ");
			runScanJsonResponse = new RunScanDetails.GetRunScanDetails(main).getRunScanData();
			System.out.println("END - getRunScanData() ");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return runScanJsonResponse;
	}

	/**
	 * 
	 * @param formbean
	 */
	public String processRunScanCLI(RunScanForm formbean) {

		String responseMsg = "Runscan action CLI execute method has been called ";

		if (null != formbean && null != formbean.getEndDevicesArr() && !formbean.getEndDevicesArr().isEmpty()) {
			String[] ipPortArr = formbean.getEndDevicesArr().split(",");

			List<String> endDeviceList = Arrays.asList(ipPortArr);
			if (endDeviceList.size() > 0) {
				scanSelectedEndDevices(endDeviceList);
			} else {
				responseMsg = "No end devices are selected for scan";
			}
		} else {
			System.out.println(" Run scan value is not received in form bean");
		}
		return responseMsg;
	}

	/**
	 * 
	 * @param ipPortList
	 */
	public void scanSelectedEndDevices(List<String> ipPortList) {

		// Syntax:>> runchannel.exe
		// <Take TunerAdministrator of CMS from map.txt>
		// -start -tuner <List of machines having format<IP:RCP port>>
		// -channel <VInspector URL>.
		System.out.println(" Environment variable :: " + System.getenv("BCAC_HOME"));
		try {
			String marimbaSetupPath = System.getenv("BCAC_HOME");
			String driveName = marimbaSetupPath.split(":")[0];
			System.out.println(" map txt file path : " + marimbaSetupPath + " \n Drive ID : " + driveName);

			// get tunerAdmin URL from map.txt file located in marimba setup.

			String tunerAdminURL = getTunerAdminURL(marimbaSetupPath);

			if (null != tunerAdminURL && !tunerAdminURL.isEmpty()) {
				for (String ipPort : ipPortList) {

					System.out.println("Received IP:Port is ==> " + ipPort);
					String runScanCommand = "runchannel " + tunerAdminURL + " -start -tuner " + ipPort
							+ " -channel http://defensight.marimbacastanet.com:5282/vInspector";

					System.out.println(" Command is ready to trigger scan:" + runScanCommand);

					ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/" + driveName,
							"cd " + marimbaSetupPath + "\\Tuner\\" + " && " + runScanCommand);

					System.out.println("Working Directory : " + builder.command().get(2));

					builder.redirectErrorStream(true);
					Process p = builder.start();
					BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line;
					while (true) {
						line = r.readLine();
						if (line == null) {
							break;
						}
						System.out.println(" Command execution result :: " + line);
					}
				}
			} else {
				System.out.println(" Tunner admin command line channel is not subscribed");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets tuner admin URL from map.txt file.
	 * 
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public String getTunerAdminURL(String filePath) throws Exception {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filePath.concat("\\Tuner\\.marimba\\Marimba\\map.txt")));
			String line = reader.readLine();

			while (line != null) {
				if (line.contains("TunerAdministrator")) {
					String tunerAdminURL = line.split("=")[0];
					return tunerAdminURL;
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			reader.close();
		}
		return "";
	}
}
