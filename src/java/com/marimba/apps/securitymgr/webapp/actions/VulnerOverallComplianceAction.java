// Copyright 2018, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$

package com.marimba.apps.securitymgr.webapp.actions;

import com.marimba.apps.securitymgr.compliance.OVALDashboardDetails;
import com.marimba.apps.securitymgr.compliance.SecurityVulnerCompliance;
import com.marimba.apps.securitymgr.utils.ElasticSecurityMgmt;
import com.marimba.apps.securitymgr.utils.SecurityLDAPUtils;
import com.marimba.apps.securitymgr.view.SCAPBean;
import com.marimba.apps.subscriptionmanager.compliance.view.MachineBean;
import com.marimba.apps.subscriptionmanager.intf.IARTaskConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.objects.Target;
import static com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants.*;

import com.marimba.intf.util.IConfig;
import com.marimba.tools.config.ConfigUtil;

import com.marimba.castanet.http.HTTPManager;
import com.marimba.tools.net.HTTPConfig;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class VulnerOverallComplianceAction extends AbstractAction implements IWebAppConstants, ISubscriptionConstants {
	IUser user = null;

    ActionForward forward;

    public ActionForward perform(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) 
    throws IOException, ServletException {
    	init(request);
    	HttpSession session = request.getSession();
    	GUIUtils.initForm(request, mapping);

    	String action = request.getParameter("action");
    	String action1 = request.getParameter("action1");
    	
    	if (action1 == null) {
            action1 = (String)session.getAttribute("action1");
            session.setAttribute("action1", null);
        }
        
    	try {
            GUIUtils.setToSession(request, "context", "compTarget");
            user = GUIUtils.getUser(request);
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        String taskid = (String)session.getAttribute(IARTaskConstants.AR_TASK_ID);

        Target target;

        try {
            target = getTarget(request, main);
            if (target == null) {
                target = (Target) session.getAttribute("target");
            } else {
            	session.setAttribute("target", target);
            }

            if (target == null && taskid != null) {
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
            
            if ("fromdb".equals(action)) {
                JSONObject result = new JSONObject();

                if("all".equalsIgnoreCase(target.getID()) && "overall_dashboard".equals(action1)) {
                    loadAllEndpointDetails(result, target.getID());
                    loadAllEndpointScannerCount(result, target.getID());
                    getAllEndpointScannerwiseComplainceData(result, target.getID());
                } else {
                    loadMachineDetails(result, target.getID());
                    loadScannerCount(result, target);
                    getScannerwiseComplainceData(result, target.getID());
                }

                getContentsMap(result, target, action1);
                sendJSONResponse(response, result);
                return null;
            }

            if ("getprofiledetails".equals(action)) {
                String contentTitle = request.getParameter("contenttitle");
                JSONObject result = new JSONObject();
                getProfilesMap(result, target, contentTitle);
                sendJSONResponse(response, result);
                return null;
            }

            if ("getvulnerablitydetails".equals(action)) {
                JSONObject result = new JSONObject();
                getTop5VulnerableData(result, target, request);
                sendJSONResponse(response, result);
                return null;
            }

            if ("getserverstatus".equals(action)) {
                JSONObject result = new JSONObject();
                getServerStatus(result, request);
                sendJSONResponse(response, result);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        request.setAttribute("action1", action1 == null ? "":action1);
        
        forward = mapping.findForward("success");
        return forward;
    }
    private void loadAllEndpointDetails(JSONObject result, String targetId) throws JSONException {

        int compliant = 0, nonCompliant = 0, notCheckedIn = 0, notApplicable = 0;
        int windowsCompliant = 0, windowsNonCompliant = 0, windowsNotApplicable = 0;
        int linuxCompliant = 0, linuxNonCompliant = 0, linuxNotApplicable = 0;
        int macCompliant = 0, macNonCompliant = 0, macNotApplicable = 0;
        int otherCompliant = 0, otherNoncompliant = 0, otherNotApplicable = 0;

        int checkedIn24Hrs = new OVALDashboardDetails.GetLast24HourMachineDetails(main, "%").getMachinesCount();
        List<MachineBean> machineBeanList = new OVALDashboardDetails.GetAllEndpointMachines(main, targetId).getMachineBeanList();
        int totalMachineCount = new OVALDashboardDetails.GetAllEndpointMachineCount(main, "%").getMachinesCount();
        int totalWindowsMachineCount = new OVALDashboardDetails.GetAllEndpointMachineCount(main, "Windows").getMachinesCount();
        int totalLinuxMachineCount = new OVALDashboardDetails.GetAllEndpointMachineCount(main, "Linux").getMachinesCount();
        int totalMacMachineCount = new OVALDashboardDetails.GetAllEndpointMachineCount(main, "MAC").getMachinesCount();
        int otherMachineCount =  (totalMachineCount - (totalWindowsMachineCount + totalLinuxMachineCount + totalMacMachineCount));

        for (MachineBean mbean : machineBeanList) {
            String osType = mbean.getOsType().toLowerCase();
            String status = mbean.getComplianceLevel();

            if (STR_LEVEL_COMPLIANT.equalsIgnoreCase(status)) {
                compliant++;
                if ("windows".equals(osType)) windowsCompliant++;
                else if ("linux".equals(osType)) linuxCompliant++;
                else if ("mac".equals(osType)) macCompliant++;
                else otherCompliant++;
            }

            if (STR_LEVEL_NON_COMPLIANT.equalsIgnoreCase(status)) {
                nonCompliant++;
                if ("windows".equals(osType)) windowsNonCompliant++;
                else if ("linux".equals(osType)) linuxNonCompliant++;
                else if ("mac".equals(osType)) macNonCompliant++;
                else otherNoncompliant++;
            }

            if (STR_LEVEL_NOT_APPLICABLE.equalsIgnoreCase(status)) {
                notApplicable++;
                if ("windows".equals(osType)) windowsNotApplicable++;
                else if ("linux".equals(osType)) linuxNotApplicable++;
                else if ("mac".equals(osType)) macNotApplicable++;
                else otherNotApplicable++;
            }
        }

        int machineReportedCount = nonCompliant + compliant + notApplicable;
        String compliantPercentage = "0", nonCompliantPercentage = "0", notApplicablePercentage = "0";
        if (compliant != 0) compliantPercentage = String.format("%.2f", 100d * compliant / totalMachineCount);
        if (nonCompliant != 0) nonCompliantPercentage = String.format("%.2f", 100d * nonCompliant / totalMachineCount);
        if (notApplicable != 0) notApplicablePercentage = String.format("%.2f", 100d * notApplicable / totalMachineCount);
        notCheckedIn = totalMachineCount - (compliant + nonCompliant + notApplicable);

        result.put("compliant", compliant);
        result.put("noncompliant", nonCompliant);
        result.put("notcheckedin", notCheckedIn);
        result.put("notapplicable", notApplicable);
        result.put("compliant_percentage", compliantPercentage);
        result.put("noncompliant_percentage", nonCompliantPercentage);
        result.put("notapplicable_percentage", notApplicablePercentage);
        result.put("count", totalMachineCount);
        result.put("machine_reported_count", machineReportedCount);
        result.put("checkedIn24Hrs", checkedIn24Hrs);

        loadComplaintDetails(result, "windows", windowsCompliant, windowsNonCompliant, windowsNotApplicable, totalWindowsMachineCount);
        loadComplaintDetails(result, "linux", linuxCompliant, linuxNonCompliant, linuxNotApplicable, totalLinuxMachineCount);
        loadComplaintDetails(result, "mac", macCompliant, macNonCompliant, macNotApplicable, totalMacMachineCount);
        loadComplaintDetails(result, "other", otherCompliant, otherNoncompliant, otherNotApplicable, otherMachineCount);
    }
    
    private void loadMachineDetails(JSONObject result, String targetId) throws JSONException {

    	int compliant = 0, nonCompliant = 0, notCheckedIn = 0, notApplicable = 0;
        int windowsCompliant = 0, windowsNonCompliant = 0, windowsNotApplicable = 0;
        int linuxCompliant = 0, linuxNnCompliant = 0, linuxNotApplicable = 0;
        int macCompliant = 0, macNonCompliant = 0, macNotApplicable = 0;
        int otherCompliant = 0, otherNonCompliant = 0, otherNotApplicable = 0;
        int checkedIn24Hrs = new OVALDashboardDetails.GetLast24HourMachineDetails(main, targetId).getMachinesCount();

        List<MachineBean> machineBeanList = new OVALDashboardDetails.GetMachineDetails(main, targetId).getMachineBeanList();
        int totalMachineCount = new SecurityVulnerCompliance.GetTargetMachine(main, targetId).getMachineBeanList().size();
        int totalWindowsMachineCount = new SecurityVulnerCompliance.GetMachineOSCount(main, targetId, "Windows").getCount();
        int totalLinuxMachineCount = new SecurityVulnerCompliance.GetMachineOSCount(main, targetId, "Linux").getCount();
        int totalMacMachineCount = new SecurityVulnerCompliance.GetMachineOSCount(main, targetId, "MAC").getCount();
        int otherMachineCount =  (totalMachineCount - (totalWindowsMachineCount + totalLinuxMachineCount + totalMacMachineCount));

        for (MachineBean mbean : machineBeanList) {
            String osType = mbean.getOsType();
            String status = mbean.getComplianceLevel();

            if (STR_LEVEL_COMPLIANT.equalsIgnoreCase(status)) {
                compliant++;
                if ("Windows".equalsIgnoreCase(osType)) windowsCompliant++;
                else if ("linux".equalsIgnoreCase(osType)) linuxCompliant++;
                else if ("mac".equalsIgnoreCase(osType)) macCompliant++;
                else otherCompliant++;
            }

            if (STR_LEVEL_NON_COMPLIANT.equalsIgnoreCase(status)) {
                nonCompliant++;
                if ("Windows".equalsIgnoreCase(osType)) windowsNonCompliant++;
                else if ("linux".equalsIgnoreCase(osType)) linuxNnCompliant++;
                else if ("mac".equalsIgnoreCase(osType)) macNonCompliant++;
                else otherNonCompliant++;
            }

            if (STR_LEVEL_NOT_APPLICABLE.equalsIgnoreCase(status)) {
                notApplicable++;
                if ("Windows".equalsIgnoreCase(osType)) windowsNotApplicable++;
                else if ("linux".equalsIgnoreCase(osType)) linuxNotApplicable++;
                else if ("mac".equalsIgnoreCase(osType)) macNotApplicable++;
                else otherNotApplicable++;
            }
        }

        int machineReportedCount = compliant + nonCompliant + notApplicable;
        String compliantPercentage = "0", nonCompliantPercentage = "0", notApplicablePercentage = "0";
        if (compliant != 0) compliantPercentage = String.format("%.2f", 100d * compliant / totalMachineCount);
        if (nonCompliant != 0) nonCompliantPercentage = String.format("%.2f", 100d * nonCompliant / totalMachineCount);
        if (notApplicable != 0) notApplicablePercentage = String.format("%.2f", 100d * notApplicable / totalMachineCount);

        notCheckedIn = totalMachineCount - (compliant + nonCompliant + notApplicable);
        result.put("compliant", compliant);
        result.put("noncompliant", nonCompliant);
        result.put("notcheckedin", notCheckedIn);
        result.put("notapplicable", notApplicable);
        result.put("compliant_percentage", compliantPercentage);
        result.put("noncompliant_percentage", nonCompliantPercentage);
        result.put("notapplicable_percentage", notApplicablePercentage);
        result.put("count", totalMachineCount);
        result.put("machine_reported_count", machineReportedCount);
        result.put("checkedIn24Hrs", checkedIn24Hrs);
        loadComplaintDetails(result, "windows", windowsCompliant , windowsNonCompliant, windowsNotApplicable, totalWindowsMachineCount);
        loadComplaintDetails(result, "linux", linuxCompliant , linuxNnCompliant, linuxNotApplicable, totalLinuxMachineCount);
        loadComplaintDetails(result, "mac", macCompliant , macNonCompliant, macNotApplicable, totalMacMachineCount);
        loadComplaintDetails(result, "other", otherCompliant , otherNonCompliant, otherNotApplicable, otherMachineCount);
    }
    private void loadComplaintDetails(JSONObject result, String component, int compliant, int notCheckedIn, int notApplicable) throws JSONException {
        loadComplaintDetails(result, component , compliant, notCheckedIn, notApplicable, 0);
    }
    private void loadComplaintDetails(JSONObject result, String component, int compliant, int nonCompliant, int notApplicable, int count) throws JSONException {

        if (count == 0) count = compliant + nonCompliant + notApplicable;
        String compliantPercentage = "0", nonCompliantPercentage = "0", notApplicablePercentage = "0";
        if (compliant != 0) compliantPercentage = String.format("%.2f", 100d * compliant / count);
        if (nonCompliant != 0) nonCompliantPercentage = String.format("%.2f", 100d * nonCompliant / count);
        if (notApplicable != 0) notApplicablePercentage = String.format("%.2f", 100d * notApplicable / count);

        result.put(component+"_count", count);
        result.put(component+"_compliant", compliant);
        result.put(component+"_noncompliant", nonCompliant);
        result.put(component+"_notapplicable", notApplicable);
        result.put(component+"_compliant_percentage", compliantPercentage);
        result.put(component+"_noncompliant_percentage", nonCompliantPercentage);
        result.put(component+"_notapplicable_percentage", notApplicablePercentage);
    }
    private void loadAllEndpointScannerCount(JSONObject result, String targetID) throws JSONException {
        int scannerUse = new OVALDashboardDetails.GetSecurityInUseDetails(main, targetID).getSecurityCount();
        result.put("scanner_use_count", scannerUse);
    }
    private void loadScannerCount(JSONObject result, Target target) throws JSONException {
        int scannerUse = SecurityLDAPUtils.getAssignedPolicy(SECURITY_SCAN_TYPE_OVAL, target.getName(), target.getType(), target.getId(), main, user).size();
        result.put("scanner_use_count", scannerUse);
    }

    private void getScannerwiseComplainceData(JSONObject result, String targetId) throws JSONException {
        Map<String, Map<String, Integer>> scannerMap = new OVALDashboardDetails.GetScannerWiseCompliant(main, targetId).getScannerMap();
        int compliant = 0;
        int nonCompliant = 0;
        int count = 0;

        try {
            count = Integer.parseInt(result.getString("count"));
        } catch (NumberFormatException npe) {
        }

        JSONArray jsonArrays = new JSONArray();

        for (Map.Entry<String, Map<String, Integer>> entry : scannerMap.entrySet()) {
        	Map<String, Integer> map = entry.getValue();
            compliant = map.get(COMPLAINT);
            String compliantPercentage = "0";

            if (compliant != 0) compliantPercentage = String.format("%.2f", 100d * compliant / count);

            JSONObject jsonArr = new JSONObject();
            jsonArr.put("title", entry.getKey());
            jsonArr.put("percentage", compliantPercentage);
            jsonArr.put("compliant", compliant);
            jsonArr.put("count", count);
            jsonArrays.put(jsonArr);
        }
        result.put("scannerwise_complaince_data", jsonArrays);
    }

    private void getContentsMap(JSONObject result, Target target, String type) throws JSONException {
        JSONArray contentsArrays = new JSONArray();
        Map<String, String> contentsMap;

        if ("overall_dashboard".equalsIgnoreCase(type)) { // This is for entire machines dashboard
            contentsMap = new OVALDashboardDetails.GetContentInfoForAllEndpoints(main).getContentsMap();

        } else {
            contentsMap = SecurityLDAPUtils.getContentDetailsOfTarget(target, main, user);
        }

        for (String contentTile : contentsMap.keySet()) {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("title", contentTile);
            jsonObj.put("id", contentsMap.get(contentTile));
            contentsArrays.put(jsonObj);
        }
        result.put("scap_content_data", contentsArrays);
    }

    private void getProfilesMap(JSONObject result, Target target, String contentTitle) throws JSONException {
        JSONArray profilesArrays = new JSONArray();

        if (null != contentTitle && !contentTitle.trim().isEmpty()) {
            Map<String, String> profilesMap;
            if ("all".equalsIgnoreCase(target.getId())) {
                profilesMap = new OVALDashboardDetails.GetProfileInfoForAllEndpoints(main, contentTitle).getProfilesMap();
            } else {
                profilesMap = SecurityLDAPUtils.getProfileDetailsOfContent(target, main, user, contentTitle);
            }

            for (String profileId : profilesMap.keySet()) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("id", profileId);
                jsonObj.put("title", profilesMap.get(profileId));
                profilesArrays.put(jsonObj);
            }
        }
        result.put("scap_profiles_data", profilesArrays);
    }
    private void getTop5VulnerableData(JSONObject result, Target target, HttpServletRequest request) throws JSONException {
        JSONArray rulesResultArr = new JSONArray();
        String elasticUrl = main.getConfig().getProperty("subscriptionmanager.elasticurl");

        if (null == elasticUrl || elasticUrl.trim().isEmpty()) {
            System.out.println("Elastic server URL is null");
            result.put("rules", rulesResultArr);
            return;
        }

        elasticUrl = (elasticUrl.endsWith("/")) ? elasticUrl + "_search" : elasticUrl + "/_search";
        String contentId = request.getParameter("contentid"); //"xccdf_org.ssgproject.content_benchmark_Firefox";
        String profileId = request.getParameter("profileid"); //"xccdf_org.ssgproject.content_profile_stig-firefox-upstream";
        String date = request.getParameter("date");
        String targetId = target.getId(); //"cn=vm-rlnx-mar72,cn=bmc cm computers,ou=bmc cm,ou=bmc software,dc=clm-pun-2723,dc=local";

        targetId = targetId != null ? targetId.toLowerCase() : targetId;
        if ("all".equals(targetId)) { //if you give targetName as "null" it will not try to filter for target so pass null and it will pick all targets
            targetId = null;
        }

        long dateInMilli = -1;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            dateInMilli = sdf.parse(date).getTime();

        } catch(Exception ex) {/**/}

        System.out.println("Begin to get top5 vulnerable details for : contentId: ["+contentId+"], profileId: ["+profileId+"], targetId: ["+targetId+"], date: ["+dateInMilli+"]");

        IConfig tunerConfig = (IConfig) features.getChild("tunerConfig");
        HTTPConfig httpConfig = new HTTPConfig(new ConfigUtil(tunerConfig));
        HTTPManager httpManager = new HTTPManager(httpConfig);
        ElasticSecurityMgmt securityMgmt = new ElasticSecurityMgmt(httpManager, httpConfig, elasticUrl);

        com.marimba.apps.securitymgr.utils.json.JSONArray rules = null;
        try {
            rules = securityMgmt.getTopVulnerableRules(contentId, profileId, targetId, dateInMilli, 5);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("Rules results: " + rules);

        if (null != rules && rules.length() > 0) {
            Collection<String> ruleIds = new LinkedList<String>();

            for (int i = 0; i < rules.length() && i < 5; i++) {
                com.marimba.apps.securitymgr.utils.json.JSONObject item = rules.getJSONObject(i);
                String ruleId = item.getString("RuleName");

                int success = item.getInt("success"), failed = item.getInt("failed"), total = item.getInt("Total");
                int percentage = (int)((failed * 100.0f) / total);

                if(percentage> 0) {
                    JSONObject obj = new JSONObject();
                    obj.put("name", "");
                    obj.put("id", ruleId);
                    obj.put("success", success);
                    obj.put("failed", failed);
                    obj.put("total", total);
                    obj.put("percentage", percentage);
                    ruleIds.add(ruleId);
                    rulesResultArr.put(obj);
                }
            }

            OVALDashboardDetails.GetRuleNames ruleNameFromDB = new OVALDashboardDetails.GetRuleNames(main, ruleIds);
            Map<String, String> ruleInfoMap = ruleNameFromDB.getRuleNameMap();

            for (String ruleId : ruleInfoMap.keySet()) {
                String ruleName = ruleInfoMap.get(ruleId);
                for (int i = 0; i < rulesResultArr.length(); i++) {
                    JSONObject item = rulesResultArr.getJSONObject(i);
                    if (!item.get("id").equals(ruleId)) continue;
                    item.put("name", ((ruleName != null) && (ruleName.trim().length() > 0)) ? ruleName : ruleId);
                }
            }
        }

        System.out.println("rulesResultArr: " + rulesResultArr);

        result.put("rules", rulesResultArr);
    }

    private void getServerStatus(JSONObject result, HttpServletRequest request) throws JSONException {

        String elasticUrl = main.getConfig().getProperty("subscriptionmanager.elasticurl");

        if (null == elasticUrl || elasticUrl.trim().isEmpty()) {

            System.out.println("Elastic server URL is null");

            result.put("elasticstatus", "Not Configured");

            return;

        }

        elasticUrl = (elasticUrl.endsWith("/")) ? elasticUrl + "_search" : elasticUrl + "/_search";

        IConfig tunerConfig = (IConfig) features.getChild("tunerConfig");

        HTTPConfig httpConfig = new HTTPConfig(new ConfigUtil(tunerConfig));

        HTTPManager httpManager = new HTTPManager(httpConfig);

        ElasticSecurityMgmt securityMgmt = new ElasticSecurityMgmt(httpManager, httpConfig, elasticUrl);



        boolean elasticup = securityMgmt.isElasticUP();

        String elasticServerStatus = "";

        elasticServerStatus = (elasticup == false)? "Down" : "Up";

        System.out.println("elasticserver status: "+elasticServerStatus);

        result.put("elasticstatus", elasticServerStatus);

    }

    private void debug(String msg) {

        if (DEBUG3) System.out.println("TargetOverviewAction: " + msg);

    }



    private void getAllEndpointScannerwiseComplainceData(JSONObject result, String targetId) throws JSONException {

        Map<String, SCAPBean> scannerMap = new OVALDashboardDetails.GetAllEndpointScannerWiseCompliant(main, targetId).getScannerMap();

        int compliant = 0, nonCompliant = 0, count = 0;



        JSONArray jsonArrays = new JSONArray();

        for (Map.Entry<String, SCAPBean> entry : scannerMap.entrySet()) {

            SCAPBean scapBean = entry.getValue();

            compliant = scapBean.getCompliantCount();

            nonCompliant = scapBean.getNonCompliantCount();

            count = compliant + nonCompliant;

            String compliantPercentage = "0";

            if (compliant != 0) compliantPercentage = String.format("%.2f", 100d * compliant / count);



            JSONObject jsonArr = new JSONObject();

            jsonArr.put("title", entry.getKey());

            jsonArr.put("percentage", compliantPercentage);

            jsonArr.put("compliant", compliant);

            jsonArr.put("count", count);

            jsonArrays.put(jsonArr);

        }

        result.put("scannerwise_complaince_data", jsonArrays);

    }
}
