// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

// $File$, $Revision$, $Date$

package com.marimba.apps.securitymgr.compliance.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.marimba.castanet.http.JavaHTTPSManager;
import com.marimba.castanet.ssl.HTTPSManager;
import com.marimba.intf.certificates.ICertProvider;
import com.marimba.intf.ssl.ISSLProvider;
import com.marimba.castanet.http.HTTPManager;
import com.marimba.castanet.tools.PluginConnection;
import com.marimba.intf.application.IApplicationContext;
import com.marimba.intf.castanet.IWorkspace;
import com.marimba.intf.http.HTTPConstants;
import com.marimba.intf.util.IConfig;
import com.marimba.io.FastInputStream;
import com.marimba.io.FastOutputStream;
import com.marimba.tools.config.ConfigUtil;
import com.marimba.tools.net.HTTPConfig;
import com.marimba.tools.util.DebugFlag;
import com.marimba.tools.util.Password;

/**
 * ChannelStoreAuthenticateEngine w.r.t handle channel store username/pwd authentication
 *
 * @author Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 */

public class ChannelStoreAuthenticateEngine {
    static final int MAGIC = 0xDaceDeed;
    static final int VERSION = 1;
    static final String TX_TOKEN = "461265FFEC4EB10B72B4759FDF3ABC95";

    int CHANNEL_STORE_USER_AUTH_REQUEST = 3;
    int CHANNEL_STORE_USER_AUTH_REPLY = 4;
    int USER_VALIDATED = 1 << 0;
    int USER_REJECTED = 1 << 1;
    int USER_VALIDATION_ERROR = 1 << 2;
    int USER_STATE_PENDING = 1 << 3;
    int USER_STATE_REJECTED = 1 << 4;
    int USER_STATE_NEED_MORE_INFO = 1 << 5;
    int USER_ALREADY_EXIST = -1;
    int USER_ROLE_CUSTOMER = 1 << 0;
    int USER_ROLE_PUBLISHER = 1 << 1;
    int USER_ROLE_CUSTOMER_PUBLISHER = 1 << 2;
    int USER_ROLE_ADMIN = 1 << 3;

    public static int DEBUG = DebugFlag.getDebug("DEFENSIGHT/CS", "DEBUG");
    private static String CS_URL = "http://products.marimba.com:80/channelstore";
    HTTPManager httpMgr;
    IConfig serverConfig;
    IApplicationContext context;
    IConfig tunerConfig;

    ISSLProvider ssl;
    HTTPConfig httpConfig;


    public void init(IApplicationContext context) {
        this.context = context;
        httpConfig = new HTTPConfig(new ConfigUtil((IConfig) context.getFeature("config")));
        try {
            ssl = (ISSLProvider) context.getFeature("ssl");
            ICertProvider cert = (ICertProvider) context.getFeature("certificates");
            tunerConfig = (IConfig) context.getFeature("tunerConfig");
            if (cert != null && tunerConfig != null) {
                try {
                    httpMgr = new JavaHTTPSManager(httpConfig, tunerConfig, cert);
                } catch (Throwable e) {
                    try {
                        if (ssl != null) {
                            httpMgr = new HTTPSManager(httpConfig, cert, ssl);
                        }
                    } catch (UnsatisfiedLinkError ule) {
                        httpMgr = new HTTPManager(httpConfig);
                    }
                }
            } else {
                httpMgr = new HTTPManager(httpConfig);
            }
        } catch (UnsatisfiedLinkError e) {
            httpMgr = new HTTPManager(httpConfig);
        }
    }

    protected void debug(int level, String msg) {
        if (DEBUG >= level) {
            System.out.println("CS-AuthenticateEngine: " + msg);
        }
    }

    // Authenticate channel store credentials by using Plug-In Connection
    public boolean authenticateUser(String userName, String decodedPassword) {
        IWorkspace ws = (IWorkspace) context.getFeature("workspace");
        String marimbaAppURL = CS_URL;
        boolean retVal = false;
        String chAuth = "Basic " + Password.encode(userName + ":" + decodedPassword);

        if (marimbaAppURL == null) {
            debug(3, "No URL set! So just exit without trying to authenticate incoming user as a Harman customer");
            return false;
        }
        debug(3, "chauth returned after encoding is: " + chAuth);
        try {
            PluginConnection conn = new PluginConnection(httpMgr, new HTTPConfig(new ConfigUtil(serverConfig)),
                    new URL(marimbaAppURL),
                    new URL(marimbaAppURL));
            int reply = -1;
            do {
                debug(3, "doing a connection open now");
                conn.open();
                debug(3, "Finished connection open!");
                if (chAuth != null) {
                    conn.setCredential(chAuth);
                    debug(3, "Set outgoing credentials as: " + chAuth);
                } else {
                    debug(3, "Not setting any credentials");
                }
                
                conn.setSegment(".configurator");
                conn.setTunerID(ws.getIdentifier());
                conn.addField("Request-Type", "channelstore");
                conn.addField("Request-sender", "tuner");
                conn.addField("tuner-id", Long.toString(ws.getIdentifier()));
                conn.addField("isclient", "true");
                
                debug(1, "Directly to fastoutputstream");
                FastOutputStream fout = new FastOutputStream(10);
                writeChannelStoreAuthRequest(userName, decodedPassword, fout);
                fout.flush();
                byte[] data = fout.toByteArray();
                debug(2, "Amount of data to be sent on socket: " + data.length);
                OutputStream out = conn.getOutputStream((int) data.length);
                out.write(data, 0, data.length);
                out.flush();
                reply = conn.reply();
            } while (reply == HTTPConstants.HTTP_RETRY);
            boolean goAhead = false;
            switch (reply) {
                case HTTPConstants.HTTP_OK:
                    debug(1, "Received HTTP OK");
                    goAhead = true;
                    break;
                default:
                    debug(1, "Returned http reply: " + reply);
                    break;
            }
            if (goAhead) {
                debug(1, "Now going ahead");
                FastInputStream in = new FastInputStream(conn.getInputStream());
                retVal = readChannelStoreResponse(in);
            }
            debug(3, "closing the plugin connection");
            conn.close();
        } catch (Throwable t) {
            if (DEBUG >= 3) {
                t.printStackTrace();
            }
        }
        return retVal;
    }

    protected void writeChannelStoreAuthRequest(String customerUserName,
                           String customerPassWord, FastOutputStream fout) throws IOException {
        fout.writeInt(MAGIC);
        fout.writeInt(VERSION);
        fout.writeInt(CHANNEL_STORE_USER_AUTH_REQUEST);
        fout.writeUTF(customerUserName);
        fout.writeUTF(customerUserName);
        fout.writeUTF(Password.encode(customerPassWord));
    }

    protected void debug(String msg) {
        if (DEBUG > 0) {
            debug(3, msg);
        }
    }

    protected boolean readChannelStoreResponse(FastInputStream in) throws IOException {
        boolean retVal = false;
        if (in.readInt() == CHANNEL_STORE_USER_AUTH_REPLY) {
            debug("Received CHANNEL_STORE_USER_AUTH_REPLY...");
            int authStatus = in.readInt();
            if (authStatus == USER_VALIDATED) {
                debug("User validated!");
                int userRole = in.readInt();
                if (userRole == USER_ROLE_ADMIN) {
                    debug("Role: admin");
                } else if (userRole == USER_ROLE_CUSTOMER) {
                    debug("Role: customer");
                } else if (userRole == USER_ROLE_PUBLISHER) {
                    debug("Role: publisher");
                } else if (userRole == USER_ROLE_CUSTOMER_PUBLISHER) {
                    debug("Role: customer_publisher");
                } else {
                    debug("Unknown role!");
                }
                String token = in.readUTF();
                debug("Token received from plugin: " + token);
                retVal = true;
            } else if (authStatus == USER_REJECTED) {
                debug("user rejected!");
                retVal = false;
            } else if (authStatus == USER_STATE_PENDING) {
                debug("pending for approval!");
                retVal = false;
            } else if (authStatus == USER_STATE_REJECTED) {
                debug("user account is rejected!");
                retVal = false;
            } else if (authStatus == USER_STATE_NEED_MORE_INFO) {
                debug("Admin have requested more details to approve this user!");
                retVal = false;
            } else if (authStatus == USER_VALIDATION_ERROR) {
                debug("user rejected!");
                retVal = false;
            }
        }
        while (in.available() > 0) {
            in.read();
        }
        return retVal;
    }

}