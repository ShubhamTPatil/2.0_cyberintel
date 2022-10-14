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

import javax.servlet.http.HttpServletRequest;

import com.marimba.apps.subscription.common.*;

import com.marimba.apps.subscriptionmanager.webapp.forms.*;

/**
 * Used to handle command line form elements
 *
 * @author Damodar Hegde
 * @version 1.1, 09/25/2001
 */
public class CommandlineForm
    extends ActionForm
    implements ISubscriptionConstants {
    private String commandargs;
    private String output;

    /**
     * REMIND
     *
     * @param commandargs REMIND
     */
    public void setCommandargs(String commandargs) {
        this.commandargs = commandargs;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getCommandargs() {
        return this.commandargs;
    }

    /**
     * REMIND
     *
     * @param output REMIND
     */
    public void setOutput(String output) {
        this.output = output;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getOutput() {
        return this.output;
    }

    /**
     * Validate the properties that have been set from this HTTP request, and return an <code>ActionErrors</code> object that encapsulates any validation
     * errors that have been found.  If no errors are found, return <code>null</code> or an <code>ActionErrors</code> object with no recorded error messages.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     *
     * @return REMIND
     */
    public ActionErrors validate(ActionMapping      mapping,
                                 HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if ((commandargs == null) || (commandargs.length() < 1)) {
            errors.add("target", new ActionError("error.commandargs.required"));
        }

        return errors;
    }
}
