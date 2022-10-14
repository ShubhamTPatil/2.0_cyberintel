// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.result;

/**
 * Result object for a report header information
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import com.marimba.apps.subscriptionmanager.compliance.view.ReportBean;
import com.marimba.intf.msf.query.IQueryResult;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class ReportSummaryResult extends CachableResult {

    ReportBean res;
    
    public ReportSummaryResult() {
	res = new ReportBean();
    }

    public void fetch(IQueryResult rs) throws SQLException {
	if (rs.next()) {
	    res.setTarget(rs.getString("target_name"));
	    res.setCalculationId(rs.getInt("calculation_id"));
	    Timestamp startTimeStamp = rs.getTimestamp("start_time");
		Date startTime = new Date(startTimeStamp.getTime());
		res.setStartTime(startTime);
		Timestamp endTimeStamp = rs.getTimestamp("end_time");
		Date endTime = new Date(endTimeStamp.getTime());
		res.setEndTime(endTime);
	}
    }

    public ReportBean getReportSummary() {
	return res;
    }
}
