// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.cache;

/**
 * Cache refresh policy for compliance results
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import com.opensymphony.oscache.base.EntryRefreshPolicy;
import com.opensymphony.oscache.base.CacheEntry;

import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceResult;

public class ComplianceRefreshPolicy implements EntryRefreshPolicy {

    public ComplianceRefreshPolicy() {
    }

    public boolean needsRefresh(CacheEntry entry) {
	Object obj = entry.getContent();
	if (obj instanceof IComplianceResult) {
	    IComplianceResult result = (IComplianceResult) obj;
	    long nowTime = System.currentTimeMillis();
	    return result.getExpireTime() < nowTime;
	} else {
	    return false;
	}
    }
}
