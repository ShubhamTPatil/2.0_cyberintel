package com.marimba.apps.subscriptionmanager.webapp.util;

public class Machine {

	private String machineName;

	private String machineLastScan;

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
		return "Machine [machineName=" + machineName + ", machineLastScan=" + machineLastScan + "]";
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Machine)) {
			return false;
		}
		return machineName.equals(((Machine) other).machineName);
	}

	@Override
	public int hashCode() {
		return machineName.hashCode();
	}
}