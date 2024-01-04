/**
 * Copyright 2022-2023, Harman International. All Rights Reserved. Confidential and Proprietary
 * Information of Harman International. Author: Abhinav Satpute
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

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.util.*;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ConfigDashboardViewAction extends AbstractAction implements IWebAppConstants,
    ISubscriptionConstants {

  protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) {

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

      System.out.println("ConfigDashboardViewAction action = " + action);

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
          Map<String, String> profileCompliantMap = new LinkedHashMap<String, String>();
          String strProfileId = "All";
          profileCompliantMap = dashboardHandler.getConfigComplianceByProfile(strProfileId);

          dashboardForm.setConfigProfileCompliant(profileCompliantMap.get("compliant"));
          dashboardForm.setConfigProfileNonCompliant(profileCompliantMap.get("nonCompliant"));

          dashboardForm.setConfigProfileUnknown(profileCompliantMap.get("unknown"));
          dashboardForm.setConfigProfileNotApplicable(profileCompliantMap.get("notApplicable"));

          //To Set Profile Dropdown
          Map<String, String> configProfileDropdown = dashboardHandler.getConfigProfileDropdown();
          dashboardForm.setConfigProfileDropdown(configProfileDropdown);

          JSONArray jsonArray = dashboardHandler.getConfigDashboardBarChartDataByContentId();

          //To Set Data for Bar Chart Data
          JSONObject barChartData = new JSONObject();

          Set<Integer> setOfContentId = new HashSet<Integer>();
          Set<String> setOfContentTitles = new HashSet<>();
          for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            int contentId = jsonObject.getInt("contentId");
            setOfContentId.add(contentId);
            String contentTitle = jsonObject.getString("contentTitle");

            // Removed everything after word STIG
            contentTitle = contentTitle.replaceAll("STIG"+".*","STIG");

            // Replaced word "Security Technical Implementation Guide" to word STIG and removed everything after it
            contentTitle = contentTitle.replaceAll("Security Technical Implementation Guide"+".*","STIG");

            // Replaced "Red Hat Enterprise Linux" with "RHEL"
            contentTitle = contentTitle.replaceAll("Red Hat Enterprise Linux","RHEL");

            // Removed "Guide to the "
            contentTitle = contentTitle.replaceAll("Guide to the ","");

            setOfContentTitles.add(contentTitle);
          }

          List<Integer> compliantList = new ArrayList<Integer>();
          List<Integer> nonCompliantList = new ArrayList<Integer>();
          List<Integer> unknownList = new ArrayList<Integer>();
          List<Integer> notApplicableList = new ArrayList<Integer>();

          JSONArray jsonContentTitleArray = new JSONArray(setOfContentId);
          barChartData.put("contentIds", jsonContentTitleArray);

          JSONArray jsonContentNameArray = new JSONArray(setOfContentTitles);
          barChartData.put("labels", jsonContentNameArray);

          for (int j = 0; j < jsonContentTitleArray.length(); j++) {

            compliantList.add(0);
            nonCompliantList.add(0);
            unknownList.add(0);
            notApplicableList.add(0);

            for (int i = 0; i < jsonArray.length(); i++) {
              JSONObject jsonObject = jsonArray.getJSONObject(i);
              int dbContentId = jsonObject.getInt("contentId");
              String contentName = jsonObject.getString("contentName");
              String contentTitle = jsonObject.getString("contentTitle");
              int compliantCount = jsonObject.getInt("compliantCount");
              String overallCompliantLevel = jsonObject.getString("overallCompliantLevel");

              if (jsonContentTitleArray.getInt(j) == dbContentId) {

                switch (overallCompliantLevel) {

                  case "COMPLIANT":
                    compliantList.add(j, compliantCount);
                    break;

                  case "NON-COMPLIANT":
                    nonCompliantList.add(j, compliantCount);
                    break;

                  case "NOT APPLICABLE":
                    notApplicableList.add(j, compliantCount);
                    break;

                  case "UNKNOWN":
                    unknownList.add(j, compliantCount);
                    break;
                }
              }
            }
          }

          //Preparing 4 object using severity

          JSONObject compliantJsonObject = new JSONObject();
          compliantJsonObject.put("label", "Compliant");
          compliantJsonObject.put("backgroundColor", "#71DCEB");
          compliantJsonObject.put("data", compliantList);
          compliantJsonObject.put("stack", "Stack 0");

          JSONObject nonCompliantJsonObject = new JSONObject();
          nonCompliantJsonObject.put("label", "Non-Compliant");
          nonCompliantJsonObject.put("backgroundColor", "#FF5F60");
          nonCompliantJsonObject.put("data", nonCompliantList);
          nonCompliantJsonObject.put("stack", "Stack 0");

          JSONObject notApplicableJsonObject = new JSONObject();
          notApplicableJsonObject.put("label", "Not Applicable");
          notApplicableJsonObject.put("backgroundColor", "#F3CC63");
          notApplicableJsonObject.put("data", notApplicableList);
          notApplicableJsonObject.put("stack", "Stack 0");

          JSONObject unknownJsonObject = new JSONObject();
          unknownJsonObject.put("label", "Unknown");
          unknownJsonObject.put("backgroundColor", "#D4733A");
          unknownJsonObject.put("data", unknownList);
          unknownJsonObject.put("stack", "Stack 0");

          JSONArray jsonDataSetArray = new JSONArray();
          jsonDataSetArray.put(compliantJsonObject);
          jsonDataSetArray.put(nonCompliantJsonObject);
          jsonDataSetArray.put(notApplicableJsonObject);
          jsonDataSetArray.put(unknownJsonObject);

          barChartData.put("datasets", jsonDataSetArray);

          System.out.println("Bar Chart Data Content Id ::" + barChartData);

          dashboardForm.setBarChartData(barChartData.toString());

          //Data fetch for Line Chart
          JSONObject lineChartData = new JSONObject();

          JSONArray jsonLabelArray = new JSONArray();
          jsonLabelArray.put("July 2023");
          jsonLabelArray.put("Aug 2023");
          jsonLabelArray.put("Sept 2023");
          jsonLabelArray.put("Oct 2023");
          jsonLabelArray.put("Nov 2023");

          lineChartData.put("labels", jsonLabelArray);

          JSONArray jDataSetArray = new JSONArray();

          JSONObject jObject1 = new JSONObject();
          jObject1.put("label", "Profile1");
          jObject1.put("data", "[10, 15, 13, 20, 18]");
          jObject1.put("borderColor", "red");

          JSONObject jObject2 = new JSONObject();
          jObject2.put("label", "Profile2");
          jObject2.put("data", "[5, 7, 6, 10, 9]");
          jObject2.put("borderColor", "blue");

          JSONObject jObject3 = new JSONObject();
          jObject3.put("label", "Profile3");
          jObject3.put("data", "[4, 30, 19, 25, 24]");
          jObject3.put("borderColor", "yellow");

          jDataSetArray.put(jObject1);
          jDataSetArray.put(jObject2);
          jDataSetArray.put(jObject3);
          lineChartData.put("datasets", jDataSetArray);

          System.out.println("Line Chart Data ::" + lineChartData);

          dashboardForm.setLineChartData(lineChartData.toString());

          forward = mapping.findForward("view");
        } else if (action.equalsIgnoreCase("getMachineByContent")) {

          String contentId = request.getParameter("contentId");
          String complianceType = request.getParameter("complianceType").toUpperCase();

          System.out.println("Content Id : "+contentId+" and Compliance Type ::"+complianceType);

          JSONArray jsonArrayData = dashboardHandler.getBarChartMachineDataByContentId(contentId,
              complianceType);

          //System.out.println("Bar Chart Data Machine Data by Content Id ::" + jsonArrayData);

          JSONArray uiJSONArray = new JSONArray();

          for (int i = 0; i < jsonArrayData.length(); i++) {
            JSONObject jsonObject = jsonArrayData.getJSONObject(i);

            if (jsonObject.get("rulesCompliance") != null) {
              byte[] data = (byte[]) jsonObject.get("rulesCompliance");
              String result = getCVSS(data);
              jsonObject.put("rulesCompliance", result);
            }
            uiJSONArray.put(jsonObject);
          }

          System.out.println(
              "UI JSON Array Bar Chart Data Machine Data by Content Id ::" + uiJSONArray);
          sendJSONResponse(response, uiJSONArray.toString());
          forward = mapping.findForward("view");
        }

      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    protected void sendJSONResponse(HttpServletResponse response, String jsonData)
        throws Exception {
      PrintWriter out = response.getWriter();
      out.println(jsonData);
      out.flush();
    }

    private String getCVSS(byte[] jsonBytes) {

      StringBuilder sb = new StringBuilder();

      int num;

      String cvss = "0/0";

      if (jsonBytes == null) {
        return cvss;
      }

      ByteArrayInputStream bInput = new ByteArrayInputStream(jsonBytes);

      while ((num = bInput.read()) != -1) {

        sb.append((char) num);

      }

      int total = 0;

      int passCount = 0;

      try {

        JSONObject jsonObject = new JSONObject(sb.toString());

        if (jsonObject.length() == 0) {
          return cvss;
        }

        JSONObject jsonObject1 = jsonObject.getJSONObject("rules_compliance");

        Iterator iterator = jsonObject1.keys();

        while (iterator.hasNext()) {

          total++;

          String key = (String) iterator.next();

          if ("pass".equalsIgnoreCase(jsonObject1.getString(key))) {

            passCount++;

          }

        }

      } catch (Exception e) {

        e.printStackTrace();

      }

      return String.valueOf(passCount) + "/" + String.valueOf(total);

    }

  }
}
