// Copyright 1997-2005, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.cache;

import com.marimba.apps.subscriptionmanager.compliance.intf.ICacheManager;
import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceResult;
import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceQuery;
import com.marimba.apps.subscriptionmanager.compliance.result.CachableResult;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;

// import com.opensymphony.oscache.base.*;
// import com.opensymphony.oscache.general.*;
// import org.apache.log4j.*;

import java.util.HashMap;

/**
 * Provide in-memory caching for compliance query results
 *
 * @author  Manoj Kumar
 * @version $Revision$Date:
 *
 */
public class ComplianceCacheMgr implements ICacheManager {

    private static final boolean DEBUG_MODE = IAppConstants.DEBUG;

    // In-memory map for everything
    HashMap admin;
    CacheTimer timer;

    /**
     * Create the cache
     */
    public ComplianceCacheMgr(){
        admin = new HashMap();
        timer = new CacheTimer(this);
    }


    /**
     * Shutdown the cache
     */
    public void shutDownCache() {
	    timer.shutdown();
    }


    /**
     * Calculate next expire time
     * return true when everything is fine
     * return false when something is wrong
     */
    IComplianceQuery checkExpire(IComplianceQuery cached) {
        if (cached != null) {
            IComplianceResult res = cached.getResult();
            if (res == null || !(res instanceof CachableResult)) {
                expire(cached);
                cached = null;
            } else {
                // Caculate next expire time
                long expiredTime = res.touch();
                timer.addExpireTask(expiredTime, cached);
            }
        }
        return cached;
    }

    /**
     * Get from cache, return null if it's not there
     */
    public IComplianceQuery get(IComplianceQuery qry) {
        synchronized (admin) {
            return checkExpire((IComplianceQuery) admin.get(qry));
        }
    }

    /**
     * Put a query object into cache
     */
    public void put(IComplianceQuery qry) {
        if (qry != null) {
            synchronized (admin) {
                if (checkExpire(qry) != null) {
                    admin.put(qry, qry);
                }
            }
	    }
    }

   /**
    * Clear all objects in the cache
    */
    public void clear() {
        synchronized (admin) {
            admin.clear();
        }
    }

    /**
     * Expire a single object from the cache.
     */
    public void expire(IComplianceQuery qry) {
        synchronized (admin) {
            admin.remove(qry);
        }
    }
}
