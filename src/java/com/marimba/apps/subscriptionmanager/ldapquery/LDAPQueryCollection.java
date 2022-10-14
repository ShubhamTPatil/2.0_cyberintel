// Copyright 1997-2009, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.ldapquery;

import com.marimba.apps.subscription.collection.AbstractCollection;
import com.marimba.apps.subscription.collection.CollectionContext;
import com.marimba.apps.subscription.collection.CollectionException;
import com.marimba.apps.subscription.collection.LDAPSuperGroup;
import com.marimba.apps.subscription.common.LDAPEnv;
import com.marimba.apps.subscription.common.LDAPQueryInfo;
import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscription.common.SubUser;
import com.marimba.apps.subscription.common.intf.LogConstants;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscriptionmanager.TenantAttributes;
import com.marimba.apps.subscriptionmanager.compliance.core.ComplianceMain;
import com.marimba.apps.subscriptionmanager.compliance.core.DbSourceManager;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.castanet.schedule.Schedule;
import com.marimba.intf.db.IStatementPool;
import com.marimba.intf.msf.IRole;
import com.marimba.intf.msf.IRoleMap;
import com.marimba.intf.msf.acl.AclException;
import com.marimba.intf.msf.acl.AclStorageException;
import com.marimba.intf.msf.acl.IAclMgr;
import com.marimba.intf.util.IConfig;
import com.marimba.intf.util.IProperty;
import com.marimba.tools.ldap.*;
import com.marimba.tools.util.DebugFlag;

import javax.naming.*;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InvalidSearchFilterException;
import javax.naming.directory.SearchResult;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Core class that creates and performs operations on LDAP Query collection objects. During creation of the collection,
 * it does not run the collection to create groups immediately. Here dynamic groups are created either manually refreshing
 * the collection or refreshing the collection on schedule.It also Implements the new permission model in which
 * owners of every collection is distinguished.Say For Example "admin1" creates "colln1" and admin2 creates "colln2".
 * Here "admin1" cannot perform the operations(modify, delete, preview) on the collection object created by "admin2" (colln2).
 * Whereas the primary adminstrators can perform all operations on all LDAP query collection objects.
 *
 * @author Kumaravel. A
 * @version 1.2, 30/12/2004
 */
public class LDAPQueryCollection extends AbstractCollection
                                 implements IAppConstants,
                                            LogConstants {
    final static int DEBUG = DebugFlag.getDebug("SUB/LDAPQC/CORE");
    LDAPQryLogMgr    mgr;
    Vector           previewResults;
    static long      tzOffset;
    ComplianceMain compMain;
    protected LDAPQueryInfo       ldapQueryInfo;
    String PRIMARY_ADMIN_ROLE = "PrimaryAdmin";
    CollectionContext colCtx;

    /**
     * Constucts a new LDAPQueryCollection object.
     *
     * @param mgr REMIND
     * @param aclMgr REMIND
     * @param roleMap REMIND
     * @param dir REMIND
     * @param ldapQueryInfo REMIND
     * @param subConfig REMIND
     * @param ldapCfg REMIND
     * @param ldapEnv REMIND
     * @param centralized REMIND
     */
    public LDAPQueryCollection(CollectionContext context,
                               LDAPQryLogMgr  mgr,
                               IAclMgr        aclMgr,
                               IRoleMap       roleMap,
                               LDAPConnection dir,
                               LDAPQueryInfo  ldapQueryInfo,
                               IProperty      subConfig,
                               IConfig        ldapCfg,
                               LDAPEnv        ldapEnv,
                               boolean        centralized, ComplianceMain compMain) {
        // Passing null for mrbaConfig. If LDAPQC requires a property
        // valid instance needs to be passed
        super(context,
        	  aclMgr, roleMap, dir, ldapQueryInfo.getCollnName(),
        	  subConfig, null, ldapCfg,
        	  ldapEnv, centralized,
              subConfig.getProperty(LDAPConstants.CONFIG_LDAPCOLLECTIONBASE));
        this.colCtx = context;
        this.mgr       = mgr;
        this.compMain = compMain;
        this.ldapQueryInfo = ldapQueryInfo;
        previewResults = new Vector();
    }
    protected boolean setColln(LDAPQueryInfo ldapQueryInfo) {
        if(setName(ldapQueryInfo.getCollnName())) {
            this.ldapQueryInfo  = ldapQueryInfo;
            this.name = ldapQueryInfo.getCollnName();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Creates the new LDAP Query collection object in the LDAP container.
     *
     * @return true if collection object is created successfully.
     *
     * @throws NamingException REMIND
     * @throws NameNotFoundException REMIND
     */
    public boolean create() throws NamingException {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollection.create()");
        }

        // the first thing we do is check to see if ACLs are on, and if so that the user is a user in LDAP
        // if not we log an error and exit
        // we need to do this if we implement this in the GUI.
        if (aclsOn) {
            try {
                if ((owner = getCollectionOwner()) == null) {
                    mgr.log(LOG_LDAPQC_USERNOTINLDAP, LOG_MAJOR, getOwner());
                    return false;
                }
            } catch (Exception e) {
                mgr.log(LOG_LDAPQC_USERNOTINLDAP, LOG_MAJOR, null, e);
                return false;
            }
        }
        String dirType = vendor;
        Map<String, String> LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(dirType);
        if (isVendor(VENDOR_AD)) {
            // search rootDSE for schema container
            String[]   ids = { "schemaNamingContext" };
            Attributes attrs = dir.getServerAttributes(ids);
            Attribute  attr = attrs.get(ids [0]);

            if (attr != null) {
                String schemaContainer = (String) attr.get();

				// search schemaContainer for Computer classSchema, and
				// return its defaultObjectCategory attribute
				LDAPSearchFilter filter = new LDAPSearchFilter(
						LDAPSearchFilter.AND);

                // including supporting machine classes in the query
                LDAPSearchFilter macFilter = new LDAPSearchFilter(LDAPSearchFilter.OR);
                String[] machineClass = LDAPUtils.getLDAPVarArr("MACHINE_CLASS_PROP", dirType);
                for (int i=0; i<machineClass.length; i++) {
                    macFilter.addFilter(LDAPVarsMap.get("MACHINE_NAME"), machineClass[i]);
                }
				filter.addChild(macFilter);
				filter.addFilter(LDAPConstants.OBJECT_CLASS, LDAPConstants.CLASS_SCHEMA);

                String[] values = null;

                try {
                    values = dir.searchAndList(filter.toString(), "defaultObjectCategory", null, false, schemaContainer, true);

                    if ((values != null) && (values.length > 0)) {
                        computerObjectCategory = values [0];
                        useObjectCategory      = true;

                        if (DEBUG > 1) {
                            System.out.println("LDAPCollection: Optimize searches: " + computerObjectCategory);
                        }
                    }
                } catch (LDAPException e) {
                    throw e.getRootException();
                }
            }
                if (centralized) {
                    globalCatalog = getGlobalCatalog();

                    if (DEBUG > 1) {
                        System.out.println("LDAPCollection: Connected to GC: " + globalCatalog.toString());
                    }
                }
        }

        // Set attributes
        Vector attrs = new Vector();
        attrs.addElement(format(LDAPConstants.OBJECT_CLASS, LDAPConstants.TOP_CLASS));
        attrs.addElement(format(LDAPConstants.OBJECT_CLASS, LDAPVarsMap.get("GROUP_CLASS")));
        attrs.addElement(format(LDAPConstants.OBJECT_CLASS, LDAPVarsMap.get("COLLECTION_CLASS")));
        attrs.addElement(format(LDAPConstants.OBJECT_CLASS, LDAPVarsMap.get("COLLECTION_CLASS")));

        if (desc != null) {
            attrs.addElement(format(LDAPVarsMap.get("GROUP_DESCRIPTION"), desc));
        }

        if (isVendor(VENDOR_AD)) {
            attrs.addElement(format(LDAPVarsMap.get("GROUP_CREATE_ID"), sanitize(cn)));
        }

        if (isVendor(VENDOR_AD) || isVendor(VENDOR_ADAM)) {
            if ((ldapQueryInfo.getCollnName() != null) || (ldapQueryInfo.getQuery() != null) || (ldapQueryInfo.getSchedule() != null)) {
                attrs.addElement(format(LDAPVarsMap.get("AD_GROUP_QUERY_SCHEDULE"), ldapQueryInfo.getCollnName() + LDAPQC_DELIM + ldapQueryInfo.getQuery() + LDAPQC_DELIM + ldapQueryInfo.getFilter() + LDAPQC_DELIM + ldapQueryInfo.getSchedule()));
            }

            if (ldapQueryInfo.getSearchBase() != null) {
                attrs.addElement(format(LDAPVarsMap.get("AD_GROUP_QUERY_SEARCHBASE"), ldapQueryInfo.getSearchBase()));
            } else {
                attrs.addElement(format(LDAPVarsMap.get("AD_GROUP_QUERY_SEARCHBASE"), getBaseDn()));
            }

            String run = null;
            String lastrun = null;
            String nextrun = null;

            Calendar cal = new GregorianCalendar(TimeZone.getDefault());
            tzOffset = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);
            // only when the schedule is available, next time is calculated
            if (ldapQueryInfo.getSchedule() !=null ) {
                    if (!"never".equalsIgnoreCase(ldapQueryInfo.getSchedule()) ) {
                        Schedule sched = Schedule.readSchedule(ldapQueryInfo.getSchedule());
                        long nextTime = outMS(sched.nextTime(inMS(new java.util.Date().getTime()), inMS(new java.util.Date().getTime())));
                        Date d = new Date(nextTime);
                        nextrun = getLDAPTime(d);
                    }
            }
            run = lastrun + LDAPQC_DELIM + nextrun;
            attrs.addElement(format(LDAPVarsMap.get("AD_GROUP_QUERY_DATETIME_NEXTRUN"), run));
        }

        if (isVendor(VENDOR_AD) || isVendor(VENDOR_ADAM)) {
            attrs.addElement(format(LDAPVarsMap.get("GROUP_CREATE_TYPE"), LDAPVarsMap.get("GROUP_TYPE_LOCAL_DOMAIN")));
            attrs.addElement(format(LDAPVarsMap.get("AD_GROUP_CREATEDBY"), ldapQueryInfo.getCreatedBy()));
        } else {
            attrs.addElement(format(LDAPVarsMap.get("GROUP_CREATEDBY"), ldapQueryInfo.getCreatedBy()));
            attrs.addElement(format(LDAPVarsMap.get("GROUP_QUERY_SCHEDULE"), ldapQueryInfo.getCollnName() + LDAPQC_DELIM + ldapQueryInfo.getQuery() + LDAPQC_DELIM + ldapQueryInfo.getFilter() + LDAPQC_DELIM + ldapQueryInfo.getSchedule()));
            attrs.addElement(format(LDAPVarsMap.get("GROUP_QUERY_SEARCHBASE"), ldapQueryInfo.getSearchBase()));

            String run = null;
            String nextrun = null;
            String lastrun = null;

            Calendar cal = new GregorianCalendar(TimeZone.getDefault());
            tzOffset = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);
            // only when the schedule is available, next time is calculated
            if (ldapQueryInfo.getSchedule() !=null ) {
                    if (!"never".equalsIgnoreCase(ldapQueryInfo.getSchedule()) ) {
                        Schedule sched = Schedule.readSchedule(ldapQueryInfo.getSchedule());
                        long nextTime = outMS(sched.nextTime(inMS(new java.util.Date().getTime()), inMS(new java.util.Date().getTime())));
                        Date d = new Date(nextTime);
                        nextrun = getLDAPTime(d);
                    }
            }

            run = lastrun + LDAPQC_DELIM + nextrun;
            attrs.addElement(format(LDAPVarsMap.get("GROUP_QUERY_DATETIME_NEXTRUN"), run));
        }

        String[] attrStr = new String[attrs.size()];
        attrs.copyInto(attrStr);

        // creates collection object here.
        dir.createObject(dn, attrStr, false);

        if (DEBUG > 1) {
            System.out.println("LDAPCollection: Created " + getName());
        }

        return true;
    }

    /**
     * Previews the output by executing the LDAP Query in the given collection object.
     *
     * @return REMIND
     *
     * @throws NamingException REMIND
     * @throws NameNotFoundException REMIND
     */
    public Vector previewExtsgQuery() throws AclStorageException, AclException, LDAPException, NameNotFoundException,InvalidNameException, NamingException, SQLException {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollection.previewExtsgQuery()");
        }

        String[] ids = null;
        String dirType = vendor;
        Map<String, String> LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(dirType);
        if (isVendor(VENDOR_AD) || isVendor(VENDOR_ADAM)) {
            ids = new String[] { LDAPVarsMap.get("AD_GROUP_QUERY_NAME"), LDAPVarsMap.get("AD_GROUP_CREATEDBY"), 
            		LDAPVarsMap.get("AD_GROUP_QUERY_SEARCHBASE") };
        } else {
            ids = new String[] { LDAPVarsMap.get("GROUP_QUERY_NAME"), LDAPVarsMap.get("GROUP_CREATEDBY"), 
            		LDAPVarsMap.get("GROUP_QUERY_SEARCHBASE") };
        }

        Attributes attrs;
        Attribute  attr;
        String     query = null;
        String searchBase = null;

            attrs = dir.getAttributes(dn, ids);
            attr  = attrs.get(ids [0]);

            if (attr != null) {
                query = (String) attr.get();
            }

            attr  = attrs.get(ids [2]);
            if (attr != null) {
                searchBase = (String) attr.get();
                if("null".equals(searchBase)) {
                    searchBase = null;
                }
            }
            String [] arr = splitByPattern(query, LDAPQC_DELIM, false );
            ldapQueryInfo.setQuery(arr [1]);
            ldapQueryInfo.setFilter(arr [2]);
            ldapQueryInfo.setSearchBase(searchBase);

            // retrieving the userid from the created by attribute.
            attr = attrs.get(ids [1]);

            if (attr != null) {
                ldapQueryInfo.setCreatedBy((String) attr.get());
            }

        return previewQuery();
    }
    /**
     * Previews the output by executing the LDAP Query before creating the collection object.
     *
     * @return REMIND
     *
     * @throws NamingException REMIND
     */
    public Vector previewQuery() throws AclStorageException, AclException, LDAPException, NameNotFoundException,InvalidNameException, NamingException, SQLException {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollection.previewQuery()");
        }

        String[] searchAttrs = null;
        String dirType = vendor;
        Map<String, String> LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(dirType);
        if (isVendor(VENDOR_AD) || isVendor(VENDOR_ADAM)) {
            searchAttrs = new String[] {
                              LDAPConstants.OBJECT_CLASS, LDAPVarsMap.get("CONTAINER_PREFIX"), LDAPVarsMap.get("CONTAINER_PREFIX2"), LDAPVarsMap.get("GROUP_PREFIX"), 
                              LDAPVarsMap.get("MACHINE_NAME"),
                              LDAPVarsMap.get("USER_ID"), LDAPVarsMap.get("DN")
                          };
        } else {
            searchAttrs = new String[] {
                              LDAPConstants.OBJECT_CLASS, LDAPVarsMap.get("CONTAINER_PREFIX"), LDAPVarsMap.get("CONTAINER_PREFIX2"), 
                              LDAPVarsMap.get("GROUP_PREFIX"), LDAPVarsMap.get("MACHINE_NAME"),
                              LDAPVarsMap.get("USER_ID")
                          };
        }



        if (isVendor(VENDOR_AD)) {
            if (centralized) {
                globalCatalog = getGlobalCatalog();
            }
        }
        boolean        AD = (isVendor(VENDOR_AD) && centralized && (globalCatalog != null));
        LDAPConnection conn = (AD ? globalCatalog
                               : dir);
        Vector  searchBases = new Vector();

        if ( ldapQueryInfo.getSearchBase() != null ) {
            searchBases.add(ldapQueryInfo.getSearchBase());
        } else {
            if(isVendor(VENDOR_AD) ) {
                searchBases.addAll(getTreeRoots());
            } else {
                searchBases.add(getBaseDn());
            }
        }


        if (ldapQueryInfo.getQuery() == null) {
            mgr.log(LOG_LDAPQC_QUERY_NULL, LOG_WARNING, null);
        }

        String newQuery = getNewQuery(ldapQueryInfo.getQuery());
        Vector  membersToGroup = new Vector();

        if( searchBases.size() ==0 ) {
           throw new NamingException("Unable to determine the Search Base ");
        }

        String  macqrystart = "select distinct name from inv_machine where upper(name) in('";
        String  perqrystart = "select distinct username from inv_spm_person where upper(username) in('";
        String  macDBquery  = null;
        String  perDBquery  = null;
        List    resultsList = null;
        String  macAttr     = null;
        String  perAttr     = null;

        perAttr = LDAPVarsMap.get("USER_ID");
        macAttr = LDAPVarsMap.get("MACHINE_NAME");
        String machineClass[] = LDAPUtils.getLDAPVarArr("MACHINE_CLASS_PROP", LDAPVarsMap.get("DIRECTORY_TYPE"));
        String ldapQCPrefix[] = LDAPUtils.getLDAPVarArr("LDAPQC_PREFIX_ORDER_PROP", LDAPVarsMap.get("DIRECTORY_TYPE"));
        Iterator iteratorSearchBases = searchBases.iterator();

        try {
            while (iteratorSearchBases.hasNext()) {

                String srchBase =(iteratorSearchBases.next()).toString();
                LDAPPagedSearch paged = LDAPPagedSearch.getLDAPPagedSearch(conn, newQuery, (String[]) searchAttrs, srchBase, false, ldapQCPrefix);

                int     startIndex = 0;

                paged.setPageSize(DEFAULT_PAGE_SIZE);
                if (DEBUG > 4) {
                    System.out.println(" LDAPPagedSearch ");
                    System.out.println("------------------");
                    System.out.println("Query  =" + newQuery);
                    System.out.println("SearchBase  =" + srchBase);
                }
                boolean  pagesExist = false;
                if (isVendor(VENDOR_AD) || isVendor(VENDOR_ADAM) ) {
                    pagesExist = paged.hasMoreResults() || (startIndex < paged.getSize());
                } else {
                    pagesExist = (startIndex < paged.getSize());
                }
                while(pagesExist) {

                    resultsList = paged.getPage(startIndex);
                    startIndex = startIndex + DEFAULT_PAGE_SIZE;

                    if (isVendor(VENDOR_AD) || isVendor(VENDOR_ADAM) ) {
                        pagesExist = paged.hasMoreResults() || (startIndex < paged.getSize());
                    } else {
                        pagesExist = (startIndex < paged.getSize());
                    }
                    HashMap macMap = new HashMap();
                    HashMap perMap = new HashMap();

                    String objectclass = null;
                    Iterator iterator = resultsList.iterator();

                    while(iterator.hasNext())  {
                        SearchResult sr = (SearchResult) iterator.next();
                        Attributes srAttrs = sr.getAttributes();

                        String dn = null;
                        String       resultName = sr.getName();

                        if (!((resultName == null) || "".equals(resultName))) {
                            dn = LDAPName.unescapeJNDISearchResultName(resultName) + "," + srchBase;
                        }

                        if (srAttrs.size() > 0) {
                            objectclass = LDAPUtils.getObjectClass(srAttrs, LDAPVarsMap, colCtx.getTenantName(), colCtx.getChannel());
                        }

                        if (objectclass == null) {
                            // the objectclass of the result was invalid, skip it
                            continue;
                        }
                        objectclass = objectclass.toLowerCase();
                        if(objectclass.equals(LDAPVarsMap.get("USER_CLASS"))) {
                             Attribute  attr =srAttrs.get(perAttr);
                             String key =((String)attr.get()).toUpperCase();
                             perMap.put(key, dn);
                        }
                        for (int i=0; i<machineClass.length; i++) {
                            if (objectclass.equals(machineClass[i])) {
                                 Attribute  attr =srAttrs.get(macAttr);
                                 String key =((String)attr.get()).toUpperCase();
                                 macMap.put(key, dn);
                            }
                        }
                    }
                    if (DEBUG > 4) {
                        if(macMap.size() > 0 ) {
                            System.out.println("Machine Map ");
                            System.out.println("------------");
                            System.out.println("Total ="+ macMap.size());
                            dispMap(macMap);
                        } else {
                            System.out.println("Machine map contains  " + macMap.size());
                        }
                        if(perMap.size() > 0 ) {
                            System.out.println("Person Map ");
                            System.out.println("------------");
                            System.out.println("Total ="+ perMap.size());
                            dispMap(perMap);
                        } else {
                            System.out.println("Persone map contains  " + perMap.size());
                        }
                    }
                    if(macMap.size() > 0 ) {
                        macDBquery = getDBQuery(macqrystart, macMap);
                    }
                    if(perMap.size() > 0 ) {
                        perDBquery = getDBQuery(perqrystart, perMap);
                    }
                    List maclist = null;
                    List perlist = null;

                    if (DEBUG > 4) {
                        System.out.println("Results DB Queries");
                        System.out.println("------------------");
                        if(macDBquery != null) {
                                System.out.println("Mac Query ->" + macDBquery );
                        }
                        if(perDBquery != null) {
                                System.out.println("Per Query ->" + perDBquery );
                        }
                    }
                    if(macDBquery != null) {
                        maclist = getMatchingTargets(macDBquery);
                        if(DEBUG >4 ) {
                            System.out.println("Machine list");
                            dispList(maclist);
                        }
                    }
                    if(perDBquery != null) {
                        perlist = getMatchingTargets(perDBquery);
                        if(DEBUG > 4 ) {
                            System.out.println("Person list");
                            dispList(perlist);
                        }
                    }

                    if( maclist != null ) {
                        for( int i=0; i< maclist.size(); i++) {
                            if(macMap.containsKey(((String)maclist.get(i)).toUpperCase())) {
                                   membersToGroup.addElement(macMap.get(((String)maclist.get(i)).toUpperCase()));
                            }
                        }
                    }
                    if( perlist != null ) {
                        for( int i=0; i< perlist.size(); i++) {
                            if(perMap.containsKey(((String)perlist.get(i)).toUpperCase())) {
                                   membersToGroup.addElement(perMap.get(((String)perlist.get(i)).toUpperCase()));
                            }
                        }
                    }

                }
          }
        } catch (InvalidSearchFilterException ise) {

            mgr.log(LOG_LDAPQC_INVALID_SEARCHFILTER, LOG_WARNING, "Invalid search filter", ise);
            throw ise;
        } catch(CommunicationException ce) {
            if(DEBUG >5 ) {
                ce.printStackTrace();
            }
            throw ce;
        } catch (NameNotFoundException nnfe) {
            if(DEBUG >5 ) {
                nnfe.printStackTrace();
            }
            throw  nnfe;
        } catch (InvalidNameException ine) {
            if(DEBUG >5 ) {
                ine.printStackTrace();
            }
            throw ine;
        } catch (NamingException ne) {
            if(DEBUG >5 ) {
                ne.printStackTrace();
            }
            throw ne;
        }
        if (DEBUG > 4) {
            System.out.println(" Matching Database members ");
            System.out.println(" Total "+ membersToGroup.size());
            dispVector(membersToGroup);
        }

        Vector finalResult = new Vector();
        try {
            finalResult.addAll(getAllowedTargets(membersToGroup));
            if (DEBUG > 4) {
                System.out.println(" Matching Database members with authenticated users ");
                System.out.println(" Total "+ finalResult.size());
                dispVector(finalResult);
            }
        } catch (AclStorageException acse) {
            mgr.log(LOG_LDAPQC_ACL_STORAGE_FAILED, LOG_MAJOR, null , acse);
            throw acse;
        } catch (LDAPException ldape) {
            mgr.log(LOG_LDAPQC_ADD_MEMBERS_FAILED, LOG_MAJOR, null, ldape);
            throw ldape;
        }
        return finalResult;
    }

    /**
     * Lists the details of a particular collection.
     *
     * @return REMIND
     *
     * @throws NamingException REMIND
     */
    public Vector list() throws NamingException {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollection.list()");
        }
        String[] searchAttrs = null;
        String dirType = vendor;
        Map<String, String> LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(dirType);
        if (isVendor(VENDOR_AD) || isVendor(VENDOR_ADAM)) {
            searchAttrs = new String[] {
            		LDAPVarsMap.get("AD_GROUP_QUERY_SCHEDULE"), LDAPVarsMap.get("AD_GROUP_CREATEDBY"), 
            		LDAPVarsMap.get("AD_GROUP_QUERY_SEARCHBASE"),
            		LDAPVarsMap.get("AD_GROUP_QUERY_DATETIME_NEXTRUN")
                          };
        } else {
            searchAttrs = new String[] {
            		LDAPVarsMap.get("GROUP_QUERY_SCHEDULE"), LDAPVarsMap.get("GROUP_CREATEDBY"), 
            		LDAPVarsMap.get("GROUP_QUERY_SEARCHBASE"), LDAPVarsMap.get("GROUP_QUERY_DATETIME_NEXTRUN")
                          };
        }

        String searchBase = null;

        searchBase = subConfig.getProperty(LDAPConstants.CONFIG_LDAPCOLLECTIONBASE);

        NamingEnumeration nenum = null;

        Vector        results = new Vector();
        try {
            StringBuffer buffer = new StringBuffer();
            //String escCollnName = LDAPName.escapeComponentValue(ldapQueryInfo.getCollnName());
            String escCollnName        = LDAPSearchFilter.escapeComponentValue(ldapQueryInfo.getCollnName());
            buffer.append("(&(" + LDAPVarsMap.get("COLLECTION_NAME") + "=" + escCollnName + ")");
            if (isVendor(VENDOR_AD) || isVendor(VENDOR_ADAM)) {
               buffer.append("("+ LDAPConstants.OBJECT_CLASS + "=marimbaCom1996-Castanet-SubscriptionCollection))");
            } else {
                 buffer.append("("+ LDAPConstants.OBJECT_CLASS +"=mrbacollection))");
            }

            nenum = dir.search(  buffer.toString(), searchAttrs, searchBase, false);

            Attribute     attr = null;
            Attributes    srAttrs = null;
            SearchResult  sr = null;
            LDAPQueryInfo ldapQueryInfo = null;
            String        queryname = null;
            String        commanStr = null;
            String        query = null;
            String        querySchedule = null;
            String        createdBy = null;
            String        lastrun = null;
            String        nextrun = null;
            String        filter = null;
            Date          lastrunDate = null;
            Date          nextrunDate = null;

                if (nenum != null) {
                    while (nenum.hasMoreElements()) {
                        queryname     = null;
                        commanStr     = null;
                        query         = null;
                        querySchedule = null;
                        createdBy     = null;
                        lastrun       = null;
                        nextrun       = null;
                        filter        = null;
                        lastrunDate   = null;
                        nextrunDate   = null;

                        sr      = (SearchResult) nenum.next();
                        srAttrs = sr.getAttributes();

                        attr = srAttrs.get(searchAttrs [0]);

                        if (attr != null) {
                            commanStr = (String) attr.get();

                            String [] str = splitByPattern(commanStr, LDAPQC_DELIM, false);
                            queryname      = str[0];
                            query          = str[1];
                            filter         = str[2];
                            querySchedule  = str[3];
                        }

                        attr = srAttrs.get(searchAttrs [1]);

                        if (attr != null) {
                            createdBy = (String) attr.get();
                        }

                        attr = srAttrs.get(searchAttrs [2]);

                        if (attr != null) {
                            searchBase = (String) attr.get();
                        }

                        attr = srAttrs.get(searchAttrs [3]);

                        if (attr != null) {
                            commanStr = (String) attr.get();
                            String [] str = splitByPattern(commanStr , LDAPQC_DELIM , false );
                            lastrun = str[0];
                            nextrun = str[1];
                        }

                        if (!"null".equals(lastrun)) {
                            lastrunDate = parseSimpleDate(lastrun);
                            lastrun     = lastrunDate.toString();
                        }

                        if (!"null".equals(nextrun)) {
                            nextrunDate = parseSimpleDate(nextrun);
                            nextrun     = nextrunDate.toString();
                        }

                        ldapQueryInfo = new LDAPQueryInfo(queryname, query, searchBase, querySchedule, createdBy, lastrun, nextrun, filter);
                        results.addElement(ldapQueryInfo);
                    }
                } else {
                    results = null;
                }
        } catch (NamingException ne) {

            throw ne;
        }
        return results;
    }

    /**
     * Lists all collection objects in the collection container.
     *
     * @return REMIND
     *
     * @throws NamingException REMIND
     */
    public Vector listAll() throws NamingException {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollection.listAll()");
        }
        String dirType = vendor;
        String[] searchAttrs = null;
        Map<String, String> LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(dirType);
        if (isVendor(VENDOR_AD) || isVendor(VENDOR_ADAM)) {
            searchAttrs = new String[] {
            		LDAPVarsMap.get("AD_GROUP_QUERY_SCHEDULE"), LDAPVarsMap.get("AD_GROUP_CREATEDBY"), 
            		LDAPVarsMap.get("AD_GROUP_QUERY_SEARCHBASE"),
            		LDAPVarsMap.get("AD_GROUP_QUERY_DATETIME_NEXTRUN")
                          };
        } else {
            searchAttrs = new String[] {
            		LDAPVarsMap.get("GROUP_QUERY_SCHEDULE"), LDAPVarsMap.get("GROUP_CREATEDBY"), 
            		LDAPVarsMap.get("GROUP_QUERY_SEARCHBASE"), LDAPVarsMap.get("GROUP_QUERY_DATETIME_NEXTRUN")
                          };
        }

        String searchBase = null;
        searchBase = subConfig.getProperty(LDAPConstants.CONFIG_LDAPCOLLECTIONBASE);

        NamingEnumeration nenum = null;

        String            searchFilter = null;

        if (isVendor(VENDOR_AD) || isVendor(VENDOR_ADAM)) {
            searchFilter = "(objectclass=marimbaCom1996-Castanet-SubscriptionCollection)";
        } else {
            searchFilter = "(objectclass=mrbacollection)";
        }

        Vector        results = new Vector();

        try {
            nenum = dir.search(searchFilter, searchAttrs, searchBase, false);

            Attribute     attr = null;
            Attributes    srAttrs = null;
            SearchResult  sr = null;
            LDAPQueryInfo ldapQueryInfo = null;
            String        queryname = null;
            String        commanStr = null;
            String        query = null;
            String        querySchedule = null;
            String        createdBy = null;
            String        lastrun = null;
            String        nextrun = null;
            String        filter = null;
            Date          lastrunDate = null;
            Date          nextrunDate = null;

                if (nenum != null) {
                    while (nenum.hasMoreElements()) {
                        queryname     = null;
                        commanStr     = null;
                        query         = null;
                        querySchedule = null;
                        createdBy     = null;
                        lastrun       = null;
                        nextrun       = null;
                        filter        = null;
                        lastrunDate   = null;
                        nextrunDate   = null;

                        sr      = (SearchResult) nenum.next();
                        srAttrs = sr.getAttributes();

                        attr = srAttrs.get(searchAttrs [0]);

                        if (attr != null) {
                            commanStr = (String) attr.get();
                            String [] str = splitByPattern(commanStr, LDAPQC_DELIM, false);
                            queryname      = str[0];
                            query          = str[1];
                            filter         = str[2];
                            querySchedule  = str[3];
                        }

                        attr = srAttrs.get(searchAttrs [1]);

                        if (attr != null) {
                            createdBy = (String) attr.get();
                        }

                        attr = srAttrs.get(searchAttrs [2]);

                        if (attr != null) {
                            searchBase = (String) attr.get();
                        }

                        attr = srAttrs.get(searchAttrs [3]);

                        if (attr != null) {
                            commanStr = (String) attr.get();
                            String [] str = splitByPattern(commanStr , LDAPQC_DELIM , false );
                            lastrun = str[0];
                            nextrun = str[1];
                        }

                        if (!"null".equals(lastrun)) {
                            lastrunDate = parseSimpleDate(lastrun);
                            lastrun     = lastrunDate.toString();
                        }

                        if (!"null".equals(nextrun)) {
                            nextrunDate = parseSimpleDate(nextrun);
                            nextrun     = nextrunDate.toString();
                        }

                        ldapQueryInfo = new LDAPQueryInfo(queryname, query, searchBase, querySchedule, createdBy, lastrun, nextrun, filter);
                        results.addElement(ldapQueryInfo);
                    }
                } else {
                    results = null;
                }
        } catch (NamingException ne) {

            throw ne;
        }
        return results;
    }


    /**
     * Refreshes the collection and create dynamic groups. Attributes last runtime is updated with the
     * current time and next runtime is calculated based on the schedule. If the schedule is set to never, then
     * next runtime would be null.
     *
     * @return true if the collection is refreshed successfully.
     *
     * @throws NamingException REMIND
     * @throws NameNotFoundException REMIND
     * @throws CollectionException
     */
    public boolean runQuery() throws AclStorageException, AclException ,LDAPException,NameNotFoundException, InvalidNameException, NamingException, SQLException, CollectionException {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollection.runQuery()");
        }
        String dirType = vendor;
        Map<String, String> LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(dirType);
        if (isVendor(VENDOR_AD)) {
            // search rootDSE for schema container
            String[]   ids = { "schemaNamingContext" };
            Attributes attrs = dir.getServerAttributes(ids);
            Attribute  attr = attrs.get(ids [0]);

            if (attr != null) {
                String schemaContainer = (String) attr.get();

                // search schemaContainer for Computer classSchema, and
                // return its defaultObjectCategory attribute
                LDAPSearchFilter filter = new LDAPSearchFilter(LDAPSearchFilter.AND);
                                // including supporting machine classes in the query
                LDAPSearchFilter macFilter = new LDAPSearchFilter(
                        LDAPSearchFilter.OR);
                String machineClass[] = LDAPUtils.getLDAPVarArr("MACHINE_CLASS_PROP", dirType);
                for (int i=0; i<machineClass.length; i++) {
                    macFilter.addFilter(LDAPVarsMap.get("MACHINE_NAME"), machineClass[i]);
                }
                filter.addChild(macFilter);
                filter.addFilter(LDAPConstants.OBJECT_CLASS, LDAPConstants.CLASS_SCHEMA);

                String[] values = null;

                try {
                    values = dir.searchAndList(filter.toString(), "defaultObjectCategory", null, false, schemaContainer, true);

                    if ((values != null) && (values.length > 0)) {
                        computerObjectCategory = values [0];
                        useObjectCategory      = true;

                        if (DEBUG > 1) {
                            System.out.println("LDAPCollection: Optimize searches: " + computerObjectCategory);
                        }
                    }
                } catch (LDAPException e) {
                    throw e.getRootException();
                }
            }

            try {
                if (centralized) {
                    globalCatalog = ldapEnv.createSubConfigConn();

                    if (DEBUG > 1) {
                        System.out.println("LDAPCollection: Connected to GC: " + globalCatalog.toString());
                    }
                }
            } catch (Exception e) {
                mgr.log(LOG_LDAPQC_CONN_TO_GLOBALCATAGLOG_FAILED, LOG_MAJOR, null);
            }
        }
            // Is the collection is running for the first time?
            if(!load()) {
                // arbitrarily choose  write supergroups
                writeGroup = new LDAPSuperGroup(context, dir, name + EXT0, base, maxGroupSize);
            }
        // create and overwrite
        writeGroup.create();
        writing = true;
        String[] attrs1 = new String[1];

        String[] ids = null;

        if (isVendor(VENDOR_AD) || isVendor(VENDOR_ADAM)) {
            ids = new String[] { LDAPVarsMap.get("AD_GROUP_QUERY_DATETIME_NEXTRUN"), LDAPVarsMap.get("AD_GROUP_QUERY_SCHEDULE") };
        } else {
            ids = new String[] { LDAPVarsMap.get("GROUP_QUERY_DATETIME_NEXTRUN"), LDAPVarsMap.get("GROUP_QUERY_SCHEDULE") };
        }

        Attributes attrs;
        String     lastrun = null;
        attrs = dir.getAttributes(dn, ids);

        Attribute attr;
        Date      lastrunDate = null;
        //Get the last run time from the current LDAPcollection object
        attr = attrs.get(ids [0]);

        String commanStr = null;
        String nextrun = null;
        String run = null;

        if (attr != null) {
            commanStr = (String) attr.get();

                String [] str = splitByPattern(commanStr , LDAPQC_DELIM , false );
                lastrun = str[0];
                nextrun = str[1];
        }
        //Get the Schedule from the current LDAPcollection object
        attr = attrs.get(ids [1]);

        String  schedule = null;
        boolean hasSchedule = false;

        if (attr != null) {
            String          querynamesched = (String) attr.get();
            String [] arr = splitByPattern(querynamesched ,LDAPQC_DELIM, false);
            schedule = arr [3];

            if (schedule == null || "null".equals(schedule) || "never".equals(schedule)) {
                hasSchedule = false;
            } else {
                hasSchedule = true;
            }
        }
        lastrunDate = new Date();

        Calendar cal = new GregorianCalendar(TimeZone.getDefault());
        tzOffset = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);
        // only when the schedule is available, next time is calculated
        if (hasSchedule) {
            Schedule sched = Schedule.readSchedule(schedule);

            try {
                long nextTime = outMS(sched.nextTime(inMS(lastrunDate.getTime()), inMS(new java.util.Date().getTime())));
                Date d = new Date(nextTime);
                lastrun = getLDAPTime(lastrunDate);
                nextrun = getLDAPTime(d);
                run     = lastrun + LDAPQC_DELIM + nextrun;
            } catch (Exception e) {
                mgr.log(0,LOG_WARNING,null);
            }

            attrs1 [0] = format(ids [0], run);
            dir.modifyObject(dn, attrs1);
        } else {
            lastrun    = getLDAPTime(lastrunDate);
            run        = lastrun + LDAPQC_DELIM + nextrun;
            attrs1 [0] = format(ids [0], run);
            dir.modifyObject(dn, attrs1);
        }

        Vector v = new Vector();
        v.addAll(previewExtsgQuery());

        Vector members = new Vector();
        String mdn = null;

        for (Enumeration enumMembers = v.elements(); enumMembers.hasMoreElements();) {
            mdn = (String) enumMembers.nextElement();
            members.addElement(format(LDAPVarsMap.get("GROUP_MEMBER"), mdn));
        }

            if (addMembers(v)) {
                mgr.log(LOG_LDAPQC_ADD_MEMBERS_SUCCEEDED, LOG_MAJOR, null);
                return true;
            } else {
                mgr.log(LOG_LDAPQC_ADD_MEMBERS_FAILED, LOG_MAJOR, null);
                return false;
            }
    }

    /**
     * Deletes the Super groups only
     *
     * @return true if the deletion is succeeded.
     *
     * @throws NamingException REMIND
     */
    public boolean delete() throws NamingException {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollection.delete()");
        }

        LDAPSuperGroup g = new LDAPSuperGroup(context, dir, name + EXT0, base, maxGroupSize);
        g.delete();

        g = new LDAPSuperGroup(context, dir, name + EXT1, base, maxGroupSize);
        g.delete();

        return true;
    }

    /**
     * Deletes the collection and its super groups.
     *
     * @return REMIND
     *
     * @throws NamingException REMIND
     * @throws NameNotFoundException REMIND
     */
    public boolean deleteObject() throws NamingException {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollection.deleteObject()");
        }
        if (super.delete()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Modifies the existing collection object for query, schedule, and  filter. If the schedule is set to never, then
     * nextrun time attribute will be set as null. If not, the next run time is calculated based on the last run time
     * of the query collection.
     *
     * @return REMIND
     *
     * @throws NamingException REMIND
     * @throws NameNotFoundException REMIND
     */
    public boolean modify() throws NamingException {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollection.modify()");
        }

        String[] ids = null;
        String dirType = vendor;
        Map<String, String> LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(dirType);
        if (isVendor(VENDOR_AD) || isVendor(VENDOR_ADAM)) {
            ids = new String[] { LDAPVarsMap.get("AD_GROUP_QUERY_SCHEDULE"), LDAPVarsMap.get("AD_GROUP_QUERY_SEARCHBASE"), 
            		LDAPVarsMap.get("AD_GROUP_QUERY_DATETIME_NEXTRUN") };
        } else {
            ids = new String[] { LDAPVarsMap.get("GROUP_QUERY_SCHEDULE"), LDAPVarsMap.get("GROUP_QUERY_SEARCHBASE"), 
            		LDAPVarsMap.get("GROUP_QUERY_DATETIME_NEXTRUN") };
        }

        Attributes attrs= null;
        Attribute attr;

        attrs = dir.getAttributes(dn, ids);

        //Get the Schedule from the current LDAPcollection object
        attr = attrs.get(ids [0]);
        String          commonStr = (String) attr.get();
        String [] str = splitByPattern(commonStr, LDAPQC_DELIM, false);
        String   queryname      = str[0];
        String   query          = str[1];
        String   filter         = str[2];
        String   schedule       = str[3];

        if (DEBUG > 4) {
            System.out.println("Existing query " + queryname + LDAPQC_DELIM + query + LDAPQC_DELIM + filter + LDAPQC_DELIM + schedule);
        }

        String lastrun = null;
        String run = null;
        String nextrun = null;
        Date   lastrunDate = null;

        attr = attrs.get(ids [2]);

        if (attr != null) {
            commonStr = (String) attr.get();
            String [] strings = splitByPattern(commonStr , LDAPQC_DELIM , false );
            lastrun = strings[0];
            nextrun = strings[1];
        }

        if (!"null".equals(lastrun)) {
            lastrunDate = parseSimpleDate(lastrun);
        }

        String  finalStr = null;

        boolean modifyFlag = false;

        if (ldapQueryInfo.getQuery() != null) {
            modifyFlag = true;
            query      =  ldapQueryInfo.getQuery();
        }

        if (ldapQueryInfo.getSchedule()!= null) {
            modifyFlag = true;
            schedule   = ldapQueryInfo.getSchedule();
        }

        if (ldapQueryInfo.getFilter() != null) {
            modifyFlag = true;
            filter     = ldapQueryInfo.getFilter();
        }

        finalStr = queryname + LDAPQC_DELIM + query + LDAPQC_DELIM + filter + LDAPQC_DELIM + schedule;

        if (DEBUG > 4) {
            System.out.println("modify Flag " +  modifyFlag );
            System.out.println("New  query " + finalStr);
        }

        if (modifyFlag) {
            String[] attrs1 = new String[1];
            attrs1 [0] = format(ids [0], finalStr);
            dir.modifyObject(dn, attrs1);
        }
        if (ldapQueryInfo.getSearchBase() != null) {
            String[] attrs1 = new String[1];
            attrs1 [0] = format(ids [1], ldapQueryInfo.getSearchBase());
            dir.modifyObject(dn, attrs1);
        }
        Calendar cal = new GregorianCalendar(TimeZone.getDefault());
        tzOffset = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);

        String[] attrs1 = new String[1];
        if (ldapQueryInfo.getSchedule() != null) {
            if (!"never".equalsIgnoreCase(ldapQueryInfo.getSchedule()) ) {
                    Schedule sched = Schedule.readSchedule(ldapQueryInfo.getSchedule());
                    long nextTime = 0;
                    nextTime = outMS(sched.nextTime(inMS(new java.util.Date().getTime()), inMS(new java.util.Date().getTime())));
                    Date d = new Date(nextTime);
                    nextrun = getLDAPTime(d);
                    run = lastrun + LDAPQC_DELIM + nextrun;
           } else {
                    run = lastrun + LDAPQC_DELIM + null;
           }
        } else {
                    run     = null + LDAPQC_DELIM + null;
        }
        if (isVendor(VENDOR_AD) || isVendor(VENDOR_ADAM)) {
            attrs1 [0] = format(ids [2], run);
        } else {
            attrs1 [0] = format(ids [2], run);
        }
        dir.modifyObject(dn, attrs1);

        return true;
    }

    /**
     * connects to the Global cataglog
     *
     * @return REMIND
     */
    public LDAPConnection getGlobalCatalog() {
        try {
            globalCatalog = ldapEnv.createSubConfigConn();
        } catch (Exception e) {
            mgr.log(LOG_LDAPQC_CONN_TO_GLOBALCATAGLOG_FAILED, LOG_MAJOR, null);
        }

        return globalCatalog;
    }

    /**
     * gets the base DN.
     *
     * @return basedn the basedn of the current LDAP connection.
     */
    public String getBaseDn() {
        return ldapCfg.getProperty("basedn");
    }

    /**
     * appends filter to retrieve only users and/or machines
     *
     * @param query Query supplied by user.
     *
     * @return String a new query with filters for users and/or machines only.
     */
    public String getNewQuery(String query) {
        if (DEBUG > 4) {
            System.out.println(" LDAPQueryCollection.getNewQuery()");
        }

        String newQuery = null;
        String dirType = vendor;
        Map<String, String> LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(dirType);
        if (isVendor(VENDOR_AD)) {
            if ("usersonly".equals(ldapQueryInfo.getFilter())) {
                newQuery = "(&" + query + LDAPVarsMap.get("AD_USER_FILTER") + ")";
            } else if ("machinesonly".equals(ldapQueryInfo.getFilter())) {
                newQuery = "(&" + query + LDAPVarsMap.get("AD_MACHINE_FILTER") + ")";
            } else {
                newQuery = "(&" + query + LDAPVarsMap.get("AD_COMMON_FILTER") + ")";
            }
        } else if (isVendor(VENDOR_ADAM)) {
            if ("usersonly".equals(ldapQueryInfo.getFilter())) {
                newQuery = "(&" + query + LDAPVarsMap.get("ADAM_USER_FILTER") + ")";
            } else if ("machinesonly".equals(ldapQueryInfo.getFilter())) {
                newQuery = "(&" + query + LDAPVarsMap.get("ADAM_MACHINE_FILTER") + ")";
            } else {
                newQuery = "(&" + query + LDAPVarsMap.get("ADAM_COMMON_FILTER") + ")";
            }
        } else {
            if ("usersonly".equals(ldapQueryInfo.getFilter())) {
                newQuery = "(&" + query + LDAPVarsMap.get("SUNONE_USER_FILTER") + ")";
            } else if ("machinesonly".equals(ldapQueryInfo.getFilter())) {
                newQuery = "(&" + query + LDAPVarsMap.get("SUNONE_MACHINE_FILTER") + ")";
            } else {
                newQuery = "(&" + query + LDAPVarsMap.get("SUNONE_COMMON_FILTER") + ")";
            }
        }

        return newQuery;
    }

    /**
     * Adds targets as the member of the Collection in LDAP. Unlike RC
     * Collection machine entries need not be created while adding the
     * machines. The variable shouldIgnoreFailedMcs is not used either.
     * Otherwise this method is same as the one defined in LDAPCollection.
     *
     * @param targets A vector of target names
     *
     * @return true if the targets are successfully added.
     *
     * @exception AclStorageException if attempting to add more machines than the collection can hold.
     * @throws LDAPException REMIND
     * @throws LDAPLocalException REMIND
     * @throws NamingException REMIND
     * @throws CollectionException
     */
    public boolean addMembers(Vector targets) throws AclException, AclStorageException, LDAPException, LDAPLocalException, NamingException, CollectionException {
        if (DEBUG > 4) {
            System.out.println("LDAPBaseCollection.addMembers(Vector targets)");
        }
        String dirType = vendor;
        Map<String, String> LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(dirType);
        if (!writing) {
            if (DEBUG > 1) {
                System.out.println("LDAPCollection: Collection not open for writing: " + getName());
            }

            return false;
        }
        // the first thing we do is check to see if the user is a user in LDAP
        // if not we will throw an exception
        if (aclsOn) {
            try {
                if ((owner = getCollectionOwner()) == null) {
                    mgr.log(LOG_LDAPQC_USERNOTINLDAP, LOG_MAJOR, getOwner());
                    return false;
                }
            } catch (Exception e) {
                mgr.log(LOG_LDAPQC_USERNOTINLDAP, LOG_MAJOR, e.toString());
                return false;
            }
        }

        boolean checkPermissions = checkPermissions();

        Set<String> members = new HashSet<String>(targets.size() / 2);
        String machine;

        for (Enumeration e = targets.elements(); e.hasMoreElements();) {
            machine = (String) e.nextElement();
            if (DEBUG > 2) {
                System.out.println("LDAPCollection: adding machine = " + machine);
            }
            if (!checkPermissions || (checkPermissions && checkAclPermissions(machine))) {
                members.add(format(LDAPVarsMap.get("GROUP_MEMBER"), machine));
            }
        }
        writeGroup.add(members);
        if (aclsOn) {
            try {
                // create an ACL for the owner on the collection
                createAclForCollection();
            } catch (Exception e) {
                mgr.log(LOG_LDAPQC_GENERIC_EXCEPTION, LOG_MAJOR, getOwner(), e);
                return false;
            }
        }
        return true;
    }
    /**
     * returns a collection of machines and/or users that the user has policy write permission.
     *
     * @param targets A collection of targets ( users and/or machines )
     *
     * @return the collection that contains all targets that the current user has policy write permission.
     *
     * @throws AclStorageException REMIND
     * @throws LDAPException REMIND
     * @throws LDAPLocalException REMIND
     * @throws NamingException REMIND
     * @throws NameNotFoundException REMIND
     */
    public Vector getAllowedTargets(Vector targets) throws AclException, AclStorageException, LDAPException, LDAPLocalException, NamingException {
        if (DEBUG > 4) {
            System.out.println(" LDAPBaseCollection.getAllowedTargets(Vector targets)");
        }
        // the first thing we do is check to see if ACLs are on, and if so that the user is a user in LDAP
        // if not we log an error and exit
        // we need to do this if we implement this in the GUI.
        if (aclsOn) {
            try {
                if ((owner = getCollectionOwner()) == null) {
                    mgr.log(LOG_LDAPQC_USERNOTINLDAP, LOG_MAJOR, getOwner());
                    throw new NamingException("User not in LDAP");
                }
            } catch (Exception e) {
                mgr.log(LOG_LDAPQC_USERNOTINLDAP, LOG_MAJOR, null, e);
            }
        }

        boolean checkPermissions = checkPermissions();
        Vector  members = new Vector(targets.size());
        String  target;

        for (Enumeration e = targets.elements(); e.hasMoreElements();) {
            target = (String) e.nextElement();

            if (!checkPermissions || (checkPermissions && checkAclPermissions(target))) {
                members.addElement(target);
            }
        }

        return members;
    }
    /**
     * Converts the LDAP Time to the date(dd/mm/yy) based on the vendor attribute.
     *
     * @param utc date in LDAP format
     *
     * @return Date the converted date
     */
    public Date parseSimpleDate(String utc) {
        Date       date = null;
        DateFormat formatter = null;

        // setup x.208 generalized time formatter
        if (isVendor(VENDOR_AD) || isVendor(VENDOR_ADAM)) {
            formatter = new SimpleDateFormat("yyyyMMddHHmmss'.0Z'");

            // formatter.setTimeZone(TimeZone.getTimeZone("GMT-0"));
        } else {
            formatter = new SimpleDateFormat("yyyyMMddHHmmss'Z'");
        }

        try {
            // parse utc into Date
            date = formatter.parse(utc.trim());
        } catch (ParseException pe) {
           mgr.log(LOG_LDAPQC_GENERIC_EXCEPTION, LOG_MAJOR, null, pe);
        }

        return date;
    }

    /**
     * Converts the date(dd/mm/yy) to LDAP Time.
     * Based on the vendor attribute, it returs the corresponding converted LDAP Time.
     * @param datetime REMIND
     *
     * @return REMIND
     */
    public String getLDAPTime(Date datetime) {
        DateFormat formatter = null;

        // setup x.208 generalized time formatter
        if (isVendor(VENDOR_AD) || isVendor(VENDOR_ADAM)) {
            formatter = new SimpleDateFormat("yyyyMMddHHmmss'.0Z'");
        } else {
            formatter = new SimpleDateFormat("yyyyMMddHHmmss'Z'");
        }

        String ldapDate = formatter.format(datetime);

        return ldapDate;
    }
    /**
     * Generates query to execute in the database to retrive marimba managed targets.
     *
     * @param map a collection containning mapping between dn and cn.
     * @return
     */
    public String getDBQuery(String querystart, Map map) {
        Map members = (HashMap) map;
        Set cns = members.keySet();
        Object  memberlist[] = cns.toArray();

        int total = memberlist.length -1;
        StringBuffer dbquery = new StringBuffer(querystart);

        for( int i = 0; i < total; i++ ) {
           dbquery.append(((String)memberlist [i]).toUpperCase() + "','");
        }
        dbquery.append(((String)memberlist[total]).toUpperCase()+ "')");

        return dbquery.toString();
    }
    /**
     * Establishes connection to the database and retrieves marimba managed machines/users from the database
     *
     * @param query the query to exectute and retrieve records from the database.
     * @return List An array list of marimba managed users/machines
     */
    public List  getMatchingTargets(String query) throws SQLException {
        IStatementPool sp = null;
        DbSourceManager db = null;
        ResultSet theResultSet = null;
        Statement statement = null;
        List member = new ArrayList();
        try{
           db = new DbSourceManager(this.compMain);
           sp = db.getPool();
        } catch(Exception se){

            se.printStackTrace();
        }
        try {
            if(sp != null) {
                statement = sp.createStatement();
                theResultSet = statement.executeQuery(query);
                while(theResultSet.next()) {
                    member.add(theResultSet.getString(1));

                }
            } else {
                new SQLException("Database connection failed");
            }
        } catch(SQLException sqe) {
            sqe.printStackTrace();
            throw sqe;
        } finally {
            if(sp!=null) {
                try {
                db.returnPool(sp);
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
     return member;
   }
    /**
     * Evaluates the owneship of the user on any collection object. If the user is a primary administrator
     * he would be allowed to perform any operations on the given collection object. If the user is admin,
     * his Distinguished name is checked against the created by attribute of the collection.
     * @return
     * @throws NamingException
     */
    public boolean isOperationPermitted() throws NamingException {
    	String dirType = vendor;
    	Map<String, String> LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(dirType);
        if (!isPrimaryAdmin()) {
            String[] creators = null;

            if (isVendor(VENDOR_AD) || isVendor(VENDOR_ADAM)) {
                creators = new String[] { LDAPVarsMap.get("AD_GROUP_CREATEDBY") };
            } else {
                creators = new String[] { LDAPVarsMap.get("GROUP_CREATEDBY") };
            }

            Attributes creatorsattrib;
            creatorsattrib = dir.getAttributes(dn, creators);

            Attribute creatorattr;
            creatorattr = creatorsattrib.get(creators [0]);

            String creator = (String) creatorattr.get();

            if ("null".equals(creator)) {
                    return false;
            } else {
                if (!((ldapQueryInfo.getCreatedBy()).equalsIgnoreCase(creator))) {

                    return false;
                } else {
                    return true;
                }
            }
        }
     return true;
    }
    /**
     * Evaluates the owneship of the user on any collection object. If the user is a primary administrator
     * he would be allowed to perform any operations on the given collection object. If the user is admin,
     * his Distinguished name is checked against the created by attribute of the collection.
     * @return
     * @throws NamingException
     */
    public boolean isOperationPermitted(String user) throws NamingException {

        if (!isPrimaryAdmin(user)) {
            if ("null".equals(user)) {
                    return false;
            } else {
                if (!((ldapQueryInfo.getCreatedBy()).equalsIgnoreCase(user))) {

                    return false;
                } else {
                    return true;
                }
            }
        }
     return true;
    }

    /**
     * Primary Admin is only allowed for performing "addtask -all" and "removetask -all" operations.
     * @param user String - name of the user.
     * @return boolean true if primaryadmin else false
     */

     public boolean isOperationPermittedForTask(String user) {
        return isPrimaryAdmin(user);
     }

    /**
     * Displays contents of the any Vector collection
     * @param v the vector elements to display.
     */
    public void dispVector(Vector v) {
        Enumeration e = v.elements();
         while(e.hasMoreElements()) {
             System.out.println(e.nextElement());
         }
    }
    /**
     * Displays contents of the any map as a key : value pairs
     * @param map the map to display
     */
    public void dispMap(Map map) {
        Set s = map.keySet();
        Object arr[]= s.toArray();
        int total = arr.length;
        for(int i=0; i < total; i++ ){
             System.out.println( "Key : " + arr[i] +" Value : "+map.get(arr[i]));
        }
    }
    /**
     * Displays contents of the any list sequentially
     * @param list the List to display
     */
    public void dispList(List list) {
        int total = list.size();
        for( int i = 0; i < total; i++) {
            System.out.println(list.get(i));
        }
    }
    /**
     * Convert from UTC milliseconds to local milliseconds. e.g. 16:00 GMT + (-8) = 8:00 PST
     *
     * @param tm REMIND
     *
     * @return REMIND
     */
    static long inMS(long tm) {
        if (tm <= 0) {
            return tm;
        }

        return tm + tzOffset;
    }

    /**
     * Convert from local milliseconds to UTC milliseconds. e.g. 8:00 PST - (-8) = 16:00 GMT
     *
     * @param tm REMIND
     *
     * @return REMIND
     */
    static long outMS(long tm) {
        if (tm <= 0) {
            return tm;
        }

        return tm - tzOffset;
    }

    public static String[] splitByPattern(String src, String pattern, boolean allowSpaces)    {
           if(src==null || pattern==null)    {
               return(new String[0]);
           }
           else    {
               Vector pieces = new Vector(1,1);
               // Remove all the leading separator strings
               while(src.startsWith(pattern))    {
                   src = src.substring(pattern.length());
               }

               // Remove all the trailing separator strings
               while(src.endsWith(pattern))    {
                   src = src.substring(0,src.length()-pattern.length());
               }

                // Find first index of the pattern in the source string
               int currentIndex = src.indexOf(pattern);
               int previousIndex = 0;

                // If the pattern doesn't occur anywhere in the source string, add the source string
                // to the pieces to be returned.
               if(currentIndex==-1)    {
                   pieces.addElement(src);
               }
               else    {
                   while(currentIndex!=-1)    {
                       if(previousIndex==0 && currentIndex>0)    {
                           if(allowSpaces)    {
                               pieces.addElement(src.substring(previousIndex,currentIndex));
                           }
                           else if(!src.substring(previousIndex,currentIndex).trim().equals(""))    {
                               pieces.addElement(src.substring(previousIndex,currentIndex));
                           }
                       }
                       else    {
                           if(allowSpaces)    {
                               pieces.addElement(src.substring(previousIndex+pattern.length(),currentIndex));
                           }
                           else if(!src.substring(previousIndex+pattern.length(),currentIndex).trim().equals(""))    {
                               pieces.addElement(src.substring(previousIndex+pattern.length(),currentIndex));
                           }
                       }
                       if(currentIndex==src.lastIndexOf(pattern) && currentIndex+pattern.length()<src.length())    {
                           if(allowSpaces)    {
                               pieces.addElement(src.substring(currentIndex+pattern.length()));
                           }
                           else if(!src.substring(currentIndex+pattern.length()).trim().equals(""))    {
                               pieces.addElement(src.substring(currentIndex+pattern.length()));
                           }
                       }
                       previousIndex = currentIndex;
                       currentIndex = src.indexOf(pattern,previousIndex+pattern.length());
                   }
               }
               return((String[]) pieces.toArray(new String[0]));
           }
       }
    public Vector getTreeRoots() throws NamingException {
        String namingContext = LDAPConnUtils.getInstance(context.getTenantName()).getConfigNamingContext(dir);
        return dir.searchAndReturnRootDomainTrees(namingContext);
    }

    protected AbstractCollection createConcreteTgtCollection(
            String tgtBase,
            String tgtCollectionName) {
        throw new UnsupportedOperationException();
    }

    /**
     * Looks up the roles foy Adminisr the owner to determine if the owner is a Primartrator.
     *
     * @return true if the owner of the collection is a Primary Administrator, otherwise false
     */
    protected boolean isPrimaryAdmin() {
        if (DEBUG > 4) {
            System.out.println("LDAPBaseCollection.isPrimaryAdmin()");
        }

        // let's figure out whether the user's a primary admin
        boolean primaryAdmin = false;

        String  ownerName = ldapQueryInfo.getCreatedBy();
        IRole[] roles = roleMap.resolveRoles(ownerName);

        for (int i = 0; (roles != null) && (i < roles.length); i++) {
            if (PRIMARY_ADMIN_ROLE.equals(roles [i].getName())) {
                primaryAdmin = true;

                break;
            }
        }

        return primaryAdmin;
    }
    /**
     * Looks up the roles foy Adminisr the owner to determine if the owner is a Primartrator.
     *
     * @return true if the owner of the collection is a Primary Administrator, otherwise false
     */
    private boolean isPrimaryAdmin(String user) {
        if (DEBUG > 4) {
            System.out.println("LDAPBaseCollection.isPrimaryAdmin()");
        }

        // let's figure out whether the user's a primary admin
        boolean primaryAdmin = false;

        String  ownerName = user;
        IRole[] roles = roleMap.resolveRoles(ownerName);

        for (int i = 0; (roles != null) && (i < roles.length); i++) {
            if (PRIMARY_ADMIN_ROLE.equals(roles [i].getName())) {
                primaryAdmin = true;

                break;
            }
        }

        return primaryAdmin;
    }


    /**
     * Retrieves the name of the owner.  This will either be a DN (if the user that created the Collection is in LDAP), or the login name.
     *
     * @return String owner
     */
    private String getOwner() {
        if (DEBUG > 4) {
            System.out.println("LDAPBaseCollection.getOwner()");
        }

        return ldapQueryInfo.getCreatedBy();
    }

    /**
     * Retrieves the owner of the Collection.  Will throw an exception if the owner isn't a LDAP user or return a null user.
     *
     * @return SubUser the collection owner, or null if user can't be found
     *
     * @throws LDAPException
     * @throws LDAPLocalException
     * @throws NamingException
     */
    private SubUser getCollectionOwner() throws LDAPException,
                                                LDAPLocalException,
                                                NamingException {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollection: getCollectionOwner()");
        }
        boolean        AD = (isVendor(VENDOR_AD) && centralized && (globalCatalog != null));
        LDAPConnection conn = (AD ? globalCatalog : dir);
        String ownerName = ldapQueryInfo.getCreatedBy();

        if (DEBUG > 4) {
            System.out.println("LDAPCollection: ownerName = " + ownerName);
        }
        return new SubUser(ownerName, usersInLDAP, ldapEnv, ldapCfg, conn);
    }
}
