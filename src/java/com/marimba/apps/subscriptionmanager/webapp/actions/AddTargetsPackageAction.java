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

import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;
import com.marimba.apps.subscriptionmanager.webapp.forms.AddTargetEditForm;

import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.intf.msf.acl.AclException;
import com.marimba.intf.msf.acl.AclStorageException;
import com.marimba.intf.msf.acl.IAclConstants;


/**
 * This class handles the situation when a user selects a target from the package view page.
 *
 * @author Jayaprakash Paramasivam
 * @version 1.0, 05/09/2005

 */
public final class AddTargetsPackageAction
        extends AbstractAction {
    final static boolean DEBUG = IAppConstants.DEBUG;

    /**
     * REMIND
     *
     * @param mapping  REMIND
     * @param form     REMIND
     * @param request  REMIND
     * @param response REMIND
     * @return REMIND
     * @throws IOException      REMIND
     * @throws ServletException REMIND
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException,
            ServletException {

        HttpSession session = request.getSession();
        init(request);
//        List targets = (ArrayList)session.getAttribute(ADD_REMOVE_TARGET);
        List targets = (ArrayList) session.getAttribute(ADD_REMOVE_PACKAGE);
        if (targets == null) {
            targets = new ArrayList(DEF_COLL_SIZE);
        }

        List packageTargets = new ArrayList(DEF_COLL_SIZE);

        if (targets.size() == 0) {
            session.removeAttribute(PENDING_POLICY_SAMEUSER);
            session.removeAttribute(PENDING_POLICY_DIFFUSER);
        }

        for(int i = 0;i < targets.size(); i++) {
            TargetChannelMap tcmap = (TargetChannelMap) targets.get(i);
            System.out.println("Content type patch: "+tcmap.getContentType());
            if (main.isPeerApprovalEnabled()) {
                main.updatePendingPolicySessionVar(request, (Target)tcmap.getTarget());
            }
            if(tcmap.getContentType().equals(CONTENT_TYPE_APPLICATION)) {
                packageTargets.add(tcmap);
            }
        }
        if(packageTargets.size() > 0) {
            session.setAttribute(ADD_REMOVE_PACKAGE, packageTargets);
        }
        session.setAttribute(SESSION_PERSIST_RESETRESULTS, "true");
        ((AddTargetEditForm) form).init(getResources(request), request.getLocale());

        return (mapping.findForward("success"));
    }
}
