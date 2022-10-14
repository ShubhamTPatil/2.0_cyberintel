// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.result;

/**
 * Single policy compliance within a target group
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import com.opensymphony.oscache.base.EntryRefreshPolicy;
import com.opensymphony.oscache.base.CacheEntry;
import com.marimba.intf.msf.query.IQueryResult;

import java.sql.SQLException;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class PolicyComplianceResult extends CachableResult {

    int total;
    List pkgEntries;
    Map pkgMap;

    /**
     * Class to keep one package's compliance information
     */
    public class PolicyPackageEntry {
	String url;
	String policyState;

	int numOfTotal;
	int numOfCompliant;
	int numOfNonCompliant;

	public PolicyPackageEntry() {
	    url = null;
	    policyState = null;

	    numOfTotal = 0;
	    numOfNonCompliant = 0;
	    numOfCompliant = 0;
	}

	public String getUrl() {
	    return url;
	}

	public String getPolicyState() {
	    return policyState;
	}

	public int getCompliant() {
	    return numOfCompliant;
	}

	public int getNonCompliant() {
	    return numOfNonCompliant;
	}

	public int getNotCheckIn() {
	    return numOfTotal - numOfCompliant - numOfNonCompliant;
	}

	public void setTotal(int total) {
	    numOfTotal = total;
	}

	public void setCompliant(int compliant) {
	    numOfCompliant = compliant;
	}

	public void setNonCompliant(int nonCompliant) {
	    numOfNonCompliant = nonCompliant;
	}

	public void fetch(IQueryResult rs) throws SQLException {
	    int index = 1;
	    url = rs.getString(index++);
	    policyState = rs.getString(index++);
	}
    }
	
    public PolicyComplianceResult(int total) {
	this.total = total;
	this.pkgEntries = new ArrayList();
	this.pkgMap = new HashMap();
    }

    /**
     * Get list of packages this policy has
     */
    public void fetchPackages(IQueryResult rs) throws SQLException {
	while (rs.next()) {
	    PolicyPackageEntry entry = new PolicyPackageEntry();
	    entry.fetch(rs);
	    entry.setTotal(total);
	    pkgEntries.add(entry);
	    pkgMap.put(entry.getUrl(), entry);
	}
    }

    public void fetch(IQueryResult rs) throws SQLException {
	// Policy compliance detail
	while (rs.next()) {
	    // url, compliance_level, int
	    String url = rs.getString(1);
	    String complianceLevel = rs.getString(2);
	    int num = rs.getInt(3);

	    PolicyPackageEntry entry = (PolicyPackageEntry) pkgMap.get(url);
	    if (entry != null) {
		if (STR_LEVEL_COMPLIANT.equals(complianceLevel)) {
		    entry.setCompliant(num);
		} else if (STR_LEVEL_NON_COMPLIANT.equals(complianceLevel)) {
		    entry.setNonCompliant(num);
		}
	    } else {
		// REMIND, some error message here
	    }
	}

	// Don't need this any more
	pkgMap.clear();
    }

    public List getPackages() {
	return pkgEntries;
    }
}
