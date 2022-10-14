// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.users;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.intf.msf.IUserPrincipal;
import com.marimba.webapps.intf.SystemException;
import com.marimba.intf.ldap.ILDAPManagedFactory;

/**
 * A subscription manager administrator
 *
 * @author Theen-Theen Tan
 * @version $Revision$, $Date$
 */
public class CLIUser
    extends User {

    /**
     * Creates a new CLIUser object.
     *
     * @param user REMIND
     * @param main REMIND
     * @param main REMIND
     *
     * @throws SystemException REMIND
     */
    public CLIUser(IUserPrincipal user,
                   SubscriptionMain main)
        throws SystemException {
        super(user, null, user.getProperty("password"), main);
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

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(getClass().getName());
        buf.append("[user=")
	    .append(getName())
	    .append("-")
	    .append("cli")
	    .append(']');
        return buf.toString();
    }

    /**
     * Retrieves the LDAP environment authenticated with the user's credentials
     * @return the LDAP environement authenticated with the user's credentials
     */
    public ILDAPManagedFactory getLDAPManagedFactory(){
        return null;
    }

}
