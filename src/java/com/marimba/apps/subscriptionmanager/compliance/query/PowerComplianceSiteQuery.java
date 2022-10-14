// Copyright 2013, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.query;
import java.sql.SQLException;

import java.util.*;

import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceResult;
import com.marimba.apps.subscriptionmanager.compliance.result.ComplianceSummaryResult;
import com.marimba.apps.subscriptionmanager.compliance.result.PowerSummaryResult;
import com.marimba.apps.subscriptionmanager.compliance.view.ComplianceSummaryBean;
import com.marimba.apps.subscriptionmanager.compliance.view.PowerSummaryBean;
import com.marimba.apps.subscriptionmanager.compliance.util.WebUtil;
import com.marimba.intf.msf.query.*;
/**
 * Power Site compliance query/report
 *
 * @author  Selvarj Jegatheesan
 * @version $Revision$, $Date$
 */

public class PowerComplianceSiteQuery extends ComplianceQueryObject {
	 // Query context
    String targetGroup;

    // Last check in limit
    int checkInLimit;

    public PowerComplianceSiteQuery(String targetGroup) {
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
	    if (!(obj instanceof PowerComplianceSiteQuery)) return false;

	    PowerComplianceSiteQuery qry = (PowerComplianceSiteQuery) obj;
	    if (!targetGroup.equals(qry.getTargetGroups())) return false;

	    return true;
    }

    public String getTargetGroups() {
	    return targetGroup;
    }

    public String getQueryPath() {
	    return QUERY_FOLDER_PATH + "PowerSummaryByPolicyBySiteTarget";
    }

    public String[] getQueryArgs() {
	    return new String[] {targetGroup};
    }

    public IComplianceResult getResult(IQueryResult rs) throws SQLException {
	    PowerSummaryResult res = new PowerSummaryResult();
	    res.fetch(rs);
	    res.initForCache(maxCache, cacheExt);
	    return res;
    }

    // REMIND, we should be able to remove this
    public void getSummary(PowerSummaryBean summary){
        if(null != result) {
	        PowerSummaryResult csr = (PowerSummaryResult) result;
	        summary.setPowerNoncompliant(csr.getPowerNonCompliant());
	        summary.setPowerNotcheckedin(csr.getPowerNotCheckIn());
	        summary.setPowerCompliant(csr.getPowerCompliant());
	        summary.setPowerTotal(csr.getPowerNonCompliant() + csr.getPowerNotCheckIn() + csr.getPowerCompliant());
        }
    }   
}
