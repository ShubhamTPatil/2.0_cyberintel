// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.castanet.schedule.Schedule;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.KnownException;
import com.marimba.webapps.intf.SystemException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Carries out validation of assignment before the preview page is entered into.
 *
 * @author Alex Holmes
 * @version 1.3, 07/17/2002
 */

public final class DistAsgValidateAction extends AbstractAction {

    static final int MAX_INSTALL_PRIORITY = 99999;
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
            throws IOException, ServletException {

        if (DEBUG) {
            System.out.println("DistAsgValidateAction.perform: entering");
        }

        // perform any validation here before the preview page is shown
        try {
            DistributionBean distributionBean = getDistributionBean(request);
            validateSecSchedule(distributionBean);
            // validateInstallOrder(distributionBean);
        } catch (SystemException se) {
            throw new GUIException(se);
        }

        return (mapping.findForward("success"));
    }

    void validateInstallOrder(DistributionBean distributionBean) throws SystemException {

        // commenting out this code.  since we're doing validity checks when the apply button
        // is being selected, we don't need to do one here
        /*
        System.out.println("DistAsgValidateAction.validateInstallOrder: entering");

        ArrayList channelList = distributionBean.getChannels();
        int startingPriority =  distributionBean.getStartingPriority();

        if (DEBUG) {
            for (int i = 0; i < channelList.size(); i++) {
                System.out.println("channelList[" + i + "] = " + channelList.get(i));
            }
        }

        for (int i = 0; i < channelList.size(); i++) {
            Channel channel = (Channel) channelList.get(i);
            if(channel.getOrder() > MAX_INSTALL_PRIORITY) {
                throw new KnownException(IErrorConstants.ASSIGN_MAX_PRIORITY_EXCEED);
            }
        }*/
    }

    void validateSecSchedule(DistributionBean distributionBean) throws SystemException {
        ArrayList channels = distributionBean.getChannels();
        StringBuffer badChannels = null;

        for (Object aChannel : channels) {
            Channel channel = (Channel) aChannel;
            String secState = channel.getSecState();
            Schedule secSchedule = channel.getSecSchedule();
            String secSchedulString = channel.getSecScheduleString();

            if ((secState != null) && (secState.length() > 0) && (!secState.equals(INCONSISTENT)) && (secSchedule == null) && (secSchedulString == null)) {

                if (badChannels == null) {
                    badChannels = new StringBuffer(channel.getTitle());
                } else {
                    badChannels.append(", ").append(channel.getTitle());
                }
            }
            // Commented out to fix for the defect 44158.
            /*else if(secState == null)
            {
                channel.setSecSchedule((String)null);
                distributionBean.removeChannel(channel.getUrl());
                distributionBean.addChannel(channel);
            }*/
        }

        if (badChannels != null) {
            throw new KnownException(IErrorConstants.DIST_SAVE_NOSECSCHED, badChannels.toString());
        }
    }
}
