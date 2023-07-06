// Copyright 2019-2022, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$

package com.marimba.apps.securitymgr.compliance;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.beans.*;
import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;

/**
 *  DashboardHandler Class
 *  w.r.t fetch new dashboard data from DB
 *
 * @author Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 */


public class DashboardHandler implements ComplianceConstants {

    SubscriptionMain main;

    public DashboardHandler(SubscriptionMain main) {
        this.main = main;
    }

    //Controller for total number of machine
    public int getEnrolledMachines(String targetId) {
        int count = 0;
        try {
            count = new DashboardInfoDetails.GetAllEndpointMachineCount(main, targetId).getMachinesCount();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return count;
    }

    //Controller for total number of machine reported last 24 Hrs
    public int getLast24HourEnrolledMachineCount(String targetId) {
        int count = 0;
        try {

            count = new DashboardInfoDetails.GetLast24HourMachineDetails(main, targetId).getMachinesCount();

        } catch (Exception ed) {
            ed.printStackTrace();
        }
        return count;
    }

    //Controller for total number of machine by OS type
    public int getEnrolledMachinesByOS(String osType) {
        int count = 0;
        try {

            count = new DashboardInfoDetails.GetAllEndpointMachineCount(main, osType).getMachinesCount();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return count;
    }

    //total number of machine by VScan
    public int getVScanMachinesCount(String scanType) {
        int count = 0;
        try {

            count = new DashboardInfoDetails.GetScanEndPointMachineCount(main, scanType).getScanCount();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return count;
    }

    //total number of machine by Patch Scan
    public int getPatchScanMachinesCount(String scanType) {
        int count = 0;
        try {

            count = new DashboardInfoDetails.GetScanEndPointMachineCount(main, scanType).getScanCount();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return count;
    }

    // Get VulnerableStatsInfo
    public Map<String, String> getVulnerableStatsInfo() {
        Map<String, String> vulStatsInfo =  new LinkedHashMap<String, String>();
        try {

            vulStatsInfo = new DashboardInfoDetails.GetVulnerableStatisticsInfo(main).getVulnerableStatsInfo();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return vulStatsInfo;
    }


    // Get Aging Report Vulnerabilities based on Severity
    public Map<String, String> getAgingVulnerableBySeverityWise() {
        Map<String, String> vulSeverityInfo =  new LinkedHashMap<String, String>();
        try {

            vulSeverityInfo = new DashboardInfoDetails.GetAgeingVulnerableBySeverityInfo(main).getVulnerableSeverityInfo();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return vulSeverityInfo;
    }

    // Get Patch Compliance Status Info after mitigate operation
    public List<PatchComplianceStatusBean> getPatchComplianceStatusInfo(String action) {
        List<PatchComplianceStatusBean> patchComplianceStatusInfo =  new ArrayList<PatchComplianceStatusBean>();
        try {

            patchComplianceStatusInfo = new DashboardInfoDetails.GetPatchComplianceStatusInfo(main, action).getPatchComplianceStatusInfo();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return patchComplianceStatusInfo;
    }

    // Get Top Vulnerabilities Info
    public List<TopVulnerableStatusBean> getTopVulnerabilitiesInfo() {
        List<TopVulnerableStatusBean> topVulInfo =  new ArrayList<TopVulnerableStatusBean>();
        try {

            topVulInfo = new DashboardInfoDetails.GetTopVulnerabilitiesInfo(main).getTopVulnerabilitiesInfo();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return topVulInfo;
    }


    // Get Priority Patches Info
    public List<PriorityPatchesBean> getPriorityPatchesInfo() {
        List<PriorityPatchesBean> prtyPatchesInfo =  new ArrayList<PriorityPatchesBean>();
        try {

            prtyPatchesInfo = new DashboardInfoDetails.GetPriorityPatchesInfo(main).getPriorityPatchesInfo();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return prtyPatchesInfo;
    }

    // Get Priority Patches Info
    public List<MitigatePatchesBean> getMitigatePatchesInfo(String repIds) {
        List<MitigatePatchesBean> mitigatePatchesInfo =  new ArrayList<MitigatePatchesBean>();
        try {

            mitigatePatchesInfo = new DashboardInfoDetails.GetMitigatePatchesInfo(main, repIds).getMitigatePatchesInfo();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return mitigatePatchesInfo;
    }

    // Get Compliance Reporting, Security and Patch data
    public Map<String, String> getComplianceType(String complianceType) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        try {
            result = new DashboardInfoDetails.GetComplianceReportingData(main, complianceType).getResult();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    // Get Compliance Reporting NotChecked info
    public List<ReportingNotCheckedInBean> getComplianceReportNotCheckedIn() {
        List<ReportingNotCheckedInBean> rptNotCheckedIn = new ArrayList<ReportingNotCheckedInBean>();
        try {
            rptNotCheckedIn = new DashboardInfoDetails.GetComplianceReportNotCheckedInData(main).getReportNotCheckedInInfo();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rptNotCheckedIn;
    }

    // Get channel store user and password
    public Map<String, String> getChannelStoreCredentials(String username, String password) {
        Map<String, String> channelStoreCreds =  new LinkedHashMap<String, String>();
        try {

            channelStoreCreds = new DashboardInfoDetails.GetChannelStoreCredentialsInfo(main, username, password).getChannelStoreCredentialsInfo();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return channelStoreCreds;
    }

    //Controller for total number of scurity scanners in use
    public int getSecuirtyInUseCount(String targetId) {
        int count = 0;

        try {

            count = new DashboardInfoDetails.GetSecurityInUseDetails(main, targetId).getSecurityCount();

        } catch (Exception ed) {

            ed.printStackTrace();
        }

        return count;
    }


    public int getOverallCompliantMachineCount(String targetId) {
        int count = 0;
        try {
            count = new DashboardInfoDetails.GetOverallComplianceDetails(main, targetId, COMPLAINT).getCount();
        } catch (Exception ed) {
            ed.printStackTrace();
        }
        return count;
    }

    public int getOverallNonCompliantCount(String targetId) {

        int count = 0;

        try {

            count = new DashboardInfoDetails.GetOverallComplianceDetails(main, targetId, NON_COMPLAINT).getCount();

        } catch (Exception ed) {

            ed.printStackTrace();

        }

        return count;

    }

}



