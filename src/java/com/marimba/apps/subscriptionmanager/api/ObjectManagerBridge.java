// Copyright 2009, BMC Software. All Rights Reserved.

// Confidential and Proprietary Information of BMC Software.

// Protected by or for use under one or more of the following patents:

// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,

// and 6,430,608. Other Patents Pending.

// $File: //depot/ws/products/subscriptionmanager/8.0.00/com/marimba/apps/subscriptionmanager/api

// $, $Revision$, $Date$



package com.marimba.apps.subscriptionmanager.api;



import com.marimba.apps.subscription.common.intf.IObjectManager;

import com.marimba.apps.subscription.common.intf.IUser;

import com.marimba.apps.subscription.common.intf.objects.ISubscription;

import com.marimba.apps.subscriptionmanager.ObjectManager;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;

import com.marimba.apps.subscriptionmanager.users.CLIUser;

import com.marimba.intf.util.IConfig;
import com.marimba.webapps.intf.SystemException;

import com.marimba.intf.msf.IUserPrincipal;

import com.marimba.tools.ldap.LDAPConnUtils;

import com.marimba.tools.ldap.LDAPConstants;

import com.marimba.intf.util.IProperty;

import java.util.Map;


/**

 * Implementation of contract for Policy Object operations to be used in Policy Management API

 *

 * @author Nageswara Rao.V

 * @version  %I%, %G%

 */



public class ObjectManagerBridge implements IObjectManager {



    SubscriptionMain smmain;



    public void init(SubscriptionMain main){

        this.smmain = main;

    }

    /**

     * Bridge for ObjectManager.createSubscription()

     *

     * @param targetid - FQDN of target

     * @param targettype - Target Type

     * @param user - policy user

     */

    public ISubscription createSubscription(String targetid,

                                     String targettype,

                                     IUser  user) throws SystemException {

        return ObjectManager.createSubscription(targetid, targettype, user);

    }



    /**

     * Bridge for ObjectManager.getSubscription()

     *

     * @param subname - Name of the subscription object

     * @param user - policy user

     */

    public ISubscription getSubscription(String subname,

                                  IUser  user) throws SystemException {

        return ObjectManager.getSubscription(subname, user);

    }



    /**

     * Bridge for ObjectManager.getSubscription()

     *

     * @param targetid - FQDN of target

     * @param targetid - Target Type

     * @param user - policy user

     */

    public ISubscription getSubscription(String targetid,

                                  String targettype,

                                  IUser  user)throws SystemException {

        return ObjectManager.getSubscription(targetid, targettype, user);

    }



    /**

     * Bridge for ObjectManager.openSubForRead()

     *

     * @param targetid - FQDN of target

     * @param targetid - Target Type

     * @param user - policy user

     */

    public ISubscription openSubForRead(String targetid,

                                 String targettype,

                                 IUser  user)throws SystemException {

        return ObjectManager.openSubForRead(targetid, targettype, user);

    }



    /**

     * Bridge for ObjectManager.openSubForWrite()

     *

     * @param targetid - FQDN of target

     * @param targetid - Target Type

     * @param user - policy user

     */

    public ISubscription openSubForWrite(String targetid,

                                  String targettype,

                                  IUser  user)throws SystemException {

        return ObjectManager.openSubForWrite(targetid, targettype, user);

    }



    /**

     * Bridge for ObjectManager.openSubForWrite()

     *

     * @param targetName - target name (cn)

     * @param targetType - Target Type

     * @param domain - domain name Ex: bmc.com

     * @param user - policy user

     */

    public ISubscription openSubForWrite(String targetName,

                                         String targetType,

                                         String domain,

                                         IUser user) throws SystemException {



        IProperty ldapCfg = smmain.getLDAPConfig();

        return ObjectManager.openSubForWrite(targetName, targetType, domain, user, LDAPConnUtils.getInstance((IConfig) ldapCfg).isADWithAutoDiscovery(ldapCfg));
/*
        if ("user".equalsIgnoreCase(targetType)) {
            return ObjectManager.openSubForWriteForTargetTypeUser(targetName, targetType, domain, user, LDAPConnUtils.getInstance((IConfig) ldapCfg).isADWithAutoDiscovery(ldapCfg));
        } else {
            return ObjectManager.openSubForWrite(targetName, targetType, domain, user, LDAPConnUtils.getInstance((IConfig) ldapCfg).isADWithAutoDiscovery(ldapCfg));
        }
*/
    }



    /**

     * API to create user

     *

     * @param userPrincipal - FQDN of target

     * @return IUSer - user created with IUserPrincipal

     */

    public IUser createUser(IUserPrincipal userPrincipal)throws SystemException {

        CLIUser u = new CLIUser(userPrincipal, smmain);

	    u.initialize();

	    return u;

    }
    
    /**
     * Method used to log audit messages to common audit log
     *
     * @param id  Message identifier or number
     * @param severity Message Severity
     * @param source Module generating message
     * @param user User doing current action
     * @param message Log message or Throwable to capture exceptions
     */
    public void log(int id, int severity, String source, String user, Object message ) {
        log(id, severity, source, user, message, null);
    }

    /**
     * Additionally you can specify the target module name
     */
    public void log(int id, int severity, String source, String user, Object message, String target ) {
    	smmain.log(id, severity, source, user, message, target);
    }
}

