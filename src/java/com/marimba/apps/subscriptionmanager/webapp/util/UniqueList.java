// Copyright 2004-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.util;

import java.util.ArrayList;
import java.util.Collection;

/*
 * This class is used to avoid duplicate String type objects alone and maintains insertion order.
 */

public class UniqueList<String> extends ArrayList<String> {

    public boolean add(String element) {
        return (!exists(element)) && super.add(element);
    }

    public void add(int index, String element) {
        add(element);
    }

    public boolean addAll(Collection c) {
        for (Object element : c) {
            if (!exists(element)) {
                add((String) element);
            }
        }

        return true;
    }

    public boolean addAll(int index, Collection c) {
        return addAll(c);
    }

    private boolean exists(Object str) {
        boolean exists = false;

        for (Object element : this) {
            if (str == element || str.equals(element)) {
                exists = true;
                break;
            }
        }

        return exists;
    }
}