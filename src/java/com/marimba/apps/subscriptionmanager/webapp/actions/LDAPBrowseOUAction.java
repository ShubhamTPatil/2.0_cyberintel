// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.StringResourcesHelper;
import com.marimba.apps.subscription.common.intf.SubInternalException;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.system.LDAPBean;
import com.marimba.apps.subscriptionmanager.webapp.system.PagingBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;
import com.marimba.tools.ldap.*;
import com.marimba.tools.util.URLUTF8Encoder;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.InternalException;
import com.marimba.webapps.intf.SystemException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Vector;

/**
 * Expanding a container in LDAP
 *
 * This action is called when a user expands an organizational unit or selects
 * a container link in the bread crumb.
 *
 * See LDAPBrowseTopAction for when a user requests the top level of ldap (entry points).
 * See LDAPBrowseEPAction for when a user expands an entry point.
 * See LDAPBrowseGroupAction for when a user expands a group (i.e. entry that is
 * of objectclass 'groupofnames' or 'groupofuniquenames'.
 * See LDAPPageAction for when a user selects 'previous', 'next', or
 * a set of results from the drop-down box.
 * See LDAPSearchAction for when a user performs a Search.
 *
 * @author      Michele Lin
 * @version     1.36, 11/21/2002
 */

public final class LDAPBrowseOUAction extends AbstractAction implements IWebAppsConstants, IWebAppConstants, ISubscriptionConstants {
//    final static boolean DEBUG = IAppConstants.DEBUG;

    protected Task createTask(ActionMapping mapping, ActionForm form,
                              HttpServletRequest request, HttpServletResponse response) {
        return new InitTask(mapping, form, request, response);
    }

    protected class InitTask extends SubscriptionDelayedTask {

        InitTask(ActionMapping mapping, ActionForm form,
                 HttpServletRequest request, HttpServletResponse response) {
            super(mapping, form, request, response);
        }

        public LDAPBean getLDAPBean(HttpServletRequest request) {
            HttpSession session = request.getSession();
            LDAPBean ldapBean = (LDAPBean) session.getAttribute(SESSION_LDAP);
            if (null == ldapBean) {
                ldapBean = new LDAPBean();
            }
            return ldapBean;
        }

        public void setLDAPBean(LDAPBean ldapBean, HttpServletRequest request) throws SubInternalException {
            if (null == ldapBean) {
                throw new SubInternalException(SYSTEM_INTERNAL_STATE_NULL, "ldapBean");
            }
            HttpSession session = request.getSession();
            session.setAttribute(SESSION_LDAP, ldapBean);
        }

        public PagingBean getPagingBean(HttpServletRequest request) {
            HttpSession session = request.getSession();
            PagingBean pagingBean = (PagingBean) session.getAttribute(SESSION_PAGE);
            if (null == pagingBean) {
                pagingBean = new PagingBean();
            }
            return pagingBean;
        }

        public void setPagingBean(PagingBean pagingBean, HttpServletRequest req) throws SubInternalException {
            if (null == pagingBean) {
                throw new SubInternalException(SYSTEM_INTERNAL_STATE_NULL, "pagingBean");
            }
            HttpSession session = req.getSession();
            session.setAttribute(SESSION_PAGE, pagingBean);
        }

        public void execute() {

            if (DEBUG2) {
                System.out.println("In LDAPBrowseOUAction.InitTask.execute");
            }

            HttpSession session = request.getSession();
            LDAPBean ldapBean = getLDAPBean(request);
            PagingBean pageBean = getPagingBean(request);
            String inputDN = (String) request.getParameter("inputDN");
            //only set when sourcing from transmitters
            String id = (String) request.getParameter("id");
            String epType = (String) request.getParameter("epType");
            String objectClass = (String) request.getParameter("objectClass");
            String baseURL = ldapBean.getBaseURL();
            String searchFilter = null;
            boolean isLDAPCollection = false;
            if (DEBUG4) {
                System.out.println("inputDN= " + inputDN);
                System.out.println("baseURL= " + baseURL);
                System.out.println("ObjectClass = " + objectClass);
                System.out.println("Current Domain = " + ldapBean.getCurrentDomain());
            }

            try {
                // set startIndex and countPerPage
                int startIndex = 0;
                int countPerPage = DEFAULT_COUNT_PER_PAGE;
                pageBean.setStartIndex(startIndex);
                pageBean.setCountPerPage(countPerPage);

                ServletContext sc = servlet.getServletConfig().getServletContext();
                init(request);

                // search for the entries one level below the selected container

                boolean usersInLDAP = main.getUsersInLDAP();
                if (!usersInLDAP && (PEOPLE_EP.equals(epType) || GROUPS_EP.equals(epType))) {

                    //Set to session the listing for the users or groups from the transmitter
                    try {
                        Vector listing = LDAPWebappUtils.setUsersOrGroupsListTx(epType, null, session, main, request, "*", LDAPVarsMap);
                        //This is set so that we can page through a set of results not using ldap paging.
                        ldapBean.setUseLDAPPaging(false);
                        ldapBean.setIsGroup(false);
                        ldapBean.setEntryPoint(epType);
                        ldapBean.setContainer(epType);
                        ldapBean.setObjectClass(epType);

                        session.setAttribute(PAGE_GEN_RS, listing);
                        // for generic paging, clear out the paging bean to indicate new results
                        session.removeAttribute(GEN_PAGE_BEAN);
                        //display all of the results on the page. Paging through the result set needs
                        //to be implemented
                        pageBean.setStartIndex(0);
                        pageBean.setCountPerPage(listing.size());
                        pageBean.setTotal(listing.size());

                    } catch (SystemException se) {
                        throw new GUIException(se);
                    }

                } else if (SITES_EP.equalsIgnoreCase(epType)) { // Adding sites listing here
                    try {
                        Vector listing = LDAPWebappUtils.setUsersOrGroupsListTx(epType, null, session, main, request, "*", LDAPVarsMap);
                        //This is set so that we can page through a set of results not using ldap paging.
                        ldapBean.setUseLDAPPaging(false);
                        ldapBean.setIsGroup(false);
                        ldapBean.setEntryPoint(epType);
                        ldapBean.setContainer(epType);
                        ldapBean.setObjectClass(epType);

                        session.setAttribute(PAGE_GEN_RS, listing);
                        // for generic paging, clear out the paging bean to indicate new results
                        session.removeAttribute(GEN_PAGE_BEAN);
                        //display all of the results on the page. Paging through the result set needs
                        //to be implemented
                        pageBean.setStartIndex(0);
                        pageBean.setCountPerPage(listing.size());
                        pageBean.setTotal(listing.size());

                    } catch (SystemException se) {
                        throw new GUIException(se);
                    }
                }  else {
                    // get LDAP connection
                    LDAPConnection conn;
                    // subscription base dn is needed for blue dot check below
                    LDAPName ldapName;
                    try {
                        ldapBean.setEntryPoint("ldap");
                        conn = LDAPWebappUtils.getBrowseConn(request);
                        if (LDAPConstants.DOMAIN_CLASS.equals(objectClass)) {
                            String temp = LDAPWebappUtils.dn2DNSName(conn, inputDN, false);
                            ldapBean.setCurrentDomain(temp);
                        }
                        ldapName = conn.getParser();
                        Name dnName = ldapName.parse(inputDN);
                        ldapBean.setSearchText(ldapName.getComponentValue(dnName, dnName.size() - 1));
                        boolean isCollection = isCollection(conn, inputDN, main.getCollBase());

                        String ldapCollBase = main.getLDAPCollBase();
                        //Remind: if the ldap query collection base is set by default, then the condition could be removed
                        if((ldapCollBase != null)) {
                            isLDAPCollection = isCollection(conn, inputDN, ldapCollBase);
                        }

                        boolean isCN = false;
                        if(isLDAPCollection) {
                            searchFilter = getTargetableClasses(isLDAPCollection, main, main.getLDAPEnv().getDCConn(inputDN, GUIUtils.getUser(request)), getCollectionPrefixCN(main.getLDAPCollBase()));
                        }else if(isCollection) {
                            searchFilter = getTargetableClasses(isCollection, main, main.getLDAPEnv().getDCConn(inputDN, GUIUtils.getUser(request)), getCollectionPrefixCN(main.getCollBase()));
                        }else {
                            searchFilter = getTargetableClasses(isCollection, main, main.getLDAPEnv().getDCConn(inputDN, GUIUtils.getUser(request)), "");
                        }

                        if (isCollection || isLDAPCollection) {
                            conn = main.getLDAPEnv().getDCConn(inputDN, GUIUtils.getUser(request));
                        }

                        if (DEBUG3) {
                            System.out.println("searchFilter= " + searchFilter);
                        }
                        String searchAttrs[] = {
                                LDAPConstants.OBJECT_CLASS,
                                LDAPVarsMap.get("CONTAINER_PREFIX"),
                                LDAPVarsMap.get("CONTAINER_PREFIX2"),
                                LDAPVarsMap.get("GROUP_PREFIX"),
                                LDAPVarsMap.get("MACHINE_NAME"),
                                LDAPConstants.DOMAIN_PREFIX,
                                LDAPVarsMap.get("USER_ID"),
                                LDAPVarsMap.get("DESCRIPTION"),
                                LDAPVarsMap.get("GROUP_CREATE_TYPE")
                        };
                        // get full result set and set to session
                        String[] prefixOrder = LDAPUtils.getLDAPVarArr("PREFIX_ORDER_PROP", LDAPVarsMap.get("DIRECTORY_TYPE"));
                        LDAPPagedSearch paged = LDAPPagedSearch.getLDAPPagedSearch(conn, searchFilter, (String[]) searchAttrs, inputDN, true, prefixOrder);

                        paged.setPageSize(DEFAULT_COUNT_PER_PAGE);

                        // set the LDAPPagedSearch object to the ldapBean for storage
                        ldapBean.setPagedSearch(paged);

                        // get sub-set of results for GUI display
                        java.util.List resultsList = paged.getPage(startIndex);
                        // set number of total results for paging
                        if (DEBUG3) {
                            System.out.println("pages.getSize()= " + paged.getSize());
                        }
                        pageBean.setTotal(paged.getSize());
                        pageBean.setHasMoreResults(paged.hasMoreResults());

                        // parse query result into name, objectclass, dn
                        Vector listing = LDAPWebappUtils.parseLDAPResults(request, resultsList, inputDN, main,tenant);

                        // remove any group member listings first.
                        // This is used for generic paging, and now
                        // ldap paginig is to be used.
                        session.removeAttribute(PAGE_GEN_RS);
                        // set results to session
                        session.setAttribute(DIRECTORY_TYPE, main.getDirType());
                        session.setAttribute(PAGE_TARGETS_RS, listing);
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
                        InternalException ie =
                                new InternalException(le, LDAP_BROWSE_LDAPLOCAL, StringResourcesHelper.getMessage(EXPAND_CONTAINER));
                        throw new GUIException(ie);
                    } catch (SystemException se) {
                        throw new GUIException(se);
                    }

                    // set dn for bread crumb
                    ldapBean.setContainer(inputDN);
                    //ldapBean.setHistory(OU_PREFIX + "inputDN=" + URLEncoder.encode(inputDN));
                    ldapBean.setHistory(OU_PREFIX + "inputDN=" + URLUTF8Encoder.encode(inputDN)); // Symbio modified 05/19/2005
                    ldapBean.setObjectClass(objectClass);
                    ldapBean.setUseLDAPPaging(true);
                    ldapBean.setIsGroup(false);
                    // leave bread crumb alone?
                    // no, only leave alone when  expanding groups and paging
                    ldapBean.setLeaveBreadCrumb(false);
                }
                try {
                    setLDAPBean(ldapBean, request);
                    setPagingBean(pageBean, request);
                } catch (SystemException se) {
                    throw new GUIException(se);
                }
                if(null != session.getAttribute("policystartlocation") && null!= session.getAttribute("policyfirsttime")) {
                	session.setAttribute("policyfirsttime", null);
                	String loadingLocation = (String) session.getAttribute("policystartlocation");
                	baseURL = "/ldapBrowseOU.do?inputDN=" + URLUTF8Encoder.encode(loadingLocation) + "&objectClass=container";
                }
                if(null != session.getAttribute("securitystartlocation") && null!= session.getAttribute("securityfirsttime")) {
                	session.setAttribute("securityfirsttime", null);
                	String loadingLocation = (String) session.getAttribute("securitystartlocation");
                	String forwardURL = null;
                	if("all".equals(loadingLocation)) {
                		forwardURL = "/main_ldap_nav.jsp";
                		baseURL = "/ldapBrowseTop.do?baseURL=" + URLUTF8Encoder.encode(forwardURL);
                	} else {
                		baseURL = "/ldapBrowseOU.do?inputDN=" + URLUTF8Encoder.encode(loadingLocation) + "&objectClass=container";
                	}
                }
                forward = new ActionForward(baseURL, true);
            } catch (GUIException e) {
                guiException = e;
            }
        }

        private String getCollectionPrefixCN(String base) {
            if (base == null) {
                return null;
            }

            return base.substring(0,base.indexOf("="));
        }

        public boolean isCollection(LDAPConnection conn, String dn, String collBase) throws SystemException {
            LDAPName ldapName     =  null;
            boolean  isCollection = false;
            String   collDN       = collBase;
            String   dn2          = dn;
            try {
                ldapName = conn.getParser();
                String collBaseDomain = LDAPConnUtils.getDomainDNFromDN(conn, collBase);
                String dnDomain       = LDAPConnUtils.getDomainDNFromDN(conn, dn);

                if (!dn.equals(dnDomain)) {

                    // To support distributed mode strip off basedns and compare names
                    if(collBaseDomain != null && collBaseDomain.length() > 0) {
                        int index = collBase.indexOf(collBaseDomain);
                        if (index != -1) {
                            collDN  = collBase.substring(0, index-1);
                        }
                    }

                    if(dnDomain != null && dnDomain.length() > 0) {
                        int index = dn.indexOf(dnDomain);
                        if (index != -1) {
                            dn2  = dn.substring(0, index-1);
                        }
                    }

                    Name dnName   = ldapName.parse(dn2);
                    Name collName = ldapName.parse(collDN);
                    isCollection = dnName.startsWith(collName);
                }
            } catch (NamingException ne) {
                if (DEBUG) {
                    ne.printStackTrace();
                }
                LDAPUtils.classifyLDAPException(ne);
            }
            return isCollection;
        }

        private String getTargetableClasses(boolean isCollection, SubscriptionMain main, LDAPConnection conn, String collcontainerPrefix) {
            if (isCollection) {
                String[] targets = new String[]{LDAPVarsMap.get("COLLECTION_CLASS")};
                return LDAPWebappUtils.constructSearchFilter(targets, collcontainerPrefix, LDAPVarsMap);
            } else {
                return main.getLDAPEnv().getBrowseFilter(conn);
            }
        }

    }
}
