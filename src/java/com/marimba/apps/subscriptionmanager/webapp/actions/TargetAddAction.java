// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscriptionmanager.webapp.forms.AddTargetEditForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;

/**
 * This action corresponds to the cancel action on the set distribution page.
 */
public final class TargetAddAction
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
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
        throws IOException, 
                   ServletException {
        //everytime it has inittialize
        GUIUtils.initForm(request, mapping);
        ((AddTargetEditForm) form).init(getResources(), getLocale(request));
        ((AddTargetEditForm) form).clearPagingVars(request);
        ((AddTargetEditForm) form).clearSessionVars(request);
        HttpSession session = request.getSession();
        session.removeAttribute(PENDING_POLICY_SAMEUSER);
        session.removeAttribute(PENDING_POLICY_DIFFUSER);
        session.removeAttribute(SESSION_TGS_FROMPKGS_SELECTED);
        session.removeAttribute(SESSION_PKGS_FROMTGS_SELECTED);
        session.setAttribute("context", "addTargetsPkg");
        return (mapping.findForward("success"));
    }
}
