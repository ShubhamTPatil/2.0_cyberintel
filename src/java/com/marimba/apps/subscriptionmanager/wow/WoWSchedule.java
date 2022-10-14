// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.wow;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import com.marimba.castanet.schedule.Schedule;
import com.marimba.castanet.schedule.SchedulePeriod;
import com.marimba.apps.subscription.common.util.ScheduleUtils;

/**
 * Created by IntelliJ IDEA.
 * 
 */

public class WoWSchedule implements Comparable {
	static long tzOffset;
    static Calendar cal;
    static int SECOND = 1000;
    static {
    	cal = new GregorianCalendar(TimeZone.getDefault());
    	offset(System.currentTimeMillis());
    }
    public Schedule wowSchedule = null;

    public WoWSchedule(Schedule wowSchedule) {
        this.wowSchedule = wowSchedule;
    }

    public Schedule geWowSchedule() {
        return this.wowSchedule;
    }

    public int compareTo(Object obj) {

        if (obj == null || (!(obj instanceof WoWSchedule))) {
            return -1;
        } else {
            Schedule sch1 = this.geWowSchedule();
            Schedule sch2 = ((WoWSchedule)obj).geWowSchedule();
            
            long now = System.currentTimeMillis();
            long sch1Next = outMS(sch1.nextTime(-1, inMS(now + (61 * SECOND))));
            long sch2Next = outMS(sch2.nextTime(-1, inMS(now + (61 * SECOND))));
            
            if(sch2Next < sch1Next) {
                return 1;
            }
            else {
                return -1;
            }
        }
    }
    /**
     * Recalculate the timezone offset for the specified date in milliseconds
     * UTC.
     *
     * @param tm DOCUMENT ME
     */
    protected static void offset(long tm) {
        synchronized (cal) {
            cal.setTime(new Date(tm));
            tzOffset = cal.get(Calendar.ZONE_OFFSET) +
                       cal.get(Calendar.DST_OFFSET);
        }
    }
    /**
     * Convert from UTC milliseconds to local milliseconds. e.g. 16:00 GMT +
     * (-8) = 8:00 PST
     *
     * @param tm DOCUMENT ME
     *
     * @return DOCUMENT ME
     */
    static long inMS(long tm) {
        if (tm <= 0) {
            return tm;
        }

        return tm + tzOffset;
    }
    /**
     * Convert from local milliseconds to UTC milliseconds. e.g. 8:00 PST -
     * (-8) = 16:00 GMT
     *
     * @param tm DOCUMENT ME
     *
     * @return DOCUMENT ME
     */
    static long outMS(long tm) {
        if (tm <= 0) {
            return tm;
        }

        return tm - tzOffset;
    }

}
