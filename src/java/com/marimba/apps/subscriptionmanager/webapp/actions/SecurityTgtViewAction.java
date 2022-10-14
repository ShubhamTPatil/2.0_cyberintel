// Copyright 2018, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.securitymgr.compliance.SecurityCompliance;
import com.marimba.apps.securitymgr.utils.SecurityLDAPUtils;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.TenantAttributes;
import com.marimba.apps.subscriptionmanager.beans.SecurityTargetDetailsBean;
import com.marimba.apps.subscriptionmanager.compliance.core.ComplianceMain;
import com.marimba.apps.subscriptionmanager.compliance.core.ConfigManager;
import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;
import com.marimba.apps.subscriptionmanager.compliance.service.ComplianceSummaryService;
import com.marimba.apps.subscriptionmanager.compliance.service.TargetViewService;
import com.marimba.apps.subscriptionmanager.compliance.util.WebUtil;
import com.marimba.apps.subscriptionmanager.compliance.view.*;
import com.marimba.apps.subscriptionmanager.intf.IARTaskConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.users.UserManager;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.intf.msf.acl.IAclConstants;
import com.marimba.webapps.intf.CriticalException;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.SystemException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: nrao
 * Date: May 12, 2005
 * Time: 4:49:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class SecurityTgtViewAction
        extends AbstractAction implements IWebAppConstants, ISubscriptionConstants, ComplianceConstants {

    protected boolean useURIMapping(){
        return true;
    }

    /**
     * @param mapping REMIND
     * @param form REMIND
     * @param request REMIND
     * @param response REMIND
     *
     * @return REMIND
     */

    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
        return new SecurityTgtViewAction.SecurityViewTask(mapping, form, request, response);
    }

    protected class SecurityViewTask extends SubscriptionDelayedTask {
        SecurityViewTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
            super(mapping, form, request, response);
        }

        public void execute(){
            init(request);
            GUIUtils.initForm(request, mapping);
            System.out.println("Security Target view action called..");
            try {
                GUIUtils.setToSession(request, "context", "compTarget");
            } catch (SystemException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            String view = request.getParameter( "view" );
            String results = request.getParameter( "results" );
            String taskid = (String)session.getAttribute(IARTaskConstants.AR_TASK_ID);



            HttpSession session = request.getSession();
            Target target = null;
            try {
            	if(null != session.getAttribute("allendpointdashboard")) {
            		session.setAttribute("allendpointdashboard", null);
                    session.setAttribute("action1", "overall_dashboard");
            		target = new Target("All Endpoints", "all", "all");
            		session.setAttribute(SELECTED_TARGET_ID, "all");
	    			session.setAttribute(SELECTED_TARGET_NAME, "All Endpoints");
	    			session.setAttribute(SELECTED_TARGET_TYPE, "all");
            	} else {
                    target = getTarget(request, main);
            	}

                if( target == null ) {
                    target = ( Target )session.getAttribute( "target" );
                } else {
                    session.setAttribute( "target", target );
                }

                if(target == null && (taskid != null)) {
                    List targetList = null;
                    if(session.getAttribute(MAIN_PAGE_TARGET) != null) {
                        targetList = (List)session.getAttribute(MAIN_PAGE_TARGET);
                    } else {
                        targetList = (List)session.getAttribute(MAIN_PAGE_M_TARGETS);
                    }
                    // if found multiple targets only getting the first target in the list.
                    if (targetList != null && !targetList.isEmpty() ) {
                        target = (Target) targetList.get(0);
                    }
                }

                List display_results = null;
                ComplianceSummaryBean summaryResult = null;
                if( target != null ){
                    // Prepare target id for javascript
                    String targetId = (target.getId() == null)? "" : target.getId().toLowerCase();
                    targetId = "all".equalsIgnoreCase(targetId) ? "all_all" : targetId;
                    GUIUtils.setToSession(request, "comp_target_id", targetId);
                    GUIUtils.setToSession(request, "target", (Object) target);
                    ServletContext ctx = session.getServletContext();
                    String tenantName = (String) session.getAttribute(SESSION_TENANTNAME);
                    TenantAttributes tenantAttr = UserManager.getTenantAttr(tenantName);
                    ComplianceMain cm = tenantAttr.getCompMain();
                    IUser _user = null;
                    try {
                        _user = GUIUtils.getUser(request);

                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    boolean hasReadPermission = hasPolicyReadPermission(target,_user);
                    if( hasReadPermission ){
                        session.setAttribute( IWebAppConstants.NO_ACL_PERMISSION, "false" );
                    } else{
                        session.setAttribute( IWebAppConstants.NO_ACL_PERMISSION, "true" );
                    }
                    if (target.getType().equals( TYPE_MACHINE ) && view == null) {
                        forward = mapping.findForward("targetOverview");
                        return ;
                    }
                    if( view == null ) view = "overview";

                    if(DEBUG) System.out.println( "view is "+view );

                    if (GRP_COMP_OVERVIEW_RSLT_TYPE.equals(view) && !target.getType().equals( TYPE_MACHINE )) {
                        forward = mapping.findForward("overview");
                        return;
                    } else if( GRP_COMP_TARGET_RSLT_TYPE.equals(view) ) {

                        TargetViewService targetService = (TargetViewService)
                                session.getAttribute(ComplianceConstants.TARGET_VIEW_SERVICE);
                        if( !hasReadPermission ) {
                            display_results = new ArrayList();
                            summaryResult = new ComplianceSummaryBean();
                            session.setAttribute(IWebAppConstants.SESSION_COMP_HASPOLICIES, "false");
                            session.removeAttribute(IWebAppConstants.SESSION_POLICY_LASTUPDATED);
                        } else if( target.getType().equals( TYPE_MACHINE )) {
                            display_results = SecurityLDAPUtils.getAssignedPolicy(SECURITY_SCAN_TYPE_XCCDF, target.getName(), target.getType(), target.getId(), main, _user);
                            if( display_results != null && display_results.size() > 0 ) {
                                for (int i = 0; i<display_results.size(); i++) {
                                    SecurityTargetDetailsBean securityTargetDetailsBean = (SecurityTargetDetailsBean) display_results.get(i);
                                    System.out.println("Get machine compliance :" + securityTargetDetailsBean.getAssginedToName());
                                    new SecurityCompliance.GetMachineCompliance(main,securityTargetDetailsBean);
                                    if(null == securityTargetDetailsBean.getComplaintLevel() || "".equals(securityTargetDetailsBean.getComplaintLevel())) {
                                    	securityTargetDetailsBean.setComplaintLevel(NOT_CHECKEDIN);
                                    }
                                }
                            }
                        } else {
                            display_results = SecurityLDAPUtils.getAssignedPolicy(SECURITY_SCAN_TYPE_XCCDF, target.getName(), target.getType(), target.getId(), main, _user);
                            List<MachineBean> allMachineList = new SecurityCompliance.GetTargetMachine(main, target.getId()).getMachineBeanList();
                            //only calculate the compliance if the app is invoked from AR System
                            if( display_results != null && display_results.size() > 0 ) {
                                for (int i = 0; i<display_results.size(); i++) {
                                    SecurityTargetDetailsBean securityTargetDetailsBean = (SecurityTargetDetailsBean) display_results.get(i);
                                    System.out.println("Get group compliance :" + securityTargetDetailsBean.getAssginedToName());
                                    MachineListBean machineListBean = new SecurityCompliance.GetGroupCompliance(main,securityTargetDetailsBean, target.getId()).getMachineListBean();

                                	List<MachineBean> machineBeanList = machineListBean == null ? new ArrayList<MachineBean>() : machineListBean.getList();


                                    if( results == null ){
                                        results = PKG_FAILED;
                                    }
                                    int complaint=0, noncompliant=0, notapplicable=0, notcheckedin = 0;
                                    if (machineBeanList != null) {
                                        for (MachineBean machineBean: machineBeanList) {
                                            String complianceLevel = machineBean.getComplianceLevel();
                                            if(COMPLAINT.equals(complianceLevel)) {
                                                complaint++;
                                            } else if(NON_COMPLAINT.equals(complianceLevel)) {
                                                noncompliant++;
                                            }  else if(NOT_APPLICABLE.equals(complianceLevel)) {
                                                notapplicable++;
                                            }
                                        }
                                        System.out.println("machineBeanList size :" + machineBeanList.size());
                                    }

                                    System.out.println("All machineBeanList size :" + allMachineList.size());
                                    if(null == machineBeanList || machineBeanList.size() == 0) {
                                    	notcheckedin = allMachineList.size();
                                    } else {
                                        if (allMachineList.size() > 0) {
                                            notcheckedin = allMachineList.size() - (complaint + noncompliant+ notapplicable);
                                        }
                                    }
                                    securityTargetDetailsBean.setCheckinCount(Integer.toString(notcheckedin));
                                    securityTargetDetailsBean.setCompliantCount(Integer.toString(complaint));
                                    securityTargetDetailsBean.setNonCompliantCount(Integer.toString(noncompliant));
                                    securityTargetDetailsBean.setNotApplicableCount(Integer.toString(notapplicable));
                                }
                                String lastUpdated = null;
//                                if( ( lastUpdated = getPolicyLastUpdated( target.getId(), display_results, request.getLocale() ) ) != null ){
//                                    session.setAttribute( IWebAppConstants.SESSION_POLICY_LASTUPDATED, lastUpdated );
//                                }
                                session.removeAttribute( IWebAppConstants.SESSION_COMP_HASPOLICIES );
                            } else {
                                session.setAttribute( IWebAppConstants.SESSION_COMP_HASPOLICIES, "false" );
                            }
                        }
                    } else if( GRP_COMP_OVERALL_RSLT_TYPE.equals(view)) {
                        display_results = new ArrayList<MachineBean>();
                        summaryResult = new ComplianceSummaryBean();
                        if( !hasReadPermission ){
                            display_results = new ArrayList();
                            summaryResult = new ComplianceSummaryBean();
                        } else{
                        	if( results == null ){
                                results = PKG_FAILED;
                            }
                        	int complaint=0, noncompliant=0, notapplicable=0, notcheckedin = 0, total = 0;
	                        List<MachineBean> allMachineList = new SecurityCompliance.GetTargetMachine(main, target.getId()).getMachineBeanList();
	                        System.out.println("allMachineList size :" + allMachineList.size());
	                        if(null == allMachineList || allMachineList.size() == 0) {

	                        } else {
	                        	for(MachineBean machineBean : allMachineList) {
	                        		System.out.println("Gettign result for :" + machineBean.getMachineName());
	                        		String overallComplianceLevel = new SecurityCompliance.GetOverallCompliance(main, target.getId(), machineBean.getMachineName(), machineBean.getMachineID()).getOverallComplianceLevel();
	                        		System.out.println("overallComplianceLevel :" + overallComplianceLevel);
	                        		if(COMPLAINT.equals(overallComplianceLevel)) {
	                        			System.out.println("adding compliant lelvel");
	                        			complaint++;
	                        			total++;
	                        			if(PKG_SUCCESS.equals(results)) {
	                        				display_results.add(machineBean);
	                        			}
	                        		} else if(NON_COMPLAINT.equals(overallComplianceLevel)) {
	                        			noncompliant++;
	                        			total++;
	                        			if(PKG_FAILED.equals(results) ) {
	                        				display_results.add(machineBean);
	                        			}
	                        		} else if(NOT_APPLICABLE.equals(overallComplianceLevel)) {
                                        notapplicable++;
                                        total++;
                                        if(PKG_NOT_APPLICABLE.equals(results) ) {
                                            display_results.add(machineBean);
                                        }
                                    } else if(NOT_CHECKEDIN.equals(overallComplianceLevel)) {
	                        			if(PKG_NOTCHECKIN.equals(results) ) {
	                        				display_results.add(machineBean);
	                        			}
	                        			notcheckedin++;
	                        			total++;
	                        		}
		                        }
	                        }
	                        System.out.println("compliant :" + complaint);
	                        summaryResult.setCompliant(complaint);
	                        summaryResult.setNoncompliant(noncompliant);
	                        summaryResult.setNotcheckedin(notcheckedin);
                            summaryResult.setNotapplicable(notapplicable);
	                        summaryResult.setTotal(total);

	                        ConfigManager cfm = ( cm == null ) ? null : cm.getConfig();
	                        int checkInLimit = (cfm == null) ? -48 : cfm.getInt( ConfigManager.CFG_CHECKIN_LIMIT );
	                        request.setAttribute( ConfigManager.CFG_CHECKIN_LIMIT, ""+checkInLimit );

	                        // Get the list for the result
	                        // display_results = mlb.getList();
	                        // end of checking user permission
	                        session.setAttribute( "results", results );
	                        GUIUtils.setToSession( request, IWebAppConstants.COMP_SUMMARY_RESULT, summaryResult);
	                        if( summaryResult.getComplianceCalcTime() != 0L ){
	                            java.util.Date compCalcDate = new java.util.Date( summaryResult.getComplianceCalcTime() );
	                            request.setAttribute( IWebAppConstants.SESSION_COMP_LASTCALCULATED, WebUtil.getComplianceDateFormat().format( compCalcDate ) );
	                        }
	                    }
                    }
                } // end of checking valid target

                if( display_results != null){
                    session.removeAttribute( IWebAppConstants.SESSION_POLICIES_DETAILS );
                    GUIUtils.setToSession( request, IWebAppConstants.POLICIES_FORTGTNAME, target.getName() );
                    GUIUtils.setToSession( request, IWebAppConstants.POLICIES_DETAILS_FORTGT, display_results );
                    GUIUtils.setToSession( request, IWebAppConstants.SESSION_DISPLAY_RS, display_results );
                } else {
                    // will be executed when there is no selected target
                    session.removeAttribute( IWebAppConstants.SESSION_COMP_HASPOLICIES );
                    session.removeAttribute( "results" );
                    session.removeAttribute( IWebAppConstants.SESSION_POLICY_LASTUPDATED );
                    session.removeAttribute(IWebAppConstants.SESSION_POLICIES_DETAILS);
                    session.removeAttribute(IWebAppConstants.POLICIES_FORTGTNAME);
                    session.removeAttribute(IWebAppConstants.POLICIES_DETAILS_FORTGT);
                    session.removeAttribute(IWebAppConstants.COMP_SUMMARY_RESULT);
                    session.removeAttribute(IWebAppConstants.SESSION_COMP_LASTCALCULATED);
                    session.removeAttribute(IWebAppConstants.NO_ACL_PERMISSION);
                }
                session.setAttribute( "view", view );
                forward = mapping.findForward("success");
            } catch (SystemException se) {
                guiException = new GUIException( se );
                forward = mapping.findForward("failure");
            } catch ( Exception exp ){
                exp.printStackTrace();
                guiException = new GUIException( ( new CriticalException ( exp.getMessage() ) ) );
                forward = mapping.findForward("failure");
            }
            Object bean = session.getAttribute(IWebAppsConstants.INITERROR_KEY);

            if ((bean != null) && bean instanceof Exception) {
                //remove initerror from the session because it has served its purpose
                if (DEBUG) {
                    System.out.println("ComplianceTgtViewAction: critical exception found");
                }
                session.removeAttribute(IWebAppsConstants.INITERROR_KEY);
                forward = mapping.findForward("failure");
            }
        }

        private boolean hasPolicyReadPermission(Target tgt,IUser user){
            boolean hasRead = false;
            try {
                hasRead = ObjectManager.checkSubPerm( user, tgt.getId(), tgt.getType(), IAclConstants.READ_ACTION ) ||
                        ObjectManager.checkSubPerm( user, tgt.getId(), tgt.getType(), IAclConstants.WRITE_ACTION );
            }catch(Exception e){
                hasRead=false;
                e.printStackTrace();
            }
            return hasRead;
        }

        private String getPolicyLastUpdated( String policyId, List pkgList, Locale locale ){
            SimpleDateFormat sdf = (SimpleDateFormat)DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
            String policyUpdated = null;
            Iterator iterator = pkgList.iterator();
            PackagePolicyDetails pkgPolicy = null;
            while( iterator.hasNext() ){
                pkgPolicy = ( PackagePolicyDetails )iterator.next();
                if( pkgPolicy.getTargetId().equalsIgnoreCase( policyId ) ){
                    policyUpdated = pkgPolicy.getPolicyLastUpdated();
                    try {
                        java.util.Date date = WebUtil.getComplianceDateFormat().parse(policyUpdated);
                        policyUpdated = sdf.format(date);
                    } catch (ParseException e) {
                        if (DEBUG) {
                            System.out.println("Failed to parse the policy last updated datetime");
                        }
                    }
                }
            }
            return policyUpdated;
        }
    }

    public void calculateARCompliance( List display_results, HttpServletRequest request ){
        PackagePolicyDetails packagePolicies = null;
        HttpSession session = request.getSession();
        for( int indx = 0; indx < display_results.size(); indx++ ){
            packagePolicies = ( PackagePolicyDetails )display_results.get( indx );
            ComplianceSummaryService complianceService = (ComplianceSummaryService) session.getAttribute(ComplianceConstants.COMPLIANCE_SUMMARY_SERVICE);
            if(packagePolicies.getTargetType().equals(TYPE_SITE)) {
                complianceService.getSiteComplianceSummary(packagePolicies.getTargetId(), packagePolicies.getTargetId(), packagePolicies.getPackageUrl(), true);
            } else {
                complianceService.getComplianceSummary(packagePolicies.getTargetId(), packagePolicies.getTargetId(), packagePolicies.getPackageUrl(), true);
            }

            if(DEBUG) {
                System.out.println( " Target ID "+packagePolicies.getTargetId() );
                System.out.println( " Target type "+packagePolicies.getTargetType() );
                System.out.println( " Target Name "+packagePolicies.getTargetName() );
                System.out.println( " Pakcage State "+packagePolicies.getPrimaryState() );
                System.out.println( " Policy Last updated "+packagePolicies.getPolicyLastUpdated() );
            }
        }
    }
}
