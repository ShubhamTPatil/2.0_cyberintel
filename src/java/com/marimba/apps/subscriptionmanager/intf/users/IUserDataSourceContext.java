// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.intf.users;

import com.marimba.apps.subscription.common.intf.IUser;

import com.marimba.tools.util.Props;

import com.marimba.webapps.intf.SystemException;

/**
 * An interface implemented by user that containing methods which can be accessed by the object's datasource.
 *
 * @author Theen-Theen Tan
 * @version 1.1, 09/11/2002
 */
public interface IUserDataSourceContext
    extends IUser {
    /**
     * Returns true if the user object exists in persistant storage
     *
     * @return true if the user object exists in persistant storage
     */
    public boolean exists();

    /**
     * Indicates that the user exists in persistant storage
     *
     * @param ex true or false
     */
    public void setExists(boolean ex);

    /**
     * Indicates that the user exists in persistant storage
     *
     * @return void
     */
    public Props getProps();

    /**
     * Indicates that the user exists in persistant storage
     *
     * @param ex true or false
     */
    public void setProps(Props props);
}
