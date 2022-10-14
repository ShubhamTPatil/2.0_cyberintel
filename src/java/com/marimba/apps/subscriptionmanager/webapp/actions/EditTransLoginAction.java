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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Collection;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Target;

import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.webapp.system.TLoginBean;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.forms.TransLoginForm;

import com.marimba.webapps.intf.SystemException;

/**
 * This action corresponds to the editing of transmitter login settings. This will obtain the setting current for the specified target. It will then load these
 * settings to be editing in the "Transmitter Login" page.
 */
public final class EditTransLoginAction
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
        TransLoginForm tform = (TransLoginForm) form;
        if(DEBUG5) {
            tform.dump();
        }

        try {
            TLoginBean tbean = getTloginBean((ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB));
            request.getSession().setAttribute(SESSION_TLOGINBEAN, tbean);

        } catch (SystemException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
        //Add the transmitter bean to the session to be accessed from the page.
                String action = request.getParameter("action");
        if ("preview".equals(action)) {
            return (mapping.findForward("preview"));
        }
        return (mapping.findForward("success"));
    }

    protected TLoginBean getTloginBean(ISubscription sub) throws SystemException {
        Enumeration tprops = sub.getPropertyKeys(ISubscriptionConstants.PROP_TUNER_KEYWORD);

        TLoginBean tbean = new TLoginBean();
        String prop;
        String propvalue;
        String transmitter;
        int userIdx;
        int pwdIdx;
        int propLen;
        int mrbaKeychainLen = MARIMBA_KEYCHAIN.length();

        while (tprops.hasMoreElements()) {
            prop = (String) tprops.nextElement();

            if (prop.startsWith(MARIMBA_KEYCHAIN)) {
                propLen = prop.length();
                userIdx = prop.indexOf(".user", propLen - 5);

                if (userIdx > 0) {
                    transmitter = prop.substring(mrbaKeychainLen + 1, propLen - 5);
                    propvalue = getTChPropValue(sub.getProperty(PROP_TUNER_KEYWORD, prop));

                    if (!"null".equals(propvalue)) {
                        tbean.addTUser(transmitter, propvalue);
                    }

                    continue;
                }

                pwdIdx = prop.indexOf(".password", propLen - 9);

                if (pwdIdx > 0) {
                    transmitter = prop.substring(mrbaKeychainLen + 1, propLen - 9);
                    propvalue = getTChPropValue(sub.getProperty(PROP_TUNER_KEYWORD, prop));

                    if (!"null".equals(propvalue)) {
                        tbean.addTPwd(transmitter, propvalue);
                    }
                }
            }
        }
        return tbean;
    }





}
