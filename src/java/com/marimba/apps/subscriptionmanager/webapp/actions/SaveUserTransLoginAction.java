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

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.AbstractForm;
import com.marimba.apps.subscriptionmanager.webapp.system.TLoginBean;
import com.marimba.apps.subscription.common.StringResourcesHelper;

import com.marimba.tools.util.Password;

import com.marimba.webapps.intf.*;
import com.marimba.webapps.tools.util.KnownActionError;

/**
 * This action allows to create a new user name and password setting for transmitter login
 */
public final class SaveUserTransLoginAction
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
        //Obtain the transmitter, username, and password.
        AbstractForm aform = null;

        if (!(form instanceof AbstractForm)) {
            throw new GUIException(new InternalException(GUIUTILS_INTERNAL_WRONGARG, "SaveUserTransLoginAction", (String) form.toString()));
        }
        aform = (AbstractForm) form;
         if (DEBUG5){

            System.out.println("ClassName: SaveUserTransLoginAction Method: perform");
            aform.dump();
        }
        String action = request.getParameter("action");
//        if ("save".equals(action)) {


            String hostname = (String) aform.getValue("hostname");
            if ( hostname == null || hostname.length() == 0){
               KnownException ke = new KnownException(VALIDATION_FIELD_REQUIRED,
                                                StringResourcesHelper.getMessage(HOST_NAME_AND_PORT));
               throw new GUIException(ke);
            }
            String username = (String) aform.getValue("username");
            String password = (String) aform.getValue("password");
            if (password != null && !password.equals(aform.getValue("passwordConfirm"))) {

                KnownException ke = new KnownException(TRANSLOGIN_PWDCONFIRM_FAILED);
                throw new GUIException(ke);

            }

            password = Password.encode(password);


            //Construct the properties that are to be stored in
            String userprop = IWebAppConstants.MARIMBA_KEYCHAIN + "." + hostname + ".user";
            String passwdprop = IWebAppConstants.MARIMBA_KEYCHAIN + "." + hostname + ".password";

            try {
                //Set the values to the tlogin
                TLoginBean tbean = (TLoginBean) request.getSession()
                        .getAttribute(SESSION_TLOGINBEAN);

                /* Check to see if they have changed the host name.  This is done by checking
                 * what was saved in the session for editing
                 */
                String edithost = (String) request.getSession()
                        .getAttribute(SESSION_EDITTRANSLOGIN);

                if (edithost != null) {
                    if (!edithost.equalsIgnoreCase(hostname)) {
                        /* They have changed the host name, so remove the previous entry for
                         * the transmitter
                         */
                        tbean.delTUserAndPwd(edithost);
                    }
                }

                tbean.setTUserAndPwd(hostname, username, password);


            } catch (SystemException se) {
                throw new GUIException(se);
            }
        //}        //clear out the current transmitter if one was set
        request.getSession()
                .removeAttribute(SESSION_EDITTRANSLOGIN);
        return (mapping.findForward("success"));
    }
}
