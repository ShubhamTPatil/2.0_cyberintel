package com.marimba.apps.subscriptionmanager.webapp.system;

/**
 * OS Mapping CVE-IDs Bean
 *
 * @author	Nandakumar Sankaralingam
 *
 */
public class OsMapCveIdBean {
    String profileName;
    String profileDescription;
	String OS;
	String cveIds;
    String osIndex;

    public OsMapCveIdBean (String profileName, String profileDescription, String osIndex, String OS, String cveIds) {
	this.profileName = profileName;
	this.profileDescription = profileDescription;
    this.osIndex = osIndex;
    this.OS = OS;
    this.cveIds = cveIds;
    }

    public String getName() {
	return profileName;
    }

    public String getDescription() {
	return profileDescription;
    }

    public String getOsIndex() {
	return osIndex;
    }

    public String getOs() {
	return OS;
    }

    public String getCveids() {
	return cveIds;
    }

}


