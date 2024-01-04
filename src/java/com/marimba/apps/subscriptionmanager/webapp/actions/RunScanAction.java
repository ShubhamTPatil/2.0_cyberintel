// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscriptionmanager.compliance.intf.ICveUpdateConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.RunScanForm;
import com.marimba.intf.util.IConfig;
import com.marimba.tools.config.ConfigProps;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import java.rmi.server.RemoteRef;
import java.util.*;

import com.marimba.webapps.intf.IMapProperty;
import com.marimba.apps.subscriptionmanager.webapp.util.RunScanHandler;
import com.google.gson.*;
import com.marimba.apps.subscriptionmanager.webapp.util.*;

/**
 * RunScanAction w.r.t handle run security scan via infra admin module
 *
 * @author Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 */

public class RunScanAction extends AbstractAction
        implements IWebAppConstants, ISubscriptionConstants, ICveUpdateConstants {

    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                              HttpServletResponse response) {

        return new RunScanAction.RunScanTask(mapping, form, request, response);

    }

    protected class RunScanTask extends SubscriptionDelayedTask {
        RunScanForm runScanForm;

        HttpServletRequest request;
        HttpServletResponse response;
        Locale locale;
        ActionMapping mapping;
        String action;
        String actionString;
        IConfig tunerConfig;

        RunScanTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                    HttpServletResponse response) {
            super(mapping, form, request, response);
            this.runScanForm = (RunScanForm) form;
            this.request = request;
            this.mapping = mapping;
            this.response = response;
            this.locale = request.getLocale();
        }

        public void execute() {
            actionString = request.getParameter("action");
            String responseMsg = null;
            RunScanForm runScanForm = (RunScanForm) form;
            session = request.getSession(true);

            try {
                init(request);
                action = runScanForm.getAction();

                // Initialize
                tunerConfig = (IConfig) main.getFeatures().getChild("tunerConfig");
                ConfigProps config = main.getConfig();
                RemoteAdminHandler remoteAdminHandler =  new RemoteAdminHandler();
                remoteAdminHandler.init(config, tunerConfig);


                String actionString = (request.getQueryString() != null) ? request.getParameter("actionString") : null;

                if(actionString != null && actionString.equalsIgnoreCase("scanResponse")){
                    action = actionString;
                    Map<String,String> machineScanResultMap = (Map<String,String>) tenant.getManager("security.scan.machineScanResultMap");
                    System.out.println("In Scan Reposne section machineScanResultMap is ::"+machineScanResultMap);
                    if(machineScanResultMap!=null){
                        RunScanHandler dashboardHandler = new RunScanHandler(main);
                        Gson g = new Gson();
                        com.marimba.apps.subscriptionmanager.webapp.util.ScanResultResponse scanResultResponse = g.fromJson(dashboardHandler.getRunScanData(), ScanResultResponse.class);

                        if(scanResultResponse != null){
                            com.marimba.apps.subscriptionmanager.webapp.util.Data data = scanResultResponse.getData();
                            Set<com.marimba.apps.subscriptionmanager.webapp.util.Machine> machineSet = data.getMachineList();

                            for(Machine machine : machineSet){
                                if(machineScanResultMap.containsKey(machine.getMachineName())){
                                    machine.setScanStatus(machineScanResultMap.get(machine.getMachineName()));
                                }
                            }

                            Gson gson = new Gson();
                            String updatedRunScanJsonResponse = gson.toJson(scanResultResponse);
                            runScanForm.setRunScanJson(updatedRunScanJsonResponse);

                            runScanForm.setMachineScanResultMap(machineScanResultMap);

                            Map<String,String> selectedMachineMap = (Map<String,String>) tenant.getManager("security.scan.selectedMachineMap");
                            System.out.println("In Scan Response action selected machineMap is ::"+selectedMachineMap);
                            List<String> selectedList = new ArrayList<String>();
                            if(selectedMachineMap !=null && selectedMachineMap.size() > 0 ){
                                for (Map.Entry<String,String> entry : selectedMachineMap.entrySet()) {
                                    selectedList.add(entry.getKey());
                                }
                                runScanForm.setResponseMsg(selectedList.toString());
                            }
                            Map<String,String> map = new LinkedHashMap<String,String>();
                            tenant.setManager("security.scan.selectedMachineMap", map);

                        }
                        forward = mapping.findForward("success");
                        return;
                    }
                }

                if (isNull(action)) {
                    RunScanHandler dashboardHandler = new RunScanHandler(main);

                    Gson g = new Gson();
                    com.marimba.apps.subscriptionmanager.webapp.util.ScanResultResponse scanResultResponse = g.fromJson(dashboardHandler.getRunScanData(), ScanResultResponse.class);

                    if(scanResultResponse != null) {
                        com.marimba.apps.subscriptionmanager.webapp.util.Data data = scanResultResponse.getData();
                        Set<com.marimba.apps.subscriptionmanager.webapp.util.Machine> machineSet = data.getMachineList();

                        List<String> machineNameList = new ArrayList<String>();
                        for(Machine machine : machineSet){
                            machineNameList.add(machine.getMachineName());
                        }
                        tenant.setManager("security.scan.machineslist", machineNameList);

                        Gson gson = new Gson();
                        String updatedRunScanJsonResponse = gson.toJson(scanResultResponse);
                        runScanForm.setRunScanJson(updatedRunScanJsonResponse);
                    }

                    //forward = mapping.findForward("success");
                    forward = mapping.findForward("scanStatus");
                    return;
                }


                if("getAllScanStatus".equals(action)){
                    RunScanHandler runScanHandler = new RunScanHandler(main);
                    runScanForm.setRunScanJson(runScanHandler.getRunScanData());
                    if (null != runScanForm && null != runScanForm.getEndDevicesArr() && !runScanForm.getEndDevicesArr().isEmpty()) {
                        String[] ipPortArr = runScanForm.getEndDevicesArr().split(",");
                        List<String> endDeviceList = new ArrayList<String>();
                        Collections.addAll(endDeviceList, ipPortArr);
                        String remoteAdminUser = "admin";
                        String remoteAdminPwd = ""; 
                        // Get current status of security scan on selected end-point machines.
                        remoteAdminHandler.fetchCurrentSecurityScanStatus(endDeviceList,
                                remoteAdminUser, remoteAdminPwd);
                        for(int idx = 0; idx < endDeviceList.size(); idx++) {
                           String chUrl = "http://" + endDeviceList.get(idx).trim();
                            String chStatus = remoteAdminHandler.getChannelStatusInfo(chUrl);
                            System.out.println("LogInfo: Channel Url: " + chUrl + ", Status: " + chStatus);
                        }
                        
                        if (endDeviceList.size() > 0) {
                            tenant.setManager("security.scan.machineslist", endDeviceList);
                            /// MIM Code base
                            forward = mapping.findForward("getScanStatus");
                            return;
                        } else {
                            responseMsg = "No end point machines for security scan";
                        }
                    } else {
                        System.out.println("Not selected any machines for run security scan");
                    }
                }

                if ("runscan_machines".equals(action)) {
                    RunScanHandler runScanHandler = new RunScanHandler(main);
                    runScanForm.setRunScanJson(runScanHandler.getRunScanData());
                    if (null != runScanForm && null != runScanForm.getEndDevicesArr() && !runScanForm.getEndDevicesArr().isEmpty()) {
                        String[] ipPortArr = runScanForm.getEndDevicesArr().split(",");
                        List<String> machineList = new ArrayList<String>();
                        Collections.addAll(machineList, ipPortArr);
                        String cmsUser = "admin";
                        String cmsPwd = "";
                        try {
                            int total_targets = machineList.size();
                            for(int i = 0; i < total_targets; i++) {
                                String hostname = "http://" + machineList.get(i).trim();
                                remoteAdminHandler.triggerRunScan(hostname, cmsUser, cmsPwd);
                                Thread.sleep(2000);
                            }
                            
                        } catch(Exception ex) {
                            ex.printStackTrace();
                        }
                        
                        System.out.println("Selected machines for run security scan: " + machineList);
                        if (machineList.size() > 0) {
                            tenant.setManager("security.scan.selectedMachineslist", machineList);
                            
                            forward = mapping.findForward("runscan");
                            return;
                        } else {
                            responseMsg = "No end point machines for security scan";
                        }
                    } else {
                        System.out.println("Not selected any machines for run security scan");
                    }

                }



            } catch (Exception ex) {
                ex.printStackTrace();
            }
            forward = mapping.findForward("success");
        }


        public String getWaitMessage() {
            locale = (locale == null) ? Locale.getDefault() : locale;
            if (isNull(action)) {
                return resources.getMessage(locale,
                        "page.run_scan.machines_list_operation.waitForCompletion.PleaseWait");
            } else if ("runscan_machines".equals(action)) {
                return resources.getMessage(locale,
                        "page.run_scan.runscan_operation.waitForCompletion.PleaseWait");
            }
            return resources.getMessage(locale, "page.waitForCompletion.PleaseWait");
        }


       public IConfig getTunerConfig() {
        return tunerConfig;
       }

    }
}

