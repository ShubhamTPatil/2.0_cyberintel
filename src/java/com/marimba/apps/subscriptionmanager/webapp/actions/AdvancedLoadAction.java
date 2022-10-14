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

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.ISubscriptionConstants;

import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.webapp.forms.BlackoutForm;
import com.marimba.apps.subscriptionmanager.webapp.forms.AdvancePkgForm;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.system.TLoginBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;

/**
 * This action is called when visiting the blackout page. It reads the blackout string from
 * the DistributionBean and populates the form for display.
 *
 * @author Devendra Vamathevan
 *
 */
public final class AdvancedLoadAction extends AbstractAction {
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest req,
                                 HttpServletResponse response)
            throws IOException,
            ServletException {

        try {


            if (DEBUG) {
                System.out.println("AdvancedLoadAction called");
            }
            //Initialize the form since it is used for page state
            GUIUtils.initForm(req, mapping);

            // read the black out string from SESSION_DIST Distribution bean.
            DistributionBean distributionBean = getDistributionBean(req);
            Vector propsMap = distributionBean.getTunerProps();
            TLoginBean tbean =  getTloginBean((ISubscription)GUIUtils.getFromSession(req, PAGE_TCHPROPS_SUB));



            if (DEBUG3) {
                tbean.debug();
            }

            req.getSession()
                    .setAttribute(SESSION_TLOGINBEAN, tbean);

        } catch (SystemException se) {
            GUIException guie2 = new GUIException(se);
            throw guie2;
        }
        // check if user clicked preview
        String action = req.getParameter("action");
        if ("preview".equals(action)) {
            return mapping.findForward("preview");
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
                    propvalue = sub.getProperty(PROP_TUNER_KEYWORD, prop);

                    if (!"null".equals(propvalue)) {
                        tbean.addTUser(transmitter, propvalue);
                    }

                    continue;
                }

                pwdIdx = prop.indexOf(".password", propLen - 9);

                if (pwdIdx > 0) {
                    transmitter = prop.substring(mrbaKeychainLen + 1, propLen - 9);
                    propvalue = sub.getProperty(PROP_TUNER_KEYWORD, prop);

                    if (!"null".equals(propvalue)) {
                        tbean.addTPwd(transmitter, propvalue);
                    }
                }
            }
        }
        return tbean;
    }
}
