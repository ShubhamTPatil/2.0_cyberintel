// Copyright 2019-2022, Harman International. All Rights Reserved.// Confidential and Proprietary Information of Harman International.// $File$, $Revision$, $Date$package com.marimba.apps.subscriptionmanager.webapp.actions;import com.marimba.apps.securitymgr.compliance.DashboardInfoDetails;import com.marimba.apps.securitymgr.compliance.DashboardHandler;import com.marimba.apps.subscription.common.ISubscriptionConstants;import com.marimba.apps.subscription.common.intf.IUser;import com.marimba.apps.subscriptionmanager.SubscriptionMain;import com.marimba.apps.subscriptionmanager.TenantHelper;import com.marimba.apps.subscriptionmanager.beans.*;import com.marimba.apps.subscriptionmanager.compliance.view.MachineBean;import com.marimba.intf.msf.*;import com.marimba.apps.subscription.common.objects.Target;import com.marimba.apps.subscriptionmanager.util.Utils;import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;import com.marimba.apps.subscriptionmanager.webapp.forms.NewDashboardViewForm;import com.marimba.intf.msf.ITenant;import com.marimba.intf.util.IConfig;import com.marimba.webapps.intf.IWebAppsConstants;import com.sun.jmx.snmp.tasks.Task;import org.apache.struts.action.ActionForm;import org.apache.struts.action.ActionForward;import org.apache.struts.action.ActionMapping;import com.marimba.tools.config.*;import com.marimba.intf.msf.*;import javax.servlet.http.HttpServletRequest;import javax.servlet.http.HttpServletResponse;import java.io.*;import java.util.*;/** * Dashboard View Action  *  w.r.t show new dashboard data * * @author Nandakumar Sankaralingam * @version: $Date$, $Revision$ */public class DashboardViewAction extends AbstractAction implements IWebAppConstants, ISubscriptionConstants {    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {        return new DashboardViewAction.DashboardViewTask(mapping, form, request, response);    }    protected class DashboardViewTask extends SubscriptionDelayedTask {        NewDashboardViewForm dashboardForm;        ConfigProps config = null;                DashboardViewTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {             super(mapping, form, request, response);             this.dashboardForm = (NewDashboardViewForm) form;         }        public void execute() {            init(request);            String action = request.getParameter("action");            action = dashboardForm.getAction();            System.out.println("Debug-DashboardViewAction action: " + action);            try {                DashboardHandler dashboardHandler = new DashboardHandler(main);            if (null == action || "view".equals(action)) {                int totalMachineCount = dashboardHandler.getEnrolledMachines("%");                int checkedIn24Hrs = dashboardHandler.getLast24HourEnrolledMachineCount("%");                int totalWindowsMachineCount = dashboardHandler.getEnrolledMachinesByOS("Windows");                int totalLinuxMachineCount = dashboardHandler.getEnrolledMachinesByOS("Linux");                int totalMacMachineCount = dashboardHandler.getEnrolledMachinesByOS("MAC");                // Scanned Machines Count                int vScanMachinesCount = dashboardHandler.getVScanMachinesCount("vscan");                int patchScanMachinesCount = dashboardHandler.getPatchScanMachinesCount("patchscan");                Map<String, String> vulnerableStatsInfo = dashboardHandler.getVulnerableStatsInfo();                                String critical =   isNull(vulnerableStatsInfo.get("Critical")) ? "0" : vulnerableStatsInfo.get("Critical");                String high =   isNull(vulnerableStatsInfo.get("High")) ? "0" : vulnerableStatsInfo.get("High");                String medium =   isNull(vulnerableStatsInfo.get("Medium")) ? "0" : vulnerableStatsInfo.get("Medium");                String low =   isNull(vulnerableStatsInfo.get("Low")) ? "0" : vulnerableStatsInfo.get("Low");                                String pieChartDataSet  =  "[" + critical + "," + high + "," + medium + "," + low + "]";                long totalVul = Long.parseLong(critical) + Long.parseLong(high) + Long.parseLong(medium) + Long.parseLong(low);                                VulnerableStatusBean vsbean = new VulnerableStatusBean();                vsbean.setCritical(critical);                vsbean.setHigh(high);                vsbean.setMedium(medium);                vsbean.setLow(low);                dashboardForm.setMachinesCount(String.valueOf(totalMachineCount));                dashboardForm.setMachineWindowsCount(String.valueOf(totalWindowsMachineCount));                dashboardForm.setMachineLinuxCount(String.valueOf(totalLinuxMachineCount));                dashboardForm.setMachineMacCount(String.valueOf(totalMacMachineCount));                dashboardForm.setVscanCount(String.valueOf(vScanMachinesCount));                dashboardForm.setPatchScanCount(String.valueOf(patchScanMachinesCount));                dashboardForm.setPieChartData(pieChartDataSet);                dashboardForm.setVulnerableStatusBean(vsbean);                dashboardForm.setTotalVulnerable(String.valueOf(totalVul));                                List<TopVulnerableStatusBean> topVulList = dashboardHandler.getTopVulnerabilitiesInfo();                dashboardForm.setTopVulnerableList(topVulList);                String topVulnsJson = makeTopVulnerabilitiesJsonData(topVulList);                dashboardForm.setTopVulnerableData(topVulnsJson);                                Map<String, String> reportingResult = dashboardHandler.getComplianceType("reporting");                Map<String, String> securityResult = dashboardHandler.getComplianceType("security");                Map<String, String> patchResult = dashboardHandler.getComplianceType("patch");                long notAvailCount = dashboardHandler.getEnrolledMachines("%");                notAvailCount = notAvailCount - (Long.parseLong(reportingResult.get("checkedIn")) +  Long.parseLong(reportingResult.get("notCheckedIn")));                                dashboardForm.setReportingCheckedIn(reportingResult.get("checkedIn"));                dashboardForm.setReportingNotCheckedIn(reportingResult.get("notCheckedIn"));                dashboardForm.setReportingNotAvailable(String.valueOf(notAvailCount));                /*                System.out.println("Debug: Reporting: checkedIn ==> "+ reportingResult.get("checkedIn"));                System.out.println("Debug: Reporting: notCheckedIn ==> "+ reportingResult.get("notCheckedIn"));                System.out.println("Debug: Reporting: checkedIn ==> "+ notAvailCount);                System.out.println("Debug: Security: compliant ==> "+ securityResult.get("compliant"));                System.out.println("Debug: Security: nonCompliant ==> "+ securityResult.get("nonCompliant"));                System.out.println("Debug: Patch: compliant ==> "+ patchResult.get("compliant"));                System.out.println("Debug: Patch: nonCompliant ==> "+ patchResult.get("nonCompliant"));                */                dashboardForm.setSecurityCompliant(securityResult.get("compliant"));                dashboardForm.setSecurityNonCompliant(securityResult.get("nonCompliant"));                dashboardForm.setPatchCompliant(patchResult.get("compliant"));                dashboardForm.setPatchNonCompliant(patchResult.get("nonCompliant"));                Map<String, String> vulSeverityInfo = dashboardHandler.getAgingVulnerableBySeverityWise();                String vulSeverityInfoJson =  makeVulnerableSeverityJsonData(vulSeverityInfo);                 dashboardForm.setVulnerableSeverityData(vulSeverityInfoJson);                List<PriorityPatchesBean> prtyPatchesList = dashboardHandler.getPriorityPatchesInfo();                dashboardForm.setPriorityPatchesList(prtyPatchesList);                String prtyPatchesJson = makePriorityPatchesJsonData(prtyPatchesList);                dashboardForm.setPriorityPatchesData(prtyPatchesJson);                forward = mapping.findForward("view");            } else if ("notcheckedin_info".equalsIgnoreCase(action)) {                List<ReportingNotCheckedInBean> rptNotCheckedInList = dashboardHandler.getComplianceReportNotCheckedIn();                dashboardForm.setRptNotCheckedInList(rptNotCheckedInList);                String rptNotCheckedInJson = makeReportingNotCheckedInJsonData(rptNotCheckedInList);                dashboardForm.setReportNotCheckedInData(rptNotCheckedInJson);                sendJSONResponse(response, rptNotCheckedInJson);                forward = mapping.findForward("view");            } else if ("mitigate".equalsIgnoreCase(action)) {               // System.out.println("Debug- Called mitigate flow -- ");                String patchids = request.getParameter("patchids");                String[] repIDs = patchids.split(",");                StringBuffer sb = new StringBuffer();                for(int i=0; i < repIDs.length; i++) {                    System.out.println("Selected PatchID: " + repIDs[i]);                    sb.append("'");sb.append(repIDs[i]);sb.append("'");                    if (i == (repIDs.length - 1)) {                       break;                    } else {                        sb.append(",");                    }                }                List<MitigatePatchesBean> mitigatePatchesList = dashboardHandler.getMitigatePatchesInfo(sb.toString());                String mitigatePatchesJson = makeMitigatePatchesJsonData(mitigatePatchesList);                sendJSONResponse(response, mitigatePatchesJson);                forward = mapping.findForward("view");            } else if ("apply_patches".equalsIgnoreCase(action)) {               // System.out.println("Debug- Called apply_patches flow -- ");                String machinePatchgrps = request.getParameter("machinepatchgroups");                String[] patchGroupList = machinePatchgrps.split(",");                StringBuffer sb = new StringBuffer();                List<String> machinesList = new ArrayList<String>();                for (int i = 0; i < patchGroupList.length; i++) {                  //  System.out.println("Selected Machine with Patch Group: " + patchGroupList[i]);                    machinesList.add(patchGroupList[i]);                }                initMitigateConfig();                ConfigProps config = getMitigateConfig();                if (config != null) {                    String masterTx = config.getProperty("mastertx.url");                    String policyMgrUrl = config.getProperty("policymanager.channel.url");                    String runchannelDir = config.getProperty("tunerworkspace.runchannelexe.location");                    String cmsUser = config.getProperty("console.login.user");                    String cmsPwd = config.getProperty("console.login.password");                    Map<String, String> patchDeployResult = executeMitigateCLI(machinesList, policyMgrUrl, masterTx, runchannelDir, cmsUser, cmsPwd);                    String resultDeployJSON = makeMitigateResultJSON(patchDeployResult);                    sendJSONResponse(response, resultDeployJSON);                }            } else if ("patch_applied".equalsIgnoreCase(action) || "patch_notapplied".equalsIgnoreCase(action)                    || "patch_notscanned".equalsIgnoreCase(action)) {                List<PatchComplianceStatusBean> pcsInfoList = dashboardHandler.getPatchComplianceStatusInfo(action);                System.out.println("DebugInfo: pcsInfoList ===> " + pcsInfoList);                dashboardForm.setPatchComplianceStatusList(pcsInfoList);            }            } catch (Exception ex) {                ex.printStackTrace();            }        }        private ConfigProps getMitigateConfig() {            return config;        }                private void initMitigateConfig() {            File rootDir = main.getDataDirectory();            if (rootDir != null && rootDir.isDirectory()) {                File configFile = new File(rootDir, "mitigate_config.txt");                try {                    if (!configFile.exists()) {                        config = new ConfigProps(configFile);                        config.setProperty("mastertx.url", "http://localhost:5282");                        config.setProperty("policymanager.channel.url", "");                        config.setProperty("tunerworkspace.runchannelexe.location", "");                        config.setProperty("console.login.user", "");                        config.setProperty("console.login.password", "");                        if (!config.save()) {                            throw new Exception("Failed to save mitigate configurations");                        }                        config.close();                    } else {                        config = new ConfigProps(configFile);                    }                } catch (Exception ioe) {                    ioe.printStackTrace();                    // REMIND, log something here                    config = null;                }            }        }        public String getCommandline(Vector args) {            StringBuffer command = new StringBuffer();            Enumeration e = args.elements();            while (e.hasMoreElements()) {                String arg = (String)e.nextElement();                if (arg.indexOf(" ") > 0) {                    command.append("\"");                    command.append(arg);                    command.append("\"");                } else {                    command.append(arg);                }                if (e.hasMoreElements()) {                    command.append(" ");                }            }            return command.toString();        }        private Map<String, String> executeMitigateCLI(List<String> machinePatchGroupList, String policyMgrUrl, String masterTx,                                                        String runChannelDir, String cmsUser, String cmsPwd) {            Map<String, String> deployResult = new LinkedHashMap<String, String>();            try {                int size = machinePatchGroupList.size();                String machineName="";                String patchGroupName = "";                boolean resultCLI = false;                for (int i = 0; i < size; i++) {                    String machinePatchGrp = machinePatchGroupList.get(i);                    String[] data = machinePatchGrp.split("@");                    System.out.println("\tMachine: " + data[0] + "\t" + "patchgroup: " + data[1]);                    machineName = data[0];                    patchGroupName = data[1];                    Vector args = new Vector();                    args.addElement(runChannelDir + "\\" + "runchannel.exe");                    args.addElement(policyMgrUrl);                    args.addElement("-patchsubscribe");                    args.addElement(machineName);                    args.addElement("machine");                    args.addElement(masterTx + "/PatchManagement/PatchGroups/" + patchGroupName + "=assign");                    args.addElement("-user");                    args.addElement("\"" + cmsUser + "\"");                    args.addElement("-password");                    args.addElement("\"" + cmsPwd + "\"");                    String cmdLineStr = getCommandline(args);                    System.out.println("DebugInfo: Mitigate CLI String ==> " +cmdLineStr);                    ProcessBuilder builder = new ProcessBuilder("cmd.exe", cmdLineStr);                    builder.redirectErrorStream(true);                    Process process = builder.start();                    BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));                    String line;                                        while (true) {                        line = r.readLine();                        if (line == null) {                            break;                        }                        if (line != null && line.indexOf("Policy Manager succeeded") != -1) {                            resultCLI = true;                        }                        System.out.println(" Command execution result :: " + line);                    }                    int exitCode = process.waitFor();                    System.out.println("CLI Process Exited with code : " + exitCode);                    String resultStr = (resultCLI) ? "success" : "failed";                     deployResult.put(machineName, (patchGroupName +"@"+resultStr));                }            } catch (IOException ioe) {                ioe.printStackTrace();            } catch (InterruptedException ie) {                ie.printStackTrace();            } catch (Exception ex) {                ex.printStackTrace();            }            return deployResult;        }        protected void sendJSONResponse(HttpServletResponse response, String jsonData) throws Exception {            PrintWriter out = response.getWriter();            out.println(jsonData);            out.flush();        }        private String makeMitigateResultJSON(Map<String, String> deployResult) {        String jsonData = "[";            int idx = 0;             int size = deployResult.size();            try {                StringBuffer sb =  new StringBuffer();            for(Map.Entry entry:deployResult.entrySet()) {                String machineName = (String) entry.getKey();                String valStr = (String) entry.getValue();                String[] resultData = valStr.split("@");                sb.append("{");                sb.append("\"Machine\"");sb.append(":");                sb.append("\"");sb.append(machineName);sb.append("\"");                sb.append(",");                sb.append("\"PatchGroup\"");sb.append(":");                sb.append("\"");sb.append(resultData[0]);sb.append("\"");                sb.append(",");                sb.append("\"ResultStatus\"");sb.append(":");                sb.append("\"");sb.append(resultData[1]);sb.append("\"");                sb.append("}");                if (idx == (size-1)) {                    break;                } else {                    idx++;                    sb.append(",");                }            }                jsonData = jsonData + sb.toString() + "]";            }catch(Exception ex) {                ex.printStackTrace();            }            return jsonData;        }                private String makeMitigatePatchesJsonData(List<MitigatePatchesBean> mitigatePatchesList) {            String jsonData = "[";            try {                int size = mitigatePatchesList.size();                StringBuffer sb =  new StringBuffer();                for(int i = 0; i < size; i++) {                    sb.append("{");                    MitigatePatchesBean mitigatePatchBean = mitigatePatchesList.get(i);                    sb.append("\"Impacted Machine\"");sb.append(":");                    sb.append("\"");sb.append(mitigatePatchBean.getMachineName());sb.append("\"");                    sb.append(",");                    sb.append("\"Status\"");sb.append(":");                    sb.append("\"");sb.append(mitigatePatchBean.getStatus());sb.append("\"");                    sb.append(",");                    sb.append("\"Patch Details\"");sb.append(":");                    sb.append("\"");sb.append(mitigatePatchBean.getPatchGroupName());sb.append("\"");                    sb.append("}");                    if (i == (size-1)) {                        break;                    } else {                        sb.append(",");                    }                }                jsonData = jsonData + sb.toString() + "]";              //  System.out.println("DebugInfo: Mitigate Patches JSON: "+jsonData);            } catch (Exception ex) {                ex.printStackTrace();            }            return jsonData;        }        private String makeReportingNotCheckedInJsonData(List<ReportingNotCheckedInBean> rptNotCheckedInList) {            String jsonData = "[";            try {                int size = rptNotCheckedInList.size();                StringBuffer sb =  new StringBuffer();                for(int i = 0; i < size; i++) {                    sb.append("{");                    ReportingNotCheckedInBean rptNotCheckedInBean = rptNotCheckedInList.get(i);                    sb.append("\"Machine Name\"");sb.append(":");                    sb.append("\"");sb.append(rptNotCheckedInBean.getHostName());sb.append("\"");                    sb.append(",");                    sb.append("\"Vulnerable Last ScanTime\"");sb.append(":");                    sb.append("\"");sb.append(rptNotCheckedInBean.getScanTime());sb.append("\"");                    sb.append("}");                    if (i == (size-1)) {                        break;                    } else {                        sb.append(",");                    }                }                jsonData = jsonData + sb.toString() + "]";               // System.out.println("DebugInfo: ReportNotCheckedInInfo JSON: "+jsonData);            } catch (Exception ex) {                ex.printStackTrace();            }            return jsonData;        }        private String makeVulnerableSeverityJsonData(Map<String, String> vulSeverityInfo) {            String jsonData = "[";            List<String> criticalDataSet = new ArrayList<String>();            List<String> highDataSet = new ArrayList<String>();            List<String> mediumDataSet = new ArrayList<String>();            List<String> lowDataSet = new ArrayList<String>();            try {                for(Map.Entry entry:vulSeverityInfo.entrySet()) {                    String keyStr = (String) entry.getKey();                    String valStr = (String) entry.getValue();                  //  System.out.println("key: " + keyStr + ", value :" +valStr);                    if (keyStr != null && keyStr.indexOf("Critical") != -1) {                        criticalDataSet.add(valStr);                    } else if (keyStr != null && keyStr.indexOf("High") != -1) {                        highDataSet.add(valStr);                    } else if (keyStr != null && keyStr.indexOf("Medium") != -1) {                        mediumDataSet.add(valStr);                    } else if (keyStr != null && keyStr.indexOf("Low") != -1) {                        lowDataSet.add(valStr);                    }                }                                int size = criticalDataSet.size();                StringBuffer sb =  new StringBuffer();                if (size > 0) {                    sb.append("{");                    sb.append("\"label\": \"Critical\"");                    sb.append(",");                    sb.append("\"data\":");                    sb.append("[");                for(int i = 0; i < size; i++) {                    String valStr = criticalDataSet.get(i);                    String[] dataVal = valStr.split(",");                    String y = dataVal[0];                    String x = dataVal[1];                    sb.append("{");                    sb.append("\"y\": ");sb.append(y);sb.append(",");sb.append("\"x\": ");sb.append(x);                    sb.append("}");                    if (i == (size-1)) {                        sb.append("]");                        break;                    } else {                        sb.append(",");                    }                }                   sb.append(",");                   sb.append("\"backgroundColor\": \"#FF5F60\"");sb.append(",");                   sb.append("\"pointRadius\": 5");sb.append(",");                   sb.append("\"pointHoverRadius\": 7");                   sb.append("}");                }                size = highDataSet.size();                if (size > 0) {                    sb.append(",");                    sb.append("{");                    sb.append("\"label\": \"High\"");                    sb.append(",");                    sb.append("\"data\":");                    sb.append("[");                    for(int i = 0; i < size; i++) {                        String valStr = highDataSet.get(i);                        String[] dataVal = valStr.split(",");                        String y = dataVal[0];                        String x = dataVal[1];                        sb.append("{");                        sb.append("\"y\": ");sb.append(y);sb.append(",");sb.append("\"x\": ");sb.append(x);                        sb.append("}");                        if (i == (size-1)) {                            sb.append("]");                            break;                        } else {                            sb.append(",");                        }                    }                    sb.append(",");                    sb.append("\"backgroundColor\": \"#D4733A\"");sb.append(",");                    sb.append("\"pointRadius\": 5");sb.append(",");                    sb.append("\"pointHoverRadius\": 7");                    sb.append("}");                }                size = mediumDataSet.size();                if (size > 0) {                    sb.append(",");                    sb.append("{");                    sb.append("\"label\": \"Medium\"");                    sb.append(",");                    sb.append("\"data\":");                    sb.append("[");                    for(int i = 0; i < size; i++) {                        String valStr = mediumDataSet.get(i);                        String[] dataVal = valStr.split(",");                        String y = dataVal[0];                        String x = dataVal[1];                        sb.append("{");                        sb.append("\"y\": ");sb.append(y);sb.append(",");sb.append("\"x\": ");sb.append(x);                        sb.append("}");                        if (i == (size-1)) {                            sb.append("]");                            break;                        } else {                            sb.append(",");                        }                    }                    sb.append(",");                    sb.append("\"backgroundColor\": \"#F3CC63\"");sb.append(",");                    sb.append("\"pointRadius\": 5");sb.append(",");                    sb.append("\"pointHoverRadius\": 7");                    sb.append("}");                }                size = lowDataSet.size();                if (size > 0) {                    sb.append(",");                    sb.append("{");                    sb.append("\"label\": \"Low\"");                    sb.append(",");                    sb.append("\"data\":");                    sb.append("[");                    for(int i = 0; i < size; i++) {                        String valStr = lowDataSet.get(i);                        String[] dataVal = valStr.split(",");                        String y = dataVal[0];                        String x = dataVal[1];                        sb.append("{");                        sb.append("\"y\": ");sb.append(y);sb.append(",");sb.append("\"x\": ");sb.append(x);                        sb.append("}");                        if (i == (size-1)) {                            sb.append("]");                            break;                        } else {                            sb.append(",");                        }                    }                    sb.append(",");                    sb.append("\"backgroundColor\": \"#71DCEB\"");sb.append(",");                    sb.append("\"pointRadius\": 5");sb.append(",");                    sb.append("\"pointHoverRadius\": 7");                    sb.append("}");                }                jsonData = jsonData + sb.toString() + "]";               //System.out.println("DebugInfo: Ageing VulnerabilitiesBySeverity JSON: "+jsonData);            } catch (Exception ex) {                ex.printStackTrace();            }            return jsonData;        }                private String makeTopVulnerabilitiesJsonData(List<TopVulnerableStatusBean> topVulList) {            String jsonData = "[";            try {               int size = topVulList.size();               StringBuffer sb =  new StringBuffer();               for(int i = 0; i < size; i++) {                   sb.append("{");                   TopVulnerableStatusBean topVulbean = topVulList.get(i);                   sb.append("\"CVE-ID\"");sb.append(":");                   sb.append("\"");sb.append(topVulbean.getCveId());sb.append("\"");                   sb.append(",");                   sb.append("\"Severity\"");sb.append(":");                   sb.append("\"");sb.append(topVulbean.getSeverity());sb.append("\"");                   sb.append(",");                   sb.append("\"Impacted Machines\"");sb.append(":");                   sb.append(topVulbean.getAffectedMachines());                   sb.append(",");                   sb.append("\"Patches\"");sb.append(":");                   sb.append("\"");sb.append(topVulbean.getPatchId());sb.append("\"");                   sb.append(",");                   sb.append("\"Status\"");sb.append(":");                   sb.append("\"");sb.append(topVulbean.getStatus());sb.append("\"");                   sb.append("}");                   if (i == (size-1)) {                       break;                   } else {                       sb.append(",");                   }               }               jsonData = jsonData + sb.toString() + "]";             //  System.out.println("DebugInfo: Top Vulnerabilities JSON: "+jsonData);            } catch (Exception ex) {                ex.printStackTrace();            }            return jsonData;        }        private String makePriorityPatchesJsonData(List<PriorityPatchesBean> prtyPatchesList) {            String jsonData = "[";            try {                int size = prtyPatchesList.size();                StringBuffer sb =  new StringBuffer();                for(int i = 0; i < size; i++) {                    sb.append("{");                    PriorityPatchesBean prtyPatchBean = prtyPatchesList.get(i);                    sb.append("\"Patch Name\"");sb.append(":");                    sb.append("\"");sb.append(prtyPatchBean.getPatchName());sb.append("\"");                    sb.append(",");                    sb.append("\"Severity\"");sb.append(":");                    sb.append("\"");sb.append(prtyPatchBean.getSeverity());sb.append("\"");                    sb.append(",");                    sb.append("\"Affected Machines\"");sb.append(":");                    sb.append(prtyPatchBean.getAffectedMachines());                    sb.append("}");                    if (i == (size-1)) {                        break;                    } else {                        sb.append(",");                    }                }                jsonData = jsonData + sb.toString() + "]";              //  System.out.println("DebugInfo: Priority Patches JSON: "+jsonData);            } catch (Exception ex) {                ex.printStackTrace();            }            return jsonData;        }        public String getWaitMessage() {            return getString("page.global.processing");        }    }}