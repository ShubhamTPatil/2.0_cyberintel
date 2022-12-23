package com.marimba.apps.subscriptionmanager.webapp.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.marimba.apps.subscriptionmanager.webapp.forms.ScanResultsForm;
import com.marimba.apps.subscriptionmanager.webapp.util.ScanResultsHandler;

// Copyright 1997-2004, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.

/**
 * 
 * @author insthayyur This action class is responsible to show scan
 *         results.
 * 
 */
public class ScanResultsAction extends AbstractAction {

	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {

		System.out.println("\n \n \n ScanResultsAction action execute method has been called  \n \n \n ");
		String responseMsg = null;
		try {
			init(request);
			ScanResultsForm formbean = (ScanResultsForm) form;
			System.out.println("ScanResultsAction - From Request : "+request.getParameter("endDevicesArr"));
			String actionId=(String)request.getParameter("endDevicesArr");
			System.out.println("ScanResultsAction - From formbean : "+formbean.getEndDevicesArr());
			
			if(actionId != null && !actionId.trim().isEmpty()){
				if (null != formbean) {
					System.out.println("ScanResultsAction : Got the Host list");
					ScanResultsHandler ScanResultsHandler = new ScanResultsHandler(main);
					responseMsg = ScanResultsHandler.processScanResultsCLI(formbean);
				}else{
					System.out.println("ScanResultsAction : formbean is null ");
				}
			}else{
					System.out.println("ScanResultsAction : Home Page Response");
					ScanResultsHandler dashboardHandler = new ScanResultsHandler(main);
					formbean.setScanResultsJson(dashboardHandler.getScanResultsData());
			}
			formbean.setResponseMsg(responseMsg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (mapping.findForward("success"));
	}
}
