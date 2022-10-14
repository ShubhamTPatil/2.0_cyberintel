// Copyright 1997-2005, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.intf;

/**
 * Interface to be implemented by CacheManager 
 * @author Manoj Kumar
 * @version $Revision$Date:
 */
public interface ICacheManager {

    /**
     * Get a qry object from cache
     */
    public IComplianceQuery get(IComplianceQuery qry);

    /**
     * Get a qry object from cache, if not there, create it
     */
    // public IComplianceQuery getCreate(IComplianceQuery qry);

    /**
     * Put a qry object into cache
     */
    public void put(IComplianceQuery qry);

    /**
     * Flush all objects in the cache.
     */
    public void clear();

    /**
     * Expire an object in the Cache
     */
    public void expire(IComplianceQuery qry);

    /**
     * Shutdown the cache
     */
    public void shutDownCache();
}
