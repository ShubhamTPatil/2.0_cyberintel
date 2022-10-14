// Copyright 2005, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// @(#)ComplianceServiceLoader.java, 1.0, 11/04/2005
package com.marimba.apps.subscriptionmanager.compliance.core;

import org.apache.log4j.Logger;

import com.marimba.apps.subscriptionmanager.TenantAttributes;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.users.UserManager;
import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;
import com.marimba.apps.subscriptionmanager.compliance.service.*;
import com.marimba.intf.msf.IUserPrincipal;
import com.marimba.intf.msf.*;
import com.metaparadigm.jsonrpc.JSONRPCBridge;


import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;

/**
 * Helper class that creates a JSON RPC bridge and stores it in the session
 * along with the required Service classes.
 *  @author Manoj Kumar
 *  @version $Revision$Date:
 */
public final class ComplianceServiceLoader {

    static final Logger sLogger = Logger.getLogger(ComplianceServiceLoader.class);
    static final boolean isDebug = IAppConstants.DEBUG;

    /**
     * When a session is created, store the required Service objects and RPC bridge in the session.
     * @param session
     */
    public static void loadServiceObjects(HttpSession session, ITenant tenant) {

        ServletContext ctx = session.getServletContext();
        IUserPrincipal _user = (IUserPrincipal) session.getAttribute(ComplianceConstants.USER_PRINCIPAL);
        TenantAttributes tenantAttr = UserManager.getTenantAttr(_user.getTenantName()); 
        ComplianceMain cm = tenantAttr.getCompMain();
        PackageViewService pageService = new PackageViewService(cm, _user, tenant);
        TargetViewService targetService = new TargetViewService(cm,_user, tenant);
        ComplianceSummaryService summaryService = new ComplianceSummaryService(cm,_user, tenant);
        summaryService.setSession( session );
        MachineListService machineService = new MachineListService(cm, _user, tenant);
        ReportService reportService = new ReportService(cm,_user, tenant);

        // Set session attributes
        session.setAttribute(ComplianceConstants.PACKAGE_VIEW_SERVICE, pageService);
        session.setAttribute(ComplianceConstants.TARGET_VIEW_SERVICE, targetService);
        session.setAttribute(ComplianceConstants.COMPLIANCE_SUMMARY_SERVICE, summaryService);
        session.setAttribute(ComplianceConstants.MACHINE_LIST_SERVICE, machineService);

        // Don't need this for json-rpc
        session.setAttribute(ComplianceConstants.COMPLIANCE_REPORT_SERVICE, reportService);

        // Set json rpc client
        JSONRPCBridge bridge = new JSONRPCBridge();
        bridge.registerObject(ComplianceConstants.PACKAGE_VIEW_SERVICE,pageService);
        bridge.registerObject(ComplianceConstants.TARGET_VIEW_SERVICE,targetService);
        bridge.registerObject(ComplianceConstants.COMPLIANCE_SUMMARY_SERVICE,summaryService);
        bridge.registerObject(ComplianceConstants.MACHINE_LIST_SERVICE,machineService);
        session.setAttribute(ComplianceConstants.JSON_RPC_BRIDGE, bridge);
        if(isDebug)
            sLogger.info("Stored JSONRPCBridge and Service objects in Session");
    }


     /**
     * Remove the bridge and Service from the session.
     * @param session
     */
    public static void unloadServiceObjects(HttpSession session) {

	session.removeAttribute(ComplianceConstants.PACKAGE_VIEW_SERVICE);
	session.removeAttribute(ComplianceConstants.TARGET_VIEW_SERVICE);
	session.removeAttribute(ComplianceConstants.COMPLIANCE_SUMMARY_SERVICE);
	session.removeAttribute(ComplianceConstants.MACHINE_LIST_SERVICE);
	session.removeAttribute(ComplianceConstants.COMPLIANCE_REPORT_SERVICE);

        session.removeAttribute(ComplianceConstants.JSON_RPC_BRIDGE);
        if(isDebug)
            sLogger.info("Removed JSONRPCBridge and Service objects from Session");
    }

}



