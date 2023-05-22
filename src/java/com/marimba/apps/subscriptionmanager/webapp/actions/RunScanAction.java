// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscriptionmanager.compliance.intf.ICveUpdateConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.RunScanForm;
import com.sun.jmx.snmp.tasks.Task;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import java.util.*;

import com.marimba.webapps.intf.IMapProperty;
import com.marimba.apps.subscriptionmanager.webapp.util.RunScanHandler;

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

                System.out.println("RUNSCAN: action: " + action);
                System.out.println("RUNSCAN: action string: " + actionString);

                if (isNull(action)) {
                    RunScanHandler dashboardHandler = new RunScanHandler(main);
                    runScanForm.setRunScanJson(dashboardHandler.getRunScanData());
                    forward = mapping.findForward("success");
                }

                if ("runscan_machines".equals(action)) {
                    RunScanHandler runScanHandler = new RunScanHandler(main);
                    runScanForm.setRunScanJson(runScanHandler.getRunScanData());
                    if (null != runScanForm && null != runScanForm.getEndDevicesArr() && !runScanForm.getEndDevicesArr().isEmpty()) {
                        String[] ipPortArr = runScanForm.getEndDevicesArr().split(",");
                        List<String> endDeviceList = new ArrayList<String>();
                        Collections.addAll(endDeviceList, ipPortArr);
                        System.out.println("Selected machines for run security scan: " + endDeviceList);
                        if (endDeviceList.size() > 0) {
                          tenant.setManager("security.scan.machineslist", endDeviceList);
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


    }
}

