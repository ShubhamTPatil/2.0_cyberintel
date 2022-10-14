package com.marimba.apps.subscriptionmanager.webapp.forms;

import org.apache.struts.action.ActionForm;

// Copyright 1997-2004, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

/**
 * Created by IntelliJ IDEA.
 * @author  Anantha Kasetty
 * @version $Revision$, $Date$
 * $File$
 * 
 */
public class PushUrlForm extends AbstractForm {
    private String dn;
    private String url;
    private String targetUrl;

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }



    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }



    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }



}
