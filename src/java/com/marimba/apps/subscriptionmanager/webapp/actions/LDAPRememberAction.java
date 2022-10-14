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

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.system.LDAPBean;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;

/**
 * This action remembers where the user was browsing in LDAP. This action called when the Target View tab is selected or when the user visits the Add/Remove
 * Targets page.
 *
 * @author Michele Lin
 * @version 1.4, 08/20/2002
 */
public final class LDAPRememberAction
    extends AbstractAction
    implements IWebAppConstants {
    public static boolean DEBUG = com.marimba.apps.subscriptionmanager.intf.IAppConstants.DEBUG;

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
        String           selectedTab = req.getParameter("selectedTab");
        LDAPBean         ldapBean = getLDAPBean(req);
        HttpSession      session = req.getSession();
        ServletContext   sc = servlet.getServletConfig()
                                     .getServletContext();
        SubscriptionMain main = TenantHelper.getTenantSubMain(sc, req);

        // figure out which of the two ldap pages we're referring to
        // either the Target View page, or the Add/Remove Targets page
        String baseURL = "";

        if ("true".equals(selectedTab)) {
            baseURL = "/main_ldap_nav.jsp";
        } else {
            baseURL = "/distribution/add_remove_ldap_nav.jsp";
        }

        // set the baseURL to the session so that each action
        // knows where to redirect to
        ldapBean.setBaseURL(baseURL);

        // this is the last stored location we were visiting
        // we will use this to construct a full action path
        // for example, /sm/ldapBrowseTop.do?baseURL=/main_ldap_nav.jsp
        String history = ldapBean.getHistory();

        if (DEBUG) {
            System.out.println("history= " + history);
        }

        // set the default forward url to the top level
        String forwardURL = "/ldapBrowseTop.do";

        if (history != null) {
            if ("top".equals(history)) {
                // we last visited the top level that consists of entry points
                // use default
            } else if (history.startsWith(ENTRYPOINT_PREFIX)) {
                forwardURL = "/ldapBrowseEP.do?" + history.substring(ENTRYPOINT_PREFIX.length());
            } else if (history.startsWith(OU_PREFIX)) {
                forwardURL = "/ldapBrowseOU.do?" + history.substring(OU_PREFIX.length());
            } else if (history.startsWith(GROUP_PREFIX)) {
                forwardURL = "/ldapBrowseGroup.do?" + history.substring(GROUP_PREFIX.length());

                //if the group is a transmitter group, then we need to set the history
                //appropriately
                if (!main.getUsersInLDAP()) {
                    forwardURL = "/txBrowseGroup.do?" + history.substring(GROUP_PREFIX.length());
                }
            } else if (history.startsWith(PAGE_PREFIX)) {
                forwardURL = "/ldapPage.do?" + history.substring(PAGE_PREFIX.length());
            }
        }

        if (DEBUG) {
            System.out.println("forwardURL= " + forwardURL);
        }

        try {
            setLDAPBean(ldapBean, req);
        } catch (SystemException se) {
            throw new GUIException(se);
        }
        if(req.getParameter("isTargetView")!=null){
        	session.setAttribute("isTargetView","false");	
        }else{
        	session.removeAttribute("isTargetView");	
        }
        return (new ActionForward(forwardURL, true));
    }
}
