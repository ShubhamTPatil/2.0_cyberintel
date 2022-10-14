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

import com.marimba.apps.subscription.common.intf.IUser;

import com.marimba.apps.subscriptionmanager.users.UserManager;
import com.marimba.apps.subscriptionmanager.webapp.forms.NamespaceForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;

/**
 * Loads the namespace from when user choose to switch the subscription namespace
 *
 * @author Theen-Theen Tan
 * @version 1.3, 12/02/2002
 */
public final class NamespaceSaveAction
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
        try {
            IUser user = GUIUtils.getUser(req);
            user.setProperty(IUser.PROP_NAMESPACE, ((NamespaceForm) form).getNamespace());
            user.setProperty(IUser.PROP_NAMESPACE_SKIPSETTING, ((NamespaceForm) form).getValue(IUser.PROP_NAMESPACE_SKIPSETTING).toString());
            UserManager.saveUser(user);
        } catch (SystemException se) {
            throw new GUIException(se);
        }

        return (mapping.findForward("success"));
    }
}
