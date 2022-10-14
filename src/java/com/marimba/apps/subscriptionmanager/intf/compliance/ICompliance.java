//Copyright 1997-2003, Marimba Inc. All Rights Reserved.
//Confidential and Proprietary Information of Marimba, Inc.
//Protected by or for use under one or more of the following patents:
//U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
//and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.intf.compliance;

/**
 * Compliance object
 *
 * @author  
 * @version $Revision$, $Date$ 
 */
public interface ICompliance {
    /**
     * Return the total count of endpoints under the given target
     *
     * @return the total count of endpoints under the given target
     */    
    public long getTotal();

    /**
     * Returns the number of targets that have succeeded
     * 
     * @return the number of targets that have succeeded
     */    
    public long getSucceeded();

    /**
     * Returns the number of targets that have failed
     * 
     * @return the number of targets that have failed
     */    
    public long getFailed();

    /**
     * Returns the number targets that have not checked in
     * 
     * @return the number targets that have not checked in
     */    
    public long getNotCheckedIn();
}
