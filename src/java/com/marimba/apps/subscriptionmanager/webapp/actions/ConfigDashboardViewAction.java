/**
 * Copyright 2022-2023, Harman International. All Rights Reserved.
 * Confidential and Proprietary Information of Harman International.
 * Author: Abhinav Satpute
 */

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.securitymgr.compliance.DashboardHandler;
import com.marimba.apps.securitymgr.utils.json.JSONObject;
import com.marimba.apps.subscriptionmanager.beans.*;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscriptionmanager.webapp.forms.ConfigDashboardViewForm;
import com.marimba.apps.subscriptionmanager.webapp.forms.NewDashboardViewForm;
import com.sun.jmx.snmp.tasks.Task;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ConfigDashboardViewAction extends AbstractAction implements IWebAppConstants, ISubscriptionConstants {

  protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {

    return new ConfigAssessDashboardTask(mapping, form, request, response);

  }

  protected class ConfigAssessDashboardTask extends SubscriptionDelayedTask {


    ConfigDashboardViewForm dashboardForm;

    public ConfigAssessDashboardTask(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response) {
      super(mapping, form, request, response);
      this.dashboardForm = (ConfigDashboardViewForm) form;
    }

    public void execute() {
      init(request);

      String action = request.getParameter("action");

      System.out.println("ConfigDashboardViewAction action = "+action);

      try {
        DashboardHandler dashboardHandler = new DashboardHandler(main);

        if (null == action || "view".equals(action)) {
          int totalMachineCount = dashboardHandler.getEnrolledMachines("all");
          int totalWindowsMachineCount = dashboardHandler.getEnrolledMachinesByOS("Windows");
          int totalLinuxMachineCount = dashboardHandler.getEnrolledMachinesByOS("Linux");
          int totalMacMachineCount = dashboardHandler.getEnrolledMachinesByOS("MAC");

          // Config Scanned Machines Count
          int vScanMachinesCount = dashboardHandler.getVScanMachinesCount("vscan");

          dashboardForm.setMachinesCount(String.valueOf(totalMachineCount));
          dashboardForm.setMachineWindowsCount(String.valueOf(totalWindowsMachineCount));
          dashboardForm.setMachineLinuxCount(String.valueOf(totalLinuxMachineCount));
          dashboardForm.setMachineMacCount(String.valueOf(totalMacMachineCount));
          dashboardForm.setConfigScanCount(String.valueOf(vScanMachinesCount));

          forward = mapping.findForward("view");
        }

      } catch (Exception ex) {
        ex.printStackTrace();
      }

      if(action == null) {
        forward = mapping.findForward("view");
      }
      else if(action.equals("testReact")) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("test","success");
        try {
          sendJSONResponse(response, String.valueOf(jsonObject));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }

    protected void sendJSONResponse(HttpServletResponse response, String jsonData) throws Exception {
      PrintWriter out = response.getWriter();
      out.println(jsonData);
      out.flush();
    }

  }
}
