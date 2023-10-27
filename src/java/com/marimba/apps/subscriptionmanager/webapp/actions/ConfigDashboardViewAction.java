/**
 * Copyright 2022-2023, Harman International. All Rights Reserved.
 * Confidential and Proprietary Information of Harman International.
 * Author: Abhinav Satpute
 */

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.securitymgr.utils.json.JSONObject;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.sun.jmx.snmp.tasks.Task;
import java.io.PrintWriter;
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

    public ConfigAssessDashboardTask(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response) {
      super(mapping, form, request, response);
    }

    public void execute() {
      init(request);

      String action = request.getParameter("action");

      System.out.println("ConfigDashboardViewAction action = "+action);

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
