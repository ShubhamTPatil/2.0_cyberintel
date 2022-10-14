package com.marimba.apps.subscriptionmanager.compliance.service;

import com.marimba.apps.subscriptionmanager.compliance.intf.*;
import com.marimba.apps.subscriptionmanager.compliance.query.*;
import com.marimba.apps.subscriptionmanager.compliance.core.ComplianceMain;
import com.marimba.apps.subscriptionmanager.compliance.core.*;
import com.marimba.apps.subscriptionmanager.compliance.view.ComplianceSummaryBean;
import com.marimba.apps.subscriptionmanager.compliance.view.PackagePolicyDetails;
import com.marimba.apps.subscriptionmanager.compliance.view.PowerSummaryBean;
import com.marimba.apps.subscriptionmanager.compliance.result.ComplianceSummaryResult;
import com.marimba.apps.subscriptionmanager.compliance.result.PowerSummaryResult;

import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.intf.msf.IUserPrincipal;
import com.marimba.intf.msf.*;
import org.apache.log4j.*;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Service class used by ComplianceSummaryService client.
 *  @author Manoj Kumar
 *  @version $Revision$Date:
 */

public class ComplianceSummaryService extends Service implements ISubscriptionConstants {

    static final Logger sLogger = Logger.getLogger(ComplianceSummaryService.class);
    private static boolean isDebug = IAppConstants.DEBUG;

    public ComplianceSummaryService(ComplianceMain main,IUserPrincipal user, ITenant tenant) {
	    super(main,user, tenant);
    }
    
    private void error(String s,Throwable t){
        if(isDebug)
            sLogger.error(s,t);
    }

    public ComplianceSummaryBean getComplianceSummary(String target, boolean inventoryOnly, boolean recalculate) {

	    target = (target == null) ? "" :target.toLowerCase();
        if("all".equalsIgnoreCase(target)){
            target = "all_all";
        }

	    // Prepare result bean
	    ComplianceSummaryBean _bean = new ComplianceSummaryBean();
	    _bean.setState(String.valueOf(ComplianceConstants.STATE_NOT_CALCULATE));

        // Prepare query
	    OverallComplianceQuery overallQry = new OverallComplianceQuery(target);
        overallQry.setUser(user);
        overallQry.setTenant(tenant);
	    overallQry.setCheckinLimit(cfgMgr.getInt(ConfigManager.CFG_CHECKIN_LIMIT));
	    setCacheInfoForObject(overallQry);
        overallQry.executeQuery();

	    // Set result state
	    _bean.setState(String.valueOf(overallQry.getState()));
        _bean.setComplianceCalcTime(overallQry.getEndTime());

	    // Get result
	    IComplianceResult res = overallQry.getResult();
	    if (res != null && res instanceof ComplianceSummaryResult) {
    	    ComplianceSummaryResult result = (ComplianceSummaryResult) res;
            overallQry.getSummary(_bean);
            _bean.setInfo("Created at :" + result.getCreatedTime());
        } else {
	    // Error handle here
        }
	    return _bean;
    }
    public ComplianceSummaryBean getSiteComplianceSummary(String target, boolean inventoryOnly, boolean recalculate) {

	    target = (target == null) ? "" :target.toLowerCase();
        
	    // Prepare result bean
	    ComplianceSummaryBean _bean = new ComplianceSummaryBean();
	    _bean.setState(String.valueOf(ComplianceConstants.STATE_NOT_CALCULATE));

        // Prepare query
	    OverallSiteComplianceQuery overallQry = new OverallSiteComplianceQuery(target);
        overallQry.setUser(user);
        overallQry.setTenant(tenant);
	    overallQry.setCheckinLimit(cfgMgr.getInt(ConfigManager.CFG_CHECKIN_LIMIT));
	    setCacheInfoForObject(overallQry);
        overallQry.executeQuery();

	    // Set result state
	    _bean.setState(String.valueOf(overallQry.getState()));
        _bean.setComplianceCalcTime(overallQry.getEndTime());

	    // Get result
	    IComplianceResult res = overallQry.getResult();
	    if (res != null && res instanceof ComplianceSummaryResult) {
    	    ComplianceSummaryResult result = (ComplianceSummaryResult) res;
            overallQry.getSummary(_bean);
            _bean.setInfo("Created at :" + result.getCreatedTime());
        } else {
	    // Error handle here
        }
	    return _bean;
    }
    public PowerSummaryBean getPowerSummary(String target, boolean inventoryOnly, boolean recalculate) {

        target = (target == null) ? "" :target.toLowerCase();
        if("all".equalsIgnoreCase(target)){
            target = "all_all";
        }

        // Prepare result bean
        PowerSummaryBean _bean = new PowerSummaryBean();
        _bean.setState(String.valueOf(ComplianceConstants.STATE_NOT_CALCULATE));
        PowerComplianceQuery powerQry = new PowerComplianceQuery(target);
        powerQry.setUser(user);
        powerQry.setTenant(tenant);
        powerQry.setCheckinLimit(cfgMgr.getInt(ConfigManager.CFG_CHECKIN_LIMIT));
        setCacheInfoForObject(powerQry);
        powerQry.executeQuery();

        // Set result state
        _bean.setState(String.valueOf(powerQry.getState()));
        _bean.setComplianceCalcTime(powerQry.getEndTime());

        // Get result
        IComplianceResult res = powerQry.getResult();
        if (res != null && res instanceof PowerSummaryResult) {
            PowerSummaryResult result = (PowerSummaryResult) res;
            powerQry.getSummary(_bean);
            _bean.setInfo("Created at :" + result.getCreatedTime());
        } else {
            // Error handle here
        }
        return _bean;
    }
    public PowerSummaryBean getPowerSiteSummary(String target, boolean inventoryOnly, boolean recalculate) {

        target = (target == null) ? "" :target.toLowerCase();

        // Prepare result bean
        PowerSummaryBean _bean = new PowerSummaryBean();
        _bean.setState(String.valueOf(ComplianceConstants.STATE_NOT_CALCULATE));
        PowerComplianceSiteQuery powerQry = new PowerComplianceSiteQuery(target);
        powerQry.setUser(user);
        powerQry.setTenant(tenant);
        powerQry.setCheckinLimit(cfgMgr.getInt(ConfigManager.CFG_CHECKIN_LIMIT));
        setCacheInfoForObject(powerQry);
        powerQry.executeQuery();

        // Set result state
        _bean.setState(String.valueOf(powerQry.getState()));
        _bean.setComplianceCalcTime(powerQry.getEndTime());

        // Get result
        IComplianceResult res = powerQry.getResult();
        if (res != null && res instanceof PowerSummaryResult) {
            PowerSummaryResult result = (PowerSummaryResult) res;
            powerQry.getSummary(_bean);
            _bean.setInfo("Created at :" + result.getCreatedTime());
        } else {
            // Error handle here
        }
        return _bean;
    }
    public ComplianceSummaryBean[] getComplianceSummary( String target[], String policy[], String pkg[], boolean recalculate, String view, String targetTypes[] )throws Exception{
    	ComplianceSummaryBean sumaryBean = null;
        ComplianceSummaryQuery summaryQuery = null;
        SiteComplianceSummaryQuery summarySiteQuery = null;
        ComplianceSummaryBean[] sumBean = new ComplianceSummaryBean[target.length];
        try{
            IComplianceQuery[] compQueries = doBatch( getSummaryQueries( target, targetTypes, policy, pkg ), recalculate, recalculate, -1);
            for( int index = 0; index < compQueries.length; index ++ ){
                sumaryBean = getSummaryBean();
                if(compQueries[index] instanceof SiteComplianceSummaryQuery ) {
                	summarySiteQuery = ( SiteComplianceSummaryQuery )compQueries[ index ];
                    sumaryBean.setState( String.valueOf( summarySiteQuery.getState() ) );
                    sumaryBean.setTargetType("site");
                    setSiteSummaryResult( summarySiteQuery, sumaryBean );
                    updateSiteSessionBean( summarySiteQuery, sumaryBean, view );
                } else {
                	summaryQuery = ( ComplianceSummaryQuery )compQueries[ index ];
                    sumaryBean.setState( String.valueOf( summaryQuery.getState() ) );
                    sumaryBean.setTargetType("");
                    setSummaryResult( summaryQuery, sumaryBean );
                    updateSessionBean( summaryQuery, sumaryBean, view );
                }
                sumBean[ index ] = sumaryBean;
            }
        }catch( Exception exp ){
            exp.printStackTrace();
            throw exp;
        }
        int notcheckedin;
        int compliant;
        int noncompliant;
        int notcalculated;
        int total;
        ComplianceSummaryBean sumaryBean1 = sumBean[0];
        System.out.println("Compliance summay bean :" + sumBean.length);
        System.out.println("Compliance summay bean getNotcheckedin:" + sumaryBean1.getNotcheckedin());
        System.out.println("Compliance summay bean getCompliant:" + sumaryBean1.getCompliant());
        System.out.println("Compliance summay bean getNoncompliant:" + sumaryBean1.getNoncompliant());
        System.out.println("Compliance summay bean getNotcalculated:" + sumaryBean1.getNotcalculated());
        System.out.println("Compliance summay bean getTotal:" + sumaryBean1.getTotal());
        return sumBean;
    }
    public ComplianceSummaryBean[] getComplianceSummary( String target[], String policy[], String pkg[], boolean recalculate, String view )throws Exception{
        return getComplianceSummary(target, policy, pkg, recalculate, view, null);
    }

    private void updateSessionBean( ComplianceSummaryQuery summaryQuery, ComplianceSummaryBean summaryBean, String view ){
        HttpSession session = super.getSession();
        List display_results = null;
        if( "target".equals( view ) ){
            display_results = ( List )session.getAttribute( IWebAppConstants.POLICIES_DETAILS_FORTGT );
        }else if( "package".equals( view ) ){
            display_results = ( List )session.getAttribute( IWebAppConstants.PACKAGE_DETAILS_FORPKG );
        }

        if( display_results != null ){
            for( int index = 0; index < display_results.size(); index++ ){
                PackagePolicyDetails result = ( PackagePolicyDetails )display_results.get( index );
                if( result.getTargetId().equals( summaryQuery.getTarget() ) && result.getPackageUrl().equals( summaryQuery.getPkgName() ) ){
                    result.setQueryState( summaryQuery.getState() );
                    if( summaryQuery.getState() == ComplianceConstants.STATE_DONE ){
                        result.setHasCachedCompliance(true);
                        result.setSucceeded( summaryBean.getCompliant() );
                        result.setFailed( summaryBean.getNoncompliant() );
                        result.setNotCheckedIn( summaryBean.getNotcheckedin() );
                        result.setTotal( summaryBean.getTotal() );
                        result.setCompLastCalculated( new java.sql.Date( summaryBean.getComplianceCalcTime() ) );
                    }
                }
            }
        }
    }
    private void updateSiteSessionBean( SiteComplianceSummaryQuery summaryQuery, ComplianceSummaryBean summaryBean, String view ){
        HttpSession session = super.getSession();
        List display_results = null;
        if( "target".equals( view ) ){
            display_results = ( List )session.getAttribute( IWebAppConstants.POLICIES_DETAILS_FORTGT );
        }else if( "package".equals( view ) ){
            display_results = ( List )session.getAttribute( IWebAppConstants.PACKAGE_DETAILS_FORPKG );
        }

        if( display_results != null ){
            for( int index = 0; index < display_results.size(); index++ ){
                PackagePolicyDetails result = ( PackagePolicyDetails )display_results.get( index );
                if( result.getTargetId().equals( summaryQuery.getTarget() ) && result.getPackageUrl().equals( summaryQuery.getPkgName() ) ){
                    result.setQueryState( summaryQuery.getState() );
                    if( summaryQuery.getState() == ComplianceConstants.STATE_DONE ){
                        result.setHasCachedCompliance(true);
                        result.setSucceeded( summaryBean.getCompliant() );
                        result.setFailed( summaryBean.getNoncompliant() );
                        result.setNotCheckedIn( summaryBean.getNotcheckedin() );
                        result.setTotal( summaryBean.getTotal() );
                        result.setCompLastCalculated( new java.sql.Date( summaryBean.getComplianceCalcTime() ) );
                    }
                }
            }
        }
    }
    private IComplianceQuery[] getSummaryQueries( String[] target, String[] targetTypes, String[] policy, String[] pkg ){
    	IComplianceQuery[] queries = new IComplianceQuery[target.length];
        for( int index = 0; index < queries.length; index ++ ){
        	if(null != targetTypes && targetTypes[index].equals(TYPE_SITE)) {
        		queries[ index ] = getSiteSummaryQuery( target[ index ], policy[ index ], pkg[ index ] );
        	} else {
        		queries[ index ] = getSummaryQuery( target[ index ], policy[ index ], pkg[ index ] );
        	}
        }
        return queries;
    }

    private ComplianceSummaryQuery getSummaryQuery( String target, String policy, String pkg ){
        target = (target == null) ? "" : target.toLowerCase();
        if("all".equalsIgnoreCase(target)){
            target = "all_all";
        }
        ComplianceSummaryQuery summaryQry = new ComplianceSummaryQuery();
        summaryQry.setUser(user);
        summaryQry.setTenant(tenant);
        summaryQry.setArgs(target, policy, pkg);
	    setCacheInfoForObject(summaryQry);
        return summaryQry;
    }
    private SiteComplianceSummaryQuery getSiteSummaryQuery( String target, String policy, String pkg ){
        target = (target == null) ? "" : target.toLowerCase();
        
        SiteComplianceSummaryQuery summaryQry = new SiteComplianceSummaryQuery();
        summaryQry.setUser(user);
        summaryQry.setTenant(tenant);
        summaryQry.setArgs(target, policy, pkg);
	    setCacheInfoForObject(summaryQry);
        return summaryQry;
    }
    private void setSummaryResult( ComplianceSummaryQuery summaryQuery, ComplianceSummaryBean summaryBean ){
        IComplianceResult result = summaryQuery.getResult();
        if ( result != null && result instanceof ComplianceSummaryResult) {
            summaryQuery.getSummary( summaryBean );
            summaryBean.setInfo("Created at :" + result.getCreatedTime());
        } else {
            // Error handle here
            summaryBean.setException( summaryQuery.getException() );
        }
    }
    private void setSiteSummaryResult( SiteComplianceSummaryQuery summaryQuery, ComplianceSummaryBean summaryBean ){
        IComplianceResult result = summaryQuery.getResult();
        if ( result != null && result instanceof ComplianceSummaryResult) {
            summaryQuery.getSummary( summaryBean );
            summaryBean.setInfo("Created at :" + result.getCreatedTime());
        } else {
            // Error handle here
            summaryBean.setException( summaryQuery.getException() );
        }
    }
    private ComplianceSummaryBean getSummaryBean(){
        ComplianceSummaryBean summaryBean = new ComplianceSummaryBean();
        summaryBean.setState(String.valueOf(ComplianceConstants.STATE_NOT_CALCULATE));
        return summaryBean;
    }

    public ComplianceSummaryBean getComplianceSummary(String target, String policy, String pkg, boolean recalculate){
        ComplianceSummaryBean _bean = getSummaryBean();
        ComplianceSummaryQuery summaryQry = getSummaryQuery( target, policy, pkg );

	    // If not in the cache, don't do the query
	    summaryQry = (ComplianceSummaryQuery) doQuery(summaryQry, recalculate, recalculate, -1);

	    // set state
        _bean.setState(String.valueOf(summaryQry.getState()));

	    // Get result
        setSummaryResult( summaryQry, _bean );
        return _bean;
    }
    public ComplianceSummaryBean getSiteComplianceSummary(String target, String policy, String pkg, boolean recalculate){
        ComplianceSummaryBean _bean = getSummaryBean();
        SiteComplianceSummaryQuery summaryQry = getSiteSummaryQuery( target, policy, pkg );

	    // If not in the cache, don't do the query
	    summaryQry = (SiteComplianceSummaryQuery) doQuery(summaryQry, recalculate, recalculate, -1);

	    // set state
        _bean.setState(String.valueOf(summaryQry.getState()));

	    // Get result
        setSiteSummaryResult( summaryQry, _bean );
        return _bean;
    }
    private void log(String s){
        if(isDebug)
            sLogger.info(s);
    }
}