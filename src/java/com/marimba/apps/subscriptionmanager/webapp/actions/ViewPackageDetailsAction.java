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

/**
 * This corresponds to the action when the user clicks to view package details. It is used for clearing out the session variables that have been used for
 * storing the actions that the user has taken Rahul Ravulur 1.2, 06/28/2002
 */
public final class ViewPackageDetailsAction
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
        /* There is nothing to do here because there is no session variable that is used
         * to maintain the state for this transaction.  Simply forward to the success
         * page
         */
        request.setAttribute("toDetails", "true");

        if (request.getParameter("page") != null) {
            return (mapping.findForward("success_currentPage"));
        }

        return (mapping.findForward("success"));
    }
}
