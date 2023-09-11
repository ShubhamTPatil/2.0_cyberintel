package com.marimba.apps.subscriptionmanager.webapp.util.defensight;

public class AnomalyConstants {

  public static String CONTAINER_NAME = "defensight.cosmoscontainer";
  public static String DATABASE_NAME = "defensight.cosmos.db";
  public static String HOST = "defensight.cosmos.host";
  public static String KEY = "defensight.cosmos.key";

  public static String HEAT_MAP_DATA = "heatmapData";

  public static String PARTITION_KEY = "id";

  public static class Queries {

    public static String SCATTER_CHART_QUERY = "SELECT c.host.hostname as hostname, c.predictions as anomaly, c.event.created as time FROM c  WHERE c.event.created <= @ctime AND c.event.created >= @ptime AND c.host.hostname != null AND c.predictions != null AND c.event.created != null";
  }

}
