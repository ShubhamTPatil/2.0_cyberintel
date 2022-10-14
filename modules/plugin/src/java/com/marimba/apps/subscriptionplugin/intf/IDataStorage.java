// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
package com.marimba.apps.subscriptionplugin.intf;

import java.util.*;
import java.util.HashMap;

import com.marimba.apps.subscriptionplugin.SecurityComplianceBean;
import com.marimba.apps.subscriptionplugin.LogConstants;
import com.marimba.apps.subscriptionplugin.IPluginDebug;

/**
 * An interface representing a data storage for vInspector Scan related information. This can be a File, Database or Big Data.
 *
 */
public interface IDataStorage extends LogConstants, IPluginDebug {
    public boolean insertScanDetails(ArrayList<SecurityComplianceBean> securityComplianceBeans);
    public boolean insertScanDetails(ArrayList<SecurityComplianceBean> securityComplianceBeans, String securityComplianceForMachine);
    public boolean insertScanDetails(ArrayList<SecurityComplianceBean> securityComplianceBeans, String securityComplianceForMachine, HashMap<String, String> moreInfo);
}
