// Copyright 1997-2010, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscriptionmanager.webapp.forms.vProPCAlarmSettingsForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;
import com.marimba.intf.util.IConfig;
import com.marimba.tools.config.ConfigProps;

/**
 * This action is called when visiting the Intel vPro PCAlarm Clock Settings page. It reads the vPro PCAlarm Clock
 * Configuration Settings and populates the form for display.
 *
 * @author Selvaraj Jegatheesan
 *
 **/
public class vProPCAlarmSettingsLoadAction extends AbstractAction {
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws ServletException {

        ISubscription sub = null;
        try {
            sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);
            String setAmtAlarmClk = getTChPropValue(sub.getProperty(PRO_AMT_ALARMCLK_KEYWORD, SET_ALARMCLK_SETTINGS));
            if(null != setAmtAlarmClk) {
                setSessionValue(setAmtAlarmClk, (vProPCAlarmSettingsForm)form, sub);
            } else {
                setDefaultValue((vProPCAlarmSettingsForm)form);
            }
        } catch (SystemException e) {
            throw new GUIException(e);
        }
        // check if user clicked preview
        String action = request.getParameter("action");
        if ("preview".equals(action)) {
            return mapping.findForward("preview");
        }
        return (mapping.findForward("success"));
    }
    private void setSessionValue(String setAmtAlarmClk, vProPCAlarmSettingsForm vproForm, ISubscription sub) throws SystemException {
        String wakeDays = null;
        String wakeTime = null;
        String wakeStartTime = null;
        vproForm.setValue(ENABLE_AMT_ALARM,VALID);
        vproForm.setEnablePCAlarm(VALID);
        vproForm.setVpro_alarm_none(INVALID);
        vproForm.setValue(AMT_ALARM_NONE,INVALID);
        String priority = getPriorityValue(sub.getProperty(PRO_AMT_ALARMCLK_KEYWORD, SET_ALARMCLK_SETTINGS));
        if(null == priority || "".equals(priority)) {
            priority = NOTAVBLE;
        }
        vproForm.setValue(AMT_ALARM_PRIORITY, priority);
        vproForm.setValue(AMT_ALARM_FORCEAPPLY,getTChPropValue(sub.getProperty(PRO_AMT_ALARMCLK_KEYWORD, AMT_ALARMCLK_FORCEAPPLY)));
        if(VALID.equals(setAmtAlarmClk)) {
            wakeStartTime = getTChPropValue(sub.getProperty(PRO_AMT_ALARMCLK_KEYWORD, AMT_ALARMCLK_STARTTIME));
            wakeStartTime = ((vProPCAlarmSettingsForm) vproForm).convertDateTimeMrbaToSys(wakeStartTime);
            vproForm.setValue(AMT_ALARM_STARTTIME, wakeStartTime);
            String wakeInterval =  getTChPropValue(sub.getProperty(PRO_AMT_ALARMCLK_KEYWORD, AMT_ALARMCLK_INTERVALTIME));
            int index = wakeInterval.indexOf(COLON_SEPARATOR);
            if(index != -1) {
                wakeDays = wakeInterval.substring(0, index);
                wakeTime = wakeInterval.substring(index+1);
            } else {
                wakeDays = AMT_ALARM_DAYS_DEFAULT;
                wakeTime = AMT_ALARM_TIME_DEFAULT;
            }
            vproForm.setValue(AMT_ALARM_WAKEINTERVAL_DAYS, wakeDays);
            vproForm.setValue(AMT_ALARM_WAKEINTERVAL_HRMIN, wakeTime);
            vproForm.setValue(AMT_ALARM_SETSETTING,VALID);
            vproForm.setValue(AMT_ALARM_CLRCMD,INVALID);
            vproForm.setValue(APPLY_ALARM_SETTINGS,VALID);

        } else {
            vproForm.setValue(AMT_ALARM_SETSETTING,INVALID);
            vproForm.setValue(APPLY_ALARM_SETTINGS,INVALID);
            vproForm.setValue(AMT_ALARM_CLRCMD,VALID);
        }

    }
    private void setDefaultValue(vProPCAlarmSettingsForm vproForm) throws SystemException  {
        vproForm.setValue(ENABLE_AMT_ALARM,INVALID);
    }
}