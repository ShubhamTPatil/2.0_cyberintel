// Copyright 1997-2004, Marimba Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions.patch;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.intf.SubInternalException;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.ISubscriptionConstants;

import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.system.operation.SelectedPkgsBean;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscriptionmanager.webapp.actions.PackageEditAction;
import com.marimba.apps.subscriptionmanager.webapp.forms.DistAsgForm;
import com.marimba.apps.subscriptionmanager.webapp.forms.PackageEditForm;
import com.marimba.apps.subscriptionmanager.webapp.util.PatchManagerHelper;
import com.marimba.apps.subscriptionmanager.webapp.util.PatchUtils;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.SystemException;

import com.marimba.webapps.tools.util.PropsBean;
import com.marimba.intf.msf.IUserPrincipal;

/**
 * Handles actions eithin the Edit Patch Group page.
 *
 * Actions :
 * edit(default) - Upon entering the Edit Patch Group page from, copy the distribution bean copy the patches
 * into session variable PAGE_PATCHES (SelectedPkgsBean object) that stores patch groups selected by users.
 * The PAGE_PATCHES variable is obtained through the form.
 * remove -  Removes the Patch Group selected by the user from the PAGE_PATCHES variable
 * save -  Saves the Patch Group selected by the users stored in PAGE_PATCHES to the DistributionBean
 * cancel -  Removes the PAGE_PATCHES variable from the session
 *
 * @author Theen-Theen Tan
 * @version $Revision$, $Date$
 */
public final class PatchEditAction
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
        String action = getAction(req);
        PackageEditForm editForm = (PackageEditForm) form;
        init(req);
        if (ACTION_EDIT.equals(action) || action == null || action.length() == 0) {
            DistributionBean distbean = getDistributionBean(req);
            ArrayList channels = distbean.getPatchChannels();

            SelectedPkgsBean pkgsbean = new SelectedPkgsBean();
            try {
                for (int i = 0; i < channels.size(); i++) {
                    PropsBean pkg = new PropsBean();
                    pkg.setProperty("url", ((Channel) channels.get(i)).getUrl());
                    pkg.setProperty("title", ((Channel) channels.get(i)).getTitle());
                    pkgsbean.addPackage(pkg);
                }
                editForm.setSelectedPkgs(pkgsbean, req);

                // Clear the Transmitter browsing bean, and set Default URL to Patch Transmitter
                // obtained from Patch Manager

                HttpSession session = req.getSession();
                session.removeAttribute(TXLIST_BEAN);
                session.removeAttribute(DEPLIST_BEAN);
                String patchtxURL = PatchUtils.getPatchTransmitterURL(req, tenant);
                if (patchtxURL != null) {
                    session.setAttribute(IWebAppsConstants.TXLIST_CURRENT_URL, patchtxURL);
                }
                req.setAttribute("refresh",PatchUtils.getPatchTransmitterURL(req, tenant));

            } catch (Exception e) {
                throw new GUIException(PKG_EDIT_ERROR, e);
            }
        } else if (ACTION_ADD.equals(action)) {


        } else if (ACTION_REMOVE.equals(action)) {
            String[] rmpkgs = ((PackageEditForm) form).getPackages();
            SelectedPkgsBean pkgs = editForm.getSelectedPkgs(req);
            try {
                for (int i = 0; i < rmpkgs.length; i++) {
                    if (!pkgs.removePackage(rmpkgs[i])) {
                        throw new SubInternalException(PKG_INTERNAL_REMOVE_NOTFOUND, rmpkgs[i]);
                    }
                }
                editForm.setSelectedPkgs(pkgs, req);
                editForm.setPackages(new String[0]);
            } catch (Exception e) {
                throw new GUIException(PKG_REMOVE_ERROR, e);
            }
        } else if (ACTION_SAVE.equals(action)) {

            DistributionBean distbean = getDistributionBean(req);
            SelectedPkgsBean pkgsbean = editForm.getSelectedPkgs(req);

            try {

                // Add channels from PAGE_PKGS into DistributionBean
                Collection pkgs = pkgsbean.getPackages();
                for (Iterator ite = pkgs.iterator(); ite.hasNext();) {
                    PropsBean node = (PropsBean) ite.next();
                    distbean.setChannel(node.getProperty("url"), node.getProperty("title"), ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP, 
                    		LDAPVarsMap, tenantName, channel);
                }

                // Removing channels not in the selected list from DistributionBean
                ArrayList channels = distbean.getPatchChannels();
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
            // display simulate only there is a single target
            // and the target is a machine
            String hideSimulate = "true";
            ArrayList targets = distbean.getTargets();
            if (targets.size() == 1) {
                Target t = (Target) targets.get(0);
                if (ISubscriptionConstants.TYPE_MACHINE.equals(t.getType())) {
                    hideSimulate = "false";
                }
            }
            editForm.setValue("hideSimulate", hideSimulate);
            editForm.removeSelectedPkgs(req);
            DistAsgForm distform = (DistAsgForm) req.getSession().getAttribute("distAsgForm");
            distform.clearPagingVars(req);
            return (mapping.findForward("assignment"));
        } else if (ACTION_CANCEL.equals(action)) {
            editForm.removeSelectedPkgs(req);
            return (mapping.findForward("assignment"));
        }
        return (mapping.findForward("success"));

    }


}
