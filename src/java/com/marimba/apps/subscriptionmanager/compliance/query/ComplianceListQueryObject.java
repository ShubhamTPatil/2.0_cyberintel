// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.query;

/**
 * Abstract query object for all compliance related queries for list results
 * For example, a list of machines, a list of packages etc...
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;

import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;
import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceQuery;
import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceListResult;
import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceResult;
import com.marimba.apps.subscriptionmanager.compliance.result.ListResult;

public abstract class ComplianceListQueryObject extends ComplianceQueryObject {

    // Number of pages to cache
    int numOfPageToCache;

    // Page size
    int pageSize;

    // The start page index to cache (0 is the first page)
    int startPage;

    public void setPageToGet(int page) {
	    // Let's calculate the start page based on numOfPageToCache;
	    // 0 ~ numOfPageToCache - 1 = 0
	    // numOfPageToCache ~ 2 * numOfPageToCache - 1 = 1
	    // etc...
	    this.startPage = (page / numOfPageToCache) * numOfPageToCache;
    }

    public void setStartPage(int page) {
	    this.startPage = page;
    }

    public int getStartPage() {
	    return startPage;
    }

    public void setNumOfCachedPages(int pages) {
	    this.numOfPageToCache = pages;
    }

    public int getNumOfCachedPages() {
	    return numOfPageToCache;
    }

    public void setPageSize(int pageSize) {
	    this.pageSize = pageSize;
    }

    public int getPageSize() {
	    return pageSize;
    }

    /**
     * Get result
     */
    public IComplianceListResult getListResult() throws SQLException {
	    return (IComplianceListResult) result;
    }

    /**
     * Set page information
     */
    public void setPageInfo(ListResult l) {
	    l.setStartPage(startPage);
	    l.setPageSize(pageSize);
	    l.setEndPage(startPage + numOfPageToCache);
    }
}
