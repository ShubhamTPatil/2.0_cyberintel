// Copyright 1997-2009, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.forms;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.marimba.apps.subscription.common.ISubscriptionConstants;

import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;

import com.marimba.webapps.intf.*;


/**
 * Form for the add target details page.  The primary and secondary states select boxes are identified as state#&lt;channel object hashcode> and
 * secState#&lt;channel object hashcode>
 *
 * @author Jayaprakash Paramasivam
 * @version 1.0, 05/09/2005
 */
public class AddTargetEditForm
    extends AbstractForm
    implements ISubscriptionConstants,
                   IAppConstants,
                   IWebAppConstants {
    boolean initialized = false;
    private static ArrayList states = new ArrayList(8);
    private static String[]  statesConst = {STATE_SUBSCRIBE_NOINSTALL,
					    STATE_AVAILABLE,
					    STATE_SUBSCRIBE,
					    STATE_SUBSCRIBE_START,
					    STATE_SUBSCRIBE_PERSIST,
					    STATE_START_PERSIST,
					    STATE_EXCLUDE,
					    STATE_DELETE,
					    STATE_PRIMARY};
    static {
        states.clear();

        for (int i = 0; i < statesConst.length; i++) {
            states.add(statesConst [i]);
        }
    }

    // Variables for the values and display values for primary and secondary
    // states for packages
    // The state labels should not be static as the strings are different
    // for different locales
    private ArrayList statesLabel = new ArrayList(8);
    private HashMap   stateMap = new HashMap(DEF_COLL_SIZE);
    private HashMap secStateMap = new HashMap(DEF_COLL_SIZE);

    HashMap checkedItems = new HashMap(DEF_COLL_SIZE);

    /**
     * REMIND
     *
     * @return REMIND
     */
    public ArrayList getStates() {
        return states;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public ArrayList getStatesLabel() {
        return statesLabel;
    }

    public void initialize() {
        if (!initialized) {
            props.clear();
            props.put((Object) "sortorder", (Object) "true");
            props.put((Object) "sorttype", (Object) "name");
            props.put((Object) "lastsort", (Object) "name");
            // Below deals with persitifying check boxes across page
            props.put(SESSION_PERSIST_SELECTED, SESSION_ADD_PAGEPKGS_SELECTED);
            props.put(SESSION_PERSIST_PREFIX, SESSION_ADD_PAGEPKGS_PREFIX);
            props.put(SESSION_PERSIST_BEANNAME, SESSION_ADD_PAGEPKGS_BEAN);
        }
        initialized = true;
    }


    public void init(MessageResources resources,
                                    Locale locale ) {
        stateMap.clear();
        secStateMap.clear();

        /* Iterate through and establish the initial value of the schedules radio button
         */

        statesLabel.clear();

        /* Obtain the distribution bean so that we know how to initialize
         * the fields for schedIncMap.  These is using for filling out values
         * for the radio buttons if there are inconsistent states or schedules
         *
         */
        for (int i = 0; i < statesConst.length; i++) {
            statesLabel.add(resources.getMessage(locale, "page.global." + statesConst [i] + ".uppercase"));
        }

        checkedItems.clear();
    }

    /**
     * Validate the properties that have been set from this HTTP request, and return an <code>ActionErrors</code> object that encapsulates any validation
     * errors that have been found.  If no errors are found, return <code>null</code> or an <code>ActionErrors</code> object with no recorded error messages.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     *
     * @return REMIND
     */
    public ActionErrors validate(ActionMapping      mapping,
                                 HttpServletRequest request) {
        return null;
    }

    /**
     * REMIND
     *
     * @param property REMIND
     * @param value REMIND
     */
    public void setValue(String property,
                    Object value) {
        if (property.startsWith("state#")) {
            String hashcode = property.substring(6);
            //stateMap.put(hashcode, ((String[]) value) [0]);
            stateMap.put(hashcode, value);
        } else if (property.startsWith("secState#")) {
            String hashcode = property.substring(9);
            //secStateMap.put(hashcode, ((String[]) value) [0]);
            secStateMap.put(hashcode, value);
        } else if (property.startsWith(SESSION_ADD_PAGEPKGS_PREFIX)) {
	    checkedItems.put(property, value);
	} else {
	    // Note, we should not use the AbstractForm.set method.  It will cause
	    // check boxes "persistifying" across pages to break
	    props.put(property, value);
	}
    }

    /**
     * REMIND
     *
     * @param property REMIND
     *
     * @return REMIND
     */
    public Object getValue(String property) {
        Object rval = null;

        if (property.indexOf("state#") != -1) {
            String hashcode = property.substring(6);
            return stateMap.get(hashcode);
        } else if (property.indexOf("secState#") != -1) {
            String hashcode = property.substring(9);

            return secStateMap.get(hashcode);
        } if (property.startsWith(SESSION_ADD_PAGEPKGS_PREFIX)) {
            return checkedItems.get(property);
        } else {
	    return props.get(property);
	}
    }

    public void clearCheckedItems() {
        checkedItems.clear();
    }

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        this.clearCheckedItems();
    }

    public void clearPagingVars(HttpServletRequest request) {
	request.getSession().removeAttribute(SESSION_ADD_PAGEPKGS_SELECTED);
	request.getSession().removeAttribute(SESSION_ADD_PAGEPKGS_BEAN);
	clearCheckedItems();
    }

    public void clearSessionVars(HttpServletRequest request) {
        clearCheckedItems();
        request.getSession().removeAttribute(SESSION_PERSIST_RESETRESULTS);
        request.getSession().removeAttribute("add_common_target");
        request.getSession().removeAttribute("add_remove_package");
        request.getSession().removeAttribute("add_selected_list");
        request.getSession().removeAttribute("context");
        request.getSession().removeAttribute("add_remove_target");
        request.getSession().removeAttribute("add_remove_patch");        
    }

}
