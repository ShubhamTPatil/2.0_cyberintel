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

import com.marimba.webapps.intf.*;

import com.marimba.webapps.tools.util.PropsBean;
import org.apache.struts.action.Action;
import org.apache.struts.util.MessageResources;

/**
 * Custom tag that returns the html for the drop down box for paging through a result set in a jsp page.
 *
 * @author Michele Lin
 * @version 1.2, 02/05/2002
 */
public class GetDropDownPagingTag
    extends TagSupport
    implements IWebAppConstants {
    final static boolean DEBUG = com.marimba.apps.subscriptionmanager.intf.IAppConstants.DEBUG;
    final static boolean DEBUG2 = com.marimba.apps.subscriptionmanager.intf.IAppConstants.DEBUG2;    
    final static boolean DEBUG3 = com.marimba.apps.subscriptionmanager.intf.IAppConstants.DEBUG3;    
    int                  instance;

    /**
     * REMIND
     *
     * @param instance REMIND
     */
    public void setInstance(int instance) {
        this.instance = instance;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public int getInstance() {
        return this.instance;
    }

    /**
     * Returns the html for the drop-down options in the previous/next section of a jsp page that involves paging through a result set. This tag uses
     * information stored in Paging Bean to determine options in the drop-down options. This tag also requires that the javascript function: submitSelected be
     * included in the jsp page.
     *
     * @return REMIND
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doStartTag()
        throws JspException {
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
        HttpSession        session = (HttpSession) pageContext.getSession();
        MessageResources resources = (MessageResources) pageContext.getServletConfig().getServletContext().getAttribute(Action.MESSAGES_KEY);

        PagingBean         pageBean = (PagingBean) session.getAttribute(SESSION_PAGE);
        int                total = pageBean.getTotal();
        int                currentPos = pageBean.getStartIndex();
        int                countPerPage = pageBean.getCountPerPage();
        boolean            hasMoreResults = pageBean.getHasMoreResults();

        String             submit = "submitSelected";

        if (instance == 2) {
            submit = "submitSelected2";
        }

        StringBuffer sb = new StringBuffer();

        sb.append("<select name=\"pagesList\" onChange=\"" + submit + "(document.ldapNavigationForm);\">");

        for (int i = 0; i < total; i += countPerPage) {
            int end = i + countPerPage;

            if (end > total) {
                end = total;
            }

            sb.append("<option value=\"" + i + "\" ");

            if ((i <= currentPos) && (currentPos < end)) {
                sb.append("selected");
            }

            sb.append(">");

            if (hasMoreResults) {
                sb.append((i + 1) + " - " + end + " " + resources.getMessage(req.getLocale(),"page.GetDropDownPagingTag.Of") + " " + "..");
            } else {
                sb.append((i + 1) + " - " + end + " " + resources.getMessage(req.getLocale(),"page.GetDropDownPagingTag.Of") + " " + total);
            }

            sb.append("</option>");
        }

        if (hasMoreResults) {
            sb.append("<option value=\"" + (total + 1) + "\">");
            sb.append("- more results -");
            sb.append("</option>");
        }

        sb.append("</select>");

        try {
            JspWriter writer = pageContext.getOut();
            writer.print(sb.toString());
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
