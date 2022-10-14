// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.util;

import java.util.*;

import com.marimba.apps.subscription.common.ISubscriptionConstants;

import com.marimba.apps.subscriptionmanager.intf.IAppConstants;

/**
 * This class represents one element in a bread crumb trail. A Crumb consists of a pretty-print name, and the action URL to use for the link in the bread crumb
 * trail.
 *
 * @author Michele Lin
 * @version 1.1, 02/20/2002
 */
public class Crumb
    implements ISubscriptionConstants {
    final static boolean DEBUG = IAppConstants.DEBUG;
    String               name;
    String               type;
    String               link;
    String               msgid;

    /**
     * Creates a new Crumb object.
     *
     * @param name REMIND
     * @param type REMIND
     * @param link REMIND
     * @param msgid REMIND
     */
    public Crumb(String name,
                 String type,
                 String link,
                 String msgid) {
        this.name  = name;
        this.type  = type;
        this.link  = link;
        this.msgid = msgid;
    }

    /**
     * Creates a new Crumb object.
     *
     * @param name REMIND
     * @param type REMIND
     * @param link REMIND
     */
    public Crumb(String name,
                 String type,
                 String link) {
        this.name  = name;
        this.type  = type;
        this.link  = link;
        this.msgid = null;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getMsgId() {
        return msgid;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getName() {
        return name;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getType() {
        return type;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getLink() {
        return link;
    }
}
