// Copyright 2009, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File: com/marimba/apps/subscriptionmanager/ws/subscribe/SubscribeHandler
// $, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.ws.subscribe;

import com.marimba.webapps.webservices.WebServiceException;
import com.marimba.intf.msf.IUserPrincipal;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

/**
 * Subscription Management subscribe web service
 *
 * SOAP request
 *
 *  <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
 *      <SOAP-ENV:Header>
 *          <impl:SecurityTicket xmlns:impl="http://www.bmc.com/schemas/scm/cms/security">
 *              yrDwDQAHTWFyaW1iYQAAAAEAAAC6ACAzNTNBOENBQjRCNEE5RDNBQzFEQTM5OTZCODUyRDkzM
 *          </impl:SecurityTicket>
 *      </SOAP-ENV:Header>
 *      <SOAP-ENV:Body>
 *          <sm:subscribe xmlns:sm="http://schemas.bmc.com/scm/policy">
 *              <sm:subscribe-data>
 *                  <sm:targetname>nrao2</sm:targetname>
 *                  <sm:channelurl>http://tx/InventoryService</sm:channelurl>
 *              </sm:subscribe-data>
 *          </sm:subscribe>
 *      </SOAP-ENV:Body>
 *  </SOAP-ENV:Envelope>
 *
 * SOAP response
 *
 * <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
 *                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
 *                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
 *  <soapenv:Body>
 *      <sm:subscribe xmlns:sm="http://schemas.bmc.com/scm/policy">
 *      <sm:status>
 *          <sm:code>1000</sm:code>
 *          <sm:desc>success</sm:desc>
 *          <sm:message>subscribed channel successfully</sm:message>
 *      </sm:status>
 *      </sm:subscribe>
 *  </soapenv:Body>
 * </soapenv:Envelope>
 *
 * @author Nageswara Rao V
 *
 */

public class SubscribeHandler {

    ISubscribeServiceContext policyContext;

    protected SubscribeHandler(ISubscribeServiceContext context) {
		this.policyContext = context;
	}

	public SOAPElement perform(SOAPElement inputMsg, IUserPrincipal user)
	            throws SOAPException, WebServiceException {
		Subscribe subs = new Subscribe(policyContext);
		subs.fromSOAP(inputMsg);
		return subs.execute(user);
	}
}
