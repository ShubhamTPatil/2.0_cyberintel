// Copyright 2019, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.Utils;
import com.marimba.apps.subscriptionmanager.webapp.util.SCAPUtils;
import com.marimba.apps.subscriptionmanager.webapp.forms.SecurityProfileReportForm;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;
import com.marimba.apps.subscriptionmanager.compliance.view.ComplianceSummaryBean;
import com.marimba.apps.subscriptionmanager.compliance.view.PowerSummaryBean;
import com.marimba.apps.subscriptionmanager.compliance.core.ComplianceMain;
import com.marimba.apps.subscriptionmanager.compliance.service.TargetViewService;
import com.marimba.apps.subscriptionmanager.TenantAttributes;
import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.beans.SecurityGroupBean;
import com.marimba.apps.subscriptionmanager.beans.*;
import com.marimba.apps.subscriptionmanager.users.UserManager;

import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.securitymgr.compliance.SecurityCompliance;
import com.marimba.apps.securitymgr.compliance.SecurityVulnerCompliance;
import com.marimba.apps.securitymgr.utils.SecurityLDAPUtils;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.CriticalException;
import com.marimba.intf.msf.acl.IAclConstants;
import com.marimba.tools.util.DebugFlag;

import org.json.JSONObject;
import org.json.JSONException;

import com.marimba.oval.util.xml.profiles.*;
import com.marimba.oval.util.*;
import com.marimba.xccdf.util.xml.groups.*;
import com.marimba.xccdf.util.*;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;
import java.util.*;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Elaiyaraja Thangavel
 * Date: May 12, 2005
 * Time: 4:49:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class SecurityProfileReportAction
        extends AbstractAction implements IWebAppConstants, ISubscriptionConstants, ComplianceConstants {
     final static int DEBUG = DebugFlag.getDebug("SECURITY/REPORT");
     XCCDFHandler xccdfHandler = null;
    OVALHandler ovalHandler = null;
    /**
     * @param mapping REMIND
     * @param form REMIND
     * @param request REMIND
     * @param response REMIND
     *
     * @return REMIND
     */

    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
        return new SecurityProfileReportAction.CompTgtViewTask(mapping, form, request, response);
    }

    protected class CompTgtViewTask extends SubscriptionDelayedTask {
        CompTgtViewTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
            super(mapping, form, request, response);
        }

        public void execute(){

            init(request);
            GUIUtils.initForm(request, mapping);

            try {
                GUIUtils.setToSession(request, "context", "compTarget");
            } catch (SystemException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            SecurityProfileReportForm securityProfileReportForm = (SecurityProfileReportForm) form;
            xccdfHandler = new XCCDFHandler();
            ovalHandler = new OVALHandler();
            String view = request.getParameter( "view" );
            String results = request.getParameter( "results" );
            String contentId = securityProfileReportForm.getContentId();
            String xmlType = securityProfileReportForm.getXmlType();
            if( view == null ){
                view = "target";
            }
            if( contentId == null ){
                contentId = (request.getParameter("contentId") != null) ? request.getParameter("contentId") : SECURITY_SCAN_TYPE_XCCDF;
            }
            if( xmlType == null ){
                xmlType = (request.getParameter("xmlType") != null) ? request.getParameter("xmlType") : SECURITY_SCAN_TYPE_XCCDF;
            }

            debug(1, "view is "+view );

            HttpSession session = request.getSession();
            Target target = null;
            try {
                target = getTarget(request, main);

                if( target == null ) {
                    target = ( Target )session.getAttribute( "target" );
                } else {
                    session.setAttribute( "target", target );
                }

                List display_results = null;
                ComplianceSummaryBean summaryResult = null;
                PowerSummaryBean pwrSumBean = null;

                if( target != null ){
                    // Prepare target id for javascript
                    String targetId = (target.getId() == null)? "" : target.getId().toLowerCase();
                    targetId = "all".equalsIgnoreCase(targetId) ? "all_all" : targetId;
                    GUIUtils.setToSession(request, "comp_target_id", targetId);
                    GUIUtils.setToSession(request, "target", (Object) target);
                    ServletContext ctx = session.getServletContext();
                    String tenantName = (String) session.getAttribute(SESSION_TENANTNAME);
                    TenantAttributes tenantAttr = UserManager.getTenantAttr(tenantName);
                    ComplianceMain cm = tenantAttr.getCompMain();
                    IUser user = null;
                    try {
                        user = GUIUtils.getUser(request);

                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    boolean hasReadPermission = hasPolicyReadPermission(target, user);
                    if( hasReadPermission ){
                        session.setAttribute( IWebAppConstants.NO_ACL_PERMISSION, "false" );
                    } else{
                        session.setAttribute( IWebAppConstants.NO_ACL_PERMISSION, "true" );
                    }

                    TargetViewService targetService = (TargetViewService)
                    session.getAttribute(ComplianceConstants.TARGET_VIEW_SERVICE);
                    if( !hasReadPermission ) {
                        display_results = new ArrayList();
                        summaryResult = new ComplianceSummaryBean();
                        session.setAttribute(IWebAppConstants.SESSION_COMP_HASPOLICIES, "false");
                        session.removeAttribute(IWebAppConstants.SESSION_POLICY_LASTUPDATED);
                    } else if( target.getType().equals( TYPE_MACHINE )) {
                        display_results = SecurityLDAPUtils.getAssignedPolicy(xmlType, target.getName(), target.getType(), target.getId(), main, user);
                        if( display_results != null && display_results.size() > 0 ) {
                            for (int i = 0; i<display_results.size(); i++) {
                                SecurityTargetDetailsBean securityTargetDetailsBean = (SecurityTargetDetailsBean) display_results.get(i);
                                debug(1, "Assigned Target :" + securityTargetDetailsBean.getAssginedToName());
                                if (contentId != null && contentId.equals(securityTargetDetailsBean.getSelectedContentId())) {
                                    debug(1, "Content Id: " + contentId);
                                    SecurityProfileDetailsBean securityProfileDetailsBean = new SecurityProfileDetailsBean();
                                    securityProfileDetailsBean.setAssignedToID(securityTargetDetailsBean.getAssignedToID());
                                    securityProfileDetailsBean.setTargetID(securityTargetDetailsBean.getTargetID());
                                    securityProfileDetailsBean.setTargetName(securityTargetDetailsBean.getTargetName());
                                    securityProfileDetailsBean.setProfileName(securityTargetDetailsBean.getSelectedProfileId());
                                    securityProfileDetailsBean.setProfileTitle(securityTargetDetailsBean.getSelectedProfileTitle());
                                    securityProfileDetailsBean.setContentName(securityTargetDetailsBean.getSelectedContentId());
                                    securityProfileDetailsBean.setContentFileName(securityTargetDetailsBean.getSelectedContentFileName());
                                    securityProfileDetailsBean.setContentTitle(securityTargetDetailsBean.getSelectedSecurityContentName());
                                    securityProfileDetailsBean.setCategoryType(securityTargetDetailsBean.getCategoryType());
                                    String templateName = securityTargetDetailsBean.getCustomTemplateName();
                                    securityProfileDetailsBean.setTemplateName(templateName);
                                    if (SECURITY_SCAN_TYPE_OVAL.equals(xmlType)) {
                                        new SecurityVulnerCompliance.GetProfileComplianceReport(main,securityProfileDetailsBean);
                                    } else {
                                        new SecurityCompliance.GetProfileComplianceReport(main,securityProfileDetailsBean);
                                    }
                                    if (templateName != null && !"".equals(templateName.trim())) {
                                        debug(1, "Template Name: " + templateName);
                                        securityProfileDetailsBean.setProfileTitle(templateName);
                                    }
                                    if (SECURITY_SCAN_TYPE_OVAL.equals(xmlType)) {
                                        SecurityOvalGeneralDetailsBean securityOvalGeneralDetailsBean = new SecurityOvalGeneralDetailsBean();
                                        updateOvalComplianceStatus(securityProfileDetailsBean, securityOvalGeneralDetailsBean);
                                        debug(5, "updateOvalComplianceStatus :: securityOvalGeneralDetailsBean - " + securityOvalGeneralDetailsBean);
                                        securityProfileReportForm.setSecurityOvalGeneralDetailsBean(securityOvalGeneralDetailsBean);
                                    } else {
                                        updateXccdfComplianceStatus(securityProfileDetailsBean);
                                    }
                                    debug(5, "updateOvalComplianceStatus :: securityProfileDetailsBean - " + securityProfileDetailsBean);
                                    securityProfileReportForm.setSecurityProfileDetailsBean(securityProfileDetailsBean);
                                    break;
                                }
                            }
                        }
                    }
                }
                forward = mapping.findForward("success_" + xmlType);
            } catch (SystemException se) {
                guiException = new GUIException( se );
                forward = mapping.findForward("failure");
            } catch ( Exception exp ){
                exp.printStackTrace();
                guiException = new GUIException( ( new CriticalException ( exp.getMessage() ) ) );
                forward = mapping.findForward("failure");
            }
            Object bean = session.getAttribute(IWebAppsConstants.INITERROR_KEY);

            if ((bean != null) && bean instanceof Exception) {
                //remove initerror from the session because it has served its purpose
                debug(1, "Critical Exception found " + bean.toString());
                session.removeAttribute(IWebAppsConstants.INITERROR_KEY);
                forward = mapping.findForward("failure");
            }
        }
    }

    private void updateXccdfComplianceStatus(SecurityProfileDetailsBean securityProfileDetailsBean) {
        try {
            String rulesJSONResult = securityProfileDetailsBean.getRulesJSONResult();
            HashMap<String, String> rulesResultMap = new HashMap<String, String>();
            if (rulesJSONResult != null) {
                JSONObject json = new JSONObject(rulesJSONResult);
                Map<String, Object> map = Utils.jsonObjectToMap(json);
                Map<String, Object> rulesComplianceMap = (Map<String, Object>)map.get("rules_compliance");
                Iterator itr = rulesComplianceMap.keySet().iterator();
                while (itr.hasNext()) {
                    String ruleName = (String)itr.next();
                    String ruleResult = (String) rulesComplianceMap.get(ruleName);
                    rulesResultMap.put(ruleName, ruleResult);
                }
            }
            securityProfileDetailsBean.setRulesComplianceMap(rulesResultMap);

            //retrieve group details from scap xml
            String categoryType  =  securityProfileDetailsBean.getCategoryType();
            String contentFileName = securityProfileDetailsBean.getContentFileName();
            SCAPContentDetails contentDetails = null;
            if ("scap".equals(categoryType)) {
                contentDetails = SCAPUtils.getSCAPUtils().getScapContentDetails("scapcontentdetails", contentFileName);
            }
            if ("usgcb".equals(categoryType)) {
                contentDetails = SCAPUtils.getSCAPUtils().getScapContentDetails("usgcbcontentdetails", contentFileName);
            }
            if ("custom".equals(categoryType)) {
                contentDetails = SCAPUtils.getSCAPUtils().getScapContentDetails("customcontentdetails", contentFileName);
            }
            if (contentDetails != null) {
                List<XCCDFGroup> groups = contentDetails.getGroups();
                String xmlFile = contentDetails.getXccdfFileLocation() + File.separator + contentFileName;
                String customizedContentFilePath = null;
                String templateName = securityProfileDetailsBean.getTemplateName();
                if (templateName != null && !"".equals(templateName.trim())) {
                    customizedContentFilePath = xmlFile.substring(0, xmlFile.indexOf(".xml")) + "_"+ templateName+".xml";
                    String templatePath = getTemplatePath(categoryType, templateName);
                    if (0 == xccdfHandler.getCustomizedXml(xmlFile, securityProfileDetailsBean.getProfileName(), templatePath, customizedContentFilePath, true)) {
                         xmlFile =  customizedContentFilePath;
                    }
                }
                HashMap<String, Object> rulesValueMap = SCAPUtils.getSCAPUtils().getValues(xmlFile, securityProfileDetailsBean.getProfileName());
                securityProfileDetailsBean.setRulesValueMap(rulesValueMap);
                ArrayList<SecurityGroupBean> groupsDetails = getGroupsDetails(groups, securityProfileDetailsBean);
                securityProfileDetailsBean.setGroups(groupsDetails);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateOvalComplianceStatus(SecurityProfileDetailsBean securityProfileDetailsBean, SecurityOvalGeneralDetailsBean securityOvalGeneralDetailsBean) {
        try {
            String rulesJSONResult = securityProfileDetailsBean.getRulesJSONResult();
            HashMap<String, String> rulesResultMap = new HashMap<String, String>();
            if (rulesJSONResult != null) {
                JSONObject json = new JSONObject(rulesJSONResult);
                Map<String, Object> map = Utils.jsonObjectToMap(json);
                Map<String, Object> rulesComplianceMap = (Map<String, Object>)map.get("rules_compliance");
                Iterator itr = rulesComplianceMap.keySet().iterator();
                while (itr.hasNext()) {
                    String ruleName = (String)itr.next();
                    String ruleResult = (String) rulesComplianceMap.get(ruleName);
                    rulesResultMap.put(ruleName, ruleResult);
                }
            }
            securityProfileDetailsBean.setRulesComplianceMap(rulesResultMap);

            //retrieve group details from scap xml
            String categoryType  =  securityProfileDetailsBean.getCategoryType();
            String contentFileName = securityProfileDetailsBean.getContentFileName();
            SCAPContentDetails contentDetails = null;
            if ("scap".equals(categoryType)) {
                contentDetails = SCAPUtils.getSCAPUtils().getScapContentDetails("scapcontentdetails", contentFileName);
            }
            if ("usgcb".equals(categoryType)) {
                contentDetails = SCAPUtils.getSCAPUtils().getScapContentDetails("usgcbcontentdetails", contentFileName);
            }
            if ("custom".equals(categoryType)) {
                contentDetails = SCAPUtils.getSCAPUtils().getScapContentDetails("customcontentdetails", contentFileName);
            }
            if (contentDetails != null) {
                securityOvalGeneralDetailsBean.setProductVersion(contentDetails.getOvalProductVersion());
                securityOvalGeneralDetailsBean.setProductName(contentDetails.getOvalProductName());
                securityOvalGeneralDetailsBean.setModuleName(contentDetails.getOvalModuleName());
                securityOvalGeneralDetailsBean.setTimestamp(contentDetails.getOvalTimestamp());
                securityOvalGeneralDetailsBean.setGeneratorSchemaVersion(contentDetails.getOvalGeneratorSchemaVersion());
                securityOvalGeneralDetailsBean.setGeneratorProductVersion(contentDetails.getOvalGeneratorProductVersion());
                securityOvalGeneralDetailsBean.setGeneratorProductName(contentDetails.getOvalGeneratorProductName());
                securityOvalGeneralDetailsBean.setGeneratorTimestamp(contentDetails.getOvalGeneratorTimestamp());
                securityOvalGeneralDetailsBean.setGeneratorContentVersion(contentDetails.getOvalGeneratorContentVersion());
                securityOvalGeneralDetailsBean.setTotalDefinitions(contentDetails.getOvalTotalDefinitions());
                securityOvalGeneralDetailsBean.setTotalComplianceDefinitions(contentDetails.getOvalTotalComplianceDefinitions());
                securityOvalGeneralDetailsBean.setTotalInventoryDefinitions(contentDetails.getOvalTotalInventoryDefinitions());
                securityOvalGeneralDetailsBean.setTotalMiscellaneousDefinitions(contentDetails.getOvalTotalMiscellaneousDefinitions());
                securityOvalGeneralDetailsBean.setTotalPatchDefinitions(contentDetails.getOvalTotalPatchDefinitions());
                securityOvalGeneralDetailsBean.setTotalVulnerabilityDefinitions(contentDetails.getOvalTotalVulnerabilityDefinitions());
                securityOvalGeneralDetailsBean.setTotalTests(contentDetails.getOvalTotalTests());
                securityOvalGeneralDetailsBean.setTotalObjects(contentDetails.getOvalTotalObjects());
                securityOvalGeneralDetailsBean.setTotalStates(contentDetails.getOvalTotalStates());
                securityOvalGeneralDetailsBean.setTotalVariables(contentDetails.getOvalTotalVariables());

                ArrayList<SecurityOvalDefinitionDetailsBean> securityOvalDefinitionDetailsBeans = new ArrayList<SecurityOvalDefinitionDetailsBean>();
                int totalPass = 0, totalFail = 0, totalError = 0, totalUnknown = 0, totalOther = 0;
                int totalDefinitions = 0, totalComplianceDefinitions = 0, totalInventoryDefinitions = 0, totalMiscellaneousDefinitions = 0, totalPatchDefinitions = 0, totalVulnerabilityDefinitions = 0;
                List<SCAPProfileDetails> scapProfileDetails = contentDetails.getProfileDetails();
                for (SCAPProfileDetails scapProfileDetail : scapProfileDetails) {
                    if (scapProfileDetail.getTitle().equals(securityProfileDetailsBean.getProfileTitle())) {
                        ArrayList<OVALProfileDefinition> ovalProfileDefinitions = scapProfileDetail.getDefinitions();
                        for (OVALProfileDefinition ovalProfileDefinition : ovalProfileDefinitions) {
                            String currentId = ovalProfileDefinition.getId();
                            if (rulesResultMap.get(currentId) != null) {
                                SecurityOvalDefinitionDetailsBean securityOvalDefinitionDetailsBean = new SecurityOvalDefinitionDetailsBean();
                                securityOvalDefinitionDetailsBean.setId(ovalProfileDefinition.getId());
                                securityOvalDefinitionDetailsBean.setTitle(ovalProfileDefinition.getTitle());
                                securityOvalDefinitionDetailsBean.setClassType(ovalProfileDefinition.getClassType());
                                ArrayList<OVALProfileDefinitionReference> references = ovalProfileDefinition.getReferences();
                                String currentRefId = "";
                                if (references != null) {
                                    for (OVALProfileDefinitionReference reference : references) {
                                        currentRefId += (("".equals(currentRefId)) ? "" : ", ");
                                        currentRefId += "[<a class=\"Hover\" target=\"_blank\" href=\"" + reference.getUrl() + "\">";
                                        currentRefId += reference.getName();
                                        currentRefId += "</a>]";
                                        securityOvalDefinitionDetailsBean.addReference(reference.getName(), reference.getUrl());
                                    }
                                }
                                securityOvalDefinitionDetailsBean.setRefId(currentRefId);
                                if ("fail".equals(rulesResultMap.get(currentId))) {
                                    totalFail++;
                                    securityOvalGeneralDetailsBean.addSecurityOvalFailDefinitionDetailsBeans(securityOvalDefinitionDetailsBean);
                                    if ("inventory".equals(ovalProfileDefinition.getClassType()) || "compliance".equals(ovalProfileDefinition.getClassType())) {
                                        securityOvalDefinitionDetailsBean.setResult("NOT-INSTALLED");
                                    } else {
                                        securityOvalDefinitionDetailsBean.setResult("VULNERABLE");
                                    }
                                } else if ("pass".equals(rulesResultMap.get(currentId))) {
                                    totalPass++;
                                    securityOvalGeneralDetailsBean.addSecurityOvalPassDefinitionDetailsBeans(securityOvalDefinitionDetailsBean);
                                    if ("inventory".equals(ovalProfileDefinition.getClassType()) || "compliance".equals(ovalProfileDefinition.getClassType())) {
                                        securityOvalDefinitionDetailsBean.setResult("INSTALLED");
                                    } else {
                                        securityOvalDefinitionDetailsBean.setResult("NON-VULNERABLE");
                                    }
                                } else if ("error".equals(rulesResultMap.get(currentId))) {
                                    totalError++;
                                    securityOvalGeneralDetailsBean.addSecurityOvalErrorDefinitionDetailsBeans(securityOvalDefinitionDetailsBean);
                                    securityOvalDefinitionDetailsBean.setResult("ERROR");
                                } else if ("unknown".equals(rulesResultMap.get(currentId))) {
                                    totalUnknown++;
                                    securityOvalGeneralDetailsBean.addSecurityOvalUnknownDefinitionDetailsBeans(securityOvalDefinitionDetailsBean);
                                    securityOvalDefinitionDetailsBean.setResult("UNKNOWN");
                                } else {
                                    totalOther++;
                                    securityOvalGeneralDetailsBean.addSecurityOvalOtherDefinitionDetailsBeans(securityOvalDefinitionDetailsBean);
                                    securityOvalDefinitionDetailsBean.setResult(rulesResultMap.get(currentId).toUpperCase());
                                }
                                securityOvalGeneralDetailsBean.addSecurityOvalDefinitionDetailsBeans(securityOvalDefinitionDetailsBean);

                                totalDefinitions++;
                                if ("compliance".equals(ovalProfileDefinition.getClassType())) {
                                    totalComplianceDefinitions++;
                                } else if ("inventory".equals(ovalProfileDefinition.getClassType())) {
                                    totalInventoryDefinitions++;
                                } else if ("patch".equals(ovalProfileDefinition.getClassType())) {
                                    totalPatchDefinitions++;
                                } else if ("vulnerability".equals(ovalProfileDefinition.getClassType())) {
                                    totalVulnerabilityDefinitions++;
                                } else {
                                    totalMiscellaneousDefinitions++;
                                }
                            }
                        }
                        break;
                    }
                }
                securityOvalGeneralDetailsBean.setTotalPass(totalPass);
                securityOvalGeneralDetailsBean.setTotalFail(totalFail);
                securityOvalGeneralDetailsBean.setTotalError(totalError);
                securityOvalGeneralDetailsBean.setTotalUnknown(totalUnknown);
                securityOvalGeneralDetailsBean.setTotalOther(totalOther);
                securityOvalGeneralDetailsBean.setTotalDefinitions(totalDefinitions);
                securityOvalGeneralDetailsBean.setTotalComplianceDefinitions(totalComplianceDefinitions);
                securityOvalGeneralDetailsBean.setTotalInventoryDefinitions(totalInventoryDefinitions);
                securityOvalGeneralDetailsBean.setTotalMiscellaneousDefinitions(totalMiscellaneousDefinitions);
                securityOvalGeneralDetailsBean.setTotalPatchDefinitions(totalPatchDefinitions);
                securityOvalGeneralDetailsBean.setTotalVulnerabilityDefinitions(totalVulnerabilityDefinitions);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getTemplatePath(String type, String templateName) {
        String templatePath = null;
        String dataDir = main.getDataDirectory().getAbsolutePath();
        if("custom".equals(type)) {
            templatePath = new File(dataDir + File.separator + "customtemplates" + File.separator + templateName).getAbsolutePath() + ".properties";
        } else if("usgcb".equals(type)) {
            templatePath = new File(dataDir + File.separator + "usgcbtemplates" + File.separator + templateName).getAbsolutePath() + ".properties";
        } else if("scap".equals(type)) {
            templatePath = new File(dataDir + File.separator + "scaptemplates" + File.separator + templateName).getAbsolutePath() + ".properties";
        }
        return templatePath;
    }
    private ArrayList<SecurityGroupBean> getGroupsDetails(List<XCCDFGroup> xccdfGroups, SecurityProfileDetailsBean securityProfileDetailsBean) throws Exception {
        ArrayList<SecurityGroupBean> groups = new ArrayList<SecurityGroupBean>();
        for (int i=0;i<xccdfGroups.size();i++) {
            XCCDFGroup group =  xccdfGroups.get(i);
            if (group.getGroups().size() >  0 ) {
                SecurityGroupBean groupBean = getGroupDetails(group, securityProfileDetailsBean);
                if (!"Introduction".equals(groupBean.getTitle())) {
                        groups.add(groupBean);
                        loadSubGroupsDetails(groupBean, group, securityProfileDetailsBean);
                }
            } else {
                SecurityGroupBean groupBean = getGroupDetails(group, securityProfileDetailsBean);
                if (!"Introduction".equals(groupBean.getTitle())) {
                    if (groupBean.getRulesCount() != 0) {
                        groups.add(groupBean);
                    }
                }
            }
        }
        return groups;
    }

    private void loadSubGroupsDetails(SecurityGroupBean parentGroup, XCCDFGroup group, SecurityProfileDetailsBean securityProfileDetailsBean) throws Exception {
        ArrayList<XCCDFGroup> xccdfGroups = (ArrayList<XCCDFGroup>)group.getGroups();
        for (int i=0;i<xccdfGroups.size();i++) {
            group =  xccdfGroups.get(i);
            if (group.getGroups().size() >  0 ) {
                SecurityGroupBean groupBean = getGroupDetails(group, securityProfileDetailsBean);
                parentGroup.getGroups().add(groupBean);
                loadSubGroupsDetails(groupBean, group, securityProfileDetailsBean);
            } else {
                SecurityGroupBean groupBean = getGroupDetails(group, securityProfileDetailsBean);
                if (groupBean.getRulesCount() != 0) {
                    parentGroup.getGroups().add(groupBean);
                }
            }
        }
    }
    
    private String extractFixScript(XCCDFRule rule) {
    	String fixScript = null;
    	for(XCCDFFix fix : rule.getFixes()) {
    		fixScript = fix.getFixScript();
    	}
    	return fixScript;
    }

    private SecurityGroupBean getGroupDetails(XCCDFGroup group, SecurityProfileDetailsBean securityProfileDetailsBean) throws Exception {
        SecurityGroupBean groupBean = new SecurityGroupBean();
        groupBean.setId(group.getId());
        groupBean.setTitle(group.getTitle());
        groupBean.setDescription(group.getDescription());
        groupBean.setWarning(group.getWarning());
        ArrayList<XCCDFRule> xccdfRules = (ArrayList<XCCDFRule>)group.getRules();
        ArrayList<XCCDFValue> xccdfValues = (ArrayList<XCCDFValue>)group.getValues();
        HashMap<String, String> rulesComplianceMap = securityProfileDetailsBean.getRulesComplianceMap();
        HashMap<String, SecurityRuleBean> rulesMap = securityProfileDetailsBean.getRulesMap();
        HashMap<String, Object> rulesValueMap = securityProfileDetailsBean.getRulesValueMap();
        int passedGroupRulesCount = 0, failedGroupRulesCount = 0, otherGroupRulesCount = 0;
        for (int j=0;j<xccdfRules.size();j++) {
            XCCDFRule rule =  xccdfRules.get(j);
            if (rulesComplianceMap.containsKey(rule.getId())) {
                String ruleResult = rulesComplianceMap.get(rule.getId());
                SecurityRuleBean ruleBean = new SecurityRuleBean();
                ruleBean.setId(rule.getId());
                ruleBean.setTitle(rule.getTitle());
                ruleBean.setDescription(rule.getDescription()!= null ? rule.getDescription() : "");
                ruleBean.setSeverity(rule.getSeverity() != null ?  rule.getSeverity() : "");
                ruleBean.setRationale(rule.getRationale() != null ? rule.getRationale() : "");
                ruleBean.setFixScript(extractFixScript(rule) != null ? extractFixScript(rule).trim() : "");
                ruleBean.setResult(ruleResult);
                String valueID = rule.getValueID();
                String ruleValue = null;
                if (valueID != null) {
                    ruleValue = (String) rulesValueMap.get(valueID);
                } 
                if (ruleValue == null) {
                    ruleValue = "true";
                }
                ruleBean.setValue(ruleValue);
                
                UUID uniqueKey = UUID.randomUUID();
                ruleBean.setUuid(uniqueKey.toString());
                groupBean.getRules().add(ruleBean);
                rulesMap.put(rule.getId(),ruleBean);
                String severity = rule.getSeverity();
                if ("pass".equals(ruleResult)) {
                    securityProfileDetailsBean.setPassedRulesCount(securityProfileDetailsBean.getPassedRulesCount()+1);
                    passedGroupRulesCount++;
                }  else if ("fail".equals(ruleResult)) {
                    securityProfileDetailsBean.setFailedRulesCount(securityProfileDetailsBean.getFailedRulesCount()+1);
                    failedGroupRulesCount++;
                    if ("high".equals(severity)) {
                        securityProfileDetailsBean.setFailedRulesHighSeverity(securityProfileDetailsBean.getFailedRulesHighSeverity()+1);
                    } else if ("medium".equals(severity)) {
                        securityProfileDetailsBean.setFailedRulesMediumSeverity(securityProfileDetailsBean.getFailedRulesMediumSeverity()+1);
                    } else if ("low".equals(severity)) {
                        securityProfileDetailsBean.setFailedRulesLowSeverity(securityProfileDetailsBean.getFailedRulesLowSeverity()+1);
                    } else {
                        securityProfileDetailsBean.setFailedRulesOtherSeverity(securityProfileDetailsBean.getFailedRulesOtherSeverity()+1);
                    }
                } else {
                    securityProfileDetailsBean.setOtherRulesCount(securityProfileDetailsBean.getOtherRulesCount()+1);
                    otherGroupRulesCount++;
                }
            } else {
                System.out.println(rule.getId() + " does not exist in rulesComplianceMap");
            }

            int groupRulesCount = passedGroupRulesCount + failedGroupRulesCount + otherGroupRulesCount;
            String passedRulesPercentage = "0";
            String failedRulesPercentage = "0";
            String otherRulesPercentage = "0";
            if (groupRulesCount != 0) {
                passedRulesPercentage = String.format("%.2f", 100d * passedGroupRulesCount / groupRulesCount);
                failedRulesPercentage = String.format("%.2f", 100d * failedGroupRulesCount / groupRulesCount);
                otherRulesPercentage = String.format("%.2f", 100d * otherGroupRulesCount / groupRulesCount);
            }
            groupBean.setPassedRulesCount(passedGroupRulesCount);
            groupBean.setFailedRulesCount(failedGroupRulesCount);
            groupBean.setOtherRulesCount(otherGroupRulesCount);

            groupBean.setPassedRulesPercentage(passedRulesPercentage);
            groupBean.setFailedRulesPercentage(failedRulesPercentage);
            groupBean.setOtherRulesPercentage(otherRulesPercentage);
        }
        return groupBean;
    }

    private boolean hasPolicyReadPermission(Target tgt,IUser user){
        boolean hasRead = false;
        try {
            hasRead = ObjectManager.checkSubPerm( user, tgt.getId(), tgt.getType(), IAclConstants.READ_ACTION ) ||
                    ObjectManager.checkSubPerm( user, tgt.getId(), tgt.getType(), IAclConstants.WRITE_ACTION );
        }catch(Exception e){
            hasRead=false;
            e.printStackTrace();
        }
        return hasRead;
    }

    public void debug(int level, String str) {
       if(DEBUG >= level) {
           System.out.println("SecurityProfileReportAction.java: " + str);
       }
   }
}