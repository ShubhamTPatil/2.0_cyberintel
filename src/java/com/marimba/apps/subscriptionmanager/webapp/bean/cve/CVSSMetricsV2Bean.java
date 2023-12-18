package com.marimba.apps.subscriptionmanager.webapp.bean.cve;

import com.marimba.apps.subscriptionmanager.webapp.bean.cve.CVSSDataBean;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CVSSMetricsV2Bean {

  @JsonProperty("source")
  private String v2Source;
  @JsonProperty("cvssData")
  private CVSSDataBean cvssData;

  @JsonProperty("baseSeverity")
  private String severity;

  public String getSeverity() {
    return severity;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  public CVSSDataBean getCvssData() {
    return cvssData;
  }

  public void setCvssData(CVSSDataBean cvssData) {
    this.cvssData = cvssData;
  }

  public String getV2Source() {
    return v2Source;
  }

  public void setV2Source(String v2Source) {
    this.v2Source = v2Source;
  }
}
