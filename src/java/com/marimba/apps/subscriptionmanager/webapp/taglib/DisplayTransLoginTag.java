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
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.objects.*;
import com.marimba.apps.subscription.common.util.LDAPUtils;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.intf.*;

import com.marimba.tools.ldap.*;

import com.marimba.tools.util.*;

import com.marimba.webapps.intf.*;

import com.marimba.webapps.tools.util.PropsBean;

/**
 * Custom tag that constructs an enumeration of the transmitters login keys for a subscription
 *
 * @author Angela Saval
 * @version 1.1, 01/09/2002
 */
public class DisplayTransLoginTag
    extends TagSupport
    implements IWebAppConstants,
                   ISubscriptionConstants {
    final static boolean DEBUG = com.marimba.apps.subscription.common.intf.IDebug.DEBUG;

    /**
     * Obtains the PAGE_TRANSLOGIN_SUB in order to go through the properties with the keychains.
     *
     * @return REMIND
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doStartTag()
        throws JspException {
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
        HttpSession        session = (HttpSession) pageContext.getSession();

        // get value from session
        Subscription sub = (Subscription) session.getAttribute(PAGE_TRANSLOGIN_SUB);

        //Get the property keys for the tuner properties
        try {
            Enumeration tpropkeys = sub.getPropertyKeys(PROP_TUNER_KEYWORD);
        } catch (SystemException se) {
            throw new JspException(se.toString());
        }

        // if there is a param value, then set this value to the session var
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
