package com.marimba.apps.subscriptionmanager.webapp.bean.anomaly.pojo;import com.marimba.apps.subscriptionmanager.webapp.forms.AbstractForm;public class AnomalyReportForm extends AbstractForm {  String topLevelStats = "[]";  String machineLevelAnomaly = "[]";  String machineNameList = "[]";  String topLevelStatsCurrentTime;  String topLevelStatsPrevTime;  String machineLevelCurrentTime;  String machineLevelPrevTime;  public String getTopLevelStats() {    return topLevelStats;  }  public void setTopLevelStats(String topLevelStats) {    this.topLevelStats = topLevelStats;  }  public String getMachineLevelAnomaly() {    return machineLevelAnomaly;  }  public void setMachineLevelAnomaly(String machineLevelAnomaly) {    this.machineLevelAnomaly = machineLevelAnomaly;  }  public String getMachineNameList() {    return machineNameList;  }  public void setMachineNameList(String machineNameList) {    this.machineNameList = machineNameList;  }  public String getTopLevelStatsCurrentTime() {    return topLevelStatsCurrentTime;  }  public void setTopLevelStatsCurrentTime(String topLevelStatsCurrentTime) {    this.topLevelStatsCurrentTime = topLevelStatsCurrentTime;  }  public String getTopLevelStatsPrevTime() {    return topLevelStatsPrevTime;  }  public void setTopLevelStatsPrevTime(String topLevelStatsPrevTime) {    this.topLevelStatsPrevTime = topLevelStatsPrevTime;  }  public String getMachineLevelCurrentTime() {    return machineLevelCurrentTime;  }  public void setMachineLevelCurrentTime(String machineLevelCurrentTime) {    this.machineLevelCurrentTime = machineLevelCurrentTime;  }  public String getMachineLevelPrevTime() {    return machineLevelPrevTime;  }  public void setMachineLevelPrevTime(String machineLevelPrevTime) {    this.machineLevelPrevTime = machineLevelPrevTime;  }  @Override  public String toString() {    return "AnomalyReportForm{" +            "topLevelStats='" + topLevelStats + '\'' +            ", \n machineLevelAnomaly='" + machineLevelAnomaly + '\'' +            ", \n machineNameList='" + machineNameList + '\'' +            ", \n topLevelStatsCurrentTime='" + topLevelStatsCurrentTime + '\'' +            ", \n topLevelStatsPrevTime='" + topLevelStatsPrevTime + '\'' +            ", \n machineLevelCurrentTime='" + machineLevelCurrentTime + '\'' +            ", \n machineLevelPrevTime='" + machineLevelPrevTime + '\'' +            '}';  }}