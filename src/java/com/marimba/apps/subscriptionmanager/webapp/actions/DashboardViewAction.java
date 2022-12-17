// Copyright 2019-2022, Harman International. All Rights Reserved.// Confidential and Proprietary Information of Harman International.// $File$, $Revision$, $Date$package com.marimba.apps.subscriptionmanager.webapp.actions;import com.marimba.apps.securitymgr.compliance.DashboardInfoDetails;import com.marimba.apps.securitymgr.compliance.DashboardHandler;import com.marimba.apps.subscription.common.ISubscriptionConstants;import com.marimba.apps.subscription.common.intf.IUser;import com.marimba.apps.subscriptionmanager.SubscriptionMain;import com.marimba.apps.subscriptionmanager.TenantHelper;import com.marimba.apps.subscriptionmanager.beans.TopVulnerableStatusBean;import com.marimba.apps.subscriptionmanager.beans.VulnerableStatusBean;import com.marimba.apps.subscriptionmanager.compliance.view.MachineBean;import com.marimba.intf.msf.*;import com.marimba.apps.subscription.common.objects.Target;import com.marimba.apps.subscriptionmanager.util.Utils;import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;import com.marimba.apps.subscriptionmanager.webapp.forms.NewDashboardViewForm;import com.marimba.intf.msf.ITenant;import com.marimba.intf.util.IConfig;import com.marimba.webapps.intf.IWebAppsConstants;import com.sun.jmx.snmp.tasks.Task;import org.apache.struts.action.ActionForm;import org.apache.struts.action.ActionForward;import org.apache.struts.action.ActionMapping;import javax.servlet.http.HttpServletRequest;import javax.servlet.http.HttpServletResponse;import java.util.List;import java.util.Map;/** * Dashboard View Action  *  w.r.t show new dashboard data * * @author Nandakumar Sankaralingam * @version: $Date$, $Revision$ */public class DashboardViewAction extends AbstractAction implements IWebAppConstants, ISubscriptionConstants {    protected boolean useURIMapping(){        return true;    }    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {        return new DashboardViewAction.DashboardViewTask(mapping, form, request, response);    }    protected class DashboardViewTask extends SubscriptionDelayedTask {        NewDashboardViewForm dashboardForm;        DashboardViewTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {            super(mapping, form, request, response);            this.dashboardForm = (NewDashboardViewForm) form;        }        protected boolean isNull(String s) {            return s == null || s.trim().length() == 0;        }        public void execute() {            init(request);            String action = request.getParameter("action");            action = dashboardForm.getAction();            System.out.println("Debug-DashboardViewAction action: " + action);            try {            if (null == action || "view".equals(action)) {                DashboardHandler dashboardHandler = new DashboardHandler(main);                int totalMachineCount = dashboardHandler.getEnrolledMachines("%");                int checkedIn24Hrs = dashboardHandler.getLast24HourEnrolledMachineCount("%");                int totalWindowsMachineCount = dashboardHandler.getEnrolledMachinesByOS("Windows");                int totalLinuxMachineCount = dashboardHandler.getEnrolledMachinesByOS("Linux");                int totalMacMachineCount = dashboardHandler.getEnrolledMachinesByOS("MAC");                // Scanned Machines Count                int vScanMachinesCount = dashboardHandler.getVScanMachinesCount("vscan");                int patchScanMachinesCount = dashboardHandler.getPatchScanMachinesCount("patchscan");                Map<String, String> vulnerableStatsInfo = dashboardHandler.getVulnerableStatsInfo();                                String critical =   isNull(vulnerableStatsInfo.get("Critical")) ? "0" : vulnerableStatsInfo.get("Critical");                String high =   isNull(vulnerableStatsInfo.get("High")) ? "0" : vulnerableStatsInfo.get("High");                String medium =   isNull(vulnerableStatsInfo.get("Medium")) ? "0" : vulnerableStatsInfo.get("Medium");                String low =   isNull(vulnerableStatsInfo.get("Low")) ? "0" : vulnerableStatsInfo.get("Low");                                String pieChartDataSet  =  "[" + critical + "," + high + "," + medium + "," + low + "]";                long totalVul = Long.parseLong(critical) + Long.parseLong(high) + Long.parseLong(medium) + Long.parseLong(low);                                VulnerableStatusBean vsbean = new VulnerableStatusBean();                vsbean.setCritical(critical);                vsbean.setHigh(high);                vsbean.setMedium(medium);                vsbean.setLow(low);                dashboardForm.setMachinesCount(String.valueOf(totalMachineCount));                dashboardForm.setMachineWindowsCount(String.valueOf(totalWindowsMachineCount));                dashboardForm.setMachineLinuxCount(String.valueOf(totalLinuxMachineCount));                dashboardForm.setMachineMacCount(String.valueOf(totalMacMachineCount));                dashboardForm.setVscanCount(String.valueOf(vScanMachinesCount));                dashboardForm.setPatchScanCount(String.valueOf(patchScanMachinesCount));                dashboardForm.setPieChartData(pieChartDataSet);                dashboardForm.setVulnerableStatusBean(vsbean);                dashboardForm.setTotalVulnerable(String.valueOf(totalVul));                                List<TopVulnerableStatusBean> topVulList = dashboardHandler.getTopVulnerabilitiesInfo();                dashboardForm.setTopVulnerableList(topVulList);                String topVulnsJson = makeTopVulnerabilitiesJsonData(topVulList);                dashboardForm.setTopVulnerableData(topVulnsJson);                                Map<String, String> reportingResult = dashboardHandler.getComplianceType("reporting");                Map<String, String> securityResult = dashboardHandler.getComplianceType("security");                Map<String, String> patchResult = dashboardHandler.getComplianceType("patch");                long notAvailCount = dashboardHandler.getEnrolledMachines("%");                notAvailCount = notAvailCount - (Long.parseLong(reportingResult.get("checkedIn")) +  Long.parseLong(reportingResult.get("notCheckedIn")));                                dashboardForm.setReportingCheckedIn(reportingResult.get("checkedIn"));                dashboardForm.setReportingNotCheckedIn(reportingResult.get("notCheckedIn"));                dashboardForm.setReportingNotAvailable(String.valueOf(notAvailCount));                dashboardForm.setSecurityCompliant(securityResult.get("compliant"));                dashboardForm.setSecurityNonCompliant(securityResult.get("nonCompliant"));                dashboardForm.setPatchCompliant(patchResult.get("compliant"));                dashboardForm.setPatchNonCompliant(patchResult.get("nonCompliant"));                forward = mapping.findForward("view");            }                            } catch(Exception ex) {                ex.printStackTrace();            }        }        private String makeTopVulnerabilitiesJsonData(List<TopVulnerableStatusBean> topVulList) {            String jsonData = "[";            try {               int size = topVulList.size();               StringBuffer sb =  new StringBuffer();               for(int i = 0; i < size; i++) {                   sb.append("{");                   TopVulnerableStatusBean topVulbean = topVulList.get(i);                   sb.append("\"CVE-ID\"");sb.append(":");                   sb.append("\"");sb.append(topVulbean.getCveId());sb.append("\"");                   sb.append(",");                   sb.append("\"Severity\"");sb.append(":");                   sb.append("\"");sb.append(topVulbean.getSeverity());sb.append("\"");                   sb.append(",");                   sb.append("\"Impacted Machines\"");sb.append(":");                   sb.append(topVulbean.getAffectedMachines());                   sb.append(",");                   sb.append("\"Patches\"");sb.append(":");                   sb.append("\"");sb.append(topVulbean.getPatchId());sb.append("\"");                   sb.append("}");                   if (i == (size-1)) {                       break;                   } else {                       sb.append(",");                   }               }               jsonData = jsonData + sb.toString() + "]";             //  System.out.println("DebugInfo: Top Vulnerabilities JSON: "+jsonData);            } catch (Exception ex) {                ex.printStackTrace();            }            return jsonData;        }                public String getWaitMessage() {            return getString("page.global.processing");        }    }}