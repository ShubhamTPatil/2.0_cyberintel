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
import java.util.Iterator;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscriptionmanager.webapp.forms.*;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.intf.CriticalException;

/**
 * This action corresponds to when the user chooses to switch from package view to target view or vice-a-versa. The value of the request variable "to" denotes
 * the view the user is switching to. REMINDER: when switching from view 1 to view 2, the check boxes should be initialized in view 2 for the items selected
 * in view 1.  This also may involve bringing the user to the page in view 2 in which the first item is checked. Too much work to do for now so we just clear
 * out all the check boxes, when we switch views.
 *
 * @author Rahul Ravulur
 * @author Theen-Theen Tan
 * @version 1.10, 05/05/2002
 */
public final class SwitchViewAction
    extends AbstractAction {

    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
                 return new SwitchViewTask(mapping, form, request, response);
    }
    protected class SwitchViewTask
           extends SubscriptionDelayedTask {
           SwitchViewTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
               super(mapping, form, request, response);
    }
    public void execute() {
        try {
        HttpSession session = request.getSession();

        // the user is switching from the package view page to target view
        if ("target".equals(request.getParameter("to"))) {
            // now figure out the target list
            Map       tgSel = (Map) session.getAttribute(SESSION_TGS_FROMPKGS_SELECTED);
            ArrayList targetList = new ArrayList();
            ArrayList targetMapList = new ArrayList(tgSel.values());

            if (tgSel != null) {
                for (Iterator ite = targetMapList.iterator(); ite.hasNext();) {
                    TargetChannelMap tchmap = (TargetChannelMap) ite.next();
                    targetList.add(tchmap.getTarget());
                }
            }

            // check if the selected targets is greater than 1, if so we need to redirect to
            // target view in multiple mode. So set the appropriate session variables and set a
            // flag, multipleMode and redirect to the correct page.
            boolean multipleMode = false;

            try {
                if (targetList.size() > 0) {
                    if (targetList.size() > 1) {
                        multipleMode = true;
                        session.setAttribute(SESSION_MULTITGBOOL, "true");
                        GUIUtils.setToSession(request, MAIN_PAGE_M_TARGETS, (Object) targetList);

                        Object tgform = session.getAttribute("targetDetailsMultiForm");
                        tgform = new TargetDetailsMultiForm();

                        // Initializing the form clears out the check boxes that
                        // was set on the target's page previously
                        ((AbstractForm) tgform).initialize();
                        session.setAttribute("targetDetailsMultiForm", tgform);
                    } else {
                        session.removeAttribute(SESSION_MULTITGBOOL);
                        GUIUtils.setToSession(request, MAIN_PAGE_TARGET, (Object) targetList);

                        Object tgform = session.getAttribute("targetDetailsForm");
                        tgform = new TargetDetailsForm();
                        ((AbstractForm) tgform).initialize();
                        session.setAttribute("targetDetailsForm", tgform);
                    }
                } else {
                    // no targets are selected
                    // so go into single select mode, and clear out all the session variables
                    // so that the "no targets selected" page comes up".
                    session.removeAttribute(SESSION_MULTITGBOOL);
                    session.removeAttribute(MAIN_PAGE_TARGET);
                }

                if (DEBUG) {
                    Iterator iter = targetList.iterator();

                    if (iter.hasNext() == false) {
                        System.out.println("no targets");
                    } else {
                        int count = 0;

                        while (iter.hasNext()) {
                            System.out.println("target " + count++ + "= " + iter.next());
                        }
                    }
                }
            } catch (SystemException se) {
                GUIException guie = new GUIException(se);
                throw guie;
            }

            //redirect to the target action to query for packages
            if (multipleMode) {
               forward = mapping.findForward("multitarget");
            } else {
               forward = mapping.findForward("target");
            }
        } else {
            // the user is switching from target view page to package view
            // now figure out the package list
            Map       pkgSel = (Map) session.getAttribute(SESSION_PKGS_FROMTGS_SELECTED);
            ArrayList targetMapList = new ArrayList(pkgSel.values());
            ArrayList channels = new ArrayList();

            if (pkgSel != null) {
                for (int i = 0; i < targetMapList.size(); i++) {
                    TargetChannelMap tempmap = (TargetChannelMap) targetMapList.get(i);

                    if (DEBUG) {
                        System.out.println(" The following channels are checked " + tempmap.getChannel().getUrl());
                    }

                    channels.add(tempmap.getChannel());
                }
            }

            // Check if the selected packages is greater than 1, if so we need to redirect to
            // package view in multiple mode. So set the appropriate session variables and set a
            // flag, multipleMode and redirect to the correct page.
            boolean multipleMode = false;

            try {
                if (channels.size() > 0) {
                    if (channels.size() > 1) {
                        multipleMode = true;
                        session.setAttribute(SESSION_MULTIPKGBOOL, "true");
                        GUIUtils.setToSession(request, MAIN_PAGE_M_PKGS, (Object) channels);
                    } else {
                        session.removeAttribute(SESSION_MULTIPKGBOOL);
                        GUIUtils.setToSession(request, MAIN_PAGE_PACKAGE, (Object) channels);
                    }
                } else {
                    // no channels are selected
                    // so go into single select mode, and clear out all the session variables
                    // so that the "no channels selected" page comes up".
                    session.removeAttribute(SESSION_MULTIPKGBOOL);
                    session.removeAttribute(MAIN_PAGE_PACKAGE);
                }
            } catch (SystemException se) {
                GUIException guie = new GUIException(se);
                throw guie;
            }

            Object tgform = session.getAttribute("PackageDetailsForm");
            tgform = new PackageDetailsForm();
            ((AbstractForm) tgform).initialize();
            session.setAttribute("PackageDetailsForm", tgform);

            //redirect to the target action to query for packages
            if (multipleMode) {
                forward = mapping.findForward("multipackage");
            } else {
                forward = mapping.findForward("package");
            }
           }
          } catch (Exception ex) {
                guiException = new GUIException(new CriticalException(ex.toString()));
                forward = mapping.findForward("failure");
          }
   }
 }
}
