// Copyright 1997-2004, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions.patch;

import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscriptionmanager.webapp.forms.PatchAssignmentForm;
import com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

/**
 * PatchSaveAction extract the values from the Patch Assignment Form
 * and saves them in the DistributionBean object.
 *
 * This Action is called to commit the changes every time we move out
 * of any of the patch jsp pages
 *
 * @author Devendra Vamathevan
 *
 */
public final class PatchSaveAction extends AbstractAction {

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response){


        if (DEBUG) {
            System.out.println("PatchSaveAction called for page = PatchSaveAction");
            ((PatchAssignmentForm) form).dump();
        }

         saveAssignmentPage(request, form);


        // and forward to the next action
        String forward = ((PatchAssignmentForm) form).getForward();
        // strip out '/sm' if it exists
        String context = request.getContextPath();
        if (forward.startsWith(context)) {
            return ( new ActionForward( forward.substring(context.length()), true ) );
        }
        return (new ActionForward(forward, true));
    }

    private void saveAssignmentPage(HttpServletRequest request, ActionForm form) {
        PatchAssignmentForm patchAssignmentForm = (PatchAssignmentForm) form;

        Channel curapp;
        //get bean name that values are saved in
        String beanName = (String) patchAssignmentForm.getValue(SESSION_PERSIST_BEANNAME);
        //if null we never visited the page so we have nothing to save
        if (beanName == null) {
            return;
        }

        GenericPagingBean pageBean = (GenericPagingBean) request.getSession().getAttribute(beanName);
        ArrayList tglist = (ArrayList) pageBean.getResults();
        int startIdx = pageBean.getStartIndex();
        int endIdx = pageBean.getEndIndex();

        for (int i = startIdx; i < endIdx; i++) {


            curapp = (Channel) tglist.get(i);
            if (CONTENT_TYPE_PATCHGROUP.equals(curapp.getType())) {
                /* Since the channels can be left as inconsistent, we need the ability to
                * set this value to the channels in the assignment.
                */
                String stateInc = (String) patchAssignmentForm.getValue("stateInc#" + curapp.hashCode());


                if (DEBUG) {
                    System.out.println("DisAsgSetStatesAction: stateInc# = " + stateInc);

                }

                if ("true".equals(stateInc)) {
                    curapp.setState(INCONSISTENT);
                } else {
                    String state = (String) patchAssignmentForm.getValue("state#" + curapp.hashCode());
                    if (state != null) {
                        // If we haven't paged, the widgets will not be created
                        // therefore, the values for the states will be null.
                        // In that case, we use the default state (subscribe) that was set
                        // during the channel creation time in DistributionBean.setChannel
                        curapp.setState((String) patchAssignmentForm.getValue("state#" + curapp.hashCode()));
                    }
                }


                if ((curapp.getSecState() != null) && (request != null)) {
                    request.getSession()
                            .setAttribute("hasSecStates", "true");
                }

                // Set blackout exemption
                String paramstr = (String) patchAssignmentForm.getValue("exemptBo#" + curapp.hashCode());
                patchAssignmentForm.setValue("exemptBo#" + curapp.hashCode(), paramstr);
                if ((paramstr != null) && ("true".equals(paramstr))) {
                    curapp.setExemptFromBlackout(true);
                } else {
                    curapp.setExemptFromBlackout(false);
                }

                // Set wow deployment
                paramstr = (String) patchAssignmentForm.getValue("wowDep#" + curapp.hashCode());
                patchAssignmentForm.setValue("wowDep#" + curapp.hashCode(), paramstr);
                if ((paramstr != null) && ("true".equals(paramstr))) {
                    curapp.setWowEnabled(true);
                } else {
                    curapp.setWowEnabled(false);
                }

            }

        }

    }


}
