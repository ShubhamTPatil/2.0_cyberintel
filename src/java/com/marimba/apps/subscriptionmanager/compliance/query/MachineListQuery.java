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
import com.marimba.apps.subscriptionmanager.compliance.util.WebUtil;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.intf.msf.query.*;

import java.sql.*;
import java.util.*;
import java.text.*;

/**
 * Query to get a list of machines based on a target and compliance level
 * The query result is based on inventory reported data only
 *
 * @author  Manoj Kumar
 * @version $Revision$, $Date$
 */

public class MachineListQuery extends ComplianceListQueryObject{

    static HashMap map;
    static {
	    map = new HashMap();
	    map.put(ComplianceConstants.STR_LEVEL_COMPLIANT, "OverallCompliantMachineByInventory");
	    map.put(ComplianceConstants.STR_LEVEL_NON_COMPLIANT, "OverallNonCompliantMachineByInventory");
	    map.put(ComplianceConstants.STR_LEVEL_NOT_CHECK_IN, "OverallNotCheckInMachineListByInventory");
        map.put(POWER_STR_LEVEL_COMPLIANT, "PowerCompliantMachineByTargetPolicy");
        map.put(POWER_STR_LEVEL_NON_COMPLIANT, "PowerNonCompliantMachineByTargetPolicy");
        map.put(POWER_STR_LEVEL_NOT_CHECK_IN, "PowerNotCheckInMachineByTargetPolicy");

    }

    int checkInLimit;
    String target_name;
    String complevel;

    public MachineListQuery(String targetName , String complianceLevel) {
        this.target_name =(targetName == null) ? "" : targetName;
	    this.complevel = (complianceLevel == null) ? "" : complianceLevel;
    }

    public void setCheckinLimit(int checkInLimit) {
	    this.checkInLimit = checkInLimit;
    }

    public String getComplevel() {
        return complevel;
    }

    public String getTarget_name() {
        return target_name;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MachineListQuery)) return false;

        final MachineListQuery machineListQuery = (MachineListQuery) o;

        if (!complevel.equals(machineListQuery.complevel)) return false;
        if (!target_name.equals(machineListQuery.target_name)) return false;
        if(startPage != machineListQuery.getStartPage()) return false;

	// Don't compare checkin limit here

        return true;
    }

    public int hashCode() {
        int result = target_name.hashCode();
        result = 29 * result + complevel.hashCode();
        return result + startPage;
    }

    public String getQueryPath() {
	    return QUERY_FOLDER_PATH + (String) map.get(complevel);
    }

    public String[] getQueryArgs() {
        if(complevel.startsWith(POWER_KEYWORD)) {
            return new String[] {target_name};
        }
	    Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.HOUR, checkInLimit);
        String scanDateTime = WebUtil.getComplianceDateTimeFormat( userLocale ).format(cal.getTime());
	    return new String[] {target_name, scanDateTime};
    }

    public IComplianceResult getResult(IQueryResult rs) throws SQLException {
	    MachineListResult res = new MachineListResult();
	    res.setComplianceLevel(complevel);
	    setPageInfo(res);
	    res.fetch(rs);
	    res.initForCache(maxCache, cacheExt);
	    return res;
    }
}
