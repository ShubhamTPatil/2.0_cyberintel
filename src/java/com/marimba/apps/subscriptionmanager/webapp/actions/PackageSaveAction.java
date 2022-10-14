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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.objects.Channel;

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.DistAsgForm;
import com.marimba.apps.subscriptionmanager.webapp.forms.PackageEditForm;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.system.operation.SelectedPkgsBean;

import com.marimba.webapps.intf.GUIException;

import com.marimba.webapps.tools.util.PropsBean;

/**
 * Handles the saving of selected packages into the distribution bean sessionvariable (SESSION_DIST).
 *
 * @author Theen-Theen Tan
 * @version 1.10, 03/02/2002
 */
public final class PackageSaveAction
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
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest req,
                                 HttpServletResponse response)
            throws IOException,
            ServletException {
    	init(req);
        DistributionBean distbean = getDistributionBean(req);
        SelectedPkgsBean pkgsbean = ((PackageEditForm) form).getSelectedPkgs(req);

        if (DEBUG) {
            System.out.println("################# PackageSaveAction called");
        }

        try {
            // Add channels in PAGE_PKGS
            Collection pkgs = pkgsbean.getPackages();

            for (Iterator ite = pkgs.iterator(); ite.hasNext();) {
                PropsBean node = (PropsBean) ite.next();
                String type = node.getProperty("type");
                distbean.setChannel(node.getProperty("url"), node.getProperty("title"), type, LDAPVarsMap, tenantName, channel);
            }

            // Removing channels not in PAGE_PKGS
            ArrayList channels = distbean.getApplicationChannels();
            String url = null;

            for (int i = 0; i < channels.size(); i++) {
                url = ((Channel) channels.get(i)).getUrl();

                if (pkgsbean.getPackage(url) == null) {
                    distbean.removeChannel(url);
                }
            }

            setDistributionBean(distbean, req);

        } catch (Exception e) {
            throw new GUIException(PKG_SAVE_ERROR, e);
        }

        ((PackageEditForm) form).removeSelectedPkgs(req);
        DistAsgForm distform = (DistAsgForm) req.getSession().getAttribute("distAsgForm");
        distform.clearPagingVars(req);

        ((PackageEditForm) form).setPreviousTransmitterUrl((String) req.getSession().getAttribute(TXLIST_CURRENT_URL));

        return (mapping.findForward("success"));
    }
}
