package com.marimba.apps.subscriptionmanager.webapp.actions;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.marimba.intf.util.IConfig;

import com.marimba.tools.config.*;
import com.marimba.apps.securitymgr.compliance.util.DefinitionUpdateHandler;
import com.marimba.apps.securitymgr.compliance.util.VdefCarTransferHandler;
import com.marimba.apps.subscriptionmanager.webapp.forms.DefinitionUpdateForm;

// Copyright 1997-2004, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.

/**
 * 
 * @author Inmkaklij This action class is responsible to transfer vdef.car file
 *         from one cloud location to other.
 * 
 */
public class DefinitionUpdateAction extends AbstractAction {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {

		System.out.println("Transfer vdef.car file execute method has been called ");
		init(request);
		DefinitionUpdateForm formbean = (DefinitionUpdateForm) form;
		String vdefValue;
		String responseMessage = null;
		String definationUpdateResponse = null;

		try {
			vdefValue = request.getParameter("action");
			System.out.println("vdef value from request  :" + vdefValue);

			if (vdefValue != null && !vdefValue.isEmpty()) {

				System.out.println("START - vdef.car file transfer is triggered ");
				VdefCarTransferHandler vdefCarTransferHandler = new VdefCarTransferHandler();
				responseMessage = vdefCarTransferHandler.transferVdefCARFile();
				formbean.setResponseMsg(responseMessage);
				sendJSONResponse(response, responseMessage);
				System.out.println("END - vdef.car file transfer is completed. response message:" + responseMessage);
			} else {

				System.out.println("START - fetch defination update metadata ");
				DefinitionUpdateHandler definitionUpdateHandler = new DefinitionUpdateHandler(main);
				definationUpdateResponse = definitionUpdateHandler.getMetaData();
				formbean.setDefinationUpdateResponse(definationUpdateResponse);
				System.out.println("END - fetch defination update metadata "+definationUpdateResponse.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (mapping.findForward("success"));
	}

	/**
	 * 
	 * @param response
	 * @param jsonData
	 * @throws Exception
	 */
	protected void sendJSONResponse(HttpServletResponse response, String jsonData) throws Exception {
		PrintWriter out = response.getWriter();
		out.print(jsonData);
		out.flush();
	}
}