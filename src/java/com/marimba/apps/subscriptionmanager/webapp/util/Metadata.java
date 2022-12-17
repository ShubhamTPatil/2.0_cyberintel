package com.marimba.apps.subscriptionmanager.webapp.util;

public class Metadata {


	private String id;

	private String uri;

	private String type;
	
	@Override
	public String toString() {
		return "Metadata [id=" + id + ", uri=" + uri + ", type=" + type + "]";
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}