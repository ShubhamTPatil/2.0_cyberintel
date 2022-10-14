// Copyright 1997-2009, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.taglib;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.objects.dao.LDAPSubscription;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.PackageNavigateForm;
import com.marimba.apps.subscriptionmanager.webapp.system.PagingBean;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;
import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.tools.regex.Matcher;
import com.marimba.webapps.intf.IMapProperty;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.tools.util.PropsBean;
import com.marimba.webapps.tools.util.PropsComparator;
import com.marimba.webapps.tools.util.UTFUtils;
import com.marimba.webapps.tools.util.WebAppUtils;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.*;
/**
 * Custom tag that takes a search filter for the title of the package. The results vector stored as a session variable name page_deppkgs_rs.
 *
 * @author Theen-Theen Tan
 * @version $Revision$, $Date$
 */
public class GetDeployedPackagesTag
    extends TagSupport
    implements IWebAppConstants,
                   IWebAppsConstants {
    final static boolean   DEBUG = com.marimba.apps.subscription.common.intf.IDebug.DEBUG;
    static PropsComparator comp = new PropsComparator("title");

    // this encapsulates the sort type and sort order of the results
    String           statebeanstring;
    String           search = null;
    String           contenttype = null;
    SubscriptionMain main;
    LDAPConnection   conn;
    String           base = null;

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getStateBean() {
        return this.statebeanstring;
    }

    /**
     * REMIND
     *
     * @param statebeanstring REMIND
     */
    public void setStateBean(String statebeanstring) {
        this.statebeanstring = statebeanstring;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getSearch() {
        if ((this.search == null) || ("".equals(search.trim()))) {
           return "*";
        }
        return this.search.trim();
    }

    /**
     * REMIND
     *
     * @param search REMIND
     */
    public void setSearch(String search) {
        this.search = search;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getContentType() {
        if ((this.contenttype == null) || (this.contenttype.length() == 0)) {
            return ISubscriptionConstants.CONTENT_TYPE_ALL;
        }

        return this.contenttype;
    }

    /**
     * REMIND
     *
     * @param ct type Restricts the deployed content returned by this tag to be of this specified type.
     * Values are defined as ISubscriptionConstants.CONTENT_TYPE_*.  By default, all content types are returned.
     *
     */
    public void setContentType(String ct) {
        this.contenttype = ct;
    }

    /**
     * DOCUMENT ME!
     *
     * @return REMIND
     *
     * @exception JspException if a LDAPConnection error
     */
    public int doStartTag()
        throws JspException {
        ServletContext context = pageContext.getServletContext();
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

        main = TenantHelper.getTenantSubMain(context, request);
        IMapProperty      statebean = (IMapProperty) pageContext.getSession()
                                                                  .getAttribute(statebeanstring);
        Map<String, String> LDAPVarsMap = main.getLDAPVarsMap();
        String[]  searchAttrs = new String[2];
        searchAttrs [0] = LDAPVarsMap.get("CHANNELTITLE");
        searchAttrs [1] = LDAPVarsMap.get("CHANNEL");

        if ((statebean == null)) {
            if (DEBUG4) {
                System.out.println(" GetPkgsFromTargetsTag:: statebean is null or empty");
            }

            PackageNavigateForm form = new PackageNavigateForm();
            form.initialize();
            statebean = form;
            pageContext.getSession()
                       .setAttribute(statebeanstring, form);
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
            System.out.println(" GetDeployedPackagesTag:: sorttype is " + sorttype);
            System.out.println(" GetDeployedPackagesTag:: sortorder is  " + sortorder);
            System.out.println(" GetDeployedPackagesTag:: form display type is url? " + statebean.getValue("show_url"));
        }

        try {
            conn = LDAPWebappUtils.getSubConn((HttpServletRequest) pageContext.getRequest());
            base = main.getSubBase();
        } catch (SystemException se) {
            if (DEBUG) {
                se.printStackTrace();
            }

            WebAppUtils.saveTagException(pageContext, se);
        }

        if (DEBUG) {
            System.out.println(" GetDeployedPackagesTag:: connection info: ");
            System.out.println(conn.toString());
            System.out.println(" GetDeployedPackagesTag:: base= " + base);
        }

        NamingEnumeration nenum = null;
        HashMap           chnTbl = new HashMap(100);
        String            searchFilter = null;
        Matcher           matcher = new Matcher();
        Object[]          MATCH = new Object[1];

        matcher.add(UTFUtils.convertToUTF8(getSearch()), MATCH);
        matcher.add(UTFUtils.convertToUTF8(getSearch().toLowerCase()), MATCH);

        String searchString = getSearch().replace(':', '-').replace('/', '-');

        matcher.prepare();
        StringBuffer sb = new StringBuffer();
        if ("*".equals(getSearch())) {
            pageContext.getSession()
                       .removeAttribute("page_pkgs_dep_search");
            // search filter added to display only the packages that are assigned
            // to targets browsed from ldap. Previously it displays the packages
            // includes targets that are assigned to the targets sourced from tx.
            sb.append("(&");
            sb.append("(" + LDAPVarsMap.get("CHANNEL") + "=*)");
            if(main.getUsersInLDAP()) {
                //(&(mrbachannel=*)(|(mrbatargetdn=*)(mrbatargetall=true)))
                //(&(channel)(|(retrieve ldap objects with targetDN property)(retrieve all_all target with property targetall)))
                sb.append("(|(" + LDAPVarsMap.get("TARGETDN") + "=*)(" + LDAPVarsMap.get("TARGET_ALL") + "=true)))");
            } else {
                //(&(mrbachannel=*)(|(mrbatargettxuser=*)(!(mrbatargettype=user))))
                //(&(channel)(|(retrieve ldap objects with targetDN property)(retrieve all_all target with property targetall)))
                sb.append("(|(" + LDAPVarsMap.get("TARGET_TX_USER") + "=*)(!(" + LDAPVarsMap.get("TARGETTYPE") + "=user))))");
            }
            searchFilter = sb.toString();
        } else {
            // Appending * to the front of <user_search> in
            // (LDAPVarsMap.get("CHANNELTITLE=*<user_search>) so that exact match of <user_search>
            // will work. This is because the mrbaChannelTitle is actually <url>=<title>,
            // not just title.
            // Appending * around <user_search> in (LDAPVarsMap.get("CHANNELTITLE=*<user_search>*)
            // because we are trying to match against the channelname from the url.  Since
            // mrbaChannel is actually <url>=<state>, we need the * around <user_search>.
            if(main.getUsersInLDAP()) {
                // search filter added to display only the packages that are assigned to targets
                // browsed from ldap. Previously it displays the packages includes targets that
                // are assigned to the targets sourced from tx.
                //(&(|(mrbachanneltitle=*pkg)(mrbachannel=*pkg*))(|(mrbatargetdn=*)(mrbatargetall=true)))
                sb.append("(&");
                sb.append("(|(" + LDAPVarsMap.get("CHANNELTITLE") + "=*" + getSearch() + ")(" + LDAPVarsMap.get("CHANNEL") + "=*" + getSearch() + "*))");
                sb.append("(|(" + LDAPVarsMap.get("TARGETDN") + "=*)(" + LDAPVarsMap.get("TARGET_ALL") + "=true)))");
            } else {
                //(&(|(mrbachanneltitle=*pkg)(mrbachannel=*pkg*))(|(mrbatargettxuser=*)(!(mrbatargettype=true))))
                sb.append("(&");
                sb.append("(|(" + LDAPVarsMap.get("CHANNELTITLE") + "=*" + getSearch() + ")(" + LDAPVarsMap.get("CHANNEL") + "=*" + getSearch() + "*))");
                sb.append("(|(" + LDAPVarsMap.get("TARGET_TX_USER") + "=*)(!(" + LDAPVarsMap.get("TARGETTYPE") + "=user))))");
            }
            searchFilter = sb.toString();
        }

        if (DEBUG) {
            System.out.println("searchFilter" + searchFilter);
        }

        try {
            nenum = conn.search(searchFilter, searchAttrs, base, LDAPConstants.LDAP_SCOPE_SUBTREE);

            if (nenum != null) {
                while (nenum.hasMoreElements()) {
                    SearchResult sr = (SearchResult) nenum.next();

                    if (DEBUG) {
                        System.out.println(" sr = " + sr.getName());
                    }

                    Attributes srAttrs = sr.getAttributes();

                    if (DEBUG) {
                        System.out.println(" srAttrs.size()= " + srAttrs.size());
                    }

                    Hashtable chnInitMap = LDAPSubscription.loadChannelAttr(LDAPVarsMap.get("CHANNEL"), srAttrs);
                    Hashtable chnTitleMap = LDAPSubscription.loadChannelAttr(LDAPVarsMap.get("CHANNELTITLE"), srAttrs);
                    Object[]  matches;
                    String    url = null;
                    String    title = null;
                    String    state1 = null;
                    String    type = null;

                    for (Enumeration ite = chnInitMap.keys(); ite.hasMoreElements();) {
                        url    = (String) ite.nextElement();
                        title  = (String) chnTitleMap.get(url);
                        state1 = (String) chnInitMap.get(url);

                        if (LDAPSubscription.isNone(state1)) {
                            continue;
                        }

                        int idx = state1.indexOf(',');
                        if (idx > 0) {
                            type = state1.substring(idx + 1).trim();
                            state1 = state1.substring(0, idx);
                        }

                        if (ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP.equals(type)) {
                             type = ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP;
                        } else {
                            type = ISubscriptionConstants.CONTENT_TYPE_APPLICATION;
                        }

                        if (!"true".equals(statebean.getValue("show_url"))) {
                            if (title == null) {
                                if (DEBUG) {
                                    System.out.println("matching url " + Channel.extractChannelName(url).toLowerCase());
                                }

                                matches = matcher.match(UTFUtils.convertToUTF8(Channel.extractChannelName(url).toLowerCase()));
                            } else {
                                if (DEBUG) {
                                    System.out.println("matching title " + title);
                                }

                                String chTitle = title.replace(':', '-');
                                chTitle    = chTitle.replace('/', '-');
                                matches = matcher.match(UTFUtils.convertToUTF8(chTitle.toLowerCase()));
                            }
                        } else {
                            String name = url.replace(':', '-');
                            name    = name.replace('/', '-');
                            matches = matcher.match(UTFUtils.convertToUTF8(name));
                        }

                        if (matches.length != 0) {

                            // Only return content type as defined by the caller of the tag
                            if (!ISubscriptionConstants.CONTENT_TYPE_ALL.equals(getContentType())) {
                                 if (!getContentType().equals(type)) {
                                      continue;
                                 }
                            }

                            if (chnTbl.get(url) == null) {
                                PropsBean entry = new PropsBean();
                                entry.setValue("url", url);
                                entry.setValue("type", type);

                                if (title == null) {
                                    entry.setValue("title", Channel.extractChannelName(url));
                                } else {
                                    entry.setValue("title", title);
                                }

                                chnTbl.put(url, entry);
                            }
                        }
                    }
                }
            }
        } catch (NamingException ne) {
            if (DEBUG) {
                ne.printStackTrace();
            }

            try {
                LDAPUtils.classifyLDAPException(ne, null, false);
            } catch (SystemException se) {
                WebAppUtils.saveTagException(pageContext, se);
            }
        }

        if (DEBUG) {
            System.out.println("last searched connection=" + conn.toString());
            System.out.println("result size: " + chnTbl.size());
        }

        // Set LDAP results to session var
        ArrayList result = new ArrayList(chnTbl.values());
        Collections.sort(result, comp);

        Vector newResult = new Vector(result);
        pageContext.getSession()
                   .removeAttribute(PKGLIST_BEAN);
        pageContext.getSession()
                   .setAttribute(PAGE_PKGS_DEP_RS, newResult);

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

    static String convertToValidSearchString(String searchName) {
        String newName = searchName.replace(':', '_');
        newName = newName.replace('/', '\\');

        return newName;
    }

    /**
     * REMIND
     *
     * @param req REMIND
     *
     * @return REMIND
     */
    public PagingBean getPagingBean(HttpServletRequest req) {
        HttpSession session = req.getSession();
        PagingBean  pagingBean = (PagingBean) session.getAttribute(SESSION_PAGE);

        if (null == pagingBean) {
            pagingBean = new PagingBean();
        }

        return pagingBean;
    }
}
