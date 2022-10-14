// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.util;

import java.awt.*;

import java.io.*;

import java.net.*;

import java.util.*;

import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;

import com.marimba.castanet.http.*;

import com.marimba.castanet.ssl.*;

import com.marimba.intf.application.*;

import com.marimba.intf.http.*;

import com.marimba.intf.ssl.*;

import com.marimba.intf.util.*;

import com.marimba.tools.config.*;

import com.marimba.tools.net.*;

import com.marimba.webapps.intf.*;

import marimba.io.*;

/**
 * Helper used for RPC connections.  This is used from SubcriptionMain in order to obtain the listing of users and user groups when users are sourced from the
 * transmitter.  Modifications were made to account for this working from within a web application
 *
 * @author Simon Wynn
 * @author Angela Saval
 * @version 1.3, 04/03/2002
 */
public class RPCHelper
    implements HTTPConstants,
                   IWebAppConstants {
    IConfig    localConfig;
    String     host;
    int        port;
    boolean    secure;
    IDirectory features; /* These are the container features that are accessible
    * from a web application.
    */

    /* Initializes the Host and port that will be used for making an RPC connection
     * @param features Used for obtain the various configurations needed for making
     * and RPC connection.  This includes the "tunerConfig" feature for the web a
     * application and the "ssl" feature if the connection is to secure.
     */
    public RPCHelper(IDirectory features,
                     String     url)
        throws SystemException {
        this.features = features;

        localConfig = (IConfig) features.getChild("tunerConfig");

        getRPCHost(url);
    }

    /**
     * Get the Transmitters RPC port by connecting to /rpc for the transmitter. The RPC port is used for listing the users and user groups.
     *
     * @param url The url of the transmitter to which we are to connect to obtain the the RPC port.
     *
     * @return int This port of the RPC obtained from the transmitter.  The url of the transmitter
     *
     * @throws SystemException REMIND
     * @throws InternalException REMIND
     * @throws KnownException REMIND
     */
    private int getRPCPort(String url)
        throws SystemException {
        boolean secure = false;

        if (url.startsWith("https")) {
            secure = true;
        }

        String rpcPort = null;
        URL    rpcURL = null;

        /* Attempt to construct a valid url using the transmitter url (url) and
         * the /rpc post-fix.  If we are not able to make the URL, then there has
         * been a problem with parsing and initializing the url defined in
         * "subscriptionmanager.publishurl".  It is expected that the transmitter
         * hostname passed into the RPCHelper has already been verified, so this
         * is an internal exception
         */
        try {
            rpcURL = new URL(url + "/rpc");
        } catch (Exception uexc) {
            throw new InternalException(GUIUTILS_INTERNAL_WRONGARG, "RPCHelper.url");
        }

        /* The configuration and HTTPManager are needed in order to make a
         * request to the transmitter to get the RPC port
         */
        HTTPConfig   httpConfig = new HTTPConfig(new ConfigUtil(localConfig));
		// ToDo for selva: please retrieve the channel and tenantName from appropriate place and set it below
//		httpConfig.setChannel();
//		httpConfig.setTenantName();
        ISSLProvider sslProvider = (ISSLProvider) features.getChild("ssl");

        HTTPManager  httpManager;

        try {
	        if (secure & (sslProvider != null)) {
	            httpManager = new JavaHTTPSManager(httpConfig, localConfig, sslProvider.getCertProvider());
	        } else {
	            httpManager = new HTTPManager(httpConfig);
	        }
        } catch(Throwable e) {
			try {
				if (secure & (sslProvider != null)) {
					httpManager = new HTTPSManager(httpConfig, sslProvider.getCertProvider(), sslProvider);
				} else {
					httpManager = new HTTPManager(httpConfig);
				}
			} catch (UnsatisfiedLinkError e1) {
				System.out.println("Exception occurred while connecting with ssl details : " + e1.getMessage());
				httpManager = new HTTPManager(httpConfig);
			} catch (Exception ex2) {
				System.out.println("Exception occurred while connecting with ssl details : " + ex2.getMessage());
				httpManager = new HTTPManager(httpConfig);
			}
		}

        HTTPRequest request = null;

        try {
            request = httpManager.create(rpcURL, httpConfig.getProxy(rpcURL.toString()));
        } catch (IllegalArgumentException ile) {
            //An illegal argument exception can be thrown if rpcURL is not a http URL
            throw new InternalException(ile, USETX_INVALID_RPC_URL_REQUEST, rpcURL.toString());
        }

        /* While making the request, it is possible that and UnknownHostException
         * or and IOException could be thrown.  Therefore, we enclose it in one
         * try statement
         */
        try {
            do {
                request.connect(secure); /* This can generate and UnknownHostException
                * that is handled later
                */

                request.addField("Content-type", "application/marimba");
                request.addField("Request-type", "query/3.0");
                request.get();
            } while (request.reply() == HTTP_RETRY); /* This can generate an IOException
            * because the request may be lost.
            */

            switch (request.getResult()) {
                case HTTP_OK:

                    FastInputStream in = new FastInputStream(request.getInputStream());
                    String          top = in.readLine(); //first part

                    if ((top != null) && !top.startsWith("<html><head>")) {
                        rpcPort = request.getField("RPC-Port");
                    } else {
                        rpcPort = in.readLine();

                        // if secure, read the next line since that will be the
                        // secure port
                        if (secure) {
                            rpcPort = in.readLine();
                        }

                        if (DEBUG) {
                            System.out.println("RPC port: " + rpcPort);
                        }
                    }

                    break;
            } //end of the switch statement
        } catch (UnknownHostException uhe) {
            /* Thrown when the URL specified cannot be resolved to an IP address.
             * A connection cannot be made
             */
            KnownException ke = new KnownException(uhe, USETX_UNKNOWN_HOST_RPC_PORT, rpcURL.toString());
            ke.setLogException(true); /* This exception is being logged because
            * HTTPRequest does not throw a granular enough
            * exception (distinguishing between NoRouteToHost
            * vs, NoConnection) that the exception trace
            * would be useful.
            */

            throw ke;
        } catch (IOException ioex) {
            /* This is thrown if during the middle of obtaining data from the request,
             * the connection goes down.
             */
            throw new KnownException(ioex, USETX_LOST_CONNECTION_RPC_TX, rpcURL.toString());
        } finally {
            //We must always close up the request.
            if (request != null) {
                request.close();
            }
        }

        try {
            return Integer.parseInt(rpcPort);
        } catch (NumberFormatException nfe) {
            throw new KnownException(USETX_INVALID_RPC_PORT_INT, rpcPort);
        }
    }

    /**
     * Generate the hostname:rpc port from the passed URL. Take into account inclusion of the http prefix, or not. This is needed because a connection is made
     * to the transmitter in order to obtain the RPC port.
     *
     * @param url The url of the host from which to obtain the user and user group listings.  This is to be the url of the plugin transmitter
     *
     * @exception SystemException Thrown when the host is not valid or not contactable for obtaining the RPC port.
     */
    private void getRPCHost(String url)
        throws SystemException {
        // trim off trailing '/'
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        /* The URL that is passed into the getRPCPort
         * is expected to be of the form <protocol>://<hostname:port>
         * This is because a connection is made to the transmitter
         * to obtain the port.  THerefore, if the url does not start
         * with the http, then it is prepended.
         *
         */
        if (!url.startsWith("http")) {
            url = "http://" + url;
        }

        // trim off any sub-directory
        int index1 = url.indexOf("//");

        if (index1 != -1) {
            int index2 = url.indexOf("/", index1 + 2);

            if (index2 != -1) {
                url = url.substring(0, index2);
            }
        }

        /* The RPC port is needed in order to make contact with
         * the endpoint.
         */
        port = getRPCPort(url);

        if (url.startsWith("https")) {
            secure = true;
        }

        if (url.startsWith("http")) {
            //strip off http(s) portion
            int index = url.indexOf("://");
            url = url.substring(index + 3);

            //strip off any port number
            int index2 = url.indexOf(":");

            if (index2 != -1) {
                url = url.substring(0, index2);
            }
        }

        host = url;
    }

    /**
     * Returns the RPC host
     *
     * @return REMIND
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the RPC port
     *
     * @return REMIND
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns true is RPC url is secure
     *
     * @return REMIND
     */
    public boolean isSecure() {
        return secure;
    }
}
