// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.result;

/**
 * Super class for all compliance related list query results
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import java.sql.SQLException;
import java.sql.ResultSet;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceListResult;
import com.marimba.intf.msf.query.IQueryResult;

public abstract class ListResult extends CachableResult implements IComplianceListResult {

    // Page size
    int pageSize;

    // Start page
    int startPage;

    // End page
    int endPage;

    // Results
    ArrayList list;

    // total count and page
    int totalCount;
    int totalPage;

    /**
     * Get result
     */
    public List get(int page) {
	if (page >= startPage && page <= endPage) {
	    // REMIND do we really need to do this useless copy?
	    // or should we move this logic out?
	    ArrayList res = new ArrayList();
	    int startIndex = (page - startPage) * pageSize;
	    int endIndex = startIndex + pageSize;
	    for (int i = startIndex; i < endIndex && i < list.size(); i++) {
		res.add(list.get(i));
	    }
	    return res;
	} else {
	    return null;
	}
    }

    // Start page
    public int getStartPage() {
	return startPage;
    }

    public void setStartPage(int startPage) {
	this.startPage = startPage;
    }

    // End page
    public int getEndPage() {
	return endPage;
    }

    public void setEndPage(int endPage) {
	this.endPage = endPage;
    }

    // Page size
    public int getPageSize() {
	return pageSize;
    }

    public void setPageSize(int pageSize) {
	this.pageSize = pageSize;
    }

    // Total count
    public int getTotalCount() {
	return totalCount;
    }

    public void setTotalCount(int totalCount) {
	this.totalCount = totalCount;
    }

    // Total page
    public int getTotalPage() {
	return totalPage;
    }

    public void setTotalPage(int totalPage) {
	this.totalPage = totalPage;
    }

    /**
     * Fetch each row
     */
    public abstract void fetchRow(IQueryResult rs) throws SQLException;

    /**
     * Fetch results from result set
     */
    public void fetch(IQueryResult rs) throws SQLException {
	// REMIND, we should have a better way doing this :)

	list = new ArrayList();
	
	int startIndex = startPage * pageSize;
	int endIndex = endPage * pageSize;
	int index = 0;

	while (index < startIndex) {
	    if (!rs.next()) {
		return;
	    }
	    index++;
	}
	
	while (index < endIndex && rs.next()) {
	    fetchRow(rs);
	    index++;
	}
    }

    public Iterator iterator(){
        Iterator it = null;
        if(null != list){
            it = list.iterator();
        }

        return it;
    }
}
