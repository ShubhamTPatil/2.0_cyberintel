// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.// Confidential and Proprietary Information of Marimba, Inc.// Protected by or for use under one or more of the following patents:// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,// and 6,430,608. Other Patents Pending.package com.marimba.apps.subscriptionmanager.webapp.actions;import com.azure.cosmos.util.CosmosPagedIterable;import com.fasterxml.jackson.databind.ObjectMapper;import com.marimba.apps.securitymgr.utils.json.JSONObject;import com.marimba.apps.subscriptionmanager.webapp.util.defensight.MachineNameList;import com.marimba.apps.subscriptionmanager.webapp.util.defensight.TopLevelStatsChartData;import com.marimba.apps.subscriptionmanager.webapp.util.defensight.MachineLevelAnomalyRatio;import com.marimba.apps.subscriptionmanager.webapp.util.defensight.AnomalyUtil;import com.marimba.apps.subscriptionmanager.SubscriptionMain;import com.marimba.apps.subscriptionmanager.TenantHelper;import com.marimba.intf.util.IDirectory;import com.marimba.tools.util.DebugFlag;import com.marimba.apps.subscriptionmanager.webapp.bean.anomaly.pojo.AnomalyReportForm;import java.io.IOException;import java.io.PrintWriter;import com.marimba.intf.util.IConfig;import java.time.OffsetDateTime;import java.util.List;import java.util.concurrent.CompletableFuture;import javax.servlet.ServletException;import javax.servlet.http.HttpServletRequest;import javax.servlet.http.HttpServletResponse;import org.apache.struts.action.ActionForm;import org.apache.struts.action.ActionForward;import org.apache.struts.action.ActionMapping;/** * Set the context and forwads on to the target view page. If 'forward' request parameter is set, * * <p>the action will forward to that page or action. This action may switch to single select mode * * <p>depending on the caller specified by the 'src' request parameter. * * @author Bhavesh Pandya * @version 1.0, 09/04/2023 */public final class AnomalyAction extends AbstractAction {  /**   * REMIND   *   * @param mapping REMIND   * @param form REMIND   * @param request REMIND   * @param response REMIND   * @return REMIND   * @throws IOException REMIND   * @throws ServletException REMIND   */  public static final int DEBUG = DebugFlag.getDebug("DEFENSIGHT/ACTION");  public ActionForward execute(      ActionMapping mapping,      ActionForm form,      HttpServletRequest request,      HttpServletResponse response)      throws IOException, ServletException, Exception {    init(request);    SubscriptionMain main = null;    String reqTenantName = TenantHelper.getTenantName(request);    main = TenantHelper.getTenantSubMain(context, request.getSession(), reqTenantName);    IDirectory features = main.getFeatures();    IConfig tunerConfig = (IConfig) features.getChild("tunerConfig");    AnomalyUtil anomalyUtil = new AnomalyUtil();    AnomalyReportForm anomalyReportForm = (AnomalyReportForm) form;    String action = request.getParameter("action");    String intervalMinutes = request.getParameter("interval");    String hostname = request.getParameter("hostname");    String os = request.getParameter("os");    if (action == null) {      List<OffsetDateTime> timeLimit = anomalyUtil.getCurrentAndPrevTime(2);      anomalyReportForm.setTopLevelStatsCurrentTime(timeLimit.get(0).toString());      anomalyReportForm.setTopLevelStatsPrevTime(timeLimit.get(1).toString());      anomalyReportForm.setMachineLevelCurrentTime(timeLimit.get(0).toString());      anomalyReportForm.setMachineLevelPrevTime(timeLimit.get(1).toString());      // Define two functions to be executed sequentially      CompletableFuture<List<TopLevelStatsChartData>> future1 = CompletableFuture.supplyAsync(() -> anomalyUtil.populateTopLevelStatsData(tunerConfig, timeLimit.get(0), timeLimit.get(1)));      CompletableFuture<CosmosPagedIterable<MachineLevelAnomalyRatio>> future2 = CompletableFuture.supplyAsync(() -> anomalyUtil.populateMachineLevelAnomaly(tunerConfig, null, timeLimit.get(0), timeLimit.get(1)));      CompletableFuture<CosmosPagedIterable<MachineNameList>> future3 = CompletableFuture.supplyAsync(() -> anomalyUtil.fetchMachineNameList(tunerConfig, anomalyUtil.getCurrentAndPrevTime(720).get(1)));      // Combine the results of the two functions      CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(future1, future2, future3);      // Wait for both functions to complete      combinedFuture.join();      // You can now access the results of the functions      List<TopLevelStatsChartData> result1 = future1.join();      CosmosPagedIterable<MachineLevelAnomalyRatio> result2 = future2.join();      CosmosPagedIterable<MachineNameList> machineNameList = future3.join();      anomalyReportForm.setTopLevelStats(getStringFromList(result1));      anomalyReportForm.setMachineLevelAnomaly(getStringFromList(result2));      anomalyReportForm.setMachineNameList(getStringFromList(machineNameList));      /*      List<TopLevelStatsChartData> topLevelStatsData =          anomalyUtil.populateTopLevelStatsData(tunerConfig, 5);      String toplevelStats = getStringFromList(topLevelStatsData);      anomalyReportForm.setTopLevelStats(toplevelStats);      List<MachineNameList> machineNameListForSearch = anomalyUtil.fetchMachineName(tunerConfig);      String machineList = getStringFromList(machineNameListForSearch);      anomalyReportForm.setMachineNameList(machineList);      List<MachineLevelAnomalyRatio> machineLevelAnomalyRatioList =          anomalyUtil.populateMachinelevelAnomalyRatio(tunerConfig, 5);      String machineLevelAnomaly = getStringFromList(machineLevelAnomalyRatioList);      anomalyReportForm.setMachineLevelAnomalyPieChartData(machineLevelAnomaly);       */      System.out.println(anomalyReportForm.toString());      return (mapping.findForward("success"));    } else if(action.equals("topLevelStats")) {      List<OffsetDateTime> timeLimit = anomalyUtil.getCurrentAndPrevTime(Integer.parseInt(intervalMinutes));      JSONObject jsonObject = new JSONObject();      jsonObject.put("startTime", timeLimit.get(0).toString());      jsonObject.put("prevTime", timeLimit.get(1).toString());      jsonObject.put("data", getStringFromList(anomalyUtil.populateTopLevelStatsData(tunerConfig, timeLimit.get(0), timeLimit.get(1))));      sendJSONResponse(response, jsonObject.toString());    } else if(action.equals("machineLevelAnomaly")) {      List<OffsetDateTime> timeLimit = anomalyUtil.getCurrentAndPrevTime(Integer.parseInt(intervalMinutes));      JSONObject jsonObject = new JSONObject();      jsonObject.put("startTime", timeLimit.get(0).toString());      jsonObject.put("prevTime", timeLimit.get(1).toString());      jsonObject.put("data", getStringFromList(anomalyUtil.populateMachineLevelAnomaly(tunerConfig, hostname, timeLimit.get(0), timeLimit.get(1))));      sendJSONResponse(response, jsonObject.toString());    }    return (mapping.findForward("success"));  }  private String getStringFromList(Object list) {    String str = "[]";    try {      ObjectMapper mapper = new ObjectMapper();      str = mapper.writeValueAsString(list);    } catch (Exception e) {      e.printStackTrace();    }    return str;  }  protected void sendJSONResponse(HttpServletResponse response, String jsonData) throws Exception {    PrintWriter out = response.getWriter();    out.println(jsonData);    out.flush();  }  private void debugInfo(String msg) {    if (DEBUG >= 5) {      System.out.println("DEBUG:AnomalyAction : " + msg);    }  }  private void log(String msg) {    if (DEBUG >= 3) {      System.out.println("LogInfo:AnomalyAction : " + msg);    }  }}