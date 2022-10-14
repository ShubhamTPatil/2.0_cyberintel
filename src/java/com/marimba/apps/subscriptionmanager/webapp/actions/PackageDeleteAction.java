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

import com.marimba.apps.subscription.common.intf.SubKnownException;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.objects.Channel;

import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;

import com.marimba.webapps.intf.*;

/**
 * This action is used from single select mode to delete packages that have been selected from Package View.  Additionally, it will set a session variable which determines if
 * all of the packages were selected.
 *
 * @author Jayaprakash Paramasivam
 * @version 1.0, 05/09/2005
 */
public class PackageDeleteAction
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
     * @throws GUIException REMIND
     */
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
        throws IOException, 
                   ServletException {
        if (!(form instanceof IMapProperty)) {
            InternalException ie = new InternalException(GUIUTILS_INTERNAL_WRONGARG, "PackageDeleteAction", form.toString());
            throw new GUIException(ie);
        }

        IMapProperty formbean = (IMapProperty) form;

        HttpSession session = request.getSession();

        DistributionBean distributionBean = new DistributionBean();
        PersistifyChecksAction.SelectedRecords tgSel =
                (PersistifyChecksAction.SelectedRecords)session.getAttribute(
                        (String)formbean.getValue(SESSION_PERSIST_SELECTED));

        boolean value = tgSel.isAllRecordsSelected();

        if (value) {
            session.setAttribute(SESSION_ALL,"true");
        }
        if (tgSel != null) {

            ArrayList targetList = new ArrayList();
            ArrayList targetMapList = new ArrayList(tgSel.getTargetChannelMaps());

            for (Iterator ite = targetMapList.iterator(); ite.hasNext();) {

                TargetChannelMap tchmap = (TargetChannelMap) ite.next();

                // Add to selected list only if TargetChannelMap doesn't belong to a different domain / namespace
                if(tchmap.getIsSelectedTarget().equals("true")) {
                    targetList.add(tchmap.getTarget());
                }
            }

            if (targetList.size() > 0) {
                distributionBean.setSelectedTargets(targetList);
            }
        }
        ArrayList targets = distributionBean.getTargets();
        if (DEBUG) {
            Iterator it = targets.iterator();
            while (it.hasNext()) {
                Target t = (Target)it.next();
                System.out.println("Targets in the list to be deleted"+t.getName());
            }
        }
        init(request);
        if (main.isPeerApprovalEnabled()) {
            for(Object pendingTarget : targets) {
                main.updatePendingPolicySessionVar(request, (Target)pendingTarget);
            }
        }
        session.setAttribute(SESSION_TGS_TODELETE, targets);

        return (mapping.findForward("success"));
    }
}
