package com.marimba.apps.subscriptionmanager.webapp.util.defensight;

import java.util.stream.Collectors;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscriptionmanager.webapp.actions.AnomalyAction;

import com.marimba.webapps.intf.GUIException;
import com.marimba.tools.util.DebugFlag;
import com.marimba.webapps.intf.SystemException;

//Cosmos Import
import com.marimba.apps.subscriptionmanager.webapp.bean.anomaly.User;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import java.util.List;
import com.marimba.intf.util.IConfig;
import com.azure.cosmos.models.FeedResponse;
import reactor.netty.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;

public class AnomalyUtil {
  static IConfig config;
  public final static int DEBUG = DebugFlag.getDebug("DEFENSIGHT/ACTION");
  public IConfig getIConfig(IConfig config) {
    return this.config = config;
  }
  static CosmosDatabase database;
  static CosmosContainer container;
  public String fetchDataForChart(IConfig config, String chartType) {
    getCosmosConnection(config);
    switch (chartType) {
      case "heatmapData":
        return populateBubbleChartData();
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
