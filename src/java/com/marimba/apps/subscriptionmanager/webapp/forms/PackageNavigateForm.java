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
 * This form is merely used for storing the state of the page since there are not form elements on the package navigation page. 1.4, 03/08/2002
 */
public class PackageNavigateForm
    extends AbstractForm
    implements ISubscriptionConstants,
                   IMapProperty {
    /**
     * REMIND
     */
    public void initialize() {
        props.put("sortorder", "true");
        props.put("sorttype", "title");
        props.put("show_url", "false");
        props.put("lastsort", "title");

        if (DEBUG) {
            System.out.println("PackageNavigateForm: initialize called");
        }
    }

    //documented in the interface for IMapProperty
    public void set(String property,
                    Object value) {
        if (DEBUG) {
            System.out.println("PackageNavigateForm: set called for " + property);
        }

        props.put(property, value);
    }
}
