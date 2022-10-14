// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.*;

import java.io.IOException;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.intf.SubKnownException;

import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IMapProperty;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.intf.KnownException;

/**
 * This top action is responsible for looking at all the properties in the text area. These properties are assumed to be what the user wants to set.  It clears
 * out the old properties that the user set earlier, and then  sets the properties in the text area into the subscription object. These properties are then
 * stored in the session again.
 *
 * @author Angela Saval
 * @author Theen-Theen Tan
 */
public class SaveTempPropsAction
        extends AbstractAction {
    // This action is explicitly called only when the drop-down in the
    // tuner properties is used. This will make sure that the user edits of the properties
    // in the text area aren't lost.
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException,
            ServletException {

        //update props

		processTempProps(form, request);


		String tunerPropDropDown = request.getParameter("tunerprop");
        String tunerPropType = request.getParameter("tunerproptype");

       // String common_tunerprops = request.getParameter("common_tunerprops");
        // Try to load the value set for the Tuner property that is chosen
        // from the pull down menu.
        // If there is no value set,
        //   - if a boolean, set to true
        //   - if not a boolean, clear out the value

        try {
            if (tunerPropDropDown != null ) {
                ISubscription oldsub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);
                String tunerPropVal = oldsub.getProperty(PROP_TUNER_KEYWORD, tunerPropDropDown);
                //((IMapProperty) form).set("common_tunerprops", common_tunerprops);
                if (tunerPropVal != null) {
                    ((IMapProperty) form).setValue("value(tunerpropvalue)", tunerPropVal);

                } else {
                    ((IMapProperty) form).setValue("value(tunerpropvalue)", null);
                }
            }
         } catch (SystemException se) {
                throw new GUIException(TUNERCHPROPS_SAVE_ERROR, se);
         }

        //String forward = "/target/advancedpkgview.jsp" + "?tunerprop=" + tunerPropDropDown;
         	// and forward to the next action
        String forward = (String)((IMapProperty) form).getValue("forward");
	    return (new ActionForward(forward, true));
    }

    /**
     * This method is responsible for applying the properties in the textarea. These properties are read in from a request variable allproperties. Then old
     * properties are then cleared out from the subscription object. We do this, so as to avoid having to figure out the changes the user might have done
     * while viewing the page. Then the new properties from the text area are applied and the properties saved to the session again (for clearing out on the
     * next submit). This method will also be called by the sub-classed actions that are invoked when the user clicks apply or save on the properties page.
     *
     * @param form REMIND
     * @param request REMIND
     *
     * @throws GUIException REMIND
     */
    protected void processTempProps(ActionForm form,
                                    HttpServletRequest request)
            throws GUIException {
        // get contetents of text box
        String[] allvalues = request.getParameterValues("value(allproperties)");
        String priority = ("priority".equals(request.getParameter("apply")) ? request.getParameter("priority_value") : "");
        int priorityInt = -1;

        try {
            if( priority == null || "0".equals(priority)) {
                throw new KnownException(PROPERTY_PRIORITY_EXCEED);
            }

            if (priority.trim().length() > 0) {
                priorityInt = Integer.parseInt(priority);
            }
        } catch (Exception ke) {
            throw new GUIException(TUNERCHPROPS_SAVE_ERROR, ke);
        }

        Enumeration paramnames = request.getParameterNames();

        if (paramnames != null) {
            String key;
            String[] values;
            String beanval;

            for (; paramnames.hasMoreElements();) {
                beanval = null;
                key = (String) paramnames.nextElement();
                values = request.getParameterValues(key);

                //it is assumed that the parameters being set only have one
                //value.
                if ((values != null) && (values.length > 0)) {
                    beanval = values[0];
                }

                ((IMapProperty) form).setValue(key, beanval);

                if (DEBUG) {
                    System.out.println("SaveTempPropsAction: key = " + key);
                    System.out.println("SaveTempPropsAction: form = " + ((IMapProperty) form).getValue(key));
                }
            }
        }

        if (DEBUG) {
            if ( allvalues != null ){
            System.out.println(" setting tempProps to be : " + allvalues.length);

            for (int i = 0; i < allvalues.length; i++) {
                System.out.println(" propvalues are " + allvalues[i]);
            }
            } else {
                System.out.println("text area allvelues == null");
            }

        }

        try {
            // clear all properties. we will populate with what is
            // modified in the advanced tab.
            ISubscription oldsub = 
                (ISubscription) GUIUtils.getFromSession(
                        request, PAGE_TCHPROPS_SUB);

            // no need to clear the poweroption properties, hence we clearing properties other than that.

            String[] propertyTypes = { PROP_TUNER_KEYWORD, PROP_SERVICE_KEYWORD, PROP_CHANNEL_KEYWORD, PROP_ALL_CHANNELS_KEYWORD, PROP_CRS_KEYWORD };

            oldsub.clearProperties(propertyTypes);
            oldsub.clearChannelsProperties();
            Vector allproperties = new Vector();

            if (allvalues != null) {
                StringTokenizer strt = new StringTokenizer(allvalues[0], "\n\r");
                String propandkey;

                while (strt.hasMoreElements()) {
                    propandkey = strt.nextToken();

                    int index = propandkey.indexOf('=');
                    String value = propandkey.substring(index + 1);

                    if (value != null && priority.length() > 0) {
                        index = value.indexOf(PROP_DELIM);
                        if (index == -1) {
                            propandkey = propandkey + PROP_DELIM + priority ;
                        } else {
                            propandkey = propandkey.substring(0, propandkey.lastIndexOf(PROP_DELIM) + 1) + priority ;
                        }
                    }
    
                    if (LDAPWebappUtils.isValidProperty(propandkey)) {

                        allproperties.addElement(propandkey);

                        if (DEBUG) {
                            System.out.println("SaveTempPropsAction: adding property = " + propandkey + " with " + ((priorityInt == -1) ? "no priority" : "priority = " + priority));
                        }
                    }

                    oldsub.loadProperties(allproperties);
                }
            }

        } catch (Exception e) {
            throw new GUIException(TUNERCHPROPS_SAVE_ERROR, e);
        }
    }
}
