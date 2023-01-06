// Copyright 2019-2022, Harman International. All Rights Reserved.
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

	private static final String BCAC_HOME = "BCAC_HOME";
	private static final String RUN_SCAN_CLI_PROPERTIES = "runScanCli.properties";
	private static final String V_INSPECTOR_CHANNEL_URL = "vInspector.channel.url";

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
		Resources prop = new Resources(RUN_SCAN_CLI_PROPERTIES);
		String vInspectorURL = prop.getString(V_INSPECTOR_CHANNEL_URL);
		System.out.println(" Environment variable :: " + System.getenv(BCAC_HOME));
		try {
			String marimbaSetupPath = System.getenv(BCAC_HOME);
			System.out.println(" marimba setup path :" + marimbaSetupPath);

			// get tunerAdmin URL from map.txt file located in marimba setup.

			String tunerAdminURL = getTunerAdminURL(marimbaSetupPath);

			if (null != tunerAdminURL && !tunerAdminURL.isEmpty()) {
				for (String ipPort : ipPortList) {

					System.out.println("Received IP:Port is ==> " + ipPort + " & \n vInspector URL : " + vInspectorURL);

					String runScanCommand = "runchannel " + tunerAdminURL + " -start -tuner " + ipPort + " -channel "
							+ vInspectorURL;

					ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/C", runScanCommand)
							.directory(new File(marimbaSetupPath + "\\Tuner\\"));

					System.out.println("Working Directory : " + builder.directory().getPath()
							+ "\n Command to execute ::" + builder.command().get(2));

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
