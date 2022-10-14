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

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.*;

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.*;
import com.marimba.apps.subscriptionmanager.webapp.system.operation.SelectedPkgsBean;
import com.marimba.apps.subscription.common.intf.SubInternalException;

import com.marimba.webapps.intf.IMapProperty;

/**
 * REMIND
 *
 * @author $Author$
 * @version $File$
 */
public class PackageEditForm
    extends AbstractForm
    implements IWebAppConstants {
    static final String HTTP_PFX = "http://";
    static final String HTTPS_PFX = "https://";
    protected String[] packages = new String[0];
    protected String search;
    protected String searchQuery;
    protected String url;
    protected String user;
    protected String pwd;
    protected String selection;
    protected String addUrl;

    static protected String selectedBeanName = IWebAppConstants.PAGE_PKGS;

    /**
     * REMIND
     *
     * @param packages REMIND
     */
    public void setPackages(String[] packages) {
        this.packages = packages;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String[] getPackages() {
        return this.packages;
    }

    /**
     * REMIND
     *
     * @param search REMIND
     */
    public void setSearch(String search) {
        this.search = search;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getSearch() {
        return this.search;
    }

    /**
     * REMIND
     *
     * @param searchQuery REMIND
     */
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getSearchQuery() {
        return this.searchQuery;
    }

    /**
     * REMIND
     *
     * @param url REMIND
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getUrl() {
        if ((this.url == null) || (this.url.length() == 0)) {
            return HTTP_PFX;
        }

        return this.url;
    }

    public void setAddUrl(String addUrl) {
        this.addUrl = addUrl;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getAddUrl() {
        if ((this.addUrl == null) || (this.addUrl.length() == 0)) {
            return HTTP_PFX;
        }

        return this.addUrl;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getPwd() {
        return pwd;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public String getSelection() {
        return selection;
    }

    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
//    public void reset(ActionMapping      mapping,
//                      HttpServletRequest request) {
//        setPackages(null);
//        this.url = null;
//    }

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

        if (request.getMethod()
                       .equalsIgnoreCase("POST")) {
            if (mapping.getPath()
                           .indexOf("packageBrowse") != -1) {
                // If action is user input
                if (!url.startsWith("http")) {
                    url = HTTP_PFX + url;
                }

                if ((url == null) || (url.length() < 1)) {
                    errors.add("url-req", new ActionError("error.url.required"));
                }

                if (HTTP_PFX.equals(url) || HTTPS_PFX.equals(url) || !verifyUrl(url)) {
                    errors.add("url-invalid", new ActionError("error.url.invalid", url));
                }
            }
        }

        return errors;
    }

    private boolean verifyUrl(String appurl) {
        try {
            new URL(appurl);
        } catch (Exception ex) {
            return false;
        }

        return true;
    }

    /**
     * Obtain the SelectedPkgsBean stored in the session specified by pagebean_bean
     *
     * @param req request object
     * @return a SelectedPkgsBean stored in the session specified by pagebean_bean
     *
     * REMIND tcube, ideally, we want to move the pagebean_name as a member of this form, however, to restrain
     * changes on the JSP, temporarily putting this into the session.
     */
    public SelectedPkgsBean getSelectedPkgs(HttpServletRequest req) {
        HttpSession session = req.getSession();
        SelectedPkgsBean pkgsbean = (SelectedPkgsBean) session.getAttribute(selectedBeanName);

        if (null == pkgsbean) {
            pkgsbean = new SelectedPkgsBean();
        }

        return pkgsbean;
    }

    /**
     * Stores the SelectedPkgsBean object into the session variable specified by pagebean_name
     *
     * @param bean SelectedPkgsBean object that contains the content that the users selected
     * @param req request object
     *
     * @throws com.marimba.apps.subscription.common.intf.SubInternalException if SelectedPkgsBean is null
     */
    public void setSelectedPkgs(SelectedPkgsBean bean,
                                       HttpServletRequest req)
            throws SubInternalException {
        if (null == bean) {
            throw new SubInternalException(PKG_INTERNAL_PKGSNOTFOUND);
        }

        HttpSession session = req.getSession();
        session.setAttribute(selectedBeanName, bean);
    }

    /**
     * Removes the SelectedPkgsBean from the session
     *
     * @param req REMIND
     */
    public void removeSelectedPkgs(HttpServletRequest req) {
        HttpSession session = req.getSession();
        session.removeAttribute(selectedBeanName);
    }

    public String getPreviousTransmitterUrl() {
       // return (getProperty(FORM_TXURL) == null) ? "" : getProperty(FORM_TXURL);
         return getProperty(FORM_TXURL);
    }

    public void setPreviousTransmitterUrl(String txurl) {
        setValue(FORM_TXURL, txurl);
    }
}
