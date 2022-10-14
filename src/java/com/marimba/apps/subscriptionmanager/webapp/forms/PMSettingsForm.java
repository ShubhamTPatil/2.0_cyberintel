// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.forms;

import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * Form for the performance page.
 *
 * @author Theen-Theen Tan
 * @version 1.3, 02/27/2003
 */

public class PMSettingsForm extends AbstractForm {

    private String masterTxURL;
    private String startLocationPath;

    public String getMasterTxURL() {
        return masterTxURL;
    }

    public void setMasterTxURL(String masterTxURL) {
        this.masterTxURL = (null == masterTxURL) ? masterTxURL : masterTxURL.trim();
    }

    public String getStartLocationPath() {
		return startLocationPath;
	}

	public void setStartLocationPath(String startLocationPath) {
		this.startLocationPath = (null == startLocationPath) ? startLocationPath : startLocationPath.trim();
	}

	/**
     * We have to override the AbstractForm's set method, since it distinguishes between
     * String arrays and other values.  We need to store the values in their native format
     * for the checkbox to work.
     * @param property
     * @param value
     */

    public void setValue(String property, Object value) {
        props.put(property, value);
    }

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        Set set = props.keySet();
        for (Object aSet : set) {
            props.put(aSet, "false");
        }
    }
}

