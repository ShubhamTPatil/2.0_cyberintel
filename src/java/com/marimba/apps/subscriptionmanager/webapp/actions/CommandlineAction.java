// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.*;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.ISubscriptionConstants;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.cli.Commandline;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.CommandlineForm;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.SystemException;

/**
 * processes command line args
 *
 * @author Damodar Hegde
 * @version 1.10, 03/19/2002
 */
public final class CommandlineAction
    extends Action
    implements ISubscriptionConstants,
                   IWebAppConstants,
                   IWebAppsConstants {
    /**
     * REMIND
     *
     * @param mapping REMIND
     * @param form REMIND
     * @param request REMIND
     * @param response REMIND
     *
     * @return REMIND
     *
     * @throws IOException REMIND
     * @throws ServletException REMIND
     * @throws GUIException REMIND
     */
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
        throws IOException, 
                   ServletException {
        if (DEBUG5) {
	        System.out.println("PolicyAction called");
        }

        String commandargs = ((CommandlineForm) form).getCommandargs();

        if (DEBUG5) {
        	System.out.println("CommandlineAction: commandargs = " + commandargs);
        }
        
        ServletContext sc = servlet.getServletConfig()
                                   .getServletContext();

        SubscriptionMain main = TenantHelper.getTenantSubMain(sc, request);
        String dirType = null;
        if(null != main) {
        	dirType = main.getDirType();
        }
        Commandline    cli = new Commandline(sc, main, dirType);

        try {
            boolean exitCode = cli.processArgsWrapper(commandargs, (CommandlineForm) form);
        } catch (SystemException ex) {
            throw new GUIException(ex);
        }

        return (new ActionForward(mapping.getInput(), true));
    }
}
