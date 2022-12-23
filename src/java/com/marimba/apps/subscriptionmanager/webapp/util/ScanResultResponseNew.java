package com.marimba.apps.subscriptionmanager.webapp.util;

public class ScanResultResponseNew {

	private DataScanResults data;

	public DataScanResults getData() {
		return data;
	}

	public void setData(DataScanResults data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "ScanResultResponseNew [data=" + data + "]";
	}
}
