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
"StatusBufferLength",
"StatusBuffer",
"Adapter"
})
@Generated("jsonschema2pojo")
public class EventData {

@JsonProperty("StatusBufferLength")
private String statusBufferLength;
@JsonProperty("StatusBuffer")
private String statusBuffer;
@JsonProperty("Adapter")
private String adapter;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("StatusBufferLength")
public String getStatusBufferLength() {
return statusBufferLength;
}

@JsonProperty("StatusBufferLength")
public void setStatusBufferLength(String statusBufferLength) {
this.statusBufferLength = statusBufferLength;
}

@JsonProperty("StatusBuffer")
public String getStatusBuffer() {
return statusBuffer;
}

@JsonProperty("StatusBuffer")
public void setStatusBuffer(String statusBuffer) {
this.statusBuffer = statusBuffer;
}

@JsonProperty("Adapter")
public String getAdapter() {
return adapter;
}

@JsonProperty("Adapter")
public void setAdapter(String adapter) {
this.adapter = adapter;
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
sb.append(EventData.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
sb.append("statusBufferLength");
sb.append('=');
sb.append(((this.statusBufferLength == null)?"<null>":this.statusBufferLength));
sb.append(',');
sb.append("statusBuffer");
sb.append('=');
sb.append(((this.statusBuffer == null)?"<null>":this.statusBuffer));
sb.append(',');
sb.append("adapter");
sb.append('=');
sb.append(((this.adapter == null)?"<null>":this.adapter));
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