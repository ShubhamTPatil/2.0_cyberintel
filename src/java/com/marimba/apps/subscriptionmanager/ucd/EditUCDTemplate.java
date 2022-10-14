// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.ucd;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscriptionmanager.webapp.forms.UserCentricDeploymentForm;
import com.marimba.intf.msf.AppManagerConstants;
import com.marimba.intf.msf.IDatabaseMgr;
import com.marimba.tools.config.ConfigProps;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * This class used to add or modify UCD template setting in ucd_templates.txt file
 *
 * @author Selvaraj Jegatheesan
 * @version $Revision$,  $Date$ 
 */

public class EditUCDTemplate extends AbstractAction implements ISubscriptionConstants, AppManagerConstants {

    private ConfigProps config;
    private UserCentricDeploymentForm userDeploymentForm;
    File rootDir = null;
    HttpSession session = null;

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        String templateName = null;
        String templateDesc = null;
        String enableLoginHistory = null;
        String workHrs = null;
        String workDays = null;
        String enableLockUnlock = null;
        String enableRemoteLoginMode = null;
        String enableDeviceConfig = null;
        String ramSize = null;
        String diskSpace = null;
        String osName = null;
        String processor = null;
        String templateNamePrefix = "";
        String templates = null;
        String deviceLevel = null;
        ActionErrors errors = new ActionErrors();

        userDeploymentForm = (UserCentricDeploymentForm) form;
        String action = request.getParameter("action");
        init(request);
        rootDir = main.getDataDirectory();
        session = request.getSession();
        debug("Action: " + action);
        loadTemplateConfig();
        loadStaticDeviceLevel();
        loadDeviceConfiguration();
        if(null != action && action.equalsIgnoreCase("addTemplate")) {
            userDeploymentForm.reset();
            userDeploymentForm.setTemplateType("add");
        } else if (null != action && action.equalsIgnoreCase("editTemplate")) {
            Vector<String> selectedTemplate = getSelectedTemplate(request);

            if (selectedTemplate.size() == 1) {
                templateName = (String)selectedTemplate.elementAt(0);
            }

            userDeploymentForm.loadProfile(readProfile(templateName));
            userDeploymentForm.setTemplateType("edit");

        } else if (null != action && action.equalsIgnoreCase("saveTemplate") && null != config) {
            try {
                templateName = userDeploymentForm.getTemplateName().trim();
                templateDesc = userDeploymentForm.getTemplateDesc().trim();
                deviceLevel = userDeploymentForm.getDeviceLevel().trim();

                enableLoginHistory = (String) userDeploymentForm.getValue(UCD_LOGIN_HISTORY);
                if ("on".equals(enableLoginHistory)) {
                    workHrs = (String) userDeploymentForm.getValue(UCD_WORKHRS);
                    workDays = (String) userDeploymentForm.getValue(UCD_WORKDAYS);
                    enableLockUnlock = (String) userDeploymentForm.getValue(UCD_LOCKUNLOCK);
                    enableRemoteLoginMode = (String) userDeploymentForm.getValue(UCD_REMOTELOGINMODE);
                }

                enableDeviceConfig = (String) userDeploymentForm.getValue(UCD_DEVICECONFIG);
                if ("on".equals(enableDeviceConfig)) {
                    if (!"".equals(userDeploymentForm.getValue(UCD_RAMSIZE))) {
                        ramSize =  userDeploymentForm.getRamsizeCondtion()
                                + userDeploymentForm.getValue(UCD_RAMSIZE)
                                + userDeploymentForm.getRamsizeOption();
                    }
                    if (!"".equals(userDeploymentForm.getValue(UCD_DISKSPACE))) {
                        diskSpace = userDeploymentForm.getDisksizeCondtion()
                                + (String) userDeploymentForm.getValue(UCD_DISKSPACE)
                                + userDeploymentForm.getDisksizeOption();
                    }
                    osName = userDeploymentForm.getOsList();
                    processor = userDeploymentForm.getProcessorList();
                }
                String availableTemplates = config.getProperty(UCD_TEMPLATE_NAME);
                if (templateName != null && !templateName.trim().isEmpty()) {
                    templateNamePrefix = templateName + ".";
                    templates = ((availableTemplates != null) &&
                            ((availableTemplates.startsWith(templateName + ",")) ||
                                    (availableTemplates.endsWith("," + templateName)) ||
                                    (availableTemplates.equalsIgnoreCase(templateName)) ||
                                    (availableTemplates.indexOf(","+ templateName+ ",") > -1)))
                            ?  availableTemplates : ((availableTemplates == null) ? templateName : availableTemplates + "," + templateName);
                    config.setProperty(UCD_TEMPLATE_NAME, templates);

                    if (templateDesc != null && !templateDesc.trim().isEmpty()) {
                        config.setProperty(templateNamePrefix + UCD_TEMPLATE_DESCRIPTION, templateDesc);
                    }
                    if (deviceLevel != null && !deviceLevel.trim().isEmpty()) {
                        config.setProperty(templateNamePrefix + UCD_DEVICELEVEL, deviceLevel);
                    } else {
                        config.setProperty(templateNamePrefix + UCD_DEVICELEVEL, UCD_DEFAULT_DEVICE_LEVEL);
                    }
                    // Consider Login History
                    if ("on".equals(enableLoginHistory)) {
                        config.setProperty(templateNamePrefix + UCD_LOGIN_HISTORY, "true");
                        config.setProperty(templateNamePrefix + UCD_WORKHRS, workHrs);
                        config.setProperty(templateNamePrefix + UCD_WORKDAYS, workDays);
                        if ("on".equals(enableLockUnlock)) {
                            config.setProperty(templateNamePrefix + UCD_LOCKUNLOCK, "true");
                        } else {
                            config.setProperty(templateNamePrefix + UCD_LOCKUNLOCK, null);
                        }
                        if ("on".equals(enableRemoteLoginMode)) {
                            config.setProperty(templateNamePrefix + UCD_REMOTELOGINMODE, "true");
                        } else {
                            config.setProperty(templateNamePrefix + UCD_REMOTELOGINMODE, null);
                        }
                    } else {
                        config.setProperty(templateNamePrefix + UCD_LOGIN_HISTORY, null);
                        config.setProperty(templateNamePrefix + UCD_WORKHRS, null);
                        config.setProperty(templateNamePrefix + UCD_WORKDAYS, null);
                        config.setProperty(templateNamePrefix + UCD_LOCKUNLOCK, null);
                        config.setProperty(templateNamePrefix + UCD_REMOTELOGINMODE, null);
                    }
                    // Consider Device Configuration
                    boolean isChange = false;
                    if ("on".equals(enableDeviceConfig)) {
                        config.setProperty(templateNamePrefix + UCD_DEVICECONFIG, "true");
                        if(null != ramSize && !"".equals(ramSize)) {
                            isChange = true;
                            config.setProperty(templateNamePrefix + UCD_RAMSIZE, ramSize);
                        } else {
                            config.setProperty(templateNamePrefix + UCD_RAMSIZE, null);
                        }
                        if(null != diskSpace && !"".equals(diskSpace)) {
                            isChange = true;
                            config.setProperty(templateNamePrefix + UCD_DISKSPACE, diskSpace);
                        } else {
                            config.setProperty(templateNamePrefix + UCD_DISKSPACE, null);
                        }
                        if(null != osName && !"".equals(osName)) {
                            isChange = true;
                            config.setProperty(templateNamePrefix + UCD_OSNAME, osName);
                        } else {
                            config.setProperty(templateNamePrefix + UCD_OSNAME, null);
                        }
                        if(null != processor && !"".equals(processor)) {
                            isChange = true;
                            config.setProperty(templateNamePrefix + UCD_PROCESSOR, processor);
                        } else {
                            config.setProperty(templateNamePrefix + UCD_PROCESSOR, null);
                        }
                        if(!isChange) {
                            config.setProperty(templateNamePrefix + UCD_DEVICECONFIG, null);
                        }
                    } else {
                        config.setProperty(templateNamePrefix + UCD_DEVICECONFIG, null);
                        config.setProperty(templateNamePrefix + UCD_RAMSIZE, null);
                        config.setProperty(templateNamePrefix + UCD_DISKSPACE, null);
                        config.setProperty(templateNamePrefix + UCD_OSNAME, null);
                        config.setProperty(templateNamePrefix + UCD_PROCESSOR, null);
                    }

                    if (!config.save()) {
                        debug("Failed to save ucd template settings to ucd_templates.txt file");
                    }
                }
            } catch (Exception ioe) {
                debug("Failed to save ucd template settings to ucd_templates.txt file");
                if (DEBUG5) ioe.printStackTrace();
            } finally {
                if (null != config) config.close();
                config = null;
            }
            userDeploymentForm.setTemplateType("save");
            return mapping.findForward("done");
        }
        return mapping.findForward("load");
    }
    private void loadTemplateConfig() {
        config = new ConfigProps(new File(rootDir, UCD_TEMPLATE_FILENAME));
    }

    private void loadStaticDeviceLevel() {
        ConfigProps subConfig = main.getConfig();
        int maxDeviceLevel = 2;
        if(null != subConfig.getProperty(MAX_UCD_DEVICELEVEL)) {
            try {
                maxDeviceLevel = Integer.parseInt(subConfig.getProperty(MAX_UCD_DEVICELEVEL));
            } catch(NumberFormatException ex) {
                //
            }
        }
        Vector<String> deviceList = new Vector<String>();
        try {
            for(int count = 1;count < maxDeviceLevel+1;count++) {
                deviceList.add(Integer.toString(count));
            }
        } catch(Exception ec) {
            deviceList.add("1");
            deviceList.add("2");
        }
        userDeploymentForm.setDeviceLevelList(deviceList);
    }

    private HashMap<String, String> readProfile(String profileName) {
        HashMap<String, String> props = new HashMap<String, String>(10);
        String profile = config.getProperty(UCD_TEMPLATE_NAME);

        if(profile.equalsIgnoreCase(profileName) || profile.startsWith(profileName + ",") ||
                profile.endsWith("," + profileName) || (profile.indexOf("," + profileName + ",") > -1)) {
            String[] pairs = config.getPropertyPairs();
            for (int i = 0; i < pairs.length;) {
                String key = pairs[i++];
                String value = pairs[i++];
                if (key != null && key.startsWith(profileName+".") && value != null) {
                    props.put(key.substring(profileName.length()+1), value);
                }
            }
        }
        props.put(UCD_TEMPLATE_NAME, profileName);
        return props;
    }

    private Vector<String> getSelectedTemplate(HttpServletRequest req) {
        Vector<String> v = new Vector<String>();
        for (Enumeration<String> e = req.getParameterNames(); e.hasMoreElements(); ) {
            String name = e.nextElement();
            if (name.startsWith("template_sel_")) {
                name = name.substring("template_sel_".length());
                v.addElement(name);
            }
        }
        return v;
    }

    private void loadDeviceConfiguration() {
        IDatabaseMgr dbmgr;
        Dbutils dbutils;
        try {
            dbmgr = (IDatabaseMgr)this.tenant.getDbMgr();
            if(null != dbmgr) {
                dbutils = new Dbutils(dbmgr, dbmgr.getActive("read"));
                if(dbutils.checkDBConn()) {
                    List<String> osNameList = dbutils.getOSNameList();
                    List<String> processorNameList = dbutils.getProcessorNameList();

                    if(null != osNameList && osNameList.size() > 0) {
                        userDeploymentForm.setOsNameList(osNameList);
                    }

                    if(null != processorNameList && processorNameList.size() > 0) {
                        userDeploymentForm.setProcessorNameList(processorNameList);
                    }
                    debug("OS names list: " + osNameList);
                    debug("Processor names list: " + processorNameList);
                } else {
                	System.out.println("DB connection is failed for UCD");
                }
            }

        } catch(Exception ec) {
            if(DEBUG5) {
                ec.printStackTrace();
            }
        }
        setDeviceSessionValue();
    }

    private void setDeviceSessionValue() {
        session.removeAttribute("oslist");
        session.removeAttribute("processorlist");
        session.setAttribute("oslist", userDeploymentForm.getOsNameList());
        session.setAttribute("processorlist", userDeploymentForm.getProcessorNameList());
    }

    private void debug(String msg) {
        if(DEBUG5) System.out.println("EditUCDTemplate: " + msg);
    }
}
