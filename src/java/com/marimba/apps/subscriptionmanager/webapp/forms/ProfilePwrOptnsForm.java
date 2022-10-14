// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.forms;

/**
 * Created by IntelliJ IDEA.
 * User: BARATH
 * Date: Sept 3rd, 2009
 * Time: 6:57:22 PM
 * To change this template use File | Settings | File Templates.
 */

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import com.marimba.webapps.tools.util.KnownActionError;
import com.marimba.tools.config.*;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

public class ProfilePwrOptnsForm extends AbstractForm {
    private File theFile;
    private String action;
    String create = "false";
    String name;
    String descr;
    String selected = "false";
    Hashtable affectedTargets = new Hashtable();
    boolean targetsAffected = false;
    String status = "";
    String statusDesc = "";
    FormFile file;

    public void initialize() {
        super.initialize();
        setStatus("");
        setStatusDesc("");
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return this.action;
    }

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        System.out.println("ActionDebug: Reset method called(mapping, request)");
        setStatus("");
        setStatusDesc("");
    }

    public void reset() {
        setName("");
        setDescription("");
        setValue(HIBERNATE_PROP, "false");
        setValue(HIBER_IDLETIME_PROP, POWER_IDLE_DEFAULT_VALUE);
        setValue(MONITOR_IDLETIME_PROP, POWER_IDLE_DEFAULT_VALUE);
        setValue(DISK_IDLETIME_PROP, POWER_IDLE_DEFAULT_VALUE);
        setValue(STANDBY_IDLETIME_PROP, POWER_IDLE_DEFAULT_VALUE);
        setValue(HIBER_IDLETIME_DC_PROP, POWER_IDLE_DEFAULT_VALUE);
        setValue(MONITOR_IDLETIME_DC_PROP, POWER_IDLE_DEFAULT_VALUE);
        setValue(DISK_IDLETIME_DC_PROP, POWER_IDLE_DEFAULT_VALUE);
        setValue(STANDBY_IDLETIME_DC_PROP, POWER_IDLE_DEFAULT_VALUE);
        setValue(PROMPT_PASSWORD_PROP, "false");
        setValue(FORCE_APPLY_PROP, "false");
        targetsAffected = false;
    }

    public FormFile getFile() {
        return file;
    }

    public void setFile(FormFile file) {
        this.file = file;
    }

    public void loadProfile(Hashtable props) {
        reset();
        setName((String) props.remove(POWER_PROFILE_NAME));
        String desc = (String) props.remove(POWER_PROFILE_DESC);
        if (desc != null) {
            setDescription(desc);
        }

        for (Enumeration elements = props.keys();elements.hasMoreElements();) {
            String key = (String) elements.nextElement();
            setValue(key, (String) props.get(key));
        }
    }

    private String getValue(ConfigProps prop, String propName, String defValue) {
        String value = prop.getProperty(propName);
        return (value != null) ? value:defValue;
    }

    public File getTheFile() {
        return theFile;
    }

    public void setTheFile(File theFile) {
        this.theFile = theFile;
    }

    public String getCreate() {
	return create;
    }

    public void setCreate(String create) {
	this.create = create;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getDescription() {
	return descr;
    }

    public void setDescription(String description) {
	this.descr = description;
    }

    public String getSelected() {
	return selected;
    }

    public void setSelected(String selected) {
	this.selected = selected;
    }

    public ActionErrors validate(ActionMapping mapping,
                                 HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if (("save".equals(request.getParameter("action")))) {
            if (((name == null) || (name.trim().length() < 1))) {
                errors.add("Profile Name", new KnownActionError(VALIDATION_POWERPROFILE_EMPTY));
            } else if (DEFAULT_SCHEME_8_0.equals(name.trim())) {
                errors.add("Profile Name", new KnownActionError(VALIDATION_POWERPROFILE_DEFAULT_NAME));
            } else {
                // Remove multiple space in between two words.
                name = name.replaceAll("\\s+", " ");
                
                if (!Pattern.matches("[\\w\\s+]+", name)) {
                    errors.add("Profile Name", new KnownActionError(VALIDATION_POWERPROFILE_SPLCHAR));
                }
            }
        }
        return errors;

    }

    public void setAffectedTargets(Hashtable targets) {
        affectedTargets = targets;

        if (affectedTargets != null && affectedTargets.size() > 0) {
            targetsAffected = true;
        } else {
            targetsAffected = false;
        }
    }

    public Set getAffectedTargetsSet() {
        return affectedTargets.keySet();
    }

    public Hashtable getAffectedTargetsHash() {
        return affectedTargets;
    }

    public boolean getTargetsAffected() {
        return targetsAffected;
    }

    public void setTargetsAffected(boolean targetsAffected) {
        this.targetsAffected = targetsAffected;
    }
}