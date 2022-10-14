package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscriptionmanager.webapp.forms.vProPCAlarmSettingsForm;
import com.marimba.apps.subscriptionmanager.webapp.forms.AbstractForm;

import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;
import com.marimba.intf.util.IConfig;
import com.marimba.tools.config.ConfigProps;

import java.util.Vector;

/**
 * This action is used to store the Intel vPro PCAlarm Clock settings to Distribution bean
 *
 * @author Selvaraj Jegatheesan
 */

public class vProPCAlarmSettingSaveAction extends AbstractAction{
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws ServletException {

        if (DEBUG) {
            System.out.println("vProPCAlarmSettingSaveAction called");
        }

        // save the changes to the DistributionBean
        saveState(request, (vProPCAlarmSettingsForm) form);

        // and forward to the next action
        String forward = (String)((AbstractForm) form).getValue("forward");
        return (new ActionForward(forward, true));
    }

    /**
     * Save the changes in the form to the DistributionBean.
     */
    private void saveState(HttpServletRequest request, vProPCAlarmSettingsForm form) throws GUIException {

        // Intel vPro PCAlarm Clock setting constants

        String enablePCAlarm = null;
        String vProPCAlarmClkPriority = null;
        String applyAlarmSettings = null;
        String alarmStartDate = null;
        String wakeInterval = null;
        String wakeDays = null;
        String wakeTime = null;
        String forceApply = null;

        Vector amtAlarmProps = new Vector();

        try {
            ISubscription sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);
            DistributionBean distBean = getDistributionBean(request);

            enablePCAlarm = (String) ((vProPCAlarmSettingsForm) form).getValue(ENABLE_AMT_ALARM);
            if(VALID.equals(enablePCAlarm)) {
                vProPCAlarmClkPriority = ((String) ((vProPCAlarmSettingsForm) form).getValue(AMT_ALARM_PRIORITY)).trim();
                if(NOTAVBLE.equals(vProPCAlarmClkPriority) || "".equals(vProPCAlarmClkPriority)) {
                    vProPCAlarmClkPriority = null;
                }

                applyAlarmSettings = (String) ((vProPCAlarmSettingsForm) form).getValue(APPLY_ALARM_SETTINGS);
                if(INVALID.equals(applyAlarmSettings)) {
                    setValue(sub, SET_ALARMCLK_SETTINGS, INVALID, amtAlarmProps, vProPCAlarmClkPriority);
                    setValue(sub, AMT_ALARMCLK_STARTTIME, null, amtAlarmProps, vProPCAlarmClkPriority);
                    setValue(sub, AMT_ALARMCLK_INTERVALTIME, null, amtAlarmProps, vProPCAlarmClkPriority);
                } else {
                    setValue(sub, SET_ALARMCLK_SETTINGS, VALID, amtAlarmProps, vProPCAlarmClkPriority);

                    alarmStartDate = ((String) ((vProPCAlarmSettingsForm) form).getValue(AMT_ALARM_STARTTIME)).trim();
                    alarmStartDate = ((vProPCAlarmSettingsForm) form).convertDateTimeSysToMrba(alarmStartDate);
                    setValue(sub, AMT_ALARMCLK_STARTTIME, alarmStartDate, amtAlarmProps, vProPCAlarmClkPriority);

                    wakeDays = ((String) ((vProPCAlarmSettingsForm) form).getValue(AMT_ALARM_WAKEINTERVAL_DAYS)).trim();
                    if(null == wakeDays || "".equals(wakeDays)) {
                        wakeDays = AMT_ALARM_DAYS_DEFAULT;
                    }

                    wakeTime = ((String) ((vProPCAlarmSettingsForm) form).getValue(AMT_ALARM_WAKEINTERVAL_HRMIN)).trim();
                    if(null == wakeTime || "".equals(wakeTime)) {
                        wakeTime = AMT_ALARM_TIME_DEFAULT;
                    }

                    wakeInterval = wakeDays + COLON_SEPARATOR + wakeTime;
                    setValue(sub, AMT_ALARMCLK_INTERVALTIME, wakeInterval, amtAlarmProps, vProPCAlarmClkPriority);
                }

                forceApply = (String) ((vProPCAlarmSettingsForm) form).getValue(AMT_ALARM_FORCEAPPLY);
                if("on".equals(forceApply)) {
                    setValue(sub, AMT_ALARMCLK_FORCEAPPLY, VALID, amtAlarmProps, vProPCAlarmClkPriority);
                } else {
                    setValue(sub, AMT_ALARMCLK_FORCEAPPLY, INVALID, amtAlarmProps, vProPCAlarmClkPriority);
                }
            } else {
                setValue(sub, SET_ALARMCLK_SETTINGS, null, amtAlarmProps, vProPCAlarmClkPriority);
                setValue(sub, AMT_ALARMCLK_STARTTIME, null, amtAlarmProps, vProPCAlarmClkPriority);
                setValue(sub, AMT_ALARMCLK_INTERVALTIME, null, amtAlarmProps, vProPCAlarmClkPriority);
                setValue(sub, AMT_ALARMCLK_FORCEAPPLY, null, amtAlarmProps, vProPCAlarmClkPriority);
            }
            distBean.setAmtAlarmClkProps(amtAlarmProps);
        } catch (SystemException e) {
            throw new GUIException(e);
        }

    }
    private void setValue(ISubscription sub, String key,String value,Vector amtAlarmProps,String priority) throws SystemException{
        if (value != null) {
            if(null != priority && !("".equals(priority)) && !(NOTAPP.equals(priority)))  {
                value = value + PROP_DELIM + Integer.parseInt(priority);
            }
        }
        sub.setProperty(PRO_AMT_ALARMCLK_KEYWORD, key, value);
        amtAlarmProps.add(PRO_AMT_ALARMCLK_KEYWORD + PROP_DELIM + key + "=" + value);
    }
}

