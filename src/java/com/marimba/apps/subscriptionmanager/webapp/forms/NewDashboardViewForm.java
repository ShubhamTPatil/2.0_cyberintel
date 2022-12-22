package com.marimba.apps.subscriptionmanager.webapp.forms;

// Copyright 2019-2022, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

// $File$, $Revision$, $Date$

 /**
  *	  New Dashbooard View Form
  *   @author Nandakumar Sankaralingam
  *   Version: $Revision$, $Date$
  *
 **/

import com.marimba.apps.subscriptionmanager.beans.PriorityPatchesBean;
import com.marimba.apps.subscriptionmanager.beans.ReportingNotCheckedInBean;
import com.marimba.apps.subscriptionmanager.beans.TopVulnerableStatusBean;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;


import com.marimba.apps.subscriptionmanager.beans.VulnerableStatusBean;
import java.io.*;
import java.net.*;

import java.util.*;
import javax.servlet.http.HttpServletRequest;
import com.marimba.apps.subscription.common.*;
import com.marimba.apps.subscriptionmanager.webapp.forms.*;
import com.marimba.webapps.intf.IMapProperty;


public class NewDashboardViewForm

        extends AbstractForm {

    private ArrayList targetsList = new ArrayList(100);

    private String id = null;
    private String type = null;
    private String name = null;

    private String[] selectedTargets = new String[0];

    private String taskid = null;
    private String action = null;
    private String machinesCount = "0";
    private String last24hourCount = "0";
    private String machineWindowsCount = "0";
    private String machineLinuxCount = "0";
    private String machineMacCount = "0";
    private String vscanCount = "0";
    private String patchScanCount = "0";
    private String pieChartData;
    private VulnerableStatusBean vulnerableStatusBean;
    private String totalVulnerable;
    private List<TopVulnerableStatusBean> topVulnerableList = new ArrayList<TopVulnerableStatusBean>();
    private List<PriorityPatchesBean> priorityPatchesList = new ArrayList<PriorityPatchesBean>();
    private List<ReportingNotCheckedInBean> rptNotCheckedInList = new ArrayList<ReportingNotCheckedInBean>();
    private String topVulnerableData = "[]";
    private String priorityPatchesData = "[]";
    private String reportNotCheckedInData = "[]";

    private String reportingCheckedIn = "0";
    private String reportingNotCheckedIn = "0";
    private String reportingNotAvailable = "0";
    private String securityCompliant = "0";
    private String securityNonCompliant = "0";
    private String patchCompliant = "0";
    private String patchNonCompliant = "0";
    private String vulnerableSeverityData = "[]";
    

    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }



    public void setType(String type) {

        this.type = type;

    }

    public String getName() {
        return name;
    }



    public void setName(String name) {
        this.name = name;
    }



    public ArrayList getTargetsList() {
		return targetsList;
	}



    public void setTargetsList(Object targetList) {
        this.targetsList.add(targetList);
    }



    public String[] getSelectedTargets() {
        return selectedTargets;
    }


    public void setSelectedTargets(String[] selectedTargets) {
        this.selectedTargets = selectedTargets;
    }



    public String getTaskid() {

        return taskid;

    }

    public void setTaskid(String taskid) {
        this.taskid = taskid;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }


    public String getMachinesCount() {
        return machinesCount;
    }

    public void setMachinesCount(String machinesCount) {
        this.machinesCount = machinesCount;
    }

    public String getLast24hourCount() {
        return last24hourCount;
    }

    public void setLast24hourCount(String last24hourCount) {
        this.last24hourCount = last24hourCount;
    }

    public String getMachineWindowsCount() {
        return machineWindowsCount;
    }

    public void setMachineWindowsCount(String machineWindowsCount) {
        this.machineWindowsCount = machineWindowsCount;
    }

    public String getMachineLinuxCount() {
        return machineLinuxCount;
    }

    public void setMachineLinuxCount(String machineLinuxCount) {
        this.machineLinuxCount = machineLinuxCount;
    }

    public String getMachineMacCount() {
        return machineMacCount;
    }

    public void setMachineMacCount(String machineMacCount) {
        this.machineMacCount = machineMacCount;
    }

    public String getVscanCount() {
        return vscanCount;
    }

    public void setVscanCount(String vscanCount) {
        this.vscanCount = vscanCount;
    }

    public String getPatchScanCount() {
        return patchScanCount;
    }

    public void setPatchScanCount(String patchScanCount) {
        this.patchScanCount = patchScanCount;
    }


    public String getPieChartData() {
        return pieChartData;
    }

    public void setPieChartData(String pieChartData) {
        this.pieChartData = pieChartData;
    }

    public VulnerableStatusBean getVulnerableStatusBean() {
        return vulnerableStatusBean;
    }

    public void setVulnerableStatusBean(VulnerableStatusBean vulnerableStatusBean) {
        this.vulnerableStatusBean = vulnerableStatusBean;
    }

    public String getTotalVulnerable() {
        return totalVulnerable;
    }

    public void setTotalVulnerable(String totalVulnerable) {
        this.totalVulnerable = totalVulnerable;
    }

    public List<TopVulnerableStatusBean> getTopVulnerableList() {
        return topVulnerableList;
    }

    public void setTopVulnerableList(List<TopVulnerableStatusBean> topVulnerableList) {
        this.topVulnerableList = topVulnerableList;
    }

    public List<PriorityPatchesBean> PriorityPatchesList() {
        return priorityPatchesList;
    }

    public void setPriorityPatchesList(List<PriorityPatchesBean> priorityPatchesList) {
        this.priorityPatchesList = priorityPatchesList;
    }

    public String getTopVulnerableData() {
        return topVulnerableData;
    }

    public void setTopVulnerableData(String topVulnerableData) {
        this.topVulnerableData = topVulnerableData;
    }

    public String getPriorityPatchesData() {
        return priorityPatchesData;
    }

    public void setPriorityPatchesData(String priorityPatchesData) {
        this.priorityPatchesData = priorityPatchesData;
    }

    public String getReportingCheckedIn() {
        return reportingCheckedIn;
    }

    public void setReportingCheckedIn(String reportingCheckedIn) {
        this.reportingCheckedIn = reportingCheckedIn;
    }

    public String getReportingNotCheckedIn() {
        return reportingNotCheckedIn;
    }

    public void setReportingNotCheckedIn(String reportingNotCheckedIn) {
        this.reportingNotCheckedIn = reportingNotCheckedIn;
    }

    public String getReportingNotAvailable() {
        return reportingNotAvailable;
    }

    public void setReportingNotAvailable(String reportingNotAvailable) {
        this.reportingNotAvailable = reportingNotAvailable;
    }

    public String getSecurityCompliant() {
        return securityCompliant;
    }

    public void setSecurityCompliant(String securityCompliant) {
        this.securityCompliant = securityCompliant;
    }

    public String getSecurityNonCompliant() {
        return securityNonCompliant;
    }

    public void setSecurityNonCompliant(String securityNonCompliant) {
        this.securityNonCompliant = securityNonCompliant;
    }

    public String getPatchCompliant() {
        return patchCompliant;
    }

    public void setPatchCompliant(String patchCompliant) {
        this.patchCompliant = patchCompliant;
    }

    public String getPatchNonCompliant() {
        return patchNonCompliant;
    }

    public void setPatchNonCompliant(String patchNonCompliant) {
        this.patchNonCompliant = patchNonCompliant;
    }

    public String getVulnerableSeverityData() {
        return vulnerableSeverityData;
    }

    public void setVulnerableSeverityData(String vulnerableSeverityData) {
        this.vulnerableSeverityData = vulnerableSeverityData;
    }


    public List<ReportingNotCheckedInBean> getRptNotCheckedInList() {
        return rptNotCheckedInList;
    }

    public void setRptNotCheckedInList(List<ReportingNotCheckedInBean> rptNotCheckedInList) {
        this.rptNotCheckedInList = rptNotCheckedInList;
    }


    public String getReportNotCheckedInData() {
        return reportNotCheckedInData;
    }

    public void setReportNotCheckedInData(String reportNotCheckedInData) {
        this.reportNotCheckedInData = reportNotCheckedInData;
    }



}


