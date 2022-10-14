// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.util;

import org.apache.struts.util.MessageResources;

import java.text.Collator;

import java.util.Comparator;
import java.util.Locale;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.util.ScheduleUtils;

import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.util.*;

import com.marimba.castanet.schedule.Schedule;
import com.marimba.castanet.schedule.ScheduleInfo;
import com.marimba.castanet.schedule.SchedulePeriod;

import com.marimba.intf.util.IProperty;

import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.InternalException;
import com.marimba.webapps.intf.KnownException;
import com.marimba.webapps.intf.SystemException;

/**
 * Use this comparator in order to compare the various properties of a channel. This should be used instead of PropsComparator because the comparison between
 * some of the channel's properties (state, schedule) needs to be done different from just a straight string comparison
 *
 * @author Angela Saval
 * @version 1.2, 11/13/2001
 */
public class ChannelComparator
    implements Comparator,
                   IWebAppConstants,
                   ISubscriptionConstants,
                   IErrorConstants,
                   IWebAppsConstants {
    String  sortby = CH_TITLE_KEY;
    Locale  locale;
    boolean sortorder = ASCENDING; /* This is either ascending or descending. This
    * indicates if the property is sorted from A to Z
    * or Z to A
    */
    Collator         coll = Collator.getInstance();
    MessageResources resources = null;

    /**
     * Creates a new ChannelComparator object.
     *
     * @param val REMIND
     */
    public ChannelComparator(String val) {
        this.sortby = val;
    }

    /* Determines which property to sort by.  This can be one of the
     * IWebAppConstants.CH_* constants defined for each of the properties of the
     * channel.
     *
     * @param val Defines the property of the channel that is to be used for sorting.
     */
    public void setSortProperty(String val) {
        this.sortby = val;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getSortProperty() {
        return this.sortby;
    }

    /**
     * REMIND
     *
     * @param val REMIND
     */
    public void setSortOrder(boolean val) {
        this.sortorder = val;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public boolean getSortOrder() {
        return this.sortorder;
    }

    /**
     * Since the message displayed on the screen for the state is different from what is saved to ldap, we need the message resources for obtaining the correct
     * string according to the local.
     *
     * @param val resources file for message for the webapp.  This should be passed in from the tag that is calling this comparator.  If null, then ordering
     *        based off of the presentation name cannot be done.
     */
    public void setMessageResources(MessageResources val) {
        this.resources = val;
    }

    /**
     * Obtains the resources that is used for getting the state names for comparison
     *
     * @return the resources file that was gotten from the application scope of the pageContext for a tag.
     */
    public MessageResources getMessageResources() {
        return this.resources;
    }

    /**
     * The locale is need for obtaining the message resources
     *
     * @param locale REMIND
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Retrieves the locale for the messages
     *
     * @return REMIND
     */
    public Locale getLocale() {
        return this.locale;
    }

    /**
     * Since Java 2 collections can contain null values, this comparation treats a null object as less than any instantiated values.
     *
     * @param elem1 REMIND
     * @param elem2 REMIND
     *
     * @return REMIND
     *
     * @throws IllegalArgumentException REMIND
     */
    public int compare(Object elem1,
                       Object elem2) {
        if ((elem1 == null) && (elem2 == null)) {
            return 0;
        }

        if (elem1 == null) {
            return -1;
        }

        if (elem2 == null) {
            return 1;
        }

        /* This comparator is specifically for channels, therefore insure that the objects passed in
         * are channels that implement the gui channels.
         */
        if (!(elem1 instanceof Channel) || !(elem2 instanceof Channel)) {
            throw new IllegalArgumentException("ChannelComparator.compare");

            // 	    throw new KnownException(VALIDATION_INTERNAL_WRONGARG,"ChannelComparator.compare","channel type");
        }

        Channel chelem1 = (Channel) elem1;
        Channel chelem2 = (Channel) elem2;

        int     val = channelAttrCompare(chelem1, chelem2);

        if (sortorder == DESCENDING) {
            return -1 * val;
        } else {
            return val;
        }
    }

    /**
     * REMIND
     *
     * @param chelem1 REMIND
     * @param chelem2 REMIND
     *
     * @return REMIND
     */
    public int channelAttrCompare(Channel chelem1,
                                  Channel chelem2) {
        long lval = 0;
        //Obtain the property from the channel
        if (CH_TITLE_KEY.equals(sortby)) {
            //can do a straight string comparison
            return coll.compare(chelem1.getTitle(), chelem2.getTitle());
        }

        //Do the integer comparison of the channels
        if (CH_INSTALL_PRIORITY_KEY.equals(sortby)) {
            return (chelem1.getOrder() - chelem2.getOrder());
        }

        /*State comparison is trickier because we need to obtain the GUI
         *name used and sort by this.  This can be obtained from the application resources.
         *This was used instead of hard coding the values for internationalization reasons
         */
        if (CH_STATE_KEY.equals(sortby) || CH_SECSTATE_KEY.equals(sortby)) {
            if (DEBUG) {
                System.out.println("ChannelComparator: message key = " + IWebAppsConstants.TEXT_PAGE_PREFIX + "." + IWebAppsConstants.TEXT_GLOBALTYPE + "." + (String) chelem1
                                                                                                                                                                  .get(sortby));
            }

            String message1 = resources.getMessage(locale, IWebAppsConstants.TEXT_PAGE_PREFIX + "." + IWebAppsConstants.TEXT_GLOBALTYPE + "." + (String) chelem1
                                                                                                                                                    .get(sortby));
            String message2 = resources.getMessage(locale, IWebAppsConstants.TEXT_PAGE_PREFIX + "." + IWebAppsConstants.TEXT_GLOBALTYPE + "." + (String) chelem2
                                                                                                                                                    .get(sortby));

            if (DEBUG) {
                System.out.println("ChannelComparator: message1 = " + message1);
                System.out.println("ChannelComparator: message2 = " + message2);
            }

            return coll.compare(message1, message2);
        }

        //Do the integer comparison of the channels
        if (CH_TYPE_KEY.equals(sortby)) {
            return coll.compare(chelem1.getType(), chelem2.getType());
        }

        /* The order should be based off of the date/time of the activation
         * period.
         */
        if (CH_INITSCHED_KEY.equals(sortby) || CH_SECSCHED_KEY.equals(sortby) || CH_UPDATESCHED_KEY.equals(sortby) || CH_VERREPAIRSCHED_KEY.equals(sortby)) {
            if (DEBUG) {
                System.out.println("ChannelComparator: schedule compare, key=" + CH_INITSCHED_KEY);
            }

            Schedule schedule1 = (Schedule) chelem1.get(sortby);
            Schedule schedule2 = (Schedule) chelem2.get(sortby);

            //null indicates ASAP schedules.  These situations need to handled.
            if ((schedule1 == null) && (schedule2 == null)) {
                return 0; //They are both ASAP
            }

            if (schedule1 == null) {
                return 1; //ASAP schedule takes precedence
            }

            if (schedule2 == null) {
                return -1;
            }

            /* Now handle situations when the schedule strings exactly match.
             */
            if (schedule1.toString()
                             .equals(schedule2.toString())) {
                //Exact match
                if (DEBUG) {
                    System.out.println("ChannelComparator: exact match for schedule strings");
                }

                return 0;
            }

            /* Now handle the situation when there is an activation period defined.
             * If the activation period is NOT defined, it will take precedence (return 1)
             * This require obtaining the activation period and ordering
             */
            ScheduleInfo   schinfo1 = (ScheduleInfo) chelem1.get(sortby + "Info");
            ScheduleInfo   schinfo2 = (ScheduleInfo) chelem2.get(sortby + "Info");
            SchedulePeriod schper1 = ScheduleUtils.getActivePeriod(schinfo1);
            SchedulePeriod schper2 = ScheduleUtils.getActivePeriod(schinfo2);

            /*
            if (DEBUG) {
            System.out.println("ChannelComparator: ISubscriptionConstants -info = " + CH_INITSCHED_INFO_KEY);
            System.out.println("ChannelComparator: SORTBY -info = " + sortby + "Info");
            System.out.println("ChannelComparator: schedule1 string = " + schedule1.toString());
            System.out.println("ChannelComparator: schedule1 string = " + schedule2.toString());
            System.out.println("ChannelComparator: schedule info = " + schinfo1);
            System.out.println("ChannelComparator: schedule info2 = " + schinfo2);
            }
            */

            if(schper1 != null && schper2 != null) {
                lval = schper1.getStartSchedulePeriod()
                               .getTime() - schper2.getStartSchedulePeriod()
                                                   .getTime();
            } else if(schper1 == null) {
                lval = -1;
            } else if(schper2 == null) {
                lval = 1;
            }

            if (lval < 0) {
                return -1;
            } else if (lval > 0) {
                return 1;
            } else {
                return 0;
            }
        }

        return 0; //Assume values are equal since there is no way to compare
    }
}
