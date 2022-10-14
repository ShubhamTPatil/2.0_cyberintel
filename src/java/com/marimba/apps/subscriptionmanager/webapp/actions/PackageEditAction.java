// Copyright 1997-2009, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.IDebug;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscriptionmanager.webapp.forms.PackageEditForm;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.system.operation.SelectedPkgsBean;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IMapProperty;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.tools.util.PropsBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Sets up the session variables upon entering the Add/Remove Packages page. From the distribution bean copy the channels into session variable PAGE_PKGS
 * (SelectedPkgsBean class) that stores packages selected by users.  Redirects to the add/remove package page if successful. Also contain static methods which
 * to set/get/remove the PAGE_PKGS
 *
 * @author Theen-Theen Tan
 * @version 1.16, 03/02/2002
 */
public final class PackageEditAction
        extends AbstractAction {
    final static boolean DEBUG = IDebug.DEBUG;

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

        String action = req.getParameter("action");
        //default type is application for backward comatibility
        String type = CONTENT_TYPE_APPLICATION;
        if (ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP.equals(action)) {
            type = ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP;
        }

        DistributionBean distbean = getDistributionBean(req);
        ArrayList        channels = distbean.getChannels();
        SelectedPkgsBean pkgsbean = new SelectedPkgsBean();

        if (DEBUG) {
            System.out.println("################# PackageEditAction called");
            System.out.println("loadSelectedPkgs.channels.size() " + channels.size());
        }

        try {
            for (int i = 0; i < channels.size(); i++) {
                Channel ch = (Channel) channels.get(i);
                if (type.equals(ch.getType())){
                    PropsBean pkg = new PropsBean();
                    pkg.setProperty("url",ch.getUrl());
                    pkg.setProperty("title", ch.getTitle());
                    pkg.setProperty("type", ch.getType());
                    pkgsbean.addPackage(pkg);
                }
            }
            ((PackageEditForm) form).setSelectedPkgs(pkgsbean, req);
            ( (IMapProperty)form ).setValue( "search", null );
            HttpSession session = req.getSession();
            session.removeAttribute(TXLIST_BEAN);
            session.removeAttribute(DEPLIST_BEAN);
            session.removeAttribute(PAGE_PKGS_DEP_SEARCH);
            session.setAttribute(IWebAppsConstants.TXLIST_CURRENT_URL, ((PackageEditForm) form).getPreviousTransmitterUrl());

        } catch (Exception e) {
            throw new GUIException(PKG_EDIT_ERROR, e);
        }

        return (mapping.findForward(type));
    }

}
