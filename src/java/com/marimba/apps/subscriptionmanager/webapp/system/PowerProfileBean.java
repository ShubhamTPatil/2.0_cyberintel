package com.marimba.apps.subscriptionmanager.webapp.system;

/**
 * Power Profile bean
 *
 * @author	Bharath M
 *
 */
public class PowerProfileBean {
    String profileName;
    String profileDescription;

    public PowerProfileBean (String profileName, String profileDescription) {
	this.profileName = profileName;
	this.profileDescription = profileDescription;
    }

    public String getName() {
	return profileName;
    }

    public String getDescription() {
	return profileDescription;
    }
}

