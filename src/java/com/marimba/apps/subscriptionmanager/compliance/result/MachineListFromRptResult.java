// Copyright 1997-2005, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.result;

import com.marimba.apps.subscriptionmanager.compliance.intf.*;
import com.marimba.apps.subscriptionmanager.compliance.view.*;
import com.marimba.intf.msf.query.IQueryResult;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.text.SimpleDateFormat;


/**
 * List that holds list of machines
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */

public class MachineListFromRptResult extends ListResult{

    private String complevel;
    private RowFetcher rowFetcher;

    public void setComplianceLevel(String level) {
	this.complevel = level;
	if (ComplianceConstants.STR_LEVEL_COMPLIANT.equals(level)) {
	    rowFetcher = new CompliantFetcher();
	} else if (ComplianceConstants.STR_LEVEL_NOT_CHECK_IN.equals(level)) {
	    rowFetcher = new NotCheckInFetcher();
	} else {
	    rowFetcher = new NonComplianceFetcher();
	}
    }

    public void fetchRow(IQueryResult rs) throws SQLException {
	rowFetcher.fetchRow(rs);
    }

    public abstract class RowFetcher {
	public abstract void fetchRow(IQueryResult rs) throws SQLException;
    }

    public class CompliantFetcher extends RowFetcher {

	public void fetchRow(IQueryResult rs) throws SQLException {
	    MachineBean mb = new MachineBean(rs.getString("machine_name"));
	    Timestamp scanTimeStamp = rs.getTimestamp("scantime");
		Date scanTime = new Date(scanTimeStamp.getTime());
	    mb.setLastCheckIn(scanTime);
	    list.add(mb);
	}
	
    }

    public class NonComplianceFetcher extends RowFetcher {

	public void fetchRow(IQueryResult rs) throws SQLException {
	    MachineBean mb = new MachineBean(rs.getString("machine_name"));
	    mb.setEndpointState(rs.getString("endpoint_state"));
	    mb.setPolicyState(rs.getString("policy_state"));
	    Timestamp scanTimeStamp = rs.getTimestamp("scantime");
		Date scanTime = new Date(scanTimeStamp.getTime());
	    mb.setLastCheckIn(scanTime);
	    mb.setPolicyId(rs.getString("policy_name"));
	    mb.setPackageTitle(rs.getString("package_title"));
	    list.add(mb);
	}
    }

    public class NotCheckInFetcher extends RowFetcher {

	public void fetchRow(IQueryResult rs) throws SQLException {
		try{
		    MachineBean mb = new MachineBean(rs.getString("machine_name"));
		    Timestamp scanTimeStamp = rs.getTimestamp("scantime");
			Date scanTime = new Date(scanTimeStamp.getTime());
		    mb.setLastCheckIn(scanTime);
		    mb.setPolicyState(rs.getString("policy_state"));
		    mb.setLastScanTime(scanTime);
		    mb.setPolicyId(rs.getString("policy_name"));
		    mb.setPackageTitle(rs.getString("package_title"));
		    Timestamp lastUpdatedTimeStamp = rs.getTimestamp("policy_lastupdated");
			Date lastUpdatedTime = new Date(lastUpdatedTimeStamp.getTime());
		    mb.setPolicyLastUpdated(lastUpdatedTime);
		    list.add(mb);
		}
		catch(Exception e){
			System.out.println("Failed to parse the policy last updated datetime");
		}
	}
    }

}
