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
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.webapps.intf.IMapProperty;



/**
 * An interim action that is called by DistAsgSaveAction.java when it returns from the save action.  Without this interim action, the TargetDetailsForm won't
 * be initialized if "New Assignment" is the very first action a user does.
 */
public final class DetailsFormInitAction
    extends Action implements IWebAppConstants {
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
        GUIUtils.initForm(request, mapping);

        //To remove the targets list maintained in the session
        HttpSession session = request.getSession();
        session.setAttribute(SESSION_TGTS_FROM_PKGS,null);
        session.removeAttribute(PAGE_PKGS_DEP_SEARCH);

        return (new ActionForward(mapping.getInput(), true));
    }
}
