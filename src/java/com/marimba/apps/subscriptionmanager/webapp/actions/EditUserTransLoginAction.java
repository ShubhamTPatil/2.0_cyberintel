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

import java.util.Enumeration;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscriptionmanager.webapp.system.TLoginBean;
import com.marimba.apps.subscriptionmanager.webapp.forms.TransLoginForm;

import com.marimba.webapps.intf.IMapProperty;

/**
 * This action allows to create a new user name and password setting for transmitter login
 */
public final class EditUserTransLoginAction
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
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException,
            ServletException {
        //Go through through the list and see which transmitters have been
        //selected
        TLoginBean tbean = (TLoginBean) request.getSession()
                .getAttribute(SESSION_TLOGINBEAN);

        if (tbean == null) {
            if (DEBUG) {
                System.out.println("EditUserTransLoginAction: tbean is null");
            }
        }

        IMapProperty bform = (IMapProperty) form;
        Enumeration transmitters = tbean.getTransmitters();
        String curtrans;

        if (DEBUG) {
            TransLoginForm tform = (TransLoginForm) form;
            tform.dump();
            System.out.println("EditUserTransLoginAction: value of transmitter list = " + bform.getValue("transmitterlist"));
        }

        curtrans = (String) bform.getValue("transmitterlist");
        bform.setValue("transmitterlist", null);
        if (curtrans != null && curtrans.startsWith("trans_")) {
            int curtrans_idx = 0;
            curtrans = curtrans.substring("trans_".length());
            curtrans_idx = Integer.parseInt(curtrans);
            for (int i = 0; transmitters.hasMoreElements(); i++) {
                curtrans = (String) transmitters.nextElement();
                if (i == curtrans_idx) {
                    /* found the selected item.
                     *     set it to the session
                     */
                    request.getSession()
                            .setAttribute(SESSION_EDITTRANSLOGIN, curtrans);
                    if (DEBUG) {
                        System.out.println("EditUserTransLoginAction: cur trans = " + curtrans);
                    }
                    break;
                }
            }
        }


        return (mapping.findForward("success"));
    }
}
