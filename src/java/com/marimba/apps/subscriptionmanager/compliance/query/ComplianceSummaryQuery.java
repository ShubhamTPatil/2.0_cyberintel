package com.marimba.apps.subscriptionmanager.compliance.query;

import com.marimba.apps.subscriptionmanager.compliance.intf.*;

import com.marimba.apps.subscriptionmanager.compliance.result.*;
import com.marimba.apps.subscriptionmanager.compliance.view.*;
import com.marimba.apps.subscriptionmanager.compliance.core.*;

import com.marimba.intf.msf.query.*;

import java.sql.*;
import java.util.*;

/**
 *  Query Object to get Compliance Summary information
 *
 *  @author Manoj Kumar
 *  @version $Revision$Date:
 */
public class ComplianceSummaryQuery extends ComplianceQueryObject {

    // Compliance summary based on target, policy and url
    String target;
    String policyname;
    String pkgName;

    public ComplianceSummaryQuery() {
	    target = "";
	    policyname ="";
	    pkgName = "";
    }

    public void setArgs(String _target, String _policy, String _pkg){
        target = (_target == null) ? "" : _target;
        policyname = (_policy == null) ? "" : _policy;
        pkgName = (_pkg == null) ? "" : _pkg;
    }

    public int hashCode() {
	    return target.hashCode() + policyname.hashCode() + pkgName.hashCode();
    }

    public boolean equals(Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
	    if (!(obj instanceof ComplianceSummaryQuery)) return false;

	    ComplianceSummaryQuery qry = (ComplianceSummaryQuery) obj;
	    if (!target.equals(qry.getTarget())) return false;
	    if (!policyname.equals(qry.getPolicyName())) return false;
	    if (!pkgName.equals(qry.getPkgName())) return false;
	
	    return true;
    }

    public String getPkgName() {
	    return pkgName;
    }

    public String getPolicyName() {
	    return policyname;
    }

    public String getTarget() {
	    return target;
    }

    public String getQueryPath() {
	    return QUERY_FOLDER_PATH + "SummaryByPolicyByTarget";
    }

    public String[] getQueryArgs() {
	    return new String[] {target, policyname, pkgName};
    }

    public IComplianceResult getResult(IQueryResult rs) throws SQLException {
        ComplianceSummaryResult csr = new ComplianceSummaryResult();
	    csr.fetch(rs);
	    csr.initForCache(maxCache, cacheExt);
        return csr;
    }

    // REMIND, we might not need this anymore
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
