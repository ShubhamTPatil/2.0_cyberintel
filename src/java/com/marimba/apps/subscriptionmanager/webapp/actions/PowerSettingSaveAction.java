// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscriptionmanager.webapp.forms.AbstractForm;
import com.marimba.apps.subscriptionmanager.webapp.forms.PowerSettingForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.intf.GUIConstants;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.intf.GUIException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.marimba.tools.config.*;
import com.marimba.tools.util.Props;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import java.util.Vector;
import java.io.File;
import java.io.InputStream;

/**
 * PowerSettingSaveAction interprets the powersetting form elements.
 *
 * @author Venkatesh Jeyaraman
 */
public final class PowerSettingSaveAction extends AbstractAction {
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws ServletException {
        if (DEBUG) {
            System.out.println("PowerSettingSaveAction called");
        }

        saveState(request, (PowerSettingForm) form);

        // and forward to the next action
        String forward = (String)((AbstractForm) form).getValue("forward");
        return (new ActionForward(forward, true));
    }

    /**
     * Save the changes in the form to the DistributionBean.
     */
    private void saveState(HttpServletRequest request, PowerSettingForm form) throws GUIException {
        // powerOption is the value of the radio box used to reset power setting values
        String powerOption = (String) ((PowerSettingForm) form).getValue(POWER_OPTION_PROP);
        String priority = (String) ((PowerSettingForm) form).getValue(POWER_PRIORITY_VALUE);
        priority = priority.trim();
        String hibernate = null;
        String hiberIdle = null;
        String monitorIdle = null;
        String diskIdle = null;
        String standbyIdle = null;
        String hiberIdleDC = null;
        String monitorIdleDC = null;
        String diskIdleDC = null;
        String standbyIdleDC = null;
        String promptPassword = null;
        String forceApply = null;
        String profileSelected = null;
        Vector powerProps = new Vector();
        
        ServletContext context = request.getSession().getServletContext();
        init(request);

        if(NOTAVBLE.equals(priority)) {
            priority = "";
        }
        
        if (DEBUG3) {
            System.out.println("powerOption = " + powerOption);
            System.out.println("priority_value = " + priority);
        }
        if ("true".equals(powerOption)) {

            profileSelected = (String) ((PowerSettingForm) form).getValue(POWER_PROFILE_SELECTED_PROP);
            if ("Select".equals(profileSelected)) {
                try {
                ISubscription sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB_COPY);
                if (sub.getProperty(PROP_POWER_KEYWORD, POWER_PROFILE_SELECTED_PROP) == null) {
                    profileSelected = DEFAULT_SCHEME_8_0;
                } else {
                    profileSelected  = sub.getProperty(PROP_POWER_KEYWORD, POWER_PROFILE_SELECTED_PROP);
                }
                hibernate = "true".equals(sub.getProperty(PROP_POWER_KEYWORD, HIBERNATE_PROP)) ? "on" : "false";
                if ("on".equals(hibernate)) {
                    hiberIdle = sub.getProperty(PROP_POWER_KEYWORD, HIBER_IDLETIME_PROP);
                    hiberIdleDC = sub.getProperty(PROP_POWER_KEYWORD, HIBER_IDLETIME_DC_PROP);
                }
                monitorIdle = sub.getProperty(PROP_POWER_KEYWORD, MONITOR_IDLETIME_PROP);
                diskIdle = sub.getProperty(PROP_POWER_KEYWORD, DISK_IDLETIME_PROP);
                standbyIdle = sub.getProperty(PROP_POWER_KEYWORD, STANDBY_IDLETIME_PROP);
                monitorIdleDC = sub.getProperty(PROP_POWER_KEYWORD, MONITOR_IDLETIME_DC_PROP);
                diskIdleDC = sub.getProperty(PROP_POWER_KEYWORD, DISK_IDLETIME_DC_PROP);
                standbyIdleDC = sub.getProperty(PROP_POWER_KEYWORD, STANDBY_IDLETIME_DC_PROP);
                promptPassword = "true".equals(sub.getProperty(PROP_POWER_KEYWORD, PROMPT_PASSWORD_PROP)) ? "on" : "false";
                forceApply = "true".equals(sub.getProperty(PROP_POWER_KEYWORD, FORCE_APPLY_PROP)) ? "on" : "false";
                } catch (SystemException e) {
                    throw new GUIException(e);
                }
            } else {

                ConfigProps profileProps = new ConfigProps(new File(main.getDataDirectory(), "PowerSettings.txt"));
                String powerProfile = profileProps.getProperty("name");
                if (null == powerProfile || !powerProfile.contains(profileSelected)) {
                    try {
                        InputStream is = context.getResourceAsStream("/custom-power.properties");
                        Props props1 = new Props(is, 0);
                        props1.load();
                        profileProps = new ConfigProps(props1, null);
                    } catch (Exception e) {
                    }
                }
                profileSelected = profileSelected + ".";
                hibernate = "true".equals(profileProps.getProperty(profileSelected + HIBERNATE_PROP)) ? "on" : "false";
                if ("on".equals(hibernate)) {
                    hiberIdle = profileProps.getProperty(profileSelected + HIBER_IDLETIME_PROP);
                    hiberIdleDC = profileProps.getProperty(profileSelected + HIBER_IDLETIME_DC_PROP);
                }
                monitorIdle = profileProps.getProperty(profileSelected + MONITOR_IDLETIME_PROP);
                diskIdle = profileProps.getProperty(profileSelected + DISK_IDLETIME_PROP);
                standbyIdle = profileProps.getProperty(profileSelected + STANDBY_IDLETIME_PROP);
                monitorIdleDC = profileProps.getProperty(profileSelected + MONITOR_IDLETIME_DC_PROP);
                diskIdleDC = profileProps.getProperty(profileSelected + DISK_IDLETIME_DC_PROP);
                standbyIdleDC = profileProps.getProperty(profileSelected + STANDBY_IDLETIME_DC_PROP);
                promptPassword = "true".equals(profileProps.getProperty(profileSelected + PROMPT_PASSWORD_PROP)) ? "on" : "false";
                forceApply = "true".equals(profileProps.getProperty(profileSelected + FORCE_APPLY_PROP)) ? "on" : "false";
            }
        } 

        try {
            ISubscription sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);
            DistributionBean distBean = getDistributionBean(request);

            if("exclude".equalsIgnoreCase(powerOption)) {
            	powerOption = null;
            	setValue(sub, powerProps, POWER_OPTION_EXCLUDE, "true", priority);
            } else if ("false".equalsIgnoreCase(powerOption)) {
            	powerOption = null;
            	setValue(sub, powerProps, POWER_OPTION_EXCLUDE, null, priority);
            } else if("true".equalsIgnoreCase(powerOption)) {
            	setValue(sub, powerProps, POWER_OPTION_EXCLUDE, null, priority);
            }
            if (profileSelected != null && profileSelected.endsWith(".")) {
                profileSelected = profileSelected.substring(0, profileSelected.length()-1);
            }
            setValue(sub, powerProps, POWER_OPTION_PROP, powerOption, priority);
            setValue(sub, powerProps, POWER_PROFILE_SELECTED_PROP, profileSelected, priority);

            if ("on".equals(hibernate)) {
                setValue(sub, powerProps, HIBERNATE_PROP, "true", priority);
            } else {
                if (powerOption != null) {
                    setValue(sub, powerProps, HIBERNATE_PROP, "false", priority);
                } else {
                    setValue(sub, powerProps, HIBERNATE_PROP, null, priority);
                }
            }

            if (!POWER_IDLE_DEFAULT_VALUE.equalsIgnoreCase(hiberIdle)) {
                setValue(sub, powerProps, HIBER_IDLETIME_PROP, hiberIdle, priority);
            } else {
                setValue(sub, powerProps, HIBER_IDLETIME_PROP, null, priority);
            }

            if (!POWER_IDLE_DEFAULT_VALUE.equalsIgnoreCase(hiberIdleDC)) {
                setValue(sub, powerProps, HIBER_IDLETIME_DC_PROP, hiberIdleDC, priority);
            } else {
                setValue(sub, powerProps, HIBER_IDLETIME_DC_PROP, null, priority);
            }

            if (!POWER_IDLE_DEFAULT_VALUE.equalsIgnoreCase(monitorIdle)) {
                setValue(sub, powerProps, MONITOR_IDLETIME_PROP, monitorIdle, priority);
            } else {
                setValue(sub, powerProps, MONITOR_IDLETIME_PROP, null, priority);
            }

            if (!POWER_IDLE_DEFAULT_VALUE.equalsIgnoreCase(monitorIdleDC)) {
                setValue(sub, powerProps, MONITOR_IDLETIME_DC_PROP, monitorIdleDC, priority);
            } else {
                setValue(sub, powerProps, MONITOR_IDLETIME_DC_PROP, null, priority);
            }

            if (!POWER_IDLE_DEFAULT_VALUE.equalsIgnoreCase(diskIdle)) {
                setValue(sub, powerProps, DISK_IDLETIME_PROP, diskIdle, priority);
            } else {
                setValue(sub, powerProps, DISK_IDLETIME_PROP, null, priority);
            }

            if (!POWER_IDLE_DEFAULT_VALUE.equalsIgnoreCase(diskIdleDC)) {
                setValue(sub, powerProps, DISK_IDLETIME_DC_PROP, diskIdleDC, priority);
            } else {
                setValue(sub, powerProps, DISK_IDLETIME_DC_PROP, null, priority);
            }

            if (!POWER_IDLE_DEFAULT_VALUE.equalsIgnoreCase(standbyIdle)) {
                setValue(sub, powerProps, STANDBY_IDLETIME_PROP, standbyIdle, priority);
            } else {
                setValue(sub, powerProps, STANDBY_IDLETIME_PROP, null, priority);
            }

            if (!POWER_IDLE_DEFAULT_VALUE.equalsIgnoreCase(standbyIdleDC)) {
                setValue(sub, powerProps, STANDBY_IDLETIME_DC_PROP, standbyIdleDC, priority);
            } else {
                setValue(sub, powerProps, STANDBY_IDLETIME_DC_PROP, null, priority);
            }

            if ("on".equals(promptPassword)) {
                setValue(sub, powerProps, PROMPT_PASSWORD_PROP, "true", priority);
            } else {
                if (powerOption != null) {
                    setValue(sub, powerProps, PROMPT_PASSWORD_PROP, "false", priority);
                } else {
                    setValue(sub, powerProps, PROMPT_PASSWORD_PROP, null, priority);
                }
            }

            if ("on".equals(forceApply)) {
                setValue(sub, powerProps, FORCE_APPLY_PROP, "true", priority);
            } else {
                if (powerOption != null) {
                    setValue(sub, powerProps, FORCE_APPLY_PROP, "false", priority);
                } else {
                    setValue(sub, powerProps, FORCE_APPLY_PROP, null, priority);
                }
            }

            distBean.setPowerProps(powerProps);

       } catch (SystemException e) {
           throw new GUIException(e);
        }
    }

    private void setValue(ISubscription sub, Vector powerProps, String key, String value, String priority) throws SystemException {
        if (value != null) {
            if(priority != null && !("".equals(priority)))  {
                value = value + PROP_DELIM + Integer.parseInt(priority);
            }
        }
        sub.setProperty(PROP_POWER_KEYWORD, key, value);
        powerProps.add(PROP_POWER_KEYWORD + PROP_DELIM + key + "=" + value);
    }
}
