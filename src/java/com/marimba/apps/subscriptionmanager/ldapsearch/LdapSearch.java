// Copyright 1997-2010, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.ldapsearch;

import com.marimba.apps.subscriptionmanager.intf.ILdapSearch;
import com.marimba.apps.subscriptionmanager.intf.ILdapContext;

import com.marimba.webapps.tools.util.PropsBean;
import com.marimba.webapps.intf.IMapProperty;

import javax.naming.NamingException;
import java.util.*;


/**
 * This interface is implemented by LDAPContext and LDAPContextMock. LDAPContext provides LDAPConnection object to
 * search ldap with the given query. LDAPContextMock is used for the unit testing purpose.
 *
 * @author Venkatesh Jeyaraman
 * @version 1.0, 15/07/2010
 */

 public abstract class LdapSearch implements ILdapSearch {

    final static boolean DEBUG = com.marimba.apps.subscription.common.intf.IDebug.DEBUG;
    public String type;
    IMapProperty criteria = null;
    String[] attrs = null;
    ILdapContext context = null;
    boolean addQuery = false;
    List channelsList = null;
    boolean usersInLDAP = false;

	public void setCriteria(IMapProperty criteria) {
        this.criteria = criteria;
	}

	public void setAttributes(String[] attrs) {
        this.attrs = attrs;
	}

	public void addAttribute(String attr) {
        if (null == attrs) {
            attrs = new String[10];
        }

        attrs[attrs.length] = attr;
	}

	public void setType(String type) {
        this.type = type;
	}

	public String getType() {
        return type;
	}

    public Vector<PropsBean> execute() {
        Enumeration resultEnum = null;
        Vector<PropsBean> resultVec = null;

        try {
            resultEnum = context.search(getQuery(false), attrs);
            resultVec = filterSchedule(resultEnum);
        } catch (NamingException namEx) {
            System.out.println("LDAPSearch.execute(): The given query might be wrong or there is a problem in communicating configured LDAP");
            if (DEBUG) {
                namEx.printStackTrace();
            }

            return new Vector<PropsBean>();
        }
        return resultVec;
    }

    public void setContext(ILdapContext context) {
        this.context = context;
	}

    public void setChannels(List channelsList) {
        this.channelsList = channelsList;
    }
    public void setUsersInLDAP(boolean usersInLDAP) {
    	this.usersInLDAP = usersInLDAP;
    }
    
    public boolean isUsersInLDAP() {
    	return this.usersInLDAP;
    }
	abstract protected Vector<PropsBean> filterSchedule(Enumeration ne) throws NamingException;
}
