package com.marimba.apps.subscriptionmanager.webapp.forms;

public class ConfigDashboardViewForm extends AbstractForm{


    private String machinesCount = "0";
    private String last24hourCount = "0";
    private String machineWindowsCount = "0";
    private String machineLinuxCount = "0";
    private String machineMacCount = "0";

    private String configScanCount = "0";


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
}
