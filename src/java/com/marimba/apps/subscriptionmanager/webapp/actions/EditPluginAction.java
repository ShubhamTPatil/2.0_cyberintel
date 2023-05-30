// Copyright 1996-2015, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.webapp.forms.SetPluginForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.intf.db.IConnectionPool;
import com.marimba.intf.msf.IDatabaseMgr;
import com.marimba.webapps.intf.GUIException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This corresponds to the distribution assignment page.  It is used to take care of  clearing out the session variables that have been used for storing the
 * actions that the user has taken
 */
public final class EditPluginAction extends AbstractAction {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest  request, HttpServletResponse response) throws IOException, ServletException {
        init(request);

        ServletContext   sc = servlet.getServletConfig().getServletContext();
        SubscriptionMain main = TenantHelper.getTenantSubMain(sc, request);
        try {
            main.initLDAP();
            IUser user = GUIUtils.getUser(request);
            String loggedInUserRole = user.getUserRole();
            ((SetPluginForm) form).setDisableModify((isCloudEnabled() &&
                    (("CloudPrimaryAdmin".equals(loggedInUserRole)) || "CloudAdmin".equals(loggedInUserRole)
                            || "CloudOperator".equals(loggedInUserRole))) ? "true" : "false");
        } catch (Exception e) {
            throw new GUIException(e);
        }
        SetPluginForm selectForm = (SetPluginForm) form;
        selectForm.setPrevPage("");
        selectForm.initialize(main);
        // clear out the previous test results, in case they were set for 29454
        request.getSession().removeAttribute(TEST_RESULT);
        return (mapping.findForward("success"));
    }
}


