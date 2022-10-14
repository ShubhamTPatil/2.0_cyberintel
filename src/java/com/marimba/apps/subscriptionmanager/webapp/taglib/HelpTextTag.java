// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.taglib;

import org.apache.struts.action.Action;
import org.apache.struts.taglib.bean.MessageTag;
import org.apache.struts.taglib.logic.*;
import org.apache.struts.util.MessageResources;
import org.apache.struts.Globals;

import java.io.*;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;

import com.marimba.webapps.intf.IWebAppsConstants;

/**
 * This tag checks to see if a key exists. It is used by help.jsp to determine if the "more..." link to indicate that the long help text exists should be
 * displayed. It checks for the existence of a key &lt;pagename>.introLong and returns a boolean based on that.
 *
 * @author Rahul Ravulur
 * @version 1.3, 02/22/2002
 */
public class HelpTextTag
    extends ConditionalTagBase
    implements IWebAppsConstants,
                   IWebAppConstants {
    /** The default Locale for our server. */
    private static final Locale defaultLocale = Locale.getDefault();
    String                      key;
    String                      localeKey = Globals.LOCALE_KEY;

    /** The servlet context attribute key for our resources. */
    private String bundle = Globals.MESSAGES_KEY;

    /**
     * We want to override the prefix that is used for the page's text.  There
     *
     * @return REMIND
     *
     * @exception JspException if a JSP exception has occurred
     */
    public boolean condition()
        throws JspException {
        /* Modify the key that will be used for looking up the resources for the page.
         * The standard is page.<name of page with.html extension>.<key passed in>
         */

        // Generate the hyperlink start element
        HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
        StringBuffer        results = new StringBuffer();
        HttpServletRequest  request = (HttpServletRequest) pageContext.getRequest();
        HttpSession         session = request.getSession();

        String              path = request.getRequestURI();

        if (path != null) {
            int lastslash = path.lastIndexOf('/');
            int dot = path.lastIndexOf('.');
            path = path.substring(lastslash + 1, dot);
        }

        this.key = TEXT_PAGE_PREFIX + "." + path + "." + IWebAppConstants.INTRO_LONG;

        // Acquire the resources object containing our messages
        MessageResources resources = (MessageResources) pageContext.getAttribute(bundle, PageContext.APPLICATION_SCOPE);

        if (resources == null) {
            throw new JspException(messages.getMessage("messageTag.resources", bundle));
        }

        // Calculate the Locale we will be using
        Locale locale = null;

        try {
            locale = (Locale) pageContext.getAttribute(localeKey, PageContext.SESSION_SCOPE);
        } catch (IllegalStateException e) { // Invalidated session
            locale = null;
        }

        if (locale == null) {
            locale = defaultLocale;
        }

        // Retrieve the message string we are looking for
        String[] args = {  };
        String   message = resources.getMessage(locale, key, args);

        if (message != null) {
            return true;
        }

        return false;
    }
}
