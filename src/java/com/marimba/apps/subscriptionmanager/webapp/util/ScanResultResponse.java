package com.marimba.apps.subscriptionmanager.webapp.util;

public class ScanResultResponse {

	private Data data;

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "ScanResultResponse [data=" + data + "]";
	}
}
