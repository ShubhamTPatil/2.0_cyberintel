// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.intf;

import com.opensymphony.oscache.base.*;
import com.marimba.intf.msf.query.IQueryResult;

import java.sql.*;

/**
 * Interface for all compliance result object to implement
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */

public interface IComplianceResult {

    /**
     * Init this object for cache, return expire time for this result
     */
    public long initForCache(long maxCacheTime, long touchExtension);

    /**
     * Touch the result, return the new expire time for this result
     */
    public long touch();

    /**
     * Get expire time
     */
    public long getExpireTime();
	
    /**
     * Get last access time
     */
    public long getLastAccessTime();

    /**
     * Get create time
     */
    public long getCreatedTime();

    public void fetch(IQueryResult rs) throws SQLException;

}
