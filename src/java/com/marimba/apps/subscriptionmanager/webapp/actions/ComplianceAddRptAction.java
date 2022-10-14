package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.webapps.tools.action.DelayedAction;
import com.marimba.webapps.intf.IMapProperty;
import com.marimba.apps.subscriptionmanager.compliance.service.ReportService;
import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.ISubscriptionConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by IntelliJ IDEA.
 * User: nrao
 * Date: Aug 10, 2005
 * Time: 4:40:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class ComplianceAddRptAction extends AbstractAction
    implements IWebAppConstants, ISubscriptionConstants, ComplianceConstants {

    protected DelayedAction.Task createTask( ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response ) {
        return new  ComplianceAddRptAction.CompAddRptTask( mapping, form, request, response );
    }

    protected class CompAddRptTask
               extends SubscriptionDelayedTask  {
        CompAddRptTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
                  super(mapping, form, request, response);
        }

        public void execute(){
            IMapProperty props = (IMapProperty) form;
            String reportAction = ( String )props.getValue("reportaction");
            HttpSession session = request.getSession();
            Target target = ( Target )session.getAttribute( "target" );
            ReportService reportService = (ReportService) session.getAttribute(COMPLIANCE_REPORT_SERVICE);
            if( "add".equalsIgnoreCase( reportAction ) && target != null ){
                String targetID = ("all".equalsIgnoreCase(target.getId())) ? "all_all" : target.getId();
                reportService.doReport(targetID, target.getType());
            }
            form.reset( mapping, request );
            forward = mapping.findForward( "success" ); 
            //forward = new ActionForward( mapping.findForward( "success" ).getPath(), true );
        }
    }
}
