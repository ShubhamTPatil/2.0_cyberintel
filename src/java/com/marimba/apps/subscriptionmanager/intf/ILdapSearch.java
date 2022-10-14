// Copyright 1997-2010, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.intf;

import com.marimba.webapps.tools.util.PropsBean;
import com.marimba.webapps.intf.IMapProperty;

import javax.naming.NamingException;
import java.util.*;

/**
 * Interface to form ldap query based on the parameters and search ldap for the results.
 *
 * @author Venkatesh Jeyaraman
 * @version 1.0, 15/07/2010
 */
public interface ILdapSearch {
    public static final String AFTER = "after";
    public static final String BEFORE = "before";
    public static final String BETWEEN = "between";
    public static final String ACTIVE = "active";

    /* set criteria given as input from target and package views.
     * The criteria should be given in the form of key-value pairs
    */
	public void setCriteria(IMapProperty attrs);

	// set attribtes expected in the result.
    /**
     *
     * @param attrs
     */
	public void setAttributes(String[] attrs);

	// add a particular attribute to the list irrespective of whether it is already has values or not
	public void addAttribute(String attr);

	// set the type value to form the query. The possible values for this would be 'target' or 'package'.
	public void setType(String type);

	// getter method to return type value
	public String getType();

    // getter method to return the query formed
    public String getQuery(boolean addQuery);

	// this method searches ldap with already formed query from input parameters and returns the results.
	public Vector<PropsBean> execute() throws NamingException;

	// LDAPConnection object should be passed to this method so that search can be done using given connection object
	public void setContext(ILdapContext context);

    // set channels list to filter the channels selected in multiple mode
    public void setChannels(List channelsList);
    
	// Whether users from LDAP or transmitter 
    public void setUsersInLDAP(boolean usersInLDAP);
}
