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

import java.util.Map;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.system.LDAPBean;
import com.marimba.apps.subscriptionmanager.webapp.system.PagingBean;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.SystemException;

/**
 * Browsing an LDAP group (listing the members of a group object) This action is called when a user expands a group when sourcing users from the transmitter
 * See LDAPBrowseTopAction for when a user requests the top level of ldap (entry points). See LDAPBrowseEPAction for when a user expands an entry point. See
 * LDAPBrowseOUAction for when a user expands a container. See LDAPPageAction for when a user selects 'previous', 'next', or a set of results from the
 * drop-down box. See LDAPSearchAction for when a user performs a Search.
 *
 * @author Angela Saval
 * @version 1.3, 01/26/2003
 */
public final class TXBrowseGroupAction
    extends AbstractAction
    implements IWebAppsConstants,
                   IErrorConstants,
                   IWebAppConstants {
    final static boolean DEBUG = IAppConstants.DEBUG;

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
            System.out.println("tXBrowseGroupAction called");
        }

        HttpSession session = req.getSession();
        LDAPBean    ldapBean = getLDAPBean(req);
        PagingBean  pageBean = getPagingBean(req);

        String      groupname = (String) req.getParameter("name");
        String      baseURL = ldapBean.getBaseURL();
        String      epType = ldapBean.getEntryPoint();

        //This is used for the generic paging of the group elements
        String page = (String) req.getParameter("page");

        // set the history info to ldapBean
        // this stores the info needed to reconstruct the current location being browsed
        //ldapBean.setHistory(GROUP_PREFIX + "member=" + member + "&inputDN="
        //+ URLEncoder.encode(inputDN) + "&levels=" + levels);
        if (page != null) {
            if (DEBUG) {
                System.out.println("page= " + page);
                System.out.println("baseURL= " + baseURL);
            }

            return (new ActionForward(baseURL + "?page=" + page, true));
        }

        //is Group is set to indicate that the group members are to be listed.  This is used
        //by the jsp pages and LDAPSearchAction
        ldapBean.setUseLDAPPaging(false);
        ldapBean.setIsGroup(true);
        ldapBean.setGroup(groupname);

        if (DEBUG) {
            System.out.println("TxbrowseGroupaction: group name = " + groupname);
        }

        ldapBean.setHistory(GROUP_PREFIX + "name=" + groupname);

        ServletContext   sc = servlet.getServletConfig()
                                     .getServletContext();
        SubscriptionMain main = TenantHelper.getTenantSubMain(sc, req);
        Map<String, String> LDAPVarsMap = main.getLDAPVarsMap();
        String           subBase = main.getSubBase();

        try {
            subBase = LDAPWebappUtils.getSubBaseWithNamespace(req, main);
        } catch (SystemException se) {
            throw new GUIException(se);
        }

        Vector listing = null;

        try {
            listing = LDAPWebappUtils.setUsersOrGroupsListTx(USER_TXGROUP, groupname, session, main, req, "*", LDAPVarsMap);

            session.setAttribute(PAGE_GEN_RS, listing);

            // for generic paging, clear out the paging bean to indicate new results		
            session.removeAttribute(GEN_PAGE_BEAN);
            ldapBean.setLeaveBreadCrumb(false);
            pageBean.setStartIndex(0);
            pageBean.setCountPerPage(listing.size());
            pageBean.setTotal(listing.size());
        } catch (SystemException se) {
            throw new GUIException(se);
        }

        return (new ActionForward(baseURL, true));
    }
}
