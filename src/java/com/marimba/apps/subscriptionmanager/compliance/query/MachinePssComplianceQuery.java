// Copyright 2009, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.query;


/**
 * Single machine power compliance query
 *
 * @author Vallinayagam
 * @version 1.0 $Date$
 */

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.Map;

import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceQuery;
import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceResult;
import com.marimba.apps.subscriptionmanager.compliance.result.ErrorResult;
import com.marimba.apps.subscriptionmanager.compliance.result.MachineComplianceResult;
import com.marimba.apps.subscriptionmanager.compliance.result.MachinePssComplianceResult;
import com.marimba.apps.subscriptionmanager.compliance.core.DbSourceManager;
import com.marimba.intf.msf.query.*;

public class MachinePssComplianceQuery extends ComplianceListQueryObject {

    // Target id for the machine
    String targetId;

    public MachinePssComplianceQuery(String targetId) {
	    this.targetId = (targetId == null) ? "" : targetId;
    }

    public int hashCode() {
	    return targetId.hashCode();
    }

    public boolean equals(Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
	    if (!(obj instanceof MachinePssComplianceQuery)) return false;

	    MachinePssComplianceQuery qry = (MachinePssComplianceQuery) obj;
	    if (!targetId.equals(qry.getTargetId())) return false;

	    // Don't forget list result
	    if (getStartPage() != qry.getStartPage()) return false;
	    return true;
    }

    public String getTargetId() {
	    return targetId;
    }

    public String getQueryPath() {        
	return QUERY_FOLDER_PATH + "PowerMachineComplianceDetail";
    }

    public String[] getQueryArgs() {
	    return new String[] {targetId};
    }

    public IComplianceResult getResult(IQueryResult rs) throws SQLException {
	    MachinePssComplianceResult res = new MachinePssComplianceResult();
	    setPageInfo(res);
	    res.fetch(rs);
	    res.initForCache(maxCache, cacheExt);
	
	    // Return result
	    return res;
    }
}
