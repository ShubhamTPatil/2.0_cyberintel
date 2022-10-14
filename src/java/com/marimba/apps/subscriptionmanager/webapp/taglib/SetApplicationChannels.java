package com.marimba.apps.subscriptionmanager.webapp.taglib;

import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IARTaskConstants;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.ISubscriptionConstants;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;

/**
 * This taglib class handles the situation to reset the packages that is already assigned when the user
 * directly invokes the subscription from the Remedy AR System.
 *
 * @author Jayaprakash Paramasivam
 * @version 1.0, 10/18/2005

 */
public class SetApplicationChannels extends TagSupport {

    private String sessionBean;

    public void setSessionBean(String sessionBean) {
        this.sessionBean = sessionBean;
    }

    public String getSessionBean() {
        return sessionBean;
    }

    public int doStartTag() throws JspException {
        HttpSession session = pageContext.getSession();
        boolean singleMode;

        if (session.getAttribute(IWebAppConstants.SESSION_MULTITGBOOL) == null) {
            singleMode = true;
        } else {
            singleMode = false;
        }

        DistributionBean distributionBean = (DistributionBean) session.getAttribute(sessionBean);

        ArrayList targetMapList = (ArrayList) session.getAttribute(IWebAppConstants.SESSION_PKGS_FROMTGS_RS);

        if (targetMapList != null) {
            Channel firstch = null;
            Channel tempch;
            TargetChannelMap tempmap = null;

            if (targetMapList.size() > 0) {
                // designate that we are editing an existing policy assignment
                // so that the tab is highlighted correctly in the banner
                distributionBean.setType(IWebAppConstants.EDIT);
            }

            int minOrder = ISubscriptionConstants.ORDER;

            // Set the initial values for the schedules.
            // This is derived from the first package in the list.
            for (int i = 0; i < targetMapList.size(); i++) {
                tempmap = (TargetChannelMap) targetMapList.get(i);

                if (singleMode) {
                    // In single select mode, do not consider channels which
                    // are not directly assigned
                    if (!"true".equals(tempmap.getIsSelectedTarget())) {
                        continue;
                    }
                }

                tempch = tempmap.getChannel();

                if (firstch == null) {
                    firstch = tempch;
                }
                // Use case: Package1 is assigned to a target with the uninstall state. From invoking from AR system,
                // same Package1 is assigned to the same target with the install state. Here, override the existing state.
                Channel newch = distributionBean.getChannel(tempch.getUrl());
                if( newch == null ) {
                    distributionBean.addChannel(tempch);
                    distributionBean.addInitSchedule(tempch.getUrl(), tempch.getInitScheduleString());
                    distributionBean.addSecondarySchedule(tempch.getUrl(), tempch.getSecScheduleString());
                    distributionBean.addUpdateSchedule(tempch.getUrl(), tempch.getUpdateScheduleString());
                    distributionBean.addVerifyRepairSchedule(tempch.getUrl(), tempch.getVerRepairScheduleString());
                    distributionBean.addPostponeSchedule(tempch.getUrl(), tempch.getPostponeScheduleString());
                }


                /*For each channel, set whether or not the states are inconsisted.
                 *This is used for displaying the warning messages.  Once the inconsistent
                 *state flag is set true once, it will stay true.
                 */
                distributionBean.setInconsistentStates(tempch.getInconsistentStates());

                // set the starting priority to the min (highest) priority.
                if (tempch.getOrder() < minOrder) {
                    minOrder = tempch.getOrder();
                }
            }

            // if the prioties have already been initialized, set starting priorty to
            // the min priority else set it to 1
            if (minOrder == ISubscriptionConstants.ORDER) {
                distributionBean.setStartingPriority(1);
            } else {
                distributionBean.setStartingPriority(minOrder);
            }
        }
        session.removeAttribute(IARTaskConstants.AR_TARGET_REFRESH);
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }

}
