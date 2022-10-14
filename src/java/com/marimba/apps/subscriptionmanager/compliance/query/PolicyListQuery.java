// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.query;

/**
 * Query policy list from subscription policy table given a package url
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

import java.util.Map;

import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;
import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceListResult;
import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceResult;
import com.marimba.apps.subscriptionmanager.compliance.result.PolicyListResult;
import com.marimba.apps.subscriptionmanager.compliance.result.PackageListResult;
import com.marimba.apps.subscriptionmanager.compliance.core.DbSourceManager;
import com.marimba.intf.msf.query.*;

public class PolicyListQuery extends ComplianceListQueryObject {

    String pkgUrl;

    public PolicyListQuery(String url) {
        this.pkgUrl = (url == null) ? "" : url;
    }

    public String getPkgUrl() {
        return pkgUrl;
    }

    public int hashCode() {
        return pkgUrl.hashCode();
    }

    public boolean equals(Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
	    if (!(obj instanceof PolicyListQuery)) return false;

	    PolicyListQuery qry = (PolicyListQuery) obj;
	    if (!pkgUrl.equals(qry.getPkgUrl())) return false;

	    return true;
    }

    public String getQueryPath() {
	    return QUERY_FOLDER_PATH +"TargetsByPackage";
    }

    public String[] getQueryArgs() {
	    return  new String[] {pkgUrl};
    }

    public IComplianceResult getResult(IQueryResult rs) throws SQLException {
        PolicyListResult plr = new PolicyListResult();
	    setPageInfo(plr);
	    plr.fetch(rs);
	    plr.initForCache(maxCache, cacheExt);
        return plr;
    }
}
