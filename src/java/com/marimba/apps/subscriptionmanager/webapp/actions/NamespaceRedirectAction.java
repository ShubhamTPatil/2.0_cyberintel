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

import com.marimba.apps.subscription.common.intf.IUser;

/**
 * Redirects to config tab or targtet view tab depending on whether we went to the namespace page upon login.
 *
 * @author Theen-Theen Tan
 * @version 1.0, 12/02/2002
 */
public final class NamespaceRedirectAction
    extends AbstractAction {
    /**
     * REMIND
     *
     * @param mapping REMIND
     * @param form REMIND
     * @param req REMIND
     * @param response REMIND
     *
     * @return REMIND
     *
     * @throws IOException REMIND
     * @throws ServletException REMIND
     */
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  req,
                                 HttpServletResponse response)
        throws IOException, 
                   ServletException {
        HttpSession session = req.getSession();

        if (session.getAttribute(IUser.PROP_NAMESPACE_SKIPSETTING) != null) {
            session.removeAttribute(IUser.PROP_NAMESPACE_SKIPSETTING);

            return (mapping.findForward("main"));
        }

        return (mapping.findForward("success"));
    }
}
