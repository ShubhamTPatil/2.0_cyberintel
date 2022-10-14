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

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IMapProperty;
import com.marimba.webapps.intf.SystemException;

/**
 * This action will remove a transmitter login setting made for a target. Essentially, when an existing target is removed, the properties for transmitter login
 * (marimba.keychain.&lt;transmitter name>.user and marimba.keychain.&lt;transmitter name>.password
 */
public final class RemoveUserTransLoginAction
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
     * @throws GUIException REMIND
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
        IMapProperty bform = (IMapProperty) form;

        Enumeration transmitters = tbean.getTransmitters();
        String curtrans;

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
                    try {
                        tbean.delTUserAndPwd(curtrans);
                    } catch (SystemException se) {
                        throw new GUIException(se);
                    }
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
