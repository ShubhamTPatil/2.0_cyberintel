package com.marimba.apps.subscriptionmanager.webapp.util.defensight;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marimba.apps.subscriptionmanager.webapp.bean.anomaly.User;
import com.marimba.intf.util.IConfig;
import com.marimba.tools.util.DebugFlag;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AnomalyUtil {

  static IConfig config;
  public final static int DEBUG = DebugFlag.getDebug("DEFENSIGHT/ACTION");

  public IConfig getIConfig(IConfig config) {
    return this.config = config;
  }

  static CosmosDatabase database;
  static CosmosContainer container;

  public String fetchDataForChart(IConfig config, List<String> timeInterval) throws Exception {
    getCosmosConnection(config);
    String chartType = timeInterval.get(2);
    switch (chartType) {
      case "heatmapData":
        return populateScatterChartData(timeInterval.get(0), timeInterval.get(1));
      default:
    }
    return null;
  }

  public void getCosmosConnection(IConfig config) {
    String containerName = config.getProperty("defensight.cosmoscontainer");
    String databaseName = config.getProperty("defensight.cosmos.db");
    String host = config.getProperty("defensight.cosmos.host");
    String key = config.getProperty("defensight.cosmos.key");
    try {
      CosmosClient
          client = new CosmosClientBuilder()
          .endpoint(host)
          .key(key)
          .buildClient();
      log("getCosmosConnection():connection established successfully ");
      database = client.getDatabase(databaseName);
      container = database.getContainer(containerName);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String populateScatterChartData(String startTime, String endTime) throws Exception {
    CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
    options.setPartitionKey(new PartitionKey("id"));
    ArrayList<SqlParameter> paramList = new ArrayList<SqlParameter>();
    paramList.add(new SqlParameter("@ctime", startTime));
    paramList.add(new SqlParameter("@ptime", endTime));
    SqlQuerySpec querySpec = new SqlQuerySpec(
        "SELECT c.host.hostname as hostname, c.predictions as anomaly, c.event.created as time FROM c  WHERE c.event.created <= @ctime AND c.event.created >= @ptime",
        paramList);
    return executeQueryWithQuerySpecPrintSingleResult(querySpec);
  }

  private String executeQueryWithQuerySpecPrintSingleResult(SqlQuerySpec querySpec) {
    String userData = null;
    CosmosPagedIterable<User> filterUserRecords = container.queryItems(querySpec,
        new CosmosQueryRequestOptions(), User.class);

    List<User> users = filterUserRecords.stream().limit(5000)
        .collect(Collectors.toList());

    try {
      ObjectMapper mapper = new ObjectMapper();
      userData = mapper.writeValueAsString(users);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return userData;
  }

  public String populateBubbleChartData() {
    List<User> results = null;
    String newJsonData = null;

    String sql = "SELECT c.host.hostname as hostname, c.predictions as anomaly, c.event.created as time FROM c";
    CosmosQueryRequestOptions options = new CosmosQueryRequestOptions().setMaxBufferedItemCount(
        10000);

    Iterable<FeedResponse<User>> queryResponses = container.queryItems(sql, options, User.class)
        .iterableByPage(10000);

    for (FeedResponse<User> feedResponse : queryResponses) {
      results = feedResponse.getResults();
    }
    try {
      ObjectMapper mapper = new ObjectMapper();
      newJsonData = mapper.writeValueAsString(results);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return newJsonData;
  }

  private void debugInfo(String msg) {
    if (DEBUG >= 5) {
      System.out.println("DEBUG:AnomalyAction : " + msg);
    }
  }

  private void log(String msg) {
    if (DEBUG >= 3) {
      System.out.println("LogInfo:AnomalyAction : " + msg);
    }
  }
}
