// Copyright 1997-2017, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.securitymgr.view.CustomSCAPValueParserBean;
import com.marimba.apps.securitymgr.view.SCAPBean;
import com.marimba.apps.securitymgr.view.SCAPValueParserBean;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscriptionmanager.webapp.forms.CustomSecurityForm;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.SCAPUtils;
import com.marimba.intf.util.IConfig;
import com.marimba.tools.config.ConfigProps;
import com.marimba.tools.util.Props;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.json.simple.JSONValue;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * This action is called when visiting the open Custom setting page. It reads the Custom option and populates the form for display.
 *
 * @author Selvaraj Jegatheesan
 */
public final class CustomSecurityLoadAction extends AbstractAction {
    HttpSession session;
    HttpServletRequest request;
    Map<String, SCAPBean> scapBeansList = new HashMap<String, SCAPBean>();
    Map<String, SCAPBean> scapBeansListCustom = new HashMap<String, SCAPBean>();

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws ServletException {

        this.session = request.getSession();
        this.request = request;

        System.out.println("Custom setting loading action ...");
        ISubscription sub = null;
        IConfig prop = new ConfigProps(null);

        CustomSecurityForm customForm = ((CustomSecurityForm) form);
        try {
            init(request);

            loadStandardCustomContentDetails();
            String selectedProfileName = (String) request.getParameter("selectedprofilename");
            String selectedType = (String) request.getParameter("type");
            System.out.println("selected Profile Name :" + selectedProfileName);
            System.out.println("selected type :" + selectedType);
            loadCustomSCAPContentDetails();

            if (null != selectedProfileName && null != selectedType) {
                customForm.setSelectedSCAPOption(selectedType);
                if("standard".equals(selectedType)) {
                    customForm.setSelectedStandardProfile(selectedProfileName);
                } else {
                    customForm.setSelectedProfile(selectedProfileName);
                }
            } else {
                sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);

                String scapOption = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD, CUSTOM_SECURITY_OPTION));
                String priorityValue = getPriorityValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD, CUSTOM_SECURITY_OPTION));
                priorityValue = (null == priorityValue || "".equals(priorityValue)) ? NOTAVBLE : priorityValue;

                if(null != scapOption && "customize".equalsIgnoreCase(scapOption)) {
                	customForm.setCustomizePriorityValue(priorityValue);
                } else {
                	customForm.setStandardPriorityValue(priorityValue);
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
                customForm.setRemediate(remediationEnabled);

                if(null == scapOption || "".equals(scapOption.trim()) || "false".equalsIgnoreCase(scapOption)) {
                    System.out.println("no security option avaialble");
                    customForm.setSelectedSCAPOption("exclude");
                    customForm.initialize();
                } else if("exclude".equalsIgnoreCase(scapOption)) {
                    System.out.println("exclude option selected");
                    customForm.setSelectedSCAPOption("exclude");
                    customForm.initialize();
                } else  if("customize".equalsIgnoreCase(scapOption)) {
                    System.out.println("selected customize security option avaialble");
                    customForm.setSelectedSCAPOption("customize");
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
                            SCAPBean aBean = scapBeansListCustom.get(template);
                            if (null != aBean) {
                                aBean.setSelected("true");
                                aBean.setSelectedProfile(profileId);
                                aBean.setSelectedProfileTitle(profileTitle);
                            }
                        }
                    } else {
                        System.out.println("no security profile avaialble");
                        customForm.setSelectedSCAPOption("false");
                        customForm.initialize();
                    }
                } else if("standard".equalsIgnoreCase(scapOption)){
                    System.out.println("selected standard security option avaialble");
                    customForm.setSelectedSCAPOption("standard");

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
                            SCAPBean aBean = scapBeansList.get(contentFileName);
                            if (null != aBean) {
                                aBean.setSelected("true");
                                aBean.setSelectedProfile(profileId);
                                aBean.setSelectedProfileTitle(profileTitle);
                            }
                        }
                    } else {
                        System.out.println("no standard security profile avaialble");
                        customForm.setSelectedSCAPOption("false");
                        customForm.initialize();
                    }
                } else {
                    System.out.println("no security option avaialble");
                    customForm.setSelectedSCAPOption("false");
                    customForm.initialize();
                }
            }
        } catch (SystemException e) {
            throw new GUIException(e);
        }
        // check if user clicked preview
        String action = request.getParameter("action");
        if ("preview".equals(action)) {
            Map<String, String> previewList = new HashMap<String, String>();
            Collection<SCAPBean> beans = Collections.EMPTY_SET;
            if ("standard".equals(customForm.getSelectedSCAPOption())) {
                beans = scapBeansList.values();
            } else if ("customize".equals(customForm.getSelectedSCAPOption())) {
                beans =  scapBeansListCustom.values();
            }
            for (SCAPBean aBean : beans) {
                if ("true".equals(aBean.getSelected())) {
                    previewList.put(aBean.getTitle(), aBean.getSelectedProfileTitle());
                }
            }
            customForm.setPreviewList(previewList);
            return mapping.findForward("preview");
        }
        return (mapping.findForward("success"));
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
        if (null != customContentsMap) {
            for (String fileName : customContentsMap.keySet()) {
                SCAPBean aBean = new SCAPBean();
                aBean.setFileName(fileName);
                aBean.setTitle(customContentsMap.get(fileName));
                aBean.setId(SCAPUtils.getSCAPUtils().getScapContentId(session, "customcontentdetails", fileName));
                HashMap<String, String> profilesForScapContent = SCAPUtils.getSCAPUtils().getProfilesForScapContent(fileName);
                aBean.setProfiels(profilesForScapContent);
                scapBeansList.put(fileName, aBean);
            }
        }
        session.setAttribute("scapBeansList", scapBeansList.values());
    }

    private void loadCustomSCAPContentDetails() {
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
                    System.out.println("Profile Title :" + profileTitle);
                    System.out.println("Profile Id :" + profileId);

                    scapPropsVector.addElement(templateName);
                    Map<String, String> propsMap = new HashMap<String, String>();

                    propsMap.put("type", type);
                    propsMap.put("profileid", profileId);
                    propsMap.put("templatename", templateName);
                    propsMap.put("filename", fileName);
                    customPropsMap.put(templateName, propsMap);

                    SCAPBean aBean = new SCAPBean();
                    aBean.setType(type);
                    aBean.setFileName(fileName);
                    aBean.setTitle(title);
                    aBean.setTemplateName(templateName);
                    aBean.setId(profileId);
                    HashMap<String, String> profilesDetails = new HashMap<String, String>(1);
                    profilesDetails.put(profileId, profileTitle);
                    aBean.setProfiels(profilesDetails);
                    scapBeansListCustom.put(templateName, aBean);
                }
                break;
            }
        }
        if (scapPropsVector.isEmpty()) {
            scapPropsVector.addElement("Select");
        }
        session.setAttribute("customprofiles", scapPropsVector);
        session.setAttribute("scapBeansListCustom", scapBeansListCustom.values());
        session.setAttribute("customprofilespropsmap", JSONValue.toJSONString(customPropsMap));
    }
}
