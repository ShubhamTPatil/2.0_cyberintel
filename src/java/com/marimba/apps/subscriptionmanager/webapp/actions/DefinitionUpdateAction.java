package com.marimba.apps.subscriptionmanager.webapp.actions;


import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import java.io.IOException;

import com.marimba.apps.securitymgr.compliance.util.VdefCarTransfer;

public class DefinitionUpdateAction extends AbstractAction {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {


		System.out.println("DefinitionUpdateAction action execute method has been called 1");
		init(request);
		
		//HttpSession session = request.getSession();
		try {
			VdefCarTransfer.uploadVdefCARFile();	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (mapping.findForward("success"));
	}
}
