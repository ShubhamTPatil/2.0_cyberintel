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
import com.marimba.apps.subscription.common.objects.Channel;

import com.marimba.apps.subscriptionmanager.webapp.forms.AbstractForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.InternalException;
import com.marimba.webapps.intf.SystemException;

/**
 * This action allows you to set the tuner properties value for the form
 */
public final class ApplyChannelPropsAction
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
            System.out.println("ApplyChannelPropsAction called 2");
        }

        //Get the Page state bean which indicates what type have been selected
        AbstractForm aform = null;

        if (!(form instanceof AbstractForm)) {
            throw new GUIException(new InternalException(GUIUTILS_INTERNAL_WRONGARG, "ApplyChannelPropsAction", (String) form.toString()));
        }

        aform = (AbstractForm) form;

        String   chpropstype = (String) aform.getValue("chpropstype");
        String   chpropname = (String) aform.getValue("channel_prop_name");
        String   chpropvalue = (String) aform.getValue("channel_prop_value");

        // call the temp props method in the super class that will
        // clear out the previous properties and apply the properties
        // in the text area currently.
        processTempProps(form, request);

        //Get the subscription out of the session to set the properties
        try {
            ISubscription sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);

            if (PROP_TUNER_KEYWORD.equals(chpropstype) || PROP_SERVICE_KEYWORD.equals(chpropstype) || PROP_CHANNEL_KEYWORD.equals(chpropstype) || PROP_ALL_CHANNELS_KEYWORD
                                                                                                                                                      .equals(chpropstype)) {
                if ((chpropname != null) && (chpropname.length() > 0)) {
                    sub.setProperty(chpropstype, chpropname, chpropvalue);
                }
            } else {
                Channel chn = sub.getChannel(chpropstype);

                if (chn == null) {
                    // The user is attempting to set a property
                    // for a channel that doesn't exist in the policy
                    // We check against dummy channels
                    chn = sub.getDummyChannel(chpropstype);

                    if (chn == null) {
                        chn = sub.createDummyChannel(chpropstype);
                    }
                }

                if ((chpropname != null) && (chpropname.length() > 0)) {
                    chn.setProperty(chpropname, chpropvalue);
                }
            }

        } catch (SystemException se) {
            throw new GUIException(se);
        }

        return (mapping.findForward("success"));
    }
}
