// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.objects.Target;

import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;

/**
 * This action corresponds to the saving of the tuner and channel properties
 *
 * @author Angela Saval
 * @author Theen-Theen Tan
 */
public final class SaveTunerChPropsAction
    extends SaveTempPropsAction {
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
     * @throws GUIException REMIND
     */
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
        throws IOException, 
                   ServletException {
        // call the temp props method in the super class that will
        // clear out the previous properties and apply the properties
        // in the text area currently.
        processTempProps(form, request);

        List        targetList = getSelectedTargets(request.getSession());

        if ((targetList != null) && !targetList.isEmpty()) {
            Target target = (Target) targetList.get(0);

            try {
                // A temporary variable that stores the new values.  The values should be
                // saved into newsub after conflict resolution.
                ISubscription oldsub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);

                // Load the current copy 
                ISubscription newsub = ObjectManager.openSubForWrite(target.getId(), target.getType(), GUIUtils.getUser(request));

                // obtain a copy of the old subscription from the session. This copy's properties
                // will be compared against the one in the ObjectManager to determine which
                // properties have been deleted since the subscription was loaded.
                // DiffProperties is an internal class defined below.
                ISubscription  oldsubcopy = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB_COPY);
                DiffProperties diffs = new DiffProperties(oldsubcopy, oldsub);

                resolveConflict(oldsub, newsub, diffs);

                if (DEBUG) {
                    for (int i = 0; i < conflicts.size(); i++) {
                        System.out.println(" conflicts are:  " + conflicts.get(i));
                    }
                }

                newsub.save();
                GUIUtils.removeFromSession(request, PAGE_TCHPROPS_SUB);

                if (conflicts.size() > 0) {
                    GUIUtils.log(servlet, request, LOG_SET_PROPERTIES_CONFLICTS, newsub.getTargetID(), listToString(conflicts));
                }

                GUIUtils.log(servlet, request, LOG_SET_PROPERTIES, newsub.getTargetID());
            } catch (Exception e) {
                throw new GUIException(TUNERCHPROPS_SAVE_ERROR, e);
            }
        }

        if (DEBUG) {
            System.out.println("SaveTunerChPropsAction: forwarding to success ");
            System.out.println("SaveTunerChPropsAction: forwarding value= " + mapping.findForward("success"));
        }

        return (mapping.findForward("success"));
    }

    /**
     * DOCUMENT ME!
     *
     * @param oldsub The subscription in the session that stores the information being editted
     * @param newsub The subscription that is loaded from storage that we are merging with  Resolve conflicts between the existing and new copy of subscription
     *        - If packages were deleted from policy assignment before saving of packagege properties is called, the package properties for that channel will
     *        not be saved. - If new packages were added to the existing policy, we make sure we don't deleted those packages when saving the channel
     *        properties. - If a property is modified in old subscription, and it is being modified in the new subscription, the value of the property will
     *        follow the old subscription's modification. - If a property is deleted from the new subscription, and it is being modified in the old
     *        subscription, the properties will be re-added into the subscription. - If a property was modified in the new subscription, and removed in the
     *        old subscription, it will be removed from the subscription.
     * @param diffs REMIND
     *
     * @throws SystemException REMIND
     */
    void resolveConflict(ISubscription  oldsub,
                         ISubscription  newsub,
                         DiffProperties diffs)
        throws SystemException {
        conflicts.clear();

        // Go through packages to try to set properties into subscription
        for (Enumeration enumChannels = oldsub.getChannels(); enumChannels.hasMoreElements();) {
            Channel  chn = (Channel) enumChannels.nextElement();
            Channel  newchn = newsub.getChannel(chn.getUrl());
            String[] pairs = chn.getPropertyPairs();

            if (null == newchn) {
                for (int i = 0; i < pairs.length; i += 2) {
                    conflicts.add("Package property " + pairs [i] + "=" + pairs [i + 1] + " not saved.  " + chn.getUrl() + " does not exist.  ");
                }

                continue;
            }

            // Copies the package properties into the subscription to be saved
            for (int i = 0; i < pairs.length; i += 2) {
                String oldvalue = newchn.getProperty(pairs [i]);

                if (DEBUG) {
                    System.out.println("SaveTunerChPropsAction:: Setting channel property " + pairs [i]);
                }

                newchn.setProperty(pairs [i], pairs [i + 1]);

                if ((oldvalue != null) && !pairs [i + 1].equals(oldvalue)) {
                    conflicts.add("Set " + pairs [i] + " from " + oldvalue + " to " + newchn.getProperty(pairs [i]));
                }
            }
        }

        // Go through the dummy channel set properties into subscription	
        for (Enumeration enumChannels = oldsub.getDummyChannels(); enumChannels.hasMoreElements();) {
            Channel chn = (Channel) enumChannels.nextElement();
            Channel newchn = newsub.getChannel(chn.getUrl());

            if (newchn != null) {
                String[] pairs = chn.getPropertyPairs();

                // There is a real package defined in the subscription already saved.
                // Set the properties from the dummy channel into the real channel.
                // Copies the package properties into the subscription to be saved
                for (int i = 0; i < pairs.length; i += 2) {
                    String oldvalue = newchn.getProperty(pairs [i]);

                    if (DEBUG) {
                        System.out.println("SaveTunerChPropsAction:: Setting dummy channel property to actual channel" + pairs [i]);
                    }

                    newchn.setProperty(pairs [i], pairs [i + 1]);

                    if ((oldvalue != null) && !pairs [i + 1].equals(oldvalue)) {
                        conflicts.add("Set " + pairs [i] + " from " + oldvalue + " to " + newchn.getProperty(pairs [i]));
                    }
                }

                // REMIND t3 : need to remove DummyChannel from here
            } else {
                // Set the current dummy channel into the subscription to be saved
                newsub.setDummyChannel(chn);
            }
        }

        copyTunerProperties(oldsub, newsub, PROP_TUNER_KEYWORD, diffs);
        copyTunerProperties(oldsub, newsub, PROP_SERVICE_KEYWORD, diffs);
        copyTunerProperties(oldsub, newsub, PROP_CHANNEL_KEYWORD, diffs);
        copyTunerProperties(oldsub, newsub, PROP_ALL_CHANNELS_KEYWORD, diffs);
        copyTunerProperties(oldsub, newsub, PROP_DEVICES_KEYWORD, diffs);
        copyTunerProperties(oldsub, newsub, PROP_POWER_KEYWORD, diffs);
        copyTunerProperties(oldsub, newsub, PROP_SECURITY_KEYWORD, diffs);
        copyTunerProperties(oldsub, newsub, PROP_SCAP_SECURITY_KEYWORD, diffs);
        copyTunerProperties(oldsub, newsub, PROP_USGCB_SECURITY_KEYWORD, diffs);
        copyTunerProperties(oldsub, newsub, PROP_CUSTOM_SECURITY_KEYWORD, diffs);
        copyTunerProperties(oldsub, newsub, PROP_AMT_KEYWORD, diffs);
        copyTunerProperties(oldsub, newsub, PRO_AMT_ALARMCLK_KEYWORD, diffs);
        copyTunerProperties(oldsub, newsub, PROP_OSM_KEYWORD, diffs);
        copyTunerProperties(oldsub, newsub, PROP_PBACKUP_KEYWORD, diffs);
        removeDeletedProperties(newsub, diffs);
    }

    void copyTunerProperties(ISubscription  oldsub,
                             ISubscription  newsub,
                             String         type,
                             DiffProperties diffs)
        throws SystemException {
        String[] pairs = oldsub.getPropertyPairs(type);

        for (int i = 0; i < pairs.length; i += 2) {
            String oldvalue = newsub.getProperty(type, pairs [i]);

            // only save out the properties that have changed. This allows us to manage
            // conflicts. The property in newsub, could have changed while this edit is in
            // progress and hence should be treated as newer than an unchanged property in oldsub.
            if (diffs.hasChanged(pairs [i], type)) {
                newsub.setProperty(type, pairs [i], pairs [i + 1]);

                if ((oldvalue != null) && !pairs [i + 1].equals(oldvalue)) {
                    conflicts.add("Set " + pairs [i] + " from " + oldvalue + " to " + newsub.getProperty(type, pairs [i]));
                }
            }
        }
    }

    void removeDeletedProperties(ISubscription  newsub,
                                 DiffProperties diffs)
        throws SystemException {
        // parse the deleted vector
        Vector deletedProps = diffs.getDeletedProps();

        for (int i = 0; i < deletedProps.size(); i++) {
            String[] elem = (String[]) deletedProps.elementAt(i);
            String   type = elem [0];
            String   key = elem [1];

            // if the property has been deleted by the user, explicitly set it to null
            // so that it it removed from the subscription.
            if (type.equals(PROP_TUNER_KEYWORD) || type.equals(PROP_SERVICE_KEYWORD) || type.equals(PROP_CHANNEL_KEYWORD)
                    || type.equals(PROP_ALL_CHANNELS_KEYWORD) || type.equals(PROP_POWER_KEYWORD) || type.equals(PROP_SECURITY_KEYWORD)
                    || type.equals(PROP_AMT_KEYWORD) || type.equals(PRO_AMT_ALARMCLK_KEYWORD) || type.equals(PROP_DEVICES_KEYWORD)
                    || type.equals(PROP_OSM_KEYWORD) || type.equals(PROP_PBACKUP_KEYWORD) || type.equals(PROP_SCAP_SECURITY_KEYWORD)
                    || type.equals(PROP_USGCB_SECURITY_KEYWORD) || type.equals(PROP_CUSTOM_SECURITY_KEYWORD)) {
                if (DEBUG) {
                    System.out.println("SaveTunerChPropsAction:: property  " + key + " has been deleted for type " + type);
                }

                newsub.setProperty(type, key, null);
            } else {
                String  url = type;
                Channel chn = newsub.getChannel(url);

                if (DEBUG) {
                    System.out.println("SaveTunerChPropsAction:: removing channel property  " + key);
                }

                if (chn == null) {
                    chn = newsub.getDummyChannel(url);

                    if (chn != null) {
                        chn.setProperty(key, null);

                        if (chn.getPropertyPairs() == null) {
                            // remove the dummy channel if there is no longer
                            // any properties associated with it
                            newsub.removeDummyChannel(chn.getUrl());
                        }
                    }

                    // do nothing, since channel is removed anyway.
                } else {
                    chn.setProperty(key, null);
                }
            }
        }
    }

    /**
     * Internal class that is responsible for determining the difference between two subscription properties. It is type sensitive. It provides an easy
     * interface to query for changed, deleted and created properties.
     */
    class DiffProperties {
        ISubscription oldsub;
        ISubscription newsub;
        Vector        deleted;

        /**
         * Creates a new DiffProperties object.
         *
         * @param oldsub REMIND
         * @param newsub REMIND
         *
         * @throws SystemException REMIND
         */
        public DiffProperties(ISubscription oldsub,
                              ISubscription newsub)
            throws SystemException {
            this.oldsub = oldsub;
            this.newsub = newsub;
            deleted     = findDeletes();
        }

        private Vector findDeletes()
            throws SystemException {
            // iterate through the old properties
            String   line = null;
            String   key = null;
            String   value = null;
            String   type = null;
            Vector   deleted = new Vector();
            String[] propertyTypes = { PROP_TUNER_KEYWORD, PROP_SERVICE_KEYWORD, PROP_CHANNEL_KEYWORD, PROP_DEVICES_KEYWORD,
                    PROP_ALL_CHANNELS_KEYWORD, PROP_POWER_KEYWORD, PROP_SECURITY_KEYWORD, PROP_AMT_KEYWORD, PRO_AMT_ALARMCLK_KEYWORD,
                    PROP_OSM_KEYWORD, PROP_PBACKUP_KEYWORD, PROP_SCAP_SECURITY_KEYWORD, PROP_USGCB_SECURITY_KEYWORD, PROP_CUSTOM_SECURITY_KEYWORD };

            for (int k = 0; k < propertyTypes.length; k++) {
                type  = propertyTypes [k];
                value = null;

                Enumeration properties = oldsub.getPropertyKeys(type);

                while (properties.hasMoreElements()) {
                    key = (String) properties.nextElement();

                    if (newsub.getProperty(type, key) == null) {
                        // property does not exist in the new subscription
                        // so add it to the deleted vector.
                        if (DEBUG) {
                            System.out.println("SaveTunerChPropsAction:: property does not exist " + type);
                            System.out.println("SaveTunerChPropsAction:: for key                 " + key);
                        }

                        String[] elem = { type, key };
                        deleted.add(elem);
                    }
                }
            }

            for (Enumeration enumChannels = oldsub.getChannels(); enumChannels.hasMoreElements();) {
                Channel  chn = (Channel) enumChannels.nextElement();
                Channel  newchn = newsub.getChannel(chn.getUrl());
                String[] pairs = chn.getPropertyPairs();

                if (null == newchn) {
                    // all the channel properties may be removed.
                    if (DEBUG) {
                        System.out.println(" SaveTunerChPropsAction:: All channel properties are removed ");
                    }

                    for (int i = 0; i < pairs.length; i += 2) {
                        String[] elems = { chn.getUrl(), pairs [i] };
                        deleted.add(elems);
                    }

                    continue;
                } else {
                    // some channel properties may be removed
                    if (DEBUG) {
                        System.out.println("SaveTunerChPropsAction:: Some channel properties are removed for " + newchn.getUrl());
                    }

                    for (int i = 0; i < pairs.length; i += 2) {
                        if (DEBUG) {
                            System.out.print(" SaveTunerChPropsAction:: Checking if property exists " + pairs [i]);
                            System.out.println(" value is " + (newchn.getProperty(pairs [i])));
                        }

                        if (newchn.getProperty(pairs [i]) == null) {
                            if (DEBUG) {
                                System.out.println("SaveTunerChPropsAction:: property is going to be removed " + pairs [i]);
                            }

                            String[] elems = { chn.getUrl(), pairs [i] };
                            deleted.add(elems);
                        }
                    }

                    continue;
                }
            }

            return deleted;
        }

        // returns a boolean indicating if a property has been changed
        // from oldsub to newsub.
        public boolean hasChanged(String key,
                                  String type)
            throws SystemException {
            String val = oldsub.getProperty(type, key);

            if (val == null) {
                return true;
            }

            // it hasn't changed if and only val equals the value in the newsub.
            if (val.equals(newsub.getProperty(type, key))) {
                return false;
            }

            return true;
        }

        /**
         * REMIND
         *
         * @return REMIND
         */
        public Vector getDeletedProps() {
            return deleted;
        }
    }
}
