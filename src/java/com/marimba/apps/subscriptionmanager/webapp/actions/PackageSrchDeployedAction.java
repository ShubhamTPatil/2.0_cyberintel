// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.*;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.PackageEditForm;

/**
 * Sets the search parameters for deployed packages, and redirect to input page (in which GetDeployedPackagesTag uses the search parameters to query LDAP for a
 * list of packages in current subscriptions)
 *
 * @author Theen-Theen Tan
 * @version 1.6, 03/05/2002
 */
public final class PackageSrchDeployedAction
    extends Action
    implements IWebAppConstants {
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
        HttpSession session = request.getSession();
        String basicSearch =(String) ((PackageEditForm) form).getValue(SEARCH);
        String advancedSearch =(String) ((PackageEditForm) form).getValue("searchQuery");
        String searchQuery;

         if(null == basicSearch || "".equals(basicSearch.trim())) {
             searchQuery = advancedSearch;
         } else {
             searchQuery = basicSearch;
         }

        if (searchQuery != null) {
            session.setAttribute(PAGE_PKGS_DEP_SEARCH, searchQuery);
            session.removeAttribute(PKGLIST_BEAN);
        }

        String fwd = request.getParameter("forwardPage");

        return (new ActionForward(fwd, true));
    }

    /**
     * REMIND
     *
     * @param req REMIND
     */
    public static void removeSearchResult(HttpServletRequest req) {
        HttpSession session = req.getSession();
        session.removeAttribute(PAGE_PKGS_DEP_RS);
        session.removeAttribute(PAGE_PKGS_DEP_SEARCH);
    }
}
