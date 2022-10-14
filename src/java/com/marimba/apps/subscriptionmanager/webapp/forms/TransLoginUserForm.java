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
 * This form is used for initializing the tuner and channel properties for a particular target. It is used by the Tuner/Channel Properties page.
 */
public class TransLoginUserForm
    extends AbstractForm
    implements ISubscriptionConstants,
                   IMapProperty {
    /**
     * REMIND
     */
    public void initialize() {
        if (DEBUG) {
            System.out.println("TransLoginUserForm: initialize called");
        }
    }

    /**
     * REMIND
     */
    public void clear() {
        props.clear();
    }
}
