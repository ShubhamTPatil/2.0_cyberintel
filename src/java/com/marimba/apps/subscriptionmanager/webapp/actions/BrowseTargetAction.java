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
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.objects.Target;

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
public final class BrowseTargetAction
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
    init(request);
	String src = (String) request.getParameter("src");
	HttpSession session = request.getSession();
	session.removeAttribute("subcontext");
	session.removeAttribute("principalAcl");
    session.removeAttribute(ISubscriptionConstants.IS_FROM_PKG_VIEW);
    session.removeAttribute(IWebAppConstants.PAGE_PKGS);
    session.removeAttribute("multi_trgts_pkg");

    /////////Policy Copy RFE related cache items/////////////
    request.getSession().removeAttribute(IWebAppConstants.COPY_RHS_LIST);
    request.getSession().removeAttribute("policy_exists");
    request.getSession().removeAttribute("copy_preview");
    request.getSession().removeAttribute(PAGE_TCHPROPS_CHANNELS);
    request.getSession().removeAttribute(PAGE_TCHPROPS_SUB);
    request.getSession().removeAttribute(PAGE_TCHPROPS_SUB_COPY);
    request.getSession().removeAttribute(SESSION_COPY);
    DistributionBean copyBean = getDistributionBeanCopy(request);
    ArrayList targets = copyBean.getTargets();
    for(int i=0;i<targets.size();i++){
        Target tg = (Target)targets.get(i);
        request.getSession().removeAttribute(tg.getName());
    }
    ////////////////////////////////////////////////////

    // Edit Target List session varaibles
    request.getSession().removeAttribute(IWebAppConstants.ADD_REMOVE_PACKAGE);
    request.getSession().removeAttribute("add_common_target");
    request.getSession().removeAttribute("add_selected_list");

	if (session.getAttribute("context") == null || !((String)session.getAttribute("context")).equals("targetDetailsAddMulti")) {
		session.setAttribute("context", "targetDetailsAdd");
	}

	if ("pcview".equals(src)) {
	    session.removeAttribute(SESSION_MULTITGBOOL);
	    session.removeAttribute(SESSION_PKGS_FROMTGS_RS);
	    session.removeAttribute(MAIN_PAGE_M_TARGETS);
	}
	Object bean = session.getAttribute(IWebAppsConstants.INITERROR_KEY);

	if ((bean != null) && bean instanceof Exception) {
		//remove initerror from the session because it has served its purpose
		if (DEBUG) {
			System.out.println("BrowseTargetAction: critical exception found");
		}
		session.removeAttribute(IWebAppsConstants.INITERROR_KEY);
		return mapping.findForward("initerror");
	}
	if (request.getParameter("forward") != null) {
	    return getForward(request);
	}

        return (mapping.findForward("success"));
    }
}
