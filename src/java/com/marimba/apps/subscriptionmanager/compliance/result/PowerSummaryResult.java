// Copyright 2009, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.result;

/**
 * Result object for power compliance summary (level:number)
 *
 * @author  Venkatesh Jeyaraman
 * @version $Revision$, $Date$
 */
import com.opensymphony.oscache.base.EntryRefreshPolicy;
import com.opensymphony.oscache.base.CacheEntry;
import com.marimba.intf.msf.query.IQueryResult;

import java.sql.SQLException;

public class PowerSummaryResult extends CachableResult {

    int powerNumOfCompliant;
    int powerNumOfNonCompliant;
    int powerNumOfNotCheckIn;
    int powerNumOfTotal;

    public PowerSummaryResult(int total) {
        this.powerNumOfCompliant = 0;
        this.powerNumOfNonCompliant = 0;
        this.powerNumOfNotCheckIn = 0;
        this.powerNumOfTotal = total;
    }

    public PowerSummaryResult(){
        this(0);
    }

    public void fetch(IQueryResult rs) throws SQLException {
	// Result consists of rows look like (compliance_level, number)
	// Not-check-in state is calculated by: total - nonCompliant - compliant

	while (rs.next()) {
	    int num = rs.getInt(1);
	    String level = rs.getString(2).trim();
        if (POWER_STR_LEVEL_COMPLIANT.equals(level)) {
            powerNumOfCompliant = num;
        } else if (POWER_STR_LEVEL_NON_COMPLIANT.equals(level)) {
            powerNumOfNonCompliant = num;
        } else if (POWER_STR_TOTAL_MACHINES.equals(level)) {
            powerNumOfTotal = num;
        }
	}

	// Calculate the not-check-in level number
    powerNumOfNotCheckIn = powerNumOfTotal - powerNumOfCompliant - powerNumOfNonCompliant;
    }

    public int getPowerCompliant() {
	    return powerNumOfCompliant;
    }

    public int getPowerNonCompliant() {
	    return powerNumOfNonCompliant;
    }

    public int getPowerNotCheckIn() {
	    return powerNumOfNotCheckIn;
    }
}
