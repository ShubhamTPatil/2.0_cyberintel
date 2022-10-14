// Copyright 1997-2010, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.ldapsearch;

import com.marimba.apps.subscriptionmanager.intf.ILdapContext;

import com.marimba.tools.ldap.LDAPConnection;

import javax.naming.NamingException;
import javax.naming.NamingEnumeration;
import java.util.Enumeration;
import java.util.Map;

/**
 * This interface is implemented by LDAPContext and LDAPContextMock. LDAPContext provides LDAPConnection object to
 * search ldap with the given query. LDAPContextMock is used for the unit testing purpose.
 *
 * @author Venkatesh Jeyaraman
 * @version 1.0, 15/07/2010
 */

 public class LdapContext implements ILdapContext {
	public LDAPConnection conn;
    public String base;
    public boolean level;
    public Map<String, String> LDAPVarsMap;

    //nenum = conn.search(searchFilter, searchAttrs, base, LDAPVars.LDAP_SCOPE_SUBTREE);
	public LdapContext(LDAPConnection conn, String base, boolean level, Map<String, String> LDAPVarsMap) {
		this.conn = conn;
        this.base = base;
        this.level = level;
        this.LDAPVarsMap = LDAPVarsMap;
	}

    public Enumeration search(String query, String[] attrs) throws NamingException {
        NamingEnumeration nenum = conn.search(query, attrs, base, level);
		return nenum;
	}

	public String getBase() {
		return base;
	}

	public boolean isLevel() {
		return level;
	}

	public LDAPConnection getConn() {
		return conn;
	}

	public Map<String, String> getLDAPVarsMap() {
		return LDAPVarsMap;
	}

	public void setLDAPVarsMap(Map<String, String> lDAPVarsMap) {
		LDAPVarsMap = lDAPVarsMap;
	}
}
