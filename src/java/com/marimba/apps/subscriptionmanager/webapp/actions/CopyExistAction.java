// Copyright 1997-2004, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.intf.SubKnownException;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPPolicyHelper;
import com.marimba.apps.subscriptionmanager.webapp.intf.GUIConstants;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IMapProperty;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.intf.CriticalException;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForward;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.util.List;
import java.io.IOException;

/**
 * This action is used from the target details page for the copy operation
 *
 * @author  Jayaprakash Paramasivam
 * @version 1.0, 31/12/2004
 */

public class CopyExistAction extends AbstractAction {
    /**
     * @param mapping The ActionMapping used to select this instance
     * @param request The non-HTTP request we are processing
     * @param response The non-HTTP response we are creating
     *
     * @exception java.io.IOException if an input/output error occurs
     * @exception javax.servlet.ServletException if a servlet exception occurs
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException,
            ServletException {
            init(request);
            try {

                List targetList = (List)GUIUtils.getFromSession(request,MAIN_PAGE_TARGET);
                if (targetList != null && !targetList.isEmpty() ) {
                    Target target = (Target) targetList.get(0);
                    LDAPPolicyHelper policyFinder = new LDAPPolicyHelper(LDAPWebappUtils.getSubConn(request),main.getSubBase(), main.getUsersInLDAP(), main.getDirType());
                    policyFinder.addTarget(target.getId(),target.getType());
                    boolean hasPolicy = false;
                    if(ISubscriptionConstants.TYPE_ALL.equals(target.getType()) ||
                        LDAPVarsMap.get("TARGET_ALL").equals(target.getType())) {
                        hasPolicy = policyFinder.hasPolicies("all");
                    } else {
                        hasPolicy = policyFinder.hasPolicies(target.getId());
                    }

                    if(!hasPolicy) {
                    	System.out.println("No policy assigned for the selected target");
                    	return mapping.findForward("failure");
                    }
                }
                GUIUtils.setToSession(request, "context", "copyAdd");
            }catch(SystemException se){
                throw new GUIException(se);
            }
        return mapping.findForward("success");
    }
}
