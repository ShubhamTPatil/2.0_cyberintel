// Copyright 1997-2017, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscriptionmanager.webapp.forms.AbstractForm;
import com.marimba.apps.subscriptionmanager.webapp.forms.CustomSecurityForm;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.SCAPUtils;
import com.marimba.apps.securitymgr.view.ValueParserBean;
import com.marimba.tools.config.ConfigProps;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

public final class CustomSecuritySaveAction extends AbstractAction {
    HttpSession session;
    private ConfigProps config;
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        if (DEBUG) {
            System.out.println("CustomSecuritySaveAction called");
        }
        init(request);
        session = request.getSession();
        loadStandardCustomContentDetails();
        saveState(request, (CustomSecurityForm) form);

        // and forward to the next action
        String forward = (String) ((AbstractForm) form).getValue("forward");
        return (new ActionForward(forward, true));
    }

    private void saveState(HttpServletRequest request, CustomSecurityForm form) throws GUIException {

        Vector<String> scapProps = new Vector<String>();

        try {
            ServletContext context = request.getSession().getServletContext();
            init(request);

            ISubscription sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);
            DistributionBean distBean = getDistributionBean(request);

            String scapOption = form.getSelectedSCAPOption();
            String priority = form.getCustomizePriorityValue();
            if(null != scapOption && "standard".equals(scapOption)) {
            	priority = form.getStandardPriorityValue();
            } 
            
            System.out.println("scapOption :" + scapOption);
            System.out.println("priority :" + priority);
            if (null == priority || "".equals(priority.trim()) || NOTAVBLE.equals(priority)) {
                priority = "99999";
            }

            System.out.println("Remedation checked :" + form.getRemediate());
            String remediateEnabled = form.getRemediate();
            if (null != remediateEnabled && "on".equals(remediateEnabled)) {
                remediateEnabled = "true";
            } else {
                remediateEnabled = "false";
            }
            if (null == scapOption || "false".equals(scapOption)) {
                setValue(sub, scapProps, CUSTOM_SECURITY_OPTION, null, priority);
                setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, null, priority);
                setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_ID, null, priority);
                setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_TITLE, null, priority);
                setValue(sub, scapProps, CUSTOM_SECURITY_TEMPLATE_NAME, null, priority);
                setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_ID, null, priority);
                setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_TITLE, null, priority);
                setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_REMEDIATION_ENABLED, null, priority);
            } else if("exclude".equals(scapOption)) {
                setValue(sub, scapProps, CUSTOM_SECURITY_OPTION, null, priority);
                setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, null, priority);
                setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_ID, null, priority);
                setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_TITLE, null, priority);
                setValue(sub, scapProps, CUSTOM_SECURITY_TEMPLATE_NAME, null, priority);
                setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_ID, null, priority);
                setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_TITLE, null, priority);
                setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_REMEDIATION_ENABLED, null, priority);
            } else if("customize".equals(scapOption) || "standard".equals(scapOption)) {
                setValue(sub, scapProps, CUSTOM_SECURITY_OPTION, scapOption, priority);
                String selectedScapAndProfiles = (String) ((AbstractForm) form).getValue("selectedScapAndProfilesCustom");
                System.out.println("selectedScapAndProfilesCustom : " + selectedScapAndProfiles);

                if (null != selectedScapAndProfiles && !selectedScapAndProfiles.trim().isEmpty()) {
                    ValueParserBean parserBean = new ValueParserBean(selectedScapAndProfiles, scapOption);

                    setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, parserBean.getContentFileNames(), priority);
                    String[] contentTitles = parserBean.getContentTitles().split(";");
                    String contentIds= null;
                    if("customize".equals(scapOption)) {
	                    for (String contentTitle : contentTitles) {
	                    	String[] contentTitlesStr = contentTitle.split("::");
	                        String currentContentId = contentTitlesStr[0] + "::" + SCAPUtils.getSCAPUtils().getScapContentIdFromTitle(session, "customcontentdetails", contentTitlesStr[1]);
	                        contentIds = (null == contentIds || contentIds.trim().isEmpty()) ? "" + currentContentId : contentIds + ";" + currentContentId;
	                    }
                    } else {
                        for (String contentTitle : contentTitles) {
                            String currentContentId = contentTitle + "::" + SCAPUtils.getSCAPUtils().getScapContentIdFromTitle(session, "customcontentdetails", contentTitle);
                            contentIds = (null == contentIds || contentIds.trim().isEmpty()) ? "" + currentContentId : contentIds + ";" + currentContentId;
                        }
                    }
                    setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_ID, contentIds, priority);
                    setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_TITLE, parserBean.getContentTitles(), priority);
                    if("customize".equals(scapOption)) {
                    	setValue(sub, scapProps, CUSTOM_SECURITY_TEMPLATE_NAME, parserBean.getCustomTemplateNames(), priority);
                    } else {
                    	setValue(sub, scapProps, CUSTOM_SECURITY_TEMPLATE_NAME, null, priority);
                    }
                    setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_ID, parserBean.getProfileIDs(), priority);
                    setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_TITLE, parserBean.getProfileTitles(), priority);
                    setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_REMEDIATION_ENABLED, remediateEnabled, priority);
                } else {
                    setValue(sub, scapProps, CUSTOM_SECURITY_OPTION, null, priority);
                    setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, null, priority);
                    setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_ID, null, priority);
                    setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_TITLE, null, priority);
                    setValue(sub, scapProps, CUSTOM_SECURITY_TEMPLATE_NAME, null, priority);
                    setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_ID, null, priority);
                    setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_TITLE, null, priority);
                    setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_REMEDIATION_ENABLED, null, priority);
                }
            } else {
                setValue(sub, scapProps, CUSTOM_SECURITY_OPTION, null, priority);
                setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, null, priority);
                setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_ID, null, priority);
                setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_TITLE, null, priority);
                setValue(sub, scapProps, CUSTOM_SECURITY_TEMPLATE_NAME, null, priority);
                setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_ID, null, priority);
                setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_TITLE, null, priority);
                setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_REMEDIATION_ENABLED, null, priority);
            }

            distBean.setCustomSecurityProps(scapProps);

        } catch (SystemException e) {
            throw new GUIException(e);
        }
    }
    private void loadProfileConfig() {
        config = new ConfigProps(new File(main.getDataDirectory(), CUSTOM_SECURITY_FILE_NAME));
    }
    private Hashtable readProfile(String profileName) {
        System.out.println("CustomSecuritySaveAction: reading profile: " + profileName);
        Hashtable props = new Hashtable(10);
        String profile = config.getProperty(CUSTOM_SECURITY_FILE_NAME);

        if(null != profile && (profile.equals(profileName) || profile.startsWith(profileName + ",") ||
                profile.endsWith("," + profileName) || (profile.indexOf("," + profileName + ",") > -1))) {
            String[] pairs = config.getPropertyPairs();
            for (int i = 0; i < pairs.length;) {
                String key = pairs[i++];
                String value = pairs[i++];
                if (key != null && key.startsWith(profileName+".") && value != null) {
                    props.put(key.substring(profileName.length()+1), value);
                }
            }
        }

        props.put(CUSTOM_SECURITY_PROFILE_NAME, profileName);

        return props;
    }
    private void loadStandardCustomContentDetails() {
        Map<String, String> customContentsMap = (Map<String, String>) context.getAttribute("customcontentdetailsmap");
        session.setAttribute("customcontentdetailsmap", customContentsMap);
        String[] customContentsXML = SCAPUtils.getSCAPUtils().getSupportedCustomContentsXML();
        if (customContentsXML != null) {
            for (String customContent : customContentsXML) {
                Map<String, String> customContentsXMLMap = (Map<String, String>) context.getAttribute(customContent);
                session.setAttribute(customContent, customContentsXMLMap);
            }
            if (customContentsXML.length > 0) {
                String firstCustomXML = (String) customContentsMap.keySet().toArray()[0];
                Map<String, String> customContentsXMLMap = (Map<String, String>) context.getAttribute(firstCustomXML);
                session.setAttribute("first_custom_content_profiles", customContentsXMLMap);
            } else {
                session.setAttribute("first_custom_content_profiles", (new HashMap<String, String>()));
            }
        }
    }
    private void setValue(ISubscription sub, Vector scapProps, String key, String value, String priority) throws SystemException {
        if (value != null) {
            if(priority != null && !("".equals(priority)))  {
                value = value + PROP_DELIM + Integer.parseInt(priority);
            }
        }
        sub.setProperty(PROP_CUSTOM_SECURITY_KEYWORD, key, value);
        scapProps.add(PROP_CUSTOM_SECURITY_KEYWORD + PROP_DELIM + key + "=" + value);
    }
}
