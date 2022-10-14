// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.*;

import java.io.IOException;

import java.util.ArrayList;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.intf.IUser;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.webapp.forms.NamespaceForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;

import com.marimba.tools.ldap.LDAPConnection;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IWebAppsConstants;

/**
 * Loads the namespace from when user choose to switch the subscription namespace
 *
 * @author Theen-Theen Tan
 * @version 1.9, 02/27/2003
 */
public final class NamespaceLoadAction
    extends Action {
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
        IUser user = null;

        try {
            NamespaceForm    formbean = (NamespaceForm) form;
            ServletContext   sc = servlet.getServletConfig().getServletContext();
            SubscriptionMain main = TenantHelper.getTenantSubMain(sc, req);

            // Set the namespace that the user has previously selected
            user = GUIUtils.getUser(req);
            formbean.setNamespace(main.upgradeNamespace(user.getNameSpace()));
            LDAPConnection conn = LDAPWebappUtils.getSubConn(req);
            ArrayList      nsList = main.listNameSpaces(conn);

            formbean.setRootContainer(main.getSubBase());

            formbean.setNamespaceList(nsList);
            formbean.setValue(IUser.PROP_NAMESPACE_SKIPSETTING, "true".equals(user.getProperty(IUser.PROP_NAMESPACE_SKIPSETTING)) ? "true"
                                                           : "false");
        } catch (Exception se) {
            ((NamespaceForm) form).setNamespaceList(new ArrayList());
            return (mapping.findForward("failure"));
        }

        return (mapping.findForward("success"));
    }
}
