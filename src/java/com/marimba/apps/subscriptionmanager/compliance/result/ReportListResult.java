// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.result;

/**
 * List result for cached reports
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Date;
import java.sql.Timestamp;

import java.util.Collection;
import java.util.ArrayList;

import com.marimba.apps.subscriptionmanager.compliance.view.ReportBean;
import com.marimba.apps.subscriptionmanager.compliance.service.ReportService;
import com.marimba.intf.msf.query.IQueryResult;

public class ReportListResult extends ListResult {

    /**
     * Fetch each row
     */
    public void fetchRow(IQueryResult rs) throws SQLException {
	ReportBean rpt = new ReportBean();
    int calcID = rs.getInt("calculation_id");
	rpt.setCalculationId(calcID);
	rpt.setTarget(rs.getString("target_name"));
	Timestamp startTimeStamp = rs.getTimestamp("start_time");
	Date startTime = new Date(startTimeStamp.getTime());
	rpt.setStartTime(startTime);
	Timestamp endTimeStamp = rs.getTimestamp("end_time");
	Date endTime = new Date(endTimeStamp.getTime());
	rpt.setEndTime(endTime);
    rpt.setHasCachedCompliance(true);
    
	list.add(rpt);
    }

}
