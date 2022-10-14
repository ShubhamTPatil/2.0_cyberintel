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

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.*;

/**
 * GenericPagingBean is used by the generic Next/Previous implementation. It stores the current start index being viewed, the total number of results, and the
 * number of results to display per page. It is different from the PagingBean used for paging through LDAP results in that it also stores the complete result
 * set.
 *
 * @author Michele Lin
 * @version 1.4, 05/04/2002
 */
public class GenericPagingBean
    implements ISubscriptionConstants,
                   IWebAppConstants {
    int            startIndex = 0;
    int            endIndex = 0;
    int            total = 0;
    int            countPerPage = DEFAULT_COUNT_PER_PAGE;
    java.util.List results;

    /**
     * REMIND
     *
     * @param startIndex REMIND
     */
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public int getStartIndex() {
        return this.startIndex;
    }

    /**
     * REMIND
     *
     * @param endIndex REMIND
     */
    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public int getEndIndex() {
        return this.endIndex;
    }

    /**
     * REMIND
     *
     * @param total REMIND
     */
    public void setTotal(int total) {
        this.total = total;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public int getTotal() {
        return this.total;
    }

    /**
     * REMIND
     *
     * @param countPerPage REMIND
     */
    public void setCountPerPage(int countPerPage) {
        this.countPerPage = countPerPage;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public int getCountPerPage() {
        return this.countPerPage;
    }

    /**
     * REMIND
     *
     * @param results REMIND
     */
    public void setResults(java.util.List results) {
        this.results = results;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public java.util.List getResults() {
        return this.results;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getCurrentPageString() {
        return new Integer(calculatePageNumber(startIndex, DEFAULT_COUNT_PER_PAGE)).toString();
    }

    private int calculatePageNumber(int startIndex,
                                    int pageSize) {
        if (startIndex == 0) {
            startIndex = 1;
        }

        double rem = java.lang.Math.IEEEremainder(startIndex, pageSize);
        int    pageNo = ((startIndex + 1) / pageSize);

        if (rem > 0) {
            pageNo++;
        }

        return pageNo;
    }
}
