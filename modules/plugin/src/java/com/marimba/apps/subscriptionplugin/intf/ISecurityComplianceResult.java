// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
package com.marimba.apps.subscriptionplugin.intf;

import java.util.List;

/**
 * A class that represents security compliance result information in a tree structure.
 *
 */
public interface ISecurityComplianceResult {
    /**
     * Obtain the root of the tree.
     */
    ISecurityComplianceResultNode findRoot();

    /**
     * Get the node with the given name
     */
    ISecurityComplianceResultNode findNode(String name);

    /**
     * Walk the tree and find all nodes whose names begin with the given
     * string. This is useful to get all nodes of a certain class. (e.g.,
     * machine.security_compliance)
     */
    List findNodesByPrefix(String prefix);

    /**
     * Compress and serialize the tree into a byte array.
     */
    byte[] compress();
}
