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

import com.marimba.apps.subscription.common.intf.SubInternalException;

import com.marimba.apps.subscriptionmanager.webapp.forms.PackageEditForm;
import com.marimba.apps.subscriptionmanager.webapp.forms.DistAsgForm;
import com.marimba.apps.subscriptionmanager.webapp.system.operation.SelectedPkgsBean;

import com.marimba.webapps.intf.GUIException;

/**
 * Handles removing one or many packages from the selected packages list
 *
 * @author Theen-Theen Tan
 * @version 1.5, 03/02/2002
 */
public final class PackageRemoveAction
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
     * @throws GUIException REMIND
     */
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  req,
                                 HttpServletResponse response)
        throws IOException, 
                   ServletException {
        String[]         rmpkgs = ((PackageEditForm) form).getPackages();
        SelectedPkgsBean pkgs = ((PackageEditForm) form).getSelectedPkgs(req);

        // To disable Revert Priority button when all the packages are removed.

        DistAsgForm distform = (DistAsgForm) req.getSession().getAttribute("distAsgForm");
        String remallchk = req.getParameter("selectAll");
        if(remallchk != null){
            distform.setValue(SESSION_DIST_PAGEPKGS_PREORDER, "false");
        }
        if (DEBUG) {
            System.out.println("################# PackageAddRemove called");
            System.out.println("removing " + rmpkgs.length + " items");
        }

        try {
            for (int i = 0; i < rmpkgs.length; i++) {
                if (!pkgs.removePackage(rmpkgs [i])) {
                    throw new SubInternalException(PKG_INTERNAL_REMOVE_NOTFOUND, rmpkgs [i]);
                }
            }
            ((PackageEditForm) form).setPackages(pkgs.getPackagesArray());
            ((PackageEditForm) form).setSelectedPkgs(pkgs, req);
        } catch (Exception e) {
            throw new GUIException(PKG_REMOVE_ERROR, e);
        }

        return (mapping.findForward("success"));
    }
}
