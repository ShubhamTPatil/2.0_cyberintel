// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscriptionmanager.webapp.forms.BlackoutForm;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.PatchUtils;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * This action is called when visiting the blackout page. It reads the blackout string from
 * the DistributionBean and populates the form for display.
 *
 * @author Michele Lin
 * @author Sunil Ramakrishnan
 * @author Devendra Vamathevan
 */
public final class BlackoutLoadAction extends AbstractAction {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        ISubscription sub;
        try {
        	init(request);
            sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);
            String updateBlackoutExempt = getTChPropValue(sub.getProperty(PROP_SERVICE_KEYWORD, "update.schedule.blackoutexempt"));
            String startBlackoutExempt = getTChPropValue(sub.getProperty(PROP_SERVICE_KEYWORD, "update.schedule.blackoutexempt"));
            boolean blackoutExempt = ("true".equalsIgnoreCase(updateBlackoutExempt) ||
                    "true".equalsIgnoreCase(startBlackoutExempt));
            String blackout = sub.getBlackOut();
            boolean patchBlackoutExempt = false;
            String patchServiceUrl = null;
            String status = null;

            //To check for patch configuration
            try {
                patchServiceUrl = PatchUtils.getPatchServiceUrl(request, tenant);
            } catch(SystemException e) {
                if(DEBUG) {
                    e.printStackTrace();
                }
            }
            if(patchServiceUrl == null) {
                GUIUtils.setToSession(request, "pm_configured", "false");
                status = "false";
            }else{
                GUIUtils.setToSession(request, "pm_configured", "true");
                status = "true";
            }
            // To check for whether the Patch is excempted from blackout
            if("true".equals(status)) {
                patchBlackoutExempt = isPatchBlackoutExempt(request, sub);
            }
            // initialize form elements on jsp page with existing blackout string
            ((BlackoutForm) form).initialize(blackout, blackoutExempt, patchBlackoutExempt, request.getLocale());
        } catch (SystemException e) {
            throw new GUIException(e);
        }

        // check if user clicked preview
        String action = request.getParameter("action");
        if ("preview".equals(action)) {
            return mapping.findForward("preview");
        }
        return (mapping.findForward("success"));
    }

    private boolean isPatchBlackoutExempt(HttpServletRequest request, ISubscription sub) throws SystemException {
        //if (GUIUtils.getMain(servlet).hasCapability(IAppConstants.CAPABILITIES_PATCH)) {
        String patchServiceUrl=PatchUtils.getPatchServiceUrl(request, tenant);

        if (patchServiceUrl == null) {
            return false;
        }
        String updatePatchBlackoutExempt =
                getTChPropValue(PatchUtils.getPatchServiceProperty(sub, "update.schedule.blackoutexempt"));

        String startPatchBlackoutExempt =
                getTChPropValue(PatchUtils.getPatchServiceProperty(sub, "start.schedule.blackoutexempt"));

        return "true".equalsIgnoreCase(updatePatchBlackoutExempt) || "true".equalsIgnoreCase(startPatchBlackoutExempt);        
    }
}
