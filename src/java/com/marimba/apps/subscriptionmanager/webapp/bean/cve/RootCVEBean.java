package com.marimba.apps.subscriptionmanager.webapp.bean.cve;

import  com.marimba.apps.subscriptionmanager.webapp.bean.cve.VulnerabilitiesBean;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class RootCVEBean {

  @JsonProperty("resultsPerPage")
  private int resultPerPage;
  @JsonProperty("startIndex")
  private int startIndex;
  @JsonProperty("totalResults")
  private int totalResults;
  @JsonProperty("vulnerabilities")
  private List<VulnerabilitiesBean> vulnerabilities = new ArrayList<>();

  public int getResultPerPage() {
    return resultPerPage;
  }

  public void setResultPerPage(int resultPerPage) {
    this.resultPerPage = resultPerPage;
  }

  public int getStartIndex() {
    return startIndex;
  }

  public void setStartIndex(int startIndex) {
    this.startIndex = startIndex;
  }

  public int getTotalResults() {
    return totalResults;
  }

  public void setTotalResults(int totalResults) {
    this.totalResults = totalResults;
  }

  public List<VulnerabilitiesBean> getVulnerabilities() {
    return vulnerabilities;
  }

  public void setVulnerabilities(
      List<VulnerabilitiesBean> vulnerabilities) {
    this.vulnerabilities = vulnerabilities;
  }
}
