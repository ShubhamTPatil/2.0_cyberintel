package com.marimba.apps.subscriptionmanager.webapp.bean.cve;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.marimba.apps.subscriptionmanager.webapp.bean.cve.CVSSDataBean;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CVSSMetricsV3Bean {

  @JsonProperty("source")
  private String v3Source;

  @JsonProperty("cvssData")
  private CVSSDataBean cvssData;

  public CVSSDataBean getCvssData() {
    return cvssData;
  }

  public void setCvssData(CVSSDataBean cvssData) {
    this.cvssData = cvssData;
  }

  public String getV3Source() {
    return v3Source;
  }

  public void setV3Source(String v3Source) {
    this.v3Source = v3Source;
  }
}
