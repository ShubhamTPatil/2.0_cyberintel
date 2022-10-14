// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.intf;

/**
 * Interface for all compliance query object to implement
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import com.marimba.intf.msf.IUserPrincipal;
import com.marimba.intf.msf.ITenant;
import java.util.Locale;

public interface IComplianceQuery {

    /**
     * Execute the query and returns result object
     */
    public IComplianceResult executeQuery();

    /**
     * State of the query (in-queue, in-query, done)
     */
    public void setState(int state);
    public int getState();




    /**
     * Start/end time of the query
     */
    public void setStartTime(long startTime);
    public long getStartTime();

    public void setEndTime(long endTime);
    public long getEndTime();

    /**
     * Get query result, null when query is not done
     */
    public IComplianceResult getResult();

    public void waitForResult(long secondsToWait);

    public IUserPrincipal getUser();

    public void setUser( IUserPrincipal user );

    public void setUser( IUserPrincipal user, Locale userLocale );

    public void setLocale( Locale userLocale );
    
    public ITenant getTenant();

}
