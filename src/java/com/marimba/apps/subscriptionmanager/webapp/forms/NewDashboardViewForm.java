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

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;


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

}


