// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.system;

import java.io.*;

import java.net.*;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.marimba.apps.subscription.common.*;
import com.marimba.apps.subscription.common.intf.*;
import com.marimba.apps.subscription.common.objects.*;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.intf.*;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.*;

import com.marimba.tools.util.Password;

import com.marimba.webapps.intf.*;

/**
 * This bean captures the state of the current distribution construction. This bean will be stored in the session of the user.  It will be used when creating a
 * distribution assignment from the targets view or application view. Before these methods are called, verification should have already taken place in the
 * forms.
 *
 * @author Angela Saval
 * @author Theen-Theen Tan
 */
public class TLoginBean
    implements ISubscriptionConstants,
                   IAppConstants,
                   IErrorConstants {
    /* The properties for transmitter login are saved in the format
     * marimba.keychain.<transmitter name>.user
     * marimba.keychain.<transmitter name>.password
     * These two hashtables keep track of the properties
     */
    Hashtable addedTUsers = new Hashtable(10);
    Hashtable addedTPwds = new Hashtable(10);

    /* If a transmitter is removed from the list of transmitters, it is
     * added to this hashtable so that the transmitters can be
     * removed from the subscription loaded before saving
     */
    Hashtable remTrans = new Hashtable(10);

    /**
     * REMIND
     *
     * @param transmitter REMIND
     * @param user REMIND
     *
     * @throws SystemException REMIND
     * @throws InternalException REMIND
     */
    public void addTUser(String transmitter,
                         String user)
        throws SystemException {
        if (transmitter == null) {
            throw new InternalException(GUIUTILS_INTERNAL_WRONGARG, "TLogin.addTUser", "transmitter");
        }

        if (user == null) {
            throw new InternalException(GUIUTILS_INTERNAL_WRONGARG, "TLogin.addTUser", "user");
        }

        //need add sorted.
        addedTUsers.put(transmitter, user);
    }

    /**
     * REMIND
     *
     * @param transmitter REMIND
     * @param password REMIND
     *
     * @throws SystemException REMIND
     * @throws InternalException REMIND
     */
    public void addTPwd(String transmitter,
                        String password)
        throws SystemException {
        if (transmitter == null) {
            throw new InternalException(GUIUTILS_INTERNAL_WRONGARG, "TLogin.addTPwd", "transmitter");
        }

        if (password == null) {
            throw new InternalException(GUIUTILS_INTERNAL_WRONGARG, "TLogin.addTPwd", "password");
        }

        //REMIND add sorted
        addedTPwds.put(transmitter, password);
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public Enumeration getTransmitters() {
        return addedTUsers.keys();
    }

    /**
     * REMIND
     *
     * @param trans REMIND
     *
     * @return REMIND
     */
    public String getUser(String trans) {
        return (String) addedTUsers.get(trans);
    }

    /**
     * REMIND
     *
     * @param trans REMIND
     *
     * @return REMIND
     */
    public String getPwdDecode(String trans) {
        String pwd = (String) addedTPwds.get(trans);

        return Password.decode(pwd);
    }

    /**
     * REMIND
     *
     * @param trans REMIND
     *
     * @return REMIND
     */
    public String getPwd(String trans) {
        String pwd = (String) addedTPwds.get(trans);

        return pwd;
    }

    /**
     * REMIND
     *
     * @param transmitter REMIND
     * @param user REMIND
     * @param password REMIND
     *
     * @throws SystemException REMIND
     * @throws InternalException REMIND
     */
    public void setTUserAndPwd(String transmitter,
                               String user,
                               String password)
        throws SystemException {
        if (transmitter == null) {
            throw new InternalException(GUIUTILS_INTERNAL_WRONGARG, "TLogin.setTUserAndPwd", "transmitter");
        }

        if (user == null) {
            throw new InternalException(GUIUTILS_INTERNAL_WRONGARG, "TLogin.setTUserAndPwd", "user");
        }

        if (password == null) {
            throw new InternalException(GUIUTILS_INTERNAL_WRONGARG, "TLogin.setTUserAndPwd", "password");
        }

        addedTUsers.put(transmitter, user);
        addedTPwds.put(transmitter, password);
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public Enumeration getDeletedTrans() {
        return remTrans.keys();
    }

    /**
     * REMIND
     *
     * @param transmitter REMIND
     *
     * @throws SystemException REMIND
     * @throws InternalException REMIND
     */
    public void delTUserAndPwd(String transmitter)
        throws SystemException {
        if (transmitter == null) {
            throw new InternalException(GUIUTILS_INTERNAL_WRONGARG, "TLogin.delTUserAndPwd", "transmitter");
        }

        remTrans.put(transmitter, transmitter);
        addedTUsers.remove(transmitter);
        addedTPwds.remove(transmitter);
    }

    /**
     * REMIND
     */
    public void debug() {
        String transmitter;

        for (Enumeration e = addedTUsers.keys(); e.hasMoreElements();) {
            transmitter = (String) e.nextElement();
            System.out.println("TLoginBean: transmitter3 = " + transmitter);
            System.out.println("TLoginBean: user3 = " + (String) addedTUsers.get(transmitter));
        }

        for (Enumeration e1 = addedTPwds.keys(); e1.hasMoreElements();) {
            transmitter = (String) e1.nextElement();
            System.out.println("TLoginBean: transmitter = " + transmitter);
        }

        for (Enumeration e2 = remTrans.keys(); e2.hasMoreElements();) {
            transmitter = (String) e2.nextElement();
            System.out.println("TLoginBean: removed transmitter = " + transmitter);
        }
    }
}
