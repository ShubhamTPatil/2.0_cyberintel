// Copyright 2009, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File: com/marimba/apps/subscriptionmanager/ws/subscribe/Subscribe
// $, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.ws.subscribe;

import com.marimba.webapps.webservices.WebServiceException;
import com.marimba.webapps.webservices.SOAPFactory;
import com.marimba.intf.msf.policyapi.IPolicyManagement;
import com.marimba.intf.msf.policyapi.ISubscribe;
import com.marimba.intf.msf.policyapi.PolicyManagementException;
import com.marimba.intf.msf.policyapi.IStatusConstants;
import com.marimba.intf.msf.IUserPrincipal;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.net.URL;

/**
 * Represents a subscribe elements
 *
 * @author Nageswara Rao V
 *
 */

public class Subscribe extends AbsSubsSOAPEnabledObject implements IStatusConstants {

    public static final String NAME = "subscribe";
    SubscribeData subsData;

    public Subscribe(ISubscribeServiceContext context) {
        super(context);
    }

    public SOAPElement execute(IUserPrincipal user) throws SOAPException {
        IPolicyManagement policyAPI = subsContext.getPolicyAPI();
        URL channelUrl = subsData.getChannelUrl();
        String targetNameStr = subsData.getTargetName();
        String targetName = null;
        String domain = null;

        if(targetNameStr.indexOf("\\") != -1) {
            targetName = targetNameStr.substring(0, targetNameStr.indexOf("\\"));
            domain = targetNameStr.substring(targetNameStr.indexOf("\\")+1, targetNameStr.length());
        } else {
            targetName = targetNameStr;
        }

        int statusCode = FAILED_GENERIC;

        try {
            ISubscribe subs = policyAPI.getSubscribeAPI(user);
            String[] channels = {channelUrl.toExternalForm()};
            // todo: Consider for multi domain architecture
            if(domain != null) {
                statusCode = subs.subscribe(targetName, domain, channels);
            } else {
                statusCode = subs.subscribe(targetName, "", channels);
            }
        } catch (PolicyManagementException policyExp) {
        }

        Status status;
        if(statusCode == SUBSCRIBE_SUCCESS) { // replace true with publish status
            status = new Status(subsContext, Status.SUCCESS, statusCode);
        } else {
            status = new Status(subsContext, Status.FAILURE, statusCode);
        }

        SOAPElement subscribe = SOAPFactory.createElement(NAME);
        if(subsData == null) {
            throw new IllegalStateException();
        }

        SOAPFactory.addChildElement(subscribe, subsData.toSOAP());
        SOAPFactory.addChildElement(subscribe, status.toSOAP());
        return subscribe;
    }

	public SOAPElement toSOAP() throws SOAPException {
		SOAPElement subs = SOAPFactory.createElement(NAME);
		if(subsData != null) {
			SOAPFactory.addChildElement(subs, subsData.toSOAP());
		}
		return subs;
	}

    public void fromSOAP(SOAPElement inputMsg)
	            throws SOAPException, WebServiceException {

        Iterator subsChildItr = SOAPFactory.getChildElements(inputMsg);
        while(subsChildItr.hasNext()) {
            Object subChild = subsChildItr.next();

            // Skip the null tag generated of type org.apache.axis.message.Text
            // todo: check the cases in which null tags are generated
            if(subChild instanceof SOAPElement) {
                SOAPElement subsChildElmnt = (SOAPElement) subChild;
                if(SubscribeData.NAME.equals(subsChildElmnt.getElementName().getLocalName())) {
                    subsData = new SubscribeData(subsContext);
                    subsData.fromSOAP(subsChildElmnt);
                } else {
                    throw new WebServiceException(LOG_AUDIT_WS_INVALID_POLICY_DATA_CHILD);
                }
            }
		}
		validate();
	}

	private void validate() throws WebServiceException {
		if(subsData == null) {
            throw new WebServiceException(LOG_AUDIT_WS_NO_POLICY_INFO);
		}

		// the channel URL is mandatory
		if(subsData.getChannelUrl() == null) {
			throw new WebServiceException(LOG_AUDIT_WS_INVALID_URL);
		}

		if(subsData.getTargetName() == null) {
			throw new WebServiceException(LOG_AUDIT_WS_INVALID_TARGET_NAME);
		}
	}

	/**
	 * Represents the publish status.
	 *
	 * <status>
     *  <code>1000</code>
     *  <result>success</result>
     *  <message>Subscribed channel successfully</message>
     * </status>
     *
	 * @author Nageswara Rao V
	 *
	 */
	private static class Status extends AbsSubsSOAPEnabledObject {

		private static final String NAME = "status";
		private static final String CODE_NAME = "code";
        private static final String RESULT_NAME = "result";
		private static final String MSG_NAME = "message";

		public static final String SUCCESS = "success";
		public static final String FAILURE = "failure";

		private String result;
        private int code;

		public Status(ISubscribeServiceContext context, String result, int code) {
			super(context);
			this.result = result;
            this.code = code;
		}

		public SOAPElement toSOAP() throws SOAPException {
			SOAPElement statusElmnt = SOAPFactory.createElement(NAME);
			SOAPFactory.addChildElement(
					statusElmnt,
					SOAPFactory.createTextElement(RESULT_NAME, result));

			SOAPFactory.addChildElement(
					statusElmnt,
					SOAPFactory.createTextElement(CODE_NAME, ""+code));

            //todo: get status string from respective resource bundle
			String messageKey =
				(SUCCESS.equals(result) ?
						"webservices.discplugin.pub.success" :
							"webservices.discplugin.pub.failure");

            ResourceBundle appRes = subsContext.getApplicationResources();

            String message =
				(SUCCESS.equals(result) ?
                    appRes.getString("webservices.subscribeservice.success")
						 : appRes.getString("webservices.subscribeservice.failure"));

			SOAPFactory.addChildElement(
					statusElmnt,
					SOAPFactory.createTextElement(MSG_NAME, message));
			return statusElmnt;
		}
	}
}
