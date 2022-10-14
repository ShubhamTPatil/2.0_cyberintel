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

import com.marimba.webapps.intf.*;

/**
 * REMIND
 *
 * @author $Author$
 * @version $File$
 */
public class TargetEditForm
        extends ActionForm
        implements IWebAppConstants,
        IMapProperty {
    Map props = new HashMap();
    private String selectedIncludeItems;
    private String submittedTargetsStr;
    private String action;
    private String forwardURL;

    /**
     * This set method is used to set the values for input fields in the page
     *
     * @param property REMIND
     * @param value REMIND
     */
    public void setValue(String property,
                    Object value) {
        props.put(property, value);
    }

    /**
     * This set method is used to set the values for input fields in the page
     *
     * @param property REMIND
     *
     * @return REMIND
     */
    public Object getValue(String property) {
        return props.get(property);
    }

    /**
     * REMIND
     *
     * @param selectedIncludeItems REMIND
     */
    public void setSelectedIncludeItems(String selectedIncludeItems) {
        this.selectedIncludeItems = selectedIncludeItems;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getSelectedIncludeItems() {
        return this.selectedIncludeItems;
    }

    /**
     * REMIND
     *
     * @param submittedTargetsStr REMIND
     */
    public void setSubmittedTargetsStr(String submittedTargetsStr) {
        this.submittedTargetsStr = submittedTargetsStr;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getSubmittedTargetsStr() {
        return this.submittedTargetsStr;
    }

    /**
     * REMIND
     *
     * @param action REMIND
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getAction() {
        return this.action;
    }

    /**
     * REMIND
     *
     * @param forwardURL REMIND
     */
    public void setForwardURL(String forwardURL) {
        this.forwardURL = forwardURL;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getForwardURL() {
        return this.forwardURL;
    }

    /*
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping,
                      HttpServletRequest request) {
        selectedIncludeItems = null;
        submittedTargetsStr = null;
        action = null;
        forwardURL = null;
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
    public ActionErrors validate(ActionMapping mapping,
                                 HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        return errors;
    }

    public void dump() {
        Set set = props.keySet();
        Iterator it = set.iterator();
        System.out.println("props size " + props.size());

        String s;

        while (it.hasNext()) {
            s = (String) it.next();
            System.out.print(s);
            System.out.print("=");
            String[] o;
            try {
                o = (String[]) props.get(s);
                for (int i = 0; i < o.length; i++) {
                    System.out.println(o[i]);
                }
            } catch (Exception e) {
                // e.printStackTrace();
                System.out.println(props.get(s));
            }

        }

        System.out.println("action= " + action);
        System.out.println("forwardURL= " + forwardURL);
        System.out.println("submittedTargetsStr= " + submittedTargetsStr);


    }
}
