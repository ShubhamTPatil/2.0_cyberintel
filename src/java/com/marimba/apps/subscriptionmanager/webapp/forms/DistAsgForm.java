// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.forms;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Form for the distribution assignment page.  The primary and secondary states select boxes are identified as state#&lt;channel object hashcode> and
 * secState#&lt;channel object hashcode>
 *
 * @author Theen-Theen Tan
 * @version 1.15, 10/11/2002
 */

public class DistAsgForm extends AbstractForm implements ISubscriptionConstants, IAppConstants, IWebAppConstants {

    private static ArrayList<String> states = new ArrayList<String>(8);
    //private static ArrayList secStates = new ArrayList(8);
    private static String[]  statesConst = { STATE_SUBSCRIBE_NOINSTALL, STATE_AVAILABLE, STATE_SUBSCRIBE, STATE_SUBSCRIBE_START,
            STATE_SUBSCRIBE_PERSIST, STATE_START_PERSIST, STATE_EXCLUDE, STATE_DELETE, STATE_PRIMARY };

    static {
        states.clear();
        states.addAll(Arrays.asList(statesConst));
    }

    // Variables for the values and display values for primary and secondary
    // states for packages
    // The state labels should not be static as the strings are different
    // for different locales
    private ArrayList<String> statesLabel = new ArrayList<String>(8);
    private HashMap<String, Object> stateMap = new HashMap<String, Object>(DEF_COLL_SIZE);

    /* Used when the inconsistency is to be maintained for the primary state   */
    private HashMap<String, Object> stateIncMap = new HashMap<String, Object>(DEF_COLL_SIZE);
    private HashMap<String, Object> secStateMap = new HashMap<String, Object>(DEF_COLL_SIZE);

    /* Used when the inconsistency is to maintained for the secondary state */
    private HashMap<String, Object> secStateIncMap = new HashMap<String, Object>(DEF_COLL_SIZE);
    private HashMap<String, String> schedIncMap = new HashMap<String, String>(DEF_COLL_SIZE);

    private HashMap<String, Object> exemptBoMap = new HashMap<String, Object>(DEF_COLL_SIZE);
    private HashMap<String, Object> wowDepMap = new HashMap<String, Object>(DEF_COLL_SIZE);
    private HashMap<String, String> changeOrderIncMap = new HashMap<String, String>(DEF_COLL_SIZE);
    private HashMap<String, String> changeOrderMap = new HashMap<String, String>(DEF_COLL_SIZE);
    private HashMap<String, String> changeOrderHdnMap = new HashMap<String, String>(DEF_COLL_SIZE);
    private Map<String, String> chnlUrlMap = new HashMap<String, String>(DEF_COLL_SIZE);
    private boolean deActivateWoW = false;

    private int startingPriority;

    HashMap<String, Object> checkedItems = new HashMap<String, Object>(DEF_COLL_SIZE);

    private String taskid;
    private String changeid;

    /**
     * REMIND
     *
     * @return REMIND
     */
    public ArrayList<String> getStates() {
        return states;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public ArrayList<String> getStatesLabel() {
        return statesLabel;
    }

    public int getStartingPriority() {
        return startingPriority;
    }

    public void setStartingPriority(int startingPriority) {
        this.startingPriority = startingPriority;
    }

    public void setDeActivateWoW(boolean deActivateWoW) {
        this.deActivateWoW = deActivateWoW;
    }

    public boolean getDeActivateWoW() {
        return this.deActivateWoW;
    }

    /* The distribution form is initialized from the DistAsgInitForm. It must ALWAYS be
     * initialized when entering the distribution assignment page.  This is so that we have
     * no residual distribution bean that is affected by a previous edit.
     *
     */
    public void initialize(MessageResources resources, Locale locale, HttpServletRequest request) {
        stateMap.clear();
        secStateMap.clear();
        stateIncMap.clear();
        secStateIncMap.clear();

        /* Iterate through and establish the initial value of the schedules radio button
         */
        schedIncMap.clear();

        statesLabel.clear();

        /* Obtain the distribution bean so that we know how to initialize
         * the fields for schedIncMap.  These is using for filling out values
         * for the radio buttons if there are inconsistent states or schedules
         *
         */
        initSchedAndStateInc(request);

        for (String aConst : statesConst) {
            statesLabel.add(resources.getMessage(locale, "page.global." + aConst + ".uppercase"));
        }

        // Setting the default values to the below variables. This is required for tag library to get the packages.
        props.put((Object) "sortorder", (Object) "true");
        props.put((Object) "sorttype", (Object) "title");
        props.put((Object) "showurl", (Object) "hide");
        props.put((Object) "lastsort", (Object) "title");

        // Below deals with persitifying check boxes across page
        props.put(SESSION_PERSIST_SELECTED, SESSION_DIST_PAGEPKGS_SELECTED);
        props.put(SESSION_PERSIST_PREFIX, SESSION_DIST_PAGEPKGS_PREFIX);
        props.put(SESSION_PERSIST_BEANNAME, SESSION_DIST_PAGEPKGS_BEAN);

        DistributionBean distBean = AbstractAction.getDistributionBean(request);
        if ( distBean.getPreState().size() == 0 ){
            props.put(SESSION_DIST_PAGEPKGS_PREORDER, "false");
        }

        checkedItems.clear();
        setTaskid("");
        setChangeid("");
    }

    void initSchedAndStateInc(HttpServletRequest request) {
        HttpSession      session = request.getSession();
        DistributionBean distbean = AbstractAction.getDistributionBean(request);

        /* Set the schedules initial values
         *
         */
        if (INCONSISTENT.equals(distbean.getInitScheduleInitValue())) {
            setValue(MAINTAININITSCHED, "true");
        }

        if (INCONSISTENT.equals(distbean.getSecScheduleInitValue())) {
            setValue(MAINTAINSECSCHED, "true");
        }

        if (INCONSISTENT.equals(distbean.getUpdateScheduleInitValue())) {
            setValue(MAINTAINUPDATESCHED, "true");
        }

        if (INCONSISTENT.equals(distbean.getVerRepairScheduleInitValue())) {
            setValue(MAINTAINVERREPAIRSCHED, "true");
        }

        /** Set the channels initial value for the radio button */
        ArrayList chlist = distbean.getApplicationChannels();
        Channel   ch = null;
        String str = "true";

        for (Object aChlist : chlist) {
            ch = (Channel) aChlist;

            if (INCONSISTENT.equals(distbean.getChInitStateValue(ch.getUrl()))) {
                setValue("stateInc#" + ch.hashCode(), str);
            }

            if (INCONSISTENT.equals(distbean.getChInitSecStateValue(ch.getUrl()))) {
                setValue("secStateInc#" + ch.hashCode(), str);
            }

            if (INCONSISTENT.equals(distbean.getChInitOrderStateValue(ch.getUrl()))) {
                setValue("changeOrderInc#" + ch.hashCode(), str);
            }

            if (ch.isExemptFromBlackout()) {
                setValue("exemptBo#" + ch.hashCode(), "true");
            }

            if (ch.isWowEnabled()) {
                setValue("wowDep#" + ch.hashCode(), "true");
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
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        return null;
    }

    /**
     * REMIND
     *
     * @param property REMIND
     * @param value REMIND
     */
    public void setValue(String property, Object value) {
        if (property.startsWith("state#")) {
            String hashcode = property.substring(6);
            //stateMap.put(hashcode, ((String[]) value) [0]);
            stateMap.put(hashcode, value);
        } else if (property.startsWith("secState#")) {
            String hashcode = property.substring(9);
            //secStateMap.put(hashcode, ((String[]) value) [0]);
            secStateMap.put(hashcode, value);
        } else if (property.startsWith("stateInc#")) {
            String hashcode = property.substring(9);
            //stateIncMap.put(hashcode, ((String[]) value) [0]);
            stateIncMap.put(hashcode, value);
        } else if (property.startsWith("secStateInc#")) {
            String hashcode = property.substring(12);
            //secStateIncMap.put(hashcode, ((String[]) value) [0]);
            secStateIncMap.put(hashcode, value);
        } else if (property.startsWith("schedInc#")) {
            if (DEBUG) {
                System.out.println("DistAsgForm: sched inc map - property = " + property);
            }

            if (value instanceof String[]) {
                schedIncMap.put(property, ((String[]) value) [0]);
            } else {
                schedIncMap.put(property, ((String) value));
            }
        } else if (property.startsWith("exemptBo#")) {
            String hashcode = property.substring(9);
            exemptBoMap.put(hashcode, value);
        } else if (property.startsWith("wowDep#")) {
            String hashcode = property.substring(7);
            wowDepMap.put(hashcode, value);
        } else if (property.startsWith(SESSION_DIST_PAGEPKGS_PREFIX)) {
            checkedItems.put(property, value);
        }  else if (property.startsWith("changeorder#")) {
            String hashcode = property.substring(12);
            changeOrderMap.put(hashcode, ((String)value) );
        } else if (property.startsWith("changeorderhdn#")) {
            String hashcode = property.substring(15);
            changeOrderHdnMap.put(hashcode, ((String)value) );
        } else if(property.startsWith("changeOrderInc#")){
            String hashcode = property.substring(15);
            changeOrderIncMap.put(hashcode, ((String)value) );
        } else if(property.startsWith("chnl_url_")) {
            chnlUrlMap.put(property, (String) value);
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
        } else if (property.indexOf("secState#") != -1) {
            String hashcode = property.substring(9);

            return secStateMap.get(hashcode);
        } else if (property.indexOf("stateInc#") != -1) {
            String hashcode = property.substring(9);

            return stateIncMap.get(hashcode);
        } else if (property.indexOf("secStateInc#") != -1) {
            String hashcode = property.substring(12);

            return secStateIncMap.get(hashcode);
        } else if (property.indexOf("schedInc#") != -1) {

            return schedIncMap.get(property);
        } else if (property.indexOf("exemptBo#") != -1) {
            String hashcode = property.substring(9);

            return exemptBoMap.get(hashcode);
        } else if (property.indexOf("wowDep#") != -1) {
            String hashcode = property.substring(7);

            return wowDepMap.get(hashcode);
        } else if (property.startsWith(SESSION_DIST_PAGEPKGS_PREFIX)) {
            return checkedItems.get(property);
        }  else if (property.indexOf("changeorder#") != -1) {
            String hashcode = property.substring(12);

            return changeOrderMap.get(hashcode);

        } else if(property.indexOf("changeorderhdn#") != -1) {
            String hashcode = property.substring(15);

            return changeOrderHdnMap.get(hashcode);

        } else if(property.indexOf("changeOrderInc#") != -1){
            String hashcode = property.substring(15);
            return changeOrderIncMap.get(hashcode);
        } else {
            return props.get(property);
        }
    }

    public String getTaskid() {
        return taskid;
    }

    public void setTaskid(String taskID) {
        taskid = taskID;
    }

    public String getChangeid() {
        return changeid;
    }

    public void setChangeid(String changeID) {
        changeid = changeID;
    }

    public void clearCheckedItems() {
        checkedItems.clear();
    }

    public void clearPagingVars(HttpServletRequest request) {
        request.getSession().removeAttribute((String) getValue(SESSION_PERSIST_SELECTED));
        request.getSession().removeAttribute(IWebAppConstants.SESSION_DIST_PAGEPKGS_BEAN);
        clearCheckedItems();
    }

    public void reset(ActionMapping actionMapping, HttpServletRequest request) {
        super.reset(actionMapping, request);
        checkedItems.clear();

        // let's clear exempt from blkout checkbox values for the
        // current page.
        GenericPagingBean pageBean = (GenericPagingBean) request.getSession().getAttribute(SESSION_DIST_PAGEPKGS_BEAN);
        if(pageBean != null) {
            String requestURI = request.getRequestURI();
            // clearing only when the form is submitted and not during
            // the request is forwarded to other action classes.
            // Also if OK button is clicked in the
            // Set Common Schedule page. We check for distAsgSchedCommonSave.do
            // in the request URI.
            if("POST".equals(request.getMethod()) &&
                    requestURI.indexOf("distAsgSchedCommonSave.do") == -1) {
                List results = pageBean.getResults();
                for(int i=pageBean.getStartIndex(); i<pageBean.getEndIndex(); i++ ) {
                    Channel c = (Channel) results.get(i);
                    exemptBoMap.put(String.valueOf(c.hashCode()), "false");
                    wowDepMap.put(String.valueOf(c.hashCode()), "false");
                }
            }
        }
    }

    public void removeChangeOrders() {
        changeOrderMap.clear();
        changeOrderHdnMap.clear();
    }

}
