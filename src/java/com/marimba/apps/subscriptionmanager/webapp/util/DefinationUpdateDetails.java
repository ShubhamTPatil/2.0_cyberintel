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
 * Implementing DefinationUpdateDetails to fetch metadata from database.
 *
 * @author inmkaklij
 * @version: $Date$, $Revision$
 */

public class DefinationUpdateDetails {

	public static class getDefinationUpdateMetaData extends DatabaseAccess {

		SubscriptionMain main = null;
		String definationUpdateResponse = null;

		public getDefinationUpdateMetaData(SubscriptionMain main) {

			System.out.println("START - DefinationUpdateDetails() Constructor");
			GetDefinationUpdateMeataData result = new GetDefinationUpdateMeataData(main);

			try {
				System.out.println("DefinationUpdateDetails :: RUN Quey gets called ");
				runQuery(result);
				definationUpdateResponse = result.getDefinationUpdateMetaData();
			} catch (Exception dae) {
				dae.printStackTrace();
			}
		}

		public String getDefinationUpdateMetaData() {
			return definationUpdateResponse;

		}
	}

	static class GetDefinationUpdateMeataData extends QueryExecutor {
		String definationUpdateResponse;

		public GetDefinationUpdateMeataData(SubscriptionMain main) {
			super(main);
		}

		protected void execute(IStatementPool pool) throws SQLException {

			System.out.println("execute method gets called ");
			ResultSet masterRS = null;

			try {
				// Data Object
				DefinationUpdateData definationUpdateDataObject = new DefinationUpdateData();

				String masterQuery = "Select top 1 modified,cvss_time from product_cve_info";
				PreparedStatement masterQueryPrepareStatement = pool.getConnection().prepareStatement(masterQuery);
				masterRS = masterQueryPrepareStatement.executeQuery();
				System.out.println("masterQuery size : " + masterRS.getFetchSize());

				while (masterRS.next()) {
					definationUpdateDataObject.setcVELastUpdated(masterRS.getString("modified"));
					definationUpdateDataObject.setVulDefLastUpdated(masterRS.getString("cvss_time"));
				}
				
				Gson gson = new Gson();
				definationUpdateResponse = gson.toJson(definationUpdateDataObject);
			} finally {
				if (null != masterRS) {
					masterRS.close();
				}
			}
		}

		public String getDefinationUpdateMetaData() {
			return this.definationUpdateResponse;
		}
	}
}