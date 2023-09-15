package com.marimba.apps.subscriptionmanager.webapp.util.defensight;

public class AnomalyConstants {

  public static String CONTAINER_NAME = "defensight.cosmoscontainer";
  public static String DATABASE_NAME = "defensight.cosmos.db";
  public static String HOST = "defensight.cosmos.host";
  public static String KEY = "defensight.cosmos.key";

  public static String HEAT_MAP_DATA = "heatmapData";

  public static String PARTITION_KEY = "id";

  public static class Queries {

    public static String SCATTER_CHART_QUERY =
        "SELECT c.host.hostname as hostname, c.predictions as anomaly, c.event.created as time FROM c  WHERE c.event.created <= @ctime AND c.event.created >= @ptime AND c.host.hostname != null AND c.predictions != null AND c.event.created != null";
    public static String FETCH_HOSTNAMEANDPREDICTIONS =
        "select count(1) as anomaliesCount, c.host.hostname as hostname from c where c.host.hostname != null AND c.predictions in ('Not_Trained','true') group by c.host.hostname";
    public static String MACHINE_LEVEL_ANOMALY_PIECHART =
        "select count (1) as anomalies, c.host.hostname as hostname,c.winlog.event_id as event_id from c  WHERE c.event.created <= @ctime AND c.event.created >= @ptime AND c.host.hostname=@hostname AND c.predictions != null AND c.winlog.event_id != null group by c.host.hostname, c.winlog.event_id";
    public static String MACHINE_NAME_LIST =
        "select distinct c.host.hostname as hostname from c where c.host.hostname != null";
  }
}
