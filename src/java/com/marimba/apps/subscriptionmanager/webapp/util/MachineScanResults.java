package com.marimba.apps.subscriptionmanager.webapp.util;

public class MachineScanResults {

	private String machineName;
	private String machineDomain;
	private String os;
	private String securityDefinition;
	private String profile;
	private String machineLastScan;
	private String scanStaus;

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
	
	public String getMachineDomain() {
		return machineDomain;
	}

	public void setMachineDomain(String machineDomain) {
		this.machineDomain = machineDomain;
	}
	
	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}
	
	public String getScanStaus() {
		return scanStaus;
	}

	public void setScanStaus(String scanStaus) {
		this.scanStaus = scanStaus;
	}
	
	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}
	
	public String getSecurityDefinition() {
		return securityDefinition;
	}

	public void setSecurityDefinition(String securityDefinition) {
		this.securityDefinition = securityDefinition;
	}

	public String getMachineLastScan() {
		return machineLastScan;
	}

	public void setMachineLastScan(String machineLastScan) {
		this.machineLastScan = machineLastScan;
	}

	@Override
	public String toString() {
		return "Machine [machineName=" + machineName + ",machineDomain=" + machineDomain + ",os=" + os + ",securityDefinition=" + securityDefinition + ",profile=" + profile + ",machineLastScan=" + machineLastScan + ",scanStaus=" + scanStaus +"]";
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof MachineScanResults)) {
			return false;
		}
		return machineName.equals(((MachineScanResults) other).machineName);
	}

	@Override
	public int hashCode() {
		return machineName.hashCode();
	}
}