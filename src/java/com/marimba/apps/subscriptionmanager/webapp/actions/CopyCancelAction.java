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
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscriptionmanager.webapp.forms.CopyEditForm;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscription.common.objects.Target;

/**
 * This class clears the form variables and session varaibles
 *
 * @author  Jayaprakash Paramasivam
 * @version 1.0, 31/12/2004
 */
public final class CopyCancelAction
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
        DistributionBean copyBean = getDistributionBeanCopy(request);
        ArrayList targets = copyBean.getTargets();
        init(request);
        ((CopyEditForm) form).removeSelectedtargets(request);
        ((CopyEditForm) form).clearPagingVars(request,targets);
        removeDistributionBeanCopy(request);
        // The below block should be after removing the copy session bean value
        // Update Pending Policy session variable 
        DistributionBean distributionBean = getDistributionBean(request);
        ArrayList selTargets = distributionBean.getTargets();
        for(Object target : selTargets) {
        	main.updatePendingPolicySessionVar(request, (Target)target);
        }
        return (mapping.findForward("success"));
    }
    private void removeSelectedPendingPolicyTargets(HttpServletRequest request) {
    	
    }
}
