package com.marimba.apps.subscriptionmanager.webapp.actions.patch;



import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;

import com.marimba.apps.subscriptionmanager.webapp.forms.PatchAssignmentForm;

import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;

import com.marimba.apps.subscription.common.objects.Target;

import com.marimba.apps.subscription.common.ISubscriptionConstants;

import com.marimba.webapps.intf.GUIException;

import com.marimba.webapps.intf.SystemException;

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

 *

 *

 *

 * @author Devendra Vamathevan

 * @version 1.3, 1/10/2004

 */

public final class PatchInitAction extends AbstractAction{



    public ActionForward execute(ActionMapping mapping,

                                 ActionForm form,

                                 HttpServletRequest request,

                                 HttpServletResponse response)

            throws IOException, ServletException {







        // check if user clicked preview

        String action = request.getParameter("action");

        if (action == null) {

            ((PatchAssignmentForm) form).initialize(getResources(), getLocale(request), request);

            ((PatchAssignmentForm) form).clearPagingVars(request);

             DistributionBean distbean = getDistributionBean(request);

            ArrayList channels = distbean.getPatchChannels();

                            // display simulate only there is a single target

                // and the target is a machine

                String hideSimulate = "true";

                ArrayList targets = distbean.getTargets();

                if (targets.size() == 1) {

                    Target t = (Target) targets.get(0);

                    if (ISubscriptionConstants.TYPE_MACHINE.equals(t.getType())){

                        hideSimulate = "false";

                    }

                }

                ((PatchAssignmentForm) form).setValue("hideSimulate",hideSimulate);

        } else {

            if ("preview".equals(action)) {

                return mapping.findForward("preview");

            }

        }

        return (mapping.findForward("success"));



    }

}

