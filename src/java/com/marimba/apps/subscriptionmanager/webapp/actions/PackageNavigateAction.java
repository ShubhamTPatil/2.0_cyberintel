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
import com.marimba.apps.subscriptionmanager.webapp.actions.common.PageStateAction;

/**
 * action responsible to resetting the search results since sort has been selected
 *
 * @author Rahul Ravulur
 */
public class PackageNavigateAction
    extends PageStateAction
    implements IWebAppConstants {
    /**
     * This method basically extends the PageStateAction methods.
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
        req.getSession()
           .removeAttribute(PKGLIST_BEAN);

        return execute(mapping, form, req, response);
    }
}
