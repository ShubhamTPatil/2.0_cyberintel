// Copyright 1997-2009, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.AddTargetEditForm;
import com.marimba.apps.subscriptionmanager.webapp.forms.AddTargetEditPatchForm;
import com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;
import com.marimba.webapps.intf.IMapProperty;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Saves the changes in the AddTargetEdit form to the TargetChannelMap
 * The primary and secondary states select boxes are identified as
 * state#&lt;channel object hashcode> and secState#&lt;channel object hashcode>
 *
 * @author Jayaprakash Paramasivam
 * @version 1.0, 05/09/2005
 */

public final class AddTargetInitAction extends AbstractAction
    implements IWebAppConstants {
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
        throws IOException, ServletException {

        IMapProperty formbean = (IMapProperty) form;

        String channelSessionVar = MAIN_PAGE_PACKAGE;
        String forward = null;
        String type = request.getParameter("type");

        ArrayList Channels = new ArrayList(DEF_COLL_SIZE);

        if (request.getSession().getAttribute(SESSION_MULTIPKGBOOL) != null) {
            channelSessionVar = MAIN_PAGE_M_PKGS;
        }
        saveState(request, formbean, Channels);

        if("package".equals(type)) {
            forward = ((AddTargetEditForm) form).getForward();
        } else if("patch".equals(type)) {
            forward = ((AddTargetEditPatchForm) form).getForward();
        }

	return new ActionForward(forward, true);
    }

    /**
     * If this method is called through the addTargetPackageState action as an intermediate step to the add targets,
     * add packages, edit schedule package, we also want to keep track of whether there are any secondary states set.
     * The session variable hasSecStates is used by schedule_active.jsp to decide whether to show the radio buttons
     * for activation sections.
     *
     * @param request REMIND
     * @param form REMIND
     *
     */
    public void saveState(HttpServletRequest request, IMapProperty form, ArrayList channels) {
		TargetChannelMap curapp;

		if (request != null) {
			request.getSession().removeAttribute("hasSecStates");
		}

		String formValue = (String) form.getValue(SESSION_PERSIST_BEANNAME);
		if (formValue == null) {
			return;
		}
		GenericPagingBean pageBean = (GenericPagingBean) request.getSession().getAttribute(formValue);
		ArrayList         tglist = (ArrayList) pageBean.getResults();
		int    startIdx = pageBean.getStartIndex();
		int    endIdx = pageBean.getEndIndex();

		for (int i = startIdx; i < endIdx; i++) {

                    curapp = (TargetChannelMap) tglist.get(i);
                   /* Since the channels can be left as inconsistent, we need the ability to
                     * set this value to the channels in the assignment.
                     */
                    if (CONTENT_TYPE_PATCHGROUP.equals(curapp.getContentType())){
		               //primary state
                        String state = (String) form.getValue("state#" + curapp.hashCode());
                        if(state != null) {
                            curapp.setState((String) form.getValue("state#" + curapp.hashCode()));
                        }
                        //exempt from black out
                        String paramstr = (String) form.getValue("exemptBo#" + curapp.hashCode());
                        if ((paramstr != null) && ("true".equals(paramstr))) {
                            curapp.setExemptFromBlackout(true);
                        } else {
                            curapp.setExemptFromBlackout(false);
                        }
                        //wow deployment
                        String paramstr1 = (String) form.getValue("wowDep#" + curapp.hashCode());
                        if ((paramstr1 != null) && ("true".equals(paramstr1))) {
                            curapp.setWowEnabled(true);
                        } else {
                            curapp.setWowEnabled(false);
                        }
                        if (DEBUG) {
                            System.out.println("AddTargetInitAction: Primary State = " + state);
                            System.out.println("AddTargetInitAction: Exempt from Blackout = " + paramstr);
                            System.out.println("AddTargetInitAction: WoW Deployment = " + paramstr1);
                        }
                    } else {
                        String stateInc = (String) form.getValue("stateInc#" + curapp.hashCode());
                        String secStateInc = (String) form.getValue("secStateInc#" + curapp.hashCode());

                            String state = (String) form.getValue("state#" + curapp.hashCode());
                            if (state != null) {
                                // If we haven't paged, the widgets will not be created
                                // therefore, the values for the states will be null.
                                // In that case, we use the default state (subscribe) that was set
                                // during the channel creation time in DistributionBean.setChannel
                                curapp.setState((String) form.getValue("state#" + curapp.hashCode()));

                            }
                            String secState = (String) form.getValue("secState#" + curapp.hashCode());
                            curapp.setSecState((String) form.getValue("secState#" + curapp.hashCode()));

                        if (DEBUG) {
                            System.out.println("AddTargetInitAction: stateInc# = " + state);
                            System.out.println("AddTargetInitAction: secStateInc# = " + secState);
                        }

                        if ((curapp.getSecState() != null) && (request != null)) {
                            request.getSession()
                                   .setAttribute("hasSecStates", "true");
                        }
                }
        }
    }
}
