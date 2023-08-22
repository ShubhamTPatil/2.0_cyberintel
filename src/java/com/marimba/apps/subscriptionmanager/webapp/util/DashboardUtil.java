// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

// $File$, $Revision$, $Date$


package com.marimba.apps.subscriptionmanager.webapp.util;

import java.io.*;
import java.util.*;

import com.marimba.apps.subscriptionmanager.beans.MitigatePatchesBean;
import com.marimba.apps.subscriptionmanager.beans.PriorityPatchesBean;
import com.marimba.apps.subscriptionmanager.beans.ReportingNotCheckedInBean;
import com.marimba.apps.subscriptionmanager.beans.TopVulnerableStatusBean;
import com.marimba.tools.util.DebugFlag;

/**
 * Dashboard Util
 * w.r.t make dashboard content as JSON
 *
 * @author Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 */

public class DashboardUtil {

    public static boolean DEBUG = DebugFlag.getDebug("DEFENSIGHT/DASHBOARD") >= 5;

    public String makeTopVulnerabilitiesJsonData(List<TopVulnerableStatusBean> topVulList) {
        String jsonData = "[";
        try {
            int size = topVulList.size();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < size; i++) {
                sb.append("{");
                TopVulnerableStatusBean topVulbean = topVulList.get(i);
                sb.append("\"CVE-ID\"");
                sb.append(":");
                sb.append("\"");
                sb.append(topVulbean.getCveId());
                sb.append("\"");
                sb.append(",");
                sb.append("\"Severity\"");
                sb.append(":");
                sb.append("\"");
                sb.append(topVulbean.getSeverity());
                sb.append("\"");
                sb.append(",");
                sb.append("\"Impacted Machines\"");
                sb.append(":");
                sb.append(topVulbean.getAffectedMachines());
                sb.append(",");
                sb.append("\"Patches\"");
                sb.append(":");
                sb.append("\"");
                sb.append(topVulbean.getPatchId());
                sb.append("\"");
                sb.append(",");
                sb.append("\"Status\"");
                sb.append(":");
                sb.append("\"");
                sb.append(topVulbean.getStatus());
                sb.append("\"");
                sb.append(",");
                sb.append("\"Risk Score\"");
                sb.append(":");
                sb.append("\"");
                sb.append(topVulbean.getRiskScore());
                sb.append("\"");
                sb.append("}");
                if (i == (size - 1)) {
                    break;
                } else {
                    sb.append(",");
                }
            }

            jsonData = jsonData + sb.toString() + "]";

          // System.out.println("DebugInfo: Top Vulnerabilities JSON: "+jsonData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return jsonData;
    }

    public String makePriorityPatchesJsonData(List<PriorityPatchesBean> prtyPatchesList) {
        String jsonData = "[";
        try {
            int size = prtyPatchesList.size();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < size; i++) {
                sb.append("{");
                PriorityPatchesBean prtyPatchBean = prtyPatchesList.get(i);
                sb.append("\"CVE-ID\"");
                sb.append(":");
                sb.append("\"");
                sb.append(prtyPatchBean.getCveid());
                sb.append("\"");
                sb.append(",");
                sb.append("\"Patch Name\"");
                sb.append(":");
                sb.append("\"");
                sb.append(prtyPatchBean.getPatchName());
                sb.append("\"");
                sb.append(",");
                sb.append("\"Severity\"");
                sb.append(":");
                sb.append("\"");
                sb.append(prtyPatchBean.getSeverity());
                sb.append("\"");
                sb.append(",");
                sb.append("\"Affected Machines\"");
                sb.append(":");
                sb.append(prtyPatchBean.getAffectedMachines());
                sb.append(",");
                sb.append("\"Status\"");
                sb.append(":");
                sb.append("\"");
                sb.append(prtyPatchBean.getStatus());
                sb.append("\"");
                sb.append("}");
                if (i == (size - 1)) {
                    break;
                } else {
                    sb.append(",");
                }
            }
            jsonData = jsonData + sb.toString() + "]";

          // System.out.println("DebugInfo: Priority Patches JSON: "+jsonData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return jsonData;
    }

    public String makeVulnerableSeverityJsonData(Map<String, String> vulSeverityInfo) {
        String jsonData = "[";
        List<String> criticalDataSet = new ArrayList<String>();
        List<String> highDataSet = new ArrayList<String>();
        List<String> mediumDataSet = new ArrayList<String>();
        List<String> lowDataSet = new ArrayList<String>();
        try {
            for (Map.Entry entry : vulSeverityInfo.entrySet()) {
                String keyStr = (String) entry.getKey();
                String valStr = (String) entry.getValue();
                //  System.out.println("key: " + keyStr + ", value :" +valStr);
                if (keyStr != null && keyStr.indexOf("Critical") != -1) {
                    criticalDataSet.add(valStr);
                } else if (keyStr != null && keyStr.indexOf("High") != -1) {
                    highDataSet.add(valStr);
                } else if (keyStr != null && keyStr.indexOf("Medium") != -1) {
                    mediumDataSet.add(valStr);
                } else if (keyStr != null && keyStr.indexOf("Low") != -1) {
                    lowDataSet.add(valStr);
                }
            }

            int size = criticalDataSet.size();
            StringBuilder sb = new StringBuilder();
            if (size > 0) {
                sb.append("{");
                sb.append("\"label\": \"Critical\"");
                sb.append(",");
                sb.append("\"data\":");
                sb.append("[");
                for (int i = 0; i < size; i++) {
                    String valStr = criticalDataSet.get(i);
                    String[] dataVal = valStr.split(",");
                    String y = dataVal[0];
                    String x = dataVal[1];
                    sb.append("{");
                    sb.append("\"y\": ");
                    sb.append(y);
                    sb.append(",");
                    sb.append("\"x\": ");
                    sb.append(x);
                    sb.append("}");
                    if (i == (size - 1)) {
                        sb.append("]");
                        break;
                    } else {
                        sb.append(",");
                    }
                }
                sb.append(",");
                sb.append("\"backgroundColor\": \"#FF5F60\"");
                sb.append(",");
                sb.append("\"pointRadius\": 5");
                sb.append(",");
                sb.append("\"pointHoverRadius\": 7");
                sb.append("}");
            }


            size = highDataSet.size();
            if (size > 0) {
                sb.append(",");
                sb.append("{");
                sb.append("\"label\": \"High\"");
                sb.append(",");
                sb.append("\"data\":");
                sb.append("[");
                for (int i = 0; i < size; i++) {
                    String valStr = highDataSet.get(i);
                    String[] dataVal = valStr.split(",");
                    String y = dataVal[0];
                    String x = dataVal[1];
                    sb.append("{");
                    sb.append("\"y\": ");
                    sb.append(y);
                    sb.append(",");
                    sb.append("\"x\": ");
                    sb.append(x);
                    sb.append("}");
                    if (i == (size - 1)) {
                        sb.append("]");
                        break;
                    } else {
                        sb.append(",");
                    }
                }
                sb.append(",");
                sb.append("\"backgroundColor\": \"#D4733A\"");
                sb.append(",");
                sb.append("\"pointRadius\": 5");
                sb.append(",");
                sb.append("\"pointHoverRadius\": 7");
                sb.append("}");
            }

            size = mediumDataSet.size();
            if (size > 0) {
                sb.append(",");
                sb.append("{");
                sb.append("\"label\": \"Medium\"");
                sb.append(",");
                sb.append("\"data\":");
                sb.append("[");
                for (int i = 0; i < size; i++) {
                    String valStr = mediumDataSet.get(i);
                    String[] dataVal = valStr.split(",");
                    String y = dataVal[0];
                    String x = dataVal[1];
                    sb.append("{");
                    sb.append("\"y\": ");
                    sb.append(y);
                    sb.append(",");
                    sb.append("\"x\": ");
                    sb.append(x);
                    sb.append("}");
                    if (i == (size - 1)) {
                        sb.append("]");
                        break;
                    } else {
                        sb.append(",");
                    }
                }
                sb.append(",");
                sb.append("\"backgroundColor\": \"#F3CC63\"");
                sb.append(",");
                sb.append("\"pointRadius\": 5");
                sb.append(",");
                sb.append("\"pointHoverRadius\": 7");
                sb.append("}");
            }

            size = lowDataSet.size();
            if (size > 0) {
                sb.append(",");
                sb.append("{");
                sb.append("\"label\": \"Low\"");
                sb.append(",");
                sb.append("\"data\":");
                sb.append("[");
                for (int i = 0; i < size; i++) {
                    String valStr = lowDataSet.get(i);
                    String[] dataVal = valStr.split(",");
                    String y = dataVal[0];
                    String x = dataVal[1];
                    sb.append("{");
                    sb.append("\"y\": ");
                    sb.append(y);
                    sb.append(",");
                    sb.append("\"x\": ");
                    sb.append(x);
                    sb.append("}");
                    if (i == (size - 1)) {
                        sb.append("]");
                        break;
                    } else {
                        sb.append(",");
                    }
                }
                sb.append(",");
                sb.append("\"backgroundColor\": \"#71DCEB\"");
                sb.append(",");
                sb.append("\"pointRadius\": 5");
                sb.append(",");
                sb.append("\"pointHoverRadius\": 7");
                sb.append("}");
            }

            jsonData = jsonData + sb.toString() + "]";

           //System.out.println("DebugInfo: Ageing VulnerabilitiesBySeverity JSON: "+jsonData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return jsonData;
    }

    public String makeReportingNotCheckedInJsonData(List<ReportingNotCheckedInBean> rptNotCheckedInList) {
        String jsonData = "[";
        try {
            int size = rptNotCheckedInList.size();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < size; i++) {
                sb.append("{");
                ReportingNotCheckedInBean rptNotCheckedInBean = rptNotCheckedInList.get(i);
                sb.append("\"Machine Name\"");
                sb.append(":");
                sb.append("\"");
                sb.append(rptNotCheckedInBean.getHostName());
                sb.append("\"");
                sb.append(",");
                sb.append("\"Vulnerable Last ScanTime\"");
                sb.append(":");
                sb.append("\"");
                sb.append(rptNotCheckedInBean.getScanTime());
                sb.append("\"");
                sb.append("}");
                if (i == (size - 1)) {
                    break;
                } else {
                    sb.append(",");
                }
            }
            jsonData = jsonData + sb.toString() + "]";

           // System.out.println("DebugInfo: ReportNotCheckedInInfo JSON: "+jsonData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return jsonData;
    }

    public String makeMitigatePatchesJsonData(List<MitigatePatchesBean> mitigatePatchesList) {
        String jsonData = "[";
        try {
            int size = mitigatePatchesList.size();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < size; i++) {
                sb.append("{");
                MitigatePatchesBean mitigatePatchBean = mitigatePatchesList.get(i);
                sb.append("\"Impacted Machine\"");
                sb.append(":");
                sb.append("\"");
                sb.append(mitigatePatchBean.getMachineName());
                sb.append("\"");
                sb.append(",");
                sb.append("\"Status\"");
                sb.append(":");
                sb.append("\"");
                sb.append(mitigatePatchBean.getStatus());
                sb.append("\"");
                sb.append(",");
                sb.append("\"Patch Details\"");
                sb.append(":");
                sb.append("\"");
                sb.append(mitigatePatchBean.getPatchGroupName());
                sb.append("\"");
                sb.append("}");
                if (i == (size - 1)) {
                    break;
                } else {
                    sb.append(",");
                }
            }
            jsonData = jsonData + sb.toString() + "]";

            // System.out.println("DebugInfo: Mitigate Patches JSON: "+jsonData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return jsonData;
    }

    public String makeMitigateResultJSON(Map<String, String> deployResult) {
        String jsonData = "[";
        int idx = 0;
        int size = deployResult.size();
        try {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry entry : deployResult.entrySet()) {
                String machineName = (String) entry.getKey();
                String valStr = (String) entry.getValue();
                String[] resultData = valStr.split("@");
                sb.append("{");
                sb.append("\"Machine\"");
                sb.append(":");
                sb.append("\"");
                sb.append(machineName);
                sb.append("\"");
                sb.append(",");
                sb.append("\"PatchGroup\"");
                sb.append(":");
                sb.append("\"");
                sb.append(resultData[0]);
                sb.append("\"");
                sb.append(",");
                sb.append("\"ResultStatus\"");
                sb.append(":");
                sb.append("\"");
                sb.append(resultData[1]);
                sb.append("\"");
                sb.append("}");
                if (idx == (size - 1)) {
                    break;
                } else {
                    idx++;
                    sb.append(",");
                }
            }
            jsonData = jsonData + sb.toString() + "]";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonData;
    }

    public Map<String, String> executeMitigateCLI(List<String> machinePatchGroupList, String policyMgrUrl, String masterTx,
                                                   String runChannelDir, String cmsUser, String cmsPwd) {
        Map<String, String> deployResult = new LinkedHashMap<String, String>();
        try {
            int size = machinePatchGroupList.size();
            String machineName = "";
            String patchGroupName = "";
            boolean resultCLI = false;
            for (int i = 0; i < size; i++) {
                String machinePatchGrp = machinePatchGroupList.get(i);
                String[] data = machinePatchGrp.split("@");
                System.out.println("\tMachine: " + data[0] + "\t" + "patchgroup: " + data[1]);
                machineName = data[0];
                patchGroupName = data[1];

                Vector args = new Vector();
                args.addElement(runChannelDir + "\\" + "runchannel.exe");
                args.addElement(policyMgrUrl);
                args.addElement("-patchsubscribe");
                args.addElement(machineName);
                args.addElement("machine");
                args.addElement(masterTx + "/PatchManagement/PatchGroups/" + patchGroupName + "=assign");
                args.addElement("-user");
                args.addElement("\"" + cmsUser + "\"");
                args.addElement("-password");
                args.addElement("\"" + cmsPwd + "\"");

                String cmdLineStr = getCommandline(args);
                System.out.println("LogInfo: Mitigate CLI: " + cmdLineStr);
                ProcessBuilder builder = new ProcessBuilder("cmd.exe", cmdLineStr);

                builder.redirectErrorStream(true);
                Process process = builder.start();
                BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;

                while (true) {
                    line = r.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line != null && line.indexOf("Policy Manager succeeded") != -1) {
                        resultCLI = true;
                    }
                    System.out.println(" Command execution result :: " + line);
                }
                int exitCode = process.waitFor();
                System.out.println("LogInfo: CLI Process Exited with code : " + exitCode);
                String resultStr = (resultCLI) ? "success" : "failed";
                deployResult.put(machineName, (patchGroupName + "@" + resultStr));
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return deployResult;
    }

    private String getCommandline(Vector args) {
        StringBuffer command = new StringBuffer();
        Enumeration e = args.elements();
        while (e.hasMoreElements()) {
            String arg = (String) e.nextElement();
            if (arg.indexOf(" ") > 0) {
                command.append("\"");
                command.append(arg);
                command.append("\"");
            } else {
                command.append(arg);
            }
            if (e.hasMoreElements()) {
                command.append(" ");
            }
        }
        return command.toString();
    }
    
    public static void debug(String str) {
        if (DEBUG) {
            System.out.println("DashboardUtil.java :: [" + new Date().toString() + "] ==> " + str);
        }
    }

}

