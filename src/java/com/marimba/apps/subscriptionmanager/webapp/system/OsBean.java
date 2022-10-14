package com.marimba.apps.subscriptionmanager.webapp.system;

/**
 * OS Bean
 *
 * @author	Nandakumar Sankaralingam
 *
 */
public class OsBean {
    String osIdex;
    String osValue;

    public OsBean (String osIndex, String osValue) {
	this.osIdex = osIndex;
	this.osValue = osValue;
    }

    public String getOsIndex() {
	return osIdex;
    }

    public String getOsValue() {
	return osValue;
    }

}


