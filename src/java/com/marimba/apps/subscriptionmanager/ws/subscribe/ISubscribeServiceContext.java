// Copyright 2009, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File: com/marimba/apps/subscriptionmanager/ws/subscribe/ISubscribeServiceContext
// $, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.ws.subscribe;

import com.marimba.webapps.webservices.IWebServiceContext;
import com.marimba.intf.msf.policyapi.IPolicyManagement;

import java.util.ResourceBundle;

/**
 * Environment to let the command objects in the chain get access to
 * the references defined in SubscribeService
 *
 * @author Nageswara Rao V
 *
 */
public interface ISubscribeServiceContext extends IWebServiceContext {
    // Method to get service name
    String getServiceName();

    // setter for policy-api handler
    void setPolicyAPI(IPolicyManagement policyAPI);

    // get handler for policy-api
    IPolicyManagement getPolicyAPI();

    // Resource bundle for accessing logs
    void setApplicationResources(ResourceBundle appRes);

    // get handler for application resource bundle
    ResourceBundle getApplicationResources();
}
