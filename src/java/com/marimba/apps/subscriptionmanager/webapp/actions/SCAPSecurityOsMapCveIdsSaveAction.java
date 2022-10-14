// Copyright 2021, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Subscription;
import com.marimba.apps.subscriptionmanager.webapp.forms.SCAPSecurityOsMapCveIdsForm;
import com.marimba.apps.subscriptionmanager.webapp.system.OsMapCveIdBean;
import com.marimba.apps.subscriptionmanager.webapp.system.OsBean;
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
 * SCAPSecurityOsMapCveIdsSaveAction
 *
 * @author Nandakumar Sankaralingam
 * @version $Revision$, $Date$
 */

public class SCAPSecurityOsMapCveIdsSaveAction extends AbstractAction {
	private File rootDir;
    private IUser user;
    String modifiedUserDN = null;
    private ConfigProps config;
    private HttpServletRequest req;
    private String KEY_OLD_SUB = "oldsub";
    private String KEY_NEW_SUB = "newsub";
    private static final String DOT = ".";
    private SCAPSecurityOsMapCveIdsForm profileForm;
    private Map<String, Map<String, ISubscription>> subsMap;
    private Map<String, String> osMap;
    HttpSession session;
    private String CVEID_OS_MAPPING_FILE = "cveids_os_mapping_settings.txt";
    Locale locale;
    private int osLength = 0;

    public ActionForward execute( ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

        req = request;
        profileForm = (SCAPSecurityOsMapCveIdsForm) form;
        String  action = request.getParameter("action");
        String lastAction = null;

        init(request);
        session = request.getSession();
        locale = request.getLocale();
        subsMap = new LinkedHashMap<String, Map<String, ISubscription>>(20);
        rootDir = main.getDataDirectory();
        try {
            this.user = GUIUtils.getUser(req);
            modifiedUserDN = main.resolveUserDN(user.getName());
        } catch (Exception e) {
             if (DEBUG)
             e.printStackTrace();
        }

        setOSsize();

        if ("save".equals(action) || "apply".equals(action)) {
            initializeConfig();
            Vector<String> errors = new Vector<String>();
            Vector<String> success = new Vector<String>();
            lastAction = null;
            String profileName = profileForm.getProfileName();
            String profileDesc = profileForm.getProfileName();
            String osName = profileForm.getOsName();
            String cveids = profileForm.getCveIds();
            
            if (isEmpty(profileName)) {
              String profileLabel = getString(locale, "page.scap_security_osmapcveids_profile.label.profileName");
              errors.addElement(getString(locale, "errors.required", profileLabel));
            }

            if ("apply".equals(action)) {
             osName = profileForm.getPreviousOsIndex();   
            }

            if (isEmpty(osName)) {
              String osLabel = getString(locale, "page.scap_security_osmapcveids_profile.label.OS");
              errors.addElement(getString(locale, "errors.required", osLabel));
            }
            if (isEmpty(cveids)) {
              String cveidsLabel = getString(locale, "page.scap_security_osmapcveids_profile.label.cveidslist");
              errors.addElement(getString(locale, "errors.required", cveidsLabel));
            }
            String osIndex = osName;
            if ("save".equals(action)) {
                String osValue = config.getProperty("profile.os" + osIndex + ".name");
                if (getOS(osIndex).equalsIgnoreCase(osValue)) {
                    errors.addElement(getString(locale, "page.added.mapping.exists.error", osValue));
                }
            }

            if ((errors != null) && (errors.size() > 0)) {
                request.setAttribute("errors", errors.elements());
                profileForm.setProfileName(profileName);
                profileForm.setProfileDesc(profileDesc);
                profileForm.setOsName(osName);
                profileForm.setPreviousOsIndex(osName);
                return mapping.findForward("view");
            }

             if ("apply".equals(action)) {
               String osPreviousIndex = profileForm.getPreviousOsIndex();
               if (!(osIndex.equals(osPreviousIndex))) {
                   config.setProperty("profile.os" +  osPreviousIndex + ".profile", null);
                   config.setProperty("profile.os" +  osPreviousIndex + ".profiledesc", null);
                   config.setProperty("profile.os" +  osPreviousIndex + ".name", null);
                   config.setProperty("profile.os" +  osPreviousIndex + ".cveids", null);
               }
             }

             config.setProperty("profile.os" +  osIndex + ".profile", profileName);
             config.setProperty("profile.os" +  osIndex + ".profiledesc", profileDesc);
             config.setProperty("profile.os" +  osIndex + ".name", getOS(osIndex));
             config.setProperty("profile.os" +  osIndex + ".cveids", cveids);

             config.save();
             if ("save".equals(action)) {
                success.add(getString(locale, "page.added.cveidosmapping.success"));
             } else {
                success.add(getString(locale, "page.updated.cveidosmapping.success"));
             }

            if ((success != null) && (success.size() > 0)) {
                request.setAttribute("success", success.elements());
            }
            loadOSCVEIDsMappingData(request);
            return mapping.findForward("save");
        } else if ("add".equals(action)) {
            profileForm.reset();
            profileForm.setCreate("true");
            profileForm.setOsList(getOSList());
            return mapping.findForward("add");
        } else if ("remove".equals(action)) {
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
            removeProfileMapping(toRemoveProfiles, result, false);

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
            loadOSCVEIDsMappingData(request);

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
            profileForm.setCreate("false");
            profileForm.setOsList(getOSList());
            Vector v = getSelectedProfile(request);
            String profileIndex = "0";
            if (v.size() == 1) {
                profileIndex = (String)v.elementAt(0);
            }

            initializeConfig();
            String profileName  = config.getProperty("profile.os" + Integer.parseInt(profileIndex) + ".profile");
            String profileDesc  = config.getProperty("profile.os" + Integer.parseInt(profileIndex) + ".profiledesc");
            String osName  = config.getProperty("profile.os" + Integer.parseInt(profileIndex) + ".name");
            String cveids  = config.getProperty("profile.os" + Integer.parseInt(profileIndex) + ".cveids");

            String osIndex = profileIndex;

            profileForm.setProfileName(profileName);
            profileForm.setProfileDesc(profileDesc);
            profileForm.setOsName(osIndex);
            profileForm.setCveIds(cveids);
            profileForm.setPreviousOsIndex(osIndex);

            return mapping.findForward("add");
        } else if ("cancel".equals(action)) {
            loadOSCVEIDsMappingData(request);
            return mapping.findForward("cancel");
        } else {
            loadOSCVEIDsMappingData(request);
            return mapping.findForward("load");
        }
    }

    private List<OsBean> getOSList() {
        List<String> osData = SCAPUtils.getSCAPUtils().getOsInfoDetails(main);
        List<OsBean> osList =  new ArrayList<OsBean>();
        osMap = new LinkedHashMap<String, String>();
        for(int i = 0; i < osData.size(); i++) {
          //  System.out.println("OS Info - " + osData.get(i));
            osList.add(new OsBean(String.valueOf(i+1), osData.get(i)));
            osMap.put(String.valueOf(i+1), osData.get(i));
        }
        return osList;
    }

    public String getOS(String osIndex) {
        return osMap.get(osIndex);
    }

    private void setOSsize() {
        osLength = SCAPUtils.getSCAPUtils().getOsInfoDetails(main).size();
        initializeConfig();
        config.setProperty("os.size", String.valueOf(osLength));
        config.save();
    }

    private void removeProfileMapping(Vector<String> removeProfs, HashMap<String, String> result, boolean ignoreFailures) {
       initializeConfig();
       for (String removeProfileStr : removeProfs) {
          String removeProfileIdx = removeProfileStr.startsWith("profile_sel_") ? removeProfileStr.substring(12) : removeProfileStr;
          String removeProfile = config.getProperty("profile.os" + Integer.parseInt(removeProfileIdx) + ".profile");
           config.setProperty("profile.os" + Integer.parseInt(removeProfileIdx) + ".profile", null);
           config.setProperty("profile.os" + Integer.parseInt(removeProfileIdx) + ".profiledesc" , null);
           config.setProperty("profile.os" + Integer.parseInt(removeProfileIdx) + ".name", null);
           config.setProperty("profile.os" + Integer.parseInt(removeProfileIdx) + ".cveids", null);
           result.put(removeProfile, "success");
       }
       config.save();
    }

    private Vector<String> getSelectedProfile(HttpServletRequest req) {
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

    private void loadOSCVEIDsMappingData(HttpServletRequest req) {
        Vector v = new Vector();
        try {
            initializeConfig();
            String mappingDataSize = config.getProperty("os.size");
            int size = (mappingDataSize != null) ? Integer.parseInt(mappingDataSize) : 0;
            for (int i = 1;  i <= size; i++) {
               String profileName = config.getProperty("profile.os" + i + ".profile");
               String profileDesc = config.getProperty("profile.os" + i + ".profiledesc");
               String osName = config.getProperty("profile.os" + i + ".name");
               String cveids = config.getProperty("profile.os" + i + ".cveids");
               if (!isEmpty(profileName)) {
                   v.addElement(new OsMapCveIdBean(profileName, profileDesc, String.valueOf(i), osName, cveids));
               }
            }

            if (!config.save()) {
                throw new Exception("Failed to save OS CVE-IDs mapping Configurations");
            }

        } catch (Exception ioe) {
            System.out.println("Failed to load OS CVE-IDs mapping configurations");
            if (DEBUG) {
                ioe.printStackTrace();
            }
            config = null;
        }
        req.setAttribute("profiles", v);
    }


    private void initializeConfig() {
        config = new ConfigProps(new File(rootDir, CVEID_OS_MAPPING_FILE));
    }

}

