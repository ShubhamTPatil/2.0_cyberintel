// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.webapps.intf.CriticalException;
import com.marimba.apps.subscriptionmanager.approval.ApprovalUtils;
import com.marimba.apps.subscriptionmanager.approval.LoadDBPolicy;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction.SubscriptionDelayedTask;
import com.marimba.apps.subscriptionmanager.webapp.forms.PolicyStatusForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.webapps.tools.util.WebAppUtils;
import com.marimba.tools.config.ConfigProps;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.intf.IWebAppsConstants;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * Description about the class PolicyStatusAction
 *
 * @author	 svasudev
 * @version  $Revision$,  $Date$
 *
 */

public class PolicyStatusAction extends AbstractAction {

    String userName;
    LoadDBPolicy dbPolicy;
    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        return new PolicyStatusActionTask(mapping, form, request, response);
    }

    protected class PolicyStatusActionTask extends SubscriptionDelayedTask {
    	PolicyStatusActionTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
            super(mapping, form, request, response);
        }

        public String getWaitMessage() {
            return getString(locale, "page.approval.common.processing.request");
        }

		public void execute() {
			init(request);
			forward = mapping.findForward("view");
			boolean hasPermission = false;
			userName = GUIUtils.getUserName(request);
			debug("Logged in user: " + userName);
			try {
				ConfigProps config = main.getConfig();
				PolicyStatusForm statusForm = (PolicyStatusForm) form;
				// Just tweak to display page on safer side
				statusForm.setPendingPolicies(new ArrayList());
				if (!"true".equals(config.getProperty(PEER_APPROVAL_SETTINGS))) {
					System.out.println("PolicyStatusAction : Peer Approval Policy Setting is not enabled");
				} else {
					if (null == main.getDBStorage()) {
						statusForm.setPendingPolicies(new ArrayList());
						debug("Peer approval policy database storage is empty");
					} else {
						dbPolicy = new LoadDBPolicy(main.getDBStorage());
	
						String command = request.getParameter("command");
						String policyName = null;
						if ("details".equals(command) || "viewpendingpolicy".equals(command)) {
							policyName = request.getParameter("policyName");
						}
						if(null != policyName) {
							statusForm.setSearchPolicyStr(policyName);
						} else {
							statusForm.setSearchPolicyStr("");
						}
						hasPermission = ApprovalUtils.hasApproverPermission(request, main);
	
						if (hasPermission) {
							statusForm.setPendingPolicies(dbPolicy.getAllPolicy());
						} else {
							try {
								statusForm.setPendingPolicies(dbPolicy.getPolicyByUser(main.resolveUserDN(userName)));
							} catch (SystemException se) {
								if (DEBUG5)
									se.printStackTrace();
								statusForm.setPendingPolicies(new ArrayList());
							}
						}
					}
				}
			} catch(Exception ec) {
				ec.printStackTrace();
				WebAppUtils.saveInitException(session, new CriticalException("error.ldap.connect.failed"));
			}
			Object bean = request.getSession().getAttribute(IWebAppsConstants.INITERROR_KEY);

	    	if ((bean != null) && bean instanceof Exception) {
	    		//remove initerror from the session because it has served its purpose
	    		if (DEBUG) {
	    			System.out.println("PolicyStatusAction: critical exception found");
	    		}
	    		request.getSession().removeAttribute(IWebAppsConstants.INITERROR_KEY);
	    		forward = mapping.findForward("failure");
	    	}
		}
    }
    private void debug(String msg) {
        if (DEBUG) System.out.println("PolicyStatusAction: " + msg);
    }
}
