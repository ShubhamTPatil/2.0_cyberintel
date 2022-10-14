// Copyright 2004-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.util;

import java.util.List;
import java.util.Vector;

import javax.naming.CommunicationException;
import javax.naming.NamingException;

import com.marimba.intf.ldap.ILDAPConnection;
import com.marimba.intf.ldap.ILDAPConnectionPool;
import com.marimba.intf.ldap.ILDAPManagedFactory;
import com.marimba.intf.ldap.ILDAPPagedSearchModule;
import com.marimba.intf.ldap.LDAPException;
import com.marimba.tools.util.DebugFlag;

/**
 * Utility class to enumerate the LDAP search results
 * 
 */
public class SearchResultEnumeration {
	public static String           OID_VLV = "2.16.840.1.113730.3.4.9";
	public static String           OID_PR = "1.2.840.113556.1.4.319";
	private ILDAPPagedSearchModule pagedResult;
	int                            startIndex;
	int                            size;
	String                         pagingType;
	int                            pageSize;
	private int                    actualPageSize;
	private boolean                pagesExist;
	private ILDAPConnectionPool   ldapPool;
	private ILDAPConnection conn;
	public String LDAP_QUERY_EMPTY = "(|)";
	
	Vector            roots;
	private int resultIndex = 0;
	String              query;
	String[]            attributes;
	String[]            sortAttrs;
	ILDAPManagedFactory ldapManaged;
	boolean queryOk = true;
	
	 // Page size should be such that 500 / pagesize without reminders
	public SearchResultEnumeration(ILDAPManagedFactory ldapManaged,
									String              query,
									int                 pageSize,
									String[]            attributes,
									String[]            sortAttrs,
									ILDAPConnectionPool connPool)
	     throws Exception {
		this.ldapManaged = ldapManaged;
	     try {
	         if(null != connPool) {
	             this.ldapPool = connPool;
	             roots = new Vector();
                 roots.add(connPool.getBaseDN());
	         } else {
	             throw new Exception("PageResultEnumeration : Invalid or null connection object");
	         }
	
	         ldapPool.setConnectionTimeout(600);
	         this.query = query;
	         this.attributes = attributes;
	         this.sortAttrs = sortAttrs;
	         this.pageSize = pageSize;
	     } catch (Exception e) {
	         e.printStackTrace();
	         throw new Exception(e);
	     }
	 }
	
	private void checkEmptyQuery() {
        if (LDAP_QUERY_EMPTY.equals(query)) {
            queryOk = false;
        }
   }

   public boolean hasMoreResults() {
       checkEmptyQuery();
       if(queryOk && roots != null && resultIndex < roots.size()) {
           return true;
       }
       return false;
   }

   public void moveToNextResult() throws Exception {
       try {
           String nextRoot = (String)roots.get(resultIndex);
           pagedResult = ldapManaged.getLDAPPagedSearch(ldapPool, query, attributes, nextRoot , false, sortAttrs);
           pagedResult.setPageSize(pageSize);
           this.actualPageSize = pageSize; //pagedResult.getPageSize();
           pagingType    = pagedResult.getType();
           startIndex    = 0;
           init();
           resultIndex ++;
       } catch (CommunicationException ce) {
               System.out.println("CommunicationException occurred LDAP query executed : "+ query);
               ce.printStackTrace();
           throw new Exception(ce);
       } catch (NamingException e) {
               System.out.println("NamingException occurred LDAP query executed : "+ query);
               e.printStackTrace();
           throw new Exception(e);
       }
   }

   /**
    * Initiate the variables with default value
    *
    * @throws NamingException REMIND
    */
   public void init()
       throws NamingException {

       conn = ldapPool.getConnection();

           System.out.println("Got new connection for fresh search "+conn.toString());

       if (OID_PR.equals(pagingType)) {
           // AD paging does not give the total number of pages
           pagesExist = true;
       } else {
           // NS paging gives the total number of pages
           size       = pagedResult.getSize((ILDAPConnection)conn);
           pagesExist = (size > 0);
       }

   }

   /**
    * REMIND
    *
    * @return REMIND
    *
    * @throws NamingException REMIND
    */
   public List getNextPage()
       throws NamingException {
       int prevStartIndex = startIndex;
       startIndex = startIndex + actualPageSize;

       List results = null;
       try {
           results = pagedResult.getPage(prevStartIndex, (ILDAPConnection)conn);
       } catch (NamingException nexp) {
           ldapPool.releaseConnection((ILDAPConnection)conn);
           throw new LDAPException(nexp);
       }
       return results;
   }

   /**
    * REMIND
    *
    * @return REMIND
    *
    * @throws NamingException REMIND
    */
   public boolean nextPagesExist()
       throws NamingException {
       if (OID_PR.equals(pagingType)) {
           boolean hasMoreResults = pagedResult.hasMoreResults();
           boolean pageSizeComp = (startIndex < pagedResult.getSize((ILDAPConnection)conn));
               System.out.println("MemberResultEnumeration.nextPagesExist() hasMoreResults="
                       + hasMoreResults+" pageSizeComp="+pageSizeComp);
           pagesExist =  hasMoreResults || pageSizeComp;
       } else {
           pagesExist = (startIndex < size);
       }

       if(!pagesExist){
           ldapPool.releaseConnection((ILDAPConnection)conn);
       }
       return pagesExist;
   }
}