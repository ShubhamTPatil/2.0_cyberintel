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
"computer_name",
"record_id",
"process",
"task",
"event_id",
"provider_guid",
"channel",
"api",
"event_data",
"opcode",
"provider_name"
})
@Generated("jsonschema2pojo")
public class Winlog {

@JsonProperty("computer_name")
private String computerName;
@JsonProperty("record_id")
private Integer recordId;
@JsonProperty("process")
private Process process;
@JsonProperty("task")
private String task;
@JsonProperty("event_id")
private Integer eventId;
@JsonProperty("provider_guid")
private String providerGuid;
@JsonProperty("channel")
private String channel;
@JsonProperty("api")
private String api;
@JsonProperty("event_data")
private EventData eventData;
@JsonProperty("opcode")
private String opcode;
@JsonProperty("provider_name")
private String providerName;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("computer_name")
public String getComputerName() {
return computerName;
}

@JsonProperty("computer_name")
public void setComputerName(String computerName) {
this.computerName = computerName;
}

@JsonProperty("record_id")
public Integer getRecordId() {
return recordId;
}

@JsonProperty("record_id")
public void setRecordId(Integer recordId) {
this.recordId = recordId;
}

@JsonProperty("process")
public Process getProcess() {
return process;
}

@JsonProperty("process")
public void setProcess(Process process) {
this.process = process;
}

@JsonProperty("task")
public String getTask() {
return task;
}

@JsonProperty("task")
public void setTask(String task) {
this.task = task;
}

@JsonProperty("event_id")
public Integer getEventId() {
return eventId;
}

@JsonProperty("event_id")
public void setEventId(Integer eventId) {
this.eventId = eventId;
}

@JsonProperty("provider_guid")
public String getProviderGuid() {
return providerGuid;
}

@JsonProperty("provider_guid")
public void setProviderGuid(String providerGuid) {
this.providerGuid = providerGuid;
}

@JsonProperty("channel")
public String getChannel() {
return channel;
}

@JsonProperty("channel")
public void setChannel(String channel) {
this.channel = channel;
}

@JsonProperty("api")
public String getApi() {
return api;
}

@JsonProperty("api")
public void setApi(String api) {
this.api = api;
}

@JsonProperty("event_data")
public EventData getEventData() {
return eventData;
}

@JsonProperty("event_data")
public void setEventData(EventData eventData) {
this.eventData = eventData;
}

@JsonProperty("opcode")
public String getOpcode() {
return opcode;
}

@JsonProperty("opcode")
public void setOpcode(String opcode) {
this.opcode = opcode;
}

@JsonProperty("provider_name")
public String getProviderName() {
return providerName;
}

@JsonProperty("provider_name")
public void setProviderName(String providerName) {
this.providerName = providerName;
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
sb.append(Winlog.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
sb.append("computerName");
sb.append('=');
sb.append(((this.computerName == null)?"<null>":this.computerName));
sb.append(',');
sb.append("recordId");
sb.append('=');
sb.append(((this.recordId == null)?"<null>":this.recordId));
sb.append(',');
sb.append("process");
sb.append('=');
sb.append(((this.process == null)?"<null>":this.process));
sb.append(',');
sb.append("task");
sb.append('=');
sb.append(((this.task == null)?"<null>":this.task));
sb.append(',');
sb.append("eventId");
sb.append('=');
sb.append(((this.eventId == null)?"<null>":this.eventId));
sb.append(',');
sb.append("providerGuid");
sb.append('=');
sb.append(((this.providerGuid == null)?"<null>":this.providerGuid));
sb.append(',');
sb.append("channel");
sb.append('=');
sb.append(((this.channel == null)?"<null>":this.channel));
sb.append(',');
sb.append("api");
sb.append('=');
sb.append(((this.api == null)?"<null>":this.api));
sb.append(',');
sb.append("eventData");
sb.append('=');
sb.append(((this.eventData == null)?"<null>":this.eventData));
sb.append(',');
sb.append("opcode");
sb.append('=');
sb.append(((this.opcode == null)?"<null>":this.opcode));
sb.append(',');
sb.append("providerName");
sb.append('=');
sb.append(((this.providerName == null)?"<null>":this.providerName));
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