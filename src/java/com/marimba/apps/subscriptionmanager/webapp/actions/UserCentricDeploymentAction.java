// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions;
import static com.marimba.apps.subscriptionmanager.approval.IApprovalPolicyConstants.ADD_OPERATION;
import static com.marimba.apps.subscriptionmanager.approval.IApprovalPolicyConstants.MODIFY_OPERATION;
import static com.marimba.apps.subscriptionmanager.approval.IApprovalPolicyConstants.POLICY_PENDING;

import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiff;
import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiffMailFormatter;
import com.marimba.apps.subscriptionmanager.ucd.FileMappingDeviceIdentifier;
import com.marimba.apps.subscriptionmanager.ucd.UCDTemplateBean;
import com.marimba.apps.subscriptionmanager.webapp.forms.UserCentricDeploymentForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.webapps.intf.SystemException;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.tools.config.ConfigProps;
import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPName;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.objects.Subscription;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.upload.FormFile;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
/**
 * This class used for defining user centric deployment action 
 *
 * @author Selvaraj Jegatheesan
 * @version $Revision$,  $Date$
 */

public class UserCentricDeploymentAction extends AbstractAction implements ISubscriptionConstants {
	
	IUser iuser;
	File rootDir = null;
	private ConfigProps config;
	UserCentricDeploymentForm  userDeploymentForm;
	private HttpServletRequest req;
	private String KEY_OLD_SUB = "oldsub";
    private String KEY_NEW_SUB = "newsub";
    String modifiedUserDN = null;
    private IUser user;
	private Map<String, Map<String, ISubscription>> subsMap;
	public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		
		userDeploymentForm = (UserCentricDeploymentForm) form;
		req = request;
		String action = request.getParameter("action");
		boolean errorStatus = false;
		debug("Action : " + action);
		init(request);
		subsMap = new LinkedHashMap<String, Map<String, ISubscription>>(20);
		iuser = (IUser) request.getSession().getAttribute(IWebAppConstants.SESSION_SMUSER);
		rootDir = main.getDataDirectory();
		request.setAttribute("deviceIdentType", "templateType");
		try {
            this.user = GUIUtils.getUser(req);
            modifiedUserDN = main.resolveUserDN(user.getName());
        } catch (Exception e) {
            if (DEBUG) e.printStackTrace();
        }
		if(null == action) loadMaxDeviceLevel(); 
		if(null != action && action.equals("saveDeviceLevel")) { 
			Vector<String> errorList = new Vector<String>();
			userDeploymentForm.setErrorFlag("");
			userDeploymentForm.setErrorList(errorList);
			saveMaxDeviceLevel();
			request.setAttribute("deviceIdentType", "fileUploadType");
		}
		if(null != action && action.equalsIgnoreCase("fileUploadType")) {
			request.setAttribute("deviceIdentType", "fileUploadType");
			FormFile file = (FormFile)userDeploymentForm.getFile();
			Vector<String> errorList = new Vector<String>();
			userDeploymentForm.setErrorFlag("");
			userDeploymentForm.setErrorList(errorList);
			
			if(file.getFileSize() == 0 ) { 
				errorStatus = true;
				userDeploymentForm.setErrorFlag("true");
				errorList.addElement(resources.getMessage(request.getLocale(), "exception.ucd.mappingfile.required",""));
				userDeploymentForm.setErrorList(errorList);
			}
			if(!errorStatus && !"application/vnd.ms-excel".equals(file.getContentType())) {
				errorStatus = true;
				userDeploymentForm.setErrorFlag("true");
				errorList.addElement(resources.getMessage(request.getLocale(), "exception.ucd.mappingfile.invalid", file.getFileName()));
				userDeploymentForm.setErrorList(errorList);
			}
			if(!errorStatus) {
				FileMappingDeviceIdentifier deviceIdentifier = new FileMappingDeviceIdentifier(userDeploymentForm, iuser, main);
				deviceIdentifier.MappUserDeviceLevel();
				if((userDeploymentForm.getErrorList()).size() == 0) {
					userDeploymentForm.setErrorFlag("completed");
					errorList.addElement("Successfully all user policy updated with device level mapping informations");
					errorList.addElement("File Name : " + file.getFileName());
					userDeploymentForm.setErrorList(errorList);
				}
			}
		} 
		if(null != action && action.equalsIgnoreCase("removeTemplate")) {
			Vector<String> toRemoveTemplates = new Vector<String>();
            String paramName = "";
            Enumeration params = request.getParameterNames();
            while(params.hasMoreElements()) {
                paramName = (String)params.nextElement();
                if (("on".equals(request.getParameter(paramName))) && (paramName.indexOf("template_sel_") > -1)) {
                    toRemoveTemplates.addElement(paramName);
                }
            }
            removeTemplate(toRemoveTemplates);
		}
		generateTemplateList(request);
		return mapping.findForward("load");
	}
	private void saveMaxDeviceLevel() {
		Vector<String> errorList = new Vector<String>();
		try {
			ConfigProps config = main.getConfig();
			if(null != config.getProperty(MAX_UCD_DEVICELEVEL)) {
				config.setProperty(MAX_UCD_DEVICELEVEL, userDeploymentForm.getMaxDeviceLevel());
			} else {
				config.setProperty(MAX_UCD_DEVICELEVEL, DEFAULT_MAX_DEVICELEVEL);
			}
			config.save();
			
			userDeploymentForm.setErrorFlag("saveddevicelevel");
			errorList.addElement("Successfully modified maximum device level property");
			userDeploymentForm.setErrorList(errorList);
		} catch(Exception ec) {
			System.out.println("Failed to modified maximum device level property");
			userDeploymentForm.setErrorFlag("true");
			errorList.addElement("Failed to modify maximum device level property");
			userDeploymentForm.setErrorList(errorList);
			ec.printStackTrace();
		}
	}
	private void loadMaxDeviceLevel() {
		try {
			ConfigProps config = main.getConfig();
			if(null != config.getProperty(MAX_UCD_DEVICELEVEL)) {
				userDeploymentForm.setMaxDeviceLevel(config.getProperty(MAX_UCD_DEVICELEVEL));
			} else {
				userDeploymentForm.setMaxDeviceLevel(DEFAULT_MAX_DEVICELEVEL);
				config.setProperty(MAX_UCD_DEVICELEVEL, DEFAULT_MAX_DEVICELEVEL);
				config.save();
			}
		} catch(Exception ex) {
			System.out.println("Failed to load device level");
			ex.printStackTrace();
		}
	}
    private void generateTemplateList(HttpServletRequest request) {
        Vector<UCDTemplateBean> templatesVector = new Vector<UCDTemplateBean>();
        HttpSession session = request.getSession();
        try {
        	loadTemplateConfig();
            String template = config.getProperty(UCD_TEMPLATE_NAME);

            if (template != null) {
                StringTokenizer st = new StringTokenizer(template, ",", false);
                //iterate through tokens
                while(st.hasMoreTokens()) {
                    String templateName = st.nextToken(",");
                    String templateDesc = config.getProperty(templateName + UCD_DOT + UCD_TEMPLATE_DESCRIPTION);
                    templatesVector.addElement(new UCDTemplateBean(templateName, templateDesc));
                }
            }
        } catch (Exception ioe) {
            debug("Failed to generate template list from ucd_templates.txt file");
            if (DEBUG) {
                ioe.printStackTrace();
            }
            config = null;
        }
        session.setAttribute("templates", templatesVector);
    }
    /**
     * This method used to update affected target policy for UCD template
     * @throws Exception
     */
    void updateAffectedTargets () throws Exception {
    	loadTemplateConfig();
        String profileName = config.getProperty(UCD_TEMPLATE_NAME);
        String profiles[] = profileName.split(",");
        for (int i = 0; i < profiles.length; i++) {
            try {
                loadAffectedTargets(profiles[i]);
                updatePolicyForAffectedTargets(profiles[i]);
            } catch (Exception ex) {
                throw new Exception("Unable to update UCD template " + profiles[i] +" for targets. " + ex.getMessage());
            }
        }
    }
    /**
     * To find out affected target by UCD template name
     * @param profileName
     * @throws Exception
     */
    private void loadAffectedTargets(String profileName) throws Exception {
        Map<String, String> affectedTargets = new HashMap<String, String>();
        String searchStr = "(&("+LDAPVarsMap.get("CHANNELUCDTEMPLATE")+"=*"+profileName+"*)";
        searchStr += "(|("+LDAPVarsMap.get("TARGETDN")+"=*)("+LDAPVarsMap.get("TARGET_ALL")+"="+VALID+")("+LDAPVarsMap.get("TARGETTYPE")+"="+TYPE_SITE+")))";
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
            userDeploymentForm.setAffectedTargets(affectedTargets);
        } catch (Exception ex) {
            if (DEBUG) {
                ex.printStackTrace();
            }
        }
    }
    /**
     * Update affected targets subscription
     * @param profileName
     * @throws Exception
     */
    private void updatePolicyForAffectedTargets(String ucdTemplate) throws Exception {
        Map<String, String> affectedTargets = userDeploymentForm.getAffectedTargets();
        if (affectedTargets.size() > 0) {
            ISubscription newSub, oldSub;
            for (Map.Entry<String, String> targetsMap : affectedTargets.entrySet()){
                String priority = null;
                String profNameFromPolicy = ucdTemplate;
                String targetId = targetsMap.getKey();
                String targetType = targetsMap.getValue();

                newSub = ObjectManager.openSubForWrite(targetId, targetType, GUIUtils.getUser(req));
                oldSub = new Subscription((Subscription) newSub);
                
                Enumeration channels = newSub.getChannels();
                while(channels.hasMoreElements()) {
                    Channel channel = (Channel) channels.nextElement();
                    String ucdTemplateFromPolicy = channel.getUcdTemplates();
                    if(null != ucdTemplateFromPolicy) {
                    	if(ucdTemplateFromPolicy.equalsIgnoreCase(ucdTemplate) || ucdTemplateFromPolicy.startsWith(ucdTemplate + ",") || 
                    			ucdTemplateFromPolicy.endsWith("," + ucdTemplate) || (ucdTemplateFromPolicy.indexOf("," + ucdTemplate + ",") > -1)) {
                    			System.out.println("Removing selected UCD template : " + ucdTemplate + " from Channel : " 
                    					+ channel.getUrl() +" for the target :" + targetId);
                                int index = ucdTemplateFromPolicy.indexOf(ucdTemplate + ",");
                                int startIdx = 0;
                                int endIdx = ucdTemplateFromPolicy.length();

                                if (index != -1) {
                                    startIdx = index;
                                    endIdx = index+ucdTemplate.length()+1;
                                } else {
                                    index = ucdTemplateFromPolicy.indexOf("," + ucdTemplate);
                                    if (index != -1) {
                                        startIdx = index;
                                        endIdx = index+ucdTemplate.length()+1;
                                    } else {
                                    	ucdTemplateFromPolicy = null;
                                    }
                                }

                                if (ucdTemplateFromPolicy != null) {
                                	ucdTemplateFromPolicy = ucdTemplateFromPolicy.substring(0, startIdx) + 
                                		ucdTemplateFromPolicy.substring(endIdx, ucdTemplateFromPolicy.length());
                                }
                    	}
                    }
                    channel.setUcdTemplates(ucdTemplateFromPolicy);
                }
                
                if(!main.isPeerApprovalEnabled()) {
                	newSub.save();
                	System.out.println("Modified values was updated to the policy for the target: " + targetId);
                }
                addToSubsMap(KEY_OLD_SUB, targetId, oldSub);
                addToSubsMap(KEY_NEW_SUB, targetId, newSub);
            }
            triggerMail();
        } else {
            System.out.println("There is no targets contain selected UCD template '" + ucdTemplate + "'");
        }
    }
    private void removeTemplate(Vector<String> removeProfs) {
    	loadTemplateConfig();
        for (int j = 0; j < removeProfs.size(); j++) {
            try {
                String profile = config.getProperty(UCD_TEMPLATE_NAME);
                String removeProfileStr = removeProfs.elementAt(j);
                String removeProfile = removeProfileStr.substring(13);
                
                if(profile.equalsIgnoreCase(removeProfile) || profile.startsWith(removeProfile + ",") || profile.endsWith("," + removeProfile) || (profile.indexOf("," + removeProfile + ",") > -1)) {
                    clearTemplate(config, removeProfile);
                    try {
                        loadAffectedTargets(removeProfile);
                        updatePolicyForAffectedTargets(removeProfile);
                    } catch (Exception ex) {
                        throw new Exception("Unable to update UCD template " + removeProfile +" for targets. " + ex.getMessage());
                    }
                }
            } catch (Exception ioe) {
                System.out.println("Failed to remove selected template from list");
                if (DEBUG) {
                    ioe.printStackTrace();
                }
            }
        }
        config.save();
    }

    public void clearTemplate(ConfigProps prop, String templateName) {
        prop.setProperty(templateName + UCD_DOT + UCD_TEMPLATE_DESCRIPTION, null);
        prop.setProperty(templateName + UCD_DOT + UCD_DEVICELEVEL, null);
        prop.setProperty(templateName + UCD_DOT + UCD_LOGIN_HISTORY, null);
        prop.setProperty(templateName + UCD_DOT + UCD_WORKHRS, null);
        prop.setProperty(templateName + UCD_DOT + UCD_WORKDAYS, null);
        prop.setProperty(templateName + UCD_DOT + UCD_LOCKUNLOCK, null);
        prop.setProperty(templateName + UCD_DOT + UCD_REMOTELOGINMODE, null);
        prop.setProperty(templateName + UCD_DOT + UCD_TELNET, null);
        prop.setProperty(templateName + UCD_DOT + UCD_RLOGON, null);
        prop.setProperty(templateName + UCD_DOT + UCD_SSH, null);
        prop.setProperty(templateName + UCD_DOT + UCD_RDC, null);
        prop.setProperty(templateName + UCD_DOT + UCD_DEVICECONFIG, null);
        prop.setProperty(templateName + UCD_DOT + UCD_RAMSIZE, null);
        prop.setProperty(templateName + UCD_DOT + UCD_DISKSPACE, null);
        prop.setProperty(templateName + UCD_DOT + UCD_OSNAME, null);
        prop.setProperty(templateName + UCD_DOT + UCD_PROCESSOR, null);

        String availableProfiles = prop.getProperty(UCD_TEMPLATE_NAME);

        if (availableProfiles != null) {
            int index = availableProfiles.indexOf(templateName + ",");
            int startIdx = 0;
            int endIdx = availableProfiles.length();

            if (index != -1) {
                startIdx = index;
                endIdx = index+templateName.length()+1;
            } else {
                index = availableProfiles.indexOf("," + templateName);
                if (index != -1) {
                    startIdx = index;
                    endIdx = index+templateName.length()+1;
                } else {
                    availableProfiles = null;
                }
            }

            if (availableProfiles != null) {
                availableProfiles = availableProfiles.substring(0, startIdx) + availableProfiles.substring(endIdx, availableProfiles.length());
            }
        }
        prop.setProperty(UCD_TEMPLATE_NAME, availableProfiles);
    }

    
    private void loadTemplateConfig() {
        config = new ConfigProps(new File(rootDir, UCD_TEMPLATE_FILENAME));
    }
    
    private void debug(String msg) {
    	System.out.println("UserCentricDeploymentAction : " + msg);
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
    private void addToSubsMap(String subType, String targetId, ISubscription sub) {

        if (null != subsMap.get(targetId)) {
            subsMap.get(targetId).put(subType, sub);
        } else {
            Map<String, ISubscription> newTmpMap = new HashMap<String, ISubscription>(2);
            newTmpMap.put(subType, sub);
            subsMap.put(targetId, newTmpMap);
        }
    }
}
