// Copyright 1997-2010, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscriptionmanager.intf.ILdapSearch;
import com.marimba.apps.subscriptionmanager.intf.ILdapContext;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.PackageViewForm;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;
import com.marimba.apps.subscriptionmanager.ldapsearch.PackageViewQueryBuilder;
import com.marimba.apps.subscriptionmanager.ldapsearch.LdapContext;
import com.marimba.apps.subscription.common.LDAPVars;

import com.marimba.webapps.tools.util.PropsBean;
import com.marimba.webapps.tools.util.PropsComparator;
import com.marimba.webapps.intf.IMapProperty;
import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;


/**
 * This interface is implemented by LDAPContext and LDAPContextMock. LDAPContext provides LDAPConnection object to
 * search ldap with the given query. LDAPContextMock is used for the unit testing purpose.
 *
 * @author Venkatesh Jeyaraman
 * @version 1.0, 15/08/2010
 */
public final class PackageViewAction extends AbstractAction implements IWebAppConstants {

    final static boolean DEBUG = com.marimba.apps.subscription.common.intf.IDebug.DEBUG;
    final static String STATE_BEAN = "packageViewForm";
    static PropsComparator comp = new PropsComparator("title");

//    final static String[] searchAttrs = new String[2];
//
//        searchAttrs [0] = LDAPVarsMap.get("CHANNELTITLE;
//        searchAttrs [1] = LDAPVarsMap.get("CHANNEL;
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) {
        Vector result = null;
        HttpSession session = request.getSession();
        String forward = "init";

        init(request);
        PackageViewForm pkgViewForm = (PackageViewForm) form;
        String value = request.getParameter("sortorder");

        if (null != value) {
            pkgViewForm.setValue("sortorder", value);
        }

        value = request.getParameter("sorttype");
        if (null != value) {
            pkgViewForm.setValue("sorttype", value);
        }

        value = request.getParameter("lastsort");
        if (null != value) {
            pkgViewForm.setValue("lastsort", value);
        }

        value = request.getParameter("type");
        if (null == value) {
            value = pkgViewForm.getSearchType();
        }

        if(null == value || "".equals(value = value.trim())) {
            pkgViewForm.setSearchType(value = "basic");
        }

        String actionType = request.getParameter("actionType");
        if(null == main) {
        	result = new Vector<PropsBean>();
        	forward = "failure";
        	Collections.sort(result, comp);
            session.removeAttribute(PKGLIST_BEAN);
            session.setAttribute(PAGE_PKGS_DEP_RS, result);

            return mapping.findForward(forward);
        }
        String dirType = main.getDirType();
        
        if ("sortAction".equals(actionType)) {
            result = (Vector) request.getSession().getAttribute(PAGE_PKGS_DEP_RS);
            setSortOrder((IMapProperty) form, request.getSession());
            forward = "success";
        } else if ("flipAction".equals(actionType)) {
            result = (Vector) request.getSession().getAttribute(PAGE_PKGS_DEP_RS);
            String displayType = request.getParameter("displayType");
            flipAction(displayType, pkgViewForm);
            forward = "success";
        } else if (null == actionType || "".equals(actionType) || "initAction".equals(actionType)) {
            pkgViewForm.setValue("search", "*");
            pkgViewForm.setValue("searchType", "basic");

            result = searchPackages(request, pkgViewForm);
            forward = "init";
        } else {
            String searchType = (String) (pkgViewForm.getValue("searchType"));

            if (null != searchType && !"".equals(searchType) && !"basic".equals(searchType)) {
                splitUserQuery(pkgViewForm);
            }

            result = searchPackages(request, pkgViewForm);
            forward = "success";
        }
        if(null == result) {
        	result = new Vector<PropsBean>();
        	forward = "failure";
        } 
        // set the result in session
        Collections.sort(result, comp);
        session.removeAttribute(PKGLIST_BEAN);
        session.setAttribute(PAGE_PKGS_DEP_RS, result);

        return mapping.findForward(forward);
    }

    private void flipAction(String displayType, IMapProperty form) {
        if ("url".equals(displayType)) {
            form.setValue("show_url", "true");
            form.setValue("sorttype", "url");
            form.setValue("sortorder", "true");
        } else {
            form.setValue("show_url", "false");
            form.setValue("sortorder", "true");
            form.setValue("sorttype", "title");
        }
    }

    private void splitUserQuery(IMapProperty pkgViewForm) {
        String search = (String) pkgViewForm.getValue("searchQuery");

        if (null != search && search.trim().length() > 0) {
            if (search.indexOf("&#;") != -1) {
                String[] pairs = search.split("&#;");

                for (int i = 0; i < pairs.length; i++) {
                    int index = pairs[i].indexOf('=');

                    if (index != -1) {
                        String key = pairs[i].substring(0, index);
                        String value = pairs[i].substring(index+1);
                        pkgViewForm.setValue(key, value);
                    }
                }
            }
        }
    }

    private void setSortOrder(IMapProperty statebean, HttpSession session) {

        if ((statebean == null)) {
            if (DEBUG4) {
                System.out.println(" PackageViewAction: statebean is null or empty");
            }

            PackageViewForm pkgViewForm = new PackageViewForm();
            pkgViewForm.initialize();
            statebean = pkgViewForm;
            session.setAttribute("packageViewForm", pkgViewForm);
        }

        String sorttype = (String) statebean.getValue("sorttype");

        if (sorttype == null) {
            sorttype = "title";
        }

        comp.setSortProperty(sorttype);

        boolean sortorder = true;
        String  sortorderstring = (String) statebean.getValue("sortorder");

        if (sortorderstring != null) {
            if (!"true".equals(sortorderstring)) {
                sortorder = false;
            }
        }

        comp.setSortOrder(sortorder);

        if (DEBUG4) {
            System.out.println("PackageViewAction: sorttype is " + sorttype);
            System.out.println("PackageViewAction: sortorder is  " + sortorder);
            System.out.println("PackageViewAction: form display type is url? " + statebean.getValue("show_url"));
        }
    }

    private Vector searchPackages(HttpServletRequest request, IMapProperty stateBean) {
        Vector<PropsBean> resultVec = null;
        LDAPConnection conn = null;

        setSortOrder(stateBean, request.getSession());

        try {
            String[] advancedSearchAttrs = new String[9];
            advancedSearchAttrs [0] = LDAPVarsMap.get("CHANNEL");
            advancedSearchAttrs [1] = LDAPVarsMap.get("CHANNELSEC");
            advancedSearchAttrs [2] = LDAPVarsMap.get("CHANNELINITSCHED");
            advancedSearchAttrs [3] = LDAPVarsMap.get("CHANNELSECSCHED");
            advancedSearchAttrs [4] = LDAPVarsMap.get("CHANNELUPDATESCHED");
            advancedSearchAttrs [5] = LDAPVarsMap.get("CHANNELVERREPAIRSCHED");
            advancedSearchAttrs [6] = LDAPVarsMap.get("CHANNELTITLE");
            advancedSearchAttrs [7] = LDAPVarsMap.get("CHANNELEXEMPTBLACKOUT");
            advancedSearchAttrs [8] = LDAPVarsMap.get("CHANNELWOWENABLED");

            ILdapSearch queryBuilder = new PackageViewQueryBuilder();
            queryBuilder.setCriteria(stateBean);

            conn = LDAPWebappUtils.getSubConn(request);

            ILdapContext context = new LdapContext(conn, main.getSubBase(), LDAPConstants.LDAP_SCOPE_SUBTREE, LDAPVarsMap);
            queryBuilder.setContext(context);
            queryBuilder.setAttributes(advancedSearchAttrs);

            queryBuilder.setType("package");
            queryBuilder.setUsersInLDAP(main.getUsersInLDAP());
            resultVec = queryBuilder.execute();

            if (DEBUG) {
                if (null != resultVec && resultVec.size() > 0) {

                    for (int i = 0; i < resultVec.size(); i++) {
                        PropsBean props = resultVec.elementAt(i);
                        String[] pairs = props.getPropertyPairs();

                        for (int j = 0; j < pairs.length;) {
                            System.out.println("PackageViewAction: key = " + pairs[j++] + "; value = " + pairs[j++]);
                        }
                    }
                }
            }
        } catch (Exception ne) {
            System.out.println("PackageViewAction: There is a problem with LDAP connection. Please check the server is up.");
            if (DEBUG) {
                ne.printStackTrace();
            }
        }
        return resultVec;
    }
}
