// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.result;

/**
 * Single machine compliance result
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Date;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.List;

import com.marimba.apps.subscriptionmanager.compliance.view.MachinePackageBean;
import com.marimba.intf.msf.query.IQueryResult;

public class MachineComplianceResult extends ListResult {

    public void fetchRow(IQueryResult rs) throws SQLException {
	MachinePackageBean bean = new MachinePackageBean();

	bean.setUrl(rs.getString("url"));
	bean.setTitle(rs.getString("package_title"));
	
	bean.setPackageState(rs.getString("primary_state"));
	bean.setEndPointState(rs.getString("endpoint_state"));
    bean.setContent_type(rs.getString("content_type"));
    Timestamp scanTimeStamp = rs.getTimestamp("scantime");
    Date scanDate = new Date(scanTimeStamp.getTime());
    Timestamp lastPublishedStamp = rs.getTimestamp("lastpublished");
    Date publishedDate = new Date(lastPublishedStamp.getTime());
    bean.setPkgPublishedTime( publishedDate );
	

	// Set compliance level, special care for not check in state
	String complianceLevel = rs.getString("compliance_level");
	if (complianceLevel == null || "".equals(complianceLevel)) {
	    bean.setComplianceLevel(STR_LEVEL_NOT_CHECK_IN);
	} else if (publishedDate != null && scanDate != null && scanDate.before(publishedDate)) {
	    bean.setComplianceLevel(STR_LEVEL_NOT_CHECK_IN);	    
	} else {
	    bean.setComplianceLevel(complianceLevel);
	}
	bean.setPolicy(rs.getString("target_name"));
	bean.setPolicyTargetType(rs.getString("type"));
	    
	// Add to the list
	list.add(bean);
    }
}
