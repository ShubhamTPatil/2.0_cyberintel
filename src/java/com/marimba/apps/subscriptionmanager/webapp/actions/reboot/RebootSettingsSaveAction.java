// Copyright 2009, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions.reboot;

import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.intf.GUIConstants;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.forms.CommonRebootSettingsForm;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.KnownException;
import com.marimba.webapps.intf.SystemException;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;





/**

 * Extract the values from the CommonRebootSettingsForm

 * and saves them in the DistributionBean object.

 *

 * This Action is called to commit the changes every time we move out

 * of any of the reboot settings jsp pages

 *

 */

public final class RebootSettingsSaveAction extends AbstractAction {


    public ActionForward execute(ActionMapping mapping,

                                 ActionForm form,

                                 HttpServletRequest request,

                                 HttpServletResponse response)

            throws ServletException {


        String pageName = request.getParameter("page");

        if (DEBUG5) {

            System.out.println("Reboot Setting Save Action called for page = " + pageName);

            ((CommonRebootSettingsForm) form).dump();

        }



        saveRebootPage(request, form);

        // and forward to the next action

        String forward = ((CommonRebootSettingsForm) form).getForward();
        if (DEBUG5) {
            System.out.println("After Reboot Setting save forwarding to "+forward);
        }

        return (new ActionForward(forward, true));

    }





    private void saveRebootPage(HttpServletRequest request, ActionForm form) throws GUIException {

        CommonRebootSettingsForm commonrebootForm = (CommonRebootSettingsForm) form;

        try {

            ISubscription sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);

            DistributionBean distributionBean = getDistributionBean(request);

            if("true".equals(commonrebootForm.getValue("SET_REBOOT_SCHEDULE"))) {
                String rebootSchedule = commonrebootForm.getScheduleString();
                if(DEBUG5) {
                    System.out.println("Reboot Setting: rebootSchedule = " + rebootSchedule);
                }

                // If priority for this property is already defined, keep the priority and the value alone
                rebootSchedule = stripOutExistingPriority(sub.getProperty(PROP_TUNER_KEYWORD, GUIConstants.REBOOT_SCHEDULE_AT),
                        rebootSchedule);

                sub.setProperty(PROP_TUNER_KEYWORD, GUIConstants.REBOOT_SCHEDULE_AT, rebootSchedule);
                distributionBean.setRebootSchedule(rebootSchedule);

            } else {
                String prop = sub.getProperty(PROP_TUNER_KEYWORD, GUIConstants.REBOOT_SCHEDULE_AT);
                if (prop == null) {
                    sub.setProperty(PROP_TUNER_KEYWORD, GUIConstants.REBOOT_SCHEDULE_AT, null);
                    distributionBean.setRebootSchedule(null);
                } else {
                    sub.setProperty(PROP_TUNER_KEYWORD, GUIConstants.REBOOT_SCHEDULE_AT, "null");
                    distributionBean.setRebootSchedule(null);
                }
            }
            
        } catch (SystemException e) {
            throw new GUIException(e);
        }
    }

}


