package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.marimba.webapps.intf.IMapProperty;
import com.marimba.apps.subscriptionmanager.webapp.util.push.DMDeploymentView;
import com.marimba.apps.subscriptionmanager.webapp.util.push.DMHelper;
import com.marimba.apps.subscriptionmanager.webapp.forms.PushUrlForm;
import com.marimba.apps.subscriptionmanager.webapp.forms.RunScanForm;
import com.marimba.apps.securitymgr.compliance.DashboardHandler;
import com.marimba.apps.subscriptionmanager.webapp.util.RunScanHandler;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;


// Copyright 1997-2004, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.

/**
 * 
 * @author Inmkaklij
 * This action class is responsible to showcase run scan results.
 * 
 */
public class RunScanAction extends AbstractAction {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {

		System.out.println("runscan action execute method has been called 1");
		init(request);
		RunScanForm formbean = (RunScanForm) form;
		//HttpSession session = request.getSession();
		try {
			
			RunScanHandler dashboardHandler = new RunScanHandler(main);			
			System.out.println("runscan action execute method has been called 2 dashboardHandler : "+dashboardHandler);
			formbean.setRunScanJson(dashboardHandler.getRunScanData());
			System.out.println("runscan action execute method has been called 3 end : "+ dashboardHandler.getRunScanData());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return (mapping.findForward("success"));
	}
}
