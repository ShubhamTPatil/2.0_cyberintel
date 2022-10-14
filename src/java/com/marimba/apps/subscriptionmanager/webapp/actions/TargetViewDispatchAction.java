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

import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.marimba.apps.subscriptionmanager.webapp.forms.LDAPNavigationForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;

import com.marimba.webapps.intf.SystemException;
import com.marimba.tools.util.URLUTF8Encoder;

/**
 * REMIND
 *
 * @author $Author$
 * @version $File$
 */
public class TargetViewDispatchAction
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

        if (DEBUG2) {
            System.out.println(dispatch2(request) + getQueryString(request));
            System.out.println("dispatching to -- " + dispatch2(request) + getQueryString(request));
        }


        return new ActionForward(dispatch2(request) + getQueryString(request));
    }

    private String getQueryString(HttpServletRequest request) {
        Enumeration ex = request.getParameterNames();
        StringBuffer sb = new StringBuffer(256);
        sb.append("?");

        while (ex.hasMoreElements()) {
            String s = (String) ex.nextElement();
            sb.append(s);
            sb.append("=");
            sb.append(com.marimba.tools.util.URLUTF8Encoder.encode(request.getParameter(s)));
            sb.append("&");
        }

        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    String dispatch2(HttpServletRequest request) {
        String ctx = null;

        try {
            ctx = (String) GUIUtils.getFromSession(request, "context");
        } catch (Exception e) {
            e.printStackTrace(); //To change body of catch statement use Options | File Templates.
        }

        if (ctx == null) {
            return "/targetDetailsAdd.do";
        } else {
            return "/" + ctx + ".do";
        }
    }
}
