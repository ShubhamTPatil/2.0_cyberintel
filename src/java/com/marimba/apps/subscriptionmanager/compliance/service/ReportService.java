package com.marimba.apps.subscriptionmanager.compliance.service;

import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;
import com.marimba.apps.subscriptionmanager.compliance.intf.ICacheManager;
import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceQuery;
import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceResult;
import com.marimba.apps.subscriptionmanager.compliance.report.TargetReport;
import com.marimba.apps.subscriptionmanager.compliance.report.DeleteReport;
import com.marimba.apps.subscriptionmanager.compliance.core.ComplianceMain;
import com.marimba.apps.subscriptionmanager.compliance.queue.ReportQueue;
import com.marimba.apps.subscriptionmanager.compliance.queue.QueryQueue;
import com.marimba.apps.subscriptionmanager.compliance.result.ReportListResult;
import com.marimba.apps.subscriptionmanager.compliance.result.ListResult;
import com.marimba.apps.subscriptionmanager.compliance.result.ReportSummaryResult;
import com.marimba.apps.subscriptionmanager.compliance.result.ComplianceSummaryFromRptResult;
import com.marimba.apps.subscriptionmanager.compliance.query.ReportListQuery;
import com.marimba.apps.subscriptionmanager.compliance.query.MachineListFromRptQuery;
import com.marimba.apps.subscriptionmanager.compliance.query.ComplianceSummaryFromRptQuery;
import com.marimba.apps.subscriptionmanager.compliance.query.ReportSummaryQuery;
import com.marimba.apps.subscriptionmanager.compliance.view.MachineListBean;
import com.marimba.apps.subscriptionmanager.compliance.view.MachineBean;
import com.marimba.apps.subscriptionmanager.compliance.view.ComplianceSummaryBean;
import com.marimba.apps.subscriptionmanager.compliance.view.ReportBean;
import com.marimba.intf.msf.IUserPrincipal;
import com.marimba.intf.msf.*;
import java.util.*;
import java.io.*;

public class ReportService extends Service {

    ReportQueue delQueue;
    
    public ReportService(ComplianceMain main,IUserPrincipal user, ITenant tenant) {
	    super(main,user, tenant);
	    delQueue = main.getQueueManager().getDelQueue();
    }

    public void deleteReport(int calculationId) {
	    delQueue.add(new DeleteReport(calculationId));
    }
    public void doReport(String target) {
    	doReport(target, "");
    }
    public void doReport(String target, String targetType) {
	    if (target != null && !"".equals(target.trim())) {
	        TargetReport tr = new TargetReport();
	        tr.setTarget(target);
            tr.setUser(user);
            tr.setTenant(tenant);
            tr.setTargetType(targetType);
	        rptQueue.add(tr);
	    }
    }

    public List getInQueueReports() {
	    return rptQueue.getQueue();
    }

    public List getCachedReports(String target, int page) {
	    ReportListQuery qry = new ReportListQuery();
        qry.setUser(user);
        qry.setTenant(tenant);
	    setPageInfo(qry, page);
	    setCacheInfoForList(qry);

        /*Executing query immediately, instead of adding into
        the query queue to avoid please wait screen*/
        qry.executeQuery();
	    IComplianceResult cr = qry.getResult();
	    if (cr != null && cr instanceof ListResult) {
	        ListResult lr = (ListResult) cr;
	        return lr.get(page);
	    } else {
	        return new ArrayList();
	    }
    }

    public ReportBean getReport(int calculationId) {
	    ReportSummaryQuery rq = new ReportSummaryQuery(calculationId);
        rq.setUser(user);
        rq.setTenant(tenant);
	    setCacheInfoForObject(rq);
	    rq = (ReportSummaryQuery) doQuery(rq, true, true, -1);

	    IComplianceResult mr = rq.getResult();
	    if (mr != null && mr instanceof ReportSummaryResult) {
	        return ((ReportSummaryResult) mr).getReportSummary();
	    } else {
	        return null;
	    }
    }

    /**
     * Get report center url
     */
    public String getRCURL(String level, int calculationId) {
	    return MachineListFromRptQuery.getRCURL(level, calculationId);
    }

    /**
     * Get machine list based on compliance level, from report
     */
    public MachineListBean getMachines(int calculationId, String level, int page, boolean recalc) {
	    MachineListBean ml = new MachineListBean();
	    ml.setError(true);
	
        MachineListFromRptQuery _mq = new MachineListFromRptQuery(calculationId, level);
        _mq.setUser(user);
        _mq.setTenant(tenant);
	    setPageInfo(_mq, page);
	    setCacheInfoForList(_mq);

        /*Executing query immediately, instead of adding into
        the query queue to avoid please wait screen*/
        _mq.executeQuery();

        IComplianceResult _mr = _mq.getResult();
        if(_mr != null && _mr instanceof ListResult) {
            ListResult _lr = (ListResult) _mr;
            if (_lr != null) {
		        ml.setList(_lr.get(page));
		        ml.setCurrentPage(page);
		        ml.setError(false);
            }
        }
        return ml;
    }


    /**
     * get compliance summary information for a report
     */
    public ComplianceSummaryBean getComplianceSummary(int calculationId, boolean recalculate) {
	    ComplianceSummaryBean _bean = new ComplianceSummaryBean();
	    _bean.setState(String.valueOf(ComplianceConstants.STATE_NOT_CALCULATE));

	    ComplianceSummaryFromRptQuery qry = new ComplianceSummaryFromRptQuery(calculationId);
        qry.setUser(user);
        qry.setTenant(tenant);
	    setCacheInfoForObject(qry);

        /*Executing query immediately, instead of adding into
        the query queue to avoid please wait screen*/
        qry.executeQuery();

	    // Set the state
	    _bean.setState(String.valueOf(qry.getState()));

	    // Get result
	    IComplianceResult res = qry.getResult();
	    if (res != null && res instanceof ComplianceSummaryFromRptResult) {
	        ComplianceSummaryFromRptResult result = (ComplianceSummaryFromRptResult) res;
	        _bean.setTotal(result.getTotal());
	        _bean.setCompliant(result.getCompliant());
	        _bean.setNoncompliant(result.getNonCompliant());
	        _bean.setNotcheckedin(result.getNotCheckIn());
	        _bean.setNotcalculated(result.getNotCalculated());
            _bean.setInfo("Created at :" + result.getCreatedTime());
        } else {
	        // Error handle here
        }
	    return _bean;
    }
}
