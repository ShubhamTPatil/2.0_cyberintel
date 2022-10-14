// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.intf;

/**
 * Interface for all compliance report object to implement
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import com.marimba.intf.msf.IUserPrincipal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Date;

public interface IComplianceReport {

    /**
     * Execute the query and returns result object
     */
    public void execute(Connection con) throws SQLException;

    /**
     * State of the query (in-queue, in-query, done)
     */
    public void setState(int state);

    public int getState();

    /**
     * Get target
     */
    public String getTarget();

    /**
     * Start and end time in queue
     */
    public Date getStartTime();

    public void setStartTime(Date date);

    public Date getEndTime();

    public void setEndTime(Date date);

    public IUserPrincipal getUser();

    public void setUser( IUserPrincipal user );
}
