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

import com.marimba.apps.subscriptionmanager.intf.*;
import com.marimba.apps.subscriptionmanager.webapp.forms.*;

import com.marimba.webapps.intf.IMapProperty;

/**
 * This form is merely used for storing the state of the page since there are not form elements on the package details page. 1.4, 05/04/2002
 */
public class PackageDetailsForm
    extends AbstractForm
    implements ISubscriptionConstants,
                   IMapProperty {
    boolean initialized = false;
    HashMap checkedItems = new HashMap(DEF_COLL_SIZE);

    public void initialize() {
        // We only want to initialize if this is the first time
        // the form is created.  Otherwise the sortorder will be
        // changed everytime TargetDetailsAction/TargetAddAction is called.	
        if (!initialized) {
            props.put("sortorder", "true");
            props.put("sorttype", "name");
            props.put("lastsort", "name");
            props.put(SESSION_PERSIST_SELECTED, SESSION_TGS_FROMPKGS_SELECTED);
            props.put(SESSION_PERSIST_PREFIX, TGRESULT_PREFIX);
            props.put(SESSION_PERSIST_BEANNAME, TGS_FROMPKGSLIST_BEAN);
        }

        initialized = true;

        if (DEBUG) {
            System.out.println("PackageDetailsForm: initialize called");
        }
    }

    public void setValue(String property,
                    Object value) {
        if (property.startsWith(TGRESULT_PREFIX)) {
            checkedItems.put(property, value);
        } else {
            props.put(property, value);
        }
    }

    public Object getValue(String property) {
        if (property.startsWith(TGRESULT_PREFIX)) {
            return checkedItems.get(property);
        } else {
            return props.get(property);
        }
    }

    public void clearCheckedItems() {
        checkedItems.clear();
    }

    public void clearPaginVars(HttpServletRequest request) {
        request.getSession().removeAttribute((String) getValue(SESSION_PERSIST_SELECTED));
        request.getSession().removeAttribute((String) getValue(SESSION_PERSIST_BEANNAME));
        clearCheckedItems();
    }

    public void reset(ActionMapping actionMapping, HttpServletRequest request) {
        super.reset(actionMapping, request);
        checkedItems.clear();
    }
}
