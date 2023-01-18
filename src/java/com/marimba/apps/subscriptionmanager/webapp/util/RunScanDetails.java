// Copyright 2019-2022, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.*;
import com.marimba.apps.securitymgr.db.DatabaseAccess;
import com.marimba.apps.securitymgr.db.QueryExecutor;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.intf.db.IStatementPool;

/**
 * Implementing run scan details feature responsible to fetch run scan metadata
 * from database.
 *
 * @author inmkaklij
 * @version: $Date$, $Revision$
 */

public class RunScanDetails {

	public static class GetRunScanDetails extends DatabaseAccess {

		SubscriptionMain main = null;
		String runScanJsonResponse = null;

		public GetRunScanDetails(SubscriptionMain main) {

			System.out.println("START - GetRunScanDetails() Constructor");
			GetRunScanData result = new GetRunScanData(main);

			try {
				System.out.println("RUN Quey gets called ");
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

			System.out.println("GetRunScanData execute method gets called ");
			ResultSet masterRS = null;
			ResultSet subQuery1Rs = null;
			ResultSet subQuery2Rs = null;

			try {

				// Data Object
				Data dataObject = new Data();
				ScanResultResponse scanResultResponseObject = new ScanResultResponse();

				String masterQuery = "select distinct inv_machine.name,address,rpcport,scantime \r\n"
						+ "from inv_machine,inv_network,inv_tuner,ldapsync_targets_machines\r\n"
						+ "where inv_machine.id = inv_tuner.machine_id\r\n"
						+ "and inv_tuner.machine_id = inv_network.machine_id\r\n"
						+ "and inv_network.machine_id = ldapsync_targets_machines.machine_id";

				PreparedStatement masterQueryPrStatement = pool.getConnection().prepareStatement(masterQuery);
				masterRS = masterQueryPrStatement.executeQuery();
				System.out.println("masterQueryRs size : " + masterRS.getFetchSize());

				// Machine List object
				@SuppressWarnings("unchecked")
				Set<Machine> machineSetObject = (Set<Machine>) new HashSet();
				while (masterRS.next()) {
					Machine machineObject = new Machine();

					// machineObject.setMachineId(rs.getString("id"));
					machineObject.setMachineName(
							masterRS.getString("name").concat(":").concat(String.valueOf(masterRS.getInt("rpcport"))));
					machineObject.setMachineLastScan(masterRS.getString("scantime"));
					machineSetObject.add(machineObject);
				}

				String subQuery1 = "Select top 1 modified,cvss_time from product_cve_info";
				PreparedStatement subQuery1PrepareStatement = pool.getConnection().prepareStatement(subQuery1);
				subQuery1Rs = subQuery1PrepareStatement.executeQuery();
				System.out.println("subQuery1 size : " + subQuery1Rs.getFetchSize());

				while (subQuery1Rs.next()) {
					dataObject.setcVELastUpdated(subQuery1Rs.getString("modified"));
					dataObject.setVulDefLastUpdated(subQuery1Rs.getString("cvss_time"));
				}

				String subQuery2 = "Select top 1 modified_date from security_cve_info";
				PreparedStatement subQuery2PrepareStatement = pool.getConnection().prepareStatement(subQuery2);
				subQuery2Rs = subQuery2PrepareStatement.executeQuery();
				System.out.println("subQuery2Rs size : " + subQuery2Rs.getFetchSize());

				while (subQuery2Rs.next()) {
					dataObject.setSecDefLastUpdated(subQuery2Rs.getString("modified_date"));
				}
				dataObject.setMachineList(machineSetObject);

				Metadata metadataObject = new Metadata();
				metadataObject.setId("http://azuretx.marimbacastanet.com:8888/marimba/runScanHome");
				metadataObject.setType("runScanHomeService");
				metadataObject.setUri("http://azuretx.marimbacastanet.com:8888/marimba/runScanHome");

				dataObject.setMetadata(metadataObject);
				scanResultResponseObject.setData(dataObject);
				Gson gson = new Gson();
				runScanJsonResponse = gson.toJson(scanResultResponseObject);
			} finally {
				if (null != masterRS && null != subQuery1Rs && null != subQuery2Rs) {
					masterRS.close();
					subQuery1Rs.close();
					subQuery2Rs.close();
				}
			}
		}

		public String getRunScanData() {
			return this.runScanJsonResponse;
		}
	}
}