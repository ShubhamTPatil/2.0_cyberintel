package com.marimba.apps.subscriptionmanager.webapp.bean.cve;

import com.marimba.apps.subscriptionmanager.webapp.bean.cve.DescriptionBean;
import com.marimba.apps.subscriptionmanager.webapp.bean.cve.MetricsBean;
import com.marimba.apps.subscriptionmanager.webapp.bean.cve.ConfigurationBean;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CVEBean {

  @JsonProperty("id")
  private String id;

  @JsonProperty("published")
  private String publishDate;

  @JsonProperty("lastModified")
  private String lastModifyDate;

  @JsonProperty("descriptions")
  private DescriptionBean[] description;

  @JsonProperty("metrics")
  private MetricsBean metrics;

  @JsonProperty("configurations")
  private ConfigurationBean[] configuration;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPublishDate() {
    return publishDate;
  }

  public void setPublishDate(String publishDate) {
    this.publishDate = publishDate;
  }

  public String getLastModifyDate() {
    return lastModifyDate;
  }

  public void setLastModifyDate(String lastModifyDate) {
    this.lastModifyDate = lastModifyDate;
  }

  public DescriptionBean[] getDescription() {
    return description;
  }

  public void setDescription(
      DescriptionBean[] description) {
    this.description = description;
  }

  public MetricsBean getMetrics() {
    return metrics;
  }

  public void setMetrics(MetricsBean metrics) {
    this.metrics = metrics;
  }

  public ConfigurationBean[] getConfiguration() {
    return configuration;
  }

  public void setConfiguration(
      ConfigurationBean[] configuration) {
    this.configuration = configuration;
  }
}
