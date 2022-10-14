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

import com.marimba.tools.ldap.*;

import com.marimba.tools.util.*;

import com.marimba.webapps.intf.*;

import com.marimba.webapps.tools.util.PropsBean;

/**
 * Custom tag that sets a value to the session.  If no value is found, then the default value is used.
 *
 * @author Michele Lin
 * @version 1.2, 11/19/2001
 */
public class SetToSessionTag
    extends TagSupport
    implements IWebAppConstants {
    final static boolean DEBUG = com.marimba.apps.subscriptionmanager.intf.IAppConstants.DEBUG;
    final static boolean DEBUG2 = com.marimba.apps.subscriptionmanager.intf.IAppConstants.DEBUG2;    
    final static boolean DEBUG3 = com.marimba.apps.subscriptionmanager.intf.IAppConstants.DEBUG3;    
    String               name;
    String               defaultValue;

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getName() {
        return this.name;
    }

    /**
     * REMIND
     *
     * @param name REMIND
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * REMIND
     *
     * @param defaultValue REMIND
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Checks request for 'name' parameter.  It it exists, then set the session variable to this value.  If it does not exist, then
     *
     * @return REMIND
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doStartTag()
        throws JspException {
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
        HttpSession        session = (HttpSession) pageContext.getSession();

        // get value from request
        String paramValue = (String) req.getParameter(name);

        // get value from session
        String sessionValue = (String) session.getAttribute(name);

        if (DEBUG) {
            System.out.println("name= " + name);
            System.out.println("paramValue= " + paramValue);
            System.out.println("sessionValue= " + sessionValue);
        }

        // if there is a param value, then set this value to the session var
        if ((paramValue != null) && !"".equals(paramValue)) {
            session.setAttribute(name, paramValue);
        } else {
            // if the session var does not have a value, set it to the default value
            if ((sessionValue == null) || "".equals(sessionValue)) {
                session.setAttribute(name, defaultValue);
            }
        }

        if (DEBUG) {
            System.out.println("resulting session value= " + (String) session.getAttribute(name));
        }

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
