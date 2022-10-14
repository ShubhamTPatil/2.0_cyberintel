// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;

/**
 * Set the context and forwads on to the target view page.
 * If 'forward' request parameter is set, the action will forward to that page or action.
 * This action may switch to single select mode depending on the caller specified by the
 * 'src' request parameter.
 *
 * @author Devendra Vamathevan
 * @version 1.36, 11/21/2002
 */
public final class AddRemoveTargetsAction
        extends AbstractAction {
    /**
     * REMIND
     *
     * @param mapping REMIND
     * @param form REMIND
     * @param request REMIND
     * @param response REMIND
     *
     * @return REMIND
     *
     * @throws IOException REMIND
     * @throws ServletException REMIND
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException,
            ServletException {

	String src = (String) request.getParameter("src");
	HttpSession session = request.getSession();
	session.setAttribute("context", "addRemoveAdd");
	session.removeAttribute("subcontext");
	session.removeAttribute("principalAcl");
    DistributionBean distBean = getDistributionBean(request);
    session.setAttribute(ADD_REMOVE_SELECTED_PAGE_TARGETS,distBean.getTargets().clone());

	if ("pcview".equals(src)) {
	    session.removeAttribute(SESSION_MULTITGBOOL);
	    session.removeAttribute(SESSION_PKGS_FROMTGS_RS);
	    session.removeAttribute(MAIN_PAGE_M_TARGETS);
	}

	if (request.getParameter("forward") != null) {
	    return getForward(request);
	}

        return (mapping.findForward("success"));
    }
}
