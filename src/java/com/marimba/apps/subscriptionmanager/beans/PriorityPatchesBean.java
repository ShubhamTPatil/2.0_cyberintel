// Copyright 2019-2022, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.beans;

import java.lang.*;


/**
 *  PriorityPatchesBean
 *  w.r.t show Priority Patches Info - Tabular Report
 *
 * @author Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 */


public class PriorityPatchesBean {
    String patchName;
    String severity;
    String affectedMachines;

    public String getPatchName() {
        return patchName;
    }

    public void setPatchName(String patchName) {
        this.patchName = patchName;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getAffectedMachines() {
        return affectedMachines;
    }

    public void setAffectedMachines(String affectedMachines) {
        this.affectedMachines = affectedMachines;
    }


}


