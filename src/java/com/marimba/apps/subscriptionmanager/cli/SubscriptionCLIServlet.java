// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.cli;

import org.apache.struts.util.MessageResources;
import org.apache.struts.util.MessageResourcesFactory;

import java.io.*;

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.intf.msf.IUserPrincipal;
import com.marimba.intf.logs.ILog;
import com.marimba.intf.logs.ILogConstants;

import com.marimba.intf.msf.*;

import com.marimba.intf.tuner.*;

import com.marimba.intf.util.*;

import com.marimba.webapps.intf.*;

import com.marimba.webapps.tools.util.*;

/**
 * Sample Servlet that deals with the new CSF command-line
 *
 * @author Johan Eriksson
 * @version 1.8, 05/08/2002
 */
public class SubscriptionCLIServlet
    extends HttpServlet
    implements IObserver,
                   IWebAppsConstants,
                   IWebAppConstants {
    IProducer        app;
    String[]         args;
    ServletContext   context;
    Commandline      cmdparser;
    SubscriptionMain main = null;
    PrintWriter      pw = null;

    /*
     * The resource files for the system errors
     */
    MessageResources app_rsrc = null;

    /*
     * The resource files for the system errors
     */
    MessageResources systemerrors_rsrc = null;

    /*
     * The resource file for the logs that an application is to use.
     */
    MessageResources logs_rsrc = null;

    /*
     * The log to write to
     */
    ILog applog = null;

    /**
     * Servlet-init - Get the producer-attribute and register as an observer
     *
     * @throws ServletException REMIND
     */
    public void init()
        throws ServletException {
        super.init();
        context = getServletConfig()
                      .getServletContext();
        app = (IProducer) context.getAttribute("com.marimba.servlet.context.producer");
        app.addObserver(this, 0, 0);

        initResources();
    }

    /**
     * Handle notifications
     *
     * @param sender REMIND
     * @param msg REMIND
     * @param args REMIND
     */
    public void notify(Object sender,
                       int    msg,
                       Object args) {
        switch (msg) {
            case WebAppConstants.WEBAPP_CLI:

                ICommandLine cli = (ICommandLine) args;
                IUserPrincipal userPrin = (IUserPrincipal)cli.getUser();
                String dirType = null;
//                if(null != userPrin && null != userPrin.getTenantName()) {
//                    String tenantName = userPrin.getTenantName();
//                    main      = TenantHelper.getTenantSubMain(context, tenantName);
//                } 
//                if(null != main || isHelpCMDExists(cli)) {
//                	dirType = (null != main) ? main.getDirType() : null;
//                	cmdparser = new Commandline(context, main, dirType);
//                    handleCLI(cli);
//                } else {
//                	pw = new PrintWriter(cli.getOutputStream(), true);
//                	printMessage("The logged in user does not have permission to access Policy Manager");
//                	cli.exit(1);
//                }
                printMessage("No command line option available for vDesk");
            	cli.exit(1);
                break;
        }
    }
    private boolean isHelpCMDExists(ICommandLine cli) {
    	if(null != cli) {
    		String[] args = cli.getArguments();
    		for (int i = 0; i < args.length; i++) {
    			if(args[i].equalsIgnoreCase("-help") || args[i].equalsIgnoreCase("help")) {
    				return true;
    			}
    		}
    	}
    	return false;
    }
    /**
     * Set the last arguments that were passed to this app This needs to be threadsafe
     *
     * @param args REMIND
     */
    synchronized void setLastArgs(String[] args) {
        this.args = args;
    }

    /**
     * Deal with the command-line interface. IMPORTANT: - This needs to be multi-thread safe - Make sure to ALWAYS call exit on the CLI-object!
     *
     * @param cli REMIND
     */
    synchronized void handleCLI(ICommandLine cli) {
        pw = new PrintWriter(cli.getOutputStream(), true);
        boolean retval = false;
        try {
            retval = cmdparser.processArgs(cli);
        } catch (SystemException se) {
            printException(se, cmdparser.getUserName());
            retval = true;
        }
        printMessage(cmdparser.getOutputStr());
        setLastArgs(args);
        if (retval) {
            cli.exit(1);
        }
        cli.exit(0);
    }

    /**
     * Destroy the servlet - remove ourselves as observers of the producer
     */
    public void destroy() {
    	try {
	        app_rsrc          = null;
	        systemerrors_rsrc = null;
	        logs_rsrc         = null;
	        cmdparser.destroy();
	        app.removeObserver(this);
	        super.destroy();
    	} catch(Exception ec) {
    		
    	}
    }

    /**
     * Initialize the message and log resources
     */
    private void initResources() {
        String appvalue = getServletConfig()
                              .getInitParameter("application");
        String systemvalue = getServletConfig()
                                 .getInitParameter("systemerrors_rsrc");
        String logsvalue = getServletConfig()
                               .getInitParameter("logs_rsrc");
        String factory = getServletConfig()
                             .getInitParameter("factory");

        String oldFactory = MessageResourcesFactory.getFactoryClass();

        if (factory != null) {
            MessageResourcesFactory.setFactoryClass(factory);
        }

        MessageResourcesFactory factoryObject = MessageResourcesFactory.createFactory();
        app_rsrc          = factoryObject.createResources(appvalue);
        systemerrors_rsrc = factoryObject.createResources(systemvalue);
        logs_rsrc         = factoryObject.createResources(logsvalue);

        MessageResourcesFactory.setFactoryClass(oldFactory);

        String value = context.getInitParameter("null");

        if (value == null) {
            value = "true";
        }

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes")) {
            systemerrors_rsrc.setReturnNull(true);
            logs_rsrc.setReturnNull(true);
            app_rsrc.setReturnNull(true);
        } else {
            systemerrors_rsrc.setReturnNull(false);
            logs_rsrc.setReturnNull(false);
            app_rsrc.setReturnNull(false);
        }

        applog = (ILog) context.getAttribute("com.marimba.servlet.context.log");
    }

    private void printException(SystemException se,
                                String          username) {
        if (se instanceof InternalException) {
            printException((InternalException) se, username);
        } else if (se instanceof KnownException) {
            printException((KnownException) se, username);
        }
    }

    private void printException(InternalException ie,
                                String            username) {
        if (DEBUG) {
            Throwable rx = ie.getRootCause();

            if (rx != null) {
                rx.printStackTrace();
            }
        }

        String msg = getMessage(ie);

        if (ie instanceof CriticalException) {
            printMessage(getMessage(app_rsrc, "page.internalerror.CriticalErrorMsg", null, null, null));
        } else {
            printMessage(getMessage(app_rsrc, "page.internalerror.InternalErrorMsg", null, null, null));
        }

        printMessage(msg);

        WebAppUtils.webAppLog(Locale.getDefault(), username, ie, systemerrors_rsrc, logs_rsrc, applog, null, "Command Line", LOG_WEBAPP_INTERNALEXCEPTION);
    }

    private void printException(KnownException ke,
                                String         username) {
        if (DEBUG) {
            Throwable rx = ke.getRootCause();
            System.out.println(getMessage(ke));

            if (rx != null) {
                rx.printStackTrace();
            }
        }

        printMessage(getMessage(ke));

        if (ke.getLogException()) {
            WebAppUtils.webAppLog(Locale.getDefault(), username, ke, systemerrors_rsrc, logs_rsrc, applog, null, "Command Line", LOG_WEBAPP_KNOWNEXCEPTION);
        }
    }

    void printMessage(String msg) {
        pw.println(msg);
    }

    String getMessage(SystemException se) {
        return getMessage(systemerrors_rsrc, se.getKey(), se.getArg0(), se.getArg1(), se.getArg2());
    }

    String getMessage(MessageResources rsrc,
                      String           key,
                      String           arg0,
                      String           arg1,
                      String           arg2) {
        return WebAppUtils.getMessage(rsrc, Locale.getDefault(), key, arg0, arg1, arg2);
    }
}
