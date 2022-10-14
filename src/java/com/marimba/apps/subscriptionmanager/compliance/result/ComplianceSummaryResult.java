// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.result;

/**
 * Result object for compliance summary (level:number)
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import com.opensymphony.oscache.base.EntryRefreshPolicy;
import com.opensymphony.oscache.base.CacheEntry;
import com.marimba.intf.msf.query.IQueryResult;

import java.sql.SQLException;
import java.sql.ResultSet;

public class ComplianceSummaryResult extends CachableResult {

    int numOfCompliant;
    int numOfNonCompliant;
    int numOfNotCheckIn;
    int numOfTotal;

    public ComplianceSummaryResult(int total) {
	this.numOfTotal = total;
	this.numOfCompliant = 0;
	this.numOfNonCompliant = 0;
	this.numOfNotCheckIn = 0;
    }

    public ComplianceSummaryResult(){
        this(0);
    }

    public ComplianceSummaryResult(int success, int failed, int notcheckedin) {
	// For testing purpose only
	this.numOfTotal = success + failed + notcheckedin;
	this.numOfCompliant = success;
	this.numOfNonCompliant = failed;
	this.numOfNotCheckIn = notcheckedin;
    }

    public void fetch(IQueryResult rs) throws SQLException {
	// Result consists of rows look like (compliance_level, number)
	// Not-check-in state is calculated by: total - nonCompliant - compliant

	while (rs.next()) {
	    int num = rs.getInt(1);
	    String level = rs.getString(2).trim();
	    if (STR_LEVEL_COMPLIANT.equals(level)) {
		numOfCompliant = num;
	    } else if (STR_LEVEL_NON_COMPLIANT.equals(level)) {
		numOfNonCompliant = num;
	    } else if(STR_TOTAL_MACHINES.equals(level)) {
        numOfTotal = num;    
        }
	}

	// Calculate the not-check-in level number
	numOfNotCheckIn = numOfTotal - numOfCompliant - numOfNonCompliant;
    }

    public int getCompliant() {
	return numOfCompliant;
    }

    public int getNonCompliant() {
	return numOfNonCompliant;
    }

    public int getNotCheckIn() {
	return numOfNotCheckIn;
    }

}
