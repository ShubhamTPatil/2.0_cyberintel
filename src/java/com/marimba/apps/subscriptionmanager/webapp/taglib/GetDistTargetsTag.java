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
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.util.LDAPUtils;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.intf.*;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;

import com.marimba.tools.ldap.*;

import com.marimba.tools.util.*;

import com.marimba.webapps.tools.util.PropsBean;

/**
 * Custom tag loads the selected targets list from the distribution bean and sets the add_remove_selected_page_targets session variable.  This tag is called
 * when loading the select_exclude.jsp file which is the right pane of the Add/Remove Targets page.
 *
 * @author Michele Lin
 * @author Damodar Hegde
 * @version 1.4, 01/14/2002
 */
public class GetDistTargetsTag
    extends TagSupport
    implements IWebAppConstants {
    final static boolean DEBUG = com.marimba.apps.subscription.common.intf.IDebug.DEBUG;

    String               init = "";

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getInit() {
        return this.init;
    }

    /**
     * REMIND
     *
     * @param init REMIND
     */
    public void setInit(String init) {
        this.init = init;
        ;
    }

    /**
     * Copies any selected targets into the temp variables used by the select_exclude.jsp page.  This is necessary to populate the selected box with any
     * targets previously chosen.
     *
     * @return REMIND
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doStartTag()
        throws JspException {
        HttpSession      session = pageContext.getSession();
        DistributionBean distBean = (DistributionBean) session.getAttribute(SESSION_DIST);

        // only initialize the selected targets if the
        // distribution bean exists and if this is the first time visiting this page
        //   init is a parameter passed into the tag and
        //   is set on the add_remove_targets.jsp
        if ((distBean != null) && "true".equals(init)) {
            // get the targets lists from the distribution bean in the session
            ArrayList distSelected = distBean.getTargets();

            // set them to the session
            session.setAttribute(ADD_REMOVE_SELECTED_PAGE_TARGETS, distSelected.clone());
        }

        return (EVAL_BODY_INCLUDE);
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
