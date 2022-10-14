// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.intf;

/**
 * Interface for all compliance list result objects to implement
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import java.util.List;

public interface IComplianceListResult extends IComplianceResult {

    /**
     * Get result base on page, first page is 0
     */
    public List get(int page);

    /**
     * Get start page for this result object
     */
    public int getStartPage();

    /**
     * Get end page
     */
    public int getEndPage();
 
    /**
     * Get page size
     */
    public int getPageSize();


}
