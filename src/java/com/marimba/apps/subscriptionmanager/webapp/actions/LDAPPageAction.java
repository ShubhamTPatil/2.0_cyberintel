// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;

//import java.net.URLEncoder;
import com.marimba.tools.util.URLUTF8Encoder; // Symbio added 05/19/2005

import java.util.Vector;

import javax.naming.NamingException;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscription.common.StringResourcesHelper;
import com.marimba.apps.subscription.common.util.LDAPUtils;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.system.LDAPBean;
import com.marimba.apps.subscriptionmanager.webapp.system.PagingBean;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;

import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPLocalException;
import com.marimba.tools.ldap.LDAPPagedSearch;

import com.marimba.webapps.intf.*;

/**
 * Paging through a large LDAP result set This action is called when a user selects a paging link on an ldap page: 'previous', 'next', or a selection from the
 * drop down box. (with the exception of browsing group members, which uses generic paging- genPrevNext.jsp) See LDAPBrowseTopAction for when a user requests
 * the top level of ldap (entry points). See LDAPBrowseEPAction for when a user expands an entry point. See LDAPBrowseOUAction for when a user expands a
 * container. See LDAPBrowseGroupAction for when a user expands a group (i.e. entry that is of objectclass 'groupofnames' or 'groupofuniquenames'. See
 * LDAPPageAction for when a user selects 'previous', 'next', or a set of results from the drop-down box. See LDAPSearchAction for when a user performs a
 * Search.
 *
 * @author Michele Lin
 * @version 1.21, 11/21/2002
 */
public final class LDAPPageAction
    extends AbstractAction
    implements IWebAppsConstants {
    final static boolean DEBUG = IAppConstants.DEBUG;
//    static String[]      searchAttrs = {
//                                           LDAPConstants.OBJECT_CLASS, LDAPVars.CONTAINER_PREFIX, LDAPVars.CONTAINER_PREFIX2, LDAPVars.GROUP_PREFIX,
//                                           LDAPVars.MACHINE_NAME, LDAPVars.UI_USER_ID
//    };

    /**
     * REMIND
     *
     * @param mapping REMIND
     * @param form REMIND
     * @param req REMIND
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
                                 HttpServletRequest  req,
                                 HttpServletResponse response)
        throws IOException, 
                   ServletException {
        if (DEBUG) {
            System.out.println("LDAPPageAction called");
        }
        init(req);
        HttpSession session = req.getSession();
        LDAPBean    ldapBean = getLDAPBean(req);
        PagingBean  pageBean = getPagingBean(req);

        String      inputDN = (String) req.getParameter("container");
        String      startIndexStr = (String) req.getParameter("startIndex");
        String      baseURL = ldapBean.getBaseURL();

        int         startIndex = 0;
        int         countPerPage = DEFAULT_COUNT_PER_PAGE;

        if ((startIndexStr != null) && !"".equals(startIndexStr)) {
            startIndex = Integer.parseInt(startIndexStr);
        }

        // set startIndex and countPerPage
        pageBean.setStartIndex(startIndex);
        pageBean.setCountPerPage(countPerPage);

        if (DEBUG) {
            System.out.println("inputDN= " + inputDN);
            System.out.println("baseURL= " + baseURL);
            System.out.println("startIndex= " + startIndex);
        }

        Vector         listing = null;
        java.util.List resultsList = null;

        // ldap connection and subscription base dn is needed to perform blue dot check
        ServletContext   sc = servlet.getServletConfig()
                                     .getServletContext();
        SubscriptionMain main = TenantHelper.getTenantSubMain(sc, req);
        boolean          usersInLDAP = main.getUsersInLDAP();

        LDAPConnection   conn;

        try {
            conn = LDAPWebappUtils.getBrowseConn(req);

            // Get the correct namespace for obtaining subscriptions	    
        } catch (SystemException se) {
            throw new GUIException(se);
        }

        // user is getting another page in the result set
        try {
            // get results from session bean so that search is not repeated
            LDAPPagedSearch paged = ldapBean.getPagedSearch();
            paged.setPageSize(DEFAULT_COUNT_PER_PAGE);
            resultsList = paged.getPage(startIndex);
            pageBean.setTotal(paged.getSize());
            pageBean.setHasMoreResults(paged.hasMoreResults());
            listing = LDAPWebappUtils.parseLDAPResults(req, resultsList, inputDN, main, tenant);
        } catch (NamingException ne) {
            if (DEBUG) {
                ne.printStackTrace();
            }

            try {
                LDAPUtils.classifyLDAPException(ne);
            } catch (SystemException se) {
                throw new GUIException(se);
            }
        } catch (LDAPLocalException le) {
            if (DEBUG) {
                le.printStackTrace();
            }

            InternalException ie = new InternalException(le, LDAP_BROWSE_LDAPLOCAL,
                                        StringResourcesHelper.getMessage(EXPANDING_THEENTRY_POINT));
            throw new GUIException(ie);
        } catch (SystemException se) {
            throw new GUIException(se);
        }

        // set results to be displayed in GUI to session
        session.setAttribute(DIRECTORY_TYPE, main.getDirType());
        session.setAttribute(PAGE_TARGETS_RS, listing);

        // (setBreadCrumbTrailTag) not to change the bread crumb.
        //ldapBean.setHistory(PAGE_PREFIX + "container=" + URLEncoder.encode(inputDN) + "&startIndex=" + URLEncoder.encode(startIndexStr));
        ldapBean.setHistory(PAGE_PREFIX + "container=" + URLUTF8Encoder.encode(inputDN) + "&startIndex=" + URLUTF8Encoder.encode(startIndexStr)); // Symbio modified 05/19/2005
        ldapBean.setUseLDAPPaging(true);
        ldapBean.setIsGroup(false);

        // leave bread crumb alone?  yes, because we are paging
        ldapBean.setLeaveBreadCrumb(true);

        try {
            setLDAPBean(ldapBean, req);
            setPagingBean(pageBean, req);
        } catch (SystemException se) {
            throw new GUIException(se);
        }

        return (new ActionForward(baseURL, true));
    }
}
