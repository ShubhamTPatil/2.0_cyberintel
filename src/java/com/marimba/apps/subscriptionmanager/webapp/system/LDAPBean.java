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

import com.marimba.apps.subscription.*;
import com.marimba.apps.subscription.common.*;
import com.marimba.apps.subscription.common.objects.*;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.webapp.forms.*;

import com.marimba.tools.ldap.*;

/**
 * This bean captures information for the ldap navigation page.  It includes the baseURL (or the page to redirect to), container (for bread crumb) and the type
 * of object being viewed (for example, machines, groups, collections, or people)
 *
 * @author Michele Lin
 */
public class LDAPBean
    implements ISubscriptionConstants {
    String baseURL;
    String container;
    String objectClass;
    String currentDomain;

    // used as an indicator for generic paging to page through page members
    boolean paging;

    // used as an indicator that a group is being browsed
    boolean isGroup;

    //This indicates the group whose members are to be searched
    String group;

    // used for groups and paging to leave the bread crumb alone    
    boolean         leaveBreadCrumb;
    String          entryPoint;
    LDAPPagedSearch paged;
    String          usersInLDAP;
    String          host;
    String          history;
    String          searchText;

    /**
     * REMIND
     *
     * @param baseURL REMIND
     */
    public void setBaseURL(String baseURL) {
        // remove '/sm/servlet' from baseURL
        if (baseURL.startsWith("/servlet/sm")) {
            baseURL = baseURL.substring(11);
        }

        this.baseURL = baseURL;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getBaseURL() {
        return this.baseURL;
    }

    /**
     * REMIND
     *
     * @param container REMIND
     */
    public void setContainer(String container) {
        this.container = container;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getContainer() {
        return this.container;
    }

    /**
     * REMIND
     *
     * @param paging REMIND
     */
    public void setUseLDAPPaging(boolean paging) {
        this.paging = paging;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public boolean getUseLDAPPaging() {
        return this.paging;
    }

    /**
     * REMIND
     *
     * @param isGroup REMIND
     */
    public void setIsGroup(boolean isGroup) {
        this.isGroup = isGroup;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public boolean getIsGroup() {
        return this.isGroup;
    }

    /**
     * Used to obtain the group name that is used for the LDAPSearchAction.
     *
     * @param group REMIND
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getGroup() {
        return this.group;
    }

    /**
     * REMIND
     *
     * @param leaveBreadCrumb REMIND
     */
    public void setLeaveBreadCrumb(boolean leaveBreadCrumb) {
        this.leaveBreadCrumb = leaveBreadCrumb;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public boolean getLeaveBreadCrumb() {
        return this.leaveBreadCrumb;
    }

    /**
     * REMIND
     *
     * @param entryPoint REMIND
     */
    public void setEntryPoint(String entryPoint) {
        this.entryPoint = entryPoint;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getEntryPoint() {
        return this.entryPoint;
    }

    /**
     * REMIND
     */
    public void clearEntryPoint() {
        entryPoint = null;
    }

    /**
     * REMIND
     *
     * @param paged REMIND
     */
    public void setPagedSearch(LDAPPagedSearch paged) {
        this.paged = paged;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public LDAPPagedSearch getPagedSearch() {
        return this.paged;
    }

    /**
     * REMIND
     *
     * @param usersInLDAP REMIND
     */
    public void setUsersInLDAP(String usersInLDAP) {
        this.usersInLDAP = usersInLDAP;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getUsersInLDAP() {
        return usersInLDAP;
    }

    /**
     * REMIND
     *
     * @param history REMIND
     */
    public void setHistory(String history) {
        this.history = history;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getHistory() {
        return this.history;
    }

    /**
     * REMIND
     *
     * @param objectClass REMIND
     */
    public void setObjectClass(String objectClass) {
        this.objectClass = objectClass;
    }

    /**
     * REMIND
     */
    public void clearObjectClass() {
        this.objectClass = null;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getObjectClass() {
        return this.objectClass;
    }

    /**
     * REMIND
     *
     * @param currentDomain REMIND
     */
    public void setCurrentDomain(String currentDomain) {
        this.currentDomain = currentDomain;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getCurrentDomain() {
        return this.currentDomain;
    }

    /**
     * REMIND
     */
    public void clearCurrentDomain() {
        this.currentDomain = null;
    }

    /**
     * REMIND
     *
     * @param searchText REMIND
     */
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getSearchText() {
        return this.searchText;
    }
}
