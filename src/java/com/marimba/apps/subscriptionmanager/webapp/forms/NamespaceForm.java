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

/**
 * Form for the namespace selection page.
 *
 * @author Theen-Theen Tan
 * @version 1.3, 02/27/2003
 */
public class NamespaceForm
    extends AbstractForm {
    private String    namespace;
    private String    rootContainer;
    private ArrayList nsList = new ArrayList(3);

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getRootContainer() {
        return rootContainer;
    }

    /**
     * REMIND
     *
     * @param subContainer REMIND
     */
    public void setRootContainer(String subContainer) {
        rootContainer = subContainer;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public ArrayList getNamespaceList() {
        return nsList;
    }

    /**
     * REMIND
     *
     * @param list REMIND
     */
    public void setNamespaceList(ArrayList list) {
        nsList = list;
    }

    /**
     * REMIND
     *
     * @param namespace REMIND
     */
    public void setNamespace(String namespace) {
        if ((namespace != null) && (namespace.length() == 0)) {
            namespace = null;
        }

        this.namespace = namespace;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getNamespace() {
        return this.namespace;
    }

    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping      mapping,
                      HttpServletRequest request) {
        Set set=props.keySet();
        Iterator iter=set.iterator();
        while(iter.hasNext()){
            Object obj=iter.next();
            props.put(obj,"false");
        }
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
        return null;
    }

    /**
     * We have to override the AbstractForm's set method, since it distinguishes between
     * String arrays and other values.  We need to store the values in their native format
     * for the checkbox to work.
     * @param property
     * @param value
     */ 
    public void setValue(String property,
                    Object value) {
        props.put(property, value);
    }
}
