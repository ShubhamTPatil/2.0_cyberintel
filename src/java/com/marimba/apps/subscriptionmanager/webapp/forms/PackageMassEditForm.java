// Copyright 2009, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.forms;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;
import org.apache.struts.Globals;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Set;

import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscription.common.objects.Channel;

/**
 * This form is merely used for storing the state of the mass edit page.
 */

public class PackageMassEditForm extends AbstractForm {
    /* Constant holding available channel states */
    private static String[]  statesConst = {
                        STATE_SUBSCRIBE_NOINSTALL,
					    STATE_AVAILABLE,
					    STATE_SUBSCRIBE,
					    STATE_SUBSCRIBE_START,
					    STATE_SUBSCRIBE_PERSIST,
					    STATE_START_PERSIST,
					    STATE_EXCLUDE,
					    STATE_DELETE,
					    STATE_PRIMARY};
    /* Used to store channel state local specific display strings  */
    private ArrayList statesLabel = null;

    /* Used to store channel state value strings  */
    private ArrayList states = null;

    /* Used to store channel primary state value  */
    private HashMap   stateMap = null;

    /* Used when the inconsistency is to be maintained for the primary state   */
    private HashMap stateIncMap = null;

    /* Used to store channel secondary state value  */
    private HashMap secStateMap = null;

    /* Used when the inconsistency is to maintained for the secondary state */
    private HashMap secStateIncMap = null;

    /* Used to store channel deploy option, Excempt from Block period  */
    private HashMap exemptBoMap = null;

    /* Used to store channel deploy option, Wake On WAN  */
    private HashMap wowDepMap = null;

    /* Used to store checkbox values to find selected packages   */
    private HashMap checkedItems;

    private String action;

    public PackageMassEditForm() {
        //Initialize state constants
        states = new ArrayList(statesConst.length);
        for (int i = 0; i < statesConst.length; i++) {
            this.states.add(statesConst[i]);
        }

        props.put("state_all#primary","");
        props.put("state_all#secondary","");

        stateMap = new HashMap(1);
        stateIncMap = new HashMap(1);
        secStateMap = new HashMap(1);
        secStateIncMap = new HashMap(1);
        exemptBoMap = new HashMap(1);
        wowDepMap = new HashMap(1);
        checkedItems = new HashMap(1);

    }

    public void reset(ActionMapping actionMapping, HttpServletRequest httpServletRequest) {
        super.reset(actionMapping, httpServletRequest);

        stateMap = new HashMap(1);
        stateIncMap = new HashMap(1);
        secStateMap = new HashMap(1);
        secStateIncMap = new HashMap(1);
        exemptBoMap = new HashMap(1);
        wowDepMap = new HashMap(1);
     
        initStateLabels(httpServletRequest);
    }

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

    public void initStateLabels(HttpServletRequest request) {
        //Initialize state labels, different strings displayed for different locale
        if( null == statesLabel) {
            statesLabel = new ArrayList(statesConst.length);
        }
        else {
            statesLabel.clear();
            statesLabel.ensureCapacity(statesConst.length);
        }
        ServletContext context = request.getSession().getServletContext();
        MessageResources resources = (MessageResources) context.getAttribute(Globals.MESSAGES_KEY);
        for (int i = 0; i < statesConst.length; i++) {
            this.statesLabel.add(resources.getMessage(request.getLocale(),
                    "page.global." + statesConst [i] + ".uppercase"));
        }
    }

    public void initStateInc(HttpServletRequest request) {
        DistributionBean distbean = AbstractAction.getDistributionBean(request);
        if(distbean != null) {
            /** Set the channels initial value for the radio button */
            ArrayList chlist = distbean.getApplicationChannels();
            Channel   ch = null;
            String str = "true";


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
        else {
             System.out.println("MAJOR: Distribution bean not available in Session");
        }
    }

    /**
         * REMIND
         *
         * @param property REMIND
         * @param value REMIND
         */
        public void setValue(String property,
                        Object value) {
//            System.out.println("PackageMassEditForm - setValue(): key="+property+" value="+value);
            if (property.startsWith("state#")) {
                String hashcode = property.substring(6);
                stateMap.put(hashcode, value);
            } else if (property.startsWith("secState#")) {
                String hashcode = property.substring(9);
                secStateMap.put(hashcode, value);
            } else if (property.startsWith("stateInc#")) {
                String hashcode = property.substring(9);
                stateIncMap.put(hashcode, value);
            } else if (property.startsWith("secStateInc#")) {
                String hashcode = property.substring(12);
                secStateIncMap.put(hashcode, value);
        } else if (property.startsWith("exemptBo#")) {
                String hashcode = property.substring(9);
                exemptBoMap.put(hashcode, value);
        } else if (property.startsWith("wowDep#")) {
                String hashcode = property.substring(7);
                wowDepMap.put(hashcode, value);
        } else {
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
//            System.out.println("getValue():"+property);

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
            } else if (property.indexOf("exemptBo#") != -1) {
                String hashcode = property.substring(9);

                return exemptBoMap.get(hashcode);
            } else if (property.indexOf("wowDep#") != -1) {
                String hashcode = property.substring(7);

                return wowDepMap.get(hashcode);
//            } if (property.startsWith(SESSION_DIST_PAGEPKGS_PREFIX)) {
//                return checkedItems.get(property);
            } else {
                return props.get(property);
            }
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;            
        }
}
