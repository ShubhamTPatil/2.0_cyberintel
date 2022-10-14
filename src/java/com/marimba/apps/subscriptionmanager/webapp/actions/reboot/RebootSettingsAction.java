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
import com.marimba.apps.subscriptionmanager.webapp.forms.CommonRebootSettingsForm;

import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.intf.GUIException;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.*;

/**
 * This action is called when visiting the reboote settings page.  It gets the target from the session variable,
 * queries LDAP to obtain the subscription for the target, and loads the reboot schedule information to be displayed
 *
 * @author Marie Antoine (Tony)
 */
public final class RebootSettingsAction
        extends AbstractAction {

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest req,
                                 HttpServletResponse response) throws Exception {

        String action = req.getParameter("action");
         
        if (DEBUG5) {
            System.out.println("CommonRebootSettingsAction called");
        }

        // read the service schedule string from SESSION_DIST Distribution bean.
        try {
            ISubscription sub = (ISubscription) GUIUtils.getFromSession(req, PAGE_TCHPROPS_SUB);
            String rebootSchedule = getTChPropValue(sub.getProperty(PROP_TUNER_KEYWORD, GUIConstants.REBOOT_SCHEDULE_AT));

            // Create Form Objects
            CommonRebootSettingsForm crsForm = (CommonRebootSettingsForm) form;
            crsForm.init(req.getLocale());
            crsForm .setType("reboot");

            // set the schedule value
            crsForm.setScheduleString(rebootSchedule);

        } catch (SystemException e) {
        	if(DEBUG5) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        	}
            throw new GUIException(e);
        }

        // check if user clicked preview
       
        if ("preview".equals(action)) {
            return mapping.findForward("preview");
        }
        return (mapping.findForward("success"));
    }
}
