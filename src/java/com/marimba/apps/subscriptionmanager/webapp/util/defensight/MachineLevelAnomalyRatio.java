package com.marimba.apps.subscriptionmanager.webapp.util.defensight;

public class MachineLevelAnomalyRatio {
  private String hostname;

  private int anomalies;
  String event_id;

  public MachineLevelAnomalyRatio() {}

  public MachineLevelAnomalyRatio(String hostname, int anomalies, String event_id) {
    this.hostname = hostname;
    this.anomalies = anomalies;
    this.event_id = event_id;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public int getAnomalies() {
    return anomalies;
  }

  public void setAnomalies(int anomalies) {
    this.anomalies = anomalies;
  }

  public String getEvent_id() {
    return event_id;
  }

  public void setEvent_id(String event_id) {
    this.event_id = event_id;
  }
}
