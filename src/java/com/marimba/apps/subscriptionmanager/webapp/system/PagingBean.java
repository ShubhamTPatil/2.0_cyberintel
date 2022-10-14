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
 * PagingBean is used by the GetNextPrevious tag.  It stores the current start index being viewed, the total number of results, and the number to display per
 * page.
 *
 * @author Michele Lin
 * @version 1.5, 01/30/2002
 */
public class PagingBean
    implements ISubscriptionConstants,
                   IWebAppConstants {
    int     total = 0;
    int     startIndex = 0;
    int     countPerPage = DEFAULT_COUNT_PER_PAGE;
    boolean hasMore = false;

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
     * @param more REMIND
     */
    public void setHasMoreResults(boolean more) {
        this.hasMore = more;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public boolean getHasMoreResults() {
        return this.hasMore;
    }
}
