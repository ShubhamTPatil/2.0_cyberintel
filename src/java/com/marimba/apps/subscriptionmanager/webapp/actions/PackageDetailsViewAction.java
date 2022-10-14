package com.marimba.apps.subscriptionmanager.webapp.actions;

/*  Copyright 2009, BMC Software. All Rights Reserved.
    Confidential and Proprietary Information of BMC Software.
    Protected by or for use under one or more of the following patents:
    U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
    and 6,430,608. Other Patents Pending.

    $File$, $Revision$, $Date$

     Display the selected Packages

     @author Selvaraj Jegatheesan
     @version 4, 2009/03/16
*/

import java.util.List;
import java.util.ListIterator;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscriptionmanager.webapp.forms.PackageDetailsViewForm;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;



public class PackageDetailsViewAction
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
     * @throws ServletException REMIND
     * @throws GUIException REMIND
     */
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
        throws ServletException {

        PackageDetailsViewForm channelForm =(PackageDetailsViewForm)form;

        HttpSession session = request.getSession();
        String channelSessionVar = MAIN_PAGE_M_PKGS;

        List channelList = (List) session.getAttribute(channelSessionVar);

        if (channelList != null) {
            for (ListIterator ite = channelList.listIterator(); ite.hasNext();) {
                Channel itg = (Channel) ite.next();
                channelForm.setUrl(itg.getUrl());
                channelForm.setType(itg.getType());
                channelForm.setTitle(itg.getTitle());
                channelForm.setChannelsList(itg);
            }
        } 

        if  (request.getParameter("src") != null) {
            // We came from another tab, repaint the whole page
            return mapping.findForward("bothpanes");
       }

        return mapping.findForward("success");
    }
}
