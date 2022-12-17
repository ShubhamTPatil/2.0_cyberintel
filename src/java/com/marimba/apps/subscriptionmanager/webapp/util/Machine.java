package com.marimba.apps.subscriptionmanager.webapp.util;

public class Machine {

	private String machineId;

	private String machineName;

	private String machineLastScan;

	public String getMachineId() {
		return machineId;
	}

	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getMachineLastScan() {
		return machineLastScan;
	}

	public void setMachineLastScan(String machineLastScan) {
		this.machineLastScan = machineLastScan;
	}

	@Override
	public String toString() {
		return "Machine [machineId=" + machineId + ", machineName=" + machineName + ", machineLastScan="
				+ machineLastScan + "]";
	}
}