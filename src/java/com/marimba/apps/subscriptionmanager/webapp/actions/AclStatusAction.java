// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

/**
 * Created by IntelliJ IDEA.
 * User: Kumaravel
 * Date: Nov 18, 2005
 * Time: 6:55:05 PM
 * To change this template use File | Settings | File Templates.
 */
import org.apache.struts.action.*;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.LogConstants;
import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.AclStatusForm;
import com.marimba.apps.subscriptionmanager.webapp.forms.AbstractForm;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.intf.logs.ILogConstants;

/**
 * Loads the aclstatus from when user choose to turn on / off the acl feature.
 *
 * @author Kumaravel Ayyakkannu
 * @version 1, 11/16/2003
 */
public final class AclStatusAction extends Action implements ISubscriptionConstants {
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
     * @throws GUIException REMIND
     */
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  req,
                                 HttpServletResponse response)
        throws IOException,
                   ServletException {

        String  action = req.getParameter("action");
        ServletContext context = servlet.getServletConfig().getServletContext();
        SubscriptionMain smmain = TenantHelper.getTenantSubMain(context, req);
        AclStatusForm    formbean = (AclStatusForm) form;
        String returnPage = null;
        try {
            if("set_aclStatus".equals(action)) {
                boolean isAclOn = "true".equals(formbean.getValue(IWebAppConstants.ACL_STATUS));
                HashMap map = new HashMap();
                map.put(LDAPConstants.CONFIG_ACLON, String.valueOf(isAclOn));
                smmain.setSubscriptionConfigProperties(map);
                smmain.setAccessPermission();
                smmain.logAuditInfo(LogConstants.LOG_AUDIT_ACLSETTINGS, ILogConstants.LOG_AUDIT, "vDesk", isAclOn ? "Enabled" : "Disabled", req, ACL_STATUS);
                returnPage = "save";
            } else {
                boolean isAclOn = smmain.isAclOn();
                formbean.setValue(IWebAppConstants.ACL_STATUS, isAclOn ? "true": "false");
                returnPage = "view";
            }
        } catch (Exception se) {
            throw new GUIException(se);
        }
        return (mapping.findForward(returnPage));
    }
}
