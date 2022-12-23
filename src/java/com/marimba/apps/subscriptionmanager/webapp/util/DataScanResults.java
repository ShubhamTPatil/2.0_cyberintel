package com.marimba.apps.subscriptionmanager.webapp.util;

import java.util.Set;

public class DataScanResults {

	private Metadata metadata;

	private Set<MachineScanResults> machineList = null;
	
	private Set<MachineScanResults> machineList2 = null;

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public Set<MachineScanResults> getMachineList() {
		return machineList;
	}

	public void setMachineList(Set<MachineScanResults> machineList) {
		this.machineList = machineList;
	}
	
	public Set<MachineScanResults> getMachineList2() {
		return machineList2;
	}

	public void setMachineList2(Set<MachineScanResults> machineList2) {
		this.machineList2 = machineList2;
	}

	@Override
	public String toString() {
		return "DataScanResults [metadata=" + metadata + ", machineList=" + machineList + ", machineList2=" + machineList2 + "]";
	}
}
