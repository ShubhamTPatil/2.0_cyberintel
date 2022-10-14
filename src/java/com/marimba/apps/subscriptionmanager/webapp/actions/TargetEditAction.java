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

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.TargetEditForm;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscription.common.objects.Target;

/**
 * This action corresponds to the cancel action on the set distribution page.
 */
public final class TargetEditAction
        extends AbstractAction
        implements IWebAppConstants {
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
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest req,
                                 HttpServletResponse response)
            throws IOException,
            ServletException {
        TargetEditForm tEditForm = (TargetEditForm) form;
        String action = tEditForm.getAction();

        // if action is null, then get it from the request parameters
        // this means that either ok or cancel was selected and the action
        // was called by a javascript redirect, thus *not* submitting the form
        if ((action == null) || "".equals(action)) {
            action = req.getParameter("action");
        }

        String submittedTargetsStr = ((TargetEditForm) form).getSubmittedTargetsStr();
        String forwardURL = ((TargetEditForm) form).getForwardURL();
        HttpSession session = req.getSession();
        ArrayList selectedList = (ArrayList) session.getAttribute(ADD_REMOVE_SELECTED_PAGE_TARGETS);
        Integer[] submittedTargets = GUIUtils.parseSelectedStr(submittedTargetsStr);

        if (DEBUG) {
            tEditForm.dump();
            for (int d = 0; d < selectedList.size(); d++) {
                System.out.println("selectedList[" + d + "]= " + selectedList.get(d));
            }

            for (int d = 0; d < submittedTargets.length; d++) {
                System.out.println("submittedTargets[" + d + "]= " + submittedTargets[d]);
            }

        }
        if ("remove".equals(action)) {
            String[] targets = (String[]) tEditForm.getValue("targets");
            if (targets != null) {
                for (int i = 0; i < targets.length; i++) {
                    for (int j = 0; j < selectedList.size(); j++) {
                        if (((Target) selectedList.get(j)).getId().equals(targets[i])) {
                            selectedList.remove(j);
                            break;
                        }
                    }
                }
            }
            return (mapping.findForward("remove"));
        } else if (ACTION_REMOVE_SELECTED.equals(action)) {
            GUIUtils.removeItems(req, ADD_REMOVE_SELECTED_PAGE_TARGETS, selectedList, submittedTargets);
        } else if (ACTION_OK.equals(action)) {
            // set selected list to distribution
            DistributionBean distBean = getDistributionBean(req);
            distBean.setSelectedTargets(selectedList);

            // Clear the session attributes for selected targets
            session.removeAttribute(ADD_REMOVE_SELECTED_PAGE_TARGETS);

            // redirect to distribution page after OK
            return (mapping.findForward("success_OK"));
        } else if (ACTION_CANCEL.equals(action)) {
            // Clear the session attributes for selected targets
            session.removeAttribute(ADD_REMOVE_SELECTED_PAGE_TARGETS);

            // redirect to distribution page after Cancel
            return (mapping.findForward("success_Cancel"));
        }

        return (new ActionForward(forwardURL, true));
    }
}
