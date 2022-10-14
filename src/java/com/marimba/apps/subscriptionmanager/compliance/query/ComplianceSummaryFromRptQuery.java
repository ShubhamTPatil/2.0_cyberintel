package com.marimba.apps.subscriptionmanager.compliance.query;

import com.marimba.apps.subscriptionmanager.compliance.intf.*;
import com.marimba.apps.subscriptionmanager.compliance.intf.*;
import com.marimba.apps.subscriptionmanager.compliance.result.ComplianceSummaryFromRptResult;
import com.marimba.apps.subscriptionmanager.compliance.view.ComplianceSummaryBean;
import com.marimba.apps.subscriptionmanager.compliance.core.DbSourceManager;
import com.marimba.intf.msf.query.*;

import java.sql.*;
import java.util.*;

/**
 * Query Object to get Compliance Summary information from report
 *
 *  @author  Zheng Xia
 *  @version $Revision$, $Date$
 */
public class ComplianceSummaryFromRptQuery extends ComplianceQueryObject {

    int calculationId;

    public ComplianceSummaryFromRptQuery(int calculationId) {
	    this.calculationId = calculationId;
    }

    public int getCalculationId() {
	    return calculationId;
    }

    public boolean equals(Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
	    if (!(obj instanceof ComplianceSummaryFromRptQuery)) return false;
	    ComplianceSummaryFromRptQuery qry = (ComplianceSummaryFromRptQuery) obj;
	    return qry.getCalculationId() == calculationId;
    }

    public String getQueryPath() {
	    return QUERY_FOLDER_PATH + "ComplianceSummaryReportByCID";
    }

    public String[] getQueryArgs() {
	    return new String[]{String.valueOf(calculationId)};
    }

    public IComplianceResult getResult(IQueryResult rs) throws SQLException {
	    ComplianceSummaryFromRptResult csr = new ComplianceSummaryFromRptResult();
	    csr.fetch(rs);
	    csr.initForCache(maxCache, cacheExt);
	    return csr;
    }
}
