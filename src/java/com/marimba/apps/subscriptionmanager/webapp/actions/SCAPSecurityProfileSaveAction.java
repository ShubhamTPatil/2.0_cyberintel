// Copyright 2018, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Subscription;
import com.marimba.apps.subscriptionmanager.ObjectManager;
import static com.marimba.apps.subscriptionmanager.approval.IApprovalPolicyConstants.*;

import com.marimba.apps.subscriptionmanager.beans.SCAPContentDetails;
import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiff;
import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiffMailFormatter;
import com.marimba.apps.subscriptionmanager.webapp.forms.SCAPSecurityProfileForm;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.system.PowerProfileBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.SCAPUtils;
import com.marimba.intf.util.*;
import com.marimba.tools.config.*;
import com.marimba.tools.util.Password;
import com.marimba.tools.util.Props;
import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.tools.ldap.LDAPName;
import com.marimba.tools.util.OSConstants;
import com.marimba.tools.xml.XMLOutputStream;
import com.marimba.tools.xml.XMLParser;
import com.marimba.tools.xml.XMLException;
import com.marimba.webapps.intf.SystemException;

import com.marimba.xccdf.util.*;
import com.marimba.oval.util.*;
import com.marimba.intf.db.IConnectionPool;

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
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;
/**
 * SCAPSecurityProfileSaveAction
 *
 * @author Selvaraj Jegatheesan
 * @version $Revision$, $Date$
 */

public class SCAPSecurityProfileSaveAction extends AbstractAction {
	private File rootDir;
    private IUser user;
    String modifiedUserDN = null;
    private ConfigProps config;
    private HttpServletRequest req;
    private String KEY_OLD_SUB = "oldsub";
    private String KEY_NEW_SUB = "newsub";
    private static final String DOT = ".";
    private SCAPSecurityProfileForm profileForm;
    private Map<String, Map<String, ISubscription>> subsMap;
    HttpSession session;

    public ActionForward execute( ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

        System.out.println("SCAPSecurityProfileSaveAction called...");
        req = request;
        profileForm = (SCAPSecurityProfileForm) form;
        String  action = request.getParameter("action");
        String lastAction = null;
        String profileName = null;
        String profileDesc = null;

        String forceApply = null;
        String immediateUpdate = null;
        String profileNamePrefix = "";
        String profiles = null;

        init(request);
        session = request.getSession();
        subsMap = new LinkedHashMap<String, Map<String, ISubscription>>(20);
        rootDir = main.getDataDirectory();
        try {
            this.user = GUIUtils.getUser(req);
            modifiedUserDN = main.resolveUserDN(user.getName());
        } catch (Exception e) {
            if (DEBUG) e.printStackTrace();
        }
        loadStandardSCAPContentDetails(request);
        if ("save".equals(action) || "apply".equals(action)) {
        	loadProfileConfig();
            System.out.println("SCAPSecurityProfileSaveAction: action is save or apply...");
            boolean pluginPublished = false, configSaved = false, dbInserted = false, customTemplatePropSaved = false;
            Map<String, String> existingProfileSettings = new HashMap<String, String>();
            Map<String, String> existingProfileChanges = new HashMap<String, String>();

            profileName = profileForm.getName().trim();
            profileDesc = profileForm.getDescription().trim();
            forceApply = profileForm.getForceApplyEnabled();
            String selectedSCAPContent = profileForm.getSelectedSCAPContent();
            System.out.println("selectedSCAPContent :" + selectedSCAPContent);
            String scapType = SCAPUtils.getSCAPUtils().getXMLContentType(selectedSCAPContent);
            System.out.println("scapType :" + scapType);
            String contentIdValue = SCAPUtils.getSCAPUtils().getScapContentId(session, "scapcontentdetails", selectedSCAPContent);
            System.out.println("selected SCAP Content Id:" + contentIdValue);
            String contentTitleValue = SCAPUtils.getSCAPUtils().getSessionMapValue(session, selectedSCAPContent, "scapcontentdetailsmap");
            System.out.println("selected SCAP Content Title:" + contentTitleValue);
            String selectedProfileId = profileForm.getSelectedSCAPProfile();
            String parentTemplateName = "";
            if (selectedProfileId.lastIndexOf("@"+parentTemplateName) != -1) {
                parentTemplateName = selectedProfileId.substring(selectedProfileId.lastIndexOf("@")+1);
                selectedProfileId = selectedProfileId.substring(0,selectedProfileId.lastIndexOf("@"));
            }
            System.out.println("selected profile id:" + selectedProfileId);
            System.out.println("selected parent template name:" + parentTemplateName);
            String selectedProfileTitle = SCAPUtils.getSCAPUtils().getSessionMapValue(session, selectedProfileId, selectedSCAPContent);
            System.out.println("selected profile title :" + selectedProfileTitle);

            String templateType = "scaptemplates";
            String modifiedRules = profileForm.getModifiedRules();
            Map<String, String> modifiedRulesMap = new HashMap<String, String>();
            if ((parentTemplateName != null) && (parentTemplateName.trim().length() > 0)) {
                modifiedRulesMap = readProfileChanges(parentTemplateName, templateType);
            }

            if (modifiedRules != null) {
                modifiedRules = modifiedRules.trim();
            } else {
                modifiedRules = "";
            }

            if (modifiedRules.indexOf(",") > -1) {
                String[] modifiedRulesSplit = modifiedRules.split(",");
                for (int i=0; i < modifiedRulesSplit.length; i+=2) {
                    String key = modifiedRulesSplit[i];
                    if ((key == null) || (key.trim().length() < 0)) {
                        continue;
                    }
                    if (key.endsWith("Chk")) {
                    } else if (key.endsWith("Select")) {
                    } else if (key.endsWith("Text")) {
                        key = key.substring(0, key.length() - 4) + "Select";
                    } else {
                        continue;
                    }
                    String value = "";
                    if ((i + 1) != modifiedRulesSplit.length) {
                        value = modifiedRulesSplit[i + 1];
                    }
                    if("oval".equals(scapType)) {
                    	int index = key.trim().indexOf("Chk");
                    	String modifiedKey = key.trim();
                    	if(index > -1) {
                    		modifiedKey = modifiedKey.substring(0, index);
                    	}
                    	modifiedRulesMap.put(modifiedKey, value.trim());
                	} else {
                		modifiedRulesMap.put(key.trim(), value.trim());
                	}
                }
            }

            System.out.println("Profile Name :" + profileName);
            System.out.println("Profile Description :" + profileDesc);
            System.out.println("force policy apply :" + forceApply);
            System.out.println("selected SCAP Content File Name :" + selectedSCAPContent);
            System.out.println("Modified Rules :" + modifiedRules);
            System.out.println("Modified Rules Map :" + modifiedRulesMap);

            if (new File(rootDir + File.separator + templateType + File.separator + profileName + ".properties").exists()) {
                existingProfileChanges = readProfileChanges(profileName, templateType);
                existingProfileSettings = readProfileSettings(profileName, templateType);
            }
            
            Map<String, String> parametersMap = new HashMap<String, String>();
            parametersMap.put("profile.id", selectedProfileId);
            parametersMap.put("profile.title", selectedProfileTitle);
            parametersMap.put("profile.description", profileDesc);

            customTemplatePropSaved = writeCustomTemplateChange(modifiedRulesMap, profileName, "save".equals(action) ? "add" : "edit", parametersMap, scapType);

            String result = "";
            System.out.println("customTemplatePropSaved :" + customTemplatePropSaved);
            if (customTemplatePropSaved) {
                try {
                    String availableProfiles = config.getProperty(SCAP_SECURITY_PROFILE_NAME);
                    System.out.println("availableProfiles :" + availableProfiles);
                    if (profileName != null && profileName != "") {
                        profileNamePrefix = profileName + ".";
                        profiles = ((availableProfiles != null) &&
                                ((availableProfiles.startsWith(profileName + ",")) ||
                                        (availableProfiles.endsWith("," + profileName)) ||
                                        (availableProfiles.equals(profileName)) ||
                                        (availableProfiles.indexOf("," + profileName + ",") > -1)))
                                ? availableProfiles : ((availableProfiles == null) ? profileName : availableProfiles + "," + profileName);
                        config.setProperty(SCAP_SECURITY_PROFILE_NAME, profiles);
                        config.setProperty(profileNamePrefix + SCAP_SECURITY_TEMPLATE_NAME, profileName);

                        if (!isEmpty(profileDesc)) {
                            config.setProperty(profileNamePrefix + SCAP_SECURITY_TEMPLATE_DESC, profileDesc);
                        }
                        config.setProperty(profileNamePrefix + SCAP_SECURITY_ENABLED, "true");
                        String forceApplyEnabled = profileForm.getForceApplyEnabled();
                        if ("on".equals(forceApplyEnabled)) {
                            forceApplyEnabled = "true";
                        } else {
                            forceApplyEnabled = "false";
                        }
                        config.setProperty(profileNamePrefix + SCAP_SECURITY_FORCE_APPLY, null);
                        config.setProperty(profileNamePrefix + SCAP_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, profileForm.getSelectedSCAPContent());
                        config.setProperty(profileNamePrefix + SCAP_SECURITY_SCAP_SELECTEDCONTENT_ID, contentIdValue);
                        config.setProperty(profileNamePrefix + SCAP_SECURITY_SCAP_SELECTEDCONTENT_TITLE, contentTitleValue);
                        config.setProperty(profileNamePrefix + SCAP_SECURITY_SCAP_SELECTEDPROFILE_ID, selectedProfileId);
                        config.setProperty(profileNamePrefix + SCAP_SECURITY_SCAP_SELECTEDPROFILE_TITLE, selectedProfileTitle);

                        if (!config.save()) {
                            configSaved = false;
                            result = "template.error.config";
                        } else {
                            configSaved = true;
                            System.out.println("Details : " + rootDir + File.separator + templateType + File.separator + profileName + ".properties");
                            if("oval".equals(scapType)) {
                            	dbInserted = insertCustomOvalTemplateIntoDB(selectedSCAPContent, selectedProfileId, profileName, profileDesc, (rootDir + File.separator + templateType + File.separator + profileName + ".properties"));
                            } else {
                            	dbInserted = insertCustomTemplateIntoDB(selectedSCAPContent, selectedProfileId, profileName, profileDesc, (rootDir + File.separator + templateType + File.separator + profileName + ".properties"));
                            }

                            if (dbInserted) {
                                // do a plugin publish now... just reload the same plugin props we persisted in previous publish and reset it.
                                // this will ensure, the template file is also now sent to plugin...
                                String publishUser = main.getConfig().getProperty("subscriptionmanager.publishurl.username");
                                String publishPassword = main.getConfig().getProperty("subscriptionmanager.publishurl.password");
                                if (publishPassword != null) {
                                    publishPassword = Password.decode(publishPassword);
                                }

                                try {
                                    pluginPublished = main.publishChannel(publishUser, publishPassword, main.getConfig());
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                    pluginPublished = false;
                                }

                                if (pluginPublished) {
                                    createProfileList(request);
                                    /*
                                    if ("apply".equals(action)) {
                                        updatePolicyForAffectedTargets(profileName);
                                    }
                                    */
                                    result = "template.success";
                                } else {
                                    result = "template.error.plugin";
                                }
                            } else {
                                result = "template.error.db";
                            }
                        }

                    }
                } catch (Exception ioe) {
                    ioe.printStackTrace();
                    config = null;
                }
            } else {
                result = "template.error.template";
            }

            request.setAttribute("createedit.result", result.equals("template.success") ? "success" : "failed");
            if ("save".equals(action)) {
                request.setAttribute("create.result", getString(req.getLocale(), "page.create." + result, profileName));
            } else {
                request.setAttribute("edit.result", getString(req.getLocale(), "page.edit." + result, profileName));
            }
            if (customTemplatePropSaved && dbInserted && configSaved && pluginPublished) {
                System.out.println("Successfully saved SCAP Security Policy template for the profile :" + profileName);
            } else {
                if ("apply".equals(action)) {
                    if (customTemplatePropSaved) {
                        if (configSaved) {
                            if (dbInserted) {
                                if (pluginPublished) {
                                    //all success... do nothing...
                                } else {
                                    revertChanges(profileName, templateType, existingProfileChanges, existingProfileSettings, scapType, false, false, false, true);
                                }
                            } else {
                                revertChanges(profileName, templateType, existingProfileChanges, existingProfileSettings, scapType, false, false, true, true);
                            }
                        } else {
                            revertChanges(profileName, templateType, existingProfileChanges, existingProfileSettings, scapType, false, true, true, true);
                        }
                    } else {
                        // do nothing... nothing changed due to edit...
                    }
                } else {
                    if (config.getProperty(SCAP_SECURITY_PROFILE_NAME) != null) {
                        Vector<String> removeProfs = new Vector<String>();
                        removeProfs.add(profileName);
                        removeProfile(removeProfs, new HashMap<String, String>(), true);
                    }
                }

                System.out.println("Failed to save SCAP Security Policy template for the profile :" + profileName);
            }

            createProfileList(request);

            profileForm.reset();
            lastAction = null;

            return mapping.findForward("save");

        } else if ("add".equals(action)) {
            System.out.println("SCAPSecurityProfileSaveAction: action is 'add'...");
            profileForm.reset();
            profileForm.setCreate("true");
            profileForm.setInitialIFramePath(getInitialIFramePath(request));
            request.setAttribute("customization", null);
            return mapping.findForward("add");
        } else if ("remove".equals(action)) {
            System.out.println("SCAPSecurityProfileSaveAction: action is 'remove'...");
            Vector<String> toRemoveProfiles = new Vector<String>();
            String paramName = "";
            Enumeration params = request.getParameterNames();
            while(params.hasMoreElements()) {
                paramName = (String) params.nextElement();
                if (("on".equals(request.getParameter(paramName))) && (paramName.indexOf("profile_sel_") > -1)) {
                    toRemoveProfiles.addElement(paramName);
                }
            }

            HashMap<String, String> result = new HashMap<String, String>();
            removeProfile(toRemoveProfiles, result, false);
            System.out.println("removeProfile, result - " + result);

            boolean allSuccess = true;
            ArrayList<String> errors = new ArrayList<String>();
            ArrayList<String> success = new ArrayList<String>();
            for (Map.Entry<String, String> entry : result.entrySet()) {
                String profile = entry.getKey();
                String status = entry.getValue();
                if ("success".equals(status)) {
                    success.add(profile);
                } else {
                    allSuccess = false;
                    errors.add(profile + " - " + getString(req.getLocale(), status));
                }
            }
            createProfileList(request);

            if (allSuccess) {
                request.setAttribute("remove.allSuccess", "true");
            } else {
                if ((success != null) && (success.size() > 0)) {
                    request.setAttribute("remove.success", success);
                }
                if ((errors != null) && (errors.size() > 0)) {
                    request.setAttribute("remove.errors", errors);
                }
            }

            return mapping.findForward("remove");

        } else if ("edit".equals(action)) {
            System.out.println("SCAPSecurityProfileSaveAction: action is 'edit'...");

            Vector v = getSelectedServers(request);

            if (v.size() == 1) {
                profileName = (String)v.elementAt(0);
            }

            System.out.println("SCAPSecurityProfileSaveAction: edit is in progress for profile: " + profileName);
            Hashtable profileInfo = readProfile(profileName);
            profileForm.loadProfile(profileInfo);

            System.out.println("SCAPSecurityProfileSaveAction:: Loading profile: done...");

            profileForm.setCreate("false");
            lastAction = "edit";
            loadAffectedTargets(profileName);

            ConfigProps customProfile = new ConfigProps(new File(rootDir + File.separator + "scaptemplates", profileName + ".properties"));
            String[] customProfileProps = customProfile.getPropertyPairs();
            request.setAttribute("customization", customProfileProps);
            request.setAttribute("content.id", (String) profileInfo.get(SCAP_SECURITY_SCAP_SELECTEDCONTENT_ID));
            request.setAttribute("content.title", (String) profileInfo.get(SCAP_SECURITY_SCAP_SELECTEDCONTENT_TITLE));
            request.setAttribute("profile.id", (String) profileInfo.get(SCAP_SECURITY_SCAP_SELECTEDPROFILE_ID));
            request.setAttribute("profile.title", (String) profileInfo.get(SCAP_SECURITY_SCAP_SELECTEDPROFILE_TITLE));

            return mapping.findForward("add");
        } else if ("cancel".equals(action)) {
            createProfileList(request);
            return mapping.findForward("cancel");
        } else {
            createProfileList(request);
            return mapping.findForward("load");
        }
    }

    private void revertChanges(String revertProfile, String templateType, Map<String, String> existingProfileChanges, Map<String, String> existingProfileSettings, 
    		String scapType, boolean skipTemplate, boolean skipConfig, boolean skipDb, boolean skipPlugin) throws Exception {
        String contentIdValue = existingProfileSettings.get(SCAP_SECURITY_SCAP_SELECTEDCONTENT_ID);
        String contentTitleValue = existingProfileSettings.get(SCAP_SECURITY_SCAP_SELECTEDCONTENT_TITLE);
        String contentFileName = existingProfileSettings.get(SCAP_SECURITY_SCAP_SELECTEDCONTENT_FILENAME);
        String profileId = existingProfileSettings.get(SCAP_SECURITY_SCAP_SELECTEDPROFILE_ID);
        String profileName = existingProfileSettings.get(SCAP_SECURITY_TEMPLATE_NAME);
        String profileTitle = existingProfileSettings.get(SCAP_SECURITY_SCAP_SELECTEDPROFILE_TITLE);
        String profileDescription = existingProfileSettings.get(SCAP_SECURITY_TEMPLATE_DESC);
        String forceApplyEnabled = existingProfileSettings.get(SCAP_SECURITY_FORCE_APPLY);
        
        Map<String, String> parametersMap = new HashMap<String, String>();
        parametersMap.put("profile.id", profileId);
        parametersMap.put("profile.title", profileTitle);
        parametersMap.put("profile.description", profileDescription);
        
        if (!skipTemplate) {
            writeCustomTemplateChange(existingProfileChanges, revertProfile, "edit", true, parametersMap, scapType);
        }

        if (!skipConfig) {
            String availableProfiles = config.getProperty(SCAP_SECURITY_PROFILE_NAME);
            if (profileName != null && profileName != "") {
                String profileNamePrefix = profileName + ".";
                String profiles = ((availableProfiles != null) &&
                        ((availableProfiles.startsWith(profileName + ",")) ||
                                (availableProfiles.endsWith("," + profileName)) ||
                                (availableProfiles.equals(profileName)) ||
                                (availableProfiles.indexOf("," + profileName + ",") > -1)))
                        ? availableProfiles : ((availableProfiles == null) ? profileName : availableProfiles + "," + profileName);
                config.setProperty(SCAP_SECURITY_PROFILE_NAME, profiles);
                config.setProperty(profileNamePrefix + SCAP_SECURITY_TEMPLATE_NAME, profileName);

                if (!isEmpty(profileDescription)) {
                    config.setProperty(profileNamePrefix + SCAP_SECURITY_TEMPLATE_DESC, profileDescription);
                }
                config.setProperty(profileNamePrefix + SCAP_SECURITY_ENABLED, "true");
                if ("on".equals(forceApplyEnabled)) {
                    forceApplyEnabled = "true";
                } else {
                    forceApplyEnabled = "false";
                }
                config.setProperty(profileNamePrefix + SCAP_SECURITY_FORCE_APPLY, null);
                config.setProperty(profileNamePrefix + SCAP_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, contentFileName);
                config.setProperty(profileNamePrefix + SCAP_SECURITY_SCAP_SELECTEDCONTENT_ID, contentIdValue);
                config.setProperty(profileNamePrefix + SCAP_SECURITY_SCAP_SELECTEDCONTENT_TITLE, contentTitleValue);
                config.setProperty(profileNamePrefix + SCAP_SECURITY_SCAP_SELECTEDPROFILE_ID, profileId);
                config.setProperty(profileNamePrefix + SCAP_SECURITY_SCAP_SELECTEDPROFILE_TITLE, profileTitle);

                config.save();
            }
        }

        if (!skipDb) {
        	if("oval".equals(scapType)) {
        		insertCustomOvalTemplateIntoDB(contentFileName, profileId, profileName, profileDescription, (rootDir + File.separator + templateType + File.separator + profileName + ".properties"));
        	} else {
        		insertCustomTemplateIntoDB(contentFileName, profileId, profileName, profileDescription, (rootDir + File.separator + templateType + File.separator + profileName + ".properties"));
        	}
        }
    }

    private boolean writeCustomTemplateChange(Map<String, String> changesMap, String profileName, String action, Map<String, String> parametersMap, String xmlType) {
        return writeCustomTemplateChange(changesMap, profileName, action, false, parametersMap, xmlType);
    }

    private boolean writeCustomTemplateChange(Map<String, String> changesMap, String profileName, String action, boolean overwrite,
    		Map<String, String> parametersMap, String xmlType) {
        File scapTemplateDir = new File(rootDir + File.separator + "scaptemplates");
        if (!scapTemplateDir.exists()) {
            scapTemplateDir.mkdir();
        }
        File scapTemplateFile = new File(scapTemplateDir, profileName + ".properties");
        if((((null == changesMap) || (changesMap.size() == 0)) && "add".equals(action)) || overwrite) {
            try {
                if (scapTemplateFile.exists()) {
                    scapTemplateFile.delete();
                }
                scapTemplateFile.createNewFile();
            } catch (Exception ed) {
                ed.printStackTrace();
            }
        }

        if(null == changesMap || changesMap.size() == 0) {
            //do nothing...
        } else {
            try {
                ConfigProps customProfile = new ConfigProps(scapTemplateFile);
                for (Map.Entry<String, String> entry : changesMap.entrySet()) {
                    customProfile.setProperty(entry.getKey(), entry.getValue());
                }
                if(null != parametersMap && "oval".equals(xmlType)) {
	                for (Map.Entry<String, String> entry : parametersMap.entrySet()) {
	                    customProfile.setProperty(entry.getKey(), entry.getValue());
	                }
                }
                customProfile.save();
            } catch (Exception ed) {
                ed.printStackTrace();
            }
        }
        return scapTemplateFile.exists();
    }

    private Map<String, String> readProfileChanges(String profileName, String templateType) {
        Map<String, String> props = new HashMap<String, String>(10);
        try {
            ConfigProps customProfile = new ConfigProps(new File(rootDir + File.separator + templateType + File.separator + profileName + ".properties"));
            String[] customProfileProps = customProfile.getPropertyPairs();

            if (customProfileProps != null) {
                for (int i = 0; i < customProfileProps.length; i += 2) {
                    props.put(customProfileProps[i], customProfileProps[i + 1]);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return props;
    }

    private Map<String, String> readProfileSettings(String profileName, String templateType) {
        Map<String, String> props = new HashMap<String, String>();
        ConfigProps config = new ConfigProps(new File(rootDir, SCAP_SECURITY_FILE_NAME));
        String profile = config.getProperty(SCAP_SECURITY_PROFILE_NAME);

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

        props.put("name", profileName);

        return props;
    }

    private boolean insertCustomTemplateIntoDB(String contentFileName, String profileNameOld, String profileNameNew, String profileDescNew, String templateProperties) {
        boolean inserted = false;
        try {
            String securityManagerXCCDFDir = rootDir + File.separator + "xccdf" + File.separator + "nonwindows" + File.separator;
            System.out.println("SCAPSecurityProfileSaveAction: insertCustomTemplateIntoDB(), vDeskXCCDFDir - " + securityManagerXCCDFDir);

            XCCDFHandler xccdfHandler = new XCCDFHandler();
            String contentFilePath = securityManagerXCCDFDir + File.separator + contentFileName;
            IConnectionPool connectionPool = main.getConnectionPool();
            System.out.println("SCAPSecurityProfileSaveAction: insertCustomTemplateIntoDB(), connectionPool1 - " + connectionPool);
            if (connectionPool == null) {
                try {
                    main.getQueryExecutor().run();
                    connectionPool = main.getConnectionPool();
                } catch (Throwable t) {
                    connectionPool = null;
                }
                System.out.println("SCAPSecurityProfileSaveAction: insertCustomTemplateIntoDB(), connectionPool2 - " + connectionPool);
            }
            int result = 1;
            if (connectionPool != null) {
                result = xccdfHandler.addProfileToDB(contentFilePath, profileNameOld, profileNameOld + "_" + profileNameNew, profileDescNew, templateProperties, connectionPool, true);
                System.out.println("SCAPSecurityProfileSaveAction: insertCustomTemplateIntoDB(), result - " + result);
            }
            inserted = (result == 0);
        } catch(Exception ed) {
            ed.printStackTrace();
            inserted = false;
        }
        return inserted;
    }
    private boolean insertCustomOvalTemplateIntoDB(String contentFileName, String profileNameOld, String profileNameNew, String profileDescNew, String templateProperties) {
        boolean inserted = false;
        try {
            String securityManagerOVALDir = rootDir + File.separator + "oval" + File.separator + "nonwindows" + File.separator;
            System.out.println("SCAPSecurityProfileSaveAction: insertCustomOvalTemplateIntoDB(), vDeskOVALFDir - " + securityManagerOVALDir);

            OVALHandler ovalHandler = new OVALHandler();
            String contentFilePath = securityManagerOVALDir + File.separator + contentFileName;
            IConnectionPool connectionPool = main.getConnectionPool();
            System.out.println("SCAPSecurityProfileSaveAction: insertCustomOvalTemplateIntoDB(), connectionPool1 - " + connectionPool);
            if (connectionPool == null) {
                try {
                    main.getQueryExecutor().run();
                    connectionPool = main.getConnectionPool();
                } catch (Throwable t) {
                    connectionPool = null;
                }
                System.out.println("SCAPSecurityProfileSaveAction: insertCustomOvalTemplateIntoDB(), connectionPool2 - " + connectionPool);
            }
            int result = 1;
            if (connectionPool != null) {
                result = ovalHandler.addProfileToDB(contentFilePath, profileNameOld, profileNameOld + "_" + profileNameNew, profileDescNew, templateProperties, connectionPool, true);
                System.out.println("SCAPSecurityProfileSaveAction: insertCustomOvalTemplateIntoDB(), result - " + result);
            }
            inserted = (result == 0);
        } catch(Exception ed) {
            ed.printStackTrace();
            inserted = false;
        }
        return inserted;
    }
    
    private boolean deleteCustomTemplateFromDB(String contentName, String profileName) {
        boolean deleted = false;
        try {
            XCCDFHandler xccdfHandler = new XCCDFHandler();
            System.out.println("SCAPSecurityProfileSaveAction: deleteCustomTemplateFromDB(), contentName - " + contentName);
            System.out.println("SCAPSecurityProfileSaveAction: deleteCustomTemplateFromDB(), profileName - " + profileName);
            IConnectionPool connectionPool = main.getConnectionPool();
            System.out.println("SCAPSecurityProfileSaveAction: deleteCustomTemplateFromDB(), connectionPool1 - " + connectionPool);
            if (connectionPool == null) {
                try {
                    main.getQueryExecutor().run();
                    connectionPool = main.getConnectionPool();
                } catch (Throwable t) {
                    connectionPool = null;
                }
                System.out.println("SCAPSecurityProfileSaveAction: deleteCustomTemplateFromDB(), connectionPool2 - " + connectionPool);
            }
            int result = 1;
            if (connectionPool != null) {
                result = xccdfHandler.deleteProfileFromDB(contentName, profileName, connectionPool);
                System.out.println("SCAPSecurityProfileSaveAction: deleteCustomTemplateFromDB(), result - " + result);
            }
            deleted = (result == 0);
        } catch(Exception ed) {
            ed.printStackTrace();
            deleted = false;
        }
        return deleted;
    }
    private boolean deleteCustomOVALTemplateFromDB(String contentName, String profileName) {
        boolean deleted = false;
        try {
            OVALHandler ovalHandler = new OVALHandler();
            System.out.println("SCAPSecurityProfileSaveAction: deleteCustomOVALTemplateFromDB(), contentName - " + contentName);
            System.out.println("SCAPSecurityProfileSaveAction: deleteCustomOVALTemplateFromDB(), profileName - " + profileName);
            IConnectionPool connectionPool = main.getConnectionPool();
            System.out.println("SCAPSecurityProfileSaveAction: deleteCustomOVALTemplateFromDB(), connectionPool1 - " + connectionPool);
            if (connectionPool == null) {
                try {
                    main.getQueryExecutor().run();
                    connectionPool = main.getConnectionPool();
                } catch (Throwable t) {
                    connectionPool = null;
                }
                System.out.println("SCAPSecurityProfileSaveAction: deleteCustomOVALTemplateFromDB(), connectionPool2 - " + connectionPool);
            }
            int result = 1;
            if (connectionPool != null) {
                result = ovalHandler.deleteProfileFromDB(contentName, profileName, connectionPool);
                System.out.println("SCAPSecurityProfileSaveAction: deleteCustomOVALTemplateFromDB(), result - " + result);
            }
            deleted = (result == 0);
        } catch(Exception ed) {
            ed.printStackTrace();
            deleted = false;
        }
        return deleted;
    }
    private void loadStandardSCAPContentDetails(HttpServletRequest request) {
        TreeMap<String, String> scapContentsMap = new TreeMap<String, String>();
        scapContentsMap.putAll((HashMap<String, String>) context.getAttribute("scapcontentdetailsmap"));
        TreeMap<String, String> inSyncScapContentsMap = new TreeMap<String, String>();
    	session.setAttribute("scapcontentdetailsmap", scapContentsMap);
    	String[] scapContentsXML = SCAPUtils.getSCAPUtils().getSupportedSCAPContentsXML();
        if (scapContentsXML != null) {
            for (String scapContent : scapContentsXML) {
                Map<String, String> scapContentsXMLMap = (Map<String, String>) context.getAttribute(scapContent);
                loadNonWindowsCustomContentDetails(request, scapContent, scapContentsXMLMap);
                session.setAttribute(scapContent, scapContentsXMLMap);
                if (SCAPUtils.getSCAPUtils().isSyncedOnce(scapContent)) {
                    inSyncScapContentsMap.put(scapContent, scapContentsMap.get(scapContent));
                }
            }
            session.setAttribute("insyncscapcontentdetailsmap", inSyncScapContentsMap);
            if (inSyncScapContentsMap.size() > 0) {
                String firstSCAPXML = (String) inSyncScapContentsMap.keySet().toArray()[0];
                TreeMap<String, String> scapContentsXMLMap = new TreeMap<String, String>();
                scapContentsXMLMap.putAll((Map<String, String>) context.getAttribute(firstSCAPXML));
                profileForm.setSelectedSCAPProfileSelect((String) scapContentsXMLMap.keySet().toArray()[0]);
                session.setAttribute("first_scap_content_profiles", scapContentsXMLMap);
            } else {
                session.setAttribute("first_scap_content_profiles", (new HashMap<String, String>()));
            }
        }
    }

    private void loadNonWindowsCustomContentDetails(HttpServletRequest request, String scapContent, Map<String, String> scapContentsXMLMap) {
        DistributionBean distributionBean = new DistributionBean(request);
        Props props = distributionBean.getScapProfiles();
        String[] scapProfileStr = distributionBean.getScapProfiles().getPropertyPairs();

        for (int i = 0; i < scapProfileStr.length; i++) {
            if ("name".equals(scapProfileStr[i])) {
                StringTokenizer st = new StringTokenizer(scapProfileStr[i + 1], ",", false);
                // iterate through tokens
                while (st.hasMoreTokens()) {
                    String templateName = st.nextToken(",");
                    String profileId = props.getProperty(templateName + "." + SCAP_SECURITY_SCAP_SELECTEDPROFILE_ID);
                    String templateDesc = props.getProperty(templateName + "." + SCAP_SECURITY_TEMPLATE_DESC);
                    String fileName = props.getProperty(templateName + "." + SCAP_SECURITY_SCAP_SELECTEDCONTENT_FILENAME);

                    if (fileName.equals(scapContent)) {
                        if (profileId != null && templateName != null) {
                            profileId = profileId + "@" + templateName;
                            scapContentsXMLMap.put(profileId, templateDesc);
                        }
                    }
                }
                break;
            }
        }
    }

    private String getInitialIFramePath(HttpServletRequest request) {
        String path = "";
        TreeMap<String, String> scapContentsMap = new TreeMap<String, String>();
        scapContentsMap.putAll((HashMap<String, String>) context.getAttribute("scapcontentdetailsmap"));
        TreeMap<String, String> inSyncScapContentsMap = new TreeMap<String, String>();
        String[] scapContentsXML = SCAPUtils.getSCAPUtils().getSupportedSCAPContentsXML();
        if (scapContentsXML != null) {
            for (String scapContent : scapContentsXML) {
                if (SCAPUtils.getSCAPUtils().isSyncedOnce(scapContent)) {
                    inSyncScapContentsMap.put(scapContent, scapContentsMap.get(scapContent));
                }
            }

            if (inSyncScapContentsMap.size() > 0) {
                String firstScapXML = (String) inSyncScapContentsMap.keySet().toArray()[0];
                TreeMap<String, String> scapContentsXMLMap = new TreeMap<String, String>();
                scapContentsXMLMap.putAll((Map<String, String>) context.getAttribute(firstScapXML));
                String firstProfile = (String) scapContentsXMLMap.keySet().toArray()[0];
                String firstTemplateName = "";
                if (firstProfile.lastIndexOf("@"+firstTemplateName) != -1) {
                    firstTemplateName = firstProfile.substring(firstProfile.lastIndexOf("@")+1);
                    firstProfile = firstProfile.substring(0,firstProfile.lastIndexOf("@"));
                }
                path = "/spm/securitymgmt?command=gethtml&target=nonwindows&customize=true&content=" + firstScapXML + "&profile=" + firstProfile;
                if ((firstTemplateName != null) && (firstTemplateName.trim().length() > 0)) {
                    path = path + "&template="+ firstTemplateName + ".properties";
                }
            } else {
            }
        }
        return path;
    }
    void updateAffectedTargets () throws Exception {
        loadProfileConfig();
        String profileName = config.getProperty(SCAP_SECURITY_PROFILE_NAME);
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

    private void loadAffectedTargets(String profileName) throws Exception {
        profileForm.setAffectedTargets(getAffectedTargets(profileName));
    }

    private Map<String, String> getAffectedTargets(String profileName) throws Exception {
        Map<String, String> affectedTargets = new Hashtable<String, String>(10);

        String searchStr = "(&("+LDAPVarsMap.get("PROPERTY")+"="+SCAP_SECURITY_TEMPLATE_NAME+PROP_DELIM+PROP_SCAP_SECURITY_KEYWORD+"=";
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

                String profNameWithPriority = newSub.getProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_TEMPLATE_NAME);
                int index = profNameWithPriority.indexOf(',');
                if (index != -1) {
                    profNameFromPolicy = profNameWithPriority.substring(0, index);
                    priority = profNameWithPriority.substring(index+1, profNameWithPriority.length());
                }

                if (profileName.equalsIgnoreCase(profNameFromPolicy)) {
                    newSub.clearProperties(PROP_SCAP_SECURITY_KEYWORD);
                    Hashtable props = readProfile(profileName);
                    if (props != null && props.size() > 0){
                        Enumeration propKeys = props.keys();
                        while (propKeys.hasMoreElements()) {
                            String key = (String) propKeys.nextElement();
                            if (key.equals(SCAP_SECURITY_PROFILE_NAME)) {
                                setProperty(newSub, SCAP_SECURITY_TEMPLATE_NAME, (String) props.get(key), priority);
                            } //else if (key.equals(SCAP_SECURITY_PROFILE_DESC)) {
                                // dont store profile desc into LDAP
                            //} 
                            else {
                                setProperty(newSub, key, (String) props.get(key), priority);
                            }
                        }
                        setProperty(newSub, SCAP_SECURITY_ENABLED, "true", priority);
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

        sub.setProperty(PROP_SCAP_SECURITY_KEYWORD, key, value);
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
            String profile = config.getProperty(SCAP_SECURITY_PROFILE_NAME);

            if (profile != null) {
                StringTokenizer st = new StringTokenizer(profile, ",", false);
                //iterate through tokens
                while(st.hasMoreTokens()) {
                    String profileName = st.nextToken(",");
                    String profileDescription = config.getProperty(profileName + "." + SCAP_SECURITY_TEMPLATE_DESC);
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
        System.out.println("SCAPSecurityProfileSaveAction: reading profile: " + profileName);
        Hashtable props = new Hashtable(10);
        String profile = config.getProperty(SCAP_SECURITY_PROFILE_NAME);

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

        props.put(SCAP_SECURITY_PROFILE_NAME, profileName);

        return props;
    }

    private void removeProfile(Vector<String> removeProfs) {
        removeProfile(removeProfs, new HashMap<String, String>(), false);
    }

    private void removeProfile(Vector<String> removeProfs, HashMap<String, String> result, boolean ignoreFailures) {
        removeProfile(removeProfs, result, ignoreFailures, false, false, false, false);
    }

    private void removeProfile(Vector<String> removeProfs, HashMap<String, String> result, boolean ignoreFailures, boolean skipTemplate, boolean skipConfig, boolean skipDb, boolean skipPlugin) {
        loadProfileConfig();
        String profileStr = config.getProperty(SCAP_SECURITY_PROFILE_NAME);
        String[] profileArr = profileStr.split(PROP_DELIM);

        for (String removeProfileStr : removeProfs) {
            try {
                boolean pluginPublished = false, configUpdated = false, dbDeleted = false, customTemplatePropDeleted = false;
                String removeProfile = removeProfileStr.startsWith("profile_sel_") ? removeProfileStr.substring(12) : removeProfileStr;

                for (String profile : profileArr) {
                    if (!isEmpty(profile) && profile.equals(removeProfile)) {
                        String contentId = config.getProperty(removeProfile + "." + SCAP_SECURITY_SCAP_SELECTEDCONTENT_ID);
                        String profileId = config.getProperty(removeProfile + "." + SCAP_SECURITY_SCAP_SELECTEDPROFILE_ID);
                        String fileName = config.getProperty(removeProfile + "." + SCAP_SECURITY_SCAP_SELECTEDCONTENT_FILENAME);

                        System.out.println("SCAPSecurityProfileSaveAction: removeProfile(), removeProfile - " + removeProfile);
                        System.out.println("SCAPSecurityProfileSaveAction: removeProfile(), contentId - " + contentId);
                        System.out.println("SCAPSecurityProfileSaveAction: removeProfile(), profileId - " + profileId);
                        System.out.println("SCAPSecurityProfileSaveAction: removeProfile(), fileName - " + fileName);
                        
                        if (skipDb) {
                            dbDeleted = true;
                        } else {
                            //remove it from tables
                        	if("oval".equals(SCAPUtils.getSCAPUtils().getXMLContentType(fileName))) {
                        		dbDeleted = deleteCustomOVALTemplateFromDB(contentId, profileId + "_" + removeProfile);
                        	} else {
                        		dbDeleted = deleteCustomTemplateFromDB(contentId, profileId + "_" + removeProfile);
                        	}
                        }
                        if (dbDeleted || ignoreFailures) {
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
                                if (skipConfig) {
                                    configUpdated = true;
                                } else {
                                    clearProfile(config, removeProfile);
                                    configUpdated = true;
                                }
                            }

                            if (skipTemplate) {
                                customTemplatePropDeleted = true;
                            } else {
                                customTemplatePropDeleted = new File(rootDir + File.separator + "scaptemplates", removeProfile + ".properties").delete();
                            }

                            if (customTemplatePropDeleted || ignoreFailures) {
                                if (configUpdated || ignoreFailures) {
                                    if (skipPlugin) {
                                        pluginPublished = true;
                                    } else {
                                        // do a plugin publish now... just reload the same plugin props we persisted in previous publish and reset it.
                                        // this will ensure, the template file is also now sent to plugin...
                                        String publishUser = main.getConfig().getProperty("subscriptionmanager.publishurl.username");
                                        String publishPassword = main.getConfig().getProperty("subscriptionmanager.publishurl.password");
                                        if (publishPassword != null) {
                                            publishPassword = Password.decode(publishPassword);
                                        }

                                        try {
                                            pluginPublished = main.publishChannel(publishUser, publishPassword, main.getConfig());
                                        } catch (Throwable t) {
                                            t.printStackTrace();
                                            pluginPublished = false;
                                        }
                                    }

                                    if (pluginPublished) {
                                        System.out.println("Successfully deleted SCAP Security Policy template for the profile :" + profileId + "@" + removeProfile);
                                    } else {
                                        //plugin publish failed... but ignore it, no harm...
                                        System.out.println("Failed to delete SCAP Security Policy template for the profile :" + profileId + "@" + removeProfile);
                                        result.put(removeProfile, "page.remove.template.error.plugin");
                                    }

                                    //remove it from context
                                    SCAPUtils.getSCAPUtils().deleteProfileForScapContent(fileName, profileId + "@" + removeProfile);
                                    result.put(removeProfile, "success");
                                } else {
                                    //config update failed...
                                    result.put(removeProfile, "page.remove.template.error.config");
                                }
                            } else {
                                //template delete failed...
                                result.put(removeProfile, "page.remove.template.error.template");
                            }
                        } else {
                            //db delete failed...
                            result.put(removeProfile, "page.remove.template.error.db");
                        }
                    } else {
                        //ignore...
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
        sub.setProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_ENABLED, null);
        sub.setProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_TEMPLATE_NAME, null);
        sub.setProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_TEMPLATE_DESC, null);
        sub.setProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_FORCE_APPLY, null);
        sub.setProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, null);
        sub.setProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_SCAP_SELECTEDCONTENT_ID, null);
        sub.setProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_SCAP_SELECTEDCONTENT_TITLE, null);
        sub.setProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_SCAP_SELECTEDPROFILE_ID, null);
        sub.setProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_SCAP_SELECTEDPROFILE_TITLE, null);
    }

    public void clearProfile(ConfigProps prop, String profileName) {
        prop.setProperty(profileName + DOT + SCAP_SECURITY_TEMPLATE_NAME, null);
        prop.setProperty(profileName + DOT + SCAP_SECURITY_TEMPLATE_DESC, null);
        prop.setProperty(profileName + DOT + SCAP_SECURITY_FORCE_APPLY, null);
        prop.setProperty(profileName + DOT + SCAP_SECURITY_ENABLED, null);
        prop.setProperty(profileName + DOT + SCAP_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, null);
        prop.setProperty(profileName + DOT + SCAP_SECURITY_SCAP_SELECTEDCONTENT_ID, null);
        prop.setProperty(profileName + DOT + SCAP_SECURITY_SCAP_SELECTEDCONTENT_TITLE, null);
        prop.setProperty(profileName + DOT + SCAP_SECURITY_SCAP_SELECTEDPROFILE_ID, null);
        prop.setProperty(profileName + DOT + SCAP_SECURITY_SCAP_SELECTEDPROFILE_TITLE, null);
        String availableProfiles = prop.getProperty(SCAP_SECURITY_PROFILE_NAME);
        prop.setProperty(SCAP_SECURITY_PROFILE_NAME, removeProfileName(availableProfiles, profileName));
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
        config = new ConfigProps(new File(rootDir, SCAP_SECURITY_FILE_NAME));
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
