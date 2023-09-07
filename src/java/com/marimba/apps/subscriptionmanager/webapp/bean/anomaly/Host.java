package  com.marimba.apps.subscriptionmanager.webapp.bean.anomaly;

import java.util.LinkedHashMap;
import java.util.List;
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
"hostname",
"os",
"ip",
"name",
"id",
"mac",
"architecture"
})
@Generated("jsonschema2pojo")
public class Host {

@JsonProperty("hostname")
private String hostname;
@JsonProperty("os")
private Os os;
@JsonProperty("ip")
private List<String> ip;
@JsonProperty("name")
private String name;
@JsonProperty("id")
private String id;
@JsonProperty("mac")
private List<String> mac;
@JsonProperty("architecture")
private String architecture;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("hostname")
public String getHostname() {
return hostname;
}

@JsonProperty("hostname")
public void setHostname(String hostname) {
this.hostname = hostname;
}

@JsonProperty("os")
public Os getOs() {
return os;
}

@JsonProperty("os")
public void setOs(Os os) {
this.os = os;
}

@JsonProperty("ip")
public List<String> getIp() {
return ip;
}

@JsonProperty("ip")
public void setIp(List<String> ip) {
this.ip = ip;
}

@JsonProperty("name")
public String getName() {
return name;
}

@JsonProperty("name")
public void setName(String name) {
this.name = name;
}

@JsonProperty("id")
public String getId() {
return id;
}

@JsonProperty("id")
public void setId(String id) {
this.id = id;
}

@JsonProperty("mac")
public List<String> getMac() {
return mac;
}

@JsonProperty("mac")
public void setMac(List<String> mac) {
this.mac = mac;
}

@JsonProperty("architecture")
public String getArchitecture() {
return architecture;
}

@JsonProperty("architecture")
public void setArchitecture(String architecture) {
this.architecture = architecture;
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
sb.append(Host.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
sb.append("hostname");
sb.append('=');
sb.append(((this.hostname == null)?"<null>":this.hostname));
sb.append(',');
sb.append("os");
sb.append('=');
sb.append(((this.os == null)?"<null>":this.os));
sb.append(',');
sb.append("ip");
sb.append('=');
sb.append(((this.ip == null)?"<null>":this.ip));
sb.append(',');
sb.append("name");
sb.append('=');
sb.append(((this.name == null)?"<null>":this.name));
sb.append(',');
sb.append("id");
sb.append('=');
sb.append(((this.id == null)?"<null>":this.id));
sb.append(',');
sb.append("mac");
sb.append('=');
sb.append(((this.mac == null)?"<null>":this.mac));
sb.append(',');
sb.append("architecture");
sb.append('=');
sb.append(((this.architecture == null)?"<null>":this.architecture));
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