package com.marimba.apps.subscriptionmanager.webapp.forms;

/*  Copyright 2009, BMC Software. All Rights Reserved.
    Confidential and Proprietary Information of BMC Software.
    Protected by or for use under one or more of the following patents:
    U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
    and 6,430,608. Other Patents Pending.

    $File$, $Revision$, $Date$

     Get and set selected target Details

     @author Selvaraj Jegatheesan
     @version 4, 2009/03/16
*/
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

public class TargetDetailsViewForm
        extends AbstractForm {


    private ArrayList targetsList = new ArrayList(100);
    private String id = null;
    private String type = null;
    private String name = null;
    private String[] selectedTargets = new String[0];
    private String taskid = null;

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


}
