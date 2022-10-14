// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Subscription;
import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.util.SubscriptionMgrParser;
import static com.marimba.apps.subscriptionmanager.approval.IApprovalPolicyConstants.*;
import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiff;
import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiffMailFormatter;
import com.marimba.apps.subscriptionmanager.webapp.forms.ProfilePwrOptnsForm;
import com.marimba.apps.subscriptionmanager.webapp.system.PowerProfileBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
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
 * Created by IntelliJ IDEA.
 * User: BHARATH
 * Date: September 1st, 2009
 * Time: 3:07:42 PM
 * To change this template use File | Settings | File Templates.
 */

public class ProfilePwrOptnsSaveAction extends AbstractAction {
    private File rootDir;
    private IUser user;
    String modifiedUserDN = null;
    private ConfigProps config;
    private HttpServletRequest req;
    private String KEY_OLD_SUB = "oldsub";
    private String KEY_NEW_SUB = "newsub";
    private static final String DOT = ".";
    private ProfilePwrOptnsForm profileForm;
    private Map<String, Map<String, ISubscription>> subsMap;

    public ActionForward execute( ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {

        req = request;
        profileForm = (ProfilePwrOptnsForm) form;
        String  action = request.getParameter("action");
        String lastAction = null;
        String profileName = null;
        String profileDesc = null;
        String hibernate = null;
        String hiberIdle = null;
        String monitorIdle = null;
        String diskIdle = null;
        String standbyIdle = null;
        String hiberIdleDC = null;
        String monitorIdleDC = null;
        String diskIdleDC = null;
        String standbyIdleDC = null;
        String promptPassword = null;
        String forceApply = null;
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

        if ("save".equals(action) || "apply".equals(action)) {
            profileName = profileForm.getName().trim();
            profileDesc = profileForm.getDescription().trim();
            hibernate = (String) profileForm.getValue(HIBERNATE_PROP);
            if ("on".equals(hibernate)) {
                hiberIdle = (String) profileForm.getValue(HIBER_IDLETIME_PROP);
                hiberIdleDC = (String) profileForm.getValue(HIBER_IDLETIME_DC_PROP);
            }
            monitorIdle = (String) profileForm.getValue(MONITOR_IDLETIME_PROP);
            diskIdle = (String) profileForm.getValue(DISK_IDLETIME_PROP);
            standbyIdle = (String) profileForm.getValue(STANDBY_IDLETIME_PROP);
            monitorIdleDC = (String) profileForm.getValue(MONITOR_IDLETIME_DC_PROP);
            diskIdleDC = (String) profileForm.getValue(DISK_IDLETIME_DC_PROP);
            standbyIdleDC = (String) profileForm.getValue(STANDBY_IDLETIME_DC_PROP);
            promptPassword = (String) profileForm.getValue(PROMPT_PASSWORD_PROP);
            forceApply = (String) profileForm.getValue(FORCE_APPLY_PROP);

            try {
                String availableProfiles = config.getProperty(POWER_PROFILE_NAME);
                if(profileName != null && profileName != "") {
                    profileNamePrefix = profileName + ".";
                    profiles = ((availableProfiles != null) &&
                            ((availableProfiles.startsWith(profileName + ",")) ||
                                    (availableProfiles.endsWith("," + profileName)) ||
                                    (availableProfiles.equals(profileName)) ||
                                    (availableProfiles.indexOf(","+ profileName+ ",") > -1)))
                            ?  availableProfiles : ((availableProfiles == null) ? profileName : availableProfiles + "," + profileName);
                    config.setProperty(POWER_PROFILE_NAME, profiles);

                    if(profileDesc != null && profileDesc != "") {
                        config.setProperty(profileNamePrefix + POWER_PROFILE_DESC, profileDesc);
                    }

                    if ("on".equals(hibernate)) {
                        config.setProperty(profileNamePrefix + HIBERNATE_PROP, "true");
                    } else {
                        config.setProperty(profileNamePrefix + HIBERNATE_PROP, "false");
                    }

                    if (!POWER_IDLE_DEFAULT_VALUE.equalsIgnoreCase(hiberIdle)) {
                        config.setProperty(profileNamePrefix + HIBER_IDLETIME_PROP, hiberIdle);
                    } else {
                        config.setProperty(profileNamePrefix + HIBER_IDLETIME_PROP, null);
                    }

                    if (!POWER_IDLE_DEFAULT_VALUE.equalsIgnoreCase(hiberIdleDC)) {
                        config.setProperty(profileNamePrefix + HIBER_IDLETIME_DC_PROP, hiberIdleDC);
                    } else {
                        config.setProperty(profileNamePrefix + HIBER_IDLETIME_DC_PROP, null);
                    }

                    if (!POWER_IDLE_DEFAULT_VALUE.equalsIgnoreCase(monitorIdle)) {
                        config.setProperty(profileNamePrefix + MONITOR_IDLETIME_PROP, monitorIdle);
                    } else {
                        config.setProperty(profileNamePrefix + MONITOR_IDLETIME_PROP, null);
                    }

                    if (!POWER_IDLE_DEFAULT_VALUE.equalsIgnoreCase(monitorIdleDC)) {
                        config.setProperty(profileNamePrefix + MONITOR_IDLETIME_DC_PROP, monitorIdleDC);
                    } else {
                        config.setProperty(profileNamePrefix + MONITOR_IDLETIME_DC_PROP, null);
                    }

                    if (!POWER_IDLE_DEFAULT_VALUE.equalsIgnoreCase(diskIdle)) {
                        config.setProperty(profileNamePrefix + DISK_IDLETIME_PROP, diskIdle);
                    } else {
                        config.setProperty(profileNamePrefix + DISK_IDLETIME_PROP, null);
                    }

                    if (!POWER_IDLE_DEFAULT_VALUE.equalsIgnoreCase(diskIdleDC)) {
                        config.setProperty(profileNamePrefix + DISK_IDLETIME_DC_PROP, diskIdleDC);
                    } else {
                        config.setProperty(profileNamePrefix + DISK_IDLETIME_DC_PROP, null);
                    }

                    if (!POWER_IDLE_DEFAULT_VALUE.equalsIgnoreCase(standbyIdle)) {
                        config.setProperty(profileNamePrefix + STANDBY_IDLETIME_PROP, standbyIdle);
                    } else {
                        config.setProperty(profileNamePrefix + STANDBY_IDLETIME_PROP, null);
                    }

                    if (!POWER_IDLE_DEFAULT_VALUE.equalsIgnoreCase(standbyIdleDC)) {
                        config.setProperty(profileNamePrefix + STANDBY_IDLETIME_DC_PROP, standbyIdleDC);
                    } else {
                        config.setProperty(profileNamePrefix + STANDBY_IDLETIME_DC_PROP, null);
                    }

                    if ("on".equals(promptPassword)) {
                        config.setProperty(profileNamePrefix + PROMPT_PASSWORD_PROP, "true");
                    } else {
                        config.setProperty(profileNamePrefix + PROMPT_PASSWORD_PROP, "false");
                    }

                    if ("on".equals(forceApply)) {
                        config.setProperty(profileNamePrefix + FORCE_APPLY_PROP, "true");
                    } else {
                        config.setProperty(profileNamePrefix + FORCE_APPLY_PROP, "false");
                    }

                    if (!config.save()) {
                        throw new Exception("Failed to save init configurations");
                    }
                }
            } catch (Exception ioe) {
                ioe.printStackTrace();
                config = null;
            }
            createProfileList(request);

            if ("apply".equals(action)) {
                updatePolicyForAffectedTargets(profileName);
            }

            profileForm.reset();
            lastAction = null;

            return mapping.findForward("save");

        } else if ("add".equals(action)) {
            profileForm.reset();
            profileForm.setCreate("true");

            return mapping.findForward("add");
        } else if ("remove".equals(action)) {
            Vector toRemoveProfiles = new Vector();
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
            return mapping.findForward("remove");

        } else if ("edit".equals(action)) {
            Vector v = getSelectedServers(request);

            if (v.size() == 1) {
                profileName = (String)v.elementAt(0);
            }

            profileForm.loadProfile(readProfile(profileName));
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
            createProfileList(request);
            return mapping.findForward("load");
        }
    }

    void updateAffectedTargets () throws Exception {
        loadProfileConfig();
        String profileName = config.getProperty(POWER_PROFILE_NAME);
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

    private void loadAffectedTargets(String profileName) throws Exception {
        Hashtable affectedTargets = new Hashtable(10);
        String searchStr = "(&("+LDAPVarsMap.get("PROPERTY")+"="+POWER_PROFILE_SELECTED_PROP+PROP_DELIM+PROP_POWER_KEYWORD+"=";
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
                    if (DEBUG) {
                        System.out.println("targetId: " + targetId + ", targetType: " + targetType);
                    }
                    affectedTargets.put(targetId, targetType);
                }
            }
            profileForm.setAffectedTargets(affectedTargets);
        } catch (Exception ex) {
            if (DEBUG) {
                ex.printStackTrace();
            }
        }
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

                String profNameWithPriority = newSub.getProperty(PROP_POWER_KEYWORD, POWER_PROFILE_SELECTED_PROP);
                int index = profNameWithPriority.indexOf(',');
                if (index != -1) {
                    profNameFromPolicy = profNameWithPriority.substring(0, index);
                    priority = profNameWithPriority.substring(index+1, profNameWithPriority.length());
                }

                if (profileName.equalsIgnoreCase(profNameFromPolicy)) {
                    newSub.clearProperties(PROP_POWER_KEYWORD);
                    Hashtable props = readProfile(profileName);
                    if (props != null && props.size() > 0){
                        Enumeration propKeys = props.keys();
                        while (propKeys.hasMoreElements()) {
                            String key = (String) propKeys.nextElement();
                            if (key.equals(POWER_PROFILE_NAME)) {
                                setProperty(newSub, POWER_PROFILE_SELECTED_PROP, (String) props.get(key), priority);
                            } else if (key.equals(POWER_PROFILE_DESC)) {
                                // dont store profile desc into LDAP
                            } else {
                                setProperty(newSub, key, (String) props.get(key), priority);
                            }
                        }
                        setProperty(newSub, POWER_OPTION_PROP, "true", priority);
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

        sub.setProperty(PROP_POWER_KEYWORD, key, value);
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
            String profile = config.getProperty(POWER_PROFILE_NAME);

            if (profile != null) {
                StringTokenizer st = new StringTokenizer(profile, ",", false);
                //iterate through tokens
                while(st.hasMoreTokens()) {
                    String profileName = st.nextToken(",");
                    String profileDescription = config.getProperty(profileName + "." + POWER_PROFILE_DESC);
                    v.addElement(new PowerProfileBean(profileName, profileDescription));
                }
                if (!config.save()) {
                    throw new Exception("Failed to save Profile Configurations");
                }
            }
            Properties props = (Properties) context.getAttribute(POWER_TEMPLATES);
            String default_profiles = props.getProperty(POWER_PROFILE_NAME);

            StringTokenizer default_profiles_tokens = new StringTokenizer(default_profiles, ",", false);

            while(default_profiles_tokens.hasMoreTokens()) {
                String profileName = default_profiles_tokens.nextToken(",");
                if (null == profile || profile.indexOf(profileName) == -1) {
                    String profileDescription = props.getProperty(profileName + "." + POWER_PROFILE_DESC);
                    v.addElement(new PowerProfileBean(profileName, profileDescription));
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
        Hashtable props = new Hashtable(10);
        String profile = config.getProperty(POWER_PROFILE_NAME);

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
        } else {
            Properties properties = (Properties) context.getAttribute(POWER_TEMPLATES);
            Enumeration enumneration = properties.keys();
            while (enumneration.hasMoreElements()) {
                String key = (String) enumneration.nextElement();
                if (key != null && key.startsWith(profileName+DOT) && properties.getProperty(key) != null) {
                    props.put(key.substring(profileName.length()+1), properties.getProperty(key));
                }
            }
        }
        props.put(POWER_PROFILE_NAME, profileName);
        return props;
    }

    private void removeProfile(Vector removeProfs) {
        loadProfileConfig();
        for (int j = 0; j < removeProfs.size(); j++) {
            try {
                String profile = config.getProperty(POWER_PROFILE_NAME);
                String removeProfileStr = ((String)removeProfs.elementAt(j));
                String removeProfile = removeProfileStr.substring(12);

                if(profile.equals(removeProfile) || profile.startsWith(removeProfile + ",") || profile.endsWith("," + removeProfile) || (profile.indexOf("," + removeProfile + ",") > -1)) {
                    clearProfile(config, removeProfile);
                }
            } catch (Exception ioe) {
                System.out.println("Failed to remove configurations for the specified profile");
                if (DEBUG) {
                    ioe.printStackTrace();
                }
            }
        }
        config.save();
    }

    public void clearProfile(ConfigProps prop, String profileName) {

        prop.setProperty(profileName + DOT + POWER_PROFILE_DESC, null);
        prop.setProperty(profileName + DOT + HIBERNATE_PROP, null);
        prop.setProperty(profileName + DOT + HIBER_IDLETIME_PROP, null);
        prop.setProperty(profileName + DOT + MONITOR_IDLETIME_PROP, null);
        prop.setProperty(profileName + DOT + DISK_IDLETIME_PROP, null);
        prop.setProperty(profileName + DOT + STANDBY_IDLETIME_PROP, null);
        prop.setProperty(profileName + DOT + HIBER_IDLETIME_DC_PROP, null);
        prop.setProperty(profileName + DOT + MONITOR_IDLETIME_DC_PROP, null);
        prop.setProperty(profileName + DOT + DISK_IDLETIME_DC_PROP, null);
        prop.setProperty(profileName + DOT + STANDBY_IDLETIME_DC_PROP, null);
        prop.setProperty(profileName + DOT + PROMPT_PASSWORD_PROP, null);
        prop.setProperty(profileName + DOT + FORCE_APPLY_PROP, null);
        String availableProfiles = prop.getProperty(POWER_PROFILE_NAME);

        if (availableProfiles != null) {
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
        prop.setProperty(POWER_PROFILE_NAME, availableProfiles);
    }

    private void loadProfileConfig() {
        config = new ConfigProps(new File(rootDir, "PowerSettings.txt"));
    }

    private Vector getSelectedServers(HttpServletRequest req) {
        Vector v = new Vector();
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