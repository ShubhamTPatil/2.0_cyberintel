// Copyright 1997-2010, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.intf;

import javax.naming.NamingException;

import com.marimba.tools.ldap.LDAPConnection;

import java.util.Enumeration;
import java.util.Map;

/**
 * This interface is implemented by LDAPContext and LDAPContextMock. LDAPContext provides LDAPConnection object to
 * search ldap with the given query. LDAPContextMock is used for the unit testing purpose.
 *
 * @author Venkatesh Jeyaraman
 * @version 1.0, 15/07/2010
 */

public interface ILdapContext {

    // method used to search ldap with the given query and returns the specified attributes
    public Enumeration search(String query, String[] attrs) throws NamingException;
    public String getBase();
    public LDAPConnection getConn();
    public Map<String, String> getLDAPVarsMap();
}