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

import java.util.List;
import java.util.ListIterator;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.intf.SubInternalException;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;

import com.marimba.apps.subscriptionmanager.webapp.system.operation.SelectedPkgsBean;
import com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean;
import com.marimba.apps.subscriptionmanager.webapp.forms.PackageEditForm;

import com.marimba.webapps.intf.KnownException;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.GUIException;

import com.marimba.webapps.tools.util.PropsBean;

/**
 * Handles the adding of a package from a list of packages in existing subscriptions
 *
 * @author Theen-Theen Tan
 * @version 1.5, 03/02/2002
 */
public final class PackageAddDeployedAction
    extends AbstractAction
    implements IWebAppsConstants {
    final static boolean DEBUG = IAppConstants.DEBUG;

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
        String           path = null;
        String           url = req.getParameter("addurl");
        SelectedPkgsBean pkgs = ((PackageEditForm) form).getSelectedPkgs(req);
        List             chnlist = (List) req.getSession().getAttribute(PAGE_PKGS_DEP_RS);
        HttpSession session = req.getSession();
        GenericPagingBean pageBean = ( GenericPagingBean )session.getAttribute( PKGLIST_BEAN );
        if( pageBean != null ){
            req.setAttribute( "curpage", pageBean.getCurrentPageString() );
        }

        try {
            if (url == null) {
                throw new SubInternalException(PKG_INTERNAL_ADD_PATHNOTFOUND);
            }

            if (chnlist == null) {
                throw new SubInternalException(PKG_INTERNAL_ADDDEP_RSNOTFOUND);
            }

            if (DEBUG) {
                System.out.println("################# PackageAddDeployedAction called");
                System.out.println("Adding = " + url);
                System.out.println("add channel: " + url);
            }

            for (ListIterator ite = chnlist.listIterator(); ite.hasNext();) {
                PropsBean channel = (PropsBean) ite.next();

                if (url.equals(channel.getProperty("url"))) {
		    if (pkgs.getPackage(url) != null) {
			throw new KnownException(PKG_ADD_PKGEXIST, url);
		    }
                    pkgs.addPackage((PropsBean) channel.clone());
                    break;
                }
            }

            ((PackageEditForm) form).setSelectedPkgs(pkgs, req);
        } catch (Exception e) {
            throw new GUIException(PKG_ADD_ERROR, e);
        }

        return (mapping.findForward("success"));
    }
}
