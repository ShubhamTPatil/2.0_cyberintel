// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscriptionmanager.webapp.util.IndexItem;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.tools.util.ProtectedBeanList;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

/**
 * Base-class for Index-page actions
 *
 * @author	Johan Eriksson
 * @version 	%I%, %G%
 */

public class IndexAction extends AbstractAction {

    protected ProtectedBeanList list;
    private Locale locale;

    protected void addLink(String role, String name, String descr, String href) {
        list.addBean(new IndexItem(role, getString(locale,name), href, getString(locale,descr)));
    }

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        init(request);
        locale = request.getLocale();
        list = new ProtectedBeanList();
        ActionForward forward = mapping.findForward("view");
        addConfigLink();
        if (list != null) {
            request.getSession().setAttribute("links", list.elements(request));
        }
        Object bean = request.getSession().getAttribute(IWebAppsConstants.INITERROR_KEY);

        if ((bean != null) && bean instanceof Exception) {
            //remove initerror from the session because it has served its purpose
            if (DEBUG) {
                System.out.println("vDesk Configuration Action: critical exception found");
            }
            request.getSession().removeAttribute(IWebAppsConstants.INITERROR_KEY);
            forward = mapping.findForward("failure");
        }
        return forward;
    }

    private void addConfigLink() {
        addLink("managePlugin", "page.config.PlugIn", "page.config.PlugInText", "pluginEdit.do");
        addLink("manageNamespace", "page.config.Namespace", "page.config.NamespaceText", "namespaceLoad.do");
        addLink("manageEmailConfig", "page.email.link", "page.email.link.help", "mailconfig.do");
        addLink("manageSCAP", "page.config.SCAPSecurityProfiles", "page.config.SCAPSecurityProfilesStatusText", "scapSecurityTemplateListing.do?action=load");
        addLink("manageUSGCB", "page.config.USGCBSecurityProfiles", "page.config.USGCBSecurityProfilesStatusText", "usgcbSecurityTemplateListing.do?action=load");
        addLink("manageCustom", "page.config.CustomSecurityProfiles", "page.config.CustomSecurityProfilesStatusText", "customSecurityTemplateListing.do?action=load");
        addLink("manageProfile", "page.config.CveIdsOsMappingsProfiles", "page.config.CveIdsOsMappingsProfilesStatusText", "scapSecurityOsMappingCveIdsListing.do?action=load");
    }
}
