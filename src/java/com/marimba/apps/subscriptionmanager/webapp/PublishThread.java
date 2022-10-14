// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp;

import javax.servlet.http.HttpSession;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.PluginPropsProcessor;
import com.marimba.apps.subscriptionmanager.intf.*;

import com.marimba.tools.config.*;

import com.marimba.tools.util.*;

/**
 * This thread is used to publish the data up to the subscription plugin. It is called from the
 * com.marimba.apps.subscriptionmanager.webapp.actions.SetPluginSaveAction. The session variable, SESSION_LONGTASK, must be set when the publish has
 * completed. This is needed so that the com.marimba.apps.subscriptionmanager.webapp.actions.common.WaitforAction will find this variable so that it knows to
 * stop refreshing.
 *
 * @author Angela Saval
 * @version 1.8, 07/26/2002
 *
 * @see com.marimba.apps.subscriptionmanager.webapp.actions.SetPluginSaveAction
 * @see com.marimba.apps.subscriptionmanager.webapp.actions.common.WaitForAction
 */
public class PublishThread implements Runnable, IWebAppConstants {
    HttpSession session;
    SubscriptionMain smmain;
    ConfigProps userPluginProps;
    String[] parameter1;
    String[] parameter2;
    String url;

    /**
     * Creates a new PublishThread object.
     *
     * @param session REMIND
     * @param smmain REMIND
     * @param userPluginProps REMIND
     * @param parameter1 REMIND
     * @param parameter2 REMIND
     */
    public PublishThread(HttpSession session, SubscriptionMain smmain, ConfigProps userPluginProps, String[] parameter1, String[] parameter2) {
        this.session = session;
        this.smmain = smmain;
        this.userPluginProps = userPluginProps;
        this.parameter1 = parameter1;
        this.parameter2 = parameter2;
        this.url = userPluginProps.getProperty("subscriptionmanager.publishurl");
    }

    /**
     * REMIND
     */
    public void run() {
        try {
            if (DEBUG) {
                String[] propPairs1 = userPluginProps.getPropertyPairs();
                if (propPairs1 != null) {
                    for (int j = 0; j < propPairs1.length; j++) {
                        System.out.println("PublishThread: run(), " + propPairs1[j] + "=" + propPairs1[++j]);
                    }
                }
            }
            smmain.publishChannel((parameter1 != null) ? parameter1 [0] : null, (parameter2 != null) ? Password.decode(parameter2 [0]) : null, userPluginProps);

            //The publish succeeded, therefore we can save the configuration object
            ConfigProps config = smmain.getConfig();
            String[] propPairs = userPluginProps.getPropertyPairs();

            int i = 0;

            for (i = 0; i < propPairs.length; i++) {
                config.setProperty(propPairs [i], propPairs [++i]);
            }

            config.save();

            // Cache CMS config to Subscription properties.txt. This is used to determine whether CMS LDAP properties were modified.
            PluginPropsProcessor pluginPropsProc = new PluginPropsProcessor(smmain.getLDAPConfig(), smmain.getConfig());
            pluginPropsProc.setServerConfig(smmain.getServerConfig());
            pluginPropsProc.copyCMSProps();
            pluginPropsProc.copyCMSLDAPProps();
            pluginPropsProc.persistSubsProps();

        } catch (Exception se) {
            se.printStackTrace();
            if (DEBUG) {
                System.out.println("PublishThread: run(), setting failure for the long task");
            }

            //Create action errors from the System Exception to be displayed to the user
            if (session != null) {
                session.setAttribute(SESSION_LONGTASK, "failure");
                session.setAttribute(SESSION_LONGTASKEXC, se);
            }
            return;
        }

        if (DEBUG) {
            System.out.println("PublishThread: run(), setting success for the long task");
        }

        if (session != null ) {
            session.setAttribute(SESSION_LONGTASK, "success");
        }
    }
}
