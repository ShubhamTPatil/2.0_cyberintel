// Copyright 2019-2022, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.*;
import com.marimba.apps.securitymgr.db.DatabaseAccess;
import com.marimba.apps.securitymgr.db.QueryExecutor;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.intf.db.IStatementPool;

/**
 * Implementing run scan details feature responsible to fetch run scan metadata from database.
 *
 * @author inmkaklij
 * @version: $Date$, $Revision$
 */

public class RunScanDetails {

	public static class GetRunScanDetails extends DatabaseAccess {

		SubscriptionMain main = null;
		String runScanJsonResponse = null;

		public GetRunScanDetails(SubscriptionMain main) {
			GetRunScanData result = new GetRunScanData(main);

			try {				
				runQuery(result);
				runScanJsonResponse = result.getRunScanData();				
			} catch (Exception dae) {
				dae.printStackTrace();
			}
		}

		public String getRunScanData() {
			return runScanJsonResponse;

		}
	}

	static class GetRunScanData extends QueryExecutor {
		String runScanJsonResponse;

		public GetRunScanData(SubscriptionMain main) {
			super(main);
		}

		protected void execute(IStatementPool pool) throws SQLException {

			
			String masterQuery = "select inv_machine.name,inv_machine.id,address,rpcport,scantime from inv_machine,inv_network,inv_tuner\r\n"
					+ "where inv_machine.id = inv_tuner.machine_id\r\n"
					+ "and inv_tuner.machine_id = inv_network.machine_id";
			ResultSet childQuery1Rs = null;
			ResultSet rs = null;
			ResultSet childQuery2Rs = null;
			try {
				PreparedStatement masterQueryRs = pool.getConnection().prepareStatement(masterQuery);

			
				ScanResultResponse scanResultResponseObject = new ScanResultResponse();

				// Data Object
				Data dataObject = new Data();
				rs = masterQueryRs.executeQuery();
				System.out.println("Rs size : " + rs.getFetchSize());
				// Machine List object
				List<Machine> MachineListObject = new ArrayList<Machine>();
				while (rs.next()) {
					Machine machineObject = new Machine();
					machineObject.setMachineId(rs.getString("id"));
					machineObject.setMachineName(rs.getString("name"));
					machineObject.setMachineLastScan(rs.getString("scantime"));

					MachineListObject.add(machineObject);
				}

				String childQuery1 = "Select top 1 modified,cvss_time from product_cve_info";
				childQuery1Rs = masterQueryRs.executeQuery(childQuery1);
				
				
				while (childQuery1Rs.next()) {
					dataObject.setcVELastUpdated(childQuery1Rs.getString("modified"));
					dataObject.setVulDefLastUpdated(childQuery1Rs.getString("cvss_time"));
				}

				String childQuery2 = "Select top 1 modified_date from security_cve_info";
				childQuery2Rs = masterQueryRs.executeQuery(childQuery2);
				
				
				while (childQuery2Rs.next()) {
					dataObject.setSecDefLastUpdated(childQuery2Rs.getString("modified_date"));
				}
				dataObject.setMachineList(MachineListObject);

				Metadata metadataObject = new Metadata();
				metadataObject.setId("http://azuretx.marimbacastanet.com:8888/marimba/runScanHome");
				metadataObject.setType("runScanHomeService");
				metadataObject.setUri("http://azuretx.marimbacastanet.com:8888/marimba/runScanHome");

				dataObject.setMetadata(metadataObject);
				scanResultResponseObject.setData(dataObject);
				Gson gson = new Gson();
				runScanJsonResponse = gson.toJson(scanResultResponseObject);
			} finally {
				rs.close();
				childQuery1Rs.close();
				childQuery2Rs.close();
			}
		}

		public String getRunScanData() {
			return this.runScanJsonResponse;
		}
	}
}