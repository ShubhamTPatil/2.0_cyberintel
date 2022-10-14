// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.ldapquery;

import java.io.IOException;
import java.util.Locale;

import com.marimba.apps.subscription.common.intf.LogConstants;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.intf.logs.ILog;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.tools.util.DebugFlag;
import com.marimba.webapps.tools.util.WebAppUtils;
import org.apache.struts.util.MessageResources;

/**
 * Class that helps to log the messages during all stages of LDAP Query collection.
 *
 * @author Narayanan. A R
 * @author Kumaravel. A
 * @version 1.1, 20/12/2004
 */
public class LDAPQryLogMgr
    implements IAppConstants,
                   LogConstants,
                   LDAPConstants {
    final static int DEBUG = DebugFlag.getDebug("SM/LDAPQC/LOGMGR");
    ILog             applog;
    SubscriptionMain main;

    /**
     * Creates a new LDAPQueryTaskManager object.
     *
     * @param applog REMIND
     */
    public LDAPQryLogMgr(ILog applog, SubscriptionMain main) {
        this.applog = applog;
        this.main = main;
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
    log(id, severity, description, exception, null);
    }
/**
     * Log an event.
     *
     * @param id REMIND
     * @param severity REMIND
     * @param description REMIND
     * @param exception REMIND
     * @param target REMIND
     */
    public void log(int       id,
                    int       severity,
                    String    description,
                    Throwable exception,
                    String target) {
            
        if (null != main) {

            logaudit(applog, main.getLogResources(), id, description, null, null, severity, null, exception, target);            

        } else {
            if (null != exception) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public boolean getCollectionFailedMachinePolicy() {
        return true;
    }
    public void logaudit(ILog appLog, MessageResources logres, int logid, String arg1,
                   String arg2, String arg3, int severity, String user, Throwable exception, String target) {


        String stlogid = Integer.toString(logid);
        //Get the message from the application resources
        String msg = getMessage(logres, Locale.getDefault(), stlogid,arg1,arg2,arg3);        
        main.log(logid, severity, null, user, msg, target);
    }
    public String getMessage(MessageResources msgres, Locale locale, String msgid,
                             String arg1, String arg2, String arg3) {
        Object[] args = new Object[3];
        args[0] = arg1;
        if (null != arg2) {
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
