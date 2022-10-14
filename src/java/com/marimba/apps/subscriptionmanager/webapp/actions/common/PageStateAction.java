// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions.common;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;

import java.util.Enumeration;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;

import com.marimba.webapps.intf.IMapProperty;

/**
 * General purpose action that is kept in the session to maintain the state of a page.  The form that is to maintain the state is set into the
 *
 * @author Angela Saval
 */
public class PageStateAction
    extends AbstractAction {
    /**
     * This method is called when the action is submitted.  The page state action assumes that the form being passed in implements IMapProperty.  If it
     * doesn't, then nothing will be set. The IMapProperty is used for tracking the page state so that we can arbitrarily set anything we want.
     *
     */
    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
                     return new  PageStateTask(mapping, form, request, response);
    }
    protected class PageStateTask
               extends SubscriptionDelayedTask {
        PageStateTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
                  super(mapping, form, request, response);
     }
     public void execute() {
        //Determine if the form implements IMapProperty
        if (!(form instanceof IMapProperty) || request.getMethod()
                                                       .equalsIgnoreCase("POST")) {
            if (DEBUG) {
                System.out.println("PageStateAction: request uri = " + request.getRequestURI());
                System.out.println("PageStateAction: request url = " + request.getContextPath());
            }
            forward = new ActionForward(mapping.getInput(),true);
        }
        session.removeAttribute(SESSION_TGS_FROMPKGS_SELECTED);
         /*Obtain the request parameters that are sent in for the page state.
         * Set them into the form.  The form must be a set into a session
         */
        Enumeration paramnames = request.getParameterNames();

        if (paramnames != null) {
            String   key;
            String[] values;
            String   beanval;
            for (; paramnames.hasMoreElements();) {
                beanval = null;
                key     = (String) paramnames.nextElement();
                values  = request.getParameterValues(key);
                //it is assumed that the parameters being set only have one
                //value.
                if ((values != null) && (values.length > 0)) {
                    beanval = values [0];
                }

                ((IMapProperty) form).setValue(key, (Object) beanval);

                if (DEBUG) {
                    System.out.println("PageStateAction: key = " + key);
                    System.out.println("PageStateAction: form = " + ((IMapProperty) form).getValue(key));
                }
            }
        }

        /* The return parameter should be the page that this action was submitted from
         */
        if (DEBUG) {
            System.out.println("PageStateAction: request uri = " + request.getRequestURI());
            System.out.println("PageStateAction: request input = " + mapping.getInput());
        }

        if (request.getParameter("forward") != null) {
            forward = new ActionForward((String) request.getParameter("forward"));
        }else {
            forward =  new ActionForward(mapping.getInput());
        }
    }
  }
}