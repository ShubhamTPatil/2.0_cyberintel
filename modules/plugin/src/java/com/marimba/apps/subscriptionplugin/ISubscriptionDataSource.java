// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionplugin;

import com.marimba.intf.castanet.IIndex;

import com.marimba.intf.plugin.IRequest;

/**
 * Subscription data source interface
 */
public interface ISubscriptionDataSource {
    /**
     * Get data from the data source.  Returns the new index if successful, or null if something went wrong.
     */
    IIndex getData(IRequest request);

    /**
     * Close the underlying data source
     */
    void close();
}
