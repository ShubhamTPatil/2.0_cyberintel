package com.marimba.apps.subscriptionmanager.webapp.bean.cve;

import com.marimba.apps.subscriptionmanager.webapp.bean.cve.CPEMatchBean;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeBean {

  @JsonProperty("cpeMatch")
  private CPEMatchBean[] cpeMatch;

  public CPEMatchBean[] getCpeMatch() {
    return cpeMatch;
  }

  public void setCpeMatch(CPEMatchBean[] cpeMatch) {
    this.cpeMatch = cpeMatch;
  }
}
