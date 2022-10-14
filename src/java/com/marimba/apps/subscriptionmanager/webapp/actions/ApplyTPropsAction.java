// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;

import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.intf.objects.ISubscription;

import com.marimba.apps.subscriptionmanager.webapp.forms.AbstractForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.InternalException;
import com.marimba.webapps.intf.SystemException;

/**
 * This action allows you to set the tuner properties value for the form
 */
public final class ApplyTPropsAction
    extends SaveTempPropsAction {
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
     * @throws IOException REMIND
     * @throws ServletException REMIND
     * @throws GUIException REMIND
     */
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
        throws IOException,
                    ServletException {
        if (DEBUG) {
            System.out.println("ApplyTPropsAction called");
        }

        // call the temp props method in the super class that will
        // clear out the previous properties and apply the properties
        // in the text area currently.
        processTempProps(form, request);

        AbstractForm aform = null;

        if (!(form instanceof AbstractForm)) {
            throw new GUIException(new InternalException(GUIUTILS_INTERNAL_WRONGARG, APPLY_TPROPS_ACTION , (String) form.toString()));
        }

        aform = (AbstractForm) form;

        //Set the tuner property into the subscription
        String tunerprop = (String) aform.getValue("tunerprop");
        String svalue = null;
        Object value = aform.getValue("value(tunerpropvalue)");

        if (value instanceof String[]) {
            if (value != null) {
                String[] arrvalue = (String[]) value;
                svalue = arrvalue [0];
            } else {
                svalue = "null";
            }
        } else {
            svalue = (String) value;
        }

        //Get the subscription out of the session to set the properties
        try {
            ISubscription sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);
            sub.setProperty(PROP_TUNER_KEYWORD, tunerprop, svalue);
        } catch (SystemException se) {
            throw new GUIException(se);
        }

        return (mapping.findForward("success"));
    }
}
