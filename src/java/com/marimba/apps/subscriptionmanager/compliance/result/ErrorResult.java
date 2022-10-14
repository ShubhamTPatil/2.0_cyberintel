// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.result;

/**
 * Result object for sql exception
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */

import com.opensymphony.oscache.base.EntryRefreshPolicy;
import com.opensymphony.oscache.base.CacheEntry;
import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceResult;
import com.marimba.intf.msf.query.IQueryResult;

import java.sql.SQLException;
import java.sql.ResultSet;

public class ErrorResult extends CachableResult {

    Exception exception;

    public ErrorResult(Exception e) {
	this.exception = e;
    }

    public Exception getException() {
	return exception;
    }

    public void fetch(IQueryResult rs) throws SQLException {
        //nothing for this class
    }

}
