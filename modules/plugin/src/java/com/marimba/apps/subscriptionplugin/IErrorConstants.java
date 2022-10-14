// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionplugin;

import com.marimba.apps.subscription.common.intf.ICommonErrorConstants;

/**
 * Interface which should contain the error constants used by the vDesk
 */
public interface IErrorConstants
    extends ICommonErrorConstants {
    String PLUGIN_INIT_LDAP_FAILED = "error.plugin.init.ldap.failed";
    String PLUGIN_GET_DATA_FAILED = "error.plugin.get.data.failed";
}
