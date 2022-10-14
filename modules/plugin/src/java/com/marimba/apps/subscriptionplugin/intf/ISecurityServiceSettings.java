// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
package com.marimba.apps.subscriptionplugin.intf;

import com.marimba.intf.util.*;
import com.marimba.castanet.tools.*;
import com.marimba.apps.subscriptionplugin.ISubsPluginContext;
import com.marimba.apps.subscriptionplugin.SecurityScanReportQueue;


/**
 * Interface representing vInspector settings.
 */

public interface ISecurityServiceSettings extends IObserver {

	IDataStorage getStorage();

    IDataStorage getStorage(String type);

    String getDataDir();

    boolean needToForwardRequests();

    PluginConnection getFreshPluginConnection();

    ISubsPluginContext getPluginContext();

    String getNewGUID();

    SecurityScanReportQueue getSecurityScanReportQueue();

    String getPluginSubscribeCredentials();

}

