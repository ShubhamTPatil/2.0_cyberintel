package com.marimba.apps.subscriptionmanager.webapp.bean.cve;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CPEMatchBean {

  @JsonProperty("criteria")
  private String cepCriteria;

  @JsonProperty("matchCriteriaId")
  private String matchCriteriaId;

  public String getCepCriteria() {
    return cepCriteria;
  }

  public void setCepCriteria(String cepCriteria) {
    this.cepCriteria = cepCriteria;
  }

  public String getMatchCriteriaId() {
    return matchCriteriaId;
  }

  public void setMatchCriteriaId(String matchCriteriaId) {
    this.matchCriteriaId = matchCriteriaId;
  }
}
