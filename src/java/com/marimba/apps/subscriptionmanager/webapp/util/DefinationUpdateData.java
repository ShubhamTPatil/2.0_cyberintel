package com.marimba.apps.subscriptionmanager.webapp.util;

public class DefinationUpdateData {

	private String cVELastUpdated;

	private String vulDefLastUpdated;

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

	@Override
	public String toString() {
		return "DefinationUpdateData [cVELastUpdated=" + cVELastUpdated + ", vulDefLastUpdated=" + vulDefLastUpdated
				+ "]";
	}
}
