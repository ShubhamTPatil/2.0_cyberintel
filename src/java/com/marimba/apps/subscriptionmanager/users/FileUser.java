// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.users;

import java.io.*;

import java.util.*;

import com.marimba.apps.subscription.common.intf.*;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.intf.*;
import com.marimba.apps.subscriptionmanager.intf.users.*;

import com.marimba.tools.util.Props;

import com.marimba.webapps.intf.*;

/**
 * File based persistent store for a User object
 *
 * @author Theen-Theen Tan
 * @version 1.0, 11/21/2001
 */
public class FileUser
    implements IUserDataSource,
                   IDebug {
    File userFile = null;
    String CANTLOAD_OBJECT_SUBISNOTLADP  ="CantLoadObject--SubIsNotInLDAP";
    String  CANTDELETE_OBJECT_PREROBJECTNOTINSTORAGE="CantDeleteObject--PrefObjectNotInStorage";
    SubscriptionMain subMain;
    /**
     * Create the data source.  The creation is done by DataSourceFactory.
     */
    public FileUser(SubscriptionMain main) {
    	this.subMain = main;
        if (DEBUG) {
            debug("FileUser created");
        }
    }

    /**
     * REMIND
     *
     * @param obj REMIND
     *
     * @throws SystemException REMIND
     */
    public void open(IUserDataSourceContext obj)
        throws SystemException {
        String dn = null;
        System.out.println("User Data Directory :" + subMain.getDataDirectory());
        userFile = new File(new File(subMain.getDataDirectory(), IAppConstants.USER_DIR), obj.getName());
        
        if (DEBUG) {
            debug("userFile, " + userFile.getAbsolutePath());
        }

        if (userFile.exists()) {
            obj.setExists(true);

            if (DEBUG) {
                debug("FileUser, open, exists: " + obj.getName());
            }
        } else {
            if (DEBUG) {
                debug("FileUser, open, new: " + obj.getName());
            }
        }

        Props props = new Props(userFile);
        obj.setProps(props);
    }

    /**
     * REMIND
     *
     * @param obj REMIND
     *
     * @throws SystemException REMIND
     * @throws SubInternalException REMIND
     */
    public void load(IUserDataSourceContext obj)
        throws SystemException {
        if (!obj.exists()) {
            throw new SubInternalException(CANTLOAD_OBJECT_SUBISNOTLADP);
        }

        if (DEBUG) {
            debug("Loading from " + userFile.getAbsolutePath());
        }

        obj.getProps()
           .load();
    }

    /**
     * REMIND
     *
     * @param obj REMIND
     *
     * @throws SystemException REMIND
     */
    public void save(IUserDataSourceContext obj)
        throws SystemException {
        boolean saved = obj.getProps()
                           .save();
        obj.setExists(true);

        if (DEBUG) {
            debug("saved " + saved);
        }
    }

    /**
     * REMIND
     *
     * @param obj REMIND
     *
     * @throws SystemException REMIND
     * @throws SubInternalException REMIND
     */
    public void delete(IUserDataSourceContext obj)
        throws SystemException {
        if (!obj.exists()) {
            throw new SubInternalException(CANTDELETE_OBJECT_PREROBJECTNOTINSTORAGE);
        }

        obj.getProps()
           .getFile()
           .delete();
    }

    /**
     * REMIND
     *
     * @return REMIND
     *
     * @throws SystemException REMIND
     */
    public String[] list()
        throws SystemException {
        return userFile.getParentFile()
                       .list();
    }

    /**
     * Returns a string representation of the props source for debugging etc.
     *
     * @return REMIND
     */
    public String toString() {
        return "FileUserSource: ";
    }

    /**
     * REMIND
     *
     * @param msg REMIND
     */
    public void debug(String msg) {
        System.out.println(msg);
    }
}
