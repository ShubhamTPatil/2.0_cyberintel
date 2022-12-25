// Copyright 2019-2022, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.beans;

import java.lang.*;


/**
 *  MitigatePatchesBean
 *  w.r.t show Mitigate Patches Info - Tabular Report
 *
 * @author Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 */


public class MitigatePatchesBean {
    String machineName;
    String patchName;
    String status;
    String patchGroupName;

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getPatchName() {
        return patchName;
    }

    public void setPatchName(String patchName) {
        this.patchName = patchName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPatchGroupName() {
        return patchGroupName;
    }

    public void setPatchGroupName(String patchGroupName) {
        this.patchGroupName = patchGroupName;
    }

}


