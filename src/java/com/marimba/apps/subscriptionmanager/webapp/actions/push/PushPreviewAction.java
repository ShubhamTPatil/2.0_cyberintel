// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions.push;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;



import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.intf.SubKnownException;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscription.common.objects.Target;


import com.marimba.apps.subscriptionmanager.webapp.forms.TargetDetailsForm;
import com.marimba.apps.subscriptionmanager.webapp.forms.TargetDetailsMultiForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;

import com.marimba.webapps.intf.*;

/**
 * This action is used from single select mode to push packages to the selected targets.
 *
 * @author Anantha Kasetty
 * @version $Revision$, $Date$
 */
public class PushPreviewAction
    extends com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction {
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
     * @throws java.io.IOException REMIND
     * @throws javax.servlet.ServletException REMIND
     */
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
        throws IOException,
            ServletException {

        IMapProperty formbean = (IMapProperty) form;

        /*Add in the targets from the session variable used from the target schedule details.
          We must first determine which set of targets we are dealing with. This is done\
          by looking at the boolean which is set when the user is in multiple select mode
        */
        HttpSession session = request.getSession();
        String all = request.getParameter("all");

        ServletContext   sc = servlet.getServletConfig()
                                     .getServletContext();
        try {
            List   targets = getSelectedTargets(session);

            // see if there is any package to push, if none throw an error.
            String hasPackages = (String) session.getAttribute("hasPackages");
            if (hasPackages == null || !hasPackages.equals("true")) {
                Target tgt = (Target)targets.get(0);
                throw new SubKnownException(PUSH_PREVIEW_NOSUB, tgt.getName());
            }
            session.setAttribute(SESSION_TGS_TOPUSH, targets);
            if (all != null && all.equals("true")) {
                session.setAttribute(PUSH_ALL_POLICIES, "true");
            } else {
                session.removeAttribute(PUSH_ALL_POLICIES);
            }

        }  catch (SystemException se) {

            throw new GUIException(se);
        }
        return (mapping.findForward("success"));
    }


    protected void cleanup(ActionForm form) {
        if (form instanceof TargetDetailsForm) {
            ((TargetDetailsForm) form).clearCheckedItems();
        } else {
            ((TargetDetailsMultiForm) form).clearCheckedItems();
        }
    }
}
