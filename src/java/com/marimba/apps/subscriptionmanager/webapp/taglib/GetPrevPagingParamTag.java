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
import com.marimba.apps.subscriptionmanager.webapp.system.PagingBean;

import com.marimba.tools.ldap.*;

import com.marimba.tools.util.*;
import com.marimba.tools.util.URLUTF8Encoder; // Symbio added 05/20/2005

import com.marimba.webapps.intf.*;

import com.marimba.webapps.tools.util.PropsBean;

/**
 * Custom tag that returns the path to be used in the '&lt; Previous' link on a jsp page.
 *
 * @author Michele Lin
 * @version 1.1, 11/27/2001
 */
public class GetPrevPagingParamTag
    extends TagSupport
    implements IWebAppConstants {
    final static boolean DEBUG = com.marimba.apps.subscriptionmanager.intf.IAppConstants.DEBUG;
    final static boolean DEBUG2 = com.marimba.apps.subscriptionmanager.intf.IAppConstants.DEBUG2;    
    final static boolean DEBUG3 = com.marimba.apps.subscriptionmanager.intf.IAppConstants.DEBUG3;    

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

        PagingBean         pageBean = (PagingBean) session.getAttribute(SESSION_PAGE);
        int                currentPos = pageBean.getStartIndex();
        int                countPerPage = pageBean.getCountPerPage();
        int                previousPos = currentPos - countPerPage;

        String             resultStr = "0";

        if (previousPos > 0) {
            //resultStr = URLEncoder.encode(String.valueOf(previousPos));
            resultStr = URLUTF8Encoder.encode(String.valueOf(previousPos)); // Symbio modified 05/19/2005
        }

        String link = "startIndex=" + resultStr;

        if (DEBUG3) {
            System.out.println("link= " + link);
        }

        try {
            JspWriter writer = pageContext.getOut();

            if (DEBUG3) {
                System.out.println("link= " + link);
            }

            writer.print(link);
        } catch (IOException e) {
            throw new JspException(e.toString());
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
