package com.marimba.apps.subscriptionmanager.compliance.query;

import com.marimba.apps.subscriptionmanager.compliance.intf.*;
import com.marimba.apps.subscriptionmanager.compliance.intf.*;
import com.marimba.apps.subscriptionmanager.compliance.result.ReportSummaryResult;
import com.marimba.intf.msf.query.*;

import java.sql.*;

/**
 * Query object to get a report summary information
 *
 *  @author  Zheng Xia
 *  @version $Revision$, $Date$
 */
public class ReportSummaryQuery extends ComplianceQueryObject {

    int calculationId;

    public ReportSummaryQuery(int calculationId) {
	    this.calculationId = calculationId;
    }

    public int getCalculationId() {
	    return calculationId;
    }

    public boolean equals(Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
	    if (!(obj instanceof ReportSummaryQuery)) return false;
	    ReportSummaryQuery qry = (ReportSummaryQuery) obj;
	    return qry.getCalculationId() == calculationId;
    }

    public String getQueryPath() {
	    return QUERY_FOLDER_PATH + "ComplianceCacheHeaderInfo";
    }

    public String[] getQueryArgs() {
	    return new String[] {String.valueOf(calculationId)};
    }

    public IComplianceResult getResult(IQueryResult rs) throws SQLException {
	    ReportSummaryResult csr = new ReportSummaryResult();
	    csr.fetch(rs);
	    csr.initForCache(maxCache, cacheExt);
	    return csr;
    }
}
