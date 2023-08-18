package com.marimba.apps.subscriptionmanager.webapp.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CVEFormBean {

  @JsonProperty("id")
  private String cveId;
  @JsonProperty("cwe")
  private String cwe;

  @JsonProperty("cvss")
  private String cvss;

  @JsonProperty("summary")
  private String summary;

  @JsonProperty("Modified")
  private String modified;

  @JsonProperty("Published")
  private String published;

  @JsonProperty("vulnerable_product")
  private List<String> vulnerableProducts;

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getCwe() {
    return cwe;
  }

  public void setCwe(String cwe) {
    this.cwe = cwe;
  }

  public String getCveId() {
    return cveId;
  }

  public void setCveId(String cveId) {
    this.cveId = cveId;
  }

  public String getCvss() {
    return cvss;
  }

  public void setCvss(String cvss) {
    this.cvss = cvss;
  }

  public String getModified() {
    return modified;
  }

  public void setModified(String modified) {
    this.modified = modified;
  }

  public String getPublished() {
    return published;
  }

  public void setPublished(String published) {
    this.published = published;
  }

  public List<String> getVulnerableProducts() {
    return vulnerableProducts;
  }

  public void setVulnerableProducts(List<String> vulnerableProducts) {
    this.vulnerableProducts = vulnerableProducts;
  }
}

