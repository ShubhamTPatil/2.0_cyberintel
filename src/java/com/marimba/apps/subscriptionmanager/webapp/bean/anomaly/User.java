package com.marimba.apps.subscriptionmanager.webapp.bean.anomaly;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.marimba.apps.subscriptionmanager.webapp.bean.anomaly.Host;

@JsonInclude(JsonInclude.Include.NON_NULL)

@JsonIgnoreProperties(ignoreUnknown = true)
@Generated("jsonschema2pojo")
public class User {

  @JsonProperty("@timestamp")
  private String timestamp;


  @JsonProperty("host")
  private Host host;
  @JsonProperty("event")
  private Event event;

  @JsonProperty("predictions")
  private Boolean predictions;
  @JsonProperty("id")
  private String id;

  @JsonProperty("_ts")
  private Integer ts;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

  @JsonProperty("@timestamp")
  public String getTimestamp() {
    return timestamp;
  }

  @JsonProperty("@timestamp")
  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  @JsonProperty("host")
  public Host getHost() {
    return host;
  }

  @JsonProperty("host")
  public void setHost(Host host) {
    this.host = host;
  }

  @JsonProperty("event")
  public Event getEvent() {
    return event;
  }

  @JsonProperty("event")
  public void setEvent(Event event) {
    this.event = event;
  }


  @JsonProperty("predictions") //anomaly
  public Boolean getPredictions() {
    return predictions;
  }

  @JsonProperty("predictions")
  public void setPredictions(Boolean predictions) {
    this.predictions = predictions;
  }

  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }


  @JsonProperty("_ts")
  public Integer getTs() {
    return ts;
  }

  @JsonProperty("_ts")
  public void setTs(Integer ts) {
    this.ts = ts;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

}