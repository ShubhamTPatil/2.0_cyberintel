// Copyright 1997-2006, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.ws;

import com.marimba.apps.subscriptionmanager.intf.IARTaskConstants;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.intf.castanet.IChannel;
import com.marimba.intf.msf.websvc.IWebSvcRegistry;
import com.marimba.intf.msf.websvc.IWebSvcEntry;
import com.marimba.intf.msf.websvc.WebSvcRegException;
import com.marimba.tools.util.DebugFlag;

import java.util.Set;
import java.util.Iterator;

/**
 * Helper Class which publish the verification task service to CMS Registry.
 *
 * @author      Jayaprakash Paramasivam
 * @version 	$Revision$, $Date$
 */

public class WebServiceHelper {

    final static int DEBUG = DebugFlag.getDebug("SPM/WS");
    final static String SERVICE_NAME = "PolicyMgrTaskService";
    final static String WSDL_FILE = "/webapp/WEB-INF/wsdl/PolicyMgrTaskService.wsdl";
    final static String PROVIDER_NAME = "Subscription Manager";
    private SubscriptionMain main;

    public WebServiceHelper(SubscriptionMain main) {
        this.main = main;
    }

    public void publish(IChannel channel, IWebSvcRegistry registry) {
        String description = main.getAppResources().getMessage("ws.log.description");
        int endPointType = IWebSvcEntry.EP_TYPE_CMS_SERVICE;
        String endPointName = IARTaskConstants.VERIFY_TASK_SERVICE;
        IWebSvcEntry wse = registry.createWebSvcEntry(SERVICE_NAME, description, endPointType, endPointName, channel, WSDL_FILE);
        wse.setProvider(PROVIDER_NAME);

        try {
            registry.register(wse);
        } catch (WebSvcRegException wsre) {
            if(DEBUG > 4) {
                System.out.println("Failed to publish the vDesk Verify task Service");
                wsre.printStackTrace();
            }
        }
    }

    public void delete(IWebSvcRegistry registry) {
        try {
            Set registryset  = registry.getWebSvcEntry(SERVICE_NAME);
            for(Iterator serviceIterator = registryset.iterator(); serviceIterator.hasNext();) {
                IWebSvcEntry entry = (IWebSvcEntry) serviceIterator.next();
                registry.deleteWebSvcEntry(entry.getId());
            }
        } catch(WebSvcRegException wsre) {
            if(DEBUG > 4) {
                System.out.println("Failed to delete the vDesk Verify task Service");
                wsre.printStackTrace();
            }
        }
    }
}
