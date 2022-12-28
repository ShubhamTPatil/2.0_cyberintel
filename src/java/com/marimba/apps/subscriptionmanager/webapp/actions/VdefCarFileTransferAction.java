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

import com.marimba.apps.securitymgr.compliance.util.VdefCarTransferHandler;
import com.marimba.apps.subscriptionmanager.webapp.forms.VdefTransferForm;

// Copyright 1997-2004, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.

/**
 * 
 * @author Inmkaklij This action class is responsible to transfer vdef.car file
 *         from one cloud location to other.
 * 
 */
public class VdefCarFileTransferAction extends AbstractAction {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {

		System.out.println("Transfer vdef.car file execute method has been called ");
		init(request);		
		VdefTransferForm formbean = (VdefTransferForm) form;
		String vdefValue;
		String responseMessage=null;
		try {
			vdefValue = request.getParameter("action");;
			System.out.println("vdef value from request  :"+vdefValue);
			if(null==vdefValue) {			
				vdefValue = formbean.getVdefDefaultValue();			
			}
			System.out.println("vdef value formbean  :"+vdefValue);
			
			if(vdefValue!=null && !vdefValue.isEmpty()){
				VdefCarTransferHandler vdefCarTransferHandler = new VdefCarTransferHandler();				
				responseMessage = vdefCarTransferHandler.transferVdefCARFile();
				System.out.println("END - vdef.car file transfer is completed. response message:"+responseMessage);
			} else {
				responseMessage="differnt process";				
			}
			formbean.setResponseMsg(responseMessage);
			sendJSONResponse(response,responseMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (mapping.findForward("success"));
	}
	 protected void sendJSONResponse(HttpServletResponse response, String jsonData) throws Exception {
         PrintWriter out = response.getWriter();
         out.println(jsonData);
         out.flush();
     }

	
}
