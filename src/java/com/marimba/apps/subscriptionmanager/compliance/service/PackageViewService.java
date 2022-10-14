// Copyright 1997-2005, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.service;

import com.marimba.apps.subscriptionmanager.compliance.core.*;
import com.marimba.apps.subscriptionmanager.compliance.intf.*;
import com.marimba.apps.subscriptionmanager.compliance.query.*;
import com.marimba.apps.subscriptionmanager.compliance.result.*;
import com.marimba.apps.subscriptionmanager.compliance.view.*;
import com.marimba.apps.subscriptionmanager.compliance.util.WebUtil;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.util.Utils;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;
import com.marimba.apps.subscriptionmanager.webapp.forms.ScheduleEditForm;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.SubInternalException;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.intf.msf.acl.IAclConstants;
import com.marimba.intf.msf.*;
import com.marimba.webapps.tools.util.KnownActionError;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.ParseException;

/**
 * Service class used by PackageView client.
 * @author  Manoj Kumar
 * @version $Revision$, $Date$
 */
public class PackageViewService extends Service implements  IAppConstants {

    static final Logger sLogger = Logger.getLogger(PackageViewService.class);
    private static boolean isDebug = IAppConstants.DEBUG;
    private Locale userLocale;

    public PackageViewService(ComplianceMain main, IUserPrincipal user,ITenant tenant) {
        super(main,user, tenant);
    }

    /**
     * Get a list of packages from the database
     * @return List
     */
    public PackageListBean getPackages(String pkgName, String pkgTime) throws Exception {
        return getPackages(pkgName, pkgTime, 0);
    }
    public PackageListBean getPackages(String pkgName, String pkgTime, int page) throws Exception {
        // List _list = new ArrayList();
        PackageListBean res = new PackageListBean();

        res.setError(true);

        PackageListQuery packageQry = new PackageListQuery(pkgName, pkgTime);
        packageQry.setUser(user);
        packageQry.setTenant(tenant);
        packageQry.setLocale(userLocale);
        setPageInfo(packageQry, page);
        setCacheInfoForList(packageQry);
        packageQry.executeQuery();

        IComplianceResult cr = packageQry.getResult();
        if (cr != null && cr instanceof ListResult) {
            ListResult lr = (ListResult) cr;
            res.setList(lr.get(page));
            res.setError(false);
        }

        return res;

    }

    public PackageListBean getPackages(String pkgName) throws Exception{
        return getPackages(pkgName, "", 0);
    }

    /**
     * Get a list of policy names and the primary state for a given package.
     * @param pkgName
     * @return Map of Policy names and the primary state
     * @throws Exception
     */

    //REMIND: Passing in a request object is bad design, we have to do this here because the LDAP
    // api's need the request object.
    public List getPolicies(String pkgName, HttpServletRequest request) throws Exception {

        return getPolicies(pkgName, 0,request);
    }

    public List getPolicies(String pkgName, int page, HttpServletRequest request) throws Exception   {

        ListResult lr = null;
        ServletContext ctx = main.getServletContext();
        SubscriptionMain submain = TenantHelper.getTenantSubMain(ctx, request);
        IUser  user = GUIUtils.getUser(request);
        PolicyListQuery policyQry = new PolicyListQuery(pkgName);
        policyQry.setUser(this.user);
        policyQry.setTenant(tenant);
        setPageInfo(policyQry, page);
        setCacheInfoForList(policyQry);
        policyQry.executeQuery();
        IComplianceResult cr = policyQry.getResult();

        if (cr != null && cr instanceof ListResult) {
            lr = (ListResult) cr;
            lr = getACLFilteredResults(request,lr,user,submain);
            return lr.get(page);
        } else {
            // Handle error here
            return new ArrayList();
        }

    }

    private ListResult getACLFilteredResults(HttpServletRequest req, ListResult results, IUser user, SubscriptionMain submain) throws Exception {

        //listing = LDAPWebappUtils.parseLDAPTargetResults(req, results, channellist, subBase, childContainer);
        boolean primaryAdmin = Utils.isPrimaryAdmin(req);

        if (!primaryAdmin && submain.isAclsOn()) {
            if(DEBUG){
                sLogger.info("Acl is turned on..getting ACL Filtered results in PackageViewService");
            }
            Iterator iter = results.iterator();
            while(iter.hasNext()){
                PackagePolicyDetails ppd = (PackagePolicyDetails) iter.next();
                Target target  = ppd.getTarget();
                int actions = submain.getAclMgr().permissionExists( IAclConstants.SUBSCRIPTION_PERMISSION,
                        user, target, null, true, submain.getUsersInLDAP());

                if ((actions & IAclConstants.READ_ACTION_INT) == 0) {
                    if (DEBUG) {
                        sLogger.info("Removing " + target.toString() + " from resultset");
                    }
                    iter.remove();
                }

            }
        }
        return results;
    }

    /**
     * Populate policy list data with compliance information in the cache
     */
    public void getCachedCompliance(String pkgUrl, List policies) {
        Iterator i = policies.iterator();
        while (i.hasNext()) {
            PackagePolicyDetails pd = (PackagePolicyDetails) i.next();
            ComplianceSummaryQuery summaryQry = new ComplianceSummaryQuery();
            summaryQry.setArgs(pd.getTargetId(), pd.getTargetId(), pkgUrl);
            IComplianceQuery qry = cache.get(summaryQry);
            if (qry != null) {
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
            }
        }
    }

    private void error(String s,Throwable t){
        if(isDebug)
            sLogger.error(s,t);
    }

    private void log(String s){
        if(isDebug)
            sLogger.info(s);
    }
    /**
     * Checks the format of the Date/Time entered by user is based on locale. If entered date is in correct
     * format then we change the locale-based time format to Marimba Date format to proceed further.
     * @param dateStr  DateTime String passed from JSP
     * @param localeStr  String generated to create locale object.
     * @return
     */
    public String isValidDateTime( String dateStr, String localeStr ) {
        if( DEBUG ){
            System.out.println( "PackageViewService.isValidDateTime " );
            System.out.println( "Input PublishedAfter "+dateStr );
            System.out.println( "Input Locale "+localeStr );
        }
        StringTokenizer  st = new StringTokenizer(localeStr, ",");
        String language = "";
        String country = "";

        if(st.countTokens()==2) {
            language = st.nextToken();
            country = st.nextToken();
        } else {
            language = st.nextToken();
        }

        userLocale = new Locale(language, country);

          try {
              SimpleDateFormat sdf = WebUtil.getComplianceDateFormat( userLocale );
              sdf.setLenient( false );
              Date publishDate = sdf.parse( dateStr );
              String compDateStr = WebUtil.getComplianceDateFormat( userLocale ).format( publishDate );
              if( DEBUG ){
                  System.out.println( "Parsed PublishedAfter "+publishDate );
                  System.out.println( "Formatted PublishedAfter "+compDateStr );
              }
              return compDateStr;
          } catch (ParseException e) {
                if(isDebug) {
                    System.out.println(" ParseException  "+ e );
                }
          } catch (IllegalArgumentException e) {
                if(isDebug) {
                    System.out.println(" IllegalArgumentException  "+ e );
                }
          }
        return "invalid";
    }

}

