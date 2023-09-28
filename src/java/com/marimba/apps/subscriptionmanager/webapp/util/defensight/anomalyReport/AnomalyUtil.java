package com.marimba.apps.subscriptionmanager.webapp.util.defensight.anomalyReport;import com.azure.cosmos.CosmosClient;import com.azure.cosmos.CosmosClientBuilder;import com.azure.cosmos.CosmosContainer;import com.azure.cosmos.CosmosDatabase;import com.azure.cosmos.util.CosmosPagedIterable;import com.fasterxml.jackson.databind.ObjectMapper;import com.marimba.apps.securitymgr.utils.json.JSONObject;import com.marimba.apps.subscriptionmanager.webapp.bean.anomaly.MachineLevelAnomaly;import com.marimba.apps.subscriptionmanager.webapp.bean.anomaly.MachineNameList;import com.marimba.apps.subscriptionmanager.webapp.bean.anomaly.TopLevelStatsChartData;import com.marimba.apps.subscriptionmanager.webapp.forms.AnomalyReportForm;import com.marimba.intf.util.IConfig;import com.marimba.tools.util.DebugFlag;import java.text.SimpleDateFormat;import java.time.Clock;import java.time.OffsetDateTime;import java.util.*;import java.util.concurrent.CompletableFuture;public class AnomalyUtil {  public static final int DEBUG = DebugFlag.getDebug("DEFENSIGHT/ANOMALY");  private TopLevelStatsUtil topLevelStatsUtil = new TopLevelStatsUtil();  private MachineLevelAnomalyUtil machineLevelAnomalyUtil = new MachineLevelAnomalyUtil();  static CosmosContainer container = null;  /**   * Runnable to get run the getCosmosConnection method. Will get called from the DefenSight   * dashboard action NewDashboardAction.java To keep the connection ready before landing on   * analytics page   *   * @param tunerConfig   * @return Runnable   */  public static Runnable getCosmosConectionRunnable(IConfig tunerConfig) {    return () -> AnomalyUtil.getCosmosConnection(tunerConfig);  }  /**   * Make the connection with Azure Cosmos DB.   *   * @param config tunerConfig   */  static synchronized void getCosmosConnection(IConfig config) {    String containerName = config.getProperty(AnomalyConstants.CONTAINER_NAME);    String databaseName = config.getProperty(AnomalyConstants.DATABASE_NAME);    String host = config.getProperty(AnomalyConstants.HOST);    String key = config.getProperty(AnomalyConstants.KEY);    if (container == null) {      try {        CosmosClient client = new CosmosClientBuilder().endpoint(host).key(key).buildClient();        debugInfo("getCosmosConnection(): connection established successfully ");        CosmosDatabase database = client.getDatabase(databaseName);        container = database.getContainer(containerName);      } catch (Exception e) {        debugInfo("getCosmosConnection(): connection failed " + e.getMessage());        e.printStackTrace();      }    }  }  public List<OffsetDateTime> getCurrentAndPrevTime(int minutes) {    OffsetDateTime currentTime = OffsetDateTime.now(Clock.systemUTC()).minusMinutes(1);    OffsetDateTime prevTime = currentTime.minusMinutes(minutes);    List<OffsetDateTime> list = new ArrayList<>();    list.add(currentTime);    list.add(prevTime);    return list;  }  public void setFormData(AnomalyReportForm anomalyReportForm, IConfig tunerConfig) {    List<OffsetDateTime> timeLimit = getCurrentAndPrevTime(2);    CosmosPagedIterable<MachineNameList> machineList = machineLevelAnomalyUtil.fetchMachineNameList(        tunerConfig, timeLimit.get(1));    if (!machineList.iterator().hasNext()) {      return;    }    // Define two functions to be executed sequentially    CompletableFuture<List<TopLevelStatsChartData>> topLevelStatsFuture = CompletableFuture.supplyAsync(        () -> topLevelStatsUtil.populateTopLevelStatsData(tunerConfig, machineList,            timeLimit.get(0), timeLimit.get(1)));    CompletableFuture<CosmosPagedIterable<MachineLevelAnomaly>> machineLevelAnomalyFuture = CompletableFuture.supplyAsync(        () -> machineLevelAnomalyUtil.populateMachineLevelAnomaly(tunerConfig,            machineList.stream().iterator().next().getHostname(), timeLimit.get(0),            timeLimit.get(1)));    CompletableFuture<CosmosPagedIterable<MachineNameList>> machineListFuture = CompletableFuture.supplyAsync(        () -> machineLevelAnomalyUtil.fetchMachineNameList(tunerConfig,            getCurrentAndPrevTime(720).get(1)));    // Combine the results of the two functions    CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(topLevelStatsFuture,        machineLevelAnomalyFuture, machineListFuture);    // Wait for both functions to complete    combinedFuture.join();    // You can now access the results of the functions    List<TopLevelStatsChartData> topLevelStatsDataList = topLevelStatsFuture.join();    CosmosPagedIterable<MachineLevelAnomaly> machineLevelAnomalyList = machineLevelAnomalyFuture.join();    CosmosPagedIterable<MachineNameList> machineNameList = machineListFuture.join();    anomalyReportForm.setTopLevelStatsCurrentTime(timeLimit.get(0).toString());    anomalyReportForm.setTopLevelStatsPrevTime(timeLimit.get(1).toString());    anomalyReportForm.setTopLevelStats(getStringFromList(topLevelStatsDataList));    anomalyReportForm.setMachineLevelAnomaly(getStringFromList(machineLevelAnomalyList));    anomalyReportForm.setMachineNameList(getStringFromList(machineNameList));  }  public static String getStringFromList(Object list) {    String str = "[]";    try {      ObjectMapper mapper = new ObjectMapper();      str = mapper.writeValueAsString(list);    } catch (Exception e) {      e.printStackTrace();    }    return str;  }  public JSONObject getTopLevelStatsJson(IConfig tunerConfig, String intervalMinutes) {    List<OffsetDateTime> timeLimit = getCurrentAndPrevTime(Integer.parseInt(intervalMinutes));    CosmosPagedIterable<MachineNameList> machineList = machineLevelAnomalyUtil.fetchMachineNameList(        tunerConfig, timeLimit.get(1));    JSONObject jsonObject = new JSONObject();    jsonObject.put("startTime", timeLimit.get(0).toString());    jsonObject.put("prevTime", timeLimit.get(1).toString());    jsonObject.put("data", getStringFromList(        topLevelStatsUtil.populateTopLevelStatsData(tunerConfig, machineList, timeLimit.get(0),            timeLimit.get(1))));    return jsonObject;  }  public JSONObject getMachineLevelAnomalyJson(IConfig tunerConfig, String intervalMinutes,      String hostname) {    List<OffsetDateTime> timeLimit = getCurrentAndPrevTime(Integer.parseInt(intervalMinutes));    JSONObject jsonObject = new JSONObject();    jsonObject.put("startTime", timeLimit.get(0).toString());    jsonObject.put("prevTime", timeLimit.get(1).toString());    jsonObject.put("data", getStringFromList(        machineLevelAnomalyUtil.populateMachineLevelAnomaly(tunerConfig, hostname, timeLimit.get(0),            timeLimit.get(1))));    return jsonObject;  }  static void debugInfo(String msg) {    if (DEBUG >= 5) {      System.out.println(          new SimpleDateFormat("MMM dd, yyyy hh:mm:ss").format(new Date(System.currentTimeMillis()))              + "::DEBUG:AnomalyUtil : " + msg);    }  }  private void log(String msg) {    if (DEBUG >= 3) {      System.out.println(          new SimpleDateFormat("MMM dd, yyyy hh:mm:ss").format(new Date(System.currentTimeMillis()))              + "::INFO:AnomalyUtil : " + msg);    }  }}