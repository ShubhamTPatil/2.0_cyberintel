package  com.marimba.apps.subscriptionmanager.webapp.bean.anomaly;

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
"build",
"kernel",
"name",
"family",
"version",
"platform"
})
@Generated("jsonschema2pojo")
public class Os {

@JsonProperty("build")
private String build;
@JsonProperty("kernel")
private String kernel;
@JsonProperty("name")
private String name;
@JsonProperty("family")
private String family;
@JsonProperty("version")
private String version;
@JsonProperty("platform")
private String platform;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("build")
public String getBuild() {
return build;
}

@JsonProperty("build")
public void setBuild(String build) {
this.build = build;
}

@JsonProperty("kernel")
public String getKernel() {
return kernel;
}

@JsonProperty("kernel")
public void setKernel(String kernel) {
this.kernel = kernel;
}

@JsonProperty("name")
public String getName() {
return name;
}

@JsonProperty("name")
public void setName(String name) {
this.name = name;
}

@JsonProperty("family")
public String getFamily() {
return family;
}

@JsonProperty("family")
public void setFamily(String family) {
this.family = family;
}

@JsonProperty("version")
public String getVersion() {
return version;
}

@JsonProperty("version")
public void setVersion(String version) {
this.version = version;
}

@JsonProperty("platform")
public String getPlatform() {
return platform;
}

@JsonProperty("platform")
public void setPlatform(String platform) {
this.platform = platform;
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
sb.append(Os.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
sb.append("build");
sb.append('=');
sb.append(((this.build == null)?"<null>":this.build));
sb.append(',');
sb.append("kernel");
sb.append('=');
sb.append(((this.kernel == null)?"<null>":this.kernel));
sb.append(',');
sb.append("name");
sb.append('=');
sb.append(((this.name == null)?"<null>":this.name));
sb.append(',');
sb.append("family");
sb.append('=');
sb.append(((this.family == null)?"<null>":this.family));
sb.append(',');
sb.append("version");
sb.append('=');
sb.append(((this.version == null)?"<null>":this.version));
sb.append(',');
sb.append("platform");
sb.append('=');
sb.append(((this.platform == null)?"<null>":this.platform));
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

