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
"pid",
"thread"
})
@Generated("jsonschema2pojo")
public class Process {

@JsonProperty("pid")
private Integer pid;
@JsonProperty("thread")
private Thread thread;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("pid")
public Integer getPid() {
return pid;
}

@JsonProperty("pid")
public void setPid(Integer pid) {
this.pid = pid;
}

@JsonProperty("thread")
public Thread getThread() {
return thread;
}

@JsonProperty("thread")
public void setThread(Thread thread) {
this.thread = thread;
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
sb.append(Process.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
sb.append("pid");
sb.append('=');
sb.append(((this.pid == null)?"<null>":this.pid));
sb.append(',');
sb.append("thread");
sb.append('=');
sb.append(((this.thread == null)?"<null>":this.thread));
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
