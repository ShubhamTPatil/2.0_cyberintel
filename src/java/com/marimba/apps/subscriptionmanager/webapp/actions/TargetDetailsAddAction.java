// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscriptionmanager.approval.ApprovalPolicyDTO;
import com.marimba.apps.subscriptionmanager.approval.LoadDBPolicy;
import com.marimba.apps.subscription.common.intf.IUser;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;

/**
 * This class handles the situation when a user selects a target from the target view page.  It deals with the fact that the user may be in single select mode
 * (one target) or multiple select mode.  It is used within the /main_ldap_navigation.jsp page.
 */
public final class TargetDetailsAddAction
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
     * @throws ServletException REMIND
     * @throws GUIException REMIND
     */
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
        throws ServletException {
        boolean  multiMode = false; //Used to determine if the user is in multiple select mode
        LoadDBPolicy dbPolicy;
        String modifiedUserDN = null;
        IUser user;
	    init(request);

		Target target = null;
		try {
	        target = getTarget(request, main);
		} catch (SystemException se) {
	        throw new GUIException(se);
		}
		HttpSession session = request.getSession();
		
		user = (IUser) session.getAttribute(SESSION_SMUSER);
		try {
            modifiedUserDN = main.resolveUserDN(user.getName());
        } catch (Exception e) {
            if (DEBUG) e.printStackTrace();
        }
	    String targetsSessionVar = MAIN_PAGE_TARGET;
        if (DEBUG) {
            System.out.println("TargetDetailsAddAction:target= " + target);
        }

        GUIUtils.initForm(request, mapping);

        /* If the user has chosen to select multiple targets, a different session
         * variable is to be used.
         */

        if (session.getAttribute(SESSION_MULTITGBOOL) != null) {
            multiMode         = true;
            targetsSessionVar = MAIN_PAGE_M_TARGETS;
        }

        List<Target> targetList = getSelectedTargets(session);
        
        if (null == targetList) {
            targetList = Collections.synchronizedList(new ArrayList<Target>(DEF_COLL_SIZE));
        } else {
            if (!multiMode) {
                targetList.clear();
            }
        }
         
        		

        // TODO: We should use a Set for targetList so that we dono't have to check for "contain"
        // Ensure that there are not repeats in the list
        boolean contains = false;
        synchronized (targetList) {
            for (ListIterator ite = targetList.listIterator(); ite.hasNext();) {
                Target itg = (Target) ite.next();
                if (itg.getId()
                        .equals(target.getId())) {
                    contains = true;
                    break;
                }
            }
        }

        if (!contains) {
            if (DEBUG) {
                System.out.println("ADDED TARGET= " + target);
            }
            targetList.add(target);

            // update pending policy session variable
            main.updatePendingPolicySessionVar(request, target);
        }
        
        // Set new Target list to session var
        try {
            GUIUtils.setToSession(request, targetsSessionVar, (Object) targetList);
        } catch (SystemException se) {
            GUIException guie = new GUIException(se);
            throw guie;
        }

        if  (request.getParameter("src") != null) {
            // We came from another tab, repaint the whole page
            return mapping.findForward("bothpanes");
        }
        // We came from the main target view page, repaint only the right hand pane
        return mapping.findForward("rpane");
    }
}
