// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.arsystem;

import java.util.*;
import java.sql.*;


import com.marimba.apps.subscription.common.*;
import com.marimba.apps.subscriptionmanager.compliance.core.DbSourceManager;
import com.marimba.apps.subscriptionmanager.compliance.intf.*;
import com.marimba.apps.subscriptionmanager.compliance.util.*;
import com.marimba.apps.subscriptionmanager.intf.IARTaskConstants;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;

import com.marimba.intf.msf.IServer;
import com.marimba.intf.msf.arsys.*;
import com.marimba.intf.msf.query.*;
import com.marimba.intf.msf.task.*;
import com.marimba.webapps.intf.*;
import com.marimba.intf.msf.ITenant;
import com.marimba.intf.msf.task.ITaskContext;
import javax.servlet.ServletContext;

import org.apache.struts.util.MessageResources;

/**
 *
 *
 * @author $Author$
 * @version $Revision$
 * $Id$
 */
public class VerificationTask implements ITask, ARTaskLogConstants, ISubscriptionConstants {
    public final static String QUERY_GET_PACKAGES_FOR_TARGET = ComplianceConstants.QUERY_FOLDER_PATH + "PackagesByTarget";
    public final static String QUERY_GET_TARGETS_FOR_PACKAGE = ComplianceConstants.QUERY_FOLDER_PATH + "TargetsByPackage";
    public final static String QUERY_GET_COMPLIANCE = ComplianceConstants.QUERY_FOLDER_PATH + "SummaryByPolicyByTarget";
    public final static String QUERY_GET_COMPLIANCE_FOR_MACHINE = ComplianceConstants.QUERY_FOLDER_PATH + "MachineComplianceSummary";

    final static int SCHEDULE_NEVER_INT = -1;
    final static String AR_TEST = "AR_TEST";
    protected ITaskContext taskContext = null;
    protected IQueryMgrContext queryMgrContext;
    protected int compliancePercentage;

    protected long expireTime;

    protected boolean bCompliant;
    protected boolean bSentWorklog;
    protected boolean bError;
    protected String verifyResult;

    // if task is removed before a chance to execute, destroy() will not send worklog

    protected String arTaskID;
    protected Map taskMap;
    protected String scheduleStr;

    protected ITaskMgr taskmgr;
    protected SubscriptionMain main;
    protected ServletContext servletContext;
    protected ARLogMgr arLogMgr;
    protected MessageResources msgResources;
    protected ITenant tenant;
    public void init(ITaskContext context)
        throws TaskException {
        this.taskContext = context;
        this.tenant = context.getTenant();
        scheduleStr = taskContext.getTaskProperty("ar_schedule");
        expireTime = taskContext.getLong("task.expirationTime", -1);
        arTaskID = taskContext.getTaskProperty("ar_taskid");
        taskMap = new HashMap();
        taskMap.put(IARConstants.PARAM_TASK_ID, new String[] {arTaskID});
        taskmgr = taskContext.getTaskMgr();
        this.servletContext = (ServletContext) taskContext.getFeature("servletContext");
        main = TenantHelper.getTenantSubMain(servletContext, tenant.getName());
        arLogMgr = new ARLogMgr(main, main.getAppLog());
        msgResources = main.getAppResources();
        taskMap.put(IARTaskConstants.AR_USER, msgResources.getMessage("ar.worklog.modifieduser")+taskContext.getTaskProperty("query_as"));
    }

    public void execute(ITaskRuntime runtime)
        throws TaskException {
        arLogMgr.log(0, LOG_AUDIT, "Executing Verification Task", null, AR_EXECUTION);
        if (bSentWorklog) {
            arLogMgr.log(0, LOG_AUDIT, "Already Sent the Verification Task Reply : " + bSentWorklog, null, AR_EXECUTION);
            return;
        }
        bError = false;

        ITaskResult result = runtime.getResult();
        IQueryMgr queryMgr = null;
        try {
        	queryMgr = DbSourceManager.getQueryManager(tenant);
            this.queryMgrContext = queryMgr.createContext();
            bCompliant = true;
            verifyTask();
            // mark as done
            if (bCompliant) {
                result.setProperty("task.completed", "true");
            }
            arLogMgr.log(LOG_AR_VERIFYTASK_SUCCEEDED, LOG_AUDIT, String.valueOf(bCompliant), null, AR_EXECUTION);
        }
        catch (Exception e) {
            bError = true;
            verifyResult = e.getMessage();
            arLogMgr.log(LOG_AR_VERIFYTASK_FAILED, LOG_MAJOR, verifyResult, e);
	    }
        // send the AR WorkLog if totally compliant or reached end of the time window
        long nextInvocation = taskmgr.getNextInvocation(scheduleStr, System.currentTimeMillis());
        if  (bCompliant || (nextInvocation == SCHEDULE_NEVER_INT || nextInvocation >= expireTime))   {
            sendARWorklog();
        }
        this.queryMgrContext = null;
        arLogMgr.log(0, LOG_AUDIT, "End Executing Verification Task", null, AR_EXECUTION);
    }

    private void verifyTask()    {

        String resultStr = null;
        String targets = taskContext.getTaskProperty("targets");
        String packages = taskContext.getTaskProperty("packages");
        compliancePercentage = taskContext.getInteger("compliance_percentage", 100);


        arLogMgr.log(LOG_AR_TARGETS, LOG_AUDIT, targets, null, AR_EXECUTION);
        arLogMgr.log(LOG_AR_CHANNELS, LOG_AUDIT, packages, null, AR_EXECUTION);

        if (targets != null && packages != null)    {
           resultStr = computeComplianceForTargets(targets, packages);
        }
        else if (targets != null)   {
            resultStr = computeComplianceForTargets(targets, null);
        }
        else if (packages != null)  {
            resultStr = computeComplianceForPackage(packages);
        }
        else    {
            resultStr = "Bad task configuration, no targets or packages found.";
            bCompliant = false;
            bError = true;
        }
        verifyResult = resultStr;
        arLogMgr.log(0, LOG_AUDIT, verifyResult, null, AR_EXECUTION);
    }

    public void destroy() {

    }

    private String computeComplianceForTargets(String targets, String packages) {
        StringBuffer strbuf = new StringBuffer();

        StringTokenizer strtok = new StringTokenizer(targets, "\" ");
        String target = null;
        String targetType = null;
        boolean isSingleEntity = false;

        while (strtok.hasMoreElements())    {
            target = strtok.nextToken();
            targetType = strtok.nextToken();
            isSingleEntity = false;
            if (ISubscriptionConstants.TYPE_MACHINE.equalsIgnoreCase(targetType) ||
                    ISubscriptionConstants.TYPE_USER.equalsIgnoreCase(targetType))    {
                isSingleEntity = true;
            }
            if (isSingleEntity && packages == null)    {
                computeMachineCompliance(target, strbuf);
            }
            else    {
                computeTargetCompliance(target, packages, isSingleEntity, strbuf);
            }
        }

        return strbuf.toString();
    }

    private void computeTargetCompliance(String target, String packages, boolean isSingleEntity, StringBuffer strbuf)   {
        int compliance = 0;
        List pkgList = getPackagesForTarget(target);
        // trim the list to the specified packages
        if (packages != null)   {
            pkgList = getPoliciesForPackages(pkgList, packages, target, strbuf);
        }

        int nPkgs = pkgList.size();
        if (nPkgs == 0) {
            strbuf.append("No packages assigned/found for ");
            strbuf.append(target);
            strbuf.append(". ");
            bCompliant = false;
            return;
        }
        String[] params = new String[3];
        ComplianceResult result = new ComplianceResult();
        int nNonCompliant = 0;
        String currentPkg = null;
        int indexOfSpace = -1;
        for (int pkg=0; pkg<nPkgs; pkg++) {
            currentPkg = (String) pkgList.get(pkg);
            params[0] = target;
            indexOfSpace = currentPkg.indexOf(" ");
            params[1] = currentPkg.substring(0, indexOfSpace);
            params[2] = currentPkg.substring(indexOfSpace+1);
            computeCompliance(result, params, QUERY_GET_COMPLIANCE);
            if ( ! result.isCompliant() )   {
                nNonCompliant++;
                if ( ! isSingleEntity) {
                    bCompliant = false;
                    strbuf.append("Package ");
                    strbuf.append(params[2]);
                    strbuf.append(" is non-compliant (");
                    strbuf.append(result.getCompliancePercentage());
                    strbuf.append(") for target ");
                    strbuf.append(target);
                    strbuf.append("\n");
                }
            }
        }
        if (isSingleEntity) {
            compliance = ((nPkgs - nNonCompliant)/nPkgs)*100;
            if (compliance < compliancePercentage)  {
                bCompliant = false;
                strbuf.append("Target ");
                strbuf.append(target);
                strbuf.append(" is non-compliant, (");
                strbuf.append(compliance);
                strbuf.append(")");
                strbuf.append("\n");
            }
            else    {
                strbuf.append("Target ");
                strbuf.append(target);
                strbuf.append(" is compliant");
                strbuf.append("\n");
            }
        }
        else {
            strbuf.append("Target ");
            strbuf.append(target);
            if (nNonCompliant == 0)  {
                strbuf.append(" is compliant");
                strbuf.append("\n");
            }
            else    {
                strbuf.append(" is non-compliant");
                strbuf.append("\n");
            }
        }
        return;
    }

    private List getPoliciesForPackages(List pkgList, String packages, String target, StringBuffer msgbuf)  {
        List newPkgList = new ArrayList();

        Map pkgPolicyMap = new HashMap();
        int indexOfSpace = -1;
        String str = null;
        String pkgurl = null;
        for (int j=0; j<pkgList.size(); j++)    {
            str = (String) pkgList.get(j);
            indexOfSpace = str.indexOf(" ");
            if (indexOfSpace != -1) {
                pkgurl = str.substring(indexOfSpace+1);
                ARUtils.debug("Making Maps: Package URL: "+pkgurl+ " STR: "+str);
                pkgPolicyMap.put(pkgurl, str);
            }
        }

        StringTokenizer strtok2 = new StringTokenizer(packages, ",");
        String ccmPkg = null;
        while (strtok2.hasMoreTokens()) {
            ccmPkg = strtok2.nextToken();
            str = (String) pkgPolicyMap.get(ccmPkg);
            ARUtils.debug("VerificationTask: CCM Package: "+ccmPkg);
            if (str == null)    {
                ARUtils.debug("VerificationTask: Policy Doesn't Exist for target: "+target+" package: "+ccmPkg);
                msgbuf.append("Policy does not exist for target " +target +" package " + ccmPkg +". ");
            }
            else    {
                newPkgList.add(str);
            }
        }
        return newPkgList;
    }

    private void computeMachineCompliance(String target, StringBuffer strbuf)    {
        ARUtils.debug("VerificationTask: Compute Comapliance for Endpoint: "+target);
        ComplianceResult result = new ComplianceResult();
        String[] params = new String[1];
        params[0] = target;
        computeCompliance(result, params, QUERY_GET_COMPLIANCE_FOR_MACHINE);
        int compliance = result.getCompliancePercentage();
        if (compliance < compliancePercentage) {
            bCompliant = false;
            strbuf.append("Target ");
            strbuf.append(target);
            strbuf.append(" is non-compliant (");
            strbuf.append(compliance);
            strbuf.append(")");
            strbuf.append("\n");
        }
        else    {
            strbuf.append("Target ");
            strbuf.append(target);
            strbuf.append(" is compliant (");
            strbuf.append(compliance);
            strbuf.append(")");
            strbuf.append("\n");
        }
        return;
    }

    private String computeComplianceForPackage(String pkgList) {
        StringBuffer strbuf = new StringBuffer();
        StringTokenizer strtok = new StringTokenizer(pkgList, ",");

        String pkg = null;
        String target = null;
        int computedCompliance = 0;
        while (strtok.hasMoreTokens())  {
            pkg = strtok.nextToken();
            List targetList = getTargetsForPackage(pkg);

            int nTargets = targetList.size();
            String[] params = new String[3];
            ComplianceResult result = new ComplianceResult();
            if (nTargets == 0)  {
                bCompliant = false;
                strbuf.append("No targets found for package ");
                strbuf.append(pkg);
                strbuf.append("\n");
            }
            else    {
                for (int i=0; i<nTargets; i++) {
                    target = (String) targetList.get(i);
                    params[0] = target;
                    params[1] = target;
                    params[2] = pkg;
                    computeCompliance(result, params, QUERY_GET_COMPLIANCE);
                    computedCompliance = result.getCompliancePercentage();
                    if (computedCompliance < compliancePercentage)  {
                        bCompliant = false;
                        strbuf.append("Not compliant (value=");
                        strbuf.append(computedCompliance);
                        strbuf.append(") for package ");
                        strbuf.append(pkg);
                        strbuf.append(" and target ");
                        strbuf.append(target);
                        strbuf.append("\n");
                    }
                    else    {
                        strbuf.append("Compliant (");
                        strbuf.append(computedCompliance);
                        strbuf.append(") for package ");
                        strbuf.append(pkg);
                        strbuf.append("\n");
                    }
                }
            }
        }
        if (bCompliant) {
            strbuf.append("All packages are compliant. ");
        }
        else    {
           strbuf.append("Atleast one package is non-compliant. ");
        }
        return strbuf.toString();
    }

    private List getPackagesForTarget(String target)    {
        List pkgList = new ArrayList();
        String[] params = new String[1];
        params[0] = target;
        ARUtils.debug("VerificationTask: Get Packages for Target: "+target);
        IQueryResult rs = null;

        try {
            rs = doQuery(QUERY_GET_PACKAGES_FOR_TARGET, params);
            ARUtils.debug("VerificationTask: Get Row Count: "+rs.getRowCount());
            while (rs.next())   {
                pkgList.add(rs.getString("target_name") + " " + rs.getString("url"));
                ARUtils.debug("VerificationTask: Target from Table: "+rs.getString("target_name"));
            }

        }
        catch (SQLException sqle)    {
            arLogMgr.log(LOG_AR_SQL_EXCEPTION, LOG_MAJOR, null, sqle);
        }
        finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    arLogMgr.log(LOG_AR_SQL_EXCEPTION, LOG_MAJOR, null, e);
                }
            }
        }
        return pkgList;
    }



    private List getTargetsForPackage(String pkg)   {
        List targetList = new ArrayList();
        String[] params = new String[1];
        params[0] = pkg;
        ARUtils.debug("VerificationTask: Get Targets for Package: "+pkg);
        IQueryResult rs = null;
        try {
            rs = doQuery(QUERY_GET_TARGETS_FOR_PACKAGE, params);
            ARUtils.debug("VerificationTask: Get Row Count: "+rs.getRowCount());
            while (rs.next())   {
                targetList.add(rs.getString("policyname"));
                ARUtils.debug("VerificationTask: Policy Name: "+rs.getString("policyname"));
            }
        }
        catch (SQLException sqle)    {
            arLogMgr.log(LOG_AR_SQL_EXCEPTION, LOG_MAJOR, null, sqle);
        }
        finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    arLogMgr.log(LOG_AR_SQL_EXCEPTION, LOG_MAJOR, null, e);
                }
            }
        }
        return targetList;
    }

    private void computeCompliance(ComplianceResult result, String[] params, String queryName/*, String target, String pkg*/)  {
        result.reset();
        IQueryResult rs = null;
        long count;
        String level = "";
        int nResult = 0;
        try {
            rs = doQuery(queryName, params);
            ARUtils.debug("VerificationTask: Get Row Count: "+rs.getRowCount());

            while (rs.next())   {
                count = rs.getInt(1);
                if (QUERY_GET_COMPLIANCE_FOR_MACHINE.equals(queryName)) {
                    if (nResult == 0)   {
                        result.total = count;
                        nResult++;
                    }
                    else    {
                        result.compliant = count;
                    }
                }
                else    {
                    level = rs.getString(2);
                    result.setCompliance(count, level);
                }
                ARUtils.debug("VerificationTask: Count: "+count);
                ARUtils.debug("VerificationTask: Compliance Level: "+level);
            }

        }
        catch (SQLException sqle)    {
            arLogMgr.log(LOG_AR_SQL_EXCEPTION, LOG_MAJOR, null, sqle);
        }
        finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    arLogMgr.log(LOG_AR_SQL_EXCEPTION, LOG_MAJOR, null, e);
                }
            }
        }
    }

    private IQueryResult doQuery(String queryPath, String[] parameterValues) throws SQLException {
        IQueryNode node = queryMgrContext.lookup(queryPath);
        IQuery query = node.getQuery();
	    IQueryParameter parameters[] = query.getParameters();
        if (parameters != null) {
            if (parameterValues == null || parameterValues.length != parameters.length) {
                throw new SQLException("Missing query parameters for " + this);
            }
            for (int i = 0; i < parameters.length; i++) {
                parameters[i].set(WebUtil.stripDots(parameterValues[i]));
            }
        }
        return query.execute();
    }

    private void sendARWorklog()    {
        String status = null;
        if (bError) {
            status = IARConstants.TMS_RETURN_CODE_ERROR;
        }
        else if (bCompliant)    {
            status = IARConstants.TMS_RETURN_CODE_SUCCESS;
        }
        else {
            status = IARConstants.TMS_RETURN_CODE_FAILURE;
        }
        String[] taskid = (String[]) taskMap.get(IARConstants.PARAM_TASK_ID);
        if (AR_TEST.equals(taskid[0]))    {
            ARUtils.debug("VerificationTask: Verify Result: "+verifyResult);
            ARUtils.debug("VerificationTask: Status: "+status);
            bSentWorklog = true;
            return;
        }
        try {
            ARWorklog workLog = new ARWorklog(taskMap, tenant);
            workLog.write(verifyResult);
            workLog.close(taskMap, status, msgResources.getMessage("ar.worklog.verifytaskupdated")+status);
            bSentWorklog = true;
            ARUtils.debug("VerificationTask: Sent Worklog: "+verifyResult);
        }
        catch (Exception e)    {
            arLogMgr.log(LOG_AR_EXCEPTION, LOG_MAJOR, null, e);
        }
    }

    private class ComplianceResult  {
        long total;
        long compliant;
        long non_compliant;

        void reset()    {
            total = 0;
            compliant = 0;
            non_compliant = 0;
        }

        void setCompliance(long num, String level)   {
            if (ComplianceConstants.STR_LEVEL_COMPLIANT.equals(level)) {
                compliant = num;
            }
            else if (ComplianceConstants.STR_LEVEL_NON_COMPLIANT.equals(level)) {
                non_compliant = num;
            }
            else if(ComplianceConstants.STR_TOTAL_MACHINES.equals(level)) {
                total = num;
            }
        }
        long getCompliant() {
            return compliant;
        }
        long getNonCompliant() {
            return non_compliant;
        }
        long getTotalCompliant() {
            return total;
        }
        long getNotCheckedIn() {
            return total - compliant - non_compliant;
        }

        boolean isCompliant()  {
            return (getCompliancePercentage() >=  compliancePercentage);
        }

        int getCompliancePercentage()  {
            int perc = 0;
            float compliant_temp = (float) compliant;
            float total_temp = (float) total;
            if (total > 0)  {
                float val = (compliant_temp/total_temp)*100.0F;
                perc =  (int) val;
            }
            return perc;
        }
    }

}
