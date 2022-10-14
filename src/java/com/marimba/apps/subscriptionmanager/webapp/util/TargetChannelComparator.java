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
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.util.ScheduleUtils;

import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
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
 * Use this comparator in order to compare the various properties of a TargetChannelMap This should be used instead of PropsComparator because the comparison
 * between some of the channel's properties (state, schedule) needs to be done different from just a straight string comparison culled from ChannelComparator
 *
 * @author Rahul Ravulur
 * @version 1.4, 01/07/2003
 */
public class TargetChannelComparator
    extends ChannelComparator {
    Locale locale;

    /**
     * Creates a new TargetChannelComparator object.
     *
     * @param val REMIND
     */
    public TargetChannelComparator(String val) {
        super(val);
        this.sortby = TARGET_NAME_KEY;
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
        if (!(elem1 instanceof TargetChannelMap) || !(elem2 instanceof TargetChannelMap)) {
            throw new IllegalArgumentException("TargetChannelComparator.compare");
        }

        TargetChannelMap chelem1 = (TargetChannelMap) elem1;
        TargetChannelMap chelem2 = (TargetChannelMap) elem2;
        int              val = 0;
        boolean          targetsort = false;

        if (TARGET_NAME_KEY.equals(sortby)) {
            val        = (coll.compare(chelem1.getName(), chelem2.getName()));
            targetsort = true;
        } else if (TARGET_DIRECTLYASSIGNED_KEY.equals(sortby)) {
            /*See if the mapping is a selected target or not.  Its packages
             *should be at the top
             */
            if ("true".equals(chelem1.getIsSelectedTarget()) && !"true".equals(chelem2.getIsSelectedTarget())) {
                return -1;
            }

            if (!"true".equals(chelem1.getIsSelectedTarget()) && "true".equals(chelem2.getIsSelectedTarget())) {
                return 1;
            }

            targetsort = true;

            Target t1 = chelem1.getTarget();
            val = t1.compareTo(chelem2.getTarget());
        }

        if (targetsort) {
            if (sortorder == DESCENDING) {
                return -1 * val;
            } else {
                return val;
            }
        }

        return super.compare(chelem1.getChannel(), chelem2.getChannel());
    }
}
