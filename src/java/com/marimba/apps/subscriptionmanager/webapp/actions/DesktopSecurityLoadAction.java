// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscriptionmanager.webapp.forms.DesktopSecurityForm;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;


import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;
import com.marimba.intf.util.IConfig;
import com.marimba.tools.config.ConfigProps;
import com.marimba.tools.util.Props;

import java.util.Vector;
import java.util.StringTokenizer;
import java.io.File;
import java.io.InputStream;


/**
 * This action is called when visiting the Desktop Security page. It reads the security settings and populates the form for display.
 *
 * @author Venkatesh Jeyaraman
 */
public final class DesktopSecurityLoadAction extends AbstractAction {

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws ServletException {

    	System.out.println("Desktop Security loading action ...");
        ISubscription sub = null;
        IConfig prop = new ConfigProps(null);
        HttpSession session = request.getSession();
        String propValue = null;
        String propEnabled = null;

        try {
        	init(request);
            DistributionBean distributionBean = new DistributionBean(request);

            String[] securityProfileStr = distributionBean.getSecurityProfiles().getPropertyPairs();
            Vector<String> securityProps = new Vector<String>();
            securityProps.addElement(POWER_IDLE_DEFAULT_VALUE);

            for(int i=0; i < securityProfileStr.length; i++) {
                if ("name".equals(securityProfileStr[i])) {
                    StringTokenizer st = new StringTokenizer(securityProfileStr[i+1], ",", false);
                    //iterate through tokens
                    while(st.hasMoreTokens()) {
                        securityProps.addElement(st.nextToken(","));
                    }
                    break;
                }
            }

            session.setAttribute("profiles", securityProps);
            String toggleToProfile = ((DesktopSecurityForm)form).getSelectedProfile();
            
            if(DEBUG3) {
                System.out.println("Profile Name : " + toggleToProfile);
            }
            
            sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);
            String excludeSecurityOption = getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_EXCLUDE));

            if(null != excludeSecurityOption && "true".equalsIgnoreCase(excludeSecurityOption) && null == toggleToProfile) {
            	prop.setProperty(DESKTOP_SECURITY_ENABLE, "exclude");
            } else {
	            if (toggleToProfile == null) {
                    System.out.println("DesktopSecurityLoadAction: toggleToProfile is null");
                    prop.setProperty(DESKTOP_SECURITY_ENABLE, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_ENABLE)));

	                prop.setProperty(DESKTOP_SECURITY_CMD_PROMPT, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_CMD_PROMPT)));
	                prop.setProperty(DESKTOP_SECURITY_FILE_SHARING, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_FILE_SHARING)));

	                prop.setProperty(DESKTOP_SECURITY_USER_READ_FLOPPY, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_READ_FLOPPY)));
	                prop.setProperty(DESKTOP_SECURITY_USER_WRITE_FLOPPY, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_WRITE_FLOPPY)));
	                prop.setProperty(DESKTOP_SECURITY_USER_READ_CDDVD, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_READ_CDDVD)));
	                prop.setProperty(DESKTOP_SECURITY_USER_WRITE_CDDVD, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_WRITE_CDDVD)));
	                prop.setProperty(DESKTOP_SECURITY_USER_READ_WPD, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_READ_WPD)));
	                prop.setProperty(DESKTOP_SECURITY_USER_WRITE_WPD, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_WRITE_WPD)));

	                prop.setProperty(DESKTOP_SECURITY_MACH_READ_FLOPPY, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_READ_FLOPPY)));
	                prop.setProperty(DESKTOP_SECURITY_MACH_WRITE_FLOPPY, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_WRITE_FLOPPY)));
	                prop.setProperty(DESKTOP_SECURITY_MACH_READ_CDDVD, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_READ_CDDVD)));
	                prop.setProperty(DESKTOP_SECURITY_MACH_WRITE_CDDVD, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_WRITE_CDDVD)));
	                prop.setProperty(DESKTOP_SECURITY_MACH_READ_WPD, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_READ_WPD)));
	                prop.setProperty(DESKTOP_SECURITY_MACH_WRITE_WPD, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_WRITE_WPD)));

                    prop.setProperty(DESKTOP_SECURITY_USER_INTERNET, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_INTERNET)));
                    prop.setProperty(DESKTOP_SECURITY_MACH_INTERNET, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_INTERNET)));


                    prop.setProperty(DESKTOP_SECURITY_MIN_PWD_STRENGTH, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MIN_PWD_STRENGTH)));
                    prop.setProperty(DESKTOP_SECURITY_MAX_PWD_AGE, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MAX_PWD_AGE)));
                    prop.setProperty(DESKTOP_SECURITY_MIN_PWD_AGE, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MIN_PWD_AGE)));
                    prop.setProperty(DESKTOP_SECURITY_FORCED_LOGOUT_TIME, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_FORCED_LOGOUT_TIME)));
                    prop.setProperty(DESKTOP_SECURITY_ENFORCE_PWD_HISTORY, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_ENFORCE_PWD_HISTORY)));
                    prop.setProperty(DESKTOP_SECURITY_ACC_LOCK_THRESHOLD, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_ACC_LOCK_THRESHOLD)));
                    prop.setProperty(DESKTOP_SECURITY_RESET_ACC_LOCK_COUNTER, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_RESET_ACC_LOCK_COUNTER)));
                    prop.setProperty(DESKTOP_SECURITY_ACC_LOCK_COUNTER, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_ACC_LOCK_COUNTER)));

                    prop.setProperty(DESKTOP_SECURITY_USER_ENABLE_SCREENSAVER, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_ENABLE_SCREENSAVER)));
                    prop.setProperty(DESKTOP_SECURITY_USER_SECURE_SCREENSAVER, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_SECURE_SCREENSAVER)));

                    propValue = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_SCREENSAVER_TIMEOUT);
                    propEnabled = getPropertyValue(propValue);
                    prop.setProperty(DESKTOP_SECURITY_USER_SCREENSAVER_TIMEOUT, propEnabled);
                    if ("true".equals(propEnabled)) {
                        prop.setProperty(DESKTOP_SECURITY_USER_SCREENSAVER_TIMEOUT_VALUE, getRestrictionPolicyValue(propValue));
                    }

                    propValue = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_FORCE_SCREENSAVER);
                    propEnabled = getPropertyValue(propValue);
                    prop.setProperty(DESKTOP_SECURITY_USER_FORCE_SCREENSAVER, propEnabled);
                    if ("true".equals(propEnabled)) {
                        prop.setProperty(DESKTOP_SECURITY_USER_FORCE_SCREENSAVER_VALUE, getRestrictionPolicyValue(propValue));
                    }

                    prop.setProperty(DESKTOP_SECURITY_FORCE_APPLY, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_FORCE_APPLY)));
                    prop.setProperty(DESKTOP_SECURITY_IMMEDIATE_UPDATE, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_IMMEDIATE_UPDATE)));
	                prop.setProperty(DESKTOP_SECURITY_PRIORITY_VALUE, getPriorityValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_TEMPLATE_NAME)));

                    prop.setProperty(DESKTOP_SECURITY_SOFTWARES_ALLOWED, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_SOFTWARES_ALLOWED)));
	                prop.setProperty(DESKTOP_SECURITY_SOFTWARES_BLOCKED, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_SOFTWARES_BLOCKED)));

                    System.out.println("DesktopSecurityLoadAction: Enable property from sub object is: " + getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_ENABLE)));
	                if ("true".equals(getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_ENABLE)))) {
                        prop.setProperty(DESKTOP_SECURITY_TEMPLATE_NAME, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_TEMPLATE_NAME)));
                        System.out.println("template name: " + prop.getProperty(DESKTOP_SECURITY_TEMPLATE_NAME));
	                } else {
	                    prop.setProperty(DESKTOP_SECURITY_ENABLE, "false");
                        System.out.println("settings security enable as false");
	                }
	            } else {
	                if ("Select".equals(toggleToProfile)) {
                        System.out.println("DesktopSecurityLoadAction: toggleToProfile is Select; enable property from sub object is: " + getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_ENABLE)));
	                    if ("true".equals(getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_ENABLE)))) {
                            sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB_COPY);
                            prop.setProperty(DESKTOP_SECURITY_TEMPLATE_NAME, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_TEMPLATE_NAME)));
	                    }

                        prop.setProperty(DESKTOP_SECURITY_ENABLE, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_ENABLE)));

                        prop.setProperty(DESKTOP_SECURITY_CMD_PROMPT, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_CMD_PROMPT)));
                        prop.setProperty(DESKTOP_SECURITY_FILE_SHARING, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_FILE_SHARING)));

                        prop.setProperty(DESKTOP_SECURITY_USER_READ_FLOPPY, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_READ_FLOPPY)));
                        prop.setProperty(DESKTOP_SECURITY_USER_WRITE_FLOPPY, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_WRITE_FLOPPY)));
                        prop.setProperty(DESKTOP_SECURITY_USER_READ_CDDVD, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_READ_CDDVD)));
                        prop.setProperty(DESKTOP_SECURITY_USER_WRITE_CDDVD, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_WRITE_CDDVD)));
                        prop.setProperty(DESKTOP_SECURITY_USER_READ_WPD, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_READ_WPD)));
                        prop.setProperty(DESKTOP_SECURITY_USER_WRITE_WPD, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_WRITE_WPD)));

                        prop.setProperty(DESKTOP_SECURITY_MACH_READ_FLOPPY, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_READ_FLOPPY)));
                        prop.setProperty(DESKTOP_SECURITY_MACH_WRITE_FLOPPY, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_WRITE_FLOPPY)));
                        prop.setProperty(DESKTOP_SECURITY_MACH_READ_CDDVD, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_READ_CDDVD)));
                        prop.setProperty(DESKTOP_SECURITY_MACH_WRITE_CDDVD, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_WRITE_CDDVD)));
                        prop.setProperty(DESKTOP_SECURITY_MACH_READ_WPD, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_READ_WPD)));
                        prop.setProperty(DESKTOP_SECURITY_MACH_WRITE_WPD, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_WRITE_WPD)));

                        prop.setProperty(DESKTOP_SECURITY_USER_INTERNET, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_INTERNET)));
                        prop.setProperty(DESKTOP_SECURITY_MACH_INTERNET, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_INTERNET)));

                        prop.setProperty(DESKTOP_SECURITY_MIN_PWD_STRENGTH, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MIN_PWD_STRENGTH)));
                        prop.setProperty(DESKTOP_SECURITY_MAX_PWD_AGE, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MAX_PWD_AGE)));
                        prop.setProperty(DESKTOP_SECURITY_MIN_PWD_AGE, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MIN_PWD_AGE)));
                        prop.setProperty(DESKTOP_SECURITY_FORCED_LOGOUT_TIME, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_FORCED_LOGOUT_TIME)));
                        prop.setProperty(DESKTOP_SECURITY_ENFORCE_PWD_HISTORY, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_ENFORCE_PWD_HISTORY)));
                        prop.setProperty(DESKTOP_SECURITY_ACC_LOCK_THRESHOLD, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_ACC_LOCK_THRESHOLD)));
                        prop.setProperty(DESKTOP_SECURITY_RESET_ACC_LOCK_COUNTER, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_RESET_ACC_LOCK_COUNTER)));
                        prop.setProperty(DESKTOP_SECURITY_ACC_LOCK_COUNTER, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_ACC_LOCK_COUNTER)));

                        prop.setProperty(DESKTOP_SECURITY_USER_ENABLE_SCREENSAVER, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_ENABLE_SCREENSAVER)));
                        prop.setProperty(DESKTOP_SECURITY_USER_SECURE_SCREENSAVER, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_SECURE_SCREENSAVER)));

                        propValue = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_SCREENSAVER_TIMEOUT);
                        propEnabled = getPropertyValue(propValue);
                        prop.setProperty(DESKTOP_SECURITY_USER_SCREENSAVER_TIMEOUT, propEnabled);
                        if ("true".equals(propEnabled)) {
                            prop.setProperty(DESKTOP_SECURITY_USER_SCREENSAVER_TIMEOUT_VALUE, getRestrictionPolicyValue(propValue));
                        }

                        propValue = sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_FORCE_SCREENSAVER);
                        propEnabled = getPropertyValue(propValue);
                        prop.setProperty(DESKTOP_SECURITY_USER_FORCE_SCREENSAVER, propEnabled);
                        if ("true".equals(propEnabled)) {
                            prop.setProperty(DESKTOP_SECURITY_USER_FORCE_SCREENSAVER_VALUE, getRestrictionPolicyValue(propValue));
                        }

                        prop.setProperty(DESKTOP_SECURITY_FORCE_APPLY, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_FORCE_APPLY)));
                        prop.setProperty(DESKTOP_SECURITY_IMMEDIATE_UPDATE, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_IMMEDIATE_UPDATE)));
                        prop.setProperty(DESKTOP_SECURITY_PRIORITY_VALUE, getPriorityValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_TEMPLATE_NAME)));

                        prop.setProperty(DESKTOP_SECURITY_SOFTWARES_ALLOWED, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_SOFTWARES_ALLOWED)));
                        prop.setProperty(DESKTOP_SECURITY_SOFTWARES_BLOCKED, getPropertyValue(sub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_SOFTWARES_BLOCKED)));

	                } else {
                        System.out.println("Toggle to profile: " + toggleToProfile);

	                    // Load the scheme from desktop-security-settings.txt file
	                    init(request);
	                    ConfigProps config = new ConfigProps(new File(main.getDataDirectory(), "desktop-security-settings.txt"));
                        prop.setProperty(DESKTOP_SECURITY_TEMPLATE_NAME, toggleToProfile);
	                    toggleToProfile = toggleToProfile + ".";
	
	                    prop.setProperty(DESKTOP_SECURITY_ENABLE, "true");

	                    prop.setProperty(DESKTOP_SECURITY_CMD_PROMPT, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_CMD_PROMPT)));
	                    prop.setProperty(DESKTOP_SECURITY_FILE_SHARING, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_FILE_SHARING)));

                        prop.setProperty(DESKTOP_SECURITY_USER_READ_FLOPPY, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_USER_READ_FLOPPY)));
	                    prop.setProperty(DESKTOP_SECURITY_USER_WRITE_FLOPPY, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_USER_WRITE_FLOPPY)));
	                    prop.setProperty(DESKTOP_SECURITY_USER_READ_CDDVD, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_USER_READ_CDDVD)));
	                    prop.setProperty(DESKTOP_SECURITY_USER_WRITE_CDDVD, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_USER_WRITE_CDDVD)));
	                    prop.setProperty(DESKTOP_SECURITY_USER_READ_WPD, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_USER_READ_WPD)));
	                    prop.setProperty(DESKTOP_SECURITY_USER_WRITE_WPD, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_USER_WRITE_WPD)));

                        prop.setProperty(DESKTOP_SECURITY_MACH_READ_FLOPPY, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_MACH_READ_FLOPPY)));
	                    prop.setProperty(DESKTOP_SECURITY_MACH_WRITE_FLOPPY, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_MACH_WRITE_FLOPPY)));
	                    prop.setProperty(DESKTOP_SECURITY_MACH_READ_CDDVD, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_MACH_READ_CDDVD)));
	                    prop.setProperty(DESKTOP_SECURITY_MACH_WRITE_CDDVD, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_MACH_WRITE_CDDVD)));
	                    prop.setProperty(DESKTOP_SECURITY_MACH_READ_WPD, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_MACH_READ_WPD)));
	                    prop.setProperty(DESKTOP_SECURITY_MACH_WRITE_WPD, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_MACH_WRITE_WPD)));

                        prop.setProperty(DESKTOP_SECURITY_USER_INTERNET, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_USER_INTERNET)));
                        prop.setProperty(DESKTOP_SECURITY_MACH_INTERNET, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_MACH_INTERNET)));

                        prop.setProperty(DESKTOP_SECURITY_MIN_PWD_STRENGTH, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_MIN_PWD_STRENGTH)));
                        prop.setProperty(DESKTOP_SECURITY_MAX_PWD_AGE, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_MAX_PWD_AGE)));
                        prop.setProperty(DESKTOP_SECURITY_MIN_PWD_AGE, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_MIN_PWD_AGE)));
                        prop.setProperty(DESKTOP_SECURITY_FORCED_LOGOUT_TIME, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_FORCED_LOGOUT_TIME)));
                        prop.setProperty(DESKTOP_SECURITY_ENFORCE_PWD_HISTORY, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_ENFORCE_PWD_HISTORY)));
                        prop.setProperty(DESKTOP_SECURITY_ACC_LOCK_THRESHOLD, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_ACC_LOCK_THRESHOLD)));
                        prop.setProperty(DESKTOP_SECURITY_RESET_ACC_LOCK_COUNTER, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_RESET_ACC_LOCK_COUNTER)));
                        prop.setProperty(DESKTOP_SECURITY_ACC_LOCK_COUNTER, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_ACC_LOCK_COUNTER)));

                        prop.setProperty(DESKTOP_SECURITY_USER_ENABLE_SCREENSAVER, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_USER_ENABLE_SCREENSAVER)));
                        prop.setProperty(DESKTOP_SECURITY_USER_SECURE_SCREENSAVER, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_USER_SECURE_SCREENSAVER)));

                        propValue = config.getProperty(toggleToProfile + DESKTOP_SECURITY_USER_SCREENSAVER_TIMEOUT);
                        propEnabled = getPropertyValue(propValue);
                        prop.setProperty(DESKTOP_SECURITY_USER_SCREENSAVER_TIMEOUT, propEnabled);
                        if ("true".equals(propEnabled)) {
                            prop.setProperty(DESKTOP_SECURITY_USER_SCREENSAVER_TIMEOUT_VALUE, getRestrictionPolicyValue(propValue));
                        }

                        propValue = config.getProperty(toggleToProfile + DESKTOP_SECURITY_USER_FORCE_SCREENSAVER);
                        propEnabled = getPropertyValue(propValue);
                        prop.setProperty(DESKTOP_SECURITY_USER_FORCE_SCREENSAVER, propEnabled);
                        if ("true".equals(propEnabled)) {
                            prop.setProperty(DESKTOP_SECURITY_USER_FORCE_SCREENSAVER_VALUE, getRestrictionPolicyValue(propValue));
                        }

                        prop.setProperty(DESKTOP_SECURITY_FORCE_APPLY, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_FORCE_APPLY)));
                        prop.setProperty(DESKTOP_SECURITY_IMMEDIATE_UPDATE, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_IMMEDIATE_UPDATE)));
	                    prop.setProperty(DESKTOP_SECURITY_PRIORITY_VALUE, "");

                        prop.setProperty(DESKTOP_SECURITY_SOFTWARES_ALLOWED, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_SOFTWARES_ALLOWED)));
                        prop.setProperty(DESKTOP_SECURITY_SOFTWARES_BLOCKED, getPropertyValue(config.getProperty(toggleToProfile + DESKTOP_SECURITY_SOFTWARES_BLOCKED)));
	                }
	            }
            }

            System.out.println("B4 initializing form : " + prop.getProperty(DESKTOP_SECURITY_TEMPLATE_NAME));
            // initialize form elements on jsp page with existing security setting values
            ((DesktopSecurityForm) form).initialize(prop);

            if ("true".equals(getPropertyValue(prop.getProperty(DESKTOP_SECURITY_ENABLE))) &&
                    !"Select".equals(prop.getProperty(prop.getProperty(DESKTOP_SECURITY_TEMPLATE_NAME)))) {
                System.out.println("Scheme display enabled");
                ((DesktopSecurityForm) form).setschemeDisplay("true");
            } else {
                System.out.println("Scheme display disabled");
                ((DesktopSecurityForm) form).setschemeDisplay("false");
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

    private String getRestrictionPolicyValue(String value) {
        String excludingPriority = getPropertyValueExcludingPriority(value);
        int idx = excludingPriority.indexOf(",");

        if (idx > -1) {
            return excludingPriority.substring(idx + 1);
        }

        return excludingPriority;
    }
}
