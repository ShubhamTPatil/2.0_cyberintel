// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions.common;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;

/**
 * This action returns the user to the page in which the action performed was originated from. SESSION_RETURN_PAGE - This variable stores the page that the in
 * which the original action was performed. For example, - "edit assignment" can be performed from four places, either package view/package detail/target
 * view/target detail. - Target level and package delete actions can be performed from either target view or target detail page.  If SESSION_RETURN_PAGE is
 * not set, we default by returning to the Target View Page.
 *
 * @author Theen-Theen Tan
 * @version 1.0, 05/07/2002
 */
public final class ReturnToOriginAction
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
        String returnPage = null;

        try {
            returnPage = (String) GUIUtils.getFromSession(request, SESSION_RETURN_PAGE);
        } catch (Exception ex) {
            // Do nothing
        }

        if (null == returnPage) {
            returnPage = "/dashboard/main_view.jsp";
        }

        if (DEBUG) {
            System.out.println("Returning to ... " + returnPage);
        }

        return new ActionForward(returnPage, true);
    }
}
