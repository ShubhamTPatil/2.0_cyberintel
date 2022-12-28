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
				
				String masterQuery = "select * from (\n" +
                        "    select DISTINCT machine_name as 'MachineName', os.product as 'OS', \n" +
                        "content_title  as 'SecurityDefinition', profile_title as 'Profile', \n" +
                        "(select (cast(a+(x.machine_id) as varchar(8)) + ' / ' + cast(b+(x.machine_id) as varchar(8))) as c from \n" +
                        "    (select machine_id,case when count(machine_id) = 0 then '0\\0' else count(machine_id) end a\n" +
                        "    from inv_security_oval_compliance where overall_compliant_level='COMPLIANT' \n" +
                        "    and machine_id = t1.machine_id  group by machine_id) x,\n" +
                        "    (select machine_id, case when count(machine_id) = 0 then '0\\0' else count(machine_id) end b\n" +
                        "    from inv_security_xccdf_compliance where machine_id = t1.machine_id  group by machine_id) y  \n" +
                        "    where x.machine_id=y.machine_id group by x.machine_id,a,b) as Compliance, \n" +
                        "finished_at as 'LastScan' \n" +
                        "from inv_security_oval_compliance t1,inv_os os where t1.machine_id=os.machine_id and\n" +
                        "upper(machine_name) like upper('%') \n" +
                        "and exists  (select 1 from ldapsync_targets_marimba ltm\n" +
                        "    where ltm.marimba_table_primary_id = t1.machine_id)) d where Compliance IS NOT NULL";

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

                    /* as of now commented by inian new query
					if(masterRS.getString("MachineDomain")!=null && masterRS.getString("MachineDomain")!="") {
						machineObject.setMachineDomain(masterRS.getString("MachineDomain"));		
					} else {
						machineObject.setMachineDomain("Not Available");	
					}
				     */

					machineObject.setOs(masterRS.getString("OS"));
					machineObject.setSecurityDefinition(masterRS.getString("SecurityDefinition"));
					machineObject.setProfile(masterRS.getString("Profile"));
                    machineObject.setScanStaus(masterRS.getString("Compliance"));
                    machineObject.setMachineLastScan(masterRS.getString("LastScan"));
					machineSetObject.add(machineObject);
				}
				dataObject.setMachineList(machineSetObject);
				
				//------  Vulnerability Assessment-----End-----//
				
				//------  Configuration Assessment-----Start-----//
				String masterQuery2 = "select * from (\n" +
                        "    select DISTINCT machine_name as 'MachineName', os.product as 'OS', \n" +
                        "content_title  as 'SecurityDefinition', profile_title as 'Profile', \n" +
                        "(select (cast(a+(x.machine_id) as varchar(8)) + ' / ' + cast(b+(x.machine_id) as varchar(8))) as c from \n" +
                        "    (select machine_id,case when count(machine_id) = 0 then '0\\0' else count(machine_id) end a\n" +
                        "    from inv_security_xccdf_compliance where overall_compliant_level='COMPLIANT' \n" +
                        "    and machine_id = t1.machine_id  group by machine_id) x,\n" +
                        "    (select machine_id, case when count(machine_id) = 0 then '0\\0' else count(machine_id) end b\n" +
                        "    from inv_security_xccdf_compliance where machine_id = t1.machine_id  group by machine_id) y  \n" +
                        "    where x.machine_id=y.machine_id group by x.machine_id,a,b) as Compliance, \n" +
                        "finished_at as 'LastScan' \n" +
                        "from inv_security_xccdf_compliance t1,inv_os os where t1.machine_id=os.machine_id and\n" +
                        "upper(machine_name) like upper('%') \n" +
                        "and exists  (select 1 from ldapsync_targets_marimba ltm\n" +
                        "    where ltm.marimba_table_primary_id = t1.machine_id)) d where Compliance IS NOT NULL";

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

                    /* // as of now commented by inian new query
					if(masterRS2.getString("MachineDomain")!=null && masterRS2.getString("MachineDomain")!="") {
						machineObject2.setMachineDomain(masterRS2.getString("MachineDomain"));		
					} else {
						machineObject2.setMachineDomain("Not Available");	
					}
					*/
					machineObject2.setOs(masterRS2.getString("OS"));
					machineObject2.setSecurityDefinition(masterRS2.getString("SecurityDefinition"));
					machineObject2.setProfile(masterRS2.getString("Profile"));
                    machineObject2.setScanStaus(masterRS2.getString("Compliance"));
					machineObject2.setMachineLastScan(masterRS2.getString("LastScan"));

					machineSetObject2.add(machineObject2);
				}
				dataObject.setMachineList2(machineSetObject2);
				//------  Configuration Assessment-----End-----//
				//commented by awadhesh
//				Metadata metadataObject = new Metadata();
//				metadataObject.setId("http://azuretx.marimbacastanet.com:8888/marimba/ScanResultsHome");
//				metadataObject.setType("runScanHomeService");
//				metadataObject.setUri("http://azuretx.marimbacastanet.com:8888/marimba/ScanResultsHome");
//
//				dataObject.setMetadata(metadataObject);

				scanResultResponseObject.setData(dataObject);
				Gson gson = new Gson();
				ScanResultsJsonResponse = gson.toJson(scanResultResponseObject);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			finally {
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