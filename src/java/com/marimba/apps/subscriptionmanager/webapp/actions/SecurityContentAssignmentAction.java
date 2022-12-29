// Copyright 2018, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.securitymgr.view.CustomSCAPValueParserBean;
import com.marimba.apps.securitymgr.view.SCAPBean;
import com.marimba.apps.securitymgr.view.SCAPValueParserBean;
import com.marimba.apps.securitymgr.view.ValueParserBean;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscriptionmanager.webapp.forms.SecurityContentAssignmentForm;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.SCAPUtils;
import com.marimba.intf.util.IConfig;
import com.marimba.tools.config.*;
import com.marimba.tools.util.Password;
import com.marimba.tools.util.Props;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;
import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.tools.ldap.LDAPName;
import com.marimba.tools.util.OSConstants;
import com.marimba.tools.xml.XMLOutputStream;
import com.marimba.tools.xml.XMLParser;
import com.marimba.tools.xml.XMLException;
import com.marimba.webapps.intf.SystemException;
import com.marimba.tools.util.DebugFlag;

import com.marimba.xccdf.util.*;
import com.marimba.oval.util.*;
import com.marimba.intf.db.IConnectionPool;

import org.json.simple.JSONValue;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.io.File;

/**
 * 
 *
 * @author Elaiyaraja Thagavel
 */

public final class SecurityContentAssignmentAction extends AbstractAction {
    final static int DEBUG = DebugFlag.getDebug("SECURITY/WORKFLOW");
    private static final String DOT = ".";
    HttpSession session;
    HttpServletRequest request;
    Map<String, SCAPBean> scapBeansListWindows;
    Map<String, SCAPBean> scapBeansListNonWindows;
    File rootDir;
    HashMap<String, String> customizedProfilesMap;
    Map<String, Map<String, Map<String, String>>> customizedTemplatesMap;

    ISubscription sub = null;
    DistributionBean distBean;
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws ServletException {
        this.session = request.getSession();
        this.request = request;

        System.out.println("SCAP(Windows) setting loading action ...");
        IConfig prop = new ConfigProps(null);
        SecurityContentAssignmentForm assignmentForm = ((SecurityContentAssignmentForm) form);
        String action =  assignmentForm.getAction();
        String view = "load";
        try {
            init(request);
            if (action == null) {
                action = "load";
            }
            System.out.println("Action = " + action);
            sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);
            distBean = getDistributionBean(request);

            rootDir = main.getDataDirectory();
            if ("load".equals(action)) {

                scapBeansListWindows = new HashMap<String, SCAPBean>();
                scapBeansListNonWindows = new HashMap<String, SCAPBean>();
                customizedProfilesMap = new HashMap<String, String>();
                customizedTemplatesMap = new HashMap<String, Map<String, Map<String, String>>>();

                loadWindowsContentDetails();
                loadNonWindowsContentDetails();
                assignmentForm.setRemediateEnabled(getRemediationEnableStatus());
                assignmentForm.setPriority(getPriorityValue());
            }

            if ("save".equals(action)) {
                String customizedProfiles = assignmentForm.getCustomizedProfiles();
                System.out.println("Customized profiles "+ customizedProfiles);
                try {
                    saveCustomizedProfilesTemplate(customizedProfiles);
                } catch (Exception ex) {
                    view = "save_failure";
                    request.setAttribute("error", ex.getMessage());
                    return (mapping.findForward(view));
                }

                String selectedWindowsProfiles = assignmentForm.getSelectedWindowsProfiles();
                System.out.println("Selected windows profiles "+ selectedWindowsProfiles);

                String selectedNonWindowsProfiles = assignmentForm.getSelectedNonWindowsProfiles();
                System.out.println("Selected non windows profiles "+ selectedNonWindowsProfiles);

                HashMap<String, String> profilesMap = parseProfiles(selectedWindowsProfiles, selectedNonWindowsProfiles);

                String priority = assignmentForm.getPriority();
                if (priority == null || "".equals(priority)) {
                    priority = "99999";
                }
                String remediateEnabled = assignmentForm.getRemediateEnabled();
                if (remediateEnabled == null || "".equals(remediateEnabled)) {
                    remediateEnabled = "false";    
                }
                System.out.println("save() priority = " + priority);
                System.out.println("save() remediateEnabled = " + remediateEnabled);

                // windows
                String windowsStandardProfiles = profilesMap.get("windows_standard");
                String windowsCustomizedProfiles = profilesMap.get("windows_customized");
                if ( (windowsStandardProfiles == null || windowsStandardProfiles.trim().isEmpty()) &&
                     (windowsCustomizedProfiles == null || windowsCustomizedProfiles.trim().isEmpty())) {
                    removeWindowsProfiles(priority);
                } else {
                	Map<String, String> storedPropsMap = new HashMap<String, String>();

                    saveWindowsProfiles(windowsStandardProfiles, "standard", priority, remediateEnabled, storedPropsMap);
                    saveWindowsProfiles(windowsCustomizedProfiles, "customize", priority, remediateEnabled, storedPropsMap);
                    storeWindowsAttributes(storedPropsMap, priority, remediateEnabled);
                }

                // non windows
                String nonWindowsStandardProfiles = profilesMap.get("nonwindows_standard");
                String nonWindowsCustomizedProfiles = profilesMap.get("nonwindows_customized");
                if ( (nonWindowsStandardProfiles == null || nonWindowsStandardProfiles.trim().isEmpty()) &&
                     (nonWindowsCustomizedProfiles == null || nonWindowsCustomizedProfiles.trim().isEmpty())) {
                    removeNonWindowsProfiles(priority);
                } else {
                	Map<String, String> storedPropsMap = new HashMap<String, String>();
                    saveNonWindowsProfiles(nonWindowsStandardProfiles, "standard", priority, remediateEnabled, storedPropsMap);
                    saveNonWindowsProfiles(nonWindowsCustomizedProfiles, "customize", priority, remediateEnabled, storedPropsMap);
                    storeNonWindowsAttributes(storedPropsMap, priority, remediateEnabled);
                }

                //custom
                String customStandardProfiles = profilesMap.get("custom_standard");
                String customCustomizedProfiles = profilesMap.get("custom_customized");
                if ( (customStandardProfiles == null || customStandardProfiles.trim().isEmpty()) &&
                     (customCustomizedProfiles == null || customCustomizedProfiles.trim().isEmpty())) {
                    removeCustomProfiles(priority);
                } else {
                	Map<String, String> storedPropsMap = new HashMap<String, String>();
                    saveCustomProfiles(customStandardProfiles, "standard", priority, remediateEnabled, storedPropsMap);
                    saveCustomProfiles(customCustomizedProfiles, "customize", priority, remediateEnabled, storedPropsMap);
                    storeCustomAttributes(storedPropsMap, priority, remediateEnabled);
                }

                view = "save_success";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (mapping.findForward(view));
    }

    private String getPriorityValue() throws Exception {
        String priorityValue = getPriorityValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD, USGCB_SECURITY_SCAP_SELECTEDCONTENT_TITLE));
        if (priorityValue == null || "".equals(priorityValue) || NOTAVBLE.equals(priorityValue)) {
            priorityValue = getPriorityValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_SCAP_SELECTEDCONTENT_TITLE));
            if (priorityValue == null || "".equals(priorityValue) || NOTAVBLE.equals(priorityValue)) {
                priorityValue = getPriorityValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_TITLE));
            }
        }
        if (priorityValue == null || "".equals(priorityValue)) {
            priorityValue =  NOTAVBLE;
        }
        System.out.println("getPriorityValue() priority = " + priorityValue);
        return priorityValue;
    }

    private String getRemediationEnableStatus() throws Exception {
        String remediationEnabled = "false";
        remediationEnabled = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD, USGCB_SECURITY_SCAP_REMEDIATION_ENABLED));
        if (remediationEnabled == null || "".equals(remediationEnabled)) {
            remediationEnabled = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_SCAP_REMEDIATION_ENABLED));
            if (remediationEnabled == null || "".equals(remediationEnabled)) {
                remediationEnabled = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD, CUSTOM_SECURITY_SCAP_REMEDIATION_ENABLED));
            }
        }
        if (remediationEnabled == null || "".equals(remediationEnabled)) {
            remediationEnabled = "false";
        }
        System.out.println("getRemediationEnableStatus() remediationEnabled = " + remediationEnabled);

        return remediationEnabled;
    }

    private HashMap<String, String> parseProfiles(String windowsProfiles, String nonWindowsProfiles) {
        HashMap<String, String> profilesMap = new HashMap<String, String>();

        StringBuffer windowsStandardProfilesBuffer = new StringBuffer();
        StringBuffer windowsCustomizedProfilesBuffer = new StringBuffer();

        StringBuffer nonWindowsStandardProfilesBuffer = new StringBuffer();
        StringBuffer nonWindowsCustomizedProfilesBuffer = new StringBuffer();

        StringBuffer customStandardProfilesBuffer = new StringBuffer();
        StringBuffer customCustomizedProfilesBuffer = new StringBuffer();

        if (windowsProfiles != null && !windowsProfiles.trim().isEmpty()) {
            String profiles[] = windowsProfiles.split(":_:");
            for (int i=0; i<profiles.length;i++) {
                String profile = profiles[i];
                if (profile != null) {
                    String details[] = profile.split("\\$-\\$");
                    if (details.length == 6) {
                        String contentFileName = details[0];
                        String contentTitle = details[1];
                        String profileId = details[2];
                        String profileTitle = details[3];
                        String type =  details[4];
                        if ("custom".equals(type)) {
                            if (isCustomizedProfile(contentFileName, profileId, "custom")) {
                                String templateName = getProfileTemplate(contentFileName);
                                if (profileId.lastIndexOf("@") != -1) {
                                    if (templateName == null) {
                                        templateName = profileId.substring(profileId.lastIndexOf("@")+1);
                                    }
                                    profileId = profileId.substring(0, profileId.lastIndexOf("@"));
                                }
                                profile = contentFileName + "$-$" + templateName + "$-$" + contentTitle + "$-$" + profileId + "$-$" + profileTitle;
                                if (!customCustomizedProfilesBuffer.toString().equals("")) {
                                    customCustomizedProfilesBuffer.append(":_:");
                                }
                                customCustomizedProfilesBuffer.append(profile);
                            } else {
                                if (!customStandardProfilesBuffer.toString().equals("")) {
                                    customStandardProfilesBuffer.append(":_:");
                                }
                                customStandardProfilesBuffer.append(profile);
                            }
                        } else {
                            if (isCustomizedProfile(contentFileName, profileId, "windows")) {
                                String templateName = getProfileTemplate(contentFileName);
                                if (profileId.lastIndexOf("@") != -1) {
                                    if (templateName == null) {
                                        templateName = profileId.substring(profileId.lastIndexOf("@")+1);
                                    }
                                    profileId = profileId.substring(0, profileId.lastIndexOf("@"));
                                }

                                profile = contentFileName + "$-$" + templateName + "$-$" + contentTitle + "$-$" + profileId + "$-$" + profileTitle;
                                if (!windowsCustomizedProfilesBuffer.toString().equals("")) {
                                    windowsCustomizedProfilesBuffer.append(":_:");
                                }
                                windowsCustomizedProfilesBuffer.append(profile);
                            } else {
                                if (!windowsStandardProfilesBuffer.toString().equals("")) {
                                    windowsStandardProfilesBuffer.append(":_:");
                                }
                                windowsStandardProfilesBuffer.append(profile);
                            }
                        }
                    }
                }
            }
        }

        if (nonWindowsProfiles != null && !nonWindowsProfiles.trim().isEmpty()) {
            String profiles[] = nonWindowsProfiles.split(":_:");
            for (int i=0; i<profiles.length;i++) {
                String profile = profiles[i];
                if (profile != null) {
                    String details[] = profile.split("\\$-\\$");
                    if (details.length == 6) {
                        String contentFileName = details[0];
                        String contentTitle = details[1];
                        String profileId = details[2];
                        String profileTitle = details[3];
                        String type =  details[4];
                        if ("custom".equals(type)) {
                            if (isCustomizedProfile(contentFileName, profileId, "custom")) {
                                String templateName = getProfileTemplate(contentFileName);
                                if (profileId.lastIndexOf("@") != -1) {
                                    if (templateName == null) {
                                        templateName = profileId.substring(profileId.lastIndexOf("@")+1);
                                    }
                                    profileId = profileId.substring(0, profileId.lastIndexOf("@"));
                                }
                                profile = contentFileName + "$-$" + templateName + "$-$" + contentTitle + "$-$" + profileId + "$-$" + profileTitle;
                                if (!customCustomizedProfilesBuffer.toString().equals("")) {
                                    customCustomizedProfilesBuffer.append(":_:");
                                }
                                customCustomizedProfilesBuffer.append(profile);
                            } else {
                                if (!customStandardProfilesBuffer.toString().equals("")) {
                                    customStandardProfilesBuffer.append(":_:");
                                }
                                customStandardProfilesBuffer.append(profile);
                            }
                        } else {
                            if (isCustomizedProfile(contentFileName, profileId, "nonwindows")) {
                                String templateName = getProfileTemplate(contentFileName);
                                if (profileId.lastIndexOf("@") != -1) {
                                    if (templateName == null) {
                                        templateName = profileId.substring(profileId.lastIndexOf("@")+1);
                                    }
                                    profileId = profileId.substring(0, profileId.lastIndexOf("@"));
                                }
                                profile = contentFileName + "$-$" + templateName + "$-$" + contentTitle + "$-$" + profileId + "$-$" + profileTitle;
                                if (!nonWindowsCustomizedProfilesBuffer.toString().equals("")) {
                                    nonWindowsCustomizedProfilesBuffer.append(":_:");
                                }
                                nonWindowsCustomizedProfilesBuffer.append(profile);
                            } else {
                                if (!nonWindowsStandardProfilesBuffer.toString().equals("")) {
                                    nonWindowsStandardProfilesBuffer.append(":_:");
                                }
                                nonWindowsStandardProfilesBuffer.append(profile);
                            }
                        }
                    }
                }
            }
        }

        System.out.println("windows_standard = "+ windowsStandardProfilesBuffer.toString());
        System.out.println("windows_customized = "+ windowsCustomizedProfilesBuffer.toString());

        System.out.println("nonwindows_standard = "+ nonWindowsStandardProfilesBuffer.toString());
        System.out.println("nonwindows_customized = "+ nonWindowsCustomizedProfilesBuffer.toString());

        System.out.println("custom_standard = "+ customStandardProfilesBuffer.toString());
        System.out.println("custom_customized = "+ customCustomizedProfilesBuffer.toString());

        profilesMap.put("windows_standard", windowsStandardProfilesBuffer.toString());
        profilesMap.put("windows_customized", windowsCustomizedProfilesBuffer.toString());

        profilesMap.put("nonwindows_standard", nonWindowsStandardProfilesBuffer.toString());
        profilesMap.put("nonwindows_customized", nonWindowsCustomizedProfilesBuffer.toString());

        profilesMap.put("custom_standard", customStandardProfilesBuffer.toString());
        profilesMap.put("custom_customized", customCustomizedProfilesBuffer.toString());

        return profilesMap;
    }

    private boolean isCustomizedProfile(String contentFileName, String profileName, String type) {
        boolean result = false;
        if (profileName.indexOf("@") != -1) {
            profileName = profileName.substring(profileName.indexOf("@") + 1);
            if (customizedProfilesMap != null) {
                if (customizedProfilesMap.containsKey(contentFileName)) {
                    result = true;
                }
            }

            if (!result) {
                Map<String, Map<String, String>> templates = customizedTemplatesMap.get(type);
                if (templates != null && templates.get(profileName) != null) {
                    result = true;
                }
            }
        } else {
            if (customizedProfilesMap != null && null != contentFileName) {
                if (customizedProfilesMap.containsKey(contentFileName)) {
                    result = true;
                }
            }
        }
        return result;
    }                                         

    private String getProfileTemplate(String contentFileName) {
        String templateName = null;
        if (customizedProfilesMap != null) {
            if (customizedProfilesMap.containsKey(contentFileName)) {
                templateName = customizedProfilesMap.get(contentFileName);
            }
        }
        return templateName;
    }
    private void storeValue(Map<String, String> storedPropsMap, String key, String value) {
    	if(null == storedPropsMap.get(key)) {
    		storedPropsMap.put(key, value);
    	} else {
    		String availableValue = storedPropsMap.get(key);
    		availableValue = availableValue + ";" + value;
    		storedPropsMap.put(key, availableValue);
    	}
    }
    private void saveWindowsProfiles(String selectedScapAndProfiles, String scapOption, String priority, String remediateEnabled, Map<String, String> storedPropsMap) throws Exception {
        Vector<String> scapProps = new Vector<String>();
        String type = "windows";
        if (null != selectedScapAndProfiles && !selectedScapAndProfiles.trim().isEmpty() ) {
            ValueParserBean parserBean = new ValueParserBean(selectedScapAndProfiles, scapOption);

            storeValue(storedPropsMap, USGCB_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, parserBean.getContentFileNames());
            String[] contentTitles = parserBean.getContentTitles().split(";");
            String contentIds = null;
            if("customize".equals(scapOption)) {
                for (String contentTitle : contentTitles) {
                    String[] contentTitlesStr = contentTitle.split("::");
                    String currentContentId = contentTitlesStr[0] + "::" + SCAPUtils.getSCAPUtils().getScapContentIdFromTitle(session, "usgcbcontentdetails", contentTitlesStr[1]);
                    contentIds = (null == contentIds || contentIds.trim().isEmpty()) ? "" + currentContentId : contentIds + ";" + currentContentId;
                }
            } else {
                for (String contentTitle : contentTitles) {
                    String currentContentId = contentTitle + "::" + SCAPUtils.getSCAPUtils().getScapContentIdFromTitle(session, "usgcbcontentdetails", contentTitle);
                    contentIds = (null == contentIds || contentIds.trim().isEmpty()) ? "" + currentContentId : contentIds + ";" + currentContentId;
                }
            }
            storeValue(storedPropsMap, USGCB_SECURITY_SCAP_SELECTEDCONTENT_ID, contentIds);
            storeValue(storedPropsMap, USGCB_SECURITY_SCAP_SELECTEDCONTENT_TITLE, parserBean.getContentTitles());
            if("customize".equals(scapOption)) {
                storeValue(storedPropsMap, USGCB_SECURITY_TEMPLATE_NAME, parserBean.getCustomTemplateNames());
            } 
            storeValue(storedPropsMap, USGCB_SECURITY_SCAP_SELECTEDPROFILE_ID, parserBean.getProfileIDs());
            storeValue(storedPropsMap, USGCB_SECURITY_SCAP_SELECTEDPROFILE_TITLE, parserBean.getProfileTitles());
        }
    }
    private void storeWindowsAttributes(Map<String, String> windowsPropsMap, String priority, String remediateEnabled) {
    	if(windowsPropsMap.size() > 0) {
    		try {
		    	Vector<String> scapProps = new Vector<String>();
		        String type = "windows";
		        setValue(sub, scapProps, USGCB_SECURITY_OPTION, null, priority,type); // not required so set as null
		        setValue(sub, scapProps, USGCB_SECURITY_SCAP_SELECTEDCONTENT_ID, windowsPropsMap.get(USGCB_SECURITY_SCAP_SELECTEDCONTENT_ID), priority, type);
		        setValue(sub, scapProps, USGCB_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, windowsPropsMap.get(USGCB_SECURITY_SCAP_SELECTEDCONTENT_FILENAME), priority, type);
		        setValue(sub, scapProps, USGCB_SECURITY_SCAP_SELECTEDCONTENT_TITLE, windowsPropsMap.get(USGCB_SECURITY_SCAP_SELECTEDCONTENT_TITLE), priority, type);
		        setValue(sub, scapProps, USGCB_SECURITY_TEMPLATE_NAME, windowsPropsMap.get(USGCB_SECURITY_TEMPLATE_NAME), priority, type);
		        setValue(sub, scapProps, USGCB_SECURITY_SCAP_SELECTEDPROFILE_ID, windowsPropsMap.get(USGCB_SECURITY_SCAP_SELECTEDPROFILE_ID), priority, type);
		        setValue(sub, scapProps, USGCB_SECURITY_SCAP_SELECTEDPROFILE_TITLE, windowsPropsMap.get(USGCB_SECURITY_SCAP_SELECTEDPROFILE_TITLE), priority, type);
		        setValue(sub, scapProps, USGCB_SECURITY_SCAP_REMEDIATION_ENABLED, remediateEnabled, priority, type);
		        if (distBean != null) {
		            distBean.setUsgcbSecurityProps(scapProps);
		        }
    		} catch(Exception ed) {
    			ed.printStackTrace();
    			System.out.println("Failed to store windows profile");
    		}
    	}
    }
    private void storeNonWindowsAttributes(Map<String, String> nonwindowsPropsMap, String priority, String remediateEnabled) {
    	if(nonwindowsPropsMap.size() > 0) {
    		try {
    			Vector<String> scapProps = new Vector<String>();
    	        String type = "nonwindows";
    	        setValue(sub, scapProps, SCAP_SECURITY_OPTION, null, priority, type); // not required so set as null
    	        setValue(sub, scapProps, SCAP_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, nonwindowsPropsMap.get(SCAP_SECURITY_SCAP_SELECTEDCONTENT_FILENAME), priority, type);
    	        setValue(sub, scapProps, SCAP_SECURITY_SCAP_SELECTEDCONTENT_ID, nonwindowsPropsMap.get(SCAP_SECURITY_SCAP_SELECTEDCONTENT_ID), priority, type);
    	        setValue(sub, scapProps, SCAP_SECURITY_SCAP_SELECTEDCONTENT_TITLE, nonwindowsPropsMap.get(SCAP_SECURITY_SCAP_SELECTEDCONTENT_TITLE), priority, type);
    	        setValue(sub, scapProps, SCAP_SECURITY_TEMPLATE_NAME, nonwindowsPropsMap.get(SCAP_SECURITY_TEMPLATE_NAME), priority, type);
    	        setValue(sub, scapProps, SCAP_SECURITY_SCAP_SELECTEDPROFILE_ID, nonwindowsPropsMap.get(SCAP_SECURITY_SCAP_SELECTEDPROFILE_ID), priority, type);
    	        setValue(sub, scapProps, SCAP_SECURITY_SCAP_SELECTEDPROFILE_TITLE, nonwindowsPropsMap.get(SCAP_SECURITY_SCAP_SELECTEDPROFILE_TITLE), priority, type);
    	        setValue(sub, scapProps, SCAP_SECURITY_SCAP_REMEDIATION_ENABLED, remediateEnabled, priority, type);
    	        if (distBean != null) {
    	            distBean.setScapSecurityProps(scapProps);
    	        }
    		} catch(Exception ed) {
    			ed.printStackTrace();
    			System.out.println("Failed to store non-windows profile");
    		}
    	}
    }
    private void storeCustomAttributes(Map<String, String> customPropsMap, String priority, String remediateEnabled) {
    	if(customPropsMap.size() > 0) {
    		try {
    			Vector<String> scapProps = new Vector<String>();
    	        String type = "custom";
    	        setValue(sub, scapProps, CUSTOM_SECURITY_OPTION, null, priority, type);
    	        setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, customPropsMap.get(CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_FILENAME), priority, type);
    	        setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_ID, customPropsMap.get(CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_ID), priority, type);
    	        setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_TITLE, customPropsMap.get(CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_TITLE), priority, type);
    	        setValue(sub, scapProps, CUSTOM_SECURITY_TEMPLATE_NAME, customPropsMap.get(CUSTOM_SECURITY_TEMPLATE_NAME), priority, type);
    	        setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_ID, customPropsMap.get(CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_ID), priority, type);
    	        setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_TITLE, customPropsMap.get(CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_TITLE), priority, type);
    	        setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_REMEDIATION_ENABLED, remediateEnabled, priority, type);
    	        if (distBean != null) {
    	            distBean.setCustomSecurityProps(scapProps);
    	        }
    		} catch(Exception ed) {
    			ed.printStackTrace();
    			System.out.println("Failed to store custom profile");
    		}
    	}
    }
    private void removeWindowsProfiles(String priority) throws Exception {
        Vector<String> scapProps = new Vector<String>();
        String type = "windows";
        setValue(sub, scapProps, USGCB_SECURITY_OPTION, null, priority,type);
        setValue(sub, scapProps, USGCB_SECURITY_SCAP_SELECTEDCONTENT_ID, null, priority, type);
        setValue(sub, scapProps, USGCB_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, null, priority, type);
        setValue(sub, scapProps, USGCB_SECURITY_SCAP_SELECTEDCONTENT_TITLE, null, priority, type);
        setValue(sub, scapProps, USGCB_SECURITY_TEMPLATE_NAME, null, priority, type);
        setValue(sub, scapProps, USGCB_SECURITY_SCAP_SELECTEDPROFILE_ID, null, priority, type);
        setValue(sub, scapProps, USGCB_SECURITY_SCAP_SELECTEDPROFILE_TITLE, null, priority, type);
        setValue(sub, scapProps, USGCB_SECURITY_SCAP_REMEDIATION_ENABLED, null, priority, type);
        if (distBean != null) {
            distBean.setUsgcbSecurityProps(scapProps);
        }
        System.out.println("Removed windows Profiles");
    }

    private void saveNonWindowsProfiles(String selectedScapAndProfiles, String scapOption, String priority, String remediateEnabled,
    		Map<String, String> storedPropsMap) throws Exception {
        Vector<String> scapProps = new Vector<String>();
        String type = "nonwindows";
        if (null != selectedScapAndProfiles && !selectedScapAndProfiles.trim().isEmpty()) {
            ValueParserBean parserBean = new ValueParserBean(selectedScapAndProfiles, scapOption);
            storeValue(storedPropsMap, SCAP_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, parserBean.getContentFileNames());
            String[] contentTitles = parserBean.getContentTitles().split(";");
            String contentIds= null;
            if("customize".equals(scapOption)) {
                for (String contentTitle : contentTitles) {
                    String[] contentTitlesStr = contentTitle.split("::");
                    String currentContentId = contentTitlesStr[0] + "::" + SCAPUtils.getSCAPUtils().getScapContentIdFromTitle(session, "scapcontentdetails", contentTitlesStr[1]);
                    contentIds = (null == contentIds || contentIds.trim().isEmpty()) ? "" + currentContentId : contentIds + ";" + currentContentId;
                }
            } else {
                for (String contentTitle : contentTitles) {
                    String currentContentId = contentTitle + "::" + SCAPUtils.getSCAPUtils().getScapContentIdFromTitle(session, "scapcontentdetails", contentTitle);
                    contentIds = (null == contentIds || contentIds.trim().isEmpty()) ? "" + currentContentId : contentIds + ";" + currentContentId;
                }
            }
            storeValue(storedPropsMap, SCAP_SECURITY_SCAP_SELECTEDCONTENT_ID, contentIds);
            storeValue(storedPropsMap, SCAP_SECURITY_SCAP_SELECTEDCONTENT_TITLE, parserBean.getContentTitles());
            if("customize".equals(scapOption)) {
                storeValue(storedPropsMap, SCAP_SECURITY_TEMPLATE_NAME, parserBean.getCustomTemplateNames());
            } 
            storeValue(storedPropsMap, SCAP_SECURITY_SCAP_SELECTEDPROFILE_ID, parserBean.getProfileIDs());
            storeValue(storedPropsMap, SCAP_SECURITY_SCAP_SELECTEDPROFILE_TITLE, parserBean.getProfileTitles());
        }
    }

    private void removeNonWindowsProfiles(String priority) throws Exception {
        Vector<String> scapProps = new Vector<String>();
        String type = "nonwindows";
        setValue(sub, scapProps, SCAP_SECURITY_OPTION, null, priority, type);
        setValue(sub, scapProps, SCAP_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, null, priority, type);
        setValue(sub, scapProps, SCAP_SECURITY_SCAP_SELECTEDCONTENT_ID, null, priority, type);
        setValue(sub, scapProps, SCAP_SECURITY_SCAP_SELECTEDCONTENT_TITLE, null, priority, type);
        setValue(sub, scapProps, SCAP_SECURITY_TEMPLATE_NAME, null, priority, type);
        setValue(sub, scapProps, SCAP_SECURITY_SCAP_SELECTEDPROFILE_ID, null, priority, type);
        setValue(sub, scapProps, SCAP_SECURITY_SCAP_SELECTEDPROFILE_TITLE, null, priority, type);
        setValue(sub, scapProps, SCAP_SECURITY_SCAP_REMEDIATION_ENABLED, null, priority, type);
        if (distBean != null) {
            distBean.setScapSecurityProps(scapProps);
        }
        System.out.println("Removed non windows Profiles");
    }

    private void saveCustomProfiles(String selectedScapAndProfiles, String scapOption, String priority, String remediateEnabled,
    		Map<String, String> storedPropsMap) throws Exception {
        Vector<String> scapProps = new Vector<String>();
        String type = "custom";
        if (null != selectedScapAndProfiles && !selectedScapAndProfiles.trim().isEmpty()) {
            ValueParserBean parserBean = new ValueParserBean(selectedScapAndProfiles, scapOption);

            storeValue(storedPropsMap, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, parserBean.getContentFileNames());
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
            storeValue(storedPropsMap, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_ID, contentIds);
            storeValue(storedPropsMap, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_TITLE, parserBean.getContentTitles());
            if("customize".equals(scapOption)) {
                storeValue(storedPropsMap, CUSTOM_SECURITY_TEMPLATE_NAME, parserBean.getCustomTemplateNames());
            } 
            storeValue(storedPropsMap, CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_ID, parserBean.getProfileIDs());
            storeValue(storedPropsMap, CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_TITLE, parserBean.getProfileTitles());
        }
    }

    private void removeCustomProfiles(String priority) throws Exception {
        Vector<String> scapProps = new Vector<String>();
        String type = "custom";
        setValue(sub, scapProps, CUSTOM_SECURITY_OPTION, null, priority, type);
        setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, null, priority, type);
        setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_ID, null, priority, type);
        setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_TITLE, null, priority, type);
        setValue(sub, scapProps, CUSTOM_SECURITY_TEMPLATE_NAME, null, priority, type);
        setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_ID, null, priority, type);
        setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_TITLE, null, priority, type);
        setValue(sub, scapProps, CUSTOM_SECURITY_SCAP_REMEDIATION_ENABLED, null, priority, type);
        if (distBean != null) {
            distBean.setCustomSecurityProps(scapProps);
        }
        System.out.println("Removed custom profiles");
    }

    private void saveCustomizedProfilesTemplate(String customizedProfiles) throws Exception {

        System.out.println("saveCustomizedProfiles...");

        if (customizedProfiles == null || "".equals(customizedProfiles.trim())) {
            return;
        }
        customizedProfilesMap = new HashMap<String, String>();
        String profiles[] = customizedProfiles.split(":_:");
        System.out.println("Profile count "+ profiles.length);
        for (int i=0; i< profiles.length;i++) {
            String profile = profiles[i];
            System.out.println(profile);
            String details[] = profile.split(";");
            System.out.println("Profile Attributes count "+ details.length);
            if (details.length > 0) {
                boolean isEditOperation = false;
                String contentFileName = details[0];
                String profileId = details[1];
                String targetType = details[2];
                String profileName = details[3];
                String profileDescription = details[4];
                String priority = details[5];
                String modifiedRules = null;
                if (details.length > 6) {
                    modifiedRules = details[6];
                }
                if (modifiedRules != null) {
                    modifiedRules = modifiedRules.trim();
                } else {
                    modifiedRules = "";
                }

                customizedProfilesMap.put(contentFileName, profileName);
                String scapType = SCAPUtils.getSCAPUtils().getXMLContentType(contentFileName);
                
                HashMap<String, String> modifiedRulesMap = getModifiedRulesMap(modifiedRules,scapType);

                String forceApplyEnabled = "false";

                System.out.println("contentFileName : "+ contentFileName);
                System.out.println("targetType : "+ targetType);
                System.out.println("profileId : "+ profileId);
                System.out.println("profileName : "+ profileName);
                System.out.println("profileDescription : "+ profileDescription);
                System.out.println("profilePriority : "+ priority);
                System.out.println("modifiedRules : "+ modifiedRules);
                System.out.println("modifiedRulesMap : "+ modifiedRulesMap);
                String contentMapType = null;
                String templateType = null;
                String contentDetail = null;
                
                if ("windows".equals(targetType)) {
                	contentDetail = "usgcbcontentdetails";
                    contentMapType = "usgcbcontentdetailsmap";
                    templateType = "usgcbtemplates";
                } else if ("nonwindows".equals(targetType)) {
                	contentDetail = "scapcontentdetails";
                    contentMapType = "scapcontentdetailsmap";
                    templateType = "scaptemplates";
                } else if ("custom".equals(targetType)) {
                	contentDetail = "customcontentdetails";
                    contentMapType = "customcontentdetailsmap";
                    templateType = "customtemplates";
                }

                boolean pluginPublished = false, configSaved = false, dbInserted = false, customTemplatePropSaved = false;
                Map<String, String> existingProfileSettings = new HashMap<String, String>();
                Map<String, String> existingProfileChanges = new HashMap<String, String>();
                try {
                    String contentIdValue = SCAPUtils.getSCAPUtils().getScapContentId(session, contentDetail, contentFileName);
                    System.out.println("SCAP Content Id:" + contentIdValue);
                    String contentTitleValue = SCAPUtils.getSCAPUtils().getSessionMapValue(session, contentFileName, contentMapType);
                    System.out.println("SCAP Content Title:" + contentTitleValue);
                    String profileTitle = SCAPUtils.getSCAPUtils().getSessionMapValue(session, profileId, contentMapType);
                    if(null == profileTitle) profileTitle =  profileName;
                    System.out.println("Profile title :" + profileTitle);
                    
                    if (new File(rootDir + File.separator + templateType + File.separator + profileName + ".properties").exists()) {
                        existingProfileChanges = readProfileChanges(profileName, templateType);
                        existingProfileSettings = readProfileSettings(profileName, templateType);
                        isEditOperation = true;
                    }
                    String result = "";
                    
                    Map<String, String> parametersMap = new HashMap<String, String>();
                    parametersMap.put("profile.id", profileId);
                    parametersMap.put("profile.title", profileName);
                    parametersMap.put("profile.description", profileDescription);
                    
                    customTemplatePropSaved = writeCustomTemplateChange(modifiedRulesMap, profileName, templateType, parametersMap, scapType);
                    if (!customTemplatePropSaved) {
                        throw new Exception("Failed to save custom template \"" + profileName+ "\" properties");
                    }
                    configSaved = saveCustomTemplate(contentIdValue, contentTitleValue, contentFileName, profileId, profileName, profileTitle, profileDescription, targetType, forceApplyEnabled);
                    if (!configSaved) {
                        throw new Exception("Failed to save custom template \"" + profileName+ "\" in config");
                    }
                    if("oval".equals(scapType)) {
                    	dbInserted = insertCustomOvalTemplateIntoDB(contentFileName, profileId, profileName, profileDescription, targetType, (rootDir + File.separator + templateType + File.separator + profileName + ".properties"));                    	
                    } else {
                    	dbInserted = insertCustomTemplateIntoDB(contentFileName, profileId, profileName, profileDescription, targetType, (rootDir + File.separator + templateType + File.separator + profileName + ".properties"));                    	
                    }
                    
                    if (!dbInserted) {
                        throw new Exception("Failed to save custom template \"" + profileName+ "\" in database");
                    }
                    pluginPublished = publishPlugin();
                    if (!pluginPublished) {
                        throw new Exception("Failed to publish plugin after saving template \""+ profileName+"\"");
                    }
                } catch (Exception ex) {
                    if (DEBUG > 4) {
                        ex.printStackTrace();
                    }
                    if (isEditOperation) {
                        if (customTemplatePropSaved) {
                            if (configSaved) {
                                if (dbInserted) {
                                    if (pluginPublished) {
                                        //all success... do nothing...
                                    } else {
                                        revertChanges(profileName, templateType, targetType, existingProfileChanges, existingProfileSettings, scapType, false, false, false, true);
                                    }
                                } else {
                                    revertChanges(profileName, templateType, targetType, existingProfileChanges, existingProfileSettings, scapType, false, false, true, true);
                                }
                            } else {
                                revertChanges(profileName, templateType, targetType, existingProfileChanges, existingProfileSettings, scapType, false, true, true, true);
                            }
                        } else {
                            // do nothing... nothing changed due to edit...
                        }
                    } else {
                        removeProfile(profileName, targetType, true);
                    }
                    throw new Exception(ex.getMessage());
                }
            }
        }
    }

    private void revertChanges(String revertProfile, String templateType, String targetType, Map<String, String> existingProfileChanges, 
    		Map<String, String> existingProfileSettings, String scapType, boolean skipTemplate, boolean skipConfig, boolean skipDb, boolean skipPlugin) throws Exception {
        String contentIdValue = "";
        String contentTitleValue = "";
        String contentFileName = "";
        String profileId = "";
        String profileName = "";
        String profileTitle = "";
        String profileDescription = "";
        String forceApplyEnabled = "";
        if ("usgcbtemplates".equals(templateType)) {
            contentIdValue = existingProfileSettings.get(USGCB_SECURITY_SCAP_SELECTEDCONTENT_ID);
            contentTitleValue = existingProfileSettings.get(USGCB_SECURITY_SCAP_SELECTEDCONTENT_TITLE);
            contentFileName = existingProfileSettings.get(USGCB_SECURITY_SCAP_SELECTEDCONTENT_FILENAME);
            profileId = existingProfileSettings.get(USGCB_SECURITY_SCAP_SELECTEDPROFILE_ID);
            profileName = existingProfileSettings.get(USGCB_SECURITY_TEMPLATE_NAME);
            profileTitle = existingProfileSettings.get(USGCB_SECURITY_SCAP_SELECTEDPROFILE_TITLE);
            profileDescription = existingProfileSettings.get(USGCB_SECURITY_TEMPLATE_DESC);
            forceApplyEnabled = existingProfileSettings.get(USGCB_SECURITY_FORCE_APPLY);
        } else if ("scaptemplates".equals(templateType)) {
            contentIdValue = existingProfileSettings.get(SCAP_SECURITY_SCAP_SELECTEDCONTENT_ID);
            contentTitleValue = existingProfileSettings.get(SCAP_SECURITY_SCAP_SELECTEDCONTENT_TITLE);
            contentFileName = existingProfileSettings.get(SCAP_SECURITY_SCAP_SELECTEDCONTENT_FILENAME);
            profileId = existingProfileSettings.get(SCAP_SECURITY_SCAP_SELECTEDPROFILE_ID);
            profileName = existingProfileSettings.get(SCAP_SECURITY_TEMPLATE_NAME);
            profileTitle = existingProfileSettings.get(SCAP_SECURITY_SCAP_SELECTEDPROFILE_TITLE);
            profileDescription = existingProfileSettings.get(SCAP_SECURITY_TEMPLATE_DESC);
            forceApplyEnabled = existingProfileSettings.get(SCAP_SECURITY_FORCE_APPLY);
        } else if ("customtemplates".equals(templateType)) {
            contentIdValue = existingProfileSettings.get(CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_ID);
            contentTitleValue = existingProfileSettings.get(CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_TITLE);
            contentFileName = existingProfileSettings.get(CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_FILENAME);
            profileId = existingProfileSettings.get(CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_ID);
            profileName = existingProfileSettings.get(CUSTOM_SECURITY_TEMPLATE_NAME);
            profileTitle = existingProfileSettings.get(CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_TITLE);
            profileDescription = existingProfileSettings.get(CUSTOM_SECURITY_TEMPLATE_DESC);
            forceApplyEnabled = existingProfileSettings.get(CUSTOM_SECURITY_FORCE_APPLY);
        }
        
        Map<String, String> parametersMap = new HashMap<String, String>();
        parametersMap.put("profile.id", profileId);
        parametersMap.put("profile.title", profileTitle);
        parametersMap.put("profile.description", profileDescription);
        
        if (!skipTemplate) {
            writeCustomTemplateChange(existingProfileChanges, revertProfile, templateType, true, parametersMap, scapType);
        }
        if (!skipConfig) {
            saveCustomTemplate(contentIdValue, contentTitleValue, contentFileName, profileId, profileName, profileTitle, profileDescription, targetType, forceApplyEnabled);
        }
        if (!skipDb) {
        	if("oval".equals(scapType)) {
        		insertCustomOvalTemplateIntoDB(contentFileName, profileId, profileName, profileDescription, targetType, (rootDir + File.separator + templateType + File.separator + profileName + ".properties"));
        	} else {
        		insertCustomTemplateIntoDB(contentFileName, profileId, profileName, profileDescription, targetType, (rootDir + File.separator + templateType + File.separator + profileName + ".properties"));
        	}
        }
    }

    private boolean saveCustomTemplate(String contentIdValue, String contentTitleValue, String contentFileName, String profileId, String profileName, String profileTitle, String profileDescription, String targetType, String forceApplyEnabled) {
        boolean isTemplateSaved = false;
        if ("windows".equals(targetType)) {
            ConfigProps config = new ConfigProps(new File(rootDir, USGCB_SECURITY_FILE_NAME));
            try {
                String availableProfiles = config.getProperty(USGCB_SECURITY_PROFILE_NAME);
                if(profileName != null && profileName != "") {
                    String profileNamePrefix = profileName + ".";
                    String updatedProfiles = ((availableProfiles != null) &&
                            ((availableProfiles.startsWith(profileName + ",")) ||
                                    (availableProfiles.endsWith("," + profileName)) ||
                                    (availableProfiles.equals(profileName)) ||
                                    (availableProfiles.indexOf(","+ profileName+ ",") > -1)))
                            ?  availableProfiles : ((availableProfiles == null) ? profileName : availableProfiles + "," + profileName);
                    config.setProperty(USGCB_SECURITY_PROFILE_NAME, updatedProfiles);
                    config.setProperty(profileNamePrefix + USGCB_SECURITY_TEMPLATE_NAME, profileName);

                    if (!isEmpty(profileDescription)) {
                        config.setProperty(profileNamePrefix + USGCB_SECURITY_TEMPLATE_DESC, profileDescription);
                    }
                    config.setProperty(profileNamePrefix + USGCB_SECURITY_ENABLED, "true");

                    if("on".equals(forceApplyEnabled)) {
                        forceApplyEnabled = "true";
                    } else {
                        forceApplyEnabled = "false";
                    }
                    config.setProperty(profileNamePrefix + USGCB_SECURITY_FORCE_APPLY, null);
                    config.setProperty(profileNamePrefix + USGCB_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, contentFileName);
                    config.setProperty(profileNamePrefix + USGCB_SECURITY_SCAP_SELECTEDCONTENT_ID, contentIdValue);
                    config.setProperty(profileNamePrefix + USGCB_SECURITY_SCAP_SELECTEDCONTENT_TITLE, contentTitleValue);
                    config.setProperty(profileNamePrefix + USGCB_SECURITY_SCAP_SELECTEDPROFILE_ID, profileId);
                    config.setProperty(profileNamePrefix + USGCB_SECURITY_SCAP_SELECTEDPROFILE_TITLE, profileTitle);

                    if (!config.save()) {
                        throw new Exception("Failed to save init configurations");
                    }
                    isTemplateSaved = true;
                }
                System.out.println("Successfully saved SCAP Security Policy template for the profile :" + profileName);
            } catch (Exception ioe) {
                ioe.printStackTrace();
                config = null;
            }
        } if ("nonwindows".equals(targetType)) {
            ConfigProps config = new ConfigProps(new File(rootDir, SCAP_SECURITY_FILE_NAME));
            try {
                String availableProfiles = config.getProperty(SCAP_SECURITY_PROFILE_NAME);
                if(profileName != null && profileName != "") {
                    String profileNamePrefix = profileName + ".";
                    String updatedProfiles = ((availableProfiles != null) &&
                            ((availableProfiles.startsWith(profileName + ",")) ||
                                    (availableProfiles.endsWith("," + profileName)) ||
                                    (availableProfiles.equals(profileName)) ||
                                    (availableProfiles.indexOf(","+ profileName+ ",") > -1)))
                            ?  availableProfiles : ((availableProfiles == null) ? profileName : availableProfiles + "," + profileName);
                    config.setProperty(SCAP_SECURITY_PROFILE_NAME, updatedProfiles);
                    config.setProperty(profileNamePrefix + SCAP_SECURITY_TEMPLATE_NAME, profileName);

                    if (!isEmpty(profileDescription)) {
                        config.setProperty(profileNamePrefix + SCAP_SECURITY_TEMPLATE_DESC, profileDescription);
                    }
                    config.setProperty(profileNamePrefix + SCAP_SECURITY_ENABLED, "true");
                    if("on".equals(forceApplyEnabled)) {
                        forceApplyEnabled = "true";
                    } else {
                        forceApplyEnabled = "false";
                    }
                    config.setProperty(profileNamePrefix + SCAP_SECURITY_FORCE_APPLY, null);
                    config.setProperty(profileNamePrefix + SCAP_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, contentFileName);
                    config.setProperty(profileNamePrefix + SCAP_SECURITY_SCAP_SELECTEDCONTENT_ID, contentIdValue);
                    config.setProperty(profileNamePrefix + SCAP_SECURITY_SCAP_SELECTEDCONTENT_TITLE, contentTitleValue);
                    config.setProperty(profileNamePrefix + SCAP_SECURITY_SCAP_SELECTEDPROFILE_ID, profileId);
                    config.setProperty(profileNamePrefix + SCAP_SECURITY_SCAP_SELECTEDPROFILE_TITLE, profileTitle);

                    if (!config.save()) {
                        throw new Exception("Failed to save init configurations");
                    }
                    isTemplateSaved = true;
                    System.out.println("Successfully saved SCAP Security Policy template for the profile :" + profileName);
                }
            } catch (Exception ioe) {
                ioe.printStackTrace();
                config = null;
            }
        } if ("custom".equals(targetType)) {
            ConfigProps config = new ConfigProps(new File(rootDir, CUSTOM_SECURITY_FILE_NAME));
            try {
                String availableProfiles = config.getProperty(CUSTOM_SECURITY_PROFILE_NAME);
                if(profileName != null && profileName != "") {
                    String profileNamePrefix = profileName + ".";
                    String updatedProfiles = ((availableProfiles != null) &&
                            ((availableProfiles.startsWith(profileName + ",")) ||
                                    (availableProfiles.endsWith("," + profileName)) ||
                                    (availableProfiles.equals(profileName)) ||
                                    (availableProfiles.indexOf(","+ profileName+ ",") > -1)))
                            ?  availableProfiles : ((availableProfiles == null) ? profileName : availableProfiles + "," + profileName);
                    config.setProperty(CUSTOM_SECURITY_PROFILE_NAME, updatedProfiles);
                    config.setProperty(profileNamePrefix + CUSTOM_SECURITY_TEMPLATE_NAME, profileName);

                    if (!isEmpty(profileDescription)) {
                        config.setProperty(profileNamePrefix + CUSTOM_SECURITY_TEMPLATE_DESC, profileDescription);
                    }
                    config.setProperty(profileNamePrefix + CUSTOM_SECURITY_ENABLED, "true");
                    if("on".equals(forceApplyEnabled)) {
                        forceApplyEnabled = "true";
                    } else {
                        forceApplyEnabled = "false";
                    }
                    config.setProperty(profileNamePrefix + CUSTOM_SECURITY_FORCE_APPLY, null);
                    config.setProperty(profileNamePrefix + CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, contentFileName);
                    config.setProperty(profileNamePrefix + CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_ID, contentIdValue);
                    config.setProperty(profileNamePrefix + CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_TITLE, contentTitleValue);
                    config.setProperty(profileNamePrefix + CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_ID, profileId);
                    config.setProperty(profileNamePrefix + CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_TITLE, profileTitle);

                    if (!config.save()) {
                        throw new Exception("Failed to save init configurations");
                    }
                    isTemplateSaved = true;
                    System.out.println("Successfully saved Custom Security Policy template for the profile :" + profileName);
                }
            } catch (Exception ioe) {
                ioe.printStackTrace();
                config = null;
            }
        }
        return isTemplateSaved;
    }

    private boolean publishPlugin() {
        boolean pluginPublished = false;
        try {
            // do a plugin publish now... just reload the same plugin props we persisted in previous publish and reset it.
            // this will ensure, the template file is also now sent to plugin...
            String publishUser = main.getConfig().getProperty("subscriptionmanager.publishurl.username");
            String publishPassword = main.getConfig().getProperty("subscriptionmanager.publishurl.password");
            if (publishPassword != null) {
                publishPassword = Password.decode(publishPassword);
            }
            pluginPublished = main.publishChannel(publishUser, publishPassword, main.getConfig());
        } catch (Exception ex) {
            if (DEBUG > 0) {
                ex.printStackTrace();
            }
        }
        return pluginPublished;
    }

    private void removeProfile(String removeProfileStr, String targetType, boolean ignoreFailures) throws Exception {
        removeProfile(removeProfileStr, targetType, ignoreFailures, false, false, false, false);
    }

    private void removeProfile(String removeProfileStr, String targetType, boolean ignoreFailures, boolean skipTemplate, boolean skipConfig, boolean skipDb, boolean skipPlugin) throws Exception {
        ConfigProps config = null;
        String profileStr = null;
        String[] profileArr = null;
        String templateType = null;
        if ("windows".equals(targetType)) {
            config = new ConfigProps(new File(rootDir, USGCB_SECURITY_FILE_NAME));
            profileStr = config.getProperty(USGCB_SECURITY_PROFILE_NAME);
            profileArr = profileStr.split(PROP_DELIM);
            templateType = "usgcbtemplates";
        } else if ("nonwindows".equals(targetType)) {
            config = new ConfigProps(new File(rootDir, SCAP_SECURITY_FILE_NAME));
            profileStr = config.getProperty(SCAP_SECURITY_PROFILE_NAME);
            profileArr = profileStr.split(PROP_DELIM);
            templateType = "scaptemplates";
        } else if ("custom".equals(targetType)) {
            config = new ConfigProps(new File(rootDir, CUSTOM_SECURITY_FILE_NAME));
            profileStr = config.getProperty(CUSTOM_SECURITY_PROFILE_NAME);
            profileArr = profileStr.split(PROP_DELIM);
            templateType = "customtemplates";
        }

        if (removeProfileStr != null) {
            try {
                boolean pluginPublished = false, configUpdated = false, dbDeleted = false, customTemplatePropDeleted = false;
                String removeProfile = removeProfileStr.startsWith("profile_sel_") ? removeProfileStr.substring(12) : removeProfileStr;

                for (String profile : profileArr) {
                    if (!isEmpty(profile) && profile.equals(removeProfile)) {
                        String contentId = null, profileId = null, fileName = null;
                        if ("windows".equals(targetType)) {
                            contentId = config.getProperty(removeProfile + "." + USGCB_SECURITY_SCAP_SELECTEDCONTENT_ID);
                            profileId = config.getProperty(removeProfile + "." + USGCB_SECURITY_SCAP_SELECTEDPROFILE_ID);
                            fileName = config.getProperty(removeProfile + "." + USGCB_SECURITY_SCAP_SELECTEDCONTENT_FILENAME);
                        } else if ("nonwindows".equals(targetType)) {
                            contentId = config.getProperty(removeProfile + "." + SCAP_SECURITY_SCAP_SELECTEDCONTENT_ID);
                            profileId = config.getProperty(removeProfile + "." + SCAP_SECURITY_SCAP_SELECTEDPROFILE_ID);
                            fileName = config.getProperty(removeProfile + "." + SCAP_SECURITY_SCAP_SELECTEDCONTENT_FILENAME);
                        } else if ("custom".equals(targetType)) {
                            contentId = config.getProperty(removeProfile + "." + CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_ID);
                            profileId = config.getProperty(removeProfile + "." + CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_ID);
                            fileName = config.getProperty(removeProfile + "." + CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_FILENAME);
                        }

                        System.out.println("removeProfile(), removeProfile - " + removeProfile);
                        System.out.println("removeProfile(), contentId - " + contentId);
                        System.out.println("removeProfile(), profileId - " + profileId);
                        System.out.println("removeProfile(), fileName - " + fileName);
                        String scapType = SCAPUtils.getSCAPUtils().getXMLContentType(fileName);
                        if (skipDb) {
                            dbDeleted = true;
                        } else {
                            //remove it from tables
                        	if("oval".equals(scapType)) {
                        		dbDeleted = deleteCustomTemplateFromDB(contentId, profileId + "_" + removeProfile);
                        	} else {
                        		dbDeleted = deleteCustomOVALTemplateFromDB(contentId, profileId + "_" + removeProfile);
                        	}
                        }
                        if (dbDeleted || ignoreFailures) {
                            if (skipTemplate) {
                                customTemplatePropDeleted = true;
                            } else {
                                customTemplatePropDeleted = new File(rootDir + File.separator + templateType, removeProfile + ".properties").delete();
                            }

                            if (customTemplatePropDeleted || ignoreFailures) {
                                if (skipConfig) {
                                    configUpdated = true;
                                } else {
                                    clearProfile(config, removeProfile, targetType);
                                    configUpdated = true;
                                }
                                if (configUpdated || ignoreFailures) {
                                    if (skipPlugin) {
                                        pluginPublished = true;
                                    } else {
                                        // do a plugin publish now... just reload the same plugin props we persisted in previous publish and reset it.
                                        // this will ensure, the template file is also now sent to plugin...
                                        String pluginPropsFile = main.getDataDirectory().getAbsolutePath() + File.separator + "properties.txt";
                                        IConfig userPluginProps = new ConfigPrefs(new File(pluginPropsFile), null, null);

                                        String publishUser = userPluginProps.getProperty("subscriptionmanager.publishurl.username");
                                        String publishPassword = userPluginProps.getProperty("subscriptionmanager.publishurl.password");
                                        if (publishPassword != null) {
                                            publishPassword = Password.decode(publishPassword);
                                        }

                                        try {
                                            pluginPublished = main.publishChannel(publishUser, publishPassword, userPluginProps);
                                        } catch (Throwable t) {
                                            t.printStackTrace();
                                            pluginPublished = false;
                                        }
                                    }

                                    //remove it from context
                                    SCAPUtils.getSCAPUtils().deleteProfileForScapContent(fileName, profileId + "@" + removeProfile);

                                    if (pluginPublished) {
                                        System.out.println("Successfully deleted Security Policy template for the profile :" + profileId + "@" + removeProfile);
                                    } else {
                                        //plugin publish failed... but ignore it, no harm...
                                        System.out.println("Failed to delete Security Policy template for the profile :" + profileId + "@" + removeProfile);
                                        throw new Exception("Failed to remove profile from vInspector Plugin");
                                    }
                                } else {
                                    //config update failed...
                                    throw new Exception("Failed to remove template settings");
                                }
                            } else {
                                //template delete failed...
                                throw new Exception("Failed to remove template settings");
                            }
                        } else {
                            //db delete failed...
                            throw new Exception("Failed to remove profile from Database");
                        }
                    } else {
                        //ignore...
                    }
                }
            } catch (Exception ioe) {
                System.out.println("Failed to remove configurations for profile: " + removeProfileStr);
                if (DEBUG > 0) {
                    ioe.printStackTrace();
                }
            }
        }
        if (config != null) {
            config.save();
        }
    }

     private boolean deleteCustomTemplateFromDB(String contentName, String profileName) {
        boolean deleted = false;
        try {
            XCCDFHandler xccdfHandler = new XCCDFHandler();
            System.out.println("deleteCustomTemplateFromDB(), contentName - " + contentName);
            System.out.println("deleteCustomTemplateFromDB(), profileName - " + profileName);
            IConnectionPool connectionPool = main.getConnectionPool();
            System.out.println("deleteCustomTemplateFromDB(), connectionPool1 - " + connectionPool);
            if (connectionPool == null) {
                try {
                    main.getQueryExecutor().run();
                    connectionPool = main.getConnectionPool();
                } catch (Throwable t) {
                    connectionPool = null;
                }
                System.out.println("deleteCustomTemplateFromDB(), connectionPool2 - " + connectionPool);
            }
            int result = 1;
            if (connectionPool != null) {
                result = xccdfHandler.deleteProfileFromDB(contentName, profileName, connectionPool);
                System.out.println("deleteCustomTemplateFromDB(), result - " + result);
            }
            deleted = (result == 0);
        } catch(Exception ed) {
            ed.printStackTrace();
            deleted = false;
        }
        return deleted;
    }
     private boolean deleteCustomOVALTemplateFromDB(String contentName, String profileName) {
         boolean deleted = false;
         try {
             OVALHandler ovalHandler = new OVALHandler();
             System.out.println("deleteCustomOVALTemplateFromDB(), contentName - " + contentName);
             System.out.println("deleteCustomOVALTemplateFromDB(), profileName - " + profileName);
             IConnectionPool connectionPool = main.getConnectionPool();
             System.out.println("deleteCustomOVALTemplateFromDB(), connectionPool1 - " + connectionPool);
             if (connectionPool == null) {
                 try {
                     main.getQueryExecutor().run();
                     connectionPool = main.getConnectionPool();
                 } catch (Throwable t) {
                     connectionPool = null;
                 }
                 System.out.println("deleteCustomOVALTemplateFromDB(), connectionPool2 - " + connectionPool);
             }
             int result = 1;
             if (connectionPool != null) {
                 result = ovalHandler.deleteProfileFromDB(contentName, profileName, connectionPool);
                 System.out.println("deleteCustomOVALTemplateFromDB(), result - " + result);
             }
             deleted = (result == 0);
         } catch(Exception ed) {
             ed.printStackTrace();
             deleted = false;
         }
         return deleted;
     }
     public void clearProfile(ConfigProps prop, String profileName, String targetType) {
        if ("windows".equals(targetType)) {
            prop.setProperty(profileName + DOT + USGCB_SECURITY_TEMPLATE_NAME, null);
            prop.setProperty(profileName + DOT + USGCB_SECURITY_TEMPLATE_DESC, null);
            prop.setProperty(profileName + DOT + USGCB_SECURITY_FORCE_APPLY, null);
            prop.setProperty(profileName + DOT + USGCB_SECURITY_ENABLED, null);
            prop.setProperty(profileName + DOT + USGCB_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, null);
            prop.setProperty(profileName + DOT + USGCB_SECURITY_SCAP_SELECTEDCONTENT_ID, null);
            prop.setProperty(profileName + DOT + USGCB_SECURITY_SCAP_SELECTEDCONTENT_TITLE, null);
            prop.setProperty(profileName + DOT + USGCB_SECURITY_SCAP_SELECTEDPROFILE_ID, null);
            prop.setProperty(profileName + DOT + USGCB_SECURITY_SCAP_SELECTEDPROFILE_TITLE, null);
            String availableProfiles = prop.getProperty(USGCB_SECURITY_PROFILE_NAME);
            prop.setProperty(USGCB_SECURITY_PROFILE_NAME, removeProfileName(availableProfiles, profileName));
        } else if ("nonwindows".equals(targetType)) {
            prop.setProperty(profileName + DOT + SCAP_SECURITY_TEMPLATE_NAME, null);
            prop.setProperty(profileName + DOT + SCAP_SECURITY_TEMPLATE_DESC, null);
            prop.setProperty(profileName + DOT + SCAP_SECURITY_FORCE_APPLY, null);
            prop.setProperty(profileName + DOT + SCAP_SECURITY_ENABLED, null);
            prop.setProperty(profileName + DOT + SCAP_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, null);
            prop.setProperty(profileName + DOT + SCAP_SECURITY_SCAP_SELECTEDCONTENT_ID, null);
            prop.setProperty(profileName + DOT + SCAP_SECURITY_SCAP_SELECTEDCONTENT_TITLE, null);
            prop.setProperty(profileName + DOT + SCAP_SECURITY_SCAP_SELECTEDPROFILE_ID, null);
            prop.setProperty(profileName + DOT + SCAP_SECURITY_SCAP_SELECTEDPROFILE_TITLE, null);
            String availableProfiles = prop.getProperty(SCAP_SECURITY_PROFILE_NAME);
            prop.setProperty(SCAP_SECURITY_PROFILE_NAME, removeProfileName(availableProfiles, profileName));
        } else if ("custom".equals(targetType)) {
            prop.setProperty(profileName + DOT + CUSTOM_SECURITY_TEMPLATE_NAME, null);
            prop.setProperty(profileName + DOT + CUSTOM_SECURITY_TEMPLATE_DESC, null);
            prop.setProperty(profileName + DOT + CUSTOM_SECURITY_FORCE_APPLY, null);
            prop.setProperty(profileName + DOT + CUSTOM_SECURITY_ENABLED, null);
            prop.setProperty(profileName + DOT + CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, null);
            prop.setProperty(profileName + DOT + CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_ID, null);
            prop.setProperty(profileName + DOT + CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_TITLE, null);
            prop.setProperty(profileName + DOT + CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_ID, null);
            prop.setProperty(profileName + DOT + CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_TITLE, null);
            String availableProfiles = prop.getProperty(CUSTOM_SECURITY_PROFILE_NAME);
            prop.setProperty(CUSTOM_SECURITY_PROFILE_NAME, removeProfileName(availableProfiles, profileName));
        }
    }

    private String removeProfileName(String commaSeparatedProfiles, String profileNameToBeRemoved) {
       StringBuilder newProfileSet = new StringBuilder();
       if (!isEmpty(commaSeparatedProfiles)) {
           for (String profile : commaSeparatedProfiles.split(PROP_DELIM)) {
               if (!isEmpty(profile) && !profile.equals(profileNameToBeRemoved)) {
                   newProfileSet.append(profile).append(PROP_DELIM);
               }
           }
       }
       return newProfileSet.toString().replaceAll(",$", "");
   }

    private boolean writeCustomTemplateChange(Map<String, String> changesMap, String profileName, String templateType, Map<String, String> parametersMap, String xmlType) {
        return writeCustomTemplateChange(changesMap, profileName, templateType, false, parametersMap, xmlType);
    }

    private boolean writeCustomTemplateChange(Map<String, String> changesMap, String profileName, String templateType, boolean overwrite, 
    		Map<String, String> parametersMap, String xmlType) {
        File scapTemplateDir = new File(rootDir + File.separator + templateType);
        if (!scapTemplateDir.exists()) {
            scapTemplateDir.mkdir();
        }
        File scapTemplateFile = new File(scapTemplateDir, profileName + ".properties");
        if(((null == changesMap) || (changesMap.size() == 0)) || overwrite) {
            try {
                if (scapTemplateFile.exists()) {
                    scapTemplateFile.delete();
                }
                scapTemplateFile.createNewFile();
            } catch (Exception ed) {
                ed.printStackTrace();
            }
        }

        if(null == changesMap || changesMap.size() == 0) {
            //do nothing...
        } else {
            try {
                ConfigProps customProfile = new ConfigProps(scapTemplateFile);
                for (Map.Entry<String, String> entry : changesMap.entrySet()) {
                    customProfile.setProperty(entry.getKey(), entry.getValue());
                }
                if(null != parametersMap && "oval".equals(xmlType)) {
	                for (Map.Entry<String, String> entry : parametersMap.entrySet()) {
	                    customProfile.setProperty(entry.getKey(), entry.getValue());
	                }
                }
                customProfile.save();
            } catch (Exception ed) {
                ed.printStackTrace();
            }
        }
        return scapTemplateFile.exists();
    }

    private Map<String, String> readProfileChanges(String profileName, String templateType) {
        Map<String, String> props = new HashMap<String, String>(10);
        try {
            ConfigProps customProfile = new ConfigProps(new File(rootDir + File.separator + templateType + File.separator + profileName + ".properties"));
            String[] customProfileProps = customProfile.getPropertyPairs();

            if (customProfileProps != null) {
                for (int i = 0; i < customProfileProps.length; i += 2) {
                    props.put(customProfileProps[i], customProfileProps[i + 1]);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return props;
    }

    private Map<String, String> readProfileSettings(String profileName, String templateType) {
        Map<String, String> props = new HashMap<String, String>();
        ConfigProps config = null;
        String profile = null;
        if ("usgcbtemplates".equals(templateType)) {
            config = new ConfigProps(new File(rootDir, USGCB_SECURITY_FILE_NAME));
            profile = config.getProperty(USGCB_SECURITY_PROFILE_NAME);
        } else if ("scaptemplates".equals(templateType)) {
            config = new ConfigProps(new File(rootDir, SCAP_SECURITY_FILE_NAME));
            profile = config.getProperty(SCAP_SECURITY_PROFILE_NAME);
        } else if ("customtemplates".equals(templateType)) {
            config = new ConfigProps(new File(rootDir, CUSTOM_SECURITY_FILE_NAME));
            profile = config.getProperty(CUSTOM_SECURITY_PROFILE_NAME);
        } else {
            return props;
        }

        if(null != profile && (profile.equals(profileName) || profile.startsWith(profileName + ",") ||
                profile.endsWith("," + profileName) || (profile.indexOf("," + profileName + ",") > -1))) {
            String[] pairs = config.getPropertyPairs();
            for (int i = 0; i < pairs.length;) {
                String key = pairs[i++];
                String value = pairs[i++];
                if (key != null && key.startsWith(profileName+DOT) && value != null) {
                    props.put(key.substring(profileName.length()+1), value);
                }
            }
        }

        props.put("name", profileName);

        return props;
    }

    private HashMap<String,String> getModifiedRulesMap(String modifiedRules, String xmlType) {
        HashMap<String,String> modifiedRulesMap = new HashMap<String,String>();
        if (modifiedRules.indexOf(",") > -1) {
            String[] modifiedRulesSplit = modifiedRules.split(",");
            for (int i=0; i < modifiedRulesSplit.length; i+=2) {
                String key = modifiedRulesSplit[i];
                if ((key == null) || (key.trim().length() < 0)) {
                    continue;
                }
                if (key.endsWith("Chk")) {
                } else if (key.endsWith("Select")) {
                } else if (key.endsWith("Text")) {
                    key = key.substring(0, key.length() - 4) + "Select";
                } else {
                    continue;
                }
                String value = "";
                if ((i + 1) != modifiedRulesSplit.length) {
                    value = modifiedRulesSplit[i + 1];
                }
                if("oval".equals(xmlType)) {
                	int index = key.trim().indexOf("Chk");
                	String modifiedKey = key.trim();
                	if(index > -1) {
                		modifiedKey = modifiedKey.substring(0, index);
                	}
                	modifiedRulesMap.put(modifiedKey, value.trim());
                } else {
                	modifiedRulesMap.put(key.trim(), value.trim());
                }
            }
        }
        return modifiedRulesMap;
    }

    private boolean insertCustomTemplateIntoDB(String contentFileName, String profileNameOld, String profileNameNew, String profileDescNew, String targetType, String templateProperties) {
        boolean inserted = false;
        try {
             String securityManagerXCCDFDir = rootDir + File.separator + "xccdf" + File.separator + targetType + File.separator;
             if ("custom".equals(targetType)) {
                String[] customWindowsXMLs = SCAPUtils.getSCAPUtils().getSupportedCustomWindowsContentsXML();
                String os = "";
                System.out.println("CustomSecurityProfileSaveAction: insertCustomTemplateIntoDB(), contentFileName - " + contentFileName);
                System.out.println("CustomSecurityProfileSaveAction: insertCustomTemplateIntoDB(), securityManagerXCCDFDir - " + securityManagerXCCDFDir);
                if ((customWindowsXMLs != null) && (customWindowsXMLs.length > 0)) {
                    System.out.println("CustomSecurityProfileSaveAction: insertCustomTemplateIntoDB(), customWindowsXMLs - " + Arrays.asList(customWindowsXMLs));
                    if (Arrays.asList(customWindowsXMLs).contains(contentFileName) && new File(securityManagerXCCDFDir + "windows" + File.separator + contentFileName).exists()) {
                        os = "windows";
                    }
                }

                if ("".equals(os)) {
                    String[] customNonWindowsXMLs = SCAPUtils.getSCAPUtils().getSupportedCustomNonWindowsContentsXML();
                    if ((customNonWindowsXMLs != null) && (customNonWindowsXMLs.length > 0)) {
                        System.out.println("CustomSecurityProfileSaveAction: insertCustomTemplateIntoDB(), customNonWindowsXMLs - " + Arrays.asList(customNonWindowsXMLs));
                        if (Arrays.asList(customNonWindowsXMLs).contains(contentFileName) && new File(securityManagerXCCDFDir + "nonwindows" + File.separator + contentFileName).exists()) {
                            os = "nonwindows";
                        }
                    }
                }

                System.out.println("CustomSecurityProfileSaveAction: insertCustomTemplateIntoDB(), os - " + os);
                if ("".equals(os)) {
                return false;
                }
                securityManagerXCCDFDir += os;
             }

             System.out.println("USGCBSecurityProfileSaveAction: insertCustomTemplateIntoDB(), vDeskXCCDFDir - " + securityManagerXCCDFDir);

             XCCDFHandler xccdfHandler = new XCCDFHandler();
             String contentFilePath = securityManagerXCCDFDir + File.separator + contentFileName;
             IConnectionPool connectionPool = main.getConnectionPool();
             System.out.println("USGCBSecurityProfileSaveAction: insertCustomTemplateIntoDB(), connectionPool1 - " + connectionPool);
             if (connectionPool == null) {
                 try {
                     main.getQueryExecutor().run();
                     connectionPool = main.getConnectionPool();
                 } catch (Throwable t) {
                      connectionPool = null;
                 }
                 System.out.println("USGCBSecurityProfileSaveAction: insertCustomTemplateIntoDB(), connectionPool2 - " + connectionPool);
             }
            int result = 1;
            if (connectionPool != null) {
                result = xccdfHandler.addProfileToDB(contentFilePath, profileNameOld, profileNameOld + "_" + profileNameNew, profileDescNew, templateProperties, connectionPool, true);
            }
            System.out.println("USGCBSecurityProfileSaveAction: insertCustomTemplateIntoDB(), result - " + result);
            inserted = (result == 0);
         } catch(Exception ed) {
            ed.printStackTrace();
            inserted = false;
         }
         return inserted;
     }
    private boolean insertCustomOvalTemplateIntoDB(String contentFileName, String profileNameOld, String profileNameNew, String profileDescNew, String targetType, String templateProperties) {
        boolean inserted = false;
        try {
             String securityManagerOVALDir = rootDir + File.separator + "oval" + File.separator + targetType + File.separator;
             if ("custom".equals(targetType)) {
                String[] customWindowsXMLs = SCAPUtils.getSCAPUtils().getSupportedCustomWindowsContentsXML();
                String os = "";
                System.out.println("CustomSecurityProfileSaveAction: insertCustomOvalTemplateIntoDB(), contentFileName - " + contentFileName);
                System.out.println("CustomSecurityProfileSaveAction: insertCustomOvalTemplateIntoDB(), securityManagerXCCDFDir - " + securityManagerOVALDir);
                if ((customWindowsXMLs != null) && (customWindowsXMLs.length > 0)) {
                    System.out.println("CustomSecurityProfileSaveAction: insertCustomOvalTemplateIntoDB(), customWindowsXMLs - " + Arrays.asList(customWindowsXMLs));
                    if (Arrays.asList(customWindowsXMLs).contains(contentFileName) && new File(securityManagerOVALDir + "windows" + File.separator + contentFileName).exists()) {
                        os = "windows";
                    }
                }

                if ("".equals(os)) {
                    String[] customNonWindowsXMLs = SCAPUtils.getSCAPUtils().getSupportedCustomNonWindowsContentsXML();
                    if ((customNonWindowsXMLs != null) && (customNonWindowsXMLs.length > 0)) {
                        System.out.println("CustomSecurityProfileSaveAction: insertCustomOvalTemplateIntoDB(), customNonWindowsXMLs - " + Arrays.asList(customNonWindowsXMLs));
                        if (Arrays.asList(customNonWindowsXMLs).contains(contentFileName) && new File(securityManagerOVALDir + "nonwindows" + File.separator + contentFileName).exists()) {
                            os = "nonwindows";
                        }
                    }
                }

                System.out.println("CustomSecurityProfileSaveAction: insertCustomOvalTemplateIntoDB(), os - " + os);
                if ("".equals(os)) {
                return false;
                }
                securityManagerOVALDir += os;
             }

             System.out.println("USGCBSecurityProfileSaveAction: insertCustomOvalTemplateIntoDB(), vDeskXCCDFDir - " + securityManagerOVALDir);

             OVALHandler ovalHandler = new OVALHandler();
             String contentFilePath = securityManagerOVALDir + File.separator + contentFileName;
             IConnectionPool connectionPool = main.getConnectionPool();
             System.out.println("USGCBSecurityProfileSaveAction: insertCustomOvalTemplateIntoDB(), connectionPool1 - " + connectionPool);
             if (connectionPool == null) {
                 try {
                     main.getQueryExecutor().run();
                     connectionPool = main.getConnectionPool();
                 } catch (Throwable t) {
                      connectionPool = null;
                 }
                 System.out.println("USGCBSecurityProfileSaveAction: insertCustomOvalTemplateIntoDB(), connectionPool2 - " + connectionPool);
             }
            int result = 1;
            if (connectionPool != null) {
                result = ovalHandler.addProfileToDB(contentFilePath, profileNameOld, profileNameOld + "_" + profileNameNew, profileDescNew, templateProperties, connectionPool, true);
            }
            System.out.println("USGCBSecurityProfileSaveAction: insertCustomOvalTemplateIntoDB(), result - " + result);
            inserted = (result == 0);
         } catch(Exception ed) {
            ed.printStackTrace();
            inserted = false;
         }
         return inserted;
     }
    private void loadWindowsContentDetails()throws Exception {
        Map<String, String> scapContentsMap = (Map<String, String>) context.getAttribute("usgcbcontentdetailsmap");
        session.setAttribute("usgcbcontentdetailsmap", scapContentsMap);
        String[] scapContentsXML = SCAPUtils.getSCAPUtils().getSupportedUSGCBContentsXML();
        if (scapContentsXML != null) {
            for (String scapContent : scapContentsXML) {
                Map<String, String> scapContentsXMLMap = (Map<String, String>) context.getAttribute(scapContent);
                session.setAttribute(scapContent, scapContentsXMLMap);
            }
            if (scapContentsXML.length > 0) {
                String firstCustomXML = (String) scapContentsMap.keySet().toArray()[0];
                Map<String, String> scapContentsXMLMap = (Map<String, String>) context.getAttribute(firstCustomXML);
                session.setAttribute("first_usgcb_content_profiles", scapContentsXMLMap);
            } else {
                session.setAttribute("first_usgcb_content_profiles", (new HashMap<String, String>()));
            }

            if (null != scapContentsMap) {
                for (String fileName : scapContentsMap.keySet()) {
                    if (!SCAPUtils.getSCAPUtils().isSyncedOnce(fileName)) {
                        continue;
                    }
                    SCAPBean aBean = new SCAPBean();
                    aBean.setFileName(fileName);
                    aBean.setTitle(scapContentsMap.get(fileName));
                    String assessmentType = SCAPUtils.getSCAPUtils().getAssessmentType(fileName);
                    aBean.setAssessmentType(assessmentType);
                    aBean.setType("standard");
                    aBean.setId(SCAPUtils.getSCAPUtils().getScapContentId(session, "usgcbcontentdetails", fileName));
                    HashMap<String, String> profilesForScapContent = SCAPUtils.getSCAPUtils().getProfilesForScapContent(fileName);
                    aBean.setProfiels(profilesForScapContent);
                    scapBeansListWindows.put(fileName, aBean);
                }
            }

            String props_content_id = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD,USGCB_SECURITY_SCAP_SELECTEDCONTENT_ID));
            String props_content_title = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD,USGCB_SECURITY_SCAP_SELECTEDCONTENT_TITLE));
            String props_content_fileName = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD,USGCB_SECURITY_SCAP_SELECTEDCONTENT_FILENAME));
            String props_profile_id = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD,USGCB_SECURITY_SCAP_SELECTEDPROFILE_ID));
            String props_profile_title = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD,USGCB_SECURITY_SCAP_SELECTEDPROFILE_TITLE));
            String props_custom_template = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD,USGCB_SECURITY_TEMPLATE_NAME));

            String remediationEnabled = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD, USGCB_SECURITY_SCAP_REMEDIATION_ENABLED));
            if (null != remediationEnabled && "true".equals(remediationEnabled)) {
                remediationEnabled = "true";
            } else {
                remediationEnabled = "false";
            }
            //scapForm.setRemediate(remediationEnabled);
            SCAPValueParserBean parserBean = null;
            if (null != props_content_title) {
                Map<String, String> propsToFeed = new Hashtable<String, String>();
                propsToFeed.put("props_content_id", props_content_id);
                propsToFeed.put("props_content_title", props_content_title);
                propsToFeed.put("props_content_fileName", props_content_fileName);
                propsToFeed.put("props_profile_id", props_profile_id);
                propsToFeed.put("props_profile_title", props_profile_title);

                parserBean = new SCAPValueParserBean(propsToFeed);
                Set<String> contentTitles = parserBean.getContentTitles();
                Map<String, Map<String, String>> details = parserBean.getContentDetails();

                for (String contentTitle : contentTitles) {
                    Map<String, String> detail = details.get(contentTitle);
                    String contentId = detail.get("content_id");
                    String contentFileName = detail.get("content_filename");
                    String profileId = detail.get("profile_id");
                    String profileTitle = detail.get("profile_title");
                    SCAPBean aBean = scapBeansListWindows.get(contentFileName);
                    if (null != aBean) {
                        aBean.setSelected("true");
                        aBean.setSelectedProfile(profileId);
                        aBean.setSelectedProfileTitle(profileTitle);
                    }
                }
            }

            loadWindowsCustomContentDetails();

            //custom scanners
            loadStandardCustomContentDetails("windows");
            session.setAttribute("scapBeansListWindows", scapBeansListWindows.values());
        }                System.out.println("AB scapBeansListWindows = "+scapBeansListWindows);
    }

    private void loadWindowsCustomContentDetails() throws Exception {
        System.out.println("loadWindowsCustomContentDetails called");
        Vector<String> scapPropsVector = new Vector<String>();
        Map<String, Map<String, String>> customPropsMap = new HashMap<String, Map<String, String>>();
        DistributionBean distributionBean = new DistributionBean(request);
        Props usgcbProps = distributionBean.getUsgcbProfiles();
        String[] scapProfileStr = distributionBean.getUsgcbProfiles().getPropertyPairs();

        for (int i = 0; i < scapProfileStr.length; i++) {
            if ("name".equals(scapProfileStr[i])) {
                StringTokenizer st = new StringTokenizer(scapProfileStr[i + 1], ",", false);
                // iterate through tokens
                while (st.hasMoreTokens()) {
                    String templateName = st.nextToken(",");
                    System.out.println("Template Name : " + templateName);
                    String fileName = usgcbProps.getProperty(templateName + "." + USGCB_SECURITY_SCAP_SELECTEDCONTENT_FILENAME);
                    String title = usgcbProps.getProperty(templateName + "." + USGCB_SECURITY_SCAP_SELECTEDCONTENT_TITLE);
                    String profileId = usgcbProps.getProperty(templateName + "." + USGCB_SECURITY_SCAP_SELECTEDPROFILE_ID);
                    String profileTitle = usgcbProps.getProperty(templateName + "." + USGCB_SECURITY_SCAP_SELECTEDPROFILE_TITLE);
                    String templateDesc = usgcbProps.getProperty(templateName + "." + USGCB_SECURITY_TEMPLATE_DESC);

                    String[] xmlNamesFromSecurityInfo = SCAPUtils.getSCAPUtils().getSupportedUSGCBContentsXML();
                    String type = "";
                    for (int j=0; j < xmlNamesFromSecurityInfo.length; j++) {
                        if (fileName.equals(xmlNamesFromSecurityInfo[j])) {
                            type = SCAPUtils.getSCAPUtils().getSupportedUSGCBContents()[j];
                            break;
                        }
                    }
                    System.out.println("Type :" + type);
                    System.out.println("Custom template name :" + templateName);
                    System.out.println("Custom template desc :" + templateDesc);
                    System.out.println("Content Title :" + title);
                    System.out.println("Content File Name :" + fileName);
                    System.out.println("Profile Title :" + profileTitle);
                    System.out.println("Profile Id :" + profileId);

                    scapPropsVector.addElement(templateName);
                    Map<String, String> propsMap = new HashMap<String, String>();

                    propsMap.put("type", type);
                    propsMap.put("profileid", profileId);
                    propsMap.put("templatename", templateDesc);
                    propsMap.put("templatedesc", fileName);
                    propsMap.put("filename", fileName);

                    customPropsMap.put(templateName, propsMap);
                    
                    if (scapBeansListWindows.containsKey(fileName)) {
                        SCAPBean aBean = scapBeansListWindows.get(fileName);
                        if (profileId != null && templateName != null) {
                            Map<String, String> profiles =  aBean.getProfiels();
                            profileId = profileId + "@"+ templateName;
                            profiles.put(profileId, templateDesc);
                            aBean.setProfiels(profiles);
                        }
                    } else {
                        System.out.println(fileName + " does not exist");
                    }
                }
                break;
            }
        }

        customizedTemplatesMap.put("windows", customPropsMap);

        String props_content_id = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD,USGCB_SECURITY_SCAP_SELECTEDCONTENT_ID));
        String props_content_title = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD,USGCB_SECURITY_SCAP_SELECTEDCONTENT_TITLE));
        String props_content_fileName = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD,USGCB_SECURITY_SCAP_SELECTEDCONTENT_FILENAME));
        String props_profile_id = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD,USGCB_SECURITY_SCAP_SELECTEDPROFILE_ID));
        String props_profile_title = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD,USGCB_SECURITY_SCAP_SELECTEDPROFILE_TITLE));
        String props_custom_template = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD,USGCB_SECURITY_TEMPLATE_NAME));

        if (null != props_custom_template) {
            Map<String, String> propsToFeed = new Hashtable<String, String>();
            propsToFeed.put("props_content_id", props_content_id);
            propsToFeed.put("props_content_title", props_content_title);
            propsToFeed.put("props_content_fileName", props_content_fileName);
            propsToFeed.put("props_profile_id", props_profile_id);
            propsToFeed.put("props_profile_title", props_profile_title);
            propsToFeed.put("props_custom_template", props_custom_template);

            CustomSCAPValueParserBean parserBean = new CustomSCAPValueParserBean(propsToFeed);
            Set<String> customTemplates = parserBean.getCustomTemplates();
            Map<String, Map<String, String>> details = parserBean.getContentDetails();

            for (String template : customTemplates) {
                Map<String, String> detail = details.get(template);
                String contentId = detail.get("content_id");
                String contentTitle = detail.get("content_title");
                String contentFileName = detail.get("content_filename");
                String profileId = detail.get("profile_id");
                String profileTitle = detail.get("profile_title");
                SCAPBean aBean = scapBeansListWindows.get(contentFileName);
                if (null != aBean) {
                    aBean.setSelected("true");
                    profileId = profileId + "@" + template;
                    aBean.setSelectedProfile(profileId);
                    aBean.setSelectedProfileTitle(template);
                }
            }
        }
    }


    private void loadNonWindowsContentDetails() throws Exception {
        Map<String, String> scapContentsMap = (Map<String, String>) context.getAttribute("scapcontentdetailsmap");
        session.setAttribute("scapcontentdetailsmap", scapContentsMap);
        String[] scapContentsXML = SCAPUtils.getSCAPUtils().getSupportedSCAPContentsXML();
        if (scapContentsXML != null) {
            for (String scapContent : scapContentsXML) {
                Map<String, String> scapContentsXMLMap = (Map<String, String>) context.getAttribute(scapContent);
                session.setAttribute(scapContent, scapContentsXMLMap);
            }
            if (scapContentsXML.length > 0) {
                String firstCustomXML = (String) scapContentsMap.keySet().toArray()[0];
                Map<String, String> scapContentsXMLMap = (Map<String, String>) context.getAttribute(firstCustomXML);
                session.setAttribute("first_scap_content_profiles", scapContentsXMLMap);
            } else {
                session.setAttribute("first_scap_content_profiles", (new HashMap<String, String>()));
            }
        }

        if (null != scapContentsMap) {
            for (String fileName : scapContentsMap.keySet()) {
                if (!SCAPUtils.getSCAPUtils().isSyncedOnce(fileName)) {
                    continue;
                }
                SCAPBean aBean = new SCAPBean();
                aBean.setFileName(fileName);
                String assessmentType = SCAPUtils.getSCAPUtils().getAssessmentType(fileName);
                aBean.setAssessmentType(assessmentType);
                aBean.setTitle(scapContentsMap.get(fileName));
                aBean.setId(SCAPUtils.getSCAPUtils().getScapContentId(session, "scapcontentdetails", fileName));
                aBean.setType("standard");
                HashMap<String, String> profilesForScapContent = SCAPUtils.getSCAPUtils().getProfilesForScapContent(fileName);
                aBean.setProfiels(profilesForScapContent);
                scapBeansListNonWindows.put(fileName, aBean);
            }
        }


        String props_content_id = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD,SCAP_SECURITY_SCAP_SELECTEDCONTENT_ID));
        String props_content_title = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD,SCAP_SECURITY_SCAP_SELECTEDCONTENT_TITLE));
        String props_content_fileName = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD,SCAP_SECURITY_SCAP_SELECTEDCONTENT_FILENAME));
        String props_profile_id = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD,SCAP_SECURITY_SCAP_SELECTEDPROFILE_ID));
        String props_profile_title = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD,SCAP_SECURITY_SCAP_SELECTEDPROFILE_TITLE));
        String props_custom_template = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD,SCAP_SECURITY_TEMPLATE_NAME));
        String remediationEnabled = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_SCAP_REMEDIATION_ENABLED));
        if (null != remediationEnabled && "true".equals(remediationEnabled)) {
            remediationEnabled = "true";
        } else {
            remediationEnabled = "false";
        }
        //scapForm.setRemediate(remediationEnabled);

        if (null != props_content_title) {
            Map<String, String> propsToFeed = new Hashtable<String, String>();
            propsToFeed.put("props_content_id", props_content_id);
            propsToFeed.put("props_content_title", props_content_title);
            propsToFeed.put("props_content_fileName", props_content_fileName);
            propsToFeed.put("props_profile_id", props_profile_id);
            propsToFeed.put("props_profile_title", props_profile_title);

            SCAPValueParserBean parserBean = new SCAPValueParserBean(propsToFeed);
            Set<String> contentTitles = parserBean.getContentTitles();
            Map<String, Map<String, String>> details = parserBean.getContentDetails();

            for (String contentTitle : contentTitles) {
                Map<String, String> detail = details.get(contentTitle);
                String contentId = detail.get("content_id");
                String contentFileName = detail.get("content_filename");
                String profileId = detail.get("profile_id");
                String profileTitle = detail.get("profile_title");
                SCAPBean aBean = scapBeansListNonWindows.get(contentFileName);
                if (null != aBean) {
                    aBean.setSelected("true");
                    aBean.setSelectedProfile(profileId);
                    aBean.setSelectedProfileTitle(profileTitle);
                }
            }
        }

        loadNonWindowsCustomContentDetails();

        //custom scanners
        loadStandardCustomContentDetails("nonwindows");

        session.setAttribute("scapBeansListNonWindows", scapBeansListNonWindows.values());
        System.out.println("AB scapBeansListNonWindows = "+scapBeansListNonWindows);
    }

    private void loadNonWindowsCustomContentDetails() throws Exception {
        Vector<String> scapPropsVector = new Vector<String>();
        Map<String, Map<String, String>> customPropsMap = new HashMap<String, Map<String, String>>();
        DistributionBean distributionBean = new DistributionBean(request);
        Props props = distributionBean.getScapProfiles();
        String[] scapProfileStr = distributionBean.getScapProfiles().getPropertyPairs();

        for (int i = 0; i < scapProfileStr.length; i++) {
            if ("name".equals(scapProfileStr[i])) {
                StringTokenizer st = new StringTokenizer(scapProfileStr[i + 1], ",", false);
                // iterate through tokens
                while (st.hasMoreTokens()) {
                    String templateName = st.nextToken(",");
                    String fileName = props.getProperty(templateName + "." + SCAP_SECURITY_SCAP_SELECTEDCONTENT_FILENAME);
                    String title = props.getProperty(templateName + "." + SCAP_SECURITY_SCAP_SELECTEDCONTENT_TITLE);
                    String profileId = props.getProperty(templateName + "." + SCAP_SECURITY_SCAP_SELECTEDPROFILE_ID);
                    String profileTitle = props.getProperty(templateName + "." + SCAP_SECURITY_SCAP_SELECTEDPROFILE_TITLE);
                    String templateDesc = props.getProperty(templateName + "." + SCAP_SECURITY_TEMPLATE_DESC);

                    String[] xmlNamesFromSecurityInfo = SCAPUtils.getSCAPUtils().getSupportedSCAPContentsXML();
                    String type = "";
                    for (int j=0; j < xmlNamesFromSecurityInfo.length; j++) {
                        if (fileName.equals(xmlNamesFromSecurityInfo[j])) {
                            type = SCAPUtils.getSCAPUtils().getSupportedSCAPContents()[j];
                            break;
                        }
                    }
                    System.out.println("Type :" + type);
                    System.out.println("Content Title :" + title);
                    System.out.println("Content File Name :" + fileName);
                    System.out.println("Template Name :" + templateName);
                    System.out.println("Template Desc :" + templateDesc);
                    System.out.println("Profile Title :" + profileTitle);
                    System.out.println("Profile Id :" + profileId);

                    scapPropsVector.addElement(templateName);
                    Map<String, String> propsMap = new HashMap<String, String>();

                    propsMap.put("type", type);
                    propsMap.put("profileid", profileId);
                    propsMap.put("templatename", templateName);
                    propsMap.put("templatedesc", templateDesc);
                    propsMap.put("filename", fileName);
                    customPropsMap.put(templateName, propsMap);

                    if (scapBeansListNonWindows.containsKey(fileName)) {
                        SCAPBean aBean = scapBeansListNonWindows.get(fileName);
                        if (profileId != null && templateName != null) {
                            Map<String, String> profiles =  aBean.getProfiels();
                            profileId = profileId + "@"+ templateName;
                            profiles.put(profileId, templateDesc);
                            aBean.setProfiels(profiles);
                        }
                    } else {
                        System.out.println(fileName + " does not exist");
                    }
                }
                break;
            }
        }

        customizedTemplatesMap.put("nonwindows", customPropsMap);

        String props_content_id = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD,SCAP_SECURITY_SCAP_SELECTEDCONTENT_ID));
        String props_content_title = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD,SCAP_SECURITY_SCAP_SELECTEDCONTENT_TITLE));
        String props_content_fileName = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD,SCAP_SECURITY_SCAP_SELECTEDCONTENT_FILENAME));
        String props_profile_id = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD,SCAP_SECURITY_SCAP_SELECTEDPROFILE_ID));
        String props_profile_title = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD,SCAP_SECURITY_SCAP_SELECTEDPROFILE_TITLE));
        String props_custom_template = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD,SCAP_SECURITY_TEMPLATE_NAME));
        String remediationEnabled = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_SCAP_REMEDIATION_ENABLED));

        if (null != props_custom_template) {
            Map<String, String> propsToFeed = new Hashtable<String, String>();
            propsToFeed.put("props_content_id", props_content_id);
            propsToFeed.put("props_content_title", props_content_title);
            propsToFeed.put("props_content_fileName", props_content_fileName);
            propsToFeed.put("props_profile_id", props_profile_id);
            propsToFeed.put("props_profile_title", props_profile_title);
            propsToFeed.put("props_custom_template", props_custom_template);

            CustomSCAPValueParserBean parserBean = new CustomSCAPValueParserBean(propsToFeed);
            Set<String> customTemplates = parserBean.getCustomTemplates();
            Map<String, Map<String, String>> details = parserBean.getContentDetails();

            for (String template : customTemplates) {
                Map<String, String> detail = details.get(template);
                String contentId = detail.get("content_id");
                String contentTitle = detail.get("content_title");
                String contentFileName = detail.get("content_filename");
                String profileId = detail.get("profile_id");
                String profileTitle = detail.get("profile_title");
                SCAPBean aBean = scapBeansListNonWindows.get(contentFileName);
                if (null != aBean) {
                    aBean.setSelected("true");
                    profileId = profileId + "@" + template;
                    aBean.setSelectedProfile(profileId);
                    aBean.setSelectedProfileTitle(template);
                    aBean.setTemplateName(template);
                }
            }
        }
    }

    private void loadStandardCustomContentDetails(String platformType) throws Exception {
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
        if (null != customContentsMap) {
            for (String fileName : customContentsMap.keySet()) {
                String platform = SCAPUtils.getSCAPUtils().getScapContentPlatform(session, "customcontentdetails", fileName);
                if (platformType.equals(platform)) {
                    if (!SCAPUtils.getSCAPUtils().isSyncedOnce(fileName)) {
                        continue;
                    }
                    SCAPBean aBean = new SCAPBean();
                    aBean.setFileName(fileName);
                    String assessmentType = SCAPUtils.getSCAPUtils().getAssessmentType(fileName);
                    aBean.setAssessmentType(assessmentType);
                    aBean.setTitle(customContentsMap.get(fileName));
                    aBean.setType("custom");
                    aBean.setId(SCAPUtils.getSCAPUtils().getScapContentId(session, "customcontentdetails", fileName));
                    HashMap<String, String> profilesForScapContent = SCAPUtils.getSCAPUtils().getProfilesForScapContent(fileName);
                    aBean.setProfiels(profilesForScapContent);
                    if (platformType.equals("windows")) {
                        scapBeansListWindows.put(fileName, aBean);
                    } else if (platformType.equals("nonwindows")) {
                        scapBeansListNonWindows.put(fileName, aBean);
                    }
                }
            }
        }

        String props_content_id = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD,CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_ID));
        String props_content_title = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD,CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_TITLE));
        String props_content_fileName = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD,CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_FILENAME));
        String props_profile_id = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD,CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_ID));
        String props_profile_title = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD,CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_TITLE));
        String props_custom_template = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD,CUSTOM_SECURITY_TEMPLATE_NAME));
        String remediationEnabled = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD, CUSTOM_SECURITY_SCAP_REMEDIATION_ENABLED));
        if(null != remediationEnabled && "true".equals(remediationEnabled)) {
            remediationEnabled = "true";
        } else {
            remediationEnabled = "false";
        }
        //customForm.setRemediate(remediationEnabled);
        if (null != props_content_title) {
            Map<String, String> propsToFeed = new Hashtable<String, String>();
            propsToFeed.put("props_content_id", props_content_id);
            propsToFeed.put("props_content_title", props_content_title);
            propsToFeed.put("props_content_fileName", props_content_fileName);
            propsToFeed.put("props_profile_id", props_profile_id);
            propsToFeed.put("props_profile_title", props_profile_title);

            SCAPValueParserBean parserBean = new SCAPValueParserBean(propsToFeed);
            Set<String> contentTitles = parserBean.getContentTitles();
            Map<String, Map<String, String>> details = parserBean.getContentDetails();

            for (String contentTitle : contentTitles) {
                Map<String, String> detail = details.get(contentTitle);
                String contentId = detail.get("content_id");
                String contentFileName = detail.get("content_filename");
                String profileId = detail.get("profile_id");
                String profileTitle = detail.get("profile_title");
                SCAPBean aBean = null;
                if (platformType.equals("windows")) {
                    aBean = scapBeansListWindows.get(contentFileName);
                } else if (platformType.equals("nonwindows")) {
                    aBean = scapBeansListNonWindows.get(contentFileName);
                }
                if (null != aBean) {
                    aBean.setSelected("true");
                    aBean.setSelectedProfile(profileId);
                    aBean.setSelectedProfileTitle(profileTitle);
                }
            }
        }

        loadCustomSCAPContentDetails();
    }

    private void loadCustomSCAPContentDetails() throws Exception {
        Vector<String> scapPropsVector = new Vector<String>();
        Map<String, Map<String, String>> customPropsMap = new HashMap<String, Map<String, String>>();
        DistributionBean distributionBean = new DistributionBean(request);
        Props props = distributionBean.getCustomSecurityPolicies();
        String[] scapProfileStr = distributionBean.getCustomSecurityPolicies().getPropertyPairs();

        for (int i = 0; i < scapProfileStr.length; i++) {
            if ("name".equals(scapProfileStr[i])) {
                StringTokenizer st = new StringTokenizer(scapProfileStr[i + 1], ",", false);
                // iterate through tokens
                while (st.hasMoreTokens()) {
                    String templateName = st.nextToken(",");
                    String fileName = props.getProperty(templateName + "." + CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_FILENAME);
                    String title = props.getProperty(templateName + "." + CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_TITLE);
                    String profileId = props.getProperty(templateName + "." + CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_ID);
                    String profileTitle = props.getProperty(templateName + "." + CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_TITLE);
                    String templateDesc = props.getProperty(templateName + "." + CUSTOM_SECURITY_TEMPLATE_DESC);

                    String[] xmlNamesFromSecurityInfo = SCAPUtils.getSCAPUtils().getSupportedCustomContentsXML();
                    String type = "";
                    for (int j=0; j < xmlNamesFromSecurityInfo.length; j++) {
                        if (fileName.equals(xmlNamesFromSecurityInfo[j])) {
                            type = SCAPUtils.getSCAPUtils().getSupportedCustomContents()[j];
                            break;
                        }
                    }
                    System.out.println("Type :" + type);
                    System.out.println("Content Title :" + title);
                    System.out.println("Content File Name :" + fileName);
                    System.out.println("Template Name :" + templateName);
                    System.out.println("Template Desc :" + templateDesc);
                    System.out.println("Profile Title :" + profileTitle);
                    System.out.println("Profile Id :" + profileId);

                    scapPropsVector.addElement(templateName);
                    Map<String, String> propsMap = new HashMap<String, String>();

                    propsMap.put("type", type);
                    propsMap.put("profileid", profileId);
                    propsMap.put("templatename", templateName);
                    propsMap.put("templatedesc", templateDesc);
                    propsMap.put("filename", fileName);
                    customPropsMap.put(templateName, propsMap);

                    if (scapBeansListWindows.containsKey(fileName)) {
                        SCAPBean aBean = scapBeansListWindows.get(fileName);
                        if (profileId != null && templateName != null) {
                            Map<String, String> profiles =  aBean.getProfiels();
                            profileId = profileId + "@"+ templateName;
                            profiles.put(profileId, templateDesc);
                            aBean.setProfiels(profiles);
                        }
                    } else {
                        System.out.println(fileName + " does not exist");
                    }
                }
                break;
            }
        }

        customizedTemplatesMap.put("custom", customPropsMap);

        String props_content_id = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD,CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_ID));
        String props_content_title = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD,CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_TITLE));
        String props_content_fileName = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD,CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_FILENAME));
        String props_profile_id = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD,CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_ID));
        String props_profile_title = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD,CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_TITLE));
        String props_custom_template = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD,CUSTOM_SECURITY_TEMPLATE_NAME));
        String remediationEnabled = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD, CUSTOM_SECURITY_SCAP_REMEDIATION_ENABLED));

        if (null != props_custom_template) {
            Map<String, String> propsToFeed = new Hashtable<String, String>();
            propsToFeed.put("props_content_id", props_content_id);
            propsToFeed.put("props_content_title", props_content_title);
            propsToFeed.put("props_content_fileName", props_content_fileName);
            propsToFeed.put("props_profile_id", props_profile_id);
            propsToFeed.put("props_profile_title", props_profile_title);
            propsToFeed.put("props_custom_template", props_custom_template);

            CustomSCAPValueParserBean parserBean = new CustomSCAPValueParserBean(propsToFeed);
            Set<String> customTemplates = parserBean.getCustomTemplates();
            Map<String, Map<String, String>> details = parserBean.getContentDetails();

            for (String template : customTemplates) {
                Map<String, String> detail = details.get(template);
                String contentId = detail.get("content_id");
                String contentTitle = detail.get("content_title");
                String contentFileName = detail.get("content_filename");
                String profileId = detail.get("profile_id");
                String profileTitle = detail.get("profile_title");
                SCAPBean aBean = scapBeansListWindows.get(contentFileName);
                if (null != aBean) {
                    profileId = profileId + "@" + template;
                    aBean.setSelected("true");
                    aBean.setSelectedProfile(profileId);
                    aBean.setSelectedProfileTitle(template);
                    aBean.setTemplateName(template);
                }
            }
        }
    }

    private void setValue(ISubscription sub, Vector scapProps, String key, String value, String priority, String type) throws SystemException {
        if (value != null) {
            if(priority != null && !("".equals(priority)))  {
            	if("N/A".equalsIgnoreCase(priority)) {
            		priority = "99999";
            	}
                value = value + PROP_DELIM + Integer.parseInt(priority);
            }
        }
        if ("windows".equals(type)) {
            sub.setProperty(PROP_USGCB_SECURITY_KEYWORD, key, value);
            scapProps.add(PROP_USGCB_SECURITY_KEYWORD + PROP_DELIM + key + "=" + value);
        }
        if ("nonwindows".equals(type)) {
            sub.setProperty(PROP_SCAP_SECURITY_KEYWORD, key, value);
            scapProps.add(PROP_SCAP_SECURITY_KEYWORD + PROP_DELIM + key + "=" + value);
        }
        if ("custom".equals(type)) {
            sub.setProperty(PROP_CUSTOM_SECURITY_KEYWORD, key, value);
            scapProps.add(PROP_CUSTOM_SECURITY_KEYWORD + PROP_DELIM + key + "=" + value);
        }
    }
}