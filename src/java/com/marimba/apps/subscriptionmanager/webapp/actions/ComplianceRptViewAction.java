package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.*;
import com.marimba.apps.subscription.common.intf.*;
import com.marimba.apps.subscription.common.objects.*;
import com.marimba.apps.subscriptionmanager.*;
import com.marimba.apps.subscriptionmanager.compliance.core.*;
import com.marimba.apps.subscriptionmanager.compliance.intf.*;
import com.marimba.apps.subscriptionmanager.compliance.service.*;
import com.marimba.apps.subscriptionmanager.compliance.view.*;
import com.marimba.apps.subscriptionmanager.intf.*;
import com.marimba.apps.subscriptionmanager.users.UserManager;
import com.marimba.intf.msf.IUserPrincipal;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.apps.subscriptionmanager.webapp.util.*;
import com.marimba.webapps.intf.*;
import com.marimba.webapps.tools.action.*;
import com.marimba.intf.msf.acl.*;
import org.apache.log4j.*;
import org.apache.struts.action.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;

/**
 * For compliance report related actions
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
public class ComplianceRptViewAction extends AbstractAction
    implements IWebAppConstants, ISubscriptionConstants, ComplianceConstants {

    static final Logger sLogger = Logger.getLogger(ComplianceRptViewAction.class);
    private static boolean isDebug = IAppConstants.DEBUG;


    protected DelayedAction.Task createTask( ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response ) {
        return new  ComplianceRptViewAction.CompRptViewTask( mapping, form, request, response );
    }

    protected boolean useURIMapping(){
        return true;
    }
    protected class CompRptViewTask
               extends SubscriptionDelayedTask  {
        CompRptViewTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
                  super(mapping, form, request, response);
        }

        public void execute(){
            IMapProperty props = (IMapProperty) form;
            String reportAction = ( String )props.getValue("reportaction");
            HttpSession session = request.getSession();
        	init(request);
            Target target = ( Target )session.getAttribute( "target" );

            try{
                ReportService reportService = (ReportService) session.getAttribute(COMPLIANCE_REPORT_SERVICE);
                // sending wild card to list out all the available reports
                List reportList = reportService.getCachedReports( "", 0 );

                try {
                    if(DEBUG){
                    sLogger.info("Before ACL Filtering compliance reports : list size = " + ((reportList==null)?0:reportList.size()));
                    }
                    reportList = performACLFilter(reportList);
                     if(DEBUG){
                    sLogger.info("After ACL Filtering compliance reports : list size = " + ((reportList==null)?0:reportList.size()));
                    }
                } catch(Exception e) {
                    sLogger.error("Error while enforcing ACL filter on Compliance Report list (ComplianceRptViewAction) ");
                }
                Iterator it = reportList.iterator();
                while(it.hasNext()){
                    ReportBean rb = (ReportBean) it.next();
                    int calcID = rb.getCalculationId();
                    ComplianceSummaryBean csb = reportService.getComplianceSummary(calcID,false);
                    rb.setReportSummary(csb);
                }
                session.removeAttribute( IWebAppConstants.SESSION_POLICIES_DETAILS );
                GUIUtils.setToSession( request, IWebAppConstants.COMPRPT_CACHERPTS, reportList );
                forward = mapping.findForward( "success" );
            } catch (SystemException se) {
                se.printStackTrace();
                guiException = new GUIException(se);
            } catch(Exception ex) {
            	forward = mapping.findForward("failure");
            }
            Object bean = request.getSession().getAttribute(IWebAppsConstants.INITERROR_KEY);

        	if ((bean != null) && bean instanceof Exception) {
        		//remove initerror from the session because it has served its purpose
        		if (DEBUG) {
        			System.out.println("ComplianceRptViewAction: critical exception found");
        		}
        		request.getSession().removeAttribute(IWebAppsConstants.INITERROR_KEY);
        		forward = mapping.findForward("failure");
        	}
        }


        private List performACLFilter(List toFilter) throws Exception {
            ServletContext ctx = session.getServletContext();
            IUser _user = GUIUtils.getUser(request);
            String tenantName = (String) session.getAttribute(SESSION_TENANTNAME);
            TenantAttributes tenantAttr = UserManager.getTenantAttr(tenantName);
            ComplianceMain cm = tenantAttr.getCompMain();
            SubscriptionMain sm = cm.getSubscriptionMain();
            // already another utils used so specify with package name
            boolean primaryAdmin = com.marimba.apps.subscriptionmanager.util.Utils.isPrimaryAdmin(request);
            if (!primaryAdmin && sm.isAclsOn()) {
                if(DEBUG){
                    sLogger.info("Acl is turned on..filtering report results in ComplianceRptViewAction");
                }
                Iterator iter = toFilter.iterator();
                while(iter.hasNext()){
                    ReportBean  rb = (ReportBean) iter.next();
                    //TODO find a better way to get a target than creating a new target object in this while loop (Manoj)
                    Target target  = new Target(rb.getTarget(),null,null);
                    int actions = sm.getAclMgr().permissionExists( IAclConstants.SUBSCRIPTION_PERMISSION,
                            _user, target, null, true, sm.getUsersInLDAP());

                    if ((actions & IAclConstants.READ_ACTION_INT) == 0) {
                        if (DEBUG) {
                            sLogger.info("Removing " + target.toString() + " from report list");
                        }
                        iter.remove();
                    }

                }
            }


            return toFilter;
        }

    }
}
