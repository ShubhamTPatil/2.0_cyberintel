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

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;

/**
 * This action allows you to cancel out of the tuner properties
 */
public final class CancelTunerChPropsAction
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
     * @throws IOException REMIND
     * @throws ServletException REMIND
     */
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
        throws IOException, 
                   ServletException {
        try {
            GUIUtils.removeFromSession(request, PAGE_TCHPROPS_SUB);
            GUIUtils.removeFromSession(request, PAGE_TCHPROPS_TPROPSKEYS);
            GUIUtils.removeFromSession(request, PAGE_TCHPROPS_SPROPSKEYS);
            GUIUtils.removeFromSession(request, PAGE_TCHPROPS_CHPROPSKEYS);
            GUIUtils.removeFromSession(request, PAGE_TCHPROPS_ALLCHSPROPSKEYS);
            GUIUtils.removeFromSession(request, PAGE_TCHPROPS_CHANNELS);
        } catch (SystemException se) {
            GUIException guie = new GUIException(TUNERCHPROPS_CANCEL_ERROR, se);
            throw guie;
        }

        return (mapping.findForward("success"));
    }
}
