// Copyright 1996-2015, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Subscription;
import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.ucd.Dbutils;
import com.marimba.apps.subscriptionmanager.util.SubscriptionMgrParser;
import static com.marimba.apps.subscriptionmanager.approval.IApprovalPolicyConstants.*;

import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiff;
import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiffMailFormatter;
import com.marimba.apps.subscriptionmanager.webapp.forms.DesktopSecurityProfileForm;
import com.marimba.apps.subscriptionmanager.webapp.system.PowerProfileBean;
import com.marimba.apps.subscriptionmanager.webapp.system.SoftwareDetailsBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.intf.msf.IDatabaseMgr;
import com.marimba.tools.config.ConfigProps;
import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.tools.ldap.LDAPName;
import com.marimba.tools.xml.XMLOutputStream;
import com.marimba.tools.xml.XMLParser;
import com.marimba.tools.xml.XMLException;
import com.marimba.webapps.intf.SystemException;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

import org.json.JSONObject;
import org.json.JSONException;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.io.*;
import java.util.*;

/**
 * DesktopSecurityProfileSaveAction
 *
 * @author Selvaraj Jegatheesan
 * @version $Revision$, $Date$
 */

public class DesktopSecurityProfileSaveAction extends AbstractAction {
	private File rootDir;
    private IUser user;
    String modifiedUserDN = null;
    private ConfigProps config;
    private HttpServletRequest req;
    private String KEY_OLD_SUB = "oldsub";
    private String KEY_NEW_SUB = "newsub";
    private static final String DOT = ".";
    private DesktopSecurityProfileForm profileForm;
    private Map<String, Map<String, ISubscription>> subsMap;


    public ActionForward execute( ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

        System.out.println("DesktopSecurityProfileSaveAction called...");
        req = request;
        profileForm = (DesktopSecurityProfileForm) form;
        String  action = request.getParameter("action");
        String lastAction = null;
        String profileName = null;
        String profileDesc = null;

//        String blockedSoftwares = null;
//        String allowedSoftwares = null;
        
        String forceApply = null;
        String immediateUpdate = null;
        String profileNamePrefix = "";
        String profiles = null;

        init(request);
        
        subsMap = new LinkedHashMap<String, Map<String, ISubscription>>(20);
        rootDir = main.getDataDirectory();
        try {
            this.user = GUIUtils.getUser(req);
            modifiedUserDN = main.resolveUserDN(user.getName());
        } catch (Exception e) {
            if (DEBUG) e.printStackTrace();
        }

        if ("addAllow".equals(action)) {
            String app = profileForm.getSelectedApp();

            if (app != null && app.trim().length() != 0) {
                Map<String, SoftwareDetailsBean> managedApps = profileForm.getManagedApps();
                Set<String> appsSet = profileForm.getAppSet();

                appsSet.remove(app);
                profileForm.setAppSet(appsSet);
                SoftwareDetailsBean bean = new SoftwareDetailsBean();
                bean.setName(app);
                bean.setType("allowed");
                managedApps.put(app, bean);
                profileForm.setManagedApps(managedApps);

                profileForm.setShowAppTab("app");
            }

            request.setAttribute("result", getString(req.getLocale(), "page.allow.success"));

            return mapping.findForward("add");
        } else if ("addBlock".equals(action)) {
            String app = profileForm.getSelectedApp();

            if (app != null && app.trim().length() != 0) {
                Map<String, SoftwareDetailsBean> managedApps = profileForm.getManagedApps();
                Set<String> appsSet = profileForm.getAppSet();

                appsSet.remove(app);
                profileForm.setAppSet(appsSet);
                SoftwareDetailsBean bean = new SoftwareDetailsBean();
                bean.setName(app);
                bean.setType("blocked");
                managedApps.put(app, bean);
                profileForm.setManagedApps(managedApps);
                profileForm.setShowAppTab("app");
            }

            request.setAttribute("result", getString(req.getLocale(), "page.block.success"));

            return mapping.findForward("add");
        } else if ("removeApp".equals(action)) {
            String app = profileForm.getSelected();
            Map<String, SoftwareDetailsBean> managedApps = profileForm.getManagedApps();

            profileForm.getAppSet().add(app);

            managedApps.remove(app);
            profileForm.setManagedApps(managedApps);
            profileForm.setShowAppTab("app");

            request.setAttribute("result", getString(req.getLocale(), "page.remove.success"));

            return mapping.findForward("add");
        } else if ("getList".equals(action)) {
            String prefix = request.getParameter("prefix");
            response.setContentType("application/json; charset=utf-8");
            response.setHeader("Cache-Control", "max-age=0, no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            PrintWriter out;

            try {
                out = response.getWriter();
                JSONObject jsonObject = new JSONObject();
                List<String> apps = new ArrayList<String>();

                /*try {
                    Set<String> allApps = (Set<String>) req.getSession().getAttribute("softwares");
                    if (allApps != null && allApps.size() > 0) {

                    }
                    jsonObject.put("details", profileDetails);
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/

                System.out.println("Map: " + apps);
                System.out.println("JSON: " + jsonObject.toString());
                out.println(jsonObject.toString());
                out.flush();
            } catch (IOException e) { /**/}

            return mapping.findForward("add");
        } else if ("save".equals(action) || "apply".equals(action)) {
            System.out.println("DesktopSecurityProfileSaveAction: action is save or apply...");
            profileName = profileForm.getName().trim();
            profileDesc = profileForm.getDescription().trim();
            forceApply = profileForm.getForceApplyEnabled();
            immediateUpdate = profileForm.getImmediateUpdateEnabled();

//            Map<String, SoftwareDetailsBean> managedApps = profileForm.getManagedApps();
//            StringBuilder allowed = new StringBuilder();
//            StringBuilder blocked = new StringBuilder();
//            for (SoftwareDetailsBean bean : managedApps.values()) {
//                if ("allowed".equals(bean.getType())) {
//                    allowed.append(bean.getName()).append(";");
//                } else {
//                    blocked.append(bean.getName()).append(";");
//                }
//            }

//            blockedSoftwares = blocked.toString();
//            allowedSoftwares = allowed.toString();
//
            if(DEBUG5) {
	            System.out.println("Profile Name :" + profileName);
	            System.out.println("Profile Description :" + profileDesc);
	            System.out.println("force policy apply :" + forceApply);
	            System.out.println("immediate update :" + immediateUpdate);

	            System.out.println("Blocked Softwares :" + profileForm.getBlockedAsStr());
	            System.out.println("Allowed Softwares :" + profileForm.getAllowedAsStr());
	            
	            System.out.println("cmdPrompt : " + profileForm.getCmdPrompt());
	            System.out.println("File Sharing : " + profileForm.getFileSharing());
	            System.out.println("readFloppyDrive : " + profileForm.getUserReadFloppy());
	            System.out.println("writeFloppyDrive : " + profileForm.getUserWriteFloppy());
	            System.out.println("readCDDVDDrive : " + profileForm.getUserReadCDDVD());
	            System.out.println("writeCDDVDDrive : " + profileForm.getUserWriteCDDVD());
	            System.out.println("readWPDDrive : " + profileForm.getUserReadWPD());
	            System.out.println("writeWPDDrive : " + profileForm.getUserWriteWPD());
	            System.out.println("Internet : " + profileForm.getUserInternet());
	            System.out.println("ScreenSaverPwd : " + profileForm.getUserSecureScreen());
	            System.out.println("enableScreenSaver : " + profileForm.getUserEnableScreen());
	            System.out.println("screenSaverTimeout : " + profileForm.getUserScreenTimeout());
	            System.out.println("forceSpecificScreenSaver : " + profileForm.getUserForceSpecificScreen());
	            System.out.println("minPwdStrength : " + profileForm.getMinPwdStrengthVal());
                System.out.println("maxPwdAge : " + profileForm.getMaxPwdAgeVal());
                System.out.println("minPwdAge : " + profileForm.getMinPwdAgeVal());
	            System.out.println("forcedLogoutTime : " + profileForm.getForcedLogoutTimeVal());
	            System.out.println("enforcePwdHistory : " + profileForm.getEnforcePwdHistoryVal());
	            System.out.println("accountLockoutThreshold : " + profileForm.getAccountLockoutThresholdVal());
	            System.out.println("resetAccountLockoutCounter : " + profileForm.getResetAccountLockoutCounterVal());
	            System.out.println("accountLockoutCounter : " + profileForm.getAccountLockoutCounterVal());
            }

            try {
                String availableProfiles = config.getProperty(DESKTOP_SECURITY_PROFILE_NAME);
                if(profileName != null && profileName != "") {
                    profileNamePrefix = profileName + ".";
                    profiles = ((availableProfiles != null) &&
                            ((availableProfiles.startsWith(profileName + ",")) ||
                                    (availableProfiles.endsWith("," + profileName)) ||
                                    (availableProfiles.equals(profileName)) ||
                                    (availableProfiles.indexOf(","+ profileName+ ",") > -1)))
                            ?  availableProfiles : ((availableProfiles == null) ? profileName : availableProfiles + "," + profileName);
                    config.setProperty(DESKTOP_SECURITY_PROFILE_NAME, profiles);
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_TEMPLATE_NAME, profileName);

                    if (!isEmpty(profileDesc)) {
                        config.setProperty(profileNamePrefix + DEKTOP_SECURITY_PROFILE_DESC, profileDesc);
                    }

                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_CMD_PROMPT, profileForm.getCmdPrompt());
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_FILE_SHARING, profileForm.getFileSharing());

                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_USER_READ_FLOPPY, profileForm.getUserReadFloppy());
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_MACH_READ_FLOPPY, profileForm.getMachReadFloppy());
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_USER_WRITE_FLOPPY, profileForm.getUserWriteFloppy());
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_MACH_WRITE_FLOPPY, profileForm.getMachWriteFloppy());
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_USER_READ_CDDVD, profileForm.getUserReadCDDVD());
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_MACH_READ_CDDVD, profileForm.getMachReadCDDVD());
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_USER_WRITE_CDDVD, profileForm.getUserWriteCDDVD());
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_MACH_WRITE_CDDVD, profileForm.getMachWriteCDDVD());
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_USER_READ_WPD, profileForm.getUserReadWPD());
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_MACH_READ_WPD, profileForm.getMachReadWPD());
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_USER_WRITE_WPD, profileForm.getUserWriteWPD());
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_MACH_WRITE_WPD, profileForm.getMachWriteWPD());
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_USER_INTERNET, profileForm.getUserInternet());
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_MACH_INTERNET, profileForm.getMachInternet());
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_USER_SECURE_SCREENSAVER, profileForm.getUserSecureScreen());
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_USER_ENABLE_SCREENSAVER, profileForm.getUserEnableScreen());
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_USER_SCREENSAVER_TIMEOUT, profileForm.getUserScreenTimeout() + "," + profileForm.getUserScreenTimeoutVal());
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_USER_FORCE_SCREENSAVER, profileForm.getUserForceSpecificScreen() + "," + profileForm.getUserForceSpecificScreenVal());
//                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_MIN_PWD_STRENGTH, profileForm.getMinPwdStrength() + "," + profileForm.getMinPwdStrengthVal());
//                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_MAX_PWD_AGE, profileForm.getMaxPwdAge() + "," + profileForm.getMaxPwdAgeVal());
//                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_MIN_PWD_AGE, profileForm.getMinPwdAge() + "," + profileForm.getMinPwdAgeVal());
//                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_FORCED_LOGOUT_TIME, profileForm.getForcedLogoutTime() + "," + profileForm.getForcedLogoutTimeVal());
//                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_ENFORCE_PWD_HISTORY, profileForm.getEnforcePwdHistory() + "," + profileForm.getEnforcePwdHistoryVal());
//                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_ACC_LOCK_THRESHOLD, profileForm.getAccountLockoutThreshold() + "," + profileForm.getAccountLockoutThresholdVal());
//                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_RESET_ACC_LOCK_COUNTER, profileForm.getResetAccountLockoutCounter() + "," + profileForm.getResetAccountLockoutCounterVal());
//                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_ACC_LOCK_COUNTER, profileForm.getAccountLockoutCounter() + "," + profileForm.getAccountLockoutCounterVal());

                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_MIN_PWD_STRENGTH, profileForm.getMinPwdStrengthVal() + "");
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_MAX_PWD_AGE, profileForm.getMaxPwdAgeVal() + "");
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_MIN_PWD_AGE, profileForm.getMinPwdAgeVal() + "");
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_FORCED_LOGOUT_TIME, profileForm.getForcedLogoutTimeVal() + "");
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_ENFORCE_PWD_HISTORY, profileForm.getEnforcePwdHistoryVal() + "");
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_ACC_LOCK_THRESHOLD, profileForm.getAccountLockoutThresholdVal() + "");
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_RESET_ACC_LOCK_COUNTER, profileForm.getResetAccountLockoutCounterVal() + "");
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_ACC_LOCK_COUNTER, profileForm.getAccountLockoutCounterVal() + "");

                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_FORCE_APPLY, forceApply);
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_IMMEDIATE_UPDATE, immediateUpdate);

//                    if("".equals(blockedSoftwares.trim())) {
//                    	blockedSoftwares = null;
//                    }
//
//                    if("".equals(allowedSoftwares.trim())) {
//                    	allowedSoftwares = null;
//                    }
//
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_SOFTWARES_BLOCKED, isEmpty(profileForm.getBlockedAsStr()) ? "" : profileForm.getBlockedAsStr());
                    config.setProperty(profileNamePrefix + DESKTOP_SECURITY_SOFTWARES_ALLOWED, isEmpty(profileForm.getAllowedAsStr()) ? "" : profileForm.getAllowedAsStr());
                    
                    if (!config.save()) {
                        throw new Exception("Failed to save init configurations");
                    }

                    System.out.println("Successfully saved Desktop Security Policy template for the profile :" + profileName);
                }
            } catch (Exception ioe) {
                ioe.printStackTrace();
                config = null;
            }
            createProfileList(request);
            /*
            if ("apply".equals(action)) {
                updatePolicyForAffectedTargets(profileName);
            }
            */
            profileForm.reset();
            lastAction = null;

            return mapping.findForward("save");

        } else if ("add".equals(action)) {
            System.out.println("DesktopSecurityProfileSaveAction: action is 'add'...");
            profileForm.reset();
            profileForm.setCreate("true");
            return mapping.findForward("add");
        } else if ("remove".equals(action)) {
            System.out.println("DesktopSecurityProfileSaveAction: action is 'remove'...");
            Vector<String> toRemoveProfiles = new Vector<String>();
            String paramName = "";
            Enumeration params = request.getParameterNames();
            while(params.hasMoreElements()) {
                paramName = (String) params.nextElement();
                if (("on".equals(request.getParameter(paramName))) && (paramName.indexOf("profile_sel_") > -1)) {
                    toRemoveProfiles.addElement(paramName);
                }
            }

            removeProfile(toRemoveProfiles);

            createProfileList(request);

            request.setAttribute("result", getString(req.getLocale(), "page.remove.template.success"));

            return mapping.findForward("remove");

        } else if ("edit".equals(action)) {
            System.out.println("DesktopSecurityProfileSaveAction: action is 'edit'...");

            Vector v = getSelectedServers(request);

            if (v.size() == 1) {
                profileName = (String)v.elementAt(0);
            }

            System.out.println("DesktopSecurityProfileSaveAction: edit is in progress for profile: " + profileName);
            profileForm.loadProfile(readProfile(profileName));

            // todo: check next profile is getting affected because of this
            Set<String> appsSet = (Set<String>) req.getSession().getAttribute("appsSet");
            Map<String, SoftwareDetailsBean> managedApps = profileForm.getManagedApps();
            for (String anApp : managedApps.keySet()) {
                appsSet.remove(anApp);
            }

            System.out.println("DesktopSecurityProfileForm:: Loading profile: done...");

            profileForm.setCreate("false");
            lastAction = "edit";
            loadAffectedTargets(profileName);

            return mapping.findForward("add");
        } else if ("cancel".equals(action)) {
            createProfileList(request);
            return mapping.findForward("cancel");
        } else if ("export".equals(action)) {
            try {
                exportPowerSchemes(request, response);
                profileForm.setStatus("Export-Succeeded");
            } catch (Exception ex) {
                profileForm.setStatus("Export-Failed");
                profileForm.setStatusDesc(ex.getMessage());
            }

            createProfileList(request);

            return mapping.findForward("load");
        } else if ("import".equals(action)) {
            try{
                importPowerSchemes(request, response);
                profileForm.setStatus("Import-Succeeded");
                updateAffectedTargets();
            } catch (Exception ex) {
                profileForm.setStatus("Import-Failed");
                profileForm.setStatusDesc(ex.getMessage());
            }

            createProfileList(request);

            return mapping.findForward("load");
        } else {
            Set<String> appsSet = loadExecutables();

            if (appsSet.size() == 0) {
                appsSet = loadExecutables1();
            }

            req.getSession().setAttribute("appsSet", appsSet);
            profileForm.setAppSet(appsSet);

            createProfileList(request);
            return mapping.findForward("load");
        }
    }

    void updateAffectedTargets () throws Exception {
        loadProfileConfig();
        String profileName = config.getProperty(DESKTOP_SECURITY_PROFILE_NAME);
        String profiles[] = profileName.split(",");
        for (int i = 0; i < profiles.length; i++) {
            try {
                loadAffectedTargets(profiles[i]);
                updatePolicyForAffectedTargets(profiles[i]);
            } catch (Exception ex) {
                throw new Exception("Unable to update profiles " + profiles[i] +" for targets. " + ex.getMessage());
            }
        }
    }


    public void importPowerSchemes(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
        FormFile file = profileForm.getFile();
        String importFile = main.getDataDirectory()
                + File.separator;
        String fileName = file.getFileName();
        File newFile = null;
        if(!("").equals(fileName) && (fileName.endsWith(".xml"))){
            newFile = new File(importFile, fileName);
            // Make sure file not exists in Server before copying it.
            if (newFile.exists()) {
                newFile.delete();
            }

            InputStream is = null;
            try {
                if(!newFile.exists()){
                    FileOutputStream fos = new FileOutputStream(newFile);
                    fos.write(file.getFileData());
                    fos.flush();
                    fos.close();
                }

                XMLParser parser = new XMLParser();
                is = new FileInputStream(newFile);
                SubscriptionMgrParser spmParser = new SubscriptionMgrParser(main);
                try {
					//ToDo: channel and tenantName should be passed here for tenant based logging
                    parser.parse(is, spmParser);
                } catch (XMLException ex) {
                    throw new Exception("Invalid file. Unable to parse " + newFile.getName() + ".");
                }
                Exception ex = spmParser.errorOccoured();
                if (ex != null) {
                    throw ex;
                }
            } catch (Exception ex) {
                throw ex;
            }
            finally {
                is.close();
                // Make sure we delete the file from Server once imported.
                if (newFile != null && newFile.exists()) {
                    newFile.delete();
                }
            }

        } else {
            throw new Exception("Invalid file. Unable to parse " + fileName + ".");
        }
    }

    public void exportPowerSchemes(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
        ServletOutputStream out = null;
        FileInputStream in = null;
        String exportFile = main.getDataDirectory()
                + File.separator + "subscriptionExport.xml";
        SubscriptionMgrParser parser = null;
        List selected = Collections.list(request.getParameterNames());
        parser = new SubscriptionMgrParser(main, new XMLOutputStream(exportFile));
        parser.serializeSelectedPowerConfig(selected);
        try {
            in = new FileInputStream(new File(exportFile));
            out = response.getOutputStream();
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment;filename=subscriptionExport.xml");
            // Get it from file system
            byte[] outputByte = new byte[1024];
            int read = 0;
            // copy binary content to output stream
            while ((read = in.read(outputByte)) != -1) {
                out.write(outputByte, 0, read);
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }

    private Set<String> loadExecutables1() {
        Set<String> exeListFromSoftTitle = new HashSet<String>();

        exeListFromSoftTitle.add("powercfg.exe");
        exeListFromSoftTitle.add("gtalk.exe");
        exeListFromSoftTitle.add("utorrent.exe");
        exeListFromSoftTitle.add("skype.exe");
        exeListFromSoftTitle.add("chrome.exe");
        exeListFromSoftTitle.add("messenger.exe");
        exeListFromSoftTitle.add("app7.exe");
        exeListFromSoftTitle.add("app8.exe");
        exeListFromSoftTitle.add("app9.exe");
        exeListFromSoftTitle.add("app10.exe");

        return exeListFromSoftTitle;
    }

    private Set<String> loadExecutables() {
        Set<String> exeListFromSoftTitle = new HashSet<String>();

        try {
            IDatabaseMgr dbmgr = this.tenant.getDbMgr();

            if(null != dbmgr) {
                Dbutils dbutils = new Dbutils(dbmgr, dbmgr.getActive("read"));

                if(dbutils.checkDBConn()) {
                    exeListFromSoftTitle.addAll(dbutils.getExectablesFromSoftwareTitle());
                    exeListFromSoftTitle.addAll(dbutils.getExecutableFromInvApplication());

                    System.out.println("Number of executables from inv_aplication and sw_exe_map: " + exeListFromSoftTitle.size());
                } else {
                    System.out.println("Check connection with database is failed.");
                }
            }

        } catch(Exception ec) {
            System.out.println(ec.getMessage());
            if(DEBUG5) {
                ec.printStackTrace();
            }
        }

        return exeListFromSoftTitle;
    }

    private void loadAffectedTargets(String profileName) throws Exception {
        profileForm.setAffectedTargets(getAffectedTargets(profileName));
    }

    private Map<String, String> getAffectedTargets(String profileName) throws Exception {
        Map<String, String> affectedTargets = new Hashtable<String, String>(10);

        String searchStr = "(&("+LDAPVarsMap.get("PROPERTY")+"="+DESKTOP_SECURITY_TEMPLATE_NAME+PROP_DELIM+PROP_SECURITY_KEYWORD+"=";
        searchStr += profileName;
        searchStr += "*)(|("+LDAPVarsMap.get("TARGETDN")+"=*)("+LDAPVarsMap.get("TARGET_ALL")+"="+VALID+")("+LDAPVarsMap.get("TARGETTYPE")+"="+TYPE_SITE+")))";
        String[] searchAttrs = {LDAPConstants.OBJECT_CLASS, LDAPVarsMap.get("SUBSCRIPTION_NAME"), LDAPVarsMap.get("TARGETTYPE"), LDAPVarsMap.get("TARGETDN")};
        String searchBase = null;

        try {
            IUser user = GUIUtils.getUser(req);
            LDAPConnection conn = user.getSubConn();
            searchBase = main.getSubBase();

            NamingEnumeration nenum = conn.search(searchStr, searchAttrs, searchBase, false);

            while (nenum.hasMore()) {
                SearchResult sr = (SearchResult) nenum.next();
                Attributes srAttrs = sr.getAttributes();
                String targetType = loadAttributes(srAttrs, LDAPVarsMap.get("TARGETTYPE"));
                String targetId = loadAttributes(srAttrs, LDAPVarsMap.get("TARGETDN"));
                String subName = loadAttributes(srAttrs, LDAPVarsMap.get("SUBSCRIPTION_NAME"));
                String resultName = sr.getName();
                String dn = null;

                if (!((resultName == null) || "".equals(resultName))) {
                    dn = LDAPName.unescapeJNDISearchResultName(resultName) + "," + searchBase;
                    if (DEBUG) {
                        System.out.println("dn = " + dn);
                        System.out.println("Search Result sr= " + sr);
                        System.out.println("Attributes srAttrs= " + srAttrs);
                        System.out.println("resultName= " + resultName);
                    }
                }

                if ("all".equals(targetType)) {
                    targetId = dn;
                }

                if(TYPE_SITE.equals(targetType)) { // support SBRP site target
                    targetId = subName;
                }

                if (targetId != null && targetType != null) {
                    affectedTargets.put(targetId, targetType);
                }

                if (DEBUG) {
                    System.out.println("targetId: " + targetId + ", targetType: " + targetType);
                }

            }
        } catch (Exception ex) {
            if (DEBUG) {
                ex.printStackTrace();
            }
        }

        return affectedTargets;
    }

    private void updatePolicyForAffectedTargets(String profileName) throws Exception {
        Map affectedTargets = profileForm.getAffectedTargetsHash();
        if (affectedTargets.size() > 0) {
            ISubscription newSub, oldSub;
            for (Object aKey : affectedTargets.keySet()){
                String priority = null;
                String profNameFromPolicy = profileName;
                String targetId = (String) aKey;
                String targetType = (String) affectedTargets.get(targetId);

                newSub = ObjectManager.openSubForWrite(targetId, targetType, GUIUtils.getUser(req));
                oldSub = new Subscription((Subscription) newSub);

                String profNameWithPriority = newSub.getProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_TEMPLATE_NAME);
                int index = profNameWithPriority.indexOf(',');
                if (index != -1) {
                    profNameFromPolicy = profNameWithPriority.substring(0, index);
                    priority = profNameWithPriority.substring(index+1, profNameWithPriority.length());
                }

                if (profileName.equalsIgnoreCase(profNameFromPolicy)) {
                    newSub.clearProperties(PROP_SECURITY_KEYWORD);
                    Hashtable props = readProfile(profileName);
                    if (props != null && props.size() > 0){
                        Enumeration propKeys = props.keys();
                        while (propKeys.hasMoreElements()) {
                            String key = (String) propKeys.nextElement();
                            if (key.equals(DESKTOP_SECURITY_PROFILE_NAME)) {
                                setProperty(newSub, DESKTOP_SECURITY_TEMPLATE_NAME, (String) props.get(key), priority);
                            } else if (key.equals(DEKTOP_SECURITY_PROFILE_DESC)) {
                                // dont store profile desc into LDAP
                            } else {
                                setProperty(newSub, key, (String) props.get(key), priority);
                            }
                        }
                        setProperty(newSub, DESKTOP_SECURITY_ENABLE, "true", priority);
                    }
                    if(!main.isPeerApprovalEnabled()) {
                        newSub.save();
                        System.out.println("Modified values was updated to the policy for the target: " + targetId);
                    }
                    addToSubsMap(KEY_OLD_SUB, targetId, oldSub);
                    addToSubsMap(KEY_NEW_SUB, targetId, newSub);
                }
            }
            triggerMail();
        } else {
            System.out.println("There is no targets contain selected profile '" + profileName + "'");
        }
    }

    private void setProperty(ISubscription sub, String key, String value, String priority)
            throws Exception {
        if (priority != null) {
            value = value + "," + priority;
        }

        sub.setProperty(PROP_SECURITY_KEYWORD, key, value);
    }

    private String loadAttributes(Attributes srAttrs, String attr) throws NamingException{
        String strValue = null;
        Attribute attrs = srAttrs.get(attr);
        NamingEnumeration ne;
        if (attrs != null) {
            ne= attrs.getAll();
            while (ne.hasMoreElements()) {
                Object value = ne.next();
                if (value instanceof String) {
                    strValue = (String) value;
                }
            }
        }
        return strValue;
    }

    private void createProfileList(HttpServletRequest req) {
        Vector v = new Vector();

        try {
            loadProfileConfig();
            String profile = config.getProperty(DESKTOP_SECURITY_PROFILE_NAME);

            if (profile != null) {
                StringTokenizer st = new StringTokenizer(profile, ",", false);
                //iterate through tokens
                while(st.hasMoreTokens()) {
                    String profileName = st.nextToken(",");
                    String profileDescription = config.getProperty(profileName + "." + DEKTOP_SECURITY_PROFILE_DESC);
                    v.addElement(new PowerProfileBean(profileName, profileDescription));
                }
                if (!config.save()) {
                    throw new Exception("Failed to save Profile Configurations");
                }
            }
            
        } catch (Exception ioe) {
            System.out.println("Failed to save configurations for the specified profile");
            if (DEBUG) {
                ioe.printStackTrace();
            }
            config = null;
        }

        req.setAttribute("profiles", v);
    }

    private Hashtable readProfile(String profileName) {
        System.out.println("DesktopSecurityProfileSaveAction: reading profile: " + profileName);
        Hashtable props = new Hashtable(10);
        String profile = config.getProperty(DESKTOP_SECURITY_PROFILE_NAME);

        if(null != profile && (profile.equals(profileName) || profile.startsWith(profileName + ",") ||
                profile.endsWith("," + profileName) || (profile.indexOf("," + profileName + ",") > -1))) {
            String[] pairs = config.getPropertyPairs();
            for (int i = 0; i < pairs.length;) {
                String key = pairs[i++];
                String value = pairs[i++];
                if (key != null && key.startsWith(profileName+DOT) && value != null) {
                    props.put(key.substring(profileName.length()+1), value);
                }
            }
        }

        props.put(DESKTOP_SECURITY_PROFILE_NAME, profileName);

        return props;
    }

    private void removeProfile(Vector<String> removeProfs) {
        loadProfileConfig();
        String profileStr = config.getProperty(DESKTOP_SECURITY_PROFILE_NAME);
        String[] profileArr = profileStr.split(PROP_DELIM);

        for (String removeProfileStr : removeProfs) {
            try {
                String removeProfile = removeProfileStr.substring(12);

                for (String profile : profileArr) {
                    if (!isEmpty(profile) && profile.equals(removeProfile)) {
                        // remove it from policy objects
                        Map<String, String> affectedTargets = getAffectedTargets(removeProfile);
                        boolean removedFromAllPolicies = true;

                        for (String aTarget : affectedTargets.keySet()) {
                            String type = affectedTargets.get(aTarget);

                            try {
                                ISubscription sub = ObjectManager.openSubForWrite(aTarget, type, GUIUtils.getUser(req));
                                removePropsInSubObject(sub);
                                sub.save();
                            } catch (SystemException sysEx) {
                                removedFromAllPolicies = false;
                                System.out.println("Failed to remove configurations for profile: '" + removeProfileStr + "' for the target '" + aTarget + "'");
                                if (DEBUG) sysEx.printStackTrace();
                            }
                        }

                        // remove it from local property file
                        if (removedFromAllPolicies) {
                            clearProfile(config, removeProfile);
                        }
                    }
                }
            } catch (Exception ioe) {
                System.out.println("Failed to remove configurations for profile: " + removeProfileStr);
                if (DEBUG) {
                    ioe.printStackTrace();
                }
            }
        }

        config.save();
    }

    private void removePropsInSubObject(ISubscription sub) throws SystemException {
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_ENABLE, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_TEMPLATE_NAME, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DEKTOP_SECURITY_PROFILE_DESC, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_FORCE_APPLY, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_IMMEDIATE_UPDATE, null);

        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_FILE_SHARING, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_CMD_PROMPT, null);

        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_READ_FLOPPY, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_WRITE_FLOPPY, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_READ_CDDVD, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_WRITE_CDDVD, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_READ_WPD, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_WRITE_WPD, null);

        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_READ_FLOPPY, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_WRITE_FLOPPY, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_READ_CDDVD, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_WRITE_CDDVD, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_READ_WPD, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_WRITE_WPD, null);

        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_ENABLE_SCREENSAVER, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_SECURE_SCREENSAVER, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_SCREENSAVER_TIMEOUT, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_FORCE_SCREENSAVER, null);

        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_USER_INTERNET, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MACH_INTERNET, null);

        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MIN_PWD_STRENGTH, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MAX_PWD_AGE, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_MIN_PWD_AGE, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_FORCED_LOGOUT_TIME, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_ENFORCE_PWD_HISTORY, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_ACC_LOCK_THRESHOLD, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_RESET_ACC_LOCK_COUNTER, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_ACC_LOCK_COUNTER, null);

        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_SOFTWARES_ALLOWED, null);
        sub.setProperty(PROP_SECURITY_KEYWORD, DESKTOP_SECURITY_SOFTWARES_BLOCKED, null);
    }

    public void clearProfile(ConfigProps prop, String profileName) {

        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_TEMPLATE_NAME, null);
        prop.setProperty(profileName + DOT + DEKTOP_SECURITY_PROFILE_DESC, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_FORCE_APPLY, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_IMMEDIATE_UPDATE, null);

        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_FILE_SHARING, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_CMD_PROMPT, null);

        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_USER_READ_FLOPPY, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_USER_WRITE_FLOPPY, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_USER_READ_CDDVD, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_USER_WRITE_CDDVD, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_USER_READ_WPD, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_USER_WRITE_WPD, null);

        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_MACH_READ_FLOPPY, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_MACH_WRITE_FLOPPY, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_MACH_READ_CDDVD, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_MACH_WRITE_CDDVD, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_MACH_READ_WPD, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_MACH_WRITE_WPD, null);

        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_USER_ENABLE_SCREENSAVER, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_USER_SECURE_SCREENSAVER, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_USER_SCREENSAVER_TIMEOUT, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_USER_FORCE_SCREENSAVER, null);

        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_USER_INTERNET, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_MACH_INTERNET, null);

        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_MIN_PWD_STRENGTH, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_MAX_PWD_AGE, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_MIN_PWD_AGE, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_FORCED_LOGOUT_TIME, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_ENFORCE_PWD_HISTORY, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_ACC_LOCK_THRESHOLD, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_RESET_ACC_LOCK_COUNTER, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_ACC_LOCK_COUNTER, null);

        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_SOFTWARES_ALLOWED, null);
        prop.setProperty(profileName + DOT + DESKTOP_SECURITY_SOFTWARES_BLOCKED, null);


        String availableProfiles = prop.getProperty(DESKTOP_SECURITY_PROFILE_NAME);
        prop.setProperty(DESKTOP_SECURITY_PROFILE_NAME, removeProfileName(availableProfiles, profileName));

        /*if (availableProfiles != null) {
            int index = availableProfiles.indexOf(profileName + ",");
            int startIdx = 0;
            int endIdx = availableProfiles.length();

            if (index != -1) {
                startIdx = index;
                endIdx = index+profileName.length()+1;
            } else {
                index = availableProfiles.indexOf("," + profileName);
                if (index != -1) {
                    startIdx = index;
                    endIdx = index+profileName.length()+1;
                } else {
                    availableProfiles = null;
                }
            }

            if (availableProfiles != null) {
                availableProfiles = availableProfiles.substring(0, startIdx) + availableProfiles.substring(endIdx, availableProfiles.length());
            }
        }
        prop.setProperty(DESKTOP_SECURITY_PROFILE_NAME, availableProfiles);*/
    }

    private String removeProfileName(String commaSeparatedProfiles, String profileNameToBeRemoved) {
        StringBuilder newProfileSet = new StringBuilder();
        if (!isEmpty(commaSeparatedProfiles)) {
            for (String profile : commaSeparatedProfiles.split(PROP_DELIM)) {
                if (!isEmpty(profile) && !profile.equals(profileNameToBeRemoved)) {
                    newProfileSet.append(profile).append(PROP_DELIM);
                }
            }
        }

        return newProfileSet.toString().replaceAll(",$", "");
    }

    private void loadProfileConfig() {
        config = new ConfigProps(new File(rootDir, DESKTOP_SECURITY_FILE_NAME));
    }

    private Vector<String> getSelectedServers(HttpServletRequest req) {
        Vector<String> v = new Vector<String>();

        for (Enumeration e = req.getParameterNames(); e.hasMoreElements(); ) {
            String name = (String)e.nextElement();
            if (name.startsWith("profile_sel_")) {
                name = name.substring("profile_sel_".length());
                v.addElement(name);
            }
        }

        return v;
    }

    private void addToSubsMap(String subType, String targetId, ISubscription sub) {

        if (null != subsMap.get(targetId)) {
            subsMap.get(targetId).put(subType, sub);
        } else {
            Map<String, ISubscription> newTmpMap = new HashMap<String, ISubscription>(2);
            newTmpMap.put(subType, sub);
            subsMap.put(targetId, newTmpMap);
        }
    }

    private void triggerMail() {
        List<PolicyDiff> policyDiffs = new ArrayList<PolicyDiff>();

        for (String tgt : subsMap.keySet()) {
            Map<String, ISubscription> tmpMap = subsMap.get(tgt);
            PolicyDiff policyDiff = new PolicyDiff(tmpMap.get(KEY_OLD_SUB), tmpMap.get(KEY_NEW_SUB), main);
            policyDiff.setPolicyAction( null != tmpMap.get(KEY_OLD_SUB) ? MODIFY_OPERATION : ADD_OPERATION);
            if(null != modifiedUserDN) {
                policyDiff.setUser(modifiedUserDN);
            }
            // set default policy status as pending
            policyDiff.setPolicyStatus(POLICY_PENDING);
            policyDiffs.add(policyDiff);
        }

        String userName = null;
        try {
            userName = main.getDisplayName(main.resolveUserDN(user.getName()));
        } catch (SystemException e) {
            if (DEBUG) e.printStackTrace();
        }

        PolicyDiffMailFormatter mailFormatter = new PolicyDiffMailFormatter(policyDiffs, resources);
        mailFormatter.setCreatedByDispName((null == userName || userName.trim().isEmpty()) ? user.getName() : userName);
        mailFormatter.setPeerApprovalEnabled(main.isPeerApprovalEnabled());
        mailFormatter.prepare();

        if(!policyDiffs.isEmpty()) {
            main.storeApprovalPolicy2DB(policyDiffs);
        }

        main.sendMail(mailFormatter);
    }
}
