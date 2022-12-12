// Copyright 2019-2022, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$

package com.marimba.apps.securitymgr.compliance;

import java.util.ArrayList;
import java.util.List;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
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



