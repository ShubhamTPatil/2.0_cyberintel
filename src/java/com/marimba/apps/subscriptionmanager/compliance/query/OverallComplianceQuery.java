// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.query;

/**
 * Overall compliance query/report
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */

import java.sql.SQLException;

import java.util.*;

import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceResult;
import com.marimba.apps.subscriptionmanager.compliance.result.ComplianceSummaryResult;
import com.marimba.apps.subscriptionmanager.compliance.view.ComplianceSummaryBean;
import com.marimba.apps.subscriptionmanager.compliance.util.WebUtil;
import com.marimba.intf.msf.query.*;

public class OverallComplianceQuery extends ComplianceQueryObject {

    // Query context
    String targetGroup;

    // Last check in limit
    int checkInLimit;

    public OverallComplianceQuery(String targetGroup) {
	    this.targetGroup = (targetGroup == null) ? "" : targetGroup;
    }
    
    public void setCheckinLimit(int limit) {
	    this.checkInLimit = limit;
    }

    public int hashCode() {
	    return targetGroup.hashCode();
    }

    public boolean equals(Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
	    if (!(obj instanceof OverallComplianceQuery)) return false;

	    OverallComplianceQuery qry = (OverallComplianceQuery) obj;
	    if (!targetGroup.equals(qry.getTargetGroups())) return false;

	    return true;
    }

    public String getTargetGroups() {
	    return targetGroup;
    }

    public String getQueryPath() {
	    return QUERY_FOLDER_PATH + "OverallSummaryByInventory";
    }

    public String[] getQueryArgs() {
	    Calendar c = Calendar.getInstance();
	    c.add(Calendar.HOUR, checkInLimit);
        String scanDateTime = WebUtil.getComplianceDateTimeFormat( userLocale ).format(c.getTime());
	    return new String[] {targetGroup, scanDateTime};
    }

    public IComplianceResult getResult(IQueryResult rs) throws SQLException {
	    ComplianceSummaryResult res = new ComplianceSummaryResult();
	    res.fetch(rs);
	    res.initForCache(maxCache, cacheExt);
	    return res;
    }

    // REMIND, we should be able to remove this
    public void getSummary(ComplianceSummaryBean summary){
        if(null != result) {
	        ComplianceSummaryResult csr = (ComplianceSummaryResult) result;
	        summary.setNoncompliant(csr.getNonCompliant());
	        summary.setNotcheckedin(csr.getNotCheckIn());
	        summary.setCompliant(csr.getCompliant());
	        summary.setTotal(csr.getNonCompliant() + csr.getNotCheckIn() + csr.getCompliant());
        }
    }    
}
