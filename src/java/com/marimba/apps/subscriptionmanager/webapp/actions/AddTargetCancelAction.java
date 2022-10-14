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

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscriptionmanager.webapp.forms.AddTargetEditForm;

/**
 * This class clears the form variables and session varaibles
 *
 * @author  Jayaprakash Paramasivam
 * @version 1.0, 05/11/2004
 */
public final class AddTargetCancelAction
    extends AbstractAction {

    /**
     * @param mapping The ActionMapping used to select this instance
     * @param request The non-HTTP request we are processing
     * @param response The non-HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
        throws IOException, 
                   ServletException {
        ((AddTargetEditForm) form).clearSessionVars(request);
//        ((AddTargetEditForm) form).clearPagingVars(request);
        return (mapping.findForward("success"));
    }
}
