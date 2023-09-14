package com.marimba.apps.subscriptionmanager.webapp.util.defensight;

public class MachineAnomalyCount {

  private String hostname;

  private int anomaliesCount;

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public int getAnomaliesCount() {
    return anomaliesCount;
  }

  public void setAnomaliesCount(int anomaliesCount) {
    this.anomaliesCount = anomaliesCount;
  }
}
