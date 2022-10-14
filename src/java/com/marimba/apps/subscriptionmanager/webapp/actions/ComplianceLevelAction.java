package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;
import com.marimba.apps.subscriptionmanager.compliance.service.ReportService;
import com.marimba.apps.subscriptionmanager.compliance.view.ReportBean;
import com.marimba.apps.subscriptionmanager.compliance.view.MachineBean;
import com.marimba.apps.subscriptionmanager.compliance.view.ComplianceSummaryBean;
import com.marimba.apps.subscriptionmanager.compliance.util.WebUtil;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.webapps.intf.IMapProperty;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.ArrayList;
import java.sql.Date;

/**
 * Created by IntelliJ IDEA.
 * User: nrao
 * Date: Jul 9, 2005
 * Time: 1:06:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class ComplianceLevelAction extends AbstractAction
    implements IWebAppConstants, ISubscriptionConstants, ComplianceConstants {

    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
        return new ComplianceLevelAction.CompLevelTask(mapping, form, request, response);
    }

    protected class CompLevelTask extends SubscriptionDelayedTask {
        CompLevelTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
            super(mapping, form, request, response);
        }

        public void execute(){
            GUIUtils.initForm(request, mapping);
            HttpSession session = request.getSession();
            init(request);
            IMapProperty props = (IMapProperty) form;
            String compLevel = ( String )props.getValue("compLevel");
            String calcId = ( String )props.getValue("calcId");

            int calculationId = 0;
            if( calcId != null ){
                calculationId = Integer.parseInt( calcId ); 
            }

            List machineList = null;
            if( calculationId != -1 ){
                ReportService reportService = (ReportService) session.getAttribute(COMPLIANCE_REPORT_SERVICE);
                if( "success".equals( compLevel ) ){
                    machineList = ( reportService.getMachines( calculationId, STR_LEVEL_COMPLIANT, 0, false ) ).getList();
                } else if( "failed".equals( compLevel ) ) {
                    machineList = ( reportService.getMachines( calculationId, STR_LEVEL_NON_COMPLIANT, 0, false ) ).getList();
                    WebUtil.checkEndpointState( machineList, request, resources );
                } else if( "nci".equals( compLevel ) ){
                    machineList = ( reportService.getMachines( calculationId, STR_LEVEL_NOT_CHECK_IN, 0, false ) ).getList();
                }
                ReportBean reportBean = reportService.getReport( calculationId );
                ComplianceSummaryBean csb = reportService.getComplianceSummary(calculationId,false);
                reportBean.setReportSummary( csb );
                session.setAttribute( "reportBean", reportBean );
            }
            session.removeAttribute( IWebAppConstants.SESSION_POLICIES_DETAILS );
            if( machineList != null ){
                session.setAttribute( IWebAppConstants.COMPRPT_CACHERPTS, machineList );
            } else {
                session.removeAttribute( IWebAppConstants.COMPRPT_CACHERPTS );
            }
            form.reset( mapping, request );
            if( compLevel != null ){
                session.setAttribute( "compLevel", compLevel );
                forward = mapping.findForward( "success" );
            } else {
                forward = mapping.findForward( "failed" );
            }
        }
    }
}
