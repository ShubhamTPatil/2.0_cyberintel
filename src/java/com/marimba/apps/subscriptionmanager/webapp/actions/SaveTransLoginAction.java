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

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Target;

import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.webapp.system.TLoginBean;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.forms.BlackoutForm;
import com.marimba.apps.subscriptionmanager.webapp.forms.TransLoginForm;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;

/**
 * This action corresponds to the editing of the tuner properties
 */
public final class SaveTransLoginAction
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

        //REMIND: throw an exception if they try to go here with multitg set.
        //try {

        try {
                ISubscription sub = null;
                sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);
                TLoginBean tbean = (TLoginBean) request.getSession().getAttribute(SESSION_TLOGINBEAN);
                setLoginProperties(sub, tbean);
                //tbean = getTloginBean((ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB));
                //request.getSession().setAttribute(SESSION_TLOGINBEAN, tbean);
                DistributionBean distributionBean = getDistributionBean(request);
                distributionBean.setTransmitterProps(tbean);
            } catch (SystemException e) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            }

        //TLoginBean tbean = (TLoginBean) request.getSession()
          //      .getAttribute(SESSION_TLOGINBEAN);

        //distributionBean.setTransmitterProps(tbean);
        TransLoginForm tform = (TransLoginForm) form;
        String forward = tform.getForward();
        return (new ActionForward(forward, true));

    }

   private void setLoginProperties(ISubscription sub, TLoginBean tbean) throws SystemException {
        if (tbean == null){
            return;
        }
        Enumeration remtransmitters = tbean.getDeletedTrans();
        String remtrans;

        for (; remtransmitters.hasMoreElements();) {
            remtrans = (String) remtransmitters.nextElement();
            sub.setProperty(PROP_TUNER_KEYWORD, MARIMBA_KEYCHAIN + "." + remtrans + ".user", null);
            sub.setProperty(PROP_TUNER_KEYWORD, MARIMBA_KEYCHAIN + "." + remtrans + ".password", null);
        }

        //get the transmitter username and passwords and set it to the subscription.
        Enumeration transmitters = tbean.getTransmitters();
        String curtrans;
        String uname;
        String pwd;

        for (; transmitters.hasMoreElements();) {
            curtrans = (String) transmitters.nextElement();
            uname = tbean.getUser(curtrans);
            pwd = tbean.getPwd(curtrans);
            sub.setProperty(PROP_TUNER_KEYWORD, MARIMBA_KEYCHAIN + "." + curtrans + ".user", uname);
            sub.setProperty(PROP_TUNER_KEYWORD, MARIMBA_KEYCHAIN + "." + curtrans + ".password", pwd);
        }

    }
}
