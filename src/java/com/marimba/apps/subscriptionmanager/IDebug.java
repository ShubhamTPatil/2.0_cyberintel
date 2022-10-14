// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager;

/**
 * Debug interface.  This interface centrally turn on or off debugging for the package.
 *
 * @author Simon Wynn
 * @version 1.4, 09/10/2001
 */
public interface IDebug {
    /** Turn debuging on or off. */
    final boolean DEBUG = true;

    /**
     * Display the debug information based on debug level
     */
    void debug(String str);
}
