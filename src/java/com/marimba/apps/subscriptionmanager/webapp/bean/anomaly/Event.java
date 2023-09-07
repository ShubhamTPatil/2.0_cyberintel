package com.marimba.apps.subscriptionmanager.webapp.bean.anomaly;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"code",
"provider",
"kind",
"created"
})
@Generated("jsonschema2pojo")
public class Event {

@JsonProperty("code")
private Integer code;
@JsonProperty("provider")
private String provider;
@JsonProperty("kind")
private String kind;
@JsonProperty("created")
private String created;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("code")
public Integer getCode() {
return code;
}

@JsonProperty("code")
public void setCode(Integer code) {
this.code = code;
}

@JsonProperty("provider")
public String getProvider() {
return provider;
}

@JsonProperty("provider")
public void setProvider(String provider) {
this.provider = provider;
}

@JsonProperty("kind")
public String getKind() {
return kind;
}

@JsonProperty("kind")
public void setKind(String kind) {
this.kind = kind;
}

@JsonProperty("created")
public String getCreated() {
return created;
}

@JsonProperty("created")
public void setCreated(String created) {
this.created = created;
}

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

@Override
public String toString() {
StringBuilder sb = new StringBuilder();
sb.append(Event.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
sb.append("code");
sb.append('=');
sb.append(((this.code == null)?"<null>":this.code));
sb.append(',');
sb.append("provider");
sb.append('=');
sb.append(((this.provider == null)?"<null>":this.provider));
sb.append(',');
sb.append("kind");
sb.append('=');
sb.append(((this.kind == null)?"<null>":this.kind));
sb.append(',');
sb.append("created");
sb.append('=');
sb.append(((this.created == null)?"<null>":this.created));
sb.append(',');
sb.append("additionalProperties");
sb.append('=');
sb.append(((this.additionalProperties == null)?"<null>":this.additionalProperties));
sb.append(',');
if (sb.charAt((sb.length()- 1)) == ',') {
sb.setCharAt((sb.length()- 1), ']');
} else {
sb.append(']');
}
return sb.toString();
}

}