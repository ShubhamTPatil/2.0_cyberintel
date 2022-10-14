package com.marimba.apps.subscriptionmanager.webapp.actions;

/*  Copyright 2009, BMC Software. All Rights Reserved.
    Confidential and Proprietary Information of BMC Software.
    Protected by or for use under one or more of the following patents:
    U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
    and 6,430,608. Other Patents Pending.

    $File$, $Revision$, $Date$

     Display the selected Targets
     
     @author Selvaraj Jegatheesan
     @version 4, 2009/03/16
*/

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscriptionmanager.webapp.forms.TargetDetailsViewForm;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;

public class TargetDetailsView
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

        TargetDetailsViewForm targetForm =(TargetDetailsViewForm)form;
        HttpSession session = request.getSession();
        
        List targetList = getSelectedTargets(session);

        if (targetList != null ) {
            for (ListIterator ite = targetList.listIterator(); ite.hasNext();) {
                Target itg = (Target) ite.next();
                targetForm.setId(itg.getId());
                targetForm.setType(itg.getType());
                targetForm.setName(itg.getName());
                targetForm.setTargetsList(itg);
            }
        }
        
        if  (request.getParameter("src") != null) {
            // We came from another tab, repaint the whole page
            return mapping.findForward("bothpanes");
       }

        return mapping.findForward("success");
    }
}
