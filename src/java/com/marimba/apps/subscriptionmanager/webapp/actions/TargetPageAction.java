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
import javax.servlet.jsp.*;

import com.marimba.apps.subscriptionmanager.webapp.system.PagingBean;

import com.marimba.webapps.intf.GUIException;

/**
 * Paging through a large LDAP target result set on the package view page. This action is called when a user selects a paging link on the page: 'previous',
 * 'next', or a selection from the drop down box. This sets the paging bean and a session variable to indicate that the user is paging. That session variable
 * is checked by the GetTargetsFromPkgsTag to page correctly through the result set.
 *
 * @author Rahul Ravulur
 * @version 1.3, 12/15/2002
 */
public final class TargetPageAction
    extends AbstractAction {
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

        HttpSession session = req.getSession();

        //	LDAPBean ldapBean = getLDAPBean(req);
        PagingBean pageBean = getPagingBean(req);

        boolean    multi = false;

        if ("true".equals((String) req.getParameter("multi"))) {
            multi = true;
        }

        String startIndexStr = (String) req.getParameter("startIndex");

        int    startIndex = 0;
        int    countPerPage = DEFAULT_COUNT_PER_PAGE;

        if ((startIndexStr != null) && !"".equals(startIndexStr)) {
            startIndex = Integer.parseInt(startIndexStr);
        }

        // set startIndex and countPerPage
        pageBean.setStartIndex(startIndex);

        // indicate that this is a paging request
        session.setAttribute(TARGET_PAGING, "true");

        if (DEBUG) {
            System.out.println("startIndex= " + startIndex);
        }

        //  	try {
        //  	    // get results from session bean so that search is not repeated
        //  	    LDAPPagedSearch paged = ldapBean.getPagedSearch();
        //  	    paged.setPageSize(DEFAULT_COUNT_PER_PAGE);
        //  	    pageBean.setTotal(paged.getSize());		
        //  	} catch (NamingException ne) {
        //  	    ne.printStackTrace();
        //  	    throw new GUIException(ne);
        //  	} catch (LDAPLocalException le) {
        //  	    le.printStackTrace();
        //  	    throw new GUIException(le);
        //  	}
        try {
            //	    setLDAPBean(ldapBean, req);
            setPagingBean(pageBean, req);
        } catch (Exception e) {
            // Remind mlin: need to add text to this exception
            throw new GUIException(e);
        }

        if (!multi) {
            return (mapping.findForward("success"));
        } else {
            return (mapping.findForward("msuccess"));
        }
    }
}
