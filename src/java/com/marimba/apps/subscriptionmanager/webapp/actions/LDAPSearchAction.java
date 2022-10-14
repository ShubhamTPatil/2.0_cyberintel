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

import com.marimba.tools.util.URLUTF8Decoder;
import com.marimba.apps.subscriptionmanager.ldapsearch.*;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscription.common.LDAPEnv;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.intf.util.IConfig;
import com.marimba.apps.subscriptionmanager.intf.ILdapContext;
import com.marimba.apps.subscriptionmanager.intf.ILdapSearch;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.webapp.forms.LDAPNavigationForm;
import com.marimba.apps.subscriptionmanager.webapp.system.LDAPBean;
import com.marimba.apps.subscriptionmanager.webapp.system.PagingBean;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPPolicyHelper;
import com.marimba.apps.subscriptionmanager.webapp.intf.GUIConstants;
import com.marimba.intf.util.IProperty;

import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPName;
import com.marimba.tools.ldap.LDAPConstants;


import com.marimba.webapps.intf.*;

import com.marimba.webapps.tools.util.PropsBean;

/**
 * Searching LDAP This action is called when a user enters a text string in the 'Search for' field and clicks 'Go'. See LDAPBrowseGroupAction for when a user
 * expands a group (i.e. entry that is of objectclass 'groupofnames' or 'groupofuniquenames'. See LDAPBrowseAction for when a user expands an organizational
 * unit or clicks a crumb in the bread crumb trail.
 *
 * @author Michele Lin
 * @version 1.29, 01/24/2003
 */
public final class LDAPSearchAction
        extends AbstractAction
        implements IWebAppsConstants {
    final static boolean DEBUG = com.marimba.apps.subscriptionmanager.intf.IAppConstants.DEBUG;

    //    final static boolean DEBUG = false;

    static final String SEARCH_ALL = "All types";
    static final String SEARCH_USER = "User";
    static final String SEARCH_GROUP = "Group";
    static final String SEARCH_MACHINE = "Machine";
    static final String SEARCH_COLLECTION = "Collection";
    static final String SEARCH_CONTAINER = "Container";    
    
    static final String BASIC = "basic";
    static final String ADVANCED = "advanced";
    static final String POLICY = "policy";
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
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest req,
                                 HttpServletResponse response)
            throws IOException,
            ServletException {
        Vector listing = null;
        if (DEBUG) {
            System.out.println("LDAPSearchAction called");
        }

        super.init(req);

        HttpSession session = req.getSession();
        LDAPBean ldapBean = getLDAPBean(req);
        PagingBean pageBean = getPagingBean(req);
        String baseURL = ldapBean.getBaseURL();
        String entryPoint = ldapBean.getEntryPoint();
        String container = ((LDAPNavigationForm) form).getContainer();
        String searchString = ((LDAPNavigationForm) form).getSearchString();
        String advSearchString = ((LDAPNavigationForm) form).getAdvSearchString();
        String searchType = ((LDAPNavigationForm) form).getSearchType();
        String basicLink = ((LDAPNavigationForm) form).getBasicLink();
        String limitSearch = ((LDAPNavigationForm) form).getLimitSearch();
        String advLimitSearch = ((LDAPNavigationForm) form).getAdvLimitSearch();        
        
        String searchQuery = req.getParameter(VALUE_OF_SEARCHQUERY); 
        PropsBean searchCriteria = splitUserQuery(searchQuery);        
        String createDateFrom = searchCriteria.getProperty(CREATE_DATE_FROM);                             
        String createDateTo = searchCriteria.getProperty(CREATE_DATE_TO);             
        String modifyDateFrom = searchCriteria.getProperty(MODIFY_DATE_FROM);
        String modifyDateTo = searchCriteria.getProperty(MODIFY_DATE_TO);        
        String createDateCriteria = searchCriteria.getProperty(CREATE_DATE_CRITERIA);
        String modifyDateCriteria = searchCriteria.getProperty(MODIFY_DATE_CRITERIA);
        String targetName = searchCriteria.getProperty(TARGET_NAME);
        String orphanPolicy = searchCriteria.getProperty(ORPHAN_POLICY);
        String policyCriteria = req.getParameter(POLICY_CRITERIA);
        String searchBase = null;  
        String params = null;
        if(basicLink.equals(BASIC) && (null == searchString || "".equals(searchString.trim()))){
        	KnownException ke = new KnownException(LDAP_SEARCH_EMPTYSTRING);
            throw new GUIException(ke);
        } else if(basicLink.equals(ADVANCED) && (null == advSearchString || "".equals(advSearchString.trim()))) {
        	KnownException ke = new KnownException(LDAP_SEARCH_EMPTYSTRING);
            throw new GUIException(ke);
        }   
        	
        boolean level = LDAPConstants.LDAP_SCOPE_SUBTREE;
        if ((basicLink.equals(BASIC) && (null != limitSearch && limitSearch.equals("true"))) ||
                (basicLink.equals(ADVANCED) && (null != advLimitSearch && advLimitSearch.equals("true")))) {
            level = LDAPConstants.LDAP_SCOPE_ONELEVEL;
        }

        if (DEBUG) {
            System.out.println("searchString = " + searchString);
            System.out.println("advSearchString = " + advSearchString);
            System.out.println("searchType = " + searchType);
            System.out.println("limitSearch = " + limitSearch);
        }


        // remove '/sm/servlet' from baseURL
        String context = req.getContextPath();

        if (baseURL.startsWith(context)) {
            baseURL = baseURL.substring(context.length());
        }

        ServletContext sc = servlet.getServletConfig()
                .getServletContext();
        SubscriptionMain main = TenantHelper.getTenantSubMain(sc, req);
        Map<String, String> LDAPVarsMap = main.getLDAPVarsMap();
        
        String[] queryAttr = getQueryAttr(main.getSubscriptionConfig(), LDAPVarsMap);
        String[] targetAbleClasses = getTargetAbleClasses(main.getSubscriptionConfig(), main.getMarimbaConfig(), LDAPVarsMap);
        
        String[] searchAttrs = {
                LDAPConstants.OBJECT_CLASS, LDAPVarsMap.get("CONTAINER_PREFIX"), LDAPVarsMap.get("CONTAINER_PREFIX2"), LDAPVarsMap.get("GROUP_PREFIX"), LDAPVarsMap.get("MACHINE_NAME"),
                LDAPVarsMap.get("USER_ID"), LDAPConstants.DOMAIN_PREFIX
            };
        String attrs[]= {LDAPVarsMap.get("CREATE_DATE"), LDAPVarsMap.get("MODIFY_DATE"), LDAPConstants.OBJECT_CLASS, LDAPVarsMap.get("DN"),LDAPVarsMap.get("TARGETTYPE"),
			  LDAPVarsMap.get("CONTAINER_PREFIX"), LDAPVarsMap.get("CONTAINER_PREFIX2"), LDAPVarsMap.get("GROUP_PREFIX"), LDAPVarsMap.get("MACHINE_NAME"),
			  LDAPVarsMap.get("USER_ID"), LDAPConstants.DOMAIN_PREFIX, LDAPVarsMap.get("TARGETDN"), LDAPVarsMap.get("AD_UID")};
        
        // get ldap connection
        LDAPConnection conn;
        LDAPConnection dcConn;
        String subBase = main.getSubBase();

        try {
            conn = LDAPWebappUtils.getBrowseConn(req);
            dcConn = LDAPWebappUtils.getCollConn(req);
            subBase = main.getSubBase();
        } catch (SystemException se) {
            throw new GUIException(se);
        }

        if (DEBUG) {
            System.out.println("container= " + container);
            System.out.println("baseURL= " + baseURL);
            System.out.println("searchString= " + searchString);
            System.out.println("limitSearch= " + limitSearch);
        }

        boolean usersInLDAP = main.getUsersInLDAP();
        NamingEnumeration nenum = null;
        listing = new Vector();

        // This is the searchFilter that is used to match the string entered
        // by the user with entries in the directory based on which
        // entry point they're in.
        // The search base if the current container the user is visiting,
        // unless the 'search entire entry point' option is selected.
        // Then the search base is the full dn of the entry point.
        // Also, only machines are matched under the machines entry point, users under the
        // Users entry point, etc...
        // These are retrieved from the LDAPEntryPoints class.
        // remind mlin: in the future, we also want to provide a top-level search
        if(container==null || container.equals("")){
        	container=main.getSubBase();
        }
        searchBase = URLUTF8Decoder.decode(container);

        if ((searchBase == null) || "".equals(searchBase)) {
            KnownException ke = new KnownException(LDAP_SEARCH_EMPTYCONTAINER);
            throw new GUIException(ke);
        }
        if(!POLICY.equals(basicLink))
        {
        String prefix = "cn";

        String searchFilter;
        if (basicLink.equals(BASIC)) {
            searchString = searchString.trim();
            searchFilter = createBasicSearchFilter(searchString, searchType, queryAttr, targetAbleClasses, main.getSubscriptionConfig() , main.getMarimbaConfig(), LDAPVarsMap);
        } else {
            advSearchString = advSearchString.trim();
            searchFilter = createAdvSearchFilter(advSearchString);
        }

        if (DEBUG) {
            System.out.println("entryPoint= " + entryPoint);
        }

        if (!usersInLDAP && (PEOPLE_EP.equals(entryPoint) || GROUPS_EP.equals(entryPoint))) {
            try {
                if (DEBUG) {
                    System.out.println("LDAPSearchAction: tx source search");
                }

                if (ldapBean.getIsGroup() && GROUPS_EP.equals(entryPoint)) {
                    listing = LDAPWebappUtils.setUsersOrGroupsListTx(USER_TXGROUP, ldapBean.getGroup(), session, main, req, searchString, LDAPVarsMap);

                    if (DEBUG) {
                        System.out.println("LDAPSearch: tx group member search, group = " + ldapBean.getGroup());
                    }
                } else {
                    listing = LDAPWebappUtils.setUsersOrGroupsListTx(entryPoint, null, session, main, req, searchString, LDAPVarsMap);
                }
            } catch (SystemException se) {
                throw new GUIException(se);
            }
        } else {
            if (DEBUG) {
                System.out.println("searchFilter= " + searchFilter);
                System.out.println("searchBase= " + searchBase);
            }
            //We are not sourcing users from transmitter, so parse the LDAP search results
            try {

                Vector bases;

                // if we're at the top level, we retrieve all the domains and query them
                // individually.  otherwise use the base at whatever level we're at.
                if ("top".equals(searchBase)) {
                    bases = main.getLDAPEnv().getTargetHome(dcConn);
                } else {
                    bases = new Vector();
                    bases.add(searchBase);
                }

                Iterator iter = bases.iterator();
                while (iter.hasNext()) {
                    String base = iter.next().toString();
                    if(DEBUG) {
                        System.out.println("querying base = " + base);
                    }

                    nenum = conn.search(searchFilter, searchAttrs, base, level);

                    // Obtain the DNs of the search results.
                    // Resolution of target type and blue dot
                    // is done in the MemberListProcessor
                    // when the setPagingResultsTag is called from
                    // ldap_nav.jsp.
                    listing.addAll(getTargets(req, nenum, base));
                }

                if (DEBUG) {
                    System.out.println("listing from ldapSearchAction= ");

                    for (int i = 0; i < listing.size(); i++) {
                        System.out.println("listing[" + i + "]= " + ((PropsBean) listing.get(i)).getValue("dn"));
                    }
                }
            } catch (NamingException ne) {
                if (DEBUG) {
                    ne.printStackTrace();
                }

                try {
                    LDAPUtils.classifyLDAPException(ne);
                } catch (SystemException se) {
                    throw new GUIException(se);
                }
            } catch (SystemException se) {
                throw new GUIException(se);
            }
        }        

        params = "?search=true";

        if (searchBase != null) {
            //params = params + "&container=" + URLEncoder.encode(searchBase);
            params = params + "&container=" + URLUTF8Encoder.encode(searchBase); // Symbio modified 05/19/2005
        }

        if (searchString != null) {
            //params = params + "&searchString=" + URLEncoder.encode(searchString);
            params = params + "&searchString=" + URLUTF8Encoder.encode(searchString); // Symbio modified 05/19/2005
        }

        if (limitSearch != null) {
            //params = params + "&limitSearch=" + URLEncoder.encode(limitSearch);
            params = params + "&limitSearch=" + URLUTF8Encoder.encode(limitSearch); // Symbio modified 05/19/2005
        }
      }
      else{ // policy search block   
        	PropsBean criteria = new PropsBean();        	       	        	        		        	          	
            criteria.setValue(CREATE_DATE_FROM, createDateFrom);
            criteria.setValue(CREATE_DATE_CRITERIA, createDateCriteria);
            criteria.setValue(MODIFY_DATE_FROM, modifyDateFrom);
            criteria.setValue(MODIFY_DATE_CRITERIA, modifyDateCriteria);            
            criteria.setValue(TARGET_NAME, targetName);
            criteria.setValue(CREATE_DATE_TO, createDateTo);
            criteria.setValue(MODIFY_DATE_TO, modifyDateTo);
            criteria.setValue(ORPHAN_POLICY, orphanPolicy);
            IProperty prop =  main.getLDAPConfig();
        	ILdapSearch ldapSearch = new TargetViewQueryBuilder();
        	ILdapContext ldapContext = null;
        	try {
        		ldapContext = new LdapContext(LDAPWebappUtils.getBrowseConn(req), main.getSubBase(), LDAPConstants.LDAP_SCOPE_SUBTREE, LDAPVarsMap);	
        	} catch (SystemException se) {
                throw new GUIException(se);
            }        	
        	ldapSearch.setContext(ldapContext);        	        	        	
        	ldapSearch.setCriteria(criteria);    	    	                  
            ldapSearch.setAttributes(attrs);
            ldapSearch.setUsersInLDAP(usersInLDAP);
        	try {        		
        		listing = ldapSearch.execute();
        	}catch (Exception e) {
            	e.printStackTrace();
            }
        	
        	params = "?search=true";
            searchBase = URLUTF8Decoder.decode(container);
            if (null != searchBase) {
                params = params + "&container=" + URLUTF8Encoder.encode(searchBase); 
            }
            params = params + "&policyCriteria=" + policyCriteria;           
        }
              
      //remove any results for all of the items listed
        session.removeAttribute(PAGE_TARGETS_RS);
        session.setAttribute(DIRECTORY_TYPE, main.getDirType());
        // Set LDAP results to session var
        if (DEBUG) {
            System.out.println("LDAPSearchAction: page var = " + PAGE_TARGETS_RS);
            System.out.println("LDAPSearchAction: listing = " + listing);
        }

        session.setAttribute(PAGE_GEN_RS, listing);

        // for generic paging, clear out the paging bean to indicate new results
        session.removeAttribute(GEN_PAGE_BEAN);
        ldapBean.setContainer(searchBase);
        ldapBean.setUseLDAPPaging(false);

        //ldapBean.setIsGroup(false);
        // leave bread crumb alone?  no, leave alone only when expanding groups and paging
        ldapBean.setLeaveBreadCrumb(false);

        // we currently aren't paging through search results, defect 28910
        // so just set the paging params so that all results display on one page
        pageBean.setStartIndex(0);
        pageBean.setCountPerPage(listing.size());
        pageBean.setTotal(listing.size());
        pageBean.setHasMoreResults(false);

        try {
            setLDAPBean(ldapBean, req);
            setPagingBean(pageBean, req);
        } catch (SystemException se) {
            throw new GUIException(se);
        }

        return (new ActionForward(baseURL + params, true));
    }

    String createBasicSearchFilter(String searchString, String searchType, String[] queryAttr, String[] targetAbleClasses, 
    		IProperty subConfig, IProperty marimbaConfig, Map<String, String>LDAPVarsMap) {
        StringBuffer sb = new StringBuffer();

        if (SEARCH_ALL.equals(searchType)) {
			sb.append("(&" + "(|");

            for (int i = 0; i < queryAttr.length; i++) {
                sb.append("(" + queryAttr[i] + "=" + searchString + ")");
            }

            sb.append(")(|");

            for (int i = 0; i < targetAbleClasses.length; i++) {
            	if (!targetAbleClasses[i].contains(",")) {
    				sb.append("(objectclass=" + targetAbleClasses[i] + ")");
    			} else {
    				String[] classes = targetAbleClasses[i].split(",");
                    for (String objClass : classes) {
                        sb.append("(objectclass=" + objClass + ")");
                    }
                }
            }

            sb.append(")");

            sb.append("(!(objectclass= " + LDAPVarsMap.get("SUBSCRIPTION_CLASS") + "))" + ")");
        } else if (SEARCH_USER.equals(searchType)) {
            String attrib = subConfig.getProperty(LDAPConstants.CONFIG_USERIDATTR);
            String objclass = subConfig.getProperty(LDAPConstants.CONFIG_USERCLASS);
            if (attrib == null) {
                attrib = LDAPVarsMap.get("USER_NAME");
            }
            if (objclass == null) {
                objclass = LDAPVarsMap.get("USER_CLASS");
            }
            sb.append("(&");
            sb.append("(" + attrib + "=" + searchString + ")");

			if (!objclass.contains(",")) {
				sb.append("(objectclass=" + objclass + ")");
			} else {
				String[] classes = objclass.split(",");
                sb.append("(|");
                for (String objClass : classes) {
                    sb.append("(objectclass=" + objClass + ")");
                }
                sb.append(")");
            }
            sb.append("(!");
            sb.append(main.getLDAPEnv().createSearchFilter(LDAPConstants.OBJECT_CLASS, LDAPUtils.getLDAPVarArr("MACHINE_CLASS_PROP", main.getDirType())));
            sb.append(")");
            sb.append(")");
        } else if (SEARCH_GROUP.equals(searchType)) {
            String attrib = subConfig.getProperty(LDAPConstants.CONFIG_GROUPNAMEATTR);
            String objclass = subConfig.getProperty(LDAPConstants.CONFIG_GROUPCLASS);
            String[] objclasses;

            if (attrib == null) {
                attrib = LDAPVarsMap.get("GROUP_PREFIX");
            }
            if (objclass == null) {
                objclasses = new String[]{LDAPVarsMap.get("GROUP_CLASS"), LDAPVarsMap.get("GROUP_CLASS_UNIQUE")};
            } else {
                // parse the groups (they're comma-seperated)
                StringTokenizer st = new StringTokenizer(objclass, ",");
                objclasses = new String[st.countTokens()];
                int i = 0;
                while (st.hasMoreTokens()) {
                    objclasses[i++] = st.nextToken();
                }
            }
            sb.append("(&");
            sb.append("(" + attrib + "=" + searchString + ")");
            sb.append("(|");
            for (int i = 0; i < objclasses.length; i++) {
                sb.append("(objectclass=" + objclasses[i] + ")");
            }
            sb.append("))");
        } else if (SEARCH_MACHINE.equals(searchType)) {
            String attrib = subConfig.getProperty(LDAPConstants.CONFIG_MACHINENAMEATTR);
            String objclass = marimbaConfig.getProperty(LDAPConstants.CONFIG_MACHINECLASS);
            if (attrib == null) {
                attrib = LDAPVarsMap.get("MACHINE_NAME");
            }
            sb.append("(&");
            sb.append("(" + attrib + "=" + searchString + ")");
            if (objclass == null) {
                sb.append(main.getLDAPEnv().createSearchFilter(LDAPConstants.OBJECT_CLASS, LDAPUtils.getLDAPVarArr("MACHINE_CLASS_PROP", main.getDirType())));
            } else {
                sb.append(LDAPUtils.createSearchFilter(LDAPConstants.OBJECT_CLASS, objclass));
            }
            sb.append(")");
        } else if (SEARCH_COLLECTION.equals(searchType)) {
            String attrib = LDAPVarsMap.get("COLLECTION_NAME");
            String objclass = subConfig.getProperty(LDAPConstants.CONFIG_COLLECTIONCLASS);
            if (objclass == null) {
                objclass = LDAPVarsMap.get("COLLECTION_CLASS");
            }
            sb.append("(&");
            sb.append("(" + attrib + "=" + searchString + ")");
            sb.append("(objectclass=" + objclass + ")");
            sb.append(")");
        } else if (SEARCH_CONTAINER.equals(searchType)) {
            String attribs[] = {LDAPVarsMap.get("CONTAINER_PREFIX"), LDAPVarsMap.get("CONTAINER_PREFIX2")};
            String objclasses[] = {LDAPVarsMap.get("CONTAINER_CLASS"), LDAPVarsMap.get("CONTAINER_CLASS2")};
            sb.append("(|");
            for (int i = 0; i < objclasses.length; i++) {
                sb.append("(&");
                sb.append("(" + attribs[i] + "=" + searchString + ")");
                sb.append("(objectclass=" + objclasses[i] + ")");
                sb.append(")");

                // for iplanet they're the same, no point in making the search slower...
                if (LDAPVarsMap.get("CONTAINER_PREFIX").equals(LDAPVarsMap.get("CONTAINER_PREFIX2")) && 
                		LDAPVarsMap.get("CONTAINER_CLASS").equals(LDAPVarsMap.get("CONTAINER_CLASS2"))) {
                    break;
                }
            }
            sb.append(")");
        }

        return sb.toString();

    }

    public static final char LF = 10;
    public static final char CR = 13;
    public static final char SPACE = ' ';

    String createAdvSearchFilter(String advSearchString) {

        String buf = advSearchString.replace(LF, SPACE);
        buf = buf.replace(CR, SPACE);

//        String buf = removeCharFromString(advSearchString, LF);
//        buf = removeCharFromString(buf, CR);

        return buf;
    }

    String removeCharFromString(String str, char c) {

        StringBuffer b = new StringBuffer();
        StringTokenizer tokens = new StringTokenizer(str, new String(new char[]{c}));
        while (tokens.hasMoreTokens()) {
            b.append(tokens.nextToken());
        }
        return b.toString();
    }

    Vector getTargets(HttpServletRequest req,
                      NamingEnumeration nenum,
                      String searchBase)
            throws SystemException {
        Vector results = new Vector(100, 10);

        try {
            if (nenum != null) {
                String subBase = main.getSubBase();
                LDAPPolicyHelper policyFinder = new LDAPPolicyHelper(LDAPWebappUtils.getSubConn(req), subBase, main.getUsersInLDAP(), main.getDirType());
                LDAPName ldapName = LDAPWebappUtils.getSubConn(req).getParser();
                Name subName = ldapName.parse(subBase);
                while (nenum.hasMoreElements()) {
                    SearchResult sr = (SearchResult) nenum.next();
                    Attributes srAttrs = sr.getAttributes();
                    PropsBean entry = new PropsBean();
                    String resultName = sr.getName();

                    if (!((resultName == null) || "".equals(resultName))) {
                        String dn = LDAPName.unescapeJNDISearchResultName(resultName) + "," + searchBase;

                        if (DEBUG) {
                            System.out.println("Search Result sr= " + sr);
                            System.out.println("Attributes srAttrs= " + srAttrs);
                            System.out.println("resultName= " + resultName);
                        }

                        // set the dn for each element
                        entry.setValue(LDAPVarsMap.get("DN"), dn);

                        String objectclass = null;

                        if (srAttrs.size() > 0) {
                            objectclass = LDAPUtils.getObjectClass(srAttrs, LDAPVarsMap, tenantName, channel);
                        }

                        if (objectclass != null) {
                            entry.setValue(LDAPConstants.OBJECT_CLASS, objectclass);

                            String prefix = getPrefixFromObjectClass(objectclass, LDAPVarsMap);
                            String name = (String) LDAPUtils.getValue(srAttrs, prefix, tenantName, channel)
                                    .lastElement();
                            entry.setValue(prefix, name);

                            entry.setValue("type", LDAPUtils.objClassToTargetType(objectclass, LDAPVarsMap));

                            LDAPWebappUtils.setTargetAbleProperty(entry, false, LDAPVarsMap.get("USER_CLASS"));
                            
                            boolean isSubscription = false;

                            if (LDAPVarsMap.get("CONTAINER_CLASS").equals(objectclass)) {
                                Name dnName = ldapName.parse(dn);

                                // hide certain folders
                                if (dnName.startsWith(subName)) {
                                    // hide the subscription folder and the castanet
                                    // container that contains the config object
                                    // Note that the reason we hard-code 'castanet'
                                    // is because it doesn't make sense to store the
                                    // location of the config object IN the config object
                                    // itself- the value is hardcoded.
                                    isSubscription = true;
                                }
                            }
                            boolean isHidden = false;
                            // need to get hidden property from msf.txt
                            IConfig tenantConfig = tenant.getConfig();
                            if(null != tenantConfig.getProperty("marimba.ldap.cloud.hideentries")) {
                            	String[] cloudHideEntries = LDAPWebappUtils.getTokenizerProperty(tenantConfig.getProperty("marimba.ldap.cloud.hideentries"));
                            		if(null != cloudHideEntries) {
                            			Name inputDnName = ldapName.parse(dn);
                            			for(String cloudHideEntrie : cloudHideEntries) {
                            				Name hideName = ldapName.parse(cloudHideEntrie);
                            				if (inputDnName.equals(hideName)) {
                                                isHidden = true;
                                            }
                            		}
                            	}
                            }
                            if(!isHidden) {
                            	policyFinder.addTarget(dn, objectclass);
                            	results.addElement(entry);
                            }
                        } else {
                            if (DEBUG) {
                                System.out.println("Discarding dn, no objectclass match " + dn);
                            }
                        }
                    }
                }
                LDAPWebappUtils.setDirectTargetProperty(results, policyFinder, LDAPVarsMap);

            }
        } catch (NamingException ne) {
            if (DEBUG) {
                ne.printStackTrace();
            }

            LDAPUtils.classifyLDAPException(ne);
        }

        return results;
    }

    String[] getQueryAttr(IProperty subConfig, Map<String, String> LDAPVarsMap) {
        String[] lVarsAttr = {LDAPVarsMap.get("GROUP_PREFIX"), LDAPVarsMap.get("CONTAINER_PREFIX2")};

        String[] sConfigAttr = {LDAPConstants.CONFIG_USERIDATTR};
        String[] qryAttrs = new String[lVarsAttr.length + sConfigAttr.length];

        for (int i = 0; i < lVarsAttr.length; i++) {
            qryAttrs[i] = lVarsAttr[i];
        }

        for (int i = 0; i < sConfigAttr.length; i++) {
            qryAttrs[lVarsAttr.length + i] = subConfig.getProperty(sConfigAttr[i]);
        }

        return qryAttrs;
    }

    /**
     * REMIND
     *
     * @param subConfig REMIND
     *
     * @return REMIND
     */
    public static String[] getTargetAbleClasses(IProperty subConfig, IProperty marimbaConfig, Map<String, String>LDAPVarsMap) {
        String[] lVarsClasses = {LDAPVarsMap.get("GROUP_CLASS_UNIQUE"), LDAPVarsMap.get("GROUP_CLASS"), LDAPVarsMap.get("CONTAINER_CLASS"), LDAPVarsMap.get("CONTAINER_CLASS2")};

        String[] sConfigClasses = {LDAPConstants.CONFIG_GROUPCLASS, LDAPConstants.CONFIG_MACHINECLASS, LDAPConstants.CONFIG_USERCLASS};

        String[] qryAttrs = new String[lVarsClasses.length + sConfigClasses.length];

        for (int i = 0; i < lVarsClasses.length; i++) {
            qryAttrs[i] = lVarsClasses[i];
        }

        for (int i = 0; i < sConfigClasses.length; i++) {
            qryAttrs[lVarsClasses.length + i] = subConfig.getProperty(sConfigClasses[i]);
            if ( qryAttrs[lVarsClasses.length + i] == null) {
                qryAttrs[lVarsClasses.length + i] = marimbaConfig.getProperty(sConfigClasses[i]);                
            }
        }

        return qryAttrs;
    }

    private final String getPrefixFromObjectClass(String objectclass, Map<String, String>LDAPVarsMap) {
        //REMIND : do away with is maping in ldap_nav list in the future
        // changes need to be made in the jps page, ldap web app util parseresults
    	String[] machineClass = LDAPUtils.getLDAPVarArr("MACHINE_CLASS_PROP", LDAPVarsMap.get("DIRECTORY_TYPE"));
        if (LDAPVarsMap.get("CONTAINER_CLASS2").equals(objectclass)) {
            return LDAPVarsMap.get("CONTAINER_PREFIX2");
        } else if (LDAPVarsMap.get("USER_CLASS").equals(objectclass)) {
            return LDAPVarsMap.get("USER_ID");
        } else if (machineClass[0].equals(objectclass)) {
            return LDAPVarsMap.get("MACHINE_NAME");
        } else if (LDAPVarsMap.get("COLLECTION_CLASS").equals(objectclass)) {
            return LDAPVarsMap.get("COLLECTION_NAME");
        } else if (LDAPVarsMap.get("GROUP_CLASS_UNIQUE").equals(objectclass)) {
            return LDAPVarsMap.get("GROUP_PREFIX");
        } else if (LDAPConstants.DOMAIN_CLASS.equals(objectclass) || LDAPConstants.DOMAIN_CLASS2.equals(objectclass) || LDAPConstants.DOMAIN_CLASS3.equals(objectclass)) {
            return LDAPConstants.DOMAIN_PREFIX;
        } else {
            return LDAPVarsMap.get("GROUP_PREFIX");
        }
    }
        
    private PropsBean splitUserQuery(String searchQry) {    	
    	PropsBean searchBean = new PropsBean();
        if (null != searchQry && searchQry.trim().length() > 0) {
            if (searchQry.indexOf(QRY_DELIM) != -1) {
                String[] pairs = searchQry.split(QRY_DELIM);

                for (int i = 0; i < pairs.length; i++) {
                    int index = pairs[i].indexOf('=');
                    if (index != -1) {
                        String key = pairs[i].substring(0, index);
                        String value = pairs[i].substring(index+1);
                        searchBean.setProperty(key, value);
                    }
                }
            }
        }
        return searchBean;
    }
        
}
