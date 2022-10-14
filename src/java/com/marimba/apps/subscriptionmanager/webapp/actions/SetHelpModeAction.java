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

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;

/**
 * Action responsible for setting a session variable to indicate the help mode There are currently two modes: expanded and contracted The action processes two
 * parameters attr=&lt;requestURL> showExpanded=&lt;boolean> It sets a session attribute &lt;requestURL>=value of showExpanded The help.jsp file shows the
 * appropriate text based on the value of this variable
 *
 * @author Rahul Ravulur
 */
public class SetHelpModeAction
    extends AbstractAction
    implements IWebAppConstants {
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
        String pageName = (String) req.getParameter("attr");
        String showExpanded = req.getParameter("showExpanded");

        if (DEBUG) {
            System.out.println(" The pagename var is \t: " + pageName);
            System.out.println(" The showExpanded value is \t: " + showExpanded);
            System.out.println(" The forward parameter is \t: " + req.getParameter("forward"));
            System.out.println(" get context path is \t: " + req.getContextPath());
            System.out.println(" query string is \t: " + req.getQueryString());
            System.out.println(" get request uri is \t:" + req.getRequestURI());
            System.out.println(" get servlet path is \t: " + req.getServletPath());
        }

        req.getSession()
           .setAttribute(pageName, showExpanded);

        return new ActionForward(req.getParameter("attr"), true);
    }
}
