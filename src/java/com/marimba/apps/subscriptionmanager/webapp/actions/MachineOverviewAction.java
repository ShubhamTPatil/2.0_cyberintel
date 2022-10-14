// Copyright 2018, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscriptionmanager.intf.IARTaskConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.Utils;
import com.marimba.apps.subscriptionmanager.beans.SecurityTargetDetailsBean;
import com.marimba.apps.subscriptionmanager.beans.SecurityProfileDetailsBean;
import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;
import com.marimba.apps.securitymgr.utils.SecurityLDAPUtils;
import com.marimba.apps.securitymgr.compliance.SecurityCompliance;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.tools.action.DelayedAction;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.util.*;


public class MachineOverviewAction extends AbstractAction implements IWebAppConstants, ISubscriptionConstants {

    IUser user = null;
    protected DelayedAction.Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
        return new MachineOverviewAction.MachineOverviewActionTask(mapping, form, request, response);
    }

    protected class MachineOverviewActionTask extends AbstractAction.SubscriptionDelayedTask {
        MachineOverviewActionTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
            super(mapping, form, request, response);
        }

        public void execute(){
            init(request);
            String action = request.getParameter("action");
            String view = request.getParameter("view");
            HttpSession session = request.getSession();
            GUIUtils.initForm(request, mapping);
            try {
                GUIUtils.setToSession(request, "context", "compTarget");
            } catch (SystemException e) {
                e.printStackTrace();
            }

            try {
                user = GUIUtils.getUser(request);
            } catch(Exception e){
                e.printStackTrace();
            }

            Target target;
            try {
                target = getTarget(request, main);
                if (target == null) {
                    target = (Target) session.getAttribute("target");
                } else {
                    session.setAttribute("target", target);
                }
                if (target == null) {
                    List targetList;
                    if (session.getAttribute(MAIN_PAGE_TARGET) != null) {
                        targetList = (List) session.getAttribute(MAIN_PAGE_TARGET);
                    } else {
                        targetList = (List) session.getAttribute(MAIN_PAGE_M_TARGETS);
                    }
                    // if found multiple targets then use the first target from the list.
                    if (targetList != null && !targetList.isEmpty() ) {
                        target = (Target) targetList.get(0);
                    }
                }

                IUser user = null;
                try {
                    user = GUIUtils.getUser(request);

                }catch(Exception e){
                    e.printStackTrace();
                }
                if (target != null) {
                    List display_results = SecurityLDAPUtils.getAssignedPolicy(SECURITY_SCAN_TYPE_XCCDF, target.getName(), target.getType(), target.getId(), main, user);
                    SecurityTargetDetailsBean targetDetails = new SecurityTargetDetailsBean();
                    targetDetails.setTargetName(target.getName());
                    int profileCount = 0;
                    int compliantCount = 0, nonCompliantCount = 0, notCheckedInCount=0, notApplicableCount = 0;
                    if( display_results != null && display_results.size() > 0 ) {
                        profileCount = display_results.size();
                        targetDetails.setProfileCount(profileCount);
                        ArrayList<SecurityProfileDetailsBean> profiles= new ArrayList<SecurityProfileDetailsBean>();
                        for (int i = 0; i<display_results.size(); i++) {
                            int passedRulesCount = 0, failedRulesCount = 0, otherRulesCount = 0;
                            SecurityTargetDetailsBean securityTargetDetailsBean = (SecurityTargetDetailsBean) display_results.get(i);
                            SecurityProfileDetailsBean securityProfileDetailsBean = new SecurityProfileDetailsBean();
                            securityProfileDetailsBean.setAssignedToID(securityTargetDetailsBean.getAssignedToID());
                            securityProfileDetailsBean.setTargetID(securityTargetDetailsBean.getTargetID());
                            securityProfileDetailsBean.setTargetName(securityTargetDetailsBean.getTargetName());
                            securityProfileDetailsBean.setProfileName(securityTargetDetailsBean.getSelectedProfileId());
                            securityProfileDetailsBean.setContentName(securityTargetDetailsBean.getSelectedContentId());
                            securityProfileDetailsBean.setContentTitle(securityTargetDetailsBean.getSelectedSecurityContentName());
                            securityProfileDetailsBean.setCategoryType(securityTargetDetailsBean.getCategoryType());
                            new SecurityCompliance.GetProfileComplianceReport(main,securityProfileDetailsBean);
                            String compliance = securityProfileDetailsBean.getComplaintLevel();
                            System.out.println("Compliance : "+ compliance);
                            if (compliance == null || "".equals(compliance)) {
                                securityProfileDetailsBean.setComplaintLevel(ComplianceConstants.STR_LEVEL_NOT_CHECK_IN);
                                notCheckedInCount++;
                            } else {
                                if ("compliant".equalsIgnoreCase(compliance)) {
                                    compliantCount++;
                                }
                                if ("non-compliant".equalsIgnoreCase(compliance)) {
                                    nonCompliantCount++;
                                }
                                if ("not applicable".equalsIgnoreCase(compliance)) {
                                    notApplicableCount++;
                                }
                                String rulesJSONResult = securityProfileDetailsBean.getRulesJSONResult();
                                JSONObject json = new JSONObject(rulesJSONResult);
                                Map<String, Object> map = Utils.jsonObjectToMap(json);
                                Map<String, Object> rulesComplianceMap = (Map<String, Object>)map.get("rules_compliance");
                                if (rulesComplianceMap != null) {
                                    Iterator itr = rulesComplianceMap.keySet().iterator();
                                    HashMap<String, String> rulesResultMap = new HashMap<String, String>();
                                    while (itr.hasNext()) {
                                        String ruleName = (String)itr.next();
                                        String ruleResult = (String) rulesComplianceMap.get(ruleName);
                                        rulesResultMap.put(ruleName, ruleResult);
                                        if ("pass".equals(ruleResult)) {
                                            passedRulesCount++;
                                        } else if ("fail".equals(ruleResult)) {
                                            failedRulesCount++;
                                        } else {
                                            otherRulesCount++;
                                        }
                                    }
                                } 
                            }
                            profiles.add(securityProfileDetailsBean);
                            securityProfileDetailsBean.setPassedRulesCount(passedRulesCount);
                            securityProfileDetailsBean.setFailedRulesCount(failedRulesCount);
                            securityProfileDetailsBean.setOtherRulesCount(otherRulesCount);
                            targetDetails.setTargetOS(securityProfileDetailsBean.getTargetOS());
                            if (securityProfileDetailsBean.getFinishTime() != null) {
                                targetDetails.setLastPolicyUpdateTimeInString(securityProfileDetailsBean.getFinishTime());
                            }
                        }
                        System.out.println("notApplicableCount = " + notApplicableCount);
                        targetDetails.setProfiles(profiles);
                        targetDetails.setCompliantCount(String.valueOf(compliantCount));
                        targetDetails.setNonCompliantCount(String.valueOf(nonCompliantCount));
                        targetDetails.setCheckinCount(String.valueOf(notCheckedInCount));
                        targetDetails.setNotApplicableCount(String.valueOf(notApplicableCount));

                        if (profileCount == compliantCount) {
                            targetDetails.setComplaintLevel("COMPLIANT");
                        } else if (profileCount == notCheckedInCount) {
                            targetDetails.setComplaintLevel("NOT-CHECKED-IN");
                        } else if (profileCount == notApplicableCount) {
                            targetDetails.setComplaintLevel("NOT APPLICABLE");
                        } else if (compliantCount == 0 && notCheckedInCount == 0 && notApplicableCount > 0) {
                            targetDetails.setComplaintLevel("NOT APPLICABLE");
                        } else {
                            targetDetails.setComplaintLevel("NON-COMPLIANT");
                        }
                        if (targetDetails.getLastPolicyUpdateTimeInString() == null) {
                            targetDetails.setLastPolicyUpdateTimeInString("NA");
                        }
                    }
                    setRequestAttribute("targetDetails", targetDetails);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            forward = mapping.findForward("success");
        }
    }

}