// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionplugin;

import com.marimba.tools.util.DebugFlag;

/**
 * Constants for using in the debug statements.
 */
public interface IPluginDebug {
    /** Turn debuging on or off. */
    boolean RESERVEDDEBUG = DebugFlag.getDebug("SECURITY/PLUGIN") >= 1;
    boolean ERROR = DebugFlag.getDebug("SECURITY/PLUGIN") >= 2;
    boolean HIGH_PRIORITY_INFO = DebugFlag.getDebug("SECURITY/PLUGIN") >= 2;
    boolean WARNING = DebugFlag.getDebug("SECURITY/PLUGIN") >= 3;
    boolean INFO = DebugFlag.getDebug("SECURITY/PLUGIN") >= 4;
    boolean DETAILED_INFO = DebugFlag.getDebug("SECURITY/PLUGIN") >= 5;
}
