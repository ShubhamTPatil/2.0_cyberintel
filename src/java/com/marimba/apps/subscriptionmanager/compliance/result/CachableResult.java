// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.result;

/**
 * Super class for all compliance related query results
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceResult;
import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;
import com.opensymphony.oscache.base.EntryRefreshPolicy;

public abstract class CachableResult implements IComplianceResult, ComplianceConstants {

    // Max cache time
    protected long maxCacheTime = 0;

    // Extension of cache life for each touch
    protected long touchExtension = 0;

    // the time to expire no matter what
    protected long maxExpiredTime = 0;

    // expire time
    protected long expireTime = 0;

    // the last touch time
    protected long lastTouchTime = 0;

    // the created time
    protected long createdTime = 0;

    /**
     * Init for cache
     */
    public long initForCache(long maxCacheTime, long touchExtension) {
	this.touchExtension = touchExtension;
	this.maxCacheTime = maxCacheTime;
	createdTime = System.currentTimeMillis();
	lastTouchTime = createdTime;
	maxExpiredTime = createdTime + maxCacheTime;
	expireTime = createdTime + touchExtension;
	return expireTime;
    }

    /**
     * Extend the life of the result a little bit, but don't exceed the max time
     */
    public long touch() {
	lastTouchTime = System.currentTimeMillis();

	if (lastTouchTime + touchExtension < maxExpiredTime) {
	    expireTime = lastTouchTime + touchExtension;
	} else {
	    expireTime = maxExpiredTime;
	}
	return expireTime;
    }

    /**
     * Get expire time
     */
    public long getExpireTime() {
	return expireTime;
    }

    /**
     * Get last touch time
     */
    public long getLastAccessTime() {
	return lastTouchTime;
    }

    /**
     * Get created time
     */
    public long getCreatedTime() {
	return createdTime;
    }

}
