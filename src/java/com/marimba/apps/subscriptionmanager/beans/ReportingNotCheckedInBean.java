// Copyright 2019-2022, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.beans;

import java.lang.*;


/**
 *  ReportingNotCheckedInBean
 *  w.r.t show ReportingNotCheckedIn Info
 *
 * @author Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 */


public class ReportingNotCheckedInBean {
    private String hostName = "";
    private  String scanTime = "";

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getScanTime() {
        return scanTime;
    }

    public void setScanTime(String scanTime) {
        this.scanTime = scanTime;
    }


}


