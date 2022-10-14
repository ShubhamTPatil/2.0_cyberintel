// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.webapp.intf.GUIConstants;
import com.marimba.apps.subscriptionmanager.webapp.system.LDAPBean;
import com.marimba.apps.subscriptionmanager.webapp.system.PagingBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPPolicyHelper;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;
import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.tools.ldap.LDAPName;
import com.marimba.tools.util.URLUTF8Encoder;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.tools.util.PropsBean;
import com.marimba.webapps.tools.util.WebAppUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Vector;

/**
 * Displaying the top level of ldap navigation This action is called when a user first views the ldap navigation pane- either on the Target View page or the
 * Add/Remove Targets page. See LDAPBrowseOUAction for when a user expands a container. See LDAPBrowseGroupAction for when a user expands a group (i.e. entry
 * that is of objectclass 'groupofnames' or 'groupofuniquenames'. See LDAPPageAction for when a user selects 'previous', 'next', or a set of results from the
 * drop-down box. See LDAPSearchAction for when a user performs a Search.
 *
 * @author Michele Lin
 * @version 1.24, 11/21/2002
 */

public final class LDAPBrowseTopAction extends AbstractAction implements IWebAppsConstants {

    private String allEndpoints;

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
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest req, HttpServletResponse response) throws IOException, ServletException {

        //Adding to load resources
        init(req);
        String[] searchAttrs = {
                LDAPConstants.OBJECT_CLASS, LDAPVarsMap.get("CONTAINER_PREFIX"), LDAPVarsMap.get("CONTAINER_PREFIX2"), LDAPVarsMap.get("GROUP_PREFIX"),
                LDAPVarsMap.get("MACHINE_NAME"), LDAPConstants.DOMAIN_PREFIX, LDAPVarsMap.get("UI_USER_ID"), LDAPVarsMap.get("DESCRIPTION"), LDAPVarsMap.get("GROUP_CREATE_TYPE")
        };
        if (DEBUG) {
            System.out.println(" LDAPBrowseTopAction:: called");
        }
        Locale locale = req.getLocale();
        HttpSession session = req.getSession();
        LDAPBean ldapBean = getLDAPBean(req);
        PagingBean pageBean = getPagingBean(req);
        String baseURL = ldapBean.getBaseURL();

        if (DEBUG4) {
            System.out.println(" LDAPBrowseTopAction:: baseURL= " + baseURL);
        }

        // set startIndex and countPerPage
        int startIndex = 0;
        int countPerPage = DEFAULT_COUNT_PER_PAGE;
        pageBean.setStartIndex(startIndex);
        pageBean.setCountPerPage(countPerPage);

        // get ldap entry points from SubscriptionMain
        ServletContext sc = servlet.getServletConfig().getServletContext();
        init(req);
        // if false uses come from the transmitter
        boolean usersInLDAP = main.getUsersInLDAP();

        // the top level of ldap navigation consists of the
        // pre-defined addresses of ldap entry points.
        // Thus, no query is needed here.
        Vector<PropsBean> listing;

        LDAPConnection browseConn;
        LDAPConnection subConn;

        try {
        	main.initLDAP();
            browseConn = LDAPWebappUtils.getBrowseConn(req);
            subConn = LDAPWebappUtils.getSubConn(req);

            // Get the correct namespace for obtaining subscriptions
            listing = getTopLevel(req, main, searchAttrs);
        } catch (NamingException ne) {
            throw new GUIException(ne);
        } catch (SystemException se) {
            throw new GUIException(se);
        }

        // reset java bean that keeps track of which entry points we're in
        ldapBean.clearEntryPoint();
        ldapBean.clearCurrentDomain();
        ldapBean.setContainer("top");
        ldapBean.setHistory("top");
        ldapBean.setUseLDAPPaging(true);
        ldapBean.setIsGroup(false);

        // leave bread crumb alone?
        //  no, leave alone only when expanding groups and paging
        ldapBean.setLeaveBreadCrumb(false);
        ldapBean.setUsersInLDAP("" + usersInLDAP);

        // for paging
        pageBean.setTotal(listing.size());
        pageBean.setHasMoreResults(false);


        /* Try to set the transmitter host name.  The hostname can only be
        * accessed if the plugin has been published.  This is because it is the
        * plugin transmitter that is used for listing the users and the user groups
        */
        try {
            String ctx = (String) GUIUtils.getFromSession(req, "context");

            if (!main.getUsersInLDAP() && !( "aclDisplayTarget".equals(ctx) || "aclAdd".equals(ctx) )) {

                int size = listing.size() - 1;
                String name = WebAppUtils.getMessage(main.getAppResources(), locale, "page.global.People");

                PropsBean entry = new PropsBean();
                entry.setValue(LDAPConstants.OBJECT_CLASS, PEOPLE_EP);
                entry.setValue(GUIConstants.DISPLAYNAME, name);
                LDAPWebappUtils.setExpandAbleProperty(entry, LDAPVarsMap);
                entry.setValue("type", "tx");
                listing.add(size++, entry);

                name = WebAppUtils.getMessage(main.getAppResources(), locale, "page.global.Groups");
                entry = new PropsBean();
                entry.setValue(LDAPConstants.OBJECT_CLASS, GROUPS_EP);
                entry.setValue(GUIConstants.DISPLAYNAME, name);
                LDAPWebappUtils.setExpandAbleProperty(entry, LDAPVarsMap);
                entry.setValue("type", "tx");
                listing.add(size, entry);
            }
        } catch (SystemException se) {
            throw new GUIException(se);
        }

// Add Site informations in ldap_nav.jsp page for Site Based Policy Deployment
        if (main.isSiteBasedPolicyEnabled()) {
            PropsBean entry = new PropsBean();
            entry.setValue(LDAPConstants.OBJECT_CLASS, TYPE_SITE);
            String name = WebAppUtils.getMessage(main.getAppResources(), locale, "page.global.Sites");
            entry.setValue(GUIConstants.DISPLAYNAME, name);
            LDAPWebappUtils.setExpandAbleProperty(entry, LDAPVarsMap);
            entry.setValue("type", "network");

            listing.add(listing.size() - 1, entry);
        }

        // remove any group member listings first
        session.removeAttribute(PAGE_GEN_RS);

        // set results to session
        session.setAttribute(DIRECTORY_TYPE, main.getDirType());
        session.setAttribute(PAGE_TARGETS_RS, listing);

        try {
            setLDAPBean(ldapBean, req);
            setPagingBean(pageBean, req);
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
//        Enumeration<PropsBean> e = listing.elements();
        return (new ActionForward(baseURL, true));
    }

    /**
     * REMIND
     *
     * @param req REMIND
     * @param main REMIND
     *
     * @return REMIND
     *
     */
    public Vector<PropsBean> getTopLevel(HttpServletRequest req, SubscriptionMain main, String[] searchAttrs)
            throws NamingException, SystemException {

        String subBase = main.getSubBase();
        LDAPConnection dcConn = LDAPWebappUtils.getCollConn(req);
        LDAPConnection browseConn = LDAPWebappUtils.getBrowseConn(req);

        Vector domains = main.getLDAPEnv().getTargetHome(dcConn);
        allEndpoints = resources.getMessage(req.getLocale(), "page.global.All");
        //REMIND Move this code into LDAPConnUtils
        //it is being done here only because the other classes
        // are not compile in JAVA2
        String[] domainArray = new String[domains.size()];

        LDAPName ldapName = browseConn.getParser();
        Name[] hiddenNames = main.getLDAPEnv().getHiddenEntries(browseConn);
        Name dnName;
        int numOfDomains = 0;
        for (int i = 0; i < domainArray.length; i++) {
            boolean isHiddenEntry = false;
            String dn = domains.elementAt(i).toString();
            dnName = ldapName.parse(dn);

            for (Name hiddenName : hiddenNames) {
                if (dnName.equals(hiddenName)) {
                    isHiddenEntry = true;
                    numOfDomains++;
                }
            }
            if (!isHiddenEntry) {
                domainArray[i] = dn;
            }
        }

        Arrays.sort(domainArray);

        PropsBean entry;
        Vector<PropsBean> results = new Vector<PropsBean>();
        LDAPPolicyHelper policyFinder = new LDAPPolicyHelper(LDAPWebappUtils.getSubConn(req), subBase, main.getUsersInLDAP(), main.getDirType());
        // get ldap entry points from SubscriptionMain
        for (int i = 0; i < domainArray.length && numOfDomains < domains.size(); i++) {
            String dn = domainArray[i];
            if (dn != null) {
                Attributes domainAttr = browseConn.getAttributes(dn, searchAttrs);
                entry = parseResults(req, dn, domainAttr);
                if (entry != null) {
                    policyFinder.addTarget((String) entry.getValue(LDAPVarsMap.get("DN")), (String) entry.getValue(LDAPConstants.OBJECT_CLASS));
                    results.add(entry);
                }
            }
        }

        entry = new PropsBean();
        entry.setValue(LDAPConstants.OBJECT_CLASS, TYPE_ALL);
        entry.setValue("type", TARGET_ALL);
        entry.setValue(GUIConstants.DISPLAYNAME, allEndpoints);
        entry.setValue(LDAPVarsMap.get("DN"), TYPE_ALL);
        policyFinder.addTarget(TYPE_ALL, TYPE_ALL);
        LDAPWebappUtils.setTargetAbleProperty(entry, false, LDAPVarsMap.get("USER_CLASS"));
        results.addElement(entry);
        if (DEBUG3) {
            System.out.println(" LDAPBrowseTopAction:: Entries returned by top: ");

            for (int i = 0; i < results.size(); i++) {
                LDAPWebappUtils.dumpProperties(results.elementAt(i), LDAPConstants.DOMAIN_CLASS + i);
            }
        }
        LDAPWebappUtils.setDirectTargetProperty(results, policyFinder, LDAPVarsMap);
        return results;
    }

    /**
     * REMIND
     *
     * @param req REMIND
     * @param dn REMIND
     * @param srAttrs REMIND
     *
     * @return REMIND
     *
     * @throws SystemException REMIND
     * @throws NamingException REMIND
     */
    public PropsBean parseResults(HttpServletRequest req, String dn, Attributes srAttrs)
            throws SystemException, NamingException {
        //no attributes - we dont have read access
        if (srAttrs == null) {
            return null;
        }

        PropsBean entry = new PropsBean();
        LDAPConnection browseConn = LDAPWebappUtils.getBrowseConn(req);

        if (LDAPConstants.VENDOR_AD.equals(LDAPVarsMap.get("DIRECTORY_TYPE"))) {
            entry.setValue(LDAPVarsMap.get("DN"), dn);
            entry.setValue(LDAPConstants.OBJECT_CLASS, LDAPConstants.DOMAIN_CLASS);
            entry.setValue("type", LDAPConstants.DOMAIN_CLASS);
            entry.setValue(GUIConstants.DISPLAYNAME, LDAPWebappUtils.dn2DNSName(browseConn, dn, false));
            LDAPWebappUtils.setTargetAbleProperty(entry, false, LDAPVarsMap.get("USER_CLASS"));
            LDAPWebappUtils.setExpandAbleProperty(entry, LDAPVarsMap);
        } else {
            LDAPName ldapName = browseConn.getParser();
            Name dnName = ldapName.parse(dn);

            if (dnName.size() > 0) {
                String name = ldapName.getComponentValue(dnName, dnName.size() - 1);
                entry.setValue(LDAPVarsMap.get("DN"), dn);
                entry.setValue(GUIConstants.DISPLAYNAME, name);
                entry.setValue("type", LDAPConstants.DOMAIN_CLASS);
                entry.setValue(LDAPConstants.OBJECT_CLASS, LDAPConstants.DOMAIN_CLASS);
                LDAPWebappUtils.setTargetAbleProperty(entry, false, LDAPVarsMap.get("USER_CLASS"));
                LDAPWebappUtils.setExpandAbleProperty(entry, LDAPVarsMap);
            }
        }

        return entry;
    }
}
