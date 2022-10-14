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
import java.util.Locale;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.system.LDAPBean;
import com.marimba.apps.subscriptionmanager.webapp.system.PagingBean;
import com.marimba.apps.subscriptionmanager.webapp.util.Crumb;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;
import com.marimba.apps.subscriptionmanager.webapp.intf.GUIConstants;
import com.marimba.intf.util.IConfig;
import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPName;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.SystemException;

import com.marimba.webapps.tools.util.PropsBean;
import com.marimba.webapps.tools.util.WebAppUtils;

/**
 * Browsing an LDAP group (listing the members of a group object) This action is called when a user expands a group. See LDAPBrowseTopAction for when a user
 * requests the top level of ldap (entry points). See LDAPBrowseEPAction for when a user expands an entry point. See LDAPBrowseOUAction for when a user
 * expands a container. See LDAPPageAction for when a user selects 'previous', 'next', or a set of results from the drop-down box. See LDAPSearchAction for
 * when a user performs a Search.
 *
 * @author Michele Lin
 * @version 1.42, 01/06/2003
 */
public final class LDAPBrowseGroupAction
    extends AbstractAction
    implements IWebAppsConstants,
                   IErrorConstants,
                   IWebAppConstants {
    final static boolean DEBUG = IAppConstants.DEBUG;
    final static int     AD_ATTR_LIMIT = 1000;
    String dirType;

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
            System.out.println("LDAPBrowseGroupAction called");
        }
        init(req);
        HttpSession session = req.getSession();
        LDAPBean    ldapBean = getLDAPBean(req);
        PagingBean  pageBean = getPagingBean(req);

        String      inputDN = (String) req.getParameter("inputDN");

        //need to store the current input dn because collection will
        //skip the first result set
        String collDN = inputDN;
        String objectClass = (String) req.getParameter("objectClass");
        String collxnName = (String) req.getParameter("collxnName");
        String baseURL = ldapBean.getBaseURL();

        // the member var holds the specific name of the member attribute
        // for example: 'member' or 'uniqueMember'
        String member = (String) req.getParameter("member");

        // this is the number of embedded group levels used for the breadcrumb
        String levels = (String) req.getParameter("levels");
        String page = (String) req.getParameter("page");

        // set the history info to ldapBean
        // this stores the info needed to reconstruct the current location being browsed
        //ldapBean.setHistory(GROUP_PREFIX + "member=" + member + "&inputDN=" + URLEncoder.encode(inputDN) + "&levels=" + levels);
        ldapBean.setHistory(GROUP_PREFIX + "member=" + member + "&inputDN=" + URLUTF8Encoder.encode(inputDN) + "&levels=" + levels); // Symbio modified 05/19/2005
        ldapBean.setContainer(inputDN);

        if (page != null) {
            if (DEBUG) {
                System.out.println("page= " + page);
                System.out.println("baseURL= " + baseURL);
            }

            return (new ActionForward(baseURL + "?page=" + page, true));
        }

        Crumb[] breadCrumb = (Crumb[]) session.getAttribute(PAGE_BREADCRUMB);

        // newBCLen is the new length of the bread crumb trail
        int newBCLen = Integer.parseInt(levels) + 1;

        if (DEBUG) {
            System.out.println("inputDN= " + inputDN);
            System.out.println("baseURL= " + baseURL);
            System.out.println("member= " + member);
            System.out.println("newBCLen= " + newBCLen);
            System.out.println("levels= " + levels);
        }

        LDAPConnection   conn;
        SubscriptionMain main;
        boolean          isCollection = false;

        try {
            ServletContext sc = servlet.getServletConfig()
                                       .getServletContext();
            main = TenantHelper.getTenantSubMain(sc, req);
            dirType = main.getDirType();
            // Get the an LDAPConnection to either the GC or a DC depending
            // on where the group belongs to.
            if (LDAPVarsMap.get("COLLECTION_CLASS").equals(objectClass)) {
                // if it is a collection we need to use the DC
                // we also need to find the memeber DN
                conn = main.getLDAPEnv()
                           .getDCConn(inputDN, GUIUtils.getUser(req));
                inputDN      = getCollxnMemberDN(conn, inputDN);
                isCollection = true;
            } else {
                conn = LDAPWebappUtils.getBrowseConn(req);
            }
        } catch (SystemException se) {
            throw new GUIException(se);
        }

        Vector listing = null;

        if (!INVALID_COLLECTION.equals(inputDN)) {
            try {
                LDAPConnection conn1 = main.getLDAPEnv()
                                           .getGroupConn(inputDN, GUIUtils.getUser((HttpServletRequest) req));
                listing = getMemberAttributes(conn1, member, inputDN, isCollection);
            } catch (NamingException nexception2) {
                if (DEBUG) {
                    System.out.println("LDAPBrowseGroup: caught NamingException(2)");
                    nexception2.printStackTrace();
                }

                try {
                    LDAPUtils.classifyLDAPException(nexception2);
                } catch (SystemException se) {
                    throw new GUIException(se);
                }
            } catch (SystemException se) {
                throw new GUIException(se);
            }
        } else {
            // we are expanding an invalid collection
            PropsBean entry = new PropsBean();
            Locale locale = req.getLocale();
            String name = WebAppUtils.getMessage(main.getAppResources(), locale, "page.global.invalid_link");
            entry.setValue("type","invalid_target");
            entry.setValue(LDAPConstants.OBJECT_CLASS, INVALID_LINK);
            entry.setProperty(GUIConstants.DISPLAYNAME,name);
            listing = new Vector(1);
            listing.add(entry);
        }

        // add expanded group to bread crumb trail
        String groupName = "";
        String type = "group";

        if (collxnName != null) {
            // when expanding a collection, we are hiding the intermediate group that
            // contains the machines from the user.  Thus, add the name of
            // the collection to the breadcrumb, and not the name of
            // the group we're expanding.
            groupName = collxnName;

            // use collxn icon
            type = "collxn";
        } else {
            try {
                if (!INVALID_COLLECTION.equals(inputDN)) {
                    groupName = LDAPWebappUtils.parseName(conn, inputDN);
                } else {
                    groupName = "invalid";
                }
            } catch (SystemException se) {
                throw new GUIException(se);
            }
        }

        Crumb[] newBreadCrumb;

        /*String  link = "ldapBrowseGroup.do?member=" + member + "&inputDN=" + URLEncoder.encode((isCollection) ? collDN
                                                                                               : inputDN) + "&levels=" + newBCLen + "&objectClass=" + objectClass; */                                                                                    
        // Symbio modified 05/19/2005
        String  link = "ldapBrowseGroup.do?member=" + member + "&inputDN=" + URLUTF8Encoder.encode((isCollection) ? collDN
                                                                                               : inputDN) + "&levels=" + newBCLen + "&objectClass=" + objectClass;
	

        //if bread crumb has more elemens than levels
        //bread crumb has shrunk truncate otherwise it has grown add new DN
        if (breadCrumb.length >= newBCLen) {
            newBreadCrumb = new Crumb[newBCLen - 1];
            System.arraycopy(breadCrumb, 0, newBreadCrumb, 0, (newBCLen - 1));
        } else {
            newBreadCrumb = new Crumb[newBCLen];
            System.arraycopy(breadCrumb, 0, newBreadCrumb, 0, (newBCLen - 1));
            newBreadCrumb [newBCLen - 1] = new Crumb(groupName, type, link);
        }

        try {
            if (!INVALID_COLLECTION.equals(inputDN)) {
                LDAPName ldapName = conn.getParser();
                Name     dnName = ldapName.parse(inputDN);
                ldapBean.setSearchText(ldapName.getComponentValue(dnName, dnName.size() - 1));
            }
        } catch (NamingException ne) {
            throw new GUIException(ne);
        }
        setSessionVars(req, ldapBean, pageBean, listing, newBreadCrumb);
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
        return (new ActionForward(baseURL, true));
    }

    private void setSessionVars(HttpServletRequest req,
                                LDAPBean           ldapBean,
                                PagingBean         pageBean,
                                Vector             listing,
                                Crumb[]            newBreadCrumb)
        throws GUIException {
        if (DEBUG) {
            System.out.println("in setSessionVars");
        }

        HttpSession session = req.getSession();

        ldapBean.setUseLDAPPaging(false);
        ldapBean.setIsGroup(true);

        // leave bread crumb alone? yes, because we are paging
        ldapBean.setLeaveBreadCrumb(true);
        session.setAttribute(PAGE_BREADCRUMB, newBreadCrumb);

        int numResults = 0;
        session.setAttribute(DIRECTORY_TYPE, dirType);
        if (listing != null) {
            // remove any (non-group) ldap listings first
            session.removeAttribute(PAGE_TARGETS_RS);
            session.setAttribute(PAGE_GEN_RS, listing);
            numResults = listing.size();

            // for generic paging, clear out the paging bean to indicate new results
            session.removeAttribute(GEN_PAGE_BEAN);
        }

        pageBean.setStartIndex(0);
        pageBean.setCountPerPage(numResults);
        pageBean.setTotal(numResults);

        try {
            setLDAPBean(ldapBean, req);
            setPagingBean(pageBean, req);

            if (DEBUG) {
                System.out.println("after setLDAPBean and setPagingBean");
            }
        } catch (SystemException se) {
            throw new GUIException(se);
        }
    }

    /**
     * Get the list of member dn's for the group. For both AD, and iPlanet, we obtain all the attributes for the group.  AD will only return 1000 attributes at
     * one time so we make multiple getAttributes calls to get the entire list. remind : in the future, we can implement an intermediate layer that allows us
     * not to have to store all the results
     *
     * @param conn An LDAP connection
     * @param member The name of the member attribute {member|uniquemember}
     * @param inputDN The DN of the group in which we want to get the members of the groups
     * @param isCollection REMIND
     *
     * @return REMIND
     *
     * @throws NamingException REMIND
     */
    Vector getMemberAttributes(LDAPConnection conn,
                               String         member,
                               String         inputDN,
                               boolean        isCollection)
        throws NamingException {
        Attributes groupAttrs = null;
        Attribute  memberAttr = null;
        Vector     listing = new Vector(100);
        String[]   attrIDs = new String[1];

        if (LDAPVars.ACTIVE_DIRECTORY.equals(LDAPVarsMap.get("DIRECTORY_TYPE"))||LDAPVars.ADAM.equals(LDAPVarsMap.get("DIRECTORY_TYPE"))) {
            int start = 0;
            attrIDs [0] = member + ";range=" + start + "-*";

            try {
                groupAttrs = conn.getAttributes((String) inputDN, attrIDs);
            } catch (NamingException ne) {
                // this happens when we are sitting on the collections folder
                // and the result set has been refreshed by report center
                // we will have a dn that has been deleted
                PropsBean entry = new PropsBean();
                Locale locale = Locale.getDefault();
                String name = WebAppUtils.getMessage(main.getAppResources(), locale, "page.global.invalid_link");
                entry.setValue("type","invalid_target");
                entry.setValue(LDAPConstants.OBJECT_CLASS, INVALID_LINK);
                entry.setProperty(GUIConstants.DISPLAYNAME,name);
                listing.add(entry);

                return listing;
            }

            // We can't just use groupAttrs.get(attriIDs[0]) because
            // the server will actually the * to the actual end index
            // that it is returning in this call
            NamingEnumeration allAttrs = groupAttrs.getAll();

            while (allAttrs.hasMore()) {
                memberAttr = (Attribute) allAttrs.next();
            }

            while (memberAttr != null) {
                addMembers(listing, memberAttr, isCollection, conn);

                if (DEBUG) {
                    System.out.println("start " + start);
                    System.out.println("+++++ list has " + listing.size());
                }

                // Obtains the range of objects returned from the
                // server in the format of member;range0=0-x.  When x
                // is a '*', we have finished listing all the attributes
                String returnID = memberAttr.getID();
                int    idx = returnID.indexOf('-');
                returnID = returnID.substring(idx + 1);

                try {
                    start = Integer.parseInt(returnID) + 1;
                } catch (NumberFormatException ne) {
                    memberAttr = null;

                    continue;
                }

                attrIDs [0] = member + ";range=" + start + "-*";
                groupAttrs  = conn.getAttributes((String) inputDN, attrIDs);
                allAttrs    = groupAttrs.getAll();
                memberAttr  = null;

                while (allAttrs.hasMore()) {
                    memberAttr = (Attribute) allAttrs.next();
                }
            }
        } else {
            attrIDs [0] = member;
            groupAttrs  = conn.getAttributes((String) inputDN, attrIDs);
            memberAttr  = groupAttrs.get(attrIDs [0]);
            addMembers(listing, memberAttr, isCollection, conn);
        }

        return listing;
    }

    void addMembers(Vector    listing,
                    Attribute memberAttr,
                    boolean   isCollection, LDAPConnection conn)
        throws NamingException {
        if (memberAttr != null) {
            NamingEnumeration ne = memberAttr.getAll();

            // iterate through the list of member dn's
            while (ne.hasMoreElements()) {
                String    dn = (String) ne.next();
                PropsBean entry = new PropsBean();

                // Store the dn of each member
                entry.setValue(LDAPVarsMap.get("DN"), dn);
                 if (isCollection) {
                     entry.setValue(GUIConstants.ISTARGETABLE,"false");
                 }
                 if(!isCloudHidableEntry(dn, conn)) {
                   listing.addElement(entry);
                 }
            }
        }
    }
    private boolean isCloudHidableEntry(String dn, LDAPConnection conn) {
    	try {
    		IConfig tenantConfig = tenant.getConfig();
            if(null != tenantConfig.getProperty("marimba.ldap.cloud.hideentries")) {
            	LDAPName ldapName = conn.getParser();
            	String[] cloudHideEntries = LDAPWebappUtils.getTokenizerProperty(tenantConfig.getProperty("marimba.ldap.cloud.hideentries"));
            		if(null != cloudHideEntries) {
            			Name inputDnName = ldapName.parse(dn);
            			for(String cloudHideEntrie : cloudHideEntries) {
            				Name hideName = ldapName.parse(cloudHideEntrie);
            				if (inputDnName.equals(hideName)) {
                                return true;
                            }
            		}
            	}
            }
    	} catch(Exception ed) {
    		
    	}
    	return false;
    }

    /**
     * This method takes the dn of a collection object and returns its member attribute.  That member attribute is the dn of a (machine) group that represents
     * the results of performing the query associated with that collection.
     *
     * @param conn REMIND
     * @param dn REMIND
     *
     * @return REMIND
     *
     * @throws SystemException REMIND
     */
    String getCollxnMemberDN(LDAPConnection conn,
                             String         dn)
        throws SystemException {
        try {
            String[] attrIDs = new String[1];
            attrIDs [0] = LDAPVarsMap.get("GROUP_MEMBER");

            // get the list of member dn's for the collection object
            Attributes groupAttrs = conn.getAttributes(dn, attrIDs);

            // get the Attribute out of the Attributes object
            Attribute memberAttr = groupAttrs.get(attrIDs [0]);

            if ((memberAttr != null) && (memberAttr.size() == 1)) {
                // there is only one member attribute per collection object
                // that contains the dn of the group that holds the groups of results.
                String memberDN = (String) memberAttr.get(0);

                // make sure this is a valid 5.0 collection by
                // querying that the member is a group object
                // we're assuming here that they do not have any of
                // Josh's first version of 5.0 collections.
                String[] searchAttrs = new String[1];
                searchAttrs [0] = LDAPConstants.OBJECT_CLASS;

                Attributes attrs = conn.getAttributes(memberDN.trim(), searchAttrs);
                String     objectclass = LDAPUtils.getObjectClass(attrs, LDAPVarsMap, tenantName, channel);

                if (LDAPVarsMap.get("GROUP_CLASS").equalsIgnoreCase(objectclass) || LDAPVarsMap.get("GROUP_CLASS_UNIQUE").equalsIgnoreCase(objectclass)) {
                    return memberDN.trim();
                } else {
                    // the member is not a group, therefore invalid
                    return INVALID_COLLECTION;
                }
            } else {
                // this is either a 4.x collection that
                // has multiple members or this is an invalid
                // 5.0 collection that is missing its single member attribute
                return INVALID_COLLECTION;
            }
        } catch (NamingException ne) {
            LDAPUtils.classifyLDAPException(ne);
        }

        return dn;
    }
}
