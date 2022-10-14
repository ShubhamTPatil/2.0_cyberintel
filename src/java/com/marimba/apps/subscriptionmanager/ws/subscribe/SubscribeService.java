// Copyright 2009, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File: com/marimba/apps/subscriptionmanager/ws/subscribe/SubscribeService
// $, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.ws.subscribe;

import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscription.common.intf.LogConstants;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.webapps.webservices.BasicWebService;
import com.marimba.intf.util.IDirectory;
import com.marimba.intf.msf.policyapi.IPolicyManagement;
import com.marimba.intf.msf.websvc.IWebSvcEntry;
import com.marimba.intf.msf.IWebApplicationInfo;
import com.marimba.intf.msf.IUserPrincipal;
import com.marimba.intf.castanet.IChannel;
import com.marimba.tools.util.StringResourceBundle;
import com.marimba.intf.msf.ITenant;
import javax.servlet.ServletContext;
import javax.xml.soap.SOAPElement;

import org.apache.axis.message.SOAPBodyElement;
import java.util.ResourceBundle;
import java.util.Locale;
import java.net.URL;
import com.marimba.webapps.webservices.WebServiceAppFault;
import com.marimba.webapps.webservices.WebServiceException;

import org.w3c.dom.Element;

/**
 * Exposes subscribing a channel for a machine as a web service.
 *
 * @author Nageswara Rao V
 *
 */

public class SubscribeService extends BasicWebService
        implements  ISubscribeServiceConstants,
                    LogConstants,
                    IAppConstants,
                    ISubscriptionConstants {

    private static final String SERVICE_NAME = "SubscribeService";

	private ISubscribeServiceContext subsContext;
    IChannel channel;
    ITenant tenant;

    public SubscribeService(ServletContext context, ITenant tenant) {
        super(context, SERVICE_NAME, "Subscription Manager");
        this.tenant = tenant;
        subsContext = new SubscribeServiceContext();
	}

	protected IWebSvcEntry createWebSvcEntry() {
        IWebApplicationInfo webAppInfo = (IWebApplicationInfo)servletContext.getAttribute(
                                            "com.marimba.servlet.context.info");
        ResourceBundle appRes = webAppInfo.getResourceBundle("webapp/WEB-INF/classes/ApplicationResources", Locale.ENGLISH);
        subsContext.setApplicationResources(appRes);
        channel =
        	((IWebApplicationInfo)
        			servletContext.getAttribute(
        					"com.marimba.servlet.context.info")).getChannel();

        IWebSvcEntry wse =  registry.createWebSvcEntry(
        		"SubscribeService", appRes.getString("webservices.subscribeservice.desc"),
        		IWebSvcEntry.EP_TYPE_CMS_SERVICE,
        		SERVICE_NAME,
        		channel,
        		"/webapp/WEB-INF/wsdl/SubscribeService.wsdl");
        // need to define wsdl
        return wse;
	}

	protected String getNamespacePrefix() {
        return "sm";
    }

	protected String getNamespaceURI() {
		return "http://schemas.bmc.com/scm/subscribe";
	}

    protected ResourceBundle getLogResourceBundle() {
        // Get the log-resources
        try {
            IChannel channel =
            	((IWebApplicationInfo)
            			servletContext.getAttribute(
            					"com.marimba.servlet.context.info")).getChannel();
            URL base = new URL(
            		"tuner://" +
            		channel.getAddress() +
            		"/" + channel.getPath() + "/");
            return StringResourceBundle.getBundle(base, "log");
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

	protected int getLogOffset() {
		return LOG_AUDIT_WS_WEBCOMMON_OFFSET;
	}

	public Element[] handleService(Element[] inputMsg,
			                       IUserPrincipal user)
					 throws Exception {
		Element response[] = new Element[1];
		try {
			response[0] = handleService0(inputMsg, user);
		} catch(WebServiceException e) {
			channel.log(e.getId(),-1, LOG_AUDIT, SERVICE_NAME, null, null, SUB_SERVICE);
            // generate application error response
			response[0] =
				new WebServiceAppFault(subsContext, e.getId()).toSOAP();

		} catch(Exception e) {
			log(LOG_AUDIT_WS_FATAL_ERROR, LOG_MAJOR, SERVICE_NAME, e);

			// generate a fatal error response
			response[0] = new WebServiceAppFault(
					subsContext, LOG_AUDIT_WS_FATAL_ERROR, e).toSOAP();
		}
		return response;
	}

	public Element handleService0(Element[] inputMsg, IUserPrincipal user) throws Exception {
        if(inputMsg == null) {
            channel.log(LOG_AUDIT_WS_INVALID_ELMNT, -1, LOG_AUDIT, SERVICE_NAME, null, null, SUB_SERVICE);
            return new WebServiceAppFault(
                    subsContext,
                    LOG_AUDIT_WS_INVALID_ELMNT, null).toSOAP();
        }

        if(inputMsg.length != 1) {
            channel.log(LOG_AUDIT_WS_INVALID_ELMNT_CNT, -1, LOG_AUDIT, SERVICE_NAME, null, null, SUB_SERVICE);

            return new WebServiceAppFault(
                    subsContext,
                    LOG_AUDIT_WS_INVALID_ELMNT_CNT, null).toSOAP();
        }
        SubscribeHandler subsHandler = new SubscribeHandler(subsContext);
        SOAPElement response = subsHandler.perform(new SOAPBodyElement(inputMsg[0]), user);
        return response;
    }

    private class SubscribeServiceContext
		          implements ISubscribeServiceContext {

        IPolicyManagement policyAPI;

        ResourceBundle appRes;

        public void setApplicationResources(ResourceBundle appRes) {
            this.appRes = appRes;
        }

        public ResourceBundle getApplicationResources() {
            return this.appRes;
        }

        public void setPolicyAPI(IPolicyManagement api) {
            this.policyAPI = api;
        }

        public IPolicyManagement getPolicyAPI() {
            return (IPolicyManagement)tenant.getManager(IPolicyManagement.SERVICE_NAME);
            //return this.policyAPI;
        }

		public String getServiceName() {
			return SERVICE_NAME;
		}

		public String getLogMessage(int id) {
			return SubscribeService.super.getLogMessage(id);
		}

		public void log(int id, int severity,
				String description, Throwable exception) {
			SubscribeService.super
			                      .log(id, severity, description, exception);
		}

		public IDirectory getApplicationFeatures() {
			return SubscribeService.super.getApplicationFeatures();
		}
	}
}
