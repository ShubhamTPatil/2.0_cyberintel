// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions.common;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;

/**
 * This action is used to waiting on a long task on the server side.  A page that has a long task to execute should kick the task off into another thread. When
 * the thread completed, it should set a session variable, SESSION_LONGTASK, that this action monitors.
 *
 * @see com.marimba.apps.subscriptionmanager.webapp.action.SetPluginSaveAction
 * @see com.marimba.apps.subscriptionmanager.webapp.PublishThread
 * @see /products/subscriptionmanager/rsrc/web/includes/waitforcompletion.jsp
 */
public final class WaitForAction
    extends AbstractAction {
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
     * @throws IOException REMIND
     * @throws ServletException REMIND
     */
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
        throws IOException, 
                   ServletException {
        HttpSession session = request.getSession();
        String      pbsuccess = (String) session.getAttribute(SESSION_LONGTASK);
        String      waitForMarker = (String) session.getAttribute(SESSION_WAITFORMARKER);

        if (DEBUG) {
            System.out.println("PublishAction: pbsuccess = " + pbsuccess);
        }

        if ("success".equals(pbsuccess)) {
            session.removeAttribute(SESSION_LONGTASK);
            session.removeAttribute(SESSION_WAITFORMARKER);

            if (session.getAttribute(SESSION_CHANGE_REQUEST_MSG) != null) {
                String changeRequestMsg = (String) session.getAttribute(SESSION_CHANGE_REQUEST_MSG);
                if (DEBUG) {
                    System.out.println("PublishAction: changeRequestMsg = " + changeRequestMsg);
                }
                if ((changeRequestMsg != null) && (changeRequestMsg.trim().length() > 0)) {
                    request.setAttribute("changeRequestMsg", changeRequestMsg);
                }
            }
            session.removeAttribute(SESSION_CHANGE_REQUEST_MSG);

            return (mapping.findForward("success"));
        } else if ("failure".equals(pbsuccess)) {
            SystemException se = (SystemException) session.getAttribute(SESSION_LONGTASKEXC);
            GUIException    ge = new GUIException(se);
            session.removeAttribute(SESSION_LONGTASK);
            session.removeAttribute(SESSION_WAITFORMARKER);
            session.removeAttribute(SESSION_CHANGE_REQUEST_MSG);
            throw ge;
        }

        if ("true".equals(waitForMarker)) {
            //Then we are still in a holding pattern because a result
            //has not been found yet.
            return (mapping.findForward("hold"));
        } else {
            //A result was earlier found, but the user used the back button
            //and is now we need to go to the originating page.
            return (new ActionForward(mapping.getInput(), true));
        }
    }
}
