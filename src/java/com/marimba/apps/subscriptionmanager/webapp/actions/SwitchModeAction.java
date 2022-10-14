// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.*;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscriptionmanager.webapp.forms.AbstractForm;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.CriticalException;

/**
 * This corresponds to the action when the user switches mode from single select mode to multiple select mode or vice-a-versa. Rahul Ravulur 1.4, 04/23/2002
 */
public final class SwitchModeAction
    extends AbstractAction {
    static final String PACKAGE_TYPE = "package";
    static final String TARGET_TYPE = "target";
    static final String MULTIPLE_MODE = "multiple";
    static final String SINGLE_MODE = "single";

    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
                     return new SwitchModeTask(mapping, form, request, response);
    }
    protected class SwitchModeTask
               extends SubscriptionDelayedTask {
        SwitchModeTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
                  super(mapping, form, request, response);
     }
    public void execute() {
    try {
        HttpSession session = request.getSession();

        String      to = (String) request.getParameter("to");
        String      type = (String) request.getParameter("type");

        //Initialize the form that we are redirecting to.  This is so that
        //The page will display correctly
        AbstractForm aform = (AbstractForm) form;
        aform.initialize();

        if (PACKAGE_TYPE.equals(type)) {
            if (SINGLE_MODE.equals(to)) {
                session.removeAttribute(SESSION_MULTIPKGBOOL);
                session.removeAttribute(SESSION_TGS_FROMPKGS_RS);
                session.removeAttribute(MAIN_PAGE_M_PKGS);
            } else {
                // copy the first element out of the single select session variable
                // into the multiple select mode.
                if (!(null == session.getAttribute(MAIN_PAGE_PACKAGE))) {
                    session.setAttribute(MAIN_PAGE_M_PKGS, session.getAttribute(MAIN_PAGE_PACKAGE));
                }

                session.setAttribute(SESSION_MULTIPKGBOOL, "true");

                // clean up the single select mode session variables.
                session.removeAttribute(SESSION_TGS_FROMPKGS_RS);
                session.removeAttribute(MAIN_PAGE_PACKAGE);
            }
        } else {
            if (SINGLE_MODE.equals(to)) {
                session.removeAttribute(SESSION_MULTITGBOOL);
                session.removeAttribute(SESSION_PKGS_FROMTGS_RS);
                session.removeAttribute(MAIN_PAGE_M_TARGETS);
                session.setAttribute("context", "targetDetailsAdd");
            } else {
                // copy the first element out of the single select session variable
                // into the multiple select mode.
                if (!(null == session.getAttribute(MAIN_PAGE_TARGET))) {
                    session.setAttribute(MAIN_PAGE_M_TARGETS, session.getAttribute(MAIN_PAGE_TARGET));
                }

                session.setAttribute(SESSION_MULTITGBOOL, "true");
                session.setAttribute("context", "targetDetailsAddMulti");

                // clean up the single select mode session variables.
                session.removeAttribute(SESSION_PKGS_FROMTGS_RS);
                session.removeAttribute(MAIN_PAGE_TARGET);
            }
        }

        String fwd = request.getParameter("fwdURL");
        if (DEBUG) {
            System.out.println(" forwarding to " + fwd);
        }
        forward = new ActionForward(fwd, true);
        } catch (Exception ex) {
            guiException = new GUIException(new CriticalException(ex.toString()));
            forward = mapping.findForward("failure");
        }
    }
 }
}

