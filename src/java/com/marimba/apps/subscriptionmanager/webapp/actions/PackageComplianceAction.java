package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;

import com.marimba.apps.subscriptionmanager.TenantAttributes;
import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;
import com.marimba.apps.subscriptionmanager.compliance.core.ComplianceMain;
import com.marimba.apps.subscriptionmanager.compliance.service.PackageViewService;
import com.marimba.apps.subscriptionmanager.compliance.service.ComplianceSummaryService;
import com.marimba.apps.subscriptionmanager.compliance.view.PackagePolicyDetails;
import com.marimba.apps.subscriptionmanager.users.UserManager;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IARTaskConstants;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.intf.CriticalException;
import com.marimba.webapps.tools.action.DelayedAction;
import com.marimba.webapps.tools.util.PropsBean;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: os-nrao
 * Date: Apr 29, 2005
 * Time: 10:51:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class PackageComplianceAction extends AbstractAction implements ISubscriptionConstants {
    protected DelayedAction.Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
                     return new  PackageComplianceAction.PkgCompTask(mapping, form, request, response);
    }

    protected class PkgCompTask
               extends SubscriptionDelayedTask  {
        PkgCompTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
                  super(mapping, form, request, response);
        }

        public void execute() {

            try {
            	init(request);
                HttpSession      session = request.getSession();
                GUIUtils.initForm(request, mapping);

                String channelURL = request.getParameter("channelURL");
                String channelTitle = request.getParameter("channelTitle");
                String content_type = request.getParameter("content_type");

                String taskid = (String)session.getAttribute(IARTaskConstants.AR_TASK_ID);

                if( (taskid != null) && (channelURL == null)) {
                    ArrayList channels = (ArrayList)session.getAttribute(IARTaskConstants.AR_COMPLIANCE_CHANNEL_DISPLAY);
                    if (channels != null && !channels.isEmpty()) {
                        PropsBean bean = (PropsBean)channels.get(0);
                        channelURL = (String)bean.getValue("url");
                        channelTitle = (String)bean.getValue("title");
                        // Todo content_type is required in AR_TASK ?
                    }
                } else if( channelURL != null ){
                    session.setAttribute( "channelURL", channelURL );
                    session.setAttribute( "channelTitle", channelTitle );
                    session.setAttribute( "content_type", content_type );
                }

                if (DEBUG) {
                    System.out.println("channelURL is " + channelURL);
                    System.out.println("channelTitle is " + channelTitle);
                    System.out.println("content_type is " + content_type);
                }

                if (channelURL != null) {
                    try {
                        ServletContext ctx = session.getServletContext();
                        String tenantName = (String) session.getAttribute(SESSION_TENANTNAME);
                        TenantAttributes tenantAttr = UserManager.getTenantAttr(tenantName);
                        ComplianceMain cm = tenantAttr.getCompMain();
                        //IUserPrincipal _user = (IUserPrincipal) session.getAttribute("session_smuser");
                        PackageViewService pkgViewService = (PackageViewService) session.getAttribute(ComplianceConstants.PACKAGE_VIEW_SERVICE);
                        List pkgPolicies = pkgViewService.getPolicies( channelURL , request);

                        if( taskid != null) {
                            PackagePolicyDetails pkgPolicyDetails = null;
                            ComplianceSummaryService complianceService = null;
                            for( int indx = 0; indx < pkgPolicies.size(); indx++ ) {
                                pkgPolicyDetails = (PackagePolicyDetails) pkgPolicies.get(indx);
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
                                System.out.println( " Content Type "+packagePolicies.getPackageType() );
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
                    } catch (SystemException se) {
                        GUIException guie = new GUIException(se);
                        throw guie;
                    }
                    finally{
                    }
                }
                forward = mapping.findForward("success");
            } catch ( Exception exp ) {
                exp.printStackTrace();
                guiException = new GUIException(new CriticalException(exp.toString()));
                forward = mapping.findForward("failure");
            }
        }
    }
}
