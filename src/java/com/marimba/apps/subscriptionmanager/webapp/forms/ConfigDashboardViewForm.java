package com.marimba.apps.subscriptionmanager.webapp.forms;

import com.marimba.apps.securitymgr.utils.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigDashboardViewForm extends AbstractForm{


    private String machinesCount = "0";
    private String last24hourCount = "0";
    private String machineWindowsCount = "0";
    private String machineLinuxCount = "0";
    private String machineMacCount = "0";

    private String configScanCount = "0";

    Map<String, String> configProfileDropdown = new LinkedHashMap<String, String>();

    private String configProfileCompliant = "0";

    private String configProfileNonCompliant = "0";


    private String barChartData;

    private String lineChartData;

    public String getMachinesCount() {
        return machinesCount;
    }

    public void setMachinesCount(String machinesCount) {
        this.machinesCount = machinesCount;
    }

    public String getLast24hourCount() {
        return last24hourCount;
    }

    public void setLast24hourCount(String last24hourCount) {
        this.last24hourCount = last24hourCount;
    }

    public String getMachineWindowsCount() {
        return machineWindowsCount;
    }

    public void setMachineWindowsCount(String machineWindowsCount) {
        this.machineWindowsCount = machineWindowsCount;
    }

    public String getMachineLinuxCount() {
        return machineLinuxCount;
    }

    public void setMachineLinuxCount(String machineLinuxCount) {
        this.machineLinuxCount = machineLinuxCount;
    }

    public String getMachineMacCount() {
        return machineMacCount;
    }

    public void setMachineMacCount(String machineMacCount) {
        this.machineMacCount = machineMacCount;
    }

    public String getConfigScanCount() {
        return configScanCount;
    }

    public void setConfigScanCount(String configScanCount) {
        this.configScanCount = configScanCount;
    }

    public Map<String, String> getConfigProfileDropdown() {
        return configProfileDropdown;
    }

    public void setConfigProfileDropdown(Map<String, String> configProfileDropdown) {
        this.configProfileDropdown = configProfileDropdown;
    }

    public String getConfigProfileCompliant() {
        return configProfileCompliant;
    }

    public void setConfigProfileCompliant(String configProfileCompliant) {
        this.configProfileCompliant = configProfileCompliant;
    }

    public String getConfigProfileNonCompliant() {
        return configProfileNonCompliant;
    }

    public void setConfigProfileNonCompliant(String configProfileNonCompliant) {
        this.configProfileNonCompliant = configProfileNonCompliant;
    }

    public String getBarChartData() {
        return barChartData;
    }

    public void setBarChartData(String barChartData) {
        this.barChartData = barChartData;
    }

    public String getLineChartData() {
        return lineChartData;
    }

    public void setLineChartData(String lineChartData) {
        this.lineChartData = lineChartData;
    }
}
