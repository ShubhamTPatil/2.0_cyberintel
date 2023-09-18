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

			ResultSet masterRS = null;
			ResultSet cveInfoRs = null;
			ResultSet secDefLastUpdateRs = null;

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

				String cveInfoQuery = "Select top 1 modified,cvss_time from cve_info";
				PreparedStatement cveInfoPs = pool.getConnection().prepareStatement(cveInfoQuery);
				cveInfoRs = cveInfoPs.executeQuery();

				while (cveInfoRs.next()) {
					dataObject.setcVELastUpdated(cveInfoRs.getString("modified"));
					dataObject.setVulDefLastUpdated(cveInfoRs.getString("cvss_time"));
				}

				String secDefLastUpdateQuery = "SELECT TOP 1 modified_date FROM security_cve_info ORDER BY modified_date DESC";
				PreparedStatement secDefLastUpdatePs = pool.getConnection()
						.prepareStatement(secDefLastUpdateQuery);
				secDefLastUpdateRs = secDefLastUpdatePs.executeQuery();

				while (secDefLastUpdateRs.next()) {
					dataObject.setSecDefLastUpdated(secDefLastUpdateRs.getString("modified_date"));
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
				if (null != masterRS && null != cveInfoRs && null != secDefLastUpdateRs) {
					masterRS.close();
					cveInfoRs.close();
					secDefLastUpdateRs.close();
				}
			}
		}

		public String getRunScanData() {
			return this.runScanJsonResponse;
		}
	}
}