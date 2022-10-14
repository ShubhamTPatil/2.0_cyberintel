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

import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.objects.dao.LDAPSubscription;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.forms.PackageDetailsForm;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.intf.CriticalException;

/**
 * This class handles the situation when a user selects a package from the package view page. It desals with the fact that the user may be in single select
 * mode or multiple select mode. It is used within the oackage_navigation_area.jsp page. The logic followed is identical to TargetDetailsAddACtion. Rahul
 * Ravulur 1.11, 11/08/2002
 */
public final class PackageDetailsAddAction
    extends AbstractAction {
    //REMIND::RCR get rid of this
//    static String[] searchAttrs = {
//                                      LDAPVars.SUBSCRIPTION_NAME, LDAPVars.TARGETDN, LDAPVars.TARGETTYPE, LDAPVars.TARGET_TX_USER, LDAPVars.TARGET_TX_GROUP,
//                                      LDAPVars.CHANNELINITSCHED, LDAPVars.CHANNELSECSCHED, LDAPVars.CHANNELUPDATESCHED, LDAPVars.CHANNELVERREPAIRSCHED,
//                                      LDAPVars.CHANNEL, LDAPVars.CHANNELSEC, LDAPVars.CHANNELTITLE
//    };

    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
                     return new  PkgDtlAddTask(mapping, form, request, response);
    }

    protected class PkgDtlAddTask
               extends SubscriptionDelayedTask {
        PkgDtlAddTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
                  super(mapping, form, request, response);
    }
    public void execute() {
        
        try {
        boolean multiMode = false; //Used to determine if the user is in multiple select mode
        // get a reference to SubscriptionMain and interrogate it for properties
        ServletContext   servletContext = servlet.getServletConfig()
                                                 .getServletContext();
        SubscriptionMain main = TenantHelper.getTenantSubMain(servletContext, request);
        HttpSession      session = request.getSession();
        String           channelSessionVar = MAIN_PAGE_PACKAGE;
        GUIUtils.initForm(request, mapping);


        // determine what mode the user is in, and based on that decide
        // what session variable holds the channels.
        if (session.getAttribute(SESSION_MULTIPKGBOOL) != null) {
            multiMode         = true;
            channelSessionVar = MAIN_PAGE_M_PKGS;
        }
        List channelList = (List) session.getAttribute(channelSessionVar);

        if (null == channelList) {
            channelList = new ArrayList(DEF_COLL_SIZE);
        } else {
            if (!multiMode) {
                channelList.clear();
            }
        }
        // remove the bean to indicate that the results need to be obtained again
        // so that the cached results are not displayed
        session.removeAttribute(TGS_FROMPKGSLIST_BEAN);

        String channelURL = request.getParameter("channelURL");
        String channelTitle = request.getParameter("channelTitle");
        String channelType = request.getParameter("channelType");
        if (channelType == null) {
            channelType = CONTENT_TYPE_APPLICATION;
        }
        // channelURL would be null if the user clicked remove in a multi-mode on
        // the package details page. The RemovePackageAction takes care of the
        // channel session variables and forwards to here, so that the target
        // list can be updated with the new slimmer channel list.
        if (channelURL != null) {
            try {

            // REMIND::RCR deal with this correctly?
            // these are incompletely filled channel objects
            // when the query is completed in the tag, they will be replaced
            // by filled in channel objects.
            Channel channel = LDAPSubscription.createContent(channelURL, STATE_SUBSCRIBE + "," + channelType, null, main.getLDAPVarsMap(), 
            		main.getTenantName(), main.getChannel());
            channel.setTitle(channelTitle);

            boolean contains = false;
            /* Ensure that there are no repeats in the list
             */
            for (ListIterator ite = channelList.listIterator(); ite.hasNext();) {
                Channel itg = (Channel) ite.next();

                if (itg.getUrl()
                           .equals(channel.getUrl())) {
                    contains = true;

                    break;
                }
            }

            if (!contains) {
                channelList.add(channel);
            }

            if (DEBUG) {
                System.out.println("multiMode is " + multiMode);
                System.out.println("ADDED channel= " + channel);
                System.out.println("channelType= " + channelType);
                System.out.println("channelsessionvar is " + channelSessionVar);
            }

            // Set new channel list to session var
                GUIUtils.setToSession(request, channelSessionVar, (Object) channelList);
            } catch (SystemException se) {
                GUIException guie = new GUIException(se);
                throw guie;
            } finally{
                // clears selected targets selected related to previous package
                ((PackageDetailsForm) form).clearCheckedItems();
                GUIUtils.removeFromSession( request, SESSION_TGS_FROMPKGS_SELECTED );
            }
        }

        if (DEBUG) {
            Iterator iter = channelList.iterator();

            if (iter.hasNext() == false) {
                System.out.println("no channels");
            } else {
                int count = 0;

                while (iter.hasNext()) {
                    System.out.println("channel " + count++ + "= " + iter.next());
                }
            }
        }
         forward = mapping.findForward("success");
        } catch (Exception ex) {
            guiException = new GUIException(new CriticalException(ex.toString()));
            forward = mapping.findForward("failure");
       }
    }
}
}