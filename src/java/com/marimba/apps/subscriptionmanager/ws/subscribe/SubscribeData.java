// Copyright 2009, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File: com/marimba/apps/subscriptionmanager/ws/subscribe/SubscribeData
// $, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.ws.subscribe;

import com.marimba.webapps.webservices.SOAPFactory;
import com.marimba.webapps.webservices.WebServiceException;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Iterator;

/**
 * Represents the subscribe-data of a policy object
 *
 * @author Nageswara Rao V
 *
 */
public class SubscribeData extends AbsSubsSOAPEnabledObject {

    public static final String NAME = "subscribe-data";

    private static final String CH_URL_ELMNT_NAME = "channelurl";
    private static final String TARGET_NAME_ELMNT_NAME = "targetname";

    String targetName;
    URL channelUrl;

    public SubscribeData(ISubscribeServiceContext context) {
        super(context);
    }

    public String getTargetName() {
        return this.targetName;
    }

    public URL getChannelUrl() {
        return this.channelUrl;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public void setChannelUrl(URL chUrl) {
        this.channelUrl = chUrl;
    }

	public SOAPElement toSOAP() throws SOAPException {

		SOAPElement metaData = SOAPFactory.createElement(NAME);

		if(channelUrl != null) {
			SOAPFactory.addChildElement(
					metaData,
					SOAPFactory.createTextElement(CH_URL_ELMNT_NAME,
                                                  channelUrl.toExternalForm()));
		}
		if(targetName != null) {
			SOAPFactory.addChildElement(
					metaData,
					SOAPFactory.createTextElement(TARGET_NAME_ELMNT_NAME,
							                      targetName));
		}
		return metaData;
	}

	public void fromSOAP(SOAPElement inputMsg)
	            throws SOAPException, WebServiceException {

		Iterator metaDataItr = SOAPFactory.getChildElements(inputMsg);

		while(metaDataItr.hasNext()) {
            Object subChild = metaDataItr.next();
            // Skip the null tag generated of type org.apache.axis.message.Text
            // todo: check the cases in which null tags are generated
            if(subChild instanceof SOAPElement) {
                SOAPElement subsDataElmnt = (SOAPElement) subChild;
                if(CH_URL_ELMNT_NAME.equals(subsDataElmnt.getElementName().getLocalName())) {
                    String url = SOAPFactory.getValue(subsDataElmnt);
                    try {
                        channelUrl = new URL(url);
                    } catch (MalformedURLException e) {
                        throw new WebServiceException(LOG_AUDIT_WS_INVALID_URL);
                    }
                } else if(TARGET_NAME_ELMNT_NAME.equals(subsDataElmnt.getElementName().getLocalName())) {
                    targetName = SOAPFactory.getValue(subsDataElmnt);
                } else {
                    throw new WebServiceException(LOG_AUDIT_WS_INVALID_POLICY_DATA_CHILD);
                }
            }
        }
	}
}
