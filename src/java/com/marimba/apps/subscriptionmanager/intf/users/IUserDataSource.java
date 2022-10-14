// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.intf.users;

import java.io.File;

import com.marimba.apps.subscriptionmanager.intf.users.IUserDataSourceContext;

import com.marimba.webapps.intf.SystemException;

/**
 * Interface to a persistent store for the Subscription object. Could be implemeted in a file system, LDAP directory, RDBMS etc.
 *
 * @author Theen-Theen Tan
 * @version 1.0, 11/21/2001
 */
public interface IUserDataSource {
    /**
     * Open this data source to read and write a specific persistent object If the subscription object exists in the persistent storage, the object's id,
     * target's type, and target's id are set. If this is a new object, a new id for the subscription is created. This method will try to derive the target
     * name and target type from the subscription's name if the target name and target type are not already specified in the object.  In this case the
     * subscription name is expected to be in &lt;targetname>_&lt;targettype> format.
     *
     * @param obj the subscription to access
     */
    public void open(IUserDataSourceContext obj)
        throws SystemException;

    /**
     * Load Subscription object, including creating the subcomponents ScheduleGroup, Channels, Phases, etc. open() be called before this method is called.
     * Object must be labeled as exists().
     *
     * @param obj the subscription object to be loaded
     * @param return void
     */
    public void load(IUserDataSourceContext obj)
        throws SystemException;

    /**
     * Save data from the subscrpiption object to persistent store
     *
     * @param obj the subscription object to be loaded
     * @param return void
     */
    public void save(IUserDataSourceContext obj)
        throws SystemException;

    /**
     * Delete the object representing these properties from the persistent store.
     *
     * @param obj the subscription object to be loaded
     * @param return void
     */
    public void delete(IUserDataSourceContext obj)
        throws SystemException;

    /**
     * List all subscription available in the storage Ignore all subscription objects that don't have names that conform to the  &lt;target>_&lt;type> naming
     * convention.
     *
     * @return a list of names of the subscription available in the system
     */
    public String[] list()
        throws SystemException;
}
