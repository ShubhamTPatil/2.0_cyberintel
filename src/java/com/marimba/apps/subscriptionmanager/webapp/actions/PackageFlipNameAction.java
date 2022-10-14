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

import com.marimba.webapps.intf.IMapProperty;

/**
 * This action corresponds to the show url or show package name in the package navigation area 1.5,06/28/2002
 */
public final class PackageFlipNameAction
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
        // grab the channelURL to be queried
        String type = request.getParameter("displayType");
        //REMIND::RCR handle this when form is not a bean
        IMapProperty props = null;

        if (form instanceof IMapProperty) {
            props = (IMapProperty) form;
            if ("url".equals(type)) {
                props.setValue("show_url", "true");
                props.setValue("sorttype", "url");
                props.setValue("sortorder", "true");
            } else {
                props.setValue("show_url", "false");
                props.setValue("sortorder", "true");
                props.setValue("sorttype", "title");
            }
        }

        return (mapping.findForward("success"));
    }
}
