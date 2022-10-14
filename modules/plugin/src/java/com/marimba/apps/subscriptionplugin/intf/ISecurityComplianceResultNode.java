// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
package com.marimba.apps.subscriptionplugin.intf;

import com.marimba.intf.util.INamed;
import com.marimba.intf.util.IProperty;

/**
 * An interface to represent a node in securitycompliance result data. Security Compliance Result data is stored
 * in a tree structure, and each node contains a list of (key, value) pairs.
 */
public interface ISecurityComplianceResultNode
    extends IProperty,
            INamed {
    /**
     * Get the property keys within the node.
     */
    String[] getKeys();

    /**
     * Count the number of child nodes.
     */
    int getChildrenCount();

    /**
     * Get the children of the node.
     */
    ISecurityComplianceResultNode[] getChildren();

    /**
     * Get the offspring under the current node with the given prefix and the
     * instance number.
     */
    ISecurityComplianceResultNode getDescendant(String prefix,
                                 int    instanceNumber);
}
