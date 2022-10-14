// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.forms;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import java.io.*;

import java.net.*;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.marimba.apps.subscription.common.*;

import com.marimba.apps.subscriptionmanager.webapp.forms.*;

import com.marimba.webapps.intf.IMapProperty;

/**
 * This form is merely used for storing the state of the page since there are not form elements on the target details page.
 */
public class TargetDetailsMultiForm
    extends AbstractForm
    implements ISubscriptionConstants,
                   IMapProperty {
    boolean initialized = false;
    HashMap checkedItems = new HashMap(DEF_COLL_SIZE);

    /**
     * REMIND
     */
    public void initialize() {
        // We only want to initialize if this is the first time
        // the form is created. Otherwise the sortorder will be
        // changed everytime TargetDetailsAction/TargetAddAction is called.
        if (!initialized) {
            props.put((Object) "sortorder", (Object) "true");
            props.put((Object) "sorttype", (Object) "title");
            props.put((Object) "showurl", (Object) "hide");
            props.put((Object) "lastsort", (Object) "title");
            props.put(SESSION_PERSIST_SELECTED, SESSION_PKGS_FROMTGS_SELECTED);
            props.put(SESSION_PERSIST_PREFIX, SESSION_PKGS_FROMTGS_PREFIX);
            props.put(SESSION_PERSIST_BEANNAME, TARGET_PKGS_BEAN);
            checkedItems.clear();
        }

        initialized = true;

        if (DEBUG) {
            System.out.println("TargetDetailsMultiForm: initialize called");
        }
    }

    //documented in the interface for IMapProperty
    public void setValue(String property,
                    Object value) {
        if (property.startsWith(SESSION_PKGS_FROMTGS_PREFIX)) {
            checkedItems.put(property, value);
        } else {
            props.put(property, value);
        }
    }

    // documented in the interface for IMapProperty
    public Object getValue(String property) {
        if (property.startsWith(SESSION_PKGS_FROMTGS_PREFIX)) {
            return checkedItems.get(property);
        } else {
            return props.get(property);
        }
    }

    /**
     * REMIND
     */
    public void clearCheckedItems() {
        checkedItems.clear();
    }
}
