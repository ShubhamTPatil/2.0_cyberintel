// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions.ccm;

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IARTaskConstants;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;
import com.marimba.apps.subscriptionmanager.compliance.service.PackageViewService;
import com.marimba.apps.subscriptionmanager.compliance.service.ComplianceSummaryService;
import com.marimba.apps.subscriptionmanager.compliance.view.PackagePolicyDetails;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.webapps.tools.util.PropsBean;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.CriticalException;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * This class handles displaying the package compliance information which is invoked from AR.
 *
 * @author  Jayaprakash Paramasivam
 * @version 1.0, 11/23/2005
 */


public final class CCMCompliancePackageAction
        extends AbstractAction implements IWebAppConstants, ISubscriptionConstants {
    /**

     *
     * @param mapping REMIND
     * @param form REMIND
     * @param request REMIND
     * @param response REMIND
     *
     * @return REMIND
     *
     */
    public ActionForward perform(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
        throws IOException,
            ServletException {
        GUIUtils.initForm(request, mapping);
        //To remove the targets list maintained in the session
        HttpSession session = request.getSession();
        session.removeAttribute( IWebAppConstants.POLICIES_FORPKGNAME );
        session.removeAttribute( IWebAppConstants.SESSION_DISPLAY_RS );

        String channelURL = null;
        String channelTitle = null;
        String taskid = (String) session.getAttribute(IARTaskConstants.AR_TASK_ID);
        GUIException guie = null;
        if (taskid == null)     {
            guie = new GUIException(new CriticalException(AR_NO_TASK_ID));
            throw guie;
        }

        ArrayList channels = (ArrayList)session.getAttribute(IARTaskConstants.AR_COMPLIANCE_CHANNEL_DISPLAY);
        if (channels != null && !channels.isEmpty()) {
            PropsBean bean = (PropsBean)channels.get(0);
            channelURL = (String)bean.getValue("url");
            channelTitle = (String)bean.getValue("title");
            session.setAttribute("channelURL", channelURL);
            session.setAttribute("channelTitle", channelTitle);
        }

        if (channelURL != null) {
            try {
                ServletContext ctx = session.getServletContext();
                PackageViewService pkgViewService = (PackageViewService) session.getAttribute(ComplianceConstants.PACKAGE_VIEW_SERVICE);
                List pkgPolicies = pkgViewService.getPolicies( channelURL , request);

                if( taskid != null) {
                    PackagePolicyDetails pkgPolicyDetails = null;
                    ComplianceSummaryService complianceService = null;
                    for( int index = 0; index < pkgPolicies.size(); index++ ) {
                        pkgPolicyDetails = (PackagePolicyDetails) pkgPolicies.get(index);
                        complianceService = (ComplianceSummaryService) session.getAttribute(ComplianceConstants.COMPLIANCE_SUMMARY_SERVICE);
                        if(pkgPolicyDetails.getTargetType().equals(TYPE_SITE)) {
                        	complianceService.getSiteComplianceSummary(pkgPolicyDetails.getTargetId(), pkgPolicyDetails.getTargetId(), channelURL, true);
                        } else {
                        	complianceService.getComplianceSummary(pkgPolicyDetails.getTargetId(), pkgPolicyDetails.getTargetId(), channelURL, true);
                        }
                        
                    }
                }

                if (DEBUG) {
                    System.out.println( "Number of policies found are "+pkgPolicies.size() );
                    PackagePolicyDetails packagePolicies = null;
                    for( int indx = 0; indx < pkgPolicies.size(); indx++ ){
                        packagePolicies = ( PackagePolicyDetails )pkgPolicies.get( indx );
                        System.out.println( " Target ID "+packagePolicies.getTargetId() );
                        System.out.println( " Target type "+packagePolicies.getTargetType() );
                        System.out.println( " Target Name "+packagePolicies.getTargetName() );
                        System.out.println( " Pakcage State "+packagePolicies.getPrimaryState() );
                        System.out.println( " Policy Last updated "+packagePolicies.getPolicyLastUpdated() );
                    }
                }

                // Let's get the cached compliance data
                pkgViewService.getCachedCompliance(channelURL, pkgPolicies);

                if( pkgPolicies != null){
                    session.removeAttribute( IWebAppConstants.SESSION_PACKAGE_POLICIES );
                    //GUIUtils.setToSession( request, IWebAppConstants.POLICIES_FORPKGNAME, channelTitle);
                    GUIUtils.setToSession( request, IWebAppConstants.PACKAGE_DETAILS_FORPKG, pkgPolicies );
                } else {
                    session.removeAttribute(IWebAppConstants.SESSION_PACKAGE_POLICIES);
                    session.removeAttribute(IWebAppConstants.PACKAGE_DETAILS_FORPKG);
                }
                } catch (Exception e) {
                    guie = new GUIException(e);
                    throw guie;
            }
        }

        return (new ActionForward(mapping.getInput(), true));
    }
}