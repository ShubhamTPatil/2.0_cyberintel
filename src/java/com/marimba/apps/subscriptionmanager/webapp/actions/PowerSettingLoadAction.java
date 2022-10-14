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


import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscriptionmanager.webapp.forms.PowerSettingForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;
import com.marimba.intf.util.IConfig;
import com.marimba.tools.config.ConfigProps;
import com.marimba.tools.util.Props;

import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Properties;
import java.io.File;
import java.io.InputStream;
import java.net.URL;


/**
 * This action is called when visiting the power setting page. It reads the power option and populates the form for display.
 *
 * @author Venkatesh Jeyaraman
 */
public final class PowerSettingLoadAction extends AbstractAction {
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws ServletException {
    	System.out.println("Power setting loading action ...");
        ISubscription sub = null;
        IConfig prop = new ConfigProps(null);
        HttpSession session = request.getSession();
        try {
        	init(request);
            DistributionBean distributionBean = new DistributionBean(request);

            String[] powerProfileStr = distributionBean.getPowerProfiles().getPropertyPairs();
            Vector powerProps = new Vector();
            powerProps.addElement(POWER_IDLE_DEFAULT_VALUE);
            for(int i=0; i < powerProfileStr.length; i++) {
                if ("name".equals(powerProfileStr[i])) {
                    StringTokenizer st = new StringTokenizer(powerProfileStr[i+1], ",", false);
                    //iterate through tokens
                    while(st.hasMoreTokens()) {
                        powerProps.addElement(st.nextToken(","));
                    }
                    break;
                }
            }

            Properties properties = (Properties)context.getAttribute(POWER_TEMPLATES);
            String profiles = properties.getProperty("name");
            StringTokenizer st = new StringTokenizer(profiles, ",", false);
            //iterate through tokens
            while(st.hasMoreTokens()) {
                String profileName = st.nextToken(",");
                if (!powerProps.contains(profileName)) {
                    powerProps.addElement(profileName);
                }
            }
            session.setAttribute("profiles", powerProps);
            String toggleToProfile = ((PowerSettingForm)form).getSelectedProfile();
            
            if(DEBUG3) {
                System.out.println("Profile Name : " + toggleToProfile);
            }
            
            sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);
            String excludePowerOption = getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, POWER_OPTION_EXCLUDE));

            if(null != excludePowerOption && "true".equalsIgnoreCase(excludePowerOption) && null == toggleToProfile) {
            	prop.setProperty(POWER_OPTION_PROP, "exclude");
            } else {
	            if (toggleToProfile == null) {
	                prop.setProperty(POWER_OPTION_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, POWER_OPTION_PROP)));
	                String hibernate = getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, HIBERNATE_PROP));
	                prop.setProperty(HIBERNATE_PROP, hibernate);
	
	                if ("true".equals(hibernate)) {
	                    prop.setProperty(HIBER_IDLETIME_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, HIBER_IDLETIME_PROP)));
	                    prop.setProperty(HIBER_IDLETIME_DC_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, HIBER_IDLETIME_DC_PROP)));
	                }
	                else {
	                    prop.setProperty(HIBER_IDLETIME_PROP, "Select");
	                    prop.setProperty(HIBER_IDLETIME_DC_PROP, "Select");
	                }
	                prop.setProperty(MONITOR_IDLETIME_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, MONITOR_IDLETIME_PROP)));
	                prop.setProperty(DISK_IDLETIME_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, DISK_IDLETIME_PROP)));
	                prop.setProperty(STANDBY_IDLETIME_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, STANDBY_IDLETIME_PROP)));
	                prop.setProperty(MONITOR_IDLETIME_DC_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, MONITOR_IDLETIME_DC_PROP)));
	                prop.setProperty(DISK_IDLETIME_DC_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, DISK_IDLETIME_DC_PROP)));
	                prop.setProperty(STANDBY_IDLETIME_DC_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, STANDBY_IDLETIME_DC_PROP)));
	                prop.setProperty(PROMPT_PASSWORD_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, PROMPT_PASSWORD_PROP)));
	                prop.setProperty(FORCE_APPLY_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, FORCE_APPLY_PROP)));
	                prop.setProperty(POWER_PRIORITY_VALUE, getPriorityValue(sub.getProperty(PROP_POWER_KEYWORD, POWER_PROFILE_SELECTED_PROP)));
	                if ("true".equals(getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, POWER_OPTION_PROP)))) {
	                    if (sub.getProperty(PROP_POWER_KEYWORD, POWER_PROFILE_SELECTED_PROP) == null ) {
	                        prop.setProperty(POWER_PROFILE_SELECTED_PROP, DEFAULT_SCHEME_8_0);
	                    } else {
	                        prop.setProperty(POWER_PROFILE_SELECTED_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, POWER_PROFILE_SELECTED_PROP)));
	                    }
	                }
	                else {
	                    prop.setProperty(POWER_OPTION_PROP, "false");
	                }
	            } else {
	                if ("Select".equals(toggleToProfile)) {
	                    if ("true".equals(getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, POWER_OPTION_PROP)))) {
	                        if ("".equals(getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, POWER_PROFILE_SELECTED_PROP)))) {
	                            prop.setProperty(POWER_PROFILE_SELECTED_PROP, DEFAULT_SCHEME_8_0);
	                        } else {
	                            sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB_COPY);
	                            prop.setProperty(POWER_PROFILE_SELECTED_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, POWER_PROFILE_SELECTED_PROP)));
	                        }
	                    }
	                    prop.setProperty(POWER_OPTION_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, POWER_OPTION_PROP)));
	                    String hibernate = getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, HIBERNATE_PROP));
	                    prop.setProperty(HIBERNATE_PROP, hibernate);
	
	                    if ("true".equals(hibernate)) {
	                        prop.setProperty(HIBER_IDLETIME_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, HIBER_IDLETIME_PROP)));
	                        prop.setProperty(HIBER_IDLETIME_DC_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, HIBER_IDLETIME_DC_PROP)));
	                    }
	                    else {
	                        prop.setProperty(HIBER_IDLETIME_PROP, "Select");
	                        prop.setProperty(HIBER_IDLETIME_DC_PROP, "Select");
	                    }
	                    prop.setProperty(MONITOR_IDLETIME_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, MONITOR_IDLETIME_PROP)));
	                    prop.setProperty(DISK_IDLETIME_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, DISK_IDLETIME_PROP)));
	                    prop.setProperty(STANDBY_IDLETIME_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, STANDBY_IDLETIME_PROP)));
	                    prop.setProperty(MONITOR_IDLETIME_DC_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, MONITOR_IDLETIME_DC_PROP)));
	                    prop.setProperty(DISK_IDLETIME_DC_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, DISK_IDLETIME_DC_PROP)));
	                    prop.setProperty(STANDBY_IDLETIME_DC_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, STANDBY_IDLETIME_DC_PROP)));
	                    prop.setProperty(PROMPT_PASSWORD_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, PROMPT_PASSWORD_PROP)));
	                    prop.setProperty(FORCE_APPLY_PROP, getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, FORCE_APPLY_PROP)));
	                    prop.setProperty(POWER_PRIORITY_VALUE, getPriorityValue(sub.getProperty(PROP_POWER_KEYWORD, POWER_PROFILE_SELECTED_PROP)));
	                } else {
	                    // Load the scheme from powerSettings.txt file
	                    init(request);
	                    ConfigProps config = new ConfigProps(new File(main.getDataDirectory(), "PowerSettings.txt"));
                        String powerProfile = config.getProperty("name");
                        if (null == powerProfile || !powerProfile.contains(toggleToProfile)) {
                            try {
                                InputStream is = context.getResourceAsStream("/custom-power.properties");
                                Props props1 = new Props(is, 0);
                                props1.load();
                                config= new ConfigProps(props1, null);
                            } catch (Exception e) {

                            }
                        }

                        prop.setProperty(POWER_PROFILE_SELECTED_PROP, toggleToProfile);
	                    toggleToProfile = toggleToProfile + ".";
	
	                    prop.setProperty(POWER_OPTION_PROP, "true");
	                    String hibernate = getPropertyValue(config.getProperty(toggleToProfile + HIBERNATE_PROP));
	                    prop.setProperty(HIBERNATE_PROP, hibernate);
	
	                    if ("true".equals(hibernate)) {
	                        prop.setProperty(HIBER_IDLETIME_PROP, getPropertyValue(config.getProperty(toggleToProfile + HIBER_IDLETIME_PROP)));
	                        prop.setProperty(HIBER_IDLETIME_DC_PROP, getPropertyValue(config.getProperty(toggleToProfile + HIBER_IDLETIME_DC_PROP)));
	                    }
	                    prop.setProperty(MONITOR_IDLETIME_PROP, getPropertyValue(config.getProperty(toggleToProfile + MONITOR_IDLETIME_PROP)));
	                    prop.setProperty(DISK_IDLETIME_PROP, getPropertyValue(config.getProperty(toggleToProfile + DISK_IDLETIME_PROP)));
	                    prop.setProperty(STANDBY_IDLETIME_PROP, getPropertyValue(config.getProperty(toggleToProfile + STANDBY_IDLETIME_PROP)));
	                    prop.setProperty(MONITOR_IDLETIME_DC_PROP, getPropertyValue(config.getProperty(toggleToProfile + MONITOR_IDLETIME_DC_PROP)));
	                    prop.setProperty(DISK_IDLETIME_DC_PROP, getPropertyValue(config.getProperty(toggleToProfile + DISK_IDLETIME_DC_PROP)));
	                    prop.setProperty(STANDBY_IDLETIME_DC_PROP, getPropertyValue(config.getProperty(toggleToProfile + STANDBY_IDLETIME_DC_PROP)));
	                    prop.setProperty(PROMPT_PASSWORD_PROP, getPropertyValue(config.getProperty(toggleToProfile + PROMPT_PASSWORD_PROP)));
	                    prop.setProperty(FORCE_APPLY_PROP, getPropertyValue(config.getProperty(toggleToProfile + FORCE_APPLY_PROP)));
	                    prop.setProperty(POWER_PRIORITY_VALUE, "");
	                }
	            }
            }
            // initialize form elements on jsp page with existing power setting values
            ((PowerSettingForm) form).initialize(prop);
            if ("true".equals(getPropertyValue(sub.getProperty(PROP_POWER_KEYWORD, POWER_OPTION_PROP)))) {
                ((PowerSettingForm) form).setschemeDisplay("true");
            } else {
                ((PowerSettingForm) form).setschemeDisplay("false");
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
}
