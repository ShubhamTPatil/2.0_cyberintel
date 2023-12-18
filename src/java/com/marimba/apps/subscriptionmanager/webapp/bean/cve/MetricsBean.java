package com.marimba.apps.subscriptionmanager.webapp.bean.cve;
import com.marimba.apps.subscriptionmanager.webapp.bean.cve.CVSSMetricsV2Bean;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.marimba.apps.subscriptionmanager.webapp.bean.cve.CVSSMetricsV3Bean;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MetricsBean {

  @JsonProperty("cvssMetricV2")
  private CVSSMetricsV2Bean[] cvssMetricV2;

  @JsonProperty("cvssMetricV31")
  private CVSSMetricsV3Bean[] cvssMetricV3;

  public CVSSMetricsV2Bean[] getCvssMetricV2() {
    return cvssMetricV2;
  }

  public void setCvssMetricV2(
      CVSSMetricsV2Bean[] cvssMetricV2) {
    this.cvssMetricV2 = cvssMetricV2;
  }

  public CVSSMetricsV3Bean[] getCvssMetricV3() {
    return cvssMetricV3;
  }

  public void setCvssMetricV3(
      CVSSMetricsV3Bean[] cvssMetricV3) {
    this.cvssMetricV3 = cvssMetricV3;
  }
}
