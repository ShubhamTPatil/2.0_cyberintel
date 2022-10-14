// Copyright 2007-2011, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.distribution;

import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.intf.msf.IUserPrincipal;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;


/**

 * JSON service to support Distribution Edit related operations.
 * @author Marie Antoine
 */

public class DistributionService implements IWebAppConstants {
    protected SubscriptionMain main;
    protected IUserPrincipal user;
    protected HttpSession session = null;

    public DistributionService(SubscriptionMain main, IUserPrincipal _user) {
        this.main = main;
        this.user = _user;
    }

    public void setUserSession(HttpSession session) {
        this.session = session;
    }

    public void changeState(String url, String property, String value) {

        Map selMap = (Map) session.getAttribute(SESSION_SELECT_PACKAGES);
        if(null == selMap) {
            selMap = new HashMap(1);
        }

        DistributionBean distbean = (DistributionBean) session.getAttribute(SESSION_DIST);
        Channel channel = distbean.getChannel(url);

        if(channel != null) {
            PackageBean pkgBean = null;
            Object pkgObj = selMap.get(url);
            if(null == pkgObj) {
                pkgBean = new PackageBean();
                pkgBean.setUrl(url);
            }
            else {
                pkgBean = (PackageBean) pkgObj;
            }

            if("primary_state".equals(property)) {
                pkgBean.setPrimaryState(value);
            }

            else if("secondary_state".equals(property)) {
                pkgBean.setSecondaryState(value);
            }

            else if("exemptBo".equals(property)) {
                pkgBean.setBlackout(value);
            }

            else if("wowDep".equals(property)) {
                pkgBean.setWow(value);
            }

            else {
                System.out.println("Cannot find the state specified");
            }

            selMap.put(url, pkgBean);
        }
        else {
            System.out.println("Could not find the channel in session: "+ url);
        }
    }

    public void changeWowBlkoutState(String url, String property, String value) {

        DistributionBean distbean = (DistributionBean) session.getAttribute(SESSION_DIST);
        Channel channel = distbean.getChannel(url);
        boolean stateValue = false;
        if(channel != null) {
            if("exemptBo".equals(property)) {
                if("true".equals(value)) stateValue = true;
                else stateValue= false;
                channel.setExemptFromBlackout(stateValue);
            } else if("wowDep".equals(property)) {
                if("true".equals(value)) stateValue = true;
                else stateValue= false;
                channel.setWowEnabled(stateValue);
            } else if ("state".equals(property)) {
                channel.setState(value);
            } else if ("secState".equals(property)) {
                if ("null".equals(value)) {
                    value="";
                }
                channel.setSecState(value);
            } else {
                System.out.println("Cannot find the state specified");
            }

        } else {
            System.out.println("Could not find the channel in session: "+ url);
        }
    }


}

