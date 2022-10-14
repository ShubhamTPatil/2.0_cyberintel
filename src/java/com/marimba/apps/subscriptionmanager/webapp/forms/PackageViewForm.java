// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.forms;

import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * This form is merely used for storing the state of the page since there are not form elements on the package navigation page. 1.4, 03/08/2002
 */

public class PackageViewForm extends AbstractForm {

    public void initialize() {
        props.put("sortorder", "true");
        props.put("sorttype", "title");
        props.put("show_url", "false");
        props.put("lastsort", "title");
        props.put("searchType", "basic");
        props.put("search", "*");

        if (DEBUG) {
            System.out.println("PackageViewForm: initialize called");
        }
    }

    public void setSearchType(String type) {
        props.put("searchType", type);
    }

    public String getSearchType() {
        return (String) props.get("searchType");
    }

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        // have to restore the properties used for last sorting
        String sortorder = (String) props.get("sortorder");
        String sorttype = (String) props.get("sorttype");
        String lastsort = (String) props.get("lastsort");
        String show_url = (String) props.get("show_url");
        String search = (String) props.get("search");
        
        props.clear();
        props.put("sortorder", sortorder);
        props.put("sorttype", sorttype);
        props.put("lastsort", lastsort);
        props.put("show_url", show_url);
        props.put("searchType", "basic");
        props.put("search", search);
    }
}
