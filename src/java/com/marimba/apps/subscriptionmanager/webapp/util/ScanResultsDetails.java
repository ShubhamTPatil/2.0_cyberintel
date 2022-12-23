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
 * @author insthayyur
 * @version: $Date$, $Revision$
 */

public class ScanResultsDetails {

	public static class GetScanResultsDetails extends DatabaseAccess {

		SubscriptionMain main = null;
		String ScanResultsJsonResponse = null;

		public GetScanResultsDetails(SubscriptionMain main) {

			System.out.println("START - GetScanResultsDetails() Constructor");
			GetScanResultsData result = new GetScanResultsData(main);

			try {
				System.out.println("GetScanResultsDetails - RUN Quey gets called ");
				runQuery(result);
				ScanResultsJsonResponse = result.getScanResultsData();
			} catch (Exception dae) {
				dae.printStackTrace();
			}
		}

		public String getScanResultsData() {
			return ScanResultsJsonResponse;

		}
	}

	static class GetScanResultsData extends QueryExecutor {
		String ScanResultsJsonResponse;

		public GetScanResultsData(SubscriptionMain main) {
			super(main);
		}

		protected void execute(IStatementPool pool) throws SQLException {

			System.out.println("ScanResults execute method gets called ");
			ResultSet masterRS = null;
			ResultSet masterRS2 = null;

			try {

				// DataScanResults Object
				DataScanResults dataObject = new DataScanResults();
				ScanResultResponseNew scanResultResponseObject = new ScanResultResponseNew();
				
				//------  Vulnerability Assessment-----Start-----//
				
				String masterQuery = "select DISTINCT machine_name as 'MachineName', machine_domain as 'MachineDomain',\r\n"
						+ "content_title as 'OS', content_name as 'SecurityDefinition', profile_name as 'Profile',\r\n"
						+ "finished_at as 'LastScan', overall_compliant_level as 'ScanStaus'\r\n"
						+ "from inv_security_oval_compliance t1 where upper(machine_name) like upper('%')\r\n"
						+ "and exists  (select 1 from ldapsync_targets_marimba ltm\r\n"
						+ "where ltm.marimba_table_primary_id = t1.machine_id)";

				PreparedStatement masterQueryPrStatement = pool.getConnection().prepareStatement(masterQuery);
				masterRS = masterQueryPrStatement.executeQuery();
				System.out.println("ScanResults - Vulnerability getFetchSize : " + masterRS.getFetchSize());

				// Machine List object
				@SuppressWarnings("unchecked")
				Set<MachineScanResults> machineSetObject = (Set<MachineScanResults>) new HashSet();
				while (masterRS.next()) {
					MachineScanResults machineObject = new MachineScanResults();

					// machineObject.setMachineId(rs.getString("id"));
					machineObject.setMachineName(masterRS.getString("MachineName"));
					machineObject.setMachineDomain(masterRS.getString("MachineDomain"));
					machineObject.setOs(masterRS.getString("OS"));
					machineObject.setSecurityDefinition(masterRS.getString("SecurityDefinition"));
					machineObject.setProfile(masterRS.getString("Profile"));
					machineObject.setMachineLastScan(masterRS.getString("LastScan"));
					machineObject.setScanStaus(masterRS.getString("ScanStaus"));
					machineSetObject.add(machineObject);
				}
				dataObject.setMachineList(machineSetObject);
				
				//------  Vulnerability Assessment-----End-----//
				
				//------  Configuration Assessment-----Start-----//
				String masterQuery2 = "select DISTINCT machine_name as 'MachineName', machine_domain as 'MachineDomain',\r\n"
						+ "content_title as 'OS', content_name as 'SecurityDefinition', profile_name as 'Profile',\r\n"
						+ "finished_at as 'LastScan', overall_compliant_level as 'ScanStaus'\r\n"
						+ "from inv_security_xccdf_compliance t1 where upper(machine_name) like upper('%')\r\n"
						+ "and exists  (select 1 from ldapsync_targets_marimba ltm\r\n"
						+ "where ltm.marimba_table_primary_id = t1.machine_id)";

				PreparedStatement masterQueryPrStatement2 = pool.getConnection().prepareStatement(masterQuery2);
				masterRS2 = masterQueryPrStatement2.executeQuery();
				System.out.println("ScanResults Configuration getFetchSize is : " + masterRS2.getFetchSize());

				// Machine List object
				@SuppressWarnings("unchecked")
				Set<MachineScanResults> machineSetObject2 = (Set<MachineScanResults>) new HashSet();
				while (masterRS2.next()) {
					MachineScanResults machineObject2 = new MachineScanResults();

					// machineObject.setMachineId(rs.getString("id"));
					machineObject2.setMachineName(masterRS2.getString("MachineName"));
					machineObject2.setMachineDomain(masterRS2.getString("MachineDomain"));
					machineObject2.setOs(masterRS2.getString("OS"));
					machineObject2.setSecurityDefinition(masterRS2.getString("SecurityDefinition"));
					machineObject2.setProfile(masterRS2.getString("Profile"));
					machineObject2.setMachineLastScan(masterRS2.getString("LastScan"));
					machineObject2.setScanStaus(masterRS2.getString("ScanStaus"));
					machineSetObject2.add(machineObject2);
				}
				dataObject.setMachineList2(machineSetObject2);
				//------  Configuration Assessment-----End-----//
				
				Metadata metadataObject = new Metadata();
				metadataObject.setId("http://azuretx.marimbacastanet.com:8888/marimba/ScanResultsHome");
				metadataObject.setType("runScanHomeService");
				metadataObject.setUri("http://azuretx.marimbacastanet.com:8888/marimba/ScanResultsHome");

				dataObject.setMetadata(metadataObject);
				scanResultResponseObject.setData(dataObject);
				Gson gson = new Gson();
				ScanResultsJsonResponse = gson.toJson(scanResultResponseObject);
			} finally {
				if (null != masterRS && null != masterRS2) {
					masterRS.close();
					masterRS2.close();
				}
			}
		}

		public String getScanResultsData() {
			return this.ScanResultsJsonResponse;
		}
	}
}