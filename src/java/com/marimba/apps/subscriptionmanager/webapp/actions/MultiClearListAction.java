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

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * This action handles the clearing of the session variable  of targets for the multiple select page
 */
public final class MultiClearListAction
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
        String requiredRemove = null;

        /* If the user has chosen to select multiple targets, a different session
         * variable is to be used.
         */
        if (session.getAttribute(SESSION_MULTITGBOOL) != null) {
            //get the index that we care about
            String[]    stgidx = request.getParameterValues("targetid");
            int targetIndex = 0;
            List tglist = getSelectedTargets(session);
            if (tglist != null) {
                for(int targetCount =0;targetCount<stgidx.length;targetCount++) {
                    String selectedTarget = stgidx[targetCount];
                    int tgidx = Integer.parseInt(selectedTarget);
                    if( targetIndex != 0){
                        tgidx= tgidx - targetIndex;
                    }
                    targetIndex+=1;
                    tglist.remove(tgidx);
                    requiredRemove = "1";
                    session.setAttribute("requiredRemove",requiredRemove);
                }
            }


            /* IF we have removed the last target, indicate that no targets have been
             * selected
             */
            if (tglist.size() == 0) {
                session.removeAttribute(MAIN_PAGE_M_TARGETS);
                return mapping.findForward("failure");
            }
        }

        return mapping.findForward("success");
    }
}
