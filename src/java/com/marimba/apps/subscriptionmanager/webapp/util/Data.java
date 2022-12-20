package com.marimba.apps.subscriptionmanager.webapp.util;

import java.util.Set;

public class Data {

	private Metadata metadata;

	private String cVELastUpdated;

	private String vulDefLastUpdated;

	private String secDefLastUpdated;

	private Set<Machine> machineList = null;

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public String getcVELastUpdated() {
		return cVELastUpdated;
	}

	public void setcVELastUpdated(String cVELastUpdated) {
		this.cVELastUpdated = cVELastUpdated;
	}

	public String getVulDefLastUpdated() {
		return vulDefLastUpdated;
	}

	public void setVulDefLastUpdated(String vulDefLastUpdated) {
		this.vulDefLastUpdated = vulDefLastUpdated;
	}

	public String getSecDefLastUpdated() {
		return secDefLastUpdated;
	}

	public void setSecDefLastUpdated(String secDefLastUpdated) {
		this.secDefLastUpdated = secDefLastUpdated;
	}

	public Set<Machine> getMachineList() {
		return machineList;
	}

	public void setMachineList(Set<Machine> machineList) {
		this.machineList = machineList;
	}

	@Override
	public String toString() {
		return "Data [metadata=" + metadata + ", cVELastUpdated=" + cVELastUpdated + ", vulDefLastUpdated="
				+ vulDefLastUpdated + ", secDefLastUpdated=" + secDefLastUpdated + ", machineList=" + machineList + "]";
	}
}
