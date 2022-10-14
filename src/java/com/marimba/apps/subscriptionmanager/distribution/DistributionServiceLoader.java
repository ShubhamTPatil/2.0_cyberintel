// Copyright 2009, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.distribution;

import com.marimba.intf.msf.IUserPrincipal;
import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.metaparadigm.jsonrpc.JSONRPCBridge;

import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;

/**
 * Distribution Service loader on Session init and destroy
 *
 * @author Marie Antoine
 */
public class DistributionServiceLoader {
    public static String DISTRIBUTION_SERVICE = "DistService";

    public static void loadServiceObjects(HttpSession session) {
        ServletContext ctx = session.getServletContext();
        IUserPrincipal _user = (IUserPrincipal) session.getAttribute(ComplianceConstants.USER_PRINCIPAL);
        System.out.println("Distribution Service Loader : Tenant name " + _user.getTenantName());
        SubscriptionMain main = TenantHelper.getTenantSubMain(ctx, session, _user.getTenantName());

        DistributionService service = new DistributionService(main, _user);
        service.setUserSession(session);
        
        session.setAttribute(DISTRIBUTION_SERVICE , service);

        JSONRPCBridge json_bridge = (JSONRPCBridge) session.getAttribute("JSONRPCBridge");
        if(json_bridge == null)
        {
          json_bridge = new JSONRPCBridge();
          session.setAttribute("JSONRPCBridge", json_bridge);
        }
        json_bridge.registerObject(DISTRIBUTION_SERVICE, service);
    }

    public static void unloadServiceObjects(HttpSession session) {
        session.removeAttribute(DISTRIBUTION_SERVICE);
    }
}
