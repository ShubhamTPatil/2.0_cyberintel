// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions.common;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.util.MessageResources;

import java.io.IOException;

import java.util.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.marimba.apps.subscriptionmanager.webapp.actions.*;
import com.marimba.apps.subscriptionmanager.webapp.forms.*;
import com.marimba.apps.subscriptionmanager.webapp.system.*;

import com.marimba.webapps.intf.*;

/**
 * Populate all the check box items list so that the check boxes are either checked(select all) or unchecked (clear all)
 *
 * @author Theen-Theen Tan
 */
public class CheckBoxSetAction
    extends PersistifyChecksAction {
    protected int getStartIndex(GenericPagingBean pageBean) {
        return 0;
    }

    protected int getEndIndex(GenericPagingBean pageBean) {
        return pageBean.getTotal();
    }

    protected String getSelected(HttpServletRequest request,
                                 Object             parameter) {
        if (request.getParameter("setas")
                       .equals("true")) {
            return request.getParameter("setas");
        }

        return null;
    }

    protected ActionForward getReturnPage(HttpServletRequest request) {
	return new ActionForward(request.getParameter("forward"));
    }
}
