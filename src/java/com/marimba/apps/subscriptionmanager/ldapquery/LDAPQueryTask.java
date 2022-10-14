// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.ldapquery;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import java.util.Date;

import javax.servlet.ServletContext;
import javax.naming.NamingException;

import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscription.common.LDAPQueryInfo;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;

import com.marimba.intf.msf.IServer;
import com.marimba.intf.msf.task.*;

import com.marimba.intf.util.IDirectory;

import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.SystemException;

/**
 * This class is used to create a querytask object to run as the CMS Task
 *
 * @author Kumaravel. A
 * @version Version 1.2
 */
public class LDAPQueryTask
    implements ITask, ISubscriptionConstants {
    // static variables
    private static final String FEATURES = "com.marimba.servlet.context.features";
    private static final String SERVLETCTX = "servletContext";

    //private static boolean sIsDebugEnabled = IAppConstants.DEBUG;
    private ITaskContext   taskContext = null;
    private ServletContext servletContext = null;
    private IServer        server = null;

    /**
     * Creates a new LDAPQueryTask object.
     */
    public LDAPQueryTask() {
    }

    /**
     * Initialize the task, this method is invoked by the TaskManager once during the lifecycle of this task.
     *
     * @param context
     *
     * @throws TaskException
     */
    public void init(ITaskContext context)
        throws TaskException {
        this.taskContext = context;
        servletContext   = (ServletContext) taskContext.getFeature(SERVLETCTX);
        server           = (IServer) ((IDirectory) servletContext.getAttribute(FEATURES)).getChild("server");
    }

    /**
     * The execute method is invoked by the CMS TaskManager on a schedule that is specified in the Properties file for this task.
     *
     * @param runtime notify the Taskmanager about the outcome of the Task.
     *
     * @throws TaskException
     */
    public void execute(ITaskRuntime runtime)
        throws TaskException {
        boolean runStatus = false;

        ITaskResult taskResult = runtime.getResult();
        LDAPQueryInfo         ldapQueryInfo = null;
        LDAPQueryCollnManager ldapqcm = null;

        Object           mainObj = servletContext.getAttribute(IWebAppsConstants.APP_MAIN);
        SubscriptionMain smmain = null;

        if (mainObj != null) {
            smmain = (SubscriptionMain) mainObj;
        }
        ldapqcm = new LDAPQueryCollnManager(servletContext, smmain);
        ldapqcm.logMgr.log(STATUS_NORMAL, LDAPQueryCollnManager.LOG_AUDIT,
                "Started executing LDAP query task at " + new Date().toString() + "....", null, LADPQUERY_EXECUTE);

        try {
            if (hasScheduleEnabled()) {
                ldapQueryInfo = new LDAPQueryInfo(taskContext.getTaskProperty("collnName"), taskContext.getTaskProperty("createdBy"));
                try {
                    ldapqcm.setQuery(ldapQueryInfo);
                } catch(NumberFormatException nfe) {
                    ldapqcm.logMgr.log(STATUS_FAILURE, ldapqcm.LOG_WARNING, "The property marimba.ldapqc.vlv.pagesize in prefs.txt cannot be a String",nfe);
                    return;
                }
                ldapqcm.refresh();
            } else {
                    ldapqcm.logMgr.log(STATUS_FAILURE, ldapqcm.LOG_WARNING, "The schedule has been set to off in the subscription configuration");
            }
        } catch (Exception se) {
            taskResult.setStatus(STATUS_FAILURE, "LDAP Query Task failed to run");
            ldapqcm.logMgr.log(STATUS_FAILURE, LDAPQueryCollnManager.LOG_CRITICAL,  "Query Collection run failed");
            throw new TaskException(se.getMessage());
        } finally {
            ldapqcm.logMgr.log(STATUS_NORMAL, LDAPQueryCollnManager.LOG_AUDIT,
                    "Finished executing LDAP Query Task at " + new Date().toString(), null, LADPQUERY_EXECUTE);
        }
    }

    /**
     * Invoked by the Taskmanager before unloading the Task. Any resources that have been allocated to this task should be released here.
     */
    public void destroy() {
        // clean up here.
    }

    // Set the CMS context here.
    public void setCMS(IServer cms) {
        server = cms;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public IServer getCMS() {
        return this.server;
    }

    private boolean hasScheduleEnabled()
        throws SystemException {
        //refresh the SubscriptionConfig properties and check the scheduled property
        boolean isTaskRunnable = false;
        Object  mainObj = servletContext.getAttribute(IWebAppsConstants.APP_MAIN);

        if (mainObj != null) {
            SubscriptionMain smmain = (SubscriptionMain) mainObj;
            smmain.refreshSubscriptionConfig(smmain.getAdminUser().getBrowseConn());

            String schedule = smmain.getSubscriptionConfig()
                                    .getProperty(LDAPConstants.CONFIG_LDAP_QUERY_COLLN_SCHED);
            isTaskRunnable = (schedule.equals("on")) ? true
                             : false;
        }
        return isTaskRunnable;
    }
}
