// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;

import java.io.IOException;

import com.marimba.webapps.intf.*;

import com.marimba.apps.subscriptionmanager.webapp.forms.PackageDetailsForm;

/**
 * This action is used from the delete preview page when user confirms the deletion of selected targets from a single select mode.
 *
 * @author Jayaprakash Paramasivam
 * @version 1.0, 05/30/2005
 */

public class PackageDeleteCancelAction
        extends AbstractAction {
    /**
     * REMIND
     *
     * @param mapping ActionMapping
     * @param form ActionForm
     * @param req HttpServletRequest
     * @param response HttpServletResponse
     *
     * @return Actionmapping forward
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

        ((PackageDetailsForm) form).clearPaginVars(req);

        return (mapping.findForward("success"));
    }
}
