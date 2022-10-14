// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.users;

import java.io.*;

import java.util.*;
import java.util.List;

import javax.naming.NamingException;

import javax.servlet.http.*;

import com.marimba.apps.subscription.common.*;
import com.marimba.apps.subscription.common.intf.*;

import com.marimba.apps.subscriptionmanager.*;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.intf.users.*;

import com.marimba.intf.msf.*;
import com.marimba.intf.util.*;

import com.marimba.rpc.*;

import com.marimba.tools.config.*;

import com.marimba.tools.ldap.*;

import com.marimba.tools.util.*;

import com.marimba.webapps.intf.*;

/**
 * A subscription manager administrator
 *
 * @author Theen-Theen Tan
 * @version $Date$, $Revision$
 */
public class AdminUser extends User {

    /**
     * Creates a new User object.
     *
     * @param wauser REMIND
     * @param httpSession REMIND
     * @param password REMIND
     * @param main REMIND
     *
     * @throws SystemException REMIND
     */
    public AdminUser(SubscriptionMain main)
        throws SystemException {
	super(null, null, null, main);
	this.ldapCfg = main.getLDAPConfig();	
	this.encPassword = ldapCfg.getProperty(LDAPConstants.PROP_PASSWORD);
	this.userDN = ldapCfg.getProperty(LDAPConstants.PROP_USERRDN);	
        if (DEBUG2) {
            connCache.ldapCfgDebug();
        }
    }

    /**
     * Loads a User object from persistant storage
     *
     * @throws SystemException REMIND
     */
    public void load()
        throws SystemException {
	// Does nothing	
    }

    /**
     * Saves a User object from persistant storage
     *
     * @throws SystemException REMIND
     */
    public void save()
        throws SystemException {
	// Does nothing
    }

    /**
     * Deletes a User object from persistant storage
     *
     * @throws SystemException REMIND
     */
    public void delete()
        throws SystemException {
	// Does nothing
    }

    /**
     * Returns the namespace for this user
     *
     * @return REMIND
     */
    public String getNameSpace() {
        return null;
    }

    /**
     * Obtains the property pairs for the user preferences and the ldap environment
     *
     * @return REMIND
     */
    public String[] getPropertyPairs() {
        int      length = 0;

        String[] proppairs = props.getPropertyPairs();

        if (proppairs != null) {
            length = length + proppairs.length;
        }

        String[] ldappairs = ldapCfg.getPropertyPairs();

        if (proppairs != null) {
            length = length + ldappairs.length;
        }

        String[] pairresults = new String[length];
        int      pos = 0;

        if (proppairs != null) {
            System.arraycopy(proppairs, 0, pairresults, 0, proppairs.length);
            pos = proppairs.length;
        }

        if (ldappairs != null) {
            System.arraycopy(ldappairs, 0, pairresults, pos, proppairs.length);
        }

        return pairresults;
    }

    /*
     *    Handle multi-user session
     */

    /**
     * The session created when the user logged in.
     *
     * @return REMIND
     */
    public HttpSession getSession() {
        return this.httpSession;
    }

    /**
     * Implements IUserPrincipal
     *
     * @return REMIND
     */
    public String getName() {
        return ldapCfg.getProperty(LDAPConstants.PROP_USERRDN);
    }

    /**
     * REMIND
     *
     * @param another REMIND
     *
     * @return REMIND
     */
    public boolean equals(Object another) {
        if (another instanceof User) {
            return getName()
                         .equalsIgnoreCase(((User) another).getName());
        }

        return false;
    }

    public void connectToLDAP()
        throws SystemException {
        main.getLDAPEnv()
            .connectUserToLDAP(this);
    }
    
    /**
     * REMIND
     *
     * @throws SystemException REMIND
     */
    public void initialize()
        throws SystemException {
        connectToLDAP();
        initialized = true;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(getClass().getName());
        buf.append("[user=")
           .append(getName())
           .append(']');

        return buf.toString();
    }    

}
