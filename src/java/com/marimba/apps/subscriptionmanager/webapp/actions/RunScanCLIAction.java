package com.marimba.apps.subscriptionmanager.webapp.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.marimba.apps.subscriptionmanager.webapp.forms.RunScanForm;
import com.marimba.apps.subscriptionmanager.webapp.util.RunScanHandler;

// Copyright 1997-2004, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.

/**
 * 
 * @author Inmkaklij This action class is responsible to showcase run scan
 *         results.
 * 
 */
public class RunScanCLIAction extends AbstractAction {

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

		System.out.println("runscan action CLI execute method has been called");
		String responseMsg = null;
		try {
			init(request);
			RunScanForm formbean = (RunScanForm) form;

			if (null != formbean) {
				RunScanHandler runScanHandler = new RunScanHandler(main);				
				formbean.setRunScanJson(runScanHandler.getRunScanData());
				responseMsg = runScanHandler.processRunScanCLI(formbean);
			}
			formbean.setResponseMsg(responseMsg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (mapping.findForward("success"));
	}
}
