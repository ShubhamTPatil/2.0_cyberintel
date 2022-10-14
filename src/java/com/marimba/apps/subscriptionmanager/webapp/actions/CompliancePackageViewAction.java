// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import com.marimba.webapps.intf.IWebAppsConstants;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import java.io.IOException;


/**
 * Set the context and forwads on to the compliance package view page.
 * If 'forward' request parameter is set, the action will forward to
 * that page or action.
 *
 *
 * @author Nageswara Rao.V
 * @version $Date$, $Revision$
 */
public final class CompliancePackageViewAction
        extends AbstractAction implements IWebAppConstants {
    /**

     *
     * @param mapping REMIND
     * @param form REMIND
     * @param request REMIND
     * @param response REMIND
     *
     * @return REMIND
     *
     */
    public ActionForward perform(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
        throws IOException,
            ServletException {
    	init(request);
        GUIUtils.initForm(request, mapping);
        //To remove the targets list maintained in the session
        HttpSession session = request.getSession();
        session.removeAttribute( IWebAppConstants.POLICIES_FORPKGNAME );
        session.removeAttribute( IWebAppConstants.SESSION_DISPLAY_RS );
        session.removeAttribute( IWebAppConstants.PACKAGE_DETAILS_FORPKG);
        Object bean = request.getSession().getAttribute(IWebAppsConstants.INITERROR_KEY);

    	if ((bean != null) && bean instanceof Exception) {
    		//remove initerror from the session because it has served its purpose
    		if (DEBUG) {
    			System.out.println("CompliancePackageViewAction: critical exception found");
    		}
    		request.getSession().removeAttribute(IWebAppsConstants.INITERROR_KEY);
    		return mapping.findForward("failure");
    	}
        return (new ActionForward(mapping.getInput(), false));
    }
}