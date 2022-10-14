// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$
package com.marimba.apps.subscriptionmanager.webapp.validations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.naming.CommunicationException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.intf.ldap.*;
import com.marimba.intf.msf.*;
import static com.marimba.intf.ldap.config.ILDAPConfigConstants.*;
import com.marimba.intf.logs.ILogConstants;
import com.marimba.intf.util.IProperty;
import com.marimba.intf.ldap.ILDAPConnectionPool;
import com.marimba.intf.msf.IRole;
import com.marimba.intf.msf.IRoleMap;
import javax.servlet.ServletContext;
import com.marimba.intf.msf.IServer;
import com.marimba.intf.msf.IAccessControlMgr;
import javax.servlet.http.HttpServletRequest;
import static com.marimba.intf.msf.AppManagerConstants.*;
import com.marimba.intf.util.IDirectory;
import org.apache.struts.action.ActionErrors;
import com.marimba.webapps.tools.util.KnownActionError;

import com.marimba.intf.ldap.*;
import com.marimba.intf.ldap.config.ILDAPConfigModule;
import com.marimba.intf.ldap.ILDAPManagedFactory;
import com.marimba.intf.ldap.config.ILDAPConfigModule;
import com.marimba.tools.util.DebugFlag;
import com.marimba.apps.subscriptionmanager.webapp.util.UniqueList;
import com.marimba.apps.subscriptionmanager.webapp.util.SearchResultEnumeration;
import com.marimba.tools.util.QuotedTokenizer;

public class LDAPGroupsValidation implements  ILDAPConstants,IErrorConstants, IWebAppConstants {

	private int MAXGROUPRECURSIVECOUNT = 10;
	private int PROCESS_MEMBERS_LIMIT = 100;
	private int GRACE_PROCESS_MEMBERS_LIMIT = (int) (PROCESS_MEMBERS_LIMIT * .25F);
	private int GROUP_QUERY_PAGE_SIZE = 500;
	private int MACHINES_COUNT_PER_BATCH = 990;
	private String DISTINGUISHED_NAME = "distinguishedName";
	private String   PROP_GRPMEMBERATTR = "groupMemberAttr";
	private String   PROP_GRPCLASS = "groupClass";
	private String CN = "cn";
    
    private ILDAPManagedFactory managedFactory;
    private String activeLdap;
	protected ILDAPConnectionPool ldapPool;
	protected ILDAPConfigModule configModule;
	protected IProperty ldapCfg;
	protected String machineBase;
	protected String machineClass;
	protected boolean isNS;
	protected boolean isAD;
	protected boolean isADAM;
	protected boolean isMSLDAP;
	protected IAccessControlMgr acmgr;
	protected SubscriptionMain main;
	protected IDirectory features;
	protected ITenantManager tenantMgr;
	protected IServer server;
	protected ITenant tenant;
	
	List OperatorGroupList = new ArrayList();
	Map OpreratorsGroupMap = new HashMap();

	public LDAPGroupsValidation(HttpServletRequest request) {
		try {
			init(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init(HttpServletRequest request) throws Exception {
        try {        	
        	 ServletContext  context = request.getSession().getServletContext();
        	 this.features = (IDirectory) context.getAttribute("com.marimba.servlet.context.features");
        	 this.tenantMgr = (ITenantManager)features.getChild("tenantMgr");
        	 this.tenant = TenantHelper.getTenantObject(tenantMgr, TenantHelper.getTenantName(request));
        	 acmgr = tenant.getAccessControlMgr();
        	 main = TenantHelper.getTenantSubMain(context, request.getSession(), tenant.getName());
        	 
        	 this.managedFactory = tenant.getLdapManagedFactory();
        	 if(null != managedFactory) {
                 this.activeLdap = managedFactory.getLDAPConfig().getProperty("vendor");
                 this.configModule = managedFactory.getLDAPConfigModule();
                 this.ldapPool = configModule.getMarimbaReadConnectionPool();
                 this.ldapCfg = managedFactory.getLDAPConfig();
                 this.machineClass = configModule.getMarimbaConfig().getProperty(CONFIG_PROP_MARIMBA_MACHINE_CLASS);
                 setVendorType(ldapPool.getVendor());
             }

        }  catch (Exception ex) {
            if (DEBUG2) {
                ex.printStackTrace();
            }
        }
    }
	
	protected void setVendorType(String ldapType) {
        if(VENDOR_AD.equals(ldapType)) {
            isAD = isMSLDAP = true;
        } else if(VENDOR_ADAM.equals(ldapType)) {
            isADAM = isMSLDAP = true;
        } else if(VENDOR_NS.equals(ldapType)) {
            isNS = true;
        }
    }
	
	private List<String> enumerateMembers(List<String> groupDNs, String baseDN, String[] returnAttr,
            SearchResultEnumeration resultPages) throws Exception {
		List<String> members = new UniqueList<String>();
		while (resultPages.hasMoreResults()) {
			resultPages.moveToNextResult();	
			while (resultPages.nextPagesExist()) {
				List pageList = resultPages.getNextPage();
				for (Object list : pageList) {
					 SearchResult cnResult = (SearchResult) list;
	                    Attributes attrs = cnResult.getAttributes();
	                    if (isMSLDAP) {
	                        // Add the DN since this is identified as a group
	                        Attribute dnAttr = attrs.get(DISTINGUISHED_NAME);
	                        groupDNs.add(normalizeDN((String) dnAttr.get()));
	                    } else {
	                        Attribute cnAttr = attrs.get(CN);
	                        String rdn = cnResult.getName();
	                        if ((rdn != null) && (!"".equalsIgnoreCase(rdn.trim()))) {
	                            groupDNs.add(normalizeDN(unescapeSearchResultRDN(rdn) + ',' + baseDN));
	                        } else {
	                            throw new IllegalArgumentException("Unable to read RDN value. "
	                                    + "Query should produce only group DNs: "+ cnAttr.get());
	                        }
	                    }
	                    Attribute membersAttr = null;
	                    for (String attr : returnAttr) {
	                        if (DISTINGUISHED_NAME.equals(attr)) {
	                            continue;
	                        }

	                        if (CN.equals(attr)) {
	                            continue;
	                        }
	                        if(DEBUG5){
	                        	System.out.println("attrs.get(attr) value :"+attrs.get(attr));
	                        }
	                        if(attr.equals("member")){
	                        	if (attrs.get(attr) != null) {
	                        		membersAttr = attrs.get(attr);
	                        		break;
	                        	}
	                        }
	                    }
	                    if (membersAttr == null) {
	                        continue;
	                    }
	                    for (int j = 0; j < membersAttr.size(); j++) {
	                        members.add(normalizeDN((String) membersAttr.get(j)));
	                    }
				}
			}
		}
		 return members;
	}
	
	
	public String normalizeDN(String dn) throws NamingException {
        ILDAPName parser = ldapPool.getParser();
        return parser.getCanonicalName(dn);
    }
	
	 private String unescapeSearchResultRDN(String rdn) throws NamingException {
	        ILDAPName parser = ldapPool.getParser();
	        return parser.unescapeSearchResultName(rdn);
	    }
	 
	public List<String> getOperatorGroups() throws Exception {
		List<String> operatorsList = new ArrayList<String>();
		String baseDN = ldapPool.getBaseDN();
		
		IRoleMap roleMap = acmgr.getRoleMap();
        Vector<String> applns = roleMap.getApplications();

        String operatorKey = tenantMgr.isCloudModel() ? ITenantConstants.CLOUD_ROLE_OPERATOR :
                ((tenantMgr.isTenantModel()) ? ITenantConstants.TENANT_ROLE_OPERATOR : ITenantConstants.ROLE_OPERATOR);
        IRole operatorRole = roleMap.get(operatorKey);

        for (String application : applns) {
        	if(application.equals("sm") || application.equals("all")){
        		String str = operatorRole.getApplicationMapping(application, "");

        		if (str != null) {
                    QuotedTokenizer tok = new QuotedTokenizer(str, ", ", '\\');
                    while (tok.hasMoreTokens()) {
                        try{
                            String  strTok = tok.nextToken().trim();
                            if(strTok.contains(baseDN)) {
                                operatorsList.add(strTok);
                            } else {
                                operatorsList.add(main.resolveGroupDN(strTok));
                            }
                        } catch(Exception se) {
                            se.printStackTrace();
                        }
                    }
        		}
        	}
        }

        return operatorsList;
	}
	
	// Method to list out all childmember groups from entered groupDNs .

	
	public List getChildMembers(List<String> groupDNs) throws Exception {
        if (groupDNs != null) {
            String baseDN = ldapPool.getBaseDN();
            return getChildMembers(groupDNs, ldapPool);
        } else {
            throw new IllegalArgumentException("Invalid group DN, value is null");
        }
    }
	
	// List out childmember groups from entered groupDNs .
	
	
	public List getChildMembers(List<String> groupDNs, ILDAPConnectionPool conn) throws Exception {
        int depth = 0;
        List<String> allMembers;
        List<String> dnsToResolve;
        List<String> resolvedDNs = new UniqueList<String>();

        // Assign initial set of DNs to start resolving child groups
        dnsToResolve = groupDNs;
        
        do {
            dnsToResolve.removeAll(resolvedDNs);
            allMembers = getMembers(dnsToResolve, groupDNs, conn);
            resolvedDNs.addAll(dnsToResolve);
            groupDNs.addAll(allMembers);
            dnsToResolve = allMembers;
        } while (!dnsToResolve.isEmpty() && (depth++ <= MAXGROUPRECURSIVECOUNT));
        return filterMachineObjects(groupDNs, conn);
    }
	
	
	
	
	
	private List filterMachineObjects(List<String> members, ILDAPConnectionPool conn) throws Exception {
        String baseDN = conn.getBaseDN();
        String sortAttr[] = new String[] { CN };
        SearchResultEnumeration resultPages = null;
        List<String> returnAttrList = new ArrayList<String>(2);
        List<String> tmp_mrbaMachines = new UniqueList<String>();
        Map <String, List<String>> mrbaMachines =  new HashMap<String, List<String>>();

        String machineFilter = getMachinesFilter().toString();
        String dnAttrName = configModule.getProperty(KEY_ATTR_DNENTRY);
        List<String> batchList = buildLDAPQueryBatches(members, machineFilter, dnAttrName);

        StringTokenizer tokens = new StringTokenizer(dnAttrName, ",");

        while (tokens.hasMoreTokens()) {
            returnAttrList.add(tokens.nextToken());
        }
        returnAttrList.add(CN);
        String returnAttr[] = new String[returnAttrList.size()];
        returnAttrList.toArray(returnAttr);
        try {
            for (String aQuery : batchList) {
                resultPages = new SearchResultEnumeration(managedFactory, aQuery, GROUP_QUERY_PAGE_SIZE, returnAttr, sortAttr, conn);
                tmp_mrbaMachines.addAll(parseResult(resultPages));
            }
        } catch (NamingException e) {
        	if (DEBUG5){
        		System.out.println("Unable to parse the machines " + e.toString());
        		e.printStackTrace();
        	}
            throw new Exception(e);
        }  catch (Exception lex) {
        	if (DEBUG5){
	            System.out.println("Unable to parse the machines " + lex.toString());
	            lex.printStackTrace();
        	}
            throw lex;
        }
        if(DEBUG5){
        	System.out.println("Filtered marimba machines size is " + tmp_mrbaMachines.size() + " and members : " + tmp_mrbaMachines);
        }
        mrbaMachines.put(baseDN, tmp_mrbaMachines);
        OperatorGroupList.addAll(tmp_mrbaMachines);
        return OperatorGroupList;
    }
	
	private List<String> parseResult(SearchResultEnumeration resultPages) throws Exception {
        List<String> result = new UniqueList<String>();

        if(resultPages.hasMoreResults()) {
            resultPages.moveToNextResult();
            while (resultPages.nextPagesExist()) {
                List pageList = null;
                pageList = resultPages.getNextPage();
                for (Object list : pageList) {
                    SearchResult cnResult = (SearchResult) list;
                    Attributes attrs = cnResult.getAttributes();
                    Attribute dnAttr = attrs.get(DISTINGUISHED_NAME);
                    result.add(((String) dnAttr.get()).toLowerCase());
                }
            }
        }
        return result;
    }
	
	private List<String> buildLDAPQueryBatches(List<String> macSet, String typeFilter, String dnAttrName) {
        int start_index = 0;
        List<String> batch_list = new ArrayList<String>();
        int total_machine_count = macSet.size();
        int total_batch_count = total_machine_count / MACHINES_COUNT_PER_BATCH;
        String tmp_query = "";

        List<String> tmp_list = new ArrayList<String>(macSet);
        if(total_machine_count % MACHINES_COUNT_PER_BATCH != 0) {
            total_batch_count = total_batch_count + 1;
        }
        
        if(DEBUG5){
            System.out.println("Total ldap query batch counts : " + total_batch_count);
        }   
        for(int loop = 0; loop < total_batch_count; loop++) {
            Set<String> tmp_mac_list = new HashSet<String>();
            for (int loop2 = 0; (loop2 < MACHINES_COUNT_PER_BATCH && start_index < tmp_list.size()); loop2++) {
                tmp_mac_list.add(tmp_list.get(start_index));
                start_index++;
            }
            tmp_query = formGetMembersQuery(tmp_mac_list, typeFilter, dnAttrName);
            if(tmp_query.length() > 0) {
                batch_list.add(tmp_query);
            }
        }
        return batch_list;
    }
	
	public StringBuffer getMachinesFilter() {
        StringBuffer buf = new StringBuffer();
        buf.append("(|");
        if (isMSLDAP) {
            buf.append(getValueFilter(ILDAPConstants.OBJECT_CLASS, ldapCfg.getProperty(PROP_GRPCLASS)));
        } else {
        	buf.append(getValueFilter(ILDAPConstants.OBJECT_CLASS, ldapCfg.getProperty(PROP_GRPCLASS)));
        }
        buf.append(')');
        return buf;
    }
	
	private List<String> getMembers(List<String> dns, List<String> groupDNs, ILDAPConnectionPool dcConn) throws Exception {
        List<String> result = new UniqueList<String>();
        String groupFilter = getGroupsFilter();
        String dnAttrName = configModule.getProperty(KEY_ATTR_DNENTRY);

        // Process the given DNs in batches to resolve child groups and other members
        int count = 0;
        int processedCount = 0;
        int size = dns.size();
        String filter;
        Set<String> dnsSubSet = new HashSet<String>();
        Iterator<String> dnsItr = dns.iterator();

        while (dnsItr.hasNext()) {
            if (count++ < PROCESS_MEMBERS_LIMIT) {
                dnsSubSet.add(dnsItr.next());
            } else {
                processedCount += PROCESS_MEMBERS_LIMIT;

                if (dnsItr.hasNext()) {
                    if ((size - processedCount) <= GRACE_PROCESS_MEMBERS_LIMIT) {
                    	if(DEBUG5){
                            System.out.println("The next set contains "+ (size - processedCount)
                                    + " entries. Processing in the same iteration");
                    	}
                        while (dnsItr.hasNext()) {
                            dnsSubSet.add(dnsItr.next());
                        }
                    }
                }

                count = 0;
                filter = formGetMembersQuery(dnsSubSet, groupFilter, dnAttrName);
                dnsSubSet.clear();
                result.addAll(runGetMembersQuery(filter, groupDNs, dcConn));
            }
        }

        if (!dnsSubSet.isEmpty()) {
            filter = formGetMembersQuery(dnsSubSet, groupFilter, dnAttrName);
            dnsSubSet.clear();
            result.addAll(runGetMembersQuery(filter, groupDNs, dcConn));
        }
        return result;
    }
	
	
	
	private String formGetMembersQuery(Collection<String> dnsSubSet, String typeFilter, String dnAttrName) {
        StringBuffer filterBuffer = new StringBuffer();

        // (&(|(objectCategory=group))(|(distinguishedName=1)(distinguishedName=2)))
        filterBuffer.append("(&").append(typeFilter);
        filterBuffer.append("(|");

        for (String subSet : dnsSubSet) {
            filterBuffer.append('(');
            filterBuffer.append(dnAttrName).append('=').append(escapeDN(subSet));
            filterBuffer.append(')');
        }

        filterBuffer.append("))");
        if(DEBUG5){
            System.out.println("LDAP Query : " + filterBuffer.toString());
        }
        return filterBuffer.toString();
    }
	
	 private List<String> runGetMembersQuery(String filter, List<String> groupDNs, ILDAPConnectionPool dcConn) throws Exception {
	        String sortAttr[];
	        SearchResultEnumeration resultPages;
	        List<String> members = new UniqueList<String>();
	        List<String> returnAttrList = new ArrayList<String>(2);

	        StringTokenizer tokens = new StringTokenizer(ldapCfg.getProperty(PROP_GRPMEMBERATTR), ",");

	        while (tokens.hasMoreTokens()) {
	            returnAttrList.add(tokens.nextToken());
	        }
	        if (isMSLDAP) {
	            returnAttrList.add(DISTINGUISHED_NAME);
	        } else {
	            returnAttrList.add(CN);
	        }
	        returnAttrList.add(ILDAPConstants.OBJECT_CLASS);
	        String returnAttr[] = new String[returnAttrList.size()];
	        returnAttrList.toArray(returnAttr);

	        sortAttr = new String[] { CN };
	        String baseDN = dcConn.getBaseDN();
	        try {
	            resultPages = new SearchResultEnumeration(managedFactory, filter,
	                    GROUP_QUERY_PAGE_SIZE, returnAttr, sortAttr, dcConn);
	            List<String> domainMembers = enumerateMembers(groupDNs, baseDN, returnAttr, resultPages);
	            members.addAll(domainMembers);
	            if(DEBUG5){
	            System.out.println("Found the list of members of affected groups for "
	                    + "domain: " + baseDN + " = " + domainMembers.size());
	            }
	            
	        } catch (NamingException e) {
	            throw new Exception(e);
	        }
	        return members;
	    }
	    
	 	public String escapeDN(String dn) {
	        return managedFactory.getLDAPSearchFilter().escapeComponentValue(dn);
	    }
	 	
	 	
	 
	  public String getGroupsFilter() {
	        StringBuffer buf = new StringBuffer();
	        buf.append("(|");

	        if (isMSLDAP) {
	            buf.append(getValueFilter(ILDAPConstants.OBJECT_CLASS, ldapCfg.getProperty(PROP_GRPCLASS)));
	        } else {
	            buf.append(getValueFilter(ILDAPConstants.OBJECT_CLASS, ldapCfg.getProperty(PROP_GRPCLASS)));
	        }

	        buf.append(')');
	        return buf.toString();
	    }
	  
	  
	  public static StringBuffer getValueFilter(String constantAttribute, String commaSeparatedValue) {
	        StringTokenizer strToken = new StringTokenizer(commaSeparatedValue, ",");
	        StringBuffer buf = new StringBuffer(1000);

	        while (strToken.hasMoreTokens()) {
	            buf.append('(').append(constantAttribute).append('=').append(strToken.nextToken()).append(')');
	        }

	        return buf;
	    }
	  
	  //Method to list out  all entered PeerApprovalGroups .

	  public List getPeerApprovalGroups(Vector<String> grp) {
			List list = new ArrayList();
			String dnValue="";
			String baseDN = ldapPool.getBaseDN();
	        
			for (int i = 0; i < grp.size(); i++) {
	        	String application = (String) grp.get(i);
	        		if (application != null) {
	        			try{
	        				if(application.contains(baseDN)){
	        					list.add(normalizeDN(application));
	        					OpreratorsGroupMap.put(normalizeDN(application),application);
	        				}
	        				else{
	        					dnValue = main.resolveGroupDN(application);
	        					list.add(normalizeDN(dnValue));
	        					OpreratorsGroupMap.put(normalizeDN(dnValue),application);
	        				}
	        			}catch(Exception se){
	        				System.out.println("Exception in getPeerApprovalGroups");
	        				se.printStackTrace();
	        			}
	        		}
	        }
			return list;
		}

	  //check whether entered PeerApprovalGroups belongs to OperatorGroups or not.
	public void compareOperAndPeerApproval(List peerApprovalGrpList, List operatorGrpList,ActionErrors errors) {
		Vector<String> grp = new Vector<String>();
		String baseDN = ldapPool.getBaseDN();
		for (int i = 0; i < peerApprovalGrpList.size(); i++) {
			if(operatorGrpList.contains(peerApprovalGrpList.get(i))){
				grp.addElement(peerApprovalGrpList.get(i).toString());
			}
		}
		if (grp.size() > 0) {
            for (Object group : grp.toArray()) {
            	String errStr = OpreratorsGroupMap.get(group).toString();
            	if(errStr.contains(baseDN)){
            		errors.add("peersToAddress", new KnownActionError(LDAP_OPERATOR_GROUP, errStr));
            		
            	}else{
            		errors.add("peersToAddress", new KnownActionError(LDAP_OPERATOR_GROUP, OpreratorsGroupMap.get(group).toString()));
            	}
            }
        }
	}
}





