// Copyright 1997-2005, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.query;

import com.marimba.apps.subscriptionmanager.compliance.intf.*;
import com.marimba.apps.subscriptionmanager.compliance.result.*;
import com.marimba.apps.subscriptionmanager.compliance.core.DbSourceManager;

import com.marimba.intf.msf.query.*;

import java.sql.*;
import java.util.*;

/**
 * Query for GroupCompliance
 *
 * @author  Manoj Kumar
 * @version $Revision$, $Date$
 */

public class PackageListByGroupQuery extends ComplianceListQueryObject {
    
    // Group id for the machine
    String groupId;

    public PackageListByGroupQuery(String groupId) {
	    this.groupId = (groupId == null) ? "" : groupId;
    }

    public String getGroupId() {
	    return groupId;
    }

    public int hashCode() {
	    return groupId.hashCode();
    }

    public boolean equals(Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
	    if (!(obj instanceof PackageListByGroupQuery)) return false;

	    PackageListByGroupQuery qry = (PackageListByGroupQuery) obj;
	    if (!groupId.equals(qry.getGroupId())) return false;
	    if (startPage != qry.getStartPage()) return false;

	    return true;
    }

    public String getQueryPath() {
	    return QUERY_FOLDER_PATH + "PackagesByTarget";
    }

    public String[] getQueryArgs() {
	    return new String[] {groupId};
    }

    public IComplianceResult getResult(IQueryResult rs) throws SQLException {
	    PackageListByGroupResult res = new PackageListByGroupResult();
	    setPageInfo(res);
	    res.fetch(rs);
	    res.initForCache(maxCache, cacheExt);
	    return res;
    }
}
