// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.beans;

import java.lang.*;


/**
 *  PatchComplianceStatusBean
 *  w.r.t show patch compliance info after mitigate
 *
 * @author Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 */


public class PatchComplianceStatusBean {
    String machineId;
    String machineName;
    String patch;
    String patchGroup;
    String complianceLevel;

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getPatch() {
        return patch;
    }

    public void setPatch(String patch) {
        this.patch = patch;
    }

    public String getPatchGroup() {
        return patchGroup;
    }

    public void setPatchGroup(String patchGroup) {
        this.patchGroup = patchGroup;
    }

    public String getComplianceLevel() {
        return complianceLevel;
    }

    public void setComplianceLevel(String complianceLevel) {
        this.complianceLevel = complianceLevel;
    }
}



