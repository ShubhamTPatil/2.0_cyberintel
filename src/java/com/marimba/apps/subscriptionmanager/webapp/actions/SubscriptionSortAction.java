// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.util.SubscriptionSortBean;
import com.marimba.webapps.intf.GUIException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;

/**
 * Handles the request for Sorting from Subscription 
 *
 * @author Narasimhan L Mahendrakumar
 * @version 1.00, 09/24/2003
 */
public final class SubscriptionSortAction
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
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException,
            ServletException {
		String doAction			= "";
		String sortObject		= "";
		String sortColumn		= "";
		String order			= "";

		try
		{
			
			doAction = request.getParameter("doaction");
			sortObject = request.getParameter("sortobject");
			sortColumn = request.getParameter("sortcolumn");
			order = request.getParameter("order");

			HttpSession        session = request.getSession();
			session.removeAttribute(IWebAppConstants.PC_STATUS_PAGEBEAN);		// This clears the paging buffer/bean.

			if("sort".equals(doAction)) {
				try
				{

					SubscriptionSortBean subSort = new SubscriptionSortBean();
					Comparator comparator = subSort.getComparator(sortObject,sortColumn,Integer.parseInt(order));
					session.setAttribute(sortObject,SubscriptionSortBean.sortList((Collection)session.getAttribute(sortObject),comparator));
					session.setAttribute("order",order);
					
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}

			}

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
            GUIException guie = new GUIException(ex);
            throw guie;
		}

        return (mapping.findForward("success"));
    }

}
