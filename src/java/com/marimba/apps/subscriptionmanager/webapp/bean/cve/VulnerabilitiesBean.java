package com.marimba.apps.subscriptionmanager.webapp.bean.cve;

import com.marimba.apps.subscriptionmanager.webapp.bean.cve.CVEBean;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public class VulnerabilitiesBean {

  @JsonProperty("cve")
  private CVEBean cveBean;

  public CVEBean getCveBean() {
    return cveBean;
  }

  public void setCveBean(CVEBean cveBean) {
    this.cveBean = cveBean;
  }
}
