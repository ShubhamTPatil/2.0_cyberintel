// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.arsystem;

import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscription.common.intf.LogConstants;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.intf.logs.ILog;
import com.marimba.webapps.tools.util.WebAppUtils;
import org.apache.struts.util.MessageResources;
import org.apache.struts.action.ActionServlet;

import java.util.Locale;

/**
 * Logging the information into the channel log.
 *
 * @author Jayaprakash Paramasivam
 * @version 1.0 12/28/2005
 */

public class ARLogMgr
        implements IAppConstants,
        LogConstants,
        LDAPConstants {

    private ILog log;    
    private SubscriptionMain main;

    public ARLogMgr(SubscriptionMain main, ILog log) {
        this.main = main;
        this.log = log;
    }

    /**
     * Log an event.
     *
     * @param id REMIND
     * @param severity REMIND
     * @param description REMIND
     */
    public void log(int    id,
                    int    severity,
                    String description) {
        log(id, severity, description, null);
    }

    /**
     * Log an event.
     *
     * @param id REMIND
     * @param severity REMIND
     * @param description REMIND
     * @param exception REMIND
     */
    public void log(int       id,
                    int       severity,
                    String    description,
                    Throwable exception) {
        log(id, severity, description,exception, null);
        
    }
         /**
     * Log an event.
     *
     * @param id REMIND
     * @param severity REMIND
     * @param description REMIND
     * @param exception REMIND
     * @param target an additional argument
     */
    public void log(int       id,
                    int       severity,
                    String    description,
                    Throwable exception,
                    String    target) {
        if (null != main) {

            logaudit(log, main.getLogResources(), id, description, null, null, severity, null, exception, target);
            
        } else {
            if (null != exception) {
                exception.printStackTrace();
            }
        }
    }
    public void logaudit(ILog logaudit, MessageResources logres, int logid, String arg1,
                   String arg2, String arg3, int severity, String user, Throwable exception, String target) {


        String stlogid = Integer.toString(logid);
        //Get the message from the application resources
        String msg = getMessage(logres,Locale.getDefault(), stlogid, arg1, arg2, arg3);
        if (DEBUG) {
            System.out.println("ARLogMgr: log id = " + logid + ", logmsg = " + msg);
            if (null == logaudit) {
                System.out.println("ARLogMgr: logaudit is null");
            } else {
                System.out.println("ARLogMgr: logaudit is not null");
            }
        }
        main.log(logid, severity, null, user, msg, target);
    }
    public static String getMessage(MessageResources msgres, Locale locale, String msgid,
                             String arg1, String arg2, String arg3) {
        Object[] args = new Object[3];
        args[0] = arg1;
        if (null != arg2 ) {
            args[1] = arg2;
        } else {
            args[1] = "";
        }
        if (null != arg3) {
            args[2] = arg3;
        }
        return msgres.getMessage(locale,msgid,args);
    }
}
