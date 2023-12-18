package com.marimba.apps.subscriptionmanager.webapp.bean.cve;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CVSSDataBean {

  @JsonProperty("baseScore")
  private String baseScore;

  @JsonProperty("baseSeverity")
  private String baseV3Severity;

  public String getBaseV3Severity() {
    return baseV3Severity;
  }

  public void setB1aseV3Severity(String baseV3Severity) {
    this.baseV3Severity = baseV3Severity;
  }

  public String getBaseScore() {
    return baseScore;
  }

  public void setBaseScore(String baseScore) {
    this.baseScore = baseScore;
  }
}
