/**
 * Copyright 2022-2023, Harman International. All Rights Reserved.
 * Confidential and Proprietary Information of Harman International.
 * Author: Abhinav Satpute
 */

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.securitymgr.compliance.DashboardHandler;
import com.marimba.apps.securitymgr.utils.json.JSONArray;
import com.marimba.apps.securitymgr.utils.json.JSONObject;
import com.marimba.apps.subscriptionmanager.beans.*;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscriptionmanager.webapp.forms.ConfigDashboardViewForm;
import com.marimba.apps.subscriptionmanager.webapp.forms.NewDashboardViewForm;
import com.sun.jmx.snmp.tasks.Task;
import java.io.PrintWriter;
import java.util.*;

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
          int configScanMachinesCount = dashboardHandler.getVScanMachinesCount("configscan");


          dashboardForm.setMachinesCount(String.valueOf(totalMachineCount));
          dashboardForm.setMachineWindowsCount(String.valueOf(totalWindowsMachineCount));
          dashboardForm.setMachineLinuxCount(String.valueOf(totalLinuxMachineCount));
          dashboardForm.setMachineMacCount(String.valueOf(totalMacMachineCount));
          dashboardForm.setConfigScanCount(String.valueOf(configScanMachinesCount));


          //to get the profile dropdown


          //to get Profile Compliant and Non Compliant
          Map<String,String> profileCompliantMap = new LinkedHashMap<String,String>();
          String strProfileId = "All";
          profileCompliantMap = dashboardHandler.getConfigComplianceByProfile(strProfileId);

          dashboardForm.setConfigProfileCompliant(profileCompliantMap.get("compliant"));
          dashboardForm.setConfigProfileNonCompliant(profileCompliantMap.get("nonCompliant"));

          //To Set Profile Dropdown
          Map<String,String> configProfileDropdown = dashboardHandler.getConfigProfileDropdown();
          dashboardForm.setConfigProfileDropdown(configProfileDropdown);


         JSONArray jsonArray =  dashboardHandler.getConfigDashboardBarChartDataInfo();

          //To Set Data for Bar Chart Data
          JSONObject barChartData = new JSONObject();


          JSONArray jsonProfileResultArray = new JSONArray();

          Set<Integer> setProfileId = new HashSet<Integer>();
         for(int i=0; i < jsonArray.length(); i++){
           JSONObject jsonObject =  jsonArray.getJSONObject(i);
           int profileId = jsonObject.getInt("profileId");
           setProfileId.add(profileId);
         }

         List<Integer> criticalSeverityList = new ArrayList<Integer>();
         List<Integer> highSeverityList = new ArrayList<Integer>();
         List<Integer> mediumSeverityList = new ArrayList<Integer>();
         List<Integer> lowSeverityList = new ArrayList<Integer>();

          JSONArray jsonProfileNameArray = new JSONArray(setProfileId);
          barChartData.put("labels",jsonProfileNameArray);

          for(int j=0; j < jsonProfileNameArray.length(); j++){

            criticalSeverityList.add(0);
            highSeverityList.add(0);
            mediumSeverityList.add(0);
            lowSeverityList.add(0);


            for(int i=0; i < jsonArray.length(); i++) {
              JSONObject jsonObject =  jsonArray.getJSONObject(i);
              int dbProfileId = jsonObject.getInt("profileId");
              String profileName = jsonObject.getString("profileName");
              String profileTitle = jsonObject.getString("profileTitle");
              int failedRuleCount = jsonObject.getInt("failedRuleCount");
              String ruleSeverity = jsonObject.getString("ruleSeverity");


              if( jsonProfileNameArray.getInt(j)== dbProfileId){

                switch (ruleSeverity){

                  case "critical":
                    criticalSeverityList.add(j,failedRuleCount);
                    break;

                  case "high":
                    highSeverityList.add(j,failedRuleCount);
                    break;

                  case "medium":
                    mediumSeverityList.add(j,failedRuleCount);
                    break;

                  case "low":
                    lowSeverityList.add(j,failedRuleCount);
                    break;
                }
              }
            }
          }

          //Preparing 4 object using severity

          JSONObject criticalJsonObject = new JSONObject();
          criticalJsonObject.put("label","Critical");
          criticalJsonObject.put("backgroundColor","#FF5F60");
          criticalJsonObject.put("data",criticalSeverityList);
          criticalJsonObject.put("stack","Stack 0");

          JSONObject highJsonObject = new JSONObject();
          highJsonObject.put("label","High");
          highJsonObject.put("backgroundColor","#D4733A");
          highJsonObject.put("data",highSeverityList);
          highJsonObject.put("stack","Stack 0");

          JSONObject mediumJsonObject = new JSONObject();
          mediumJsonObject.put("label","Medium");
          mediumJsonObject.put("backgroundColor","#F3CC63");
          mediumJsonObject.put("data",mediumSeverityList);
          mediumJsonObject.put("stack","Stack 0");

          JSONObject lowJsonObject = new JSONObject();
          lowJsonObject.put("label","Low");
          lowJsonObject.put("backgroundColor","#71DCEB");
          lowJsonObject.put("data",lowSeverityList);
          lowJsonObject.put("stack","Stack 0");


          JSONArray jsonDataSetArray = new JSONArray();
          jsonDataSetArray.put(criticalJsonObject);
          jsonDataSetArray.put(highJsonObject);
          jsonDataSetArray.put(mediumJsonObject);
          jsonDataSetArray.put(lowJsonObject);

          barChartData.put("datasets",jsonDataSetArray);

        System.out.println("Bar Chart Data ::"+barChartData);

          dashboardForm.setBarChartData(barChartData.toString());


          //Data fetch for Line Chart
          JSONObject lineChartData = new JSONObject();

          JSONArray jsonLabelArray = new JSONArray();
          jsonLabelArray.put("July 2023");
          jsonLabelArray.put("Aug 2023");
          jsonLabelArray.put("Sept 2023");
          jsonLabelArray.put("Oct 2023");
          jsonLabelArray.put("Nov 2023");

          lineChartData.put("labels",jsonLabelArray);


          JSONArray jDataSetArray = new JSONArray();

          JSONObject jObject1 = new JSONObject();
          jObject1.put("label","Profile1");
          jObject1.put("data","[10, 15, 13, 20, 18]");
          jObject1.put("borderColor","red");

          JSONObject jObject2 = new JSONObject();
          jObject1.put("label","Profile2");
          jObject1.put("data","[5, 7, 6, 10, 9]");
          jObject1.put("borderColor","blue");

          JSONObject jObject3 = new JSONObject();
          jObject1.put("label","Profile3");
          jObject1.put("data","[4, 30, 19, 25, 24]");
          jObject1.put("borderColor","yellow");

          jDataSetArray.put(jObject1);
          jDataSetArray.put(jObject2);
          jDataSetArray.put(jObject3);
          lineChartData.put("datasets",jDataSetArray);

          System.out.println("Line Chart Data ::"+lineChartData);

          dashboardForm.setLineChartData(lineChartData.toString());

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
