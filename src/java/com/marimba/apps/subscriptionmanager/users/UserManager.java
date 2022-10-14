// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.users;

import java.io.*;

import java.lang.reflect.Constructor;

import java.util.*;

import javax.servlet.http.*;

import com.marimba.apps.subscription.common.intf.*;
import com.marimba.apps.subscription.common.intf.IDebug;

import com.marimba.apps.subscriptionmanager.*;
import com.marimba.apps.subscriptionmanager.intf.*;
import com.marimba.apps.subscriptionmanager.intf.users.*;

import com.marimba.intf.msf.*;

import com.marimba.tools.ldap.*;

import com.marimba.webapps.intf.*;

/**
 * A singleton class that manages users logged on to Subscription Manager REMIND : note the relationship between GUI and CLI
 *
 * @author Theen-Theen
 * @version 1.11, 04/23/2003
 */
public class UserManager
    implements IAppConstants {
    static String                   subClassPath = "com.marimba.apps.subscriptionmanager.users.FileUser";
    private static Hashtable        users;
    private static File             userDir = null;
    private static SubscriptionMain main = null;
    private static Map<String, TenantAttributes> tenantsAttr;
    /**
     * Initialize the User Manager
     *
     * @param userClassName the class name of the persistent storage used to  store user preferences
     * @param sm REMIND
     *
     * @throws SubInternalException REMIND
     */
    public static void init(String           userClassName,
                            SubscriptionMain sm)
        throws SubInternalException {
        subClassPath = userClassName;
        main         = sm;

        // If data source is a file system, create the user directory
        // if it isn't already created
        if (subClassPath.indexOf("FileUser") != -1) {
            File userDir = new File(main.getDataDirectory(), USER_DIR);

            if (!userDir.exists()) {
                userDir.mkdirs();
            }
        }

        users = new Hashtable();
    }
    public synchronized static void setTenantsAttributes(Map<String, TenantAttributes> tenantsAttributes) {
    	tenantsAttr = tenantsAttributes;
    }
    public synchronized static TenantAttributes getTenantAttr(String tenantName) {
    	return tenantsAttr.get(tenantName);
    }
    private synchronized static void displayTanant() {
    	Iterator it = tenantsAttr.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            String tenantName = (String)pairs.getKey();
            TenantAttributes tenan = (TenantAttributes) pairs.getValue();
            SubscriptionMain main = tenan.getTenantMain();
            String path = main.getDataDirectory().getAbsolutePath();
            System.out.println("Main dir  = " + path);
        }
    }
    /**
     * Retrieves the data store for a user object.  The data source object is set using init()
     *
     * @return IUserDataSource a user data source object.
     */
    public static IUserDataSource getUserDataSource(SubscriptionMain main) {
        // REMIND tcube : Later, we will use the reflection API to
        // create an LDAP DataSource for user
    	
        return new FileUser(main);
    }

    /**
     * Adds a subscription manager user object for users who are currently logged in. Loads the user's preference file if there is one for this user.
     * Otherwise, creates one.
     *
     * @param wauser REMIND
     * @param session REMIND
     * @param password REMIND
     *
     * @return REMIND
     *
     * @throws SystemException REMIND
     */
    public static IUser createUser(IUserPrincipal   wauser,
                                   HttpSession session,
                                   String      password)
        throws SystemException {
        if (DEBUG) {
            System.out.println("*************** UserManager: wauser = " + wauser.getName());
        }
        System.out.println("Create user Tenant Name : "+ wauser.getTenantName());
        TenantAttributes tenantAtt = getTenantAttr(wauser.getTenantName());
        if(null == tenantAtt) System.out.println("Tenant Attributes is empty");
        User user = new User(wauser, session, password, tenantAtt.getTenantMain());
        users.put(getUserKey(user), user);

        return user;
    }

    /**
     * Saves the user object to the persistent store.
     *
     * @param user REMIND
     *
     * @throws SystemException REMIND
     */
    public static void saveUser(IUser user)
        throws SystemException {
        if (DEBUG) {
            System.out.println("UserManager: user = " + user);
        }

        User userobj = (User) users.get(getUserKey(user));
        userobj.save();
    }

    /**
     * This method removes a user object from the list of users currently logged in.
     *
     * @param user the user's login
     *
     * @throws SystemException REMIND
     */
    public static void removeUser(IUser user)
        throws SystemException {
        String key = getUserKey(user);
        removeUserByKey(key);
    }

    static void removeUserByKey(String key)
        throws SystemException {
        User userobj = (User) users.get(key);

        if (userobj != null) {
            userobj.destroy();
            users.remove(key);
        }
    }

    static String getUserKey(IUser iuser) {
        User   user = (User) iuser;
        String key = user.getName() + "_" + user.getSession()
                                                .getId();

        if (DEBUG) {
            System.out.println("UserManager: user key = " + key);
        }

        return key;
    }

    /**
     * REMIND
     */
    public static void destroy() {
        if (DEBUG) {
            System.out.println("UserManager:destroy " + users.size());
        }

        Collection userList = users.values();

        for (Iterator ite = userList.iterator(); ite.hasNext();) {
            User userobj = (User) ite.next();
            userobj.destroy();
            users.remove(userobj);
        }

        users.clear();
        users = null;

        if (DEBUG) {
            System.out.println("UserManager: destroy complete");
        }
    }
}
