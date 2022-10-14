// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.query;

/**
 * Query a list of cached reports
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import java.sql.SQLException;
import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceResult;
import com.marimba.apps.subscriptionmanager.compliance.result.ReportListResult;
import com.marimba.intf.msf.query.*;

public class ReportListQuery extends ComplianceListQueryObject {

    public boolean equals(Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
	    if (!(obj instanceof ReportListQuery)) return false;

	    ReportListQuery qry = (ReportListQuery) obj;
	    if (qry.getStartPage() != getStartPage()) return false;
	    //if (!(target.equals(qry.getTarget()))) return false;

	    return true;
    }

    public String getQueryPath() {
	    return QUERY_FOLDER_PATH +"ComplianceReportListByTarget";
    }

    public String[] getQueryArgs() {
        return new String[] { user.getName() };
    }

    public IComplianceResult getResult(IQueryResult rs) throws SQLException {
	    ReportListResult rlr = new ReportListResult();
	    setPageInfo(rlr);
	    rlr.fetch(rs);
	    rlr.initForCache(maxCache, cacheExt);
	    return rlr;
    }
}
