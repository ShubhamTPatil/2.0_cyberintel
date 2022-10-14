// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.forms;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.objects.Channel;

import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;


/**
 * Form for the add patch details page.  The primary states and exempt blackout are identified as state#&lt;channel object hashcode> and
 * exemptbo#&lt;channel object hashcode>
 *
 * @author Jayaprakash Paramasivam
 * @version 1.0, 05/31/2006
 */

public class AddTargetEditPatchForm
    extends AbstractForm
    implements ISubscriptionConstants,
                   IAppConstants,
                   IWebAppConstants {
    boolean initialized = false;
    private static ArrayList states = new ArrayList(2);
    private static String[]  statesConst = {STATE_SUBSCRIBE,
					    STATE_EXCLUDE};
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
    private ArrayList statesLabel = new ArrayList(2);
    private HashMap   stateMap = new HashMap(DEF_COLL_SIZE);
    private HashMap exemptBoMap = new HashMap(DEF_COLL_SIZE);
    private HashMap wowDepMap = new HashMap(DEF_COLL_SIZE);

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
            props.put(SESSION_PERSIST_SELECTED, SESSION_ADD_PAGEPATCH_SELECTED);
            props.put(SESSION_PERSIST_PREFIX, SESSION_ADD_PAGEPATCH_PREFIX);
            props.put(SESSION_PERSIST_BEANNAME, SESSION_ADD_PAGEPATCH_BEAN);
        }
        initialized = true;
    }


    public void init(MessageResources   resources,
                           Locale             locale,
                           HttpServletRequest request) {
        stateMap.clear();

        /* Iterate through and establish the initial value of the schedules radio button
         */

        statesLabel.clear();

//        exemptBoMap.clear();

        checkedItems.clear();

        for (int i = 0; i < statesConst.length; i++) {
            statesLabel.add(resources.getMessage(locale, "page.global." + statesConst [i] + ".patch.uppercase"));
        }

        initStateAndExempt(request);
    }

    void initStateAndExempt(HttpServletRequest request) {
        List targets = (ArrayList) request.getSession().getAttribute(ADD_REMOVE_PATCH);
        if (targets == null) {
            targets = new ArrayList(DEF_COLL_SIZE);
        }
        for (int i=0; i<targets.size();i++) {
            TargetChannelMap tcmap = (TargetChannelMap) targets.get(i);
            if ("true".equals(tcmap.getExemptFromBlackout())) {
                setValue("exemptBo#" + tcmap.hashCode(), "true");
            } else {
                setValue("exemptBo#" + tcmap.hashCode(), "false");
            }

            if ("true".equals(tcmap.getWowEnabled())) {
                setValue("wowDep#" + tcmap.hashCode(), "true");
            } else {
                setValue("wowDep#" + tcmap.hashCode(), "false");
            }
        }
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
        } else if (property.startsWith("exemptBo#")) {
            String hashcode = property.substring(9);
            //secStateMap.put(hashcode, ((String[]) value) [0]);
            exemptBoMap.put(hashcode, value);
        } else if (property.startsWith("wowDep#")) {
            String hashcode = property.substring(7);
            wowDepMap.put(hashcode, value);
        } else if (property.startsWith(SESSION_ADD_PAGEPATCH_PREFIX)) {
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
        if (property.indexOf("state#") != -1) {
            String hashcode = property.substring(6);
            return stateMap.get(hashcode);
        } else if (property.indexOf("exemptBo#") != -1) {
            String hashcode = property.substring(9);
            return exemptBoMap.get(hashcode);
        } else if (property.indexOf("wowDep#") != -1) {
            String hashcode = property.substring(7);
            return wowDepMap.get(hashcode);
        } if (property.startsWith(SESSION_ADD_PAGEPATCH_PREFIX)) {
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
        // let's clear exempt from blkout checkbox values for the
        // current page.
        GenericPagingBean pageBean = 
            (GenericPagingBean) request.getSession()
                                       .getAttribute(SESSION_ADD_PAGEPATCH_BEAN);
        if(pageBean != null) {
            // clearing only when the form is submitted and not during
            // the request is forwarded to other action classes 
            if("POST".equals(request.getMethod())) {
                List results = pageBean.getResults();
                for(int i=pageBean.getStartIndex(); i<pageBean.getEndIndex(); i++ ) {
                    TargetChannelMap tcm = (TargetChannelMap) results.get(i);
                    exemptBoMap.put(String.valueOf(tcm.hashCode()), "false");
                    wowDepMap.put(String.valueOf(tcm.hashCode()), "false");
                }
            }
        }
    }

    public void clearPagingVars(HttpServletRequest request) {
	request.getSession().removeAttribute(SESSION_ADD_PAGEPATCH_SELECTED);
	request.getSession().removeAttribute(SESSION_ADD_PAGEPATCH_BEAN);
	clearCheckedItems();
    }

    public void clearSessionVars(HttpServletRequest request) {
        clearCheckedItems();
        request.getSession().removeAttribute(SESSION_PERSIST_RESETRESULTS);
        request.getSession().removeAttribute("add_common_target");
        request.getSession().removeAttribute("add_remove_patch");
        request.getSession().removeAttribute("add_selected_list");
        request.getSession().removeAttribute("context");
        request.getSession().removeAttribute("add_remove_target");
    }

}
