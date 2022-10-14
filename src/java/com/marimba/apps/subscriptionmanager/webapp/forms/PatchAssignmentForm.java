package com.marimba.apps.subscriptionmanager.webapp.forms;



import com.marimba.apps.subscription.common.ISubscriptionConstants;

import com.marimba.apps.subscription.common.objects.Channel;

import com.marimba.apps.subscriptionmanager.intf.IAppConstants;

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;

import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;

import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean;

import com.marimba.webapps.intf.IMapProperty;

import org.apache.struts.action.ActionErrors;

import org.apache.struts.action.ActionMapping;

import org.apache.struts.util.MessageResources;



import javax.servlet.http.HttpServletRequest;

import java.util.*;



/**

 * Created by IntelliJ IDEA.

 * User: dvamathevan

 * Date: Dec 29, 2003

 * Time: 10:50:02 PM

 * To change this template use Options | File Templates.

 */

public class PatchAssignmentForm

        extends AbstractForm

        implements ISubscriptionConstants,

        IAppConstants,

        IWebAppConstants,

        IMapProperty {





    private static String[] statesConst = {STATE_SUBSCRIBE,

                                           STATE_EXCLUDE};

     private static ArrayList states = new ArrayList(statesConst.length);



    static {

        states.clear();

        for (int i = 0; i < statesConst.length; i++) {

            states.add(statesConst[i]);

        }

    }



    // Variables for the values and display values for primary and secondary

    // states for packages

    // The state labels should not be static as the strings are different

    // for different locales

    private ArrayList statesLabel = new ArrayList(8);

    private HashMap stateMap = new HashMap(DEF_COLL_SIZE);



    /* Used when the inconsistency is to be maintained for the primary state   */

    private HashMap stateIncMap = new HashMap(DEF_COLL_SIZE);



    /* Used when the inconsistency is to maintained for the secondary state */



    private HashMap schedIncMap = new HashMap(DEF_COLL_SIZE);



    private HashMap exemptBoMap = new HashMap(DEF_COLL_SIZE);



    private HashMap wowDepMap = new HashMap(DEF_COLL_SIZE);



    private int startingPriority;



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



    public int getStartingPriority() {

        return startingPriority;

    }



    public void setStartingPriority(int startingPriority) {

        this.startingPriority = startingPriority;

    }



    /* The distribution form is initialized from the DistAsgInitForm. It must ALWAYS be

     * initialized when entering the distribution assignment page.  This is so that we have

     * no residual distribution bean that is affected by a previous edit.

     *

     */

    public void initialize(MessageResources resources,

                           Locale locale,

                           HttpServletRequest request) {

        stateMap.clear();

        stateIncMap.clear();

        props.clear();



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



        for (int i = 0; i < statesConst.length; i++) {

            statesLabel.add(resources.getMessage(locale, "page.global." + statesConst[i] + ".patch.uppercase"));

        }





        // Below deals with persitifying check boxes across page

        props.put(SESSION_PERSIST_SELECTED, SESSION_DIST_PAGEPKGS_SELECTED);

        props.put(SESSION_PERSIST_PREFIX, SESSION_DIST_PAGEPKGS_PREFIX);

        props.put(SESSION_PERSIST_BEANNAME, SESSION_DIST_PAGEPKGS_BEAN);

        clearCheckedItems();

    }



    void initSchedAndStateInc(HttpServletRequest request) {



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

        ArrayList chlist = distbean.getPatchChannels();

        Channel ch = null;

        String[] str = new String[1];

        str[0] = "true";



        for (Iterator ite = chlist.iterator(); ite.hasNext();) {

            ch = (Channel) ite.next();



            if (INCONSISTENT.equals(distbean.getChInitStateValue(ch.getUrl()))) {

                setValue("stateInc#" + ch.hashCode(), str);

            }



            if (INCONSISTENT.equals(distbean.getChInitSecStateValue(ch.getUrl()))) {

                setValue("secStateInc#" + ch.hashCode(), str);

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

    public ActionErrors validate(ActionMapping mapping,

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
            stateMap.put(hashcode, (String) value);
        } else if (property.startsWith("stateInc#")) {

            String hashcode = property.substring(9);
            if (value instanceof String[]) {
            	stateIncMap.put(hashcode, ((String[]) value)[0]);
            } else {
            	stateIncMap.put(hashcode, ((String) value));
            }
        } else if (property.startsWith("schedInc#")) {

            if (DEBUG) {

                System.out.println("DistAsgForm: sched inc map - property = " + property);

            }



            if (value instanceof String[]) {

                schedIncMap.put(property, ((String[]) value)[0]);

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



            // In the case that we disabled the secondary schedule

            // secState# will not be sent to set method when the

            // form is submitted. Therefore we clear it out

            // now if it previously set and the primary state is not

            // available or stage.

            String initState = (String) stateMap.get(hashcode);





        } else if (property.indexOf("stateInc#") != -1) {

            String hashcode = property.substring(9);



            return stateIncMap.get(hashcode);



        } else if (property.indexOf("schedInc#") != -1) {

            return schedIncMap.get(property);

        } else if (property.indexOf("exemptBo#") != -1) {

            String hashcode = property.substring(9);

            return exemptBoMap.get(hashcode);

        } else if (property.indexOf("wowDep#") != -1) {

            String hashcode = property.substring(7);

            return wowDepMap.get(hashcode);

        }

        if (property.startsWith(SESSION_DIST_PAGEPKGS_PREFIX)) {

            return checkedItems.get(property);

        } else {

            return super.getProperty(property);

        }

    }



   public void reset(ActionMapping actionMapping, HttpServletRequest request) {
        clearCheckedItems();
        // let's clear exempt from blkout checkbox values for the
        // current page.
        GenericPagingBean pageBean = 
            (GenericPagingBean) request.getSession()
                                       .getAttribute(SESSION_DIST_PAGEPKGS_BEAN);
        if(pageBean != null) {
            String requestURL = request.getRequestURL().toString();
            // clearing only when the form is submitted and not during
            // the request is forwarded to other action classes 
            if("POST".equals(request.getMethod()) 
                    && requestURL.indexOf("patchSave.do?page=reboot") == -1 && requestURL.indexOf("patchMain.do")== -1) {
                List results = pageBean.getResults();
                for(int i=pageBean.getStartIndex(); i<pageBean.getEndIndex(); i++ ) {
                    Channel c = (Channel) results.get(i);
                    exemptBoMap.put(String.valueOf(c.hashCode()), "false");
                    wowDepMap.put(String.valueOf(c.hashCode()), "false");
                }
           }
       }
    }



    public void clearCheckedItems() {

       checkedItems.clear();

       props.remove("displayAlert");

       props.remove("allowReboot");

    }





    public void clearPagingVars(HttpServletRequest request) {

        request.getSession().

                removeAttribute((String) getValue(SESSION_PERSIST_SELECTED));

        request.getSession()

                .removeAttribute(IWebAppConstants.SESSION_DIST_PAGEPKGS_BEAN);

        clearCheckedItems();

    }



}

