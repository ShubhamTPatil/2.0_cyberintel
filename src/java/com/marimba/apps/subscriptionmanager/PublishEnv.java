// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager;

import java.io.*;

import java.net.*;

import com.marimba.castanet.publish.*;

import com.marimba.intf.util.*;

import com.marimba.tools.config.*;

import com.marimba.tools.net.*;

import com.marimba.tools.util.*;

/**
 * Our imlementation of the CastanetPublishEnvironment.
 */
public class PublishEnv
    extends HTTPConfig
    implements CastanetPublishEnvironment {
    final static boolean debug = false;
    String               proxyHost;
    String               proxyPassword;

    /**
     * Constructor
     *
     * @param config REMIND
     */
    public PublishEnv(ConfigUtil config) {
        super(config);
		// ToDo for selva: please retrieve the channel and tenantName from appropriate place and set it below
//		httpConfig.setChannel();
//		httpConfig.setTenantName();

	}

    /**
     * Creates a new PublishEnv object.
     *
     * @param config REMIND
     * @param proxyHost REMIND
     * @param proxyPassword REMIND
     */
    public PublishEnv(ConfigUtil config,
                      String     proxyHost,
                      String     proxyPassword) {
        super(config);
		// ToDo for selva: please retrieve the channel and tenantName from appropriate place and set it below
//		httpConfig.setChannel();
//		httpConfig.setTenantName();

		this.proxyHost     = proxyHost;
        this.proxyPassword = proxyPassword;
    }

    /**
     * CastanetPublishEnvironment interface methods
     *
     * @param file REMIND
     *
     * @return REMIND
     */
    public boolean hide(File file) {
        // return com.marimba.desktop.Desktop.getDesktop().hide(file);
        return true;
    }

    /**
     * HTTPEnvironment interface methods
     *
     * @return REMIND
     */
    public boolean enableNetwork() {
        return true;
    }

    /**
     * REMIND
     *
     * @param url REMIND
     *
     * @return REMIND
     */
    public String getProxy(String url) {
        return (proxyHost != null) ? proxyHost
               : super.getProxy(url);
    }

    /**
     * REMIND
     *
     * @param url REMIND
     * @param ask REMIND
     *
     * @return REMIND
     */
    public String getPassword(String  url,
                              boolean ask) {
        return (proxyHost != null) ? proxyPassword
               : super.getPassword(url, ask);
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getHTTPUserAgent() {
        return "Subscription/5.0";
    }

    /**
     * REMIND
     *
     * @param host REMIND
     * @param path REMIND
     *
     * @return REMIND
     */
    public String getHTTPCookie(String host,
                                String path) {
        return null;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public InetAddress getOutgoingHost() {
        return null;
    }
}
