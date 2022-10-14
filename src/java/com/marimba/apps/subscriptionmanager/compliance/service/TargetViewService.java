// Copyright 1997-2005, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.service;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscriptionmanager.compliance.core.ComplianceMain;
import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;
import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceQuery;
import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceResult;
import com.marimba.apps.subscriptionmanager.compliance.query.*;
import com.marimba.apps.subscriptionmanager.compliance.result.ComplianceSummaryResult;
import com.marimba.apps.subscriptionmanager.compliance.result.ListResult;
import com.marimba.apps.subscriptionmanager.compliance.view.PackagePolicyDetails;
import com.marimba.intf.msf.ITenant;
import com.marimba.intf.msf.IUserPrincipal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Service for target view client
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
public class TargetViewService extends Service implements ISubscriptionConstants, ComplianceConstants{

    public TargetViewService(ComplianceMain main, IUserPrincipal user, ITenant tenant) {
	    super(main,user, tenant);
    }

    /**
     * Single machine compliance information.
     * @return List
     */
    public List getDetailComplianceByMachine(String targetId, int page, boolean recalculate) throws Exception {
	    targetId = (targetId == null) ? "" : targetId.toLowerCase();
        MachineComplianceQuery mcq = new MachineComplianceQuery(targetId);
        mcq.setUser(user);
        mcq.setTenant(tenant);
        setPageInfo(mcq, page);
        setCacheInfoForList(mcq);
        mcq = (MachineComplianceQuery) doQuery(mcq, recalculate, recalculate, -1);

        IComplianceResult res = mcq.getResult();
        if (res != null && res instanceof ListResult) {
            ListResult lr = (ListResult) res;
            return lr.get(page);
        } else {
            return new ArrayList();
        }
    }

    /**
     * Get Target details Exists DB as well LDAP
     * @param targetId
     * @param page
     * @param recalculate
     * @return
     * @throws Exception
     */
    public List getTargetDetailsExistsDB(String targetId, int page, boolean recalculate) throws Exception {
	    targetId = (targetId == null) ? "" : targetId.toLowerCase();
        TargetListQuery targetListQuery = new TargetListQuery(targetId);
        targetListQuery.setUser(user);
        targetListQuery.setTenant(tenant);
        setPageInfo(targetListQuery, page);
        setCacheInfoForList(targetListQuery);
        targetListQuery = (TargetListQuery) doQuery(targetListQuery, recalculate, recalculate, -1);

        IComplianceResult res = targetListQuery.getResult();
        if (res != null && res instanceof ListResult) {
            ListResult lr = (ListResult) res;
            return lr.get(page);
        } else {
            return new ArrayList();
        }
    }


    /**
     * Single machine Power setting compliance information.
     * @return List
     */
    public List getDetailPssComplianceByMachine(String targetId, int page, boolean recalculate) throws Exception {
	    targetId = (targetId == null) ? "" : targetId.toLowerCase();
        MachinePssComplianceQuery mcq = new MachinePssComplianceQuery(targetId);
        mcq.setUser(user);
        mcq.setTenant(tenant);
        setPageInfo(mcq, page);
        setCacheInfoForList(mcq);
        mcq = (MachinePssComplianceQuery) doQuery(mcq, recalculate, recalculate, -1);

        IComplianceResult res = mcq.getResult();
        if (res != null && res instanceof ListResult) {
            ListResult lr = (ListResult) res;
            return lr.get(page);
        } else {
            return new ArrayList();
        }
    }

    /**
     * Return group direct and indirect assigned package list
     * @param groupID
     * @param page
     * @param recalculate
     * @return List with Group compliance information
     * @throws Exception
     */
    public List getPackageListByGroup(String groupID, int page, boolean recalculate) throws Exception {

        groupID = (groupID == null) ? "" : groupID.toLowerCase();
        if("all".equalsIgnoreCase(groupID)) {
            // if the UI passes in "all", set it to "all_all" which is a pseudo dn for all endpoints target.
            groupID = "all_all";
        }
        PackageListByGroupQuery pgq = new PackageListByGroupQuery(groupID);
        pgq.setUser(user);
        pgq.setTenant(tenant);
        setPageInfo(pgq, page);

        /* excuting the query immediately
        instead of adding into QueryQueue to
        avoid please wait screen */

        pgq.executeQuery();
        setCacheInfoForList(pgq);

        IComplianceResult res = pgq.getResult();
        if(res != null && res instanceof ListResult) {
            ListResult lr = (ListResult) res;
            return lr.get(page);
        } else {
            return new ArrayList();
        }
    }
    /**
     * Return group direct and indirect assigned package list
     * @param groupID
     * @param page
     * @param recalculate
     * @return List with Group compliance information
     * @throws Exception
     */
    public List getPackageListBySite(String groupID, int page, boolean recalculate) throws Exception {

        groupID = (groupID == null) ? "" : groupID.toLowerCase();
        PackageListBySiteQuery pgq = new PackageListBySiteQuery(groupID);
        pgq.setUser(user);
        pgq.setTenant(tenant);
        setPageInfo(pgq, page);

        /* excuting the query immediately
        instead of adding into QueryQueue to
        avoid please wait screen */

        pgq.executeQuery();
        setCacheInfoForList(pgq);

        IComplianceResult res = pgq.getResult();
        if(res != null && res instanceof ListResult) {
            ListResult lr = (ListResult) res;
            return lr.get(page);
        } else {
            return new ArrayList();
        }
    }

    /**
     * Populate package list data with compliance information in the cache
     */
    public void getCachedCompliance(String groupId, List packages) {
        groupId = (groupId == null) ? "" : groupId.toLowerCase();
        // if the groupID is all, force the groupID to "all_all"
        if(groupId.equalsIgnoreCase("all")) {
            groupId = "all_all";
        }
        Iterator i = packages.iterator();
        String _targetID = "";
        int queryState = ComplianceConstants.STATE_NOT_CALCULATE;
        while (i.hasNext()) {
            PackagePolicyDetails pd = (PackagePolicyDetails) i.next();
            ComplianceSummaryQuery summaryQry = new ComplianceSummaryQuery();
            summaryQry.setUser(user);
            summaryQry.setTenant(tenant);
            _targetID = pd.getTargetId();
            summaryQry.setArgs(groupId, _targetID, pd.getPackageUrl());
            IComplianceQuery qry = cache.get(summaryQry);

            /* checking whether query is executed by the same user */
            if ( qry != null && qry.getUser().equals( super.user ) ) {
                IComplianceResult res = qry.getResult();
                if (res instanceof ComplianceSummaryResult) {
                    // We got one good result
                    ComplianceSummaryResult csr = (ComplianceSummaryResult) res;
                    int success = csr.getCompliant();
                    int failed = csr.getNonCompliant();
                    int notcheckedin = csr.getNotCheckIn();

                    pd.setHasCachedCompliance(true);
                    pd.setSucceeded(success);
                    pd.setFailed(failed);
                    pd.setNotCheckedIn(notcheckedin);
                    pd.setTotal(success + failed + notcheckedin);
                    pd.setCompLastCalculated(new java.sql.Date(csr.getCreatedTime()));
                }
            } else if( ( queryState = getQueryState( summaryQry ) ) != STATE_NOT_CALCULATE ){
                pd.setQueryState( queryState );
            }
        }
    }
    /**
     * Populate package list data with compliance information in the cache
     */
    public void getCachedSiteCompliance(String groupId, List packages) {
        groupId = (groupId == null) ? "" : groupId.toLowerCase();
        System.out.println("Entring site base summary compliance");
        Iterator i = packages.iterator();
        String _targetID = "";
        int queryState = ComplianceConstants.STATE_NOT_CALCULATE;
        while (i.hasNext()) {
            PackagePolicyDetails pd = (PackagePolicyDetails) i.next();
            SiteComplianceSummaryQuery summaryQry = new SiteComplianceSummaryQuery();
            summaryQry.setUser(user);
            summaryQry.setTenant(tenant);
            _targetID = pd.getTargetId();
            summaryQry.setArgs(groupId, _targetID, pd.getPackageUrl());
            IComplianceQuery qry = cache.get(summaryQry);

            /* checking whether query is executed by the same user */
            if ( qry != null && qry.getUser().equals( super.user ) ) {
                IComplianceResult res = qry.getResult();
                if (res instanceof ComplianceSummaryResult) {
                    // We got one good result
                    ComplianceSummaryResult csr = (ComplianceSummaryResult) res;
                    int success = csr.getCompliant();
                    int failed = csr.getNonCompliant();
                    int notcheckedin = csr.getNotCheckIn();

                    pd.setHasCachedCompliance(true);
                    pd.setSucceeded(success);
                    pd.setFailed(failed);
                    pd.setNotCheckedIn(notcheckedin);
                    pd.setTotal(success + failed + notcheckedin);
                    pd.setCompLastCalculated(new java.sql.Date(csr.getCreatedTime()));
                }
            } else if( ( queryState = getQueryState( summaryQry ) ) != STATE_NOT_CALCULATE ){
                pd.setQueryState( queryState );
            }
        }
    }
    private int getQueryState( IComplianceQuery qry ){
        IComplianceQuery sumqry = ( IComplianceQuery )qryQueue.getQueries().get( qry );
        if( sumqry != null ){
            return sumqry.getState();
        }
        return ComplianceConstants.STATE_NOT_CALCULATE;
    }

    /**
     * Get Target details member of specified group
     * @param targetId String
     * @param page int
     * @param recalculate boolean
     *
     * @return
     * @throws Exception
     */
    public List getTargetDetails(String targetId, int page, boolean recalculate) throws Exception {
	    targetId = (targetId == null) ? "" : targetId.toLowerCase();
        if(targetId.equalsIgnoreCase("all")) {
            targetId = "all_all";
        }
        TargetListByGroupQuery targetListQuery = new TargetListByGroupQuery(targetId);
        targetListQuery.setUser(user);
        targetListQuery.setTenant(tenant);
        setPageInfo(targetListQuery, page);
        setCacheInfoForList(targetListQuery);
        targetListQuery = (TargetListByGroupQuery) doQuery(targetListQuery, recalculate, recalculate, -1);

        IComplianceResult res = targetListQuery.getResult();
        if (res != null && res instanceof ListResult) {
            ListResult lr = (ListResult) res;
            return lr.get(page);
        } else {
            return new ArrayList();
        }
    }
}
