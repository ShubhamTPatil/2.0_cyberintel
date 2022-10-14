// Copyright 2018, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.securitymgr.query;

import java.util.*;

public class DirNode {
    private String name;
    private String type;
    private String className;
    private String displayName;

    private Map<String, String> nodeProperties = new HashMap<String, String>();
    private Map<String, DirNode> childNodes = new LinkedHashMap<String, DirNode>();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }


    public void setProps(String key, String value) {
        nodeProperties.put(key, value);
    }

    public String getValue(String key) {
        return nodeProperties.get(key);
    }

    public void addNode(DirNode dirNode) {
        childNodes.put(dirNode.getName(), dirNode);
    }

    public Map<String, DirNode> getChildNode() {
        return childNodes;
    }
}
