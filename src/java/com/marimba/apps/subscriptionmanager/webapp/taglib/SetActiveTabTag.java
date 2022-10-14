// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.taglib;

import java.io.*;

import java.net.*;

import java.util.*;

import javax.naming.*;
import javax.naming.directory.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import com.marimba.apps.subscription.*;
import com.marimba.apps.subscription.common.*;
import com.marimba.apps.subscription.common.util.LDAPUtils;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.intf.*;
import com.marimba.apps.subscriptionmanager.webapp.system.*;

import com.marimba.tools.ldap.*;

import com.marimba.tools.util.*;

import com.marimba.webapps.intf.*;

import com.marimba.webapps.tools.util.PropsBean;

/**
 * Custom tag that returns html for the banner tabs with the appropriate tabs highlighted.
 *
 * @author Michele Lin
 * @version 1.3, 07/17/2002
 */
public class SetActiveTabTag
    extends TagSupport
    implements IWebAppConstants {
    final static boolean DEBUG = com.marimba.apps.subscriptionmanager.intf.IAppConstants.DEBUG;
    final static String  TARGET_VIEW = "target_view";
    final static String  PACKAGE_VIEW = "package_view";
    final static String  NEW_ASSIGNMENT = "new_assignment";
    final static String  CONFIGURATION = "configuration";

    /**
     * Returns the correct html segment for the tabs in the banner. This uses the path of the request to determine which tab should be highlighted.
     *
     * @return REMIND
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doStartTag()
        throws JspException {
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
        HttpSession        session = (HttpSession) pageContext.getSession();
        DistributionBean   distbean = (DistributionBean) session.getAttribute(SESSION_DIST);
        String             distType = NEW;

        if (distbean != null) {
            distType = distbean.getType();
        }

        String result = "";
        String uri = req.getRequestURI();

        // remove '/sm/servlet' from uri
        String context = req.getContextPath();

        if (uri.startsWith(context)) {
            uri = uri.substring(context.length());
        }

        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }

        if (DEBUG) {
            System.out.println("uri= " + uri);
        }

        if (uri.startsWith("main_view") || uri.startsWith("target")) {
            result = TARGET_VIEW;
        } else if (uri.startsWith("distribution") && NEW.equals(distType)) {
            result = NEW_ASSIGNMENT;
        } else if (uri.startsWith("package")) {
            result = PACKAGE_VIEW;
        } else if (uri.startsWith("config")) {
            result = CONFIGURATION;
        }

        if (DEBUG) {
            System.out.println("result= " + result);
        }

        req.setAttribute("active_tab", result);

        return (SKIP_BODY);
    }

    /**
     * Process the end of this tag.  The default implementation does nothing.
     *
     * @return REMIND
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doEndTag()
        throws JspException {
        return (EVAL_PAGE);
    }
}
