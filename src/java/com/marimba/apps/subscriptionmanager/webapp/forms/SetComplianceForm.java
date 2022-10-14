package com.marimba.apps.subscriptionmanager.webapp.forms;

import com.marimba.castanet.schedule.*;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import javax.servlet.*;

import java.util.*;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionError;
import com.marimba.webapps.intf.IMapProperty;

import javax.servlet.http.HttpServletRequest;

/**
 * User: Jean Ro
 * Date: Jul 27, 2004
 * Time: 5:24:18 PM
 */

public class SetComplianceForm extends AbstractForm
{

    private String hostName;
    private String calcCompEnabled;
    private String collectCompEnabled;
    private String cacheOption;
    private ScheduleInfo schedInfo = new ScheduleInfo();
    private String readOnlySchedule;
    private String action;
    private String targetname;
    private ArrayList targetlist;
    private String addblankrow = "false";
    private boolean clearData = true;
    private String targetsize = "0";

    public void setTargetsize(String ts) {
        this.targetsize = ts;
    }

    public String getTargetsize() {
        return targetsize;
    }
    public void setClearData(boolean flag) {
        this.clearData = flag;
    }
    
    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getCalcCompEnabled() {
        return calcCompEnabled;
    }

    public void setCalcCompEnabled(String calcCompEnabled) {
        this.calcCompEnabled = calcCompEnabled;
    }

    public String getCollectCompEnabled() {
        return collectCompEnabled;
    }

    public void setCollectCompEnabled(String collectCompEnabled) {
        this.collectCompEnabled = collectCompEnabled;
    }

    public String getCacheOption() {
        return cacheOption;
    }

    public void setCacheOption(String cacheopt) {
        this.cacheOption = cacheopt;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String act) {
        this.action = act;
    }

    public String getTargetname() {
        return targetname;
    }

    public void setTargetname(String tn) {
        this.targetname = tn;
    }

    public ArrayList getTargetlist() {
        return targetlist;
    }

    public void setTargetlist(ArrayList al) {
        this.targetlist = al;
    }

    public String getAddblankrow() {
        return addblankrow;
    }

    public void setAddblankrow(String br) {
        this.addblankrow = br;
    }
    public String getSchedule(String prefix) {
        StringBuffer buf = null;
       try {
        // never schedule
        buf = new StringBuffer();
        if ((prefix + COMPLIANCE_REFRESH_NEVER).equals(getString(prefix + COMPLIANCE_REFRESH_FREQUENCY))) {
            buf.append("NEVER");
            return buf.toString();
        }
        // daily, weekly or monthly periods
        if ((prefix + COMPLIANCE_REFRESH_DAILY).equals(getString(prefix + COMPLIANCE_REFRESH_FREQUENCY))) {
            buf.append("every ");
            buf.append(getString(prefix + DAYS));
            buf.append(" days");
        } else if ((prefix + COMPLIANCE_REFRESH_WEEKLY).equals(getString(prefix + COMPLIANCE_REFRESH_FREQUENCY))) {
            StringBuffer dbuf = null;

            for (int i = 0; i < 7; ++i) {
            String value = getString(prefix + WEEKDAYS[i]);
            if (value != null) {
                if (dbuf == null) {
                dbuf = new StringBuffer();
                } else {
                dbuf.append("+");
                }
                switch (i) {
                  case 0: dbuf.append("sun"); break;
                  case 1: dbuf.append("mon"); break;
                  case 2: dbuf.append("tue"); break;
                  case 3: dbuf.append("wed"); break;
                  case 4: dbuf.append("thu"); break;
                  case 5: dbuf.append("fri"); break;
                  case 6: dbuf.append("sat"); break;
                }
            }
            }

            int interval = 1;
            try {
            interval = Integer.parseInt(getString(prefix + WEEKS));
            } catch(NumberFormatException nex) {
            }
            if (interval != 1) {
            buf.append("every ");
            buf.append(interval);
            buf.append(" weeks ");
            } else {
            buf.append("every week ");
            }

            buf.append("on ");
            buf.append(dbuf);
        } else if ((prefix + COMPLIANCE_REFRESH_MONTHLY).equals(getString(prefix + COMPLIANCE_REFRESH_FREQUENCY))) {
            buf.append("day ");
            buf.append(getString(prefix + MONTHDAY));
            buf.append(" every ");
            buf.append(getString(prefix + MONTHS));
            buf.append(" months");
        }

        // create a filter
        String token = "update";
        if (ScheduleInfo.AT == strToTimeType(prefix, getString(prefix + HRFROMTO))) {
            buf.append(" " + token + " at ");
            buf.append(getString(prefix + HRFROM));
            buf.append(":");
            buf.append(getString(prefix + MINFROM));
            if (getString(prefix + MINFROM).length() > 0) {
            buf.append(getString(prefix + AMPMFROM));
            }
        } else {
            buf.append(" " + token + " every ");

            // note with the new mock up this interval is always in minutes
            buf.append(getString(prefix + FREQUENCY));
            buf.append(" minutes");
            buf.append(" between ");
            buf.append(getString(prefix + HRFROM) + ":" + getString(prefix + MINFROM));
            if (getString(prefix + MINFROM).length() > 0) {
            buf.append(getString(prefix + AMPMFROM));
            }
            buf.append(" and ");
            buf.append(getString(prefix + HRTO) + ":" + getString(prefix + MINTO));
            if (getString(prefix + MINTO).length() > 0) {
            buf.append(getString(prefix + AMPMTO));
            }
        }

       } catch (Exception e) {

       }
        return buf.toString();
    }


    public String getReadOnlySchedule() {
        return readOnlySchedule;
    }

    public void setReadOnlySchedule(String readOnlySchedule) {
        this.readOnlySchedule = readOnlySchedule;
    }

    public String getString(String property) {
        String newProperty = "value("+ property + ")";
	    Object value = getValue(newProperty);
	    return getAsString(value);
    }

    public String getAsString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String[]) {
            String[] valueArray = (String[]) value;
            if (valueArray.length > 0) {
            return valueArray[0];
            }
            return null;
        }
        return value.toString();
    }

    /*
    public void setRetPage(String retPage) {
	    this.retPage = retPage;
    }
    */

    /**
     * Reset all properties necessary.  This is called any time the form is accessed (Even if it already exists in the session)
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping      mapping,
                      HttpServletRequest request) {
        if(clearData) {
            setHostName(null);
            setCalcCompEnabled(CALC_COMP_SPM_DISABLE);
            setCacheOption(ONLY_ALL_TARGET);
            setAction(null);
            setTargetname(null);
            setTargetlist(null);
            props.clear();
            setAddblankrow("false");
        }
    }

   public void setSchedule(String prefix, String sch) {

        if (sch == null || sch.length() == 0 || sch.equals("inconsistent")) {
            schedInfo = new ScheduleInfo();
        } else {
            schedInfo = Schedule.getScheduleInfo(sch);
        }
        setScheduleProperties(prefix);
    }

    public void setScheduleProperties(String prefix) {
	if (schedInfo.getFlag(ScheduleInfo.CALENDAR_PERIOD) == null) {
	    // if CAL PERIOD is null then schedule is NEVER
	    setValue((prefix + COMPLIANCE_REFRESH_FREQUENCY), (prefix + COMPLIANCE_REFRESH_NEVER));
	} else {
	    // set the scan frequency
	    setValue((prefix + COMPLIANCE_REFRESH_FREQUENCY), (prefix + calTypeToStr(schedInfo.getFlag(ScheduleInfo.CALENDAR_PERIOD).intValue())));
	    if (ScheduleInfo.ONCE != schedInfo.getFlag(ScheduleInfo.CALENDAR_PERIOD).intValue() &&
		ScheduleInfo.NEVER != schedInfo.getFlag(ScheduleInfo.CALENDAR_PERIOD).intValue()) {
		if (ScheduleInfo.DAILY == schedInfo.getFlag(ScheduleInfo.CALENDAR_PERIOD).intValue()) {
		    // set days
		    if (schedInfo.getLong(ScheduleInfo.DAY_INTERVAL).longValue() > 0) {
			setValue((prefix + DAYS), schedInfo.getLong(ScheduleInfo.DAY_INTERVAL).toString());
		    }
		} else if (ScheduleInfo.WEEKLY == schedInfo.getFlag(ScheduleInfo.CALENDAR_PERIOD).intValue()) {
		    // set the weeks
		    setValue((prefix + WEEKS), schedInfo.getLong(ScheduleInfo.WEEK_INTERVAL).toString());
		    // set the week days
		    setDaysOfWeek(prefix, schedInfo.getBooleanArray(ScheduleInfo.DAYS_OF_WEEK));
		} else if (ScheduleInfo.MONTHLY == schedInfo.getFlag(ScheduleInfo.CALENDAR_PERIOD).intValue()) {
		    setValue((prefix + MONTHS), schedInfo.getLong(ScheduleInfo.MONTH_INTERVAL).toString());
		    setValue((prefix + MONTHDAY), schedInfo.getLong(ScheduleInfo.DAY_OF_MONTH).toString());
		} else {
		    // log something this is an error
		}
		// lets process all the time filters
		setValue((prefix + HRFROMTO), (prefix + timeTypeToStr(schedInfo.getFlag(ScheduleInfo.TIME_PERIOD).intValue())));

		int time;
		int cidx;
		int midx;
		String timeStr;
		if (ScheduleInfo.AT == schedInfo.getFlag(ScheduleInfo.TIME_PERIOD).intValue()) {
		    time = schedInfo.getLong(ScheduleInfo.AT_TIME).intValue();
		    timeStr = intToTime(time);
		    cidx = timeStr.indexOf(':');
		    midx = timeStr.indexOf('M');
		    setValue((prefix + HRFROM), timeStr.substring(0, cidx));
		    if (midx != -1) {
			setValue((prefix + MINFROM), timeStr.substring(cidx + 1, midx-1));
			setValue((prefix + AMPMFROM), timeStr.substring(midx-1));
		    } else {
			setValue((prefix + MINFROM), timeStr.substring(cidx + 1));
		    }
		} else {
		    // for inventory the interval is always in minutes
		    long intervalInHr = 0;
		    long intervalInMins = 0;
		    if ((schedInfo.getLong(ScheduleInfo.HOUR_INTERVAL)) != null) {
			intervalInHr += ((schedInfo.getLong(ScheduleInfo.HOUR_INTERVAL).longValue()) * 60);
		    } else {
			intervalInMins += (schedInfo.getLong(ScheduleInfo.MINUTE_INTERVAL).longValue());
		    }
		    setValue((prefix + FREQUENCY), (new Long(intervalInHr + intervalInMins)).toString());

		    if (schedInfo.getLong(schedInfo.START_TIME) != null) {
			time = schedInfo.getLong(ScheduleInfo.START_TIME).intValue();
			timeStr = intToTime(time);
			cidx = timeStr.indexOf(':');
			midx = timeStr.indexOf('M');
			setValue((prefix + HRFROM), timeStr.substring(0, cidx));
			if (midx != -1) {
			    setValue((prefix + MINFROM), timeStr.substring(cidx + 1, midx-1));
			    setValue((prefix + AMPMFROM), timeStr.substring(midx-1));
			} else {
			    setValue((prefix + MINFROM), timeStr.substring(cidx + 1));
			}
		    }

		    if (schedInfo.getLong(ScheduleInfo.END_TIME) != null) {
			time = schedInfo.getLong(ScheduleInfo.END_TIME).intValue();
			timeStr = intToTime(time);
			cidx = timeStr.indexOf(':');
			midx = timeStr.indexOf('M');
			setValue((prefix + HRTO), timeStr.substring(0, cidx));
			if (midx != -1) {
			    setValue((prefix + MINTO), timeStr.substring(cidx + 1, midx-1));
			    setValue((prefix + AMPMTO), timeStr.substring(midx-1));
			} else {
			    setValue((prefix + MINTO), timeStr.substring(cidx + 1));
			}
		    }
		}
	    }
	}
    }

    void setDaysOfWeek(String prefix, boolean vals[]) {
	for (int i = 0; i < vals.length; i++ ) {
	    switch (i) {
	      case 0: if (vals[i]) {setValue((prefix + WEEKDAYS[i]), "true");} break;
	      case 1: if (vals[i]) {setValue((prefix + WEEKDAYS[i]), "true");} break;
	      case 2: if (vals[i]) {setValue((prefix + WEEKDAYS[i]), "true");} break;
	      case 3: if (vals[i]) {setValue((prefix + WEEKDAYS[i]), "true");} break;
	      case 4: if (vals[i]) {setValue((prefix + WEEKDAYS[i]), "true");} break;
	      case 5: if (vals[i]) {setValue((prefix + WEEKDAYS[i]), "true");} break;
	      case 6: if (vals[i]) {setValue((prefix + WEEKDAYS[i]), "true");} break;
	    }
	}
    }

    String calTypeToStr(int type) {
	String refFreq = COMPLIANCE_REFRESH_NEVER;
	switch (type) {
	  case 2: refFreq = COMPLIANCE_REFRESH_DAILY; break;
	  case 3: refFreq = COMPLIANCE_REFRESH_WEEKLY; break;
	  case 4: refFreq = COMPLIANCE_REFRESH_MONTHLY; break;
	}
	return refFreq;
    }

    String timeTypeToStr(int type) {
	    return (type == ScheduleInfo.EVERY) ? FROM : AT;
    }

    int strToTimeType(String prefix, String str) {
	    return ((prefix + FROM).equals(str)) ? ScheduleInfo.EVERY : ScheduleInfo.AT;
    }

    /**
     * Convert a minute to a time string. 0 = 12:00 AM, 1439 = 11:59 PM.
     * This is based on tool.util.TimeUtil. The difference is that it does not take the
     * tuner's locale into consideration because RC GUI doesn't support 24-hour time format.
     */
    private String intToTime(int i) {
        SimpleDateFormat format = (SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.SHORT);
        boolean am = true;
        int hour = (i / 60);

        if (hour > 11) {
            am = false;
	    hour -= 12;
        }
        if (hour == 0) {
            hour = 12;
        }
        int min = (i % 60);
        // to string
        StringBuffer buf = new StringBuffer();
        if (hour < 10) {
            buf.append('0');
        }
        buf.append(hour);
        buf.append(':');
        if (min < 10) {
            buf.append('0');
        }
        buf.append(min);
	buf.append(am ? "AM" : "PM");
        return buf.toString();
    }

    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {

        if(this.getAction() != null && "cancel".equalsIgnoreCase(this.getAction())) {
            return null;
        }
        
        ActionErrors errs = super.validate(mapping, request);
        String prefix = "tn_tx_";

        if(props != null) props.clear();
        props.putAll(request.getParameterMap());

        if(this.getCacheOption() != null && this.getAction() != null) {
            if(this.getAction().equals("save") && this.getCacheOption().equals(SPECIFIED_ONLY)) {
                boolean valueExists = false;
                boolean invalidTargetExists = false;
                Map params = request.getParameterMap();
                Set keys = params.keySet();
                Iterator it = keys.iterator();
                ServletContext   sc = servlet.getServletConfig().getServletContext();
                SubscriptionMain smmain = TenantHelper.getTenantSubMain(sc, request);

                while(it.hasNext()) {
                    String key = (String)it.next();
                    if(key.startsWith("target_")) {
                        String value = ((String[])params.get(key))[0];
                        if(value != null && value.trim().length() > 0) {
                            valueExists = true;
                            if(TARGET_ALL.equalsIgnoreCase(value)) continue;
                            StringBuffer bufferedString = new StringBuffer(value);
                            if(!smmain.isValidComplianceTarget(bufferedString)) {
                                errs.add("invalidtarget", new ActionError("invalidtarget"));
                                break;
                            }
                            String dnValue = bufferedString.toString();
                            if(!value.equalsIgnoreCase(dnValue)) {
                                request.setAttribute(value, dnValue);
                            }
                        }
                    }
                }
                if(valueExists == false && !CALC_COMP_SPM_DISABLE.equalsIgnoreCase(getCalcCompEnabled())) errs.add("nulltargetlist", new ActionError("nulltargetlist"));
            }
        }

        if ((prefix + COMPLIANCE_REFRESH_NEVER).equals(this.getString(prefix + COMPLIANCE_REFRESH_FREQUENCY)))
            return errs;

        if ((prefix + COMPLIANCE_REFRESH_DAILY).equals(this.getString(prefix + COMPLIANCE_REFRESH_FREQUENCY))) {
           // daily schedule; check for the day interval
            if (this.getString(prefix + DAYS) == null || this.getString(prefix + DAYS).trim().length() == 0)
                errs.add("nullDayInterval", new ActionError("nullDayInterval"));
        } else if ((prefix + COMPLIANCE_REFRESH_WEEKLY).equals(this.getString(prefix + COMPLIANCE_REFRESH_FREQUENCY))) {
            // weekly schedule; check for the week interval
            if (this.getString(prefix + WEEKS) == null || this.getString(prefix + WEEKS).trim().length() == 0)
                errs.add("nullWeekInterval", new ActionError("nullWeekInterval"));

            // weekly schedule; check for days of week
            StringBuffer dbuf = null;
            for (int i = 0; i < 7; ++i) {
                String value = this.getString(prefix + WEEKDAYS[i]);
                if (value != null) {
                    if (dbuf == null) dbuf = new StringBuffer();
                    else dbuf.append("+");
                    switch (i) {
                      case 0: dbuf.append("sun"); break;
                      case 1: dbuf.append("mon"); break;
                      case 2: dbuf.append("tue"); break;
                      case 3: dbuf.append("wed"); break;
                      case 4: dbuf.append("thu"); break;
                      case 5: dbuf.append("fri"); break;
                      case 6: dbuf.append("sat"); break;
                    }
                }
            }
            if ((dbuf == null) || (dbuf.length() <= 0)) {
                errs.add("errors.config.nullDaysOfWeek", new ActionError("errors.config.nullDaysOfWeek"));
            }
        } else if ((prefix + COMPLIANCE_REFRESH_MONTHLY).equals(this.getString(prefix + COMPLIANCE_REFRESH_FREQUENCY))) {
            // monthly schedule; check for the month interval
            if (this.getString(prefix + MONTHS) == null || this.getString(prefix + MONTHS).trim().length() == 0)
                errs.add("nullMonthInterval", new ActionError("nullMonthInterval"));
        }

        if ((prefix + AT).equals(this.getString(prefix + HRFROMTO))) {
            // update at calender period; check for the update hr/min
            if (this.getString(prefix + HRFROM) == null || this.getString(prefix + HRFROM).trim().length() == 0)
                errs.add("nullTimeHour", new ActionError("nullTimeHour"));
            if (this.getString(prefix + MINFROM) == null || this.getString(prefix + MINFROM).trim().length() == 0)
                errs.add("nullTimeMin", new ActionError("nullTimeMin"));
        } else if ((prefix + FROM).equals(this.getString(prefix + HRFROMTO))) {
            // update every calender period; check for start hr/min and end hr/min
            if (this.getString(prefix + HRFROM) == null || this.getString(prefix + HRFROM).trim().length() == 0)
                errs.add("nullStartHr", new ActionError("nullStartHr"));
            if (this.getString(prefix + MINFROM) == null || this.getString(prefix + MINFROM).trim().length() == 0)
                errs.add("nullStartMin", new ActionError("nullStartMin"));
            if (this.getString(prefix + HRTO) == null || this.getString(prefix + HRTO).trim().length() == 0)
                errs.add("nullEndHr", new ActionError("nullEndHr"));
            if (this.getString(prefix + MINTO) == null || this.getString(prefix + MINTO).trim().length() == 0)
                errs.add("nullEndMin", new ActionError("nullEndMin"));
            if (this.getString(prefix + FREQUENCY) == null || this.getString(prefix + FREQUENCY).trim().length() == 0)
                errs.add("nullMinInterval", new ActionError("nullMinInterval"));
        }

	    return errs;
    }

    //override the set method
    public void setValue(String property, Object value) {
        if (value != null) {
            props.put(property, value);
        } else {
            props.remove(property);
        }
    }
}
