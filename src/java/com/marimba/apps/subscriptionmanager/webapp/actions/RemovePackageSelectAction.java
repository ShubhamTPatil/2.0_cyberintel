// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.*;

import java.io.IOException;

import java.util.List;
import java.util.ListIterator;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.objects.Channel;

/**
 * This corresponds to the action when the user clicks remove in the multiple select view of package details. It is used for clearing out the session variables
 * that stores the channels that the user has selected previously. If this was the last channel selected, the result set variable is removed entirely so that
 * the no packages selected page can be displayed. Rahul Ravulur 1.3, 03/21/2002
 */
public final class RemovePackageSelectAction
    extends AbstractAction {

    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
                     return new  RmvPkgSelectTask(mapping, form, request, response);
    }
     protected class RmvPkgSelectTask
               extends SubscriptionDelayedTask {
        RmvPkgSelectTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
                  super(mapping, form, request, response);
        }
       public void execute() {
        HttpSession session = request.getSession();

        String[]    spkgidx = request.getParameterValues("channelid");
        String requiredRemove = null;

        List channelList = (List) session.getAttribute(MAIN_PAGE_M_PKGS);

        for(int channelCount = 0;channelCount<spkgidx.length;channelCount++) {
            for (ListIterator ite = channelList.listIterator(); ite.hasNext();) {
                Channel itg = (Channel) ite.next();
                String urlName = spkgidx[channelCount];
                if (DEBUG) {
                    System.out.println("RemovePackageSelectAction:: removing url " + urlName);
                    System.out.println("looking through selected URLs " + itg.getUrl());
                }

                if (itg.getUrl()
                           .equals(urlName)) {
                    channelList.remove(itg);
                    requiredRemove = "1";
                    session.setAttribute("requiredRemove",requiredRemove);

                    if (DEBUG) {
                        System.out.println(" removed channel " + itg.getUrl());
                    }
                    break;
                }

            }
        }


        // this is a work-around till bug 29528 gets fixed correctly. So,
        // now if the user clicks X on the multiple selected package BUT
        // is in single select mode, just remove the variable and redirect so that
        // the page doesn't render incorrectly.
        if (session.getAttribute(SESSION_MULTIPKGBOOL) == null) {
            session.removeAttribute(MAIN_PAGE_M_PKGS);
            session.removeAttribute(SESSION_TGS_FROMPKGS_RS);

            forward = mapping.findForward("empty");
        }
        //To remove the targets list maintained in the session
        session.setAttribute(SESSION_TGTS_FROM_PKGS,null);
        // check if channels exist, if not, no need to forward to PackageDetailsAction
        // if they do, forward to it so that the new target list can be computed.
        if (channelList.size() > 0) {
            if (DEBUG) {
                System.out.println("Setting the new channelList in the session ");
            }

            session.setAttribute(MAIN_PAGE_M_PKGS, channelList);
            forward = mapping.findForward("success");
        } else {
            session.removeAttribute(MAIN_PAGE_M_PKGS);
            session.removeAttribute(SESSION_TGS_FROMPKGS_RS);
            forward = mapping.findForward("empty");

        }
    }
  }
}
