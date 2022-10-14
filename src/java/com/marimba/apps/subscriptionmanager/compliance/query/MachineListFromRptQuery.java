// Copyright 1997-2005, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.query;

import com.marimba.apps.subscriptionmanager.compliance.intf.*;
import com.marimba.apps.subscriptionmanager.compliance.result.*;
import com.marimba.intf.msf.query.*;

import java.sql.*;
import java.util.*;
import java.net.*;

/**
 * Query to get a list of machines based on a calculation id and compliance level
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */

public class MachineListFromRptQuery extends ComplianceListQueryObject{

    static HashMap map;
    static {
	    map = new HashMap();
	    map.put(ComplianceConstants.STR_LEVEL_COMPLIANT, "CompliantMachineReportByCID");
	    map.put(ComplianceConstants.STR_LEVEL_NON_COMPLIANT, "NonCompliantMachineReportByCID");
	    map.put(ComplianceConstants.STR_LEVEL_NOT_CHECK_IN, "NotCheckInMachineReportByCID");
    }

    public static String getRCURL(String level, int calculationId) {
	    return "/im/machine/query.do?pageType=result&treePath=" +
	    URLEncoder.encode(QUERY_FOLDER_PATH + (String) map.get(level)) +
	    "&extra_0=" + calculationId;
    }
    
    private int calculationId;
    private String complevel;

    public MachineListFromRptQuery(int calculationId , String complianceLevel) {
        this.calculationId = calculationId;
	    this.complevel = (complianceLevel == null) ? "" : complianceLevel;
    }

    public String getComplevel() {
        return complevel;
    }

    public int getCalculationId() {
	    return calculationId;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MachineListFromRptQuery)) return false;

        final MachineListFromRptQuery qry = (MachineListFromRptQuery) o;

        if (!complevel.equals(qry.complevel)) return false;
        if (calculationId != qry.calculationId) return false;
        if(startPage != qry.getStartPage()) return false;

        return true;
    }

    public int hashCode() {
        return complevel.hashCode() + calculationId + getStartPage();
    }

    public String getQueryPath() {
	    return QUERY_FOLDER_PATH + (String) map.get(complevel);
    }

    public String[] getQueryArgs() {
	    return new String[] {String.valueOf(calculationId)};
    }

    public IComplianceResult getResult(IQueryResult rs) throws SQLException {
	    MachineListFromRptResult res = new MachineListFromRptResult();
	    res.setComplianceLevel(complevel);
	    setPageInfo(res);
	    res.fetch(rs);
	    res.initForCache(maxCache, cacheExt);
	    return res;
    }
}
