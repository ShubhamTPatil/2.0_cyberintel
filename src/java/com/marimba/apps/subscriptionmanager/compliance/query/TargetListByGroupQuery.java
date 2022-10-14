// Copyright 1997-2011, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.query;

/**
 * Target List by group
 *
 * @author  Selvaraj Jegatheesan
 * @version $Revision$, $Date$
 */

import java.sql.SQLException;
import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceResult;
import com.marimba.apps.subscriptionmanager.compliance.result.TargetListByGroupResult;
import com.marimba.intf.msf.query.*;

public class TargetListByGroupQuery extends ComplianceListQueryObject {

    // Target id for the machine
    String targetName;

    public TargetListByGroupQuery(String targetId) {
	    this.targetName = (targetId == null) ? "" : targetId;
    }

    public int hashCode() {
	    return targetName.hashCode();
    }

    public boolean equals(Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
	    if (!(obj instanceof TargetListQuery)) return false;

	    TargetListQuery qry = (TargetListQuery) obj;
	    if (!targetName.equals(qry.getTargetName())) return false;

	    // Don't forget list result
	    if (getStartPage() != qry.getStartPage()) return false;
	    return true;
    }

    public String getTargetName() {
	    return targetName;
    }

    public String getQueryPath() {
	    return QUERY_FOLDER_PATH + "TargetsDetailsXGroup";
    }

    public String[] getQueryArgs() {
	    return new String[] {targetName};
    }

    public IComplianceResult getResult(IQueryResult rs) throws SQLException {
	    TargetListByGroupResult res = new TargetListByGroupResult();
	    setPageInfo(res);
	    res.fetch(rs);
	    res.initForCache(maxCache, cacheExt);

	    // Return result
	    return res;
    }
}
