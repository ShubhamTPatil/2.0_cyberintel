// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions.common;

import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import org.apache.struts.action.ActionForm;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Description about the class
 *
 * @author   dvamathevan
 * @version  $Revision$,  $Date$
 *
 */

public class GenericDistributionAction extends AbstractAction {

    // Add in the targets from the session variable used from the target schedule details.
    // We must first determine which set of targets we are dealing with. This is done\
    // by looking at the boolean which is set when the user is in multiple select mode

    protected HttpSession session;
    protected boolean singleMode;


    public void init(ActionForm form, HttpServletRequest request) throws IOException, ServletException {

        session = request.getSession();
        // get the list of targets and set it in the DistributionBean
        singleMode = session.getAttribute(SESSION_MULTITGBOOL) == null;
    }
}