// Copyright 2004, Marimba, Inc. All Rights Reserved.

// Confidential and Proprietary Information of Marimba, Inc.

// Protected by or for use under one or more of the following patents:

// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,

// and 6,430,608. Other Patents Pending.



package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import java.io.IOException;
import java.util.*;
import java.net.InetAddress;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.NamingException;

import com.marimba.apps.subscriptionmanager.webapp.forms.SetComplianceForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;
import com.marimba.intf.util.IProperty;


public class ComplianceConfigSaveAction

    extends AbstractAction {

    private static final String TASK_TYPE = "PolicyCompliance/CacheComplianceReport";
    private static final String TASK_GROUP = "ComplianceTaskGrp";
    private static final String TASK_NAME = "ComplianceTask";
    private static final String TASK_SCHEDULE = "task.schedule";

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

        try {

            super.init(request);

            SetComplianceForm compForm = (SetComplianceForm) form;
            String action = compForm.getAction();
            if("cancel".equals(action)) {
                //reset the form values
                compForm.setClearData(true);
            }else if("add".equals(action)) {
                //add a row to the target list
                addTargetRow(compForm, request);
                compForm.setAddblankrow("true");
                compForm.setClearData(false);
                ArrayList targetList = compForm.getTargetlist();
                if(targetList != null) {
                    int tlSize = targetList.size();
                    compForm.setTargetsize(Integer.toString(tlSize));
                    request.getSession().setAttribute("tarraysize", Integer.toString(tlSize));
                }

                String refreshSchedule = compForm.getSchedule("tn_tx_");
                compForm.setSchedule("tn_tx_", refreshSchedule);

                return (mapping.findForward("forward"));
            }else if("delete".equals(action)) {
                //delete a row from the target list
                deleteTargetRow(compForm, request);
                compForm.setAddblankrow("true");
                compForm.setClearData(false);
                ArrayList targetList = compForm.getTargetlist();
                if(targetList != null) {
                    int tlSize = targetList.size();
                    compForm.setTargetsize(Integer.toString(tlSize));
                    request.getSession().setAttribute("tarraysize", Integer.toString(tlSize));
                    if(tlSize > 0) compForm.setAddblankrow("false");
                }

                String refreshSchedule = compForm.getSchedule("tn_tx_");
                compForm.setSchedule("tn_tx_", refreshSchedule);

                return (mapping.findForward("forward"));
            }else{
                String calcCompEnabled = compForm.getCalcCompEnabled();


                String collectCompEnabled = compForm.getCollectCompEnabled();
	            IUser user = GUIUtils.getUser(request);
                saveCollectingComplianceData(user, collectCompEnabled);

                String hostName = InetAddress.getLocalHost().getHostName().trim();

                String cacheOpt = compForm.getCacheOption();
                String refreshSchedule = compForm.getSchedule("tn_tx_");

                if(DEBUG) System.out.println("Schedule: " + refreshSchedule);

                String oldHost = main.getSubscriptionConfig().getProperty(CALC_COMP_SPM_HOSTNAME);
                oldHost = (oldHost == null) ? "" : oldHost.trim();

                //set the report refresh schedule anc cache option and save in LDAP


                HashMap props = new HashMap();

                //Identify whether to set/reset the host name property
                if(CALC_COMP_SPM_ENABLE.equals(calcCompEnabled)) {
                    props.put(CALC_COMP_SPM_HOSTNAME, hostName);
                } else {
                    if(oldHost.equals(hostName)) {
                        props.put(CALC_COMP_SPM_HOSTNAME, "");
                    }
                }
                props.put(COMPLIANCE_REPORT_CACHE_SCHED, refreshSchedule);
                props.put(COMPLIANCE_GROUP2CACHE, cacheOpt);
                if(SPECIFIED_ONLY.equalsIgnoreCase(cacheOpt)) {
                    //Find the list and generate a target list
                    generateTargetList(compForm, props, request);
                    //remove the previous list
                    removeOldTargetList(main);
                }
                main.setSubscriptionConfigProperties(props);
                compForm.setAddblankrow("false");
                compForm.setClearData(true);

                //Set the new values to CMS task and the ComplianceEngine
               // percolateComplianceConfig(main.getSubscriptionConfig(), hostName, oldHost);
            }
        } catch (Exception e) {
            throw new GUIException(e);
        }

        return (mapping.findForward("success"));
    }

    public void removeOldTargetList(SubscriptionMain smmain) throws SystemException, NamingException {
        if(smmain == null) return;
        IProperty config = smmain.getSubscriptionConfig();
        HashMap oldvalues = getLDAPTargetList(config);
        if(oldvalues == null || oldvalues.size() == 0) return;
        //remove them from LDAP Config
        smmain.removeSubscriptionConfigProperties(oldvalues);
    }

    public void addTargetRow(SetComplianceForm compForm, HttpServletRequest request) {
        ArrayList oldList = compForm.getTargetlist();
        if(oldList == null) oldList= new ArrayList();
        String tName = compForm.getTargetname();
        if(tName != null) {
            if(compForm.getProperty(tName) != null &&
                    compForm.getProperty(tName).trim().length() > 0) {
                populateTargetList(compForm, request, oldList, null);
                compForm.setTargetlist(oldList);
            }
        }
    }

    public void populateTargetList(SetComplianceForm compForm, HttpServletRequest request, ArrayList list, String skip) {
        if(compForm==null || request == null || list == null) return;
        Map params = request.getParameterMap();
        TreeSet keys = new TreeSet(params.keySet());
        list.clear();
        Iterator it = keys.iterator();
        while(it.hasNext()) {
            String key = (String)it.next();
            if(key.startsWith("target_")) {
               String value = ((String[])params.get(key))[0];
               if(value != null && value.trim().length() > 0) {
                   if(!(skip != null && value.equals(skip))) {
                     list.add(value);
                   }
               }
            }
        }
    }

    public void deleteTargetRow(SetComplianceForm compForm, HttpServletRequest request) {
        if(compForm == null || request == null) return;
        String tName = compForm.getTargetname();
        if(tName != null) {
            if(compForm.getProperty(tName) != null) {
                String removeItem = request.getParameter(tName);
                ArrayList tList = compForm.getTargetlist();
                populateTargetList(compForm, request, tList, removeItem);
                compForm.setTargetlist(tList);
            }
        }
    }

    public void generateTargetList(SetComplianceForm compForm, HashMap props, HttpServletRequest request) {
        if(compForm == null || props == null) return;
        ArrayList tList = compForm.getTargetlist();
        if(tList == null) tList = new ArrayList();
        populateTargetList(compForm, request, tList, null);
        Iterator it = tList.iterator();
        int index = 0;
        while(it.hasNext()) {
            String aTarget = (String)it.next();
            if(aTarget != null && request.getAttribute(aTarget) != null)
                aTarget = (String) request.getAttribute(aTarget);
            String ldapPropKey = COMPLIANCE_LIST2CACHE + "." + index;
            props.put(ldapPropKey, aTarget);
            index++;
        }
    }

    public HashMap getLDAPTargetList(IProperty config) {
        String[] propList = config.getPropertyPairs();
        if(propList == null) return null;
        HashMap result = new HashMap();
        for(int i = 0; i<propList.length; i=i+2) {
            if(propList[i] != null && propList[i].startsWith(COMPLIANCE_LIST2CACHE)) {
                result.put(propList[i], propList[i+1]);
            }
        }
        return result;
    }

	void saveCollectingComplianceData(IUser  user,String collect) throws SystemException {
		String enabled = "false";
		ISubscription sub = ObjectManager.openSubForWrite(TYPE_ALL,TYPE_ALL, user);
		if ("enable".equalsIgnoreCase(collect)){
			enabled = "true";
		}
		sub.setProperty(PROP_TUNER_KEYWORD, COLLECT_INVENTORY_DATA, enabled);
		sub.save();
	}
}
