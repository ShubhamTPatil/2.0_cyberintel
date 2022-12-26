package com.marimba.apps.subscriptionmanager.webapp.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

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
		try {
			VdefCarTransferHandler vdefCarTransferHandler = new VdefCarTransferHandler();
			System.out.println("start - Transfer vdef.car file  is called  :");
			String responseMessage = vdefCarTransferHandler.transferVdefCARFile();
			formbean.setResponseMsg(responseMessage);
			System.out.println("END - vdef.car file transfer is completed. ");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return (mapping.findForward("success"));
	}
}
