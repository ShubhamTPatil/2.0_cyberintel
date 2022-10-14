// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.ajax.ucd;

import com.bmc.web.ajax.AjaxArg;
import com.bmc.web.ajax.AjaxCall;
import com.bmc.web.ajax.AjaxException;
import static com.marimba.apps.subscription.common.ISubscriptionConstants.UCD_TEMPLATE_FILENAME;
import static com.marimba.apps.subscription.common.ISubscriptionConstants.UCD_TEMPLATE_NAME;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscriptionmanager.webapp.ajax.BasicAjaxFunction;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.intf.util.IConfig;
import com.marimba.tools.config.ConfigPrefix;
import com.marimba.tools.config.ConfigProps;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

/**
 * UCDTemplatesAction, loads all templates from ucd_templates.txt for UI and save selected templates to session.
 *
 * @author   Tamilselvan Teivasekamani
 * @version  $Revision$,  $Date$
 *
 */

public class UCDTemplatesAction extends BasicAjaxFunction {

    private String NAME = "loaducdtemplates";
    private Map<String, String> tmplts;
    private ConfigProps templatesConfig;
    private ConfigProps config;
    private Locale locale;


    private AjaxArg[] ARGUMENTS = {
            new AjaxArg("action", String.class, true),
            new AjaxArg("tmpltname", String.class, false),
            new AjaxArg("selectedUrls", String.class, false),
            new AjaxArg("selectedchnls", String.class, false),
            new AjaxArg("selectedtmpltsname", String.class, false)
    };

    protected JSONObject doInvoke(AjaxCall call) throws AjaxException, JSONException {
        this.jsonObj = new JSONObject();
        this.session = call.getSession();
        this.locale = call.getLocale();
        String action = call.getString("action");

        File rootDir = main.getDataDirectory();
        this.templatesConfig = new ConfigProps(new File(rootDir, UCD_TEMPLATE_FILENAME));
        this.config = new ConfigProps(new File(rootDir, "properties.txt"));
        debug("action : " + action);

        if ("gettmpltnames".equalsIgnoreCase(action)) {
            List<String> selectedChnls = new ArrayList<String>();
            String selectedchnls = (String)call.getArg("selectedchnls");
            Set<Set<String>> completeTmpltsList = new HashSet<Set<String>>();
            Set<String> resultsSet = new HashSet<String>(5);
            if (null != selectedchnls) {
                String[] tmpArr = selectedchnls.split(",");
                selectedChnls.addAll(Arrays.asList(tmpArr));
                resultsSet = getExistingTemplates(selectedChnls.remove(0));
                for (String chnlUrl : selectedChnls) {
                    completeTmpltsList.add(getExistingTemplates(chnlUrl));
                }
                for (Set<String> tmpSet : completeTmpltsList) {
                    resultsSet.retainAll(tmpSet);
                }
            }
            this.tmplts = new TreeMap<String, String>();
            String sDeviceLevel = "";
            boolean firstTime = false;
            debug("Common templates list : " + resultsSet);
            if(resultsSet.isEmpty()) {
            	firstTime = true;
            } else {
            	firstTime = false;
            }
            sDeviceLevel = resultsSet.toString();
            if(sDeviceLevel.contains("StaticLevel_")) {
            	for(String staticLevel : resultsSet) {
            		sDeviceLevel = staticLevel;
            	}
            	resultsSet = new HashSet<String>();
            } else {
            	sDeviceLevel = "";
            }
            loadTemplatesName(resultsSet);
            int maxDeviceLevel = 2;
            if(null != config.getProperty(MAX_UCD_DEVICELEVEL)) {
            	try {
            		maxDeviceLevel = Integer.parseInt(config.getProperty(MAX_UCD_DEVICELEVEL));
            	} catch(Exception ex) {
            		//
            	}
            	
            }
            System.out.println("max device level:" + maxDeviceLevel);
            this.jsonObj.put("static_devicelevel", sDeviceLevel);
            this.jsonObj.put("maxLevel", maxDeviceLevel);
            this.jsonObj.put("firstTimeLoad", firstTime);
            this.jsonObj.put("all_templates", tmplts);
        }
        if ("gettmpltdetails".equalsIgnoreCase(action)) {
            String tmpltName = call.getString("tmpltname");
            Map<String, String> propsMap = loadTemplateProperties(tmpltName);
            propsMap.put(UCD_TEMPLATE_NAME, tmpltName);
            String ucdDetails = new UCDTemplateDetailsFormatter(this, propsMap).getFormatedDetails();
            System.out.println("Formatted UCD Details : " + ucdDetails);
            this.jsonObj.put("tmpltdetails", ucdDetails);
        }
        if ("savetemplates".equalsIgnoreCase(action)) {
            if (null == this.tmplts) loadTemplatesName(new HashSet<String>(0));
            String tmpltNames = call.getString("selectedtmpltsname");
            String[] tmpArr = null;

            if(null != tmpltNames && tmpltNames.startsWith("StaticLevel_")) {
            	tmpArr = tmpltNames.split("-"); 
            } else {
            	tmpArr = tmpltNames.split(",");
            }
            debug("Selected template names : " + tmpltNames);
            Set<String> selectedTmpltNames = new HashSet<String>(Arrays.asList(tmpArr));

            // just upload the values to session, this will captured by DistAsgInitAction
            this.session.setAttribute("selectedUCDTmplts", selectedTmpltNames);
            if (null != templatesConfig) {
                templatesConfig.close();
                this.templatesConfig = null;
            }
        }
        if(null != config) {
        	config.close();
        	this.config = null;
        }

        return this.jsonObj;
    }

    private void loadTemplatesName(Set<String> resultsSet) throws JSONException {
        String templateNames = templatesConfig.getProperty(UCD_TEMPLATE_NAME);
        if (null != templateNames) {
            for (String aTmpltName : templateNames.split(",")) {
                tmplts.put(aTmpltName, Boolean.toString(resultsSet.contains(aTmpltName)));
            }
        }
    }

    private Map<String, String> loadTemplateProperties(String tmpltName) throws JSONException {
        IConfig tmpltCfg = new ConfigPrefix(tmpltName + ".", templatesConfig);
        String[] pairs = tmpltCfg.getPropertyPairs();
        Map<String, String> detailsMap = new HashMap<String, String>(20);

        for (int i = 0; i < pairs.length; i += 2) {
            detailsMap.put(pairs[i], pairs[i + 1]);
        }
        return detailsMap;
    }

    private Set<String> getExistingTemplates(String chnlUrl) {
        Set<String> existTmplts = new HashSet<String>(5);
        Channel chnl = getChannel(chnlUrl);
        if (null != chnl && null != chnl.getUcdTemplates()) {
        	String[] tmpArr = null;
        	if(chnl.getUcdTemplates().startsWith("StaticLevel_")) {
        		tmpArr = chnl.getUcdTemplates().split("-");
        	} else {
        		tmpArr = chnl.getUcdTemplates().split(",");
        	}
            existTmplts.addAll(Arrays.asList(tmpArr));
        }
        return existTmplts;
    }

    private Channel getChannel(String chnlUrl) {
        Channel chnl = null;
        DistributionBean distBean = (DistributionBean) session.getAttribute(SESSION_DIST);
        if (null != distBean) {
            chnl = distBean.getChannel(chnlUrl);
        }
        return chnl;
    }

    public String getName() {
        return NAME;
    }

    public AjaxArg[] getArguments() {
        return ARGUMENTS;
    }

    protected String getMessage(String key) {
        return super.getMessage(this.locale, key);
    }

    protected void debug(String message) {
        super.debug("[UCDTemplatesAction] " + message);
    }
}
