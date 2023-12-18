package com.marimba.apps.subscriptionmanager.webapp.bean.cve;

import com.marimba.apps.subscriptionmanager.webapp.bean.cve.NodeBean;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigurationBean {

  @JsonProperty("nodes")
  private NodeBean[] nodes;

  public NodeBean[] getNodes() {
    return nodes;
  }

  public void setNodes(NodeBean[] nodes) {
    this.nodes = nodes;
  }
}
