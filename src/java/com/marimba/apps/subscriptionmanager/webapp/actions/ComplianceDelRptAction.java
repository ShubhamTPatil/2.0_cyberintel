// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;



import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;

import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;

import com.marimba.apps.subscriptionmanager.compliance.service.ReportService;

import com.marimba.apps.subscriptionmanager.compliance.view.ReportBean;

import com.marimba.apps.subscription.common.ISubscriptionConstants;

import com.marimba.webapps.tools.action.DelayedAction;

import com.marimba.webapps.intf.IMapProperty;

import org.apache.struts.action.ActionMapping;

import org.apache.struts.action.ActionForm;

import org.apache.struts.action.ActionForward;



import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import javax.servlet.http.HttpSession;

import java.util.Iterator;

import java.util.HashSet;



/**

 * Created by IntelliJ IDEA.

 * User: nrao

 * Date: Aug 10, 2005
 * Time: 5:11:22 PM

 * To change this template use File | Settings | File Templates.

 */

public class ComplianceDelRptAction  extends AbstractAction

    implements IWebAppConstants, ISubscriptionConstants, ComplianceConstants {



    protected DelayedAction.Task createTask( ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response ) {

        return new  ComplianceDelRptAction.ComplianceDelRptTask( mapping, form, request, response );

    }



    protected class ComplianceDelRptTask

               extends SubscriptionDelayedTask  {

        ComplianceDelRptTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {

                  super(mapping, form, request, response);

        }



        public void execute(){

            IMapProperty props = (IMapProperty) form;

            String reportAction = ( String )props.getValue("reportaction");

            HttpSession session = request.getSession();
            init(request);
            ReportService reportService = (ReportService) session.getAttribute(COMPLIANCE_REPORT_SERVICE);

            ReportBean reportBean = null;

            if( "delete".equalsIgnoreCase( reportAction ) ){

                HashSet selectedIds = getSelectedIds( props );

                Iterator ids = selectedIds.iterator();

                String calcId = null;

                int cId = -1;

                while( ids.hasNext() ){

                    calcId = ( String )ids.next();

                    if( calcId != null ){

                        cId = Integer.parseInt( calcId );

                        reportBean = reportService.getReport( cId );

                        reportService.deleteReport( cId );

                        if (reportBean != null) {

                            main.logAuditInfo(LOG_AUDIT_COMPLIANCE_REPORTS, LOG_AUDIT,  "Policy Manager", reportBean.getName() , request, COMPLIANCE_DEL_REPORT);

                        }

                    }

                }

            }

            form.reset( mapping, request );

            forward = mapping.findForward( "success" );

            //forward = new ActionForward( mapping.findForward( "success" ).getPath(), true );

        }



        private HashSet getSelectedIds( IMapProperty props ){

            HashSet selectedIds = new HashSet();

            Object totalObj = props.getValue( "totalReports" );

            Object stateObj = props.getValue( "target_sel_all" );

            int totalReports = ( totalObj == null )? 0 : Integer.parseInt( ( String )totalObj );

            String state = null;

            if( stateObj != null && "on".equalsIgnoreCase( state = ( String ) stateObj ) ){

                for( int indx = 0; indx < totalReports; indx++ ){

                    selectedIds.add( props.getValue( "comp_target_cid_"+indx ) );

                }

            } else {

                for( int indx = 0; indx < totalReports; indx++ ){

                    stateObj = props.getValue( "target_sel_"+indx );

                    if( stateObj != null && "on".equalsIgnoreCase( state = ( String )stateObj ) ){

                        props.setValue( "target_sel_"+indx, "" );

                        selectedIds.add( props.getValue( "comp_target_cid_"+indx ) );

                    }

                }

            }

            return selectedIds;

        }

    }

}

