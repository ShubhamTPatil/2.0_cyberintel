// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.result;

/**
 * List result for policies from subscription policy table
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Date;

import java.util.Collection;
import java.util.ArrayList;

import com.marimba.apps.subscriptionmanager.compliance.view.PackagePolicyDetails;
import com.marimba.intf.msf.query.IQueryResult;

public class PolicyListResult extends ListResult {

    /**
     * Fetch each row
     */
    public void fetchRow(IQueryResult rs) throws SQLException {
	PackagePolicyDetails policy = new PackagePolicyDetails();
	policy.setTargetId(rs.getString("policyname"));
	policy.setTargetType(rs.getString("type"));
	policy.setPrimaryState(rs.getString("primary_state"));
	policy.setPolicyLastUpdated(rs.getDate("last_updated"));
    policy.setPackageType(rs.getString("content_type"));
    list.add(policy);
    }

}
