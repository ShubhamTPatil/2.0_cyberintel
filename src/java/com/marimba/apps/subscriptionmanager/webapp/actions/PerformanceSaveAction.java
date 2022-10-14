// Copyright 2004, Marimba, Inc. All Rights Reserved.

// Confidential and Proprietary Information of Marimba, Inc.

// Protected by or for use under one or more of the following patents:

// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,

// and 6,430,608. Other Patents Pending.



package com.marimba.apps.subscriptionmanager.webapp.actions;



import org.apache.struts.action.ActionForm;

import org.apache.struts.action.ActionForward;

import org.apache.struts.action.ActionMapping;



import java.io.IOException;



import javax.servlet.*;

import javax.servlet.http.*;



import com.marimba.apps.subscription.common.intf.IUser;



import com.marimba.apps.subscriptionmanager.webapp.forms.SetPluginForm;

import com.marimba.apps.subscriptionmanager.webapp.forms.AbstractForm;

import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;

import com.marimba.apps.subscriptionmanager.compliance.ComplianceConstants;



import com.marimba.webapps.intf.GUIException;

import com.marimba.webapps.intf.IWebAppsConstants;

import com.marimba.tools.config.ConfigProps;



public class PerformanceSaveAction

    extends AbstractAction {

    /**

     * REMIND

     *

     * @param mapping REMIND

     * @param form REMIND

     * @param request REMIND

     * @param response REMIND

     *

     * @return REMIND

     *

     * @throws java.io.IOException REMIND

     * @throws javax.servlet.ServletException REMIND

     * @throws com.marimba.webapps.intf.GUIException REMIND

     */

    public ActionForward execute(ActionMapping       mapping,

                                 ActionForm          form,

                                 HttpServletRequest  request,

                                 HttpServletResponse response)

        throws IOException,

                   ServletException {

        AbstractForm myForm = (AbstractForm) form;

        try {
        	ServletContext   context = servlet.getServletConfig().getServletContext(); 
            SubscriptionMain smmain = TenantHelper.getTenantSubMain(context, request) ;

            ConfigProps config = smmain.getConfig();

            boolean scrubberOn = "true".equals(myForm.getValue(IWebAppConstants.PERFORMANCE_SCRUBBERON).toString());

            config.setProperty(ComplianceConstants.sScrubberFlagString, scrubberOn == true ? Integer.toString(ComplianceConstants.sComplianceScrubberDoEverything) : Integer.toString(ComplianceConstants.sComplianceScrubberDoNothing));

            config.save();

        } catch (Exception e) {

            throw new GUIException(e);

        }

        return (mapping.findForward("success"));

    }

}





